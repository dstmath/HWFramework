package com.huawei.internal.telephony.uicc;

import android.os.Message;
import android.util.Log;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;

public class UiccControllerEx {
    public static synchronized UiccController getInstance() {
        UiccController instance;
        synchronized (UiccControllerEx.class) {
            instance = UiccController.getInstance();
        }
        return instance;
    }

    public static synchronized UiccCard getUiccCard(UiccController obj) {
        synchronized (UiccControllerEx.class) {
            if (obj == null) {
                return null;
            }
            return obj.getUiccCard(0);
        }
    }

    public static synchronized UiccCard getUiccCard(UiccController obj, int slotId) {
        synchronized (UiccControllerEx.class) {
            if (obj == null) {
                return null;
            }
            return obj.getUiccCard(slotId);
        }
    }

    public static void getUiccCardStatus(Message result, int slotId) {
        try {
            getInstance().getUiccCardStatus(result, slotId);
        } catch (Exception e) {
            Log.e("UiccControllerEx", "UiccControllerEx is fail");
        }
    }
}
