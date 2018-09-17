package com.huawei.android.hwaps;

import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.hwaps.FpsRequest.SceneTypeE;
import java.util.Calendar;

public class ApsUserFeedback {
    private static final int ADJUST_MAX_FPS = 55;
    private static final int APS_ANALYZE_ENABLE = 2;
    private static final int APS_CONTROL_ENABLE = 1;
    private static final int APS_DO_NOTHING = 0;
    private static final int BIG_ORDER = 1;
    private static final int CUR_CTRL_DURATION = 1;
    private static final int DATA_NOT_ENOUGH = -1;
    private static final int DEFAULT_MAX_FPS = 60;
    private static final int DEFAULT_MIN_FPS = 30;
    private static final int DEFAULT_MIN_NONPLAY_FPS = 30;
    private static final int DEFAULT_MIN_PLAYING_FPS = 30;
    private static final int HALF_OF_DEFAULT_STORE_COUNT = 7;
    private static final int LAST_CTRL_BEGIN_DATE = 3;
    private static final int LAST_CTRL_DURATION = 2;
    private static final int LAST_STUDY_BEGIN_DATE = 4;
    private static final int LITTLE_ORDER = 0;
    private static final int MIN_BAD_COUNT_TO_ADJUST_FPS = 2;
    private static final int NOT_BAD_COUNT_TO_REDUCE_FPS = 10;
    private static final int QUALIFIED_ROUND_DURATION = 5;
    private static final int RAISE_FPS_ALL = 3;
    private static final int RAISE_FPS_ALL_SPECIAL = 2;
    private static final int REDUCE_FPS_SINGLE = 1;
    private static final int REDUSE_FPS_ALL = 0;
    private static final int RESULT_IS_BAD = 0;
    private static final int RESULT_IS_NOT_BAD = 1;
    private static final int ROUND_DURATION_CHECK_PEROID = 180;
    private static final int SLEEP_SECOND_BEFORE_START_THREAD = 3;
    private static final int STAND_FEED_BACK_COUNT = 5;
    private static final int SWITCH_CONTINUALLY_CHECK_PEROID = 60;
    private static final String TAG = "ApsUserFeedback";
    private static final int USER_FEEDBACK_CHECK_PEROID = 15;
    private static ApsUserFeedbackThread sApsUserFeedbackThread = null;
    private static ApsUserFeedback sInstance = null;
    public int FIRST_SWITCH_CONTINUALLY_TIMER_COUNT = (this.SWITCH_CONTINUALLY_TIMER_COUNT * 2);
    public int ROUNT_DURATION_TIMER_COUNT = 12;
    public int SWITCH_CONTINUALLY_TIMER_COUNT = 4;
    private int mBadCountJudgeByFps = 0;
    private int mCountOfGamesDuration = 0;
    private int mCountOfGamesInterval = 0;
    private int mCountofGameRoundsDuration = 0;
    private long mCtrlGameInterval = 0;
    private long mCtrlGamesDuration = 0;
    private long mCurrentBeginDate = 0;
    private long mCurrentBeginTime = 0;
    private long mCurrentPauseTime = 0;
    private long mCurrentResumeTime = 0;
    private FpsRequest mFpsRequest = null;
    private GameState mGameState = null;
    private long mGamesDuration = 0;
    private boolean mIsApsControlled = false;
    private boolean mIsFirstBadJudgedByFps = true;
    private boolean mIsFirstTimeBadJudgedByFps = true;
    private boolean mIsFirstTimeComputePlayDuration = true;
    private boolean mIsFirstTimeFeedBackCtrl = true;
    private boolean mIsFirstTimeToPerformUserFeedBack = true;
    private boolean mIsNeedUserFeedBack = true;
    private boolean mIsStoredCtrlBeginTime = false;
    private boolean mIsStoredStudyBeginTime = false;
    private long mLastCtrlBeginDate = 0;
    private long mLastStudyBeginDate = 0;
    private long mMaxStudyGameInterval = 0;
    private long mMaxStudyPlayDuration = 0;
    private long mMaxStudyRoundDuration = 0;
    private long mMinStudyGameInterval = 0;
    private long mMinStudyPlayDuration = 0;
    private long mMinStudyRoundDuration = 0;
    private int mNonplayMinFps = 30;
    private OperateExperienceLib mOperateExLib = null;
    private int mPlayingMinFps = 30;
    private int mRoundDurationTimerCount = 0;
    private int mStandFeedbackCount = 0;
    private int mSwitchContinuallyTimerCount = 0;

