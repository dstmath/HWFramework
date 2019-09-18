package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class UsbMSMidiOutputJack extends UsbACInterface {
    private static final String TAG = "UsbMSMidiOutputJack";

    public UsbMSMidiOutputJack(int length, byte type, byte subtype, int subclass) {
        super(length, type, subtype, subclass);
    }

    public int parseRawDescriptors(ByteStream stream) {
        stream.advance(this.mLength - stream.getReadCount());
        return this.mLength;
    }

    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.writeHeader(3, "MS Midi Output Jack: " + ReportCanvas.getHexString(getType()) + " SubType: " + ReportCanvas.getHexString(getSubclass()) + " Length: " + getLength());
    }
}
