package com.huawei.zxing.datamatrix.encoder;

final class DataMatrixSymbolInfo144 extends SymbolInfo {
    DataMatrixSymbolInfo144() {
        super(false, 1558, 620, 22, 22, 36, -1, 62);
    }

    @Override // com.huawei.zxing.datamatrix.encoder.SymbolInfo
    public int getInterleavedBlockCount() {
        return 10;
    }

    @Override // com.huawei.zxing.datamatrix.encoder.SymbolInfo
    public int getDataLengthForInterleavedBlock(int index) {
        return index <= 8 ? 156 : 155;
    }
}
