package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef;
import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;

public class ECParameters extends AlgorithmParametersSpi {
    private OpenSSLECGroupContext curve;

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
        if (algorithmParameterSpec instanceof ECGenParameterSpec) {
            String newCurveName = ((ECGenParameterSpec) algorithmParameterSpec).getName();
            OpenSSLECGroupContext newCurve = OpenSSLECGroupContext.getCurveByName(newCurveName);
            if (newCurve != null) {
                this.curve = newCurve;
                return;
            }
            throw new InvalidParameterSpecException("Unknown EC curve name: " + newCurveName);
        } else if (algorithmParameterSpec instanceof ECParameterSpec) {
            ECParameterSpec ecParamSpec = (ECParameterSpec) algorithmParameterSpec;
            try {
                OpenSSLECGroupContext newCurve2 = OpenSSLECGroupContext.getInstance(ecParamSpec);
                if (newCurve2 != null) {
                    this.curve = newCurve2;
                    return;
                }
                throw new InvalidParameterSpecException("Unknown EC curve: " + ecParamSpec);
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidParameterSpecException(e.getMessage());
            }
        } else {
            throw new InvalidParameterSpecException("Only ECParameterSpec and ECGenParameterSpec are supported");
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes) throws IOException {
        long ref = NativeCrypto.EC_KEY_parse_curve_name(bytes);
        if (ref != 0) {
            this.curve = new OpenSSLECGroupContext(new NativeRef.EC_GROUP(ref));
            return;
        }
        throw new IOException("Error reading ASN.1 encoding");
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bytes, String format) throws IOException {
        if (format == null || format.equals("ASN.1")) {
            engineInit(bytes);
            return;
        }
        throw new IOException("Unsupported format: " + format);
    }

    /* access modifiers changed from: protected */
    public <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> aClass) throws InvalidParameterSpecException {
        if (aClass == ECParameterSpec.class) {
            return this.curve.getECParameterSpec();
        }
        if (aClass == ECGenParameterSpec.class) {
            return new ECGenParameterSpec(Platform.getCurveName(this.curve.getECParameterSpec()));
        }
        throw new InvalidParameterSpecException("Unsupported class: " + aClass);
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded() throws IOException {
        return NativeCrypto.EC_KEY_marshal_curve_name(this.curve.getNativeRef());
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded(String format) throws IOException {
        if (format == null || format.equals("ASN.1")) {
            return engineGetEncoded();
        }
        throw new IOException("Unsupported format: " + format);
    }

    /* access modifiers changed from: protected */
    public String engineToString() {
        return "Conscrypt EC AlgorithmParameters";
    }
}
