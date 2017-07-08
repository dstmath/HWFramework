package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_WIFIPRO_EXCEPTION_EVENT extends ChrLogBaseEventModel {
    public LogByteArray aucAPSsid;
    public LogByteArray aucRO_APSsid;
    public ENCAC_FailType enAC_FailType;
    public ENCAutoCloseRootCause enAutoCloseRootCause;
    public ENCAutoOpenRootCause enAutoOpenRootCause;
    public ENCBG_AC_DiffType enBG_AC_DiffType;
    public ENCEventId enEventId;
    public ENCRATType enRATType;
    public ENCWifiProSubEvent enWifiProSubEvent;
    public LogInt iHomeAPJudgeTime;
    public LogString strAPBssid;
    public LogString strREDIRECT_URL;
    public LogDate tmTimeStamp;
    public LogByte ucAPSecurity;
    public LogByte ucCardIndex;
    public LogByte ucIPQLevel;
    public LogByte ucMobileSignalLevel;
    public LogShort usAutoOpenWhiteNum;
    public LogShort usCreditScoreRO_Rate;
    public LogShort usHighDataRateRO_Rate;
    public LogShort usHistoryQuilityRO_Rate;
    public LogShort usLen;
    public LogShort usOTA_PacketDropRate;
    public LogShort usRO_Duration;
    public LogShort usRSSI_VALUE;
    public LogShort usRttAvg;
    public LogShort usTcpInSegs;
    public LogShort usTcpOutSegs;
    public LogShort usTcpRetransSegs;
    public LogShort usWIFI_NetSpeed;

    public CSegEVENT_WIFI_WIFIPRO_EXCEPTION_EVENT() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_EXCEPTION_EVENT.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_EXCEPTION_EVENT.<init>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_EXCEPTION_EVENT.<init>():void");
    }
}
