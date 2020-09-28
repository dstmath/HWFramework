package com.android.internal.view;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Matrix;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.InputChannel;
import com.android.internal.view.IInputMethodSession;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class InputBindResult implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<InputBindResult> CREATOR = new Parcelable.Creator<InputBindResult>() {
        /* class com.android.internal.view.InputBindResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InputBindResult createFromParcel(Parcel source) {
            return new InputBindResult(source);
        }

        @Override // android.os.Parcelable.Creator
        public InputBindResult[] newArray(int size) {
            return new InputBindResult[size];
        }
    };
    public static final InputBindResult DISPLAY_ID_MISMATCH = error(13);
    public static final InputBindResult IME_NOT_CONNECTED = error(8);
    public static final InputBindResult INVALID_CLIENT = error(15);
    public static final InputBindResult INVALID_DISPLAY_ID = error(14);
    public static final InputBindResult INVALID_PACKAGE_NAME = error(6);
    public static final InputBindResult INVALID_USER = error(9);
    public static final InputBindResult NOT_IME_TARGET_WINDOW = error(11);
    public static final InputBindResult NO_EDITOR = error(12);
    public static final InputBindResult NO_IME = error(5);
    public static final InputBindResult NULL = error(4);
    public static final InputBindResult NULL_EDITOR_INFO = error(10);
    public final InputChannel channel;
    public final String id;
    private final float[] mActivityViewToScreenMatrixValues;
    @UnsupportedAppUsage
    public final IInputMethodSession method;
    public final int result;
    public final int sequence;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResultCode {
        public static final int ERROR_DISPLAY_ID_MISMATCH = 13;
        public static final int ERROR_IME_NOT_CONNECTED = 8;
        public static final int ERROR_INVALID_CLIENT = 15;
        public static final int ERROR_INVALID_DISPLAY_ID = 14;
        public static final int ERROR_INVALID_PACKAGE_NAME = 6;
        public static final int ERROR_INVALID_USER = 9;
        public static final int ERROR_NOT_IME_TARGET_WINDOW = 11;
        public static final int ERROR_NO_EDITOR = 12;
        public static final int ERROR_NO_IME = 5;
        public static final int ERROR_NULL = 4;
        public static final int ERROR_NULL_EDITOR_INFO = 10;
        public static final int ERROR_SYSTEM_NOT_READY = 7;
        public static final int SUCCESS_REPORT_WINDOW_FOCUS_ONLY = 3;
        public static final int SUCCESS_WAITING_IME_BINDING = 2;
        public static final int SUCCESS_WAITING_IME_SESSION = 1;
        public static final int SUCCESS_WITH_IME_SESSION = 0;
    }

    public Matrix getActivityViewToScreenMatrix() {
        if (this.mActivityViewToScreenMatrixValues == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.setValues(this.mActivityViewToScreenMatrixValues);
        return matrix;
    }

    public InputBindResult(int _result, IInputMethodSession _method, InputChannel _channel, String _id, int _sequence, Matrix activityViewToScreenMatrix) {
        this.result = _result;
        this.method = _method;
        this.channel = _channel;
        this.id = _id;
        this.sequence = _sequence;
        if (activityViewToScreenMatrix == null) {
            this.mActivityViewToScreenMatrixValues = null;
            return;
        }
        this.mActivityViewToScreenMatrixValues = new float[9];
        activityViewToScreenMatrix.getValues(this.mActivityViewToScreenMatrixValues);
    }

    InputBindResult(Parcel source) {
        this.result = source.readInt();
        this.method = IInputMethodSession.Stub.asInterface(source.readStrongBinder());
        if (source.readInt() != 0) {
            this.channel = InputChannel.CREATOR.createFromParcel(source);
        } else {
            this.channel = null;
        }
        this.id = source.readString();
        this.sequence = source.readInt();
        this.mActivityViewToScreenMatrixValues = source.createFloatArray();
    }

    public String toString() {
        return "InputBindResult{result=" + getResultString() + " method=" + this.method + " id=" + this.id + " sequence=" + this.sequence + " activityViewToScreenMatrix=" + getActivityViewToScreenMatrix() + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.result);
        dest.writeStrongInterface(this.method);
        if (this.channel != null) {
            dest.writeInt(1);
            this.channel.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(this.id);
        dest.writeInt(this.sequence);
        dest.writeFloatArray(this.mActivityViewToScreenMatrixValues);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        InputChannel inputChannel = this.channel;
        if (inputChannel != null) {
            return inputChannel.describeContents();
        }
        return 0;
    }

    public String getResultString() {
        switch (this.result) {
            case 0:
                return "SUCCESS_WITH_IME_SESSION";
            case 1:
                return "SUCCESS_WAITING_IME_SESSION";
            case 2:
                return "SUCCESS_WAITING_IME_BINDING";
            case 3:
                return "SUCCESS_REPORT_WINDOW_FOCUS_ONLY";
            case 4:
                return "ERROR_NULL";
            case 5:
                return "ERROR_NO_IME";
            case 6:
                return "ERROR_INVALID_PACKAGE_NAME";
            case 7:
                return "ERROR_SYSTEM_NOT_READY";
            case 8:
                return "ERROR_IME_NOT_CONNECTED";
            case 9:
                return "ERROR_INVALID_USER";
            case 10:
                return "ERROR_NULL_EDITOR_INFO";
            case 11:
                return "ERROR_NOT_IME_TARGET_WINDOW";
            case 12:
                return "ERROR_NO_EDITOR";
            case 13:
                return "ERROR_DISPLAY_ID_MISMATCH";
            case 14:
                return "ERROR_INVALID_DISPLAY_ID";
            case 15:
                return "ERROR_INVALID_CLIENT";
            default:
                return "Unknown(" + this.result + ")";
        }
    }

    private static InputBindResult error(int result2) {
        return new InputBindResult(result2, null, null, null, -1, null);
    }
}
