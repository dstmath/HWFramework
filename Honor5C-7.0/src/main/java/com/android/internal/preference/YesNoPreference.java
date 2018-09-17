package com.android.internal.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.DialogPreference;
import android.preference.Preference.BaseSavedState;
import android.util.AttributeSet;
import com.android.internal.R;

public class YesNoPreference extends DialogPreference {
    private boolean mWasPositiveResult;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        boolean wasPositiveResult;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.preference.YesNoPreference.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.preference.YesNoPreference.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.preference.YesNoPreference.SavedState.<clinit>():void");
        }

        public SavedState(Parcel source) {
            boolean z = true;
            super(source);
            if (source.readInt() != 1) {
                z = false;
            }
            this.wasPositiveResult = z;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.wasPositiveResult ? 1 : 0);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public YesNoPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public YesNoPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public YesNoPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.yesNoPreferenceStyle);
    }

    public YesNoPreference(Context context) {
        this(context, null);
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (callChangeListener(Boolean.valueOf(positiveResult))) {
            setValue(positiveResult);
        }
    }

    public void setValue(boolean value) {
        boolean z;
        this.mWasPositiveResult = value;
        persistBoolean(value);
        if (value) {
            z = false;
        } else {
            z = true;
        }
        notifyDependencyChange(z);
    }

    public boolean getValue() {
        return this.mWasPositiveResult;
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Boolean.valueOf(a.getBoolean(index, false));
    }

    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        boolean persistedBoolean;
        if (restorePersistedValue) {
            persistedBoolean = getPersistedBoolean(this.mWasPositiveResult);
        } else {
            persistedBoolean = ((Boolean) defaultValue).booleanValue();
        }
        setValue(persistedBoolean);
    }

    public boolean shouldDisableDependents() {
        return this.mWasPositiveResult ? super.shouldDisableDependents() : true;
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.wasPositiveResult = getValue();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state.getClass().equals(SavedState.class)) {
            SavedState myState = (SavedState) state;
            super.onRestoreInstanceState(myState.getSuperState());
            setValue(myState.wasPositiveResult);
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
