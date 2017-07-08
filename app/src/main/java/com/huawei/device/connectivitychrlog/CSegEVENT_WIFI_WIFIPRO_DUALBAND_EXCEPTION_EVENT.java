package com.huawei.device.connectivitychrlog;

public class CSegEVENT_WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT extends ChrLogBaseEventModel {
    public LogByteArray aucSSID_2G;
    public LogByteArray aucSSID_5G;
    public ENCDualbandExSubEvent enDualbandExSubEvent;
    public ENCEventId enEventId;
    public LogDate tmTimeStamp;
    public LogByte ucCardIndex;
    public LogByte ucIsBluetoothConnected;
    public LogByte ucSingleOrMixed;
    public LogShort usConnectTime_2G;
    public LogShort usConnectTime_5G;
    public LogShort usHandOverErrCode;
    public LogShort usLen;
    public LogShort usLossRate_2G;
    public LogShort usLossRate_5G;
    public LogShort usRSSI_2G;
    public LogShort usRSSI_5G;
    public LogShort usRTT_2G;
    public LogShort usRTT_5G;
    public LogShort usScan_Threshod_RSSI_2G;
    public LogShort usScore_2G;
    public LogShort usScore_5G;
    public LogShort usTarget_RSSI_5G;

    public CSegEVENT_WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT.<init>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT.<init>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT.<init>():void");
    }
}
