package ohos.sysappcomponents.contact.entity;

import ohos.app.Context;

public abstract class Label {
    public static final int CUSTOM_LABEL = 0;
    public static final int INVALID_LABEL_ID = -1;
    private int labelId = -1;
    private String labelName;
    private String mainData;

    /* access modifiers changed from: protected */
    public abstract int getDefaultLabelId();

    /* access modifiers changed from: protected */
    public abstract String getLabelNameResId(Context context, int i);

    /* access modifiers changed from: protected */
    public abstract boolean isValidLabel(int i);

    public Label(Context context, String str, int i) {
        this.mainData = str;
        this.labelId = isValidLabel(i) ? i : getDefaultLabelId();
        this.labelName = getLabelNameResId(context, i);
    }

    public Label(String str, String str2) {
        this.mainData = str;
        this.labelId = 0;
        this.labelName = str2;
    }

    public Label(String str, int i, String str2) {
        this.mainData = str;
        this.labelId = i;
        this.labelName = str2;
    }

    public String getMainData() {
        return this.mainData;
    }

    public void setMainData(String str) {
        this.mainData = str;
    }

    public int getLabelId() {
        return this.labelId;
    }

    public void setLabelId(Context context, int i) {
        this.labelId = isValidLabel(i) ? i : getDefaultLabelId();
        this.labelName = getLabelNameResId(context, i);
    }

    public void setCustomLabelName(String str) {
        this.labelId = 0;
        this.labelName = str;
    }

    public String getLabelName() {
        return this.labelName;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Label label = (Label) obj;
        if (this.labelId == label.labelId && this.mainData.equals(label.mainData)) {
            return this.labelName.equals(label.labelName);
        }
        return false;
    }

    public int hashCode() {
        return this.mainData.hashCode();
    }
}
