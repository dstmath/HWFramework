package com.android.org.bouncycastle.jcajce.provider.symmetric;

import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.cms.GCMParameters;
import com.android.org.bouncycastle.util.Integers;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

class GcmSpecUtil {
    static final Class gcmSpecClass = lookup("javax.crypto.spec.GCMParameterSpec");

    GcmSpecUtil() {
    }

    static boolean gcmSpecExists() {
        return gcmSpecClass != null;
    }

    static boolean isGcmSpec(AlgorithmParameterSpec paramSpec) {
        return gcmSpecClass != null ? gcmSpecClass.isInstance(paramSpec) : false;
    }

    static boolean isGcmSpec(Class paramSpecClass) {
        return gcmSpecClass == paramSpecClass;
    }

    static AlgorithmParameterSpec extractGcmSpec(ASN1Primitive spec) throws InvalidParameterSpecException {
        try {
            GCMParameters gcmParams = GCMParameters.getInstance(spec);
            return (AlgorithmParameterSpec) gcmSpecClass.getConstructor(new Class[]{Integer.TYPE, byte[].class}).newInstance(new Object[]{Integers.valueOf(gcmParams.getIcvLen() * 8), gcmParams.getNonce()});
        } catch (NoSuchMethodException e) {
            throw new InvalidParameterSpecException("No constructor found!");
        } catch (Exception e2) {
            throw new InvalidParameterSpecException("Construction failed: " + e2.getMessage());
        }
    }

    static GCMParameters extractGcmParameters(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        try {
            return new GCMParameters((byte[]) gcmSpecClass.getDeclaredMethod("getIV", new Class[0]).invoke(paramSpec, new Object[0]), ((Integer) gcmSpecClass.getDeclaredMethod("getTLen", new Class[0]).invoke(paramSpec, new Object[0])).intValue() / 8);
        } catch (Exception e) {
            throw new InvalidParameterSpecException("Cannot process GCMParameterSpec");
        }
    }

    private static Class lookup(String className) {
        try {
            return GcmSpecUtil.class.getClassLoader().loadClass(className);
        } catch (Exception e) {
            return null;
        }
    }
}