    public class ApsUserFeedbackThread implements Runnable {
        public boolean mRunOver = false;
        public boolean mStop = false;

        public void run() {
            ApsCommon.logI(ApsUserFeedback.TAG, "ApsUserFeedbackThread-run-start");
            this.mRunOver = false;
            sleep(3);
            if (ApsUserFeedback.this.mIsNeedUserFeedBack) {
                while (!this.mStop) {
                    ApsUserFeedback.getInstance().userFeedbackCtrl();
                    sleep(15);
                }
                this.mRunOver = true;
                ApsCommon.logI(ApsUserFeedback.TAG, "ApsUserFeedbackThread-run-end");
                return;
            }
            ApsCommon.logD(ApsUserFeedback.TAG, "APS: ApsUserFeedbackThread----return");
        }

        private void sleep(long second) {
            try {
                Thread.sleep(1000 * second);
            } catch (InterruptedException e) {
                Log.w("ApsUserFeedbackThread", "APS: sleep exception");
            }
        }
    }

    public static synchronized ApsUserFeedback getInstance() {
        ApsUserFeedback apsUserFeedback;
        synchronized (ApsUserFeedback.class) {
            if (sInstance == null) {
                sInstance = new ApsUserFeedback();
            }
            apsUserFeedback = sInstance;
        }
        return apsUserFeedback;
    }

    public static boolean isSupportApsUserFeedback() {
        int isApsSupport = SystemProperties.getInt("sys.aps.support", 0);
        if (16 == (isApsSupport & 16) && (isApsSupport & 3) != 0) {
            return true;
        }
        Log.w(TAG, "APS: User feedback is not supported");
        return false;
    }

    public ApsUserFeedback() {
        if (this.mFpsRequest == null) {
            this.mFpsRequest = new FpsRequest(SceneTypeE.USER_EXPERIENCE);
        }
        startApsUserFeedbackThread();
        ApsCommon.logI(TAG, "APS:User feedback module create success");
    }

    public void stop() {
        if (!sApsUserFeedbackThread.mStop) {
            this.mSwitchContinuallyTimerCount = 0;
            this.mRoundDurationTimerCount = 0;
            this.mCurrentPauseTime = System.currentTimeMillis() / 1000;
            computeAndSaveGamesDuration();
            ApsCommon.logI(TAG, "APS:stop user feedback check.");
            if (sApsUserFeedbackThread != null) {
                sApsUserFeedbackThread.mStop = true;
            }
        }
    }

    public void resume() {
        if (sApsUserFeedbackThread.mStop) {
            ApsCommon.logI(TAG, "APS:resume user feedback check.");
            if (sApsUserFeedbackThread == null) {
                Log.w(TAG, "APS: resume ApsUserFeedbackThread is null");
                return;
            }
            this.mCurrentResumeTime = System.currentTimeMillis() / 1000;
            sApsUserFeedbackThread.mStop = false;
            if (sApsUserFeedbackThread.mRunOver) {
                startApsUserFeedbackThread();
            }
        }
    }

    private void startApsUserFeedbackThread() {
        if (sApsUserFeedbackThread == null) {
            sApsUserFeedbackThread = new ApsUserFeedbackThread();
        }
        new Thread(sApsUserFeedbackThread, TAG).start();
        ApsCommon.logI(TAG, "APS:APS user feedback thread start");
    }

