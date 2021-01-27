package ohos.sysappcomponents.contact.entity;

import ohos.app.Context;
import ohos.sysappcomponents.contact.Attribute;

public class ImAddress extends Label {
    public static final int CUSTOM_LABEL = -1;
    public static final int IM_AIM = 0;
    public static final int IM_ICQ = 6;
    public static final int IM_JABBER = 7;
    public static final int IM_MSN = 1;
    public static final int IM_QQ = 4;
    public static final int IM_SKYPE = 3;
    public static final int IM_YAHOO = 2;
    public static final int INVALID_LABEL_ID = -2;
    private int id;

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public int getDefaultLabelId() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public boolean isValidLabel(int i) {
        return (i >= -1 && i <= 4) || (i >= 6 && i <= 7);
    }

    public ImAddress(Context context, String str, int i) {
        super(context, str, i);
    }

    public ImAddress(String str, String str2) {
        super(str, -1, str2);
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getImAddress() {
        return getMainData();
    }

    public void setImAddress(String str) {
        setMainData(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public String getLabelNameResId(Context context, int i) {
        return Attribute.CommonDataKinds.ImAddress.getLabelNameResId(context, i);
    }
}
