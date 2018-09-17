package com.huawei.zxing.pdf417.encoder;

import com.huawei.zxing.WriterException;
import com.huawei.zxing.pdf417.PDF417Common;

public final class PDF417 {
    private static final int[][] CODEWORD_TABLE = null;
    private static final float DEFAULT_MODULE_WIDTH = 0.357f;
    private static final float HEIGHT = 2.0f;
    private static final float PREFERRED_RATIO = 3.0f;
    private static final int START_PATTERN = 130728;
    private static final int STOP_PATTERN = 260649;
    private BarcodeMatrix barcodeMatrix;
    private boolean compact;
    private Compaction compaction;
    private int maxCols;
    private int maxRows;
    private int minCols;
    private int minRows;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.pdf417.encoder.PDF417.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.pdf417.encoder.PDF417.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.pdf417.encoder.PDF417.<clinit>():void");
    }

    public PDF417() {
        this(false);
    }

    public PDF417(boolean compact) {
        this.compact = compact;
        this.compaction = Compaction.AUTO;
        this.minCols = 2;
        this.maxCols = 30;
        this.maxRows = 30;
        this.minRows = 2;
    }

    public BarcodeMatrix getBarcodeMatrix() {
        return this.barcodeMatrix;
    }

    private static int calculateNumberOfRows(int m, int k, int c) {
        int r = (((m + 1) + k) / c) + 1;
        if (c * r >= ((m + 1) + k) + c) {
            return r - 1;
        }
        return r;
    }

    private static int getNumberOfPadCodewords(int m, int k, int c, int r) {
        int n = (c * r) - k;
        return n > m + 1 ? (n - m) - 1 : 0;
    }

    private static void encodeChar(int pattern, int len, BarcodeRow logic) {
        int map = 1 << (len - 1);
        boolean last = (pattern & map) != 0;
        int width = 0;
        for (int i = 0; i < len; i++) {
            boolean black;
            if ((pattern & map) != 0) {
                black = true;
            } else {
                black = false;
            }
            if (last == black) {
                width++;
            } else {
                logic.addBar(last, width);
                last = black;
                width = 1;
            }
            map >>= 1;
        }
        logic.addBar(last, width);
    }

    private void encodeLowLevel(CharSequence fullCodewords, int c, int r, int errorCorrectionLevel, BarcodeMatrix logic) {
        int idx = 0;
        for (int y = 0; y < r; y++) {
            int left;
            int right;
            int cluster = y % 3;
            logic.startRow();
            encodeChar(START_PATTERN, 17, logic.getCurrentRow());
            if (cluster == 0) {
                left = ((y / 3) * 30) + ((r - 1) / 3);
                right = ((y / 3) * 30) + (c - 1);
            } else if (cluster == 1) {
                left = (((y / 3) * 30) + (errorCorrectionLevel * 3)) + ((r - 1) % 3);
                right = ((y / 3) * 30) + ((r - 1) / 3);
            } else {
                left = ((y / 3) * 30) + (c - 1);
                right = (((y / 3) * 30) + (errorCorrectionLevel * 3)) + ((r - 1) % 3);
            }
            encodeChar(CODEWORD_TABLE[cluster][left], 17, logic.getCurrentRow());
            for (int x = 0; x < c; x++) {
                encodeChar(CODEWORD_TABLE[cluster][fullCodewords.charAt(idx)], 17, logic.getCurrentRow());
                idx++;
            }
            if (this.compact) {
                encodeChar(STOP_PATTERN, 1, logic.getCurrentRow());
            } else {
                encodeChar(CODEWORD_TABLE[cluster][right], 17, logic.getCurrentRow());
                encodeChar(STOP_PATTERN, 18, logic.getCurrentRow());
            }
        }
    }

    public void generateBarcodeLogic(String msg, int errorCorrectionLevel) throws WriterException {
        int errorCorrectionCodeWords = PDF417ErrorCorrection.getErrorCorrectionCodewordCount(errorCorrectionLevel);
        String highLevel = PDF417HighLevelEncoder.encodeHighLevel(msg, this.compaction);
        int sourceCodeWords = highLevel.length();
        int[] dimension = determineDimensions(sourceCodeWords, errorCorrectionCodeWords);
        int cols = dimension[0];
        int rows = dimension[1];
        int pad = getNumberOfPadCodewords(sourceCodeWords, errorCorrectionCodeWords, cols, rows);
        if ((sourceCodeWords + errorCorrectionCodeWords) + 1 > PDF417Common.NUMBER_OF_CODEWORDS) {
            throw new WriterException("Encoded message contains to many code words, message to big (" + msg.length() + " bytes)");
        }
        int n = (sourceCodeWords + pad) + 1;
        StringBuilder stringBuilder = new StringBuilder(n);
        stringBuilder.append((char) n);
        stringBuilder.append(highLevel);
        for (int i = 0; i < pad; i++) {
            stringBuilder.append('\u0384');
        }
        String dataCodewords = stringBuilder.toString();
        String fullCodewords = dataCodewords + PDF417ErrorCorrection.generateErrorCorrection(dataCodewords, errorCorrectionLevel);
        this.barcodeMatrix = new BarcodeMatrix(rows, cols);
        encodeLowLevel(fullCodewords, cols, rows, errorCorrectionLevel, this.barcodeMatrix);
    }

    private int[] determineDimensions(int sourceCodeWords, int errorCorrectionCodeWords) throws WriterException {
        float ratio = 0.0f;
        int[] dimension = null;
        for (int cols = this.minCols; cols <= this.maxCols; cols++) {
            int rows = calculateNumberOfRows(sourceCodeWords, errorCorrectionCodeWords, cols);
            if (rows < this.minRows) {
                break;
            }
            if (rows <= this.maxRows) {
                float newRatio = (((float) ((cols * 17) + 69)) * DEFAULT_MODULE_WIDTH) / (((float) rows) * HEIGHT);
                if (dimension == null || Math.abs(newRatio - PREFERRED_RATIO) <= Math.abs(ratio - PREFERRED_RATIO)) {
                    ratio = newRatio;
                    dimension = new int[]{cols, rows};
                }
            }
        }
        if (dimension == null && calculateNumberOfRows(sourceCodeWords, errorCorrectionCodeWords, this.minCols) < this.minRows) {
            dimension = new int[]{this.minCols, this.minRows};
        }
        if (dimension != null) {
            return dimension;
        }
        throw new WriterException("Unable to fit message in columns");
    }

    public void setDimensions(int maxCols, int minCols, int maxRows, int minRows) {
        this.maxCols = maxCols;
        this.minCols = minCols;
        this.maxRows = maxRows;
        this.minRows = minRows;
    }

    public void setCompaction(Compaction compaction) {
        this.compaction = compaction;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }
}
