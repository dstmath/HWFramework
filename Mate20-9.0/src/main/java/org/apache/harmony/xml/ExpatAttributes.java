package org.apache.harmony.xml;

import org.xml.sax.Attributes;

abstract class ExpatAttributes implements Attributes {
    private static final String CDATA = "CDATA";

    private static native int getIndex(long j, String str, String str2);

    private static native int getIndexForQName(long j, String str);

    private static native String getLocalName(long j, long j2, int i);

    private static native String getQName(long j, long j2, int i);

    private static native String getURI(long j, long j2, int i);

    private static native String getValue(long j, String str, String str2);

    private static native String getValueByIndex(long j, int i);

    private static native String getValueForQName(long j, String str);

    /* access modifiers changed from: protected */
    public native void freeAttributes(long j);

    public abstract int getLength();

    /* access modifiers changed from: package-private */
    public abstract long getParserPointer();

    public abstract long getPointer();

    ExpatAttributes() {
    }

    public String getURI(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return getURI(getParserPointer(), getPointer(), index);
    }

    public String getLocalName(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return getLocalName(getParserPointer(), getPointer(), index);
    }

    public String getQName(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return getQName(getParserPointer(), getPointer(), index);
    }

    public String getType(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return CDATA;
    }

    public String getValue(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return getValueByIndex(getPointer(), index);
    }

    public int getIndex(String uri, String localName) {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        } else if (localName != null) {
            long pointer = getPointer();
            if (pointer == 0) {
                return -1;
            }
            return getIndex(pointer, uri, localName);
        } else {
            throw new NullPointerException("localName == null");
        }
    }

    public int getIndex(String qName) {
        if (qName != null) {
            long pointer = getPointer();
            if (pointer == 0) {
                return -1;
            }
            return getIndexForQName(pointer, qName);
        }
        throw new NullPointerException("qName == null");
    }

    public String getType(String uri, String localName) {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        } else if (localName == null) {
            throw new NullPointerException("localName == null");
        } else if (getIndex(uri, localName) == -1) {
            return null;
        } else {
            return CDATA;
        }
    }

    public String getType(String qName) {
        if (getIndex(qName) == -1) {
            return null;
        }
        return CDATA;
    }

    public String getValue(String uri, String localName) {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        } else if (localName != null) {
            long pointer = getPointer();
            if (pointer == 0) {
                return null;
            }
            return getValue(pointer, uri, localName);
        } else {
            throw new NullPointerException("localName == null");
        }
    }

    public String getValue(String qName) {
        if (qName != null) {
            long pointer = getPointer();
            if (pointer == 0) {
                return null;
            }
            return getValueForQName(pointer, qName);
        }
        throw new NullPointerException("qName == null");
    }
}
