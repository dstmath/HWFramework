package com.android.server.location.gnsschrlog;

import java.util.List;

public class CSegEVENT_GPS_SESSION_EVENT extends ChrLogBaseEventModel {
    public List<CSubApk_Name> cApk_NameList;
    public CSubBrcmPosReferenceInfo cBrcmPosReferenceInfo;
    public CSubFixPos_status cFixPos_status;
    public List<CSubLosPos_Status> cLosPos_StatusList;
    public List<CSubResumePos_Status> cResumePos_StatusList;
    public List<CSubVdrDisableTime> cVdrDisableTimeList;
    public List<CSubVdrEnableTime> cVdrEnableTimeList;
    public ENCAppUsedParm enAppUsedParm;
    public ENCEventId enEventId;
    public ENCNetworkStatus enNetworkStatus;
    public LogInt iAvgPositionAcc;
    public LogInt iCell_Lac;
    public LogInt iCell_Mcc;
    public LogInt iCell_Mnc;
    public LogInt iFixAccuracy;
    public LogInt iFixSpeed;
    public LogInt iLostPosCnt;
    public LogInt iTTFF;
    public LogLong lCatchSvTime;
    public LogLong lFirstCatchSvTime;
    public LogLong lStartTime;
    public LogLong lStopTime;
    public LogString strDataCall_Switch;
    public LogString strIsGpsdResart;
    public LogString strIsIssueSession;
    public LogString strLocSetStatus;
    public LogString strNetWorkAvailable;
    public LogString strProviderMode;
    public LogString strRef_Clk;
    public LogString strWifi_Switch;
    public LogDate tmTimeStamp;
    public LogByte ucAvgCN0When40KMPH;
    public LogByte ucCardIndex;
    public LogByte ucInjectAiding;
    public LogByte ucPosMode;
    public LogShort usCellN_ID;
    public LogShort usGpsdReStartCnt;
    public LogShort usLen;

    public CSegEVENT_GPS_SESSION_EVENT() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.gnsschrlog.CSegEVENT_GPS_SESSION_EVENT.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.gnsschrlog.CSegEVENT_GPS_SESSION_EVENT.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.gnsschrlog.CSegEVENT_GPS_SESSION_EVENT.<init>():void");
    }

    public void setCSubApk_NameList(CSubApk_Name pApk_Name) {
        if (pApk_Name != null) {
            this.cApk_NameList.add(pApk_Name);
            this.lengthMap.put("cApk_NameList", Integer.valueOf((((ChrLogBaseModel) this.cApk_NameList.get(0)).getTotalBytes() * this.cApk_NameList.size()) + 2));
            this.fieldMap.put("cApk_NameList", this.cApk_NameList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubFixPos_status(CSubFixPos_status pFixPos_status) {
        if (pFixPos_status != null) {
            this.cFixPos_status = pFixPos_status;
            this.lengthMap.put("cFixPos_status", Integer.valueOf(this.cFixPos_status.getTotalBytes()));
            this.fieldMap.put("cFixPos_status", this.cFixPos_status);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubLosPos_StatusList(CSubLosPos_Status pLosPos_Status) {
        if (pLosPos_Status != null) {
            this.cLosPos_StatusList.add(pLosPos_Status);
            this.lengthMap.put("cLosPos_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cLosPos_StatusList.get(0)).getTotalBytes() * this.cLosPos_StatusList.size()) + 2));
            this.fieldMap.put("cLosPos_StatusList", this.cLosPos_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubVdrEnableTimeList(CSubVdrEnableTime pVdrEnableTime) {
        if (pVdrEnableTime != null) {
            this.cVdrEnableTimeList.add(pVdrEnableTime);
            this.lengthMap.put("cVdrEnableTimeList", Integer.valueOf((((ChrLogBaseModel) this.cVdrEnableTimeList.get(0)).getTotalBytes() * this.cVdrEnableTimeList.size()) + 2));
            this.fieldMap.put("cVdrEnableTimeList", this.cVdrEnableTimeList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubVdrDisableTimeList(CSubVdrDisableTime pVdrDisableTime) {
        if (pVdrDisableTime != null) {
            this.cVdrDisableTimeList.add(pVdrDisableTime);
            this.lengthMap.put("cVdrDisableTimeList", Integer.valueOf((((ChrLogBaseModel) this.cVdrDisableTimeList.get(0)).getTotalBytes() * this.cVdrDisableTimeList.size()) + 2));
            this.fieldMap.put("cVdrDisableTimeList", this.cVdrDisableTimeList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubResumePos_StatusList(CSubResumePos_Status pResumePos_Status) {
        if (pResumePos_Status != null) {
            this.cResumePos_StatusList.add(pResumePos_Status);
            this.lengthMap.put("cResumePos_StatusList", Integer.valueOf((((ChrLogBaseModel) this.cResumePos_StatusList.get(0)).getTotalBytes() * this.cResumePos_StatusList.size()) + 2));
            this.fieldMap.put("cResumePos_StatusList", this.cResumePos_StatusList);
            this.usLen.setValue(getTotalLen());
        }
    }

    public void setCSubBrcmPosReferenceInfo(CSubBrcmPosReferenceInfo pBrcmPosReferenceInfo) {
        if (pBrcmPosReferenceInfo != null) {
            this.cBrcmPosReferenceInfo = pBrcmPosReferenceInfo;
            this.lengthMap.put("cBrcmPosReferenceInfo", Integer.valueOf(this.cBrcmPosReferenceInfo.getTotalBytes()));
            this.fieldMap.put("cBrcmPosReferenceInfo", this.cBrcmPosReferenceInfo);
            this.usLen.setValue(getTotalLen());
        }
    }
}
