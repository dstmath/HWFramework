package android.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;
import java.util.HashMap;
import java.util.Locale;

public class HwActivitySplitterImpl implements IHwActivitySplitterImpl {
    private static final String ACTION_AIR_SHARING = "com.huawei.android.airsharing.action.ACTION_DEVICE_SELECTOR";
    private static final String ACTION_CROP_WALLPAPER = "com.android.camera.action.CROP_WALLPAPER";
    private static final int ENSURE_CONTENT_SHOWING_DELAY = 300;
    public static final String EXTRA_JUMPED_ACTIVITY = "huawei.intent.extra.JUMPED_ACTIVITY";
    private static final String EXTRA_SPLIT_MODE = "huawei.extra.splitmode";
    private static final int EXTRA_VALUE_BASE_ACTIVITY = 4;
    private static final int EXTRA_VALUE_EXIT_ALONE = 2;
    private static final int EXTRA_VALUE_FORCE_SPLIT = 8;
    private static final int EXTRA_VALUE_SEC_ACTIVITY = 1;
    private static final int EXTRA_VALUE_SUBINTENT_ONE = 16;
    private static final int FINISH_BACKGROUND_DELAY = 800;
    private static final int MSG_ADD_TO_ENTRY_STACK = 2;
    private static final int MSG_CLEAR_ENTRY_STACK = 1;
    private static final int MSG_SET_ACTIONBAR = 3;
    private static final int MSG_SET_FIRST_DONE = 4;
    private static final int MSG_SET_RESUMED = 5;
    private static final String TAG = "HwActivitySplitterImpl";
    private static final String TYPE_FILEMANAGER = "filemanager.dir";
    private static boolean mControllerShowing;
    private static Intent mCurrentSubIntent;
    private static boolean mFirstTimeStart;
    private static HashMap<Activity, HwActivitySplitterImpl> mInstanceMap;
    private Activity mActivity;
    private boolean mAllContentGone;
    private boolean mAllSubFinished;
    private boolean mBackPressed;
    private View mContentIndexView;
    private float mContentIndexViewWeight;
    private float mContentWindowWeight;
    private boolean mIsJumpActivity;
    private boolean mIsSecondStageActivity;
    private boolean mIsSplitBaseActivity;
    private boolean mRestart;
    private boolean mResumed;
    private boolean mSplit;
    private Handler mSplitHandler;
    private IBinder mToken;
    private int mTransCodeAddEntry;
    private int mTransCodeClearEntry;
    private int mTransCodeGetLast;
    private int mTransCodeIsTop;
    private int mTransCodeRemoveEntry;
    private int mTransCodeSetLast;
    private int mUpButtonVisibility;
    private Point mWinSize;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.HwActivitySplitterImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.HwActivitySplitterImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.HwActivitySplitterImpl.<clinit>():void");
    }

    public void setActivityInfo(Activity a, IBinder token) {
        this.mActivity = a;
        this.mToken = token;
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
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | MSG_CLEAR_ENTRY_STACK);
    }

    private Intent setToExitAlone(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | MSG_ADD_TO_ENTRY_STACK);
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
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | MSG_SET_FIRST_DONE);
    }

    public Intent setToSubIntentOne(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | EXTRA_VALUE_SUBINTENT_ONE);
    }

    public Intent setToForceSplit(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | EXTRA_VALUE_FORCE_SPLIT);
    }

    public HwActivitySplitterImpl(Activity a, boolean isBase) {
        this.mRestart = false;
        this.mSplit = false;
        this.mIsSplitBaseActivity = false;
        this.mIsSecondStageActivity = false;
        this.mAllContentGone = false;
        this.mUpButtonVisibility = -1;
        this.mWinSize = null;
        this.mBackPressed = false;
        this.mIsJumpActivity = false;
        this.mResumed = true;
        this.mAllSubFinished = false;
        this.mTransCodeSetLast = 0;
        this.mTransCodeGetLast = 0;
        this.mTransCodeAddEntry = 0;
        this.mTransCodeClearEntry = 0;
        this.mTransCodeRemoveEntry = 0;
        this.mTransCodeIsTop = 0;
        this.mActivity = a;
        if (isBase) {
            removeCancelFlag(a.getIntent());
        }
    }

    public static HwActivitySplitterImpl getDefault(Activity a) {
        return getDefault(a, false);
    }

    public static HwActivitySplitterImpl getDefault(Activity a, boolean isBase) {
        if (a == null) {
            return null;
        }
        HwActivitySplitterImpl instance = (HwActivitySplitterImpl) mInstanceMap.get(a);
        if (instance != null) {
            return instance;
        }
        if (!isBase && (a.getIntent() == null || (a.getIntent().getHwFlags() & MSG_SET_FIRST_DONE) == 0)) {
            return null;
        }
        instance = new HwActivitySplitterImpl(a, isBase);
        mInstanceMap.put(a, instance);
        return instance;
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
        this.mContentIndexView = contentIndexView;
        this.mContentIndexViewWeight = weight;
        this.mContentWindowWeight = HwFragmentMenuItemView.ALPHA_NORMAL - this.mContentIndexViewWeight;
        adjustContentIndexView();
    }

    public void setSplit(View contentIndexView) {
        setBaseActivity();
        this.mContentIndexView = contentIndexView;
        int leftWeight = this.mActivity.getResources().getInteger(17694888);
        this.mContentIndexViewWeight = (((float) leftWeight) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) (leftWeight + this.mActivity.getResources().getInteger(17694889)));
        this.mContentWindowWeight = HwFragmentMenuItemView.ALPHA_NORMAL - this.mContentIndexViewWeight;
        adjustContentIndexView();
    }

    public void setSplit(float contentWeight) {
        setBaseActivity();
        this.mContentWindowWeight = contentWeight;
    }

    public void setSplit() {
        setBaseActivity();
        int leftWeight = this.mActivity.getResources().getInteger(17694888);
        int rightWeight = this.mActivity.getResources().getInteger(17694889);
        this.mContentWindowWeight = (((float) rightWeight) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) (leftWeight + rightWeight));
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
        if (this.mIsSplitBaseActivity && intent != null && mCurrentSubIntent == null) {
            setSecondStageIntent(intent);
        }
    }

    public void setTargetIntent(Intent intent) {
        setToSecondaryStage(intent);
        setSubIntent(intent);
        if (Log.HWLog) {
            Log.i(TAG, "Init subintent tagert, to " + mCurrentSubIntent);
        }
        intent.addHwFlags(MSG_SET_FIRST_DONE);
        deliverSplitSize(intent);
    }

    private static void setSubIntent(Intent intent) {
        if (intent == null || (intent.getIntExtra(EXTRA_SPLIT_MODE, 0) & EXTRA_VALUE_SUBINTENT_ONE) == 0) {
            mCurrentSubIntent = intent;
        } else {
            mCurrentSubIntent = null;
        }
    }

    private void adjustContentIndexView() {
        if (this.mContentIndexView != null && this.mContentIndexViewWeight > 0.0f && reachSplitSize()) {
            LayoutParams lp = this.mContentIndexView.getLayoutParams();
            lp.width = (int) (this.mContentIndexViewWeight * ((float) getCurrentWindowWidth()));
            if (Log.HWLog) {
                Log.i(TAG, "Adjust content index view, set width to " + lp.width);
            }
            this.mContentIndexView.setLayoutParams(lp);
            adjustActionBar();
        }
    }

    public View getActionBarView() {
        View v = this.mActivity.getWindow().getDecorView();
        View actionBar = v.findViewById(this.mActivity.getResources().getIdentifier("action_bar", "id", "android"));
        if (actionBar == null) {
            return v.findViewById(this.mActivity.getResources().getIdentifier("action_bar", "id", this.mActivity.getPackageName()));
        }
        return actionBar;
    }

    private void adjustActionBar() {
        View abLayout = getActionBarView();
        if (abLayout == null || this.mContentIndexViewWeight <= 0.0f) {
            Log.w(TAG, "Can not get actionbar layout.");
            return;
        }
        LayoutParams lp = abLayout.getLayoutParams();
        lp.width = (int) (this.mContentIndexViewWeight * ((float) getCurrentWindowWidth()));
        abLayout.setLayoutParams(lp);
    }

    public void reduceIndexView() {
        if (this.mContentIndexView != null) {
            LayoutParams lp = this.mContentIndexView.getLayoutParams();
            lp.width = -1;
            this.mContentIndexView.setLayoutParams(lp);
            reduceActionBar();
        }
    }

    private void reduceActionBar() {
        View abLayout = getActionBarView();
        if (abLayout != null) {
            LayoutParams lp = abLayout.getLayoutParams();
            lp.width = -1;
            abLayout.setLayoutParams(lp);
        }
    }

    public boolean needSplitActivity() {
        if (this.mActivity == null) {
            return false;
        }
        return needSplitActivity(this.mActivity.getIntent());
    }

    public boolean needSplitActivity(Intent intent) {
        boolean split = false;
        if (intent != null) {
            split = (intent.getHwFlags() & MSG_SET_FIRST_DONE) != 0 ? needCancelSplit(intent) ? (intent.getIntExtra(EXTRA_SPLIT_MODE, 0) & EXTRA_VALUE_FORCE_SPLIT) != 0 : true : false;
            if (!split) {
                disposeFakeActivity(intent);
            }
        }
        return split;
    }

    private void disposeFakeActivity(Intent intent) {
        checkSecondStage(intent);
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

    private boolean needCancelSplit(Intent intent) {
        if (intent == null) {
            return false;
        }
        boolean isInUnsplittableList;
        if ((intent.getHwFlags() & EXTRA_VALUE_FORCE_SPLIT) == 0) {
            isInUnsplittableList = isInUnsplittableList(intent);
        } else {
            isInUnsplittableList = true;
        }
        return isInUnsplittableList;
    }

    public static boolean needSplit(Intent intent) {
        boolean z = false;
        if (intent == null) {
            return false;
        }
        if ((intent.getHwFlags() & MSG_SET_FIRST_DONE) != 0) {
            z = true;
        }
        return z;
    }

    public boolean notSupportSplit() {
        return false;
    }

    public void splitActivityIfNeeded() {
        checkSecondStage(this.mActivity.getIntent());
        if (reachSplitSize()) {
            splitActivity();
        }
        handleSplitActivityStack(this.mActivity.getIntent());
    }

    private void checkSecondStage(Intent intent) {
        if (intent != null) {
            boolean z;
            if ((getSplitModeValue(intent) & MSG_CLEAR_ENTRY_STACK) != 0) {
                z = true;
            } else {
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

    private void setActionBarButton(boolean enable) {
        if (isBaseActivityNeedExit()) {
            View abLayout = getActionBarView();
            if (abLayout != null) {
                View up = abLayout.findViewById(16908363);
                if (up != null) {
                    if (this.mUpButtonVisibility < 0) {
                        this.mUpButtonVisibility = up.getVisibility();
                    }
                    if (enable) {
                        up.setVisibility(this.mUpButtonVisibility);
                    } else {
                        up.setVisibility(EXTRA_VALUE_FORCE_SPLIT);
                    }
                }
            }
        }
    }

    private void handleSplitActivityStack(Intent intent) {
        if (intent != null) {
            if (this.mIsSecondStageActivity) {
                pushSplitActivityToStack();
                if (this.mSplit) {
                    finishOtherSplitActivity();
                }
                return;
            }
            if (needSplitActivity(intent) && !this.mIsSplitBaseActivity) {
                pushSplitActivityToStack();
            }
        }
    }

    private String getCompName(Intent intent) {
        if (intent == null) {
            return null;
        }
        String compName = intent.getStringExtra(":settings:show_fragment");
        if (compName == null) {
            compName = intent.getComponent() == null ? null : intent.getComponent().getClassName();
        } else if (Log.HWLog) {
            Log.i(TAG, "get fragment name " + compName);
        }
        return compName;
    }

    private void pushSplitActivityToStack() {
        checkSplitHandler();
        this.mSplitHandler.sendEmptyMessage(MSG_ADD_TO_ENTRY_STACK);
    }

    private void finishOtherSplitActivity() {
        checkSplitHandler();
        this.mSplitHandler.sendEmptyMessageDelayed(MSG_CLEAR_ENTRY_STACK, 800);
    }

    private void checkSplitHandler() {
        if (this.mSplitHandler == null) {
            this.mSplitHandler = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == HwActivitySplitterImpl.MSG_CLEAR_ENTRY_STACK) {
                        boolean isTop = HwActivitySplitterImpl.this.isTopSplitActivity();
                        if (Log.HWLog) {
                            Log.i(HwActivitySplitterImpl.TAG, "Is top ? " + isTop + ", activity is " + HwActivitySplitterImpl.this.mActivity);
                        }
                        if (!isTop) {
                            return;
                        }
                        if (HwActivitySplitterImpl.this.mIsSecondStageActivity && !HwActivitySplitterImpl.this.isJumpedActivity()) {
                            if (Log.HWLog) {
                                Log.w(HwActivitySplitterImpl.TAG, "Try to clear entry stack.");
                            }
                            HwActivitySplitterImpl.this.clearEntryStack(false);
                        }
                    } else if (msg.what == HwActivitySplitterImpl.MSG_ADD_TO_ENTRY_STACK) {
                        try {
                            HwActivitySplitterImpl.this.addToEntryStack(HwActivitySplitterImpl.this.mToken, 0, null);
                        } catch (RemoteException e) {
                            Log.e(HwActivitySplitterImpl.TAG, "addToEntryStack FAIL!");
                        }
                    } else if (msg.what == HwActivitySplitterImpl.MSG_SET_RESUMED) {
                        if (!(HwActivitySplitterImpl.mCurrentSubIntent == null || HwActivitySplitterImpl.this.mAllSubFinished || !HwActivitySplitterImpl.this.isBaseActivityNeedExit())) {
                            HwActivitySplitterImpl.this.clearSplittableInfo();
                            HwActivitySplitterImpl.this.mActivity.startActivity(HwActivitySplitterImpl.mCurrentSubIntent);
                        }
                    } else if (msg.what == HwActivitySplitterImpl.MSG_SET_ACTIONBAR) {
                        HwActivitySplitterImpl.this.setActionBarButton(false);
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
        return HwSplitUtils.isNeedSplit(this.mActivity);
    }

    private void restartLastContentIfNeeded(boolean fromConfigChange) {
        if (Log.HWLog) {
            Log.i(TAG, "Try to start subintent, subintent is " + mCurrentSubIntent);
        }
        if (this.mIsSplitBaseActivity && mCurrentSubIntent != null && (this.mResumed || this.mActivity.isInMultiWindowMode())) {
            boolean start = fromConfigChange || ((mFirstTimeStart || this.mRestart) && !this.mAllSubFinished);
            if (Log.HWLog) {
                Log.i(TAG, "fromConfigChange ? " + fromConfigChange + ", FirstTime start ? " + mFirstTimeStart + ", mRestart ?" + this.mRestart);
            }
            this.mRestart = false;
            mFirstTimeStart = false;
            if (start) {
                if (Log.HWLog) {
                    Log.i(TAG, "Start subintent 2, intent is " + mCurrentSubIntent);
                }
                setSecondStageIntent(mCurrentSubIntent);
                if (reachSplitSize()) {
                    clearSplittableInfo();
                    this.mActivity.startActivity(mCurrentSubIntent);
                }
            }
            return;
        }
        if (Log.HWLog) {
            Log.i(TAG, "Try to start subintent, return 1");
        }
    }

    public void restartLastContentIfNeeded() {
        restartLastContentIfNeeded(false);
    }

    private void splitActivity() {
        if (Log.HWLog) {
            Log.i(TAG, "Try to split Activity");
        }
        if (reachSplitSize()) {
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
                int contentWidth = (int) (this.mContentWindowWeight * ((float) winWidth));
                if (isRtlLocale()) {
                    lp.x = 0;
                } else {
                    lp.x = winWidth - contentWidth;
                }
                lp.width = contentWidth;
            }
            this.mActivity.getWindow().setAttributes(lp);
            this.mActivity.getWindow().addFlags(32);
            return;
        }
        Log.w(TAG, "Not support split, return");
    }

    private float getContentWeight() {
        if (this.mContentWindowWeight > 0.0f) {
            return this.mContentWindowWeight;
        }
        int leftWeight = this.mActivity.getResources().getInteger(17694888);
        int rightWeight = this.mActivity.getResources().getInteger(17694889);
        this.mContentWindowWeight = (((float) rightWeight) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) (leftWeight + rightWeight));
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
        if (this.mIsSecondStageActivity) {
            setActionBarButton(true);
        }
    }

    public boolean checkAllContentGone() {
        if (!this.mIsSplitBaseActivity) {
            return false;
        }
        if (this.mAllContentGone && !this.mAllSubFinished && isBaseActivityNeedExit() && reachSplitSize()) {
            if (Log.HWLog) {
                Log.i(TAG, "AllContentGone.");
            }
            if (mCurrentSubIntent != null) {
                this.mActivity.startActivity(mCurrentSubIntent);
            }
            return true;
        }
        this.mAllContentGone = true;
        clearSplittableInfo();
        return false;
    }

    public void onSplitActivityNewIntent(Intent intent) {
        if (this.mAllContentGone) {
            this.mAllContentGone = false;
        }
        if (!this.mSplit || needSplitActivity(intent)) {
            if (needSplitActivity(intent) && !this.mSplit) {
                splitActivity();
            }
        } else if (!isTopSplitActivity()) {
            reduceActivity();
        }
    }

    private void redirectIntent(Intent intent) {
        if (isSplitMode() && "android.settings.SETTINGS".equals(intent.getAction())) {
            intent.setAction("android.settings.WIFI_SETTINGS");
        }
    }

    public boolean isDuplicateSplittableActivity(Intent intent) {
        if (intent == null) {
            return false;
        }
        redirectIntent(intent);
        if (this.mIsSplitBaseActivity || this.mIsSecondStageActivity) {
            markJumpedActivity(intent);
        }
        if (this.mIsSplitBaseActivity && reachSplitSize()) {
            return isLastIntent(intent);
        }
        return false;
    }

    private String getIntentBundle(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                return String.valueOf(extras);
            }
        }
        return null;
    }

    private boolean isLastIntent(Intent intent) {
        try {
            if (isLastSplittableActivity(intent, getIntentBundle(intent))) {
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "LastSplittableActivity FAIL!");
        }
        return false;
    }

    private void recordLastIntentInfo(Intent intent) {
        try {
            setLastSplittableActivity(intent, getIntentBundle(intent));
        } catch (RemoteException e) {
            Log.e(TAG, "setLastSplittableActivity FAIL!");
        }
    }

    public void clearIllegalSplitActivity() {
    }

    public void onSplitActivityConfigurationChanged(Configuration newConfig) {
        if (Log.HWLog) {
            Log.i(TAG, "In onConfigurationChanged.");
        }
        if (this.mIsSplitBaseActivity) {
            if (reachSplitSize()) {
                adjustContentIndexView();
                if (Log.HWLog) {
                    Log.i(TAG, "Try to start subintent from onConfigChanged");
                }
                restartLastContentIfNeeded(true);
                return;
            }
            if (this.mSplitHandler != null && this.mSplitHandler.hasMessages(MSG_SET_RESUMED)) {
                this.mSplitHandler.removeMessages(MSG_SET_RESUMED);
            }
            reduceIndexView();
        } else if (!needSplitActivity()) {
        } else {
            if (reachSplitSize()) {
                splitActivity();
                return;
            }
            if (this.mSplitHandler != null) {
                this.mSplitHandler.removeMessages(MSG_CLEAR_ENTRY_STACK);
                this.mSplitHandler.sendEmptyMessage(MSG_CLEAR_ENTRY_STACK);
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

    private boolean isBaseActivityNeedExit() {
        return isBaseActivityNeedExit(this.mActivity.getIntent());
    }

    private boolean isBaseActivityNeedExit(Intent intent) {
        boolean isBaseNotExit;
        if (intent == null || (getSplitModeValue(intent) & MSG_ADD_TO_ENTRY_STACK) == 0) {
            isBaseNotExit = false;
        } else {
            isBaseNotExit = true;
        }
        return !isBaseNotExit;
    }

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
        if (Log.HWLog) {
            Log.i(TAG, "Unsplittable intent " + intent);
        }
        return true;
    }

    private boolean isUnsplittableAction(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return false;
        }
        boolean isUnsplittable = false;
        if (action.equals("android.credentials.INSTALL") || action.equals("android.intent.action.OPEN_DOCUMENT") || action.equals(ACTION_CROP_WALLPAPER) || action.equals(ACTION_AIR_SHARING) || action.equals("android.media.action.STILL_IMAGE_CAMERA") || action.equals("android.media.action.STILL_IMAGE_CAMERA_SECURE") || action.equals("android.media.action.VIDEO_CAMERA") || action.equals("android.media.action.IMAGE_CAPTURE") || action.equals("android.media.action.IMAGE_CAPTURE_SECURE") || action.equals("android.media.action.VIDEO_CAPTURE")) {
            isUnsplittable = true;
        } else if (action.equals("android.intent.action.VIEW")) {
            if (intent.getType() != null) {
                isUnsplittable = intent.getType().startsWith(TYPE_FILEMANAGER);
            } else {
                isUnsplittable = false;
            }
        }
        return isUnsplittable;
    }

    private boolean isUnsplittablePackage(String pkgName) {
        if ("com.huawei.hwid".equals(pkgName) || "com.android.browser".equals(pkgName)) {
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
            intent.addHwFlags(EXTRA_VALUE_FORCE_SPLIT);
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
        } else if ((this.mContentWindowWeight > 0.0f || needSplitActivity()) && !needCancelSplit(intent)) {
            if (Log.HWLog) {
                Log.i(TAG, "Set splittable for intent : " + intent);
            }
            intent.addHwFlags(MSG_SET_FIRST_DONE);
            deliverSplitSize(intent);
        }
    }

    private void setSecondStageIntent(Intent intent) {
        if (this.mIsSplitBaseActivity && reachSplitSize() && !needCancelSplit(intent)) {
            recordLastIntentInfo(intent);
            setToSecondaryStage(intent);
            if (!isBaseActivityNeedExit()) {
                setToExitAlone(intent);
            }
            setSubIntent(intent);
            if (Log.HWLog) {
                Log.i(TAG, "Init subintent second, to " + mCurrentSubIntent);
            }
            intent.addHwFlags(MSG_SET_FIRST_DONE);
            deliverSplitSize(intent);
        }
    }

    private void setBaseActivity() {
        this.mIsSplitBaseActivity = true;
        markBaseActivity(this.mActivity.getIntent());
    }

    private void clearSplittableInfo() {
        try {
            setLastSplittableActivity(null, null);
        } catch (RemoteException e) {
            Log.e(TAG, "setLastSplittableActivity FAIL!");
        }
    }

    public void setSplitActivityOrientation(int requestedOrientation) {
    }

    public Intent getCurrentSubIntent() {
        return mCurrentSubIntent;
    }

    public void cancelSplit(Intent intent) {
        intent.addHwFlags(EXTRA_VALUE_FORCE_SPLIT);
    }

    public void setAnimForSplitActivity() {
    }

    public void setResumed(boolean resumed) {
        if (this.mIsSplitBaseActivity || this.mIsSecondStageActivity) {
            if (Log.HWLog) {
                Log.i(TAG, "Set resumed to " + resumed + " for " + this.mActivity);
            }
            this.mResumed = resumed;
            if (!this.mResumed) {
                checkSplitHandler();
                if (this.mSplitHandler.hasMessages(MSG_CLEAR_ENTRY_STACK)) {
                    this.mSplitHandler.removeMessages(MSG_CLEAR_ENTRY_STACK);
                }
            }
            if (this.mIsSplitBaseActivity) {
                if (Log.HWLog) {
                    Log.i(TAG, "setResumed, Base window splitted? " + this.mAllSubFinished);
                }
                if (resumed && !this.mAllSubFinished && reachSplitSize()) {
                    checkSplitHandler();
                    this.mSplitHandler.sendEmptyMessageDelayed(MSG_SET_RESUMED, 300);
                } else if (this.mSplitHandler != null && this.mSplitHandler.hasMessages(MSG_SET_RESUMED)) {
                    this.mSplitHandler.removeMessages(MSG_SET_RESUMED);
                }
            }
            if (this.mIsSecondStageActivity && isSplitMode() && isBaseActivityNeedExit() && this.mSplitHandler != null) {
                this.mSplitHandler.sendEmptyMessage(MSG_SET_ACTIONBAR);
            }
        }
    }

    public void splitActivityFinish() {
        if (this.mIsSecondStageActivity && isSplitMode() && isBaseActivityNeedExit() && (isTopSplitActivity() || this.mBackPressed)) {
            this.mActivity.moveTaskToBack(true);
        }
        if (this.mAllContentGone || !(!this.mIsSecondStageActivity || isBaseActivityNeedExit() || this.mIsJumpActivity)) {
            if (Log.HWLog) {
                Log.e(TAG, "Clear LastActivity.");
            }
            clearSplittableInfo();
        }
    }

    public void finishAllSubActivities() {
        if (this.mIsSplitBaseActivity) {
            clearEntryStack(true);
            clearSplittableInfo();
            this.mAllSubFinished = true;
        }
    }

    public void onSplitActivityDestroy() {
        if (this.mIsSplitBaseActivity) {
            mInstanceMap.clear();
            clearSplittableInfo();
        } else {
            mInstanceMap.remove(this.mActivity);
        }
        if (this.mIsSplitBaseActivity && !isControllerShowing()) {
            resetFirsetTimeStart();
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
        if (currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw")) {
            return true;
        }
        return currentLang.contains("ur");
    }

    private boolean isTopSplitActivity() {
        try {
            if (isTopSplitActivity(this.mToken)) {
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Get top activity FAIL!");
        }
        return false;
    }

    private void clearEntryStack(boolean includeSelf) {
        if (includeSelf) {
            try {
                clearEntryStack(null);
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "clearEntryStack FAIL!");
                return;
            }
        }
        clearEntryStack(this.mToken);
    }

    private void deliverSplitSize(Intent intent) {
        double[] dArr = null;
        if (intent != null && this.mActivity.getIntent() != null && this.mActivity.getIntent().hasExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE)) {
            String str = HwSplitUtils.EXTRAS_HWSPLIT_SIZE;
            if (this.mActivity.getIntent() != null) {
                dArr = this.mActivity.getIntent().getDoubleArrayExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE);
            }
            intent.putExtra(str, dArr);
        }
    }

    public void setSplitDeviceSize(double landSplitLimit, double portSplitLimit) {
        if (this.mActivity.getIntent() != null) {
            Intent intent = this.mActivity.getIntent();
            String str = HwSplitUtils.EXTRAS_HWSPLIT_SIZE;
            double[] dArr = new double[MSG_ADD_TO_ENTRY_STACK];
            dArr[0] = landSplitLimit;
            dArr[MSG_CLEAR_ENTRY_STACK] = portSplitLimit;
            intent.putExtra(str, dArr);
        }
    }

    private int getTransCode(String name) {
        int transCode = 0;
        try {
            transCode = Class.forName("com.huawei.android.os.HwTransCodeEx").getField(name).getInt(null);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getTransCode : ClassNotFoundException");
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "getTransCode : NoSuchFieldException");
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getTransCode : IllegalAccessException");
        } catch (IllegalArgumentException e4) {
            Log.e(TAG, "getTransCode : IllegalArgumentException");
        }
        return transCode;
    }

    public void setLastSplittableActivity(Intent intent, String extras) throws RemoteException {
        if (this.mTransCodeSetLast == 0) {
            this.mTransCodeSetLast = getTransCode("SET_LAST_SPLIT_ACTIVITY_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeParcelable(intent, 0);
        data.writeString(extras);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeSetLast, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean isLastSplittableActivity(Intent intent, String extras) throws RemoteException {
        if (this.mTransCodeGetLast == 0) {
            this.mTransCodeGetLast = getTransCode("GET_LAST_SPLIT_ACTIVITY_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeParcelable(intent, 0);
        data.writeString(extras);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeGetLast, data, reply, 0);
        reply.readException();
        boolean ret = reply.readInt() > 0;
        data.recycle();
        reply.recycle();
        return ret;
    }

    public void addToEntryStack(IBinder token, int resultCode, Intent resultData) throws RemoteException {
        if (this.mTransCodeAddEntry == 0) {
            this.mTransCodeAddEntry = getTransCode("ADD_TO_ENTRY_STACK_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeStrongBinder(token);
        data.writeInt(resultCode);
        data.writeParcelable(resultData, 0);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeAddEntry, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void clearEntryStack(IBinder token) throws RemoteException {
        if (this.mTransCodeClearEntry == 0) {
            this.mTransCodeClearEntry = getTransCode("CLEAR_ENTRY_STACK_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeStrongBinder(token);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeClearEntry, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public void removeFromEntryStack(IBinder token) throws RemoteException {
        if (this.mTransCodeRemoveEntry == 0) {
            this.mTransCodeRemoveEntry = getTransCode("REMOVE_FROM_ENTRY_STACK_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeStrongBinder(token);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeRemoveEntry, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    public boolean isTopSplitActivity(IBinder token) throws RemoteException {
        if (this.mTransCodeIsTop == 0) {
            this.mTransCodeIsTop = getTransCode("IS_TOP_SPLIT_ACTIVITY_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeStrongBinder(token);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeIsTop, data, reply, 0);
        reply.readException();
        boolean ret = reply.readInt() > 0;
        data.recycle();
        reply.recycle();
        return ret;
    }
}
