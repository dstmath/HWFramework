package org.bouncycastle.asn1.cms;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetStringParser;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1SequenceParser;
import org.bouncycastle.asn1.DERIA5String;

public class TimeStampedDataParser {
    private ASN1OctetStringParser content;
    private DERIA5String dataUri;
    private MetaData metaData;
    private ASN1SequenceParser parser;
    private Evidence temporalEvidence;
    private ASN1Integer version;

    private TimeStampedDataParser(ASN1SequenceParser aSN1SequenceParser) throws IOException {
        this.parser = aSN1SequenceParser;
        this.version = ASN1Integer.getInstance(aSN1SequenceParser.readObject());
        ASN1Encodable readObject = aSN1SequenceParser.readObject();
        if (readObject instanceof DERIA5String) {
            this.dataUri = DERIA5String.getInstance(readObject);
            readObject = aSN1SequenceParser.readObject();
        }
        if ((readObject instanceof MetaData) || (readObject instanceof ASN1SequenceParser)) {
            this.metaData = MetaData.getInstance(readObject.toASN1Primitive());
            readObject = aSN1SequenceParser.readObject();
        }
        if (readObject instanceof ASN1OctetStringParser) {
            this.content = (ASN1OctetStringParser) readObject;
        }
    }

    public static TimeStampedDataParser getInstance(Object obj) throws IOException {
        if (obj instanceof ASN1Sequence) {
            return new TimeStampedDataParser(((ASN1Sequence) obj).parser());
        }
        if (obj instanceof ASN1SequenceParser) {
            return new TimeStampedDataParser((ASN1SequenceParser) obj);
        }
        return null;
    }

    public ASN1OctetStringParser getContent() {
        return this.content;
    }

    public DERIA5String getDataUri() {
        return this.dataUri;
    }

    public MetaData getMetaData() {
        return this.metaData;
    }

    public Evidence getTemporalEvidence() throws IOException {
        if (this.temporalEvidence == null) {
            this.temporalEvidence = Evidence.getInstance(this.parser.readObject().toASN1Primitive());
        }
        return this.temporalEvidence;
    }
}
