package com.android.server.hdmi;

import android.net.util.NetworkConstants;
import com.android.server.wm.WindowManagerService;
import java.util.Arrays;
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
            for (byte data : this.mParams) {
                s.append(String.format(" %02X", new Object[]{Byte.valueOf(data)}));
            }
        }
        return s.toString();
    }

    private static String opcodeToString(int opcode) {
        switch (opcode) {
            case 4:
                return "Image View On";
            case 5:
                return "Tuner Step Increment";
            case 6:
                return "Tuner Step Decrement";
            case 7:
                return "Tuner Device Staus";
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
                    case WindowManagerService.H.DO_ANIMATION_CALLBACK /*26*/:
                        return "Give Deck Status";
                    case 27:
                        return "Deck Status";
                    default:
                        switch (opcode) {
                            case HdmiCecKeycode.CEC_KEYCODE_PREVIOUS_CHANNEL:
                                return "Set Menu Language";
                            case 51:
                                return "Clear Analog Timer";
                            case 52:
                                return "Set Analog Timer";
                            case 53:
                                return "Timer Status";
                            case 54:
                                return "Standby";
                            default:
                                switch (opcode) {
                                    case HdmiCecKeycode.CEC_KEYCODE_VOLUME_UP:
                                        return "Play";
                                    case HdmiCecKeycode.CEC_KEYCODE_VOLUME_DOWN:
                                        return "Deck Control";
                                    case 67:
                                        return "Timer Cleared Status";
                                    case 68:
                                        return "User Control Pressed";
                                    case HdmiCecKeycode.CEC_KEYCODE_STOP:
                                        return "User Control Release";
                                    case HdmiCecKeycode.CEC_KEYCODE_PAUSE:
                                        return "Give Osd Name";
                                    case HdmiCecKeycode.CEC_KEYCODE_RECORD:
                                        return "Set Osd Name";
                                    default:
                                        switch (opcode) {
                                            case 112:
                                                return "System Audio Mode Request";
                                            case 113:
                                                return "Give Audio Status";
                                            case 114:
                                                return "Set System Audio Mode";
                                            default:
                                                switch (opcode) {
                                                    case 125:
                                                        return "Give System Audio Mode Status";
                                                    case 126:
                                                        return "System Audio Mode Status";
                                                    default:
                                                        switch (opcode) {
                                                            case 128:
                                                                return "Routing Change";
                                                            case NetworkConstants.ICMPV6_ECHO_REPLY_TYPE:
                                                                return "Routing Information";
                                                            case 130:
                                                                return "Active Source";
                                                            case 131:
                                                                return "Give Physical Address";
                                                            case 132:
                                                                return "Report Physical Address";
                                                            case NetworkConstants.ICMPV6_ROUTER_SOLICITATION:
                                                                return "Request Active Source";
                                                            case NetworkConstants.ICMPV6_ROUTER_ADVERTISEMENT:
                                                                return "Set Stream Path";
                                                            case NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION:
                                                                return "Device Vendor Id";
                                                            default:
                                                                switch (opcode) {
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
                                                                    case 144:
                                                                        return "Report Power Status";
                                                                    case HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2:
                                                                        return "Get Menu Language";
                                                                    case 146:
                                                                        return "Select Analog Service";
                                                                    case 147:
                                                                        return "Select Digital Service";
                                                                    default:
                                                                        switch (opcode) {
                                                                            case 153:
                                                                                return "Clear Digital Timer";
                                                                            case 154:
                                                                                return "Set Audio Rate";
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
                                                                                        return "Repot Short Audio Descriptor";
                                                                                    case 164:
                                                                                        return "Request Short Audio Descriptor";
                                                                                    default:
                                                                                        switch (opcode) {
                                                                                            case 192:
                                                                                                return "Initiate ARC";
                                                                                            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS:
                                                                                                return "Report ARC Initiated";
                                                                                            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL:
                                                                                                return "Report ARC Terminated";
                                                                                            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS:
                                                                                                return "Request ARC Initiation";
                                                                                            case 196:
                                                                                                return "Request ARC Termination";
                                                                                            case 197:
                                                                                                return "Terminate ARC";
                                                                                            default:
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
                                                                                                    case 122:
                                                                                                        return "Report Audio Status";
                                                                                                    case 151:
                                                                                                        return "Set Digital Timer";
                                                                                                    case 248:
                                                                                                        return "Cdc Message";
                                                                                                    case 255:
                                                                                                        return "Abort";
                                                                                                    default:
                                                                                                        return String.format("Opcode: %02X", new Object[]{Integer.valueOf(opcode)});
                                                                                                }
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
    }
}
