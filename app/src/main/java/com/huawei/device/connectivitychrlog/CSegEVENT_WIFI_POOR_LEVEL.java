package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_POOR_LEVEL extends ChrLogBaseEventModel {
    public ENCEventId enEventId;
    public LogInt iRssi2g0LevelCnt;
    public LogInt iRssi2g1LevelCnt;
    public LogInt iRssi2g2LevelCnt;
    public LogInt iRssi2g3LevelCnt;
    public LogInt iRssi2g4LevelCnt;
    public LogInt iRssi5g0LevelCnt;
    public LogInt iRssi5g1LevelCnt;
    public LogInt iRssi5g2LevelCnt;
    public LogInt iRssi5g3LevelCnt;
    public LogInt iRssi5g4LevelCnt;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucRssi2g0LevelAvg;
    public LogByte ucRssi2g1LevelAvg;
    public LogByte ucRssi2g2LevelAvg;
    public LogByte ucRssi2g3LevelAvg;
    public LogByte ucRssi2g4LevelAvg;
    public LogByte ucRssi2gMaxRssi;
    public LogByte ucRssi2gMinRssi;
    public LogByte ucRssi5g0LevelAvg;
    public LogByte ucRssi5g1LevelAvg;
    public LogByte ucRssi5g2LevelAvg;
    public LogByte ucRssi5g3LevelAvg;
    public LogByte ucRssi5g4LevelAvg;
    public LogByte ucRssi5gMaxRssi;
    public LogByte ucRssi5gMinRssi;
    public LogShort usLen;

    public CSegEVENT_WIFI_POOR_LEVEL() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_POOR_LEVEL.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_POOR_LEVEL.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_POOR_LEVEL.<init>():void");
    }
}
