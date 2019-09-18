package com.huawei.zxing.oned.rss.expanded.decoders;

final class CurrentParsingState {
    private State encoding = State.NUMERIC;
    private int position = 0;

    private enum State {
        NUMERIC,
        ALPHA,
        ISO_IEC_646
    }

    CurrentParsingState() {
    }

    /* access modifiers changed from: package-private */
    public int getPosition() {
        return this.position;
    }

    /* access modifiers changed from: package-private */
    public void setPosition(int position2) {
        this.position = position2;
    }

    /* access modifiers changed from: package-private */
    public void incrementPosition(int delta) {
        this.position += delta;
    }

    /* access modifiers changed from: package-private */
    public boolean isAlpha() {
        return this.encoding == State.ALPHA;
    }

    /* access modifiers changed from: package-private */
    public boolean isNumeric() {
        return this.encoding == State.NUMERIC;
    }

    /* access modifiers changed from: package-private */
    public boolean isIsoIec646() {
        return this.encoding == State.ISO_IEC_646;
    }

    /* access modifiers changed from: package-private */
    public void setNumeric() {
        this.encoding = State.NUMERIC;
    }

    /* access modifiers changed from: package-private */
    public void setAlpha() {
        this.encoding = State.ALPHA;
    }

    /* access modifiers changed from: package-private */
    public void setIsoIec646() {
        this.encoding = State.ISO_IEC_646;
    }
}
