package org.bouncycastle.tsp.cms;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.cms.Attributes;
import org.bouncycastle.asn1.cms.MetaData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.DigestCalculator;

class MetaDataUtil {
    private final MetaData metaData;

    MetaDataUtil(MetaData metaData2) {
        this.metaData = metaData2;
    }

    private String convertString(ASN1String aSN1String) {
        if (aSN1String != null) {
            return aSN1String.toString();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getFileName() {
        if (this.metaData != null) {
            return convertString(this.metaData.getFileName());
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getMediaType() {
        if (this.metaData != null) {
            return convertString(this.metaData.getMediaType());
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Attributes getOtherMetaData() {
        if (this.metaData != null) {
            return this.metaData.getOtherMetaData();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void initialiseMessageImprintDigestCalculator(DigestCalculator digestCalculator) throws CMSException {
        if (this.metaData != null && this.metaData.isHashProtected()) {
            try {
                digestCalculator.getOutputStream().write(this.metaData.getEncoded(ASN1Encoding.DER));
            } catch (IOException e) {
                throw new CMSException("unable to initialise calculator from metaData: " + e.getMessage(), e);
            }
        }
    }
}
