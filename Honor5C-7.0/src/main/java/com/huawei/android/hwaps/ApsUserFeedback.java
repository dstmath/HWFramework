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
    private static ApsUserFeedbackThread sApsUserFeedbackThread;
    private static ApsUserFeedback sInstance;
    public int FIRST_SWITCH_CONTINUALLY_TIMER_COUNT;
    public int ROUNT_DURATION_TIMER_COUNT;
    public int SWITCH_CONTINUALLY_TIMER_COUNT;
    private int mBadCountJudgeByFps;
    private int mCountOfGamesDuration;
    private int mCountOfGamesInterval;
    private int mCountofGameRoundsDuration;
    private long mCtrlGameInterval;
    private long mCtrlGamesDuration;
    private long mCurrentBeginDate;
    private long mCurrentBeginTime;
    private long mCurrentPauseTime;
    private long mCurrentResumeTime;
    private FpsRequest mFpsRequest;
    private GameState mGameState;
    private long mGamesDuration;
    private boolean mIsApsControlled;
    private boolean mIsFirstBadJudgedByFps;
    private boolean mIsFirstTimeBadJudgedByFps;
    private boolean mIsFirstTimeComputePlayDuration;
    private boolean mIsFirstTimeFeedBackCtrl;
    private boolean mIsFirstTimeToPerformUserFeedBack;
    private boolean mIsNeedUserFeedBack;
    private boolean mIsStoredCtrlBeginTime;
    private boolean mIsStoredStudyBeginTime;
    private long mLastCtrlBeginDate;
    private long mLastStudyBeginDate;
    private long mMaxStudyGameInterval;
    private long mMaxStudyPlayDuration;
    private long mMaxStudyRoundDuration;
    private long mMinStudyGameInterval;
    private long mMinStudyPlayDuration;
    private long mMinStudyRoundDuration;
    private int mNonplayMinFps;
    private OperateExperienceLib mOperateExLib;
    private int mPlayingMinFps;
    private int mRoundDurationTimerCount;
    private int mStandFeedbackCount;
    private int mSwitchContinuallyTimerCount;

    public class ApsUserFeedbackThread implements Runnable {
        public boolean mRunOver;
        public boolean mStop;

        public ApsUserFeedbackThread() {
            this.mStop = false;
            this.mRunOver = false;
        }

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.ApsUserFeedback.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.ApsUserFeedback.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.ApsUserFeedback.<clinit>():void");
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
        int isApsSupport = SystemProperties.getInt("sys.aps.support", RESULT_IS_BAD);
        if (16 == (isApsSupport & 16) && (isApsSupport & SLEEP_SECOND_BEFORE_START_THREAD) != 0) {
            return true;
        }
        Log.w(TAG, "APS: User feedback is not supported");
        return false;
    }

    public ApsUserFeedback() {
        this.mFpsRequest = null;
        this.mOperateExLib = null;
        this.mGameState = null;
        this.ROUNT_DURATION_TIMER_COUNT = 12;
        this.SWITCH_CONTINUALLY_TIMER_COUNT = LAST_STUDY_BEGIN_DATE;
        this.FIRST_SWITCH_CONTINUALLY_TIMER_COUNT = this.SWITCH_CONTINUALLY_TIMER_COUNT * RAISE_FPS_ALL_SPECIAL;
        this.mNonplayMinFps = DEFAULT_MIN_PLAYING_FPS;
        this.mPlayingMinFps = DEFAULT_MIN_PLAYING_FPS;
        this.mCountofGameRoundsDuration = RESULT_IS_BAD;
        this.mCountOfGamesDuration = RESULT_IS_BAD;
        this.mCountOfGamesInterval = RESULT_IS_BAD;
        this.mStandFeedbackCount = RESULT_IS_BAD;
        this.mSwitchContinuallyTimerCount = RESULT_IS_BAD;
        this.mRoundDurationTimerCount = RESULT_IS_BAD;
        this.mBadCountJudgeByFps = RESULT_IS_BAD;
        this.mIsFirstTimeBadJudgedByFps = true;
        this.mIsApsControlled = false;
        this.mIsFirstTimeComputePlayDuration = true;
        this.mIsStoredCtrlBeginTime = false;
        this.mIsStoredStudyBeginTime = false;
        this.mIsFirstTimeToPerformUserFeedBack = true;
        this.mIsFirstBadJudgedByFps = true;
        this.mIsFirstTimeFeedBackCtrl = true;
        this.mIsNeedUserFeedBack = true;
        this.mCurrentBeginTime = 0;
        this.mCurrentResumeTime = 0;
        this.mCurrentPauseTime = 0;
        this.mCurrentBeginDate = 0;
        this.mLastCtrlBeginDate = 0;
        this.mLastStudyBeginDate = 0;
        this.mGamesDuration = 0;
        this.mMinStudyRoundDuration = 0;
        this.mMaxStudyRoundDuration = 0;
        this.mMinStudyPlayDuration = 0;
        this.mMaxStudyPlayDuration = 0;
        this.mMinStudyGameInterval = 0;
        this.mMaxStudyGameInterval = 0;
        this.mCtrlGamesDuration = 0;
        this.mCtrlGameInterval = 0;
        if (this.mFpsRequest == null) {
            this.mFpsRequest = new FpsRequest(SceneTypeE.USER_EXPERIENCE);
        }
        startApsUserFeedbackThread();
        ApsCommon.logI(TAG, "APS:User feedback module create success");
    }

    public void stop() {
        if (!sApsUserFeedbackThread.mStop) {
            this.mSwitchContinuallyTimerCount = RESULT_IS_BAD;
            this.mRoundDurationTimerCount = RESULT_IS_BAD;
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
        this.mCurrentBeginDate = (long) (((cal.get(RESULT_IS_NOT_BAD) * 10000) + ((cal.get(RAISE_FPS_ALL_SPECIAL) + RESULT_IS_NOT_BAD) * 100)) + cal.get(STAND_FEED_BACK_COUNT));
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
            int apsState = SystemProperties.getInt("debug.aps.enable", RESULT_IS_BAD);
            if (apsState != 0) {
                if (RESULT_IS_NOT_BAD == apsState) {
                    this.mIsApsControlled = true;
                }
                performApsUserFeedback(apsState);
            }
        }
    }

    private void performApsUserFeedback(int apsState) {
        int resultByFps = DATA_NOT_ENOUGH;
        int resultByRoundDuration = DATA_NOT_ENOUGH;
        int resultByGameDuration = DATA_NOT_ENOUGH;
        int resultByGamesInterval = DATA_NOT_ENOUGH;
        if (this.mIsFirstTimeToPerformUserFeedBack) {
            this.mIsFirstTimeToPerformUserFeedBack = false;
            this.mRoundDurationTimerCount = RESULT_IS_BAD;
            computeAndSaveGamesInterval();
            resultByGameDuration = isBadJudgedByGamesDuration();
            resultByGamesInterval = isBadJudgedByGamesInterval();
        }
        if (this.mNonplayMinFps != ADJUST_MAX_FPS) {
            resultByFps = isBadJudgedByFps();
            if (resultByFps == 0) {
                this.mBadCountJudgeByFps += RESULT_IS_NOT_BAD;
            }
        }
        if (apsState == RAISE_FPS_ALL_SPECIAL) {
            getAndSaveRoundDuration(true);
        } else if (apsState == RESULT_IS_NOT_BAD) {
            resultByRoundDuration = isBadJudgedByRoundDuration(getAndSaveRoundDuration(false));
        }
        operateBasedOnResult(apsState, resultByFps, resultByRoundDuration, resultByGameDuration, resultByGamesInterval);
    }

    private void operateBasedOnResult(int apsState, int resultByFps, int resultByRoundDuration, int resultByGameDuration, int resultByGamesInterval) {
        if (!(resultByGameDuration == 0 || resultByGamesInterval == 0 || resultByRoundDuration == 0 || resultByFps == 0 || RESULT_IS_NOT_BAD == resultByGameDuration || RESULT_IS_NOT_BAD == resultByGamesInterval || RESULT_IS_NOT_BAD == resultByRoundDuration)) {
            if (RESULT_IS_NOT_BAD == resultByFps) {
            }
            if (resultByFps != 0) {
                if (RAISE_FPS_ALL_SPECIAL != apsState) {
                    if (this.mBadCountJudgeByFps >= RAISE_FPS_ALL_SPECIAL) {
                        adjustMinFpsInDatabase(RAISE_FPS_ALL_SPECIAL);
                        resetStandFeedBackCount();
                    }
                } else if (RESULT_IS_NOT_BAD == apsState) {
                    operateWhenBadJudgedByFps();
                }
            } else if (resultByGameDuration != 0 || resultByGamesInterval == 0 || resultByRoundDuration == 0) {
                if (RAISE_FPS_ALL_SPECIAL == apsState) {
                    adjustMinFpsInDatabase(SLEEP_SECOND_BEFORE_START_THREAD);
                    resetStandFeedBackCount();
                } else if (RESULT_IS_NOT_BAD == apsState) {
                    operateWhenBadAndNotJudgedByFps();
                }
                return;
            } else if (RESULT_IS_NOT_BAD == resultByGameDuration || RESULT_IS_NOT_BAD == resultByGamesInterval || RESULT_IS_NOT_BAD == resultByRoundDuration) {
                operateWhenNotBad();
                return;
            } else {
                return;
            }
        }
        ApsCommon.logD(TAG, "APS: operateBasedOnResult----apsState = " + apsState + "; resultByFps = " + resultByFps + ";resultByRoundDuration = " + resultByRoundDuration + "; resultByGameDuration = " + resultByGameDuration + ";resultByGamesInterval = " + resultByGamesInterval);
        if (resultByFps != 0) {
            if (resultByGameDuration != 0) {
            }
            if (RAISE_FPS_ALL_SPECIAL == apsState) {
                adjustMinFpsInDatabase(SLEEP_SECOND_BEFORE_START_THREAD);
                resetStandFeedBackCount();
            } else if (RESULT_IS_NOT_BAD == apsState) {
                operateWhenBadAndNotJudgedByFps();
            }
            return;
        }
        if (RAISE_FPS_ALL_SPECIAL != apsState) {
            if (RESULT_IS_NOT_BAD == apsState) {
                operateWhenBadJudgedByFps();
            }
        } else if (this.mBadCountJudgeByFps >= RAISE_FPS_ALL_SPECIAL) {
            adjustMinFpsInDatabase(RAISE_FPS_ALL_SPECIAL);
            resetStandFeedBackCount();
        }
    }

    private void operateWhenNotBad() {
        this.mStandFeedbackCount += RESULT_IS_NOT_BAD;
        if (this.mStandFeedbackCount >= NOT_BAD_COUNT_TO_REDUCE_FPS) {
            if (ADJUST_MAX_FPS == this.mNonplayMinFps) {
                adjustMinFpsInDatabase(RESULT_IS_NOT_BAD);
            } else {
                adjustMinFpsInDatabase(RESULT_IS_BAD);
                this.mGameState.setNonplayFrame(this.mNonplayMinFps);
            }
            resetStandFeedBackCount();
            return;
        }
        this.mOperateExLib.saveGamePlayInfo((long) this.mStandFeedbackCount, STAND_FEED_BACK_COUNT);
    }

    private void operateWhenBadJudgedByFps() {
        if (this.mIsFirstBadJudgedByFps) {
            this.mGameState.setNonplayFrame(ADJUST_MAX_FPS);
            EventAnalyzed.stopStudyAndControl(true);
            increaseCurrentFpsToMax();
            resetStandFeedBackCount();
            this.mIsFirstBadJudgedByFps = false;
        } else if (this.mBadCountJudgeByFps >= RAISE_FPS_ALL_SPECIAL) {
            adjustMinFpsInDatabase(RAISE_FPS_ALL_SPECIAL);
            this.mGameState.setNonplayFrame(SWITCH_CONTINUALLY_CHECK_PEROID);
            sApsUserFeedbackThread.mStop = true;
        }
    }

    private void operateWhenBadAndNotJudgedByFps() {
        adjustMinFpsInDatabase(SLEEP_SECOND_BEFORE_START_THREAD);
        this.mGameState.setNonplayFrame(SWITCH_CONTINUALLY_CHECK_PEROID);
        EventAnalyzed.stopStudyAndControl(true);
        increaseCurrentFpsToMax();
        resetStandFeedBackCount();
        sApsUserFeedbackThread.mStop = true;
    }

    private void resetStandFeedBackCount() {
        if (this.mStandFeedbackCount != 0) {
            this.mStandFeedbackCount = RESULT_IS_BAD;
            this.mOperateExLib.saveGamePlayInfo((long) this.mStandFeedbackCount, STAND_FEED_BACK_COUNT);
        }
    }

    private void adjustMinFpsInDatabase(int adjustType) {
        switch (adjustType) {
            case RESULT_IS_BAD /*0*/:
                this.mNonplayMinFps -= Math.round(((float) (this.mNonplayMinFps - 30)) / 5.0f);
                this.mPlayingMinFps -= Math.round(((float) (this.mPlayingMinFps - 30)) / 5.0f);
                break;
            case RESULT_IS_NOT_BAD /*1*/:
                this.mPlayingMinFps -= Math.round(((float) (this.mPlayingMinFps - 30)) / 5.0f);
                break;
            case RAISE_FPS_ALL_SPECIAL /*2*/:
                this.mNonplayMinFps = ADJUST_MAX_FPS;
                this.mPlayingMinFps = Math.round(((float) (55 - this.mPlayingMinFps)) / 5.0f) + this.mPlayingMinFps;
                break;
            case SLEEP_SECOND_BEFORE_START_THREAD /*3*/:
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
            while (DATA_NOT_ENOUGH == targetFps) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.w("ApsUserFeedbackThread", "APS: sleep exception");
                }
                targetFps = this.mFpsRequest.getTargetFPS();
                ApsCommon.logI(TAG, "increaseCurrentFpsToMax----targetFps = " + targetFps);
            }
            int feedbackFps = ADJUST_MAX_FPS > targetFps ? 55 - targetFps : RESULT_IS_BAD;
            if (feedbackFps != 0 && SystemProperties.getInt("sys.aps.performance", RESULT_IS_BAD) <= 0) {
                this.mFpsRequest.startFeedback(feedbackFps);
            }
            ApsCommon.logI(TAG, "APS: increaseCurrentFpsToMax, targetFps = " + targetFps + "; feedbackFps = " + feedbackFps);
        }
    }

    private long getAndSaveRoundDuration(boolean isToStore) {
        this.mRoundDurationTimerCount += RESULT_IS_NOT_BAD;
        if (this.mRoundDurationTimerCount < this.ROUNT_DURATION_TIMER_COUNT) {
            return 0;
        }
        long mRoundDuration = (long) this.mGameState.getGameRoundDuration();
        this.mRoundDurationTimerCount = RESULT_IS_BAD;
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
        this.mCountofGameRoundsDuration += RESULT_IS_NOT_BAD;
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
        this.mOperateExLib.saveGamePlayInfo(gamesDurationToStore, RESULT_IS_NOT_BAD);
    }

    private void computeAndSaveGamesInterval() {
        if (this.mIsApsControlled && !this.mIsStoredCtrlBeginTime && this.mLastCtrlBeginDate != this.mCurrentBeginDate) {
            this.mLastStudyBeginDate = 0;
            this.mLastCtrlBeginDate = this.mCurrentBeginDate;
            this.mIsStoredCtrlBeginTime = true;
            this.mIsStoredStudyBeginTime = true;
            this.mOperateExLib.saveGamePlayInfo(this.mLastCtrlBeginDate, SLEEP_SECOND_BEFORE_START_THREAD);
        } else if (!this.mIsStoredStudyBeginTime && this.mLastStudyBeginDate != this.mCurrentBeginDate && this.mLastCtrlBeginDate != this.mCurrentBeginDate) {
            this.mLastStudyBeginDate = this.mCurrentBeginDate;
            this.mLastCtrlBeginDate = 0;
            this.mIsStoredStudyBeginTime = true;
            this.mOperateExLib.saveGamePlayInfo(this.mLastStudyBeginDate, LAST_STUDY_BEGIN_DATE);
        }
    }

    private int isBadJudgedByFps() {
        this.mSwitchContinuallyTimerCount += RESULT_IS_NOT_BAD;
        int result;
        if (this.mIsFirstTimeBadJudgedByFps) {
            if (this.mSwitchContinuallyTimerCount < this.FIRST_SWITCH_CONTINUALLY_TIMER_COUNT) {
                return DATA_NOT_ENOUGH;
            }
            result = this.mGameState.getResultJudgedByFps();
            this.mIsFirstTimeBadJudgedByFps = false;
            this.mSwitchContinuallyTimerCount = RESULT_IS_BAD;
            return result;
        } else if (this.mSwitchContinuallyTimerCount < this.SWITCH_CONTINUALLY_TIMER_COUNT) {
            return DATA_NOT_ENOUGH;
        } else {
            result = this.mGameState.getResultJudgedByFps();
            if (DATA_NOT_ENOUGH == result) {
                return result;
            }
            this.mSwitchContinuallyTimerCount = RESULT_IS_BAD;
            return result;
        }
    }

    private int isBadJudgedByRoundDuration(long ctrlRoundDuration) {
        if (this.mCountofGameRoundsDuration < HALF_OF_DEFAULT_STORE_COUNT || 0 == ctrlRoundDuration) {
            return DATA_NOT_ENOUGH;
        }
        return isBadJudgedByEstimateAlgorithm(this.mMaxStudyRoundDuration, this.mMinStudyRoundDuration, ctrlRoundDuration, RESULT_IS_BAD);
    }

    private int isBadJudgedByGamesDuration() {
        if (0 == this.mCtrlGamesDuration) {
            return DATA_NOT_ENOUGH;
        }
        this.mCountOfGamesDuration = this.mOperateExLib.queryCount(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesDuration"));
        if (this.mCountOfGamesDuration < HALF_OF_DEFAULT_STORE_COUNT) {
            return DATA_NOT_ENOUGH;
        }
        this.mMinStudyPlayDuration = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesDuration/Min"));
        this.mMaxStudyPlayDuration = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesDuration/Max"));
        return isBadJudgedByEstimateAlgorithm(this.mMaxStudyPlayDuration, this.mMinStudyPlayDuration, this.mCtrlGamesDuration, RESULT_IS_BAD);
    }

    private int isBadJudgedByGamesInterval() {
        if (0 == this.mCtrlGameInterval) {
            return DATA_NOT_ENOUGH;
        }
        this.mCountOfGamesInterval = this.mOperateExLib.queryCount(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesInterval"));
        if (this.mCountOfGamesInterval < HALF_OF_DEFAULT_STORE_COUNT) {
            return DATA_NOT_ENOUGH;
        }
        this.mMinStudyGameInterval = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesInterval/Min"));
        this.mMaxStudyGameInterval = this.mOperateExLib.queryGameTimeInfo(Uri.parse("content://com.huawei.android.hwaps.ApsProvider/GamesInterval/Max"));
        return isBadJudgedByEstimateAlgorithm(this.mMaxStudyGameInterval, this.mMinStudyGameInterval, this.mCtrlGameInterval, RESULT_IS_NOT_BAD);
    }

    private int isBadJudgedByEstimateAlgorithm(long max, long min, long cur, int type) {
        ApsCommon.logD(TAG, "isBadJudgedByEstimateAlgorithm,max = " + max + "; min = " + min + "; cur = " + cur + "; type = " + type);
        if (0 == max || 0 == min || 0 == cur) {
            return DATA_NOT_ENOUGH;
        }
        if (type == 0) {
            return ((double) (min - cur)) / ((double) cur) > ((double) (max - min)) / ((double) min) ? RESULT_IS_BAD : RESULT_IS_NOT_BAD;
        } else if (RESULT_IS_NOT_BAD != type) {
            return RESULT_IS_NOT_BAD;
        } else {
            return ((double) (cur - max)) / ((double) max) > ((double) (max - min)) / ((double) min) ? RESULT_IS_BAD : RESULT_IS_NOT_BAD;
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
