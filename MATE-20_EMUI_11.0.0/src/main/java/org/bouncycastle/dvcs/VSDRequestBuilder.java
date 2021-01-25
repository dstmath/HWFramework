package org.bouncycastle.dvcs;

import java.io.IOException;
import java.util.Date;
import org.bouncycastle.asn1.dvcs.DVCSRequestInformationBuilder;
import org.bouncycastle.asn1.dvcs.DVCSTime;
import org.bouncycastle.asn1.dvcs.Data;
import org.bouncycastle.asn1.dvcs.ServiceType;
import org.bouncycastle.cms.CMSSignedData;

public class VSDRequestBuilder extends DVCSRequestBuilder {
    public VSDRequestBuilder() {
        super(new DVCSRequestInformationBuilder(ServiceType.VSD));
    }

    public DVCSRequest build(CMSSignedData cMSSignedData) throws DVCSException {
        try {
            return createDVCRequest(new Data(cMSSignedData.getEncoded()));
        } catch (IOException e) {
            throw new DVCSException("Failed to encode CMS signed data", e);
        }
    }

    public void setRequestTime(Date date) {
        this.requestInformationBuilder.setRequestTime(new DVCSTime(date));
    }
}
