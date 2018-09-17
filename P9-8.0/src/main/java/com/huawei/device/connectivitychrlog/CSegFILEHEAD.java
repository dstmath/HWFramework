package com.huawei.device.connectivitychrlog;

public class CSegFILEHEAD extends ChrLogBaseModel {
    public ENCChipsetType enChipsetType = new ENCChipsetType();
    private LogString strProductName = new LogString(20);
    public LogString strSoftwareVersion = new LogString(50);
    public LogString strnoExplain = new LogString(100);
    public LogByte ucDay = new LogByte();
    public LogByte ucHour = new LogByte();
    private LogByte ucLogVersion = new LogByte();
    public LogByte ucMinute = new LogByte();
    public LogByte ucMonth = new LogByte();
    public LogByte ucReportType = new LogByte();
    public LogByte ucSecond = new LogByte();
    public LogShort usChecksum = new LogShort();
    public LogShort usTimeZone = new LogShort();
    public LogShort usYear = new LogShort();

    public CSegFILEHEAD() {
        this.lengthMap.put("strProductName", Integer.valueOf(20));
        this.fieldMap.put("strProductName", this.strProductName);
        this.lengthMap.put("ucLogVersion", Integer.valueOf(1));
        this.fieldMap.put("ucLogVersion", this.ucLogVersion);
        this.lengthMap.put("strSoftwareVersion", Integer.valueOf(50));
        this.fieldMap.put("strSoftwareVersion", this.strSoftwareVersion);
        this.lengthMap.put("enChipsetType", Integer.valueOf(1));
        this.fieldMap.put("enChipsetType", this.enChipsetType);
        this.lengthMap.put("usYear", Integer.valueOf(2));
        this.fieldMap.put("usYear", this.usYear);
        this.lengthMap.put("ucMonth", Integer.valueOf(1));
        this.fieldMap.put("ucMonth", this.ucMonth);
        this.lengthMap.put("ucDay", Integer.valueOf(1));
        this.fieldMap.put("ucDay", this.ucDay);
        this.lengthMap.put("ucHour", Integer.valueOf(1));
        this.fieldMap.put("ucHour", this.ucHour);
        this.lengthMap.put("ucMinute", Integer.valueOf(1));
        this.fieldMap.put("ucMinute", this.ucMinute);
        this.lengthMap.put("ucSecond", Integer.valueOf(1));
        this.fieldMap.put("ucSecond", this.ucSecond);
        this.lengthMap.put("usTimeZone", Integer.valueOf(2));
        this.fieldMap.put("usTimeZone", this.usTimeZone);
        this.lengthMap.put("usChecksum", Integer.valueOf(2));
        this.fieldMap.put("usChecksum", this.usChecksum);
        this.lengthMap.put("ucReportType", Integer.valueOf(1));
        this.fieldMap.put("ucReportType", this.ucReportType);
        this.lengthMap.put("strnoExplain", Integer.valueOf(100));
        this.fieldMap.put("strnoExplain", this.strnoExplain);
        this.strProductName.setValue("EMUI51");
        this.ucLogVersion.setValue(18);
    }
}
