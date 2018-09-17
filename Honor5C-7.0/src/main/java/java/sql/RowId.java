package java.sql;

public interface RowId {
    boolean equals(Object obj);

    byte[] getBytes();

    int hashCode();

    String toString();
}
