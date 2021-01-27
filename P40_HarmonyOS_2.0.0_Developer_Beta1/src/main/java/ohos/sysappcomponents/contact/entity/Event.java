package ohos.sysappcomponents.contact.entity;

import ohos.app.Context;
import ohos.sysappcomponents.contact.Attribute;

public class Event extends Label {
    public static final int EVENT_ANNIVERSARY = 1;
    public static final int EVENT_BIRTHDAY = 3;
    public static final int EVENT_OTHER = 2;
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

    public Event(Context context, String str, int i) {
        super(context, str, i);
    }

    public Event(String str, String str2) {
        super(str, str2);
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getEventDate() {
        return getMainData();
    }

    public void setEventDate(String str) {
        setMainData(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public String getLabelNameResId(Context context, int i) {
        return Attribute.CommonDataKinds.Event.getLabelNameResId(context, i);
    }
}
