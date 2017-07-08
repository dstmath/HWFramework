package com.huawei.device.connectivitychrlog;

import com.huawei.hsm.permission.StubController;

public class CSegEVENT_WIFI_PORTAL_SAMPLES_COLLECTE extends ChrLogBaseEventModel {
    public LogByteArray aucAPSsid;
    public LogByteArray aucWebContent;
    public ENCEventId enEventId;
    public LogString strAPBssid;
    public LogString strAP_URL;
    public LogString strHTML_Code_Input_ID;
    public LogString strHTML_Login_Button_ID;
    public LogString strHTML_Send_Button_ID;
    public LogString strLOC_CellID;
    public LogString strPhone_Numbe_Input_ID;
    public LogDate tmTimeStamp;
    public LogByte ucAPSsid_len;
    public LogByte ucCardIndex;
    public LogByte ucHTML_Input_Number;
    public LogShort usLen;

    public CSegEVENT_WIFI_PORTAL_SAMPLES_COLLECTE() {
        this.enEventId = new ENCEventId();
        this.usLen = new LogShort();
        this.tmTimeStamp = new LogDate(6);
        this.ucCardIndex = new LogByte();
        this.ucAPSsid_len = new LogByte();
        this.aucAPSsid = new LogByteArray(32);
        this.strAPBssid = new LogString(17);
        this.strAP_URL = new LogString(StubController.PERMISSION_ACCESS_3G);
        this.strLOC_CellID = new LogString(32);
        this.strPhone_Numbe_Input_ID = new LogString(32);
        this.strHTML_Send_Button_ID = new LogString(32);
        this.strHTML_Code_Input_ID = new LogString(32);
        this.strHTML_Login_Button_ID = new LogString(32);
        this.ucHTML_Input_Number = new LogByte();
        this.aucWebContent = new LogByteArray(5120);
        this.lengthMap.put("enEventId", Integer.valueOf(1));
        this.fieldMap.put("enEventId", this.enEventId);
        this.lengthMap.put("usLen", Integer.valueOf(2));
        this.fieldMap.put("usLen", this.usLen);
        this.lengthMap.put("tmTimeStamp", Integer.valueOf(6));
        this.fieldMap.put("tmTimeStamp", this.tmTimeStamp);
        this.lengthMap.put("ucCardIndex", Integer.valueOf(1));
        this.fieldMap.put("ucCardIndex", this.ucCardIndex);
        this.lengthMap.put("ucAPSsid_len", Integer.valueOf(1));
        this.fieldMap.put("ucAPSsid_len", this.ucAPSsid_len);
        this.lengthMap.put("aucAPSsid", Integer.valueOf(32));
        this.fieldMap.put("aucAPSsid", this.aucAPSsid);
        this.lengthMap.put("strAPBssid", Integer.valueOf(17));
        this.fieldMap.put("strAPBssid", this.strAPBssid);
        this.lengthMap.put("strAP_URL", Integer.valueOf(StubController.PERMISSION_ACCESS_3G));
        this.fieldMap.put("strAP_URL", this.strAP_URL);
        this.lengthMap.put("strLOC_CellID", Integer.valueOf(32));
        this.fieldMap.put("strLOC_CellID", this.strLOC_CellID);
        this.lengthMap.put("strPhone_Numbe_Input_ID", Integer.valueOf(32));
        this.fieldMap.put("strPhone_Numbe_Input_ID", this.strPhone_Numbe_Input_ID);
        this.lengthMap.put("strHTML_Send_Button_ID", Integer.valueOf(32));
        this.fieldMap.put("strHTML_Send_Button_ID", this.strHTML_Send_Button_ID);
        this.lengthMap.put("strHTML_Code_Input_ID", Integer.valueOf(32));
        this.fieldMap.put("strHTML_Code_Input_ID", this.strHTML_Code_Input_ID);
        this.lengthMap.put("strHTML_Login_Button_ID", Integer.valueOf(32));
        this.fieldMap.put("strHTML_Login_Button_ID", this.strHTML_Login_Button_ID);
        this.lengthMap.put("ucHTML_Input_Number", Integer.valueOf(1));
        this.fieldMap.put("ucHTML_Input_Number", this.ucHTML_Input_Number);
        this.lengthMap.put("aucWebContent", Integer.valueOf(5120));
        this.fieldMap.put("aucWebContent", this.aucWebContent);
        this.enEventId.setValue("WIFI_PORTAL_SAMPLES_COLLECTE");
        this.usLen.setValue(getTotalLen());
    }
}
