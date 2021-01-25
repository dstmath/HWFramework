package android.widget.sr;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public final class SRInfoImpl implements SRInfo {
    public SRInfoImpl() {
        clearInfoWithStatus(0);
    }

    public void clearInfoWithStatus(int status) {
    }

    public boolean shouldDoSRProcess() {
        return false;
    }

    public void increaseInvalidateDrawableCount() {
    }

    public void increaseTryCount() {
    }

    public void setIsInWhiteList(boolean isInWhiteList) {
    }

    public boolean getIsInWhiteList() {
        return false;
    }

    public void setIsFullScreen(boolean isFullScreen) {
    }

    public boolean getIsFullScreen() {
        return false;
    }

    public void setMatchResolution(boolean isMatchResolution) {
    }

    public boolean getMatchResolution() {
        return false;
    }

    public void setStatus(int status) {
    }

    public int getStatus() {
        return -1;
    }

    public void setInvalidateDrawableCount(int invalidateDrawableCount) {
    }

    public int getInvalidateDrawableCount() {
        return -1;
    }

    public void setFirstTryTime(long firstTryTime) {
    }

    public long getFirstTryTime() {
        return -1;
    }

    public void setTryCount(int tryCount) {
    }

    public int getTryCount() {
        return -1;
    }

    public void setTaskInfo(SRTaskInfo taskInfo) {
    }

    public SRTaskInfo getTaskInfo() {
        return null;
    }

    public void setSrcDrawable(Drawable srcDrawable) {
    }

    public Drawable getSrcDrawable() {
        return null;
    }

    public void setSRDrawable(BitmapDrawable SRDrawable) {
    }

    public BitmapDrawable getSRDrawable() {
        return null;
    }

    public void setScaleX(float scaleX) {
    }

    public float getScaleX() {
        return -1.0f;
    }

    public void setScaleY(float scaleY) {
    }

    public float getScaleY() {
        return -1.0f;
    }
}
