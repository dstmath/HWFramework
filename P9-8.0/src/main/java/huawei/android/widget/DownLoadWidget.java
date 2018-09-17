package huawei.android.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.BaseSavedState;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownLoadWidget extends FrameLayout {
    private static final int STATE_DOWNLOAD = 1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PAUSE = 2;
    private static final String TAG = "DownLoadWidget";
    private ProgressBar mDownLoadProgress;
    private String mPauseText;
    private TextView mPercentage;
    private int mState;

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int mProgress;
        int mSaveState;

        /* synthetic */ SavedState(Parcel parcel, SavedState -this1) {
            this(parcel);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            if (parcel == null) {
                Log.w(DownLoadWidget.TAG, "SavedState, parcel is null");
                return;
            }
            this.mProgress = parcel.readInt();
            this.mSaveState = parcel.readInt();
        }

        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            if (parcel == null) {
                Log.w(DownLoadWidget.TAG, "writeToParcel, parcel is null");
                return;
            }
            parcel.writeInt(this.mProgress);
            parcel.writeInt(this.mSaveState);
        }
    }

    public DownLoadWidget(Context context) {
        super(context);
        this.mDownLoadProgress = null;
        this.mPercentage = null;
        this.mState = 0;
        init();
    }

    public DownLoadWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownLoadWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDownLoadProgress = null;
        this.mPercentage = null;
        this.mState = 0;
        init();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(34013214, this, true);
        this.mDownLoadProgress = (ProgressBar) findViewById(34603103);
        this.mPercentage = (TextView) findViewById(34603104);
        this.mPauseText = "";
    }

    public void setIdleText(String idleText) {
        if (idleText == null) {
            Log.w(TAG, "setIdleText, idleText is null");
        } else if (this.mState != 0) {
            Log.w(TAG, "setIdleText, mState = " + this.mState);
        } else {
            this.mPercentage.setText(idleText);
        }
    }

    public void setPauseText(String pauseText) {
        if (pauseText == null) {
            Log.w(TAG, "setPauseText, pauseText is null");
        } else {
            this.mPauseText = pauseText;
        }
    }

    public int getState() {
        return this.mState;
    }

    public void incrementProgressBy(int progress) {
        if (this.mDownLoadProgress == null) {
            Log.w(TAG, "incrementProgressBy, mDownLoadProgress is null");
            return;
        }
        if (1 != this.mState) {
            this.mState = 1;
            this.mDownLoadProgress.setBackground(null);
            this.mDownLoadProgress.setProgressDrawable(getResources().getDrawable(33751740));
        }
        this.mDownLoadProgress.incrementProgressBy(progress);
        setPercentage(this.mDownLoadProgress.getProgress());
    }

    private void setPercentage(int percent) {
        if (this.mPercentage == null) {
            Log.w(TAG, "setPercentage, mPercentage is null");
            return;
        }
        if (2 == this.mState) {
            this.mPercentage.setText(this.mPauseText);
        } else {
            this.mPercentage.setText(String.format("%2d", new Object[]{Integer.valueOf(percent)}) + "%");
        }
    }

    public void stop() {
        this.mState = 2;
        setPercentage(this.mDownLoadProgress.getProgress());
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.mProgress = this.mDownLoadProgress.getProgress();
        ss.mSaveState = this.mState;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            this.mState = ss.mSaveState;
            if (this.mState == 0 || this.mDownLoadProgress == null) {
                Log.w(TAG, "onRestoreInstanceState mState = " + this.mState + " , mDownLoadProgress = " + this.mDownLoadProgress);
                return;
            }
            this.mDownLoadProgress.setProgressDrawable(getResources().getDrawable(33751740));
            this.mDownLoadProgress.setProgress(ss.mProgress);
            setPercentage(ss.mProgress);
            return;
        }
        Log.w(TAG, "onRestoreInstanceState, state = " + state);
    }
}
