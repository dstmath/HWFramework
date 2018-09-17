package android.graphics;

import android.rms.HwSysResource;
import android.service.notification.NotificationRankerService;
import android.service.voice.VoiceInteractionSession;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;

public class PorterDuff {

    public enum Mode {
        ;
        
        public final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.PorterDuff.Mode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.PorterDuff.Mode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.PorterDuff.Mode.<clinit>():void");
        }

        private Mode(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    public PorterDuff() {
    }

    public static final int modeToInt(Mode mode) {
        return mode.nativeInt;
    }

    public static final Mode intToMode(int val) {
        switch (val) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                return Mode.SRC;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                return Mode.DST;
            case Engine.DEFAULT_STREAM /*3*/:
                return Mode.SRC_OVER;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                return Mode.DST_OVER;
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                return Mode.SRC_IN;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                return Mode.DST_IN;
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                return Mode.SRC_OUT;
            case AudioState.ROUTE_SPEAKER /*8*/:
                return Mode.DST_OUT;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
                return Mode.SRC_ATOP;
            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
                return Mode.DST_ATOP;
            case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
                return Mode.XOR;
            case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
                return Mode.ADD;
            case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                return Mode.MULTIPLY;
            case NotificationRankerService.REASON_PACKAGE_SUSPENDED /*14*/:
                return Mode.SCREEN;
            case NotificationRankerService.REASON_PROFILE_TURNED_OFF /*15*/:
                return Mode.OVERLAY;
            case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                return Mode.DARKEN;
            case HwSysResource.CURSOR /*17*/:
                return Mode.LIGHTEN;
            default:
                return Mode.CLEAR;
        }
    }
}
