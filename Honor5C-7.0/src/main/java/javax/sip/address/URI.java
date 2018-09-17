package javax.sip.address;

import java.io.Serializable;

public interface URI extends Cloneable, Serializable {
    Object clone();

    String getScheme();

    boolean isSipURI();

    String toString();
}
