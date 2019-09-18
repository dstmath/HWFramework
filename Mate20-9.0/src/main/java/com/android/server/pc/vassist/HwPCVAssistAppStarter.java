package com.android.server.pc.vassist;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.HwPCUtils;
import com.android.server.pc.HwPCManagerService;
import com.android.server.pc.vassist.HwPCVAssistCmdExecutor;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public final class HwPCVAssistAppStarter {
    private static final String APP_CENTER_SEARCH_ACTION = "com.huawei.appmarket.appmarket.intent.action.SearchActivity";
    private static final String APP_CENTER_SEARCH_KEY_WORD = "keyWord";
    private static final String HIAPP_PACKAGE_NAME = "com.huawei.appmarket";
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
        int N = string.length();
        for (int i = 0; i < N; i++) {
            char c = string.charAt(i);
            unicode.append("\\u");
            unicode.append(Integer.toHexString(c));
        }
        return unicode.toString();
    }

    private static Intent launchIntentForPhoneFileManager() {
        ComponentName component = new ComponentName("com.huawei.hidisk", "com.huawei.hidisk.filemanager.FileManager");
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.setComponent(component);
        intent.setFlags(268435456);
        return intent;
    }

    private static Intent launchIntentForDesktopFileManager() {
        ComponentName component = new ComponentName("com.huawei.desktop.explorer", "com.huawei.filemanager.activities.MainActivity");
        Intent intent = new Intent("android.intent.action.VIEW", null);
        intent.setComponent(component);
        intent.setFlags(402653184);
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
            HwPCUtils.log(TAG, "warning: " + e.toString());
        }
        if (packageInfo == null || packageInfo.applicationInfo == null) {
            return false;
        }
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        HwPCUtils.log(TAG, "isApkSupportedInPCMode applicationInfo: " + applicationInfo);
        if ((applicationInfo.flags & 1) != 0 && (applicationInfo.hwFlags & 33554432) == 0 && (applicationInfo.hwFlags & 67108864) == 0) {
            return false;
        }
        return true;
    }

    public void startApp(HwPCVAssistCmdExecutor.VoiceCmd cmd) {
        HwPCVAssistCmdExecutor.VoiceCmd voiceCmd = cmd;
        HwPCUtils.log(TAG, "startApp cmd = " + voiceCmd);
        if (!TextUtils.isEmpty(voiceCmd.pkgName) || !TextUtils.isEmpty(voiceCmd.extra)) {
            HwPCUtils.bdReport(this.mContext, 10058, String.format("{appName:%s, pkgName:%s}", new Object[]{voiceCmd.pkgName, voiceCmd.extra}));
            boolean searchedInMarket = false;
            Intent intent = null;
            if (TextUtils.isEmpty(voiceCmd.pkgName) && !TextUtils.isEmpty(voiceCmd.extra)) {
                voiceCmd.pkgName = getPackageName(this.mContext, voiceCmd.extra);
                if (TextUtils.isEmpty(voiceCmd.pkgName)) {
                    intent = searchAppFromWeb(voiceCmd.extra);
                    if (intent == null) {
                        HwPCUtils.log(TAG, "cannot find this app in market");
                        replyResultForStartApp(42, true, voiceCmd);
                        return;
                    }
                    searchedInMarket = true;
                    replyResultForStartApp(RESULT_APP_START_SUCC_SEARCH_IN_MARKET, true, voiceCmd);
                }
            } else if ("com.huawei.desktop.explorer".equals(voiceCmd.pkgName)) {
                intent = launchIntentForDesktopFileManager();
            } else if (UNICODE_PHONE_FILE_MANAGER.equals(string2Unicode(voiceCmd.extra))) {
                intent = launchIntentForPhoneFileManager();
            } else {
                intent = this.mContext.getPackageManager().getLaunchIntentForPackage(voiceCmd.pkgName);
                if (intent == null) {
                    intent = searchAppFromWeb(voiceCmd.extra);
                    if (intent == null) {
                        HwPCUtils.log(TAG, "cannot find this app in market");
                        replyResultForStartApp(42, true, voiceCmd);
                        return;
                    }
                    searchedInMarket = true;
                    replyResultForStartApp(RESULT_APP_START_SUCC_SEARCH_IN_MARKET, true, voiceCmd);
                }
            }
            boolean searchedInMarket2 = searchedInMarket;
            if (intent == null) {
                HwPCUtils.log(TAG, "wrong parameters");
                replyResultForStartApp(42, true, voiceCmd);
                return;
            }
            boolean isForceStartedInPhone = false;
            boolean isForceStartedOnExternal = false;
            if (!TextUtils.isEmpty(voiceCmd.pkgName) && !isApkSupportedInPCMode(this.mContext, voiceCmd.pkgName) && this.mService.getPackageSupportPcState(voiceCmd.pkgName) == -1 && voiceCmd.targetDisplay > 0) {
                voiceCmd.targetDisplay = 0;
                HwPCUtils.log(TAG, "startApp package is not supported in external display, ignore displayId.");
                isForceStartedInPhone = true;
            }
            boolean isForceStartedInPhone2 = isForceStartedInPhone;
            if ("com.huawei.desktop.explorer".equals(voiceCmd.pkgName) && voiceCmd.targetDisplay == 0) {
                voiceCmd.targetDisplay = voiceCmd.castingDisplay;
                isForceStartedOnExternal = true;
            }
            Bundle options = null;
            if (voiceCmd.targetDisplay != 0) {
                options = new Bundle();
                options.putInt("android.activity.launchDisplayId", voiceCmd.targetDisplay);
            }
            try {
                int result = ActivityManager.getService().startActivity(ActivityThread.currentActivityThread().getApplicationThread(), this.mContext.getBasePackageName(), intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), null, null, -1, 0, null, options);
                HwPCUtils.log(TAG, "startApp result = " + result);
                if (!searchedInMarket2) {
                    if (isForceStartedInPhone2) {
                        if (result == 99 || result == 98 || !ActivityManager.isStartResultSuccessful(result)) {
                            replyResultForStartApp(result, false, voiceCmd);
                        } else {
                            replyResultForStartApp(RESULT_APP_START_SUCC_IN_PHONE, true, voiceCmd);
                        }
                    } else if (!isForceStartedOnExternal || !ActivityManager.isStartResultSuccessful(result)) {
                        replyResultForStartApp(result, false, voiceCmd);
                    } else {
                        replyResultForStartApp(RESULT_APP_START_SUCC_ON_EXT_DISPLAY, true, voiceCmd);
                    }
                }
            } catch (RemoteException e) {
                replyResultForStartApp(1, false, voiceCmd);
                HwPCUtils.log(TAG, "startApp RemoteException");
            } catch (Exception e2) {
                replyResultForStartApp(1, false, voiceCmd);
                HwPCUtils.log(TAG, "startApp other Exception");
            }
        } else {
            HwPCUtils.log(TAG, "Category is null");
            replyResultForStartApp(42, true, voiceCmd);
        }
    }

    private void genJsonReplyForNormalResultSuccess(JSONObject obj) throws JSONException {
        obj.put("errorCode", 0);
        String text = this.mCmdExecutor.getRandomResponseStr(this.mCmdExecutor.mAppStartSuccStrs);
        obj.put("responseText", text);
        obj.put("ttsText", text);
    }

    private void replyResultForStartApp(int errCode, boolean extResult, HwPCVAssistCmdExecutor.VoiceCmd cmd) {
        String responseStr;
        String responseStr2;
        HwPCUtils.log(TAG, "replyResultForStartApp errCode = " + errCode + ", extResult = " + extResult + ", cmd = " + cmd);
        JSONObject obj = new JSONObject();
        try {
            obj.put("isFinish", "true");
            if (!extResult) {
                switch (errCode) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        genJsonReplyForNormalResultSuccess(obj);
                        break;
                    default:
                        switch (errCode) {
                            case 98:
                            case 99:
                                obj.put("errorCode", 0);
                                if (errCode == 98) {
                                    responseStr = this.mContext.getString(33686182);
                                } else {
                                    responseStr = this.mContext.getString(33686180);
                                }
                                String responseStr3 = String.format(responseStr, new Object[]{cmd.extra});
                                obj.put("responseText", responseStr3);
                                obj.put("ttsText", responseStr3);
                                break;
                            default:
                                if (!ActivityManager.isStartResultSuccessful(errCode)) {
                                    obj.put("errorCode", 1);
                                    obj.put("responseText", this.mContext.getString(33686177));
                                    obj.put("ttsText", this.mContext.getString(33686177));
                                    break;
                                } else {
                                    genJsonReplyForNormalResultSuccess(obj);
                                    break;
                                }
                        }
                }
            } else {
                switch (errCode) {
                    case RESULT_APP_START_SUCC_SEARCH_IN_MARKET /*-43*/:
                        obj.put("errorCode", 0);
                        String responseStr4 = this.mContext.getString(33686187);
                        obj.put("responseText", responseStr4);
                        obj.put("ttsText", responseStr4);
                        break;
                    case RESULT_APP_START_SUCC_ON_EXT_DISPLAY /*-42*/:
                    case RESULT_APP_START_SUCC_IN_PHONE /*-41*/:
                        obj.put("errorCode", 0);
                        if (errCode == RESULT_APP_START_SUCC_IN_PHONE) {
                            responseStr2 = this.mContext.getString(33686201);
                        } else {
                            responseStr2 = this.mContext.getString(33686202);
                        }
                        String responseStr5 = String.format(responseStr2, new Object[]{cmd.extra});
                        HwPCUtils.log(TAG, "replyResultForStartApp responseStr = " + responseStr5);
                        obj.put("responseText", responseStr5);
                        obj.put("ttsText", responseStr5);
                        break;
                    default:
                        obj.put("errorCode", 1);
                        obj.put("responseText", this.mContext.getString(33686177));
                        obj.put("ttsText", this.mContext.getString(33686177));
                        break;
                }
            }
        } catch (JSONException e) {
            HwPCUtils.log(TAG, "replyResultForStartApp JSONException occurred");
        }
        this.mCmdExecutor.replyResultToVAssist(obj, cmd);
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
        List<ResolveInfo> listAppInfo = pm.queryIntentActivities(intent, 0);
        if (listAppInfo == null) {
            HwPCUtils.log(TAG, "getPackageName :error: listAppcations is null");
            return null;
        }
        String packageNameString = null;
        for (ResolveInfo app : listAppInfo) {
            String appLabel = (String) app.loadLabel(pm);
            if (!TextUtils.isEmpty(appLabel)) {
                if (TextUtils.equals(appLabel.toUpperCase(Locale.US), appName.toUpperCase(Locale.US))) {
                    String packageNameString2 = app.activityInfo.packageName;
                    HwPCUtils.log(TAG, "getPackageName packageName = " + packageNameString2 + ", appLabel = " + appLabel);
                    return packageNameString2;
                } else if (appLabel.toUpperCase(Locale.US).contains(appName.toUpperCase(Locale.US))) {
                    packageNameString = app.activityInfo.packageName;
                    HwPCUtils.log(TAG, "getPackageName packageName = " + packageNameString + ", appLabel = " + appLabel);
                }
            }
        }
        listAppInfo.clear();
        return packageNameString;
    }
}
