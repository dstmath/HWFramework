package jcifs.util;

public class DES {
    private static int[] SP1;
    private static int[] SP2;
    private static int[] SP3;
    private static int[] SP4;
    private static int[] SP5;
    private static int[] SP6;
    private static int[] SP7;
    private static int[] SP8;
    private static int[] bigbyte;
    private static byte[] bytebit;
    private static byte[] pc1;
    private static byte[] pc2;
    private static int[] totrot;
    private int[] decryptKeys;
    private int[] encryptKeys;
    private int[] tempInts;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.util.DES.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.util.DES.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.util.DES.<clinit>():void");
    }

    public DES() {
        this.encryptKeys = new int[32];
        this.decryptKeys = new int[32];
        this.tempInts = new int[2];
    }

    public DES(byte[] key) {
        this.encryptKeys = new int[32];
        this.decryptKeys = new int[32];
        this.tempInts = new int[2];
        if (key.length == 7) {
            byte[] key8 = new byte[8];
            makeSMBKey(key, key8);
            setKey(key8);
            return;
        }
        setKey(key);
    }

    public static void makeSMBKey(byte[] key7, byte[] key8) {
        key8[0] = (byte) ((key7[0] >> 1) & 255);
        key8[1] = (byte) ((((key7[0] & 1) << 6) | (((key7[1] & 255) >> 2) & 255)) & 255);
        key8[2] = (byte) ((((key7[1] & 3) << 5) | (((key7[2] & 255) >> 3) & 255)) & 255);
        key8[3] = (byte) ((((key7[2] & 7) << 4) | (((key7[3] & 255) >> 4) & 255)) & 255);
        key8[4] = (byte) ((((key7[3] & 15) << 3) | (((key7[4] & 255) >> 5) & 255)) & 255);
        key8[5] = (byte) ((((key7[4] & 31) << 2) | (((key7[5] & 255) >> 6) & 255)) & 255);
        key8[6] = (byte) ((((key7[5] & 63) << 1) | (((key7[6] & 255) >> 7) & 255)) & 255);
        key8[7] = (byte) (key7[6] & 127);
        for (int i = 0; i < 8; i++) {
            key8[i] = (byte) (key8[i] << 1);
        }
    }

    public void setKey(byte[] key) {
        deskey(key, true, this.encryptKeys);
        deskey(key, false, this.decryptKeys);
    }

    private void deskey(byte[] keyBlock, boolean encrypting, int[] KnL) {
        int j;
        int[] pc1m = new int[56];
        int[] pcr = new int[56];
        int[] kn = new int[32];
        for (j = 0; j < 56; j++) {
            int l = pc1[j];
            pc1m[j] = (keyBlock[l >>> 3] & bytebit[l & 7]) != 0 ? 1 : 0;
        }
        for (int i = 0; i < 16; i++) {
            int m;
            if (encrypting) {
                m = i << 1;
            } else {
                m = (15 - i) << 1;
            }
            int n = m + 1;
            kn[n] = 0;
            kn[m] = 0;
            for (j = 0; j < 28; j++) {
                l = j + totrot[i];
                if (l < 28) {
                    pcr[j] = pc1m[l];
                } else {
                    pcr[j] = pc1m[l - 28];
                }
            }
            for (j = 28; j < 56; j++) {
                l = j + totrot[i];
                if (l < 56) {
                    pcr[j] = pc1m[l];
                } else {
                    pcr[j] = pc1m[l - 28];
                }
            }
            for (j = 0; j < 24; j++) {
                if (pcr[pc2[j]] != 0) {
                    kn[m] = kn[m] | bigbyte[j];
                }
                if (pcr[pc2[j + 24]] != 0) {
                    kn[n] = kn[n] | bigbyte[j];
                }
            }
        }
        cookey(kn, KnL);
    }

    private void cookey(int[] raw, int[] KnL) {
        int KnLi = 0;
        int rawi = 0;
        for (int i = 0; i < 16; i++) {
            int rawi2 = rawi + 1;
            int raw0 = raw[rawi];
            rawi = rawi2 + 1;
            int raw1 = raw[rawi2];
            KnL[KnLi] = (raw0 & 16515072) << 6;
            KnL[KnLi] = KnL[KnLi] | ((raw0 & 4032) << 10);
            KnL[KnLi] = KnL[KnLi] | ((raw1 & 16515072) >>> 10);
            KnL[KnLi] = KnL[KnLi] | ((raw1 & 4032) >>> 6);
            KnLi++;
            KnL[KnLi] = (raw0 & 258048) << 12;
            KnL[KnLi] = KnL[KnLi] | ((raw0 & 63) << 16);
            KnL[KnLi] = KnL[KnLi] | ((raw1 & 258048) >>> 4);
            KnL[KnLi] = KnL[KnLi] | (raw1 & 63);
            KnLi++;
        }
    }

    private void encrypt(byte[] clearText, int clearOff, byte[] cipherText, int cipherOff) {
        squashBytesToInts(clearText, clearOff, this.tempInts, 0, 2);
        des(this.tempInts, this.tempInts, this.encryptKeys);
        spreadIntsToBytes(this.tempInts, 0, cipherText, cipherOff, 2);
    }

    private void decrypt(byte[] cipherText, int cipherOff, byte[] clearText, int clearOff) {
        squashBytesToInts(cipherText, cipherOff, this.tempInts, 0, 2);
        des(this.tempInts, this.tempInts, this.decryptKeys);
        spreadIntsToBytes(this.tempInts, 0, clearText, clearOff, 2);
    }

    private void des(int[] inInts, int[] outInts, int[] keys) {
        int leftt = inInts[0];
        int right = inInts[1];
        int work = ((leftt >>> 4) ^ right) & 252645135;
        right ^= work;
        leftt ^= work << 4;
        work = ((leftt >>> 16) ^ right) & 65535;
        right ^= work;
        leftt ^= work << 16;
        work = ((right >>> 2) ^ leftt) & 858993459;
        leftt ^= work;
        right ^= work << 2;
        work = ((right >>> 8) ^ leftt) & 16711935;
        leftt ^= work;
        right ^= work << 8;
        right = (right << 1) | ((right >>> 31) & 1);
        work = (leftt ^ right) & -1431655766;
        leftt ^= work;
        right ^= work;
        leftt = (leftt << 1) | ((leftt >>> 31) & 1);
        int keysi = 0;
        for (int round = 0; round < 8; round++) {
            int keysi2 = keysi + 1;
            work = ((right << 28) | (right >>> 4)) ^ keys[keysi];
            int fval = ((SP7[work & 63] | SP5[(work >>> 8) & 63]) | SP3[(work >>> 16) & 63]) | SP1[(work >>> 24) & 63];
            keysi = keysi2 + 1;
            work = right ^ keys[keysi2];
            leftt ^= (((fval | SP8[work & 63]) | SP6[(work >>> 8) & 63]) | SP4[(work >>> 16) & 63]) | SP2[(work >>> 24) & 63];
            keysi2 = keysi + 1;
            work = ((leftt << 28) | (leftt >>> 4)) ^ keys[keysi];
            fval = ((SP7[work & 63] | SP5[(work >>> 8) & 63]) | SP3[(work >>> 16) & 63]) | SP1[(work >>> 24) & 63];
            keysi = keysi2 + 1;
            work = leftt ^ keys[keysi2];
            right ^= (((fval | SP8[work & 63]) | SP6[(work >>> 8) & 63]) | SP4[(work >>> 16) & 63]) | SP2[(work >>> 24) & 63];
        }
        right = (right << 31) | (right >>> 1);
        work = (leftt ^ right) & -1431655766;
        leftt ^= work;
        right ^= work;
        leftt = (leftt << 31) | (leftt >>> 1);
        work = ((leftt >>> 8) ^ right) & 16711935;
        right ^= work;
        leftt ^= work << 8;
        work = ((leftt >>> 2) ^ right) & 858993459;
        right ^= work;
        leftt ^= work << 2;
        work = ((right >>> 16) ^ leftt) & 65535;
        leftt ^= work;
        right ^= work << 16;
        work = ((right >>> 4) ^ leftt) & 252645135;
        leftt ^= work;
        outInts[0] = right ^ (work << 4);
        outInts[1] = leftt;
    }

    public void encrypt(byte[] clearText, byte[] cipherText) {
        encrypt(clearText, 0, cipherText, 0);
    }

    public void decrypt(byte[] cipherText, byte[] clearText) {
        decrypt(cipherText, 0, clearText, 0);
    }

    public byte[] encrypt(byte[] clearText) {
        int length = clearText.length;
        if (length % 8 != 0) {
            System.out.println("Array must be a multiple of 8");
            return null;
        }
        byte[] cipherText = new byte[length];
        int count = length / 8;
        for (int i = 0; i < count; i++) {
            encrypt(clearText, i * 8, cipherText, i * 8);
        }
        return cipherText;
    }

    public byte[] decrypt(byte[] cipherText) {
        int length = cipherText.length;
        if (length % 8 != 0) {
            System.out.println("Array must be a multiple of 8");
            return null;
        }
        byte[] clearText = new byte[length];
        int count = length / 8;
        for (int i = 0; i < count; i++) {
            encrypt(cipherText, i * 8, clearText, i * 8);
        }
        return clearText;
    }

    public static void squashBytesToInts(byte[] inBytes, int inOff, int[] outInts, int outOff, int intLen) {
        for (int i = 0; i < intLen; i++) {
            outInts[outOff + i] = ((((inBytes[(i * 4) + inOff] & 255) << 24) | ((inBytes[((i * 4) + inOff) + 1] & 255) << 16)) | ((inBytes[((i * 4) + inOff) + 2] & 255) << 8)) | (inBytes[((i * 4) + inOff) + 3] & 255);
        }
    }

    public static void spreadIntsToBytes(int[] inInts, int inOff, byte[] outBytes, int outOff, int intLen) {
        for (int i = 0; i < intLen; i++) {
            outBytes[(i * 4) + outOff] = (byte) (inInts[inOff + i] >>> 24);
            outBytes[((i * 4) + outOff) + 1] = (byte) (inInts[inOff + i] >>> 16);
            outBytes[((i * 4) + outOff) + 2] = (byte) (inInts[inOff + i] >>> 8);
            outBytes[((i * 4) + outOff) + 3] = (byte) inInts[inOff + i];
        }
    }
}
