package java.sql;

public interface ParameterMetaData extends Wrapper {
    public static final int parameterModeIn = 1;
    public static final int parameterModeInOut = 2;
    public static final int parameterModeOut = 4;
    public static final int parameterModeUnknown = 0;
    public static final int parameterNoNulls = 0;
    public static final int parameterNullable = 1;
    public static final int parameterNullableUnknown = 2;

    String getParameterClassName(int i) throws SQLException;

    int getParameterCount() throws SQLException;

    int getParameterMode(int i) throws SQLException;

    int getParameterType(int i) throws SQLException;

    String getParameterTypeName(int i) throws SQLException;

    int getPrecision(int i) throws SQLException;

    int getScale(int i) throws SQLException;

    int isNullable(int i) throws SQLException;

    boolean isSigned(int i) throws SQLException;
}
