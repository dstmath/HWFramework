package android.icu.util;

import java.io.Serializable;

final class STZInfo implements Serializable {
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
    public void setStart(int sm2, int sdwm2, int sdw2, int st2, int sdm2, boolean sa2) {
        this.sm = sm2;
        this.sdwm = sdwm2;
        this.sdw = sdw2;
        this.st = st2;
        this.sdm = sdm2;
        this.sa = sa2;
    }

    /* access modifiers changed from: package-private */
    public void setEnd(int em2, int edwm2, int edw2, int et2, int edm2, boolean ea2) {
        this.em = em2;
        this.edwm = edwm2;
        this.edw = edw2;
        this.et = et2;
        this.edm = edm2;
        this.ea = ea2;
    }

    /* access modifiers changed from: package-private */
    public void applyTo(SimpleTimeZone stz) {
        if (this.sy != -1) {
            stz.setStartYear(this.sy);
        }
        if (this.sm != -1) {
            if (this.sdm == -1) {
                stz.setStartRule(this.sm, this.sdwm, this.sdw, this.st);
            } else if (this.sdw == -1) {
                stz.setStartRule(this.sm, this.sdm, this.st);
            } else {
                stz.setStartRule(this.sm, this.sdm, this.sdw, this.st, this.sa);
            }
        }
        if (this.em == -1) {
            return;
        }
        if (this.edm == -1) {
            stz.setEndRule(this.em, this.edwm, this.edw, this.et);
        } else if (this.edw == -1) {
            stz.setEndRule(this.em, this.edm, this.et);
        } else {
            stz.setEndRule(this.em, this.edm, this.edw, this.et, this.ea);
        }
    }
}
