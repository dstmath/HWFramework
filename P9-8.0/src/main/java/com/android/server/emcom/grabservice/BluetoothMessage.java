package com.android.server.emcom.grabservice;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

class BluetoothMessage {
    public static final byte CATEGORY_ID = (byte) 6;
    public static final byte CATE_ID_OFFS = (byte) 6;
    private static final int CHARACTER_SIZE = 7;
    public static final byte CID_OFFS = (byte) 5;
    public static final byte COMMAND_ID = (byte) 2;
    public static final int CRC_SIZE = 2;
    public static final byte CTRL = (byte) 0;
    public static final int CTRL_SIZE = 1;
    public static final byte EVENT_ID_OFFS = (byte) 7;
    public static final byte EVENT_MSG_RECEIVE = (byte) 2;
    public static final byte EVENT_MSG_SEND = (byte) 1;
    public static final byte EVENT_TYPE_OFFS = (byte) 8;
    public static final int LEN_SIZE = 2;
    public static final int MIN_LEN = 9;
    public static final byte NOTIFY_LEN_OFFS = (byte) 10;
    public static final byte NOTIFY_TYPE = (byte) 1;
    public static final byte NOTIFY_TYPE_OFFS = (byte) 9;
    public static final byte NOTIFY_VAL_OFFS = (byte) 11;
    private static final int PROTOCOL_SIZE = 6;
    public static final byte RECEIVE_CANCLE = (byte) 4;
    public static final byte RECEIVE_GRAB = (byte) 6;
    public static final byte SEND_CANCLE = (byte) 3;
    public static final byte SEND_NEW_PACKET = (byte) 1;
    public static final byte SERVICE_ID = (byte) -96;
    public static final byte SID_OFFS = (byte) 4;
    public static final byte SOF_OFFS = (byte) 0;
    public static final int SOF_SIZE = 1;
    public static final byte START_OF_FRAME = (byte) 90;
    private static final String TAG = "GrabService";
    private static int[] crc_table = new int[]{0, 4129, 8258, 12387, 16516, 20645, 24774, 28903, 33032, 37161, 41290, 45419, 49548, 53677, 57806, 61935, 4657, 528, 12915, 8786, 21173, 17044, 29431, 25302, 37689, 33560, 45947, 41818, 54205, 50076, 62463, 58334, 9314, 13379, 1056, 5121, 25830, 29895, 17572, 21637, 42346, 46411, 34088, 38153, 58862, 62927, 50604, 54669, 13907, 9842, 5649, 1584, 30423, 26358, 22165, 18100, 46939, 42874, 38681, 34616, 63455, 59390, 55197, 51132, 18628, 22757, 26758, 30887, 2112, 6241, 10242, 14371, 51660, 55789, 59790, 63919, 35144, 39273, 43274, 47403, 23285, 19156, 31415, 27286, 6769, 2640, 14899, 10770, 56317, 52188, 64447, 60318, 39801, 35672, 47931, 43802, 27814, 31879, 19684, 23749, 11298, 15363, 3168, 7233, 60846, 64911, 52716, 56781, 44330, 48395, 36200, 40265, 32407, 28342, 24277, 20212, 15891, 11826, 7761, 3696, 65439, 61374, 57309, 53244, 48923, 44858, 40793, 36728, 37256, 33193, 45514, 41451, 53516, 49453, 61774, 57711, 4224, 161, 12482, 8419, 20484, 16421, 28742, 24679, 33721, 37784, 41979, 46042, 49981, 54044, 58239, 62302, 689, 4752, 8947, 13010, 16949, 21012, 25207, 29270, 46570, 42443, 38312, 34185, 62830, 58703, 54572, 50445, 13538, 9411, 5280, 1153, 29798, 25671, 21540, 17413, 42971, 47098, 34713, 38840, 59231, 63358, 50973, 55100, 9939, 14066, 1681, 5808, 26199, 30326, 17941, 22068, 55628, 51565, 63758, 59695, 39368, 35305, 47498, 43435, 22596, 18533, 30726, 26663, 6336, 2273, 14466, 10403, 52093, 56156, 60223, 64286, 35833, 39896, 43963, 48026, 19061, 23124, 27191, 31254, 2801, 6864, 10931, 14994, 64814, 60687, 56684, 52557, 48554, 44427, 40424, 36297, 31782, 27655, 23652, 19525, 15522, 11395, 7392, 3265, 61215, 65342, 53085, 57212, 44955, 49082, 36825, 40952, 28183, 32310, 20053, 24180, 11923, 16050, 3793, 7920};
    byte categoryId;
    byte commandId;
    byte eventId;
    byte eventType;
    byte notifyType;
    byte[] notifyVal;
    byte serviceId;

