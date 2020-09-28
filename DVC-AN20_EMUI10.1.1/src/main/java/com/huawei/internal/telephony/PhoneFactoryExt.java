package com.huawei.internal.telephony;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class PhoneFactoryExt {
    public static final int MAX_ACTIVE_PHONES = PhoneFactory.MAX_ACTIVE_PHONES;

    public static PhoneExt getPhone(int phoneId) {
        Phone phone = PhoneFactory.getPhone(phoneId);
        if (phone == null) {
            return null;
        }
        PhoneExt phoneExt = new PhoneExt();
        phoneExt.setPhone(phone);
        return phoneExt;
    }

    public static PhoneExt getDefaultPhone() {
        Phone phone = PhoneFactory.getDefaultPhone();
        if (phone == null) {
            return null;
        }
        PhoneExt phoneExt = new PhoneExt();
        phoneExt.setPhone(phone);
        return phoneExt;
    }

    public static PhoneExt[] getPhones() {
        Phone[] phones = PhoneFactory.getPhones();
        PhoneExt[] phoneExts = new PhoneExt[phones.length];
        for (int i = 0; i < phones.length; i++) {
            phoneExts[i] = new PhoneExt();
            phoneExts[i].setPhone(phones[i]);
        }
        return phoneExts;
    }

    public static int onDataSubChange() {
        return PhoneFactory.onDataSubChange();
    }

    public static void resendDataAllowed(int phoneId) {
        PhoneFactory.resendDataAllowed(phoneId);
    }

    public static void cleanIccids() {
        if (PhoneFactory.getSubInfoRecordUpdater() != null) {
            PhoneFactory.getSubInfoRecordUpdater().cleanIccids();
        }
    }
}
