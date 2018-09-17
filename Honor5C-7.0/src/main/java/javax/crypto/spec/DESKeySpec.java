package javax.crypto.spec;

import java.security.InvalidKeyException;
import java.security.spec.KeySpec;

public class DESKeySpec implements KeySpec {
    public static final int DES_KEY_LEN = 8;
    private static final byte[][] WEAK_KEYS = null;
    private byte[] key;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.crypto.spec.DESKeySpec.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.crypto.spec.DESKeySpec.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: javax.crypto.spec.DESKeySpec.<clinit>():void");
    }

    public DESKeySpec(byte[] key) throws InvalidKeyException {
        this(key, 0);
    }

    public DESKeySpec(byte[] key, int offset) throws InvalidKeyException {
        if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException("Wrong key size");
        }
        this.key = new byte[DES_KEY_LEN];
        System.arraycopy(key, offset, this.key, 0, (int) DES_KEY_LEN);
    }

    public byte[] getKey() {
        return (byte[]) this.key.clone();
    }

    public static boolean isParityAdjusted(byte[] key, int offset) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("null key");
        } else if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException("Wrong key size");
        } else {
            int i = 0;
            int offset2 = offset;
            while (i < DES_KEY_LEN) {
                offset = offset2 + 1;
                if ((Integer.bitCount(key[offset2] & 255) & 1) == 0) {
                    return false;
                }
                i++;
                offset2 = offset;
            }
            return true;
        }
    }

    public static boolean isWeak(byte[] key, int offset) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("null key");
        } else if (key.length - offset < DES_KEY_LEN) {
            throw new InvalidKeyException("Wrong key size");
        } else {
            for (byte[] bArr : WEAK_KEYS) {
                boolean found = true;
                for (int j = 0; j < DES_KEY_LEN && found; j++) {
                    if (bArr[j] != key[j + offset]) {
                        found = false;
                    }
                }
                if (found) {
                    return found;
                }
            }
            return false;
        }
    }
}
