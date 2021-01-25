package com.huawei.server.wm;

import android.content.Context;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.freeform.HwFreeFormManager;
import android.freeform.HwFreeFormUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Flog;
import com.android.server.wm.WindowManagerInternalEx;
import com.android.server.wm.WindowManagerServiceEx;
import com.android.server.wm.utils.HwDisplaySizeUtilEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityTaskManagerExt;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.sidetouch.HwSideTouchManager;
import com.huawei.singlehandlib.BuildConfig;
import java.util.ArrayList;
import java.util.Iterator;

public class HwSingleHandAdapterImpl implements IHwSingleHandAdapter {
    private static final int DEAL_WITH_SINGLE_HANDLE_DELAY_TIME = 1000;
    private static final int DISMISS_POST_DELAY_TIME = 50;
    private static final boolean IS_NOTCH_PROP = (!SystemPropertiesEx.get("ro.config.hw_notch_size", BuildConfig.FLAVOR).equals(BuildConfig.FLAVOR));
    static final String KEY_SINGLE_HAND_SCREEN_ZOOM = "single_hand_screen_zoom";
    private static final int MSG_DEAL_WITH_SINGLE_HAND = 1;
    private static final int MSG_ENTER_DEFALT_DELAY_TIME = 2000;
    private static final int MSG_ENTER_SINGLEHAND_TIMEOUT = 2;
    private static final int SHOW_POST_DELAY_TIME = 10;
    private static final String SHOW_ROUNDED_CORNERS = "show_rounded_corners";
    static final Object S_LOCK = new Object();
    static final String TAG = "SingleHand";
    private static boolean isSingleHandEnabled = true;
    private static int mRoundedStateFlag = 1;
    private static int mRoundedStateFlagPre = 0;
    private final Context mContext;
    private CoverManager mCoverManager = null;
    private String mCurrentMode = BuildConfig.FLAVOR;
    private DisplayInfoEx mDefaultDisplayInfo;
    private final Handler mHandler;
    private boolean mIsCoverOpen = true;
    private boolean mIsEnterSingleHandWindow2s = false;
    private final ArrayList<SingleHandHandle> mOverlays = new ArrayList<>();
    private final Handler mPaperHandler;
    private final WindowManagerServiceEx mService;
    private Handler mSideTouchHandler = new Handler();
    private IsingleHandInner mSingleHandInner;
    private final Handler mUiHandler;

