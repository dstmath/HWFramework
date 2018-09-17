package com.huawei.android.hwaps;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;

public class ApsLtrGameIdentify {
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_UP = 1;
    public static final int GROUP_COUNT = 11;
    private static final String TAG = "ApsLtrGameIdentify";
    private static ApsLtrGameIdentify sInstance = null;
    private int mDownCount;
    private int mIsCardGameCount;
    private boolean mIsCardGameLaetTime;
    private int mIsNotCardGameCount;
    private long mLastGetResultTime;
    private ArrayList<PointCoordinate> mPointInFirstAndSecondRegion;
    private ArrayList<PointCoordinate> mPointInFourthRegion;
    private ArrayList<PointCoordinate> mPointInThirdRegion;
    private int mScreenHeightRegionForCard;
    private int mScreenWidth;

    private static class PointCoordinate {
        private int mPointX = 0;
        private int mPointY = 0;

        public PointCoordinate(int x, int y) {
            this.mPointX = x;
            this.mPointY = y;
        }

        public void setPointX(int x) {
            this.mPointX = x;
        }

        public void setPointY(int y) {
            this.mPointY = y;
        }

        public int getPointX() {
            return this.mPointX;
        }

        public int getPointY() {
            return this.mPointY;
        }
    }

    public static synchronized ApsLtrGameIdentify getInstance() {
        ApsLtrGameIdentify apsLtrGameIdentify;
        synchronized (ApsLtrGameIdentify.class) {
            if (sInstance == null) {
                sInstance = new ApsLtrGameIdentify();
            }
            apsLtrGameIdentify = sInstance;
        }
        return apsLtrGameIdentify;
    }

    public static boolean isSupportApsLtrGameIdentify() {
        if (128 == (SystemProperties.getInt("sys.aps.support", 0) & 128)) {
            return true;
        }
        Log.w(TAG, "APS: Ltr game identify is not supported");
        return false;
    }

    private ApsLtrGameIdentify() {
        this.mScreenWidth = 0;
        this.mScreenHeightRegionForCard = 0;
        this.mDownCount = 0;
        this.mIsCardGameCount = 0;
        this.mIsNotCardGameCount = 0;
        this.mLastGetResultTime = 0;
        this.mIsCardGameLaetTime = false;
        this.mPointInFirstAndSecondRegion = null;
        this.mPointInThirdRegion = null;
        this.mPointInFourthRegion = null;
        this.mScreenWidth = 0;
        this.mScreenHeightRegionForCard = 0;
        this.mDownCount = 0;
        this.mPointInFirstAndSecondRegion = new ArrayList();
        this.mPointInThirdRegion = new ArrayList();
        this.mPointInFourthRegion = new ArrayList();
    }

    public void resetApsLtrGameIdentify() {
        resetPointCoordinate();
    }

    public void setParaForLtrGameIdentify(int screenWidth, int screenHeight) {
        this.mScreenHeightRegionForCard = screenHeight / 4;
        this.mScreenWidth = screenWidth;
    }

    public boolean isLtrGame(String mStrPkgName) {
        boolean isLtrGame = (isLordGameByPakName(mStrPkgName) && isLordGame()) ? true : !isLordGameByPakName(mStrPkgName) ? isCardCame() : false;
        ApsCommon.logD(TAG, "APS:this is LtrGame judge by diff frame: " + isLtrGame);
        return isLtrGame;
    }

    public boolean isLordGameByPakName(String mStrPkgName) {
        if (mStrPkgName.toLowerCase().contains("lord") || mStrPkgName.toLowerCase().contains("ddz")) {
            return true;
        }
        return mStrPkgName.toLowerCase().contains("doudizhu");
    }

    private boolean isLordGame() {
        if (this.mDownCount < 20) {
            return this.mIsCardGameLaetTime;
        }
        boolean isCardGameFinally = false;
        int[] threeRegion = computeRegionMode(this.mPointInThirdRegion);
        int[] fourRegion = computeRegionMode(this.mPointInFourthRegion);
        for (int i = 0; i < 11; i++) {
            ApsCommon.logD(TAG, "APS:threeRegion[i] = " + threeRegion[i] + "fourRegion[i] = " + fourRegion[i]);
        }
        if (isSatisfiedThreeRegion(threeRegion, true) && isSatisfiedFourRegion(fourRegion)) {
            isCardGameFinally = true;
        }
        resetPointCoordinate();
        this.mIsCardGameLaetTime = isCardGameFinally;
        return isCardGameFinally;
    }

