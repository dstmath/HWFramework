package huawei.android.widget.pattern;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwDownloadButton extends ImageButton {
    public static final int DOWNLOADING = 2;
    public static final int DOWNLOAD_DONE = 4;
    public static final int DOWNLOAD_PAUSE = 3;
    public static final int DOWNLOAD_READY = 0;
    public static final int DOWNLOAD_WAIT = 1;
    private static final int MAX_PROGRESS = 100;
    /* access modifiers changed from: private */
    public DownloadClickListener mDownloadClickListener;
    private int mDownloadDoneDrawableId;
    private LayerDrawable mDownloadDrawable;
    private DownloadInnerClickListener mDownloadInnerClickListener;
    private int mDownloadPauseDrawableId;
    private int mDownloadReadyDrawableId;
    /* access modifiers changed from: private */
    public int mDownloadStatus;
    private int mDownloadWaitDrawableId;
    private int mDownloadingDrawableId;
    /* access modifiers changed from: private */
    public int mProgress;
    private HwProgressDrawable mProgressDrawable;

    public interface DownloadClickListener {
        void onClick();
    }

    private class DownloadInnerClickListener implements View.OnClickListener {
        private DownloadInnerClickListener() {
        }

        public void onClick(View v) {
            if (HwDownloadButton.this.mDownloadClickListener != null) {
                HwDownloadButton.this.mDownloadClickListener.onClick();
            }
            if (HwDownloadButton.this.mProgress > 0 && HwDownloadButton.this.mProgress < HwDownloadButton.MAX_PROGRESS) {
                if (HwDownloadButton.this.mDownloadStatus == 3) {
                    HwDownloadButton.this.setDownloadStatus(2, HwDownloadButton.this.mProgress);
                } else if (HwDownloadButton.this.mDownloadStatus == 2) {
                    HwDownloadButton.this.setDownloadStatus(3, HwDownloadButton.this.mProgress);
                }
            }
        }
    }

    public HwDownloadButton(Context context) {
        this(context, null);
    }

    public HwDownloadButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwDownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwDownloadButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDownloadStatus = 0;
        this.mProgressDrawable = new HwProgressDrawable(getContext().getResources(), null);
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(new int[]{ResLoader.getInstance().getIdentifier(context, "attr", "hwDownloadProgressColor")});
            this.mProgressDrawable.setColor(a.getColor(0, 0));
            a.recycle();
        }
        this.mDownloadReadyDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_download");
        this.mDownloadingDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_continue");
        this.mDownloadWaitDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_clock");
        this.mDownloadPauseDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_pause");
        this.mDownloadDoneDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_ok");
        setDownloadStatus(0, 0);
        this.mDownloadInnerClickListener = new DownloadInnerClickListener();
        setOnClickListener(this.mDownloadInnerClickListener);
    }

    public void setDownloadClickListener(DownloadClickListener downloadClickListener) {
        this.mDownloadClickListener = downloadClickListener;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: android.graphics.drawable.Drawable[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v10, resolved type: android.graphics.drawable.Drawable[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void setDownloadStatus(int downloadStatus, int progress) {
        this.mProgress = progress;
        this.mDownloadStatus = downloadStatus;
        if (this.mDownloadStatus == 0) {
            setImageDrawable(getContext().getDrawable(this.mDownloadReadyDrawableId));
        } else if (this.mDownloadStatus == 1) {
            setImageDrawable(getContext().getDrawable(this.mDownloadWaitDrawableId));
        } else if (this.mDownloadStatus == 2) {
            this.mProgressDrawable.setProgress(progress);
            this.mDownloadDrawable = new LayerDrawable(new Drawable[]{getContext().getDrawable(this.mDownloadPauseDrawableId), this.mProgressDrawable});
            setImageDrawable(this.mDownloadDrawable);
        } else if (this.mDownloadStatus == 3) {
            this.mProgressDrawable.setProgress(progress);
            this.mDownloadDrawable = new LayerDrawable(new Drawable[]{getContext().getDrawable(this.mDownloadingDrawableId), this.mProgressDrawable});
            setImageDrawable(this.mDownloadDrawable);
        } else if (this.mDownloadStatus == 4) {
            setImageDrawable(getContext().getDrawable(this.mDownloadDoneDrawableId));
        }
    }

    public int getDownloadStatus() {
        return this.mDownloadStatus;
    }
}
