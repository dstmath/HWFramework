package android.telephony;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class HwCustTelephonyManagerImpl extends HwCustTelephonyManager {
    private static final String APP_POWER_GENIE = "com.huawei.powergenie";
    private static final int INDEX_INVALID = -1;
    private static final String TAG = "HwCustTelephonyManagerImpl";
    private static final int VZW_CARD_LEN_LIMIT = 10;
    private static final String VZW_MCCMNC = "311810;311480";
    private static final HashSet<String> WHITE_LIST = new HashSet<String>() {
        /* class android.telephony.HwCustTelephonyManagerImpl.AnonymousClass1 */

        {
            add(HwCustTelephonyManagerImpl.APP_POWER_GENIE);
        }
    };
    private AlertDialog mConfirmDialog;
    private Context mContext;
    private boolean mIsOkClicked;

    public HwCustTelephonyManagerImpl() {
    }

    public HwCustTelephonyManagerImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean isVZW() {
        return HwCustUtil.isVZW;
    }

    public String getVZWLine1Number(int subId, String number, String mccmnc) {
        String vzwNumber = number;
        if (!isVZWValidNumber(vzwNumber)) {
            vzwNumber = handleIMPUToNumber(HwTelephonyManagerInner.getDefault().getImsImpu(subId), mccmnc);
        }
        if (isVZWValidNumber(vzwNumber)) {
            return handleVZWNumber(vzwNumber, mccmnc);
        }
        return null;
    }

    public boolean isVZWValidNumber(String number) {
        String vzwNumber = number;
        if (TextUtils.isEmpty(vzwNumber)) {
            return false;
        }
        if (vzwNumber.startsWith("+")) {
            vzwNumber = number.substring(1);
        }
        int length = vzwNumber.length();
        for (int pos = 0; pos < length; pos++) {
            if (!Character.isDigit(vzwNumber.charAt(pos))) {
                return false;
            }
        }
        for (int pos2 = 0; pos2 < length; pos2++) {
            if ('0' != vzwNumber.charAt(pos2)) {
                return true;
            }
        }
        return false;
    }

    private String handleIMPUToNumber(String IMPU, String mccmnc) {
        Log.d(TAG, "handleIMPUToNumber");
        if (IMPU == null) {
            return null;
        }
        String number = IMPU.split("@")[0];
        int index = number.indexOf(":");
        if (index > INDEX_INVALID) {
            number = number.substring(index + 1);
        }
        if (number.length() == 0 || !number.startsWith(mccmnc)) {
            return number;
        }
        return null;
    }

    private String handleVZWNumber(String number, String mccmnc) {
        if (number == null || number.length() == 0) {
            return null;
        }
        int length = number.length();
        if (length <= VZW_CARD_LEN_LIMIT || !isVZWCard(mccmnc)) {
            return number;
        }
        return number.substring(length - 10, length);
    }

    private boolean isVZWCard(String mccmnc) {
        if (mccmnc == null || mccmnc.length() == 0 || !Arrays.asList(VZW_MCCMNC.trim().split(";")).contains(mccmnc)) {
            return false;
        }
        return true;
    }

    public void setDataEnabledVZW(Context context, int subId, boolean isToEnabled) {
        if (context != null) {
            TelephonyManager teleManager = (TelephonyManager) context.getSystemService("phone");
            String callingApp = getCallingAppName(context);
            if (isToEnabled || WHITE_LIST.contains(callingApp)) {
                Log.d(TAG, "setDataEnabledVZW: calling app is:  " + callingApp + ", setDataEnabled :  " + isToEnabled + ", without prompt.");
                TelephonyManagerEx.setDataEnabled(teleManager, subId, isToEnabled);
                return;
            }
            Log.d(TAG, "setDataEnabledVZW: calling app is:  " + callingApp + ", setDataEnabled :  " + isToEnabled + ", with prompt.");
            Settings.Global.putInt(context.getContentResolver(), "mobile_data", 0);
            this.mIsOkClicked = false;
            AlertDialog.Builder builder = obtainDialogBuilder(context, subId, isToEnabled, teleManager);
            if (this.mConfirmDialog == null) {
                this.mConfirmDialog = builder.create();
                this.mConfirmDialog.getWindow().setType(2009);
                if (!this.mConfirmDialog.isShowing()) {
                    this.mConfirmDialog.show();
                }
            }
        }
    }

    private AlertDialog.Builder obtainDialogBuilder(Context context, final int subId, final boolean isToEnabled, final TelephonyManager teleManager) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, HwPartResourceUtils.getResourceId("Theme_Emui_Dialog_Alert"));
        builder.setTitle(HwPartResourceUtils.getResourceId("data_enable_confirm_title"));
        builder.setMessage(HwPartResourceUtils.getResourceId("data_enable_confirm_msg"));
        if (HwCustUtil.isVoLteOn && !HwCustUtil.isVoWiFi) {
            builder.setMessage(HwPartResourceUtils.getResourceId("data_enable_confirm_msg_vowifi"));
        }
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            /* class android.telephony.HwCustTelephonyManagerImpl.AnonymousClass2 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                TelephonyManagerEx.setDataEnabled(teleManager, subId, isToEnabled);
                if (dialog != null) {
                    dialog.dismiss();
                }
                HwCustTelephonyManagerImpl.this.mConfirmDialog = null;
                HwCustTelephonyManagerImpl.this.mIsOkClicked = true;
                Log.d(HwCustTelephonyManagerImpl.TAG, "setDataEnabledVZW: Confirm to turn off data --> OK");
            }
        });
        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            /* class android.telephony.HwCustTelephonyManagerImpl.AnonymousClass3 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                HwCustTelephonyManagerImpl.this.mIsOkClicked = false;
                HwCustTelephonyManagerImpl.this.onDialogDismiss();
                Log.d(HwCustTelephonyManagerImpl.TAG, "setDataEnabledVZW: Confirm to turn off data --> Cancel");
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class android.telephony.HwCustTelephonyManagerImpl.AnonymousClass4 */

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialog) {
                HwCustTelephonyManagerImpl.this.onDialogDismiss();
            }
        });
        return builder;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDialogDismiss() {
        Settings.Global.putInt(this.mContext.getContentResolver(), "mobile_data", !this.mIsOkClicked ? 1 : 0);
        this.mConfirmDialog = null;
        Log.d(TAG, "setDataEnabledVZW: Turn off data:" + this.mIsOkClicked);
    }

    private String getCallingAppName(Context context) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList;
        String appName = "";
        if (context == null) {
            return appName;
        }
        int callingPid = Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        if (am == null || (appProcessList = am.getRunningAppProcesses()) == null) {
            return appName;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == callingPid) {
                appName = appProcess.processName;
            }
        }
        return appName;
    }
}
