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
    public static final int USB_CONTROL_TRANSFER_TIMEOUT_MS = 200;
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
        int i = this.mLength;
        if (bytesRead < i) {
            stream.advance(i - bytesRead);
            this.mStatus = 2;
            this.mOverUnderRunCount = this.mLength - bytesRead;
            Log.w(TAG, "UNDERRUN t:0x" + Integer.toHexString(this.mType) + " r: " + bytesRead + " < l: " + this.mLength);
        } else if (bytesRead > i) {
            stream.reverse(bytesRead - i);
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
            return "";
        }
        try {
            int rdo = connection.controlTransfer(128, 6, strIndex | 768, 0, sStringBuffer, 255, 200);
            if (rdo >= 0) {
                return new String(sStringBuffer, 2, rdo - 2, "UTF-16LE");
            }
            return "?";
        } catch (Exception e) {
            Log.e(TAG, "Can not communicate with USB device", e);
            return "";
        }
    }

    private void reportParseStatus(ReportCanvas canvas) {
        int status = getStatus();
        if (status != 0) {
            if (status == 1) {
                return;
            }
            if (!(status == 2 || status == 3)) {
                return;
            }
        }
        canvas.writeParagraph("status: " + getStatusString() + " [" + getOverUnderRunCount() + "]", true);
    }

    @Override // com.android.server.usb.descriptors.report.Reporting
    public void report(ReportCanvas canvas) {
        String text = UsbStrings.getDescriptorName(getType()) + ": " + ReportCanvas.getHexString(getType()) + " Len: " + getLength();
        int i = this.mHierarchyLevel;
        if (i != 0) {
            canvas.writeHeader(i, text);
        } else {
            canvas.writeParagraph(text, false);
        }
        if (getStatus() != 1) {
            reportParseStatus(canvas);
        }
    }

    @Override // com.android.server.usb.descriptors.report.Reporting
    public void shortReport(ReportCanvas canvas) {
        String descTypeStr = UsbStrings.getDescriptorName(getType());
        canvas.writeParagraph(descTypeStr + ": " + ReportCanvas.getHexString(getType()) + " Len: " + getLength(), false);
    }
}
