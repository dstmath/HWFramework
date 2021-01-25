package org.bouncycastle.openssl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;

public class PKCS8Generator implements PemObjectGenerator {
    public static final ASN1ObjectIdentifier AES_128_CBC = NISTObjectIdentifiers.id_aes128_CBC;
    public static final ASN1ObjectIdentifier AES_192_CBC = NISTObjectIdentifiers.id_aes192_CBC;
    public static final ASN1ObjectIdentifier AES_256_CBC = NISTObjectIdentifiers.id_aes256_CBC;
    public static final ASN1ObjectIdentifier DES3_CBC = PKCSObjectIdentifiers.des_EDE3_CBC;
    public static final ASN1ObjectIdentifier PBE_SHA1_2DES = PKCSObjectIdentifiers.pbeWithSHAAnd2_KeyTripleDES_CBC;
    public static final ASN1ObjectIdentifier PBE_SHA1_3DES = PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC;
    public static final ASN1ObjectIdentifier PBE_SHA1_RC2_128 = PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC2_CBC;
    public static final ASN1ObjectIdentifier PBE_SHA1_RC2_40 = PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC;
    public static final ASN1ObjectIdentifier PBE_SHA1_RC4_128 = PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4;
    public static final ASN1ObjectIdentifier PBE_SHA1_RC4_40 = PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC4;
    public static final AlgorithmIdentifier PRF_HMACGOST3411 = new AlgorithmIdentifier(CryptoProObjectIdentifiers.gostR3411Hmac, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_HMACSHA1 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA1, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_HMACSHA224 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA224, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_HMACSHA256 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA256, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_HMACSHA384 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA384, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_HMACSHA3_224 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_hmacWithSHA3_224, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_HMACSHA3_256 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_hmacWithSHA3_256, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_HMACSHA3_384 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_hmacWithSHA3_384, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_HMACSHA3_512 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_hmacWithSHA3_512, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_HMACSHA512 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE);
    private PrivateKeyInfo key;
    private OutputEncryptor outputEncryptor;

    public PKCS8Generator(PrivateKeyInfo privateKeyInfo, OutputEncryptor outputEncryptor2) {
        this.key = privateKeyInfo;
        this.outputEncryptor = outputEncryptor2;
    }

    private PemObject generate(PrivateKeyInfo privateKeyInfo, OutputEncryptor outputEncryptor2) throws PemGenerationException {
        try {
            byte[] encoded = privateKeyInfo.getEncoded();
            if (outputEncryptor2 == null) {
                return new PemObject("PRIVATE KEY", encoded);
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStream outputStream = outputEncryptor2.getOutputStream(byteArrayOutputStream);
            outputStream.write(privateKeyInfo.getEncoded());
            outputStream.close();
            return new PemObject("ENCRYPTED PRIVATE KEY", new EncryptedPrivateKeyInfo(outputEncryptor2.getAlgorithmIdentifier(), byteArrayOutputStream.toByteArray()).getEncoded());
        } catch (IOException e) {
            throw new PemGenerationException("unable to process encoded key data: " + e.getMessage(), e);
        }
    }

    @Override // org.bouncycastle.util.io.pem.PemObjectGenerator
    public PemObject generate() throws PemGenerationException {
        OutputEncryptor outputEncryptor2 = this.outputEncryptor;
        return outputEncryptor2 != null ? generate(this.key, outputEncryptor2) : generate(this.key, null);
    }
}
