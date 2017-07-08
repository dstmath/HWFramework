package com.android.server.location.gnsschrlog;

import java.util.List;

public class CSegEVENT_GPS_POS_ERROR_EVENT extends ChrLogBaseEventModel {
    public List<CSubApk_Name> cApk_NameList;
    public CSubBrcm_Assert_Info cBrcm_Assert_Info;
    public CSubData_Delivery_Delay cData_Delivery_Delay;
    public CSubFirst_Fix_Time_Out cFirst_Fix_Time_Out;
    public CSubLos_pos_param cLos_pos_param;
    public CSubNetwork_Pos_Timeout cNetwork_Pos_Timeout;
    public CSubNtp_Data_Param cNtp_Data_Param;
    public ENCEventId enEventId;
    public ENCNetworkStatus enNetworkStatus;
    public ENCScreen_Orientation enScreen_Orientation;
    public LogInt iCell_Baseid;
    public LogInt iCell_Cid;
    public LogInt iCell_Lac;
    public LogInt iCell_Mcc;
    public LogInt iCell_Mnc;
    public LogLong lStartTime;
    public LogString strDataCall_Switch;
    public LogString strLocSetStatus;
    public LogString strNetWorkAvailable;
    public LogString strProviderMode;
    public LogString strScreenState;
    public LogString strWifi_Bssid;
    public LogString strWifi_Ssid;
    public LogString strWifi_Switch;
    public LogDate tmTimeStamp;
    public LogByte ucBT_Switch;
    public LogByte ucCardIndex;
    public LogByte ucErrorCode;
    public LogByte ucNFC_Switch;
    public LogByte ucPosMode;
    public LogByte ucUSB_State;
    public LogShort usCellN_ID;
    public LogShort usCell_SID;
    public LogShort usLen;

    public CSegEVENT_GPS_POS_ERROR_EVENT() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_ERROR_EVENT.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_ERROR_EVENT.<init>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_ERROR_EVENT.<init>():void");
    }

    public void setCSubApk_NameList(CSubApk_Name pApk_Name) {
        if (pApk_Name != null) {
            this.cApk_NameList.add(pApk_Name);
            this.lengthMap.put("cApk_NameList", Integer.valueOf((((ChrLogBaseModel) this.cApk_NameList.get(0)).getTotalBytes() * this.cApk_NameList.size()) + 2));
            this.fieldMap.put("cApk_NameList", this.cApk_NameList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubNtp_Data_Param(CSubNtp_Data_Param pNtp_Data_Param) {
        if (pNtp_Data_Param != null) {
            this.cNtp_Data_Param = pNtp_Data_Param;
            this.lengthMap.put("cNtp_Data_Param", Integer.valueOf(this.cNtp_Data_Param.getTotalBytes()));
            this.fieldMap.put("cNtp_Data_Param", this.cNtp_Data_Param);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubLos_pos_param(CSubLos_pos_param pLos_pos_param) {
        if (pLos_pos_param != null) {
            this.cLos_pos_param = pLos_pos_param;
            this.lengthMap.put("cLos_pos_param", Integer.valueOf(this.cLos_pos_param.getTotalBytes()));
            this.fieldMap.put("cLos_pos_param", this.cLos_pos_param);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubFirst_Fix_Time_Out(CSubFirst_Fix_Time_Out pFirst_Fix_Time_Out) {
        if (pFirst_Fix_Time_Out != null) {
            this.cFirst_Fix_Time_Out = pFirst_Fix_Time_Out;
            this.lengthMap.put("cFirst_Fix_Time_Out", Integer.valueOf(this.cFirst_Fix_Time_Out.getTotalBytes()));
            this.fieldMap.put("cFirst_Fix_Time_Out", this.cFirst_Fix_Time_Out);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubData_Delivery_Delay(CSubData_Delivery_Delay pData_Delivery_Delay) {
        if (pData_Delivery_Delay != null) {
            this.cData_Delivery_Delay = pData_Delivery_Delay;
            this.lengthMap.put("cData_Delivery_Delay", Integer.valueOf(this.cData_Delivery_Delay.getTotalBytes()));
            this.fieldMap.put("cData_Delivery_Delay", this.cData_Delivery_Delay);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubNetwork_Pos_Timeout(CSubNetwork_Pos_Timeout pNetwork_Pos_Timeout) {
        if (pNetwork_Pos_Timeout != null) {
            this.cNetwork_Pos_Timeout = pNetwork_Pos_Timeout;
            this.lengthMap.put("cNetwork_Pos_Timeout", Integer.valueOf(this.cNetwork_Pos_Timeout.getTotalBytes()));
            this.fieldMap.put("cNetwork_Pos_Timeout", this.cNetwork_Pos_Timeout);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubBrcm_Assert_Info(CSubBrcm_Assert_Info pBrcm_Assert_Info) {
        if (pBrcm_Assert_Info != null) {
            this.cBrcm_Assert_Info = pBrcm_Assert_Info;
            this.lengthMap.put("cBrcm_Assert_Info", Integer.valueOf(this.cBrcm_Assert_Info.getTotalBytes()));
            this.fieldMap.put("cBrcm_Assert_Info", this.cBrcm_Assert_Info);
            this.usLen.setValue(getTotalLen());
        }
    }
}
