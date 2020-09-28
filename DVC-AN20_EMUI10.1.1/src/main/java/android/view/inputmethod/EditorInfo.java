package android.view.inputmethod;

import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Printer;
import java.util.Arrays;

public class EditorInfo implements InputType, Parcelable {
    public static final Parcelable.Creator<EditorInfo> CREATOR = new Parcelable.Creator<EditorInfo>() {
        /* class android.view.inputmethod.EditorInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EditorInfo createFromParcel(Parcel source) {
            EditorInfo res = new EditorInfo();
            res.inputType = source.readInt();
            res.imeOptions = source.readInt();
            res.privateImeOptions = source.readString();
            res.actionLabel = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            res.actionId = source.readInt();
            res.initialSelStart = source.readInt();
            res.initialSelEnd = source.readInt();
            res.initialCapsMode = source.readInt();
            res.hintText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            res.label = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            res.packageName = source.readString();
            res.fieldId = source.readInt();
            res.fieldName = source.readString();
            res.extras = source.readBundle();
            LocaleList hintLocales = LocaleList.CREATOR.createFromParcel(source);
            res.hintLocales = hintLocales.isEmpty() ? null : hintLocales;
            res.contentMimeTypes = source.readStringArray();
            res.targetInputMethodUser = UserHandle.readFromParcel(source);
            return res;
        }

        @Override // android.os.Parcelable.Creator
        public EditorInfo[] newArray(int size) {
            return new EditorInfo[size];
        }
    };
    public static final int IME_ACTION_DONE = 6;
    public static final int IME_ACTION_GO = 2;
    public static final int IME_ACTION_NEXT = 5;
    public static final int IME_ACTION_NONE = 1;
    public static final int IME_ACTION_PREVIOUS = 7;
    public static final int IME_ACTION_SEARCH = 3;
    public static final int IME_ACTION_SEND = 4;
    public static final int IME_ACTION_UNSPECIFIED = 0;
    public static final int IME_FLAG_FORCE_ASCII = Integer.MIN_VALUE;
    public static final int IME_FLAG_NAVIGATE_NEXT = 134217728;
    public static final int IME_FLAG_NAVIGATE_PREVIOUS = 67108864;
    public static final int IME_FLAG_NO_ACCESSORY_ACTION = 536870912;
    public static final int IME_FLAG_NO_ENTER_ACTION = 1073741824;
    public static final int IME_FLAG_NO_EXTRACT_UI = 268435456;
    public static final int IME_FLAG_NO_FULLSCREEN = 33554432;
    public static final int IME_FLAG_NO_PERSONALIZED_LEARNING = 16777216;
    public static final int IME_MASK_ACTION = 255;
    public static final int IME_NULL = 0;
    public int actionId = 0;
    public CharSequence actionLabel = null;
    public String[] contentMimeTypes = null;
    public Bundle extras;
    public int fieldId;
    public String fieldName;
    public LocaleList hintLocales = null;
    public CharSequence hintText;
    public int imeOptions = 0;
    public int initialCapsMode = 0;
    public int initialSelEnd = -1;
    public int initialSelStart = -1;
    public int inputType = 0;
    public CharSequence label;
    public String packageName;
    public String privateImeOptions = null;
    public UserHandle targetInputMethodUser = null;

    public final void makeCompatible(int targetSdkVersion) {
        if (targetSdkVersion < 11) {
            int i = this.inputType;
            int i2 = i & 4095;
            if (i2 == 2 || i2 == 18) {
                this.inputType = (this.inputType & InputType.TYPE_MASK_FLAGS) | 2;
            } else if (i2 == 209) {
                this.inputType = (i & InputType.TYPE_MASK_FLAGS) | 33;
            } else if (i2 == 225) {
                this.inputType = (i & InputType.TYPE_MASK_FLAGS) | 129;
            }
        }
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + "inputType=0x" + Integer.toHexString(this.inputType) + " imeOptions=0x" + Integer.toHexString(this.imeOptions) + " privateImeOptions=" + this.privateImeOptions);
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("actionLabel=");
        sb.append((Object) this.actionLabel);
        sb.append(" actionId=");
        sb.append(this.actionId);
        pw.println(sb.toString());
        pw.println(prefix + "initialSelStart=" + this.initialSelStart + " initialSelEnd=" + this.initialSelEnd + " initialCapsMode=0x" + Integer.toHexString(this.initialCapsMode));
        StringBuilder sb2 = new StringBuilder();
        sb2.append(prefix);
        sb2.append("hintText=");
        sb2.append((Object) this.hintText);
        sb2.append(" label=");
        sb2.append((Object) this.label);
        pw.println(sb2.toString());
        pw.println(prefix + "packageName=" + this.packageName + " fieldId=" + this.fieldId + " fieldName=" + this.fieldName);
        StringBuilder sb3 = new StringBuilder();
        sb3.append(prefix);
        sb3.append("extras=");
        sb3.append(this.extras);
        pw.println(sb3.toString());
        pw.println(prefix + "hintLocales=" + this.hintLocales);
        pw.println(prefix + "contentMimeTypes=" + Arrays.toString(this.contentMimeTypes));
        if (this.targetInputMethodUser != null) {
            pw.println(prefix + "targetInputMethodUserId=" + this.targetInputMethodUser.getIdentifier());
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.inputType);
        dest.writeInt(this.imeOptions);
        dest.writeString(this.privateImeOptions);
        TextUtils.writeToParcel(this.actionLabel, dest, flags);
        dest.writeInt(this.actionId);
        dest.writeInt(this.initialSelStart);
        dest.writeInt(this.initialSelEnd);
        dest.writeInt(this.initialCapsMode);
        TextUtils.writeToParcel(this.hintText, dest, flags);
        TextUtils.writeToParcel(this.label, dest, flags);
        dest.writeString(this.packageName);
        dest.writeInt(this.fieldId);
        dest.writeString(this.fieldName);
        dest.writeBundle(this.extras);
        LocaleList localeList = this.hintLocales;
        if (localeList != null) {
            localeList.writeToParcel(dest, flags);
        } else {
            LocaleList.getEmptyLocaleList().writeToParcel(dest, flags);
        }
        dest.writeStringArray(this.contentMimeTypes);
        UserHandle.writeToParcel(this.targetInputMethodUser, dest);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
