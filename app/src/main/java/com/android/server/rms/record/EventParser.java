package com.android.server.rms.record;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.android.server.rms.utils.Utils;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.util.ArrayList;

public class EventParser {
    private static final String TAG = "RMS.ParserEvent";

    public static ArrayList<ResourceOverloadRecord> parser(Context context, String event) {
        int i = 0;
        ArrayList<ResourceOverloadRecord> recordList = new ArrayList();
        if (event != null && event.length() > 0) {
            int pos = event.indexOf(58);
            String eventName = event.substring(0, pos);
            String eventData = event.substring(pos + 1);
            int resourceType = ResourceUtils.getResourcesType(eventName);
            PackageManager pm = context.getPackageManager();
            if (Utils.DEBUG || Utils.HWFLOW) {
                Log.w(TAG, "parser eventName:" + eventName + " eventData:" + eventData);
            }
            switch (resourceType) {
                case HwSecDiagnoseConstant.BIT_SU /*16*/:
                    String[] pidsEvent = eventData.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    int length = pidsEvent.length;
                    while (i < length) {
                        recordList.add(composePidsRecord(pm, splitInt(pidsEvent[i])));
                        i++;
                    }
                    break;
            }
        }
        return recordList;
    }

    private static int[] splitInt(String str) {
        if (str == null) {
            return null;
        }
        String[] data = str.split(" ");
        int[] output = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            output[i] = Integer.parseInt(data[i]);
        }
        return output;
    }

    private static ResourceOverloadRecord composePidsRecord(PackageManager pm, int[] data) {
        if (data == null) {
            return null;
        }
        int pid = data[1];
        int uid = data[2];
        int overloadNum = data[3];
        ResourceOverloadRecord record = new ResourceOverloadRecord();
        record.setUid(uid);
        record.setPid(pid);
        record.setResourceType(16);
        record.setCountOverLoadNum(overloadNum);
        if (pm != null) {
            record.setPackageName(pm.getNameForUid(uid));
        }
        return record;
    }
}
