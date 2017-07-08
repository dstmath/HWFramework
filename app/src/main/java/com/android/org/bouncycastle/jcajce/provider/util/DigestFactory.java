package com.android.org.bouncycastle.jcajce.provider.util;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.util.Strings;
import java.util.Map;
import java.util.Set;

public class DigestFactory {
    private static Set md5;
    private static Map oids;
    private static Set sha1;
    private static Set sha224;
    private static Set sha256;
    private static Set sha384;
    private static Set sha512;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.jcajce.provider.util.DigestFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.jcajce.provider.util.DigestFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.jcajce.provider.util.DigestFactory.<clinit>():void");
    }

    public static Digest getDigest(String digestName) {
        digestName = Strings.toUpperCase(digestName);
        if (sha1.contains(digestName)) {
            return AndroidDigestFactory.getSHA1();
        }
        if (md5.contains(digestName)) {
            return AndroidDigestFactory.getMD5();
        }
        if (sha224.contains(digestName)) {
            return AndroidDigestFactory.getSHA224();
        }
        if (sha256.contains(digestName)) {
            return AndroidDigestFactory.getSHA256();
        }
        if (sha384.contains(digestName)) {
            return AndroidDigestFactory.getSHA384();
        }
        if (sha512.contains(digestName)) {
            return AndroidDigestFactory.getSHA512();
        }
        return null;
    }

    public static boolean isSameDigest(String digest1, String digest2) {
        if ((sha1.contains(digest1) && sha1.contains(digest2)) || ((sha224.contains(digest1) && sha224.contains(digest2)) || ((sha256.contains(digest1) && sha256.contains(digest2)) || ((sha384.contains(digest1) && sha384.contains(digest2)) || (sha512.contains(digest1) && sha512.contains(digest2)))))) {
            return true;
        }
        if (md5.contains(digest1)) {
            return md5.contains(digest2);
        }
        return false;
    }

    public static ASN1ObjectIdentifier getOID(String digestName) {
        return (ASN1ObjectIdentifier) oids.get(digestName);
    }
}
