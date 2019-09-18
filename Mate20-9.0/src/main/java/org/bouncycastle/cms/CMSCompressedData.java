package org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.cms.CompressedData;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.operator.InputExpanderProvider;
import org.bouncycastle.util.Encodable;

public class CMSCompressedData implements Encodable {
    CompressedData comData;
    ContentInfo contentInfo;

    public CMSCompressedData(InputStream inputStream) throws CMSException {
        this(CMSUtils.readContentInfo(inputStream));
    }

    public CMSCompressedData(ContentInfo contentInfo2) throws CMSException {
        this.contentInfo = contentInfo2;
        try {
            this.comData = CompressedData.getInstance(contentInfo2.getContent());
        } catch (ClassCastException e) {
            throw new CMSException("Malformed content.", e);
        } catch (IllegalArgumentException e2) {
            throw new CMSException("Malformed content.", e2);
        }
    }

    public CMSCompressedData(byte[] bArr) throws CMSException {
        this(CMSUtils.readContentInfo(bArr));
    }

    public byte[] getContent(InputExpanderProvider inputExpanderProvider) throws CMSException {
        try {
            return CMSUtils.streamToByteArray(inputExpanderProvider.get(this.comData.getCompressionAlgorithmIdentifier()).getInputStream(((ASN1OctetString) this.comData.getEncapContentInfo().getContent()).getOctetStream()));
        } catch (IOException e) {
            throw new CMSException("exception reading compressed stream.", e);
        }
    }

    public ASN1ObjectIdentifier getContentType() {
        return this.contentInfo.getContentType();
    }

    public byte[] getEncoded() throws IOException {
        return this.contentInfo.getEncoded();
    }

    public ContentInfo toASN1Structure() {
        return this.contentInfo;
    }
}
