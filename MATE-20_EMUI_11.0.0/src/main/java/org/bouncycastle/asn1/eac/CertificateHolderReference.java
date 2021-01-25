package org.bouncycastle.asn1.eac;

import java.io.UnsupportedEncodingException;

public class CertificateHolderReference {
    private static final String ReferenceEncoding = "ISO-8859-1";
    private String countryCode;
    private String holderMnemonic;
    private String sequenceNumber;

    public CertificateHolderReference(String str, String str2, String str3) {
        this.countryCode = str;
        this.holderMnemonic = str2;
        this.sequenceNumber = str3;
    }

    CertificateHolderReference(byte[] bArr) {
        try {
            String str = new String(bArr, "ISO-8859-1");
            this.countryCode = str.substring(0, 2);
            this.holderMnemonic = str.substring(2, str.length() - 5);
            this.sequenceNumber = str.substring(str.length() - 5);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public byte[] getEncoded() {
        try {
            return (this.countryCode + this.holderMnemonic + this.sequenceNumber).getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    public String getHolderMnemonic() {
        return this.holderMnemonic;
    }

    public String getSequenceNumber() {
        return this.sequenceNumber;
    }
}
