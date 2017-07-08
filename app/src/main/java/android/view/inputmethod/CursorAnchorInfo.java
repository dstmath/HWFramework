package android.view.inputmethod;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.PtmLog;
import android.view.inputmethod.SparseRectFArray.SparseRectFArrayBuilder;
import java.util.Arrays;
import java.util.Objects;

public final class CursorAnchorInfo implements Parcelable {
    public static final Creator<CursorAnchorInfo> CREATOR = null;
    public static final int FLAG_HAS_INVISIBLE_REGION = 2;
    public static final int FLAG_HAS_VISIBLE_REGION = 1;
    public static final int FLAG_IS_RTL = 4;
    private final SparseRectFArray mCharacterBoundsArray;
    private final CharSequence mComposingText;
    private final int mComposingTextStart;
    private final int mHashCode;
    private final float mInsertionMarkerBaseline;
    private final float mInsertionMarkerBottom;
    private final int mInsertionMarkerFlags;
    private final float mInsertionMarkerHorizontal;
    private final float mInsertionMarkerTop;
    private final float[] mMatrixValues;
    private final int mSelectionEnd;
    private final int mSelectionStart;

    public static final class Builder {
        private SparseRectFArrayBuilder mCharacterBoundsArrayBuilder;
        private CharSequence mComposingText;
        private int mComposingTextStart;
        private float mInsertionMarkerBaseline;
        private float mInsertionMarkerBottom;
        private int mInsertionMarkerFlags;
        private float mInsertionMarkerHorizontal;
        private float mInsertionMarkerTop;
        private boolean mMatrixInitialized;
        private float[] mMatrixValues;
        private int mSelectionEnd;
        private int mSelectionStart;

        public Builder() {
            this.mSelectionStart = -1;
            this.mSelectionEnd = -1;
            this.mComposingTextStart = -1;
            this.mComposingText = null;
            this.mInsertionMarkerHorizontal = Float.NaN;
            this.mInsertionMarkerTop = Float.NaN;
            this.mInsertionMarkerBaseline = Float.NaN;
            this.mInsertionMarkerBottom = Float.NaN;
            this.mInsertionMarkerFlags = 0;
            this.mCharacterBoundsArrayBuilder = null;
            this.mMatrixValues = null;
            this.mMatrixInitialized = false;
        }

        public Builder setSelectionRange(int newStart, int newEnd) {
            this.mSelectionStart = newStart;
            this.mSelectionEnd = newEnd;
            return this;
        }

        public Builder setComposingText(int composingTextStart, CharSequence composingText) {
            this.mComposingTextStart = composingTextStart;
            if (composingText == null) {
                this.mComposingText = null;
            } else {
                this.mComposingText = new SpannedString(composingText);
            }
            return this;
        }

        public Builder setInsertionMarkerLocation(float horizontalPosition, float lineTop, float lineBaseline, float lineBottom, int flags) {
            this.mInsertionMarkerHorizontal = horizontalPosition;
            this.mInsertionMarkerTop = lineTop;
            this.mInsertionMarkerBaseline = lineBaseline;
            this.mInsertionMarkerBottom = lineBottom;
            this.mInsertionMarkerFlags = flags;
            return this;
        }

        public Builder addCharacterBounds(int index, float left, float top, float right, float bottom, int flags) {
            if (index < 0) {
                throw new IllegalArgumentException("index must not be a negative integer.");
            }
            if (this.mCharacterBoundsArrayBuilder == null) {
                this.mCharacterBoundsArrayBuilder = new SparseRectFArrayBuilder();
            }
            this.mCharacterBoundsArrayBuilder.append(index, left, top, right, bottom, flags);
            return this;
        }

        public Builder setMatrix(Matrix matrix) {
            if (this.mMatrixValues == null) {
                this.mMatrixValues = new float[9];
            }
            if (matrix == null) {
                matrix = Matrix.IDENTITY_MATRIX;
            }
            matrix.getValues(this.mMatrixValues);
            this.mMatrixInitialized = true;
            return this;
        }

