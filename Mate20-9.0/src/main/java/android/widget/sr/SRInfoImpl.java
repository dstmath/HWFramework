package android.widget.sr;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public final class SRInfoImpl implements SRInfo {
    private static final String SR_TAG = "SuperResolution";
    private long mFirstTryTime;
    private int mInvalidateDrawableCount;
    private boolean mIsFullScreen = false;
    private boolean mIsInWhiteList = false;
    private boolean mMatchResolution;
    private BitmapDrawable mSRDrawable;
    private float mScaleX;
    private float mScaleY;
    private Drawable mSrcDrawable;
    private int mStatus;
    private SRTaskInfo mTaskInfo;
    private int mTryCount;

    public SRInfoImpl() {
        clearInfoWithStatus(0);
    }

    public void clearInfoWithStatus(int status) {
        this.mTaskInfo = null;
        this.mStatus = status;
        this.mInvalidateDrawableCount = 0;
        this.mFirstTryTime = 0;
        this.mTryCount = 0;
        this.mMatchResolution = false;
        this.mSrcDrawable = null;
        this.mSRDrawable = null;
        this.mScaleX = 1.0f;
        this.mScaleY = 1.0f;
    }

    public boolean shouldDoSRProcess() {
        return this.mIsInWhiteList && this.mMatchResolution && this.mIsFullScreen && this.mStatus == 0;
    }

    public void increaseInvalidateDrawableCount() {
        this.mInvalidateDrawableCount++;
    }

    public void increaseTryCount() {
        this.mTryCount++;
    }

    public void setIsInWhiteList(boolean isInWhiteList) {
        this.mIsInWhiteList = isInWhiteList;
    }

    public boolean getIsInWhiteList() {
        return this.mIsInWhiteList;
    }

    public void setIsFullScreen(boolean isFullScreen) {
        this.mIsFullScreen = isFullScreen;
    }

    public boolean getIsFullScreen() {
        return this.mIsFullScreen;
    }

    public void setMatchResolution(boolean matchResolution) {
        this.mMatchResolution = matchResolution;
    }

    public boolean getMatchResolution() {
        return this.mMatchResolution;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public void setInvalidateDrawableCount(int invalidateDrawableCount) {
        this.mInvalidateDrawableCount = invalidateDrawableCount;
    }

    public int getInvalidateDrawableCount() {
        return this.mInvalidateDrawableCount;
    }

    public void setFirstTryTime(long firstTryTime) {
        this.mFirstTryTime = firstTryTime;
    }

    public long getFirstTryTime() {
        return this.mFirstTryTime;
    }

    public void setTryCount(int tryCount) {
        this.mTryCount = tryCount;
    }

    public int getTryCount() {
        return this.mTryCount;
    }

    public void setTaskInfo(SRTaskInfo taskInfo) {
        this.mTaskInfo = taskInfo;
    }

    public SRTaskInfo getTaskInfo() {
        return this.mTaskInfo;
    }

    public void setSrcDrawable(Drawable srcDrawable) {
        this.mSrcDrawable = srcDrawable;
    }

    public Drawable getSrcDrawable() {
        return this.mSrcDrawable;
    }

    public void setSRDrawable(BitmapDrawable SRDrawable) {
        this.mSRDrawable = SRDrawable;
    }

    public BitmapDrawable getSRDrawable() {
        return this.mSRDrawable;
    }

    public void setScaleX(float scaleX) {
        this.mScaleX = scaleX;
    }

    public float getScaleX() {
        return this.mScaleX;
    }

    public void setScaleY(float scaleY) {
        this.mScaleY = scaleY;
    }

    public float getScaleY() {
        return this.mScaleY;
    }
}
