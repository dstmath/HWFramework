package com.android.server.display;

import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class HwPersistentDataStoreEx implements IHwPersistentDataStoreEx {
    private static final String ATTR_DEVICE_HDCP = "isSupportHDCP";
    private static final String ATTR_DEVICE_UIBC_EXCEPTION = "UibcException";
    private ArrayList<String> mHdcpSupportedList = new ArrayList<>();
    private IPersistentDataStoreInner mPds;
    private ArrayList<String> mUibcExceptionList = new ArrayList<>();

    public HwPersistentDataStoreEx(IPersistentDataStoreInner pds) {
        this.mPds = pds;
    }

    public void addHdcpSupportedDevice(String address) {
        if (address != null && address.length() != 0 && !this.mHdcpSupportedList.contains(address)) {
            this.mHdcpSupportedList.add(address);
        }
    }

    public boolean isHdcpSupported(String address) {
        if (address == null || address.length() == 0) {
            return false;
        }
        return this.mHdcpSupportedList.contains(address);
    }

    public void addUibcExceptionDevice(String address) {
        if (address != null && address.length() != 0 && !this.mUibcExceptionList.contains(address)) {
            this.mUibcExceptionList.add(address);
        }
    }

    public boolean isUibcException(String address) {
        if (address == null || address.length() == 0) {
            return false;
        }
        return this.mUibcExceptionList.contains(address);
    }

    public void loadWifiDisplayExtendAttribute(XmlPullParser parser, String deviceAddress) {
        if (parser != null) {
            if (Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_DEVICE_HDCP))) {
                addHdcpSupportedDevice(deviceAddress);
            }
            if (Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_DEVICE_UIBC_EXCEPTION))) {
                addUibcExceptionDevice(deviceAddress);
            }
        }
    }

    public void saveWifiDisplayExtendAttribute(XmlSerializer serializer, String deviceAddress) throws IOException {
        if (serializer != null && deviceAddress != null && deviceAddress.length() != 0) {
            serializer.attribute(null, ATTR_DEVICE_HDCP, Boolean.toString(isHdcpSupported(deviceAddress)));
            serializer.attribute(null, ATTR_DEVICE_UIBC_EXCEPTION, Boolean.toString(isUibcException(deviceAddress)));
        }
    }
}
