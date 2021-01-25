package ohos.usb;

public class USBEndpoint {
    private int localEndpointAddress;
    private int localEndpointAttributes;
    private int localEndpointDirection;
    private int localEndpointInterval;
    private int localEndpointMaxPacketSize;
    private int localEndpointNumber;
    private int localEndpointType;

    public USBEndpoint(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        this.localEndpointAddress = i;
        this.localEndpointAttributes = i2;
        this.localEndpointDirection = i3;
        this.localEndpointNumber = i4;
        this.localEndpointInterval = i5;
        this.localEndpointMaxPacketSize = i6;
        this.localEndpointType = i7;
    }

    public int getAddress() {
        return this.localEndpointAddress;
    }

    public int getAttributes() {
        return this.localEndpointAttributes;
    }

    public int getDirection() {
        return this.localEndpointDirection;
    }

    public int getEndpointNumber() {
        return this.localEndpointNumber;
    }

    public int getInterval() {
        return this.localEndpointInterval;
    }

    public int getMaxPacketSize() {
        return this.localEndpointMaxPacketSize;
    }

    public int getType() {
        return this.localEndpointType;
    }
}
