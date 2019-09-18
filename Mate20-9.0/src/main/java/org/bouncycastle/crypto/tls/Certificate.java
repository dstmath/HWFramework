package org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1Encoding;

public class Certificate {
    public static final Certificate EMPTY_CHAIN = new Certificate(new org.bouncycastle.asn1.x509.Certificate[0]);
    protected org.bouncycastle.asn1.x509.Certificate[] certificateList;

    public Certificate(org.bouncycastle.asn1.x509.Certificate[] certificateArr) {
        if (certificateArr != null) {
            this.certificateList = certificateArr;
            return;
        }
        throw new IllegalArgumentException("'certificateList' cannot be null");
    }

    public static Certificate parse(InputStream inputStream) throws IOException {
        int readUint24 = TlsUtils.readUint24(inputStream);
        if (readUint24 == 0) {
            return EMPTY_CHAIN;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(TlsUtils.readFully(readUint24, inputStream));
        Vector vector = new Vector();
        while (byteArrayInputStream.available() > 0) {
            vector.addElement(org.bouncycastle.asn1.x509.Certificate.getInstance(TlsUtils.readASN1Object(TlsUtils.readOpaque24(byteArrayInputStream))));
        }
        org.bouncycastle.asn1.x509.Certificate[] certificateArr = new org.bouncycastle.asn1.x509.Certificate[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            certificateArr[i] = (org.bouncycastle.asn1.x509.Certificate) vector.elementAt(i);
        }
        return new Certificate(certificateArr);
    }

    /* access modifiers changed from: protected */
    public org.bouncycastle.asn1.x509.Certificate[] cloneCertificateList() {
        org.bouncycastle.asn1.x509.Certificate[] certificateArr = new org.bouncycastle.asn1.x509.Certificate[this.certificateList.length];
        System.arraycopy(this.certificateList, 0, certificateArr, 0, certificateArr.length);
        return certificateArr;
    }

    public void encode(OutputStream outputStream) throws IOException {
        Vector vector = new Vector(this.certificateList.length);
        int i = 0;
        for (org.bouncycastle.asn1.x509.Certificate encoded : this.certificateList) {
            byte[] encoded2 = encoded.getEncoded(ASN1Encoding.DER);
            vector.addElement(encoded2);
            i += encoded2.length + 3;
        }
        TlsUtils.checkUint24(i);
        TlsUtils.writeUint24(i, outputStream);
        for (int i2 = 0; i2 < vector.size(); i2++) {
            TlsUtils.writeOpaque24((byte[]) vector.elementAt(i2), outputStream);
        }
    }

    public org.bouncycastle.asn1.x509.Certificate getCertificateAt(int i) {
        return this.certificateList[i];
    }

    public org.bouncycastle.asn1.x509.Certificate[] getCertificateList() {
        return cloneCertificateList();
    }

    public int getLength() {
        return this.certificateList.length;
    }

    public boolean isEmpty() {
        return this.certificateList.length == 0;
    }
}
