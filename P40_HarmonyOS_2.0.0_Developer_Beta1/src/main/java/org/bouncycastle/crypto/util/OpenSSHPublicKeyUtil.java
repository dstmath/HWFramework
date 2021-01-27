package org.bouncycastle.crypto.util;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.math.ec.ECCurve;

public class OpenSSHPublicKeyUtil {
    private static final String DSS = "ssh-dss";
    private static final String ECDSA = "ecdsa";
    private static final String ED_25519 = "ssh-ed25519";
    private static final String RSA = "ssh-rsa";

    private OpenSSHPublicKeyUtil() {
    }

    public static byte[] encodePublicKey(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        if (asymmetricKeyParameter == null) {
            throw new IllegalArgumentException("cipherParameters was null.");
        } else if (asymmetricKeyParameter instanceof RSAKeyParameters) {
            if (!asymmetricKeyParameter.isPrivate()) {
                RSAKeyParameters rSAKeyParameters = (RSAKeyParameters) asymmetricKeyParameter;
                SSHBuilder sSHBuilder = new SSHBuilder();
                sSHBuilder.writeString(RSA);
                sSHBuilder.writeBigNum(rSAKeyParameters.getExponent());
                sSHBuilder.writeBigNum(rSAKeyParameters.getModulus());
                return sSHBuilder.getBytes();
            }
            throw new IllegalArgumentException("RSAKeyParamaters was for encryption");
        } else if (asymmetricKeyParameter instanceof ECPublicKeyParameters) {
            SSHBuilder sSHBuilder2 = new SSHBuilder();
            ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters) asymmetricKeyParameter;
            String nameForParameters = SSHNamedCurves.getNameForParameters(eCPublicKeyParameters.getParameters());
            if (nameForParameters != null) {
                sSHBuilder2.writeString("ecdsa-sha2-" + nameForParameters);
                sSHBuilder2.writeString(nameForParameters);
                sSHBuilder2.writeBlock(eCPublicKeyParameters.getQ().getEncoded(false));
                return sSHBuilder2.getBytes();
            }
            throw new IllegalArgumentException("unable to derive ssh curve name for " + eCPublicKeyParameters.getParameters().getCurve().getClass().getName());
        } else if (asymmetricKeyParameter instanceof DSAPublicKeyParameters) {
            DSAPublicKeyParameters dSAPublicKeyParameters = (DSAPublicKeyParameters) asymmetricKeyParameter;
            DSAParameters parameters = dSAPublicKeyParameters.getParameters();
            SSHBuilder sSHBuilder3 = new SSHBuilder();
            sSHBuilder3.writeString(DSS);
            sSHBuilder3.writeBigNum(parameters.getP());
            sSHBuilder3.writeBigNum(parameters.getQ());
            sSHBuilder3.writeBigNum(parameters.getG());
            sSHBuilder3.writeBigNum(dSAPublicKeyParameters.getY());
            return sSHBuilder3.getBytes();
        } else if (asymmetricKeyParameter instanceof Ed25519PublicKeyParameters) {
            SSHBuilder sSHBuilder4 = new SSHBuilder();
            sSHBuilder4.writeString(ED_25519);
            sSHBuilder4.writeBlock(((Ed25519PublicKeyParameters) asymmetricKeyParameter).getEncoded());
            return sSHBuilder4.getBytes();
        } else {
            throw new IllegalArgumentException("unable to convert " + asymmetricKeyParameter.getClass().getName() + " to private key");
        }
    }

    public static AsymmetricKeyParameter parsePublicKey(SSHBuffer sSHBuffer) {
        AsymmetricKeyParameter asymmetricKeyParameter;
        String readString = sSHBuffer.readString();
        if (RSA.equals(readString)) {
            asymmetricKeyParameter = new RSAKeyParameters(false, sSHBuffer.readBigNumPositive(), sSHBuffer.readBigNumPositive());
        } else if (DSS.equals(readString)) {
            asymmetricKeyParameter = new DSAPublicKeyParameters(sSHBuffer.readBigNumPositive(), new DSAParameters(sSHBuffer.readBigNumPositive(), sSHBuffer.readBigNumPositive(), sSHBuffer.readBigNumPositive()));
        } else if (readString.startsWith(ECDSA)) {
            String readString2 = sSHBuffer.readString();
            ASN1ObjectIdentifier byName = SSHNamedCurves.getByName(readString2);
            X9ECParameters parameters = SSHNamedCurves.getParameters(byName);
            if (parameters != null) {
                ECCurve curve = parameters.getCurve();
                asymmetricKeyParameter = new ECPublicKeyParameters(curve.decodePoint(sSHBuffer.readBlock()), new ECNamedDomainParameters(byName, curve, parameters.getG(), parameters.getN(), parameters.getH(), parameters.getSeed()));
            } else {
                throw new IllegalStateException("unable to find curve for " + readString + " using curve name " + readString2);
            }
        } else if (ED_25519.equals(readString)) {
            byte[] readBlock = sSHBuffer.readBlock();
            if (readBlock.length == 32) {
                asymmetricKeyParameter = new Ed25519PublicKeyParameters(readBlock, 0);
            } else {
                throw new IllegalStateException("public key value of wrong length");
            }
        } else {
            asymmetricKeyParameter = null;
        }
        if (asymmetricKeyParameter == null) {
            throw new IllegalArgumentException("unable to parse key");
        } else if (!sSHBuffer.hasRemaining()) {
            return asymmetricKeyParameter;
        } else {
            throw new IllegalArgumentException("decoded key has trailing data");
        }
    }

    public static AsymmetricKeyParameter parsePublicKey(byte[] bArr) {
        return parsePublicKey(new SSHBuffer(bArr));
    }
}