    public void initUserFeedback() {
        Calendar cal = Calendar.getInstance();
        this.mCurrentBeginDate = (long) (((cal.get(1) * 10000) + ((cal.get(2) + 1) * 100)) + cal.get(5));
        this.mCurrentBeginTime = System.currentTimeMillis() / 1000;
        ApsCommon.logI(TAG, "APS: initUserFeedback----mCurrentBeginDate = " + this.mCurrentBeginDate);
        this.mGameState = new GameState();
        this.mOperateExLib.setGameBeginDate(this.mCurrentBeginDate);
        getHistoryDataFromLib();
    }

    public void userFeedbackCtrl() {
        if (this.mOperateExLib != null) {
            if (this.mIsFirstTimeFeedBackCtrl) {
                initUserFeedback();
                this.mIsFirstTimeFeedBackCtrl = false;
            }
            int apsState = SystemProperties.getInt("debug.aps.enable", 0);
            if (apsState != 0) {
                if (1 == apsState) {
                    this.mIsApsControlled = true;
                }
                performApsUserFeedback(apsState);
            }
        }
    }

    private void performApsUserFeedback(int apsState) {
        int resultByFps = -1;
        int resultByRoundDuration = -1;
        int resultByGameDuration = -1;
        int resultByGamesInterval = -1;
        if (this.mIsFirstTimeToPerformUserFeedBack) {
            this.mIsFirstTimeToPerformUserFeedBack = false;
            this.mRoundDurationTimerCount = 0;
            computeAndSaveGamesInterval();
            resultByGameDuration = isBadJudgedByGamesDuration();
            resultByGamesInterval = isBadJudgedByGamesInterval();
        }
        if (this.mNonplayMinFps != 55) {
            resultByFps = isBadJudgedByFps();
            if (resultByFps == 0) {
                this.mBadCountJudgeByFps++;
            }
        }
        if (apsState == 2) {
            getAndSaveRoundDuration(true);
        } else if (apsState == 1) {
            resultByRoundDuration = isBadJudgedByRoundDuration(getAndSaveRoundDuration(false));
        }
        operateBasedOnResult(apsState, resultByFps, resultByRoundDuration, resultByGameDuration, resultByGamesInterval);
    }

    private void operateBasedOnResult(int apsState, int resultByFps, int resultByRoundDuration, int resultByGameDuration, int resultByGamesInterval) {
        if (resultByGameDuration == 0 || resultByGamesInterval == 0 || resultByRoundDuration == 0 || resultByFps == 0 || 1 == resultByGameDuration || 1 == resultByGamesInterval || 1 == resultByRoundDuration || 1 == resultByFps) {
            ApsCommon.logD(TAG, "APS: operateBasedOnResult----apsState = " + apsState + "; resultByFps = " + resultByFps + ";resultByRoundDuration = " + resultByRoundDuration + "; resultByGameDuration = " + resultByGameDuration + ";resultByGamesInterval = " + resultByGamesInterval);
        }
        if (resultByFps == 0) {
            if (2 == apsState) {
                if (this.mBadCountJudgeByFps >= 2) {
                    adjustMinFpsInDatabase(2);
                    resetStandFeedBackCount();
                }
            } else if (1 == apsState) {
                operateWhenBadJudgedByFps();
            }
        } else if (resultByGameDuration == 0 || resultByGamesInterval == 0 || resultByRoundDuration == 0) {
            if (2 == apsState) {
                adjustMinFpsInDatabase(3);
                resetStandFeedBackCount();
            } else if (1 == apsState) {
                operateWhenBadAndNotJudgedByFps();
            }
        } else if (1 == resultByGameDuration || 1 == resultByGamesInterval || 1 == resultByRoundDuration) {
            operateWhenNotBad();
        }
    }

    private void operateWhenNotBad() {
        this.mStandFeedbackCount++;
        if (this.mStandFeedbackCount >= 10) {
            if (55 == this.mNonplayMinFps) {
                adjustMinFpsInDatabase(1);
            } else {
                adjustMinFpsInDatabase(0);
                this.mGameState.setNonplayFrame(this.mNonplayMinFps);
            }
            resetStandFeedBackCount();
            return;
        }
        this.mOperateExLib.saveGamePlayInfo((long) this.mStandFeedbackCount, 5);
    }

