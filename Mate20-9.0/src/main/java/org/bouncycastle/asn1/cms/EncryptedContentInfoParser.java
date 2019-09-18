package org.bouncycastle.asn1.cms;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1SequenceParser;
import org.bouncycastle.asn1.ASN1TaggedObjectParser;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class EncryptedContentInfoParser {
    private AlgorithmIdentifier _contentEncryptionAlgorithm;
    private ASN1ObjectIdentifier _contentType;
    private ASN1TaggedObjectParser _encryptedContent;

    public EncryptedContentInfoParser(ASN1SequenceParser aSN1SequenceParser) throws IOException {
        this._contentType = (ASN1ObjectIdentifier) aSN1SequenceParser.readObject();
        this._contentEncryptionAlgorithm = AlgorithmIdentifier.getInstance(aSN1SequenceParser.readObject().toASN1Primitive());
        this._encryptedContent = (ASN1TaggedObjectParser) aSN1SequenceParser.readObject();
    }

    public AlgorithmIdentifier getContentEncryptionAlgorithm() {
        return this._contentEncryptionAlgorithm;
    }

    public ASN1ObjectIdentifier getContentType() {
        return this._contentType;
    }

    public ASN1Encodable getEncryptedContent(int i) throws IOException {
        return this._encryptedContent.getObjectParser(i, false);
    }
}
