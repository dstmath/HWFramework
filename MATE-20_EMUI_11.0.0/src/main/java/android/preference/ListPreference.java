package android.preference;

import android.annotation.UnsupportedAppUsage;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.internal.R;

@Deprecated
public class ListPreference extends DialogPreference {
    @UnsupportedAppUsage
    private int mClickedDialogEntryIndex;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mSummary;
    private String mValue;
    private boolean mValueSet;

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPreference, defStyleAttr, defStyleRes);
        this.mEntries = a.getTextArray(0);
        this.mEntryValues = a.getTextArray(1);
        a.recycle();
        TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        this.mSummary = a2.getString(7);
        a2.recycle();
    }

    public ListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842897);
    }

    public ListPreference(Context context) {
        this(context, null);
    }

    public void setEntries(CharSequence[] entries) {
        this.mEntries = entries;
    }

    public void setEntries(int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        this.mEntryValues = entryValues;
    }

    public void setEntryValues(int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    public CharSequence[] getEntryValues() {
        return this.mEntryValues;
    }

    public void setValue(String value) {
        boolean changed = !TextUtils.equals(this.mValue, value);
        if (changed || !this.mValueSet) {
            this.mValue = value;
            this.mValueSet = true;
            persistString(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0017: APUT  (r2v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r4v0 java.lang.CharSequence) */
    @Override // android.preference.Preference
    public CharSequence getSummary() {
        CharSequence entry = getEntry();
        String str = this.mSummary;
        if (str == null) {
            return super.getSummary();
        }
        Object[] objArr = new Object[1];
        objArr[0] = entry == null ? "" : entry;
        return String.format(str, objArr);
    }

    @Override // android.preference.Preference
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && this.mSummary != null) {
            this.mSummary = null;
        } else if (summary != null && !summary.equals(this.mSummary)) {
            this.mSummary = summary.toString();
        }
    }

    public void setValueIndex(int index) {
        CharSequence[] charSequenceArr = this.mEntryValues;
        if (charSequenceArr != null) {
            setValue(charSequenceArr[index].toString());
        }
    }

    public String getValue() {
        return this.mValue;
    }

    public CharSequence getEntry() {
        CharSequence[] charSequenceArr;
        int index = getValueIndex();
        if (index < 0 || (charSequenceArr = this.mEntries) == null) {
            return null;
        }
        return charSequenceArr[index];
    }

    public int findIndexOfValue(String value) {
        CharSequence[] charSequenceArr;
        if (value == null || (charSequenceArr = this.mEntryValues) == null) {
            return -1;
        }
        for (int i = charSequenceArr.length - 1; i >= 0; i--) {
            if (this.mEntryValues[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private int getValueIndex() {
        return findIndexOfValue(this.mValue);
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.DialogPreference
    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        if (this.mEntries == null || this.mEntryValues == null) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }
        this.mClickedDialogEntryIndex = getValueIndex();
        builder.setSingleChoiceItems(this.mEntries, this.mClickedDialogEntryIndex, new DialogInterface.OnClickListener() {
            /* class android.preference.ListPreference.AnonymousClass1 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                ListPreference.this.mClickedDialogEntryIndex = which;
                ListPreference.this.onClick(dialog, -1);
                ListPreference.this.postDismiss();
            }
        });
        builder.setPositiveButton((CharSequence) null, (DialogInterface.OnClickListener) null);
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.DialogPreference
    public void onDialogClosed(boolean positiveResult) {
        int i;
        CharSequence[] charSequenceArr;
        super.onDialogClosed(positiveResult);
        if (positiveResult && (i = this.mClickedDialogEntryIndex) >= 0 && (charSequenceArr = this.mEntryValues) != null && i < charSequenceArr.length) {
            String value = charSequenceArr[i].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedString(this.mValue) : (String) defaultValue);
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.DialogPreference, android.preference.Preference
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.DialogPreference, android.preference.Preference
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.value);
    }

    /* access modifiers changed from: private */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class android.preference.ListPreference.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String value;

        public SavedState(Parcel source) {
            super(source);
            this.value = source.readString();
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }
}
