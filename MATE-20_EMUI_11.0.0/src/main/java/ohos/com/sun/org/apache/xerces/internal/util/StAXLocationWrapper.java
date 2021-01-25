package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.javax.xml.stream.Location;

public final class StAXLocationWrapper implements XMLLocator {
    private Location fLocation = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getBaseSystemId() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getEncoding() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getXMLVersion() {
        return null;
    }

    public void setLocation(Location location) {
        this.fLocation = location;
    }

    public Location getLocation() {
        return this.fLocation;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getPublicId() {
        Location location = this.fLocation;
        if (location != null) {
            return location.getPublicId();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getLiteralSystemId() {
        Location location = this.fLocation;
        if (location != null) {
            return location.getSystemId();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getExpandedSystemId() {
        return getLiteralSystemId();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public int getLineNumber() {
        Location location = this.fLocation;
        if (location != null) {
            return location.getLineNumber();
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public int getColumnNumber() {
        Location location = this.fLocation;
        if (location != null) {
            return location.getColumnNumber();
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public int getCharacterOffset() {
        Location location = this.fLocation;
        if (location != null) {
            return location.getCharacterOffset();
        }
        return -1;
    }
}
