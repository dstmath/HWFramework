package ohos.aafwk.utils.log;

import ohos.hiviewdfx.HiLogLabel;

public class LogLabel {
    static final LogLabel LABEL_DEF = new LogLabelBuilder().setTag("Aafwk").build();
    static final LogLabel LABEL_KEY = new LogLabelBuilder().setTag("AafwkKey").setDomain(LogDomain.KEY).build();
    static final LogLabel LABEL_KEY_BOUND = new LogLabelBuilder().setTag("AafwkKeyBound").setDomain(LogDomain.KEY).build();
    private HiLogLabel label;

    LogLabel(int i, int i2, String str) {
        this.label = new HiLogLabel(i, i2, str);
    }

    public static LogLabel createWithTag(String str) {
        return new LogLabelBuilder().setTag(str).build();
    }

    public static LogLabel create() {
        return createWithTag("");
    }

    /* access modifiers changed from: package-private */
    public HiLogLabel getLabel() {
        return this.label;
    }

    public int getDomain() {
        return this.label.domain;
    }

    public String getTag() {
        return this.label.tag;
    }
}
