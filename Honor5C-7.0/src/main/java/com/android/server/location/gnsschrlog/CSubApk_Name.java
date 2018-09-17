package com.android.server.location.gnsschrlog;

public class CSubApk_Name extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogString strApkName;
    public LogString strApkVersion;

    public CSubApk_Name() {
        this.enSubEventId = new ENCSubEventId();
        this.strApkName = new LogString(50);
        this.strApkVersion = new LogString(12);
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("strApkName", Integer.valueOf(50));
        this.fieldMap.put("strApkName", this.strApkName);
        this.lengthMap.put("strApkVersion", Integer.valueOf(12));
        this.fieldMap.put("strApkVersion", this.strApkVersion);
        this.enSubEventId.setValue("Apk_Name");
    }
}
