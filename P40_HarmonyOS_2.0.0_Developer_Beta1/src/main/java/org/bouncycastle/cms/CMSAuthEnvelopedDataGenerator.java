package org.bouncycastle.cms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DLSet;
import org.bouncycastle.asn1.cms.AuthEnvelopedData;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.EncryptedContentInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.OutputAEADEncryptor;

public class CMSAuthEnvelopedDataGenerator extends CMSAuthEnvelopedGenerator {
    private CMSAuthEnvelopedData doGenerate(CMSTypedData cMSTypedData, OutputAEADEncryptor outputAEADEncryptor) throws CMSException {
        DERSet dERSet;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            OutputStream outputStream = outputAEADEncryptor.getOutputStream(byteArrayOutputStream);
            cMSTypedData.write(outputStream);
            DLSet dLSet = null;
            if (this.authAttrsGenerator != null) {
                dERSet = new DERSet(this.authAttrsGenerator.getAttributes(new HashMap()).toASN1EncodableVector());
                outputAEADEncryptor.getAADStream().write(dERSet.getEncoded(ASN1Encoding.DER));
            } else {
                dERSet = null;
            }
            outputStream.close();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            AlgorithmIdentifier algorithmIdentifier = outputAEADEncryptor.getAlgorithmIdentifier();
            BEROctetString bEROctetString = new BEROctetString(byteArray);
            GenericKey key = outputAEADEncryptor.getKey();
            for (RecipientInfoGenerator recipientInfoGenerator : this.recipientInfoGenerators) {
                aSN1EncodableVector.add(recipientInfoGenerator.generate(key));
            }
            EncryptedContentInfo encryptedContentInfo = new EncryptedContentInfo(cMSTypedData.getContentType(), algorithmIdentifier, bEROctetString);
            if (this.unauthAttrsGenerator != null) {
                dLSet = new DLSet(this.unauthAttrsGenerator.getAttributes(new HashMap()).toASN1EncodableVector());
            }
            return new CMSAuthEnvelopedData(new ContentInfo(CMSObjectIdentifiers.authEnvelopedData, new AuthEnvelopedData(this.originatorInfo, new DERSet(aSN1EncodableVector), encryptedContentInfo, dERSet, new DEROctetString(outputAEADEncryptor.getMAC()), dLSet)));
        } catch (IOException e) {
            throw new CMSException("unable to process authenticated content: " + e.getMessage(), e);
        }
    }

    public CMSAuthEnvelopedData generate(CMSTypedData cMSTypedData, OutputAEADEncryptor outputAEADEncryptor) throws CMSException {
        return doGenerate(cMSTypedData, outputAEADEncryptor);
    }
}
