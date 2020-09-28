package com.huawei.systemmanager.mms;

import android.content.Context;
import android.net.Uri;
import java.io.InputStream;
import java.util.HashMap;

public class HwMmsInfoHelper {
    public static HwMmsInfo getHwMmsInfoFromPdu(byte[] pduData) {
        return HwMmsInfoHelperInner.getHwMmsInfoFromPdu(pduData);
    }

    public static Uri persistPduData(Context context, byte[] pduData, Uri uri, boolean createThreadId, boolean groupMmsEnabled, HashMap<Uri, InputStream> preOpenedFiles) {
        return HwMmsInfoHelperInner.persistPduData(context, pduData, uri, createThreadId, groupMmsEnabled, preOpenedFiles);
    }
}
