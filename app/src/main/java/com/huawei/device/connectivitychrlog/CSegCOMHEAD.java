package com.huawei.device.connectivitychrlog;

public class CSegCOMHEAD extends ChrLogBaseModel {
    public ENCDeviceIDType1 enDeviceIDType1;
    public ENCDeviceIDType2 enDeviceIDType2;
    public LogString strIMEIorMEID1;
    public LogString strIMEIorMEID2;
    public LogString strSerialNum;

    public CSegCOMHEAD() {
        this.enDeviceIDType1 = new ENCDeviceIDType1();
        this.strIMEIorMEID1 = new LogString(44);
        this.enDeviceIDType2 = new ENCDeviceIDType2();
        this.strIMEIorMEID2 = new LogString(44);
        this.strSerialNum = new LogString(44);
        this.lengthMap.put("enDeviceIDType1", Integer.valueOf(1));
        this.fieldMap.put("enDeviceIDType1", this.enDeviceIDType1);
        this.lengthMap.put("strIMEIorMEID1", Integer.valueOf(44));
        this.fieldMap.put("strIMEIorMEID1", this.strIMEIorMEID1);
        this.lengthMap.put("enDeviceIDType2", Integer.valueOf(1));
        this.fieldMap.put("enDeviceIDType2", this.enDeviceIDType2);
        this.lengthMap.put("strIMEIorMEID2", Integer.valueOf(44));
        this.fieldMap.put("strIMEIorMEID2", this.strIMEIorMEID2);
        this.lengthMap.put("strSerialNum", Integer.valueOf(44));
        this.fieldMap.put("strSerialNum", this.strSerialNum);
    }
}
