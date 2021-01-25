package com.android.server.usb.descriptors;

import android.hardware.usb.UsbDevice;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

public final class UsbDescriptorParser {
    private static final boolean DEBUG = false;
    private static final int DESCRIPTORS_ALLOC_SIZE = 128;
    private static final float IN_HEADSET_TRIGGER = 0.75f;
    private static final float OUT_HEADSET_TRIGGER = 0.75f;
    private static final String TAG = "UsbDescriptorParser";
    private int mACInterfacesSpec = 256;
    private UsbConfigDescriptor mCurConfigDescriptor;
    private UsbInterfaceDescriptor mCurInterfaceDescriptor;
    private final ArrayList<UsbDescriptor> mDescriptors;
    private final String mDeviceAddr;
    private UsbDeviceDescriptor mDeviceDescriptor;

    private native String getDescriptorString_native(String str, int i);

    private native byte[] getRawDescriptors_native(String str);

    public UsbDescriptorParser(String deviceAddr, ArrayList<UsbDescriptor> descriptors) {
        this.mDeviceAddr = deviceAddr;
        this.mDescriptors = descriptors;
        this.mDeviceDescriptor = (UsbDeviceDescriptor) descriptors.get(0);
    }

    public UsbDescriptorParser(String deviceAddr, byte[] rawDescriptors) {
        this.mDeviceAddr = deviceAddr;
        this.mDescriptors = new ArrayList<>(128);
        parseDescriptors(rawDescriptors);
    }

    public String getDeviceAddr() {
        return this.mDeviceAddr;
    }

    public int getUsbSpec() {
        UsbDeviceDescriptor usbDeviceDescriptor = this.mDeviceDescriptor;
        if (usbDeviceDescriptor != null) {
            return usbDeviceDescriptor.getSpec();
        }
        throw new IllegalArgumentException();
    }

    public void setACInterfaceSpec(int spec) {
        this.mACInterfacesSpec = spec;
    }

    public int getACInterfaceSpec() {
        return this.mACInterfacesSpec;
    }

    /* access modifiers changed from: private */
    public class UsbDescriptorsStreamFormatException extends Exception {
        String mMessage;

        UsbDescriptorsStreamFormatException(String message) {
            this.mMessage = message;
        }

        @Override // java.lang.Throwable, java.lang.Object
        public String toString() {
            return "Descriptor Stream Format Exception: " + this.mMessage;
        }
    }

