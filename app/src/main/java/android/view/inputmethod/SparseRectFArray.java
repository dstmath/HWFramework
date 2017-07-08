package android.view.inputmethod;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.PtmLog;
import java.util.Arrays;

public final class SparseRectFArray implements Parcelable {
    public static final Creator<SparseRectFArray> CREATOR = null;
    private final float[] mCoordinates;
    private final int[] mFlagsArray;
    private final int[] mKeys;

    public static final class SparseRectFArrayBuilder {
        private static int INITIAL_SIZE;
        private float[] mCoordinates;
        private int mCount;
        private int[] mFlagsArray;
        private int[] mKeys;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.inputmethod.SparseRectFArray.SparseRectFArrayBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.inputmethod.SparseRectFArray.SparseRectFArrayBuilder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.SparseRectFArray.SparseRectFArrayBuilder.<clinit>():void");
        }

        public SparseRectFArrayBuilder() {
            this.mCount = 0;
            this.mKeys = null;
            this.mCoordinates = null;
            this.mFlagsArray = null;
        }

        private void checkIndex(int key) {
            if (this.mCount != 0 && this.mKeys[this.mCount - 1] >= key) {
                throw new IllegalArgumentException("key must be greater than all existing keys.");
            }
        }

        private void ensureBufferSize() {
            if (this.mKeys == null) {
                this.mKeys = new int[INITIAL_SIZE];
            }
            if (this.mCoordinates == null) {
                this.mCoordinates = new float[(INITIAL_SIZE * 4)];
            }
            if (this.mFlagsArray == null) {
                this.mFlagsArray = new int[INITIAL_SIZE];
            }
            int requiredIndexArraySize = this.mCount + 1;
            if (this.mKeys.length <= requiredIndexArraySize) {
                int[] newArray = new int[(requiredIndexArraySize * 2)];
                System.arraycopy(this.mKeys, 0, newArray, 0, this.mCount);
                this.mKeys = newArray;
            }
            int requiredCoordinatesArraySize = (this.mCount + 1) * 4;
            if (this.mCoordinates.length <= requiredCoordinatesArraySize) {
                float[] newArray2 = new float[(requiredCoordinatesArraySize * 2)];
                System.arraycopy(this.mCoordinates, 0, newArray2, 0, this.mCount * 4);
                this.mCoordinates = newArray2;
            }
            int requiredFlagsArraySize = requiredIndexArraySize;
            if (this.mFlagsArray.length <= requiredIndexArraySize) {
                newArray = new int[(requiredIndexArraySize * 2)];
                System.arraycopy(this.mFlagsArray, 0, newArray, 0, this.mCount);
                this.mFlagsArray = newArray;
            }
        }

        public SparseRectFArrayBuilder append(int key, float left, float top, float right, float bottom, int flags) {
            checkIndex(key);
            ensureBufferSize();
            int baseCoordinatesIndex = this.mCount * 4;
            this.mCoordinates[baseCoordinatesIndex + 0] = left;
            this.mCoordinates[baseCoordinatesIndex + 1] = top;
            this.mCoordinates[baseCoordinatesIndex + 2] = right;
            this.mCoordinates[baseCoordinatesIndex + 3] = bottom;
            this.mFlagsArray[this.mCount] = flags;
            this.mKeys[this.mCount] = key;
            this.mCount++;
            return this;
        }

        public boolean isEmpty() {
            return this.mCount <= 0;
        }

        public SparseRectFArray build() {
            return new SparseRectFArray();
        }

        public void reset() {
            if (this.mCount == 0) {
                this.mKeys = null;
                this.mCoordinates = null;
                this.mFlagsArray = null;
            }
            this.mCount = 0;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.inputmethod.SparseRectFArray.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.inputmethod.SparseRectFArray.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.SparseRectFArray.<clinit>():void");
    }

    /* synthetic */ SparseRectFArray(SparseRectFArrayBuilder builder, SparseRectFArray sparseRectFArray) {
        this(builder);
    }

    public SparseRectFArray(Parcel source) {
        this.mKeys = source.createIntArray();
        this.mCoordinates = source.createFloatArray();
        this.mFlagsArray = source.createIntArray();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(this.mKeys);
        dest.writeFloatArray(this.mCoordinates);
        dest.writeIntArray(this.mFlagsArray);
    }

    public int hashCode() {
        if (this.mKeys == null || this.mKeys.length == 0) {
            return 0;
        }
        int hash = this.mKeys.length;
        for (int i = 0; i < 4; i++) {
            hash = (int) (((float) (hash * 31)) + this.mCoordinates[i]);
        }
        return (hash * 31) + this.mFlagsArray[0];
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SparseRectFArray)) {
            return false;
        }
        SparseRectFArray that = (SparseRectFArray) obj;
        if (Arrays.equals(this.mKeys, that.mKeys) && Arrays.equals(this.mCoordinates, that.mCoordinates)) {
            z = Arrays.equals(this.mFlagsArray, that.mFlagsArray);
        }
        return z;
    }

    public String toString() {
        if (this.mKeys == null || this.mCoordinates == null || this.mFlagsArray == null) {
            return "SparseRectFArray{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SparseRectFArray{");
        for (int i = 0; i < this.mKeys.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            int baseIndex = i * 4;
            sb.append(this.mKeys[i]);
            sb.append(":[");
            sb.append(this.mCoordinates[baseIndex + 0]);
            sb.append(PtmLog.PAIRE_DELIMETER);
            sb.append(this.mCoordinates[baseIndex + 1]);
            sb.append("],[");
            sb.append(this.mCoordinates[baseIndex + 2]);
            sb.append(PtmLog.PAIRE_DELIMETER);
            sb.append(this.mCoordinates[baseIndex + 3]);
            sb.append("]:flagsArray=");
            sb.append(this.mFlagsArray[i]);
        }
        sb.append("}");
        return sb.toString();
    }

    private SparseRectFArray(SparseRectFArrayBuilder builder) {
        if (builder.mCount == 0) {
            this.mKeys = null;
            this.mCoordinates = null;
            this.mFlagsArray = null;
            return;
        }
        this.mKeys = new int[builder.mCount];
        this.mCoordinates = new float[(builder.mCount * 4)];
        this.mFlagsArray = new int[builder.mCount];
        System.arraycopy(builder.mKeys, 0, this.mKeys, 0, builder.mCount);
        System.arraycopy(builder.mCoordinates, 0, this.mCoordinates, 0, builder.mCount * 4);
        System.arraycopy(builder.mFlagsArray, 0, this.mFlagsArray, 0, builder.mCount);
    }

    public RectF get(int index) {
        if (this.mKeys == null || index < 0) {
            return null;
        }
        int arrayIndex = Arrays.binarySearch(this.mKeys, index);
        if (arrayIndex < 0) {
            return null;
        }
        int baseCoordIndex = arrayIndex * 4;
        return new RectF(this.mCoordinates[baseCoordIndex], this.mCoordinates[baseCoordIndex + 1], this.mCoordinates[baseCoordIndex + 2], this.mCoordinates[baseCoordIndex + 3]);
    }

    public int getFlags(int index, int valueIfKeyNotFound) {
        if (this.mKeys == null || index < 0) {
            return valueIfKeyNotFound;
        }
        int arrayIndex = Arrays.binarySearch(this.mKeys, index);
        if (arrayIndex < 0) {
            return valueIfKeyNotFound;
        }
        return this.mFlagsArray[arrayIndex];
    }

    public int describeContents() {
        return 0;
    }
}
