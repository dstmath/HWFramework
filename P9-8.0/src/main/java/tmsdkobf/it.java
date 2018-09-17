package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

public class it {
    private static final String[] sc = new String[]{"android.intent.action.PHONE_STATE", "android.intent.action.PHONE_STATE_2", "android.intent.action.PHONE_STATE2", "android.intent.action.PHONE_STATE_EXT"};

    public static int a(Context context, Intent intent) {
        String stringExtra = intent.getStringExtra("state");
        if (stringExtra != null) {
            if (stringExtra.equals("IDLE")) {
                return 0;
            }
            if (stringExtra.equals("RINGING")) {
                return 1;
            }
            if (stringExtra.equals("OFFHOOK")) {
                return 2;
            }
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        return telephonyManager == null ? 0 : telephonyManager.getCallState();
    }

    public static void a(Context context, BroadcastReceiver broadcastReceiver) {
        String[] strArr;
        qc qcVar = im.rE;
        if (qcVar != null) {
            String io = qcVar.io();
            strArr = (io == null || io.equalsIgnoreCase("android.intent.action.PHONE_STATE")) ? sc : new String[]{"android.intent.action.PHONE_STATE", io};
        } else {
            strArr = sc;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(Integer.MAX_VALUE);
        intentFilter.addCategory("android.intent.category.DEFAULT");
        String[] strArr2 = strArr;
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            intentFilter.addAction(strArr2[i]);
        }
        context.registerReceiver(broadcastReceiver, intentFilter);
    }
}
