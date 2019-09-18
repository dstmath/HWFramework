package org.bouncycastle.dvcs;

import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.dvcs.DVCSObjectIdentifiers;
import org.bouncycastle.asn1.dvcs.DVCSRequest;
import org.bouncycastle.asn1.dvcs.DVCSRequestInformationBuilder;
import org.bouncycastle.asn1.dvcs.Data;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cms.CMSSignedDataGenerator;

public abstract class DVCSRequestBuilder {
    private final ExtensionsGenerator extGenerator = new ExtensionsGenerator();
    protected final DVCSRequestInformationBuilder requestInformationBuilder;
    private final CMSSignedDataGenerator signedDataGen = new CMSSignedDataGenerator();

    protected DVCSRequestBuilder(DVCSRequestInformationBuilder dVCSRequestInformationBuilder) {
        this.requestInformationBuilder = dVCSRequestInformationBuilder;
    }

    public void addExtension(ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, ASN1Encodable aSN1Encodable) throws DVCSException {
        try {
            this.extGenerator.addExtension(aSN1ObjectIdentifier, z, aSN1Encodable);
        } catch (IOException e) {
            throw new DVCSException("cannot encode extension: " + e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    public DVCSRequest createDVCRequest(Data data) throws DVCSException {
        if (!this.extGenerator.isEmpty()) {
            this.requestInformationBuilder.setExtensions(this.extGenerator.generate());
        }
        return new DVCSRequest(new ContentInfo(DVCSObjectIdentifiers.id_ct_DVCSRequestData, new DVCSRequest(this.requestInformationBuilder.build(), data)));
    }

    public void setDVCS(GeneralName generalName) {
        this.requestInformationBuilder.setDVCS(generalName);
    }

    public void setDVCS(GeneralNames generalNames) {
        this.requestInformationBuilder.setDVCS(generalNames);
    }

    public void setDataLocations(GeneralName generalName) {
        this.requestInformationBuilder.setDataLocations(generalName);
    }

    public void setDataLocations(GeneralNames generalNames) {
        this.requestInformationBuilder.setDataLocations(generalNames);
    }

    public void setNonce(BigInteger bigInteger) {
        this.requestInformationBuilder.setNonce(bigInteger);
    }

    public void setRequester(GeneralName generalName) {
        this.requestInformationBuilder.setRequester(generalName);
    }
}
