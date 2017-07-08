package android.preference;

import android.app.Dialog;
import android.bluetooth.BluetoothAssignedNumbers;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.Preference.BaseSavedState;
import android.preference.PreferenceManager.OnActivityStopListener;
import android.preference.SeekBarVolumizer.Callback;
import android.rms.HwSysResource;
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
        public static final Creator<SavedState> CREATOR = null;
        VolumeStore mVolumeStore;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.preference.VolumePreference.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.preference.VolumePreference.SavedState.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.preference.VolumePreference.SavedState.<clinit>():void");
        }

        public SavedState(Parcel source) {
            super(source);
            this.mVolumeStore = new VolumeStore();
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
            this.mVolumeStore = new VolumeStore();
        }
    }

    public static class VolumeStore {
        public int originalVolume;
        public int volume;

        public VolumeStore() {
            this.volume = -1;
            this.originalVolume = -1;
        }
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
        this(context, attrs, 18219041);
    }

    public VolumePreference(Context context) {
        this(context, null);
    }

    public void setStreamType(int streamType) {
        this.mStreamType = streamType;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        SeekBar seekBar = (SeekBar) view.findViewById(16909257);
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
            case HwSysResource.ANR /*24*/:
                if (isdown) {
                    this.mSeekBarVolumizer.changeVolumeBy(1);
                }
                return true;
            case HwSysResource.DELAY /*25*/:
                if (isdown) {
                    this.mSeekBarVolumizer.changeVolumeBy(-1);
                }
                return true;
            case BluetoothAssignedNumbers.LINAK /*164*/:
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
                View view = dialog.getWindow().getDecorView().findViewById(16909257);
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
        if (state == null || !state.getClass().equals(SavedState.class)) {
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
