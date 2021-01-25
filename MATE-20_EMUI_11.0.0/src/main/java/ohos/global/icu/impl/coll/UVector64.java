package ohos.global.icu.impl.coll;

public final class UVector64 {
    private long[] buffer = new long[32];
    private int length = 0;

    public boolean isEmpty() {
        return this.length == 0;
    }

    public int size() {
        return this.length;
    }

    public long elementAti(int i) {
        return this.buffer[i];
    }

    public long[] getBuffer() {
        return this.buffer;
    }

    public void addElement(long j) {
        ensureAppendCapacity();
        long[] jArr = this.buffer;
        int i = this.length;
        this.length = i + 1;
        jArr[i] = j;
    }

    public void setElementAt(long j, int i) {
        this.buffer[i] = j;
    }

    public void insertElementAt(long j, int i) {
        ensureAppendCapacity();
        long[] jArr = this.buffer;
        System.arraycopy(jArr, i, jArr, i + 1, this.length - i);
        this.buffer[i] = j;
        this.length++;
    }

    public void removeAllElements() {
        this.length = 0;
    }

    private void ensureAppendCapacity() {
        int i = this.length;
        long[] jArr = this.buffer;
        if (i >= jArr.length) {
            long[] jArr2 = new long[(jArr.length <= 65535 ? jArr.length * 4 : jArr.length * 2)];
            System.arraycopy(this.buffer, 0, jArr2, 0, this.length);
            this.buffer = jArr2;
        }
    }
}
