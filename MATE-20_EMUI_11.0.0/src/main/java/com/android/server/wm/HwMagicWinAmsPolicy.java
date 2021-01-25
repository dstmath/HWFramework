package com.android.server.wm;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.HwMwUtils;
import android.view.WindowManager;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.wm.ActivityStackEx;
import com.android.server.wm.HwMagicWinModulePolicy;
import com.huawei.android.app.ActivityOptionsEx;
import com.huawei.android.app.RecentTaskInfoEx;
import com.huawei.android.app.WindowConfigurationEx;
import com.huawei.android.app.servertransaction.PauseActivityItemEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.magicwin.HwMagicWinStatistics;
import com.huawei.server.magicwin.HwMagicWindowConfig;
import com.huawei.server.utils.SharedParameters;
import com.huawei.server.utils.Utils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class HwMagicWinAmsPolicy extends HwMagicWinModulePolicy.ModulePolicy {
    private static final int ADJ_WIN_DELAY_TIME = 100;
    private static final String BAIDU_HOMEWORK_INITACTIVITY = "com.baidu.homework.activity.init.InitActivity";
    private static final String BAIDU_PACKAGE_NAME = "com.baidu.searchbox";
    private static final int DEAFAULT_PROCESS_UID = -1;
    public static final String DEVICE_ADMIN_ACTIVITY = "com.android.settings.DeviceAdminAdd";
    public static final String DOCUMENTUI_PACKAGENAME = "com.android.documentsui";
    private static final String DUOWAN_PACKAGE_NAME = "com.duowan.mobile";
    public static final String FINISH_REASON_CRASH = "force-crash";
    private static final int FRAME_POINT_DIVISOR = 4;
    private static final int FRAME_SIZE_DIVISOR = 2;
    private static final boolean IS_DEFAULT_LAND_DEVICE;
    private static final String JINGDONG_PACKAGE_NAME = "com.jingdong.app.mall";
    private static final HashSet<String> LEFT_RESUME = new HashSet<>();
    public static final String MAGIC_WINDOW_FINISH_EVENT = "activity finish for magicwindow";
    private static final boolean MAIN_RELATED_ENABLE = true;
    private static final int NUM_ACTIVITY_SIZE = 2;
    private static final int NUM_MAX_TASKS = 100;
    private static final int PARAM_INDEX_FIVE = 5;
    private static final int PARAM_INDEX_FOUR = 4;
    private static final int PARAM_INDEX_ONE = 1;
    private static final int PARAM_INDEX_THREE = 3;
    private static final int PARAM_INDEX_TWO = 2;
    private static final int PARAM_INDEX_ZERO = 0;
    private static final int PARAM_NUM_PROCESS_ARGS = 4;
    private static final String PEIYIN_PACKAGE_NAME = "com.ishowedu.peiyin";
    public static final String PERMISSION_ACTIVITY = "com.android.packageinstaller.permission.ui.GrantPermissionsActivity";
    private static final String PERMISSION_PACKAGENAME = "com.android.permissioncontroller";
    public static final boolean PRESERVE_WINDOWS = true;
    private static final HashSet<String> PRE_DEFINED_FULLSCREEN_PACKAGES = new HashSet<>();
    private static final int SIZE_SHOULD_CALL_IDLE = 2;
    private static final String SYSTEM_MANAGER_PACKAGE_NAME = "com.huawei.systemmanager";
    private static final String TAG = "HWMW_HwMagicWinAmsPolicy";
    private static final String TAMLL_TMEMPTYACTIVITY = "com.tmall.wireless.common.navigator.TMEmptyActivity";
    private static final String TAOBAO_SHOPURLROUTERACTIVITY = "com.taobao.android.shop.activity.ShopUrlRouterActivity";
    private static final String TOUTIAO_PACKAGE_NAME = "com.ss.android.article.news";
    private static final HashSet<String> TRANSITION_ACTIVITIES = new HashSet<>();
    private static final String WECHAT_APPBRANDPROXYACTIVITY = "com.tencent.mm.plugin.appbrand.launching.AppBrandLaunchProxyUI";
    private static final String WELINK_PACKAGE_NAME = "com.huawei.works";
    private static final String WELINK_W3SPLASHACTIVITY = "huawei.w3.ui.welcome.W3SplashScreenActivity";
    private static final String XINLANG_NEWS_PERMISSIONACTIVITY = "com.sina.news.module.base.permission.PermissionActivity";
    private HwMagicWinModulePolicy.IPolicyOperation canPauseInHwMultiwin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$cq6hErdApFHSIVlRWDCOblQ_A */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$39$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation changeOrientationForMultiWin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$1DKEoqaYEMBvF3q_G6SH4BbGROY */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$41$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation checkMagicOrientation = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$jlrbBRXESYKYC2AsRa4Z51pigw4 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$15$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation clearTask = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$33MJujGP7rM32ZzRCdezJvJFuo */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$33$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation dumpMgc = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$jvN6upI3FcmudUGneB6W6burNZo */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$4$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation finishActivityForHwMagicWin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$zgaJebjRUnONfbAEkALg1UWqCyo */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$28$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation getDetectedParam = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$ideJDXuZz7SHDhzxCoPbECugJ1Q */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$3$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation isActivityFullscreen = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$9wgkv92FOg0cjFDhlJyp0ZQxo14 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$1$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation isDisableUpsideDownRotation = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$q1rgMkZ6YWG7wFTJt3NtSUxiRqE */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$11$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation isInAppSplite = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$e60KdyVqIw9o95buFmMGhzjoRtw */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$16$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicModeBase mA1AnMode;
    private ActivityTaskManagerServiceEx mActivityTaskManager;
    private ActivityManagerServiceEx mAms;
    private HwMagicModeBase mAnAnMode;
    private HwMagicModeBase mBaseMode;
    private Context mContext = null;
    public final Rect mDefaultFullScreenBounds = new Rect();
    private DisplayMetrics mDm = null;
    private HomePageDetect mHomeDetect;
    private int mLastDensityDpi;
    public HwMagicWinSplitManager mMagicWinSplitMng;
    public HwMagicModeSwitcher mModeSwitcher;
    private HwMagicWinManager mMwManager = null;
    private HwMagicModeBase mOpenMode;
    public OrientationPolicy mOrientationPolicy;
    private HwMagicWinModulePolicy.IPolicyOperation moveLatestActivityToTop = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$eGzOitEZYFFslTckpekgwKyxX0Y */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$13$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation moveMwToHwMultiStack = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$txSJPfQInksXEpWF3UVA2WE4sfg */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$10$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation moveStackToDisplay = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$849vP5Uxi1DMs4p1AvlHCOj_shw */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$14$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation onBackPressed = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$82Zj4tzKLDZda3v4OMMEI9RJxxw */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$18$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation onProcessDied = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$_QIkd2ZzKwbUZ6nG3De1ektlHc8 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$0$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation overrideArgsForHwMagicWin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$284c0pNZg6S4rzFzj39ubkLPo */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$26$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation overrideConfigInMagicWin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$KZM62JhTS10mo9sJgzBnl8FLXgo */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$6$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation overrideIntentFlagForHwMagicWin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$pbKZU7WZIteambPXezhJMWUru4 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$31$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation overrideIntentForHwMagicWin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$rXTb4jzNInhaRoskHu51tZb7kw */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$32$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation processHwMultiStack = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$Lm4cwXJ74nOLImSVJgqBykbziM */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$9$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation processSpliteScreenForMutilWin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$1Ni8aFXXwokB0GoSmXKPmy1xQfQ */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$8$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation putDetectedResult = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$fd8Haosxi_xTvNBXbN77C6ZU7M */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$5$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation resetTaskWindowMode = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$ZfdXwVfkUO8AFp98_KFp0N2wxM8 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$2$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation resizeForDrag = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$tSNplW1HnyMR_NMfohs7XBhNxuQ */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$40$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation resizeSpecialVideoInMagicWindowMode = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$XN7fYN0V9xF94ewzKYPMWNqoaeo */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$34$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation resizeSplitStackBeforeResume = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$v4qjkEHNF3mY9yUJE_QLLCVav0 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$24$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation resizeWhenMovebackIfNeed = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$4lo4RJhLier9Res6UTRnLLBa3Q */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$19$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation resumeActivityForHwMagicWin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$3DqYh2gesYt1widU2Ga6719f4ik */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$27$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation switchFocusIfNeeded = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$qcwULY_JybGNNSQRmLvWOVfihZ0 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$17$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updateFocusActivity = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$QRV0DGjR4_JpRsqIDvJyfKI_8kw */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$7$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updateMagicWindowConfiguration = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$aA1GjVe2CRtWhqrvp3K50P0KG80 */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$25$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updateRequestedOrientation = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$2EGpHIQYcFo3Z0sBjLRDEDUhWvg */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$43$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updateSensorRotation = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$n4dP35XYbbon5I83Qcr2ccFNT_s */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$42$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updateStackVisibilitySplitMode = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$1KrCqEA2e5JZHcfnnPyBUPEUuM */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$12$HwMagicWinAmsPolicy(list, bundle);
        }
    };
    private HwMagicWinModulePolicy.IPolicyOperation updateVisibilityForHwMagicWin = new HwMagicWinModulePolicy.IPolicyOperation() {
        /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$GYc2Vue0mZRR8Q0EozwzazQiEwg */

        @Override // com.android.server.wm.HwMagicWinModulePolicy.IPolicyOperation
        public final void execute(List list, Bundle bundle) {
            HwMagicWinAmsPolicy.this.lambda$new$29$HwMagicWinAmsPolicy(list, bundle);
        }
    };

    static {
        boolean z = false;
        if (SystemPropertiesEx.getInt("ro.panel.hw_orientation", 0) == 90) {
            z = true;
        }
        IS_DEFAULT_LAND_DEVICE = z;
        TRANSITION_ACTIVITIES.add(TAOBAO_SHOPURLROUTERACTIVITY);
        TRANSITION_ACTIVITIES.add(TAMLL_TMEMPTYACTIVITY);
        TRANSITION_ACTIVITIES.add(XINLANG_NEWS_PERMISSIONACTIVITY);
        TRANSITION_ACTIVITIES.add(BAIDU_HOMEWORK_INITACTIVITY);
        TRANSITION_ACTIVITIES.add(WELINK_W3SPLASHACTIVITY);
        TRANSITION_ACTIVITIES.add(WECHAT_APPBRANDPROXYACTIVITY);
        LEFT_RESUME.add(WELINK_PACKAGE_NAME);
        LEFT_RESUME.add(JINGDONG_PACKAGE_NAME);
        LEFT_RESUME.add(TOUTIAO_PACKAGE_NAME);
        LEFT_RESUME.add(DUOWAN_PACKAGE_NAME);
        LEFT_RESUME.add(BAIDU_PACKAGE_NAME);
        LEFT_RESUME.add(PEIYIN_PACKAGE_NAME);
        PRE_DEFINED_FULLSCREEN_PACKAGES.add(SYSTEM_MANAGER_PACKAGE_NAME);
    }

    public HwMagicWinAmsPolicy(SharedParameters parameters) {
        this.mMwManager = parameters.getMwWinManager();
        this.mContext = parameters.getContext();
        this.mAms = parameters.getAms();
        this.mActivityTaskManager = this.mAms.getActivityTaskManagerEx();
        this.mBaseMode = new HwMagicModeBase(this.mMwManager, this, this.mContext);
        this.mA1AnMode = new HwMagicModeA1An(this.mMwManager, this, this.mContext);
        this.mAnAnMode = new HwMagicModeAnAn(this.mMwManager, this, this.mContext);
        this.mOpenMode = new HwMagicModeOpen(this.mMwManager, this, this.mContext);
        this.mMagicWinSplitMng = new HwMagicWinSplitManager(parameters.getAms(), this.mMwManager, this);
        this.mModeSwitcher = new HwMagicModeSwitcher(this, this.mMwManager, parameters.getAms());
        this.mHomeDetect = new HomePageDetect(parameters);
        this.mOrientationPolicy = new OrientationPolicy(parameters.getAms(), this.mMwManager, this);
        this.mDm = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(this.mDm);
        this.mLastDensityDpi = this.mDm.densityDpi;
        initPolicy();
    }

    public HwMagicModeBase getMode(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container == null) {
            return this.mBaseMode;
        }
        int windowMode = container.getConfig().getWindowMode(Utils.getRealPkgName(activity));
        if (windowMode == 1) {
            return this.mA1AnMode;
        }
        if (windowMode == 2) {
            return this.mAnAnMode;
        }
        if (windowMode != 3) {
            return this.mBaseMode;
        }
        return this.mOpenMode;
    }

    public HwMagicModeBase getBaseMode() {
        return this.mBaseMode;
    }

    private void initPolicy() {
        addPolicy(0, this.overrideIntentForHwMagicWin, IBinder.class, Object.class, IBinder.class, Boolean.class, ActivityOptions.class);
        addPolicy(1, this.overrideIntentFlagForHwMagicWin, Object.class, ActivityOptions.class, Object.class, Object.class);
        addPolicy(2, this.overrideArgsForHwMagicWin, ApplicationInfo.class, Boolean.class, String[].class);
        addPolicy(51, this.resumeActivityForHwMagicWin, IBinder.class, String.class);
        addPolicy(52, this.finishActivityForHwMagicWin, IBinder.class, Boolean.class, Rect.class, Rect.class, String.class);
        addPolicy(3, this.updateVisibilityForHwMagicWin, IBinder.class, IBinder.class, Boolean.class, int[].class);
        addPolicy(10, this.processSpliteScreenForMutilWin, Integer.class, Boolean.class, Integer.class);
        addPolicy(41, this.resizeSpecialVideoInMagicWindowMode, IBinder.class, Integer.class);
        addPolicy(9, this.checkMagicOrientation, IBinder.class);
        addPolicy(4, this.updateMagicWindowConfiguration, Integer.class, Integer.class, IBinder.class);
        addPolicy(5, this.updateFocusActivity, IBinder.class);
        addPolicy(6, this.overrideConfigInMagicWin, Configuration.class);
        addPolicy(7, this.clearTask, IBinder.class, Integer.class, IBinder.class);
        addPolicy(31, this.getDetectedParam, String.class);
        addPolicy(32, this.putDetectedResult, IBinder.class);
        addPolicy(61, this.isDisableUpsideDownRotation, Integer.class);
        addPolicy(13, this.moveLatestActivityToTop, Boolean.class, Boolean.class, Integer.class, Integer.class, Integer.class, Integer.class);
        addPolicy(22, this.moveStackToDisplay, Integer.class, Integer.class);
        addPolicy(14, this.canPauseInHwMultiwin, IBinder.class, IBinder.class);
        addPolicy(15, this.resetTaskWindowMode, Object.class);
        addPolicy(16, this.processHwMultiStack, Integer.class);
        addPolicy(17, this.moveMwToHwMultiStack, Integer.class, Rect.class);
        addPolicy(62, this.resizeForDrag, Object.class, Rect.class, Rect.class, Integer.class);
        addPolicy(80, this.changeOrientationForMultiWin, Configuration.class, Float.class, Object.class);
        addPolicy(28, this.onBackPressed, IBinder.class);
        addPolicy(135, this.updateStackVisibilitySplitMode, Integer.class);
        addPolicy(132, this.isInAppSplite, Integer.class, Boolean.class);
        addPolicy(18, this.onProcessDied, Integer.class, Integer.class, String.class);
        addPolicy(133, this.resizeWhenMovebackIfNeed, Integer.class);
        addPolicy(136, this.resizeSplitStackBeforeResume, Object.class);
        addPolicy(137, this.switchFocusIfNeeded, Integer.class, Integer.class);
        addPolicy(108, this.isActivityFullscreen, Object.class);
        addPolicy(23, this.updateSensorRotation, Integer.class);
        addPolicy(24, this.updateRequestedOrientation, Integer.class);
        addPolicy(100, this.dumpMgc, PrintWriter.class, String.class);
    }

    public boolean isStackInHwMagicWindowMode(HwMagicContainer container) {
        ActivityStackEx focusedStack = getFocusedTopStack(container);
        if (focusedStack == null) {
            return false;
        }
        return focusedStack.inHwMagicWindowingMode();
    }

    public boolean isSupportMainRelatedMode(HwMagicContainer container, ActivityRecordEx activity) {
        return container != null && isSupportMainRelatedMode(container, Utils.getPackageName(activity));
    }

    private boolean isSupportMainRelatedMode(HwMagicContainer container, String pkgName) {
        String relateAct = container.getConfig().getRelateActivity(pkgName);
        List<String> mainActs = container.getConfig().getMainActivity(pkgName);
        return mainActs != null && mainActs.size() > 0 && !TextUtils.isEmpty(relateAct);
    }

    public boolean isMainActivity(HwMagicContainer container, ActivityRecordEx activity) {
        if (container == null) {
            return false;
        }
        List<String> mainActName = container.getConfig().getMainActivity(Utils.getPackageName(activity));
        String className = Utils.getClassName(activity);
        if (mainActName == null || !mainActName.contains(className) || TextUtils.isEmpty(container.getConfig().getRelateActivity(Utils.getPackageName(activity)))) {
            return false;
        }
        return true;
    }

    public boolean isRelatedActivity(HwMagicContainer container, ActivityRecordEx activity) {
        if (container == null) {
            return false;
        }
        String realteActName = container.getConfig().getRelateActivity(Utils.getPackageName(activity));
        String className = Utils.getClassName(activity);
        if (realteActName.isEmpty() || !realteActName.equals(className)) {
            return false;
        }
        return true;
    }

    public String getFocusedStackPackageName(HwMagicContainer container) {
        String realPkgName;
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            ActivityStackEx focusedStackEx = getFocusedTopStack(container);
            realPkgName = focusedStackEx == null ? null : Utils.getRealPkgName(focusedStackEx.getTopActivity());
        }
        return realPkgName;
    }

    public /* synthetic */ void lambda$new$0$HwMagicWinAmsPolicy(List params, Bundle result) {
        ArrayList<ActivityRecordEx> allActivities;
        int userId;
        String pkg = (String) params.get(2);
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null && container.getConfig().isSupportAppTaskSplitScreen(pkg)) {
            boolean z = false;
            int uid = ((Integer) params.get(0)).intValue();
            int pid = ((Integer) params.get(1)).intValue();
            synchronized (this.mActivityTaskManager.getGlobalLock()) {
                WindowProcessControllerEx processControllerEx = this.mActivityTaskManager.getProcessControllerEx(pid, uid);
                StringBuilder sb = new StringBuilder();
                sb.append("ProcessDied pid ");
                sb.append(pid);
                sb.append(" uid ");
                sb.append(uid);
                sb.append(" process exsit ");
                if (processControllerEx != null) {
                    z = true;
                }
                sb.append(z);
                SlogEx.i(TAG, sb.toString());
                if (processControllerEx != null) {
                    userId = processControllerEx.getUserId();
                    allActivities = processControllerEx.getRunningActivitys();
                } else {
                    userId = UserHandleEx.getUserId(uid);
                    allActivities = getAllActivities(this.mMagicWinSplitMng.getMainActivityStack(pkg, userId));
                }
                this.mMagicWinSplitMng.removeReportLoginStatus(getJoinStr(pkg, userId));
                if (allActivities != null) {
                    Iterator<ActivityRecordEx> it = allActivities.iterator();
                    while (it.hasNext()) {
                        ActivityRecordEx ar = it.next();
                        HwMagicContainer inputContainer = this.mMwManager.getContainer(ar);
                        if (isRelatedActivity(inputContainer, ar)) {
                            ar.makeFinishingLocked();
                        } else if (isMainActivity(inputContainer, ar)) {
                            ar.setHaveState(true);
                        }
                    }
                }
            }
        }
    }

    public /* synthetic */ void lambda$new$1$HwMagicWinAmsPolicy(List params, Bundle result) {
        ActivityRecordEx ar = new ActivityRecordEx();
        ar.resetActivityRecord(params.get(0));
        result.putBoolean("ACTIVITY_FULLSCREEN", isFullScreenActivity(ar));
    }

    public /* synthetic */ void lambda$new$2$HwMagicWinAmsPolicy(List params, Bundle result) {
        SlogEx.i(TAG, "### Execute -> reset Task Window Mode To UNDEFINED");
        TaskRecordEx task = new TaskRecordEx();
        task.resetTaskRecord(params.get(0));
        HwMagicContainer container = this.mMwManager.getContainer(task.getTopActivity());
        if (container != null && task.getRealActivity() != null) {
            String packageName = task.getRealActivity().getPackageName();
            if (container.getHwMagicWinEnabled(packageName)) {
                task.setWindowingMode(0);
                if (this.mMagicWinSplitMng.isMainStack(packageName, task.getStack())) {
                    this.mMagicWinSplitMng.removeReportLoginStatus(getJoinStr(packageName, task.getUserId()));
                }
            }
        }
    }

    public /* synthetic */ void lambda$new$3$HwMagicWinAmsPolicy(List params, Bundle result) {
        this.mHomeDetect.getDetectedParam((String) params.get(0), result);
    }

    public /* synthetic */ void lambda$new$4$HwMagicWinAmsPolicy(List params, Bundle result) {
        this.mMwManager.dumpAppConfig((PrintWriter) params.get(0), (String) params.get(1));
    }

    public /* synthetic */ void lambda$new$5$HwMagicWinAmsPolicy(List params, Bundle result) {
        this.mHomeDetect.putDetectedResult(ActivityRecordEx.forToken((IBinder) params.get(0)), result);
    }

    private void startRelatedAndSetMainMode(ActivityRecordEx mainAr, String pkgName) {
        HwMagicContainer container = this.mMwManager.getContainer(mainAr);
        if (container != null) {
            ArrayList<ActivityRecordEx> arList = getAllActivities(mainAr.getActivityStackEx());
            ArrayList<ActivityRecordEx> otherArList = new ArrayList<>();
            Iterator<ActivityRecordEx> it = arList.iterator();
            while (it.hasNext()) {
                ActivityRecordEx ar = it.next();
                if (ar != null && !isMainActivity(container, ar)) {
                    otherArList.add(ar);
                }
            }
            if (!this.mMwManager.isMaster(mainAr)) {
                this.mMagicWinSplitMng.addOrUpdateMainActivityStat(container, mainAr);
                updateActivityModeAndBounds(mainAr, container.getBounds(1, pkgName), HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
            }
            SlogEx.i(TAG, "start other activity size " + otherArList.size());
            if (otherArList.size() == 0) {
                String relateActName = container.getConfig().getRelateActivity(pkgName);
                if (!relateActName.isEmpty()) {
                    startRelateActivity(pkgName, relateActName, mainAr);
                    return;
                }
                return;
            }
            Iterator<ActivityRecordEx> it2 = otherArList.iterator();
            while (it2.hasNext()) {
                ActivityRecordEx otherAr = it2.next();
                SlogEx.i(TAG, "setLoginStatus startRelatedAndSetMode start otherAr =" + otherAr + " pkgName " + pkgName);
                if (otherAr != null && !this.mMwManager.isSlave(otherAr) && !this.mMwManager.isMaster(otherAr) && !PERMISSION_ACTIVITY.equals(Utils.getClassName(otherAr))) {
                    updateActivityModeAndBounds(otherAr, container.getBounds(2, pkgName), HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
                }
            }
        }
    }

    private void updateActivityModeAndBounds(ActivityRecordEx activityRecord, Rect bounds, int windowMode) {
        if (activityRecord != null) {
            ActivityStackEx activityStack = activityRecord.getTaskRecordEx().getStack();
            if (!(activityStack == null || activityStack.getWindowingMode() == windowMode)) {
                activityStack.setWindowingMode(windowMode);
            }
            SlogEx.i(TAG, "updateActivityModeAndBounds activityRecord =" + activityRecord + " bounds " + bounds);
            activityRecord.setWindowingMode(windowMode);
            activityRecord.setBounds(bounds);
        }
    }

    public /* synthetic */ void lambda$new$6$HwMagicWinAmsPolicy(List params, Bundle result) {
        Configuration config = (Configuration) params.get(0);
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (config != null && container != null && config.densityDpi != this.mLastDensityDpi) {
            SlogEx.i(TAG, "overrideConfigInMagicWin update system bound size when resolution change!!!");
            this.mLastDensityDpi = config.densityDpi;
            Message msg = this.mMwManager.getHandler().obtainMessage(16);
            this.mMwManager.getHandler().removeMessages(16);
            this.mMwManager.getHandler().sendMessage(msg);
        }
    }

    public /* synthetic */ void lambda$new$7$HwMagicWinAmsPolicy(List params, Bundle result) {
        ActivityRecordEx touchActivity = ActivityRecordEx.forToken((IBinder) params.get(0));
        SlogEx.i(TAG, "### Execute -> updateFocusActivity touchActivity : " + touchActivity);
        if (touchActivity == null || touchActivity.getTaskRecordEx() == null || isFullScreenActivity(touchActivity)) {
            SlogEx.i(TAG, "### Execute -> updateFocusActivity no need update and return");
            return;
        }
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            getMode(touchActivity).moveNextActivityToFrontIfNeeded(touchActivity);
            moveToFrontInner(touchActivity);
        }
    }

    public void checkResumeStateForMagicWindow(ActivityRecordEx focus) {
        if (this.mMwManager.isSupportMultiResume(Utils.getPackageName(focus)) && focus.isVisible() && focus.isTopRunningActivity() && focus.getActivityStackEx() != null && !focus.equalsActivityRecord(focus.getActivityStackEx().getResumedActivity()) && focus.isState(ActivityStackEx.ActivityState.RESUMED)) {
            focus.getActivityStackEx().onActivityStateChanged(focus, ActivityStackEx.ActivityState.RESUMED, "checkResumeStateForMagicWindow");
        }
    }

    public /* synthetic */ void lambda$new$8$HwMagicWinAmsPolicy(List params, Bundle result) {
        SlogEx.i(TAG, "### Execute -> processSpliteScreenForMutilWin");
        this.mModeSwitcher.processSpliteScreenForMutilWin(((Integer) params.get(0)).intValue(), ((Boolean) params.get(1)).booleanValue(), ((Integer) params.get(2)).intValue(), result);
    }

    public /* synthetic */ void lambda$new$9$HwMagicWinAmsPolicy(List params, Bundle result) {
        SlogEx.i(TAG, "### Execute -> processHwMultiStack");
        this.mModeSwitcher.processHwMultiStack(((Integer) params.get(0)).intValue(), this.mContext.getResources().getConfiguration().orientation, result);
    }

    public /* synthetic */ void lambda$new$10$HwMagicWinAmsPolicy(List params, Bundle result) {
        SlogEx.i(TAG, "### Execute -> moveMwToHwMultiStack");
        this.mModeSwitcher.moveMwToHwMultiStack(((Integer) params.get(0)).intValue(), (Rect) params.get(1), result);
    }

    public /* synthetic */ void lambda$new$11$HwMagicWinAmsPolicy(List params, Bundle result) {
        this.mOrientationPolicy.isDisableUpsideDownRotation(((Integer) params.get(0)).intValue(), result);
    }

    public /* synthetic */ void lambda$new$12$HwMagicWinAmsPolicy(List params, Bundle result) {
        this.mMagicWinSplitMng.updateStackVisibility(getActivityStackEx(((Integer) params.get(0)).intValue()), result);
    }

    public /* synthetic */ void lambda$new$13$HwMagicWinAmsPolicy(List params, Bundle result) {
        int fromStackId = ((Integer) params.get(4)).intValue();
        ActivityStackEx fromStack = getActivityStackEx(fromStackId);
        ActivityStackEx toStack = null;
        ActivityRecordEx activityRecord = fromStack != null ? fromStack.getTopActivity() : null;
        HwMagicContainer container = this.mMwManager.getContainer(activityRecord);
        if (container != null && activityRecord != null) {
            SlogEx.d(TAG, "### Execute -> Move Right Activity To Left");
            boolean quitMagicWindow = ((Boolean) params.get(0)).booleanValue();
            boolean clearBounds = ((Boolean) params.get(1)).booleanValue();
            int windowingMode = ((Integer) params.get(2)).intValue();
            int taskId = ((Integer) params.get(3)).intValue();
            int toStackId = ((Integer) params.get(5)).intValue();
            if (windowingMode == 3) {
                finishInvisibleActivityInFullMode(activityRecord);
                this.mModeSwitcher.moveLatestActivityToTop(quitMagicWindow, clearBounds);
            } else if (WindowConfigurationEx.isHwFreeFormWindowingMode(windowingMode)) {
                finishInvisibleActivityInFullMode(activityRecord);
                if (clearBounds && container.isInMagicWinOrientation()) {
                    this.mModeSwitcher.clearOverrideBounds(taskId);
                }
            } else if (windowingMode == 2) {
                finishInvisibleActivityInFullMode(activityRecord);
                if (clearBounds && container.isInMagicWinOrientation()) {
                    this.mModeSwitcher.clearOverrideBounds(activityRecord);
                }
            } else {
                if (toStackId != -1) {
                    toStack = getActivityStackEx(toStackId);
                }
                if (toStack != null && toStack.getDisplayId() != fromStack.getDisplayId()) {
                    this.mModeSwitcher.updateStackForDisplay(fromStackId, toStack.getDisplayId(), toStackId);
                }
            }
        }
    }

    public /* synthetic */ void lambda$new$14$HwMagicWinAmsPolicy(List params, Bundle result) {
        int stackId = ((Integer) params.get(0)).intValue();
        int toDisplayId = ((Integer) params.get(1)).intValue();
        SlogEx.d(TAG, "### Execute -> Move stackId" + stackId + " To display " + toDisplayId);
        this.mModeSwitcher.updateStackForDisplay(stackId, toDisplayId, stackId);
    }

    public /* synthetic */ void lambda$new$15$HwMagicWinAmsPolicy(List params, Bundle result) {
        if (!HwMwUtils.isInSuitableScene(true)) {
            SlogEx.w(TAG, "it is in PC mode or mmi test !");
            return;
        }
        this.mOrientationPolicy.checkMagicOrientation(ActivityRecordEx.forToken((IBinder) params.get(0)), result);
    }

    public /* synthetic */ void lambda$new$16$HwMagicWinAmsPolicy(List params, Bundle result) {
        int stackId = ((Integer) params.get(0)).intValue();
        boolean isUnderHomeStacks = ((Boolean) params.get(1)).booleanValue();
        result.putBoolean("RESULT_IN_APP_SPLIT", this.mMagicWinSplitMng.isInAppSplite(getActivityStackEx(stackId), isUnderHomeStacks));
    }

    public /* synthetic */ void lambda$new$17$HwMagicWinAmsPolicy(List params, Bundle result) {
        int touchDownX = ((Integer) params.get(0)).intValue();
        int touchDownY = ((Integer) params.get(1)).intValue();
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            if (!this.mMwManager.getWmsPolicy().isInputMethodWindowVisible()) {
                ActivityRecordEx topActivity = getTopActivity(this.mMwManager.getLocalContainer());
                if (topActivity != null && topActivity.inHwMagicWindowingMode()) {
                    if (!this.mMagicWinSplitMng.isPkgSpliteScreenMode(topActivity, true)) {
                        SlogEx.i(TAG, "switchFocusIfNeeded touchDownX=" + touchDownX + " touchDownY=" + touchDownY);
                        if (this.mMwManager.isSlave(topActivity)) {
                            moveToFrontIfNeeded(topActivity, 1, touchDownX, touchDownY);
                        } else if (this.mMwManager.isMaster(topActivity)) {
                            moveToFrontIfNeeded(topActivity, 2, touchDownX, touchDownY);
                        } else {
                            SlogEx.i(TAG, "switchFocusIfNeeded is not double windows");
                        }
                    }
                }
            }
        }
    }

    private void moveToFrontIfNeeded(ActivityRecordEx top, int windowPosition, int touchDownX, int touchDownY) {
        ActivityRecordEx activity = getActivityByPosition(top, windowPosition, 0);
        if (activity != null && new Region(activity.getRequestedOverrideBounds()).contains(touchDownX, touchDownY)) {
            if (activity.instanceOfHwActivityRecord()) {
                activity.setMagicWindowPageType(1);
            }
            moveToFrontInner(activity);
            activity.resumeKeyDispatchingLocked();
        }
    }

    public void moveToFrontInner(ActivityRecordEx activity) {
        activity.getTaskRecordEx().moveActivityToFrontLocked(activity);
        if (!activity.isState(ActivityStackEx.ActivityState.RESUMED)) {
            this.mActivityTaskManager.getRootActivityContainer().resumeFocusedStacksTopActivities();
        }
        checkResumeStateForMagicWindow(activity);
    }

    public /* synthetic */ void lambda$new$18$HwMagicWinAmsPolicy(List params, Bundle result) {
        ActivityRecordEx masterTop;
        ActivityRecordEx rightTop;
        IBinder token = (IBinder) params.get(0);
        ActivityRecordEx ar = ActivityRecordEx.isInStackLocked(token);
        HwMagicContainer container = this.mMwManager.getContainer(ar);
        if (ar != null && isRelatedActivity(container, ar) && isSupportMainRelatedMode(container, ar) && (masterTop = getActivityByPosition(ar, 1, 0)) != null && isMainActivity(container, masterTop) && (rightTop = getActivityByPosition(ar, 2, 0)) != null && isRelatedActivity(container, rightTop)) {
            SlogEx.i(TAG, "onBackPressed: move activity task to back for magicwindow, currentActivity = " + ar);
            this.mActivityTaskManager.moveActivityTaskToBack(token, true);
            result.putBoolean("BUNDLE_RESULT_ONBACKPRESSED", true);
        }
    }

    public /* synthetic */ void lambda$new$19$HwMagicWinAmsPolicy(List params, Bundle result) {
        this.mMagicWinSplitMng.resizeWhenMoveBackIfNeed(getActivityStackEx(((Integer) params.get(0)).intValue()));
    }

    public ActivityStackEx getActivityStackEx(int stackId) {
        ActivityDisplayEx defaultDisplay = this.mActivityTaskManager.getRootActivityContainer().getDefaultDisplay();
        ActivityStackEx result = defaultDisplay != null ? defaultDisplay.getStackEx(stackId) : null;
        if (result != null) {
            return result;
        }
        ActivityStackEx result2 = (ActivityStackEx) Optional.ofNullable(this.mMwManager.getLocalContainer()).map($$Lambda$HwMagicWinAmsPolicy$I79o0ujGUPIQo_ELvFb3m42eU.INSTANCE).map(new Function(stackId) {
            /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$Z_FtWwiqLP_8nkKyVCUWOINZYE */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ((ActivityDisplayEx) obj).getStackEx(this.f$0);
            }
        }).orElse(null);
        if (result2 != null) {
            return result2;
        }
        return (ActivityStackEx) Optional.ofNullable(this.mMwManager.getVirtualContainer()).map($$Lambda$HwMagicWinAmsPolicy$GjL5JhAzzRdT2yaOGXTWDm79XbY.INSTANCE).map(new Function(stackId) {
            /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$Mjg_me1MI40nEEFdVdAWeEQBc */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ((ActivityDisplayEx) obj).getStackEx(this.f$0);
            }
        }).orElse(null);
    }

    public /* synthetic */ void lambda$new$24$HwMagicWinAmsPolicy(List params, Bundle result) {
        ActivityRecordEx resumeActivity = new ActivityRecordEx();
        resumeActivity.resetActivityRecord(params.get(0));
        this.mMagicWinSplitMng.resizeSplitStackBeforeResume(resumeActivity, Utils.getRealPkgName(resumeActivity));
    }

    public /* synthetic */ void lambda$new$25$HwMagicWinAmsPolicy(List params, Bundle result) {
        if (!HwMwUtils.isInSuitableScene(true)) {
            SlogEx.w(TAG, "it is in PC mode or mmi test !");
            return;
        }
        int oldOrientation = ((Integer) params.get(0)).intValue();
        int newOrientation = ((Integer) params.get(1)).intValue();
        IBinder token = (IBinder) params.get(2);
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            this.mModeSwitcher.updateMagicWindowConfiguration(oldOrientation, newOrientation, token);
        }
    }

    public ActivityRecordEx getTopActivity(HwMagicContainer container) {
        ActivityStackEx topFocus = getFocusedTopStack(container);
        if (topFocus != null) {
            return topFocus.getTopActivity();
        }
        return null;
    }

    public ActivityStackEx getFocusedTopStack(HwMagicContainer container) {
        if (container == null) {
            return null;
        }
        return getFilteredTopStack(container.getActivityDisplay(), Arrays.asList(5, 2, Integer.valueOf((int) HwMagicWinAnimationScene.SCENE_START_APP)));
    }

    private ActivityStackEx getFilteredTopStack(ActivityDisplayEx activityDisplay, List<Integer> ignoreWindowModes) {
        ActivityStackEx stack = null;
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            if (activityDisplay == null) {
                SlogEx.i(TAG, "getFilteredTopStack activityDisplay null, no TopStack");
                return null;
            }
            for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                stack = activityDisplay.getChildAt(stackNdx);
                if (ignoreWindowModes == null || !ignoreWindowModes.contains(Integer.valueOf(stack.getWindowingMode()))) {
                    return stack;
                }
            }
            return stack;
        }
    }

    public ArrayList<ActivityRecordEx> getAllActivities(ActivityStackEx stack) {
        ArrayList<ActivityRecordEx> outActivities = new ArrayList<>();
        if (stack == null) {
            return outActivities;
        }
        for (int taskNdx = stack.getTaskHistory().size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecordEx task = (TaskRecordEx) stack.getTaskHistory().get(taskNdx);
            for (int activityNdx = task.getActivityRecordExs().size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecordEx activity = (ActivityRecordEx) task.getActivityRecordExs().get(activityNdx);
                if (!activity.isFinishing()) {
                    outActivities.add(activity);
                }
            }
        }
        return outActivities;
    }

    public /* synthetic */ void lambda$new$26$HwMagicWinAmsPolicy(List params, Bundle result) {
        ApplicationInfo info = (ApplicationInfo) params.get(0);
        String[] args = (String[]) params.get(2);
        if (info != null && HwMwUtils.isInSuitableScene(true)) {
            if (args == null || args.length < 4) {
                SlogEx.w(TAG, "overrideArgsForHwMagicWin args is not valid");
                return;
            }
            HwMagicContainer container = this.mMwManager.getLocalContainer();
            ActivityStackEx focusStack = getFocusedTopStack(container);
            boolean isMagicMode = focusStack != null ? focusStack.inHwMagicWindowingMode() : false;
            if (container != null && container.getHwMagicWinEnabled(info.packageName)) {
                SlogEx.w(TAG, "overrideArgsForHwMagicWin packageName " + info.packageName);
                args[0] = String.valueOf(true);
                Rect bound = container.getBounds(3, info.packageName);
                args[1] = String.valueOf(String.valueOf(bound.width()));
                args[2] = String.valueOf(String.valueOf(bound.height()));
                if (isMagicMode) {
                    args[3] = String.valueOf((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
                } else {
                    args[3] = String.valueOf(0);
                }
                args[4] = String.valueOf(container.getConfig().isDragable(info.packageName));
            }
        }
    }

    private void adjustWindowForResume(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container != null && !TRANSITION_ACTIVITIES.contains(Utils.getClassName(activity)) && !this.mMagicWinSplitMng.isPkgSpliteScreenMode(activity, true)) {
            if (this.mMwManager.isMaster(activity) && getActivityByPosition(activity, 2, 0) == null && !activity.isFinishing()) {
                if (getActivityByPosition(activity, 1, 1) == null || isMainActivity(container, activity)) {
                    moveWindow(activity, 3);
                } else {
                    moveWindow(activity, 2);
                    return;
                }
            }
            if (this.mMwManager.isSlave(activity) && getActivityByPosition(activity, 1, 0) == null) {
                ActivityRecordEx activityRecord = getActivityByPosition(activity, 3, 0);
                if (activityRecord != null) {
                    moveWindow(activityRecord, 1);
                    return;
                }
                moveWindow(activity, 3);
            }
            if ((this.mMwManager.isMaster(activity) || this.mMwManager.isSlave(activity) || isShowDragBar(activity)) && container.getConfig().isDragable(Utils.getRealPkgName(activity))) {
                this.mMwManager.getUIController().updateSplitBarVisibility(true, container.getDisplayId());
            } else {
                this.mMwManager.getUIController().updateSplitBarVisibility(false, container.getDisplayId());
            }
        }
    }

    private void adjustWindowForFinish(ActivityRecordEx activity, String finishReason) {
        if (!TRANSITION_ACTIVITIES.contains(Utils.getClassName(activity)) && !activity.isFullScreenVideoInLandscape() && !this.mMagicWinSplitMng.isPkgSpliteScreenMode(activity, true) && !this.mMwManager.isMiddle(activity) && !this.mMwManager.isFull(activity)) {
            getMode(activity).adjustWindowForFinish(activity, finishReason);
        }
    }

    private void adjustWindowForMiddle(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container == null || !this.mMwManager.isMiddle(activity) || container.getConfig().isDefaultFullscreenActivity(Utils.getRealPkgName(activity), Utils.getClassName(activity))) {
            return;
        }
        if (getActivityByPosition(activity, 1, 0) != null) {
            moveWindow(activity, 2);
        } else if (getActivityByPosition(activity, 2, 0) != null) {
            moveWindow(activity, 1);
        }
    }

    public /* synthetic */ void lambda$new$27$HwMagicWinAmsPolicy(List params, Bundle result) {
        int mode;
        ActivityRecordEx topActivity;
        ActivityRecordEx resumeActivity = ActivityRecordEx.forToken((IBinder) params.get(0));
        HwMagicContainer container = this.mMwManager.getContainer(resumeActivity);
        if (container != null) {
            SlogEx.i(TAG, "### Execute -> resumeActivityForHwMagicWin ActivityRecord resumeActivity=" + resumeActivity);
            String pkg = Utils.getRealPkgName(resumeActivity);
            if (this.mMwManager.isDragFullMode(resumeActivity)) {
                mode = resumeActivity.getTaskRecordEx().getDragFullMode();
                resumeActivity.setIsFromFullscreenToMagicWin(true);
            } else {
                mode = resumeActivity.inHwMagicWindowingMode() ? -1 : -2;
            }
            HwMagicWinStatistics.getInstance(container.getType()).startTick(container.getConfig(), pkg, mode, "resume");
            container.getCameraRotation().updateCameraRotation(1);
            requestRotation(resumeActivity);
            if (container.getConfig().isSupportAppTaskSplitScreen(pkg)) {
                this.mMagicWinSplitMng.addOrUpdateMainActivityStat(container, resumeActivity);
            }
            startRightOnResume(resumeActivity, pkg);
            if (resumeActivity.inHwMagicWindowingMode()) {
                if (resumeActivity.isTopRunningActivity()) {
                    if (this.mMwManager.isFull(resumeActivity)) {
                        SlogEx.i(TAG, "resumeActivityForHwMagicWin isFull, change to false");
                        this.mMwManager.getUIController().updateMwWallpaperVisibility(false, container.getDisplayId(), false);
                    } else if (this.mMwManager.isMiddle(resumeActivity)) {
                        SlogEx.i(TAG, "resumeActivityForHwMagicWin isMiddle, change to true");
                        this.mMwManager.getUIController().updateMwWallpaperVisibility(true, container.getDisplayId(), false);
                    }
                }
                this.mMagicWinSplitMng.resizeStackWhileResumeSplitAppIfNeed(pkg, resumeActivity);
                if (this.mMagicWinSplitMng.isPkgSpliteScreenMode(resumeActivity, true)) {
                    this.mMagicWinSplitMng.resizeStackIfNeedOnresume(resumeActivity);
                }
                if (isMainActivity(container, resumeActivity) && this.mMwManager.isMaster(resumeActivity) && !container.getConfig().isSupportAppTaskSplitScreen(pkg)) {
                    startRelateActivityIfNeed(resumeActivity, false);
                }
                TaskRecordEx taskRecord = resumeActivity.getTaskRecordEx();
                if (taskRecord != null && taskRecord.getChildCount() == 2 && (topActivity = taskRecord.getChildAt(taskRecord.getChildCount() - 1)) != null && topActivity.isFinishing() && this.mMwManager.isSlave(topActivity)) {
                    SlogEx.d(TAG, "resumeActivityForHwMagicWin call scheduleIdleLocked");
                    this.mActivityTaskManager.scheduleIdleLockedFromStackSupervisor();
                }
                adjustWindowForResume(resumeActivity);
                checkResumeStateForMagicWindow(resumeActivity);
                checkBackgroundForMagicWindow(resumeActivity);
                boolean canShowWhileOccluded = this.mAms.getWindowManagerServiceEx().getWindowManagerPolicyEx().isKeyguardOccluded();
                boolean isKeyguardLocked = this.mActivityTaskManager.getKeyguardControllerFromStackSupervisor().isKeyguardLocked();
                boolean isTopActivity = resumeActivity.equalsActivityRecord(getTopActivity(container));
                if (canShowWhileOccluded && isTopActivity && resumeActivity.isNowVisible() && !this.mMwManager.isFull(resumeActivity)) {
                    if (isKeyguardLocked) {
                        resumeActivity.setBounds(container.getBounds(3, Utils.getPackageName(resumeActivity)));
                    } else {
                        adjustWindowForMiddle(resumeActivity);
                    }
                }
                if (resumeActivity.isFinishAllRightBottom()) {
                    finishMagicWindow(resumeActivity, false);
                }
                resumeActivity.setIsFinishAllRightBottom(false);
                if (this.mMwManager.isMaster(resumeActivity)) {
                    resumeUnusualActivity(resumeActivity, 2);
                }
                if (this.mMwManager.isSlave(resumeActivity)) {
                    resumeUnusualActivity(resumeActivity, 1);
                }
                this.mMwManager.getUIController().updateBgColor(container.getDisplayId());
            } else if (resumeActivity.getWindowingMode() == 1) {
                this.mMwManager.getUIController().updateSplitBarVisibility(false, container.getDisplayId());
            }
        }
    }

    private void startRightOnResume(ActivityRecordEx resumeAr, String pkg) {
        if (isPkgInLoginStatus(resumeAr) && isNeedStartOrMoveRight(resumeAr, pkg)) {
            HwMagicContainer container = this.mMwManager.getContainer(resumeAr);
            if (isMainActivity(container, resumeAr) && resumeAr.getTaskRecordEx().getStack().equalsStack(getFocusedTopStack(container))) {
                SlogEx.w(TAG, "resumeAr on the top");
                startRelatedAndSetMainMode(resumeAr, pkg);
            } else if (isRelatedActivity(container, resumeAr)) {
                ActivityRecordEx preAr = getActivityByPosition(resumeAr, 0, 1);
                if (preAr != null && preAr.getTaskRecordEx() != null && this.mMwManager.isMiddle(preAr) && this.mMwManager.isSlave(resumeAr)) {
                    resumeAr.getTaskRecordEx().moveActivityToFrontLocked(preAr);
                    SlogEx.i(TAG, "start right resume move the middle to top");
                }
            } else {
                SlogEx.d(TAG, "start right other activity");
            }
        }
    }

    private void moveToTopWhenMultiResume(ActivityRecordEx resumedActivity) {
        ActivityRecordEx activityRecord;
        if (this.mMwManager.isSupportMultiResume(Utils.getPackageName(resumedActivity)) && this.mMwManager.isMaster(resumedActivity) && (activityRecord = getActivityByPosition(resumedActivity, 2, 0)) != null) {
            resumedActivity.getTaskRecordEx().moveActivityToFrontLocked(activityRecord);
            this.mActivityTaskManager.getRootActivityContainer().resumeFocusedStacksTopActivities();
            checkResumeStateForMagicWindow(activityRecord);
        }
    }

    private void resumeUnusualActivity(ActivityRecordEx resumeActivity, int windowPosition) {
        ActivityRecordEx activity = getActivityByPosition(resumeActivity, windowPosition, 0);
        if (activity != null && !isKeyguardLockedAndOccluded()) {
            if (activity.getAppWindowTokenEx() != null && activity.getAppWindowTokenEx().findMainWindow() == null) {
                moveToFrontForResumeUnusual(activity);
            } else if (this.mMwManager.isSupportMultiResume(Utils.getPackageName(activity)) && !activity.isState(ActivityStackEx.ActivityState.RESUMED)) {
                if (windowPosition != 1 || isSupportLeftResume(this.mMwManager.getContainer(activity), activity) || !activity.isState(ActivityStackEx.ActivityState.PAUSED, ActivityStackEx.ActivityState.PAUSING, ActivityStackEx.ActivityState.RESUMED)) {
                    moveToFrontForResumeUnusual(activity);
                }
            }
        }
    }

    private void moveToFrontForResumeUnusual(ActivityRecordEx activity) {
        SlogEx.i(TAG, "moveToFrontForResumeUnusual unusualActivity = " + activity);
        activity.getTaskRecordEx().moveActivityToFrontLocked(activity);
        this.mActivityTaskManager.getRootActivityContainer().resumeFocusedStacksTopActivities();
    }

    public boolean isKeyguardLockedAndOccluded() {
        return this.mActivityTaskManager.getKeyguardControllerFromStackSupervisor().isKeyguardLocked() && this.mAms.getWindowManagerServiceEx().getWindowManagerPolicyEx().isKeyguardOccluded();
    }

    public boolean isRelatedInSlave(HwMagicContainer container, ActivityRecordEx ar) {
        return isRelatedActivity(container, ar) && this.mMwManager.isSlave(ar);
    }

    public void checkBackgroundForMagicWindow(ActivityRecordEx resumeActivity) {
        HwMagicContainer container = this.mMwManager.getContainer(resumeActivity);
        if (container != null) {
            boolean isMiddle = this.mMwManager.isMiddle(resumeActivity);
            SlogEx.i(TAG, "checkBackgroundForMagicWindow isMiddle =" + isMiddle);
            this.mMwManager.getUIController().changeWallpaper(isMiddle, container.getDisplayId());
        }
    }

    private boolean isNormalPage(HwMagicContainer container, ActivityRecordEx finishActivity) {
        boolean isMasterTop = finishActivity.equalsActivityRecord(getActivityByPosition(finishActivity, 1, 0));
        boolean isSlaveTop = finishActivity.equalsActivityRecord(getActivityByPosition(finishActivity, 2, 0));
        if (isFullScreenActivity(finishActivity.getActivityStackEx().getTopActivity()) || (!isMasterTop && !isSlaveTop)) {
            SlogEx.w(TAG, "isNormalPage ActivityRecord is not top Activity");
            return false;
        } else if (!HwMwUtils.IS_FOLD_SCREEN_DEVICE ? !isSpecTransActivity(container, finishActivity) : !isSpecTransActivityPreDefined(container, finishActivity)) {
            return true;
        } else {
            SlogEx.w(TAG, "isNormalPage ActivityRecord is not normal Activity");
            return false;
        }
    }

    public /* synthetic */ void lambda$new$28$HwMagicWinAmsPolicy(List params, Bundle result) {
        ActivityRecordEx finishActivity = ActivityRecordEx.forToken((IBinder) params.get(0));
        HwMagicContainer container = this.mMwManager.getContainer(finishActivity);
        String finishReason = (String) params.get(4);
        if (container != null && !FINISH_REASON_CRASH.equals(finishReason)) {
            if (finishActivity.inHwMagicWindowingMode()) {
                boolean isFullScreen = this.mMwManager.isFull(finishActivity);
                if (PERMISSION_ACTIVITY.equals(Utils.getClassName(finishActivity)) || (isFullScreen && !this.mMwManager.isDragFullMode(finishActivity))) {
                    this.mMwManager.getUIController().updateSplitBarVisibility(true, container.getDisplayId());
                }
                if (isMainActivity(container, finishActivity) && finishActivity.getActivityStackEx() != null && removeRelatedActivity(container, finishActivity.getActivityStackEx(), false) && container.isVirtualContainer()) {
                    this.mActivityTaskManager.scheduleIdleLockedFromStackSupervisor();
                }
                if (this.mMwManager.isSlave(finishActivity)) {
                    getMode(finishActivity).setPairForFinish(finishActivity);
                }
                SlogEx.i(TAG, "finishActivityForHwMagicWin ActivityRecord " + finishActivity + " finishReason=" + finishReason);
                this.mHomeDetect.updateDetectHomeAfterActivityFinished(finishActivity);
                if (this.mMwManager.isDragFullMode(finishActivity) && !finishActivity.isDelayFinished()) {
                    finishInvisibleActivityInFullModeInner(finishActivity, true);
                }
                if (!isNormalPage(container, finishActivity)) {
                    presetFinishActivityAnimation(container, finishActivity, finishReason);
                    return;
                }
                if (this.mMwManager.isMaster(finishActivity) && finishActivity.isTopRunningActivity()) {
                    getMode(finishActivity).finishRightAfterFinishingLeft(finishActivity);
                }
                presetFinishActivityAnimation(container, finishActivity, finishReason);
                adjustWindowForFinish(finishActivity, finishReason);
                String pkgName = Utils.getPackageName(finishActivity);
                if (pkgName != null && pkgName.equals(Utils.getRealPkgName(getTopActivity(container))) && container.getConfig().isNeedStartByNewTaskActivity(pkgName, Utils.getClassName(finishActivity)) && finishActivity.getTaskRecordEx().getChildCount() == 1) {
                    this.mMagicWinSplitMng.showMoveAnimation(finishActivity, 1);
                }
                this.mMagicWinSplitMng.moveTaskToFullscreenIfNeed(finishActivity, false);
            } else if (!container.isFoldableDevice() && finishActivity.getWindowingMode() == 1) {
                if ((!finishActivity.isFullScreenVideoInLandscape() && (finishActivity.getBounds().width() <= finishActivity.getBounds().height() || isPkgInLogoffStatus(finishActivity))) || !container.getHwMagicWinEnabled(Utils.getRealPkgName(finishActivity))) {
                    return;
                }
                if ((this.mContext.getResources().getConfiguration().orientation == 2 || container.isVirtualContainer()) && finishActivity.isTopRunningActivity()) {
                    this.mModeSwitcher.moveAppToMagicWinWhenFinishingFullscreen(finishActivity);
                }
            }
        }
    }

    private void presetFinishActivityAnimation(HwMagicContainer container, ActivityRecordEx finishActivity, String finishReason) {
        boolean isMastersFinish;
        boolean isAniRunningBelow = true;
        ActivityRecordEx secondSlaveAR = getActivityByPosition(finishActivity, 2, 1);
        boolean isExitSliding = getMode(finishActivity).isExitSliding(finishActivity, secondSlaveAR, finishReason);
        if (isExitSliding) {
            isMastersFinish = false;
        } else {
            isMastersFinish = getMode(finishActivity).isMastersFinish(finishActivity, finishReason);
        }
        HwMagicWinAnimationScene.FinishAnimationScene animationScene = new HwMagicWinAnimationScene.FinishAnimationScene();
        animationScene.setTargetPosition(container.getBoundsPosition(finishActivity.getRequestedOverrideBounds()));
        animationScene.setTransition(isSpecTransActivity(container, finishActivity));
        animationScene.setExitSliding(isExitSliding);
        animationScene.setMastersFinish(isMastersFinish);
        container.getAnimation().overrideFinishActivityAnimation(animationScene.calculatedAnimationScene());
        if (this.mMwManager.isMiddle(finishActivity) || secondSlaveAR != null || isMastersFinish) {
            isAniRunningBelow = false;
        }
        finishActivity.setIsAniRunningBelow(isAniRunningBelow);
    }

    public boolean isHomeActivity(HwMagicContainer container, ActivityRecordEx ar) {
        return this.mHomeDetect.isHomeActivity(container, ar);
    }

    public boolean removeRelatedActivity(HwMagicContainer container, ActivityStackEx stack, boolean isClearBounds) {
        boolean isRelatedActivityBeFinish = false;
        Iterator<ActivityRecordEx> it = getAllActivities(stack).iterator();
        while (it.hasNext()) {
            ActivityRecordEx currentActivity = it.next();
            if (isRelatedActivity(container, currentActivity)) {
                if (isClearBounds) {
                    this.mModeSwitcher.clearOverrideBounds(currentActivity);
                }
                stack.finishActivityLocked(currentActivity, 0, (Intent) null, MAGIC_WINDOW_FINISH_EVENT, true, false);
                isRelatedActivityBeFinish = true;
            }
        }
        return isRelatedActivityBeFinish;
    }

    private void setVisibilityForHwMagicWin(ActivityRecordEx targetActivity, int position, Bundle result) {
        HwMagicContainer container = this.mMwManager.getContainer(targetActivity);
        if (container != null) {
            int windowBounds = container.getBoundsPosition(targetActivity.getRequestedOverrideBounds());
            if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
                SlogEx.i(TAG, "updateVisibility windowBounds=" + windowBounds + " position =" + position);
            }
            if (!isKeyguardLockedAndOccluded()) {
                if (position == 0) {
                    result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", true);
                }
                if (position > 0) {
                    String pkgName = Utils.getPackageName(targetActivity);
                    if ((container.isSupportAnAnMode(pkgName) || container.isSupportOpenMode(pkgName)) && position == 1 && windowBounds == 1 && container.isFoldableDevice()) {
                        result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", true ^ isHomeActivity(container, getActivityByPosition(targetActivity, 1, 0)));
                        return;
                    }
                    ActivityRecordEx prevActivity = getActivityByPosition(targetActivity, windowBounds, position - 1);
                    if (prevActivity == null || prevActivity.isFullscreen() || !prevActivity.isVisible()) {
                        result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", false);
                    } else {
                        result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", true);
                    }
                }
            }
        }
    }

    private String getCurrentRotationAndPkg(String pkg) {
        return pkg + "/:+" + String.valueOf(this.mAms.getWindowManagerServiceEx().getDefaultDisplayRotation());
    }

    public boolean isFullScreenActivity(ActivityRecordEx ar) {
        HwMagicContainer container = this.mMwManager.getContainer(ar);
        if (container == null) {
            return false;
        }
        if (container.isFoldableDevice()) {
            int pos = container.getBoundsPosition(ar.getRequestedOverrideBounds());
            if (pos == 3 || pos == 5) {
                return true;
            }
            return false;
        } else if (this.mMwManager.isFull(ar) || this.mDefaultFullScreenBounds.equals(ar.getRequestedOverrideBounds())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNeedUpdateVisibilityForTopFullscreen(ActivityRecordEx top, ActivityRecordEx current, boolean originalVisible, Bundle result) {
        if (isFullScreenActivity(top) && !top.equalsActivityRecord(current)) {
            if (top.isFullscreen() || !isFullScreenActivity(current)) {
                for (ActivityRecordEx ar : getAllActivities(current.getActivityStackEx())) {
                    if (ar.equalsActivityRecord(current)) {
                        break;
                    } else if (isFullScreenActivity(ar) && ar.isFullscreen()) {
                        result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", false);
                        return true;
                    }
                }
            } else {
                result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", originalVisible);
                return true;
            }
        }
        return false;
    }

    public /* synthetic */ void lambda$new$29$HwMagicWinAmsPolicy(List params, Bundle result) {
        boolean originalVisible = ((Boolean) params.get(2)).booleanValue();
        ActivityRecordEx current = ActivityRecordEx.forToken((IBinder) params.get(1));
        ActivityRecordEx top = ActivityRecordEx.forToken((IBinder) params.get(0));
        HwMagicContainer container = this.mMwManager.getContainer(current);
        if (container != null) {
            if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
                SlogEx.i(TAG, "updateVisibilityForHwMagicWin r=" + current + " top=" + top);
            }
            if (current == null || top == null) {
                result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", originalVisible);
                SlogEx.w(TAG, "updateVisibilityForHwMagicWin r or top is null");
                return;
            }
            String topPackageName = Utils.getPackageName(top);
            String rPackageName = Utils.getPackageName(current);
            if (rPackageName == null || topPackageName == null) {
                result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", originalVisible);
                SlogEx.w(TAG, "updateVisibilityForHwMagicWin rPackageName or topPackageName is null");
            } else if (container.getAppSupportMode(rPackageName) <= 0) {
                result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", originalVisible);
            } else if (this.mMagicWinSplitMng.isPkgSpliteScreenMode(current, false)) {
                result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", originalVisible);
            } else {
                ActivityStackEx activityStack = getFocusedTopStack(container);
                if (activityStack != null && !activityStack.equalsStack(current.getActivityStackEx()) && activityStack.inHwMagicWindowingMode()) {
                    result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", false);
                } else if (isKeyguardLockedAndOccluded() && !current.equalsActivityRecord(top)) {
                    result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", false);
                } else if (this.mMwManager.isMiddle(top) && !isFullScreenActivity(top) && (this.mMwManager.isMaster(current) || this.mMwManager.isSlave(current))) {
                    result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", false);
                } else if (!isNeedUpdateVisibilityForTopFullscreen(top, current, originalVisible, result)) {
                    int[] positions = (int[]) params.get(3);
                    if (this.mMwManager.isMaster(current)) {
                        setVisibilityForHwMagicWin(current, positions[0], result);
                        positions[0] = positions[0] + 1;
                    } else if (this.mMwManager.isSlave(current)) {
                        setVisibilityForHwMagicWin(current, positions[1], result);
                        positions[1] = positions[1] + 1;
                    } else {
                        result.putBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", originalVisible);
                    }
                }
            }
        }
    }

    public /* synthetic */ void lambda$new$31$HwMagicWinAmsPolicy(List params, Bundle result) {
        ActivityRecordEx next = null;
        Object paramZero = params.get(0);
        if (paramZero != null) {
            next = new ActivityRecordEx();
            next.resetActivityRecord(paramZero);
        }
        ActivityRecordEx focus = null;
        ActivityOptions options = (ActivityOptions) params.get(1);
        Object paramTwo = params.get(2);
        if (paramTwo != null) {
            focus = new ActivityRecordEx();
            focus.resetActivityRecord(paramTwo);
        } else {
            if (options != null) {
                focus = (ActivityRecordEx) Optional.ofNullable(this.mActivityTaskManager).map(new Function(options) {
                    /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$Gbuv3t07kJKiDepaxnLMZVmayNU */
                    private final /* synthetic */ ActivityOptions f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Function
                    public final Object apply(Object obj) {
                        return ((ActivityTaskManagerServiceEx) obj).getActivityDisplayEx(this.f$0.getLaunchDisplayId());
                    }
                }).map($$Lambda$HwMagicWinAmsPolicy$yBxzhEpuwJ2IQ5aOCt7nz4QLT4.INSTANCE).map($$Lambda$HwMagicWinAmsPolicy$4J_3K6vxK9lrI6Zox0X_jkLKrA.INSTANCE).orElse(null);
            }
            Object paramThree = params.get(3);
            if (focus == null && paramThree != null) {
                focus = new ActivityRecordEx();
                focus.resetActivityRecord(paramThree);
            }
        }
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (next != null && container != null) {
            SlogEx.i(TAG, "### Execute -> overrideIntentFlagForHwMagicWin");
            HwMagicModeBase appMode = getMode(focus);
            if (focus == null) {
                SlogEx.e(TAG, "overrideIntentFlagForHwMagicWin focus is null");
            } else if (!isOtherMultiWinOptions(options, next, focus)) {
                if (focus.isActivityTypeHome() && next.getIntent() != null) {
                    Set<String> categories = next.getIntent().getCategories();
                    next.setIsStartFromLauncher(categories != null && categories.contains("android.intent.category.LAUNCHER"));
                }
                if (focus.inHwMagicWindowingMode()) {
                    appMode.addNewTaskFlag(focus, next);
                    finishInvisibleActivityInFullMode(focus);
                    ActivityRecordEx slavetop = getActivityByPosition(focus, 2, 0);
                    if (slavetop != null && this.mMwManager.isMaster(focus)) {
                        slavetop.setMagicWindowPageType(1);
                    }
                }
                if (focus.isActivityTypeHome() && container.getHwMagicWinEnabled(Utils.getPackageName(next)) && !container.isFoldableDevice()) {
                    container.getAnimation().setOpenAppAnimation();
                }
                if (this.mOrientationPolicy.isDefaultLandOrientation(next.getInfo().screenOrientation)) {
                    next.setIsFullScreenVideoInLandscape(true);
                }
            } else if (options != null) {
                this.mMagicWinSplitMng.multWindowModeProcess(focus, ActivityOptionsEx.getLaunchWindowingMode(options));
            }
        }
    }

    public boolean isEnterDoubleWindowIgnoreHome(HwMagicContainer container, String packageName) {
        return isSupportMainRelatedMode(container, packageName) && container.isSupportAnAnMode(packageName);
    }

    private boolean isEnterDoubleWindowForFold(HwMagicContainer container, ActivityRecordEx focus) {
        if (!(container.isFoldableDevice() && focus.inHwMagicWindowingMode() && this.mMwManager.isFull(focus) && isEnterDoubleWindowIgnoreHome(container, Utils.getRealPkgName(focus)))) {
            return false;
        }
        for (ActivityRecordEx activity : getAllActivities(focus.getActivityStackEx())) {
            if (isDefaultFullscreenActivity(container, activity)) {
                return false;
            }
        }
        return true;
    }

    public /* synthetic */ void lambda$new$32$HwMagicWinAmsPolicy(List params, Bundle result) {
        TaskRecordEx task;
        ActivityRecordEx focus = ActivityRecordEx.forToken((IBinder) params.get(0));
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (container != null) {
            SlogEx.i(TAG, "### Execute -> overrideIntentForHwMagicWin");
            ActivityRecordEx next = new ActivityRecordEx();
            next.resetActivityRecord(params.get(1));
            ActivityOptions options = (ActivityOptions) params.get(4);
            HwMagicModeBase appMode = getMode(focus);
            if (appMode.checkStatus(focus, next) && !isOtherMultiWinOptions(options, next, focus)) {
                boolean isNewTask = ((Boolean) params.get(3)).booleanValue();
                if (focus == null || ((!focus.inHwMultiStackWindowingMode() && !focus.inFreeformWindowingMode()) || isNewTask)) {
                    if (container.isFoldableDevice() && next.isStartFromLauncher()) {
                        next.setIsStartFromLauncher(false);
                        if (!isMainActivity(container, next)) {
                            return;
                        }
                    }
                    ActivityRecordEx reusedActivity = ActivityRecordEx.forToken((IBinder) params.get(2));
                    if (!isNewTask && reusedActivity != null && (task = reusedActivity.getTaskRecordEx()) != null && task.getTopActivity() == null && !task.equalsTaskRecord(focus.getTaskRecordEx())) {
                        isNewTask = true;
                    }
                    if (isMainActivity(container, next)) {
                        focus.setStartingWindowState(ActivityRecordEx.STARTING_WINDOW_NOT_SHOWN);
                    }
                    String nextPkg = Utils.getPackageName(next);
                    String focusPkg = Utils.getPackageName(focus);
                    if (!focus.inHwMagicWindowingMode() || isEnterDoubleWindowForFold(container, focus)) {
                        if (isMainActivity(container, next)) {
                            this.mModeSwitcher.moveToMagicWinFromFullscreenForMain(focus, next);
                        } else if (!container.isFoldableDevice() && (!container.isPadDevice() || !container.getConfig().isSupportAppTaskSplitScreen(nextPkg))) {
                            appMode.overrideIntent(focus, next, isNewTask);
                        } else if (!isNewTask) {
                            this.mModeSwitcher.moveToMagicWinFromFullscreenForTah(focus, next);
                        } else {
                            return;
                        }
                    } else if (!container.isFoldableDevice() || !isNewTask || focusPkg == null || focusPkg.equals(nextPkg)) {
                        appMode.setOrigActivityToken(container, focus);
                        appMode.overrideIntent(focus, next, isNewTask);
                    } else {
                        SlogEx.d(TAG, "overrideIntentForHwMagicWin start another app");
                        return;
                    }
                    if (next.isActivityTypeHome() || isNewTask) {
                        this.mMwManager.getUIController().updateSplitBarVisibility(false, container.getDisplayId());
                    }
                    if (next.inHwMagicWindowingMode() && isMainActivity(container, next)) {
                        startRelateActivityIfNeed(next, false);
                    }
                    if (next.inHwMagicWindowingMode() && this.mMwManager.isMiddle(next) && isPkgInLogoffStatus(next)) {
                        SlogEx.d(TAG, "overrideIntentForHwMagicWin not login set to full");
                        next.setBounds((Rect) null);
                        next.setWindowingMode(1);
                    }
                    if (next.getIntent() != null) {
                        next.getIntent().removeFlags(65536);
                    }
                    container.updateActivityOptions(focus, next, options);
                }
            }
        }
    }

    public boolean isHomeStackHotStart(ActivityRecordEx focus, ActivityRecordEx next) {
        ActivityRecordEx activityRecord;
        if (!(focus == null || next == null || !next.inHwMagicWindowingMode())) {
            if (!(next.getLaunchedFromUid() != next.getAppInfo().uid && focus.getAppInfo().uid == next.getAppInfo().uid)) {
                return false;
            }
            int homeUid = -1;
            if (!(next.getDisplayEx() == null || next.getDisplayEx().getHomeStackEx() == null || (activityRecord = next.getDisplayEx().getHomeStackEx().getTopActivity()) == null)) {
                homeUid = activityRecord.getAppInfo().uid;
            }
            if (homeUid != -1 && homeUid == next.getLaunchedFromUid()) {
                return true;
            }
        }
        return false;
    }

    public /* synthetic */ void lambda$new$33$HwMagicWinAmsPolicy(List params, Bundle result) {
        SlogEx.i(TAG, "### Execute -> clearTask");
        int flag = ((Integer) params.get(1)).intValue();
        ActivityRecordEx next = ActivityRecordEx.forToken((IBinder) params.get(0));
        ActivityRecordEx current = ActivityRecordEx.forToken((IBinder) params.get(2));
        if (next == null || current == null) {
            result.putBoolean("RESULT_CLEAR_TASK", false);
            return;
        }
        boolean isClearTaskInMagicWindowMode = (67108864 & flag) != 0 || next.getLaunchMode() == 2;
        HwMagicContainer container = this.mMwManager.getContainer(current);
        if (!isClearTaskInMagicWindowMode || ((!isHomeActivity(container, current) && current.getCreateTime() >= next.getCreateTime()) || isMainActivity(container, next))) {
            result.putBoolean("RESULT_CLEAR_TASK", false);
        } else {
            result.putBoolean("RESULT_CLEAR_TASK", true);
        }
    }

    private boolean isOtherMultiWinOptions(ActivityOptions options, ActivityRecordEx next, ActivityRecordEx focus) {
        int windowMode;
        if (options == null || ((windowMode = ActivityOptionsEx.getLaunchWindowingMode(options)) == 102 && Utils.getPackageName(next) != null && Utils.getPackageName(next).equals(Utils.getRealPkgName(focus)) && focus.inHwMagicWindowingMode() && focus.getUserId() == next.getUserId())) {
            return false;
        }
        if (WindowConfigurationEx.isHwMultiStackWindowingMode(windowMode) || windowMode == 5) {
            return true;
        }
        return false;
    }

    public ActivityRecordEx getActivityByPosition(HwMagicContainer container, int position) {
        ActivityRecordEx top = getTopActivity(container);
        if (top == null) {
            return null;
        }
        return getActivityByPosition(top, position, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x006d A[SYNTHETIC] */
    public ActivityRecordEx getActivityByPosition(ActivityRecordEx focus, int windowPosition, int windowIndex) {
        boolean realPosition;
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            if (container != null) {
                if (focus.getActivityStackEx() != null) {
                    ArrayList<TaskRecordEx> taskHistory = focus.getActivityStackEx().getAllTaskRecordExs();
                    int offsetIndex = 0;
                    for (int taskIndex = taskHistory.size() - 1; taskIndex >= 0; taskIndex--) {
                        ArrayList<ActivityRecordEx> activityRecords = taskHistory.get(taskIndex).getActivityRecordExs();
                        for (int activityIndex = activityRecords.size() - 1; activityIndex >= 0; activityIndex--) {
                            ActivityRecordEx activity = activityRecords.get(activityIndex);
                            if (!activity.isFinishing()) {
                                if (windowPosition != container.getBoundsPosition(activity.getRequestedOverrideBounds())) {
                                    if (windowPosition != container.getBoundsPosition(activity.getLastBound()) || !this.mMwManager.isFull(activity)) {
                                        realPosition = false;
                                        if (!realPosition || windowPosition == 0) {
                                            if (offsetIndex != windowIndex) {
                                                return activity;
                                            }
                                            offsetIndex++;
                                        }
                                    }
                                }
                                realPosition = true;
                                if (!realPosition) {
                                }
                                if (offsetIndex != windowIndex) {
                                }
                            }
                        }
                    }
                    return null;
                }
            }
            SlogEx.w(TAG, "overrideIntentForHwMagicWin getActvityByPosition the focus or stack is null");
            return null;
        }
    }

    public void finishMagicWindow(ActivityRecordEx currentActivityRecord, boolean isFinishAll) {
        HwMagicContainer container = this.mMwManager.getContainer(currentActivityRecord);
        if (!(currentActivityRecord == null || container == null)) {
            boolean isFinishActivity = isFinishAll;
            boolean isFinishSpecTransActivity = isFinishAll;
            boolean isAnyActivityFinished = false;
            for (ActivityRecordEx ar : getAllActivities(currentActivityRecord.getActivityStackEx())) {
                if (!isFinishActivity || isRelatedInSlave(container, ar) || (!isFinishSpecTransActivity && isSpecTransActivity(container, ar))) {
                    if (currentActivityRecord.equalsActivityRecord(ar)) {
                        isFinishActivity = true;
                    }
                } else if (isFinishActivity && (this.mMwManager.isSlave(ar) || (container.getBoundsPosition(ar.getLastBound()) == 2 && this.mMwManager.isFull(ar)))) {
                    isFinishSpecTransActivity = true;
                    ar.getActivityStackEx().finishActivityLocked(ar, 0, (Intent) null, MAGIC_WINDOW_FINISH_EVENT, true, false);
                    isAnyActivityFinished = true;
                }
            }
            if (isAnyActivityFinished) {
                this.mActivityTaskManager.scheduleIdleLockedFromStackSupervisor();
            }
        }
    }

    public boolean moveWindow(ActivityRecordEx targetActivity, int position) {
        HwMagicContainer container = this.mMwManager.getContainer(targetActivity);
        if (container == null || this.mMagicWinSplitMng.isPkgSpliteScreenMode(targetActivity, false) || (isMainActivity(container, targetActivity) && position == 3)) {
            return false;
        }
        if (!container.isFoldableDevice() || position != 3) {
            SlogEx.i(TAG, "moveWindow, targetActivity=" + targetActivity + ",position = " + position);
            setWindowBoundsLocked(targetActivity, container.getBounds(position, Utils.getPackageName(targetActivity)));
            return true;
        }
        Message msg = this.mMwManager.getHandler().obtainMessage(14);
        msg.obj = targetActivity.getShadow();
        this.mMwManager.getHandler().removeMessages(14);
        this.mMwManager.getHandler().sendMessageDelayed(msg, 100);
        return true;
    }

    public void setWindowBoundsLocked(ActivityRecordEx activityRecord, Rect bounds) {
        SlogEx.i(TAG, "setWindowBoundsLocked: activityRecord = " + activityRecord + ",bounds = " + bounds);
        if (!this.mMwManager.isFull(activityRecord) || this.mMwManager.isDragFullMode(activityRecord)) {
            activityRecord.setLastBound(activityRecord.getRequestedOverrideBounds());
        }
        activityRecord.setBounds(bounds);
        resize(activityRecord);
    }

    public /* synthetic */ void lambda$new$34$HwMagicWinAmsPolicy(List params, Bundle result) {
        int requestedOrientation = ((Integer) params.get(1)).intValue();
        this.mOrientationPolicy.resizeSpecialVideoInMagicWindowMode((IBinder) params.get(0), requestedOrientation, result);
    }

    public void updateStackVisibility(ActivityRecordEx activity, boolean isWallpaperVisible) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container != null) {
            SlogEx.i(TAG, "updateStackVisibility, activity =" + activity + ", isWallpaperVisible =" + isWallpaperVisible);
            this.mMwManager.getUIController().updateMwWallpaperVisibility(isWallpaperVisible, container.getDisplayId(), false);
            synchronized (this.mActivityTaskManager.getGlobalLock()) {
                if (activity != null) {
                    if (activity.getActivityStackEx() != null) {
                        int temp = activity.getInfo().configChanges;
                        activity.getInfo().configChanges |= 3328;
                        activity.getActivityStackEx().ensureActivitiesVisibleLocked((ActivityRecordEx) null, 0, false);
                        activity.getInfo().configChanges = temp;
                    }
                }
                ActivityRecordEx master = getActivityByPosition(activity, 1, 0);
                if (master != null && isWallpaperVisible) {
                    setMagicWindowToPauseInner(master);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resize(ActivityRecordEx r) {
        this.mAms.getWindowManagerServiceEx().deferSurfaceLayout();
        try {
            r.resize();
        } catch (IllegalArgumentException e) {
            SlogEx.e(TAG, "resize error");
        } catch (Throwable th) {
            this.mAms.getWindowManagerServiceEx().continueSurfaceLayout();
            throw th;
        }
        this.mAms.getWindowManagerServiceEx().continueSurfaceLayout();
    }

    public void removeCachedMagicWindowApps(Set<String> apps) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = this.mAms.getRunningAppProcesses();
        if (appProcessList != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                String[] pkNameList = appProcess.pkgList;
                for (String pkName : pkNameList) {
                    if (apps.contains(pkName)) {
                        removeRecentMagicWindowApp(pkName, 0);
                    }
                }
            }
        }
    }

    public void removeRecentMagicWindowApp(String pkgName, int display) {
        boolean needForceStopPkg = false;
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            List<ActivityManager.RecentTaskInfo> recentTasks = this.mAms.getRecentTasks(100, 1, this.mAms.getCurrentUserId());
            if (recentTasks != null) {
                for (ActivityManager.RecentTaskInfo recentTaskInfo : recentTasks) {
                    RecentTaskInfoEx recentTaskInfoEx = new RecentTaskInfoEx(recentTaskInfo);
                    if (recentTaskInfoEx.getRealActivity() != null && pkgName.equals(recentTaskInfoEx.getRealActivity().getPackageName())) {
                        int taskDisplayId = getDisplayId(recentTaskInfo.taskId);
                        SlogEx.d(TAG, "Remove. app:" + pkgName + " task:" + recentTaskInfo.taskId + " taskDisplayId:" + taskDisplayId + " display:" + display);
                        if (taskDisplayId == display) {
                            needForceStopPkg = true;
                            this.mAms.removeTask(recentTaskInfo.persistentId);
                            if (this.mMwManager.getLocalContainer() != null) {
                                this.mMagicWinSplitMng.removeReportLoginStatus(getJoinStr(pkgName, recentTaskInfoEx.getUserId()));
                            }
                        }
                    }
                }
            }
        }
        if (needForceStopPkg) {
            ActivityManagerServiceEx activityManagerServiceEx = this.mAms;
            activityManagerServiceEx.forceStopPackage(pkgName, activityManagerServiceEx.getCurrentUserId());
        }
    }

    private int getDisplayId(int taskId) {
        return ((Integer) Optional.ofNullable(this.mActivityTaskManager).map($$Lambda$HwMagicWinAmsPolicy$l3JLtgjTod5eu8Tw3yb7j8Azjc.INSTANCE).map(new Function(taskId) {
            /* class com.android.server.wm.$$Lambda$HwMagicWinAmsPolicy$OdGdv98Kd_sCBseEZXHYaA4xZY */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return ((RootActivityContainerEx) obj).anyTaskForId(this.f$0);
            }
        }).map($$Lambda$HwMagicWinAmsPolicy$kd97T0HXrBUt4aYdJjBuk4ZV18.INSTANCE).map($$Lambda$HwMagicWinAmsPolicy$rLQ1IwAUMZS50MjaeXpBLgl4tQ.INSTANCE).orElse(0)).intValue();
    }

    public boolean isSpecTransActivityPreDefined(HwMagicContainer container, ActivityRecordEx activity) {
        String clasName = Utils.getClassName(activity);
        if (container == null || activity == null || TRANSITION_ACTIVITIES.contains(clasName)) {
            return true;
        }
        HwMagicWindowConfig config = container.getConfig();
        String pkg = Utils.getPackageName(activity);
        if (!this.mMwManager.isSupportOpenCapability() || !config.getOpenCapAppConfigs().containsKey(pkg)) {
            return false;
        }
        return config.isSpecTransActivity(pkg, clasName);
    }

    public boolean isSpecTransActivity(HwMagicContainer container, ActivityRecordEx activity) {
        if (isSpecTransActivityPreDefined(container, activity) || activity.getMagicWindowPageType() != 1) {
            return true;
        }
        return false;
    }

    @SuppressLint({"SourceLockedOrientationActivity"})
    private void requestRotation(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container != null && container.isFoldableDevice() && activity.getActivityStackEx().getWindowingMode() == 1 && !activity.isFullScreenVideoInLandscape()) {
            if ((container.getOrientation() == 2) && container.getHwMagicWinEnabled(Utils.getRealPkgName(activity))) {
                activity.setRequestedOrientation(1);
            }
        }
    }

    public boolean isSupportLeftResume(HwMagicContainer container, ActivityRecordEx resumedActivity) {
        String packageName = Utils.getPackageName(resumedActivity);
        return LEFT_RESUME.contains(packageName) || container.getConfig().isLeftResume(packageName);
    }

    public /* synthetic */ void lambda$new$39$HwMagicWinAmsPolicy(List params, Bundle result) {
        SlogEx.i(TAG, "### Execute -> canPauseInHwMultiwin");
        IBinder resumedBinder = (IBinder) params.get(0);
        ActivityRecordEx next = ActivityRecordEx.forToken((IBinder) params.get(1));
        HwMagicContainer container = this.mMwManager.getContainer(next);
        if (container == null || !this.mMwManager.isSupportMultiResume(Utils.getPackageName(next))) {
            SlogEx.i(TAG, "canPauseInHwMagicWin : app is not support multi-resume");
            result.putBoolean("CAN_PAUSE", true);
            return;
        }
        ActivityRecordEx resumedActivity = ActivityRecordEx.forToken(resumedBinder);
        if (resumedActivity.isFinishing()) {
            SlogEx.i(TAG, "canPauseInHwMagicWin : app is finishing");
            return;
        }
        boolean isFocusSlaveChangeToMaster = this.mMwManager.isSlave(resumedActivity) && this.mMwManager.isMaster(next);
        boolean isFocusMasterChangeToSlave = this.mMwManager.isMaster(resumedActivity) && this.mMwManager.isSlave(next) && isSupportLeftResume(container, resumedActivity);
        if (isFocusSlaveChangeToMaster || isFocusMasterChangeToSlave) {
            result.putBoolean("CAN_PAUSE", false);
        } else {
            result.putBoolean("CAN_PAUSE", true);
        }
    }

    public void setMagicWindowToPause(ActivityRecordEx activity) {
        if (activity.isState(ActivityStackEx.ActivityState.RESUMED) && this.mMwManager.isSupportMultiResume(Utils.getPackageName(activity))) {
            setMagicWindowToPauseInner(activity);
        }
    }

    private void setMagicWindowToPauseInner(ActivityRecordEx activity) {
        if (activity != null && activity.getActivityStackEx() != null) {
            ActivityStackEx stack = activity.getActivityStackEx();
            if (stack.getPausingActivity() != null) {
                SlogEx.w(TAG, "activity has pausing in magic window");
                return;
            }
            stack.setPausingActivity(activity);
            activity.setState(ActivityStackEx.ActivityState.PAUSING, "pause activity in magic window");
            if (activity.attachedToProcess()) {
                try {
                    this.mActivityTaskManager.getLifecycleManager().scheduleTransaction(activity, PauseActivityItemEx.obtain(activity.isFinishing(), false, activity.getConfigChangeFlags(), false));
                } catch (Exception e) {
                    SlogEx.e(TAG, "RemoteException pause activity in magic window");
                    stack.setPausingActivity((ActivityRecordEx) null);
                }
            }
        }
    }

    public boolean isInHwDoubleWindow(HwMagicContainer container) {
        ActivityStackEx focusedStack = getFocusedTopStack(container);
        if (focusedStack == null) {
            return false;
        }
        ActivityRecordEx top = focusedStack.getTopActivity();
        if (this.mMwManager.isMaster(top) || this.mMwManager.isSlave(top)) {
            return true;
        }
        return false;
    }

    private ActivityRecordEx getActivityByNameOnTask(ActivityRecordEx focus, String actName) {
        if (actName.isEmpty()) {
            return null;
        }
        ArrayList<ActivityRecordEx> actHistory = focus.getTaskRecordEx().getActivityRecordExs();
        for (int actIndex = actHistory.size() - 1; actIndex >= 0; actIndex--) {
            ActivityRecordEx activity = actHistory.get(actIndex);
            if (actName.equals(Utils.getClassName(activity)) && activity.getUserId() == focus.getUserId()) {
                return activity;
            }
        }
        return null;
    }

    private boolean isNeedStartOrMoveRight(ActivityRecordEx resumeActivity, String pkgName) {
        HwMagicContainer container = this.mMwManager.getContainer(resumeActivity);
        if (container == null) {
            return false;
        }
        boolean functionEnabled = isSupportMainRelatedMode(container, resumeActivity) && container.getHwMagicWinEnabled(pkgName);
        if (pkgName == null || !functionEnabled || isPkgInLogoffStatus(resumeActivity)) {
            SlogEx.i(TAG, "isNeedStartOrMoveRight return for package");
            return false;
        }
        ActivityStackEx resumeStack = resumeActivity.getTaskRecordEx().getStack();
        if ((resumeStack != null && resumeStack.getWindowingMode() != 1 && resumeStack.getWindowingMode() != 103) || this.mMwManager.isDragFullMode(resumeActivity)) {
            return false;
        }
        if ((container.getAppSupportMode(pkgName) == 0) || this.mMagicWinSplitMng.isPkgSpliteScreenMode(resumeActivity, true)) {
            return false;
        }
        if (container.isInMagicWinOrientation() || resumeActivity.getWindowingMode() == 103) {
            return true;
        }
        return false;
    }

    public boolean startRelateActivityIfNeed(ActivityRecordEx resumeActivity, boolean forceStart) {
        HwMagicContainer container = this.mMwManager.getContainer(resumeActivity);
        if (container == null) {
            return false;
        }
        String pkgName = Utils.getPackageName(resumeActivity);
        if (isNeedStartOrMoveRight(resumeActivity, pkgName)) {
            String relateActName = container.getConfig().getRelateActivity(pkgName);
            ActivityRecordEx relatedAr = getActivityByNameOnTask(resumeActivity, relateActName);
            ActivityRecordEx slaveAr = getActivityByPosition(resumeActivity, 2, 0);
            SlogEx.i(TAG, "startRelateActivityIfNeed resumeActivity=" + resumeActivity + " right=" + slaveAr);
            boolean isRelatedActivityFinishing = relatedAr != null && relatedAr.isFinishing();
            if (((relatedAr == null || isRelatedActivityFinishing) && slaveAr == null && isMainActivity(container, resumeActivity)) || forceStart) {
                startRelateActivity(pkgName, relateActName, resumeActivity);
                return true;
            } else if (relatedAr != null) {
                SlogEx.d(TAG, "startRelateActivityIfNeed no existing right activity, move empty activity to right");
                Rect slaveBound = container.getBounds(2, pkgName);
                if (container.getBoundsPosition(relatedAr.getRequestedOverrideBounds()) != 2) {
                    relatedAr.setBounds(slaveBound);
                }
            }
        }
        return false;
    }

    private void startRelateActivity(String pkgName, String relateActName, ActivityRecordEx mainAr) {
        if (!mainAr.getActivityStackEx().equalsStack(getFocusedTopStack(this.mMwManager.getContainer(mainAr)))) {
            SlogEx.w(TAG, "startRelateActivity is not focused top stack");
            return;
        }
        Intent intent = new Intent();
        intent.setClassName(pkgName, relateActName);
        intent.setFlags(268435456);
        Message msg = this.mMwManager.getHandler().obtainMessage(13);
        msg.obj = intent;
        msg.arg1 = mainAr.getUserId();
        msg.arg2 = mainAr.getDisplayEx().getDisplayId();
        SlogEx.i(TAG, "start relate activity " + msg.arg1 + " in display " + msg.arg2);
        this.mMwManager.getHandler().removeMessages(13);
        this.mMwManager.getHandler().sendMessage(msg);
    }

    public boolean isPkgInLoginStatus(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container == null || activity == null) {
            return false;
        }
        String pkgName = Utils.getRealPkgName(activity);
        if (!this.mMagicWinSplitMng.isInLoginStatus(getJoinStr(pkgName, activity.getUserId())) || !container.getConfig().isSupportAppTaskSplitScreen(pkgName)) {
            return false;
        }
        return true;
    }

    public boolean isPkgInLogoffStatus(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container == null || activity == null) {
            return false;
        }
        String pkgName = Utils.getRealPkgName(activity);
        if (this.mMagicWinSplitMng.isInLoginStatus(getJoinStr(pkgName, activity.getUserId())) || !container.getConfig().isSupportAppTaskSplitScreen(pkgName)) {
            return false;
        }
        return true;
    }

    public /* synthetic */ void lambda$new$40$HwMagicWinAmsPolicy(List params, Bundle result) {
        TaskRecordEx task = new TaskRecordEx();
        task.resetTaskRecord(params.get(0));
        Rect leftBounds = (Rect) params.get(1);
        Rect rightBounds = (Rect) params.get(2);
        int dragMode = ((Integer) params.get(3)).intValue();
        HwMagicContainer container = this.mMwManager.getContainer(task.getTopActivity());
        if (container != null) {
            if (this.mMwManager.isDragFullMode(dragMode)) {
                executeDragWindow(task, leftBounds, rightBounds, dragMode);
                return;
            }
            Rect[] newBounds = container.getConfig().adjustBoundsForResize(leftBounds, rightBounds);
            if (newBounds != null && newBounds.length > 1) {
                executeDragWindow(task, newBounds[0], newBounds[1], dragMode);
            }
        }
    }

    private void executeDragWindow(TaskRecordEx task, Rect leftBounds, Rect rightBounds, int dragMode) {
        Rect rightBounds2;
        Rect leftBounds2;
        Throwable th;
        String dragPkgName;
        Rect tmpLeftBounds;
        HwMagicContainer container = this.mMwManager.getContainer(task.getTopActivity());
        if (container != null) {
            Rect inLeftBound = new Rect(leftBounds);
            Rect inRightBound = new Rect(rightBounds);
            Rect tmpRightBounds = null;
            if (!this.mMwManager.isDragFullMode(dragMode)) {
                leftBounds2 = leftBounds;
                rightBounds2 = rightBounds;
            } else if (dragMode == 5) {
                leftBounds2 = leftBounds;
                rightBounds2 = leftBounds;
            } else {
                leftBounds2 = rightBounds;
                rightBounds2 = rightBounds;
            }
            synchronized (this.mActivityTaskManager.getGlobalLock()) {
                try {
                    dragPkgName = null;
                    tmpLeftBounds = null;
                    for (int activityNdx = task.getActivityRecordExs().size() - 1; activityNdx >= 0; activityNdx--) {
                        try {
                            ActivityRecordEx r = (ActivityRecordEx) task.getActivityRecordExs().get(activityNdx);
                            if (!r.isFinishing() && r.getAppEx() != null) {
                                if (this.mMwManager.isMaster(r)) {
                                    setWindowBoundsLocked(r, container.getConfig().isRtl() ? rightBounds2 : leftBounds2);
                                    dragPkgName = r.getPackageName();
                                    tmpLeftBounds = leftBounds2;
                                } else if (this.mMwManager.isSlave(r)) {
                                    String dragPkgName2 = r.getPackageName();
                                    try {
                                        setWindowBoundsLocked(r, container.getConfig().isRtl() ? leftBounds2 : rightBounds2);
                                        dragPkgName = dragPkgName2;
                                        tmpRightBounds = rightBounds2;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                }
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            throw th;
                        }
                    }
                    try {
                        executeDragWindowForFullMode(task, inLeftBound, inRightBound, dragMode, container);
                    } catch (Throwable th4) {
                        th = th4;
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
            }
            HwMagicWinStatistics.getInstance(container.getType()).startTick(container.getConfig(), dragPkgName, dragMode, "drag");
            Rect fullScreenBound = container.getBounds(5, false);
            if (tmpLeftBounds == null) {
                return;
            }
            if (!(tmpRightBounds == null || tmpLeftBounds.equals(fullScreenBound) || tmpRightBounds.equals(fullScreenBound) || tmpLeftBounds.top != tmpRightBounds.top)) {
                SlogEx.i(TAG, "execute drag for magic. left:" + tmpLeftBounds + ", right:" + tmpRightBounds);
                if (!this.mMwManager.isDragFullMode(dragMode)) {
                    container.getConfig().updateAppDragBounds(dragPkgName, tmpLeftBounds, tmpRightBounds, dragMode);
                    sendMsgToWriteSettingsXml("update_drag_mode");
                }
            }
        }
    }

    private void executeDragWindowForFullMode(TaskRecordEx task, Rect inLeftBound, Rect inRightBound, int dragMode, HwMagicContainer container) {
        if (this.mMwManager.isDragFullMode(dragMode)) {
            onEnterDragFullMode(task, dragMode, container);
        } else {
            onExitDragFullMode(task, inLeftBound, inRightBound, dragMode, container);
        }
        container.getAnimation().setAnimationNull();
    }

    private void onExitDragFullMode(TaskRecordEx task, Rect inLeftBound, Rect inRightBound, int dragMode, HwMagicContainer container) {
        ActivityRecordEx top = task.getTopActivity();
        if (this.mMwManager.isDragFullMode(top)) {
            String packageName = Utils.getPackageName(top);
            this.mMwManager.setDragFullMode(top, 0);
            container.getConfig().updateAppDragBounds(packageName, inLeftBound, inRightBound, dragMode);
            sendMsgToWriteSettingsXml("exit_drag_full_mode");
            moveSlavePageToFront(task);
            ArrayList<ActivityRecordEx> tempActivityList = getAllActivities(task.getStack());
            if (tempActivityList.size() == 1) {
                moveWindow(tempActivityList.get(0), 3);
                this.mMwManager.getUIController().updateMwWallpaperVisibility(this.mMwManager.isMiddle(tempActivityList.get(0)), container.getDisplayId(), false);
                return;
            }
            getMode(tempActivityList.get(0)).setActivityBoundByMode(tempActivityList, packageName, null);
            updateStackVisibility(top, true);
        }
    }

    private void onEnterDragFullMode(TaskRecordEx task, int dragMode, HwMagicContainer container) {
        ActivityRecordEx top = task.getTopActivity();
        if (this.mMwManager.getDragFullMode(top) != dragMode) {
            if (!this.mMwManager.isDragFullMode(top)) {
                moveMasterOrSlaveToFront(task, dragMode, container);
            }
            this.mMwManager.setDragFullMode(top, dragMode);
            top.setIsFromFullscreenToMagicWin(true);
        }
    }

    public void sendMsgToWriteSettingsXml(String reason) {
        Message msg = this.mMwManager.getHandler().obtainMessage(15);
        msg.obj = reason;
        this.mMwManager.getHandler().removeMessages(15);
        this.mMwManager.getHandler().sendMessage(msg);
    }

    public int getTaskPosition(String pkg, int taskId) {
        ActivityDisplayEx display;
        HwMagicContainer container = this.mMagicWinSplitMng.getMagicContainer(pkg, taskId);
        if (container == null) {
            return -1;
        }
        boolean isPkgSplitModeSupport = container.getConfig().isSupportAppTaskSplitScreen(pkg) && container.isInMagicWinOrientation();
        if ((isPkgSplitModeSupport && !this.mMagicWinSplitMng.isMainStackInMwMode(getTopActivity(container))) || (display = container.getActivityDisplay()) == null) {
            return -1;
        }
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStackEx stack = display.getChildAt(stackNdx);
                String packageName = Utils.getRealPkgName(stack.getTopActivity());
                TaskRecordEx taskRecord = stack.taskForIdLocked(taskId);
                if (packageName != null && pkg.equals(packageName)) {
                    if (taskRecord != null) {
                        if (isPkgSplitModeSupport && stack.getWindowingMode() == 1) {
                            stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
                        }
                        if (!stack.inHwMagicWindowingMode()) {
                            return -1;
                        }
                        int position = container.getBoundsPosition(stack.getRequestedOverrideBounds());
                        if (!container.getConfig().isSupportAppTaskSplitScreen(pkg) || position != 3) {
                            return position;
                        }
                        return 0;
                    }
                }
            }
            return -1;
        }
    }

    public void setTaskPosition(String pkg, int taskId, int targetPosition) {
        SlogEx.i(TAG, "setTaskPosition : taskId = " + taskId + " pkg = " + pkg + " pos = " + targetPosition);
        if (targetPosition != 1) {
            this.mMagicWinSplitMng.setTaskPosition(pkg, taskId, targetPosition);
        }
    }

    public void setLoginStatus(String pkg, int status, int uid) {
        this.mMagicWinSplitMng.setLoginStatus(getJoinStr(pkg, UserHandleEx.getUserId(uid)), status);
    }

    public int getStackUserId(ActivityStackEx stack) {
        int currentUserId = this.mAms.getCurrentUserId();
        if (stack == null) {
            return currentUserId;
        }
        ActivityRecordEx topAr = stack.getTopActivity();
        return topAr == null ? currentUserId : topAr.getUserId();
    }

    public String getJoinStr(String pkg, int userId) {
        if (pkg == null) {
            return "" + userId;
        }
        return pkg + userId;
    }

    public boolean isInMagicWindowMode(int taskId) {
        ArrayList<ActivityDisplayEx> activityDisplays = new ArrayList<>();
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            if (this.mMwManager.getLocalContainer() != null) {
                activityDisplays.add(this.mMwManager.getLocalContainer().getActivityDisplay());
            }
            if (this.mMwManager.getVirtualContainer() != null) {
                activityDisplays.add(this.mMwManager.getVirtualContainer().getActivityDisplay());
            }
            Iterator<ActivityDisplayEx> it = activityDisplays.iterator();
            while (it.hasNext()) {
                ActivityDisplayEx display = it.next();
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStackEx stack = display.getChildAt(stackNdx);
                    if (stack.taskForIdLocked(taskId) != null) {
                        return stack.inHwMagicWindowingMode();
                    }
                }
            }
            return false;
        }
    }

    public void finishActivitiesAfterTopActivity(HwMagicContainer container) {
        ActivityStackEx stack;
        TaskRecordEx task;
        if (container != null && container.isFoldableDevice() && this.mContext.getResources().getConfiguration().orientation == 2 && (stack = getFocusedTopStack(container)) != null) {
            ActivityRecordEx topActivity = stack.getTopActivity();
            if (container.getHwMagicWinEnabled(Utils.getRealPkgName(topActivity)) && (task = topActivity.getTaskRecordEx()) != null) {
                Iterator it = task.getActivityRecordExs().iterator();
                while (it.hasNext()) {
                    ActivityRecordEx activity = (ActivityRecordEx) it.next();
                    if (activity.getCreateTime() > topActivity.getCreateTime()) {
                        stack.finishActivityLocked(activity, 0, (Intent) null, MAGIC_WINDOW_FINISH_EVENT, true, false);
                    }
                }
            }
        }
    }

    public /* synthetic */ void lambda$new$41$HwMagicWinAmsPolicy(List params, Bundle result) {
        TaskRecordEx task = new TaskRecordEx();
        task.resetTaskRecord(params.get(2));
        HwMagicContainer container = this.mMwManager.getContainer(task.getTopActivity());
        if (container != null) {
            float density = ((Float) params.get(1)).floatValue();
            this.mOrientationPolicy.changeOrientationForMultiWin(container, (Configuration) params.get(0), density);
        }
    }

    public /* synthetic */ void lambda$new$42$HwMagicWinAmsPolicy(List params, Bundle result) {
        int rotation = ((Integer) params.get(0)).intValue();
        HwMagicContainer container = this.mMwManager.getVirtualContainer();
        if (container != null) {
            container.getCameraRotation().updateSensorRotation(rotation);
        }
    }

    public /* synthetic */ void lambda$new$43$HwMagicWinAmsPolicy(List params, Bundle result) {
        ActivityRecordEx topActivity;
        HwMagicContainer container = this.mMwManager.getVirtualContainer();
        if (container != null && (topActivity = getTopActivity(container)) != null && !topActivity.inHwMagicWindowingMode()) {
            container.getCameraRotation().updateCameraRotation(0);
        }
    }

    public void pauseTopWhenScreenOff() {
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            ActivityRecordEx topActivity = getTopActivity(this.mMwManager.getLocalContainer());
            if (topActivity != null && topActivity.inHwMagicWindowingMode()) {
                if (!topActivity.isState(ActivityStackEx.ActivityState.RESUMED)) {
                    ActivityRecordEx slaveTop = getActivityByPosition(topActivity, 2, 0);
                    ActivityRecordEx masterTop = getActivityByPosition(topActivity, 1, 0);
                    if (slaveTop != null) {
                        if (masterTop != null) {
                            if (topActivity.equalsActivityRecord(slaveTop)) {
                                setMagicWindowToPauseInner(masterTop);
                            } else {
                                setMagicWindowToPauseInner(slaveTop);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isDefaultFullscreenActivity(HwMagicContainer container, ActivityRecordEx ar) {
        if (PRE_DEFINED_FULLSCREEN_PACKAGES.contains(Utils.getPackageName(ar))) {
            return true;
        }
        if (container == null || !container.getConfig().isDefaultFullscreenActivity(Utils.getPackageName(ar), Utils.getClassName(ar))) {
            return false;
        }
        return true;
    }

    public void forceStopPackage(String packageName) {
        Message msg = this.mMwManager.getHandler().obtainMessage(6);
        msg.obj = packageName;
        this.mMwManager.getHandler().sendMessage(msg);
    }

    public void finishInvisibleActivityInFullMode(ActivityRecordEx ar) {
        if (ar != null && this.mMwManager.isDragFullMode(ar)) {
            finishInvisibleActivityInFullModeInner(ar, false);
        }
    }

    public void forceFinishInvisibleActivityInFullMode(ActivityRecordEx ar) {
        if (ar != null) {
            finishInvisibleActivityInFullModeInner(ar, false);
        }
    }

    private void finishInvisibleActivityInFullModeInner(ActivityRecordEx ar, boolean isSkipSameAr) {
        List<ActivityRecordEx> activityRecords = getAllActivities(ar.getActivityStackEx());
        if (isSkipSameAr) {
            ActivityRecordEx.remove(activityRecords, ar);
        }
        for (ActivityRecordEx are : activityRecords) {
            if (are.isDelayFinished() && !are.isFinishing()) {
                are.getActivityStackEx().finishActivityLocked(are, 0, (Intent) null, MAGIC_WINDOW_FINISH_EVENT, true, false);
            }
        }
    }

    private void moveSlavePageToFront(TaskRecordEx task) {
        ArrayList<ActivityRecordEx> activities = task.getActivityRecordExs();
        for (int index = 0; index < activities.size(); index++) {
            ActivityRecordEx ar = activities.get(index);
            if (ar.isDelayFinished() && !ar.isFinishing()) {
                task.moveActivityToFrontLocked(ar);
                ar.setDelayFinished(false);
            }
        }
        this.mActivityTaskManager.getRootActivityContainer().resumeFocusedStacksTopActivities();
    }

    private void moveMasterOrSlaveToFront(TaskRecordEx task, int dragMode, HwMagicContainer container) {
        int targetPosition = dragMode == 5 ? 1 : 2;
        ArrayList<ActivityRecordEx> activities = task.getActivityRecordExs();
        ActivityRecordEx masterTop = getActivityByPosition(task.getTopActivity(), 1, 0);
        for (int index = 0; index < activities.size(); index++) {
            ActivityRecordEx ar = activities.get(index);
            if (container.getBoundsPosition(ar.getLastBound()) == targetPosition) {
                task.moveActivityToFrontLocked(ar);
            } else if (targetPosition == 1) {
                ar.setDelayFinished(true);
            }
        }
        if (targetPosition == 2 && masterTop != null) {
            setMagicWindowToPause(masterTop);
        }
        this.mActivityTaskManager.getRootActivityContainer().resumeFocusedStacksTopActivities();
    }

    public boolean isInAppSplitWinMode(ActivityRecordEx ar) {
        return this.mMagicWinSplitMng.isPkgSpliteScreenMode(ar, false);
    }

    public boolean isFullscreenWindow(HwMagicContainer container) {
        ActivityRecordEx activityRecord = getTopActivity(container);
        return activityRecord != null && activityRecord.inHwMagicWindowingMode() && this.mMwManager.isFull(activityRecord) && !Utils.getPackageName(activityRecord).equals(PERMISSION_PACKAGENAME);
    }

    public boolean isShowDragBar(HwMagicContainer container) {
        return isShowDragBar(getTopActivity(container));
    }

    public boolean isShowDragBar(ActivityRecordEx activity) {
        TaskRecordEx task;
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container == null || activity == null || !this.mMwManager.isFull(activity) || (task = activity.getTaskRecordEx()) == null || !this.mMwManager.isDragFullMode(activity)) {
            return false;
        }
        ArrayList<ActivityRecordEx> activities = task.getActivityRecordExs();
        for (int index = 0; index < activities.size(); index++) {
            ActivityRecordEx ar = activities.get(index);
            if ((isDefaultFullscreenActivity(container, ar) || ar.isFullScreenVideoInLandscape()) && !ar.isFinishing()) {
                return false;
            }
        }
        return true;
    }
}
