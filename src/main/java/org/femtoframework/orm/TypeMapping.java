package org.femtoframework.orm;

import java.sql.*;
import java.util.Date;

public interface TypeMapping {

    /**
     * Map object to SQLType
     *
     * @param obj Object
     * @return SQLType
     */
    static SQLType toSQLType(Object obj) {
        if (obj instanceof String) {
            return JDBCType.VARCHAR;
        }
        else if (obj instanceof Integer) {
            return JDBCType.INTEGER;
        }
        else if (obj instanceof Long) {
            return JDBCType.BIGINT;
        }
        else if (obj instanceof Byte) {
            return JDBCType.TINYINT;
        }
        else if (obj instanceof Short) {
            return JDBCType.SMALLINT;
        }
        else if (obj instanceof Double) {
            return JDBCType.DOUBLE;
        }
        else if (obj instanceof Float) {
            return JDBCType.FLOAT;
        }
        else if (obj instanceof Character) {
            return JDBCType.CHAR;
        }
        else if (obj instanceof Boolean) {
            return JDBCType.BOOLEAN;
        }
        else if (obj instanceof Time) {
            return JDBCType.TIME;
        }
        else if (obj instanceof Timestamp) {
            return JDBCType.TIMESTAMP;
        }
        else if (obj instanceof Date) {
            return JDBCType.DATE;
        }
        else if (obj instanceof byte[]) {
            return JDBCType.BINARY;
        }
        else {
            throw new IllegalArgumentException("Unsupported:" + obj);
        }
    }

    /**
     * Convert SQLType to JavaType
     *
     * @param sqlType SQLType
     * @return Type Class, returns null if the type is NULL
     */
    static Class<?> toJavaType(SQLType sqlType) {
        if (sqlType instanceof JDBCType) {
            switch ((JDBCType)sqlType) {
                case BIT:
                case BOOLEAN:
                    return Boolean.class;
                case VARCHAR:
                case NVARCHAR:
                    return String.class;
                case INTEGER:
                    return Integer.class;
                case BIGINT:
                case NUMERIC:
                case DECIMAL:
                    return Long.class;
                case DOUBLE:
                case REAL:
                    return Double.class;
                case FLOAT:
                    return Float.class;
                case BINARY:
                    return byte[].class;
                case TIMESTAMP:
                    return Timestamp.class;
                case TIME:
                    return Time.class;
                case DATE:
                    return java.sql.Date.class;
                case CHAR:
                case NCHAR:
                    return Character.class;
                case NULL:
                    return null;
                case TINYINT:
                    return Byte.class;
                case SMALLINT:
                    return Short.class;
            }
        }
        return Object.class;
    }
}
