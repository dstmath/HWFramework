package com.android.server.usb.descriptors.tree;

import com.android.server.usb.descriptors.UsbConfigDescriptor;
import com.android.server.usb.descriptors.report.ReportCanvas;
import java.util.ArrayList;
import java.util.Iterator;

public final class UsbDescriptorsConfigNode extends UsbDescriptorsTreeNode {
    private static final String TAG = "UsbDescriptorsConfigNode";
    private final UsbConfigDescriptor mConfigDescriptor;
    private final ArrayList<UsbDescriptorsInterfaceNode> mInterfaceNodes = new ArrayList<>();

    public UsbDescriptorsConfigNode(UsbConfigDescriptor configDescriptor) {
        this.mConfigDescriptor = configDescriptor;
    }

    public void addInterfaceNode(UsbDescriptorsInterfaceNode interfaceNode) {
        this.mInterfaceNodes.add(interfaceNode);
    }

    public void report(ReportCanvas canvas) {
        this.mConfigDescriptor.report(canvas);
        canvas.openList();
        Iterator<UsbDescriptorsInterfaceNode> it = this.mInterfaceNodes.iterator();
        while (it.hasNext()) {
            it.next().report(canvas);
        }
        canvas.closeList();
    }
}
