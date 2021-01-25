package javax.sip.header;

import java.io.Serializable;

public interface Header extends Cloneable, Serializable {
    @Override // java.lang.Object
    Object clone();

    @Override // java.lang.Object
    boolean equals(Object obj);

    String getName();

    @Override // javax.sip.header.Header
    int hashCode();

    @Override // javax.sip.header.Header
    String toString();
}
