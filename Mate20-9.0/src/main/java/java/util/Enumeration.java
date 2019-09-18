package java.util;

public interface Enumeration<E> {
    boolean hasMoreElements();

    E nextElement();
}
