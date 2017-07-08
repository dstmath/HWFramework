package com.android.server.location.gnsschrlog;

public class CSegEVENT_GPS_POS_FLOW_ERROR_EVENT extends ChrLogBaseEventModel {
    public LogByteArray aucCurNetStatus;
    public LogByteArray auca_ucServerAdder;
    public ENCEventId enEventId;
    public LogInt ia_ucPosTime;
    public LogLong lPositionAcc;
    public LogLong la_ucAgpsEndTime;
    public LogLong la_ucAgpsStartTime;
    public LogLong la_ucAtlOpenTime;
    public LogLong la_ucConnSvrTime;
    public LogString strApkName;
    public LogString strLostPosTime;
    public LogString strResumePostime;
    public LogString strStartFixTime;
    public LogDate tmTimeStamp;
    public LogByte ucAGPSConnReq;
    public LogByte ucAGPSResult;
    public LogByte ucAidingDataReqFlg;
    public LogByte ucAidingDataStatus;
    public LogByte ucCardIndex;
    public LogByte ucErrorCode;
    public LogByte ucGpsEngineCap;
    public LogByte ucGpsRunStatus;
    public LogByte ucLocSetStatus;
    public LogByte ucNetworkStatus;
    public LogByte ucPosMethod;
    public LogByte ucPosMode;
    public LogByte ucSUPLStatus;
    public LogByte ucSubErrorCode;
    public LogByte ucTimeFlg;
    public LogByte ucucAddrFlg;
    public LogShort usLen;
    public LogShort usucServerIpPort;

    public CSegEVENT_GPS_POS_FLOW_ERROR_EVENT() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_FLOW_ERROR_EVENT.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_FLOW_ERROR_EVENT.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.gnsschrlog.CSegEVENT_GPS_POS_FLOW_ERROR_EVENT.<init>():void");
    }
}