    private UsbDescriptor allocDescriptor(ByteStream stream) throws UsbDescriptorsStreamFormatException {
        stream.resetReadCount();
        int length = stream.getUnsignedByte();
        byte type = stream.getByte();
        UsbDescriptor descriptor = null;
        if (type == 1) {
            UsbDeviceDescriptor usbDeviceDescriptor = new UsbDeviceDescriptor(length, type);
            this.mDeviceDescriptor = usbDeviceDescriptor;
            descriptor = usbDeviceDescriptor;
        } else if (type == 2) {
            UsbConfigDescriptor usbConfigDescriptor = new UsbConfigDescriptor(length, type);
            this.mCurConfigDescriptor = usbConfigDescriptor;
            descriptor = usbConfigDescriptor;
            UsbDeviceDescriptor usbDeviceDescriptor2 = this.mDeviceDescriptor;
            if (usbDeviceDescriptor2 != null) {
                usbDeviceDescriptor2.addConfigDescriptor(this.mCurConfigDescriptor);
            } else {
                Log.e(TAG, "Config Descriptor found with no associated Device Descriptor!");
                throw new UsbDescriptorsStreamFormatException("Config Descriptor found with no associated Device Descriptor!");
            }
        } else if (type == 4) {
            UsbInterfaceDescriptor usbInterfaceDescriptor = new UsbInterfaceDescriptor(length, type);
            this.mCurInterfaceDescriptor = usbInterfaceDescriptor;
            descriptor = usbInterfaceDescriptor;
            UsbConfigDescriptor usbConfigDescriptor2 = this.mCurConfigDescriptor;
            if (usbConfigDescriptor2 != null) {
                usbConfigDescriptor2.addInterfaceDescriptor(this.mCurInterfaceDescriptor);
            } else {
                Log.e(TAG, "Interface Descriptor found with no associated Config Descriptor!");
                throw new UsbDescriptorsStreamFormatException("Interface Descriptor found with no associated Config Descriptor!");
            }
        } else if (type == 5) {
            descriptor = new UsbEndpointDescriptor(length, type);
            UsbInterfaceDescriptor usbInterfaceDescriptor2 = this.mCurInterfaceDescriptor;
            if (usbInterfaceDescriptor2 != null) {
                usbInterfaceDescriptor2.addEndpointDescriptor((UsbEndpointDescriptor) descriptor);
            } else {
                Log.e(TAG, "Endpoint Descriptor found with no associated Interface Descriptor!");
                throw new UsbDescriptorsStreamFormatException("Endpoint Descriptor found with no associated Interface Descriptor!");
            }
        } else if (type == 11) {
            descriptor = new UsbInterfaceAssoc(length, type);
        } else if (type == 33) {
            descriptor = new UsbHIDDescriptor(length, type);
        } else if (type == 36) {
            descriptor = UsbACInterface.allocDescriptor(this, stream, length, type);
        } else if (type == 37) {
            descriptor = UsbACEndpoint.allocDescriptor(this, length, type);
        }
        if (descriptor != null) {
            return descriptor;
        }
        Log.i(TAG, "Unknown Descriptor len: " + length + " type:0x" + Integer.toHexString(type));
        return new UsbUnknown(length, type);
    }

    public UsbDeviceDescriptor getDeviceDescriptor() {
        return this.mDeviceDescriptor;
    }

    public UsbInterfaceDescriptor getCurInterface() {
        return this.mCurInterfaceDescriptor;
    }

    public void parseDescriptors(byte[] descriptors) {
        ByteStream stream = new ByteStream(descriptors);
        while (stream.available() > 0) {
            UsbDescriptor descriptor = null;
            try {
                descriptor = allocDescriptor(stream);
            } catch (Exception ex) {
                Log.e(TAG, "Exception allocating USB descriptor.", ex);
            }
            if (descriptor != null) {
                try {
                    descriptor.parseRawDescriptors(stream);
                    descriptor.postParse(stream);
                } catch (Exception ex2) {
                    Log.e(TAG, "Exception parsing USB descriptors.", ex2);
                    descriptor.setStatus(4);
                } catch (Throwable th) {
                    this.mDescriptors.add(descriptor);
                    throw th;
                }
                this.mDescriptors.add(descriptor);
            }
        }
    }

    public boolean parseDevice(boolean[] hasRawDescriptors) {
        byte[] rawDescriptors = getRawDescriptors();
        if (hasRawDescriptors != null && hasRawDescriptors.length == 1) {
            hasRawDescriptors[0] = rawDescriptors != null;
        }
        if (rawDescriptors == null) {
            return false;
        }
        parseDescriptors(rawDescriptors);
        return true;
    }

    public byte[] getRawDescriptors() {
        return getRawDescriptors_native(this.mDeviceAddr);
    }

    public String getDescriptorString(int stringId) {
        return getDescriptorString_native(this.mDeviceAddr, stringId);
    }

    public int getParsingSpec() {
        UsbDeviceDescriptor usbDeviceDescriptor = this.mDeviceDescriptor;
        if (usbDeviceDescriptor != null) {
            return usbDeviceDescriptor.getSpec();
        }
        return 0;
    }

    public ArrayList<UsbDescriptor> getDescriptors() {
        return this.mDescriptors;
    }