    public HwSingleHandAdapterImpl(Context context, Handler handler, Handler uiHandler, WindowManagerServiceEx service, IsingleHandInner singleHandInner) {
        this.mHandler = handler;
        this.mContext = context;
        this.mUiHandler = uiHandler;
        this.mService = service;
        this.mDefaultDisplayInfo = service.getDefaultDisplayContentLocked().getDisplayInfoEx();
        this.mSingleHandInner = singleHandInner;
        this.mPaperHandler = new Handler(this.mUiHandler.getLooper()) {
            /* class com.huawei.server.wm.HwSingleHandAdapterImpl.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    HwSingleHandAdapterImpl.this.updateSingleHandModePre();
                } else if (i == 2) {
                    SlogEx.i(HwSingleHandAdapterImpl.TAG, "enter singlehandwindow 2s");
                    synchronized (HwSingleHandAdapterImpl.S_LOCK) {
                        HwSingleHandAdapterImpl.this.mIsEnterSingleHandWindow2s = true;
                    }
                }
            }
        };
    }

    public void registorLocked() {
        Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", BuildConfig.FLAVOR);
        this.mHandler.post(new Runnable() {
            /* class com.huawei.server.wm.HwSingleHandAdapterImpl.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                HwSingleHandAdapterImpl.this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("single_hand_mode"), true, new ContentObserver(HwSingleHandAdapterImpl.this.mHandler) {
                    /* class com.huawei.server.wm.HwSingleHandAdapterImpl.AnonymousClass2.AnonymousClass1 */

                    @Override // android.database.ContentObserver
                    public void onChange(boolean isSelfChange) {
                        HwSingleHandAdapterImpl.this.updateSingleHandModePre();
                    }
                });
                HwSingleHandAdapterImpl.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(HwSingleHandAdapterImpl.KEY_SINGLE_HAND_SCREEN_ZOOM), true, new ContentObserver(HwSingleHandAdapterImpl.this.mHandler) {
                    /* class com.huawei.server.wm.HwSingleHandAdapterImpl.AnonymousClass2.AnonymousClass2 */

                    @Override // android.database.ContentObserver
                    public void onChange(boolean isSelfChange) {
                        boolean z = true;
                        if (SettingsEx.System.getIntForUser(HwSingleHandAdapterImpl.this.mContext.getContentResolver(), HwSingleHandAdapterImpl.KEY_SINGLE_HAND_SCREEN_ZOOM, 1, ActivityManagerEx.getCurrentUser()) != 1) {
                            z = false;
                        }
                        boolean unused = HwSingleHandAdapterImpl.isSingleHandEnabled = z;
                        SlogEx.i(HwSingleHandAdapterImpl.TAG, "singleHandEnabled:" + HwSingleHandAdapterImpl.isSingleHandEnabled);
                    }
                });
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSingleHandModePre() {
        if (this.mSingleHandInner.isDoAnimation()) {
            if (this.mPaperHandler.hasMessages(1)) {
                SlogEx.i(TAG, "drop this message");
                this.mPaperHandler.removeMessages(1);
            }
            this.mPaperHandler.sendEmptyMessageAtTime(1, 1000);
            return;
        }
        updateSingleHandMode();
    }

    private boolean isNotSupportEnterSingleHand(String value) {
        if (HwActivityTaskManager.isPCMultiCastMode() && !BuildConfig.FLAVOR.equals(value)) {
            SlogEx.i(TAG, "single hand mode is disabled in PC multiCastMode");
            this.mService.getAtmServiceEx().onEnteringSingleHandForMultiDisplay();
            return true;
        } else if (!this.mService.getPolicyEx().isKeyguardLocked() || BuildConfig.FLAVOR.equals(value)) {
            return false;
        } else {
            SlogEx.i(TAG, "locked is not support single hand");
            return true;
        }
    }

    private void updateSingleHandMode() {
        Handler handler;
        final String value = Settings.Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        if (!isNotSupportEnterSingleHand(value)) {
            SlogEx.i(TAG, "update singleHandMode to: " + value + " from: " + this.mCurrentMode);
            if (value == null) {
                value = BuildConfig.FLAVOR;
            }
            HwDisplaySizeUtilEx.getInstance(this.mService);
            if (HwDisplaySizeUtilEx.hasSideInScreen() && (handler = this.mSideTouchHandler) != null) {
                handler.post(new Runnable() {
                    /* class com.huawei.server.wm.HwSingleHandAdapterImpl.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        HwSideTouchManager hwSideTouchManager = HwSideTouchManager.getInstance(HwSingleHandAdapterImpl.this.mContext);
                        if (hwSideTouchManager != null) {
                            hwSideTouchManager.updateTPModeForSingleHandeMode(value);
                        }
                    }
                });
            }
            if (IS_NOTCH_PROP) {
                if (this.mCoverManager == null) {
                    this.mCoverManager = new CoverManager();
                }
                CoverManager coverManager = this.mCoverManager;
                if (coverManager != null) {
                    this.mIsCoverOpen = coverManager.isCoverOpen();
                }
                mRoundedStateFlagPre = mRoundedStateFlag;
                mRoundedStateFlag = BuildConfig.FLAVOR.equals(value) ? 1 : 0;
                if (this.mIsCoverOpen && mRoundedStateFlagPre != mRoundedStateFlag) {
                    Settings.Global.putInt(this.mContext.getContentResolver(), SHOW_ROUNDED_CORNERS, mRoundedStateFlag);
                }
            }
            if (!value.equals(this.mCurrentMode)) {
                if (HwFreeFormUtils.isFreeFormEnable()) {
                    HwFreeFormManager.getInstance(this.mContext).removeFloatListView();
                    if (new WindowManagerInternalEx().isStackVisibleLw(5)) {
                        ActivityTaskManagerExt.removeStacksInWindowingModes(new int[]{5});
                    }
                    this.mService.getAtmServiceEx().maximizeHwFreeForm();
                }
                this.mCurrentMode = value;
                synchronized (S_LOCK) {
                    if (!this.mOverlays.isEmpty()) {
                        SlogEx.i(TAG, "Dismissing all single hand window");
                        Iterator<SingleHandHandle> it = this.mOverlays.iterator();
                        while (it.hasNext()) {
                            it.next().dismissLocked();
                        }
                        this.mOverlays.clear();
                        if (!this.mIsEnterSingleHandWindow2s) {
                            Flog.bdReport(43);
                            this.mIsEnterSingleHandWindow2s = false;
                        }
                        Flog.bdReport(42);
                    }
                    if (!BuildConfig.FLAVOR.equals(value)) {
                        Settings.Global.putFloat(this.mService.getContext().getContentResolver(), SingleHandUtils.KEY_SINGLE_HAND_SCREEN_SCALE, 1.0f);
                        boolean isLeft = value.contains("left");
                        this.mOverlays.add(new SingleHandHandle(isLeft));
                        this.mIsEnterSingleHandWindow2s = false;
                        String targetStateName = "false";
                        if (isLeft) {
                            targetStateName = "true";
                        }
                        Flog.bdReport(41, String.format("{state:left_%s}", targetStateName));
                        if (this.mPaperHandler.hasMessages(2)) {
                            this.mPaperHandler.removeMessages(2);
                        }
                        this.mPaperHandler.sendEmptyMessageDelayed(2, 2000);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class SingleHandHandle {
        private final Runnable mDismissRunnable = new Runnable() {
            /* class com.huawei.server.wm.HwSingleHandAdapterImpl.SingleHandHandle.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                SingleHandWindow window = SingleHandHandle.this.mWindow;
                SingleHandHandle.this.mWindow = null;
                if (window != null) {
                    window.dismiss();
                }
            }
        };
        private final Runnable mDismissWindowRunnable = new Runnable() {
            /* class com.huawei.server.wm.HwSingleHandAdapterImpl.SingleHandHandle.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                SingleHandWindow window = SingleHandHandle.this.mWindowWalltop;
                SingleHandHandle.this.mWindowWalltop = null;
                if (window != null) {
                    window.dismiss();
                }
            }
        };
        private final boolean mIsLeft;
        private final Runnable mShowRunnable = new Runnable() {
            /* class com.huawei.server.wm.HwSingleHandAdapterImpl.SingleHandHandle.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                SingleHandWindow window = new SingleHandWindow(SingleHandHandle.this.mIsLeft, "virtual", HwSingleHandAdapterImpl.this.mDefaultDisplayInfo.getLogicalWidth(), HwSingleHandAdapterImpl.this.mDefaultDisplayInfo.getLogicalHeight(), HwSingleHandAdapterImpl.this.mSingleHandInner);
                window.show();
                SingleHandHandle.this.mWindow = window;
            }
        };
        private final Runnable mShowRunnableWalltop = new Runnable() {
            /* class com.huawei.server.wm.HwSingleHandAdapterImpl.SingleHandHandle.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                SingleHandWindow window = new SingleHandWindow(SingleHandHandle.this.mIsLeft, "blurpapertop", HwSingleHandAdapterImpl.this.mDefaultDisplayInfo.getLogicalWidth(), HwSingleHandAdapterImpl.this.mDefaultDisplayInfo.getLogicalHeight(), HwSingleHandAdapterImpl.this.mSingleHandInner);
                window.show();
                SingleHandHandle.this.mWindowWalltop = window;
            }
        };
        private SingleHandWindow mWindow;
        private SingleHandWindow mWindowWalltop;

        SingleHandHandle(boolean isLeft) {
            this.mIsLeft = isLeft;
            synchronized (HwSingleHandAdapterImpl.S_LOCK) {
                HwSingleHandAdapterImpl.this.mUiHandler.post(this.mShowRunnableWalltop);
                HwSingleHandAdapterImpl.this.mUiHandler.postDelayed(this.mShowRunnable, 10);
            }
        }

        public void dismissLocked() {
            HwSingleHandAdapterImpl.this.mUiHandler.removeCallbacks(this.mShowRunnable);
            HwSingleHandAdapterImpl.this.mUiHandler.removeCallbacks(this.mShowRunnableWalltop);
            HwSingleHandAdapterImpl.this.mUiHandler.post(this.mDismissRunnable);
            HwSingleHandAdapterImpl.this.mUiHandler.postDelayed(this.mDismissWindowRunnable, 50);
        }
    }
}
