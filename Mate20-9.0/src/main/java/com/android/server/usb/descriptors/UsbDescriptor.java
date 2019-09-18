package com.android.server.usb.descriptors;

import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;
import com.android.server.usb.descriptors.report.ReportCanvas;
import com.android.server.usb.descriptors.report.Reporting;
import com.android.server.usb.descriptors.report.UsbStrings;

public abstract class UsbDescriptor implements Reporting {
    public static final int AUDIO_AUDIOCONTROL = 1;
    public static final int AUDIO_AUDIOSTREAMING = 2;
    public static final int AUDIO_MIDISTREAMING = 3;
    public static final int AUDIO_SUBCLASS_UNDEFINED = 0;
    public static final int CLASSID_APPSPECIFIC = 254;
    public static final int CLASSID_AUDIO = 1;
    public static final int CLASSID_AUDIOVIDEO = 16;
    public static final int CLASSID_BILLBOARD = 17;
    public static final int CLASSID_CDC_CONTROL = 10;
    public static final int CLASSID_COM = 2;
    public static final int CLASSID_DEVICE = 0;
    public static final int CLASSID_DIAGNOSTIC = 220;
    public static final int CLASSID_HEALTHCARE = 15;
    public static final int CLASSID_HID = 3;
    public static final int CLASSID_HUB = 9;
    public static final int CLASSID_IMAGE = 6;
    public static final int CLASSID_MISC = 239;
    public static final int CLASSID_PHYSICAL = 5;
    public static final int CLASSID_PRINTER = 7;
    public static final int CLASSID_SECURITY = 13;
    public static final int CLASSID_SMART_CARD = 11;
    public static final int CLASSID_STORAGE = 8;
    public static final int CLASSID_TYPECBRIDGE = 18;
    public static final int CLASSID_VENDSPECIFIC = 255;
    public static final int CLASSID_VIDEO = 14;
    public static final int CLASSID_WIRELESS = 224;
    public static final byte DESCRIPTORTYPE_AUDIO_ENDPOINT = 37;
    public static final byte DESCRIPTORTYPE_AUDIO_INTERFACE = 36;
    public static final byte DESCRIPTORTYPE_BOS = 15;
    public static final byte DESCRIPTORTYPE_CAPABILITY = 16;
    public static final byte DESCRIPTORTYPE_CONFIG = 2;
    public static final byte DESCRIPTORTYPE_DEVICE = 1;
    public static final byte DESCRIPTORTYPE_ENDPOINT = 5;
    public static final byte DESCRIPTORTYPE_ENDPOINT_COMPANION = 48;
    public static final byte DESCRIPTORTYPE_HID = 33;
    public static final byte DESCRIPTORTYPE_HUB = 41;
    public static final byte DESCRIPTORTYPE_INTERFACE = 4;
    public static final byte DESCRIPTORTYPE_INTERFACEASSOC = 11;
    public static final byte DESCRIPTORTYPE_PHYSICAL = 35;
    public static final byte DESCRIPTORTYPE_REPORT = 34;
    public static final byte DESCRIPTORTYPE_STRING = 3;
    public static final byte DESCRIPTORTYPE_SUPERSPEED_HUB = 42;
    public static final int REQUEST_CLEAR_FEATURE = 1;
    public static final int REQUEST_GET_ADDRESS = 5;
    public static final int REQUEST_GET_CONFIGURATION = 8;
    public static final int REQUEST_GET_DESCRIPTOR = 6;
    public static final int REQUEST_GET_STATUS = 0;
    public static final int REQUEST_SET_CONFIGURATION = 9;
    public static final int REQUEST_SET_DESCRIPTOR = 7;
    public static final int REQUEST_SET_FEATURE = 3;
    private static final int SIZE_STRINGBUFFER = 256;
    public static final int STATUS_PARSED_OK = 1;
    public static final int STATUS_PARSED_OVERRUN = 3;
    public static final int STATUS_PARSED_UNDERRUN = 2;
    public static final int STATUS_PARSE_EXCEPTION = 4;
    public static final int STATUS_UNPARSED = 0;
    private static final String TAG = "UsbDescriptor";
    private static String[] sStatusStrings = {"UNPARSED", "PARSED - OK", "PARSED - UNDERRUN", "PARSED - OVERRUN"};
    private static byte[] sStringBuffer = new byte[256];
    protected int mHierarchyLevel;
    protected final int mLength;
    private int mOverUnderRunCount;
    private byte[] mRawData;
    private int mStatus = 0;
    protected final byte mType;

