package ohos.sysappcomponents.contact.entity;

import ohos.app.Context;
import ohos.sysappcomponents.contact.Attribute;

public class SipAddress extends Label {
    public static final int SIP_HOME = 1;
    public static final int SIP_OTHER = 3;
    public static final int SIP_WORK = 2;
    private int id;

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public int getDefaultLabelId() {
        return 1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public boolean isValidLabel(int i) {
        return i >= 0 && i <= 3;
    }

    public SipAddress(Context context, String str, int i) {
        super(context, str, i);
    }

    public SipAddress(String str, String str2) {
        super(str, 0, str2);
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getSipAddress() {
        return getMainData();
    }

    public void setSipAddress(String str) {
        setMainData(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public String getLabelNameResId(Context context, int i) {
        return Attribute.CommonDataKinds.SipAddress.getLabelNameResId(context, i);
    }
}
