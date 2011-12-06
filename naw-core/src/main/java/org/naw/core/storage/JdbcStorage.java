package org.naw.core.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.naw.core.DefaultProcessFactory;
import org.naw.core.Process;
import org.naw.core.ProcessFactory;
import org.naw.core.ProcessState;
import org.naw.core.activity.AbstractActivity;
import org.naw.core.activity.Activity;
import org.naw.core.exchange.Message;
import org.naw.core.logging.Logger;
import org.naw.core.logging.LoggerFactory;
import org.naw.core.util.InactiveTimeout;
import org.naw.core.util.Timeout;
import org.naw.core.util.internal.JdbcUtils;
import org.naw.core.util.internal.ObjectUtils;

public class JdbcStorage implements Storage {

	private static final Logger log = LoggerFactory
			.getLogger(JdbcStorage.class);

	private final DataSource ds;

	private ProcessFactory processFactory;

	public JdbcStorage(DataSource ds) {
		this.ds = ds;
		this.processFactory = new DefaultProcessFactory();
	}

	public void setProcessFactory(ProcessFactory processFactory) {
		this.processFactory = processFactory;
	}

	private void persistMaster(Connection conn, String pid, Process proc)
			throws SQLException {

		Activity activity = proc.getActivity();
		String activityName;

		if (activity == null) {
			activityName = null;
		} else {
			activityName = activity.getName();
		}

		PreparedStatement pstmt = conn
				.prepareStatement("DELETE FROM naw_process WHERE pid = ?");
		try {
			pstmt.setString(1, pid);
			pstmt.executeUpdate();
		} finally {
			JdbcUtils.tryClose(pstmt);
		}

		pstmt = conn
				.prepareStatement("INSERT INTO naw_process (pid, context_name, state, activity_name) VALUES (?, ?, ?, ?)");
		try {
			pstmt.setString(1, pid);
			pstmt.setString(2, proc.getContextName());
			pstmt.setInt(3, proc.getState().codeValue());

			if (activityName == null) {
				pstmt.setNull(4, Types.VARCHAR);
			} else {
				pstmt.setString(4, activityName);
			}

			if (pstmt.executeUpdate() != 1) {
				throw new SQLException("insert operation return non one result");
			}
		} finally {
			JdbcUtils.tryClose(pstmt);
		}
	}

	private boolean loadMaster(Connection conn, String pid, Process proc)
			throws SQLException {

		PreparedStatement pstmt = conn
				.prepareStatement("SELECT context_name, state, activity_name FROM naw_process WHERE pid = ?");

		ResultSet rs = null;

		try {
			pstmt.setString(1, pid);

			rs = pstmt.executeQuery();
			if (!rs.next()) {
				return false;
			}

			ProcessState state = ProcessState.valueOf(rs.getInt(2));
			String activityName = rs.getString(3);

			Activity activity = activityName == null ? null
					: new AbstractActivity(activityName) {

						public void execute(Process process) throws Exception {
							// do nothing
						}
					};

			proc.setContextName(rs.getString(1));
			proc.noFireEventUpdate(state, activity);

			return true;
		} finally {
			JdbcUtils.tryClose(rs);
			JdbcUtils.tryClose(pstmt);
		}
	}

	private void persistMessage(Connection conn, String pid, Message message)
			throws SQLException {

		PreparedStatement pstmt = conn
				.prepareStatement("DELETE FROM naw_message WHERE pid = ?");
		try {
			pstmt.setString(1, pid);
			pstmt.executeUpdate();
		} finally {
			JdbcUtils.tryClose(pstmt);
		}

		pstmt = conn
				.prepareStatement("INSERT INTO naw_message (pid, var_name, key_name, key_value) VALUES (?, ?, ?, ?)");
		try {
			pstmt.setString(1, pid);

			for (String var : message.getVariables()) {
				Map<String, Object> values = message.get(var);

				if (values == null) {
					continue;
				}

				pstmt.setString(2, var);

				for (Map.Entry<String, Object> e : values.entrySet()) {
					pstmt.setString(3, e.getKey());

					byte[] bytes = ObjectUtils.toBytes(e.getValue());
					if (bytes == null) {
						pstmt.setNull(4, Types.BLOB);
					} else {
						pstmt.setBytes(4, bytes);
					}

					pstmt.addBatch();
				}
			}

			pstmt.executeBatch();
		} finally {
			JdbcUtils.tryClose(pstmt);
		}
	}

