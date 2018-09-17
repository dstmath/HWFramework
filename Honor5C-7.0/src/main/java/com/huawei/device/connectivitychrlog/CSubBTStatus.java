package com.huawei.device.connectivitychrlog;

public class CSubBTStatus extends ChrLogBaseModel {
    public ENCSubEventId enSubEventId;
    public LogByte ucBTConnState;
    public LogByte ucBTState;
    public LogByte ucConnectedDevicesCnt;
    public LogByte ucisA2DPPlaying;
    public LogByte ucisAudioOn;
    public LogByte ucisOPP;

    public CSubBTStatus() {
        this.enSubEventId = new ENCSubEventId();
        this.ucBTState = new LogByte();
        this.ucBTConnState = new LogByte();
        this.ucConnectedDevicesCnt = new LogByte();
        this.ucisAudioOn = new LogByte();
        this.ucisA2DPPlaying = new LogByte();
        this.ucisOPP = new LogByte();
        this.lengthMap.put("enSubEventId", Integer.valueOf(2));
        this.fieldMap.put("enSubEventId", this.enSubEventId);
        this.lengthMap.put("ucBTState", Integer.valueOf(1));
        this.fieldMap.put("ucBTState", this.ucBTState);
        this.lengthMap.put("ucBTConnState", Integer.valueOf(1));
        this.fieldMap.put("ucBTConnState", this.ucBTConnState);
        this.lengthMap.put("ucConnectedDevicesCnt", Integer.valueOf(1));
        this.fieldMap.put("ucConnectedDevicesCnt", this.ucConnectedDevicesCnt);
        this.lengthMap.put("ucisAudioOn", Integer.valueOf(1));
        this.fieldMap.put("ucisAudioOn", this.ucisAudioOn);
        this.lengthMap.put("ucisA2DPPlaying", Integer.valueOf(1));
        this.fieldMap.put("ucisA2DPPlaying", this.ucisA2DPPlaying);
        this.lengthMap.put("ucisOPP", Integer.valueOf(1));
        this.fieldMap.put("ucisOPP", this.ucisOPP);
        this.enSubEventId.setValue("BTStatus");
    }
}
