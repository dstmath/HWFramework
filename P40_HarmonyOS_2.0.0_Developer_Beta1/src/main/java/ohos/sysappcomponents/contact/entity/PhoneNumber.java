package ohos.sysappcomponents.contact.entity;

import ohos.app.Context;
import ohos.sysappcomponents.contact.Attribute;

public class PhoneNumber extends Label {
    public static final int NUM_ASSISTANT = 19;
    public static final int NUM_CALLBACK = 8;
    public static final int NUM_CAR = 9;
    public static final int NUM_COMPANY_MAIN = 10;
    public static final int NUM_FAX_HOME = 5;
    public static final int NUM_FAX_WORK = 4;
    public static final int NUM_HOME = 1;
    public static final int NUM_ISDN = 11;
    public static final int NUM_MAIN = 12;
    public static final int NUM_MMS = 20;
    public static final int NUM_MOBILE = 2;
    public static final int NUM_OTHER = 7;
    public static final int NUM_OTHER_FAX = 13;
    public static final int NUM_PAGER = 6;
    public static final int NUM_RADIO = 14;
    public static final int NUM_TELEX = 15;
    public static final int NUM_TTY_TDD = 16;
    public static final int NUM_WORK = 3;
    public static final int NUM_WORK_MOBILE = 17;
    public static final int NUM_WORK_PAGER = 18;
    private int id;

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public int getDefaultLabelId() {
        return 1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public boolean isValidLabel(int i) {
        return i >= 0 && i <= 20;
    }

    public PhoneNumber(Context context, String str, int i) {
        super(context, str, i);
    }

    public PhoneNumber(String str, String str2) {
        super(str, str2);
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public void setPhoneNumber(String str) {
        setMainData(str);
    }

    public String getPhoneNumber() {
        return getMainData();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public String getLabelNameResId(Context context, int i) {
        return Attribute.CommonDataKinds.PhoneNumber.getLabelNameResId(context, i);
    }
}
