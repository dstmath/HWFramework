package android.preference;

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

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {
    private int mMax;
    private int mProgress;
    private boolean mTrackingTouch;

    private static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

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

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.progress);
            dest.writeInt(this.max);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        setMax(a.getInt(2, this.mMax));
        a.recycle();
        TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);
        int layoutResId = a2.getResourceId(0, 17367240);
        a2.recycle();
        setLayoutResource(layoutResId);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 17891517);
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    /* access modifiers changed from: protected */
    public void onBindView(View view) {
        super.onBindView(view);
        SeekBar seekBar = (SeekBar) view.findViewById(16909298);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(this.mMax);
        seekBar.setProgress(this.mProgress);
        seekBar.setEnabled(isEnabled());
    }

    /* access modifiers changed from: protected */
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
    public Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getInt(index, 0));
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != 0) {
            return false;
        }
        SeekBar seekBar = (SeekBar) v.findViewById(16909298);
        if (seekBar == null) {
            return false;
        }
        return seekBar.onKeyDown(keyCode, event);
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

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && !this.mTrackingTouch) {
            syncProgress(seekBar);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = true;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = false;
        if (seekBar.getProgress() != this.mProgress) {
            syncProgress(seekBar);
        }
    }

    /* access modifiers changed from: protected */
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
}
