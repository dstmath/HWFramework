package org.bouncycastle.jce.provider;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.util.ArrayList;
import java.util.Collection;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.SignedData;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.x509.X509StreamParserSpi;
import org.bouncycastle.x509.util.StreamParsingException;

public class X509CRLParser extends X509StreamParserSpi {
    private static final PEMUtil PEM_PARSER = new PEMUtil("CRL");
    private InputStream currentStream = null;
    private ASN1Set sData = null;
    private int sDataObjectCount = 0;

    private CRL getCRL() throws CRLException {
        ASN1Set aSN1Set = this.sData;
        if (aSN1Set == null || this.sDataObjectCount >= aSN1Set.size()) {
            return null;
        }
        ASN1Set aSN1Set2 = this.sData;
        int i = this.sDataObjectCount;
        this.sDataObjectCount = i + 1;
        return new X509CRLObject(CertificateList.getInstance(aSN1Set2.getObjectAt(i)));
    }

    private CRL readDERCRL(InputStream inputStream) throws IOException, CRLException {
        ASN1Sequence aSN1Sequence = (ASN1Sequence) new ASN1InputStream(inputStream).readObject();
        if (aSN1Sequence.size() <= 1 || !(aSN1Sequence.getObjectAt(0) instanceof ASN1ObjectIdentifier) || !aSN1Sequence.getObjectAt(0).equals(PKCSObjectIdentifiers.signedData)) {
            return new X509CRLObject(CertificateList.getInstance(aSN1Sequence));
        }
        this.sData = new SignedData(ASN1Sequence.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(1), true)).getCRLs();
        return getCRL();
    }

    private CRL readPEMCRL(InputStream inputStream) throws IOException, CRLException {
        ASN1Sequence readPEMObject = PEM_PARSER.readPEMObject(inputStream);
        if (readPEMObject != null) {
            return new X509CRLObject(CertificateList.getInstance(readPEMObject));
        }
        return null;
    }

    @Override // org.bouncycastle.x509.X509StreamParserSpi
    public void engineInit(InputStream inputStream) {
        this.currentStream = inputStream;
        this.sData = null;
        this.sDataObjectCount = 0;
        if (!this.currentStream.markSupported()) {
            this.currentStream = new BufferedInputStream(this.currentStream);
        }
    }

    @Override // org.bouncycastle.x509.X509StreamParserSpi
    public Object engineRead() throws StreamParsingException {
        try {
            if (this.sData == null) {
                this.currentStream.mark(10);
                int read = this.currentStream.read();
                if (read == -1) {
                    return null;
                }
                if (read != 48) {
                    this.currentStream.reset();
                    return readPEMCRL(this.currentStream);
                }
                this.currentStream.reset();
                return readDERCRL(this.currentStream);
            } else if (this.sDataObjectCount != this.sData.size()) {
                return getCRL();
            } else {
                this.sData = null;
                this.sDataObjectCount = 0;
                return null;
            }
        } catch (Exception e) {
            throw new StreamParsingException(e.toString(), e);
        }
    }

    @Override // org.bouncycastle.x509.X509StreamParserSpi
    public Collection engineReadAll() throws StreamParsingException {
        ArrayList arrayList = new ArrayList();
        while (true) {
            CRL crl = (CRL) engineRead();
            if (crl == null) {
                return arrayList;
            }
            arrayList.add(crl);
        }
    }
}
