package com.android.org.bouncycastle.asn1;

import java.io.IOException;

public class BERApplicationSpecificParser implements ASN1ApplicationSpecificParser {
    private final ASN1StreamParser parser;
    private final int tag;

    BERApplicationSpecificParser(int tag2, ASN1StreamParser parser2) {
        this.tag = tag2;
        this.parser = parser2;
    }

    public ASN1Encodable readObject() throws IOException {
        return this.parser.readObject();
    }

    public ASN1Primitive getLoadedObject() throws IOException {
        return new BERApplicationSpecific(this.tag, this.parser.readVector());
    }

    public ASN1Primitive toASN1Primitive() {
        try {
            return getLoadedObject();
        } catch (IOException e) {
            throw new ASN1ParsingException(e.getMessage(), e);
        }
    }
}
