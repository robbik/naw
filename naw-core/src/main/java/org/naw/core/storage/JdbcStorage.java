package org.naw.core.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			pstmt.setString(4, proc.getActivity().getName());

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
			Activity activity = new AbstractActivity(rs.getString(3)) {

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
					pstmt.setBytes(4, ObjectUtils.toBytes(e.getValue()));

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

				Map<String, Object> values = map.get(var);

				if (values == null) {
					values = new HashMap<String, Object>();
					map.put(var, values);
				}

				values.put(key, value);
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
				pstmt.setBytes(3, ObjectUtils.toBytes(e.getValue()));

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

				proc.setAttribute(key, value);
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
		} catch (Throwable t) {
			JdbcUtils.tryRollback(conn);

			log.error("unable to persist process " + process.getId(), t);
		} finally {
			JdbcUtils.tryClose(conn);
		}

		return false;
	}

	public void remove(String pid) {
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
		} catch (Throwable t) {
			JdbcUtils.tryRollback(conn);

			log.error("unable to remove process " + pid, t);
		} finally {
			JdbcUtils.tryClose(conn);
		}
	}

	public Process find(String pid) {
		Connection conn = JdbcUtils.tryOpen(ds, false, false, log);
		if (conn == null) {
			return null;
		}

		Process process = processFactory.newProcess();

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
			JdbcUtils.tryRollback(conn);
			process = null;

			log.error("unable to find process " + pid, t);
		} finally {
			JdbcUtils.tryClose(conn);
		}

		return process;
	}

	public Process[] findAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