    private boolean isCardCame() {
        long currtneTime = SystemClock.uptimeMillis();
        boolean isCardGameFinally = false;
        if (this.mDownCount < 20) {
            this.mLastGetResultTime = currtneTime;
            return this.mIsCardGameLaetTime;
        } else if (checkRegionSize() && (checkIsTouchCountSatisfied(currtneTime) ^ 1) == 0) {
            int i;
            int[] threeRegion = computeRegionMode(this.mPointInThirdRegion);
            int[] fourRegion = computeRegionMode(this.mPointInFourthRegion);
            for (i = 0; i < 11; i++) {
                ApsCommon.logD(TAG, "APS:threeRegion[i] = " + threeRegion[i] + "fourRegion[i] = " + fourRegion[i]);
            }
            if (isSatisfiedThreeRegion(threeRegion, false) && isSatisfiedFourRegion(fourRegion)) {
                isCardGameFinally = true;
                i = 10;
                while (i >= 0) {
                    if (((double) threeRegion[i]) > ((double) this.mPointInThirdRegion.size()) * 0.25d && ((double) fourRegion[i]) > ((double) this.mPointInFourthRegion.size()) * 0.25d && i >= 8) {
                        isCardGameFinally = false;
                        break;
                    }
                    i--;
                }
            }
            resetPointCoordinate();
            isCardGameFinally = checkResultOfIdentify(isCardGameFinally);
            this.mIsCardGameLaetTime = isCardGameFinally;
            this.mLastGetResultTime = currtneTime;
            return isCardGameFinally;
        } else {
            resetPointCoordinate();
            this.mLastGetResultTime = currtneTime;
            this.mIsNotCardGameCount++;
            return false;
        }
    }

    private boolean checkIsTouchCountSatisfied(long currtneTime) {
        boolean z = false;
        if (0 == currtneTime || 0 == this.mLastGetResultTime) {
            return false;
        }
        if (this.mDownCount < ((int) ((((double) (currtneTime - this.mLastGetResultTime)) / 1000.0d) / 1.5d))) {
            z = true;
        }
        return z;
    }

    private boolean checkResultOfIdentify(boolean result) {
        if (result) {
            this.mIsCardGameCount++;
        } else {
            this.mIsNotCardGameCount++;
        }
        if (this.mIsCardGameCount * 2 > this.mIsNotCardGameCount) {
            return true;
        }
        return false;
    }

    private boolean checkRegionSize() {
        int firstRegionSize = this.mPointInFirstAndSecondRegion.size();
        int thirdRegionSize = this.mPointInThirdRegion.size();
        int fourthRegionSize = this.mPointInFourthRegion.size();
        int allPointCount = (firstRegionSize + thirdRegionSize) + fourthRegionSize;
        ApsCommon.logD(TAG, "APS:checkRegionSize--firstRegionSize = " + firstRegionSize + "; thirdRegionSize = " + thirdRegionSize + "; fourthRegionSize = " + fourthRegionSize);
        if (thirdRegionSize == 0 || fourthRegionSize == 0 || ((double) firstRegionSize) >= ((double) allPointCount) * 0.2d || ((double) fourthRegionSize) * 1.2d < ((double) thirdRegionSize)) {
            return false;
        }
        return true;
    }

    private boolean isSatisfiedThreeRegion(int[] threeRegin, boolean isLordPkgName) {
        int mLargeRegion = 0;
        int mNotZeroRegion = 0;
        int size = this.mPointInThirdRegion.size();
        for (int i = 0; i < 11; i++) {
            if (((double) threeRegin[i]) > ((double) size) * 0.25d) {
                mLargeRegion++;
            }
            if (threeRegin[i] != 0) {
                mNotZeroRegion++;
            }
        }
        return (mLargeRegion >= 1 && mLargeRegion <= 3 && mNotZeroRegion <= 6 && isLordPkgName) || (mLargeRegion >= 1 && mLargeRegion <= 3 && mNotZeroRegion <= 4 && (isLordPkgName ^ 1) != 0);
    }

