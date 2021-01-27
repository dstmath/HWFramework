package ohos.global.icu.impl.coll;

public final class UVector32 {
    private int[] buffer = new int[32];
    private int length = 0;

    public boolean isEmpty() {
        return this.length == 0;
    }

    public int size() {
        return this.length;
    }

    public int elementAti(int i) {
        return this.buffer[i];
    }

    public int[] getBuffer() {
        return this.buffer;
    }

    public void addElement(int i) {
        ensureAppendCapacity();
        int[] iArr = this.buffer;
        int i2 = this.length;
        this.length = i2 + 1;
        iArr[i2] = i;
    }

    public void setElementAt(int i, int i2) {
        this.buffer[i2] = i;
    }

    public void insertElementAt(int i, int i2) {
        ensureAppendCapacity();
        int[] iArr = this.buffer;
        System.arraycopy(iArr, i2, iArr, i2 + 1, this.length - i2);
        this.buffer[i2] = i;
        this.length++;
    }

    public void removeAllElements() {
        this.length = 0;
    }

    private void ensureAppendCapacity() {
        int i = this.length;
        int[] iArr = this.buffer;
        if (i >= iArr.length) {
            int[] iArr2 = new int[(iArr.length <= 65535 ? iArr.length * 4 : iArr.length * 2)];
            System.arraycopy(this.buffer, 0, iArr2, 0, this.length);
            this.buffer = iArr2;
        }
    }
}
