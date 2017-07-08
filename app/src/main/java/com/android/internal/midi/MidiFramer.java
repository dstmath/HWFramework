package com.android.internal.midi;

import android.media.midi.MidiReceiver;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import java.io.IOException;

public class MidiFramer extends MidiReceiver {
    public String TAG;
    private byte[] mBuffer;
    private int mCount;
    private boolean mInSysEx;
    private int mNeeded;
    private MidiReceiver mReceiver;
    private byte mRunningStatus;

    public MidiFramer(MidiReceiver receiver) {
        this.TAG = "MidiFramer";
        this.mBuffer = new byte[3];
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
            int currentInt = currentByte & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
            if (currentInt >= LogPower.START_CHG_ROTATION) {
                if (currentInt < IndexSearchConstants.INDEX_BUILD_FLAG_MASK) {
                    this.mRunningStatus = currentByte;
                    this.mCount = 1;
                    this.mNeeded = MidiConstants.getBytesPerMessage(currentByte) - 1;
                } else if (currentInt >= MetricsEvent.FINGERPRINT_ENROLL_FINISH_SETUP) {
                    if (this.mInSysEx) {
                        this.mReceiver.send(data, sysExStartOffset, offset - sysExStartOffset, timestamp);
                        sysExStartOffset = offset + 1;
                    }
                    this.mReceiver.send(data, offset, 1, timestamp);
                } else if (currentInt == IndexSearchConstants.INDEX_BUILD_FLAG_MASK) {
                    this.mInSysEx = true;
                    sysExStartOffset = offset;
                } else if (currentInt != MetricsEvent.FINGERPRINT_FIND_SENSOR_SETUP) {
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
                    if (this.mRunningStatus != null) {
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
