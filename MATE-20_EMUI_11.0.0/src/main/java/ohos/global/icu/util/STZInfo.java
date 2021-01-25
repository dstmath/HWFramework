package ohos.global.icu.util;

import java.io.Serializable;

/* access modifiers changed from: package-private */
public final class STZInfo implements Serializable {
    private static final long serialVersionUID = -7849612037842370168L;
    boolean ea;
    int edm;
    int edw;
    int edwm;
    int em = -1;
    int et;
    boolean sa;
    int sdm;
    int sdw;
    int sdwm;
    int sm = -1;
    int st;
    int sy = -1;

    STZInfo() {
    }

    /* access modifiers changed from: package-private */
    public void setStart(int i, int i2, int i3, int i4, int i5, boolean z) {
        this.sm = i;
        this.sdwm = i2;
        this.sdw = i3;
        this.st = i4;
        this.sdm = i5;
        this.sa = z;
    }

    /* access modifiers changed from: package-private */
    public void setEnd(int i, int i2, int i3, int i4, int i5, boolean z) {
        this.em = i;
        this.edwm = i2;
        this.edw = i3;
        this.et = i4;
        this.edm = i5;
        this.ea = z;
    }

    /* access modifiers changed from: package-private */
    public void applyTo(SimpleTimeZone simpleTimeZone) {
        int i = this.sy;
        if (i != -1) {
            simpleTimeZone.setStartYear(i);
        }
        int i2 = this.sm;
        if (i2 != -1) {
            int i3 = this.sdm;
            if (i3 == -1) {
                simpleTimeZone.setStartRule(i2, this.sdwm, this.sdw, this.st);
            } else {
                int i4 = this.sdw;
                if (i4 == -1) {
                    simpleTimeZone.setStartRule(i2, i3, this.st);
                } else {
                    simpleTimeZone.setStartRule(i2, i3, i4, this.st, this.sa);
                }
            }
        }
        int i5 = this.em;
        if (i5 != -1) {
            int i6 = this.edm;
            if (i6 == -1) {
                simpleTimeZone.setEndRule(i5, this.edwm, this.edw, this.et);
                return;
            }
            int i7 = this.edw;
            if (i7 == -1) {
                simpleTimeZone.setEndRule(i5, i6, this.et);
            } else {
                simpleTimeZone.setEndRule(i5, i6, i7, this.et, this.ea);
            }
        }
    }
}
