package ohos.sysappcomponents.contact.entity;

import ohos.app.Context;
import ohos.sysappcomponents.contact.Attribute;

public class Relation extends Label {
    public static final int RELATION_ASSISTANT = 1;
    public static final int RELATION_BROTHER = 2;
    public static final int RELATION_CHILD = 3;
    public static final int RELATION_DOMESTIC_PARTNER = 4;
    public static final int RELATION_FATHER = 5;
    public static final int RELATION_FRIEND = 6;
    public static final int RELATION_MANAGER = 7;
    public static final int RELATION_MOTHER = 8;
    public static final int RELATION_PARENT = 9;
    public static final int RELATION_PARTNER = 10;
    public static final int RELATION_REFERRED_BY = 11;
    public static final int RELATION_RELATIVE = 12;
    public static final int RELATION_SISTER = 13;
    public static final int RELATION_SPOUSE = 14;
    private int id;

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public int getDefaultLabelId() {
        return 1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public boolean isValidLabel(int i) {
        return i >= 0 && i <= 14;
    }

    public Relation(Context context, String str, int i) {
        super(context, str, i);
    }

    public Relation(String str, String str2) {
        super(str, 0, str2);
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public String getRelationName() {
        return getMainData();
    }

    public void setRelationName(String str) {
        setMainData(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.sysappcomponents.contact.entity.Label
    public String getLabelNameResId(Context context, int i) {
        return Attribute.CommonDataKinds.Relation.getLabelNameResId(context, i);
    }
}
