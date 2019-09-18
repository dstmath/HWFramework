package com.huawei.wallet.sdk.common.apdu.manager;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.apdu.BaseCommonContext;
import com.huawei.wallet.sdk.common.apdu.constants.Constants;
import com.huawei.wallet.sdk.common.storage.NFCPreferences;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;

public class NFCAesManager extends BaseAesManager {
    private static final String CHAOS_UUID_1 = "chaos_uuid_1";
    private static final String SECTION_1 = "5c3abf";
    private static final String SECTION_4 = "583d4e";

    private static class Singletone {
        public static final NFCAesManager INSTANCE = new NFCAesManager();

        private Singletone() {
        }
    }

    private NFCAesManager() {
    }

    public static NFCAesManager getInstance() {
        return Singletone.INSTANCE;
    }

    /* access modifiers changed from: protected */
    public String getPersistentAesKey() {
        Context context = BaseCommonContext.getInstance().getApplicationContext();
        String str = NFCPreferences.getInstance(context).getString(CHAOS_UUID_1, null);
        StringBuilder key = new StringBuilder();
        if (TextUtils.isEmpty(str)) {
            str = recycleLeftMoveBit(PhoneDeviceUtil.getNumUUID(8), 3);
            NFCPreferences.getInstance(context).putString(CHAOS_UUID_1, str);
        }
        key.append(recycleLeftMoveBit(str, 5));
        key.append(recycleLeftMoveBit(SECTION_1, 7));
        key.append(recycleLeftMoveBit(Constants.SECTION_2, 6));
        key.append(recycleLeftMoveBit("6A5B3F", 5));
        key.append(recycleLeftMoveBit(SECTION_4, 4));
        return key.toString();
    }
}
