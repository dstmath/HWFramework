package com.android.server.mtm.utils;

import android.iawareperf.UniPerf;
import android.os.Process;
import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.dualfwk.AwareMiddleware;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppLruBase;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.FloatBallAssociate;
import com.android.server.rms.iaware.appmng.game.AwareGameStatus;
import com.android.server.rms.iaware.feature.AppAccurateRecgFeature;
import com.android.server.rms.iaware.feature.AppQuickStartFeature;
import com.android.server.rms.iaware.feature.AppSceneMngFeature;
import com.android.server.rms.iaware.memory.policy.CachedMemoryCleanPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.PressureDetector;
import com.huawei.android.os.ProcessExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class AppStatusUtils {
    private static final String ADJTYPE_CCH_BOUND_SERVICES = "cch-bound-services";
    private static final String ADJTYPE_CCH_EMPTY = "cch-empty";
    private static final String ADJTYPE_FG_LOCATION_SERVICE = "fg-service-location";
    private static final String ADJTYPE_PREVIOUS = "previous";
    private static final String ADJTYPE_RECENT_PROVIDER = "recent-provider";
    private static final String ADJTYPE_SERVICE = "service";
    private static final long BLUETOOTH_PROTECT_TIME = 120000;
    private static final String CPU_FILE = "/sys/devices/system/cpu";
    private static final String FG_SERVICE = "fg-service";
    private static final int MIN_CPU_COUNT = 4;
    private static final int PREVIOUS_FLAG = 1;
    private static final int RECENT_PROVIDER_FLAG = 2;
    private static final Object STATUS_LOCK = new Object();
    private static final String TAG = "AppStatusUtils";
    private volatile SparseArray<AwareProcessInfo> mAllProcNeedSort;
    private volatile Set<String> mBlindPkg;
    private int mCurrentUserId;
    private long mExpireTimeNanos;
    private volatile List<String> mFloatBall;
    private volatile SparseSet mForeGroundAssocPid;
    private volatile SparseSet mForeGroundPids;
    private volatile SparseArray<List<String>> mForeGroundUid;
    private volatile List<String> mGcmAppList;
    private final ArrayMap<Integer, Integer> mGooglePreviousCache;
    private final HwActivityManagerService mHwAms;
    private boolean mIsTargetOreo;
    private final AwareAppKeyBackgroup mKeyBackgroupInstance;
    private volatile SparseSet mKeyPercepServicePid;
    private volatile Set<String> mPkgsHaveMainProcAlive;
    private volatile Set<AwareAppLruBase> mPrevAwareBase;
    private volatile Set<String> mRestrainedVisWinList;
    private volatile long mUpdateTime;
    private volatile Set<String> mVisibleWindowList;
    private volatile Set<String> mWidgetList;
    private volatile Map<Status, Predicate<AwareProcessInfo>> predicates;

    public enum Status {
        NOT_IMPORTANT("not_important"),
        PERSIST("Persist"),
        TOP_ACTIVITY("TopActivity"),
        VISIBLE_APP("VisApp"),
        FOREGROUND("Fground"),
        VISIBLEWIN("VisWin"),
        WIDGET("Widget"),
        ASSOC_WITH_FG("FgAssoc"),
        KEYBACKGROUND("KeyBground"),
        BACKUP_APP("BackApp"),
        HEAVY_WEIGHT_APP("HeaWeiApp"),
        PREV_ONECLEAN("RecTask"),
        MUSIC_PLAY("Music"),
        SOUND_RECORD("Record"),
        GUIDE("Guide"),
        DOWN_UP_LOAD("Download"),
        KEY_SYS_SERVICE("KeySys"),
        HEALTH("Health"),
        PREVIOUS("LastUse"),
        SYSTEM_APP("SysApp"),
        NON_CURRENT_USER("non_cur_user"),
        LAUNCHER("Launcher"),
        INPUT_METHOD("InputMethed"),
        WALLPAPER("WallPaper"),
        FROZEN("Frozen"),
        BLUETOOTH("Btooth"),
        TOAST_WINDOW("ToastWin"),
        FOREGROUND_APP("FgApp"),
        BLIND("Blind"),
        MUSIC_INSTANT("MusicINS"),
        NONSYSTEMUSER("NonSystemUser"),
        GCM("gcm"),
        INSMALLSAMPLELIST("InSmallSampleList"),
        ACHSCRCHANGEDNUM("AchScrChangedNum"),
        SCREENRECORD("ScreenRecord"),
        CAMERARECORD("CameraRecord"),
        RESTRAINED_VIS_WIN("RestrainedVisWin"),
        OVERSEA_APP("OverseaApp"),
        DOWNLOAD_FREEZE("DownloadFreeze"),
        PROC_STATE_CACHED("PSCached"),
        REG_KEEP_ALIVER_APP("RegKeepAlive"),
        TOP_IM_APP_BASE("TopImBase"),
        BLUETOOTH_CONNECT("BtoothConnect"),
        BLUETOOTH_LAST("BtoothLast"),
        ADJ_CACHED("AdjCached"),
        BLOOTH_OFF("BloothOff"),
        SIM_CHINA("SimChina"),
        DTB_BIND("DtbBind"),
        HW_INSTALL_APP("HwInstallApp"),
        WEBVIEW_HAS_MAIN_PROC_ALIVE("WebViewHasMainProcAlive"),
        MAIN_PROC("MainProc"),
        UID_IN_FG_UID("UidInFgUids"),
        CACHED_CAN_CLEAN_ADJ_TYPE("CachedCanCleanAdjType"),
        CACHED_PROTECT("CachedProtect"),
        PKG_DOWN_UP_LOAD("PkgDownload"),
        IN_GAMING("Gaming"),
        FLOATING_BALL("FloatingBall");
        
        private String mDescription;

        private Status(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private AppStatusUtils() {
        this.mHwAms = HwActivityManagerService.self();
        this.mKeyBackgroupInstance = AwareAppKeyBackgroup.getInstance();
        this.mGooglePreviousCache = new ArrayMap<>();
        this.predicates = new HashMap();
        this.mAllProcNeedSort = null;
        this.mForeGroundUid = null;
        this.mForeGroundAssocPid = null;
        this.mForeGroundPids = null;
        this.mKeyPercepServicePid = null;
        this.mVisibleWindowList = null;
        this.mRestrainedVisWinList = null;
        this.mWidgetList = null;
        this.mBlindPkg = new ArraySet();
        this.mUpdateTime = -1;
        this.mPrevAwareBase = null;
        this.mGcmAppList = null;
        this.mPkgsHaveMainProcAlive = null;
        this.mFloatBall = null;
        this.mIsTargetOreo = SystemPropertiesEx.getBoolean("persist.sys.iaware.debug.download.target.o", false);
        this.mExpireTimeNanos = 30000000;
        this.mCurrentUserId = 0;
        initPredicates();
        initPredicatesEx();
        initPredicatesOthers();
        initPredicatesOthersEx();
        long totalRam = ProcessExt.getTotalMemory() / MemoryConstant.GB_SIZE;
        long cpuCount = (long) getCpuCounts();
        float coreFactor = 1.0f;
        float memFactor = totalRam > 8 ? 0.5f : totalRam < 2 ? 1.0f : 6.0f / (((float) totalRam) + 4.0f);
        float cpuFactor = cpuCount > 8 ? 0.2f : cpuCount < 4 ? 1.0f : 1.0f / ((float) (cpuCount - 3));
        long cpuCapability = getCpuCapability(PressureDetector.PSI_THRESHOLD_MAX) - getCpuCapability(0);
        if (cpuCapability > 0) {
            if (cpuCapability < 20000) {
                coreFactor = 0.2f;
            } else if (cpuCapability <= 80000) {
                coreFactor = ((float) (cpuCapability - 5000)) / 75000.0f;
            }
            cpuFactor = coreFactor * ((0.625f * cpuFactor) + 0.375f);
        }
        this.mExpireTimeNanos = (((long) (80.0f * memFactor * cpuFactor)) + 30) * 1000 * 1000;
    }

    private void initPredicates() {
        this.predicates.put(Status.PERSIST, $$Lambda$AppStatusUtils$lb8SJxzeyQhggDjfulZ2u7QZmyc.INSTANCE);
        this.predicates.put(Status.TOP_ACTIVITY, $$Lambda$AppStatusUtils$T41BqTBsszutBC7t2M7WZrTBG7Q.INSTANCE);
        this.predicates.put(Status.VISIBLE_APP, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$kz9bx9w1WQoIFLOnCRE8tSyCz14 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$2$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.FOREGROUND, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$4ASU84cioRf_RFkBAy_sb3UE32o */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$3$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.VISIBLEWIN, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$_TnL5tyeZ9S5aSdrO79fST8zo7s */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$4$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.WIDGET, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$av9yKP0PN7zqb1PeqEMrllMeMIk */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$5$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.ASSOC_WITH_FG, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$PxRBfNtGPq8wbHZ5BXhqu7YSTPc */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$6$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.KEYBACKGROUND, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$pCaNJ_Q8eW_PIWv0rekpLw50pgQ */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$7$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.BACKUP_APP, $$Lambda$AppStatusUtils$K2Hhf5B1WKwGMfAto_jzRtvkOdA.INSTANCE);
        this.predicates.put(Status.HEAVY_WEIGHT_APP, $$Lambda$AppStatusUtils$tJwOIrrjZYs_1B72a9UnQtQbVXU.INSTANCE);
        this.predicates.put(Status.MUSIC_PLAY, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$m8p9aW8VBbshqEtqixapX7NXfKE */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$10$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.SOUND_RECORD, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$XP8m1m_0eMV8m2JcUXIvO8IeB_8 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$11$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.GUIDE, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$14CYfCZYfNVHDIHAUcluHX8rndc */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$12$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.DOWN_UP_LOAD, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$m4RCVNUoozfWWsktQGMTfJLWW6Y */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicates$13$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
    }

    static /* synthetic */ boolean lambda$initPredicates$0(AwareProcessInfo param) {
        return param.procProcInfo.mCurAdj < HwActivityManagerService.FOREGROUND_APP_ADJ;
    }

    static /* synthetic */ boolean lambda$initPredicates$1(AwareProcessInfo param) {
        return param.procProcInfo.mCurAdj == HwActivityManagerService.FOREGROUND_APP_ADJ;
    }

    public /* synthetic */ boolean lambda$initPredicates$2$AppStatusUtils(AwareProcessInfo param) {
        return isAdjVisible(param) && !shouldFilterVisibleApp(param) && !lambda$initPredicatesEx$25$AppStatusUtils(param);
    }

    public /* synthetic */ boolean lambda$initPredicates$3$AppStatusUtils(AwareProcessInfo param) {
        return param.procProcInfo.mCurAdj < HwActivityManagerService.VISIBLE_APP_ADJ || (isAdjVisible(param) && !shouldFilterVisibleApp(param));
    }

    static /* synthetic */ boolean lambda$initPredicates$8(AwareProcessInfo param) {
        return param.procProcInfo.mCurAdj == HwActivityManagerService.BACKUP_APP_ADJ;
    }

    static /* synthetic */ boolean lambda$initPredicates$9(AwareProcessInfo param) {
        return param.procProcInfo.mCurAdj == HwActivityManagerService.HEAVY_WEIGHT_APP_ADJ;
    }

    public /* synthetic */ boolean lambda$initPredicates$10$AppStatusUtils(AwareProcessInfo param) {
        return checkKeyBackgroupByState(2, param);
    }

    public /* synthetic */ boolean lambda$initPredicates$11$AppStatusUtils(AwareProcessInfo param) {
        return checkKeyBackgroupByState(1, param);
    }

    public /* synthetic */ boolean lambda$initPredicates$12$AppStatusUtils(AwareProcessInfo param) {
        return checkKeyBackgroupByState(3, param);
    }

    private void initPredicatesEx() {
        this.predicates.put(Status.KEY_SYS_SERVICE, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$YZs8_g1ZCvvMSux_u1JMjE07TK8 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$14$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.HEALTH, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$mvQ6t18TYVlopGssoe3U7BwBL4 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$15$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.PREVIOUS, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$Nf7XX0lZyUQBdZ2VOm_bZJJgHtI */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$16$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.SYSTEM_APP, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$YW4aqhY2z1aiSc6tCQ8JAd29Sc */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$17$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.NON_CURRENT_USER, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$CWM_qxf2SuxAf5LllGp903Og */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$18$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.LAUNCHER, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$UvEjhxkhFXkDSY2EWBmoSRav_kU */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$19$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.INPUT_METHOD, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$HCHr3zrX4UPdFq2AnnpZ234cONI */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$20$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.WALLPAPER, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$9DWerzW3csYzkTikhVrX4QEiGBk */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$21$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.FROZEN, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$VP2jXXcwCmRBC70asY53mtGKgc */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$22$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.BLUETOOTH, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$Dj_e4PWiKNTjS62M_R2syXrJgko */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$23$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.TOAST_WINDOW, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$i0ViIANhUKXnkbFOnW8hMpH4s8 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$24$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.FOREGROUND_APP, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$12ETyKvXB2pSXKckjFZGhytwKig */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$25$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.BLIND, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$7ttCYHe9IRnXBoiiMcrTrqkZjAo */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$26$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.MUSIC_INSTANT, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$ICF7nWEZckzsZlJ4F23bcZbhK34 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$27$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.NONSYSTEMUSER, $$Lambda$AppStatusUtils$AMRUNX7hhUrxr_vj2FcJCaX1hXQ.INSTANCE);
        this.predicates.put(Status.GCM, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$40LEfCjOxdqdNC33f3UOSR09woE */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesEx$29$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
    }

    public /* synthetic */ boolean lambda$initPredicatesEx$15$AppStatusUtils(AwareProcessInfo param) {
        return checkKeyBackgroupByState(4, param);
    }

    private void initPredicatesOthers() {
        this.predicates.put(Status.INSMALLSAMPLELIST, $$Lambda$AppStatusUtils$ZMMQKb6wmZOD6JwXkL6q21Zhzb4.INSTANCE);
        this.predicates.put(Status.ACHSCRCHANGEDNUM, $$Lambda$AppStatusUtils$jdmznKBbHqu5WXg4TewqpfHiYLs.INSTANCE);
        this.predicates.put(Status.SCREENRECORD, $$Lambda$AppStatusUtils$496AO4kGzuAfgLhBZ4VIQQqRPMk.INSTANCE);
        this.predicates.put(Status.CAMERARECORD, $$Lambda$AppStatusUtils$38eTCjF3haftb21k8XlOS3nMTLo.INSTANCE);
        this.predicates.put(Status.RESTRAINED_VIS_WIN, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$wQj9U3nQQdlSjSYabdkb06Qkjc8 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthers$34$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.OVERSEA_APP, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$C4M02NaKnsVO00tVTC9NHT2zhNQ */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthers$35$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.DOWNLOAD_FREEZE, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$j19d6t_u82ENdMd1203ZDS89Oqo */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthers$36$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.PROC_STATE_CACHED, $$Lambda$AppStatusUtils$Aq0ai3oGQk4hJLqnG_yj20Z0Ds.INSTANCE);
        this.predicates.put(Status.REG_KEEP_ALIVER_APP, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$8Ad7V_ZJ0T9qPn3P2UogAVPRnsE */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthers$38$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.TOP_IM_APP_BASE, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$NQgYYYRYhitCt4sLX3yOriK_2BY */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthers$39$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.BLUETOOTH_CONNECT, $$Lambda$AppStatusUtils$0uLEXR0oNGi5iv4ijv7SmOmtSY.INSTANCE);
        this.predicates.put(Status.BLUETOOTH_LAST, $$Lambda$AppStatusUtils$Z6qTDdZKkG5zKP5Oku2ObkIlCiA.INSTANCE);
        this.predicates.put(Status.ADJ_CACHED, $$Lambda$AppStatusUtils$22JjKy6gSUzKGEXK0JCZl3tcDY.INSTANCE);
        this.predicates.put(Status.BLOOTH_OFF, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$A9YluswBXyfnmyodbZ870qDjU0 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthers$43$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.SIM_CHINA, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$ia57bXxbZOxLLeEoaVtCvZuNuFU */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthers$44$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.DTB_BIND, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$VCdpAHlSxl4cJITd08gZA9XY6x0 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthers$45$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
    }

    static /* synthetic */ boolean lambda$initPredicatesOthers$37(AwareProcessInfo param) {
        return param.procProcInfo.mSetProcState > 15;
    }

    public /* synthetic */ boolean lambda$initPredicatesOthers$38$AppStatusUtils(AwareProcessInfo param) {
        return AwareIntelligentRecg.getInstance().isCurUserKeepALive(getMainPkgName(param), param.procProcInfo.mUid);
    }

    public /* synthetic */ boolean lambda$initPredicatesOthers$39$AppStatusUtils(AwareProcessInfo param) {
        return AwareIntelligentRecg.getInstance().isTopImAppBase(getMainPkgName(param));
    }

    static /* synthetic */ boolean lambda$initPredicatesOthers$42(AwareProcessInfo param) {
        return param.procProcInfo.mCurAdj >= HwActivityManagerService.CACHED_APP_MIN_ADJ;
    }

    public /* synthetic */ boolean lambda$initPredicatesOthers$43$AppStatusUtils(AwareProcessInfo param) {
        return checkBloothOff();
    }

    public /* synthetic */ boolean lambda$initPredicatesOthers$44$AppStatusUtils(AwareProcessInfo param) {
        return checkSimChina();
    }

    private void initPredicatesOthersEx() {
        this.predicates.put(Status.HW_INSTALL_APP, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$bp3tjeJtBdv2FtY2HC6AgNWLuGM */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthersEx$46$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.WEBVIEW_HAS_MAIN_PROC_ALIVE, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$de1yjZONn153MVl3JwxT0RrVL2E */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthersEx$47$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.MAIN_PROC, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$NGdZ9A9jiCcPwThHU5CpnUu2cs */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthersEx$48$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.UID_IN_FG_UID, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$mQ2EqyaXMhPI5awvInjvUT4k */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthersEx$49$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.CACHED_CAN_CLEAN_ADJ_TYPE, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$RcEM2O97OIq52PNdojKKIrgYP98 */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthersEx$50$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.CACHED_PROTECT, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$AHt3OBN58aJgWA1VsKJhwonW8o */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthersEx$51$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.PKG_DOWN_UP_LOAD, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$sfGccmwSjW0XOCjMsYNib63Pbtc */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthersEx$52$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.IN_GAMING, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$1SxAurD72IC8SvvuRMEkJfRX9AY */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthersEx$53$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.FLOATING_BALL, new Predicate() {
            /* class com.android.server.mtm.utils.$$Lambda$AppStatusUtils$ilPzp4SxlhI_h4GpIUhv66K4Uw */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppStatusUtils.this.lambda$initPredicatesOthersEx$54$AppStatusUtils((AwareProcessInfo) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    public static class SingletonHolder {
        private static AppStatusUtils sInstance = new AppStatusUtils();

        private SingletonHolder() {
        }
    }

    public static AppStatusUtils getInstance() {
        return SingletonHolder.sInstance;
    }

    public boolean checkAppStatus(Status statusType, AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            AwareLog.e(TAG, "null AwareProcessInfo input!");
            return false;
        }
        Predicate<AwareProcessInfo> func = this.predicates.get(statusType);
        if (func == null) {
            AwareLog.e(TAG, "error status type input!");
            return false;
        } else if (!getNewProcesses()) {
            AwareLog.e(TAG, "update processes status failed!");
            return false;
        } else {
            SparseArray<AwareProcessInfo> tmpAllProcNeedSort = this.mAllProcNeedSort;
            if (tmpAllProcNeedSort == null || tmpAllProcNeedSort.size() == 0) {
                AwareLog.e(TAG, "mAllProcNeedSort is null");
                return false;
            }
            AwareProcessInfo newAwareProcInfo = tmpAllProcNeedSort.get(awareProcInfo.procPid);
            if (newAwareProcInfo == null) {
                newAwareProcInfo = awareProcInfo;
            }
            if (newAwareProcInfo.procProcInfo == null) {
                AwareLog.e(TAG, "null ProcessInfo input!");
                return false;
            } else if (newAwareProcInfo.procProcInfo.mCurAdj != -1) {
                return func.test(newAwareProcInfo);
            } else {
                AwareLog.e(TAG, "mCurAdj of ProcessInfo not set!");
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: checkSystemApp */
    public boolean lambda$initPredicatesEx$17$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        int uid = awareProcInfo.procProcInfo.mUid;
        int tmpCurrentUserId = this.mCurrentUserId;
        if (uid <= 10000) {
            return true;
        }
        if (uid <= tmpCurrentUserId * 100000 || uid > (100000 * tmpCurrentUserId) + 10000) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkNonCurrentUser */
    public boolean lambda$initPredicatesEx$18$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        return !AwareIntelligentRecg.getInstance().isCurrentUser(awareProcInfo.procProcInfo.mUid, this.mCurrentUserId);
    }

    /* access modifiers changed from: private */
    /* renamed from: checkAssocWithFg */
    public boolean lambda$initPredicates$6$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        SparseArray<List<String>> tmpForeGroundUid = this.mForeGroundUid;
        SparseSet tmpForeGroundAssocPid = this.mForeGroundAssocPid;
        if (tmpForeGroundUid == null) {
            return false;
        }
        if ((tmpForeGroundUid.indexOfKey(awareProcInfo.procProcInfo.mUid) < 0 && !tmpForeGroundAssocPid.contains(awareProcInfo.procProcInfo.mPid)) || awareProcInfo.procProcInfo.mForegroundActivities) {
            return false;
        }
        if (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.procProcInfo.mUid)) {
            return true;
        }
        return isPkgIncludeForTgt(awareProcInfo.procProcInfo.mPackageName, tmpForeGroundUid.get(awareProcInfo.procProcInfo.mUid));
    }

    /* access modifiers changed from: private */
    /* renamed from: checkKeySysProc */
    public boolean lambda$initPredicatesEx$14$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        if (procInfo.mType != 2) {
            return false;
        }
        if (procInfo.mCurAdj == HwActivityManagerService.SERVICE_ADJ) {
            return true;
        }
        boolean condition = !awareProcInfo.procHasShownUi && procInfo.mCreatedTime != -1;
        long timeCost = SystemClock.elapsedRealtime() - procInfo.mCreatedTime;
        if (procInfo.mUid < 10000) {
            if (procInfo.mCurAdj == HwActivityManagerService.SERVICE_B_ADJ) {
                return true;
            }
            if (condition && timeCost < AppMngConfig.getKeySysDecay()) {
                return true;
            }
        } else if (condition && timeCost < AppMngConfig.getSysDecay()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkVisibleWindow */
    public boolean lambda$initPredicates$4$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        Set<String> tmpVisibleWindowList = this.mVisibleWindowList;
        if (tmpVisibleWindowList == null || tmpVisibleWindowList.isEmpty()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (tmpVisibleWindowList.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkRestrainedVisWin */
    public boolean lambda$initPredicatesOthers$34$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        Set<String> tmpRestrainedVisWinList = this.mRestrainedVisWinList;
        if (tmpRestrainedVisWinList == null || tmpRestrainedVisWinList.isEmpty()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (tmpRestrainedVisWinList.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkWidget */
    public boolean lambda$initPredicates$5$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        Set<String> tmpWidgetList = this.mWidgetList;
        if (tmpWidgetList == null || tmpWidgetList.isEmpty()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (tmpWidgetList.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkKeyBackGround */
    public boolean lambda$initPredicates$7$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        if (procInfo.mCurAdj != HwActivityManagerService.PERCEPTIBLE_APP_ADJ) {
            return false;
        }
        if (!"fg-service".equals(procInfo.mAdjType) && !"service".equals(procInfo.mAdjType) && !ADJTYPE_FG_LOCATION_SERVICE.equals(procInfo.mAdjType)) {
            return true;
        }
        if ("fg-service".equals(procInfo.mAdjType) && "com.huawei.screenrecorder".equals(procInfo.mProcessName)) {
            return true;
        }
        SparseSet tmpKeyPercepServicePid = this.mKeyPercepServicePid;
        if (tmpKeyPercepServicePid == null || !tmpKeyPercepServicePid.contains(procInfo.mPid)) {
            return false;
        }
        if (!AppSceneMngFeature.isEnable() || !isIsolatedProcessUid(UserHandleEx.getAppId(procInfo.mUid))) {
            return true;
        }
        return false;
    }

    private boolean isIsolatedProcessUid(int uid) {
        return uid >= 99000 && uid <= 99999;
    }

    private boolean hasFlag(int value, int flag) {
        return (value & flag) == flag;
    }

    private boolean isInvalidGooglePrevious(AwareProcessInfo awareProcInfo) {
        int appUid = awareProcInfo.procProcInfo.mAppUid;
        if (ADJTYPE_PREVIOUS.equals(awareProcInfo.procProcInfo.mAdjType)) {
            return false;
        }
        synchronized (this.mGooglePreviousCache) {
            Integer flag = this.mGooglePreviousCache.get(Integer.valueOf(appUid));
            if (flag == null) {
                return false;
            }
            if (hasFlag(flag.intValue(), 1) || !hasFlag(flag.intValue(), 2)) {
                return false;
            }
            return true;
        }
    }

    private boolean isGooglePrevious(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo.procProcInfo.mCurAdj != HwActivityManagerService.PREVIOUS_APP_ADJ) {
            return false;
        }
        if (!AppAccurateRecgFeature.isEnable() || !isInvalidGooglePrevious(awareProcInfo)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkPrevious */
    public boolean lambda$initPredicatesEx$16$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        AwareProcessInfo prevProcInfo;
        if (isGooglePrevious(awareProcInfo)) {
            return true;
        }
        List<String> packageNames = awareProcInfo.procProcInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty()) {
            return false;
        }
        Set<AwareAppLruBase> tmpAppLruBase = new ArraySet<>(this.mPrevAwareBase);
        SparseArray<AwareProcessInfo> allProcNeedSort = this.mAllProcNeedSort;
        if (allProcNeedSort == null || allProcNeedSort.size() == 0) {
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return false;
        }
        for (AwareAppLruBase app : tmpAppLruBase) {
            if (awareProcInfo.procProcInfo.mUid == app.procUid && (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.procProcInfo.mUid) || ((prevProcInfo = allProcNeedSort.get(app.procPid)) != null && isPkgIncludeForTgt(packageNames, prevProcInfo.procProcInfo.mPackageName)))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkFloatingBall */
    public boolean lambda$initPredicatesOthersEx$54$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        List<String> packageNames;
        if (this.mFloatBall == null || this.mFloatBall.size() == 0 || awareProcInfo.procProcInfo == null || (packageNames = awareProcInfo.procProcInfo.mPackageName) == null || packageNames.isEmpty()) {
            return false;
        }
        int userId = UserHandleEx.getUserId(awareProcInfo.procProcInfo.mUid);
        Iterator<String> it = packageNames.iterator();
        while (it.hasNext()) {
            List<String> list = this.mFloatBall;
            if (list.contains(it.next() + "," + userId)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkLauncher */
    public boolean lambda$initPredicatesEx$19$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        return AppQuickStartFeature.isLauncherCheckPid() ? awareProcInfo.procProcInfo.mPid == AwareAppAssociate.getInstance().getCurHomeProcessPid() : awareProcInfo.procProcInfo.mUid == AwareAppAssociate.getInstance().getCurHomeProcessUid();
    }

    private String getMainPkgName(AwareProcessInfo awareProcInfo) {
        List<String> packageNames = awareProcInfo.procProcInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty()) {
            return null;
        }
        return packageNames.get(0);
    }

    /* access modifiers changed from: private */
    /* renamed from: checkInputMethod */
    public boolean lambda$initPredicatesEx$20$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        String mainPackageName = getMainPkgName(awareProcInfo);
        if (mainPackageName == null || !mainPackageName.equals(AwareIntelligentRecg.getInstance().getDefaultInputMethod())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkWallPaper */
    public boolean lambda$initPredicatesEx$21$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        String mainPackageName = getMainPkgName(awareProcInfo);
        if (mainPackageName == null || !mainPackageName.equals(AwareIntelligentRecg.getInstance().getDefaultWallPaper())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkFrozen */
    public boolean lambda$initPredicatesEx$22$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isAppFrozen(awareProcInfo.procProcInfo.mUid);
    }

    /* access modifiers changed from: private */
    /* renamed from: checkBluetooth */
    public boolean lambda$initPredicatesEx$23$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isAppBluetooth(awareProcInfo.procProcInfo.mUid);
    }

    /* access modifiers changed from: private */
    /* renamed from: checkDownloadFrozen */
    public boolean lambda$initPredicatesOthers$36$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isNotBeClean(getMainPkgName(awareProcInfo));
    }

    /* access modifiers changed from: private */
    /* renamed from: checkToastWindow */
    public boolean lambda$initPredicatesEx$24$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isToastWindow(awareProcInfo.procProcInfo.mPid);
    }

    private boolean checkKeyBackgroupByState(int state, AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        AwareAppKeyBackgroup awareAppKeyBackgroup = this.mKeyBackgroupInstance;
        if (awareAppKeyBackgroup == null) {
            return false;
        }
        return awareAppKeyBackgroup.checkKeyBackgroupByState(state, procInfo.mPid, procInfo.mUid, procInfo.mPackageName);
    }

    private void updateGooglePreviousCache(ArrayMap<Integer, Integer> cache) {
        if (AppAccurateRecgFeature.isEnable()) {
            synchronized (this.mGooglePreviousCache) {
                this.mGooglePreviousCache.clear();
                this.mGooglePreviousCache.putAll((ArrayMap<? extends Integer, ? extends Integer>) cache);
            }
        }
    }

    private void addGooglePreviousIfNeed(ArrayMap<Integer, Integer> cache, int appUid, String adjType) {
        int flag;
        if (AppAccurateRecgFeature.isEnable()) {
            if (ADJTYPE_PREVIOUS.equals(adjType)) {
                flag = 1;
            } else if (ADJTYPE_RECENT_PROVIDER.equals(adjType)) {
                flag = 2;
            } else {
                return;
            }
            Integer flagOri = cache.get(Integer.valueOf(appUid));
            if (flagOri != null) {
                flag |= flagOri.intValue();
            }
            cache.put(Integer.valueOf(appUid), Integer.valueOf(flag));
        }
    }

    public boolean getNewProcesses() {
        if (SystemClock.elapsedRealtimeNanos() - this.mUpdateTime <= this.mExpireTimeNanos) {
            return true;
        }
        synchronized (STATUS_LOCK) {
            if (SystemClock.elapsedRealtimeNanos() - this.mUpdateTime <= this.mExpireTimeNanos) {
                return true;
            }
            if (this.mHwAms == null) {
                return false;
            }
            return updateNewProcesses();
        }
    }

    private boolean updateProcInfoByAwareProcInfo(ProcessInfo procInfo, AwareProcessBaseInfo updateInfo) {
        if (updateInfo == null) {
            return false;
        }
        procInfo.mCurAdj = updateInfo.curAdj;
        procInfo.mSetProcState = updateInfo.setProcState;
        procInfo.mForegroundActivities = updateInfo.foregroundActivities;
        procInfo.mAdjType = updateInfo.adjType;
        procInfo.mAppUid = updateInfo.appUid;
        procInfo.mTargetSdkVersion = updateInfo.targetSdkVersion;
        return true;
    }

    private boolean updateNewProcesses() {
        List<ProcessInfo> procInfosTmp = ProcessInfoCollector.getInstance().getProcessInfoList();
        Map<Integer, AwareProcessBaseInfo> baseInfosTmp = this.mHwAms.getAllProcessBaseInfo();
        if (procInfosTmp.isEmpty()) {
            return false;
        }
        if (baseInfosTmp.isEmpty()) {
            return false;
        }
        SparseArray<AwareProcessInfo> allProcNeedSortTmp = new SparseArray<>();
        SparseArray<List<String>> foreGroundUidTmp = new SparseArray<>();
        SparseSet foreGroundPidsTmp = new SparseSet();
        AwareAppAssociate.getInstance().getForeGroundApp(foreGroundPidsTmp);
        SparseSet foreGroundAssocPidTmp = new SparseSet();
        SparseSet keyPercepServicePidTmp = new SparseSet();
        SparseSet fgServiceUidTmp = new SparseSet();
        SparseSet importUidTmp = new SparseSet();
        SparseIntArray percepServicePidTmp = new SparseIntArray();
        ArrayMap<Integer, Integer> googlePreviousCache = new ArrayMap<>();
        int currentUserIdTmp = AwareAppAssociate.getInstance().getCurUserId();
        Iterator<ProcessInfo> it = procInfosTmp.iterator();
        while (it.hasNext()) {
            ProcessInfo procInfo = it.next();
            if (procInfo != null) {
                AwareProcessBaseInfo updateInfo = baseInfosTmp.get(Integer.valueOf(procInfo.mPid));
                if (updateProcInfoByAwareProcInfo(procInfo, updateInfo)) {
                    updateForeGroundUid(procInfo, foreGroundUidTmp, foreGroundAssocPidTmp, foreGroundPidsTmp);
                    AwareProcessInfo awareProcInfo = new AwareProcessInfo(procInfo.mPid, procInfo);
                    awareProcInfo.procHasShownUi = updateInfo.hasShownUi;
                    allProcNeedSortTmp.put(procInfo.mPid, awareProcInfo);
                    if (procInfo.mCurAdj < HwActivityManagerService.PERCEPTIBLE_APP_ADJ) {
                        importUidTmp.add(procInfo.mUid);
                    } else {
                        updatePerceptibleApp(procInfo, fgServiceUidTmp, percepServicePidTmp, importUidTmp);
                    }
                    if (procInfo.mCurAdj == HwActivityManagerService.PREVIOUS_APP_ADJ) {
                        addGooglePreviousIfNeed(googlePreviousCache, procInfo.mAppUid, procInfo.mAdjType);
                    }
                    it = it;
                    currentUserIdTmp = currentUserIdTmp;
                }
            }
        }
        updateKeyPercepServicePid(keyPercepServicePidTmp, percepServicePidTmp, importUidTmp, fgServiceUidTmp, allProcNeedSortTmp);
        this.mCurrentUserId = currentUserIdTmp;
        this.mForeGroundUid = foreGroundUidTmp;
        this.mForeGroundPids = foreGroundPidsTmp;
        this.mForeGroundAssocPid = foreGroundAssocPidTmp;
        this.mAllProcNeedSort = allProcNeedSortTmp;
        this.mKeyPercepServicePid = keyPercepServicePidTmp;
        updateNewProcessesEx(googlePreviousCache, allProcNeedSortTmp);
        return true;
    }

    private void updateKeyPercepServicePid(SparseSet keyPercepServicePidTmp, SparseIntArray percepServicePidTmp, SparseSet importUidTmp, SparseSet fgServiceUidTmp, SparseArray<AwareProcessInfo> allProcNeedSortTmp) {
        for (int i = percepServicePidTmp.size() - 1; i >= 0; i--) {
            int pid = percepServicePidTmp.keyAt(i);
            int uid = percepServicePidTmp.valueAt(i);
            if (importUidTmp.contains(uid)) {
                keyPercepServicePidTmp.add(pid);
            } else if (!fgServiceUidTmp.contains(uid)) {
                keyPercepServicePidTmp.add(pid);
            } else {
                SparseSet strong = new SparseSet();
                AwareAppAssociate.getInstance().getAssocClientListForPid(pid, strong);
                int j = strong.size() - 1;
                while (true) {
                    if (j < 0) {
                        break;
                    }
                    AwareProcessInfo awareProcInfoItem = allProcNeedSortTmp.get(strong.keyAt(j));
                    if (awareProcInfoItem != null && awareProcInfoItem.procProcInfo.mCurAdj <= HwActivityManagerService.PERCEPTIBLE_APP_ADJ) {
                        keyPercepServicePidTmp.add(pid);
                        break;
                    }
                    j--;
                }
            }
        }
    }

    private void updateNewProcessesEx(ArrayMap<Integer, Integer> googlePreviousCache, SparseArray<AwareProcessInfo> allProcNeedSort) {
        this.mPrevAwareBase = AwareAppAssociate.getInstance().getPreviousAppOpt();
        updateGcm();
        updateGooglePreviousCache(googlePreviousCache);
        this.mWidgetList = AwareAppAssociate.getInstance().getWidgetsPkg();
        updateVisibleWindowList();
        updateSysAudioPkgsCache();
        updateForPkgsHaveMainProc(allProcNeedSort);
        this.mFloatBall = FloatBallAssociate.getInstance().getTopFloatBall();
        this.mUpdateTime = SystemClock.elapsedRealtimeNanos();
    }

    private void updateForPkgsHaveMainProc(SparseArray<AwareProcessInfo> procs) {
        if (procs == null) {
            this.mPkgsHaveMainProcAlive = new ArraySet();
            return;
        }
        Set<String> pkgsTmp = new ArraySet<>();
        int size = procs.size();
        for (int i = 0; i < size; i++) {
            AwareProcessInfo proc = procs.valueAt(i);
            if (!(proc == null || proc.procProcInfo == null)) {
                String pkg = getProcPkg(proc.procProcInfo);
                String procName = proc.procProcInfo.mProcessName;
                if (pkg != null && pkg.equals(procName)) {
                    pkgsTmp.add(pkg);
                }
            }
        }
        this.mPkgsHaveMainProcAlive = pkgsTmp;
    }

    private String getProcPkg(ProcessInfo processInfo) {
        ArrayList<String> pkgs = processInfo.mPackageName;
        if (pkgs == null || pkgs.isEmpty()) {
            return "";
        }
        return pkgs.get(0);
    }

    private void updateGcm() {
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            List<String> tmp = new ArrayList<>();
            List<String> result = habit.getGCMAppList();
            if (result != null) {
                tmp.addAll(result);
            }
            this.mGcmAppList = tmp;
        }
    }

    private void updatePerceptibleApp(ProcessInfo procInfo, SparseSet fgServiceUidTmp, SparseIntArray percepServicePidTmp, SparseSet importUidTmp) {
        if (procInfo.mCurAdj != HwActivityManagerService.PERCEPTIBLE_APP_ADJ) {
            return;
        }
        if ("fg-service".equals(procInfo.mAdjType) || ADJTYPE_FG_LOCATION_SERVICE.equals(procInfo.mAdjType)) {
            fgServiceUidTmp.add(procInfo.mUid);
        } else if ("service".equals(procInfo.mAdjType)) {
            percepServicePidTmp.put(procInfo.mPid, procInfo.mUid);
        } else {
            importUidTmp.add(procInfo.mUid);
        }
    }

    private void updateForeGroundUid(ProcessInfo procInfo, SparseArray<List<String>> foreGroundUidTmp, SparseSet foreGroundAssocPidTmp, SparseSet foreGroundPidsTmp) {
        if (procInfo.mForegroundActivities && foreGroundPidsTmp.contains(procInfo.mPid)) {
            AwareAppAssociate.getInstance().getAssocProvider(procInfo.mPid, foreGroundAssocPidTmp);
            if (AwareAppAssociate.isDealAsPkgUid(procInfo.mUid)) {
                foreGroundUidTmp.put(procInfo.mUid, procInfo.mPackageName);
            } else {
                foreGroundUidTmp.put(procInfo.mUid, null);
            }
        }
    }

    private void updateVisibleWindowList() {
        SparseArray<AwareProcessInfo> tmpAllProcNeedSort = this.mAllProcNeedSort;
        if (tmpAllProcNeedSort == null || tmpAllProcNeedSort.size() == 0) {
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return;
        }
        SparseSet visibleWindows = new SparseSet();
        Set<String> tmpVisibleWindowList = new ArraySet<>();
        AwareAppAssociate.getInstance().getVisibleWindows(visibleWindows, null);
        addVisibleWindowToList(visibleWindows, tmpVisibleWindowList, tmpAllProcNeedSort);
        this.mVisibleWindowList = tmpVisibleWindowList;
        SparseSet restrainedVisWins = new SparseSet();
        AwareAppAssociate.getInstance().getVisibleWindowsInRestriction(restrainedVisWins);
        Set<String> tmpRestrainedVisWinList = new ArraySet<>();
        addVisibleWindowToList(restrainedVisWins, tmpRestrainedVisWinList, tmpAllProcNeedSort);
        this.mRestrainedVisWinList = tmpRestrainedVisWinList;
    }

    private void addVisibleWindowToList(SparseSet visibleWindows, Set<String> visibleWindowList, SparseArray<AwareProcessInfo> procNeedSort) {
        for (int i = visibleWindows.size() - 1; i >= 0; i--) {
            AwareProcessInfo awareProcInfo = procNeedSort.get(visibleWindows.keyAt(i));
            if (!(awareProcInfo == null || awareProcInfo.procProcInfo == null || awareProcInfo.procProcInfo.mPackageName == null)) {
                visibleWindowList.addAll(awareProcInfo.procProcInfo.mPackageName);
            }
        }
    }

    private boolean isPkgIncludeForTgt(List<String> tgtPkg, List<String> dstPkg) {
        if (tgtPkg == null || tgtPkg.isEmpty() || dstPkg == null) {
            return false;
        }
        for (String pkg : dstPkg) {
            if (pkg != null && tgtPkg.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    public List<AwareProcessInfo> getAllProcNeedSort() {
        if (!getNewProcesses()) {
            AwareLog.e(TAG, "update processes status failed!");
            return null;
        }
        SparseArray<AwareProcessInfo> tmpAllProcNeedSort = this.mAllProcNeedSort;
        if (tmpAllProcNeedSort == null || tmpAllProcNeedSort.size() == 0) {
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return null;
        }
        List<AwareProcessInfo> result = new ArrayList<>();
        for (int i = tmpAllProcNeedSort.size() - 1; i >= 0; i--) {
            result.add(tmpAllProcNeedSort.valueAt(i));
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkForeground */
    public boolean lambda$initPredicatesEx$25$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        SparseSet tmpForeGroundPids = this.mForeGroundPids;
        if (tmpForeGroundPids == null) {
            return false;
        }
        return tmpForeGroundPids.contains(awareProcInfo.procProcInfo.mPid);
    }

    /* access modifiers changed from: private */
    /* renamed from: checkBlind */
    public boolean lambda$initPredicatesEx$26$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        if (procInfo.mPackageName == null || this.mBlindPkg.isEmpty()) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (this.mBlindPkg.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkMusicInstant */
    public boolean lambda$initPredicatesEx$27$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        AwareAppKeyBackgroup awareAppKeyBackgroup = this.mKeyBackgroupInstance;
        if (awareAppKeyBackgroup == null) {
            return false;
        }
        return awareAppKeyBackgroup.checkAudioOutInstant(procInfo.mPid, procInfo.mUid, procInfo.mPackageName);
    }

    public void updateBlind(Set<String> blindPkgs) {
        if (blindPkgs == null) {
            this.mBlindPkg = new ArraySet();
        } else {
            this.mBlindPkg = blindPkgs;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: checkGcm */
    public boolean lambda$initPredicatesEx$29$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        if (this.mGcmAppList == null || this.mGcmAppList.isEmpty() || awareProcInfo.procProcInfo.mPackageName == null) {
            return false;
        }
        Iterator it = awareProcInfo.procProcInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (this.mGcmAppList.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkOverseaApp */
    public boolean lambda$initPredicatesOthers$35$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        Iterator it = awareProcInfo.procProcInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (AppTypeRecoManager.getInstance().getAppWhereFrom((String) it.next()) != 0) {
                return true;
            }
        }
        return false;
    }

    private int getCpuCounts() {
        File[] files;
        File dir = new File(CPU_FILE);
        if (dir.exists() && (files = dir.listFiles(new FileFilter() {
            /* class com.android.server.mtm.utils.AppStatusUtils.AnonymousClass1CpuFilter */

            @Override // java.io.FileFilter
            public boolean accept(File file) {
                return Pattern.matches("cpu[0-9]", file.getName());
            }
        })) != null) {
            return files.length;
        }
        return 4;
    }

    private void runCommandForCpu(int maxTimes) {
        int jIndex = 0;
        for (int i = 0; i < maxTimes; i++) {
            jIndex++;
        }
    }

    private long getValueByBufferedReader(BufferedReader rd) {
        Scanner scanner = new Scanner(rd);
        long value = scanner.nextLong();
        scanner.close();
        return value;
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00e9 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00ea  */
    private long getCpuCapability(int maxTimes) {
        long testCost = -1;
        int[] values = {-1, -1};
        if (UniPerf.getInstance().uniPerfGetConfig(new int[]{7, 8}, values) == 0 && values[0] > 0 && values[1] > 0) {
            try {
                int myPid = Process.myPid();
                int myTid = Process.myTid();
                FileReader statFileBefore = new FileReader("/proc/" + myPid + "/task/" + myTid + "/schedstat");
                FileReader statFileAfter = new FileReader("/proc/" + myPid + "/task/" + myTid + "/schedstat");
                BufferedReader bufferBefore = null;
                BufferedReader bufferAfter = null;
                try {
                    bufferBefore = new BufferedReader(statFileBefore);
                    long valueBefore = getValueByBufferedReader(bufferBefore);
                    runCommandForCpu(maxTimes);
                    bufferAfter = new BufferedReader(statFileAfter);
                    testCost = getValueByBufferedReader(bufferAfter) - valueBefore;
                    try {
                        bufferBefore.close();
                    } catch (IOException e) {
                        AwareLog.e(TAG, "got cpu capability failed!");
                    }
                    try {
                        bufferAfter.close();
                    } catch (IOException e2) {
                    }
                } catch (InputMismatchException e3) {
                    AwareLog.e(TAG, "got cpu capability failed!");
                    try {
                        bufferBefore.close();
                    } catch (IOException e4) {
                        AwareLog.e(TAG, "got cpu capability failed!");
                    }
                    if (bufferAfter != null) {
                        try {
                            bufferAfter.close();
                        } catch (IOException e5) {
                            AwareLog.e(TAG, "got cpu capability failed!");
                            if (values[0] != 0) {
                            }
                        }
                    } else {
                        statFileAfter.close();
                    }
                } catch (Throwable th) {
                    try {
                        bufferBefore.close();
                    } catch (IOException e6) {
                        AwareLog.e(TAG, "got cpu capability failed!");
                    }
                    if (bufferAfter != null) {
                        try {
                            bufferAfter.close();
                        } catch (IOException e7) {
                            AwareLog.e(TAG, "got cpu capability failed!");
                        }
                    } else {
                        statFileAfter.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                AwareLog.e(TAG, "got cpu capability failed!");
            }
        }
        if (values[0] != 0) {
            return testCost;
        }
        return (((long) values[1]) * testCost) / ((long) values[0]);
    }

    /* access modifiers changed from: private */
    /* renamed from: checkDownUpload */
    public boolean lambda$initPredicates$13$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        if (AwareIntelligentRecg.getInstance().isRecogOptEnable()) {
            boolean isDozeProtected = AwareIntelligentRecg.getInstance().getDozeProtectedApps().contains(getMainPkgName(awareProcInfo));
            boolean isTargetApp = true;
            boolean isPerceptible = awareProcInfo.procProcInfo.mCurAdj <= HwActivityManagerService.PERCEPTIBLE_APP_ADJ;
            if (awareProcInfo.procProcInfo.mTargetSdkVersion < 26 && !this.mIsTargetOreo) {
                isTargetApp = false;
            }
            if (isTargetApp && !isDozeProtected && !isPerceptible) {
                return false;
            }
        }
        return checkKeyBackgroupByState(5, awareProcInfo);
    }

    private boolean checkBloothOff() {
        return !AwareIntelligentRecg.getInstance().checkBleStatus();
    }

    private boolean checkSimChina() {
        return AwareIntelligentRecg.getInstance().isChinaOperrator();
    }

    /* access modifiers changed from: private */
    /* renamed from: checkHwInstall */
    public boolean lambda$initPredicatesOthersEx$46$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo;
        if (awareProcInfo == null || (procInfo = awareProcInfo.procProcInfo) == null || procInfo.mType != 3) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkDtbBind */
    public boolean lambda$initPredicatesOthers$45$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        if (procInfo == null || procInfo.mPackageName == null) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (AwareMiddleware.getInstance().isAppDtbBind((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldFilterVisibleApp(AwareProcessInfo awareProcInfo) {
        if (!AppAccurateRecgFeature.isEnable()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.procProcInfo;
        if (procInfo.mForegroundActivities || !"service".equals(procInfo.mAdjType) || AwareIntelligentRecg.getInstance().getDefaultInputMethodUid() == procInfo.mAppUid || AwareAppAssociate.getInstance().isSystemUnRemoveApp(procInfo.mAppUid) || AwareAppAssociate.getInstance().isForeGroundApp(procInfo.mAppUid)) {
            return false;
        }
        return AwareAppAssociate.getInstance().isJobDoingForUid(procInfo.mAppUid);
    }

    private boolean isAdjVisible(AwareProcessInfo awareProcInfo) {
        return awareProcInfo.procProcInfo.mCurAdj >= HwActivityManagerService.VISIBLE_APP_ADJ && awareProcInfo.procProcInfo.mCurAdj < HwActivityManagerService.PERCEPTIBLE_APP_ADJ;
    }

    private void updateSysAudioPkgsCache() {
        AwareAppKeyBackgroup awareAppKeyBackgroup = this.mKeyBackgroupInstance;
        if (awareAppKeyBackgroup != null) {
            awareAppKeyBackgroup.updateSysAudioPkgsCache();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: checkWebViewHasMainProcAlive */
    public boolean lambda$initPredicatesOthersEx$47$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        ProcessInfo processInfo = awareProcInfo.procProcInfo;
        if (processInfo == null || !UserHandleEx.isIsolated(processInfo.mUid)) {
            return false;
        }
        String pkg = getProcPkg(processInfo);
        Set<String> pkgsHaveMainProc = this.mPkgsHaveMainProcAlive;
        if (pkg == null || pkgsHaveMainProc == null) {
            return false;
        }
        return pkgsHaveMainProc.contains(pkg);
    }

    /* access modifiers changed from: private */
    /* renamed from: checkIsMainProc */
    public boolean lambda$initPredicatesOthersEx$48$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        ProcessInfo processInfo = awareProcInfo.procProcInfo;
        if (processInfo == null) {
            return false;
        }
        String pkg = getProcPkg(processInfo);
        String procName = processInfo.mProcessName;
        if (procName == null || !procName.equals(pkg)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkUidInFgUids */
    public boolean lambda$initPredicatesOthersEx$49$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        SparseArray<List<String>> tmpForeGroundUid = this.mForeGroundUid;
        if (tmpForeGroundUid != null && tmpForeGroundUid.indexOfKey(awareProcInfo.procProcInfo.mUid) >= 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkCachedCanCleanAdjType */
    public boolean lambda$initPredicatesOthersEx$50$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo.procProcInfo.mCurAdj < HwActivityManagerService.CACHED_APP_MIN_ADJ) {
            return false;
        }
        String adjType = awareProcInfo.procProcInfo.mAdjType;
        if (ADJTYPE_CCH_EMPTY.equals(adjType) || ADJTYPE_CCH_BOUND_SERVICES.equals(adjType)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: checkCachedProtect */
    public boolean lambda$initPredicatesOthersEx$51$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo.procProcInfo == null) {
            return false;
        }
        return CachedMemoryCleanPolicy.getInstance().isCachedProtect(getProcPkg(awareProcInfo.procProcInfo), awareProcInfo.procProcInfo.mProcessName);
    }

    /* access modifiers changed from: private */
    /* renamed from: checkPkgDownUpload */
    public boolean lambda$initPredicatesOthersEx$52$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        return checkKeyBackgroupByState(5, awareProcInfo);
    }

    /* access modifiers changed from: private */
    /* renamed from: checkInGame */
    public boolean lambda$initPredicatesOthersEx$53$AppStatusUtils(AwareProcessInfo awareProcInfo) {
        ProcessInfo procProcInfo;
        ArrayList<String> pkgList;
        String pkgName;
        if (awareProcInfo == null || (procProcInfo = awareProcInfo.procProcInfo) == null || (pkgList = procProcInfo.mPackageName) == null || pkgList.isEmpty() || (pkgName = pkgList.get(0)) == null || AppTypeRecoManager.getInstance().getAppType(pkgName) != 9) {
            return false;
        }
        return AwareGameStatus.getInstance().isGaming(pkgName);
    }
}
