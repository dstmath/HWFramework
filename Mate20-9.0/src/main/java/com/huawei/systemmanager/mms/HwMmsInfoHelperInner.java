package com.huawei.systemmanager.mms;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
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
        HwMmsInfo hwMmsInfo = null;
        if (pduData == null) {
            return null;
        }
        GenericPdu genericPdu = new PduParser(pduData, false).parse();
        if (genericPdu == null) {
            return null;
        }
        int messageType = genericPdu.getMessageType();
        if (messageType != 130) {
            HwMmsInfo hwMmsInfo2 = new HwMmsInfo("", "", -1, -1, messageType);
            return hwMmsInfo2;
        }
        NotificationInd nInd = (NotificationInd) genericPdu;
        try {
            String phone = nInd.getFrom().getString();
            String body = null;
            if (nInd.getSubject() != null) {
                body = nInd.getSubject().getString();
            }
            HwMmsInfo hwMmsInfo3 = new HwMmsInfo(phone, body, nInd.getMessageSize(), nInd.getExpiry(), messageType);
            hwMmsInfo = hwMmsInfo3;
        } catch (Exception e) {
            Log.e(TAG, "wrap HwMmsInfo failed in getHwMmsInfoFromPdu");
        }
        return hwMmsInfo;
    }

    static Uri persistPduData(Context context, byte[] pduData, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles) {
        Uri uriRet = null;
        if (pduData == null || pduData.length <= 0) {
            return null;
        }
        GenericPdu genericPdu = new PduParser(pduData, false).parse();
        if (genericPdu == null) {
            return null;
        }
        try {
            uriRet = PduPersister.getPduPersister(context).persist(genericPdu, uri, createThreadId, groupMmsEnabled, preOpenedFiles);
        } catch (Exception e) {
            Log.e(TAG, "persist pduData failed");
        }
        return uriRet;
    }
}
