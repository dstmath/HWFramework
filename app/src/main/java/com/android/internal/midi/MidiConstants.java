package com.android.internal.midi;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;

public final class MidiConstants {
    public static final int[] CHANNEL_BYTE_LENGTHS = null;
    public static final byte STATUS_ACTIVE_SENSING = (byte) -2;
    public static final byte STATUS_CHANNEL_MASK = (byte) 15;
    public static final byte STATUS_CHANNEL_PRESSURE = (byte) -48;
    public static final byte STATUS_COMMAND_MASK = (byte) -16;
    public static final byte STATUS_CONTINUE = (byte) -5;
    public static final byte STATUS_CONTROL_CHANGE = (byte) -80;
    public static final byte STATUS_END_SYSEX = (byte) -9;
    public static final byte STATUS_MIDI_TIME_CODE = (byte) -15;
    public static final byte STATUS_NOTE_OFF = Byte.MIN_VALUE;
    public static final byte STATUS_NOTE_ON = (byte) -112;
    public static final byte STATUS_PITCH_BEND = (byte) -32;
    public static final byte STATUS_POLYPHONIC_AFTERTOUCH = (byte) -96;
    public static final byte STATUS_PROGRAM_CHANGE = (byte) -64;
    public static final byte STATUS_RESET = (byte) -1;
    public static final byte STATUS_SONG_POSITION = (byte) -14;
    public static final byte STATUS_SONG_SELECT = (byte) -13;
    public static final byte STATUS_START = (byte) -6;
    public static final byte STATUS_STOP = (byte) -4;
    public static final byte STATUS_SYSTEM_EXCLUSIVE = (byte) -16;
    public static final byte STATUS_TIMING_CLOCK = (byte) -8;
    public static final byte STATUS_TUNE_REQUEST = (byte) -10;
    public static final int[] SYSTEM_BYTE_LENGTHS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.midi.MidiConstants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.midi.MidiConstants.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.midi.MidiConstants.<clinit>():void");
    }

    public static int getBytesPerMessage(byte statusByte) {
        int statusInt = statusByte & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        if (statusInt >= IndexSearchConstants.INDEX_BUILD_FLAG_MASK) {
            return SYSTEM_BYTE_LENGTHS[statusInt & 15];
        }
        if (statusInt >= LogPower.START_CHG_ROTATION) {
            return CHANNEL_BYTE_LENGTHS[(statusInt >> 4) - 8];
        }
        return 0;
    }

    public static boolean isAllActiveSensing(byte[] msg, int offset, int count) {
        int goodBytes = 0;
        for (int i = 0; i < count; i++) {
            if (msg[offset + i] != -2) {
                goodBytes++;
            }
        }
        if (goodBytes == 0) {
            return true;
        }
        return false;
    }

    public static boolean allowRunningStatus(byte command) {
        return command >= -128 && command < -16;
    }

    public static boolean cancelsRunningStatus(byte command) {
        return command >= -16 && command <= -9;
    }
}
