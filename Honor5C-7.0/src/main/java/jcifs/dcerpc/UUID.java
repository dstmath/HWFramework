package jcifs.dcerpc;

import jcifs.dcerpc.rpc.uuid_t;

public class UUID extends uuid_t {
    static final char[] HEXCHARS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.dcerpc.UUID.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.dcerpc.UUID.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.dcerpc.UUID.<clinit>():void");
    }

    public static int hex_to_bin(char[] arr, int offset, int length) {
        int value = 0;
        int count = 0;
        for (int ai = offset; ai < arr.length && count < length; ai++) {
            value <<= 4;
            switch (arr[ai]) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    value += arr[ai] - 48;
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    value += (arr[ai] - 65) + 10;
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    value += (arr[ai] - 97) + 10;
                    break;
                default:
                    throw new IllegalArgumentException(new String(arr, offset, length));
            }
            count++;
        }
        return value;
    }

    public static String bin_to_hex(int value, int length) {
        char[] arr = new char[length];
        int ai = arr.length;
        while (true) {
            int ai2 = ai - 1;
            if (ai <= 0) {
                return new String(arr);
            }
            arr[ai2] = HEXCHARS[value & 15];
            value >>>= 4;
            ai = ai2;
        }
    }

    private static byte B(int i) {
        return (byte) (i & 255);
    }

    private static short S(int i) {
        return (short) (65535 & i);
    }

    public UUID(uuid_t uuid) {
        this.time_low = uuid.time_low;
        this.time_mid = uuid.time_mid;
        this.time_hi_and_version = uuid.time_hi_and_version;
        this.clock_seq_hi_and_reserved = uuid.clock_seq_hi_and_reserved;
        this.clock_seq_low = uuid.clock_seq_low;
        this.node = new byte[6];
        this.node[0] = uuid.node[0];
        this.node[1] = uuid.node[1];
        this.node[2] = uuid.node[2];
        this.node[3] = uuid.node[3];
        this.node[4] = uuid.node[4];
        this.node[5] = uuid.node[5];
    }

    public UUID(String str) {
        char[] arr = str.toCharArray();
        this.time_low = hex_to_bin(arr, 0, 8);
        this.time_mid = S(hex_to_bin(arr, 9, 4));
        this.time_hi_and_version = S(hex_to_bin(arr, 14, 4));
        this.clock_seq_hi_and_reserved = B(hex_to_bin(arr, 19, 2));
        this.clock_seq_low = B(hex_to_bin(arr, 21, 2));
        this.node = new byte[6];
        this.node[0] = B(hex_to_bin(arr, 24, 2));
        this.node[1] = B(hex_to_bin(arr, 26, 2));
        this.node[2] = B(hex_to_bin(arr, 28, 2));
        this.node[3] = B(hex_to_bin(arr, 30, 2));
        this.node[4] = B(hex_to_bin(arr, 32, 2));
        this.node[5] = B(hex_to_bin(arr, 34, 2));
    }

    public String toString() {
        return bin_to_hex(this.time_low, 8) + '-' + bin_to_hex(this.time_mid, 4) + '-' + bin_to_hex(this.time_hi_and_version, 4) + '-' + bin_to_hex(this.clock_seq_hi_and_reserved, 2) + bin_to_hex(this.clock_seq_low, 2) + '-' + bin_to_hex(this.node[0], 2) + bin_to_hex(this.node[1], 2) + bin_to_hex(this.node[2], 2) + bin_to_hex(this.node[3], 2) + bin_to_hex(this.node[4], 2) + bin_to_hex(this.node[5], 2);
    }
}
