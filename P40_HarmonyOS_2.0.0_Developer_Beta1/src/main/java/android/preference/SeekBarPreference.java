package android.preference;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import com.android.internal.R;

@Deprecated
public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {
    private int mMax;
    private int mProgress;
    private boolean mTrackingTouch;

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        setMax(a.getInt(2, this.mMax));
        a.recycle();
        TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);
        int layoutResId = a2.getResourceId(0, R.layout.preference_widget_seekbar);
        a2.recycle();
        setLayoutResource(layoutResId);
    }

    @UnsupportedAppUsage
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @UnsupportedAppUsage
    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarPreferenceStyle);
    }

    @UnsupportedAppUsage
    public SeekBarPreference(Context context) {
        this(context, null);
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public void onBindView(View view) {
        super.onBindView(view);
        SeekBar seekBar = (SeekBar) view.findViewById(16908362);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(this.mMax);
        seekBar.setProgress(this.mProgress);
        seekBar.setEnabled(isEnabled());
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int i;
        if (restoreValue) {
            i = getPersistedInt(this.mProgress);
        } else {
            i = ((Integer) defaultValue).intValue();
        }
        setProgress(i);
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getInt(index, 0));
    }

    @Override // android.preference.Preference
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        SeekBar seekBar;
        if (event.getAction() == 0 && (seekBar = (SeekBar) v.findViewById(16908362)) != null) {
            return seekBar.onKeyDown(keyCode, event);
        }
        return false;
    }

    public void setMax(int max) {
        if (max != this.mMax) {
            this.mMax = max;
            notifyChanged();
        }
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    private void setProgress(int progress, boolean notifyChanged) {
        if (progress > this.mMax) {
            progress = this.mMax;
        }
        if (progress < 0) {
            progress = 0;
        }
        if (progress != this.mProgress) {
            this.mProgress = progress;
            persistInt(progress);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    public int getProgress() {
        return this.mProgress;
    }

    /* access modifiers changed from: package-private */
    public void syncProgress(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress == this.mProgress) {
            return;
        }
        if (callChangeListener(Integer.valueOf(progress))) {
            setProgress(progress, false);
        } else {
            seekBar.setProgress(this.mProgress);
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && !this.mTrackingTouch) {
            syncProgress(seekBar);
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = true;
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = false;
        if (seekBar.getProgress() != this.mProgress) {
            syncProgress(seekBar);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.progress = this.mProgress;
        myState.max = this.mMax;
        return myState;
    }

    /* access modifiers changed from: protected */
    @Override // android.preference.Preference
    public void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        this.mProgress = myState.progress;
        this.mMax = myState.max;
        notifyChanged();
    }

    /* access modifiers changed from: private */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class android.preference.SeekBarPreference.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int max;
        int progress;

        public SavedState(Parcel source) {
            super(source);
            this.progress = source.readInt();
            this.max = source.readInt();
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.progress);
            dest.writeInt(this.max);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }
}
