package com.android.internal.midi;

public final class MidiConstants {
    public static final int[] CHANNEL_BYTE_LENGTHS = new int[]{3, 3, 3, 3, 2, 2, 3};
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
    public static final int[] SYSTEM_BYTE_LENGTHS = new int[]{1, 2, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

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
            if (msg[offset + i] != (byte) -2) {
                goodBytes++;
            }
        }
        if (goodBytes == 0) {
            return true;
        }
        return false;
    }

    public static boolean allowRunningStatus(byte command) {
        return command >= STATUS_NOTE_OFF && command < (byte) -16;
    }

    public static boolean cancelsRunningStatus(byte command) {
        return command >= (byte) -16 && command <= (byte) -9;
    }
}
