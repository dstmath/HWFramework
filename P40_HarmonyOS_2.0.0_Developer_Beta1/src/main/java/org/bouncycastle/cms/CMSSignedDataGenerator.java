package org.bouncycastle.cms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.cms.SignerInfo;

public class CMSSignedDataGenerator extends CMSSignedGenerator {
    private List signerInfs = new ArrayList();

    public CMSSignedData generate(CMSTypedData cMSTypedData) throws CMSException {
        return generate(cMSTypedData, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00d3  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00dc  */
    public CMSSignedData generate(CMSTypedData cMSTypedData, boolean z) throws CMSException {
        BEROctetString bEROctetString;
        if (this.signerInfs.isEmpty()) {
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
            this.digests.clear();
            for (SignerInformation signerInformation : this._signers) {
                aSN1EncodableVector.add(CMSSignedHelper.INSTANCE.fixAlgID(signerInformation.getDigestAlgorithmID()));
                aSN1EncodableVector2.add(signerInformation.toASN1Structure());
            }
            ASN1ObjectIdentifier contentType = cMSTypedData.getContentType();
            ASN1Set aSN1Set = null;
            if (cMSTypedData.getContent() != null) {
                ByteArrayOutputStream byteArrayOutputStream = z ? new ByteArrayOutputStream() : null;
                OutputStream safeOutputStream = CMSUtils.getSafeOutputStream(CMSUtils.attachSignersToOutputStream(this.signerGens, byteArrayOutputStream));
                try {
                    cMSTypedData.write(safeOutputStream);
                    safeOutputStream.close();
                    if (z) {
                        bEROctetString = new BEROctetString(byteArrayOutputStream.toByteArray());
                        for (SignerInfoGenerator signerInfoGenerator : this.signerGens) {
                            SignerInfo generate = signerInfoGenerator.generate(contentType);
                            aSN1EncodableVector.add(generate.getDigestAlgorithm());
                            aSN1EncodableVector2.add(generate);
                            byte[] calculatedDigest = signerInfoGenerator.getCalculatedDigest();
                            if (calculatedDigest != null) {
                                this.digests.put(generate.getDigestAlgorithm().getAlgorithm().getId(), calculatedDigest);
                            }
                        }
                        ASN1Set createBerSetFromList = this.certs.size() == 0 ? CMSUtils.createBerSetFromList(this.certs) : null;
                        if (this.crls.size() != 0) {
                            aSN1Set = CMSUtils.createBerSetFromList(this.crls);
                        }
                        return new CMSSignedData(cMSTypedData, new ContentInfo(CMSObjectIdentifiers.signedData, new SignedData(new DERSet(aSN1EncodableVector), new ContentInfo(contentType, bEROctetString), createBerSetFromList, aSN1Set, new DERSet(aSN1EncodableVector2))));
                    }
                } catch (IOException e) {
                    throw new CMSException("data processing exception: " + e.getMessage(), e);
                }
            }
            bEROctetString = null;
            while (r3.hasNext()) {
            }
            if (this.certs.size() == 0) {
            }
            if (this.crls.size() != 0) {
            }
            return new CMSSignedData(cMSTypedData, new ContentInfo(CMSObjectIdentifiers.signedData, new SignedData(new DERSet(aSN1EncodableVector), new ContentInfo(contentType, bEROctetString), createBerSetFromList, aSN1Set, new DERSet(aSN1EncodableVector2))));
        }
        throw new IllegalStateException("this method can only be used with SignerInfoGenerator");
    }

    public SignerInformationStore generateCounterSigners(SignerInformation signerInformation) throws CMSException {
        return generate(new CMSProcessableByteArray(null, signerInformation.getSignature()), false).getSignerInfos();
    }
}
