package org.bouncycastle.pkcs;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.pkcs.AuthenticatedSafe;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.MacData;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.Pfx;
import org.bouncycastle.cms.CMSEncryptedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.operator.OutputEncryptor;

public class PKCS12PfxPduBuilder {
    private ASN1EncodableVector dataVector = new ASN1EncodableVector();

    private PKCS12PfxPduBuilder addEncryptedData(OutputEncryptor outputEncryptor, ASN1Sequence aSN1Sequence) throws IOException {
        try {
            this.dataVector.add(new CMSEncryptedDataGenerator().generate(new CMSProcessableByteArray(aSN1Sequence.getEncoded()), outputEncryptor).toASN1Structure());
            return this;
        } catch (CMSException e) {
            throw new PKCSIOException(e.getMessage(), e.getCause());
        }
    }

    public PKCS12PfxPduBuilder addData(PKCS12SafeBag pKCS12SafeBag) throws IOException {
        this.dataVector.add(new ContentInfo(PKCSObjectIdentifiers.data, new DEROctetString(new DLSequence(pKCS12SafeBag.toASN1Structure()).getEncoded())));
        return this;
    }

    public PKCS12PfxPduBuilder addEncryptedData(OutputEncryptor outputEncryptor, PKCS12SafeBag pKCS12SafeBag) throws IOException {
        return addEncryptedData(outputEncryptor, new DERSequence(pKCS12SafeBag.toASN1Structure()));
    }

    public PKCS12PfxPduBuilder addEncryptedData(OutputEncryptor outputEncryptor, PKCS12SafeBag[] pKCS12SafeBagArr) throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (int i = 0; i != pKCS12SafeBagArr.length; i++) {
            aSN1EncodableVector.add(pKCS12SafeBagArr[i].toASN1Structure());
        }
        return addEncryptedData(outputEncryptor, new DLSequence(aSN1EncodableVector));
    }

    public PKCS12PfxPdu build(PKCS12MacCalculatorBuilder pKCS12MacCalculatorBuilder, char[] cArr) throws PKCSException {
        try {
            byte[] encoded = AuthenticatedSafe.getInstance(new DLSequence(this.dataVector)).getEncoded();
            ContentInfo contentInfo = new ContentInfo(PKCSObjectIdentifiers.data, new DEROctetString(encoded));
            MacData macData = null;
            if (pKCS12MacCalculatorBuilder != null) {
                macData = new MacDataGenerator(pKCS12MacCalculatorBuilder).build(cArr, encoded);
            }
            return new PKCS12PfxPdu(new Pfx(contentInfo, macData));
        } catch (IOException e) {
            throw new PKCSException("unable to encode AuthenticatedSafe: " + e.getMessage(), e);
        }
    }
}
