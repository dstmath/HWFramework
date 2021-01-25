package com.huawei.server.pc.vassist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.HwPCUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.server.pc.HwPCManagerService;
import com.huawei.server.pc.vassist.HwPCVAssistCmdExecutor;
import com.huawei.util.LogEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public final class HwPCVAssistAppStarter {
    private static final String APP_CENTER_SEARCH_ACTION = "com.huawei.appmarket.appmarket.intent.action.SearchActivity";
    private static final String APP_CENTER_SEARCH_KEY_WORD = "keyWord";
    private static final boolean DEBUG = LogEx.getLogHWInfo();
    private static final String HIAPP_PACKAGE_NAME = "com.huawei.appmarket";
    private static final String KEY_EEOR_CODE = "errorCode";
    private static final String KEY_RES_TEXT = "responseText";
    private static final String KEY_TTS_TEXT = "ttsText";
    static final int RESULT_APP_START_FAILED = 41;
    static final int RESULT_APP_START_FAILED_PARAMS = 42;
    static final int RESULT_APP_START_SUCC = -40;
    static final int RESULT_APP_START_SUCC_IN_PHONE = -41;
    static final int RESULT_APP_START_SUCC_ON_EXT_DISPLAY = -42;
    static final int RESULT_APP_START_SUCC_SEARCH_IN_MARKET = -43;
    private static final String TAG = "HwPCVAssistAppStarter";
    private static final String UNICODE_EXTERNAL_FILE_MANAGER = "\\u6211\\u7684\\u6587\\u4ef6";
    private static final String UNICODE_PHONE_FILE_MANAGER = "\\u6587\\u4ef6\\u7ba1\\u7406";
    private HwPCVAssistCmdExecutor mCmdExecutor;
    private Context mContext;
    private HwPCManagerService mService;

    public HwPCVAssistAppStarter(Context context, HwPCVAssistCmdExecutor cmdExecutor, HwPCManagerService service) {
        this.mContext = context;
        this.mCmdExecutor = cmdExecutor;
        this.mService = service;
    }

    private static String string2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();
        int len = string.length();
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            unicode.append("\\u");
            unicode.append(Integer.toHexString(c));
        }
        return unicode.toString();
    }

    private static Intent launchIntentForPhoneFileManager() {
        ComponentName component = new ComponentName("com.huawei.filemanager", "com.huawei.hidisk.filemanager.FileManager");
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.setComponent(component);
        intent.setFlags(268435456);
        return intent;
    }

    private static Intent searchAppFromWeb(String label) {
        HwPCUtils.log(TAG, "searchAppFromWeb: label = " + label);
        if (label == null) {
            HwPCUtils.log(TAG, "Null input params");
            return null;
        }
        Intent intent = new Intent();
        intent.setAction(APP_CENTER_SEARCH_ACTION);
        intent.addFlags(268468224);
        intent.putExtra(APP_CENTER_SEARCH_KEY_WORD, label);
        intent.setPackage(HIAPP_PACKAGE_NAME);
        return intent;
    }

    private static boolean isApkSupportedInPCMode(Context context, String pkg) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            HwPCUtils.log(TAG, "warning: the package can't be found");
        }
        if (packageInfo == null || packageInfo.applicationInfo == null) {
            return false;
        }
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        ApplicationInfoEx applicationInfoEx = new ApplicationInfoEx(applicationInfo);
        if (DEBUG) {
            HwPCUtils.log(TAG, "isApkSupportedInPCMode applicationInfo: " + applicationInfo);
        }
        if ((applicationInfo.flags & 1) != 0 && (applicationInfoEx.getHwFlags() & 33554432) == 0 && (applicationInfoEx.getHwFlags() & 67108864) == 0) {
            return false;
        }
        return true;
    }

    public void startApp(HwPCVAssistCmdExecutor.VoiceCmd cmd) {
        boolean isSearchedInMarket;
        boolean isForceStartedInPhone;
        Bundle options;
        HwPCUtils.log(TAG, "startApp cmd = " + cmd);
        if (!TextUtils.isEmpty(cmd.pkgName) || !TextUtils.isEmpty(cmd.extra)) {
            HwPCUtils.bdReport(this.mContext, 10058, String.format("{appName:%s, pkgName:%s}", cmd.pkgName, cmd.extra));
            Intent intent = null;
            if (TextUtils.isEmpty(cmd.pkgName) && !TextUtils.isEmpty(cmd.extra)) {
                cmd.pkgName = getPackageName(this.mContext, cmd.extra);
                if (TextUtils.isEmpty(cmd.pkgName)) {
                    intent = searchAppFromWeb(cmd.extra);
                    if (intent == null) {
                        HwPCUtils.log(TAG, "cannot find this app in market");
                        replyResultForStartApp(RESULT_APP_START_FAILED_PARAMS, true, cmd);
                        return;
                    }
                    replyResultForStartApp(RESULT_APP_START_SUCC_SEARCH_IN_MARKET, true, cmd);
                    isSearchedInMarket = true;
                } else {
                    isSearchedInMarket = false;
                }
            } else if (UNICODE_PHONE_FILE_MANAGER.equals(string2Unicode(cmd.extra))) {
                intent = launchIntentForPhoneFileManager();
                isSearchedInMarket = false;
            } else {
                intent = this.mContext.getPackageManager().getLaunchIntentForPackage(cmd.pkgName);
                if (intent == null) {
                    intent = searchAppFromWeb(cmd.extra);
                    if (intent == null) {
                        HwPCUtils.log(TAG, "cannot find this app in market");
                        replyResultForStartApp(RESULT_APP_START_FAILED_PARAMS, true, cmd);
                        return;
                    }
                    replyResultForStartApp(RESULT_APP_START_SUCC_SEARCH_IN_MARKET, true, cmd);
                    isSearchedInMarket = true;
                } else {
                    isSearchedInMarket = false;
                }
            }
            if (intent == null) {
                HwPCUtils.log(TAG, "wrong parameters");
                replyResultForStartApp(RESULT_APP_START_FAILED_PARAMS, true, cmd);
                return;
            }
            boolean isForceStartedOnExternal = false;
            if (TextUtils.isEmpty(cmd.pkgName) || isApkSupportedInPCMode(this.mContext, cmd.pkgName) || this.mService.getPackageSupportPcState(cmd.pkgName) != -1 || cmd.targetDisplay <= 0) {
                isForceStartedInPhone = false;
            } else {
                cmd.targetDisplay = 0;
                HwPCUtils.log(TAG, "startApp package is not supported in external display, ignore displayId.");
                isForceStartedInPhone = true;
            }
            if ("com.huawei.desktop.explorer".equals(cmd.pkgName) && cmd.targetDisplay == 0) {
                cmd.targetDisplay = cmd.castingDisplay;
                isForceStartedOnExternal = true;
            }
            if (cmd.targetDisplay != 0) {
                Bundle options2 = new Bundle();
                options2.putInt("android.activity.launchDisplayId", cmd.targetDisplay);
                options = options2;
            } else {
                options = null;
            }
            try {
                int result = ActivityManagerEx.startActivity(ContextEx.getBasePackageName(this.mContext), intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), (IBinder) null, (String) null, -1, 0, options);
                HwPCUtils.log(TAG, "startApp result = " + result);
                if (!isSearchedInMarket) {
                    if (isForceStartedInPhone) {
                        if (result == 99 || result == 98 || !ActivityManagerEx.isStartResultSuccessful(result)) {
                            replyResultForStartApp(result, false, cmd);
                        } else {
                            replyResultForStartApp(RESULT_APP_START_SUCC_IN_PHONE, true, cmd);
                        }
                    } else if (!isForceStartedOnExternal || !ActivityManagerEx.isStartResultSuccessful(result)) {
                        replyResultForStartApp(result, false, cmd);
                    } else {
                        replyResultForStartApp(RESULT_APP_START_SUCC_ON_EXT_DISPLAY, true, cmd);
                    }
                }
            } catch (RemoteException e) {
                replyResultForStartApp(1, false, cmd);
                HwPCUtils.log(TAG, "startApp RemoteException");
            } catch (Exception e2) {
                replyResultForStartApp(1, false, cmd);
                HwPCUtils.log(TAG, "startApp other Exception");
            }
        } else {
            HwPCUtils.log(TAG, "Category is null");
            replyResultForStartApp(RESULT_APP_START_FAILED_PARAMS, true, cmd);
        }
    }

    private void genJsonReplyForNormalResultSuccess(JSONObject obj) throws JSONException {
        obj.put(KEY_EEOR_CODE, 0);
        HwPCVAssistCmdExecutor hwPCVAssistCmdExecutor = this.mCmdExecutor;
        String text = hwPCVAssistCmdExecutor.getRandomResponseStr(hwPCVAssistCmdExecutor.mAppStartSuccStrs);
        obj.put(KEY_RES_TEXT, text);
        obj.put(KEY_TTS_TEXT, text);
    }

    private void replyResultForStartApp(int errCode, boolean extResult, HwPCVAssistCmdExecutor.VoiceCmd cmd) {
        HwPCUtils.log(TAG, "replyResultForStartApp errCode = " + errCode + ", extResult = " + extResult + ", cmd = " + cmd);
        JSONObject obj = new JSONObject();
        try {
            obj.put("isFinish", "true");
            if (extResult) {
                replyResultTrueForStartApp(obj, errCode, true, cmd);
            } else {
                replyResultFalseForStartApp(obj, errCode, false, cmd);
            }
        } catch (JSONException e) {
            HwPCUtils.log(TAG, "replyResultForStartApp JSONException occurred");
        }
        this.mCmdExecutor.replyResultToVAssist(obj, cmd);
    }

    private void replyResultTrueForStartApp(JSONObject obj, int errCode, boolean extResult, HwPCVAssistCmdExecutor.VoiceCmd cmd) throws JSONException {
        String responseStr;
        switch (errCode) {
            case RESULT_APP_START_SUCC_SEARCH_IN_MARKET /* -43 */:
                obj.put(KEY_EEOR_CODE, 0);
                String responseStr2 = this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_inline_search_market"));
                obj.put(KEY_RES_TEXT, responseStr2);
                obj.put(KEY_TTS_TEXT, responseStr2);
                return;
            case RESULT_APP_START_SUCC_ON_EXT_DISPLAY /* -42 */:
            case RESULT_APP_START_SUCC_IN_PHONE /* -41 */:
                obj.put(KEY_EEOR_CODE, 0);
                if (errCode == RESULT_APP_START_SUCC_IN_PHONE) {
                    responseStr = this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_start_in_phone"));
                } else {
                    responseStr = this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_start_in_screen"));
                }
                String responseStr3 = String.format(responseStr, cmd.extra);
                HwPCUtils.log(TAG, "replyResultForStartApp responseStr = " + responseStr3);
                obj.put(KEY_RES_TEXT, responseStr3);
                obj.put(KEY_TTS_TEXT, responseStr3);
                return;
            default:
                obj.put(KEY_EEOR_CODE, 1);
                obj.put(KEY_RES_TEXT, this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_cannot_do_it")));
                obj.put(KEY_TTS_TEXT, this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_cannot_do_it")));
                return;
        }
    }

    private void replyResultFalseForStartApp(JSONObject obj, int errCode, boolean extResult, HwPCVAssistCmdExecutor.VoiceCmd cmd) throws JSONException {
        String responseStr;
        if (errCode == 0 || errCode == 1 || errCode == 2 || errCode == 3) {
            genJsonReplyForNormalResultSuccess(obj);
        } else if (errCode == 98 || errCode == 99) {
            obj.put(KEY_EEOR_CODE, 0);
            if (errCode == 98) {
                responseStr = this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_close_phone_app_for_start"));
            } else {
                responseStr = this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_close_external_app_for_start"));
            }
            String responseStr2 = String.format(responseStr, cmd.extra);
            obj.put(KEY_RES_TEXT, responseStr2);
            obj.put(KEY_TTS_TEXT, responseStr2);
        } else if (ActivityManagerEx.isStartResultSuccessful(errCode)) {
            genJsonReplyForNormalResultSuccess(obj);
        } else {
            obj.put(KEY_EEOR_CODE, 1);
            obj.put(KEY_RES_TEXT, this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_cannot_do_it")));
            obj.put(KEY_TTS_TEXT, this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_cannot_do_it")));
        }
    }

    public static String getPackageName(Context context, String appName) {
        if (context == null || TextUtils.isEmpty(appName)) {
            HwPCUtils.log(TAG, "getPackageName input params is invalid");
            return null;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            HwPCUtils.log(TAG, "getPackageName PackageManager is invalid");
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        if (!UNICODE_EXTERNAL_FILE_MANAGER.equals(string2Unicode(appName))) {
            intent.addCategory("android.intent.category.LAUNCHER");
        }
        List<ResolveInfo> listAppInfos = pm.queryIntentActivities(intent, 0);
        if (listAppInfos == null) {
            HwPCUtils.log(TAG, "getPackageName :error: listAppcations is null");
            return null;
        }
        String packageNameString = null;
        for (ResolveInfo app : listAppInfos) {
            String appLabel = (String) app.loadLabel(pm);
            if (!TextUtils.isEmpty(appLabel)) {
                if (TextUtils.equals(appLabel.toUpperCase(Locale.ROOT), appName.toUpperCase(Locale.ROOT))) {
                    return app.activityInfo.packageName;
                }
                if (appLabel.toUpperCase(Locale.ROOT).contains(appName.toUpperCase(Locale.ROOT))) {
                    packageNameString = app.activityInfo.packageName;
                }
            }
        }
        listAppInfos.clear();
        return packageNameString;
    }
}
