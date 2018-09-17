package com.android.internal.midi;

import android.media.midi.MidiReceiver;
import java.io.IOException;

public class MidiFramer extends MidiReceiver {
    public String TAG = "MidiFramer";
    private byte[] mBuffer = new byte[3];
    private int mCount;
    private boolean mInSysEx;
    private int mNeeded;
    private MidiReceiver mReceiver;
    private byte mRunningStatus;

    public MidiFramer(MidiReceiver receiver) {
        this.mReceiver = receiver;
    }

    public static String formatMidiData(byte[] data, int offset, int count) {
        String text = "MIDI+" + offset + " : ";
        for (int i = 0; i < count; i++) {
            text = text + String.format("0x%02X, ", new Object[]{Byte.valueOf(data[offset + i])});
        }
        return text;
    }

    public void onSend(byte[] data, int offset, int count, long timestamp) throws IOException {
        int sysExStartOffset = this.mInSysEx ? offset : -1;
        for (int i = 0; i < count; i++) {
            byte currentByte = data[offset];
            int currentInt = currentByte & 255;
            if (currentInt >= 128) {
                if (currentInt < 240) {
                    this.mRunningStatus = currentByte;
                    this.mCount = 1;
                    this.mNeeded = MidiConstants.getBytesPerMessage(currentByte) - 1;
                } else if (currentInt >= 248) {
                    if (this.mInSysEx) {
                        this.mReceiver.send(data, sysExStartOffset, offset - sysExStartOffset, timestamp);
                        sysExStartOffset = offset + 1;
                    }
                    this.mReceiver.send(data, offset, 1, timestamp);
                } else if (currentInt == 240) {
                    this.mInSysEx = true;
                    sysExStartOffset = offset;
                } else if (currentInt != 247) {
                    this.mBuffer[0] = currentByte;
                    this.mRunningStatus = (byte) 0;
                    this.mCount = 1;
                    this.mNeeded = MidiConstants.getBytesPerMessage(currentByte) - 1;
                } else if (this.mInSysEx) {
                    this.mReceiver.send(data, sysExStartOffset, (offset - sysExStartOffset) + 1, timestamp);
                    this.mInSysEx = false;
                    sysExStartOffset = -1;
                }
            } else if (!this.mInSysEx) {
                byte[] bArr = this.mBuffer;
                int i2 = this.mCount;
                this.mCount = i2 + 1;
                bArr[i2] = currentByte;
                int i3 = this.mNeeded - 1;
                this.mNeeded = i3;
                if (i3 == 0) {
                    if (this.mRunningStatus != (byte) 0) {
                        this.mBuffer[0] = this.mRunningStatus;
                    }
                    this.mReceiver.send(this.mBuffer, 0, this.mCount, timestamp);
                    this.mNeeded = MidiConstants.getBytesPerMessage(this.mBuffer[0]) - 1;
                    this.mCount = 1;
                }
            }
            offset++;
        }
        if (sysExStartOffset >= 0 && sysExStartOffset < offset) {
            this.mReceiver.send(data, sysExStartOffset, offset - sysExStartOffset, timestamp);
        }
    }
}
