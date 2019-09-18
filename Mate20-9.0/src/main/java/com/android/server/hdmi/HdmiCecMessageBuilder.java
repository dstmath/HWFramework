package com.android.server.hdmi;

import android.net.util.NetworkConstants;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class HdmiCecMessageBuilder {
    private static final int OSD_NAME_MAX_LENGTH = 13;

    private HdmiCecMessageBuilder() {
    }

    static HdmiCecMessage of(int src, int dest, byte[] body) {
        return new HdmiCecMessage(src, dest, body[0], Arrays.copyOfRange(body, 1, body.length));
    }

    static HdmiCecMessage buildFeatureAbortCommand(int src, int dest, int originalOpcode, int reason) {
        return buildCommand(src, dest, 0, new byte[]{(byte) (originalOpcode & 255), (byte) (reason & 255)});
    }

    static HdmiCecMessage buildGivePhysicalAddress(int src, int dest) {
        return buildCommand(src, dest, 131);
    }

    static HdmiCecMessage buildGiveOsdNameCommand(int src, int dest) {
        return buildCommand(src, dest, 70);
    }

    static HdmiCecMessage buildGiveDeviceVendorIdCommand(int src, int dest) {
        return buildCommand(src, dest, 140);
    }

    static HdmiCecMessage buildSetMenuLanguageCommand(int src, String language) {
        if (language.length() != 3) {
            return null;
        }
        String normalized = language.toLowerCase();
        return buildCommand(src, 15, 50, new byte[]{(byte) (normalized.charAt(0) & 255), (byte) (normalized.charAt(1) & 255), (byte) (normalized.charAt(2) & 255)});
    }

    static HdmiCecMessage buildSetOsdNameCommand(int src, int dest, String name) {
        try {
            return buildCommand(src, dest, 71, name.substring(0, Math.min(name.length(), 13)).getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    static HdmiCecMessage buildReportPhysicalAddressCommand(int src, int address, int deviceType) {
        return buildCommand(src, 15, 132, new byte[]{(byte) ((address >> 8) & 255), (byte) (address & 255), (byte) (deviceType & 255)});
    }

    static HdmiCecMessage buildDeviceVendorIdCommand(int src, int vendorId) {
        return buildCommand(src, 15, NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION, new byte[]{(byte) ((vendorId >> 16) & 255), (byte) ((vendorId >> 8) & 255), (byte) (vendorId & 255)});
    }

    static HdmiCecMessage buildCecVersion(int src, int dest, int version) {
        return buildCommand(src, dest, 158, new byte[]{(byte) (version & 255)});
    }

    static HdmiCecMessage buildRequestArcInitiation(int src, int dest) {
        return buildCommand(src, dest, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS);
    }

    static HdmiCecMessage buildRequestArcTermination(int src, int dest) {
        return buildCommand(src, dest, 196);
    }

    static HdmiCecMessage buildReportArcInitiated(int src, int dest) {
        return buildCommand(src, dest, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS);
    }

    static HdmiCecMessage buildReportArcTerminated(int src, int dest) {
        return buildCommand(src, dest, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL);
    }

    static HdmiCecMessage buildTextViewOn(int src, int dest) {
        return buildCommand(src, dest, 13);
    }

    static HdmiCecMessage buildActiveSource(int src, int physicalAddress) {
        return buildCommand(src, 15, 130, physicalAddressToParam(physicalAddress));
    }

    static HdmiCecMessage buildInactiveSource(int src, int physicalAddress) {
        return buildCommand(src, 0, 157, physicalAddressToParam(physicalAddress));
    }

    static HdmiCecMessage buildSetStreamPath(int src, int streamPath) {
        return buildCommand(src, 15, NetworkConstants.ICMPV6_ROUTER_ADVERTISEMENT, physicalAddressToParam(streamPath));
    }

    static HdmiCecMessage buildRoutingChange(int src, int oldPath, int newPath) {
        return buildCommand(src, 15, 128, new byte[]{(byte) ((oldPath >> 8) & 255), (byte) (oldPath & 255), (byte) ((newPath >> 8) & 255), (byte) (newPath & 255)});
    }

    static HdmiCecMessage buildGiveDevicePowerStatus(int src, int dest) {
        return buildCommand(src, dest, 143);
    }

    static HdmiCecMessage buildReportPowerStatus(int src, int dest, int powerStatus) {
        return buildCommand(src, dest, 144, new byte[]{(byte) (powerStatus & 255)});
    }

    static HdmiCecMessage buildReportMenuStatus(int src, int dest, int menuStatus) {
        return buildCommand(src, dest, 142, new byte[]{(byte) (menuStatus & 255)});
    }

    static HdmiCecMessage buildSystemAudioModeRequest(int src, int avr, int avrPhysicalAddress, boolean enableSystemAudio) {
        if (enableSystemAudio) {
            return buildCommand(src, avr, 112, physicalAddressToParam(avrPhysicalAddress));
        }
        return buildCommand(src, avr, 112);
    }

    static HdmiCecMessage buildGiveAudioStatus(int src, int dest) {
        return buildCommand(src, dest, 113);
    }

    static HdmiCecMessage buildUserControlPressed(int src, int dest, int uiCommand) {
        return buildUserControlPressed(src, dest, new byte[]{(byte) (uiCommand & 255)});
    }

    static HdmiCecMessage buildUserControlPressed(int src, int dest, byte[] commandParam) {
        return buildCommand(src, dest, 68, commandParam);
    }

    static HdmiCecMessage buildUserControlReleased(int src, int dest) {
        return buildCommand(src, dest, 69);
    }

    static HdmiCecMessage buildGiveSystemAudioModeStatus(int src, int dest) {
        return buildCommand(src, dest, 125);
    }

    public static HdmiCecMessage buildStandby(int src, int dest) {
        return buildCommand(src, dest, 54);
    }

    static HdmiCecMessage buildVendorCommand(int src, int dest, byte[] params) {
        return buildCommand(src, dest, 137, params);
    }

    static HdmiCecMessage buildVendorCommandWithId(int src, int dest, int vendorId, byte[] operands) {
        byte[] params = new byte[(operands.length + 3)];
        params[0] = (byte) ((vendorId >> 16) & 255);
        params[1] = (byte) ((vendorId >> 8) & 255);
        params[2] = (byte) (vendorId & 255);
        System.arraycopy(operands, 0, params, 3, operands.length);
        return buildCommand(src, dest, 160, params);
    }

    static HdmiCecMessage buildRecordOn(int src, int dest, byte[] params) {
        return buildCommand(src, dest, 9, params);
    }

    static HdmiCecMessage buildRecordOff(int src, int dest) {
        return buildCommand(src, dest, 11);
    }

    static HdmiCecMessage buildSetDigitalTimer(int src, int dest, byte[] params) {
        return buildCommand(src, dest, 151, params);
    }

    static HdmiCecMessage buildSetAnalogueTimer(int src, int dest, byte[] params) {
        return buildCommand(src, dest, 52, params);
    }

    static HdmiCecMessage buildSetExternalTimer(int src, int dest, byte[] params) {
        return buildCommand(src, dest, 162, params);
    }

    static HdmiCecMessage buildClearDigitalTimer(int src, int dest, byte[] params) {
        return buildCommand(src, dest, 153, params);
    }

    static HdmiCecMessage buildClearAnalogueTimer(int src, int dest, byte[] params) {
        return buildCommand(src, dest, 51, params);
    }

    static HdmiCecMessage buildClearExternalTimer(int src, int dest, byte[] params) {
        return buildCommand(src, dest, 161, params);
    }

    private static HdmiCecMessage buildCommand(int src, int dest, int opcode) {
        return new HdmiCecMessage(src, dest, opcode, HdmiCecMessage.EMPTY_PARAM);
    }

    private static HdmiCecMessage buildCommand(int src, int dest, int opcode, byte[] params) {
        return new HdmiCecMessage(src, dest, opcode, params);
    }

    private static byte[] physicalAddressToParam(int physicalAddress) {
        return new byte[]{(byte) ((physicalAddress >> 8) & 255), (byte) (physicalAddress & 255)};
    }
}
