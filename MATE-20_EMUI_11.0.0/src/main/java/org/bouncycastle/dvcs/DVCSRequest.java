package org.bouncycastle.dvcs;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.dvcs.DVCSObjectIdentifiers;
import org.bouncycastle.asn1.dvcs.ServiceType;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cms.CMSSignedData;

public class DVCSRequest extends DVCSMessage {
    private org.bouncycastle.asn1.dvcs.DVCSRequest asn1;
    private DVCSRequestData data;
    private DVCSRequestInfo reqInfo;

    public DVCSRequest(ContentInfo contentInfo) throws DVCSConstructionException {
        super(contentInfo);
        DVCSRequestData cCPDRequestData;
        if (DVCSObjectIdentifiers.id_ct_DVCSRequestData.equals((ASN1Primitive) contentInfo.getContentType())) {
            try {
                this.asn1 = contentInfo.getContent().toASN1Primitive() instanceof ASN1Sequence ? org.bouncycastle.asn1.dvcs.DVCSRequest.getInstance(contentInfo.getContent()) : org.bouncycastle.asn1.dvcs.DVCSRequest.getInstance(ASN1OctetString.getInstance(contentInfo.getContent()).getOctets());
                this.reqInfo = new DVCSRequestInfo(this.asn1.getRequestInformation());
                int serviceType = this.reqInfo.getServiceType();
                if (serviceType == ServiceType.CPD.getValue().intValue()) {
                    cCPDRequestData = new CPDRequestData(this.asn1.getData());
                } else if (serviceType == ServiceType.VSD.getValue().intValue()) {
                    cCPDRequestData = new VSDRequestData(this.asn1.getData());
                } else if (serviceType == ServiceType.VPKC.getValue().intValue()) {
                    cCPDRequestData = new VPKCRequestData(this.asn1.getData());
                } else if (serviceType == ServiceType.CCPD.getValue().intValue()) {
                    cCPDRequestData = new CCPDRequestData(this.asn1.getData());
                } else {
                    throw new DVCSConstructionException("Unknown service type: " + serviceType);
                }
                this.data = cCPDRequestData;
            } catch (Exception e) {
                throw new DVCSConstructionException("Unable to parse content: " + e.getMessage(), e);
            }
        } else {
            throw new DVCSConstructionException("ContentInfo not a DVCS Request");
        }
    }

    public DVCSRequest(CMSSignedData cMSSignedData) throws DVCSConstructionException {
        this(SignedData.getInstance(cMSSignedData.toASN1Structure().getContent()).getEncapContentInfo());
    }

    @Override // org.bouncycastle.dvcs.DVCSMessage
    public ASN1Encodable getContent() {
        return this.asn1;
    }

    public DVCSRequestData getData() {
        return this.data;
    }

    public DVCSRequestInfo getRequestInfo() {
        return this.reqInfo;
    }

    public GeneralName getTransactionIdentifier() {
        return this.asn1.getTransactionIdentifier();
    }
}
