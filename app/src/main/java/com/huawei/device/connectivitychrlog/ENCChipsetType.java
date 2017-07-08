package com.huawei.device.connectivitychrlog;

public class ENCChipsetType extends Cenum {
    public ENCChipsetType() {
        this.map.put("QUALCOMM", Integer.valueOf(0));
        this.map.put("HISILICON", Integer.valueOf(1));
        this.map.put("MTK", Integer.valueOf(2));
        this.map.put("INFINEON", Integer.valueOf(3));
        this.map.put("SPREADTRUM", Integer.valueOf(4));
        setLength(1);
    }
}
