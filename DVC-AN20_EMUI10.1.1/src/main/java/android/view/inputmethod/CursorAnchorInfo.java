package android.view.inputmethod;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;
import android.text.SpannedString;
import android.text.TextUtils;
import android.view.inputmethod.SparseRectFArray;
import java.util.Arrays;
import java.util.Objects;

public final class CursorAnchorInfo implements Parcelable {
    public static final Parcelable.Creator<CursorAnchorInfo> CREATOR = new Parcelable.Creator<CursorAnchorInfo>() {
        /* class android.view.inputmethod.CursorAnchorInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CursorAnchorInfo createFromParcel(Parcel source) {
            return new CursorAnchorInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public CursorAnchorInfo[] newArray(int size) {
            return new CursorAnchorInfo[size];
        }
    };
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

    public CursorAnchorInfo(Parcel source) {
        this.mHashCode = source.readInt();
        this.mSelectionStart = source.readInt();
        this.mSelectionEnd = source.readInt();
        this.mComposingTextStart = source.readInt();
        this.mComposingText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mInsertionMarkerFlags = source.readInt();
        this.mInsertionMarkerHorizontal = source.readFloat();
        this.mInsertionMarkerTop = source.readFloat();
        this.mInsertionMarkerBaseline = source.readFloat();
        this.mInsertionMarkerBottom = source.readFloat();
        this.mCharacterBoundsArray = (SparseRectFArray) source.readParcelable(SparseRectFArray.class.getClassLoader());
        this.mMatrixValues = source.createFloatArray();
    }

    @Override // android.os.Parcelable
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
        if ((!Float.isNaN(a) || !Float.isNaN(b)) && a != b) {
            return false;
        }
        return true;
    }

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
        int i = 0;
        while (true) {
            float[] fArr = this.mMatrixValues;
            if (i >= fArr.length) {
                return true;
            }
            if (fArr[i] != that.mMatrixValues[i]) {
                return false;
            }
            i++;
        }
    }

    public String toString() {
        return "CursorAnchorInfo{mHashCode=" + this.mHashCode + " mSelection=" + this.mSelectionStart + SmsManager.REGEX_PREFIX_DELIMITER + this.mSelectionEnd + " mComposingTextStart=" + this.mComposingTextStart + " mComposingText=" + Objects.toString(this.mComposingText) + " mInsertionMarkerFlags=" + this.mInsertionMarkerFlags + " mInsertionMarkerHorizontal=" + this.mInsertionMarkerHorizontal + " mInsertionMarkerTop=" + this.mInsertionMarkerTop + " mInsertionMarkerBaseline=" + this.mInsertionMarkerBaseline + " mInsertionMarkerBottom=" + this.mInsertionMarkerBottom + " mCharacterBoundsArray=" + Objects.toString(this.mCharacterBoundsArray) + " mMatrix=" + Arrays.toString(this.mMatrixValues) + "}";
    }

    public static final class Builder {
        private SparseRectFArray.SparseRectFArrayBuilder mCharacterBoundsArrayBuilder = null;
        private CharSequence mComposingText = null;
        private int mComposingTextStart = -1;
        private float mInsertionMarkerBaseline = Float.NaN;
        private float mInsertionMarkerBottom = Float.NaN;
        private int mInsertionMarkerFlags = 0;
        private float mInsertionMarkerHorizontal = Float.NaN;
        private float mInsertionMarkerTop = Float.NaN;
        private boolean mMatrixInitialized = false;
        private float[] mMatrixValues = null;
        private int mSelectionEnd = -1;
        private int mSelectionStart = -1;

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
            if (index >= 0) {
                if (this.mCharacterBoundsArrayBuilder == null) {
                    this.mCharacterBoundsArrayBuilder = new SparseRectFArray.SparseRectFArrayBuilder();
                }
                this.mCharacterBoundsArrayBuilder.append(index, left, top, right, bottom, flags);
                return this;
            }
            throw new IllegalArgumentException("index must not be a negative integer.");
        }

        public Builder setMatrix(Matrix matrix) {
            if (this.mMatrixValues == null) {
                this.mMatrixValues = new float[9];
            }
            (matrix != null ? matrix : Matrix.IDENTITY_MATRIX).getValues(this.mMatrixValues);
            this.mMatrixInitialized = true;
            return this;
        }

        public CursorAnchorInfo build() {
            if (!this.mMatrixInitialized) {
                SparseRectFArray.SparseRectFArrayBuilder sparseRectFArrayBuilder = this.mCharacterBoundsArrayBuilder;
                if ((sparseRectFArrayBuilder != null && !sparseRectFArrayBuilder.isEmpty()) || !Float.isNaN(this.mInsertionMarkerHorizontal) || !Float.isNaN(this.mInsertionMarkerTop) || !Float.isNaN(this.mInsertionMarkerBaseline) || !Float.isNaN(this.mInsertionMarkerBottom)) {
                    throw new IllegalArgumentException("Coordinate transformation matrix is required when positional parameters are specified.");
                }
            }
            return CursorAnchorInfo.create(this);
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
            SparseRectFArray.SparseRectFArrayBuilder sparseRectFArrayBuilder = this.mCharacterBoundsArrayBuilder;
            if (sparseRectFArrayBuilder != null) {
                sparseRectFArrayBuilder.reset();
            }
        }
    }

    /* access modifiers changed from: private */
    public static CursorAnchorInfo create(Builder builder) {
        SparseRectFArray characterBoundsArray;
        if (builder.mCharacterBoundsArrayBuilder != null) {
            characterBoundsArray = builder.mCharacterBoundsArrayBuilder.build();
        } else {
            characterBoundsArray = null;
        }
        float[] matrixValues = new float[9];
        if (builder.mMatrixInitialized) {
            System.arraycopy(builder.mMatrixValues, 0, matrixValues, 0, 9);
        } else {
            Matrix.IDENTITY_MATRIX.getValues(matrixValues);
        }
        return new CursorAnchorInfo(builder.mSelectionStart, builder.mSelectionEnd, builder.mComposingTextStart, builder.mComposingText, builder.mInsertionMarkerFlags, builder.mInsertionMarkerHorizontal, builder.mInsertionMarkerTop, builder.mInsertionMarkerBaseline, builder.mInsertionMarkerBottom, characterBoundsArray, matrixValues);
    }

    private CursorAnchorInfo(int selectionStart, int selectionEnd, int composingTextStart, CharSequence composingText, int insertionMarkerFlags, float insertionMarkerHorizontal, float insertionMarkerTop, float insertionMarkerBaseline, float insertionMarkerBottom, SparseRectFArray characterBoundsArray, float[] matrixValues) {
        this.mSelectionStart = selectionStart;
        this.mSelectionEnd = selectionEnd;
        this.mComposingTextStart = composingTextStart;
        this.mComposingText = composingText;
        this.mInsertionMarkerFlags = insertionMarkerFlags;
        this.mInsertionMarkerHorizontal = insertionMarkerHorizontal;
        this.mInsertionMarkerTop = insertionMarkerTop;
        this.mInsertionMarkerBaseline = insertionMarkerBaseline;
        this.mInsertionMarkerBottom = insertionMarkerBottom;
        this.mCharacterBoundsArray = characterBoundsArray;
        this.mMatrixValues = matrixValues;
        this.mHashCode = (Objects.hashCode(this.mComposingText) * 31) + Arrays.hashCode(matrixValues);
    }

    public static CursorAnchorInfo createForAdditionalParentMatrix(CursorAnchorInfo original, Matrix parentMatrix) {
        return new CursorAnchorInfo(original.mSelectionStart, original.mSelectionEnd, original.mComposingTextStart, original.mComposingText, original.mInsertionMarkerFlags, original.mInsertionMarkerHorizontal, original.mInsertionMarkerTop, original.mInsertionMarkerBaseline, original.mInsertionMarkerBottom, original.mCharacterBoundsArray, computeMatrixValues(parentMatrix, original));
    }

    private static float[] computeMatrixValues(Matrix parentMatrix, CursorAnchorInfo info) {
        if (parentMatrix.isIdentity()) {
            return info.mMatrixValues;
        }
        Matrix newMatrix = new Matrix();
        newMatrix.setValues(info.mMatrixValues);
        newMatrix.postConcat(parentMatrix);
        float[] matrixValues = new float[9];
        newMatrix.getValues(matrixValues);
        return matrixValues;
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
        SparseRectFArray sparseRectFArray = this.mCharacterBoundsArray;
        if (sparseRectFArray == null) {
            return null;
        }
        return sparseRectFArray.get(index);
    }

    public int getCharacterBoundsFlags(int index) {
        SparseRectFArray sparseRectFArray = this.mCharacterBoundsArray;
        if (sparseRectFArray == null) {
            return 0;
        }
        return sparseRectFArray.getFlags(index, 0);
    }

    public Matrix getMatrix() {
        Matrix matrix = new Matrix();
        matrix.setValues(this.mMatrixValues);
        return matrix;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
