package com.android.server.usb.descriptors;

import com.android.server.usb.descriptors.report.ReportCanvas;

public final class Usb10ACHeader extends UsbACHeaderInterface {
    private static final String TAG = "Usb10ACHeader";
    private byte mControls;
    private byte[] mInterfaceNums = null;
    private byte mNumInterfaces = 0;

    public Usb10ACHeader(int length, byte type, byte subtype, int subclass, int spec) {
        super(length, type, subtype, subclass, spec);
    }

    public byte getNumInterfaces() {
        return this.mNumInterfaces;
    }

    public byte[] getInterfaceNums() {
        return this.mInterfaceNums;
    }

    public byte getControls() {
        return this.mControls;
    }

    public int parseRawDescriptors(ByteStream stream) {
        this.mTotalLength = stream.unpackUsbShort();
        if (this.mADCRelease >= 512) {
            this.mControls = stream.getByte();
        } else {
            this.mNumInterfaces = stream.getByte();
            this.mInterfaceNums = new byte[this.mNumInterfaces];
            for (int index = 0; index < this.mNumInterfaces; index++) {
                this.mInterfaceNums[index] = stream.getByte();
            }
        }
        return this.mLength;
    }

    public void report(ReportCanvas canvas) {
        super.report(canvas);
        canvas.openList();
        int numInterfaces = getNumInterfaces();
        StringBuilder sb = new StringBuilder();
        sb.append(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + numInterfaces + " Interfaces");
        if (numInterfaces > 0) {
            sb.append(" [");
            byte[] interfaceNums = getInterfaceNums();
            if (interfaceNums != null) {
                for (int index = 0; index < numInterfaces; index++) {
                    sb.append(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + interfaceNums[index]);
                    if (index < numInterfaces - 1) {
                        sb.append(" ");
                    }
                }
            }
            sb.append("]");
        }
        canvas.writeListItem(sb.toString());
        canvas.writeListItem("Controls: " + ReportCanvas.getHexString(getControls()));
        canvas.closeList();
    }
}
