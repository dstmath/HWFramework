package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_DAILY_UPLOAD extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public LogInt iAntSwitchCnt;
    public LogInt iAvgRSSI;
    public LogInt iMainAntTime;
    public LogInt iSecAntTime;
    public LogInt iaccessfailCnt;
    public LogInt iassocrejectCnt;
    public LogInt iauthfailCnt;
    public LogInt iclosefailCnt;
    public LogInt idhcpfailCnt;
    public LogInt idisconnectCnt;
    public LogInt iopenfailCnt;
    public LogInt ireasoncodeCnt1;
    public LogInt ireasoncodeCnt2;
    public LogInt ireasoncodeCnt3;
    public LogInt ireasoncodeCnt4;
    public LogInt ireasoncodeCnt5;
    public LogInt ireasoncodeCnt6;
    public LogInt iscanfailCnt;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogShort usLen;
    public LogShort usSubErrorCode;
    public LogShort usreasoncode1;
    public LogShort usreasoncode2;
    public LogShort usreasoncode3;
    public LogShort usreasoncode4;
    public LogShort usreasoncode5;
    public LogShort usreasoncode6;

    public CSegEVENT_WIFI_DAILY_UPLOAD() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_DAILY_UPLOAD.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_DAILY_UPLOAD.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_DAILY_UPLOAD.<init>():void");
    }
}
