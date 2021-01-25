package com.android.server.usb.descriptors.report;

import com.android.server.usb.descriptors.UsbDescriptorParser;

public abstract class ReportCanvas {
    private static final String TAG = "ReportCanvas";
    private final UsbDescriptorParser mParser;

    public abstract void closeHeader(int i);

    public abstract void closeList();

    public abstract void closeListItem();

    public abstract void closeParagraph();

    public abstract void openHeader(int i);

    public abstract void openList();

    public abstract void openListItem();

    public abstract void openParagraph(boolean z);

    public abstract void write(String str);

    public abstract void writeParagraph(String str, boolean z);

    public ReportCanvas(UsbDescriptorParser parser) {
        this.mParser = parser;
    }

    public UsbDescriptorParser getParser() {
        return this.mParser;
    }

    public void writeHeader(int level, String text) {
        openHeader(level);
        write(text);
        closeHeader(level);
    }

    public void writeListItem(String text) {
        openListItem();
        write(text);
        closeListItem();
    }

    public static String getHexString(byte value) {
        return "0x" + Integer.toHexString(value & 255).toUpperCase();
    }

    public static String getBCDString(int valueBCD) {
        return "" + ((valueBCD >> 8) & 15) + "." + ((valueBCD >> 4) & 15) + (valueBCD & 15);
    }

    public static String getHexString(int value) {
        return "0x" + Integer.toHexString(65535 & value).toUpperCase();
    }

    public void dumpHexArray(byte[] rawData, StringBuilder builder) {
        if (rawData != null) {
            openParagraph(false);
            for (int index = 0; index < rawData.length; index++) {
                builder.append(getHexString(rawData[index]) + " ");
            }
            closeParagraph();
        }
    }
}