        public CursorAnchorInfo build() {
            boolean hasCharacterBounds = false;
            if (!this.mMatrixInitialized) {
                if (!(this.mCharacterBoundsArrayBuilder == null || this.mCharacterBoundsArrayBuilder.isEmpty())) {
                    hasCharacterBounds = true;
                }
                if (!(!hasCharacterBounds && Float.isNaN(this.mInsertionMarkerHorizontal) && Float.isNaN(this.mInsertionMarkerTop) && Float.isNaN(this.mInsertionMarkerBaseline) && Float.isNaN(this.mInsertionMarkerBottom))) {
                    throw new IllegalArgumentException("Coordinate transformation matrix is required when positional parameters are specified.");
                }
            }
            return new CursorAnchorInfo();
        }

        public void reset() {
            this.mSelectionStart = -1;
            this.mSelectionEnd = -1;
            this.mComposingTextStart = -1;
            this.mComposingText = null;
            this.mInsertionMarkerFlags = 0;
            this.mInsertionMarkerHorizontal = Float.NaN;
            this.mInsertionMarkerTop = Float.NaN;
            this.mInsertionMarkerBaseline = Float.NaN;
            this.mInsertionMarkerBottom = Float.NaN;
            this.mMatrixInitialized = false;
            if (this.mCharacterBoundsArrayBuilder != null) {
                this.mCharacterBoundsArrayBuilder.reset();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.inputmethod.CursorAnchorInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.inputmethod.CursorAnchorInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.CursorAnchorInfo.<clinit>():void");
    }

    public CursorAnchorInfo(Parcel source) {
        this.mHashCode = source.readInt();
        this.mSelectionStart = source.readInt();
        this.mSelectionEnd = source.readInt();
        this.mComposingTextStart = source.readInt();
        this.mComposingText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mInsertionMarkerFlags = source.readInt();
        this.mInsertionMarkerHorizontal = source.readFloat();
        this.mInsertionMarkerTop = source.readFloat();
        this.mInsertionMarkerBaseline = source.readFloat();
        this.mInsertionMarkerBottom = source.readFloat();
        this.mCharacterBoundsArray = (SparseRectFArray) source.readParcelable(SparseRectFArray.class.getClassLoader());
        this.mMatrixValues = source.createFloatArray();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mHashCode);
        dest.writeInt(this.mSelectionStart);
        dest.writeInt(this.mSelectionEnd);
        dest.writeInt(this.mComposingTextStart);
        TextUtils.writeToParcel(this.mComposingText, dest, flags);
        dest.writeInt(this.mInsertionMarkerFlags);
        dest.writeFloat(this.mInsertionMarkerHorizontal);
        dest.writeFloat(this.mInsertionMarkerTop);
        dest.writeFloat(this.mInsertionMarkerBaseline);
        dest.writeFloat(this.mInsertionMarkerBottom);
        dest.writeParcelable(this.mCharacterBoundsArray, flags);
        dest.writeFloatArray(this.mMatrixValues);
    }

    public int hashCode() {
        return this.mHashCode;
    }

