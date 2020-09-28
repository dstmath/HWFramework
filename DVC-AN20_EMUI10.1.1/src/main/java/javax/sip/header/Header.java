package javax.sip.header;

import java.io.Serializable;

public interface Header extends Cloneable, Serializable {
    @Override // java.lang.Object
    Object clone();

    boolean equals(Object obj);

    String getName();

    int hashCode();

    String toString();
}