    private void operateWhenBadJudgedByFps() {
        if (this.mIsFirstBadJudgedByFps) {
            this.mGameState.setNonplayFrame(55);
            EventAnalyzed.stopStudyAndControl(true);
            increaseCurrentFpsToMax();
            resetStandFeedBackCount();
            this.mIsFirstBadJudgedByFps = false;
        } else if (this.mBadCountJudgeByFps >= 2) {
            adjustMinFpsInDatabase(2);
            this.mGameState.setNonplayFrame(60);
            sApsUserFeedbackThread.mStop = true;
        }
    }

    private void operateWhenBadAndNotJudgedByFps() {
        adjustMinFpsInDatabase(3);
        this.mGameState.setNonplayFrame(60);
        EventAnalyzed.stopStudyAndControl(true);
        increaseCurrentFpsToMax();
        resetStandFeedBackCount();
        sApsUserFeedbackThread.mStop = true;
    }

    private void resetStandFeedBackCount() {
        if (this.mStandFeedbackCount != 0) {
            this.mStandFeedbackCount = 0;
            this.mOperateExLib.saveGamePlayInfo((long) this.mStandFeedbackCount, 5);
        }
    }

    private void adjustMinFpsInDatabase(int adjustType) {
        switch (adjustType) {
            case 0:
                this.mNonplayMinFps -= Math.round(((float) (this.mNonplayMinFps - 30)) / 5.0f);
                this.mPlayingMinFps -= Math.round(((float) (this.mPlayingMinFps - 30)) / 5.0f);
                break;
            case 1:
                this.mPlayingMinFps -= Math.round(((float) (this.mPlayingMinFps - 30)) / 5.0f);
                break;
            case 2:
                this.mNonplayMinFps = 55;
                this.mPlayingMinFps = Math.round(((float) (55 - this.mPlayingMinFps)) / 5.0f) + this.mPlayingMinFps;
                break;
            case 3:
                this.mNonplayMinFps = Math.round(((float) (55 - this.mNonplayMinFps)) / 5.0f) + this.mNonplayMinFps;
                this.mPlayingMinFps = Math.round(((float) (55 - this.mPlayingMinFps)) / 5.0f) + this.mPlayingMinFps;
                break;
        }
        ApsCommon.logI(TAG, "APS: adjustMinFpsInDatabase----mNonplayMinFps = " + this.mNonplayMinFps + "; mPlayingMinFps = " + this.mPlayingMinFps);
        this.mOperateExLib.saveMinFps(this.mNonplayMinFps, this.mPlayingMinFps);
    }

