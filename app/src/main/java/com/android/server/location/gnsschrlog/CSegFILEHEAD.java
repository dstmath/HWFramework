package com.android.server.location.gnsschrlog;

public class CSegFILEHEAD extends ChrLogBaseModel {
    public ENCChipsetType enChipsetType;
    private LogString strProductName;
    public LogString strSoftwareVersion;
    public LogString strnoExplain;
    public LogByte ucDay;
    public LogByte ucHour;
    private LogByte ucLogVersion;
    public LogByte ucMinute;
    public LogByte ucMonth;
    public LogByte ucReportType;
    public LogByte ucSecond;
    public LogShort usChecksum;
    public LogShort usTimeZone;
    public LogShort usYear;

    public CSegFILEHEAD() {
        this.strProductName = new LogString(20);
        this.ucLogVersion = new LogByte();
        this.strSoftwareVersion = new LogString(50);
        this.enChipsetType = new ENCChipsetType();
        this.usYear = new LogShort();
        this.ucMonth = new LogByte();
        this.ucDay = new LogByte();
        this.ucHour = new LogByte();
        this.ucMinute = new LogByte();
        this.ucSecond = new LogByte();
        this.usTimeZone = new LogShort();
        this.usChecksum = new LogShort();
        this.ucReportType = new LogByte();
        this.strnoExplain = new LogString(100);
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
        this.strProductName.setValue("EMUI50");
        this.ucLogVersion.setValue(7);
    }
}
