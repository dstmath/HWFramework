package com.android.server.location.gnsschrlog;

public class CSegEVENT_GPS_DAILY_CNT_REPORT extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public LogInt iAgpsConnCnt;
    public LogInt iAgpsConnFailedCnt;
    public LogInt iAutoNavi_cnt;
    public LogInt iAvgPosAcc;
    public LogInt iAvgTTFF;
    public LogInt iBaidu_cnt;
    public LogInt iCareland_cnt;
    public LogInt iNtpFlashSuccCnt;
    public LogInt iNtpMobileFailCnt;
    public LogInt iNtpReqCnt;
    public LogInt iNtpWifiFailCnt;
    public LogInt iRstartCnt;
    public LogInt iXtraDloadCnt;
    public LogInt iXtraReqCnt;
    public LogInt igpserroruploadCnt;
    public LogInt igpsreqCnt;
    public LogInt inetworkReqCnt;
    public LogInt inetworktimeoutCnt;
    public LogLong lAllPosTime;
    public LogString strIsCn0Good;
    public LogString strIsCn0Valied;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucErrorCode;
    public LogByte ucGpsRfBitMask;
    public LogShort usLen;
    public LogShort usSubErrorCode;

    public CSegEVENT_GPS_DAILY_CNT_REPORT() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.gnsschrlog.CSegEVENT_GPS_DAILY_CNT_REPORT.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.gnsschrlog.CSegEVENT_GPS_DAILY_CNT_REPORT.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.gnsschrlog.CSegEVENT_GPS_DAILY_CNT_REPORT.<init>():void");
    }
}