	private void loadMessage(Connection conn, String pid, Message message)
			throws SQLException {

		PreparedStatement pstmt = conn
				.prepareStatement("SELECT var_name, key_name, key_value FROM naw_message WHERE pid = ?");

		ResultSet rs = null;

		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();

		try {
			pstmt.setString(1, pid);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				String var = rs.getString(1);
				String key = rs.getString(2);

				Object value = ObjectUtils.fromBytes(rs.getBytes(3));
				if (value != null) {
					Map<String, Object> values = map.get(var);

					if (values == null) {
						values = new HashMap<String, Object>();
						map.put(var, values);
					}

					values.put(key, value);
				}
			}
		} finally {
			JdbcUtils.tryClose(rs);
			JdbcUtils.tryClose(pstmt);
		}

		for (Map.Entry<String, Map<String, Object>> e : map.entrySet()) {
			message.set(e.getKey(), e.getValue());
		}

		map.clear();
		map = null;
	}

	private void persistAttributes(Connection conn, String pid,
			Map<String, Object> attributes) throws SQLException {

		PreparedStatement pstmt = conn
				.prepareStatement("DELETE FROM naw_attributes WHERE pid = ?");
		try {
			pstmt.setString(1, pid);
			pstmt.executeUpdate();
		} finally {
			JdbcUtils.tryClose(pstmt);
		}

		pstmt = conn
				.prepareStatement("INSERT INTO naw_attributes (pid, attr_name, attr_value) VALUES (?, ?, ?)");
		try {
			pstmt.setString(1, pid);

			for (Map.Entry<String, Object> e : attributes.entrySet()) {
				pstmt.setString(2, e.getKey());

				byte[] bytes = ObjectUtils.toBytes(e.getValue());
				if (bytes == null) {
					pstmt.setNull(3, Types.BLOB);
				} else {
					pstmt.setBytes(3, bytes);
				}

				pstmt.addBatch();
			}

			pstmt.executeBatch();
		} finally {
			JdbcUtils.tryClose(pstmt);
		}
	}

	private void loadAttributes(Connection conn, String pid, Process proc)
			throws SQLException {

		PreparedStatement pstmt = conn
				.prepareStatement("SELECT attr_name, attr_value FROM naw_attributes WHERE pid = ?");

		ResultSet rs = null;

		try {
			pstmt.setString(1, pid);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				String key = rs.getString(1);
				Object value = ObjectUtils.fromBytes(rs.getBytes(2));

				if (value != null) {
					proc.setAttribute(key, value);
				}
			}
		} finally {
			JdbcUtils.tryClose(rs);
			JdbcUtils.tryClose(pstmt);
		}
	}

	private void persistTimeouts(Connection conn, String pid,
			List<Timeout> timeouts) throws SQLException {

		PreparedStatement pstmt = conn
				.prepareStatement("DELETE FROM naw_timeouts WHERE pid = ?");
		try {
			pstmt.setString(1, pid);
			pstmt.executeUpdate();
		} finally {
			JdbcUtils.tryClose(pstmt);
		}

		pstmt = conn
				.prepareStatement("INSERT INTO naw_timeouts (pid, activity_name, deadline) VALUES (?, ?, ?)");
		try {
			pstmt.setString(1, pid);

			for (int i = 0, len = timeouts.size(); i < len; ++i) {
				Timeout to = timeouts.get(i);

				pstmt.setString(2, to.getActivityName());
				pstmt.setLong(3, to.getDeadline());

				pstmt.addBatch();
			}

			pstmt.executeBatch();
		} finally {
			JdbcUtils.tryClose(pstmt);
		}
	}

	private void loadTimeouts(Connection conn, String pid, Process process)
			throws SQLException {

		PreparedStatement pstmt = conn
				.prepareStatement("SELECT activity_name, deadline FROM naw_timeouts WHERE pid = ?");

		ResultSet rs = null;

		try {
			pstmt.setString(1, pid);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				InactiveTimeout to = new InactiveTimeout();
				to.setProcessId(pid);
				to.setActivityName(rs.getString(1));
				to.setDeadline(rs.getLong(2));

				process.registerTimeout(to);
			}
		} finally {
			JdbcUtils.tryClose(rs);
			JdbcUtils.tryClose(pstmt);
		}
	}

	public boolean persist(Process process) {
		String pid = process.getId();

		Connection conn = JdbcUtils.tryOpen(ds, false, false, log);
		if (conn == null) {
			return false;
		}

		try {
			// persist master data
			persistMaster(conn, pid, process);

			// persist message
			Message message = process.getMessage();

			synchronized (message) {
				persistMessage(conn, pid, message);
			}

			// persist attributes
			persistAttributes(conn, pid, process.getAttributes());

			// persist timeouts
			persistTimeouts(conn, pid, process.getTimeouts());

			// commit changes
			conn.commit();
		} catch (Throwable t) {
			JdbcUtils.tryRollback(conn);

			log.error("unable to persist process " + process.getId(), t);
		} finally {
			JdbcUtils.tryClose(conn);
		}

		return false;
	}

	public void remove(Process process) {
		String pid = process.getId();

		Connection conn = JdbcUtils.tryOpen(ds, false, false, log);
		if (conn == null) {
			return;
		}

		PreparedStatement pstmt;

		try {
			// delete master data
			pstmt = conn
					.prepareStatement("DELETE FROM naw_process WHERE pid = ?");
			try {
				pstmt.setString(1, pid);
				pstmt.executeUpdate();
			} finally {
				JdbcUtils.tryClose(pstmt);
				pstmt = null;
			}

			// delete message
			pstmt = conn
					.prepareStatement("DELETE FROM naw_message WHERE pid = ?");
			try {
				pstmt.setString(1, pid);
				pstmt.executeUpdate();
			} finally {
				JdbcUtils.tryClose(pstmt);
				pstmt = null;
			}

			// delete attributes
			pstmt = conn
					.prepareStatement("DELETE FROM naw_attributes WHERE pid = ?");
			try {
				pstmt.setString(1, pid);
				pstmt.executeUpdate();
			} finally {
				JdbcUtils.tryClose(pstmt);
				pstmt = null;
			}

			// delete timeouts
			pstmt = conn
					.prepareStatement("DELETE FROM naw_timeouts WHERE pid = ?");
			try {
				pstmt.setString(1, pid);
				pstmt.executeUpdate();
			} finally {
				JdbcUtils.tryClose(pstmt);
				pstmt = null;
			}
			
			// commit changes
			conn.commit();
		} catch (Throwable t) {
			JdbcUtils.tryRollback(conn);

			log.error("unable to remove process " + pid, t);
		} finally {
			JdbcUtils.tryClose(conn);
		}
	}

	private Process find(Connection conn, String pid) {
		Process process = processFactory.newProcess(pid);

		try {
			// load master data
			loadMaster(conn, pid, process);

			// load message
			loadMessage(conn, pid, process.getMessage());

			// load attributes
			loadAttributes(conn, pid, process);

			// load timeouts
			loadTimeouts(conn, pid, process);
		} catch (Throwable t) {
			process = null;

			log.error("unable to find process " + pid, t);
		}

		return process;
	}

	public Process find(String pid) {
		Connection conn = JdbcUtils.tryOpen(ds, true, false, log);
		if (conn == null) {
			return null;
		}

		return find(conn, pid);
	}

	public Process[] findByProcessContext(String contextName) {
		Connection conn = JdbcUtils.tryOpen(ds, true, false, log);
		if (conn == null) {
			return null;
		}

		List<Process> list = new ArrayList<Process>();

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = conn
					.prepareStatement("SELECT pid FROM naw_process WHERE context_name = ?");

			pstmt.setString(1, contextName);

			Set<String> pids = new HashSet<String>();

			rs = pstmt.executeQuery();
			while (rs.next()) {
				pids.add(rs.getString(1));
			}

			JdbcUtils.tryClose(rs);
			rs = null;

			JdbcUtils.tryClose(pstmt);
			pstmt = null;

			for (String pid : pids) {
				Process proc = find(conn, pid);

				if (proc != null) {
					list.add(proc);
				}
			}
			
			// commit changes
			conn.commit();
		} catch (Throwable t) {
			JdbcUtils.tryRollback(conn);

			log.error("unable to find processes with context " + contextName, t);
		} finally {
			JdbcUtils.tryClose(rs);
			JdbcUtils.tryClose(pstmt);

			JdbcUtils.tryClose(conn);
		}

		return list.toArray(new Process[0]);
	}

	public Process[] findAll() {
		Connection conn = JdbcUtils.tryOpen(ds, true, false, log);
		if (conn == null) {
			return null;
		}

		List<Process> list = new ArrayList<Process>();

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();

			Set<String> pids = new HashSet<String>();

			rs = stmt.executeQuery("SELECT pid FROM naw_process");
			while (rs.next()) {
				pids.add(rs.getString(1));
			}

			JdbcUtils.tryClose(rs);
			rs = null;

			JdbcUtils.tryClose(stmt);
			stmt = null;

			for (String pid : pids) {
				Process proc = find(conn, pid);

				if (proc != null) {
					list.add(proc);
				}
			}
			
			// commit changes
			conn.commit();
		} catch (Throwable t) {
			JdbcUtils.tryRollback(conn);

			log.error("unable to find all processes", t);
		} finally {
			JdbcUtils.tryClose(rs);
			JdbcUtils.tryClose(stmt);

			JdbcUtils.tryClose(conn);
		}

		return list.toArray(new Process[0]);
	}

}
