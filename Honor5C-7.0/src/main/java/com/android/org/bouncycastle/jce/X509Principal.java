package com.android.org.bouncycastle.jce;

import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x509.X509Name;
import java.io.IOException;
import java.security.Principal;
import java.util.Hashtable;
import java.util.Vector;

public class X509Principal extends X509Name implements Principal {
    private static ASN1Sequence readSequence(ASN1InputStream aIn) throws IOException {
        try {
            return ASN1Sequence.getInstance(aIn.readObject());
        } catch (IllegalArgumentException e) {
            throw new IOException("not an ASN.1 Sequence: " + e);
        }
    }

    public X509Principal(byte[] bytes) throws IOException {
        super(readSequence(new ASN1InputStream(bytes)));
    }

    public X509Principal(X509Name name) {
        super((ASN1Sequence) name.toASN1Primitive());
    }

    public X509Principal(X500Name name) {
        super((ASN1Sequence) name.toASN1Primitive());
    }

    public X509Principal(Hashtable attributes) {
        super(attributes);
    }

    public X509Principal(Vector ordering, Hashtable attributes) {
        super(ordering, attributes);
    }

    public X509Principal(Vector oids, Vector values) {
        super(oids, values);
    }

    public X509Principal(String dirName) {
        super(dirName);
    }

    public X509Principal(boolean reverse, String dirName) {
        super(reverse, dirName);
    }

    public X509Principal(boolean reverse, Hashtable lookUp, String dirName) {
        super(reverse, lookUp, dirName);
    }

    public String getName() {
        return toString();
    }

    public byte[] getEncoded() {
        try {
            return getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }
}
