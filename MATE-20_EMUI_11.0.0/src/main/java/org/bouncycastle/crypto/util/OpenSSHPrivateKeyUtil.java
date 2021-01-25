package org.bouncycastle.crypto.util;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Strings;

public class OpenSSHPrivateKeyUtil {
    static final byte[] AUTH_MAGIC = Strings.toByteArray("openssh-key-v1\u0000");

    private OpenSSHPrivateKeyUtil() {
    }

    private static boolean allIntegers(ASN1Sequence aSN1Sequence) {
        for (int i = 0; i < aSN1Sequence.size(); i++) {
            if (!(aSN1Sequence.getObjectAt(i) instanceof ASN1Integer)) {
                return false;
            }
        }
        return true;
    }

    public static byte[] encodePrivateKey(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        if (asymmetricKeyParameter == null) {
            throw new IllegalArgumentException("param is null");
        } else if (asymmetricKeyParameter instanceof RSAPrivateCrtKeyParameters) {
            return PrivateKeyInfoFactory.createPrivateKeyInfo(asymmetricKeyParameter).parsePrivateKey().toASN1Primitive().getEncoded();
        } else {
            if (asymmetricKeyParameter instanceof ECPrivateKeyParameters) {
                return PrivateKeyInfoFactory.createPrivateKeyInfo(asymmetricKeyParameter).parsePrivateKey().toASN1Primitive().getEncoded();
            }
            if (asymmetricKeyParameter instanceof DSAPrivateKeyParameters) {
                DSAPrivateKeyParameters dSAPrivateKeyParameters = (DSAPrivateKeyParameters) asymmetricKeyParameter;
                DSAParameters parameters = dSAPrivateKeyParameters.getParameters();
                ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                aSN1EncodableVector.add(new ASN1Integer(0));
                aSN1EncodableVector.add(new ASN1Integer(parameters.getP()));
                aSN1EncodableVector.add(new ASN1Integer(parameters.getQ()));
                aSN1EncodableVector.add(new ASN1Integer(parameters.getG()));
                aSN1EncodableVector.add(new ASN1Integer(parameters.getG().modPow(dSAPrivateKeyParameters.getX(), parameters.getP())));
                aSN1EncodableVector.add(new ASN1Integer(dSAPrivateKeyParameters.getX()));
                try {
                    return new DERSequence(aSN1EncodableVector).getEncoded();
                } catch (Exception e) {
                    throw new IllegalStateException("unable to encode DSAPrivateKeyParameters " + e.getMessage());
                }
            } else if (asymmetricKeyParameter instanceof Ed25519PrivateKeyParameters) {
                Ed25519PrivateKeyParameters ed25519PrivateKeyParameters = (Ed25519PrivateKeyParameters) asymmetricKeyParameter;
                Ed25519PublicKeyParameters generatePublicKey = ed25519PrivateKeyParameters.generatePublicKey();
                SSHBuilder sSHBuilder = new SSHBuilder();
                sSHBuilder.writeBytes(AUTH_MAGIC);
                sSHBuilder.writeString("none");
                sSHBuilder.writeString("none");
                sSHBuilder.writeString("");
                sSHBuilder.u32(1);
                sSHBuilder.writeBlock(OpenSSHPublicKeyUtil.encodePublicKey(generatePublicKey));
                SSHBuilder sSHBuilder2 = new SSHBuilder();
                int nextInt = CryptoServicesRegistrar.getSecureRandom().nextInt();
                sSHBuilder2.u32(nextInt);
                sSHBuilder2.u32(nextInt);
                sSHBuilder2.writeString("ssh-ed25519");
                byte[] encoded = generatePublicKey.getEncoded();
                sSHBuilder2.writeBlock(encoded);
                sSHBuilder2.writeBlock(Arrays.concatenate(ed25519PrivateKeyParameters.getEncoded(), encoded));
                sSHBuilder2.writeString("");
                sSHBuilder.writeBlock(sSHBuilder2.getPaddedBytes());
                return sSHBuilder.getBytes();
            } else {
                throw new IllegalArgumentException("unable to convert " + asymmetricKeyParameter.getClass().getName() + " to openssh private key");
            }
        }
    }