    private static boolean areSameFloatImpl(float a, float b) {
        boolean z = true;
        if (Float.isNaN(a) && Float.isNaN(b)) {
            return true;
        }
        if (a != b) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CursorAnchorInfo)) {
            return false;
        }
        CursorAnchorInfo that = (CursorAnchorInfo) obj;
        if (hashCode() != that.hashCode() || this.mSelectionStart != that.mSelectionStart || this.mSelectionEnd != that.mSelectionEnd || this.mInsertionMarkerFlags != that.mInsertionMarkerFlags || !areSameFloatImpl(this.mInsertionMarkerHorizontal, that.mInsertionMarkerHorizontal) || !areSameFloatImpl(this.mInsertionMarkerTop, that.mInsertionMarkerTop) || !areSameFloatImpl(this.mInsertionMarkerBaseline, that.mInsertionMarkerBaseline) || !areSameFloatImpl(this.mInsertionMarkerBottom, that.mInsertionMarkerBottom) || !Objects.equals(this.mCharacterBoundsArray, that.mCharacterBoundsArray) || this.mComposingTextStart != that.mComposingTextStart || !Objects.equals(this.mComposingText, that.mComposingText) || this.mMatrixValues.length != that.mMatrixValues.length) {
            return false;
        }
        for (int i = 0; i < this.mMatrixValues.length; i += FLAG_HAS_VISIBLE_REGION) {
            if (this.mMatrixValues[i] != that.mMatrixValues[i]) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "CursorAnchorInfo{mHashCode=" + this.mHashCode + " mSelection=" + this.mSelectionStart + PtmLog.PAIRE_DELIMETER + this.mSelectionEnd + " mComposingTextStart=" + this.mComposingTextStart + " mComposingText=" + Objects.toString(this.mComposingText) + " mInsertionMarkerFlags=" + this.mInsertionMarkerFlags + " mInsertionMarkerHorizontal=" + this.mInsertionMarkerHorizontal + " mInsertionMarkerTop=" + this.mInsertionMarkerTop + " mInsertionMarkerBaseline=" + this.mInsertionMarkerBaseline + " mInsertionMarkerBottom=" + this.mInsertionMarkerBottom + " mCharacterBoundsArray=" + Objects.toString(this.mCharacterBoundsArray) + " mMatrix=" + Arrays.toString(this.mMatrixValues) + "}";
    }

    private CursorAnchorInfo(Builder builder) {
        SparseRectFArray sparseRectFArray = null;
        this.mSelectionStart = builder.mSelectionStart;
        this.mSelectionEnd = builder.mSelectionEnd;
        this.mComposingTextStart = builder.mComposingTextStart;
        this.mComposingText = builder.mComposingText;
        this.mInsertionMarkerFlags = builder.mInsertionMarkerFlags;
        this.mInsertionMarkerHorizontal = builder.mInsertionMarkerHorizontal;
        this.mInsertionMarkerTop = builder.mInsertionMarkerTop;
        this.mInsertionMarkerBaseline = builder.mInsertionMarkerBaseline;
        this.mInsertionMarkerBottom = builder.mInsertionMarkerBottom;
        if (builder.mCharacterBoundsArrayBuilder != null) {
            sparseRectFArray = builder.mCharacterBoundsArrayBuilder.build();
        }
        this.mCharacterBoundsArray = sparseRectFArray;
        this.mMatrixValues = new float[9];
        if (builder.mMatrixInitialized) {
            System.arraycopy(builder.mMatrixValues, 0, this.mMatrixValues, 0, 9);
        } else {
            Matrix.IDENTITY_MATRIX.getValues(this.mMatrixValues);
        }
        this.mHashCode = (Objects.hashCode(this.mComposingText) * 31) + Arrays.hashCode(this.mMatrixValues);
    }

    public int getSelectionStart() {
        return this.mSelectionStart;
    }

    public int getSelectionEnd() {
        return this.mSelectionEnd;
    }

    public int getComposingTextStart() {
        return this.mComposingTextStart;
    }

    public CharSequence getComposingText() {
        return this.mComposingText;
    }

    public int getInsertionMarkerFlags() {
        return this.mInsertionMarkerFlags;
    }

    public float getInsertionMarkerHorizontal() {
        return this.mInsertionMarkerHorizontal;
    }

    public float getInsertionMarkerTop() {
        return this.mInsertionMarkerTop;
    }

    public float getInsertionMarkerBaseline() {
        return this.mInsertionMarkerBaseline;
    }

    public float getInsertionMarkerBottom() {
        return this.mInsertionMarkerBottom;
    }

    public RectF getCharacterBounds(int index) {
        if (this.mCharacterBoundsArray == null) {
            return null;
        }
        return this.mCharacterBoundsArray.get(index);
    }

    public int getCharacterBoundsFlags(int index) {
        if (this.mCharacterBoundsArray == null) {
            return 0;
        }
        return this.mCharacterBoundsArray.getFlags(index, 0);
    }

    public Matrix getMatrix() {
        Matrix matrix = new Matrix();
        matrix.setValues(this.mMatrixValues);
        return matrix;
    }

    public int describeContents() {
        return 0;
    }
}
