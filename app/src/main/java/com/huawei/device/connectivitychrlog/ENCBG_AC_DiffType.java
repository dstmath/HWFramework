package com.huawei.device.connectivitychrlog;

public class ENCBG_AC_DiffType extends Cenum {
    public ENCBG_AC_DiffType() {
        this.map.put("UNKNOWN", Integer.valueOf(0));
        this.map.put("BG_AV_CN_NAV", Integer.valueOf(1));
        this.map.put("BG_AV_CN_POT", Integer.valueOf(2));
        this.map.put("BG_NAV_CN_AV", Integer.valueOf(3));
        this.map.put("BG_NAV_CN_POT", Integer.valueOf(4));
        this.map.put("BG_POT_CN_AV", Integer.valueOf(5));
        this.map.put("BG_POT_CN_NAV", Integer.valueOf(6));
        this.map.put("BG_CGT_CN_AV", Integer.valueOf(7));
        this.map.put("BG_CGT_CN_NAV", Integer.valueOf(8));
        this.map.put("BG_CGT_CN_POT", Integer.valueOf(9));
        setLength(1);
    }
}
