package com.huawei.zxing.datamatrix.encoder;

import com.huawei.zxing.Dimension;
import java.nio.charset.Charset;

/* access modifiers changed from: package-private */
public final class EncoderContext {
    private final StringBuilder codewords;
    private Dimension maxSize;
    private Dimension minSize;
    private final String msg;
    private int newEncoding;
    int pos;
    private SymbolShapeHint shape;
    private int skipAtEnd;
    private SymbolInfo symbolInfo;

    EncoderContext(String msg2) {
        byte[] msgBinary = msg2.getBytes(Charset.forName("ISO-8859-1"));
        StringBuilder sb = new StringBuilder(msgBinary.length);
        int c = msgBinary.length;
        for (int i = 0; i < c; i++) {
            char ch = (char) (msgBinary[i] & 255);
            if (ch != '?' || msg2.charAt(i) == '?') {
                sb.append(ch);
            } else {
                throw new IllegalArgumentException("Message contains characters outside ISO-8859-1 encoding.");
            }
        }
        this.msg = sb.toString();
        this.shape = SymbolShapeHint.FORCE_NONE;
        this.codewords = new StringBuilder(msg2.length());
        this.newEncoding = -1;
    }

    public void setSymbolShape(SymbolShapeHint shape2) {
        this.shape = shape2;
    }

    public void setSizeConstraints(Dimension minSize2, Dimension maxSize2) {
        this.minSize = minSize2;
        this.maxSize = maxSize2;
    }

    public String getMessage() {
        return this.msg;
    }

    public void setSkipAtEnd(int count) {
        this.skipAtEnd = count;
    }

    public char getCurrentChar() {
        return this.msg.charAt(this.pos);
    }

    public char getCurrent() {
        return this.msg.charAt(this.pos);
    }

    public StringBuilder getCodewords() {
        return this.codewords;
    }

    public void writeCodewords(String codewords2) {
        this.codewords.append(codewords2);
    }

    public void writeCodeword(char codeword) {
        this.codewords.append(codeword);
    }

    public int getCodewordCount() {
        return this.codewords.length();
    }

    public int getNewEncoding() {
        return this.newEncoding;
    }

    public void signalEncoderChange(int encoding) {
        this.newEncoding = encoding;
    }

    public void resetEncoderSignal() {
        this.newEncoding = -1;
    }

    public boolean hasMoreCharacters() {
        return this.pos < getTotalMessageCharCount();
    }

    private int getTotalMessageCharCount() {
        return this.msg.length() - this.skipAtEnd;
    }

    public int getRemainingCharacters() {
        return getTotalMessageCharCount() - this.pos;
    }

    public SymbolInfo getSymbolInfo() {
        return this.symbolInfo;
    }

    public void updateSymbolInfo() {
        updateSymbolInfo(getCodewordCount());
    }

    public void updateSymbolInfo(int len) {
        SymbolInfo symbolInfo2 = this.symbolInfo;
        if (symbolInfo2 == null || len > symbolInfo2.getDataCapacity()) {
            this.symbolInfo = SymbolInfo.lookup(len, this.shape, this.minSize, this.maxSize, true);
        }
    }

    public void resetSymbolInfo() {
        this.symbolInfo = null;
    }
}
