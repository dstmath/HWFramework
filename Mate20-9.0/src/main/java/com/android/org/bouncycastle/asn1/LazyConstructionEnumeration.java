package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;

class LazyConstructionEnumeration implements Enumeration {
    private ASN1InputStream aIn;
    private Object nextObj = readObject();

    public LazyConstructionEnumeration(byte[] encoded) {
        this.aIn = new ASN1InputStream(encoded, true);
    }

    public boolean hasMoreElements() {
        return this.nextObj != null;
    }

    public Object nextElement() {
        Object o = this.nextObj;
        this.nextObj = readObject();
        return o;
    }

    private Object readObject() {
        try {
            return this.aIn.readObject();
        } catch (IOException e) {
            throw new ASN1ParsingException("malformed DER construction: " + e, e);
        }
    }
}
