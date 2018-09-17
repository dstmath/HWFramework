package com.huawei.device.connectivitychrlog;

public class CSubTRAFFIC_GROUND extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt iback_recv_bytes = new LogInt();
    public LogInt iback_send_bytes = new LogInt();
    public LogInt ifore_recv_bytes = new LogInt();
    public LogInt ifore_send_bytes = new LogInt();

    public CSubTRAFFIC_GROUND() {
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("iback_send_bytes", Integer.valueOf(4));
        this.fieldMap.put("iback_send_bytes", this.iback_send_bytes);
        this.lengthMap.put("iback_recv_bytes", Integer.valueOf(4));
        this.fieldMap.put("iback_recv_bytes", this.iback_recv_bytes);
        this.lengthMap.put("ifore_send_bytes", Integer.valueOf(4));
        this.fieldMap.put("ifore_send_bytes", this.ifore_send_bytes);
        this.lengthMap.put("ifore_recv_bytes", Integer.valueOf(4));
        this.fieldMap.put("ifore_recv_bytes", this.ifore_recv_bytes);
        this.enSubEventId.setValue("TRAFFIC_GROUND");
    }
}