    private boolean isSatisfiedFourRegion(int[] fourRegin) {
        int mNotZeroRegion = 0;
        for (int i = 0; i < 11; i++) {
            if (fourRegin[i] != 0) {
                mNotZeroRegion++;
            }
        }
        if (mNotZeroRegion >= 4) {
            return true;
        }
        return false;
    }

    public void collectInputEvent(int action, int x, int y) {
        if (action == 0) {
            this.mDownCount++;
        }
        PointCoordinate pointCoordinate = new PointCoordinate(x, y);
        if (y < this.mScreenHeightRegionForCard * 2) {
            this.mPointInFirstAndSecondRegion.add(pointCoordinate);
        } else if (y < this.mScreenHeightRegionForCard * 2 || y > this.mScreenHeightRegionForCard * 3) {
            this.mPointInFourthRegion.add(pointCoordinate);
        } else {
            this.mPointInThirdRegion.add(pointCoordinate);
        }
    }

    private void resetPointCoordinate() {
        this.mPointInFirstAndSecondRegion.clear();
        this.mPointInThirdRegion.clear();
        this.mPointInFourthRegion.clear();
        this.mDownCount = 0;
    }

    private int[] computeRegionMode(ArrayList<PointCoordinate> mPointInRegion) {
        int notZeroRegionCountOfFirstMethord = 0;
        int notZeroRegionCountOfSecondMethord = 0;
        int maxCountOfFirst = 0;
        int maxCountOfSecond = 0;
        int[] pointCountOfDefault = new int[11];
        int[] pointCountOfFirstMethord = new int[11];
        int[] pointCountOfSecondMethord = new int[11];
        if (mPointInRegion.size() == 0 || this.mScreenWidth <= 0) {
            return pointCountOfDefault;
        }
        int i;
        for (i = 0; i < mPointInRegion.size(); i++) {
            int indexOfFirstMethord = (((PointCoordinate) mPointInRegion.get(i)).getPointX() * 10) / this.mScreenWidth;
            int indexOfSecondMethord = ((((PointCoordinate) mPointInRegion.get(i)).getPointX() + (this.mScreenWidth / 20)) * 10) / this.mScreenWidth;
            if (indexOfFirstMethord >= 0 && indexOfFirstMethord <= 10 && indexOfSecondMethord >= 0 && indexOfSecondMethord <= 10) {
                pointCountOfFirstMethord[indexOfFirstMethord] = pointCountOfFirstMethord[indexOfFirstMethord] + 1;
                pointCountOfSecondMethord[indexOfSecondMethord] = pointCountOfSecondMethord[indexOfSecondMethord] + 1;
            }
        }
        for (i = 0; i < 11; i++) {
            if (pointCountOfFirstMethord[i] != 0) {
                notZeroRegionCountOfFirstMethord++;
            }
            if (maxCountOfFirst < pointCountOfFirstMethord[i]) {
                maxCountOfFirst = pointCountOfFirstMethord[i];
            }
            if (pointCountOfSecondMethord[i] != 0) {
                notZeroRegionCountOfSecondMethord++;
            }
            if (maxCountOfSecond < pointCountOfSecondMethord[i]) {
                maxCountOfSecond = pointCountOfSecondMethord[i];
            }
        }
        if ((notZeroRegionCountOfFirstMethord == notZeroRegionCountOfSecondMethord && maxCountOfFirst >= maxCountOfSecond) || notZeroRegionCountOfFirstMethord < notZeroRegionCountOfSecondMethord) {
            return pointCountOfFirstMethord;
        }
        if ((notZeroRegionCountOfFirstMethord != notZeroRegionCountOfSecondMethord || maxCountOfFirst >= maxCountOfSecond) && notZeroRegionCountOfFirstMethord <= notZeroRegionCountOfSecondMethord) {
            return pointCountOfDefault;
        }
        return pointCountOfSecondMethord;
    }
}
