package javax.sip.address;

import java.io.Serializable;
import java.text.ParseException;

public interface Address extends Cloneable, Serializable {
    @Override // java.lang.Object
    Object clone();

    @Override // java.lang.Object
    boolean equals(Object obj);

    String getDisplayName();

    String getHost();

    int getPort();

    URI getURI();

    String getUserAtHostPort();

    boolean hasDisplayName();

    @Override // javax.sip.address.Address
    int hashCode();

    boolean isSIPAddress();

    boolean isWildcard();

    void setDisplayName(String str) throws ParseException;

    void setURI(URI uri);

    void setWildCardFlag();
}
