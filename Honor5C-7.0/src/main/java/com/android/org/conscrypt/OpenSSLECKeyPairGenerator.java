package com.android.org.conscrypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public final class OpenSSLECKeyPairGenerator extends KeyPairGenerator {
    private static final String ALGORITHM = "EC";
    private static final int DEFAULT_KEY_SIZE = 256;
    private static final Map<Integer, String> SIZE_TO_CURVE_NAME = null;
    private OpenSSLECGroupContext group;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLECKeyPairGenerator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLECKeyPairGenerator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLECKeyPairGenerator.<clinit>():void");
    }

    public OpenSSLECKeyPairGenerator() {
        super(ALGORITHM);
    }

    public KeyPair generateKeyPair() {
        if (this.group == null) {
            String curveName = (String) SIZE_TO_CURVE_NAME.get(Integer.valueOf(DEFAULT_KEY_SIZE));
            this.group = OpenSSLECGroupContext.getCurveByName(curveName);
            if (this.group == null) {
                throw new RuntimeException("Curve not recognized: " + curveName);
            }
        }
        OpenSSLKey key = new OpenSSLKey(NativeCrypto.EC_KEY_generate_key(this.group.getNativeRef()));
        return new KeyPair(new OpenSSLECPublicKey(this.group, key), new OpenSSLECPrivateKey(this.group, key));
    }

    public void initialize(int keysize, SecureRandom random) {
        String name = (String) SIZE_TO_CURVE_NAME.get(Integer.valueOf(keysize));
        if (name == null) {
            throw new InvalidParameterException("unknown key size " + keysize);
        }
        OpenSSLECGroupContext possibleGroup = OpenSSLECGroupContext.getCurveByName(name);
        if (possibleGroup == null) {
            throw new InvalidParameterException("unknown curve " + name);
        }
        this.group = possibleGroup;
    }

    public void initialize(AlgorithmParameterSpec param, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (param instanceof ECParameterSpec) {
            this.group = OpenSSLECGroupContext.getInstance((ECParameterSpec) param);
        } else if (param instanceof ECGenParameterSpec) {
            String curveName = ((ECGenParameterSpec) param).getName();
            OpenSSLECGroupContext possibleGroup = OpenSSLECGroupContext.getCurveByName(curveName);
            if (possibleGroup == null) {
                throw new InvalidAlgorithmParameterException("unknown curve name: " + curveName);
            }
            this.group = possibleGroup;
        } else {
            throw new InvalidAlgorithmParameterException("parameter must be ECParameterSpec or ECGenParameterSpec");
        }
    }

    public static void assertCurvesAreValid() {
        ArrayList<String> invalidCurves = new ArrayList();
        for (String curveName : SIZE_TO_CURVE_NAME.values()) {
            if (OpenSSLECGroupContext.getCurveByName(curveName) == null) {
                invalidCurves.add(curveName);
            }
        }
        if (invalidCurves.size() > 0) {
            throw new AssertionError("Invalid curve names: " + Arrays.toString(invalidCurves.toArray()));
        }
    }
}
