package com.android.server.usb.descriptors.tree;

import com.android.server.usb.descriptors.UsbInterfaceDescriptor;
import com.android.server.usb.descriptors.report.ReportCanvas;
import java.util.ArrayList;
import java.util.Iterator;

public final class UsbDescriptorsInterfaceNode extends UsbDescriptorsTreeNode {
    private static final String TAG = "UsbDescriptorsInterfaceNode";
    private final ArrayList<UsbDescriptorsACInterfaceNode> mACInterfaceNodes = new ArrayList<>();
    private final ArrayList<UsbDescriptorsEndpointNode> mEndpointNodes = new ArrayList<>();
    private final UsbInterfaceDescriptor mInterfaceDescriptor;

    public UsbDescriptorsInterfaceNode(UsbInterfaceDescriptor interfaceDescriptor) {
        this.mInterfaceDescriptor = interfaceDescriptor;
    }

    public void addEndpointNode(UsbDescriptorsEndpointNode endpointNode) {
        this.mEndpointNodes.add(endpointNode);
    }

    public void addACInterfaceNode(UsbDescriptorsACInterfaceNode acInterfaceNode) {
        this.mACInterfaceNodes.add(acInterfaceNode);
    }

    public void report(ReportCanvas canvas) {
        this.mInterfaceDescriptor.report(canvas);
        if (this.mACInterfaceNodes.size() > 0) {
            canvas.writeParagraph("Audio Class Interfaces", false);
            canvas.openList();
            Iterator<UsbDescriptorsACInterfaceNode> it = this.mACInterfaceNodes.iterator();
            while (it.hasNext()) {
                it.next().report(canvas);
            }
            canvas.closeList();
        }
        if (this.mEndpointNodes.size() > 0) {
            canvas.writeParagraph("Endpoints", false);
            canvas.openList();
            Iterator<UsbDescriptorsEndpointNode> it2 = this.mEndpointNodes.iterator();
            while (it2.hasNext()) {
                it2.next().report(canvas);
            }
            canvas.closeList();
        }
    }
}
