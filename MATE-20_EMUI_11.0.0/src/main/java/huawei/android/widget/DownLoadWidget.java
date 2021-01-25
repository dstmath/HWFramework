package huawei.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.Locale;

public class DownLoadWidget extends FrameLayout {
    public static final int STATE_DOWNLOAD = 1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PAUSE = 2;
    private static final String TAG = "DownLoadWidget";
    private ProgressBar mDownLoadProgress;
    private int mDownloadDrawableId;
    private Drawable mDownloadStyleBg;
    private int mDownloadStyleTextColor;
    private int mDownloadTextColorId;
    private String mPauseText;
    private TextView mPercentage;
    private int mState;

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
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(ResLoaderUtil.getLayoutId(getContext(), "download_progress"), (ViewGroup) this, true);
        this.mDownLoadProgress = (ProgressBar) findViewById(ResLoaderUtil.getViewId(getContext(), "downloadProgress"));
        this.mPercentage = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "percentage"));
        this.mDownloadDrawableId = ResLoaderUtil.getDrawableId(getContext(), "download_widget_progress_layer");
        this.mDownloadTextColorId = ResLoader.getInstance().getIdentifier(getContext(), ResLoaderUtil.COLOR, "download_text_color_emui");
        this.mPauseText = "";
    }

    public void setPatternStyle() {
        this.mDownloadStyleBg = getResources().getDrawable(ResLoaderUtil.getDrawableId(getContext(), "hwpattern_btn_small"));
        this.mDownloadStyleTextColor = ResLoaderUtil.getColor(getContext(), "emui_color_gray_10");
        this.mDownLoadProgress.setProgressDrawable(this.mDownloadStyleBg);
        this.mPercentage.setTextColor(this.mDownloadStyleTextColor);
    }

    public void resetUpdate() {
        this.mDownLoadProgress.setBackgroundDrawable(null);
        this.mDownLoadProgress.setProgressDrawable(this.mDownloadStyleBg);
        this.mPercentage.setTextColor(this.mDownloadStyleTextColor);
        this.mState = 0;
    }

    public void setProgress(int progress) {
        this.mDownLoadProgress.setProgress(progress);
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
        ProgressBar progressBar = this.mDownLoadProgress;
        if (progressBar == null) {
            Log.w(TAG, "incrementProgressBy, mDownLoadProgress is null");
            return;
        }
        if (this.mState != 1) {
            this.mState = 1;
            progressBar.setBackground(null);
            this.mDownLoadProgress.setProgressDrawable(getResources().getDrawable(this.mDownloadDrawableId));
            this.mPercentage.setTextColor(getResources().getColorStateList(this.mDownloadTextColorId, getContext().getTheme()));
        }
        this.mDownLoadProgress.incrementProgressBy(progress);
        setPercentage(this.mDownLoadProgress.getProgress());
    }

    private void setPercentage(int percent) {
        TextView textView = this.mPercentage;
        if (textView == null) {
            Log.w(TAG, "setPercentage, mPercentage is null");
        } else if (this.mState == 2) {
            textView.setText(this.mPauseText);
        } else {
            textView.setText(String.format(Locale.getDefault(), "%2d", Integer.valueOf(percent)) + "%");
        }
    }

    public void stop() {
        this.mState = 2;
        setPercentage(this.mDownLoadProgress.getProgress());
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class huawei.android.widget.DownLoadWidget.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int mProgress;
        int mSaveState;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            this.mProgress = parcel.readInt();
            this.mSaveState = parcel.readInt();
        }

        @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(Parcel parcel, int flags) {
            if (parcel == null) {
                Log.w(DownLoadWidget.TAG, "writeToParcel, parcel is null");
                return;
            }
            super.writeToParcel(parcel, flags);
            parcel.writeInt(this.mProgress);
            parcel.writeInt(this.mSaveState);
        }
    }

    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mProgress = this.mDownLoadProgress.getProgress();
        savedState.mSaveState = this.mState;
        return savedState;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        ProgressBar progressBar;
        if (!(state instanceof SavedState)) {
            Log.w(TAG, "onRestoreInstanceState, state = " + state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.mState = savedState.mSaveState;
        if (this.mState == 0 || (progressBar = this.mDownLoadProgress) == null) {
            Log.w(TAG, "onRestoreInstanceState mState = " + this.mState + " , mDownLoadProgress = " + this.mDownLoadProgress);
            return;
        }
        progressBar.setProgressDrawable(getResources().getDrawable(33751740));
        this.mDownLoadProgress.setProgress(savedState.mProgress);
        setPercentage(savedState.mProgress);
    }
}
