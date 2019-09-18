package com.android.server.usb.descriptors.tree;

import com.android.server.usb.descriptors.UsbEndpointDescriptor;
import com.android.server.usb.descriptors.report.ReportCanvas;

public final class UsbDescriptorsEndpointNode extends UsbDescriptorsTreeNode {
    private static final String TAG = "UsbDescriptorsEndpointNode";
    private final UsbEndpointDescriptor mEndpointDescriptor;

    public UsbDescriptorsEndpointNode(UsbEndpointDescriptor endpointDescriptor) {
        this.mEndpointDescriptor = endpointDescriptor;
    }

    public void report(ReportCanvas canvas) {
        this.mEndpointDescriptor.report(canvas);
    }
}
