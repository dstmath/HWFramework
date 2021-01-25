package com.huawei.server.magicwin;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWindowConfig;
import com.huawei.server.utils.Utils;
import com.huawei.utils.HwPartResourceUtils;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwMagicWindowConfig {
    private static final String ACT_DEFAULT_FULLSCREEN = "defaultFullScreen";
    private static final String ACT_DESTROY_WHEN_REPLACE_ON_RIGHT = "destroyWhenReplacedOnRight";
    private static final String ACT_LOCK_SIDE = "lockSide";
    private static final String ACT_NAME = "name";
    private static final String ACT_PAIRS_FROM = "from";
    private static final String ACT_PAIRS_TO = "to";
    private static final String ACT_SUPPORT_TASK_SPLIT_SCREEN = "isSupportTaskSplitScreen";
    private static final String BODY_ACTS = "Activities";
    private static final String BODY_ACT_PAIRS = "activityPairs";
    private static final String BODY_DUAL_ACTS = "defaultDualActivities";
    private static final String BODY_FULLSCREEN_VIDEO = "supportVideoFullscreen";
    private static final String BODY_MODE = "mode";
    private static final String BODY_RELAUNCH_ON_RESIZE = "isRelaunchwhenResize";
    private static final String BODY_TASK_SPLIT_SCREEN = "supportAppTaskSplitScreen";
    private static final String BODY_TRANS_ACTS = "transActivities";
    private static final String BODY_UX = "UX";
    private static final String CLIENT_NAME = "client_name";
    private static final String CLIENT_REQUEST = "client_request";
    private static final boolean DEFAULT_FALSE = false;
    private static final Rect DEFAULT_LEFT_BOUNDS = new Rect(20, 5, 1275, 1595);
    private static final Rect DEFAULT_MIDDLE_BOUNDS = new Rect(657, 5, 1912, 1595);
    private static final Rect DEFAULT_RIGHT_BOUNDS = new Rect(1285, 5, 2540, 1595);
    private static final Rect DEFAULT_SINGLE_BOUNDS = new Rect(657, 5, 1912, 1595);
    private static final boolean DEFAULT_TRUE = true;
    private static final String DEVICE_FOLD = "FOLD";
    private static final String DEVICE_PAD = "PAD";
    private static final int DRAG_MID_MODE = 0;
    public static final int FTYPE_APP_LST = 0;
    public static final int FTYPE_MULTISCREEN_PROJECTION_LIMIT = 3;
    public static final int FTYPE_SYS_CFG = 1;
    public static final int FTYPE_USR_DAT = 2;
    private static final int LOCAL_ONLY = 1000;
    private static final String MAIN_PAGE = "mainPage";
    private static final String MAIN_PAGE_SET = "mainPages";
    private static final int MIDDLE_GAP_WIDTH_DP = 2;
    private static final int NUM_BOUNDS = 2;
    private static final int PARSER_STRING_TO_INT_ERROR = -1;
    private static final String RATIO_SPLITTER = "\\|";
    private static final String RELATED_PAGE = "relatedPage";
    private static final int SPLIT_MIDDLE_GAP_WIDTH_DP = 8;
    private static final int SPLIT_MIDDLE_GAP_WIDTH_DP_PAD = 6;
    private static final String TAG = "HWMW_HwMagicWindowConfig";
    private static final String UX_IS_DRAGABLE = "isDraggable";
    private static final String UX_IS_SCALED = "supportRotationUxCompat";
    private static final String UX_IS_SHOW_STATUSBAR = "showStatusBar";
    private static final String UX_KEEP_PRIMARY_TOP_ALWAYS_RESUME = "keepPrimaryTopAlwaysResume";
    private static final String UX_SPLIT_BAR_BG_COLOR = "splitBarBgColor";
    private static final String UX_SPLIT_LINE_BG_COLOR = "splitLineBgColor";
    private static final String UX_SUPPORT_DRAGGING_TO_FULLSCREEN = "supportDraggingToFullScreen";
    private static final String UX_USE_SYSTEM_ACTIVITY_ANIMATION = "useSystemActivityAnimation";
    private static final String UX_WINDOWS_RATIO = "windowsRatio";
    private static final int WECHAT_BAR_COLOR_NIGHTMODE = -15198184;
    private static final int WECHAT_LINE_COLOR_NIGHTMODE = -2039584;
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    private static final String WINDOWS_RATIO_DEVICE = "device";
    private static final String WINDOWS_RATIO_RATIO = "ratio";
    private static List<String> mInstalledPkgNameList = new ArrayList();
    private final int INDEX_B;
    private final int INDEX_CNT;
    private final int INDEX_L;
    private final int INDEX_R;
    private final int INDEX_T;
    private Map<String, List<Rect>> mAppDragBoundsConfigs = new HashMap();
    private DeviceAttribute mAttribute;
    private HwMagicWindowConfigLoader mCfgLoader = null;
    private Context mContext = null;
    private Map<Integer, List<Rect>> mDragBounds = null;
    private Map<String, EasyGoConfig> mEasyGoConfigs = new ConcurrentHashMap();
    private Rect mFullBounds;
    private Map<String, HomeConfig> mHomeConfigs = new HashMap();
    private boolean mIsCurrentRtl;
    private Rect mLeftDragleBounds;
    private Map<String, LocalConfig> mLocalConfigs = new HashMap();
    private Set<Rect> mMasterBoounds;
    private Rect mMasterBounds;
    private Set<Rect> mMidBoounds;
    private Rect mMiddleBounds;
    private Rect mMiddleDragleBounds;
    private float mRatio = 1.0f;
    private Rect mRightDragleBounds;
    private Map<String, SettingConfig> mSettingConfigs = new ConcurrentHashMap();
    private Set<Rect> mSlaveBoounds;
    private Rect mSlaveBounds;
    private SystemConfig mSystemConfig = null;
    private List<Uri> mUris;

    public HwMagicWindowConfig(Context cxt, DeviceAttribute attribute, List<Uri> uris, SystemConfig localSysCfg) {
        boolean z = false;
        this.INDEX_L = 0;
        this.INDEX_T = 1;
        this.INDEX_R = 2;
        this.INDEX_B = 3;
        this.INDEX_CNT = 4;
        this.mMidBoounds = new HashSet();
        this.mMasterBoounds = new HashSet();
        this.mSlaveBoounds = new HashSet();
        this.mMasterBounds = new Rect();
        this.mMiddleBounds = new Rect();
        this.mFullBounds = new Rect();
        this.mSlaveBounds = new Rect();
        this.mLeftDragleBounds = new Rect();
        this.mMiddleDragleBounds = new Rect();
        this.mRightDragleBounds = new Rect();
        this.mUris = new ArrayList();
        this.mContext = cxt;
        this.mAttribute = attribute;
        if (attribute.isVirtualContainer() && uris != null) {
            this.mUris = uris;
        }
        this.mIsCurrentRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1 ? true : z;
        this.mCfgLoader = new HwMagicWindowConfigLoader(this.mContext, ActivityManagerEx.getCurrentUser(), attribute);
        if (attribute.isLocalContainer()) {
            createSystem(localSysCfg);
            updateInstalledPackages();
        } else if (attribute.isVirtualContainer()) {
            this.mSystemConfig = HwMagicWindowConfigLoader.loadSystem(this);
            mergeSystemConfig(localSysCfg, this.mSystemConfig);
            loadAppAdapterInfo("*");
            updateSystemBoundSize(this.mAttribute.getDisplayMetrics());
            updateInstalledPackages();
        } else {
            SlogEx.w(TAG, "Unknown container type on create config.");
        }
        this.mCfgLoader.loadPackage(this, "");
        if (attribute.isVirtualContainer()) {
            onUserSwitch();
        }
    }

    private void mergeSystemConfig(SystemConfig local, SystemConfig virtual) {
        boolean z = true;
        virtual.mRoundAngle = virtual.mRoundAngle && local.mRoundAngle;
        virtual.mAnimation = virtual.mAnimation && local.mAnimation;
        virtual.mBackground = virtual.mBackground && local.mBackground;
        virtual.mOpenCapability = local.mOpenCapability;
        virtual.mBackToMiddle = local.mBackToMiddle;
        virtual.mCornerRadius = local.mCornerRadius;
        virtual.mSplitAdjustValue = local.mSplitAdjustValue;
        virtual.mHostViewThreshold = local.mHostViewThreshold;
        if (!virtual.mSupportDraggingToFullScreen || !local.mSupportDraggingToFullScreen) {
            z = false;
        }
        virtual.mSupportDraggingToFullScreen = z;
        virtual.mSupportVirtualConfigInOsd = local.mSupportVirtualConfigInOsd;
        virtual.mSupportLocalConfigInOsd = false;
    }

    public Uri getUri(int type) {
        List<Uri> list = this.mUris;
        if (list == null || type < 0 || type > 3 || type >= list.size()) {
            return null;
        }
        return this.mUris.get(type);
    }

    public Context getContext() {
        return this.mContext;
    }

    public List<Uri> getConfigFiles() {
        List<Uri> uriList = new ArrayList<>();
        if (this.mAttribute.isVirtualContainer()) {
            return uriList;
        }
        File cur = HwMagicWindowConfigLoader.getLocalConfigFile(0);
        if (cur != null && cur.exists()) {
            uriList.add(Uri.fromFile(cur));
        }
        File cur2 = HwMagicWindowConfigLoader.getLocalConfigFile(1);
        if (cur2 != null && cur2.exists()) {
            uriList.add(Uri.fromFile(cur2));
        }
        File cur3 = HwMagicWindowConfigLoader.getLocalConfigFile(2);
        if (cur3 != null && cur3.exists()) {
            uriList.add(Uri.fromFile(cur3));
        }
        File cur4 = HwMagicWindowConfigLoader.getLocalConfigFile(3);
        if (cur4 != null && cur4.exists()) {
            uriList.add(Uri.fromFile(cur4));
        }
        return uriList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00b1, code lost:
        if (r8.getCount() == 0) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00b3, code lost:
        delAppAdapterInfo(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00b6, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00d8, code lost:
        if (r8.getCount() == 0) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00e8, code lost:
        if (r8.getCount() == 0) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    public void loadAppAdapterInfo(String packageName) {
        SlogEx.i(TAG, "loadAppAdapterInfo");
        Uri uri = Uri.parse("content://com.huawei.easygo.easygoprovider/v_function");
        Cursor cursor = null;
        try {
            Context context = ContextEx.createPackageContextAsUser(this.mContext, "com.huawei.systemserver", 0, UserHandleEx.of(ActivityManagerEx.getCurrentUser()));
            if (context != null) {
                if ("*".equals(packageName)) {
                    this.mEasyGoConfigs.clear();
                    cursor = context.getContentResolver().query(uri, new String[]{CLIENT_REQUEST, CLIENT_NAME}, "server_data_schema=\"package:magicwin\"", null, null);
                } else {
                    cursor = context.getContentResolver().query(uri, new String[]{CLIENT_REQUEST, CLIENT_NAME}, "server_data_schema=\"package:magicwin\" and client_name=?", new String[]{packageName}, null);
                }
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String clientPackageName = cursor.getString(cursor.getColumnIndex(CLIENT_NAME));
                        String clientRequest = cursor.getString(cursor.getColumnIndex(CLIENT_REQUEST));
                        SlogEx.i(TAG, "loadAppAdapter PackageName=" + clientPackageName + "Request=" + clientRequest);
                        parseRequest(clientPackageName, clientRequest);
                    } while (cursor.moveToNext());
                    if (cursor != null) {
                    }
                } else if (cursor != null) {
                }
            } else if (0 != 0) {
                if (cursor.getCount() == 0) {
                    delAppAdapterInfo(packageName);
                }
                cursor.close();
            }
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.e(TAG, "register EasyGo no package context");
            if (0 == 0) {
            }
        } catch (IllegalStateException e2) {
            SlogEx.e(TAG, "loadAppAdapter failed " + e2);
            if (0 == 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                if (cursor.getCount() == 0) {
                    delAppAdapterInfo(packageName);
                }
                cursor.close();
            }
            throw th;
        }
    }

    private void parseRequest(String pkg, String clientRequest) {
        if (pkg != null && clientRequest != null) {
            if (!this.mLocalConfigs.containsKey(pkg) || this.mLocalConfigs.get(pkg).getWindowMode() != -1) {
                EasyGoConfig easyGoCfg = new EasyGoConfig(pkg, clientRequest);
                addOpenCapAppConfig(pkg, easyGoCfg);
                easyGoCfg.parseAppConfig();
                updateSettingForPkg(pkg, "parse_kit");
            }
        }
    }

    public void updateSettingForPkg(String pkg, String reason) {
        if (getWindowMode(pkg) < 0) {
            removeSetting(pkg, reason);
        } else if (!this.mSettingConfigs.containsKey(pkg)) {
            createSetting(pkg, getDefaultSetting(pkg), false, 0, reason);
        }
    }

    private boolean getDefaultSetting(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        if (this.mLocalConfigs.containsKey(pkg)) {
            return this.mLocalConfigs.get(pkg).isDefaultSetting();
        }
        if (!this.mAttribute.isFoldableDevice()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void delAppAdapterInfo(String packageName) {
        SlogEx.i(TAG, "delete packageName=" + packageName + " AppAdapterInfo");
        if (!isEmpty(packageName)) {
            if (this.mEasyGoConfigs.containsKey(packageName) && (!this.mLocalConfigs.containsKey(packageName) || this.mLocalConfigs.get(packageName).getWindowMode() == -2)) {
                removeSetting(packageName, "del_kit_info");
            }
            this.mEasyGoConfigs.remove(packageName);
        }
    }

    private void delLocalConfigs(String packageName) {
        if (!isEmpty(packageName)) {
            this.mLocalConfigs.remove(packageName);
        }
    }

    public Set<String> onCloudUpdate() {
        this.mLocalConfigs.clear();
        this.mHomeConfigs.clear();
        this.mCfgLoader.loadPackage(this, "");
        syncSettingsWithWhiteList();
        Map<String, LocalConfig> tmpPackageConfigs = (Map) ((HashMap) this.mLocalConfigs).clone();
        tmpPackageConfigs.putAll(this.mLocalConfigs);
        return tmpPackageConfigs.keySet();
    }

    public void onUserSwitch() {
        this.mCfgLoader.initSettingsDirForUser(ActivityManagerEx.getCurrentUser());
        Utils.dbg(Utils.TAG_SETTING, "clr setting: user_switch");
        this.mSettingConfigs.clear();
        this.mAppDragBoundsConfigs.clear();
        loadUserSettingsData();
        syncSettingsWithWhiteList();
        updateInstalledPackages();
    }

    public void onAppSwitchChanged(String pkg, boolean isMagicWinEnabled) {
        SlogEx.d(TAG, "onAppSwitchChanged, pkg = " + pkg + ", hwMagicWinEnabled = " + isMagicWinEnabled);
        this.mSettingConfigs.computeIfPresent(pkg, new BiFunction(isMagicWinEnabled) {
            /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowConfig$dLE2iPtXQHLimg3GyJ25qPGUz9k */
            private final /* synthetic */ boolean f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                String str = (String) obj;
                return ((SettingConfig) obj2).setMagicWinEnabled(this.f$0);
            }
        });
        this.mCfgLoader.writeSetting(this, "app_switch_changed");
    }

    public void setDialogShownForApp(String pkg, boolean isDialogShown) {
        SlogEx.d(TAG, "setDialogShownForApp, pkg = " + pkg + ", hwDialogShown = " + isDialogShown);
        this.mSettingConfigs.computeIfPresent(pkg, new BiFunction(isDialogShown) {
            /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowConfig$6jeNJOup3ckmbbbdG_szRUL300g */
            private final /* synthetic */ boolean f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                String str = (String) obj;
                return ((SettingConfig) obj2).setHwDialogShown(this.f$0);
            }
        });
        this.mCfgLoader.writeSetting(this, "dialog_shown");
    }

    private void loadUserSettingsData() {
        this.mCfgLoader.readSetting(this);
    }

    private void syncSettingsWithWhiteList() {
        if (getIsSupportOpenCap()) {
            syncOpenCapAppConfig();
        } else {
            syncWhiteListAppConfig();
        }
        this.mCfgLoader.writeSetting(this, "after_sync_setting_finished");
    }

    public void calcHwSplitStackBounds(boolean isFoldScreen, int splitRatio, Rect primaryOutBounds, Rect secondaryOutBounds) {
        int[] displaySize = getAdjustDisplaySize(this.mAttribute.getDisplayMetrics(), isFoldScreen);
        if (displaySize != null) {
            int width = displaySize[0];
            int height = displaySize[1];
            int divideBarWidth = getDivideBarPixelSize(-1, px2dp((float) width));
            SlogEx.i(TAG, "calcHwSplitStackBounds divide bar with " + divideBarWidth + " splitRatio " + splitRatio);
            if (divideBarWidth >= 0) {
                float priRatio = 0.5f;
                if (splitRatio == 5 || splitRatio == 6) {
                    primaryOutBounds.set(0, 0, width, height);
                    secondaryOutBounds.set(0, 0, width, height);
                    return;
                }
                if (splitRatio == 1) {
                    priRatio = 0.33333334f;
                } else if (splitRatio == 2) {
                    priRatio = 0.6666667f;
                } else {
                    SlogEx.d(TAG, "calcHwSplitStackBounds not match splitRatio = " + splitRatio);
                }
                primaryOutBounds.set(0, 0, ((int) (((float) width) * priRatio)) - (divideBarWidth / 2), height);
                secondaryOutBounds.set(((int) (((float) width) * priRatio)) + (divideBarWidth / 2), 0, width, height);
            }
        }
    }

    public void writeSetting(String reason) {
        this.mCfgLoader.writeSetting(this, reason);
    }

    private BaseAppConfig getAppConfig(String pkg) {
        if (isEmpty(pkg)) {
            return null;
        }
        LocalConfig localCfg = this.mLocalConfigs.get(pkg);
        EasyGoConfig easyGoCfg = this.mEasyGoConfigs.get(pkg);
        if (easyGoCfg == null || (localCfg != null && localCfg.isLocalOnly())) {
            return localCfg;
        }
        return easyGoCfg;
    }

    public boolean getIsSupportOpenCap() {
        SystemConfig systemConfig = this.mSystemConfig;
        if (systemConfig != null) {
            return systemConfig.isSystemSupport(3);
        }
        return false;
    }

    public int getAppDragMode(String pkgName) {
        SettingConfig setCfg;
        if (!isEmpty(pkgName) && (setCfg = this.mSettingConfigs.get(pkgName)) != null) {
            return setCfg.getDragMode();
        }
        return 0;
    }

    private void syncWhiteListAppConfig() {
        this.mSettingConfigs.keySet().retainAll(this.mLocalConfigs.keySet());
        this.mLocalConfigs.forEach(new BiConsumer() {
            /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowConfig$TAT2iKxgPijbLMgqvwqD4sKj8LQ */

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                HwMagicWindowConfig.this.lambda$syncWhiteListAppConfig$2$HwMagicWindowConfig((String) obj, (LocalConfig) obj2);
            }
        });
    }

    public /* synthetic */ void lambda$syncWhiteListAppConfig$2$HwMagicWindowConfig(String key, LocalConfig pkgCfg) {
        if (!this.mSettingConfigs.containsKey(key)) {
            this.mSettingConfigs.put(key, new SettingConfig(key, pkgCfg.isDefaultSetting(), false, 0, ""));
        }
    }

    private void syncOpenCapAppConfig() {
        Set<String> results = new HashSet<>();
        Set<String> openCapSet = this.mEasyGoConfigs.keySet();
        Set<String> pkgSet = this.mLocalConfigs.keySet();
        results.addAll(openCapSet);
        results.addAll(pkgSet);
        this.mSettingConfigs.keySet().retainAll(results);
        this.mLocalConfigs.forEach(new BiConsumer() {
            /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowConfig$FcIxxmOi2hTu5Vnz34HC6RahQcs */

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                HwMagicWindowConfig.this.lambda$syncOpenCapAppConfig$3$HwMagicWindowConfig((String) obj, (LocalConfig) obj2);
            }
        });
        this.mEasyGoConfigs.forEach(new BiConsumer() {
            /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowConfig$G34EjC7Nk4NtXCF0IqJqIOWUKVg */

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                HwMagicWindowConfig.this.lambda$syncOpenCapAppConfig$4$HwMagicWindowConfig((String) obj, (HwMagicWindowConfig.EasyGoConfig) obj2);
            }
        });
        Map<String, LocalConfig> removeMap = (Map) this.mLocalConfigs.entrySet().stream().filter(new Predicate(openCapSet) {
            /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowConfig$Aw4cK7I_mFq4FOPvYdGBew7WLA */
            private final /* synthetic */ Set f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return HwMagicWindowConfig.lambda$syncOpenCapAppConfig$5(this.f$0, (Map.Entry) obj);
            }
        }).collect(Collectors.toMap($$Lambda$CSz_ibwXhtkKNl72Q8tR5oBgkWk.INSTANCE, $$Lambda$uyPzjt7BVwmYyDfEycnpfCwdk.INSTANCE));
        this.mSettingConfigs.keySet().removeAll(removeMap.keySet());
        Utils.dbg(Utils.TAG_SETTING, "remove setting:" + removeMap.keySet());
    }

    public /* synthetic */ void lambda$syncOpenCapAppConfig$3$HwMagicWindowConfig(String key, LocalConfig pkgCfg) {
        if (!this.mSettingConfigs.containsKey(key)) {
            this.mSettingConfigs.put(key, new SettingConfig(key, pkgCfg.isDefaultSetting(), false, 0, "sync_new_from_xml"));
        }
    }

    public /* synthetic */ void lambda$syncOpenCapAppConfig$4$HwMagicWindowConfig(String key, EasyGoConfig easyGoCfg) {
        if (!this.mSettingConfigs.containsKey(key)) {
            this.mSettingConfigs.put(key, new SettingConfig(key, !this.mAttribute.isFoldableDevice(), false, 0, "sync_new_from_kit"));
        }
    }

    static /* synthetic */ boolean lambda$syncOpenCapAppConfig$5(Set openCapSet, Map.Entry map) {
        return ((LocalConfig) map.getValue()).getWindowMode() == -1 || (((LocalConfig) map.getValue()).getWindowMode() == -2 && !openCapSet.contains(map.getKey()));
    }

    public boolean createPackage(LocalConfig localCfg) {
        if (localCfg == null) {
            return false;
        }
        this.mLocalConfigs.put(localCfg.mPackageName, localCfg);
        return true;
    }

    public boolean createHome(String pkg, String[] homes) {
        if (isEmpty(pkg) || isEmpty(homes)) {
            return false;
        }
        this.mHomeConfigs.put(pkg, new HomeConfig(pkg, homes));
        return true;
    }

    private boolean addOpenCapAppConfig(String pkg, EasyGoConfig easyGoCfg) {
        if (isEmpty(pkg) || easyGoCfg == null) {
            return false;
        }
        this.mEasyGoConfigs.remove(pkg);
        this.mEasyGoConfigs.put(pkg, easyGoCfg);
        return true;
    }

    public boolean isNeedDetect(String pkg) {
        return getWindowMode(pkg) > 0 && !this.mHomeConfigs.containsKey(pkg);
    }

    public boolean createSystem(SystemConfig systemConfig) {
        if (systemConfig == null) {
            return false;
        }
        this.mSystemConfig = systemConfig;
        updateSystemBoundSize(this.mAttribute.getDisplayMetrics());
        return true;
    }

    public boolean createSetting(String pkg, boolean isEnabled, boolean isDialogShown, int hwDragMode, String reason) {
        if (isEmpty(pkg)) {
            return false;
        }
        this.mSettingConfigs.put(pkg, new SettingConfig(pkg, isEnabled, isDialogShown, hwDragMode, reason));
        return true;
    }

    public void removeSetting(String pkgName, String reason) {
        SettingConfig sc = this.mSettingConfigs.remove(pkgName);
        if (sc != null) {
            Utils.dbg(Utils.TAG_SETTING, "remove setting:" + sc.getName() + " " + sc.getHwMagicWinEnabled() + " " + sc.getHwDialogShown() + " " + sc.getDragMode() + " " + reason);
            this.mCfgLoader.writeSetting(this, reason);
        }
    }

    /* access modifiers changed from: private */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private static boolean isEmpty(String[] strs) {
        return strs == null || strs.length <= 0;
    }

    /* access modifiers changed from: private */
    public static boolean isEmpty(List<String> list) {
        return list == null || list.isEmpty();
    }

    public int getWindowMode(String pkg) {
        BaseAppConfig appCfg;
        if (!isEmpty(pkg) && (appCfg = getAppConfig(pkg)) != null) {
            return appCfg.getWindowMode();
        }
        return -1;
    }

    public boolean isDefaultSetting(String pkg) {
        if (isEmpty(pkg)) {
            return false;
        }
        LocalConfig localCfg = this.mLocalConfigs.get(pkg);
        return localCfg != null ? localCfg.isDefaultSetting() : !this.mAttribute.isFoldableDevice();
    }

    public boolean needRelaunch(String pkg) {
        BaseAppConfig appCfg;
        if (!isEmpty(pkg) && (appCfg = getAppConfig(pkg)) != null) {
            return appCfg.needRelaunch();
        }
        return false;
    }

    public boolean isSpecPairActivities(String pkg, String focus, String target) {
        EasyGoConfig easyGoConfig = getOpenCapAppConfig(pkg);
        return easyGoConfig != null && easyGoConfig.isSpecPairActivities(focus, target);
    }

    public String getRelateActivity(String pkg) {
        EasyGoConfig easyGoConfig = getOpenCapAppConfig(pkg);
        return easyGoConfig == null ? "" : easyGoConfig.getRelateActivity();
    }

    public List<String> getMainActivity(String pkg) {
        EasyGoConfig easyGoConfig = getOpenCapAppConfig(pkg);
        return easyGoConfig == null ? new ArrayList() : easyGoConfig.getMainActivity();
    }

    public boolean isSpecTransActivity(String pkg, String curActivity) {
        EasyGoConfig target = getOpenCapAppConfig(pkg, curActivity);
        return target != null && target.isSpecTransActivity(curActivity);
    }

    public boolean isDefaultFullscreenActivity(String pkg, String curActivity) {
        EasyGoConfig target = getOpenCapAppConfig(pkg, curActivity);
        return target != null && target.isDefaultFullscreenActivity(curActivity);
    }

    public boolean isNeedStartByNewTaskActivity(String pkg, String curActivity) {
        EasyGoConfig target = getOpenCapAppConfig(pkg, curActivity);
        return target != null && target.isNeedStartByNewTaskActivity(curActivity);
    }

    public boolean isNeedDestroyWhenReplaceOnRight(String pkg, String curActivity) {
        EasyGoConfig target = getOpenCapAppConfig(pkg, curActivity);
        return target != null && target.isNeedDestroyWhenReplaceOnRight(curActivity);
    }

    public boolean isLockMasterActivity(String pkg, String curActivity) {
        EasyGoConfig target = getOpenCapAppConfig(pkg, curActivity);
        return target != null && target.isLockMasterActivity(curActivity);
    }

    public boolean isReLaunchWhenResize(String pkg) {
        EasyGoConfig target = getOpenCapAppConfig(pkg);
        return target != null && target.isReLaunchWhenResize();
    }

    public boolean isSupportAppTaskSplitScreen(String pkg) {
        EasyGoConfig target = getOpenCapAppConfig(pkg);
        return target != null && target.isSupportAppTaskSplitScreen() && getHwMagicWinEnabled(pkg);
    }

    public boolean isShowStatusBar(String pkgName) {
        if (WECHAT_PACKAGE_NAME.equals(pkgName) && !isSupportAppTaskSplitScreen(pkgName)) {
            return true;
        }
        EasyGoConfig target = getOpenCapAppConfig(pkgName);
        if (target == null || !target.isShowStatusBar()) {
            return false;
        }
        return true;
    }

    public boolean isUsingSystemActivityAnimation(String pkgName) {
        EasyGoConfig target = getOpenCapAppConfig(pkgName);
        return target == null || target.isUsingSystemActivityAnimation();
    }

    public boolean isSupportDraggingToFullScreen(String pkgName) {
        BaseAppConfig appCfg;
        if (!isDragable(pkgName) || !isSystemSupport(5) || (appCfg = getAppConfig(pkgName)) == null) {
            return false;
        }
        return appCfg.isDragToFullscreen();
    }

    public int getSplitLineBgColor(String pkgName) {
        EasyGoConfig easyGoConfig = getOpenCapAppConfig(pkgName);
        int bgColor = easyGoConfig == null ? -1 : easyGoConfig.getSplitLineBgColor();
        if (bgColor == -1 || !WECHAT_PACKAGE_NAME.equals(pkgName) || !Utils.isNightMode(this.mContext)) {
            return bgColor;
        }
        return WECHAT_LINE_COLOR_NIGHTMODE;
    }

    private EasyGoConfig getOpenCapAppConfig(String pkgName) {
        if (isEmpty(pkgName)) {
            return null;
        }
        LocalConfig localCfg = this.mLocalConfigs.get(pkgName);
        if (localCfg == null || !localCfg.isLocalOnly()) {
            return this.mEasyGoConfigs.get(pkgName);
        }
        return null;
    }

    private EasyGoConfig getOpenCapAppConfig(String pkgName, String curActivity) {
        if (isEmpty(curActivity)) {
            return null;
        }
        return getOpenCapAppConfig(pkgName);
    }

    public int getSplitBarBgColor(String pkgName) {
        EasyGoConfig easyGoConfig = getOpenCapAppConfig(pkgName);
        int bgColor = easyGoConfig == null ? -1 : easyGoConfig.getSplitBarBgColor();
        if (bgColor == -1 || !WECHAT_PACKAGE_NAME.equals(pkgName) || !Utils.isNightMode(this.mContext)) {
            return bgColor;
        }
        return WECHAT_BAR_COLOR_NIGHTMODE;
    }

    public boolean isLeftResume(String pkg) {
        BaseAppConfig appCfg = getAppConfig(pkg);
        if (appCfg != null) {
            return appCfg.isLeftResume();
        }
        return false;
    }

    public boolean isVideoFullscreen(String pkg) {
        if (this.mAttribute.isFoldableDevice()) {
            return false;
        }
        BaseAppConfig appCfg = getAppConfig(pkg);
        if (appCfg != null) {
            return appCfg.isVideoFullscreen();
        }
        return true;
    }

    public boolean isCameraPreview(String pkg) {
        BaseAppConfig appCfg = getAppConfig(pkg);
        if (appCfg != null) {
            return appCfg.isCameraPreview();
        }
        return false;
    }

    public String[] getHomes(String pkg) {
        if (isEmpty(pkg)) {
            return null;
        }
        HomeConfig homeTarget = this.mHomeConfigs.get(pkg);
        return homeTarget != null ? homeTarget.getHomes() : new String[0];
    }

    public int getBoundPosition(Rect bounds, int defaultPos) {
        if (this.mMasterBoounds.contains(bounds)) {
            return 1;
        }
        if (this.mSlaveBoounds.contains(bounds)) {
            return 2;
        }
        if (this.mMidBoounds.contains(bounds)) {
            return 3;
        }
        return defaultPos;
    }

    public Rect getBounds(int position, String pkgName) {
        return getBounds(position, isScaled(pkgName), isDragable(pkgName), pkgName);
    }

    public Rect getBounds(int type) {
        if (type == 1) {
            return this.mMasterBounds;
        }
        if (type == 2) {
            return this.mSlaveBounds;
        }
        if (type == 3) {
            return this.mMiddleBounds;
        }
        if (type != 5) {
            return this.mMiddleBounds;
        }
        return this.mFullBounds;
    }

    private Rect[] getOrigDragRect(String pkgName) {
        if (!isDragable(pkgName)) {
            return new Rect[]{DEFAULT_LEFT_BOUNDS, DEFAULT_RIGHT_BOUNDS};
        }
        if (!this.mAppDragBoundsConfigs.containsKey(pkgName)) {
            return new Rect[]{this.mLeftDragleBounds, this.mRightDragleBounds};
        }
        List<Rect> listRects = this.mAppDragBoundsConfigs.get(pkgName);
        return new Rect[]{listRects.get(0), listRects.get(1)};
    }

    public void updateAppBoundsFromMode(Map<Integer, List<Rect>> bounds) {
        this.mDragBounds = bounds;
        updateBoundsSet();
        this.mSettingConfigs.forEach(new BiConsumer(bounds) {
            /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowConfig$OMnFhyN8IU16B5JFAKxrK59LB8 */
            private final /* synthetic */ Map f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                HwMagicWindowConfig.this.lambda$updateAppBoundsFromMode$6$HwMagicWindowConfig(this.f$1, (String) obj, (SettingConfig) obj2);
            }
        });
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: java.util.Map<java.lang.String, java.util.List<android.graphics.Rect>> */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$updateAppBoundsFromMode$6$HwMagicWindowConfig(Map bounds, String pkg, SettingConfig cfg) {
        this.mAppDragBoundsConfigs.put(pkg, bounds.getOrDefault(Integer.valueOf(cfg.getDragMode()), bounds.get(0)));
    }

    public void updateDragModeForLocaleChange() {
        this.mAppDragBoundsConfigs.clear();
        this.mSettingConfigs.forEach($$Lambda$HwMagicWindowConfig$nuieNlM8h9MAtA9qR70xiAxZKCE.INSTANCE);
    }

    static /* synthetic */ void lambda$updateDragModeForLocaleChange$7(String pkg, SettingConfig cfg) {
        if (cfg.getDragMode() == 1) {
            cfg.setDragMode(2);
        } else if (cfg.getDragMode() == 2) {
            cfg.setDragMode(1);
        }
    }

    public void updateAppDragBounds(String pkgName, Rect leftBounds, Rect rightBounds, int dragMode) {
        if (!isEmpty(pkgName) && leftBounds != null && rightBounds != null) {
            List<Rect> listRect = new ArrayList<>();
            listRect.add(leftBounds);
            listRect.add(rightBounds);
            this.mAppDragBoundsConfigs.put(pkgName, listRect);
            this.mSettingConfigs.computeIfPresent(pkgName, new BiFunction(dragMode) {
                /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowConfig$m3vYLuKUpPQ3zYFLxIhuA76yQU */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.BiFunction
                public final Object apply(Object obj, Object obj2) {
                    String str = (String) obj;
                    return ((SettingConfig) obj2).setDragMode(this.f$0);
                }
            });
        }
    }

    public Rect[] adjustBoundsForResize(Rect leftBound, Rect rightBound) {
        if (this.mSystemConfig == null || leftBound == null || rightBound == null) {
            return null;
        }
        Rect newLeftBounds = new Rect();
        Rect newRightBounds = new Rect();
        if (leftBound.left != rightBound.left || leftBound.top >= rightBound.top) {
            newLeftBounds.left = leftBound.left + dp2px(this.mSystemConfig.mLeftPadding);
            newLeftBounds.top = leftBound.top + dp2px(this.mSystemConfig.mTopPadding);
            newLeftBounds.right = leftBound.right;
            newLeftBounds.bottom = leftBound.bottom - dp2px(this.mSystemConfig.mBottomPadding);
            newRightBounds.left = rightBound.left;
            newRightBounds.top = rightBound.top + dp2px(this.mSystemConfig.mTopPadding);
            newRightBounds.right = rightBound.right - dp2px(this.mSystemConfig.mRightPadding);
            newRightBounds.bottom = rightBound.bottom - dp2px(this.mSystemConfig.mBottomPadding);
        } else {
            newLeftBounds.left = leftBound.top + dp2px(this.mSystemConfig.mLeftPadding);
            newLeftBounds.top = leftBound.left + dp2px(this.mSystemConfig.mTopPadding);
            newLeftBounds.right = leftBound.bottom;
            newLeftBounds.bottom = leftBound.right - dp2px(this.mSystemConfig.mBottomPadding);
            newRightBounds.left = rightBound.top;
            newRightBounds.top = rightBound.left + dp2px(this.mSystemConfig.mTopPadding);
            newRightBounds.right = rightBound.bottom - dp2px(this.mSystemConfig.mRightPadding);
            newRightBounds.bottom = rightBound.right - dp2px(this.mSystemConfig.mBottomPadding);
        }
        return new Rect[]{newLeftBounds, newRightBounds};
    }

    public Rect getBounds(int position, boolean isScaled) {
        return getBounds(position, isScaled, false, "");
    }

    private Rect getBounds(int position, boolean isScaled, boolean isDragable, String pkgName) {
        if (this.mSystemConfig == null) {
            return DEFAULT_SINGLE_BOUNDS;
        }
        int posIndex = 1;
        if (position == 1 || position == 2) {
            if (!isDragable) {
                return getRationBounds(position, isScaled, pkgName);
            }
            if ((position == 1 && !this.mIsCurrentRtl) || (position == 2 && this.mIsCurrentRtl)) {
                posIndex = 0;
            }
            return getOrigDragRect(pkgName)[posIndex];
        } else if (position == 3) {
            return getMidConfigBounds(position, isScaled, isDragable, pkgName);
        } else {
            if (position != 5) {
                return DEFAULT_SINGLE_BOUNDS;
            }
            return getBounds(5);
        }
    }

    private Rect getMidConfigBounds(int type, boolean scaled, boolean isDragable, String pkgName) {
        if (isSupportAppTaskSplitScreen(pkgName)) {
            Rect midRect = getBounds(5);
            this.mMidBoounds.add(midRect);
            return midRect;
        }
        Rect midRect2 = isDragable ? this.mMiddleDragleBounds : this.mMiddleBounds;
        if (isDragable || !scaled) {
            return midRect2;
        }
        return getScaledBound(midRect2);
    }

    private Rect getRationBounds(int position, boolean scaled, String pkgName) {
        if (position != 1 && position != 2) {
            return null;
        }
        Rect rect = getBounds(position);
        if (scaled) {
            rect = getScaledBound(rect);
        }
        EasyGoConfig pkgConfig = getOpenCapAppConfig(pkgName);
        if (pkgConfig == null) {
            return rect;
        }
        Rect rationRect = pkgConfig.getPositionRationBound(position);
        return rationRect == null ? rect : rationRect;
    }

    public void updateSystemBoundSize(DisplayMetrics metrics) {
        if (this.mSystemConfig != null) {
            updateSystemBoundSize(this.mAttribute.getDisplayMetrics(), this.mAttribute.isFoldableDevice(), this.mIsCurrentRtl);
            this.mEasyGoConfigs.forEach($$Lambda$HwMagicWindowConfig$QbiN_nF7wD_AAZAaN2A2ivfIriQ.INSTANCE);
        }
        updateScaleRatio();
        updateBoundsSet();
    }

    private void updateScaleRatio() {
        if (this.mSystemConfig != null && this.mAttribute.isPadDevice()) {
            this.mRatio = ((float) this.mMiddleBounds.width()) / ((float) Math.min(this.mAttribute.getDisplayMetrics().widthPixels, this.mAttribute.getDisplayMetrics().heightPixels));
        }
    }

    private void updateBoundsSet() {
        if (this.mSystemConfig == null) {
            SlogEx.w(TAG, "SystemConfig is null, can not update ration bound");
            return;
        }
        this.mMasterBoounds.clear();
        this.mSlaveBoounds.clear();
        this.mMidBoounds.clear();
        this.mMasterBoounds.add(this.mMasterBounds);
        this.mSlaveBoounds.add(this.mSlaveBounds);
        this.mMidBoounds.add(this.mMiddleBounds);
        this.mMidBoounds.add(this.mMiddleDragleBounds);
        this.mEasyGoConfigs.forEach($$Lambda$HwMagicWindowConfig$Q6V9SOQx3Y0yAeW121S8N6Ak_0.INSTANCE);
        if (this.mAttribute.isPadDevice()) {
            this.mMasterBoounds.add(getScaledBound(this.mMasterBounds));
            this.mSlaveBoounds.add(getScaledBound(this.mSlaveBounds));
            this.mMidBoounds.add(getScaledBound(this.mMiddleBounds));
        }
        Map<Integer, List<Rect>> map = this.mDragBounds;
        if (map != null) {
            for (Map.Entry<Integer, List<Rect>> map2 : map.entrySet()) {
                List<Rect> bounds = map2.getValue();
                this.mMasterBoounds.add(bounds.get(isRtl() ? 1 : 0));
                this.mSlaveBoounds.add(bounds.get(!isRtl()));
            }
        }
    }

    private int dp2px(float dp) {
        return (int) Math.ceil((double) (this.mAttribute.getDisplayMetrics().density * dp));
    }

    private int px2dp(float px) {
        return (int) Math.floor((double) (px / this.mAttribute.getDisplayMetrics().density));
    }

    private int getIdKeyPixelSize(String key, int def, Resources res) {
        int idKey = HwPartResourceUtils.getResourceId(key);
        if (idKey == -1) {
            return def;
        }
        return res.getDimensionPixelSize(idKey);
    }

    private int getDivideBarPixelSize(int def, int widthDp) {
        if (!this.mAttribute.isInFoldedStatus()) {
            return getIdKeyPixelSize("hw_split_divider_bar_width", def, this.mContext.getResources());
        }
        Configuration config = new Configuration(this.mContext.getResources().getConfiguration());
        config.smallestScreenWidthDp = widthDp;
        config.screenWidthDp = widthDp;
        Context newCtx = this.mContext.createConfigurationContext(config);
        if (newCtx == null || newCtx.getResources() == null) {
            return def;
        }
        return getIdKeyPixelSize("hw_split_divider_bar_width", def, newCtx.getResources());
    }

    private void updateSystemBoundSize(DisplayMetrics metrics, boolean isFoldScreen, boolean isRtl) {
        int[] displaySize = getAdjustDisplaySize(metrics, isFoldScreen);
        if (displaySize != null) {
            int width = displaySize[0];
            int height = displaySize[1];
            SlogEx.d(TAG, "initWindowBounds  w=" + width + " h=" + height);
            int leftToMiddleX = (width - dp2px(this.mSystemConfig.mMidPadding)) / 2;
            int rightToMiddleX = (dp2px(this.mSystemConfig.mMidPadding) + width) / 2;
            int leftPadding = dp2px(this.mSystemConfig.mLeftPadding);
            int topPadding = dp2px(this.mSystemConfig.mTopPadding);
            int rightPadding = dp2px(this.mSystemConfig.mRightPadding);
            int bottomPadding = dp2px(this.mSystemConfig.mBottomPadding);
            if (isFoldScreen) {
                this.mMasterBounds.set(0, 0, leftToMiddleX, height);
                this.mSlaveBounds.set(rightToMiddleX, 0, width, height);
                Rect rect = new Rect(0, 0, width, height);
                this.mMiddleBounds = rect;
                this.mMiddleDragleBounds = rect;
                this.mFullBounds = rect;
            } else {
                this.mMasterBounds.set(leftPadding, topPadding, leftToMiddleX, height - bottomPadding);
                this.mSlaveBounds.set(rightToMiddleX, topPadding, width - rightPadding, height - bottomPadding);
                int boundWidth = leftToMiddleX - leftPadding;
                this.mMiddleBounds.set((width - boundWidth) / 2, topPadding, (width + boundWidth) / 2, height - bottomPadding);
                this.mFullBounds.set(0, 0, width, height);
            }
            if (isRtl) {
                Rect tempBounds = new Rect(this.mMasterBounds);
                this.mMasterBounds = this.mSlaveBounds;
                this.mSlaveBounds = tempBounds;
            }
            int divBarWidth = getDivideBarPixelSize(dp2px(this.mSystemConfig.mMidDragPadding), px2dp((float) width));
            int leftToMiddleDragableX = (width / 2) - (divBarWidth / 2);
            int boundDragableWidth = leftToMiddleDragableX - leftPadding;
            this.mLeftDragleBounds.set(leftPadding, topPadding, leftToMiddleDragableX, height - bottomPadding);
            this.mRightDragleBounds.set((width / 2) + (divBarWidth / 2), topPadding, width - rightPadding, height - bottomPadding);
            if (!isFoldScreen) {
                this.mMiddleDragleBounds.set((width - boundDragableWidth) / 2, topPadding, (width + boundDragableWidth) / 2, height - bottomPadding);
            }
            this.mSystemConfig.mSplitAdjustValue = (int) (metrics.density * 4.0f);
        }
    }

    private int[] getAdjustDisplaySize(DisplayMetrics metrics, boolean isFoldScreen) {
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if (isFoldScreen) {
            Rect fullRect = HwFoldScreenState.getScreenPhysicalRect(1);
            if (fullRect == null) {
                return null;
            }
            width = fullRect.width();
            height = fullRect.height();
        }
        if (!isFoldScreen && height > width) {
            width = metrics.heightPixels;
            height = metrics.widthPixels;
        }
        return new int[]{width, height};
    }

    public boolean isSystemSupport(int type) {
        SystemConfig systemConfig = this.mSystemConfig;
        if (systemConfig == null) {
            return false;
        }
        if (type == 0) {
            return systemConfig.isSystemSupport(0);
        }
        if (type == 1) {
            return systemConfig.isSystemSupport(1);
        }
        if (type == 2) {
            return systemConfig.isSystemSupport(2);
        }
        if (type == 3) {
            return systemConfig.isSystemSupport(3);
        }
        if (type == 4) {
            return systemConfig.isSystemSupport(4);
        }
        if (type != 5) {
            return false;
        }
        return systemConfig.isSystemSupport(5);
    }

    public float getCornerRadius() {
        SystemConfig systemConfig = this.mSystemConfig;
        return systemConfig == null ? HwMagicWinAnimation.INVALID_THRESHOLD : (float) dp2px(systemConfig.mCornerRadius);
    }

    public int getHostViewThreshold() {
        SystemConfig systemConfig = this.mSystemConfig;
        if (systemConfig == null) {
            return 70;
        }
        return systemConfig.getHostViewThreshold();
    }

    public float getRatio(String pkgName) {
        if (isScaled(pkgName)) {
            return this.mRatio;
        }
        return 1.0f;
    }

    private Rect getScaledBound(Rect rect) {
        int leftValue = rect.left;
        int topValue = rect.top;
        return new Rect(leftValue, topValue, ((int) ((((float) rect.width()) / this.mRatio) + 0.5f)) + leftValue, ((int) ((((float) rect.height()) / this.mRatio) + 0.5f)) + topValue);
    }

    public boolean getHwMagicWinEnabled(String pkg) {
        SettingConfig target;
        if (!isEmpty(pkg) && (target = this.mSettingConfigs.get(pkg)) != null && target.getHwMagicWinEnabled()) {
            return true;
        }
        return false;
    }

    public boolean getDialogShownForApp(String pkg) {
        SettingConfig target;
        if (!isEmpty(pkg) && (target = this.mSettingConfigs.get(pkg)) != null && target.getHwDialogShown()) {
            return true;
        }
        return false;
    }

    public Map<String, Boolean> getHwMagicWinEnabledApps() {
        Map<String, Boolean> mHwMagicWinEnabledApps = new HashMap<>();
        this.mSettingConfigs.forEach(new BiConsumer(mHwMagicWinEnabledApps) {
            /* class com.huawei.server.magicwin.$$Lambda$HwMagicWindowConfig$soYD7N8GwJkTD3u8uBA4ylmbD6U */
            private final /* synthetic */ Map f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                HwMagicWindowConfig.lambda$getHwMagicWinEnabledApps$11(this.f$0, (String) obj, (SettingConfig) obj2);
            }
        });
        return mHwMagicWinEnabledApps;
    }

    static /* synthetic */ void lambda$getHwMagicWinEnabledApps$11(Map mHwMagicWinEnabledApps, String pkg, SettingConfig cfg) {
        Boolean bool = (Boolean) mHwMagicWinEnabledApps.put(pkg, Boolean.valueOf(cfg.getHwMagicWinEnabled()));
    }

    public Map<String, SettingConfig> getHwMagicWinSettingConfigs() {
        return this.mSettingConfigs;
    }

    public boolean isNotchModeEnabled(String pkg) {
        BaseAppConfig appCfg;
        if (pkg == null || pkg.isEmpty() || (appCfg = getAppConfig(pkg)) == null) {
            return false;
        }
        return appCfg.isNotchModeEnabled();
    }

    public boolean isScaled(String pkgName) {
        BaseAppConfig appCfg;
        if (this.mAttribute.isFoldableDevice() || this.mAttribute.isVirtualContainer() || isSupportAppTaskSplitScreen(pkgName) || (appCfg = getAppConfig(pkgName)) == null || !appCfg.isScaleEnabled() || isDragable(pkgName)) {
            return false;
        }
        return true;
    }

    public boolean isDragable(String pkgName) {
        BaseAppConfig appCfg;
        if (isSupportAppTaskSplitScreen(pkgName) || (appCfg = getAppConfig(pkgName)) == null) {
            return false;
        }
        if (!this.mAttribute.isFoldableDevice()) {
            return appCfg.isDragable();
        }
        if (!appCfg.isDragable() || !appCfg.isDragToFullscreen() || !isSystemSupport(5)) {
            return false;
        }
        return true;
    }

    public static int parseIntParama(String paramsStr, String key) {
        try {
            return new JSONObject(paramsStr).getInt(key);
        } catch (JSONException e) {
            SlogEx.e(TAG, "parseIntParamas fail ");
            return -1;
        }
    }

    public static class HomeConfig {
        private String[] mHomeActivities;
        private String mPackageName;

        public HomeConfig() {
        }

        public HomeConfig(String packageName, String[] homeActivities) {
            this.mPackageName = packageName;
            this.mHomeActivities = (String[]) homeActivities.clone();
        }

        public String[] getHomes() {
            return this.mHomeActivities;
        }
    }

    public class EasyGoConfig extends BaseAppConfig {
        private static final int DEFALUT_VAULE = -1;
        private static final String DRAG_FS_DEV_TYPE_ALL = "ALL";
        private static final String DRAG_FS_DEV_TYPE_FOLD = "FOLD";
        private static final String DRAG_FS_DEV_TYPE_PAD = "PAD";
        private static final String PRIMARY = "primary";
        private boolean isRelaunch;
        private String mClientRequest;
        private List<String> mDefaultFullScreenActivities = new ArrayList();
        private List<String> mDestroyWhenReplacedOnRightActivities = new ArrayList();
        private boolean mIsDragableExist = false;
        private boolean mIsScaleExist = false;
        private boolean mIsShowStatusBar = false;
        private boolean mIsSupportAppTaskSplitScreen = false;
        private boolean mIsUsingSystemActivityAnimation = true;
        private boolean mIsVideoFullscreenExist = false;
        private List<String> mLockMasterActivities = new ArrayList();
        private int mMode = -1;
        private int mRationL = -1;
        private Rect mRationMasterBound;
        private int mRationR = -1;
        private Rect mRationSlaveBound;
        private String mRelateActivity = "";
        private Map<String, Set<String>> mSpecPairActivityInfo = new HashMap();
        private int mSplitBarBgColor = -1;
        private int mSplitLineBgColor = -1;
        private List<String> mSupportTaskSplitScreenActivities = new ArrayList();
        private List<String> mTransActivities = new ArrayList();

        public boolean isVideoFullscreenExist() {
            return this.mIsVideoFullscreenExist;
        }

        public boolean isDragableExist() {
            return this.mIsDragableExist;
        }

        public boolean isScaleExist() {
            return this.mIsScaleExist;
        }

        private void setWindowsRationAndBound(String ratioParam, String devType) {
            if ((HwMagicWindowConfig.this.mAttribute.isFoldableDevice() ? DRAG_FS_DEV_TYPE_FOLD : DRAG_FS_DEV_TYPE_PAD).equals(devType)) {
                String[] foldRatio = ratioParam.split(HwMagicWindowConfig.RATIO_SPLITTER);
                if (foldRatio.length == 2) {
                    this.mRationL = HwMagicWindowConfig.strToInt(foldRatio[0], -1);
                    this.mRationR = HwMagicWindowConfig.strToInt(foldRatio[1], -1);
                    SlogEx.i(HwMagicWindowConfig.TAG, "setRationAndBound LR " + this.mRationL + " RR " + this.mRationR);
                    updateRationAndBound();
                    saveRationBound();
                }
            }
        }

        public void updateRationAndBound() {
            if (HwMagicWindowConfig.this.mSystemConfig == null || this.mRationL <= 0 || this.mRationR <= 0) {
                this.mRationSlaveBound = null;
                this.mRationMasterBound = null;
                return;
            }
            this.mRationMasterBound = new Rect(HwMagicWindowConfig.this.getBounds(1));
            this.mRationSlaveBound = new Rect(HwMagicWindowConfig.this.getBounds(2));
            if (!HwMagicWindowConfig.this.mIsCurrentRtl) {
                int gapRtoL = this.mRationSlaveBound.left - this.mRationMasterBound.right;
                Rect rect = this.mRationMasterBound;
                int i = rect.left;
                int i2 = this.mRationL;
                rect.right = i + ((((this.mRationSlaveBound.right - this.mRationMasterBound.left) - gapRtoL) * i2) / (this.mRationR + i2));
                this.mRationSlaveBound.left = this.mRationMasterBound.right + gapRtoL;
            } else {
                int gapRtoL2 = this.mRationMasterBound.left - this.mRationSlaveBound.right;
                Rect rect2 = this.mRationSlaveBound;
                int i3 = rect2.left;
                int i4 = this.mRationR;
                rect2.right = i3 + ((((this.mRationMasterBound.right - this.mRationSlaveBound.left) - gapRtoL2) * i4) / (i4 + this.mRationL));
                this.mRationMasterBound.left = this.mRationSlaveBound.right + gapRtoL2;
            }
            SlogEx.i(HwMagicWindowConfig.TAG, "getBounds add slaveBound " + this.mRationSlaveBound + " masterBound " + this.mRationMasterBound);
        }

        public void saveRationBound() {
            if (HwMagicWindowConfig.this.mSystemConfig == null) {
                SlogEx.w(HwMagicWindowConfig.TAG, "SystemConfig is null, can not save ration bound");
                return;
            }
            Rect adjustRationRightBound = new Rect(this.mRationSlaveBound);
            int adjust = HwMagicWindowConfig.this.mSystemConfig.getSplitAdjustValue();
            if (!HwMagicWindowConfig.this.mIsCurrentRtl) {
                adjustRationRightBound.left += adjust;
            } else {
                adjustRationRightBound.right -= adjust;
            }
            HwMagicWindowConfig.this.mMasterBoounds.add(this.mRationMasterBound);
            HwMagicWindowConfig.this.mSlaveBoounds.add(this.mRationSlaveBound);
            HwMagicWindowConfig.this.mSlaveBoounds.add(adjustRationRightBound);
        }

        public Rect getPositionRationBound(int position) {
            if (position == 2) {
                return this.mRationSlaveBound;
            }
            if (position == 1) {
                return this.mRationMasterBound;
            }
            return null;
        }

        public EasyGoConfig(String packageName, String clientRequest) {
            this.mPackageName = packageName;
            this.mClientRequest = clientRequest;
            this.mMode = -2;
            this.mSupportLeftResume = false;
            this.mSupportVideoFScreen = true;
            this.mSupportCameraPreview = false;
            this.mIsScaleEnabled = false;
            this.mIsDefaultSetting = true ^ HwMagicWindowConfig.this.mAttribute.isFoldableDevice();
            this.mIsDragable = false;
            this.mNeedRelaunch = false;
            this.mIsNotchAdapted = false;
            this.mIsDragToFullscreen = false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseAppConfig() {
            try {
                JSONObject jsonBody = new JSONObject(this.mClientRequest);
                this.mMode = HwMagicWindowConfig.strToInt(jsonBody.getString(HwMagicWindowConfig.BODY_MODE), -1);
                if (!isValidMode(this.mMode)) {
                    this.mMode = -1;
                }
                this.mSupportVideoFScreen = getProperty(jsonBody, HwMagicWindowConfig.BODY_FULLSCREEN_VIDEO, this.mSupportVideoFScreen);
                if (!jsonBody.isNull(HwMagicWindowConfig.BODY_DUAL_ACTS)) {
                    JSONObject dualPageJsonObj = jsonBody.getJSONObject(HwMagicWindowConfig.BODY_DUAL_ACTS);
                    split(dualPageJsonObj.optString(HwMagicWindowConfig.MAIN_PAGE_SET));
                    this.mRelateActivity = dualPageJsonObj.optString(HwMagicWindowConfig.RELATED_PAGE);
                    if (this.mMainActivities.isEmpty() || this.mMainActivities.contains(this.mRelateActivity)) {
                        this.mRelateActivity = "";
                        this.mMainActivities.clear();
                    }
                }
                JSONArray entities = jsonBody.optJSONArray(HwMagicWindowConfig.BODY_ACT_PAIRS);
                boolean z = true;
                if (this.mMode == 1) {
                    z = false;
                }
                parsePairsActivities(entities, z);
                parseTransActivities(jsonBody.optJSONArray(HwMagicWindowConfig.BODY_TRANS_ACTS));
                this.isRelaunch = Boolean.valueOf(jsonBody.optString(HwMagicWindowConfig.BODY_RELAUNCH_ON_RESIZE)).booleanValue();
                if (jsonBody.has(HwMagicWindowConfig.BODY_TASK_SPLIT_SCREEN)) {
                    this.mIsSupportAppTaskSplitScreen = Boolean.valueOf(jsonBody.optString(HwMagicWindowConfig.BODY_TASK_SPLIT_SCREEN)).booleanValue();
                }
                if (jsonBody.has(HwMagicWindowConfig.BODY_ACTS)) {
                    parseActivitiesParams(jsonBody.optJSONArray(HwMagicWindowConfig.BODY_ACTS));
                }
                if (jsonBody.has(HwMagicWindowConfig.BODY_UX)) {
                    parseUxParams(jsonBody.getJSONObject(HwMagicWindowConfig.BODY_UX));
                }
            } catch (NumberFormatException | JSONException e) {
                SlogEx.e(HwMagicWindowConfig.TAG, "parseRequest fail " + e);
                clearAppConfig();
            } catch (Exception e2) {
                SlogEx.e(HwMagicWindowConfig.TAG, "parseRequest fail unknow Exception");
                clearAppConfig();
            }
        }

        private void clearAppConfig() {
            HwMagicWindowConfig.this.delAppAdapterInfo(this.mPackageName);
        }

        private void parseUxParams(JSONObject uxJsonObj) throws JSONException {
            if (uxJsonObj == null) {
                SlogEx.d(HwMagicWindowConfig.TAG, "no Ux Params");
                return;
            }
            this.mIsDragable = getProperty(uxJsonObj, HwMagicWindowConfig.UX_IS_DRAGABLE, this.mIsDragable);
            this.mIsScaleEnabled = getProperty(uxJsonObj, HwMagicWindowConfig.UX_IS_SCALED, this.mIsScaleEnabled);
            if (uxJsonObj.has(HwMagicWindowConfig.UX_IS_SHOW_STATUSBAR)) {
                this.mIsShowStatusBar = Boolean.valueOf(uxJsonObj.optString(HwMagicWindowConfig.UX_IS_SHOW_STATUSBAR)).booleanValue();
            }
            if (uxJsonObj.has(HwMagicWindowConfig.UX_SPLIT_LINE_BG_COLOR)) {
                this.mSplitLineBgColor = Color.parseColor(uxJsonObj.optString(HwMagicWindowConfig.UX_SPLIT_LINE_BG_COLOR).replace("0x", "#"));
            }
            if (uxJsonObj.has(HwMagicWindowConfig.UX_SPLIT_BAR_BG_COLOR)) {
                this.mSplitBarBgColor = Color.parseColor(uxJsonObj.optString(HwMagicWindowConfig.UX_SPLIT_BAR_BG_COLOR).replace("0x", "#"));
            }
            if (uxJsonObj.has(HwMagicWindowConfig.UX_WINDOWS_RATIO)) {
                JSONArray entities = uxJsonObj.optJSONArray(HwMagicWindowConfig.UX_WINDOWS_RATIO);
                for (int i = 0; i < entities.length(); i++) {
                    JSONObject eachEntity = entities.optJSONObject(i);
                    if (eachEntity != null) {
                        String deviceType = eachEntity.optString(HwMagicWindowConfig.WINDOWS_RATIO_DEVICE);
                        String ratio = eachEntity.optString(HwMagicWindowConfig.WINDOWS_RATIO_RATIO);
                        if (!HwMagicWindowConfig.isEmpty(deviceType) && !HwMagicWindowConfig.isEmpty(ratio)) {
                            setWindowsRationAndBound(ratio, deviceType);
                        }
                    }
                }
            }
            if (uxJsonObj.has(HwMagicWindowConfig.UX_USE_SYSTEM_ACTIVITY_ANIMATION)) {
                this.mIsUsingSystemActivityAnimation = Boolean.valueOf(uxJsonObj.optString(HwMagicWindowConfig.UX_USE_SYSTEM_ACTIVITY_ANIMATION)).booleanValue();
            }
            if (uxJsonObj.has(HwMagicWindowConfig.UX_KEEP_PRIMARY_TOP_ALWAYS_RESUME)) {
                this.mSupportLeftResume = Boolean.valueOf(uxJsonObj.optString(HwMagicWindowConfig.UX_KEEP_PRIMARY_TOP_ALWAYS_RESUME)).booleanValue();
            }
            parseUxSupportDraggingToFullscreen(uxJsonObj);
        }

        private void parseUxSupportDraggingToFullscreen(JSONObject uxJsonObj) {
            String[] devTypes;
            if (uxJsonObj.has(HwMagicWindowConfig.UX_SUPPORT_DRAGGING_TO_FULLSCREEN) && (devTypes = uxJsonObj.optString(HwMagicWindowConfig.UX_SUPPORT_DRAGGING_TO_FULLSCREEN).split(HwMagicWindowConfig.RATIO_SPLITTER)) != null && devTypes.length != 0) {
                this.mIsDragToFullscreen = canDragFsByDevType(devTypes);
            }
        }

        private boolean canDragFsByDevType(String[] types) {
            for (String type : types) {
                String type2 = type.trim();
                if (DRAG_FS_DEV_TYPE_ALL.equals(type2)) {
                    return true;
                }
                if (DRAG_FS_DEV_TYPE_PAD.equals(type2) && HwMagicWindowConfig.this.mAttribute.isPadDevice()) {
                    return true;
                }
                if (DRAG_FS_DEV_TYPE_FOLD.equals(type2) && HwMagicWindowConfig.this.mAttribute.isFoldableDevice()) {
                    return true;
                }
                SlogEx.w(HwMagicWindowConfig.TAG, "Unknown type:" + type2);
            }
            return false;
        }

        private boolean getProperty(JSONObject jsonObj, String propertyName, boolean defaultValue) {
            if (jsonObj == null || HwMagicWindowConfig.isEmpty(propertyName)) {
                return defaultValue;
            }
            String value = jsonObj.optString(propertyName);
            if ("true".equalsIgnoreCase(value)) {
                return true;
            }
            if ("false".equalsIgnoreCase(value)) {
                return false;
            }
            return defaultValue;
        }

        private boolean isValidMode(int mode) {
            return mode == 0 || mode == 1;
        }

        private void parseActivitiesParams(JSONArray entities) throws JSONException {
            if (entities == null || entities.length() <= 0) {
                SlogEx.d(HwMagicWindowConfig.TAG, "no activity Params");
                return;
            }
            for (int i = 0; i < entities.length(); i++) {
                JSONObject eachEntity = entities.optJSONObject(i);
                if (eachEntity != null) {
                    String activityName = eachEntity.optString(HwMagicWindowConfig.ACT_NAME);
                    if (eachEntity.has(HwMagicWindowConfig.ACT_DEFAULT_FULLSCREEN) && Boolean.valueOf(eachEntity.optString(HwMagicWindowConfig.ACT_DEFAULT_FULLSCREEN)).booleanValue()) {
                        this.mDefaultFullScreenActivities.add(activityName);
                    }
                    if (eachEntity.has(HwMagicWindowConfig.ACT_SUPPORT_TASK_SPLIT_SCREEN) && Boolean.valueOf(eachEntity.optString(HwMagicWindowConfig.ACT_SUPPORT_TASK_SPLIT_SCREEN)).booleanValue()) {
                        this.mSupportTaskSplitScreenActivities.add(activityName);
                    }
                    if (eachEntity.has(HwMagicWindowConfig.ACT_DESTROY_WHEN_REPLACE_ON_RIGHT) && Boolean.valueOf(eachEntity.optString(HwMagicWindowConfig.ACT_DESTROY_WHEN_REPLACE_ON_RIGHT)).booleanValue()) {
                        this.mDestroyWhenReplacedOnRightActivities.add(activityName);
                    }
                    if (eachEntity.has(HwMagicWindowConfig.ACT_LOCK_SIDE) && PRIMARY.equals(eachEntity.optString(HwMagicWindowConfig.ACT_LOCK_SIDE))) {
                        this.mLockMasterActivities.add(activityName);
                    }
                }
            }
        }

        private void parsePairsActivities(JSONArray entities, boolean isSkipFromTo) throws JSONException {
            if (entities != null && entities.length() > 0) {
                for (int i = 0; i < entities.length(); i++) {
                    JSONObject eachEntity = entities.optJSONObject(i);
                    if (eachEntity != null) {
                        if (!isSkipFromTo) {
                            addActivityPolicyMap(eachEntity.optString(HwMagicWindowConfig.ACT_PAIRS_FROM), eachEntity.optString(HwMagicWindowConfig.ACT_PAIRS_TO));
                        }
                        String mainActivity = eachEntity.optString(HwMagicWindowConfig.MAIN_PAGE);
                        String relateActivity = eachEntity.optString(HwMagicWindowConfig.RELATED_PAGE);
                        if (!HwMagicWindowConfig.isEmpty(relateActivity) && !HwMagicWindowConfig.isEmpty(mainActivity) && HwMagicWindowConfig.isEmpty(this.mRelateActivity) && HwMagicWindowConfig.isEmpty(this.mMainActivities) && !mainActivity.equals(relateActivity)) {
                            this.mRelateActivity = relateActivity;
                            if (this.mMainActivities == null) {
                                this.mMainActivities = new ArrayList();
                            }
                            this.mMainActivities.add(mainActivity);
                        }
                    }
                }
            }
        }

        private void parseTransActivities(JSONArray transEntities) throws JSONException {
            if (transEntities != null && transEntities.length() > 0) {
                for (int i = 0; i < transEntities.length(); i++) {
                    String activityName = transEntities.getString(i);
                    if (!HwMagicWindowConfig.isEmpty(activityName)) {
                        this.mTransActivities.add(activityName);
                    }
                }
            }
        }

        private void addActivityPolicyMap(String activityFrom, String activityTo) {
            if (!HwMagicWindowConfig.isEmpty(activityFrom) && !HwMagicWindowConfig.isEmpty(activityTo)) {
                Set<String> singleActPolicySet = this.mSpecPairActivityInfo.get(activityFrom);
                if (singleActPolicySet == null) {
                    singleActPolicySet = new HashSet();
                    this.mSpecPairActivityInfo.put(activityFrom, singleActPolicySet);
                }
                singleActPolicySet.add(activityTo);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isSpecPairActivities(String focus, String target) {
            Map<String, Set<String>> map;
            Set<String> set;
            if (HwMagicWindowConfig.isEmpty(focus) || HwMagicWindowConfig.isEmpty(target) || (map = this.mSpecPairActivityInfo) == null || !map.containsKey(focus) || (set = this.mSpecPairActivityInfo.get(focus)) == null || set.isEmpty() || (!set.contains("*") && !set.contains(target))) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean isSpecTransActivity(String curActivity) {
            return this.mTransActivities.contains(curActivity);
        }

        /* access modifiers changed from: package-private */
        public boolean isDefaultFullscreenActivity(String curActivity) {
            return this.mDefaultFullScreenActivities.contains(curActivity);
        }

        /* access modifiers changed from: package-private */
        public boolean isNeedStartByNewTaskActivity(String curActivity) {
            return this.mSupportTaskSplitScreenActivities.contains(curActivity);
        }

        /* access modifiers changed from: package-private */
        public boolean isNeedDestroyWhenReplaceOnRight(String curActivity) {
            return this.mDestroyWhenReplacedOnRightActivities.contains(curActivity);
        }

        /* access modifiers changed from: package-private */
        public boolean isLockMasterActivity(String curActivity) {
            return this.mLockMasterActivities.contains(curActivity);
        }

        /* access modifiers changed from: package-private */
        public boolean isReLaunchWhenResize() {
            return this.isRelaunch;
        }

        /* access modifiers changed from: package-private */
        public boolean isShowStatusBar() {
            return this.mIsShowStatusBar;
        }

        /* access modifiers changed from: package-private */
        public boolean isUsingSystemActivityAnimation() {
            return this.mIsUsingSystemActivityAnimation;
        }

        /* access modifiers changed from: package-private */
        public boolean isSupportAppTaskSplitScreen() {
            return this.mIsSupportAppTaskSplitScreen;
        }

        /* access modifiers changed from: package-private */
        public int getSplitLineBgColor() {
            return this.mSplitLineBgColor;
        }

        /* access modifiers changed from: package-private */
        public int getSplitBarBgColor() {
            return this.mSplitBarBgColor;
        }

        public List<String> getMainActivity() {
            return this.mMainActivities;
        }

        public String getRelateActivity() {
            return this.mRelateActivity;
        }

        @Override // com.huawei.server.magicwin.BaseAppConfig
        public int getWindowMode() {
            int i = this.mMode;
            if (i == 0) {
                return 2;
            }
            if (i == 1) {
                return 3;
            }
            return -1;
        }
    }

    public static class SystemConfig {
        private static final String CONFIG_ALL = "ALL";
        private static final String CONFIG_LOCAL = "LOCAL";
        private static final String CONFIG_VIRTUAL = "VIRTUAL";
        private boolean mAnimation;
        private boolean mBackToMiddle;
        private boolean mBackground;
        private float mBottomPadding;
        private float mCornerRadius;
        private int mHostViewThreshold;
        private float mLeftPadding;
        private float mMidDragPadding;
        private float mMidPadding;
        private boolean mOpenCapability;
        private float mRightPadding;
        private boolean mRoundAngle;
        private int mSplitAdjustValue;
        private boolean mSupportDraggingToFullScreen;
        private boolean mSupportLocalConfigInOsd;
        private boolean mSupportVirtualConfigInOsd;
        private float mTopPadding;

        public SystemConfig() {
            this.mSplitAdjustValue = 0;
        }

        public SystemConfig(String leftPadding, String topPadding, String rightPadding, String bottomPadding, String midPadding, String midDragPadding, String roundAngle, String dynamicEffect, String background, String backToMiddle, String cornerRadius, String supportDraggingToFullScreen) {
            this.mLeftPadding = HwMagicWindowConfig.strToFloat(leftPadding, HwMagicWinAnimation.INVALID_THRESHOLD);
            this.mTopPadding = HwMagicWindowConfig.strToFloat(topPadding, HwMagicWinAnimation.INVALID_THRESHOLD);
            this.mRightPadding = HwMagicWindowConfig.strToFloat(rightPadding, HwMagicWinAnimation.INVALID_THRESHOLD);
            this.mBottomPadding = HwMagicWindowConfig.strToFloat(bottomPadding, HwMagicWinAnimation.INVALID_THRESHOLD);
            this.mMidPadding = HwMagicWindowConfig.strToFloat(midPadding, HwMagicWinAnimation.INVALID_THRESHOLD);
            this.mMidDragPadding = HwMagicWindowConfig.strToFloat(midDragPadding, HwMagicWinAnimation.INVALID_THRESHOLD);
            this.mRoundAngle = HwMagicWindowConfig.strToBoolean(roundAngle);
            this.mAnimation = HwMagicWindowConfig.strToBoolean(dynamicEffect);
            this.mBackground = HwMagicWindowConfig.strToBoolean(background);
            this.mBackToMiddle = HwMagicWindowConfig.strToBoolean(backToMiddle);
            this.mCornerRadius = HwMagicWindowConfig.strToFloat(cornerRadius, HwMagicWinAnimation.INVALID_THRESHOLD);
            this.mSupportDraggingToFullScreen = HwMagicWindowConfig.strToBoolean(supportDraggingToFullScreen);
        }

        public void setCapability(String capability) {
            if (capability != null) {
                String[] tmpArray = capability.split(HwMagicWindowConfig.RATIO_SPLITTER);
                if (tmpArray.length != 0) {
                    for (String cur : tmpArray) {
                        String cur2 = cur.trim();
                        if (CONFIG_LOCAL.equals(cur2)) {
                            this.mSupportLocalConfigInOsd = true;
                        } else if (CONFIG_VIRTUAL.equals(cur2)) {
                            this.mSupportVirtualConfigInOsd = true;
                        } else if (CONFIG_ALL.equals(cur2)) {
                            this.mSupportLocalConfigInOsd = true;
                            this.mSupportVirtualConfigInOsd = true;
                        } else {
                            SlogEx.w(HwMagicWindowConfig.TAG, "Unknown osd capability:" + cur2);
                        }
                    }
                }
            }
        }

        public boolean isSupportLocalConfig() {
            return this.mSupportLocalConfigInOsd;
        }

        public boolean isSupportVirtualConfig() {
            return this.mSupportVirtualConfigInOsd;
        }

        public void setHostViewThreshold(String viewThreshold) {
            this.mHostViewThreshold = HwMagicWindowConfig.strToInt(viewThreshold, 0);
        }

        public void setOpenCapability(String openCapability) {
            if (HwMagicWindowConfig.isEmpty(openCapability)) {
                this.mOpenCapability = false;
            } else {
                this.mOpenCapability = HwMagicWindowConfig.strToBoolean(openCapability);
            }
        }

        public int getHostViewThreshold() {
            int i = this.mHostViewThreshold;
            if (i > 0) {
                return i;
            }
            return 70;
        }

        public int getSplitAdjustValue() {
            return this.mSplitAdjustValue;
        }

        public boolean isSystemSupport(int type) {
            if (type == 0) {
                return this.mRoundAngle;
            }
            if (type == 1) {
                return this.mAnimation;
            }
            if (type == 2) {
                return this.mBackground;
            }
            if (type == 3) {
                return this.mOpenCapability;
            }
            if (type == 4) {
                return this.mBackToMiddle;
            }
            if (type != 5) {
                return false;
            }
            return this.mSupportDraggingToFullScreen;
        }
    }

    public Map<String, EasyGoConfig> getOpenCapAppConfigs() {
        return this.mEasyGoConfigs;
    }

    public void adjustSplitBound(int position, Rect bound) {
        if (position == 2) {
            SystemConfig systemConfig = this.mSystemConfig;
            int adjust = systemConfig == null ? 0 : systemConfig.getSplitAdjustValue();
            if (!this.mIsCurrentRtl) {
                bound.left += adjust;
            } else {
                bound.right -= adjust;
            }
        }
    }

    public static int strToInt(String value, int defaultValue) {
        if (isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            SlogEx.e(TAG, "parse string to int error");
            return defaultValue;
        }
    }

    /* access modifiers changed from: private */
    public static float strToFloat(String mode, float defaultValue) {
        if (mode == null || mode.isEmpty()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(mode);
        } catch (NumberFormatException e) {
            SlogEx.e(TAG, "parse string to float error.mode=" + mode + "  defaultValue=" + defaultValue);
            return defaultValue;
        }
    }

    public static boolean strToBoolean(String str) {
        return "true".equals(str);
    }

    public void setIsRtl(boolean isRtl) {
        this.mIsCurrentRtl = isRtl;
    }

    public boolean isRtl() {
        return this.mIsCurrentRtl;
    }

    public void dump(PrintWriter pw, String pkg) {
        BaseAppConfig bac = getAppConfig(pkg);
        if (bac == null) {
            pw.println("pkg=" + pkg + " type=<unknown>");
            return;
        }
        String cfgType = bac.getClass().getSimpleName();
        pw.println("pkg=" + pkg + " type=" + cfgType);
        pw.println("mode=" + getWindowMode(pkg) + " leftResume=" + isLeftResume(pkg) + " videoFullscreen=" + isVideoFullscreen(pkg) + " cameraPreview=" + isCameraPreview(pkg) + " scaled=" + isScaled(pkg) + " defaultState=" + isDefaultSetting(pkg) + " draggable=" + isDragable(pkg) + " supportDragToFullscreen=" + isSupportDraggingToFullScreen(pkg));
        dumpHome(pw, pkg);
    }

    /* access modifiers changed from: package-private */
    public void dumpHome(PrintWriter pw, String pkg) {
        String[] homes = getHomes(pkg);
        if (homes == null) {
            pw.println("home=null");
            return;
        }
        for (int i = 0; i < homes.length; i++) {
            pw.println(i + " home=" + homes[i]);
        }
    }

    private void updateInstalledPackages() {
        List<PackageInfo> pkgInfoList = this.mContext.getPackageManager().getInstalledPackages(0);
        mInstalledPkgNameList.clear();
        for (PackageInfo info : pkgInfoList) {
            mInstalledPkgNameList.add(info.packageName);
        }
    }

    public List<String> getAllInstalledPkgList() {
        return mInstalledPkgNameList;
    }

    public void clearAppConfigVirtual() {
        if (this.mAttribute.isVirtualContainer()) {
            this.mLocalConfigs.clear();
            this.mEasyGoConfigs.clear();
            Utils.dbg(Utils.TAG_SETTING, "clr setting: clear_virtual_cfg");
            this.mSettingConfigs.clear();
        }
    }

    public void delAppConfigUninstall(String pkgName, boolean isReplace) {
        if (!isReplace) {
            delAppAdapterInfo(pkgName);
            removeSetting(pkgName, "when_uninstall_app");
        }
        delLocalConfigs(pkgName);
        delInstalledPkgList(pkgName);
    }

    public void addAppConfigInstall(String pkgName) {
        addInstalledPkgList(pkgName);
        this.mCfgLoader.loadPackage(this, pkgName);
    }

    private void addInstalledPkgList(String pkgName) {
        if (!isEmpty(pkgName) && !mInstalledPkgNameList.contains(pkgName)) {
            mInstalledPkgNameList.add(pkgName);
        }
    }

    private void delInstalledPkgList(String pkgName) {
        if (!isEmpty(pkgName) && mInstalledPkgNameList.contains(pkgName)) {
            mInstalledPkgNameList.remove(pkgName);
        }
    }
}
