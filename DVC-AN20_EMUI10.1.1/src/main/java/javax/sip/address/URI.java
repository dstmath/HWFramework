package javax.sip.address;

import java.io.Serializable;

public interface URI extends Cloneable, Serializable {
    @Override // java.lang.Object
    Object clone();

    String getScheme();

    boolean isSipURI();

    String toString();
}
