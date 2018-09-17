package android.view.inputmethod;

import android.os.Bundle;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Printer;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.hisi.perfhub.PerfHub;
import com.huawei.pgmng.log.LogPower;

public class EditorInfo implements InputType, Parcelable {
    public static final Creator<EditorInfo> CREATOR = null;
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
    public static final int IME_MASK_ACTION = 255;
    public static final int IME_NULL = 0;
    public int actionId;
    public CharSequence actionLabel;
    public Bundle extras;
    public int fieldId;
    public String fieldName;
    public LocaleList hintLocales;
    public CharSequence hintText;
    public int imeOptions;
    public int initialCapsMode;
    public int initialSelEnd;
    public int initialSelStart;
    public int inputType;
    public CharSequence label;
    public String packageName;
    public String privateImeOptions;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.inputmethod.EditorInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.inputmethod.EditorInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.EditorInfo.<clinit>():void");
    }

    public EditorInfo() {
        this.inputType = IME_ACTION_UNSPECIFIED;
        this.imeOptions = IME_ACTION_UNSPECIFIED;
        this.privateImeOptions = null;
        this.actionLabel = null;
        this.actionId = IME_ACTION_UNSPECIFIED;
        this.initialSelStart = -1;
        this.initialSelEnd = -1;
        this.initialCapsMode = IME_ACTION_UNSPECIFIED;
        this.hintLocales = null;
    }

    public final void makeCompatible(int targetSdkVersion) {
        if (targetSdkVersion < 11) {
            switch (this.inputType & 4095) {
                case IME_ACTION_GO /*2*/:
                case PerfHub.PERF_TAG_IPA_CONTROL_TEMP /*18*/:
                    this.inputType = (this.inputType & InputType.TYPE_MASK_FLAGS) | IME_ACTION_GO;
                case MetricsEvent.ACTION_VOLUME_SLIDER /*209*/:
                    this.inputType = (this.inputType & InputType.TYPE_MASK_FLAGS) | 33;
                case MetricsEvent.ABOUT_LEGAL_SETTINGS /*225*/:
                    this.inputType = (this.inputType & InputType.TYPE_MASK_FLAGS) | LogPower.START_CAMERA;
                default:
            }
        }
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + "inputType=0x" + Integer.toHexString(this.inputType) + " imeOptions=0x" + Integer.toHexString(this.imeOptions) + " privateImeOptions=" + this.privateImeOptions);
        pw.println(prefix + "actionLabel=" + this.actionLabel + " actionId=" + this.actionId);
        pw.println(prefix + "initialSelStart=" + this.initialSelStart + " initialSelEnd=" + this.initialSelEnd + " initialCapsMode=0x" + Integer.toHexString(this.initialCapsMode));
        pw.println(prefix + "hintText=" + this.hintText + " label=" + this.label);
        pw.println(prefix + "packageName=" + this.packageName + " fieldId=" + this.fieldId + " fieldName=" + this.fieldName);
        pw.println(prefix + "extras=" + this.extras);
        pw.println(prefix + "hintLocales=" + this.hintLocales);
    }

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
        if (this.hintLocales != null) {
            this.hintLocales.writeToParcel(dest, flags);
        } else {
            LocaleList.getEmptyLocaleList().writeToParcel(dest, flags);
        }
    }

    public int describeContents() {
        return IME_ACTION_UNSPECIFIED;
    }
}
