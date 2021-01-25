package ohos.com.sun.org.apache.xerces.internal.util;

public final class IntStack {
    private int[] fData;
    private int fDepth;

    public int size() {
        return this.fDepth;
    }

    public void push(int i) {
        ensureCapacity(this.fDepth + 1);
        int[] iArr = this.fData;
        int i2 = this.fDepth;
        this.fDepth = i2 + 1;
        iArr[i2] = i;
    }

    public int peek() {
        return this.fData[this.fDepth - 1];
    }

    public int elementAt(int i) {
        return this.fData[i];
    }

    public int pop() {
        int[] iArr = this.fData;
        int i = this.fDepth - 1;
        this.fDepth = i;
        return iArr[i];
    }

    public void clear() {
        this.fDepth = 0;
    }

    public void print() {
        System.out.print('(');
        System.out.print(this.fDepth);
        System.out.print(") {");
        int i = 0;
        while (true) {
            if (i >= this.fDepth) {
                break;
            } else if (i == 3) {
                System.out.print(" ...");
                break;
            } else {
                System.out.print(' ');
                System.out.print(this.fData[i]);
                if (i < this.fDepth - 1) {
                    System.out.print(',');
                }
                i++;
            }
        }
        System.out.print(" }");
        System.out.println();
    }

    private void ensureCapacity(int i) {
        int[] iArr = this.fData;
        if (iArr == null) {
            this.fData = new int[32];
        } else if (iArr.length <= i) {
            int[] iArr2 = new int[(iArr.length * 2)];
            System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
            this.fData = iArr2;
        }
    }
}
