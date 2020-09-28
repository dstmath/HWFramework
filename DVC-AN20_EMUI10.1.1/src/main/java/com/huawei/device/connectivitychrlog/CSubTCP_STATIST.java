package com.huawei.device.connectivitychrlog;

public class CSubTCP_STATIST extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId = new ENCSubEventId();
    public LogInt idup_ack = new LogInt();
    public LogInt iest_rst = new LogInt();
    public LogInt iout_rst = new LogInt();
    public LogInt irecv_err_packets = new LogInt();
    public LogInt irecv_packets = new LogInt();
    public LogInt iresend_packets = new LogInt();
    public LogInt irtt_duration = new LogInt();
    public LogInt irtt_packets = new LogInt();
    public LogInt isend_packets = new LogInt();

    public CSubTCP_STATIST() {
        this.lengthMap.put("enSubEventId", 2);
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("isend_packets", 4);
        this.fieldMap.put("isend_packets", this.isend_packets);
        this.lengthMap.put("iresend_packets", 4);
        this.fieldMap.put("iresend_packets", this.iresend_packets);
        this.lengthMap.put("irecv_packets", 4);
        this.fieldMap.put("irecv_packets", this.irecv_packets);
        this.lengthMap.put("irecv_err_packets", 4);
        this.fieldMap.put("irecv_err_packets", this.irecv_err_packets);
        this.lengthMap.put("irtt_duration", 4);
        this.fieldMap.put("irtt_duration", this.irtt_duration);
        this.lengthMap.put("irtt_packets", 4);
        this.fieldMap.put("irtt_packets", this.irtt_packets);
        this.lengthMap.put("iout_rst", 4);
        this.fieldMap.put("iout_rst", this.iout_rst);
        this.lengthMap.put("iest_rst", 4);
        this.fieldMap.put("iest_rst", this.iest_rst);
        this.lengthMap.put("idup_ack", 4);
        this.fieldMap.put("idup_ack", this.idup_ack);
        this.enSubEventId.setValue("TCP_STATIST");
    }
}
