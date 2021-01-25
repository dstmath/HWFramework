package com.android.server.hdmi;

import android.hardware.hdmi.HdmiDeviceInfo;
import java.util.ArrayList;
import java.util.Iterator;

final class DelayedMessageBuffer {
    private final ArrayList<HdmiCecMessage> mBuffer = new ArrayList<>();
    private final HdmiCecLocalDevice mDevice;

    DelayedMessageBuffer(HdmiCecLocalDevice device) {
        this.mDevice = device;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:12:? A[RETURN, SYNTHETIC] */
    public void add(HdmiCecMessage message) {
        boolean buffered = true;
        int opcode = message.getOpcode();
        if (opcode != 114) {
            if (opcode == 130) {
                removeActiveSource();
                this.mBuffer.add(message);
            } else if (opcode != 192) {
                buffered = false;
            }
            if (!buffered) {
                HdmiLogger.debug("Buffering message:" + message, new Object[0]);
                return;
            }
            return;
        }
        this.mBuffer.add(message);
        if (!buffered) {
        }
    }

    private void removeActiveSource() {
        Iterator<HdmiCecMessage> iter = this.mBuffer.iterator();
        while (iter.hasNext()) {
            if (iter.next().getOpcode() == 130) {
                iter.remove();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isBuffered(int opcode) {
        Iterator<HdmiCecMessage> it = this.mBuffer.iterator();
        while (it.hasNext()) {
            if (it.next().getOpcode() == opcode) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void processAllMessages() {
        ArrayList<HdmiCecMessage> copiedBuffer = new ArrayList<>(this.mBuffer);
        this.mBuffer.clear();
        Iterator<HdmiCecMessage> it = copiedBuffer.iterator();
        while (it.hasNext()) {
            HdmiCecMessage message = it.next();
            this.mDevice.onMessage(message);
            HdmiLogger.debug("Processing message:" + message, new Object[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public void processMessagesForDevice(int address) {
        ArrayList<HdmiCecMessage> copiedBuffer = new ArrayList<>(this.mBuffer);
        this.mBuffer.clear();
        HdmiLogger.debug("Checking message for address:" + address, new Object[0]);
        Iterator<HdmiCecMessage> it = copiedBuffer.iterator();
        while (it.hasNext()) {
            HdmiCecMessage message = it.next();
            if (message.getSource() != address) {
                this.mBuffer.add(message);
            } else if (message.getOpcode() != 130 || this.mDevice.isInputReady(HdmiDeviceInfo.idForCecDevice(address))) {
                this.mDevice.onMessage(message);
                HdmiLogger.debug("Processing message:" + message, new Object[0]);
            } else {
                this.mBuffer.add(message);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void processActiveSource(int address) {
        ArrayList<HdmiCecMessage> copiedBuffer = new ArrayList<>(this.mBuffer);
        this.mBuffer.clear();
        Iterator<HdmiCecMessage> it = copiedBuffer.iterator();
        while (it.hasNext()) {
            HdmiCecMessage message = it.next();
            if (message.getOpcode() == 130 && message.getSource() == address) {
                this.mDevice.onMessage(message);
                HdmiLogger.debug("Processing message:" + message, new Object[0]);
            } else {
                this.mBuffer.add(message);
            }
        }
    }
}
