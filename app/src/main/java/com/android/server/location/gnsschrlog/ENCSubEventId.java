package com.android.server.location.gnsschrlog;

public class ENCSubEventId extends Cenum {
    public ENCSubEventId() {
        this.map.put("Apk_Name", Integer.valueOf(0));
        this.map.put("FixPos_status", Integer.valueOf(1));
        this.map.put("LosPos_Status", Integer.valueOf(2));
        this.map.put("VdrEnableTime", Integer.valueOf(3));
        this.map.put("VdrDisableTime", Integer.valueOf(4));
        this.map.put("ResumePos_Status", Integer.valueOf(5));
        this.map.put("Ntp_Data_Param", Integer.valueOf(6));
        this.map.put("Los_pos_param", Integer.valueOf(7));
        this.map.put("First_Fix_Time_Out", Integer.valueOf(8));
        this.map.put("Data_Delivery_Delay", Integer.valueOf(9));
        this.map.put("Network_Pos_Timeout", Integer.valueOf(10));
        this.map.put("Brcm_Assert_Info", Integer.valueOf(11));
        this.map.put("BrcmPosReferenceInfo", Integer.valueOf(12));
        this.map.put("MainCardCellInfo", Integer.valueOf(13));
        this.map.put("SecordaryCardCellInfo", Integer.valueOf(14));
        this.map.put("CurrentCell", Integer.valueOf(15));
        this.map.put("NeighborCell", Integer.valueOf(16));
        this.map.put("WifiApInfo", Integer.valueOf(17));
        setLength(2);
    }
}
