package android.widget.sr;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public interface SRInfo {
    public static final long MAX_ELPASED_TIME = 300;
    public static final int MAX_TRY_COUNT = 2;
    public static final int SR_STATUS_DOING = 1;
    public static final int SR_STATUS_DONE = 2;
    public static final int SR_STATUS_NOT_STARTED = 0;
    public static final int SR_STATUS_NOT_TO_DO = 3;

    void clearInfoWithStatus(int i);

    long getFirstTryTime();

    int getInvalidateDrawableCount();

    boolean getIsFullScreen();

    boolean getIsInWhiteList();

    boolean getMatchResolution();

    BitmapDrawable getSRDrawable();

    float getScaleX();

    float getScaleY();

    Drawable getSrcDrawable();

    int getStatus();

    SRTaskInfo getTaskInfo();

    int getTryCount();

    void increaseInvalidateDrawableCount();

    void increaseTryCount();

    void setFirstTryTime(long j);

    void setInvalidateDrawableCount(int i);

    void setIsFullScreen(boolean z);

    void setIsInWhiteList(boolean z);

    void setMatchResolution(boolean z);

    void setSRDrawable(BitmapDrawable bitmapDrawable);

    void setScaleX(float f);

    void setScaleY(float f);

    void setSrcDrawable(Drawable drawable);

    void setStatus(int i);

    void setTaskInfo(SRTaskInfo sRTaskInfo);

    void setTryCount(int i);

    boolean shouldDoSRProcess();
}
