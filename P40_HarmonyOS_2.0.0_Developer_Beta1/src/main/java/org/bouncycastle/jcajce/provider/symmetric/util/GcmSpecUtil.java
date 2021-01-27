package org.bouncycastle.jcajce.provider.symmetric.util;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.cms.GCMParameters;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Integers;

public class GcmSpecUtil {
    static final Class gcmSpecClass = ClassUtil.loadClass(GcmSpecUtil.class, "javax.crypto.spec.GCMParameterSpec");
    static final Method iv;
    static final Method tLen;

    static {
        Method method;
        if (gcmSpecClass != null) {
            tLen = extractMethod("getTLen");
            method = extractMethod("getIV");
        } else {
            method = null;
            tLen = null;
        }
        iv = method;
    }

    static AEADParameters extractAeadParameters(final KeyParameter keyParameter, final AlgorithmParameterSpec algorithmParameterSpec) throws InvalidAlgorithmParameterException {
        try {
            return (AEADParameters) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                /* class org.bouncycastle.jcajce.provider.symmetric.util.GcmSpecUtil.AnonymousClass2 */

                @Override // java.security.PrivilegedExceptionAction
                public Object run() throws Exception {
                    return new AEADParameters(keyParameter, ((Integer) GcmSpecUtil.tLen.invoke(algorithmParameterSpec, new Object[0])).intValue(), (byte[]) GcmSpecUtil.iv.invoke(algorithmParameterSpec, new Object[0]));
                }
            });
        } catch (Exception e) {
            throw new InvalidAlgorithmParameterException("Cannot process GCMParameterSpec.");
        }
    }

    public static GCMParameters extractGcmParameters(final AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
        try {
            return (GCMParameters) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                /* class org.bouncycastle.jcajce.provider.symmetric.util.GcmSpecUtil.AnonymousClass3 */

                @Override // java.security.PrivilegedExceptionAction
                public Object run() throws Exception {
                    return new GCMParameters((byte[]) GcmSpecUtil.iv.invoke(algorithmParameterSpec, new Object[0]), ((Integer) GcmSpecUtil.tLen.invoke(algorithmParameterSpec, new Object[0])).intValue() / 8);
                }
            });
        } catch (Exception e) {
            throw new InvalidParameterSpecException("Cannot process GCMParameterSpec");
        }
    }

    public static AlgorithmParameterSpec extractGcmSpec(ASN1Primitive aSN1Primitive) throws InvalidParameterSpecException {
        try {
            GCMParameters instance = GCMParameters.getInstance(aSN1Primitive);
            return (AlgorithmParameterSpec) gcmSpecClass.getConstructor(Integer.TYPE, byte[].class).newInstance(Integers.valueOf(instance.getIcvLen() * 8), instance.getNonce());
        } catch (NoSuchMethodException e) {
            throw new InvalidParameterSpecException("No constructor found!");
        } catch (Exception e2) {
            throw new InvalidParameterSpecException("Construction failed: " + e2.getMessage());
        }
    }

    private static Method extractMethod(final String str) {
        try {
            return (Method) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                /* class org.bouncycastle.jcajce.provider.symmetric.util.GcmSpecUtil.AnonymousClass1 */

                @Override // java.security.PrivilegedExceptionAction
                public Object run() throws Exception {
                    return GcmSpecUtil.gcmSpecClass.getDeclaredMethod(str, new Class[0]);
                }
            });
        } catch (PrivilegedActionException e) {
            return null;
        }
    }

    public static boolean gcmSpecExists() {
        return gcmSpecClass != null;
    }

    public static boolean isGcmSpec(Class cls) {
        return gcmSpecClass == cls;
    }

    public static boolean isGcmSpec(AlgorithmParameterSpec algorithmParameterSpec) {
        Class cls = gcmSpecClass;
        return cls != null && cls.isInstance(algorithmParameterSpec);
    }
}
