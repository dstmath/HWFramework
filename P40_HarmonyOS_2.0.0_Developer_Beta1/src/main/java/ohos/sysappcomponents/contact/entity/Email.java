package ohos.sysappcomponents.contact.entity;

import ohos.app.Context;
import ohos.sysappcomponents.contact.Attribute;

public class Email extends Label {
    public static final int EMAIL_HOME = 1;
    public static final int EMAIL_MOBILE = 4;
    public static final int EMAIL_OTHER = 3;
    public static final int EMAIL_WORK = 2;
    private String displayName;
    private int id;

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public int getDefaultLabelId() {
        return 1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public boolean isValidLabel(int i) {
        return i >= 0 && i <= 4;
    }

    public Email(Context context, String str, int i) {
        super(context, str, i);
    }

    public Email(String str, String str2) {
        super(str, str2);
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getEmail() {
        return getMainData();
    }

    public void setEmail(String str) {
        setMainData(str);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String str) {
        this.displayName = str;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public String getLabelNameResId(Context context, int i) {
        return Attribute.CommonDataKinds.Email.getLabelNameResId(context, i);
    }
}
