package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import java.util.ArrayList;
import java.util.Iterator;

final class DelayedMessageBuffer {
    private final ArrayList<HdmiCecMessage> mBuffer = new ArrayList();
    private final HdmiCecLocalDevice mDevice;

    DelayedMessageBuffer(HdmiCecLocalDevice device) {
        this.mDevice = device;
    }

    void add(HdmiCecMessage message) {
        boolean buffered = true;
        switch (message.getOpcode()) {
            case 114:
            case 192:
                this.mBuffer.add(message);
                break;
            case 130:
                removeActiveSource();
                this.mBuffer.add(message);
                break;
            default:
                buffered = false;
                break;
        }
        if (buffered) {
            HdmiLogger.debug("Buffering message:" + message, new Object[0]);
        }
    }

    private void removeActiveSource() {
        Iterator<HdmiCecMessage> iter = this.mBuffer.iterator();
        while (iter.hasNext()) {
            if (((HdmiCecMessage) iter.next()).getOpcode() == 130) {
                iter.remove();
            }
        }
    }

    boolean isBuffered(int opcode) {
        for (HdmiCecMessage message : this.mBuffer) {
            if (message.getOpcode() == opcode) {
                return true;
            }
        }
        return false;
    }

    void processAllMessages() {
        ArrayList<HdmiCecMessage> copiedBuffer = new ArrayList(this.mBuffer);
        this.mBuffer.clear();
        for (HdmiCecMessage message : copiedBuffer) {
            this.mDevice.onMessage(message);
            HdmiLogger.debug("Processing message:" + message, new Object[0]);
        }
    }

    void processMessagesForDevice(int address) {
        ArrayList<HdmiCecMessage> copiedBuffer = new ArrayList(this.mBuffer);
        this.mBuffer.clear();
        HdmiLogger.debug("Checking message for address:" + address, new Object[0]);
        for (HdmiCecMessage message : copiedBuffer) {
            if (message.getSource() != address) {
                this.mBuffer.add(message);
            } else if (message.getOpcode() != 130 || (this.mDevice.isInputReady(HdmiDeviceInfo.idForCecDevice(address)) ^ 1) == 0) {
                this.mDevice.onMessage(message);
                HdmiLogger.debug("Processing message:" + message, new Object[0]);
            } else {
                this.mBuffer.add(message);
            }
        }
    }

    void processActiveSource(int address) {
        ArrayList<HdmiCecMessage> copiedBuffer = new ArrayList(this.mBuffer);
        this.mBuffer.clear();
        for (HdmiCecMessage message : copiedBuffer) {
            if (message.getOpcode() == 130 && message.getSource() == address) {
                this.mDevice.onMessage(message);
                HdmiLogger.debug("Processing message:" + message, new Object[0]);
            } else {
                this.mBuffer.add(message);
            }
        }
    }
}
