package com.huawei.systemmanager.mms;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Slog;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import java.io.InputStream;
import java.util.HashMap;

class HwMmsInfoHelperInner {
    private static final String TAG = "HwMmsInfoHelperInner";

    HwMmsInfoHelperInner() {
    }

    static HwMmsInfo getHwMmsInfoFromPdu(byte[] pduData) {
        GenericPdu genericPdu;
        String body;
        if (pduData == null || (genericPdu = new PduParser(pduData, false).parse()) == null) {
            return null;
        }
        int messageType = genericPdu.getMessageType();
        if (messageType != 130) {
            return new HwMmsInfo("", "", -1, -1, messageType);
        }
        if (!(genericPdu instanceof NotificationInd)) {
            return null;
        }
        NotificationInd nInd = (NotificationInd) genericPdu;
        try {
            String phone = nInd.getFrom().getString();
            if (nInd.getSubject() != null) {
                body = nInd.getSubject().getString();
            } else {
                body = null;
            }
            return new HwMmsInfo(phone, body, nInd.getMessageSize(), nInd.getExpiry(), messageType);
        } catch (Exception e) {
            Log.e(TAG, "wrap HwMmsInfo failed in getHwMmsInfoFromPdu");
            return null;
        }
    }

    static Uri persistPduData(Context context, byte[] pduData, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles) {
        GenericPdu genericPdu;
        if (pduData == null || pduData.length <= 0 || (genericPdu = new PduParser(pduData, false).parse()) == null) {
            return null;
        }
        try {
            return PduPersister.getPduPersister(context).persist(genericPdu, uri, createThreadId, groupMmsEnabled, preOpenedFiles);
        } catch (MmsException e) {
            Slog.e(TAG, "persist pduData failed");
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "persist pduData failed");
            return null;
        }
    }
}
