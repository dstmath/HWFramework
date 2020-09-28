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
    private static final int LAYERDRAWABLE_PARAM_ARRAY_SIZE = 2;
    private static final int MAX_PROGRESS = 100;
    private DownloadClickListener mDownloadClickListener;
    private int mDownloadDoneDrawableId;
    private LayerDrawable mDownloadDrawable;
    private DownloadInnerClickListener mDownloadInnerClickListener;
    private int mDownloadPauseDrawableId;
    private int mDownloadReadyDrawableId;
    private int mDownloadStatus;
    private int mDownloadWaitDrawableId;
    private int mDownloadingDrawableId;
    private int mProgress;
    private HwProgressDrawable mProgressDrawable;

    public interface DownloadClickListener {
        void onClick();
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
            TypedArray typedArray = theme.obtainStyledAttributes(new int[]{ResLoader.getInstance().getIdentifier(context, "attr", "hwDownloadProgressColor")});
            this.mProgressDrawable.setColor(typedArray.getColor(0, 0));
            typedArray.recycle();
        }
        this.mDownloadReadyDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_download");
        this.mDownloadingDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_continue");
        this.mDownloadWaitDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_clock");
        this.mDownloadPauseDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_pause");
        this.mDownloadDoneDrawableId = ResLoaderUtil.getDrawableId(getContext(), "ic_home_ok");
        initDownloadStatus();
        this.mDownloadInnerClickListener = new DownloadInnerClickListener();
        setOnClickListener(this.mDownloadInnerClickListener);
    }

    public void setDownloadClickListener(DownloadClickListener downloadClickListener) {
        this.mDownloadClickListener = downloadClickListener;
    }

    private void initDownloadStatus() {
        this.mProgress = 0;
        this.mDownloadStatus = 0;
        setImageDrawable(getContext().getDrawable(this.mDownloadReadyDrawableId));
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v4, resolved type: android.graphics.drawable.Drawable[] */
    /* JADX DEBUG: Multi-variable search result rejected for r0v6, resolved type: android.graphics.drawable.Drawable[] */
    /* JADX WARN: Multi-variable type inference failed */
    public void setDownloadStatus(int downloadStatus, int progress) {
        this.mProgress = progress;
        this.mDownloadStatus = downloadStatus;
        int i = this.mDownloadStatus;
        if (i == 0) {
            setImageDrawable(getContext().getDrawable(this.mDownloadReadyDrawableId));
        } else if (i == 1) {
            setImageDrawable(getContext().getDrawable(this.mDownloadWaitDrawableId));
        } else if (i == 2) {
            this.mProgressDrawable.setProgress(progress);
            this.mDownloadDrawable = new LayerDrawable(new Drawable[]{getContext().getDrawable(this.mDownloadPauseDrawableId), this.mProgressDrawable});
            setImageDrawable(this.mDownloadDrawable);
        } else if (i == 3) {
            this.mProgressDrawable.setProgress(progress);
            this.mDownloadDrawable = new LayerDrawable(new Drawable[]{getContext().getDrawable(this.mDownloadingDrawableId), this.mProgressDrawable});
            setImageDrawable(this.mDownloadDrawable);
        } else if (i == 4) {
            setImageDrawable(getContext().getDrawable(this.mDownloadDoneDrawableId));
        }
    }

    public int getDownloadStatus() {
        return this.mDownloadStatus;
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
                    HwDownloadButton hwDownloadButton = HwDownloadButton.this;
                    hwDownloadButton.setDownloadStatus(2, hwDownloadButton.mProgress);
                } else if (HwDownloadButton.this.mDownloadStatus == 2) {
                    HwDownloadButton hwDownloadButton2 = HwDownloadButton.this;
                    hwDownloadButton2.setDownloadStatus(3, hwDownloadButton2.mProgress);
                }
            }
        }
    }
}
