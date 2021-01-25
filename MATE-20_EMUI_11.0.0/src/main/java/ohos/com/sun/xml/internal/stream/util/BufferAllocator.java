package ohos.com.sun.xml.internal.stream.util;

public class BufferAllocator {
    public static int LARGE_SIZE_LIMIT = 8192;
    public static int MEDIUM_SIZE_LIMIT = 2048;
    public static int SMALL_SIZE_LIMIT = 128;
    byte[] largeByteBuffer;
    char[] largeCharBuffer;
    byte[] mediumByteBuffer;
    char[] mediumCharBuffer;
    byte[] smallByteBuffer;
    char[] smallCharBuffer;

    public char[] getCharBuffer(int i) {
        if (i <= SMALL_SIZE_LIMIT) {
            char[] cArr = this.smallCharBuffer;
            this.smallCharBuffer = null;
            return cArr;
        } else if (i <= MEDIUM_SIZE_LIMIT) {
            char[] cArr2 = this.mediumCharBuffer;
            this.mediumCharBuffer = null;
            return cArr2;
        } else if (i > LARGE_SIZE_LIMIT) {
            return null;
        } else {
            char[] cArr3 = this.largeCharBuffer;
            this.largeCharBuffer = null;
            return cArr3;
        }
    }

    public void returnCharBuffer(char[] cArr) {
        if (cArr != null) {
            if (cArr.length <= SMALL_SIZE_LIMIT) {
                this.smallCharBuffer = cArr;
            } else if (cArr.length <= MEDIUM_SIZE_LIMIT) {
                this.mediumCharBuffer = cArr;
            } else if (cArr.length <= LARGE_SIZE_LIMIT) {
                this.largeCharBuffer = cArr;
            }
        }
    }

    public byte[] getByteBuffer(int i) {
        if (i <= SMALL_SIZE_LIMIT) {
            byte[] bArr = this.smallByteBuffer;
            this.smallByteBuffer = null;
            return bArr;
        } else if (i <= MEDIUM_SIZE_LIMIT) {
            byte[] bArr2 = this.mediumByteBuffer;
            this.mediumByteBuffer = null;
            return bArr2;
        } else if (i > LARGE_SIZE_LIMIT) {
            return null;
        } else {
            byte[] bArr3 = this.largeByteBuffer;
            this.largeByteBuffer = null;
            return bArr3;
        }
    }

    public void returnByteBuffer(byte[] bArr) {
        if (bArr != null) {
            if (bArr.length <= SMALL_SIZE_LIMIT) {
                this.smallByteBuffer = bArr;
            } else if (bArr.length <= MEDIUM_SIZE_LIMIT) {
                this.mediumByteBuffer = bArr;
            } else if (bArr.length <= LARGE_SIZE_LIMIT) {
                this.largeByteBuffer = bArr;
            }
        }
    }
}
