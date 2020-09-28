package com.android.internal.telephony.vsim;

import android.content.Intent;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;

@HwSystemApi
public class HwVSimControllerUtils {
    public static void putNetworkScanOperatorInfoArrayListInIntent(AsyncResultEx ar, Intent intent, String name) {
        intent.putParcelableArrayListExtra(name, (ArrayList) ar.getResult());
    }
}
