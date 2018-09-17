package java.sql;

public interface Wrapper {
    boolean isWrapperFor(Class<?> cls) throws SQLException;

    <T> T unwrap(Class<T> cls) throws SQLException;
}
