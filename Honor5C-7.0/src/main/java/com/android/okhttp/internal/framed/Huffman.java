package com.android.okhttp.internal.framed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class Huffman {
    private static final int[] CODES = null;
    private static final byte[] CODE_LENGTHS = null;
    private static final Huffman INSTANCE = null;
    private final Node root;

    private static final class Node {
        private final Node[] children;
        private final int symbol;
        private final int terminalBits;

        Node() {
            this.children = new Node[256];
            this.symbol = 0;
            this.terminalBits = 0;
        }

        Node(int symbol, int bits) {
            this.children = null;
            this.symbol = symbol;
            int b = bits & 7;
            if (b == 0) {
                b = 8;
            }
            this.terminalBits = b;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.Huffman.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.framed.Huffman.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.Huffman.<clinit>():void");
    }

    public static Huffman get() {
        return INSTANCE;
    }

    private Huffman() {
        this.root = new Node();
        buildTree();
    }

    void encode(byte[] data, OutputStream out) throws IOException {
        long current = 0;
        int n = 0;
        for (byte b : data) {
            int b2 = b & 255;
            int code = CODES[b2];
            int nbits = CODE_LENGTHS[b2];
            current = (current << nbits) | ((long) code);
            n += nbits;
            while (n >= 8) {
                n -= 8;
                out.write((int) (current >> n));
            }
        }
        if (n > 0) {
            out.write((int) ((current << (8 - n)) | ((long) (255 >>> n))));
        }
    }

    int encodedLength(byte[] bytes) {
        long len = 0;
        for (byte b : bytes) {
            len += (long) CODE_LENGTHS[b & 255];
        }
        return (int) ((7 + len) >> 3);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    byte[] decode(byte[] buf) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Node node = this.root;
        int current = 0;
        int nbits = 0;
        for (byte b : buf) {
            current = (current << 8) | (b & 255);
            nbits += 8;
            while (nbits >= 8) {
                node = node.children[(current >>> (nbits - 8)) & 255];
                if (node.children == null) {
                    baos.write(node.symbol);
                    nbits -= node.terminalBits;
                    node = this.root;
                } else {
                    nbits -= 8;
                }
            }
        }
        while (nbits > 0) {
            node = node.children[(current << (8 - nbits)) & 255];
            if (node.children == null && node.terminalBits <= nbits) {
                baos.write(node.symbol);
                nbits -= node.terminalBits;
                node = this.root;
            }
        }
        return baos.toByteArray();
    }

    private void buildTree() {
        for (int i = 0; i < CODE_LENGTHS.length; i++) {
            addCode(i, CODES[i], CODE_LENGTHS[i]);
        }
    }

    private void addCode(int sym, int code, byte len) {
        Node terminal = new Node(sym, len);
        Node current = this.root;
        while (len > 8) {
            len = (byte) (len - 8);
            int i = (code >>> len) & 255;
            if (current.children == null) {
                throw new IllegalStateException("invalid dictionary: prefix not unique");
            }
            if (current.children[i] == null) {
                current.children[i] = new Node();
            }
            current = current.children[i];
        }
        int shift = 8 - len;
        int start = (code << shift) & 255;
        int end = 1 << shift;
        for (i = start; i < start + end; i++) {
            current.children[i] = terminal;
        }
    }
}
