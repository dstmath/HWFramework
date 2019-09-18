package com.huawei.device.connectivitychrlog;

public class ENCChipsetType extends Cenum {
    public ENCChipsetType() {
        this.map.put("QUALCOMM", 0);
        this.map.put("HISILICON", 1);
        this.map.put("MTK", 2);
        this.map.put("INFINEON", 3);
        this.map.put("SPREADTRUM", 4);
        setLength(1);
    }
}