    public UsbDevice.Builder toAndroidUsbDevice() {
        UsbDeviceDescriptor usbDeviceDescriptor = this.mDeviceDescriptor;
        if (usbDeviceDescriptor == null) {
            Log.e(TAG, "toAndroidUsbDevice() ERROR - No Device Descriptor");
            return null;
        }
        UsbDevice.Builder device = usbDeviceDescriptor.toAndroid(this);
        if (device == null) {
            Log.e(TAG, "toAndroidUsbDevice() ERROR Creating Device");
        }
        return device;
    }

    public ArrayList<UsbDescriptor> getDescriptors(byte type) {
        ArrayList<UsbDescriptor> list = new ArrayList<>();
        Iterator<UsbDescriptor> it = this.mDescriptors.iterator();
        while (it.hasNext()) {
            UsbDescriptor descriptor = it.next();
            if (descriptor.getType() == type) {
                list.add(descriptor);
            }
        }
        return list;
    }

    public ArrayList<UsbDescriptor> getInterfaceDescriptorsForClass(int usbClass) {
        ArrayList<UsbDescriptor> list = new ArrayList<>();
        Iterator<UsbDescriptor> it = this.mDescriptors.iterator();
        while (it.hasNext()) {
            UsbDescriptor descriptor = it.next();
            if (descriptor.getType() == 4) {
                if (!(descriptor instanceof UsbInterfaceDescriptor)) {
                    Log.w(TAG, "Unrecognized Interface l: " + descriptor.getLength() + " t:0x" + Integer.toHexString(descriptor.getType()));
                } else if (((UsbInterfaceDescriptor) descriptor).getUsbClass() == usbClass) {
                    list.add(descriptor);
                }
            }
        }
        return list;
    }

    public ArrayList<UsbDescriptor> getACInterfaceDescriptors(byte subtype, int subclass) {
        ArrayList<UsbDescriptor> list = new ArrayList<>();
        Iterator<UsbDescriptor> it = this.mDescriptors.iterator();
        while (it.hasNext()) {
            UsbDescriptor descriptor = it.next();
            if (descriptor.getType() == 36) {
                if (descriptor instanceof UsbACInterface) {
                    UsbACInterface acDescriptor = (UsbACInterface) descriptor;
                    if (acDescriptor.getSubtype() == subtype && acDescriptor.getSubclass() == subclass) {
                        list.add(descriptor);
                    }
                } else {
                    Log.w(TAG, "Unrecognized Audio Interface l: " + descriptor.getLength() + " t:0x" + Integer.toHexString(descriptor.getType()));
                }
            }
        }
        return list;
    }

    public boolean hasInput() {
        Iterator<UsbDescriptor> it = getACInterfaceDescriptors((byte) 2, 1).iterator();
        while (it.hasNext()) {
            UsbDescriptor descriptor = it.next();
            if (descriptor instanceof UsbACTerminal) {
                int terminalCategory = ((UsbACTerminal) descriptor).getTerminalType() & -256;
                if (!(terminalCategory == 256 || terminalCategory == 768)) {
                    return true;
                }
            } else {
                Log.w(TAG, "Undefined Audio Input terminal l: " + descriptor.getLength() + " t:0x" + Integer.toHexString(descriptor.getType()));
            }
        }
        return false;
    }

    public boolean hasOutput() {
        Iterator<UsbDescriptor> it = getACInterfaceDescriptors((byte) 3, 1).iterator();
        while (it.hasNext()) {
            UsbDescriptor descriptor = it.next();
            if (descriptor instanceof UsbACTerminal) {
                int terminalCategory = ((UsbACTerminal) descriptor).getTerminalType() & -256;
                if (!(terminalCategory == 256 || terminalCategory == 512)) {
                    return true;
                }
            } else {
                Log.w(TAG, "Undefined Audio Input terminal l: " + descriptor.getLength() + " t:0x" + Integer.toHexString(descriptor.getType()));
            }
        }
        return false;
    }

