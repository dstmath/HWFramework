package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DEROctetString;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class ExtensionsGenerator {
    private Vector extOrdering = new Vector();
    private Hashtable extensions = new Hashtable();

    public void reset() {
        this.extensions = new Hashtable();
        this.extOrdering = new Vector();
    }

    public void addExtension(ASN1ObjectIdentifier oid, boolean critical, ASN1Encodable value) throws IOException {
        addExtension(oid, critical, value.toASN1Primitive().getEncoded(ASN1Encoding.DER));
    }

    public void addExtension(ASN1ObjectIdentifier oid, boolean critical, byte[] value) {
        if (this.extensions.containsKey(oid)) {
            throw new IllegalArgumentException("extension " + oid + " already added");
        }
        this.extOrdering.addElement(oid);
        this.extensions.put(oid, new Extension(oid, critical, new DEROctetString(value)));
    }

    public void addExtension(Extension extension) {
        if (this.extensions.containsKey(extension.getExtnId())) {
            throw new IllegalArgumentException("extension " + extension.getExtnId() + " already added");
        }
        this.extOrdering.addElement(extension.getExtnId());
        this.extensions.put(extension.getExtnId(), extension);
    }

    public boolean isEmpty() {
        return this.extOrdering.isEmpty();
    }

    public Extensions generate() {
        Extension[] exts = new Extension[this.extOrdering.size()];
        for (int i = 0; i != this.extOrdering.size(); i++) {
            exts[i] = (Extension) this.extensions.get(this.extOrdering.elementAt(i));
        }
        return new Extensions(exts);
    }
}
