package android.media;

import android.common.HwFrameworkFactory;
import android.rms.iaware.Events;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;

public class HwMediaMonitorManager {
    private static final String TAG = "HwMediaMonitorManager";
    private static int prePid;
    private static int preType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.HwMediaMonitorManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.HwMediaMonitorManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.HwMediaMonitorManager.<clinit>():void");
    }

    public static int writeLogMsg(int priority, int type, String msg) {
        return HwFrameworkFactory.getHwMediaMonitor().writeLogMsg(priority, type, msg);
    }

    public static int writeMediaBigData(int pid, int type, String msg) {
        if (HwMediaMonitorUtils.isMediaBigDataWritedNative(type)) {
            return HwFrameworkFactory.getHwMediaMonitor().writeMediaBigData(pid, type, msg);
        }
        if (pid == prePid && type == preType) {
            return 0;
        }
        HwFrameworkFactory.getHwMediaMonitor().writeMediaBigDataByReportInf(pid, type, msg);
        preType = type;
        prePid = pid;
        return 0;
    }

    public static int forceLogSend(int level) {
        return HwFrameworkFactory.getHwMediaMonitor().forceLogSend(level);
    }

    public static int getStreamBigDataType(int streamType) {
        switch (streamType) {
            case TextToSpeech.SUCCESS /*0*/:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_VOICE_CALL_COUNT;
            case AudioState.ROUTE_EARPIECE /*1*/:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_SYSTEM_COUNT;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_RING_COUNT;
            case Engine.DEFAULT_STREAM /*3*/:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_MUSIC_COUNT;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_ALARM_COUNT;
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_NOTIFICATION_COUNT;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_STREAM_BLUETOOTH_SCO_COUNT;
            case AudioState.ROUTE_SPEAKER /*8*/:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_DTMF_COUNT;
            default:
                return Events.EVENT_FLAG_START;
        }
    }
}
