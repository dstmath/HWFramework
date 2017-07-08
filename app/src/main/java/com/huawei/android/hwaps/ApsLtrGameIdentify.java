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
    private static ApsLtrGameIdentify sInstance;
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
        private int mPointX;
        private int mPointY;

        public PointCoordinate(int x, int y) {
            this.mPointX = ApsLtrGameIdentify.ACTION_DOWN;
            this.mPointY = ApsLtrGameIdentify.ACTION_DOWN;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.ApsLtrGameIdentify.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.ApsLtrGameIdentify.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.ApsLtrGameIdentify.<clinit>():void");
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
        if (128 == (SystemProperties.getInt("sys.aps.support", ACTION_DOWN) & 128)) {
            return true;
        }
        Log.w(TAG, "APS: Ltr game identify is not supported");
        return false;
    }

    private ApsLtrGameIdentify() {
        this.mScreenWidth = ACTION_DOWN;
        this.mScreenHeightRegionForCard = ACTION_DOWN;
        this.mDownCount = ACTION_DOWN;
        this.mIsCardGameCount = ACTION_DOWN;
        this.mIsNotCardGameCount = ACTION_DOWN;
        this.mLastGetResultTime = 0;
        this.mIsCardGameLaetTime = false;
        this.mPointInFirstAndSecondRegion = null;
        this.mPointInThirdRegion = null;
        this.mPointInFourthRegion = null;
        this.mScreenWidth = ACTION_DOWN;
        this.mScreenHeightRegionForCard = ACTION_DOWN;
        this.mDownCount = ACTION_DOWN;
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
        boolean isCardCame = (isLordGameByPakName(mStrPkgName) && isLordGame()) ? true : !isLordGameByPakName(mStrPkgName) ? isCardCame() : false;
        ApsCommon.logD(TAG, "APS:this is LtrGame judge by diff frame: " + isCardCame);
        return isCardCame;
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
        for (int i = ACTION_DOWN; i < GROUP_COUNT; i += ACTION_UP) {
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
        } else if (checkRegionSize() && checkIsTouchCountSatisfied(currtneTime)) {
            int i;
            int[] threeRegion = computeRegionMode(this.mPointInThirdRegion);
            int[] fourRegion = computeRegionMode(this.mPointInFourthRegion);
            for (i = ACTION_DOWN; i < GROUP_COUNT; i += ACTION_UP) {
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
            this.mIsNotCardGameCount += ACTION_UP;
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
            this.mIsCardGameCount += ACTION_UP;
        } else {
            this.mIsNotCardGameCount += ACTION_UP;
        }
        if (this.mIsCardGameCount * ACTION_MOVE > this.mIsNotCardGameCount) {
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
        int mLargeRegion = ACTION_DOWN;
        int mNotZeroRegion = ACTION_DOWN;
        int size = this.mPointInThirdRegion.size();
        for (int i = ACTION_DOWN; i < GROUP_COUNT; i += ACTION_UP) {
            if (((double) threeRegin[i]) > ((double) size) * 0.25d) {
                mLargeRegion += ACTION_UP;
            }
            if (threeRegin[i] != 0) {
                mNotZeroRegion += ACTION_UP;
            }
        }
        return (mLargeRegion >= ACTION_UP && mLargeRegion <= 3 && mNotZeroRegion <= 6 && isLordPkgName) || (mLargeRegion >= ACTION_UP && mLargeRegion <= 3 && mNotZeroRegion <= 4 && !isLordPkgName);
    }

    private boolean isSatisfiedFourRegion(int[] fourRegin) {
        int mNotZeroRegion = ACTION_DOWN;
        for (int i = ACTION_DOWN; i < GROUP_COUNT; i += ACTION_UP) {
            if (fourRegin[i] != 0) {
                mNotZeroRegion += ACTION_UP;
            }
        }
        if (mNotZeroRegion >= 4) {
            return true;
        }
        return false;
    }

    public void collectInputEvent(int action, int x, int y) {
        if (action == 0) {
            this.mDownCount += ACTION_UP;
        }
        PointCoordinate pointCoordinate = new PointCoordinate(x, y);
        if (y < this.mScreenHeightRegionForCard * ACTION_MOVE) {
            this.mPointInFirstAndSecondRegion.add(pointCoordinate);
        } else if (y < this.mScreenHeightRegionForCard * ACTION_MOVE || y > this.mScreenHeightRegionForCard * 3) {
            this.mPointInFourthRegion.add(pointCoordinate);
        } else {
            this.mPointInThirdRegion.add(pointCoordinate);
        }
    }

    private void resetPointCoordinate() {
        this.mPointInFirstAndSecondRegion.clear();
        this.mPointInThirdRegion.clear();
        this.mPointInFourthRegion.clear();
        this.mDownCount = ACTION_DOWN;
    }

    private int[] computeRegionMode(ArrayList<PointCoordinate> mPointInRegion) {
        int notZeroRegionCountOfFirstMethord = ACTION_DOWN;
        int notZeroRegionCountOfSecondMethord = ACTION_DOWN;
        int maxCountOfFirst = ACTION_DOWN;
        int maxCountOfSecond = ACTION_DOWN;
        int[] pointCountOfDefault = new int[GROUP_COUNT];
        int[] pointCountOfFirstMethord = new int[GROUP_COUNT];
        int[] pointCountOfSecondMethord = new int[GROUP_COUNT];
        if (mPointInRegion.size() == 0 || this.mScreenWidth <= 0) {
            return pointCountOfDefault;
        }
        int i;
        for (i = ACTION_DOWN; i < mPointInRegion.size(); i += ACTION_UP) {
            int indexOfFirstMethord = (((PointCoordinate) mPointInRegion.get(i)).getPointX() * 10) / this.mScreenWidth;
            int indexOfSecondMethord = ((((PointCoordinate) mPointInRegion.get(i)).getPointX() + (this.mScreenWidth / 20)) * 10) / this.mScreenWidth;
            if (indexOfFirstMethord >= 0 && indexOfFirstMethord <= 10 && indexOfSecondMethord >= 0 && indexOfSecondMethord <= 10) {
                pointCountOfFirstMethord[indexOfFirstMethord] = pointCountOfFirstMethord[indexOfFirstMethord] + ACTION_UP;
                pointCountOfSecondMethord[indexOfSecondMethord] = pointCountOfSecondMethord[indexOfSecondMethord] + ACTION_UP;
            }
        }
        for (i = ACTION_DOWN; i < GROUP_COUNT; i += ACTION_UP) {
            if (pointCountOfFirstMethord[i] != 0) {
                notZeroRegionCountOfFirstMethord += ACTION_UP;
            }
            if (maxCountOfFirst < pointCountOfFirstMethord[i]) {
                maxCountOfFirst = pointCountOfFirstMethord[i];
            }
            if (pointCountOfSecondMethord[i] != 0) {
                notZeroRegionCountOfSecondMethord += ACTION_UP;
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
