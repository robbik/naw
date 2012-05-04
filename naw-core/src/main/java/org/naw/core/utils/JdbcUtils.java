package org.naw.core.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import rk.commons.logging.Logger;

public abstract class JdbcUtils {

	public static Connection tryOpen(DataSource ds, boolean readOnly, boolean autoCommit) {
		return tryOpen(ds, readOnly, autoCommit, null);
	}

	public static Connection tryOpen(DataSource ds, boolean readOnly, boolean autoCommit, Logger log) {
		Connection conn = null;

		try {
			conn = ds.getConnection();

			conn.setReadOnly(readOnly);
			conn.setAutoCommit(autoCommit);
		} catch (Throwable t) {
			if (log != null) {
				log.error("unable to open jdbc connection with data source " + ds, t);
			}

			tryClose(conn);
		}

		return conn;
	}

	public static void tryRollback(Connection conn) {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	public static void tryClose(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	public static void tryClose(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	public static void tryClose(PreparedStatement pstmt) {
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	public static void tryClose(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}
}
