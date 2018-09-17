package com.huawei.zxing.datamatrix.encoder;

import com.huawei.zxing.Dimension;
import java.nio.charset.Charset;

final class EncoderContext {
    private final StringBuilder codewords;
    private Dimension maxSize;
    private Dimension minSize;
    private final String msg;
    private int newEncoding;
    int pos;
    private SymbolShapeHint shape;
    private int skipAtEnd;
    private SymbolInfo symbolInfo;

    EncoderContext(String msg) {
        byte[] msgBinary = msg.getBytes(Charset.forName("ISO-8859-1"));
        StringBuilder sb = new StringBuilder(msgBinary.length);
        int i = 0;
        int c = msgBinary.length;
        while (i < c) {
            char ch = (char) (msgBinary[i] & 255);
            if (ch != '?' || msg.charAt(i) == '?') {
                sb.append(ch);
                i++;
            } else {
                throw new IllegalArgumentException("Message contains characters outside ISO-8859-1 encoding.");
            }
        }
        this.msg = sb.toString();
        this.shape = SymbolShapeHint.FORCE_NONE;
        this.codewords = new StringBuilder(msg.length());
        this.newEncoding = -1;
    }

    public void setSymbolShape(SymbolShapeHint shape) {
        this.shape = shape;
    }

    public void setSizeConstraints(Dimension minSize, Dimension maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
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

    public void writeCodewords(String codewords) {
        this.codewords.append(codewords);
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
        if (this.symbolInfo == null || len > this.symbolInfo.getDataCapacity()) {
            this.symbolInfo = SymbolInfo.lookup(len, this.shape, this.minSize, this.maxSize, true);
        }
    }

    public void resetSymbolInfo() {
        this.symbolInfo = null;
    }
}
