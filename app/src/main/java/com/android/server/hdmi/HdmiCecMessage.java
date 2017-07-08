package com.android.server.hdmi;

import com.android.server.display.RampAnimator;
import com.android.server.radar.FrameworkRadar;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.util.Arrays;

public final class HdmiCecMessage {
    public static final byte[] EMPTY_PARAM = null;
    private final int mDestination;
    private final int mOpcode;
    private final byte[] mParams;
    private final int mSource;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.hdmi.HdmiCecMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.hdmi.HdmiCecMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.hdmi.HdmiCecMessage.<clinit>():void");
    }

    public HdmiCecMessage(int source, int destination, int opcode, byte[] params) {
        this.mSource = source;
        this.mDestination = destination;
        this.mOpcode = opcode & RampAnimator.DEFAULT_MAX_BRIGHTNESS;
        this.mParams = Arrays.copyOf(params, params.length);
    }

    public int getSource() {
        return this.mSource;
    }

    public int getDestination() {
        return this.mDestination;
    }

    public int getOpcode() {
        return this.mOpcode;
    }

    public byte[] getParams() {
        return this.mParams;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(String.format("<%s> src: %d, dst: %d", new Object[]{opcodeToString(this.mOpcode), Integer.valueOf(this.mSource), Integer.valueOf(this.mDestination)}));
        if (this.mParams.length > 0) {
            s.append(", params:");
            int length = this.mParams.length;
            for (int i = 0; i < length; i++) {
                s.append(String.format(" %02X", new Object[]{Byte.valueOf(r4[i])}));
            }
        }
        return s.toString();
    }

    private static String opcodeToString(int opcode) {
        switch (opcode) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return "Feature Abort";
            case H.DO_TRAVERSAL /*4*/:
                return "Image View On";
            case H.ADD_STARTING /*5*/:
                return "Tuner Step Increment";
            case H.REMOVE_STARTING /*6*/:
                return "Tuner Step Decrement";
            case H.FINISHED_STARTING /*7*/:
                return "Tuner Device Staus";
            case H.REPORT_APPLICATION_TOKEN_WINDOWS /*8*/:
                return "Give Tuner Device Status";
            case H.REPORT_APPLICATION_TOKEN_DRAWN /*9*/:
                return "Record On";
            case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                return "Record Status";
            case H.WINDOW_FREEZE_TIMEOUT /*11*/:
                return "Record Off";
            case H.APP_TRANSITION_TIMEOUT /*13*/:
                return "Text View On";
            case H.FORCE_GC /*15*/:
                return "Record Tv Screen";
            case H.DO_ANIMATION_CALLBACK /*26*/:
                return "Give Deck Status";
            case H.DO_DISPLAY_ADDED /*27*/:
                return "Deck Status";
            case H.NOTIFY_STARTING_WINDOW_DRAWN /*50*/:
                return "Set Menu Language";
            case H.UPDATE_ANIMATION_SCALE /*51*/:
                return "Clear Analog Timer";
            case H.WINDOW_REMOVE_TIMEOUT /*52*/:
                return "Set Analog Timer";
            case H.NOTIFY_DOCKED_STACK_MINIMIZED_CHANGED /*53*/:
                return "Timer Status";
            case HdmiCecKeycode.CEC_KEYCODE_HELP /*54*/:
                return "Standby";
            case FrameworkRadar.LEVEL_A /*65*/:
                return "Play";
            case FrameworkRadar.LEVEL_B /*66*/:
                return "Deck Control";
            case FrameworkRadar.LEVEL_C /*67*/:
                return "Timer Cleared Status";
            case HdmiCecKeycode.CEC_KEYCODE_PLAY /*68*/:
                return "User Control Pressed";
            case HdmiCecKeycode.CEC_KEYCODE_STOP /*69*/:
                return "User Control Release";
            case HdmiCecKeycode.CEC_KEYCODE_PAUSE /*70*/:
                return "Give Osd Name";
            case HdmiCecKeycode.CEC_KEYCODE_RECORD /*71*/:
                return "Set Osd Name";
            case H.WAIT_KEYGUARD_DISMISS_DONE_TIMEOUT /*100*/:
                return "Set Osd String";
            case HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION /*103*/:
                return "Set Timer Program Title";
            case HdmiCecKeycode.UI_BROADCAST_DIGITAL_CABLE /*112*/:
                return "System Audio Mode Request";
            case HdmiCecKeycode.CEC_KEYCODE_F1_BLUE /*113*/:
                return "Give Audio Status";
            case HdmiCecKeycode.CEC_KEYCODE_F2_RED /*114*/:
                return "Set System Audio Mode";
            case 122:
                return "Report Audio Status";
            case 125:
                return "Give System Audio Mode Status";
            case 126:
                return "System Audio Mode Status";
            case DumpState.DUMP_PACKAGES /*128*/:
                return "Routing Change";
            case 129:
                return "Routing Information";
            case 130:
                return "Active Source";
            case 131:
                return "Give Physical Address";
            case 132:
                return "Report Physical Address";
            case 133:
                return "Request Active Source";
            case 134:
                return "Set Stream Path";
            case 135:
                return "Device Vendor Id";
            case 137:
                return "Vendor Commandn";
            case 138:
                return "Vendor Remote Button Down";
            case 139:
                return "Vendor Remote Button Up";
            case 140:
                return "Give Device Vendor Id";
            case 141:
                return "Menu REquest";
            case 142:
                return "Menu Status";
            case 143:
                return "Give Device Power Status";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_REVERBERATION /*144*/:
                return "Report Power Status";
            case HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2 /*145*/:
                return "Get Menu Language";
            case 146:
                return "Select Analog Service";
            case 147:
                return "Select Digital Service";
            case 151:
                return "Set Digital Timer";
            case 153:
                return "Clear Digital Timer";
            case 154:
                return "Set Audio Rate";
            case 157:
                return "InActive Source";
            case 158:
                return "Cec Version";
            case 159:
                return "Get Cec Version";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_EQUALIZER /*160*/:
                return "Vendor Command With Id";
            case 161:
                return "Clear External Timer";
            case 162:
                return "Set External Timer";
            case 163:
                return "Repot Short Audio Descriptor";
            case 164:
                return "Request Short Audio Descriptor";
            case 192:
                return "Initiate ARC";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS /*193*/:
                return "Report ARC Initiated";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL /*194*/:
                return "Report ARC Terminated";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS /*195*/:
                return "Request ARC Initiation";
            case 196:
                return "Request ARC Termination";
            case 197:
                return "Terminate ARC";
            case 248:
                return "Cdc Message";
            case RampAnimator.DEFAULT_MAX_BRIGHTNESS /*255*/:
                return "Abort";
            default:
                return String.format("Opcode: %02X", new Object[]{Integer.valueOf(opcode)});
        }
    }
}
