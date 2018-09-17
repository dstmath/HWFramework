package com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa;

import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.RSAESOAEPparams;
import com.android.org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.jcajce.provider.util.DigestFactory;
import com.android.org.bouncycastle.jcajce.util.MessageDigestUtils;
import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;

public abstract class AlgorithmParametersSpi extends java.security.AlgorithmParametersSpi {

    public static class OAEP extends AlgorithmParametersSpi {
        OAEPParameterSpec currentSpec;

        protected byte[] engineGetEncoded() {
            try {
                return new RSAESOAEPparams(new AlgorithmIdentifier(DigestFactory.getOID(this.currentSpec.getDigestAlgorithm()), DERNull.INSTANCE), new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, new AlgorithmIdentifier(DigestFactory.getOID(((MGF1ParameterSpec) this.currentSpec.getMGFParameters()).getDigestAlgorithm()), DERNull.INSTANCE)), new AlgorithmIdentifier(PKCSObjectIdentifiers.id_pSpecified, new DEROctetString(((PSpecified) this.currentSpec.getPSource()).getValue()))).getEncoded(ASN1Encoding.DER);
            } catch (IOException e) {
                throw new RuntimeException("Error encoding OAEPParameters");
            }
        }

        protected byte[] engineGetEncoded(String format) {
            if (isASN1FormatString(format) || format.equalsIgnoreCase("X.509")) {
                return engineGetEncoded();
            }
            return null;
        }

        protected AlgorithmParameterSpec localEngineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
            if (paramSpec == OAEPParameterSpec.class || paramSpec == AlgorithmParameterSpec.class) {
                return this.currentSpec;
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to OAEP parameters object.");
        }

        protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
            if (paramSpec instanceof OAEPParameterSpec) {
                this.currentSpec = (OAEPParameterSpec) paramSpec;
                return;
            }
            throw new InvalidParameterSpecException("OAEPParameterSpec required to initialise an OAEP algorithm parameters object");
        }

        protected void engineInit(byte[] params) throws IOException {
            try {
                RSAESOAEPparams oaepP = RSAESOAEPparams.getInstance(params);
                if (oaepP.getMaskGenAlgorithm().getAlgorithm().equals(PKCSObjectIdentifiers.id_mgf1)) {
                    this.currentSpec = new OAEPParameterSpec(MessageDigestUtils.getDigestName(oaepP.getHashAlgorithm().getAlgorithm()), OAEPParameterSpec.DEFAULT.getMGFAlgorithm(), new MGF1ParameterSpec(MessageDigestUtils.getDigestName(AlgorithmIdentifier.getInstance(oaepP.getMaskGenAlgorithm().getParameters()).getAlgorithm())), new PSpecified(ASN1OctetString.getInstance(oaepP.getPSourceAlgorithm().getParameters()).getOctets()));
                    return;
                }
                throw new IOException("unknown mask generation function: " + oaepP.getMaskGenAlgorithm().getAlgorithm());
            } catch (ClassCastException e) {
                throw new IOException("Not a valid OAEP Parameter encoding.");
            } catch (ArrayIndexOutOfBoundsException e2) {
                throw new IOException("Not a valid OAEP Parameter encoding.");
            }
        }

        protected void engineInit(byte[] params, String format) throws IOException {
            if (format.equalsIgnoreCase("X.509") || format.equalsIgnoreCase("ASN.1")) {
                engineInit(params);
                return;
            }
            throw new IOException("Unknown parameter format " + format);
        }

        protected String engineToString() {
            return "OAEP Parameters";
        }
    }

    public static class PSS extends AlgorithmParametersSpi {
        PSSParameterSpec currentSpec;

        protected byte[] engineGetEncoded() throws IOException {
            PSSParameterSpec pssSpec = this.currentSpec;
            return new RSASSAPSSparams(new AlgorithmIdentifier(DigestFactory.getOID(pssSpec.getDigestAlgorithm()), DERNull.INSTANCE), new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, new AlgorithmIdentifier(DigestFactory.getOID(((MGF1ParameterSpec) pssSpec.getMGFParameters()).getDigestAlgorithm()), DERNull.INSTANCE)), new ASN1Integer((long) pssSpec.getSaltLength()), new ASN1Integer((long) pssSpec.getTrailerField())).getEncoded(ASN1Encoding.DER);
        }

        protected byte[] engineGetEncoded(String format) throws IOException {
            if (format.equalsIgnoreCase("X.509") || format.equalsIgnoreCase("ASN.1")) {
                return engineGetEncoded();
            }
            return null;
        }

        protected AlgorithmParameterSpec localEngineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
            if (paramSpec == PSSParameterSpec.class && this.currentSpec != null) {
                return this.currentSpec;
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to PSS parameters object.");
        }

        protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
            if (paramSpec instanceof PSSParameterSpec) {
                this.currentSpec = (PSSParameterSpec) paramSpec;
                return;
            }
            throw new InvalidParameterSpecException("PSSParameterSpec required to initialise an PSS algorithm parameters object");
        }

        protected void engineInit(byte[] params) throws IOException {
            try {
                RSASSAPSSparams pssP = RSASSAPSSparams.getInstance(params);
                if (pssP.getMaskGenAlgorithm().getAlgorithm().equals(PKCSObjectIdentifiers.id_mgf1)) {
                    this.currentSpec = new PSSParameterSpec(MessageDigestUtils.getDigestName(pssP.getHashAlgorithm().getAlgorithm()), PSSParameterSpec.DEFAULT.getMGFAlgorithm(), new MGF1ParameterSpec(MessageDigestUtils.getDigestName(AlgorithmIdentifier.getInstance(pssP.getMaskGenAlgorithm().getParameters()).getAlgorithm())), pssP.getSaltLength().intValue(), pssP.getTrailerField().intValue());
                    return;
                }
                throw new IOException("unknown mask generation function: " + pssP.getMaskGenAlgorithm().getAlgorithm());
            } catch (ClassCastException e) {
                throw new IOException("Not a valid PSS Parameter encoding.");
            } catch (ArrayIndexOutOfBoundsException e2) {
                throw new IOException("Not a valid PSS Parameter encoding.");
            }
        }

        protected void engineInit(byte[] params, String format) throws IOException {
            if (isASN1FormatString(format) || format.equalsIgnoreCase("X.509")) {
                engineInit(params);
                return;
            }
            throw new IOException("Unknown parameter format " + format);
        }

        protected String engineToString() {
            return "PSS Parameters";
        }
    }

    protected abstract AlgorithmParameterSpec localEngineGetParameterSpec(Class cls) throws InvalidParameterSpecException;

    protected boolean isASN1FormatString(String format) {
        return format != null ? format.equals("ASN.1") : true;
    }

    protected AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        if (paramSpec != null) {
            return localEngineGetParameterSpec(paramSpec);
        }
        throw new NullPointerException("argument to getParameterSpec must not be null");
    }
}
