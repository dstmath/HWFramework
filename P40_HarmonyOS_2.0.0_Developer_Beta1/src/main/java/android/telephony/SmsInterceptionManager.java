package android.telephony;

import com.android.internal.telephony.ISmsInterception;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.telephony.RlogEx;

public class SmsInterceptionManager {
    private static final String LOG_TAG = "SmsInterceptionManager";
    private static SmsInterceptionManager sInstance = null;

    public static synchronized SmsInterceptionManager getInstance() {
        SmsInterceptionManager smsInterceptionManager;
        synchronized (SmsInterceptionManager.class) {
            if (sInstance == null) {
                sInstance = new SmsInterceptionManager();
            }
            smsInterceptionManager = sInstance;
        }
        return smsInterceptionManager;
    }

    public void registerListener(SmsInterceptionListener listener, int priority) {
        try {
            ISmsInterception iSmsInterception = getISmsInterception();
            if (iSmsInterception != null) {
                iSmsInterception.registerListener(listener.callback, priority);
            }
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "Problem while register listener in registerListener");
        }
    }

    public void unregisterListener(int priority) {
        try {
            ISmsInterception iSmsInterception = getISmsInterception();
            if (iSmsInterception != null) {
                iSmsInterception.unregisterListener(priority);
            }
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "Problem while unregister listener in unregisterListener");
        }
    }

    private ISmsInterception getISmsInterception() {
        return ISmsInterception.Stub.asInterface(ServiceManagerEx.getService("isms_interception"));
    }
}
