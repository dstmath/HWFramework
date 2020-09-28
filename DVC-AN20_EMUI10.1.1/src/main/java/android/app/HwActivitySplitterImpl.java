package android.app;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.huawei.android.app.HwActivityTaskManager;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HwActivitySplitterImpl implements IHwActivitySplitterImpl {
    private static final String ACTION_CROP_IMAGE = "com.android.camera.action.CROP";
    private static final String ACTION_CROP_WALLPAPER = "com.android.camera.action.CROP_WALLPAPER";
    private static final int DEFAULT_LEFT_WEIGHT = 4;
    private static final int DEFAULT_RIGHT_WEIGHT = 6;
    private static final int ENSURE_ACTIVITY_RESUME_DELAY = 500;
    private static final int ENSURE_CONTENT_SHOWING_DELAY = 300;
    public static final String EXTRA_JUMPED_ACTIVITY = "huawei.intent.extra.JUMPED_ACTIVITY";
    private static String EXTRA_SPLIT_BASE_PID = "huawei.extra.split.PID";
    private static final String EXTRA_SPLIT_MODE = "huawei.extra.splitmode";
    private static final int EXTRA_VALUE_BASE_ACTIVITY = 4;
    private static final int EXTRA_VALUE_EXIT_ALONE = 2;
    private static final int EXTRA_VALUE_FORCE_SPLIT = 8;
    private static final int EXTRA_VALUE_SEC_ACTIVITY = 1;
    private static final int EXTRA_VALUE_SUBINTENT_ONE = 16;
    private static final int IS_NEED_PERFORM_CLICK_ACTION_DELAY = 500;
    private static final int MSG_ADD_TO_ENTRY_STACK = 2;
    private static final int MSG_CLEAR_ENTRY_STACK = 1;
    private static final int MSG_RESTART_LAST_CONTENT = 6;
    private static final int MSG_SET_ACTIONBAR = 3;
    private static final int MSG_SET_FIRST_DONE = 4;
    private static final int MSG_SET_RESUMED = 5;
    private static final String TAG = "HwActivitySplitterImpl";
    private static final String TYPE_FILEMANAGER = "filemanager.dir";
    private static final String descriptor = "android.app.IActivityManager";
    private static boolean exitMuitiWindowModeJsutRecent = false;
    private static View mContentIndexActionBar;
    private static View mContentIndexView;
    private static float mContentIndexViewWeight;
    private static boolean mControllerShowing = false;
    private static boolean mFirstTimeStart = true;
    private static HashMap<Integer, HwActivitySplitterImpl> mInstanceMap = new HashMap<>();
    private static boolean mLastReportedMultiWindowMode = false;
    private static int oldWindowWidth = 0;
    private Activity mActivity;
    private boolean mAllContentGone = false;
    private boolean mAllSubFinished = false;
    private boolean mBackPressed = false;
    private int mBasePid = -1;
    private float mContentWindowWeight;
    private long mEventDelayTimeBegin = 0;
    private boolean mIsFinishing = false;
    private boolean mIsJumpActivity = false;
    private boolean mIsSecondStageActivity = false;
    private boolean mIsSplitBaseActivity = false;
    private boolean mRecycled = false;
    private boolean mRestart = false;
    private boolean mResumed = true;
    private boolean mSplit = false;
    private Handler mSplitHandler;
    private IBinder mToken;
    private int mTransCodeAddEntry = 0;
    private int mTransCodeClearEntry = 0;
    private int mTransCodeGetLast = 0;
    private int mTransCodeIsTop = 0;
    private int mTransCodeRemoveEntry = 0;
    private int mTransCodeSetLast = 0;
    private int mUpButtonVisibility = -1;
    private Point mWinSize = null;
    private IWindowManager mWindowManager;

    public void setLastReportedMultiWindowMode(boolean newValue) {
        mLastReportedMultiWindowMode = newValue;
        exitMuitiWindowModeJsutRecent = !newValue;
        if (this.mResumed && !newValue) {
            Log.e(TAG, "setLastReportedMultiWindowMode to false and restartLastContentIfNeeded!");
            scheduleDelayRestartLastContent(true);
        }
    }

    public void setActivityInfo(Activity a, IBinder token) {
        this.mActivity = a;
        this.mToken = token;
        this.mRecycled = false;
    }

    private int getSplitModeValue(Intent intent) {
        if (intent == null) {
            return 0;
        }
        return intent.getIntExtra(EXTRA_SPLIT_MODE, 0);
    }

    private Intent setToSecondaryStage(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 1);
    }

    private Intent setToExitAlone(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 2);
    }

    private Intent setToExitTogether(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) & -3);
    }

    private Intent markBaseActivity(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 4);
    }

    public Intent setToSubIntentOne(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 16);
    }

    public Intent setToForceSplit(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 8);
    }

    public HwActivitySplitterImpl(Activity a, boolean isBase) {
        this.mActivity = a;
        if (isBase) {
            removeCancelFlagInner(a.getIntent());
        }
    }

    public static HwActivitySplitterImpl getDefault(Activity a) {
        return getDefault(a, false);
    }

    public static HwActivitySplitterImpl getDefault(Activity a, boolean isBase) {
        if (a == null) {
            return null;
        }
        HwActivitySplitterImpl instance = mInstanceMap.get(Integer.valueOf(a.hashCode()));
        if (instance != null) {
            return instance;
        }
        if (!isBase && (a.getIntent() == null || (a.getIntent().getHwFlags() & 4) == 0)) {
            return null;
        }
        HwActivitySplitterImpl instance2 = new HwActivitySplitterImpl(a, isBase);
        mInstanceMap.put(Integer.valueOf(a.hashCode()), instance2);
        return instance2;
    }

    public boolean isSplitMode() {
        return this.mSplit;
    }

    public boolean isSplitBaseActivity() {
        return this.mIsSplitBaseActivity;
    }

    public boolean isSplitSecondActivity() {
        return this.mIsSecondStageActivity;
    }

    public void setSplit(View contentIndexView, float weight) {
        setBaseActivity();
        mContentIndexView = contentIndexView;
        mContentIndexViewWeight = weight;
        this.mContentWindowWeight = 1.0f - mContentIndexViewWeight;
        adjustContentIndexView();
    }

    public void setSplit(View contentIndexView) {
        setBaseActivity();
        mContentIndexView = contentIndexView;
        int leftWeight = 4;
        int rightWeight = 6;
        if (!HwSplitUtils.IS_FOLDABLE_PHONE) {
            leftWeight = this.mActivity.getResources().getInteger(34275328);
            rightWeight = this.mActivity.getResources().getInteger(34275329);
        }
        mContentIndexViewWeight = (((float) leftWeight) * 1.0f) / ((float) (leftWeight + rightWeight));
        this.mContentWindowWeight = 1.0f - mContentIndexViewWeight;
        adjustContentIndexView();
    }

    public void setSplit(float contentWeight) {
        setBaseActivity();
        this.mContentWindowWeight = contentWeight;
    }

    public void setSplit() {
        setBaseActivity();
        int leftWeight = 4;
        int rightWeight = 6;
        if (!HwSplitUtils.IS_FOLDABLE_PHONE) {
            leftWeight = this.mActivity.getResources().getInteger(34275328);
            rightWeight = this.mActivity.getResources().getInteger(34275329);
        }
        this.mContentWindowWeight = (((float) rightWeight) * 1.0f) / ((float) (leftWeight + rightWeight));
    }

    public void setControllerShowing(boolean showing) {
        if (Log.HWLog) {
            Log.i(TAG, "set controller showing : " + showing + ", " + this.mActivity);
        }
        setControllerShowingValue(showing);
    }

    private static void setControllerShowingValue(boolean showing) {
        mControllerShowing = showing;
    }

    public boolean isControllerShowing() {
        return mControllerShowing;
    }

    public void setFirstIntent(Intent intent) {
        if (this.mIsSplitBaseActivity && intent != null && getCurrentSubIntent() == null) {
            setSecondStageIntent(intent);
            setSubIntent(intent);
        }
    }

    public void setTargetIntent(Intent intent) {
        setToSecondaryStage(intent);
        setSubIntent(intent);
        deliverExtraInfo(intent);
    }

    private void setSubIntent(Intent intent) {
        if (intent != null && (intent.getIntExtra(EXTRA_SPLIT_MODE, 0) & 16) != 0) {
            setCurrentSubIntent(null);
        } else if (!isJumpedActivity()) {
            setCurrentSubIntent(intent);
        }
    }

    private void setLastSplittableActivity(Intent intent) {
        try {
            setIntentInfo(intent, -1, getIntentBundle(intent), true);
        } catch (RemoteException e) {
            Log.e(TAG, "Record activity info fail!");
        }
    }

    private void setCurrentSubIntent(Intent intent) {
        try {
            setIntentInfo(intent, Process.myPid(), null, false);
        } catch (RemoteException e) {
            Log.e(TAG, "Record sub-intent info fail!");
        }
    }

    private void adjustContentIndexView() {
        adjustContentIndexView(false);
    }

    private void adjustContentIndexView(boolean fromExitMultiWindow) {
        if (mContentIndexView != null && mContentIndexViewWeight > 0.0f && reachSplitSize()) {
            ViewGroup.LayoutParams lp = mContentIndexView.getLayoutParams();
            oldWindowWidth = getCurrentWindowWidth();
            lp.width = (int) ((mContentIndexViewWeight * ((float) oldWindowWidth)) + 0.5f);
            if (Log.HWLog) {
                Log.i(TAG, "Adjust content index view, set width to " + lp.width);
            }
            mContentIndexView.setLayoutParams(lp);
            adjustActionBar(fromExitMultiWindow ? mContentIndexActionBar : null);
        }
    }

    public View getActionBarView() {
        View v = this.mActivity.getWindow().getDecorView();
        View actionBar = v.findViewById(this.mActivity.getResources().getIdentifier("hwtoolbar", "id", "android"));
        if (actionBar == null) {
            return v.findViewById(this.mActivity.getResources().getIdentifier("hwtoolbar", "id", this.mActivity.getPackageName()));
        }
        return actionBar;
    }

    private void adjustActionBar(View actionBar) {
        View abLayout = actionBar == null ? getActionBarView() : actionBar;
        if (this.mIsSplitBaseActivity) {
            mContentIndexActionBar = abLayout;
        }
        if (abLayout == null || mContentIndexViewWeight <= 0.0f) {
            Log.w(TAG, "Can not get actionbar layout.");
            return;
        }
        ViewGroup.LayoutParams lp = abLayout.getLayoutParams();
        lp.width = (int) ((mContentIndexViewWeight * ((float) getCurrentWindowWidth())) + 0.5f);
        abLayout.setLayoutParams(lp);
    }

    public void reduceIndexView() {
        View view = mContentIndexView;
        if (view != null) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = -1;
            mContentIndexView.setLayoutParams(lp);
            reduceActionBar();
        }
    }

    private void reduceActionBar() {
        View abLayout = getActionBarView();
        if (abLayout != null) {
            ViewGroup.LayoutParams lp = abLayout.getLayoutParams();
            lp.width = -1;
            abLayout.setLayoutParams(lp);
        }
    }

    public boolean needSplitActivity() {
        Activity activity = this.mActivity;
        if (activity == null) {
            return false;
        }
        return needSplitActivity(activity.getIntent());
    }

    public boolean needSplitActivity(Intent intent) {
        boolean split = false;
        if (intent != null) {
            try {
                split = (intent.getHwFlags() & 4) != 0 && (!needCancelSplit(intent) || (intent.getIntExtra(EXTRA_SPLIT_MODE, 0) & 8) != 0);
                checkSecondStage(intent);
                if (!split) {
                    disposeFakeActivity(intent);
                }
                if (this.mIsSecondStageActivity) {
                    if (split) {
                        setSubIntent(intent);
                    } else {
                        setExitWhenContentGone(false);
                    }
                }
            } catch (RuntimeException e) {
                Log.e(TAG, " needSplitActivity parcel err!");
            }
        }
        return split;
    }

    private void disposeFakeActivity(Intent intent) {
        if (this.mIsSecondStageActivity) {
            this.mIsSecondStageActivity = false;
            clearSplittableInfo();
        }
    }

    public void removeCancelFlag(Intent intent) {
        if (intent != null) {
            intent.setHwFlags(intent.getHwFlags() & -9);
        }
    }

    private void removeCancelFlagInner(Intent intent) {
        if (intent != null) {
            intent.setHwFlags(intent.getHwFlags() & -9);
        }
    }

    private boolean needCancelSplit(Intent intent) {
        if (intent == null) {
            return false;
        }
        if ((intent.getHwFlags() & 8) == 0 && !isInUnsplittableList(intent) && (intent.getFlags() & 268435456) == 0) {
            return false;
        }
        return true;
    }

    public static boolean needSplit(Intent intent) {
        if (intent == null || (intent.getHwFlags() & 4) == 0) {
            return false;
        }
        return true;
    }

    public boolean notSupportSplit() {
        return false;
    }

    public void splitActivityIfNeeded() {
        if (reachSplitSize()) {
            splitActivity();
        }
        handleSplitActivityStack(this.mActivity.getIntent());
    }

    private void checkSecondStage(Intent intent) {
        if (intent != null) {
            boolean z = true;
            if ((getSplitModeValue(intent) & 1) == 0) {
                z = false;
            }
            this.mIsSecondStageActivity = z;
            if (!this.mIsSecondStageActivity) {
                this.mIsSecondStageActivity = intent.getBooleanExtra(EXTRA_JUMPED_ACTIVITY, false);
            }
            if (Log.HWLog) {
                Log.i(TAG, "Is second stage ? " + this.mIsSecondStageActivity);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setActionBarButton(boolean enable) {
        View abLayout;
        View up;
        if (isBaseActivityNeedExit() && (abLayout = getActionBarView()) != null && (up = abLayout.findViewById(16909566)) != null) {
            if (this.mUpButtonVisibility < 0) {
                this.mUpButtonVisibility = up.getVisibility();
            }
            if (!enable) {
                this.mActivity.getActionBar().setDisplayHomeAsUpEnabled(false);
            } else if (this.mUpButtonVisibility == 0) {
                this.mActivity.getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void handleSplitActivityStack(Intent intent) {
        if (intent != null) {
            if (this.mIsSecondStageActivity) {
                pushSplitActivityToStack();
                if (this.mSplit && this.mActivity.getComponentName().equals(getTopActivity())) {
                    finishOtherSplitActivity();
                }
            } else if (needSplitActivity(intent) && !this.mIsSplitBaseActivity) {
                pushSplitActivityToStack();
            }
        }
    }

    private ComponentName getTopActivity() {
        List<ActivityManager.RunningTaskInfo> tasks;
        ActivityManager am = (ActivityManager) this.mActivity.getSystemService("activity");
        if (am == null || (tasks = am.getRunningTasks(1)) == null || tasks.isEmpty()) {
            return null;
        }
        return tasks.get(0).topActivity;
    }

    private String getCompName(Intent intent) {
        String compName = null;
        if (intent == null) {
            return null;
        }
        String compName2 = intent.getStringExtra(":settings:show_fragment");
        if (compName2 == null) {
            if (intent.getComponent() != null) {
                compName = intent.getComponent().getClassName();
            }
            return compName;
        } else if (!Log.HWLog) {
            return compName2;
        } else {
            Log.i(TAG, "get fragment name " + compName2);
            return compName2;
        }
    }

    private void pushSplitActivityToStack() {
        checkSplitHandler();
        this.mSplitHandler.sendEmptyMessage(2);
    }

    private void finishOtherSplitActivity() {
        checkSplitHandler();
        this.mSplitHandler.sendEmptyMessageDelayed(1, (long) getDelayTime());
    }

    private int getDelayTime() {
        try {
            int duration = this.mActivity.getResources().getInteger(this.mActivity.getResources().getIdentifier("activity_close_enter_duration", "integer", "androidhwext"));
            if (this.mWindowManager == null) {
                this.mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService(FreezeScreenScene.WINDOW_PARAM));
            }
            return (int) (((float) duration) * this.mWindowManager.getAnimationScale(1));
        } catch (RemoteException e) {
            Log.i(TAG, "throws RemoteException");
            return 0;
        }
    }

    private void checkSplitHandler() {
        if (this.mSplitHandler == null) {
            this.mSplitHandler = new Handler() {
                /* class android.app.HwActivitySplitterImpl.AnonymousClass1 */

                public void handleMessage(Message msg) {
                    boolean z = true;
                    if (msg.what == 1) {
                        boolean isTop = HwActivitySplitterImpl.this.isTopSplitActivity();
                        if (Log.HWLog) {
                            Log.i(HwActivitySplitterImpl.TAG, "Is top ? " + isTop + ", activity is " + HwActivitySplitterImpl.this.mActivity);
                        }
                        if (isTop) {
                            if (HwActivitySplitterImpl.this.mIsSecondStageActivity && !HwActivitySplitterImpl.this.isJumpedActivity()) {
                                if (Log.HWLog) {
                                    Log.w(HwActivitySplitterImpl.TAG, "Try to clear entry stack.");
                                }
                                HwActivitySplitterImpl.this.clearEntryStack((HwActivitySplitterImpl) false);
                            }
                        } else {
                            return;
                        }
                    } else if (msg.what == 2) {
                        try {
                            HwActivitySplitterImpl.this.addToEntryStack(HwActivitySplitterImpl.this.mToken, 0, null);
                        } catch (RemoteException e) {
                            Log.e(HwActivitySplitterImpl.TAG, "addToEntryStack FAIL!");
                        }
                    } else if (msg.what == 5) {
                        Intent subIntent = HwActivitySplitterImpl.this.getCurrentSubIntent();
                        if (subIntent != null && !HwActivitySplitterImpl.this.mAllSubFinished && HwActivitySplitterImpl.this.isBaseActivityNeedExit()) {
                            HwActivitySplitterImpl.this.clearSplittableInfo();
                            try {
                                HwActivitySplitterImpl.this.mActivity.startActivity(subIntent);
                            } catch (Exception e2) {
                                Log.e(HwActivitySplitterImpl.TAG, "launch activity fail!");
                            }
                        }
                    } else if (msg.what == 3) {
                        HwActivitySplitterImpl.this.setActionBarButton(false);
                    } else if (msg.what == 6) {
                        HwActivitySplitterImpl hwActivitySplitterImpl = HwActivitySplitterImpl.this;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        hwActivitySplitterImpl.handleRestartLastContentIfNeeded(z);
                    }
                    super.handleMessage(msg);
                }
            };
        }
    }

    public void onSplitActivityRestart() {
        this.mRestart = true;
    }

    public boolean reachSplitSize() {
        return (!this.mActivity.isInMultiWindowMode() || HwPCUtils.isValidExtDisplayId(this.mActivity)) && HwSplitUtils.isNeedSplit(this.mActivity);
    }

    public void hideAllContent() {
        clearEntryStack(true);
        clearSplittableInfo();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRestartLastContentIfNeeded(boolean fromConfigChange) {
        Intent subIntent;
        if (!this.mIsSplitBaseActivity || (subIntent = getCurrentSubIntent()) == null || (!this.mResumed && !mLastReportedMultiWindowMode)) {
            if (Log.HWLog) {
                Log.i(TAG, "Try to start subintent, return 1");
            }
        } else if (!fromConfigChange || !mLastReportedMultiWindowMode) {
            boolean start = (fromConfigChange || mFirstTimeStart || this.mRestart) && !this.mAllSubFinished;
            if (Log.HWLog) {
                Log.i(TAG, "fromConfigChange ? " + fromConfigChange + ", FirstTime start ? " + mFirstTimeStart + ", mRestart ?" + this.mRestart);
            }
            this.mRestart = false;
            mFirstTimeStart = false;
            if (start) {
                setSecondStageIntent(subIntent);
                if (reachSplitSize()) {
                    clearSplittableInfo();
                    try {
                        this.mActivity.startActivity(subIntent);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "startActivity Fail, ActivityNotFound!");
                    } catch (Exception e2) {
                        setCurrentSubIntent(null);
                        Log.e(TAG, "restartLastContentIfNeeded, startActivity Fail");
                    }
                }
            }
        } else {
            Log.i(TAG, "Try to start subintent return, app not exit multiwindow");
        }
    }

    private void scheduleDelayRestartLastContent(boolean fromConfigChange) {
        checkSplitHandler();
        this.mSplitHandler.sendMessageDelayed(this.mSplitHandler.obtainMessage(6, fromConfigChange ? 1 : 0, 0), 500);
    }

    public void restartLastContentIfNeeded() {
        scheduleDelayRestartLastContent(false);
    }

    private void splitActivity() {
        if (Log.HWLog) {
            Log.i(TAG, "Try to split Activity");
        }
        if (!reachSplitSize()) {
            Log.w(TAG, "Not support split, return");
            return;
        }
        this.mSplit = true;
        if (Log.HWLog) {
            Log.d(TAG, "Begin to split activity.");
        }
        WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
        Parcelable p = this.mActivity.getIntent().getParcelableExtra("huawei.intent.extra.SPLIT_REGION");
        Rect r = null;
        if (p instanceof Rect) {
            r = (Rect) p;
        }
        if (r != null) {
            lp.gravity = 8388659;
            lp.x = r.left;
            lp.y = r.top;
            lp.width = Math.abs(r.right - r.left);
            lp.height = Math.abs(r.bottom - r.top);
            if (Log.HWLog) {
                Log.d(TAG, "Rect left " + r.left + ", right " + r.right + ", top " + r.top + ", bottom " + r.bottom);
            }
        } else {
            if (this.mContentWindowWeight <= 0.0f) {
                this.mContentWindowWeight = getContentWeight();
            }
            lp.gravity = 8388611;
            int winWidth = getCurrentWindowWidth();
            int contentWidth = (int) ((this.mContentWindowWeight * ((float) winWidth)) + 0.5f);
            if (isRtlLocale()) {
                lp.x = 0;
            } else {
                lp.x = winWidth - contentWidth;
            }
            lp.width = contentWidth;
        }
        this.mActivity.getWindow().setAttributes(lp);
        this.mActivity.getWindow().addFlags(32);
    }

    private float getContentWeight() {
        float f = this.mContentWindowWeight;
        if (f > 0.0f) {
            return f;
        }
        int leftWeight = 4;
        int rightWeight = 6;
        if (!HwSplitUtils.IS_FOLDABLE_PHONE) {
            leftWeight = this.mActivity.getResources().getInteger(34275328);
            rightWeight = this.mActivity.getResources().getInteger(34275329);
        }
        this.mContentWindowWeight = (((float) rightWeight) * 1.0f) / ((float) (leftWeight + rightWeight));
        return this.mContentWindowWeight;
    }

    public void adjustWindow(int width) {
        WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
        lp.x = getCurrentWindowWidth() - width;
        lp.width = width;
        if (Log.HWLog) {
            Log.i(TAG, "Adjust content view, set width to " + lp.width);
        }
        this.mActivity.getWindow().setAttributes(lp);
    }

    public void adjustWindow(Rect rect) {
        WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
        lp.gravity = 8388659;
        lp.x = rect.left;
        lp.y = rect.top;
        lp.width = Math.abs(rect.right - rect.left);
        lp.height = Math.abs(rect.bottom - rect.top);
        this.mActivity.getWindow().setAttributes(lp);
    }

    public void adjustToFullScreen() {
        WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
        lp.width = -1;
        lp.height = -1;
        this.mActivity.getWindow().setAttributes(lp);
    }

    public void adjustToSplitScreen() {
        splitActivity();
    }

    private int getCurrentWindowWidth() {
        if (this.mWinSize == null) {
            this.mWinSize = new Point();
        }
        this.mActivity.getWindowManager().getDefaultDisplay().getSize(this.mWinSize);
        if (exitMuitiWindowModeJsutRecent && !this.mIsSplitBaseActivity && oldWindowWidth != 0 && this.mWinSize.x != oldWindowWidth) {
            exitMuitiWindowModeJsutRecent = false;
            oldWindowWidth = this.mWinSize.x;
            Log.i(TAG, "adjustContentIndexView because window width changed!");
            adjustContentIndexView(true);
        }
        if (Log.HWLog) {
            Log.i(TAG, "Get current window width is " + this.mWinSize.x);
        }
        return this.mWinSize.x;
    }

    public void reduceActivity() {
        this.mSplit = false;
        WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
        lp.gravity = 17;
        lp.height = -1;
        lp.width = -1;
        this.mActivity.getWindow().setAttributes(lp);
        lp.x = 0;
        if (this.mIsSecondStageActivity) {
            setActionBarButton(true);
        }
    }

    public boolean checkAllContentGone() {
        if (!this.mIsSplitBaseActivity) {
            return false;
        }
        if (!this.mAllContentGone || this.mAllSubFinished || !isBaseActivityNeedExit() || !reachSplitSize()) {
            this.mAllContentGone = true;
            clearSplittableInfo();
            return false;
        }
        if (Log.HWLog) {
            Log.i(TAG, "AllContentGone.");
        }
        Intent subIntent = getCurrentSubIntent();
        if (subIntent != null) {
            this.mActivity.startActivity(subIntent);
        }
        return true;
    }

    public void onSplitActivityNewIntent(Intent intent) {
        if (this.mAllContentGone) {
            this.mAllContentGone = false;
        }
        if (!this.mSplit || this.mResumed || needSplitActivity(intent)) {
            if (needSplitActivity(intent) && !this.mSplit) {
                splitActivity();
            }
        } else if (needReduceActivity(intent)) {
            reduceActivity();
        }
    }

    private boolean needReduceActivity(Intent intent) {
        if (intent == null) {
            return false;
        }
        Intent curIntent = getCurrentSubIntent();
        if (curIntent == null) {
            return true;
        }
        String comp = getCompName(intent);
        if (comp != null && comp.equals(getCompName(curIntent))) {
            return false;
        }
        String action = intent.getAction();
        if (action == null || !action.equals(curIntent.getAction())) {
            return true;
        }
        return false;
    }

    private void redirectIntent(Intent intent) {
        if (isSplitMode() && "android.settings.SETTINGS".equals(intent.getAction())) {
            intent.setAction("android.settings.WIFI_SETTINGS");
        }
    }

    public boolean isDuplicateSplittableActivity(Intent intent) {
        if (!this.mIsSplitBaseActivity && !isNeedPerformAction()) {
            return true;
        }
        if (intent == null) {
            return false;
        }
        redirectIntent(intent);
        if (this.mIsSplitBaseActivity || this.mIsSecondStageActivity) {
            markJumpedActivity(intent);
        }
        if (!this.mIsSplitBaseActivity || !reachSplitSize()) {
            return false;
        }
        return isLastIntent(intent);
    }

    /* access modifiers changed from: protected */
    public boolean isNeedPerformAction() {
        if (SystemClock.elapsedRealtime() - this.mEventDelayTimeBegin < 500) {
            this.mEventDelayTimeBegin = SystemClock.elapsedRealtime();
            Log.d(TAG, "click is too often, ignored in 500ms");
            return false;
        }
        this.mEventDelayTimeBegin = SystemClock.elapsedRealtime();
        return true;
    }

    private Bundle getIntentBundle(Intent intent) {
        if (intent != null) {
            return intent.getExtras();
        }
        return null;
    }

    private boolean isLastIntent(Intent intent) {
        Bundle bundle;
        if (intent == null) {
            return false;
        }
        Intent lastIntent = null;
        try {
            lastIntent = getIntentInfo(Process.myPid(), true);
        } catch (RemoteException e) {
            Log.e(TAG, "LastSplittableActivity FAIL!");
        }
        if (!intent.filterEquals(lastIntent) || (bundle = getIntentBundle(intent)) == null) {
            return false;
        }
        bundle.remove(EXTRA_SPLIT_BASE_PID);
        Bundle lastBundle = getIntentBundle(lastIntent);
        if (lastBundle == null) {
            return false;
        }
        lastBundle.remove(EXTRA_SPLIT_BASE_PID);
        return String.valueOf(bundle).equals(String.valueOf(lastBundle));
    }

    public void clearIllegalSplitActivity() {
    }

    public void onSplitActivityConfigurationChanged(Configuration newConfig) {
        Handler handler;
        if (Log.HWLog) {
            Log.i(TAG, "In onConfigurationChanged.");
        }
        if (HwPCUtils.isValidExtDisplayId(this.mActivity) && HwPCUtils.enabledInPad()) {
            Log.e(TAG, "In PC mode, split mode Configuration handle terminate.");
        } else if (this.mIsSplitBaseActivity) {
            if (reachSplitSize()) {
                adjustContentIndexView();
                if (Log.HWLog) {
                    Log.i(TAG, "Try to start subintent from onConfigChanged");
                }
                if (isBaseActivityNeedExit()) {
                    scheduleDelayRestartLastContent(true);
                    return;
                }
                return;
            }
            Handler handler2 = this.mSplitHandler;
            if (handler2 != null && handler2.hasMessages(5)) {
                this.mSplitHandler.removeMessages(5);
            }
            reduceIndexView();
        } else if (!needSplitActivity()) {
        } else {
            if (reachSplitSize()) {
                splitActivity();
                return;
            }
            if (this.mActivity.getComponentName().equals(getTopActivity()) && (handler = this.mSplitHandler) != null) {
                handler.removeMessages(1);
                this.mSplitHandler.sendEmptyMessage(1);
            }
            reduceActivity();
        }
    }

    public void setExitWhenContentGone(boolean exit) {
        Intent intent;
        if (exit) {
            intent = setToExitTogether(this.mActivity.getIntent());
        } else {
            intent = setToExitAlone(this.mActivity.getIntent());
        }
        if (intent != null) {
            this.mActivity.setIntent(intent);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isBaseActivityNeedExit() {
        return isBaseActivityNeedExit(this.mActivity.getIntent());
    }

    private boolean isBaseActivityNeedExit(Intent intent) {
        return !(intent != null && (getSplitModeValue(intent) & 2) != 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isJumpedActivity() {
        if (this.mActivity.getIntent() != null) {
            return this.mActivity.getIntent().getBooleanExtra(EXTRA_JUMPED_ACTIVITY, false);
        }
        return false;
    }

    public void handleBackPressed() {
        this.mBackPressed = true;
    }

    private boolean isInUnsplittableList(Intent intent) {
        if (!isUnsplittableAction(intent) && !isUnsplittableCategory(intent) && !isUnsplittablePackage(this.mActivity.getPackageName())) {
            return false;
        }
        if (!Log.HWLog) {
            return true;
        }
        Log.i(TAG, "Unsplittable intent " + intent);
        return true;
    }

    private boolean isUnsplittableAction(Intent intent) {
        String action = intent.getAction();
        boolean isUnsplittable = false;
        if (action == null) {
            return false;
        }
        char c = 65535;
        switch (action.hashCode()) {
            case -1960745709:
                if (action.equals("android.media.action.IMAGE_CAPTURE")) {
                    c = '\b';
                    break;
                }
                break;
            case -1658348509:
                if (action.equals("android.media.action.IMAGE_CAPTURE_SECURE")) {
                    c = '\t';
                    break;
                }
                break;
            case -1329926037:
                if (action.equals(ACTION_CROP_IMAGE)) {
                    c = 4;
                    break;
                }
                break;
            case -1173171990:
                if (action.equals("android.intent.action.VIEW")) {
                    c = 11;
                    break;
                }
                break;
            case -660291826:
                if (action.equals(ACTION_CROP_WALLPAPER)) {
                    c = 3;
                    break;
                }
                break;
            case -229513525:
                if (action.equals("android.intent.action.OPEN_DOCUMENT")) {
                    c = 2;
                    break;
                }
                break;
            case -95176957:
                if (action.equals("android.credentials.INSTALL_AS_USER")) {
                    c = 1;
                    break;
                }
                break;
            case 464109999:
                if (action.equals("android.media.action.STILL_IMAGE_CAMERA")) {
                    c = 5;
                    break;
                }
                break;
            case 485955591:
                if (action.equals("android.media.action.STILL_IMAGE_CAMERA_SECURE")) {
                    c = 6;
                    break;
                }
                break;
            case 701083699:
                if (action.equals("android.media.action.VIDEO_CAPTURE")) {
                    c = '\n';
                    break;
                }
                break;
            case 1130890360:
                if (action.equals("android.media.action.VIDEO_CAMERA")) {
                    c = 7;
                    break;
                }
                break;
            case 1578915466:
                if (action.equals("android.credentials.INSTALL")) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case '\b':
            case '\t':
            case '\n':
                return true;
            case 11:
                if (intent.getType() != null && intent.getType().startsWith(TYPE_FILEMANAGER)) {
                    isUnsplittable = true;
                }
                return isUnsplittable;
            default:
                return false;
        }
    }

    private boolean isUnsplittablePackage(String pkgName) {
        if ("com.huawei.hwid".equals(pkgName) || "com.android.browser".equals(pkgName) || "com.huawei.browser".equals(pkgName)) {
            return true;
        }
        return false;
    }

    private boolean isUnsplittableCategory(Intent intent) {
        if (intent.hasCategory("android.intent.category.HOME") || intent.hasCategory("android.intent.category.APP_BROWSER")) {
            return true;
        }
        return false;
    }

    public static void setNotSplit(Intent intent) {
        if (intent != null) {
            intent.addHwFlags(8);
        }
    }

    public static void setAsJumpActivity(Intent intent) {
        if (intent != null) {
            intent.putExtra(EXTRA_JUMPED_ACTIVITY, true);
        }
    }

    public void handleJumpActivity(Intent intent) {
        this.mIsJumpActivity = true;
        setAsJumpActivity(intent);
    }

    private void markJumpedActivity(Intent intent) {
        if (HwSplitUtils.isJumpedActivity(this.mActivity.getClass().getName(), getCompName(intent))) {
            handleJumpActivity(intent);
        }
    }

    public void setSplittableIfNeeded(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Intent null when set splittable." + this.mActivity);
            return;
        }
        if (Log.HWLog) {
            Log.i(TAG, "Set splittable if needed.");
        }
        this.mAllSubFinished = false;
        if (this.mIsSplitBaseActivity) {
            if (Log.HWLog) {
                Log.i(TAG, "Need split for intent " + intent);
            }
            setSecondStageIntent(intent);
            intent.putExtra(EXTRA_SPLIT_BASE_PID, Process.myPid());
        } else if ((this.mContentWindowWeight > 0.0f || needSplitActivity()) && !needCancelSplit(intent)) {
            if (Log.HWLog) {
                Log.i(TAG, "Set splittable for intent : " + intent);
            }
            deliverExtraInfo(intent);
        }
    }

    private int getBasePid() {
        int i = this.mBasePid;
        if (i > 0) {
            return i;
        }
        if (this.mActivity.getIntent() != null) {
            this.mBasePid = this.mActivity.getIntent().getIntExtra(EXTRA_SPLIT_BASE_PID, -1);
        }
        if (this.mBasePid < 0) {
            this.mBasePid = Process.myPid();
        }
        return this.mBasePid;
    }

    private void deliverExtraInfo(Intent intent) {
        if (intent != null) {
            intent.addHwFlags(4);
            if (this.mActivity.getIntent() != null) {
                if (this.mActivity.getIntent().hasExtra(EXTRA_SPLIT_BASE_PID)) {
                    intent.putExtra(EXTRA_SPLIT_BASE_PID, this.mActivity.getIntent().getIntExtra(EXTRA_SPLIT_BASE_PID, -1));
                }
                if (this.mActivity.getIntent().hasExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE)) {
                    intent.putExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE, this.mActivity.getIntent().getDoubleArrayExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE));
                }
            }
        }
    }

    private void setSecondStageIntent(Intent intent) {
        if (this.mIsSplitBaseActivity && reachSplitSize() && !needCancelSplit(intent)) {
            setLastSplittableActivity(intent);
            setToSecondaryStage(intent);
            if (!isBaseActivityNeedExit()) {
                setToExitAlone(intent);
            }
            deliverExtraInfo(intent);
        }
    }

    private void setBaseActivity() {
        this.mIsSplitBaseActivity = true;
        markBaseActivity(this.mActivity.getIntent());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearSplittableInfo() {
        setLastSplittableActivity(null);
    }

    public void setSplitActivityOrientation(int requestedOrientation) {
    }

    public Intent getCurrentSubIntent() {
        try {
            Intent re = getIntentInfo(Process.myPid(), false);
            if (re != null) {
                if (re.getComponent() != null) {
                    if (this.mIsSplitBaseActivity) {
                        ActivityInfo lastResumedInfo = HwActivityTaskManager.getLastResumedActivity();
                        Log.e(TAG, "getCurrentSubIntent lastResumedInfo:" + lastResumedInfo + ", re.getComponent:" + re.getComponent() + ", mActivity:" + this.mActivity);
                        if (lastResumedInfo == null || (!this.mActivity.getPackageName().equals(lastResumedInfo.packageName) && !re.getComponent().getPackageName().equals(lastResumedInfo.packageName))) {
                            return null;
                        }
                    }
                    return re;
                }
            }
            return re;
        } catch (RemoteException e) {
            Log.e(TAG, "LastSplittableActivity FAIL!");
            return null;
        }
    }

    public void cancelSplit(Intent intent) {
        intent.addHwFlags(8);
    }

    public void setAnimForSplitActivity() {
    }

    public void setResumed(boolean resumed) {
        if (this.mIsSplitBaseActivity || this.mIsSecondStageActivity) {
            if (Log.HWLog) {
                Log.i(TAG, "Set resumed to " + resumed + " for " + this.mActivity);
            }
            this.mResumed = resumed;
            if (this.mIsSplitBaseActivity) {
                if (Log.HWLog) {
                    Log.i(TAG, "setResumed, Base window splitted? " + this.mAllSubFinished);
                }
                if (!resumed || this.mAllSubFinished || !reachSplitSize()) {
                    Handler handler = this.mSplitHandler;
                    if (handler != null && handler.hasMessages(5)) {
                        this.mSplitHandler.removeMessages(5);
                    }
                } else {
                    checkSplitHandler();
                    this.mSplitHandler.sendEmptyMessageDelayed(5, 300);
                }
            }
            if (this.mIsSecondStageActivity && isSplitMode() && isBaseActivityNeedExit()) {
                if (this.mResumed) {
                    this.mIsFinishing = false;
                }
                Handler handler2 = this.mSplitHandler;
                if (handler2 != null) {
                    handler2.sendEmptyMessage(3);
                }
            }
        }
    }

    private void finishAssociatedActivities() {
        if (!this.mIsSecondStageActivity || !isSplitMode() || !isBaseActivityNeedExit() || this.mIsFinishing) {
            Log.w(TAG, "Activity is already finishing : " + this.mActivity);
            return;
        }
        this.mIsFinishing = true;
        if (!isTopSplitActivity() && !this.mBackPressed) {
            return;
        }
        if (isTaskRoot() || isJumpedActivity()) {
            this.mActivity.moveTaskToBack(true);
            return;
        }
        try {
            this.mActivity.finishAffinity();
        } catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void splitActivityFinish() {
        finishAssociatedActivities();
        if (this.mAllContentGone || (this.mIsSecondStageActivity && !isBaseActivityNeedExit() && !this.mIsJumpActivity && !isJumpedActivity())) {
            if (Log.HWLog) {
                Log.e(TAG, "Clear LastActivity.");
            }
            clearSplittableInfo();
        }
        releaseIfNeed();
        if (this.mIsSplitBaseActivity) {
            finishAllSubActivities();
        }
    }

    private boolean isTaskRoot() {
        List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mActivity.getSystemService("activity")).getRunningTasks(1);
        if (tasks == null || tasks.isEmpty()) {
            return false;
        }
        ActivityManager.RunningTaskInfo task = tasks.get(0);
        if (task.baseActivity == null || task.numActivities > 1) {
            return false;
        }
        return true;
    }

    public void finishAllSubActivities() {
        if (this.mIsSplitBaseActivity) {
            clearEntryStack(true);
            clearSplittableInfo();
            this.mAllSubFinished = true;
        }
    }

    public void onSplitActivityDestroy() {
        releaseIfNeed();
    }

    private void releaseIfNeed() {
        if (!this.mRecycled && !isControllerShowing()) {
            this.mRecycled = true;
            if (this.mIsSplitBaseActivity) {
                mInstanceMap.clear();
                setCurrentSubIntent(null);
                setLastSplittableActivity(null);
                resetFirsetTimeStart();
                return;
            }
            mInstanceMap.remove(Integer.valueOf(this.mActivity.hashCode()));
        }
    }

    private static void resetFirsetTimeStart() {
        mFirstTimeStart = true;
    }

    public boolean isRtlLocale() {
        String currentLang = Locale.getDefault().getLanguage();
        if (Log.HWLog) {
            Log.i(TAG, "CurrentLang is " + currentLang);
        }
        return currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw") || currentLang.contains("ur") || currentLang.contains("ug");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTopSplitActivity() {
        try {
            if (isTopSplitActivity(this.mToken)) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Get top activity FAIL!");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearEntryStack(boolean includeSelf) {
        if (includeSelf) {
            try {
                clearEntryStack((IBinder) null);
            } catch (RemoteException e) {
                Log.e(TAG, "clearEntryStack FAIL!");
            }
        } else {
            clearEntryStack(this.mToken);
        }
    }

    public void setSplitDeviceSize(double landSplitLimit, double portSplitLimit) {
        if (this.mActivity.getIntent() != null) {
            this.mActivity.getIntent().putExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE, new double[]{landSplitLimit, portSplitLimit});
        }
    }

    private int getTransCode(String name) {
        try {
            return Class.forName("com.huawei.android.os.HwTransCodeEx").getField(name).getInt(null);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getTransCode : ClassNotFoundException");
            return 0;
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "getTransCode : NoSuchFieldException");
            return 0;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getTransCode : IllegalAccessException");
            return 0;
        } catch (IllegalArgumentException e4) {
            Log.e(TAG, "getTransCode : IllegalArgumentException");
            return 0;
        }
    }

    private void setIntentInfo(Intent intent, int pid, Bundle bundle, boolean forLast) throws RemoteException {
        if (this.mTransCodeSetLast == 0) {
            this.mTransCodeSetLast = getTransCode("SET_LAST_SPLIT_ACTIVITY_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(descriptor);
        data.writeParcelable(intent, 0);
        data.writeInt(pid);
        data.writeBundle(bundle);
        data.writeInt(forLast ? 1 : 0);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeSetLast, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    private Intent getIntentInfo(int pid, boolean forLast) throws RemoteException {
        if (this.mTransCodeGetLast == 0) {
            this.mTransCodeGetLast = getTransCode("GET_LAST_SPLIT_ACTIVITY_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(descriptor);
        data.writeInt(pid);
        data.writeInt(forLast ? 1 : 0);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeGetLast, data, reply, 0);
        reply.readException();
        Parcelable[] p = reply.readParcelableArray(null);
        Intent intent = (Intent) p[0];
        Bundle bundle = (Bundle) p[1];
        if (!(intent == null || bundle == null)) {
            intent.putExtras(bundle);
        }
        data.recycle();
        reply.recycle();
        return intent;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addToEntryStack(IBinder token, int resultCode, Intent resultData) throws RemoteException {
        if (this.mTransCodeAddEntry == 0) {
            this.mTransCodeAddEntry = getTransCode("ADD_TO_ENTRY_STACK_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(descriptor);
        data.writeInt(getBasePid());
        data.writeStrongBinder(token);
        data.writeInt(resultCode);
        data.writeParcelable(resultData, 0);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeAddEntry, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    private void clearEntryStack(IBinder token) throws RemoteException {
        if (this.mTransCodeClearEntry == 0) {
            this.mTransCodeClearEntry = getTransCode("CLEAR_ENTRY_STACK_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(descriptor);
        data.writeInt(getBasePid());
        data.writeStrongBinder(token);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeClearEntry, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    private void removeFromEntryStack(IBinder token) throws RemoteException {
        if (this.mTransCodeRemoveEntry == 0) {
            this.mTransCodeRemoveEntry = getTransCode("REMOVE_FROM_ENTRY_STACK_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(descriptor);
        data.writeInt(getBasePid());
        data.writeStrongBinder(token);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeRemoveEntry, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    private boolean isTopSplitActivity(IBinder token) throws RemoteException {
        if (this.mTransCodeIsTop == 0) {
            this.mTransCodeIsTop = getTransCode("IS_TOP_SPLIT_ACTIVITY_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(descriptor);
        data.writeInt(getBasePid());
        data.writeStrongBinder(token);
        boolean ret = false;
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeIsTop, data, reply, 0);
        reply.readException();
        if (reply.readInt() > 0) {
            ret = true;
        }
        data.recycle();
        reply.recycle();
        return ret;
    }
}
