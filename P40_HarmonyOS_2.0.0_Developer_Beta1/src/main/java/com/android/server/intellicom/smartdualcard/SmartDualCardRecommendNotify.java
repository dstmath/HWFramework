package com.android.server.intellicom.smartdualcard;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.server.intellicom.common.HwSettingsObserver;
import com.android.server.intellicom.common.NetLinkManager;
import com.android.server.intellicom.common.SmartDualCardUtil;
import java.util.Locale;

public class SmartDualCardRecommendNotify extends Handler {
    public static final int EVENT_SHOW_POP_WINDOW = 0;
    private static final String SETTINGS_PRIORITY_APP_NO_LONGER_PROMPT = "all_priority_app_no_longer_prompt";
    private static final String TAG = "SmartDualCardRecommend";
    private static final int TELEPHONY_POP_WINDOW_BIND_UID_TO_SLAVE_CARD = 705;
    private static PackageManager packageManager = null;
    private static boolean sCanShowPopWindow = true;
    private static Context sContext = null;
    private HwSettingsObserver mHwSettingsObserver = null;

    public static SmartDualCardRecommendNotify getInstance() {
        return SingletonInstance.INSTANCE;
    }

    /* access modifiers changed from: private */
    public static class SingletonInstance {
        private static final SmartDualCardRecommendNotify INSTANCE = new SmartDualCardRecommendNotify();

        private SingletonInstance() {
        }
    }

    public void init(Context context) {
        sContext = context;
        packageManager = context.getPackageManager();
        this.mHwSettingsObserver = HwSettingsObserver.getInstance();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void goToIntelligentSwitchActivity() {
        Intent intent = new Intent();
        intent.setFlags(268435456);
        intent.setComponent(new ComponentName("com.huawei.dsdscardmanager", "com.huawei.dsdscardmanager.IntelligentSwitchActivity"));
        intent.addFlags(8388608);
        try {
            sContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "goToIntelligentSwitchActivity, ActivityNotFoundException");
        }
    }

    private String getSmartCardSwitchFirstPromtString(int uid, int lengthOfNoTrafficAppList) {
        String appName = getAppNameByUid(uid);
        if (TextUtils.isEmpty(appName)) {
            log("can not get appName");
            return "";
        }
        int slaveSlotId = SmartDualCardUtil.getSlaveCardSlotId() + 1;
        if (lengthOfNoTrafficAppList == 1) {
            return String.format(Locale.ROOT, sContext.getResources().getString(33686255), appName, Integer.valueOf(slaveSlotId), Integer.valueOf(slaveSlotId));
        }
        return String.format(Locale.ROOT, sContext.getResources().getQuantityString(34406413, lengthOfNoTrafficAppList - 1), appName, Integer.valueOf(lengthOfNoTrafficAppList - 1), Integer.valueOf(slaveSlotId), Integer.valueOf(slaveSlotId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkUserChoice(boolean isAllAppNoLongerNotify, int uid) {
        TelephonyManager telephonyManager = TelephonyManager.from(sContext);
        if (telephonyManager == null) {
            Log.e(TAG, "checkUserChoice: telephonyManager is null");
            return;
        }
        this.mHwSettingsObserver.setNotNotifyFlagForOnePriorityApp(HwTelephonyManager.getDefault().getSimSerialNumber(telephonyManager, SmartDualCardUtil.getSlaveCardSlotId()), uid);
        if (isAllAppNoLongerNotify) {
            Settings.Global.putInt(sContext.getContentResolver(), SETTINGS_PRIORITY_APP_NO_LONGER_PROMPT, 1);
        }
    }

    private void showAlertDialog(final int uid, int appListSize) {
        if (!sCanShowPopWindow || appListSize <= 0) {
            log("sCanShowPopWindow=" + sCanShowPopWindow + " appListSize=" + appListSize);
            return;
        }
        String toastString = getSmartCardSwitchFirstPromtString(uid, appListSize);
        if (TextUtils.isEmpty(toastString)) {
            log("tosastString is empty");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(sContext, 33947691);
        View view = LayoutInflater.from(builder.getContext()).inflate(34013438, (ViewGroup) null);
        final CheckBox checkBox = (CheckBox) view.findViewById(34603060);
        ((TextView) view.findViewById(34603509)).setText(toastString);
        builder.setView(view);
        builder.setPositiveButton(33685733, new DialogInterface.OnClickListener() {
            /* class com.android.server.intellicom.smartdualcard.SmartDualCardRecommendNotify.AnonymousClass1 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialoginterface, int i) {
                SmartDualCardRecommendNotify.this.checkUserChoice(checkBox.isChecked(), uid);
                SmartDualCardRecommendNotify.this.sendReportToBooster(SmartDualCardRecommendNotify.TELEPHONY_POP_WINDOW_BIND_UID_TO_SLAVE_CARD);
                boolean unused = SmartDualCardRecommendNotify.sCanShowPopWindow = true;
            }
        });
        builder.setNegativeButton(33686254, new DialogInterface.OnClickListener() {
            /* class com.android.server.intellicom.smartdualcard.SmartDualCardRecommendNotify.AnonymousClass2 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                SmartDualCardRecommendNotify.this.goToIntelligentSwitchActivity();
                boolean unused = SmartDualCardRecommendNotify.sCanShowPopWindow = true;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.setCancelable(false);
        dialog.show();
        sCanShowPopWindow = false;
    }

    private String getAppNameByUid(int uid) {
        try {
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageManager.getNameForUid(uid), 128));
        } catch (PackageManager.NameNotFoundException e) {
            log("NameNotFoundException");
            return "";
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg == null) {
            log("handle msg is null");
        } else if (msg.what != 0) {
            log("handleMessage, can not deal this type, msg: " + msg);
        } else {
            showAlertDialog(msg.arg1, msg.arg2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendReportToBooster(int reportType) {
        log("sendReportToBooster, reportType = " + reportType);
        IHwCommBoosterServiceManager boosterServiceManager = NetLinkManager.getInstance().getBoosterServiceManager();
        if (boosterServiceManager == null) {
            log("boosterServiceManager is null");
            return;
        }
        Bundle data = new Bundle();
        data.putInt("continue", 1);
        int ret = boosterServiceManager.reportBoosterPara("com.android.server.intellicom.common", reportType, data);
        if (ret != 0) {
            log("reportBoosterPara failed, ret=" + ret);
        }
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }
}
