package com.huawei.zxing.oned;

public abstract class UPCEANWriter extends OneDimensionalCodeWriter {
    @Override // com.huawei.zxing.oned.OneDimensionalCodeWriter
    public int getDefaultMargin() {
        return UPCEANReader.START_END_PATTERN.length;
    }
}
