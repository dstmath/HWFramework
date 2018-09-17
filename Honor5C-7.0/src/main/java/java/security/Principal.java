package java.security;

public interface Principal {
    boolean equals(Object obj);

    String getName();

    int hashCode();

    String toString();
}
