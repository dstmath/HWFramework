package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.crypto.Digest;

public final class AndroidDigestFactory {
    private static final String BouncyCastleFactoryClassName = null;
    private static final AndroidDigestFactoryInterface FACTORY = null;
    private static final String OpenSSLFactoryClassName = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory.<clinit>():void");
    }

    public static Digest getMD5() {
        return FACTORY.getMD5();
    }

    public static Digest getSHA1() {
        return FACTORY.getSHA1();
    }

    public static Digest getSHA224() {
        return FACTORY.getSHA224();
    }

    public static Digest getSHA256() {
        return FACTORY.getSHA256();
    }

    public static Digest getSHA384() {
        return FACTORY.getSHA384();
    }

    public static Digest getSHA512() {
        return FACTORY.getSHA512();
    }
}
