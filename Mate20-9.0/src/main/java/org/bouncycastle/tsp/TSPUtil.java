package org.bouncycastle.tsp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Integers;

public class TSPUtil {
    private static List EMPTY_LIST = Collections.unmodifiableList(new ArrayList());
    private static final Map digestLengths = new HashMap();
    private static final Map digestNames = new HashMap();

    static {
        digestLengths.put(PKCSObjectIdentifiers.md5.getId(), Integers.valueOf(16));
        digestLengths.put(OIWObjectIdentifiers.idSHA1.getId(), Integers.valueOf(20));
        digestLengths.put(NISTObjectIdentifiers.id_sha224.getId(), Integers.valueOf(28));
        digestLengths.put(NISTObjectIdentifiers.id_sha256.getId(), Integers.valueOf(32));
        digestLengths.put(NISTObjectIdentifiers.id_sha384.getId(), Integers.valueOf(48));
        digestLengths.put(NISTObjectIdentifiers.id_sha512.getId(), Integers.valueOf(64));
        digestLengths.put(TeleTrusTObjectIdentifiers.ripemd128.getId(), Integers.valueOf(16));
        digestLengths.put(TeleTrusTObjectIdentifiers.ripemd160.getId(), Integers.valueOf(20));
        digestLengths.put(TeleTrusTObjectIdentifiers.ripemd256.getId(), Integers.valueOf(32));
        digestLengths.put(CryptoProObjectIdentifiers.gostR3411.getId(), Integers.valueOf(32));
        digestLengths.put(RosstandartObjectIdentifiers.id_tc26_gost_3411_12_256.getId(), Integers.valueOf(32));
        digestLengths.put(RosstandartObjectIdentifiers.id_tc26_gost_3411_12_512.getId(), Integers.valueOf(64));
        digestLengths.put(GMObjectIdentifiers.sm3.getId(), Integers.valueOf(32));
        digestNames.put(PKCSObjectIdentifiers.md5.getId(), "MD5");
        digestNames.put(OIWObjectIdentifiers.idSHA1.getId(), "SHA1");
        digestNames.put(NISTObjectIdentifiers.id_sha224.getId(), "SHA224");
        digestNames.put(NISTObjectIdentifiers.id_sha256.getId(), "SHA256");
        digestNames.put(NISTObjectIdentifiers.id_sha384.getId(), "SHA384");
        digestNames.put(NISTObjectIdentifiers.id_sha512.getId(), "SHA512");
        digestNames.put(PKCSObjectIdentifiers.sha1WithRSAEncryption.getId(), "SHA1");
        digestNames.put(PKCSObjectIdentifiers.sha224WithRSAEncryption.getId(), "SHA224");
        digestNames.put(PKCSObjectIdentifiers.sha256WithRSAEncryption.getId(), "SHA256");
        digestNames.put(PKCSObjectIdentifiers.sha384WithRSAEncryption.getId(), "SHA384");
        digestNames.put(PKCSObjectIdentifiers.sha512WithRSAEncryption.getId(), "SHA512");
        digestNames.put(TeleTrusTObjectIdentifiers.ripemd128.getId(), "RIPEMD128");
        digestNames.put(TeleTrusTObjectIdentifiers.ripemd160.getId(), "RIPEMD160");
        digestNames.put(TeleTrusTObjectIdentifiers.ripemd256.getId(), "RIPEMD256");
        digestNames.put(CryptoProObjectIdentifiers.gostR3411.getId(), "GOST3411");
        digestNames.put(RosstandartObjectIdentifiers.id_tc26_gost_3411_12_256.getId(), "GOST3411-2012-256");
        digestNames.put(RosstandartObjectIdentifiers.id_tc26_gost_3411_12_512.getId(), "GOST3411-2012-512");
        digestNames.put(GMObjectIdentifiers.sm3.getId(), "SM3");
    }

    static void addExtension(ExtensionsGenerator extensionsGenerator, ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, ASN1Encodable aSN1Encodable) throws TSPIOException {
        try {
            extensionsGenerator.addExtension(aSN1ObjectIdentifier, z, aSN1Encodable);
        } catch (IOException e) {
            throw new TSPIOException("cannot encode extension: " + e.getMessage(), e);
        }
    }

    static int getDigestLength(String str) throws TSPException {
        Integer num = (Integer) digestLengths.get(str);
        if (num != null) {
            return num.intValue();
        }
        throw new TSPException("digest algorithm cannot be found.");
    }

    static List getExtensionOIDs(Extensions extensions) {
        return extensions == null ? EMPTY_LIST : Collections.unmodifiableList(Arrays.asList(extensions.getExtensionOIDs()));
    }

    public static Collection getSignatureTimestamps(SignerInformation signerInformation, DigestCalculatorProvider digestCalculatorProvider) throws TSPValidationException {
        ArrayList arrayList = new ArrayList();
        AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
        if (unsignedAttributes != null) {
            ASN1EncodableVector all = unsignedAttributes.getAll(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
            for (int i = 0; i < all.size(); i++) {
                ASN1Set attrValues = ((Attribute) all.get(i)).getAttrValues();
                int i2 = 0;
                while (i2 < attrValues.size()) {
                    try {
                        TimeStampToken timeStampToken = new TimeStampToken(ContentInfo.getInstance(attrValues.getObjectAt(i2)));
                        TimeStampTokenInfo timeStampInfo = timeStampToken.getTimeStampInfo();
                        DigestCalculator digestCalculator = digestCalculatorProvider.get(timeStampInfo.getHashAlgorithm());
                        OutputStream outputStream = digestCalculator.getOutputStream();
                        outputStream.write(signerInformation.getSignature());
                        outputStream.close();
                        if (org.bouncycastle.util.Arrays.constantTimeAreEqual(digestCalculator.getDigest(), timeStampInfo.getMessageImprintDigest())) {
                            arrayList.add(timeStampToken);
                            i2++;
                        } else {
                            throw new TSPValidationException("Incorrect digest in message imprint");
                        }
                    } catch (OperatorCreationException e) {
                        throw new TSPValidationException("Unknown hash algorithm specified in timestamp");
                    } catch (Exception e2) {
                        throw new TSPValidationException("Timestamp could not be parsed");
                    }
                }
            }
        }
        return arrayList;
    }

    public static void validateCertificate(X509CertificateHolder x509CertificateHolder) throws TSPValidationException {
        if (x509CertificateHolder.toASN1Structure().getVersionNumber() == 3) {
            Extension extension = x509CertificateHolder.getExtension(Extension.extendedKeyUsage);
            if (extension == null) {
                throw new TSPValidationException("Certificate must have an ExtendedKeyUsage extension.");
            } else if (extension.isCritical()) {
                ExtendedKeyUsage instance = ExtendedKeyUsage.getInstance(extension.getParsedValue());
                if (!instance.hasKeyPurposeId(KeyPurposeId.id_kp_timeStamping) || instance.size() != 1) {
                    throw new TSPValidationException("ExtendedKeyUsage not solely time stamping.");
                }
            } else {
                throw new TSPValidationException("Certificate must have an ExtendedKeyUsage extension marked as critical.");
            }
        } else {
            throw new IllegalArgumentException("Certificate must have an ExtendedKeyUsage extension.");
        }
    }
}
