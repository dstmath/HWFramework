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
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean wasPositiveResult;

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
        this.mWasPositiveResult = value;
        persistBoolean(value);
        notifyDependencyChange(value ^ 1);
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
