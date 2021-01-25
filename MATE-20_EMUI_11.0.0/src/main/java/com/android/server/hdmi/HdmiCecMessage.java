package com.android.server.hdmi;

import com.android.server.display.FoldPolicy;
import java.util.Arrays;
import java.util.Objects;
import libcore.util.EmptyArray;

public final class HdmiCecMessage {
    public static final byte[] EMPTY_PARAM = EmptyArray.BYTE;
    private final int mDestination;
    private final int mOpcode;
    private final byte[] mParams;
    private final int mSource;

    public HdmiCecMessage(int source, int destination, int opcode, byte[] params) {
        this.mSource = source;
        this.mDestination = destination;
        this.mOpcode = opcode & 255;
        this.mParams = Arrays.copyOf(params, params.length);
    }

    public boolean equals(Object message) {
        if (!(message instanceof HdmiCecMessage)) {
            return false;
        }
        HdmiCecMessage that = (HdmiCecMessage) message;
        if (this.mSource == that.getSource() && this.mDestination == that.getDestination() && this.mOpcode == that.getOpcode() && Arrays.equals(this.mParams, that.getParams())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mSource), Integer.valueOf(this.mDestination), Integer.valueOf(this.mOpcode), Integer.valueOf(Arrays.hashCode(this.mParams)));
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
        s.append(String.format("<%s> %X%X:%02X", opcodeToString(this.mOpcode), Integer.valueOf(this.mSource), Integer.valueOf(this.mDestination), Integer.valueOf(this.mOpcode)));
        byte[] bArr = this.mParams;
        if (bArr.length > 0) {
            int length = bArr.length;
            for (int i = 0; i < length; i++) {
                s.append(String.format(":%02X", Byte.valueOf(bArr[i])));
            }
        }
        return s.toString();
    }

    private static String opcodeToString(int opcode) {
        if (opcode == 0) {
            return "Feature Abort";
        }
        if (opcode == 26) {
            return "Give Deck Status";
        }
        if (opcode == 27) {
            return "Deck Status";
        }
        if (opcode == 125) {
            return "Give System Audio Mode Status";
        }
        if (opcode == 126) {
            return "System Audio Mode Status";
        }
        if (opcode == 153) {
            return "Clear Digital Timer";
        }
        if (opcode == 154) {
            return "Set Audio Rate";
        }
        switch (opcode) {
            case 0:
                return "Feature Abort";
            case 13:
                return "Text View On";
            case 15:
                return "Record Tv Screen";
            case 100:
                return "Set Osd String";
            case 103:
                return "Set Timer Program Title";
            case FoldPolicy.NAV_BAR_HEIGHT /* 122 */:
                return "Report Audio Status";
            case 137:
                return "Vendor Command";
            case 138:
                return "Vendor Remote Button Down";
            case 139:
                return "Vendor Remote Button Up";
            case 140:
                return "Give Device Vendor Id";
            case 141:
                return "Menu Request";
            case 142:
                return "Menu Status";
            case 143:
                return "Give Device Power Status";
            case 144:
                return "Report Power Status";
            case HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2 /* 145 */:
                return "Get Menu Language";
            case 146:
                return "Select Analog Service";
            case 147:
                return "Select Digital Service";
            case 151:
                return "Set Digital Timer";
            case 192:
                return "Initiate ARC";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS /* 193 */:
                return "Report ARC Initiated";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL /* 194 */:
                return "Report ARC Terminated";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS /* 195 */:
                return "Request ARC Initiation";
            case 196:
                return "Request ARC Termination";
            case 197:
                return "Terminate ARC";
            case 248:
                return "Cdc Message";
            case 255:
                return "Abort";
            default:
                switch (opcode) {
                    case 4:
                        return "Image View On";
                    case 5:
                        return "Tuner Step Increment";
                    case 6:
                        return "Tuner Step Decrement";
                    case 7:
                        return "Tuner Device Status";
                    case 8:
                        return "Give Tuner Device Status";
                    case 9:
                        return "Record On";
                    case 10:
                        return "Record Status";
                    case 11:
                        return "Record Off";
                    default:
                        switch (opcode) {
                            case HdmiCecKeycode.CEC_KEYCODE_PREVIOUS_CHANNEL /* 50 */:
                                return "Set Menu Language";
                            case HdmiCecKeycode.CEC_KEYCODE_SOUND_SELECT /* 51 */:
                                return "Clear Analog Timer";
                            case HdmiCecKeycode.CEC_KEYCODE_INPUT_SELECT /* 52 */:
                                return "Set Analog Timer";
                            case 53:
                                return "Timer Status";
                            case HdmiCecKeycode.CEC_KEYCODE_HELP /* 54 */:
                                return "Standby";
                            default:
                                switch (opcode) {
                                    case HdmiCecKeycode.CEC_KEYCODE_VOLUME_UP /* 65 */:
                                        return "Play";
                                    case HdmiCecKeycode.CEC_KEYCODE_VOLUME_DOWN /* 66 */:
                                        return "Deck Control";
                                    case HdmiCecKeycode.CEC_KEYCODE_MUTE /* 67 */:
                                        return "Timer Cleared Status";
                                    case HdmiCecKeycode.CEC_KEYCODE_PLAY /* 68 */:
                                        return "User Control Pressed";
                                    case HdmiCecKeycode.CEC_KEYCODE_STOP /* 69 */:
                                        return "User Control Release";
                                    case 70:
                                        return "Give Osd Name";
                                    case HdmiCecKeycode.CEC_KEYCODE_RECORD /* 71 */:
                                        return "Set Osd Name";
                                    default:
                                        switch (opcode) {
                                            case 112:
                                                return "System Audio Mode Request";
                                            case HdmiCecKeycode.CEC_KEYCODE_F1_BLUE /* 113 */:
                                                return "Give Audio Status";
                                            case HdmiCecKeycode.CEC_KEYCODE_F2_RED /* 114 */:
                                                return "Set System Audio Mode";
                                            default:
                                                switch (opcode) {
                                                    case 128:
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
                                                    default:
                                                        switch (opcode) {
                                                            case 157:
                                                                return "InActive Source";
                                                            case 158:
                                                                return "Cec Version";
                                                            case 159:
                                                                return "Get Cec Version";
                                                            case 160:
                                                                return "Vendor Command With Id";
                                                            case 161:
                                                                return "Clear External Timer";
                                                            case 162:
                                                                return "Set External Timer";
                                                            case 163:
                                                                return "Report Short Audio Descriptor";
                                                            case 164:
                                                                return "Request Short Audio Descriptor";
                                                            default:
                                                                return String.format("Opcode: %02X", Integer.valueOf(opcode));
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
    }
}
