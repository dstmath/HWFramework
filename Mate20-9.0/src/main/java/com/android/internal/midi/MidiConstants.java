package com.android.internal.midi;

public final class MidiConstants {
    public static final int[] CHANNEL_BYTE_LENGTHS = {3, 3, 3, 3, 2, 2, 3};
    public static final byte STATUS_ACTIVE_SENSING = -2;
    public static final byte STATUS_CHANNEL_MASK = 15;
    public static final byte STATUS_CHANNEL_PRESSURE = -48;
    public static final byte STATUS_COMMAND_MASK = -16;
    public static final byte STATUS_CONTINUE = -5;
    public static final byte STATUS_CONTROL_CHANGE = -80;
    public static final byte STATUS_END_SYSEX = -9;
    public static final byte STATUS_MIDI_TIME_CODE = -15;
    public static final byte STATUS_NOTE_OFF = Byte.MIN_VALUE;
    public static final byte STATUS_NOTE_ON = -112;
    public static final byte STATUS_PITCH_BEND = -32;
    public static final byte STATUS_POLYPHONIC_AFTERTOUCH = -96;
    public static final byte STATUS_PROGRAM_CHANGE = -64;
    public static final byte STATUS_RESET = -1;
    public static final byte STATUS_SONG_POSITION = -14;
    public static final byte STATUS_SONG_SELECT = -13;
    public static final byte STATUS_START = -6;
    public static final byte STATUS_STOP = -4;
    public static final byte STATUS_SYSTEM_EXCLUSIVE = -16;
    public static final byte STATUS_TIMING_CLOCK = -8;
    public static final byte STATUS_TUNE_REQUEST = -10;
    public static final int[] SYSTEM_BYTE_LENGTHS = {1, 2, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    public static int getBytesPerMessage(byte statusByte) {
        int statusInt = statusByte & 255;
        if (statusInt >= 240) {
            return SYSTEM_BYTE_LENGTHS[statusInt & 15];
        }
        if (statusInt >= 128) {
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
        return command >= Byte.MIN_VALUE && command < -16;
    }

    public static boolean cancelsRunningStatus(byte command) {
        return command >= -16 && command <= -9;
    }
}
