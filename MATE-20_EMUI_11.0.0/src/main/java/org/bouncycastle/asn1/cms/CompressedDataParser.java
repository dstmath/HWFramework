package org.bouncycastle.asn1.cms;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1SequenceParser;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class CompressedDataParser {
    private AlgorithmIdentifier _compressionAlgorithm;
    private ContentInfoParser _encapContentInfo;
    private ASN1Integer _version;

    public CompressedDataParser(ASN1SequenceParser aSN1SequenceParser) throws IOException {
        this._version = (ASN1Integer) aSN1SequenceParser.readObject();
        this._compressionAlgorithm = AlgorithmIdentifier.getInstance(aSN1SequenceParser.readObject().toASN1Primitive());
        this._encapContentInfo = new ContentInfoParser((ASN1SequenceParser) aSN1SequenceParser.readObject());
    }

    public AlgorithmIdentifier getCompressionAlgorithmIdentifier() {
        return this._compressionAlgorithm;
    }

    public ContentInfoParser getEncapContentInfo() {
        return this._encapContentInfo;
    }

    public ASN1Integer getVersion() {
        return this._version;
    }
}
