package org.bouncycastle.asn1.cms;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1ParsingException;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1SequenceParser;
import org.bouncycastle.asn1.ASN1SetParser;
import org.bouncycastle.asn1.ASN1TaggedObjectParser;

public class AuthEnvelopedDataParser {
    private EncryptedContentInfoParser authEncryptedContentInfoParser;
    private ASN1Encodable nextObject;
    private boolean originatorInfoCalled;
    private ASN1SequenceParser seq;
    private ASN1Integer version;

    public AuthEnvelopedDataParser(ASN1SequenceParser aSN1SequenceParser) throws IOException {
        this.seq = aSN1SequenceParser;
        this.version = ASN1Integer.getInstance(aSN1SequenceParser.readObject());
        if (this.version.intValueExact() != 0) {
            throw new ASN1ParsingException("AuthEnvelopedData version number must be 0");
        }
    }

    public ASN1SetParser getAuthAttrs() throws IOException {
        if (this.nextObject == null) {
            this.nextObject = this.seq.readObject();
        }
        ASN1Encodable aSN1Encodable = this.nextObject;
        if (aSN1Encodable instanceof ASN1TaggedObjectParser) {
            this.nextObject = null;
            return (ASN1SetParser) ((ASN1TaggedObjectParser) aSN1Encodable).getObjectParser(17, false);
        } else if (this.authEncryptedContentInfoParser.getContentType().equals((ASN1Primitive) CMSObjectIdentifiers.data)) {
            return null;
        } else {
            throw new ASN1ParsingException("authAttrs must be present with non-data content");
        }
    }

    public EncryptedContentInfoParser getAuthEncryptedContentInfo() throws IOException {
        if (this.nextObject == null) {
            this.nextObject = this.seq.readObject();
        }
        ASN1Encodable aSN1Encodable = this.nextObject;
        if (aSN1Encodable == null) {
            return null;
        }
        this.nextObject = null;
        this.authEncryptedContentInfoParser = new EncryptedContentInfoParser((ASN1SequenceParser) aSN1Encodable);
        return this.authEncryptedContentInfoParser;
    }

    public ASN1OctetString getMac() throws IOException {
        if (this.nextObject == null) {
            this.nextObject = this.seq.readObject();
        }
        ASN1Encodable aSN1Encodable = this.nextObject;
        this.nextObject = null;
        return ASN1OctetString.getInstance(aSN1Encodable.toASN1Primitive());
    }

    public OriginatorInfo getOriginatorInfo() throws IOException {
        this.originatorInfoCalled = true;
        if (this.nextObject == null) {
            this.nextObject = this.seq.readObject();
        }
        ASN1Encodable aSN1Encodable = this.nextObject;
        if (!(aSN1Encodable instanceof ASN1TaggedObjectParser) || ((ASN1TaggedObjectParser) aSN1Encodable).getTagNo() != 0) {
            return null;
        }
        this.nextObject = null;
        return OriginatorInfo.getInstance(((ASN1SequenceParser) ((ASN1TaggedObjectParser) this.nextObject).getObjectParser(16, false)).toASN1Primitive());
    }

    public ASN1SetParser getRecipientInfos() throws IOException {
        if (!this.originatorInfoCalled) {
            getOriginatorInfo();
        }
        if (this.nextObject == null) {
            this.nextObject = this.seq.readObject();
        }
        ASN1SetParser aSN1SetParser = (ASN1SetParser) this.nextObject;
        this.nextObject = null;
        return aSN1SetParser;
    }

    public ASN1SetParser getUnauthAttrs() throws IOException {
        if (this.nextObject == null) {
            this.nextObject = this.seq.readObject();
        }
        ASN1Encodable aSN1Encodable = this.nextObject;
        if (aSN1Encodable == null) {
            return null;
        }
        this.nextObject = null;
        return (ASN1SetParser) ((ASN1TaggedObjectParser) aSN1Encodable).getObjectParser(17, false);
    }

    public ASN1Integer getVersion() {
        return this.version;
    }
}
