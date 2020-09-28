package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitMatrix;

public final class AztecCode {
    private int codeWords;
    private boolean compact;
    private int layers;
    private BitMatrix matrix;
    private int size;

    public boolean isCompact() {
        return this.compact;
    }

    public void setCompact(boolean compact2) {
        this.compact = compact2;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size2) {
        this.size = size2;
    }

    public int getLayers() {
        return this.layers;
    }

    public void setLayers(int layers2) {
        this.layers = layers2;
    }

    public int getCodeWords() {
        return this.codeWords;
    }

    public void setCodeWords(int codeWords2) {
        this.codeWords = codeWords2;
    }

    public BitMatrix getMatrix() {
        return this.matrix;
    }

    public void setMatrix(BitMatrix matrix2) {
        this.matrix = matrix2;
    }
}
