package com.android.server.emcom.grabservice;

import android.util.Log;
import com.android.server.display.Utils;
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
    private static int[] crc_table;
    byte categoryId;
    byte commandId;
    byte eventId;
    byte eventType;
    byte notifyType;
    byte[] notifyVal;
    byte serviceId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.emcom.grabservice.BluetoothMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.emcom.grabservice.BluetoothMessage.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.emcom.grabservice.BluetoothMessage.<clinit>():void");
    }

    public BluetoothMessage(byte type, byte cmd, byte notifyType, byte[] notify) {
        this.eventId = type;
        this.eventType = cmd;
        this.notifyType = notifyType;
        this.notifyVal = notify;
    }

    public boolean parse(InputStream in, byte[] buffer) throws IOException {
        int offset = SOF_SIZE;
        int remaining = LEN_SIZE;
        buffer[0] = START_OF_FRAME;
        do {
            int countRead = in.read(buffer, offset, remaining);
            if (countRead < 0) {
                return false;
            }
            offset += countRead;
            remaining -= countRead;
        } while (remaining > 0);
        int length = ((buffer[offset - 2] & Utils.MAXINUM_TEMPERATURE) << 8) | (buffer[offset - 1] & Utils.MAXINUM_TEMPERATURE);
        Log.d(TAG, "the length of message is " + length);
        if (length < MIN_LEN) {
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
        this.categoryId = buffer[PROTOCOL_SIZE];
        if (this.serviceId == -96 && this.commandId == LEN_SIZE && this.categoryId == PROTOCOL_SIZE) {
            this.eventId = buffer[CHARACTER_SIZE];
            this.eventType = buffer[8];
            this.notifyType = buffer[MIN_LEN];
            int notifyLen = buffer[10];
            this.notifyVal = new byte[notifyLen];
            for (int i = 0; i < notifyLen; i += SOF_SIZE) {
                this.notifyVal[i] = buffer[i + 11];
            }
            remaining = LEN_SIZE;
            do {
                countRead = in.read(buffer, offset, remaining);
                if (countRead < 0) {
                    return false;
                }
                offset += countRead;
                remaining -= countRead;
            } while (remaining > 0);
            return buildCheckSum(buffer, offset + -2) == ((short) (((buffer[offset + -2] & Utils.MAXINUM_TEMPERATURE) << 8) | (buffer[offset + -1] & Utils.MAXINUM_TEMPERATURE)));
        }
        Log.d(TAG, "frame type not match.");
        return false;
    }

    public byte[] encode() {
        int resultSize = this.notifyVal.length + 13;
        byte[] result = new byte[resultSize];
        result[0] = START_OF_FRAME;
        int len = (this.notifyVal.length + CHARACTER_SIZE) + SOF_SIZE;
        result[SOF_SIZE] = (byte) ((len >> 8) & Utils.MAXINUM_TEMPERATURE);
        result[LEN_SIZE] = (byte) (len & Utils.MAXINUM_TEMPERATURE);
        result[3] = SOF_OFFS;
        result[4] = SERVICE_ID;
        result[5] = EVENT_MSG_RECEIVE;
        result[PROTOCOL_SIZE] = RECEIVE_GRAB;
        result[CHARACTER_SIZE] = this.eventId;
        result[8] = this.eventType;
        result[MIN_LEN] = this.notifyType;
        result[10] = (byte) this.notifyVal.length;
        for (int i = 0; i < this.notifyVal.length; i += SOF_SIZE) {
            result[i + 11] = this.notifyVal[i];
        }
        short crc = buildCheckSum(result, resultSize - 2);
        result[resultSize - 2] = (byte) ((crc >> 8) & Utils.MAXINUM_TEMPERATURE);
        result[resultSize - 1] = (byte) (crc & Utils.MAXINUM_TEMPERATURE);
        return result;
    }

    private static short buildCheckSum(byte[] data, int len) {
        short crc = (short) 0;
        for (int index = 0; index < len; index += SOF_SIZE) {
            crc = (short) (crc_table[((crc >> 8) ^ data[index]) & Utils.MAXINUM_TEMPERATURE] ^ (crc << 8));
        }
        return crc;
    }

    public String toString() {
        byte[] data = encode();
        StringBuffer buffer = new StringBuffer();
        int length = data.length;
        for (int i = 0; i < length; i += SOF_SIZE) {
            buffer.append(AutoGrabTools.byte2HexString(data[i])).append(" ");
        }
        return buffer.toString();
    }
}
