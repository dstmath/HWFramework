package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.service.notification.NotificationRankerService;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;

public enum SupplicantState implements Parcelable {
    ;
    
    public static final Creator<SupplicantState> CREATOR = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.SupplicantState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.SupplicantState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.SupplicantState.<clinit>():void");
    }

    public static boolean isValidState(SupplicantState state) {
        return (state == UNINITIALIZED || state == INVALID) ? false : true;
    }

    public static boolean isHandshakeState(SupplicantState state) {
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case AudioState.ROUTE_EARPIECE /*1*/:
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
            case AudioState.ROUTE_SPEAKER /*8*/:
                return true;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
            case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
            case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
            case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

    public static boolean isConnecting(SupplicantState state) {
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case AudioState.ROUTE_EARPIECE /*1*/:
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
            case AudioState.ROUTE_SPEAKER /*8*/:
                return true;
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
            case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
            case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
            case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

    public static boolean isDriverActive(SupplicantState state) {
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case AudioState.ROUTE_EARPIECE /*1*/:
            case AudioState.ROUTE_BLUETOOTH /*2*/:
            case Engine.DEFAULT_STREAM /*3*/:
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
            case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
            case AudioState.ROUTE_SPEAKER /*8*/:
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
            case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
                return true;
            case NotificationRankerService.REASON_LISTENER_CANCEL /*10*/:
            case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
            case NotificationRankerService.REASON_GROUP_OPTIMIZATION /*13*/:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }
}
