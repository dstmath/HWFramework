package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import com.android.org.bouncycastle.asn1.ASN1Null;
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

    /* access modifiers changed from: protected */
    public boolean isASN1FormatString(String format) {
        return format == null || format.equals("ASN.1");
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
        if (algorithmParameterSpec instanceof ECGenParameterSpec) {
            ECGenParameterSpec ecGenParameterSpec = (ECGenParameterSpec) algorithmParameterSpec;
            X9ECParameters params = ECUtils.getDomainParametersFromGenSpec(ecGenParameterSpec);
            if (params != null) {
                this.curveName = ecGenParameterSpec.getName();
                this.ecParameterSpec = EC5Util.convertToSpec(params);
                return;
            }
            throw new InvalidParameterSpecException("EC curve name not recognized: " + ecGenParameterSpec.getName());
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

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes) throws IOException {
        engineInit(bytes, "ASN.1");
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes, String format) throws IOException {
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

    /* access modifiers changed from: protected */
    public <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec) throws InvalidParameterSpecException {
        if (ECParameterSpec.class.isAssignableFrom(paramSpec) || paramSpec == AlgorithmParameterSpec.class) {
            return this.ecParameterSpec;
        }
        if (ECGenParameterSpec.class.isAssignableFrom(paramSpec)) {
            if (this.curveName != null) {
                ASN1ObjectIdentifier namedCurveOid = ECUtil.getNamedCurveOid(this.curveName);
                if (namedCurveOid != null) {
                    return new ECGenParameterSpec(namedCurveOid.getId());
                }
                return new ECGenParameterSpec(this.curveName);
            }
            ASN1ObjectIdentifier namedCurveOid2 = ECUtil.getNamedCurveOid(EC5Util.convertSpec(this.ecParameterSpec, false));
            if (namedCurveOid2 != null) {
                return new ECGenParameterSpec(namedCurveOid2.getId());
            }
        }
        throw new InvalidParameterSpecException("EC AlgorithmParameters cannot convert to " + paramSpec.getName());
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded() throws IOException {
        return engineGetEncoded("ASN.1");
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded(String format) throws IOException {
        X962Parameters params;
        if (isASN1FormatString(format)) {
            if (this.ecParameterSpec == null) {
                params = new X962Parameters((ASN1Null) DERNull.INSTANCE);
            } else if (this.curveName != null) {
                params = new X962Parameters(ECUtil.getNamedCurveOid(this.curveName));
            } else {
                com.android.org.bouncycastle.jce.spec.ECParameterSpec ecSpec = EC5Util.convertSpec(this.ecParameterSpec, false);
                X9ECParameters ecP = new X9ECParameters(ecSpec.getCurve(), ecSpec.getG(), ecSpec.getN(), ecSpec.getH(), ecSpec.getSeed());
                params = new X962Parameters(ecP);
            }
            return params.getEncoded();
        }
        throw new IOException("Unknown parameters format in AlgorithmParameters object: " + format);
    }

    /* access modifiers changed from: protected */
    public String engineToString() {
        return "EC AlgorithmParameters ";
    }
}
