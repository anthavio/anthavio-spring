/**
 * 
 */
package net.anthavio.spring.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author vanek
 *
 */
public class RowMappers {

	public static final RowMapper<List<Object>> LIST = new ListRowMapper();

	public static final RowMapper<List<String>> LIST_STRING = new StringListRowMapper();

	public static final RowMapper<List<String>> LIST_STRING_CLOB = new ClobStringListRowMapper();

	public static final RowMapper<Object[]> ARRAY = new ArrayRowMapper();

	public static final RowMapper<Integer> INTEGER = new IntegerRowMapper();

	public static final RowMapper<Long> LONG = new LongRowMapper();

	private static class ListRowMapper implements RowMapper<List<Object>> {

		@Override
		public List<Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
			int columnCount = rs.getMetaData().getColumnCount();
			List<Object> result = new ArrayList<Object>(columnCount);
			for (int i = 1; i <= columnCount; ++i) {
				result.add(rs.getObject(i));
			}
			return result;
		}
	}

	private static class StringListRowMapper implements RowMapper<List<String>> {
		@Override
		public List<String> mapRow(ResultSet rs, int rowNum) throws SQLException {
			int columnCount = rs.getMetaData().getColumnCount();
			List<String> result = new ArrayList<String>(columnCount);
			for (int i = 1; i <= columnCount; ++i) {
				result.add(rs.getString(i));
			}
			return result;
		}
	}

	private static class ClobStringListRowMapper implements RowMapper<List<String>> {
		@Override
		public List<String> mapRow(ResultSet rs, int rowNum) throws SQLException {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			List<String> result = new ArrayList<String>(columnCount);
			for (int i = 1; i <= columnCount; ++i) {
				if (metaData.getColumnType(i) == Types.CLOB) {
					System.out.println(metaData.getColumnName(i) + " " + metaData.getColumnType(i));
					BufferedReader reader = new BufferedReader(rs.getCharacterStream(i));
					String line = null;
					StringBuilder sb = new StringBuilder();
					try {
						while ((line = reader.readLine()) != null) {
							sb.append(line);
						}
					} catch (IOException iox) {
						throw new IllegalStateException("Error reading from clob reader", iox);
					}
					result.add(sb.toString());
				} else {
					result.add(rs.getString(i));
				}
			}
			return result;
		}
	}

	private static class ArrayRowMapper implements RowMapper<Object[]> {

		@Override
		public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
			int columnCount = rs.getMetaData().getColumnCount();
			Object[] result = new Object[columnCount];
			for (int i = 0; i < columnCount; ++i) {
				result[i] = rs.getObject(i + 1);
			}
			return result;
		}
	}

	private static class IntegerRowMapper implements RowMapper<Integer> {

		@Override
		public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
			int value = rs.getInt(1);
			if (rs.wasNull()) {
				return null;
			} else {
				return value;
			}
		}

	}

	public static class LongRowMapper implements RowMapper<Long> {

		@Override
		public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
			long value = rs.getLong(1);
			if (rs.wasNull()) {
				return null;
			} else {
				return value;
			}
		}

	}
}
