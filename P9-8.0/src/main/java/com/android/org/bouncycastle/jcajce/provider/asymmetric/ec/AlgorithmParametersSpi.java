package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.x9.ECNamedCurveTable;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.jce.spec.ECNamedCurveSpec;
import com.android.org.bouncycastle.math.ec.ECCurve;
import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;

public class AlgorithmParametersSpi extends java.security.AlgorithmParametersSpi {
    private String curveName;
    private ECParameterSpec ecParameterSpec;

    protected boolean isASN1FormatString(String format) {
        return format != null ? format.equals("ASN.1") : true;
    }

    protected void engineInit(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
        if (algorithmParameterSpec instanceof ECGenParameterSpec) {
            ECGenParameterSpec ecGenParameterSpec = (ECGenParameterSpec) algorithmParameterSpec;
            X9ECParameters params = ECUtils.getDomainParametersFromGenSpec(ecGenParameterSpec);
            if (params == null) {
                throw new InvalidParameterSpecException("EC curve name not recognized: " + ecGenParameterSpec.getName());
            }
            this.curveName = ecGenParameterSpec.getName();
            this.ecParameterSpec = EC5Util.convertToSpec(params);
        } else if (algorithmParameterSpec instanceof ECParameterSpec) {
            if (algorithmParameterSpec instanceof ECNamedCurveSpec) {
                this.curveName = ((ECNamedCurveSpec) algorithmParameterSpec).getName();
            } else {
                this.curveName = null;
            }
            this.ecParameterSpec = (ECParameterSpec) algorithmParameterSpec;
        } else {
            throw new InvalidParameterSpecException("AlgorithmParameterSpec class not recognized: " + algorithmParameterSpec.getClass().getName());
        }
    }

    protected void engineInit(byte[] bytes) throws IOException {
        engineInit(bytes, "ASN.1");
    }

    protected void engineInit(byte[] bytes, String format) throws IOException {
        if (isASN1FormatString(format)) {
            X962Parameters params = X962Parameters.getInstance(bytes);
            ECCurve curve = EC5Util.getCurve(BouncyCastleProvider.CONFIGURATION, params);
            if (params.isNamedCurve()) {
                ASN1ObjectIdentifier curveId = ASN1ObjectIdentifier.getInstance(params.getParameters());
                this.curveName = ECNamedCurveTable.getName(curveId);
                if (this.curveName == null) {
                    this.curveName = curveId.getId();
                }
            }
            this.ecParameterSpec = EC5Util.convertToSpec(params, curve);
            return;
        }
        throw new IOException("Unknown encoded parameters format in AlgorithmParameters object: " + format);
    }

    protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec) throws InvalidParameterSpecException {
        if (ECParameterSpec.class.isAssignableFrom(paramSpec) || paramSpec == AlgorithmParameterSpec.class) {
            return this.ecParameterSpec;
        }
        if (ECGenParameterSpec.class.isAssignableFrom(paramSpec)) {
            ASN1ObjectIdentifier namedCurveOid;
            if (this.curveName != null) {
                namedCurveOid = ECUtil.getNamedCurveOid(this.curveName);
                if (namedCurveOid != null) {
                    return new ECGenParameterSpec(namedCurveOid.getId());
                }
                return new ECGenParameterSpec(this.curveName);
            }
            namedCurveOid = ECUtil.getNamedCurveOid(EC5Util.convertSpec(this.ecParameterSpec, false));
            if (namedCurveOid != null) {
                return new ECGenParameterSpec(namedCurveOid.getId());
            }
        }
        throw new InvalidParameterSpecException("EC AlgorithmParameters cannot convert to " + paramSpec.getName());
    }

    protected byte[] engineGetEncoded() throws IOException {
        return engineGetEncoded("ASN.1");
    }

    protected byte[] engineGetEncoded(String format) throws IOException {
        if (isASN1FormatString(format)) {
            X962Parameters params;
            if (this.ecParameterSpec == null) {
                params = new X962Parameters(DERNull.INSTANCE);
            } else if (this.curveName != null) {
                params = new X962Parameters(ECUtil.getNamedCurveOid(this.curveName));
            } else {
                com.android.org.bouncycastle.jce.spec.ECParameterSpec ecSpec = EC5Util.convertSpec(this.ecParameterSpec, false);
                params = new X962Parameters(new X9ECParameters(ecSpec.getCurve(), ecSpec.getG(), ecSpec.getN(), ecSpec.getH(), ecSpec.getSeed()));
            }
            return params.getEncoded();
        }
        throw new IOException("Unknown parameters format in AlgorithmParameters object: " + format);
    }

    protected String engineToString() {
        return "EC AlgorithmParameters ";
    }
}
