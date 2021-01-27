package com.huawei.android.hardware.qcomfmradio;

import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class SpurTable {
    private byte mode = -1;
    private byte spurNoOfFreq = 0;
    private List<Spur> spurs = null;

    SpurTable() {
    }

    public List<Spur> GetSpurList() {
        return this.spurs;
    }

    public void SetspurNoOfFreq(byte spurNoOfFreq2) {
        this.spurNoOfFreq = spurNoOfFreq2;
    }

    public void SetMode(byte mode2) {
        this.mode = mode2;
    }

    public void InsertSpur(Spur s) {
        if (this.spurs == null) {
            this.spurs = new ArrayList();
        }
        this.spurs.add(s);
    }

    public byte GetMode() {
        return this.mode;
    }

    public byte GetspurNoOfFreq() {
        return this.spurNoOfFreq;
    }
}
