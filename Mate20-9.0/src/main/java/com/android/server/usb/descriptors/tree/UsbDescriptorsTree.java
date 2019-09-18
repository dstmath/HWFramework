package com.android.server.usb.descriptors.tree;

import com.android.server.usb.descriptors.UsbACInterface;
import com.android.server.usb.descriptors.UsbConfigDescriptor;
import com.android.server.usb.descriptors.UsbDescriptor;
import com.android.server.usb.descriptors.UsbDescriptorParser;
import com.android.server.usb.descriptors.UsbDeviceDescriptor;
import com.android.server.usb.descriptors.UsbEndpointDescriptor;
import com.android.server.usb.descriptors.UsbInterfaceDescriptor;
import com.android.server.usb.descriptors.report.ReportCanvas;
import java.util.ArrayList;

public final class UsbDescriptorsTree {
    private static final String TAG = "UsbDescriptorsTree";
    private UsbDescriptorsConfigNode mConfigNode;
    private UsbDescriptorsDeviceNode mDeviceNode;
    private UsbDescriptorsInterfaceNode mInterfaceNode;

    private void addDeviceDescriptor(UsbDeviceDescriptor deviceDescriptor) {
        this.mDeviceNode = new UsbDescriptorsDeviceNode(deviceDescriptor);
    }

    private void addConfigDescriptor(UsbConfigDescriptor configDescriptor) {
        this.mConfigNode = new UsbDescriptorsConfigNode(configDescriptor);
        this.mDeviceNode.addConfigDescriptorNode(this.mConfigNode);
    }

    private void addInterfaceDescriptor(UsbInterfaceDescriptor interfaceDescriptor) {
        this.mInterfaceNode = new UsbDescriptorsInterfaceNode(interfaceDescriptor);
        this.mConfigNode.addInterfaceNode(this.mInterfaceNode);
    }

    private void addEndpointDescriptor(UsbEndpointDescriptor endpointDescriptor) {
        this.mInterfaceNode.addEndpointNode(new UsbDescriptorsEndpointNode(endpointDescriptor));
    }

    private void addACInterface(UsbACInterface acInterface) {
        this.mInterfaceNode.addACInterfaceNode(new UsbDescriptorsACInterfaceNode(acInterface));
    }

    public void parse(UsbDescriptorParser parser) {
        ArrayList<UsbDescriptor> descriptors = parser.getDescriptors();
        for (int descrIndex = 0; descrIndex < descriptors.size(); descrIndex++) {
            UsbDescriptor descriptor = descriptors.get(descrIndex);
            switch (descriptor.getType()) {
                case 1:
                    addDeviceDescriptor((UsbDeviceDescriptor) descriptor);
                    break;
                case 2:
                    addConfigDescriptor((UsbConfigDescriptor) descriptor);
                    break;
                case 4:
                    addInterfaceDescriptor((UsbInterfaceDescriptor) descriptor);
                    break;
                case 5:
                    addEndpointDescriptor((UsbEndpointDescriptor) descriptor);
                    break;
                case 36:
                    addACInterface((UsbACInterface) descriptor);
                    break;
            }
        }
    }

    public void report(ReportCanvas canvas) {
        this.mDeviceNode.report(canvas);
    }
}
