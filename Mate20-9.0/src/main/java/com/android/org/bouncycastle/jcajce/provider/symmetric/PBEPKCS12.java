package com.android.org.bouncycastle.jcajce.provider.symmetric;

import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseAlgorithmParameters;
import com.android.org.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.PBEParameterSpec;

public class PBEPKCS12 {

    public static class AlgParams extends BaseAlgorithmParameters {
        PKCS12PBEParams params;

        /* access modifiers changed from: protected */
        public byte[] engineGetEncoded() {
            try {
                return this.params.getEncoded(ASN1Encoding.DER);
            } catch (IOException e) {
                throw new RuntimeException("Oooops! " + e.toString());
            }
        }

        /* access modifiers changed from: protected */
        public byte[] engineGetEncoded(String format) {
            if (isASN1FormatString(format)) {
                return engineGetEncoded();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public AlgorithmParameterSpec localEngineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
            if (paramSpec == PBEParameterSpec.class) {
                return new PBEParameterSpec(this.params.getIV(), this.params.getIterations().intValue());
            }
            throw new InvalidParameterSpecException("unknown parameter spec passed to PKCS12 PBE parameters object.");
        }

        /* access modifiers changed from: protected */
        public void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
            if (paramSpec instanceof PBEParameterSpec) {
                PBEParameterSpec pbeSpec = (PBEParameterSpec) paramSpec;
                this.params = new PKCS12PBEParams(pbeSpec.getSalt(), pbeSpec.getIterationCount());
                return;
            }
            throw new InvalidParameterSpecException("PBEParameterSpec required to initialise a PKCS12 PBE parameters algorithm parameters object");
        }

        /* access modifiers changed from: protected */
        public void engineInit(byte[] params2) throws IOException {
            this.params = PKCS12PBEParams.getInstance(ASN1Primitive.fromByteArray(params2));
        }

        /* access modifiers changed from: protected */
        public void engineInit(byte[] params2, String format) throws IOException {
            if (isASN1FormatString(format)) {
                engineInit(params2);
                return;
            }
            throw new IOException("Unknown parameters format in PKCS12 PBE parameters object");
        }

        /* access modifiers changed from: protected */
        public String engineToString() {
            return "PKCS12 PBE Parameters";
        }
    }

    public static class Mappings extends AlgorithmProvider {
        private static final String PREFIX = PBEPKCS12.class.getName();

        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("AlgorithmParameters.PKCS12PBE", PREFIX + "$AlgParams");
        }
    }

    private PBEPKCS12() {
    }
}