    public static AsymmetricKeyParameter parsePrivateKeyBlob(byte[] bArr) {
        AsymmetricKeyParameter asymmetricKeyParameter;
        if (bArr[0] == 48) {
            ASN1Sequence instance = ASN1Sequence.getInstance(bArr);
            if (instance.size() == 6) {
                if (allIntegers(instance) && ((ASN1Integer) instance.getObjectAt(0)).getPositiveValue().equals(BigIntegers.ZERO)) {
                    asymmetricKeyParameter = new DSAPrivateKeyParameters(((ASN1Integer) instance.getObjectAt(5)).getPositiveValue(), new DSAParameters(((ASN1Integer) instance.getObjectAt(1)).getPositiveValue(), ((ASN1Integer) instance.getObjectAt(2)).getPositiveValue(), ((ASN1Integer) instance.getObjectAt(3)).getPositiveValue()));
                }
            } else if (instance.size() == 9) {
                if (allIntegers(instance) && ((ASN1Integer) instance.getObjectAt(0)).getPositiveValue().equals(BigIntegers.ZERO)) {
                    RSAPrivateKey instance2 = RSAPrivateKey.getInstance(instance);
                    asymmetricKeyParameter = new RSAPrivateCrtKeyParameters(instance2.getModulus(), instance2.getPublicExponent(), instance2.getPrivateExponent(), instance2.getPrime1(), instance2.getPrime2(), instance2.getExponent1(), instance2.getExponent2(), instance2.getCoefficient());
                }
            } else if (instance.size() == 4 && (instance.getObjectAt(3) instanceof ASN1TaggedObject) && (instance.getObjectAt(2) instanceof ASN1TaggedObject)) {
                ECPrivateKey instance3 = ECPrivateKey.getInstance(instance);
                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) instance3.getParameters();
                X9ECParameters byOID = ECNamedCurveTable.getByOID(aSN1ObjectIdentifier);
                asymmetricKeyParameter = new ECPrivateKeyParameters(instance3.getKey(), new ECNamedDomainParameters(aSN1ObjectIdentifier, byOID.getCurve(), byOID.getG(), byOID.getN(), byOID.getH(), byOID.getSeed()));
            }
            asymmetricKeyParameter = null;
        } else {
            SSHBuffer sSHBuffer = new SSHBuffer(AUTH_MAGIC, bArr);
            if ("none".equals(sSHBuffer.readString())) {
                sSHBuffer.skipBlock();
                sSHBuffer.skipBlock();
                if (sSHBuffer.readU32() == 1) {
                    OpenSSHPublicKeyUtil.parsePublicKey(sSHBuffer.readBlock());
                    byte[] readPaddedBlock = sSHBuffer.readPaddedBlock();
                    if (!sSHBuffer.hasRemaining()) {
                        SSHBuffer sSHBuffer2 = new SSHBuffer(readPaddedBlock);
                        if (sSHBuffer2.readU32() == sSHBuffer2.readU32()) {
                            String readString = sSHBuffer2.readString();
                            if ("ssh-ed25519".equals(readString)) {
                                sSHBuffer2.skipBlock();
                                byte[] readBlock = sSHBuffer2.readBlock();
                                if (readBlock.length == 64) {
                                    Ed25519PrivateKeyParameters ed25519PrivateKeyParameters = new Ed25519PrivateKeyParameters(readBlock, 0);
                                    sSHBuffer2.skipBlock();
                                    if (!sSHBuffer2.hasRemaining()) {
                                        asymmetricKeyParameter = ed25519PrivateKeyParameters;
                                    } else {
                                        throw new IllegalArgumentException("private key block has trailing data");
                                    }
                                } else {
                                    throw new IllegalStateException("private key value of wrong length");
                                }
                            } else {
                                throw new IllegalStateException("can not parse private key of type " + readString);
                            }
                        } else {
                            throw new IllegalStateException("private key check values are not the same");
                        }
                    } else {
                        throw new IllegalArgumentException("decoded key has trailing data");
                    }
                } else {
                    throw new IllegalStateException("multiple keys not supported");
                }
            } else {
                throw new IllegalStateException("encrypted keys not supported");
            }
        }
        if (asymmetricKeyParameter != null) {
            return asymmetricKeyParameter;
        }
        throw new IllegalArgumentException("unable to parse key");
    }
}
