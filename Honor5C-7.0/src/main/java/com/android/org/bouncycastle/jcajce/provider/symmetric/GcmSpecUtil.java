package com.android.org.bouncycastle.jcajce.provider.symmetric;

import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.cms.GCMParameters;
import com.android.org.bouncycastle.util.Integers;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

class GcmSpecUtil {
    static final Class gcmSpecClass = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.jcajce.provider.symmetric.GcmSpecUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.jcajce.provider.symmetric.GcmSpecUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.symmetric.GcmSpecUtil.<clinit>():void");
    }

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