    UsbDescriptor(int length, byte type) {
        if (length >= 2) {
            this.mLength = length;
            this.mType = type;
            return;
        }
        throw new IllegalArgumentException();
    }

    public int getLength() {
        return this.mLength;
    }

    public byte getType() {
        return this.mType;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getOverUnderRunCount() {
        return this.mOverUnderRunCount;
    }

    public String getStatusString() {
        return sStatusStrings[this.mStatus];
    }

    public byte[] getRawData() {
        return this.mRawData;
    }

    public void postParse(ByteStream stream) {
        int bytesRead = stream.getReadCount();
        if (bytesRead < this.mLength) {
            stream.advance(this.mLength - bytesRead);
            this.mStatus = 2;
            this.mOverUnderRunCount = this.mLength - bytesRead;
            Log.w(TAG, "UNDERRUN t:0x" + Integer.toHexString(this.mType) + " r: " + bytesRead + " < l: " + this.mLength);
        } else if (bytesRead > this.mLength) {
            stream.reverse(bytesRead - this.mLength);
            this.mStatus = 3;
            this.mOverUnderRunCount = bytesRead - this.mLength;
            Log.w(TAG, "OVERRRUN t:0x" + Integer.toHexString(this.mType) + " r: " + bytesRead + " > l: " + this.mLength);
        } else {
            this.mStatus = 1;
        }
    }

    public int parseRawDescriptors(ByteStream stream) {
        int dataLen = this.mLength - stream.getReadCount();
        if (dataLen > 0) {
            this.mRawData = new byte[dataLen];
            for (int index = 0; index < dataLen; index++) {
                this.mRawData[index] = stream.getByte();
            }
        }
        return this.mLength;
    }

    public static String getUsbDescriptorString(UsbDeviceConnection connection, byte strIndex) {
        if (strIndex == 0) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        try {
            int rdo = connection.controlTransfer(128, 6, 768 | strIndex, 0, sStringBuffer, 255, 0);
            if (rdo >= 0) {
                return new String(sStringBuffer, 2, rdo - 2, "UTF-16LE");
            }
            return "?";
        } catch (Exception e) {
            Log.e(TAG, "Can not communicate with USB device", e);
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
    }

    private void reportParseStatus(ReportCanvas canvas) {
        switch (getStatus()) {
            case 0:
            case 2:
            case 3:
                canvas.writeParagraph("status: " + getStatusString() + " [" + getOverUnderRunCount() + "]", true);
                return;
            default:
                return;
        }
    }

    public void report(ReportCanvas canvas) {
        String descTypeStr = UsbStrings.getDescriptorName(getType());
        String text = descTypeStr + ": " + ReportCanvas.getHexString(getType()) + " Len: " + getLength();
        if (this.mHierarchyLevel != 0) {
            canvas.writeHeader(this.mHierarchyLevel, text);
        } else {
            canvas.writeParagraph(text, false);
        }
        if (getStatus() != 1) {
            reportParseStatus(canvas);
        }
    }

    public void shortReport(ReportCanvas canvas) {
        String descTypeStr = UsbStrings.getDescriptorName(getType());
        canvas.writeParagraph(descTypeStr + ": " + ReportCanvas.getHexString(getType()) + " Len: " + getLength(), false);
    }
}
