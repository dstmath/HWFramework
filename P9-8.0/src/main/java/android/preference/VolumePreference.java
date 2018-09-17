package android.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.Preference.BaseSavedState;
import android.preference.PreferenceManager.OnActivityStopListener;
import android.preference.SeekBarVolumizer.Callback;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.SeekBar;
import com.android.internal.R;

public class VolumePreference extends SeekBarDialogPreference implements OnActivityStopListener, OnKeyListener, Callback {
    private SeekBarVolumizer mSeekBarVolumizer;
    private int mStreamType;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        VolumeStore mVolumeStore = new VolumeStore();

        public SavedState(Parcel source) {
            super(source);
            this.mVolumeStore.volume = source.readInt();
            this.mVolumeStore.originalVolume = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mVolumeStore.volume);
            dest.writeInt(this.mVolumeStore.originalVolume);
        }

        VolumeStore getVolumeStore() {
            return this.mVolumeStore;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public static class VolumeStore {
        public int originalVolume = -1;
        public int volume = -1;
    }

    public VolumePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VolumePreference, defStyleAttr, defStyleRes);
        this.mStreamType = a.getInt(0, 0);
        a.recycle();
    }

    public VolumePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public VolumePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 17891504);
    }

    public VolumePreference(Context context) {
        this(context, null);
    }

    public void setStreamType(int streamType) {
        this.mStreamType = streamType;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        SeekBar seekBar = (SeekBar) view.findViewById(16909252);
        this.mSeekBarVolumizer = new SeekBarVolumizer(getContext(), this.mStreamType, null, this);
        this.mSeekBarVolumizer.start();
        this.mSeekBarVolumizer.setSeekBar(seekBar);
        getPreferenceManager().registerOnActivityStopListener(this);
        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (this.mSeekBarVolumizer == null) {
            return true;
        }
        boolean isdown = event.getAction() == 0;
        switch (keyCode) {
            case 24:
                if (isdown) {
                    this.mSeekBarVolumizer.changeVolumeBy(1);
                }
                return true;
            case 25:
                if (isdown) {
                    this.mSeekBarVolumizer.changeVolumeBy(-1);
                }
                return true;
            case 164:
                if (isdown) {
                    this.mSeekBarVolumizer.muteVolume();
                }
                return true;
            default:
                return false;
        }
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!(positiveResult || this.mSeekBarVolumizer == null)) {
            this.mSeekBarVolumizer.revertVolume();
        }
        cleanup();
    }

    public void onActivityStop() {
        if (this.mSeekBarVolumizer != null) {
            this.mSeekBarVolumizer.stopSample();
        }
    }

    private void cleanup() {
        getPreferenceManager().unregisterOnActivityStopListener(this);
        if (this.mSeekBarVolumizer != null) {
            Dialog dialog = getDialog();
            if (dialog != null && dialog.isShowing()) {
                View view = dialog.getWindow().getDecorView().findViewById(16909252);
                if (view != null) {
                    view.setOnKeyListener(null);
                }
                this.mSeekBarVolumizer.revertVolume();
            }
            this.mSeekBarVolumizer.stop();
            this.mSeekBarVolumizer = null;
        }
    }

    public void onSampleStarting(SeekBarVolumizer volumizer) {
        if (this.mSeekBarVolumizer != null && volumizer != this.mSeekBarVolumizer) {
            this.mSeekBarVolumizer.stopSample();
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
    }

    public void onMuted(boolean muted, boolean zenMuted) {
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        if (this.mSeekBarVolumizer != null) {
            this.mSeekBarVolumizer.onSaveInstanceState(myState.getVolumeStore());
        }
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || (state.getClass().equals(SavedState.class) ^ 1) != 0) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (this.mSeekBarVolumizer != null) {
            this.mSeekBarVolumizer.onRestoreInstanceState(myState.getVolumeStore());
        }
    }
}
