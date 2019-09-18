package org.bouncycastle.cms;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.cms.KEKRecipientInfo;
import org.bouncycastle.asn1.cms.KeyAgreeRecipientInfo;
import org.bouncycastle.asn1.cms.KeyTransRecipientInfo;
import org.bouncycastle.asn1.cms.PasswordRecipientInfo;
import org.bouncycastle.asn1.cms.RecipientInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.DigestCalculator;

class CMSEnvelopedHelper {

    static class CMSAuthenticatedSecureReadable implements CMSSecureReadable {
        private AlgorithmIdentifier algorithm;
        private CMSReadable readable;

        CMSAuthenticatedSecureReadable(AlgorithmIdentifier algorithmIdentifier, CMSReadable cMSReadable) {
            this.algorithm = algorithmIdentifier;
            this.readable = cMSReadable;
        }

        public InputStream getInputStream() throws IOException, CMSException {
            return this.readable.getInputStream();
        }
    }

    static class CMSDigestAuthenticatedSecureReadable implements CMSSecureReadable {
        /* access modifiers changed from: private */
        public DigestCalculator digestCalculator;
        private CMSReadable readable;

        public CMSDigestAuthenticatedSecureReadable(DigestCalculator digestCalculator2, CMSReadable cMSReadable) {
            this.digestCalculator = digestCalculator2;
            this.readable = cMSReadable;
        }

        public byte[] getDigest() {
            return this.digestCalculator.getDigest();
        }

        public InputStream getInputStream() throws IOException, CMSException {
            return new FilterInputStream(this.readable.getInputStream()) {
                public int read() throws IOException {
                    int read = this.in.read();
                    if (read >= 0) {
                        CMSDigestAuthenticatedSecureReadable.this.digestCalculator.getOutputStream().write(read);
                    }
                    return read;
                }

                public int read(byte[] bArr, int i, int i2) throws IOException {
                    int read = this.in.read(bArr, i, i2);
                    if (read >= 0) {
                        CMSDigestAuthenticatedSecureReadable.this.digestCalculator.getOutputStream().write(bArr, i, read);
                    }
                    return read;
                }
            };
        }
    }

    static class CMSEnvelopedSecureReadable implements CMSSecureReadable {
        private AlgorithmIdentifier algorithm;
        private CMSReadable readable;

        CMSEnvelopedSecureReadable(AlgorithmIdentifier algorithmIdentifier, CMSReadable cMSReadable) {
            this.algorithm = algorithmIdentifier;
            this.readable = cMSReadable;
        }

        public InputStream getInputStream() throws IOException, CMSException {
            return this.readable.getInputStream();
        }
    }

    CMSEnvelopedHelper() {
    }

    static RecipientInformationStore buildRecipientInformationStore(ASN1Set aSN1Set, AlgorithmIdentifier algorithmIdentifier, CMSSecureReadable cMSSecureReadable) {
        return buildRecipientInformationStore(aSN1Set, algorithmIdentifier, cMSSecureReadable, null);
    }

    static RecipientInformationStore buildRecipientInformationStore(ASN1Set aSN1Set, AlgorithmIdentifier algorithmIdentifier, CMSSecureReadable cMSSecureReadable, AuthAttributesProvider authAttributesProvider) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i != aSN1Set.size(); i++) {
            readRecipientInfo(arrayList, RecipientInfo.getInstance(aSN1Set.getObjectAt(i)), algorithmIdentifier, cMSSecureReadable, authAttributesProvider);
        }
        return new RecipientInformationStore((Collection<RecipientInformation>) arrayList);
    }

    private static void readRecipientInfo(List list, RecipientInfo recipientInfo, AlgorithmIdentifier algorithmIdentifier, CMSSecureReadable cMSSecureReadable, AuthAttributesProvider authAttributesProvider) {
        Object passwordRecipientInformation;
        ASN1Encodable info = recipientInfo.getInfo();
        if (info instanceof KeyTransRecipientInfo) {
            passwordRecipientInformation = new KeyTransRecipientInformation((KeyTransRecipientInfo) info, algorithmIdentifier, cMSSecureReadable, authAttributesProvider);
        } else if (info instanceof KEKRecipientInfo) {
            passwordRecipientInformation = new KEKRecipientInformation((KEKRecipientInfo) info, algorithmIdentifier, cMSSecureReadable, authAttributesProvider);
        } else if (info instanceof KeyAgreeRecipientInfo) {
            KeyAgreeRecipientInformation.readRecipientInfo(list, (KeyAgreeRecipientInfo) info, algorithmIdentifier, cMSSecureReadable, authAttributesProvider);
            return;
        } else if (info instanceof PasswordRecipientInfo) {
            passwordRecipientInformation = new PasswordRecipientInformation((PasswordRecipientInfo) info, algorithmIdentifier, cMSSecureReadable, authAttributesProvider);
        } else {
            return;
        }
        list.add(passwordRecipientInformation);
    }
}
