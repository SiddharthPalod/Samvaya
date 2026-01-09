package com.eventverse.notificationservice.converter;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;

public class JsonbStringType implements UserType<String> {

    @Override
    public int getSqlType() {
        return Types.OTHER; // Use OTHER for JSONB
    }

    @Override
    public Class<String> returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(String x, String y) {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(String x) {
        return x != null ? x.hashCode() : 0;
    }

    @Override
    public String nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Object value = rs.getObject(position);
        if (value == null) {
            return null;
        }
        // PostgreSQL returns JSONB as String or PGobject
        if (value instanceof String) {
            return (String) value;
        }
        // Handle PGobject using reflection to avoid compile-time dependency
        try {
            Class<?> pgObjectClass = Class.forName("org.postgresql.util.PGobject");
            if (pgObjectClass.isInstance(value)) {
                return (String) pgObjectClass.getMethod("getValue").invoke(value);
            }
        } catch (Exception e) {
            // Fall through to toString()
        }
        return value.toString();
    }

    @Override
    public void nullSafeSet(PreparedStatement st, String value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            // Use PGobject via reflection to explicitly set as JSONB type
            try {
                Class<?> pgObjectClass = Class.forName("org.postgresql.util.PGobject");
                Object pgObject = pgObjectClass.getDeclaredConstructor().newInstance();
                pgObjectClass.getMethod("setType", String.class).invoke(pgObject, "jsonb");
                pgObjectClass.getMethod("setValue", String.class).invoke(pgObject, value);
                st.setObject(index, pgObject, Types.OTHER);
            } catch (ClassNotFoundException e) {
                // PostgreSQL driver not available - this shouldn't happen in runtime
                throw new SQLException("PostgreSQL driver not found", e);
            } catch (Exception e) {
                // If reflection fails, try using a cast in SQL
                // We'll use a workaround by setting it as Object and letting Hibernate handle it
                st.setObject(index, value, Types.OTHER);
            }
        }
    }

    @Override
    public String deepCopy(String value) {
        return value; // String is immutable
    }

    @Override
    public boolean isMutable() {
        return false; // String is immutable
    }

    @Override
    public Serializable disassemble(String value) {
        return value;
    }

    @Override
    public String assemble(Serializable cached, Object owner) {
        return (String) cached;
    }

    @Override
    public String replace(String detached, String managed, Object owner) {
        return detached;
    }
}
