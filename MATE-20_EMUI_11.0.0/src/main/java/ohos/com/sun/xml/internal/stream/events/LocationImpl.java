package ohos.com.sun.xml.internal.stream.events;

import ohos.javax.xml.stream.Location;

public class LocationImpl implements Location {
    int charOffset;
    int colNo;
    int lineNo;
    String publicId;
    String systemId;

    LocationImpl(Location location) {
        this.systemId = location.getSystemId();
        this.publicId = location.getPublicId();
        this.lineNo = location.getLineNumber();
        this.colNo = location.getColumnNumber();
        this.charOffset = location.getCharacterOffset();
    }

    public int getCharacterOffset() {
        return this.charOffset;
    }

    public int getColumnNumber() {
        return this.colNo;
    }

    public int getLineNumber() {
        return this.lineNo;
    }

    public String getPublicId() {
        return this.publicId;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Line number = " + getLineNumber());
        stringBuffer.append("\n");
        stringBuffer.append("Column number = " + getColumnNumber());
        stringBuffer.append("\n");
        stringBuffer.append("System Id = " + getSystemId());
        stringBuffer.append("\n");
        stringBuffer.append("Public Id = " + getPublicId());
        stringBuffer.append("\n");
        stringBuffer.append("CharacterOffset = " + getCharacterOffset());
        stringBuffer.append("\n");
        return stringBuffer.toString();
    }
}
