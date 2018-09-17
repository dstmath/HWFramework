package android.icu.impl.coll;

import android.icu.text.DateTimePatternGenerator;

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

    public void addElement(int e) {
        ensureAppendCapacity();
        int[] iArr = this.buffer;
        int i = this.length;
        this.length = i + 1;
        iArr[i] = e;
    }

    public void setElementAt(int elem, int index) {
        this.buffer[index] = elem;
    }

    public void insertElementAt(int elem, int index) {
        ensureAppendCapacity();
        System.arraycopy(this.buffer, index, this.buffer, index + 1, this.length - index);
        this.buffer[index] = elem;
        this.length++;
    }

    public void removeAllElements() {
        this.length = 0;
    }

    private void ensureAppendCapacity() {
        if (this.length >= this.buffer.length) {
            int[] newBuffer = new int[(this.buffer.length <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH ? this.buffer.length * 4 : this.buffer.length * 2)];
            System.arraycopy(this.buffer, 0, newBuffer, 0, this.length);
            this.buffer = newBuffer;
        }
    }
}
