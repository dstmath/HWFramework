package java.util;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;
import sun.util.locale.LanguageTag;

public final class UUID implements Serializable, Comparable<UUID> {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final long serialVersionUID = -4856846361193249489L;
    private final long leastSigBits;
    private final long mostSigBits;

    private static class Holder {
        static final SecureRandom numberGenerator = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.UUID.Holder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.UUID.Holder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.UUID.Holder.<clinit>():void");
        }

        private Holder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.UUID.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.UUID.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.UUID.<clinit>():void");
    }

    private UUID(byte[] data) {
        int i;
        long msb = 0;
        long lsb = 0;
        if (!-assertionsDisabled) {
            if ((data.length == 16 ? 1 : null) == null) {
                throw new AssertionError((Object) "data must be 16 bytes in length");
            }
        }
        for (i = 0; i < 8; i++) {
            msb = (msb << 8) | ((long) (data[i] & 255));
        }
        for (i = 8; i < 16; i++) {
            lsb = (lsb << 8) | ((long) (data[i] & 255));
        }
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    public static UUID randomUUID() {
        byte[] randomBytes = new byte[16];
        Holder.numberGenerator.nextBytes(randomBytes);
        randomBytes[6] = (byte) (randomBytes[6] & 15);
        randomBytes[6] = (byte) (randomBytes[6] | 64);
        randomBytes[8] = (byte) (randomBytes[8] & 63);
        randomBytes[8] = (byte) (randomBytes[8] | Pattern.CANON_EQ);
        return new UUID(randomBytes);
    }

    public static UUID nameUUIDFromBytes(byte[] name) {
        try {
            byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(name);
            md5Bytes[6] = (byte) (md5Bytes[6] & 15);
            md5Bytes[6] = (byte) (md5Bytes[6] | 48);
            md5Bytes[8] = (byte) (md5Bytes[8] & 63);
            md5Bytes[8] = (byte) (md5Bytes[8] | Pattern.CANON_EQ);
            return new UUID(md5Bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("MD5 not supported");
        }
    }

    public static UUID fromString(String name) {
        String[] components = name.split(LanguageTag.SEP);
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        }
        for (int i = 0; i < 5; i++) {
            components[i] = "0x" + components[i];
        }
        return new UUID((((Long.decode(components[0]).longValue() << 16) | Long.decode(components[1]).longValue()) << 16) | Long.decode(components[2]).longValue(), (Long.decode(components[3]).longValue() << 48) | Long.decode(components[4]).longValue());
    }

    public long getLeastSignificantBits() {
        return this.leastSigBits;
    }

    public long getMostSignificantBits() {
        return this.mostSigBits;
    }

    public int version() {
        return (int) ((this.mostSigBits >> 12) & 15);
    }

    public int variant() {
        return (int) ((this.leastSigBits >>> ((int) (64 - (this.leastSigBits >>> 62)))) & (this.leastSigBits >> 63));
    }

    public long timestamp() {
        if (version() == 1) {
            return (((this.mostSigBits & 4095) << 48) | (((this.mostSigBits >> 16) & 65535) << 32)) | (this.mostSigBits >>> 32);
        }
        throw new UnsupportedOperationException("Not a time-based UUID");
    }

    public int clockSequence() {
        if (version() == 1) {
            return (int) ((this.leastSigBits & 4611404543450677248L) >>> 48);
        }
        throw new UnsupportedOperationException("Not a time-based UUID");
    }

    public long node() {
        if (version() == 1) {
            return this.leastSigBits & 281474976710655L;
        }
        throw new UnsupportedOperationException("Not a time-based UUID");
    }

    public String toString() {
        return digits(this.mostSigBits >> 32, 8) + LanguageTag.SEP + digits(this.mostSigBits >> 16, 4) + LanguageTag.SEP + digits(this.mostSigBits, 4) + LanguageTag.SEP + digits(this.leastSigBits >> 48, 4) + LanguageTag.SEP + digits(this.leastSigBits, 12);
    }

    private static String digits(long val, int digits) {
        long hi = 1 << (digits * 4);
        return Long.toHexString(((hi - 1) & val) | hi).substring(1);
    }

    public int hashCode() {
        long hilo = this.mostSigBits ^ this.leastSigBits;
        return ((int) (hilo >> 32)) ^ ((int) hilo);
    }

    public boolean equals(Object obj) {
        boolean z = -assertionsDisabled;
        if (obj == null || obj.getClass() != UUID.class) {
            return -assertionsDisabled;
        }
        UUID id = (UUID) obj;
        if (this.mostSigBits == id.mostSigBits && this.leastSigBits == id.leastSigBits) {
            z = true;
        }
        return z;
    }

    public /* bridge */ /* synthetic */ int compareTo(Object val) {
        return compareTo((UUID) val);
    }

    public int compareTo(UUID val) {
        if (this.mostSigBits < val.mostSigBits) {
            return -1;
        }
        if (this.mostSigBits > val.mostSigBits) {
            return 1;
        }
        if (this.leastSigBits < val.leastSigBits) {
            return -1;
        }
        if (this.leastSigBits > val.leastSigBits) {
            return 1;
        }
        return 0;
    }
}