    private void increaseCurrentFpsToMax() {
        if (this.mFpsRequest != null) {
            int targetFps = this.mFpsRequest.getTargetFPS();
            while (-1 == targetFps) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.w("ApsUserFeedbackThread", "APS: sleep exception");
                }
                targetFps = this.mFpsRequest.getTargetFPS();
                ApsCommon.logI(TAG, "increaseCurrentFpsToMax----targetFps = " + targetFps);
            }
            int feedbackFps = 55 > targetFps ? 55 - targetFps : 0;
            if (feedbackFps != 0 && SystemProperties.getInt("sys.aps.performance", 0) <= 0) {
                this.mFpsRequest.startFeedback(feedbackFps);
            }
            ApsCommon.logI(TAG, "APS: increaseCurrentFpsToMax, targetFps = " + targetFps + "; feedbackFps = " + feedbackFps);
        }
    }

    private long getAndSaveRoundDuration(boolean isToStore) {
        this.mRoundDurationTimerCount++;
        if (this.mRoundDurationTimerCount < this.ROUNT_DURATION_TIMER_COUNT) {
            return 0;
        }
        long mRoundDuration = (long) this.mGameState.getGameRoundDuration();
        this.mRoundDurationTimerCount = 0;
        if (!isToStore || mRoundDuration <= 5) {
            if (mRoundDuration <= 5) {
                mRoundDuration = 0;
            }
            return mRoundDuration;
        }
        if (mRoundDuration > this.mMaxStudyRoundDuration) {
            this.mMaxStudyRoundDuration = mRoundDuration;
        } else if (mRoundDuration < this.mMinStudyRoundDuration) {
            this.mMinStudyRoundDuration = mRoundDuration;
        }
        ApsCommon.logI(TAG, "APS: getAndSaveRoundDuration, mRoundDuration = " + mRoundDuration);
        this.mOperateExLib.insertGameRoundDuration(mRoundDuration);
        this.mCountofGameRoundsDuration++;
        return 0;
    }

    private void computeAndSaveGamesDuration() {
        long gamesDurationToStore;
        if (this.mIsFirstTimeComputePlayDuration) {
            this.mGamesDuration += this.mCurrentPauseTime - this.mCurrentBeginTime;
            this.mIsFirstTimeComputePlayDuration = false;
        } else {
            this.mGamesDuration += this.mCurrentPauseTime - this.mCurrentResumeTime;
        }
        if (this.mIsApsControlled) {
            gamesDurationToStore = (this.mGamesDuration * 10) + 1;
        } else {
            gamesDurationToStore = this.mGamesDuration * 10;
        }
        ApsCommon.logI(TAG, "APS: computeAndSaveGamesDuration, mGamesDuration = " + this.mGamesDuration);
        this.mOperateExLib.saveGamePlayInfo(gamesDurationToStore, 1);
    }

    private void computeAndSaveGamesInterval() {
        if (this.mIsApsControlled && (this.mIsStoredCtrlBeginTime ^ 1) != 0 && this.mLastCtrlBeginDate != this.mCurrentBeginDate) {
            this.mLastStudyBeginDate = 0;
            this.mLastCtrlBeginDate = this.mCurrentBeginDate;
            this.mIsStoredCtrlBeginTime = true;
            this.mIsStoredStudyBeginTime = true;
            this.mOperateExLib.saveGamePlayInfo(this.mLastCtrlBeginDate, 3);
        } else if (!this.mIsStoredStudyBeginTime && this.mLastStudyBeginDate != this.mCurrentBeginDate && this.mLastCtrlBeginDate != this.mCurrentBeginDate) {
            this.mLastStudyBeginDate = this.mCurrentBeginDate;
            this.mLastCtrlBeginDate = 0;
            this.mIsStoredStudyBeginTime = true;
            this.mOperateExLib.saveGamePlayInfo(this.mLastStudyBeginDate, 4);
        }
    }

    private int isBadJudgedByFps() {
        this.mSwitchContinuallyTimerCount++;
        int result;
        if (this.mIsFirstTimeBadJudgedByFps) {
            if (this.mSwitchContinuallyTimerCount < this.FIRST_SWITCH_CONTINUALLY_TIMER_COUNT) {
                return -1;
            }
            result = this.mGameState.getResultJudgedByFps();
            this.mIsFirstTimeBadJudgedByFps = false;
            this.mSwitchContinuallyTimerCount = 0;
            return result;
        } else if (this.mSwitchContinuallyTimerCount < this.SWITCH_CONTINUALLY_TIMER_COUNT) {
            return -1;
        } else {
            result = this.mGameState.getResultJudgedByFps();
            if (-1 == result) {
                return result;
            }
            this.mSwitchContinuallyTimerCount = 0;
            return result;
        }
    }

    private int isBadJudgedByRoundDuration(long ctrlRoundDuration) {
        if (this.mCountofGameRoundsDuration < 7 || 0 == ctrlRoundDuration) {
            return -1;
        }
        return isBadJudgedByEstimateAlgorithm(this.mMaxStudyRoundDuration, this.mMinStudyRoundDuration, ctrlRoundDuration, 0);
    }

    private int isBadJudgedByGamesDuration() {
        if (0 == this.mCtrlGamesDuration) {
            return -1;
        }
        this.mCountOfGamesDuration = this.mOperateExLib.queryCount(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesDuration"));
        if (this.mCountOfGamesDuration < 7) {
            return -1;
        }
        this.mMinStudyPlayDuration = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesDuration/Min"));
        this.mMaxStudyPlayDuration = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesDuration/Max"));
        return isBadJudgedByEstimateAlgorithm(this.mMaxStudyPlayDuration, this.mMinStudyPlayDuration, this.mCtrlGamesDuration, 0);
    }

    private int isBadJudgedByGamesInterval() {
        if (0 == this.mCtrlGameInterval) {
            return -1;
        }
        this.mCountOfGamesInterval = this.mOperateExLib.queryCount(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesInterval"));
        if (this.mCountOfGamesInterval < 7) {
            return -1;
        }
        this.mMinStudyGameInterval = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesInterval/Min"));
        this.mMaxStudyGameInterval = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesInterval/Max"));
        return isBadJudgedByEstimateAlgorithm(this.mMaxStudyGameInterval, this.mMinStudyGameInterval, this.mCtrlGameInterval, 1);
    }

    private int isBadJudgedByEstimateAlgorithm(long max, long min, long cur, int type) {
        ApsCommon.logD(TAG, "isBadJudgedByEstimateAlgorithm,max = " + max + "; min = " + min + "; cur = " + cur + "; type = " + type);
        if (0 == max || 0 == min || 0 == cur) {
            return -1;
        }
        if (type == 0) {
            return ((double) (min - cur)) / ((double) cur) > ((double) (max - min)) / ((double) min) ? 0 : 1;
        } else if (1 != type) {
            return 1;
        } else {
            return ((double) (cur - max)) / ((double) max) > ((double) (max - min)) / ((double) min) ? 0 : 1;
        }
    }

    private void getHistoryDataFromLib() {
        if (this.mOperateExLib != null) {
            this.mOperateExLib.queryGamePlayInfo();
            this.mOperateExLib.query();
            this.mCtrlGamesDuration = this.mOperateExLib.getCtrlPlayDuration();
            this.mLastStudyBeginDate = this.mOperateExLib.getLastStudyBeginDate();
            this.mLastCtrlBeginDate = this.mOperateExLib.getLastCtrlBeginDate();
            this.mCtrlGameInterval = this.mOperateExLib.getCtrlGameInterval();
            this.mStandFeedbackCount = this.mOperateExLib.getStandFeedbackCount();
            this.mMinStudyRoundDuration = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GameRoundsDuration/Min"));
            this.mMaxStudyRoundDuration = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GameRoundsDuration/Max"));
            this.mCountofGameRoundsDuration = this.mOperateExLib.queryCount(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GameRoundsDuration"));
            this.mPlayingMinFps = this.mOperateExLib.getMinFps();
            this.mNonplayMinFps = this.mOperateExLib.getMinNonplayFps();
        }
        ApsCommon.logI(TAG, "APS: getHistoryDataFromLib: mCtrlGamesDuration = " + this.mCtrlGamesDuration + "; mLastStudyBeginDate = " + this.mLastStudyBeginDate + "; mLastCtrlBeginDate = " + this.mLastCtrlBeginDate + "; mCtrlGameInterval = " + this.mCtrlGameInterval + "; mStandFeedbackCount = " + this.mStandFeedbackCount + "; mMinStudyRoundDuration = " + this.mMinStudyRoundDuration + "; mMaxStudyRoundDuration = " + this.mMaxStudyRoundDuration + "; mCountofGameRoundsDuration = " + this.mCountofGameRoundsDuration + "; mPlayingMinFps = " + this.mPlayingMinFps + "; mNonplayMinFps = " + this.mNonplayMinFps);
    }

    public void setNeedUserFeedBack(boolean needUserFeedBack) {
        this.mIsNeedUserFeedBack = needUserFeedBack;
    }

    public void setOperateExperienceLib(OperateExperienceLib experienceLib) {
        this.mOperateExLib = experienceLib;
    }
}