    public BluetoothMessage(byte type, byte cmd, byte notifyType, byte[] notify) {
        this.eventId = type;
        this.eventType = cmd;
        this.notifyType = notifyType;
        this.notifyVal = notify;
    }

    public boolean parse(InputStream in, byte[] buffer) throws IOException {
        int countRead;
        int offset = 1;
        int remaining = 2;
        buffer[0] = START_OF_FRAME;
        do {
            countRead = in.read(buffer, offset, remaining);
            if (countRead < 0) {
                return false;
            }
            offset += countRead;
            remaining -= countRead;
        } while (remaining > 0);
        int length = ((buffer[offset - 2] & 255) << 8) | (buffer[offset - 1] & 255);
        Log.d(TAG, "the length of message is " + length);
        if (length < 9) {
            return false;
        }
        remaining = length;
        do {
            countRead = in.read(buffer, offset, remaining);
            if (countRead < 0) {
                return false;
            }
            offset += countRead;
            remaining -= countRead;
        } while (remaining > 0);
        this.serviceId = buffer[4];
        this.commandId = buffer[5];
        this.categoryId = buffer[6];
        if (this.serviceId == SERVICE_ID && this.commandId == (byte) 2 && this.categoryId == (byte) 6) {
            this.eventId = buffer[7];
            this.eventType = buffer[8];
            this.notifyType = buffer[9];
            int notifyLen = buffer[10];
            this.notifyVal = new byte[notifyLen];
            for (int i = 0; i < notifyLen; i++) {
                this.notifyVal[i] = buffer[i + 11];
            }
            remaining = 2;
            do {
                countRead = in.read(buffer, offset, remaining);
                if (countRead < 0) {
                    return false;
                }
                offset += countRead;
                remaining -= countRead;
            } while (remaining > 0);
            return buildCheckSum(buffer, offset + -2) == ((short) (((buffer[offset + -2] & 255) << 8) | (buffer[offset + -1] & 255)));
        }
        Log.d(TAG, "frame type not match.");
        return false;
    }

    public byte[] encode() {
        int resultSize = this.notifyVal.length + 13;
        byte[] result = new byte[resultSize];
        result[0] = START_OF_FRAME;
        int len = (this.notifyVal.length + 7) + 1;
        result[1] = (byte) ((len >> 8) & 255);
        result[2] = (byte) (len & 255);
        result[3] = (byte) 0;
        result[4] = SERVICE_ID;
        result[5] = (byte) 2;
        result[6] = (byte) 6;
        result[7] = this.eventId;
        result[8] = this.eventType;
        result[9] = this.notifyType;
        result[10] = (byte) this.notifyVal.length;
        for (int i = 0; i < this.notifyVal.length; i++) {
            result[i + 11] = this.notifyVal[i];
        }
        short crc = buildCheckSum(result, resultSize - 2);
        result[resultSize - 2] = (byte) ((crc >> 8) & 255);
        result[resultSize - 1] = (byte) (crc & 255);
        return result;
    }

    private static short buildCheckSum(byte[] data, int len) {
        short crc = (short) 0;
        for (int index = 0; index < len; index++) {
            crc = (short) (crc_table[((crc >> 8) ^ data[index]) & 255] ^ (crc << 8));
        }
        return crc;
    }

    public String toString() {
        byte[] data = encode();
        StringBuffer buffer = new StringBuffer();
        for (byte b : data) {
            buffer.append(AutoGrabTools.byte2HexString(b)).append(" ");
        }
        return buffer.toString();
    }
}