    public boolean hasMic() {
        Iterator<UsbDescriptor> it = getACInterfaceDescriptors((byte) 2, 1).iterator();
        while (it.hasNext()) {
            UsbDescriptor descriptor = it.next();
            if (descriptor instanceof UsbACTerminal) {
                UsbACTerminal inDescr = (UsbACTerminal) descriptor;
                if (inDescr.getTerminalType() == 513 || inDescr.getTerminalType() == 1026 || inDescr.getTerminalType() == 1024 || inDescr.getTerminalType() == 1539) {
                    return true;
                }
            } else {
                Log.w(TAG, "Undefined Audio Input terminal l: " + descriptor.getLength() + " t:0x" + Integer.toHexString(descriptor.getType()));
            }
        }
        return false;
    }

    public boolean hasSpeaker() {
        Iterator<UsbDescriptor> it = getACInterfaceDescriptors((byte) 3, 1).iterator();
        while (it.hasNext()) {
            UsbDescriptor descriptor = it.next();
            if (descriptor instanceof UsbACTerminal) {
                UsbACTerminal outDescr = (UsbACTerminal) descriptor;
                if (outDescr.getTerminalType() == 769 || outDescr.getTerminalType() == 770 || outDescr.getTerminalType() == 1026) {
                    return true;
                }
            } else {
                Log.w(TAG, "Undefined Audio Output terminal l: " + descriptor.getLength() + " t:0x" + Integer.toHexString(descriptor.getType()));
            }
        }
        return false;
    }

    public boolean hasAudioInterface() {
        return true ^ getInterfaceDescriptorsForClass(1).isEmpty();
    }

    public boolean hasHIDInterface() {
        return !getInterfaceDescriptorsForClass(3).isEmpty();
    }

    public boolean hasStorageInterface() {
        return !getInterfaceDescriptorsForClass(8).isEmpty();
    }

    public boolean hasMIDIInterface() {
        Iterator<UsbDescriptor> it = getInterfaceDescriptorsForClass(1).iterator();
        while (it.hasNext()) {
            UsbDescriptor descriptor = it.next();
            if (!(descriptor instanceof UsbInterfaceDescriptor)) {
                Log.w(TAG, "Undefined Audio Class Interface l: " + descriptor.getLength() + " t:0x" + Integer.toHexString(descriptor.getType()));
            } else if (((UsbInterfaceDescriptor) descriptor).getUsbSubclass() == 3) {
                return true;
            }
        }
        return false;
    }

    public float getInputHeadsetProbability() {
        if (hasMIDIInterface()) {
            return 0.0f;
        }
        float probability = 0.0f;
        boolean hasMic = hasMic();
        boolean hasSpeaker = hasSpeaker();
        if (hasMic && hasSpeaker) {
            probability = 0.0f + 0.75f;
        }
        if (!hasMic || !hasHIDInterface()) {
            return probability;
        }
        return probability + 0.25f;
    }

    public boolean isInputHeadset() {
        return getInputHeadsetProbability() >= 0.75f;
    }

    public float getOutputHeadsetProbability() {
        if (hasMIDIInterface()) {
            return 0.0f;
        }
        float probability = 0.0f;
        boolean hasSpeaker = false;
        Iterator<UsbDescriptor> it = getACInterfaceDescriptors((byte) 3, 1).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            UsbDescriptor descriptor = it.next();
            if (descriptor instanceof UsbACTerminal) {
                UsbACTerminal outDescr = (UsbACTerminal) descriptor;
                if (outDescr.getTerminalType() == 769 || outDescr.getTerminalType() == 770 || outDescr.getTerminalType() == 1026) {
                    break;
                }
            } else {
                Log.w(TAG, "Undefined Audio Output terminal l: " + descriptor.getLength() + " t:0x" + Integer.toHexString(descriptor.getType()));
            }
        }
        hasSpeaker = true;
        if (hasSpeaker) {
            probability = 0.0f + 0.75f;
        }
        if (!hasSpeaker || !hasHIDInterface()) {
            return probability;
        }
        return probability + 0.25f;
    }

    public boolean isOutputHeadset() {
        return getOutputHeadsetProbability() >= 0.75f;
    }
}
