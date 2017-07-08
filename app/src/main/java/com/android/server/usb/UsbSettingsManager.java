package com.android.server.usb;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.os.Binder;
import android.os.Environment;
import android.os.Parcelable;
import android.os.UserHandle;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.Xml;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.XmlUtils;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.audio.AudioService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class UsbSettingsManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "UsbSettingsManager";
    private static final File sSingleUserSettingsFile = null;
    private final HashMap<UsbAccessory, SparseBooleanArray> mAccessoryPermissionMap;
    private final HashMap<AccessoryFilter, String> mAccessoryPreferenceMap;
    private final Context mContext;
    private final HashMap<String, SparseBooleanArray> mDevicePermissionMap;
    private final HashMap<DeviceFilter, String> mDevicePreferenceMap;
    private final boolean mDisablePermissionDialogs;
    private final Object mLock;
    private final MtpNotificationManager mMtpNotificationManager;
    private final PackageManager mPackageManager;
    MyPackageMonitor mPackageMonitor;
    private final AtomicFile mSettingsFile;
    private final UserHandle mUser;
    private final Context mUserContext;

    private static class AccessoryFilter {
        public final String mManufacturer;
        public final String mModel;
        public final String mVersion;

        public AccessoryFilter(String manufacturer, String model, String version) {
            this.mManufacturer = manufacturer;
            this.mModel = model;
            this.mVersion = version;
        }

        public AccessoryFilter(UsbAccessory accessory) {
            this.mManufacturer = accessory.getManufacturer();
            this.mModel = accessory.getModel();
            this.mVersion = accessory.getVersion();
        }

        public static AccessoryFilter read(XmlPullParser parser) throws XmlPullParserException, IOException {
            String manufacturer = null;
            String model = null;
            String version = null;
            int count = parser.getAttributeCount();
            for (int i = 0; i < count; i++) {
                String name = parser.getAttributeName(i);
                String value = parser.getAttributeValue(i);
                if ("manufacturer".equals(name)) {
                    manufacturer = value;
                } else if ("model".equals(name)) {
                    model = value;
                } else if ("version".equals(name)) {
                    version = value;
                }
            }
            return new AccessoryFilter(manufacturer, model, version);
        }

        public void write(XmlSerializer serializer) throws IOException {
            serializer.startTag(null, "usb-accessory");
            if (this.mManufacturer != null) {
                serializer.attribute(null, "manufacturer", this.mManufacturer);
            }
            if (this.mModel != null) {
                serializer.attribute(null, "model", this.mModel);
            }
            if (this.mVersion != null) {
                serializer.attribute(null, "version", this.mVersion);
            }
            serializer.endTag(null, "usb-accessory");
        }

        public boolean matches(UsbAccessory acc) {
            if (this.mManufacturer != null && !acc.getManufacturer().equals(this.mManufacturer)) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mModel != null && !acc.getModel().equals(this.mModel)) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mVersion == null || acc.getVersion().equals(this.mVersion)) {
                return true;
            }
            return UsbSettingsManager.DEBUG;
        }

        public boolean matches(AccessoryFilter f) {
            if (this.mManufacturer != null && !f.mManufacturer.equals(this.mManufacturer)) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mModel != null && !f.mModel.equals(this.mModel)) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mVersion == null || f.mVersion.equals(this.mVersion)) {
                return true;
            }
            return UsbSettingsManager.DEBUG;
        }

        public boolean equals(Object obj) {
            boolean z = UsbSettingsManager.DEBUG;
            if (this.mManufacturer == null || this.mModel == null || this.mVersion == null) {
                return UsbSettingsManager.DEBUG;
            }
            if (obj instanceof AccessoryFilter) {
                AccessoryFilter filter = (AccessoryFilter) obj;
                if (this.mManufacturer.equals(filter.mManufacturer) && this.mModel.equals(filter.mModel)) {
                    z = this.mVersion.equals(filter.mVersion);
                }
                return z;
            } else if (!(obj instanceof UsbAccessory)) {
                return UsbSettingsManager.DEBUG;
            } else {
                UsbAccessory accessory = (UsbAccessory) obj;
                if (this.mManufacturer.equals(accessory.getManufacturer()) && this.mModel.equals(accessory.getModel())) {
                    z = this.mVersion.equals(accessory.getVersion());
                }
                return z;
            }
        }

        public int hashCode() {
            int i = 0;
            int hashCode = (this.mManufacturer == null ? 0 : this.mManufacturer.hashCode()) ^ (this.mModel == null ? 0 : this.mModel.hashCode());
            if (this.mVersion != null) {
                i = this.mVersion.hashCode();
            }
            return hashCode ^ i;
        }

        public String toString() {
            return "AccessoryFilter[mManufacturer=\"" + this.mManufacturer + "\", mModel=\"" + this.mModel + "\", mVersion=\"" + this.mVersion + "\"]";
        }
    }

    private static class DeviceFilter {
        public final int mClass;
        public final String mManufacturerName;
        public final int mProductId;
        public final String mProductName;
        public final int mProtocol;
        public final String mSerialNumber;
        public final int mSubclass;
        public final int mVendorId;

        public DeviceFilter(int vid, int pid, int clasz, int subclass, int protocol, String manufacturer, String product, String serialnum) {
            this.mVendorId = vid;
            this.mProductId = pid;
            this.mClass = clasz;
            this.mSubclass = subclass;
            this.mProtocol = protocol;
            this.mManufacturerName = manufacturer;
            this.mProductName = product;
            this.mSerialNumber = serialnum;
        }

        public DeviceFilter(UsbDevice device) {
            this.mVendorId = device.getVendorId();
            this.mProductId = device.getProductId();
            this.mClass = device.getDeviceClass();
            this.mSubclass = device.getDeviceSubclass();
            this.mProtocol = device.getDeviceProtocol();
            this.mManufacturerName = device.getManufacturerName();
            this.mProductName = device.getProductName();
            this.mSerialNumber = device.getSerialNumber();
        }

        public static DeviceFilter read(XmlPullParser parser) throws XmlPullParserException, IOException {
            int vendorId = -1;
            int productId = -1;
            int deviceClass = -1;
            int deviceSubclass = -1;
            int deviceProtocol = -1;
            String manufacturerName = null;
            String productName = null;
            String serialNumber = null;
            int count = parser.getAttributeCount();
            for (int i = 0; i < count; i++) {
                String name = parser.getAttributeName(i);
                String value = parser.getAttributeValue(i);
                if ("manufacturer-name".equals(name)) {
                    manufacturerName = value;
                } else if ("product-name".equals(name)) {
                    productName = value;
                } else if ("serial-number".equals(name)) {
                    serialNumber = value;
                } else {
                    int radix = 10;
                    if (value != null && value.length() > 2 && value.charAt(0) == '0' && (value.charAt(1) == 'x' || value.charAt(1) == 'X')) {
                        radix = 16;
                        value = value.substring(2);
                    }
                    try {
                        int intValue = Integer.parseInt(value, radix);
                        if ("vendor-id".equals(name)) {
                            vendorId = intValue;
                        } else if ("product-id".equals(name)) {
                            productId = intValue;
                        } else if (AudioService.CONNECT_INTENT_KEY_DEVICE_CLASS.equals(name)) {
                            deviceClass = intValue;
                        } else if ("subclass".equals(name)) {
                            deviceSubclass = intValue;
                        } else if ("protocol".equals(name)) {
                            deviceProtocol = intValue;
                        }
                    } catch (NumberFormatException e) {
                        Slog.e(UsbSettingsManager.TAG, "invalid number for field " + name, e);
                    }
                }
            }
            return new DeviceFilter(vendorId, productId, deviceClass, deviceSubclass, deviceProtocol, manufacturerName, productName, serialNumber);
        }

        public void write(XmlSerializer serializer) throws IOException {
            serializer.startTag(null, "usb-device");
            if (this.mVendorId != -1) {
                serializer.attribute(null, "vendor-id", Integer.toString(this.mVendorId));
            }
            if (this.mProductId != -1) {
                serializer.attribute(null, "product-id", Integer.toString(this.mProductId));
            }
            if (this.mClass != -1) {
                serializer.attribute(null, AudioService.CONNECT_INTENT_KEY_DEVICE_CLASS, Integer.toString(this.mClass));
            }
            if (this.mSubclass != -1) {
                serializer.attribute(null, "subclass", Integer.toString(this.mSubclass));
            }
            if (this.mProtocol != -1) {
                serializer.attribute(null, "protocol", Integer.toString(this.mProtocol));
            }
            if (this.mManufacturerName != null) {
                serializer.attribute(null, "manufacturer-name", this.mManufacturerName);
            }
            if (this.mProductName != null) {
                serializer.attribute(null, "product-name", this.mProductName);
            }
            if (this.mSerialNumber != null) {
                serializer.attribute(null, "serial-number", this.mSerialNumber);
            }
            serializer.endTag(null, "usb-device");
        }

        private boolean matches(int clasz, int subclass, int protocol) {
            if ((this.mClass == -1 || clasz == this.mClass) && (this.mSubclass == -1 || subclass == this.mSubclass)) {
                return (this.mProtocol == -1 || protocol == this.mProtocol) ? true : UsbSettingsManager.DEBUG;
            } else {
                return UsbSettingsManager.DEBUG;
            }
        }

        public boolean matches(UsbDevice device) {
            if (this.mVendorId != -1 && device.getVendorId() != this.mVendorId) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mProductId != -1 && device.getProductId() != this.mProductId) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mManufacturerName != null && device.getManufacturerName() == null) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mProductName != null && device.getProductName() == null) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mSerialNumber != null && device.getSerialNumber() == null) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mManufacturerName != null && device.getManufacturerName() != null && !this.mManufacturerName.equals(device.getManufacturerName())) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mProductName != null && device.getProductName() != null && !this.mProductName.equals(device.getProductName())) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mSerialNumber != null && device.getSerialNumber() != null && !this.mSerialNumber.equals(device.getSerialNumber())) {
                return UsbSettingsManager.DEBUG;
            }
            if (matches(device.getDeviceClass(), device.getDeviceSubclass(), device.getDeviceProtocol())) {
                return true;
            }
            int count = device.getInterfaceCount();
            for (int i = 0; i < count; i++) {
                UsbInterface intf = device.getInterface(i);
                if (matches(intf.getInterfaceClass(), intf.getInterfaceSubclass(), intf.getInterfaceProtocol())) {
                    return true;
                }
            }
            return UsbSettingsManager.DEBUG;
        }

        public boolean matches(DeviceFilter f) {
            if (this.mVendorId != -1 && f.mVendorId != this.mVendorId) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mProductId != -1 && f.mProductId != this.mProductId) {
                return UsbSettingsManager.DEBUG;
            }
            if (f.mManufacturerName != null && this.mManufacturerName == null) {
                return UsbSettingsManager.DEBUG;
            }
            if (f.mProductName != null && this.mProductName == null) {
                return UsbSettingsManager.DEBUG;
            }
            if (f.mSerialNumber != null && this.mSerialNumber == null) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mManufacturerName != null && f.mManufacturerName != null && !this.mManufacturerName.equals(f.mManufacturerName)) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mProductName != null && f.mProductName != null && !this.mProductName.equals(f.mProductName)) {
                return UsbSettingsManager.DEBUG;
            }
            if (this.mSerialNumber == null || f.mSerialNumber == null || this.mSerialNumber.equals(f.mSerialNumber)) {
                return matches(f.mClass, f.mSubclass, f.mProtocol);
            }
            return UsbSettingsManager.DEBUG;
        }

        public boolean equals(Object obj) {
            if (this.mVendorId == -1 || this.mProductId == -1 || this.mClass == -1 || this.mSubclass == -1 || this.mProtocol == -1) {
                return UsbSettingsManager.DEBUG;
            }
            if (obj instanceof DeviceFilter) {
                DeviceFilter filter = (DeviceFilter) obj;
                if (filter.mVendorId != this.mVendorId || filter.mProductId != this.mProductId || filter.mClass != this.mClass || filter.mSubclass != this.mSubclass || filter.mProtocol != this.mProtocol) {
                    return UsbSettingsManager.DEBUG;
                }
                if ((filter.mManufacturerName == null || this.mManufacturerName != null) && ((filter.mManufacturerName != null || this.mManufacturerName == null) && ((filter.mProductName == null || this.mProductName != null) && ((filter.mProductName != null || this.mProductName == null) && ((filter.mSerialNumber == null || this.mSerialNumber != null) && (filter.mSerialNumber != null || this.mSerialNumber == null)))))) {
                    return ((filter.mManufacturerName == null || this.mManufacturerName == null || this.mManufacturerName.equals(filter.mManufacturerName)) && ((filter.mProductName == null || this.mProductName == null || this.mProductName.equals(filter.mProductName)) && (filter.mSerialNumber == null || this.mSerialNumber == null || this.mSerialNumber.equals(filter.mSerialNumber)))) ? true : UsbSettingsManager.DEBUG;
                } else {
                    return UsbSettingsManager.DEBUG;
                }
            } else if (!(obj instanceof UsbDevice)) {
                return UsbSettingsManager.DEBUG;
            } else {
                UsbDevice device = (UsbDevice) obj;
                if (device.getVendorId() != this.mVendorId || device.getProductId() != this.mProductId || device.getDeviceClass() != this.mClass || device.getDeviceSubclass() != this.mSubclass || device.getDeviceProtocol() != this.mProtocol) {
                    return UsbSettingsManager.DEBUG;
                }
                if ((this.mManufacturerName == null || device.getManufacturerName() != null) && ((this.mManufacturerName != null || device.getManufacturerName() == null) && ((this.mProductName == null || device.getProductName() != null) && ((this.mProductName != null || device.getProductName() == null) && ((this.mSerialNumber == null || device.getSerialNumber() != null) && (this.mSerialNumber != null || device.getSerialNumber() == null)))))) {
                    return ((device.getManufacturerName() == null || this.mManufacturerName.equals(device.getManufacturerName())) && ((device.getProductName() == null || this.mProductName.equals(device.getProductName())) && (device.getSerialNumber() == null || this.mSerialNumber.equals(device.getSerialNumber())))) ? true : UsbSettingsManager.DEBUG;
                } else {
                    return UsbSettingsManager.DEBUG;
                }
            }
        }

        public int hashCode() {
            return ((this.mVendorId << 16) | this.mProductId) ^ (((this.mClass << 16) | (this.mSubclass << 8)) | this.mProtocol);
        }

        public String toString() {
            return "DeviceFilter[mVendorId=" + this.mVendorId + ",mProductId=" + this.mProductId + ",mClass=" + this.mClass + ",mSubclass=" + this.mSubclass + ",mProtocol=" + this.mProtocol + ",mManufacturerName=" + this.mManufacturerName + ",mProductName=" + this.mProductName + ",mSerialNumber=" + this.mSerialNumber + "]";
        }
    }

    private class MyPackageMonitor extends PackageMonitor {
        private MyPackageMonitor() {
        }

        public void onPackageAdded(String packageName, int uid) {
            UsbSettingsManager.this.handlePackageUpdate(packageName);
        }

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            UsbSettingsManager.this.handlePackageUpdate(packageName);
            return UsbSettingsManager.DEBUG;
        }

        public void onPackageRemoved(String packageName, int uid) {
            UsbSettingsManager.this.clearDefaults(packageName);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.usb.UsbSettingsManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.usb.UsbSettingsManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.usb.UsbSettingsManager.<clinit>():void");
    }

    private boolean handlePackageUpdateLocked(java.lang.String r10, android.content.pm.ActivityInfo r11, java.lang.String r12) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005b in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r9 = this;
        r4 = 0;
        r0 = 0;
        r6 = r9.mPackageManager;	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r4 = r11.loadXmlMetaData(r6, r12);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        if (r4 != 0) goto L_0x0011;
    L_0x000a:
        r6 = 0;
        if (r4 == 0) goto L_0x0010;
    L_0x000d:
        r4.close();
    L_0x0010:
        return r6;
    L_0x0011:
        com.android.internal.util.XmlUtils.nextElement(r4);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
    L_0x0014:
        r6 = r4.getEventType();	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r7 = 1;	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        if (r6 == r7) goto L_0x0071;	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
    L_0x001b:
        r5 = r4.getName();	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r6 = "usb-device";	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r6 = r6.equals(r5);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        if (r6 == 0) goto L_0x005c;	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
    L_0x0028:
        r3 = com.android.server.usb.UsbSettingsManager.DeviceFilter.read(r4);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r6 = r9.clearCompatibleMatchesLocked(r10, r3);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        if (r6 == 0) goto L_0x0033;	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
    L_0x0032:
        r0 = 1;	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
    L_0x0033:
        com.android.internal.util.XmlUtils.nextElement(r4);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        goto L_0x0014;
    L_0x0037:
        r1 = move-exception;
        r6 = "UsbSettingsManager";	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r7.<init>();	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r8 = "Unable to load component info ";	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r8 = r11.toString();	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r7 = r7.toString();	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        android.util.Slog.w(r6, r7, r1);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        if (r4 == 0) goto L_0x005b;
    L_0x0058:
        r4.close();
    L_0x005b:
        return r0;
    L_0x005c:
        r6 = "usb-accessory";	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r6 = r6.equals(r5);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        if (r6 == 0) goto L_0x0033;	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
    L_0x0065:
        r2 = com.android.server.usb.UsbSettingsManager.AccessoryFilter.read(r4);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        r6 = r9.clearCompatibleMatchesLocked(r10, r2);	 Catch:{ Exception -> 0x0037, all -> 0x0077 }
        if (r6 == 0) goto L_0x0033;
    L_0x006f:
        r0 = 1;
        goto L_0x0033;
    L_0x0071:
        if (r4 == 0) goto L_0x005b;
    L_0x0073:
        r4.close();
        goto L_0x005b;
    L_0x0077:
        r6 = move-exception;
        if (r4 == 0) goto L_0x007d;
    L_0x007a:
        r4.close();
    L_0x007d:
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.usb.UsbSettingsManager.handlePackageUpdateLocked(java.lang.String, android.content.pm.ActivityInfo, java.lang.String):boolean");
    }

    private boolean packageMatchesLocked(android.content.pm.ResolveInfo r11, java.lang.String r12, android.hardware.usb.UsbDevice r13, android.hardware.usb.UsbAccessory r14) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0098 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r10 = this;
        r9 = 0;
        r7 = 1;
        r0 = r11.activityInfo;
        r4 = 0;
        r6 = r10.mPackageManager;	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r4 = r0.loadXmlMetaData(r6, r12);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        if (r4 != 0) goto L_0x002d;	 Catch:{ Exception -> 0x0074, all -> 0x009f }
    L_0x000d:
        r6 = "UsbSettingsManager";	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7.<init>();	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r8 = "no meta-data for ";	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7 = r7.append(r11);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7 = r7.toString();	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        android.util.Slog.w(r6, r7);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        if (r4 == 0) goto L_0x002c;
    L_0x0029:
        r4.close();
    L_0x002c:
        return r9;
    L_0x002d:
        com.android.internal.util.XmlUtils.nextElement(r4);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
    L_0x0030:
        r6 = r4.getEventType();	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        if (r6 == r7) goto L_0x0099;	 Catch:{ Exception -> 0x0074, all -> 0x009f }
    L_0x0036:
        r5 = r4.getName();	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        if (r13 == 0) goto L_0x0055;	 Catch:{ Exception -> 0x0074, all -> 0x009f }
    L_0x003c:
        r6 = "usb-device";	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r6 = r6.equals(r5);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        if (r6 == 0) goto L_0x0055;	 Catch:{ Exception -> 0x0074, all -> 0x009f }
    L_0x0045:
        r3 = com.android.server.usb.UsbSettingsManager.DeviceFilter.read(r4);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r6 = r3.matches(r13);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        if (r6 == 0) goto L_0x0070;
    L_0x004f:
        if (r4 == 0) goto L_0x0054;
    L_0x0051:
        r4.close();
    L_0x0054:
        return r7;
    L_0x0055:
        if (r14 == 0) goto L_0x0070;
    L_0x0057:
        r6 = "usb-accessory";	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r6 = r6.equals(r5);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        if (r6 == 0) goto L_0x0070;	 Catch:{ Exception -> 0x0074, all -> 0x009f }
    L_0x0060:
        r2 = com.android.server.usb.UsbSettingsManager.AccessoryFilter.read(r4);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r6 = r2.matches(r14);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        if (r6 == 0) goto L_0x0070;
    L_0x006a:
        if (r4 == 0) goto L_0x006f;
    L_0x006c:
        r4.close();
    L_0x006f:
        return r7;
    L_0x0070:
        com.android.internal.util.XmlUtils.nextElement(r4);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        goto L_0x0030;
    L_0x0074:
        r1 = move-exception;
        r6 = "UsbSettingsManager";	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7.<init>();	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r8 = "Unable to load component info ";	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r8 = r11.toString();	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7 = r7.append(r8);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        r7 = r7.toString();	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        android.util.Slog.w(r6, r7, r1);	 Catch:{ Exception -> 0x0074, all -> 0x009f }
        if (r4 == 0) goto L_0x0098;
    L_0x0095:
        r4.close();
    L_0x0098:
        return r9;
    L_0x0099:
        if (r4 == 0) goto L_0x0098;
    L_0x009b:
        r4.close();
        goto L_0x0098;
    L_0x009f:
        r6 = move-exception;
        if (r4 == 0) goto L_0x00a5;
    L_0x00a2:
        r4.close();
    L_0x00a5:
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.usb.UsbSettingsManager.packageMatchesLocked(android.content.pm.ResolveInfo, java.lang.String, android.hardware.usb.UsbDevice, android.hardware.usb.UsbAccessory):boolean");
    }

    public UsbSettingsManager(Context context, UserHandle user) {
        this.mDevicePermissionMap = new HashMap();
        this.mAccessoryPermissionMap = new HashMap();
        this.mDevicePreferenceMap = new HashMap();
        this.mAccessoryPreferenceMap = new HashMap();
        this.mLock = new Object();
        this.mPackageMonitor = new MyPackageMonitor();
        try {
            this.mUserContext = context.createPackageContextAsUser("android", 0, user);
            this.mContext = context;
            this.mPackageManager = this.mUserContext.getPackageManager();
            this.mUser = user;
            this.mSettingsFile = new AtomicFile(new File(Environment.getUserSystemDirectory(user.getIdentifier()), "usb_device_manager.xml"));
            this.mDisablePermissionDialogs = context.getResources().getBoolean(17956984);
            synchronized (this.mLock) {
                if (UserHandle.SYSTEM.equals(user)) {
                    upgradeSingleUserLocked();
                }
                readSettingsLocked();
            }
            this.mPackageMonitor.register(this.mUserContext, null, true);
            this.mMtpNotificationManager = new MtpNotificationManager(context, new OnOpenInAppListener() {
                public void onOpenInApp(UsbDevice device) {
                    UsbSettingsManager.this.resolveActivity(UsbSettingsManager.createDeviceAttachedIntent(device), device);
                }
            });
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Missing android package");
        }
    }

    private void readPreference(XmlPullParser parser) throws XmlPullParserException, IOException {
        Object packageName = null;
        int count = parser.getAttributeCount();
        for (int i = 0; i < count; i++) {
            if (HwBroadcastRadarUtil.KEY_PACKAGE.equals(parser.getAttributeName(i))) {
                packageName = parser.getAttributeValue(i);
                break;
            }
        }
        XmlUtils.nextElement(parser);
        if ("usb-device".equals(parser.getName())) {
            this.mDevicePreferenceMap.put(DeviceFilter.read(parser), packageName);
        } else if ("usb-accessory".equals(parser.getName())) {
            this.mAccessoryPreferenceMap.put(AccessoryFilter.read(parser), packageName);
        }
        XmlUtils.nextElement(parser);
    }

    private void upgradeSingleUserLocked() {
        IOException e;
        XmlPullParserException e2;
        Throwable th;
        if (sSingleUserSettingsFile.exists()) {
            this.mDevicePreferenceMap.clear();
            this.mAccessoryPreferenceMap.clear();
            AutoCloseable autoCloseable = null;
            try {
                FileInputStream fis = new FileInputStream(sSingleUserSettingsFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fis, StandardCharsets.UTF_8.name());
                    XmlUtils.nextElement(parser);
                    while (parser.getEventType() != 1) {
                        if ("preference".equals(parser.getName())) {
                            readPreference(parser);
                        } else {
                            XmlUtils.nextElement(parser);
                        }
                    }
                    IoUtils.closeQuietly(fis);
                } catch (IOException e3) {
                    e = e3;
                    autoCloseable = fis;
                } catch (XmlPullParserException e4) {
                    e2 = e4;
                    autoCloseable = fis;
                } catch (Throwable th2) {
                    th = th2;
                    Object fis2 = fis;
                }
            } catch (IOException e5) {
                e = e5;
                try {
                    Log.wtf(TAG, "Failed to read single-user settings", e);
                    IoUtils.closeQuietly(autoCloseable);
                    writeSettingsLocked();
                    sSingleUserSettingsFile.delete();
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (XmlPullParserException e6) {
                e2 = e6;
                Log.wtf(TAG, "Failed to read single-user settings", e2);
                IoUtils.closeQuietly(autoCloseable);
                writeSettingsLocked();
                sSingleUserSettingsFile.delete();
            }
            writeSettingsLocked();
            sSingleUserSettingsFile.delete();
        }
    }

    private void readSettingsLocked() {
        this.mDevicePreferenceMap.clear();
        this.mAccessoryPreferenceMap.clear();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mSettingsFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            XmlUtils.nextElement(parser);
            while (parser.getEventType() != 1) {
                if ("preference".equals(parser.getName())) {
                    readPreference(parser);
                } else {
                    XmlUtils.nextElement(parser);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (Exception e2) {
            Slog.e(TAG, "error reading settings file, deleting to start fresh", e2);
            this.mSettingsFile.delete();
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private void writeSettingsLocked() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = this.mSettingsFile.startWrite();
            FastXmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "settings");
            for (DeviceFilter filter : this.mDevicePreferenceMap.keySet()) {
                serializer.startTag(null, "preference");
                serializer.attribute(null, HwBroadcastRadarUtil.KEY_PACKAGE, (String) this.mDevicePreferenceMap.get(filter));
                filter.write(serializer);
                serializer.endTag(null, "preference");
            }
            for (AccessoryFilter filter2 : this.mAccessoryPreferenceMap.keySet()) {
                serializer.startTag(null, "preference");
                serializer.attribute(null, HwBroadcastRadarUtil.KEY_PACKAGE, (String) this.mAccessoryPreferenceMap.get(filter2));
                filter2.write(serializer);
                serializer.endTag(null, "preference");
            }
            serializer.endTag(null, "settings");
            serializer.endDocument();
            this.mSettingsFile.finishWrite(fileOutputStream);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to write settings", e);
            if (fileOutputStream != null) {
                this.mSettingsFile.failWrite(fileOutputStream);
            }
        }
    }

    private final ArrayList<ResolveInfo> getDeviceMatchesLocked(UsbDevice device, Intent intent) {
        ArrayList<ResolveInfo> matches = new ArrayList();
        List<ResolveInfo> resolveInfos = this.mPackageManager.queryIntentActivities(intent, DumpState.DUMP_PACKAGES);
        int count = resolveInfos.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
            if (packageMatchesLocked(resolveInfo, intent.getAction(), device, null)) {
                matches.add(resolveInfo);
            }
        }
        return matches;
    }

    private final ArrayList<ResolveInfo> getAccessoryMatchesLocked(UsbAccessory accessory, Intent intent) {
        ArrayList<ResolveInfo> matches = new ArrayList();
        List<ResolveInfo> resolveInfos = this.mPackageManager.queryIntentActivities(intent, DumpState.DUMP_PACKAGES);
        int count = resolveInfos.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
            if (packageMatchesLocked(resolveInfo, intent.getAction(), null, accessory)) {
                matches.add(resolveInfo);
            }
        }
        return matches;
    }

    public void deviceAttached(UsbDevice device) {
        Intent intent = createDeviceAttachedIntent(device);
        this.mUserContext.sendBroadcast(intent);
        if (MtpNotificationManager.shouldShowNotification(this.mPackageManager, device)) {
            this.mMtpNotificationManager.showNotification(device);
        } else {
            resolveActivity(intent, device);
        }
    }

    private void resolveActivity(Intent intent, UsbDevice device) {
        ArrayList<ResolveInfo> matches;
        String defaultPackage;
        synchronized (this.mLock) {
            matches = getDeviceMatchesLocked(device, intent);
            defaultPackage = (String) this.mDevicePreferenceMap.get(new DeviceFilter(device));
        }
        resolveActivity(intent, matches, defaultPackage, device, null);
    }

    public void deviceDetached(UsbDevice device) {
        this.mDevicePermissionMap.remove(device.getDeviceName());
        Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_DETACHED");
        intent.putExtra("device", device);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        this.mMtpNotificationManager.hideNotification(device.getDeviceId());
    }

    public void accessoryAttached(UsbAccessory accessory) {
        ArrayList<ResolveInfo> matches;
        String defaultPackage;
        Intent intent = new Intent("android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
        intent.putExtra("accessory", accessory);
        intent.addFlags(268435456);
        synchronized (this.mLock) {
            matches = getAccessoryMatchesLocked(accessory, intent);
            defaultPackage = (String) this.mAccessoryPreferenceMap.get(new AccessoryFilter(accessory));
        }
        resolveActivity(intent, matches, defaultPackage, null, accessory);
    }

    public void accessoryDetached(UsbAccessory accessory) {
        this.mAccessoryPermissionMap.remove(accessory);
        Intent intent = new Intent("android.hardware.usb.action.USB_ACCESSORY_DETACHED");
        intent.putExtra("accessory", accessory);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void resolveActivity(Intent intent, ArrayList<ResolveInfo> matches, String defaultPackage, UsbDevice device, UsbAccessory accessory) {
        int count = matches.size();
        if (count == 0) {
            if (accessory != null) {
                String uri = accessory.getUri();
                if (uri != null && uri.length() > 0) {
                    Intent dialogIntent = new Intent();
                    dialogIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbAccessoryUriActivity");
                    dialogIntent.addFlags(268435456);
                    dialogIntent.putExtra("accessory", accessory);
                    dialogIntent.putExtra("uri", uri);
                    try {
                        this.mUserContext.startActivityAsUser(dialogIntent, this.mUser);
                    } catch (ActivityNotFoundException e) {
                        Slog.e(TAG, "unable to start UsbAccessoryUriActivity");
                    }
                }
            }
            return;
        }
        ResolveInfo rInfo;
        ResolveInfo resolveInfo = null;
        if (count == 1 && defaultPackage == null) {
            rInfo = (ResolveInfo) matches.get(0);
            if (!(rInfo.activityInfo == null || rInfo.activityInfo.applicationInfo == null || (rInfo.activityInfo.applicationInfo.flags & 1) == 0)) {
                resolveInfo = rInfo;
            }
            if (this.mDisablePermissionDialogs) {
                rInfo = (ResolveInfo) matches.get(0);
                if (rInfo.activityInfo != null) {
                    defaultPackage = rInfo.activityInfo.packageName;
                }
            }
        }
        if (resolveInfo == null && defaultPackage != null) {
            for (int i = 0; i < count; i++) {
                rInfo = (ResolveInfo) matches.get(i);
                if (rInfo.activityInfo != null && defaultPackage.equals(rInfo.activityInfo.packageName)) {
                    resolveInfo = rInfo;
                    break;
                }
            }
        }
        if (resolveInfo != null) {
            if (device != null) {
                grantDevicePermission(device, resolveInfo.activityInfo.applicationInfo.uid);
            } else if (accessory != null) {
                grantAccessoryPermission(accessory, resolveInfo.activityInfo.applicationInfo.uid);
            }
            try {
                intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                this.mUserContext.startActivityAsUser(intent, this.mUser);
            } catch (ActivityNotFoundException e2) {
                Slog.e(TAG, "startActivity failed", e2);
            }
        } else {
            Intent resolverIntent = new Intent();
            resolverIntent.addFlags(268435456);
            if (count == 1) {
                resolverIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbConfirmActivity");
                resolverIntent.putExtra("rinfo", (Parcelable) matches.get(0));
                if (device != null) {
                    resolverIntent.putExtra("device", device);
                } else {
                    resolverIntent.putExtra("accessory", accessory);
                }
            } else {
                resolverIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbResolverActivity");
                resolverIntent.putParcelableArrayListExtra("rlist", matches);
                resolverIntent.putExtra("android.intent.extra.INTENT", intent);
            }
            try {
                this.mUserContext.startActivityAsUser(resolverIntent, this.mUser);
            } catch (ActivityNotFoundException e3) {
                Slog.e(TAG, "unable to start activity " + resolverIntent);
            }
        }
    }

    private boolean clearCompatibleMatchesLocked(String packageName, DeviceFilter filter) {
        boolean changed = DEBUG;
        for (DeviceFilter test : this.mDevicePreferenceMap.keySet()) {
            if (filter.matches(test)) {
                this.mDevicePreferenceMap.remove(test);
                changed = true;
            }
        }
        return changed;
    }

    private boolean clearCompatibleMatchesLocked(String packageName, AccessoryFilter filter) {
        boolean changed = DEBUG;
        for (AccessoryFilter test : this.mAccessoryPreferenceMap.keySet()) {
            if (filter.matches(test)) {
                this.mAccessoryPreferenceMap.remove(test);
                changed = true;
            }
        }
        return changed;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handlePackageUpdate(String packageName) {
        synchronized (this.mLock) {
            boolean changed = DEBUG;
            try {
                ActivityInfo[] activities = this.mPackageManager.getPackageInfo(packageName, 129).activities;
                if (activities == null) {
                    return;
                }
                int i = 0;
                while (true) {
                    if (i >= activities.length) {
                        break;
                    }
                    if (handlePackageUpdateLocked(packageName, activities[i], "android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                        changed = true;
                    }
                    if (handlePackageUpdateLocked(packageName, activities[i], "android.hardware.usb.action.USB_ACCESSORY_ATTACHED")) {
                        changed = true;
                    }
                    i++;
                }
                if (changed) {
                    writeSettingsLocked();
                }
            } catch (NameNotFoundException e) {
                Slog.e(TAG, "handlePackageUpdate could not find package " + packageName, e);
            }
        }
    }

    public boolean hasPermission(UsbDevice device) {
        synchronized (this.mLock) {
            int uid = Binder.getCallingUid();
            if (uid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || this.mDisablePermissionDialogs) {
                return true;
            }
            SparseBooleanArray uidList = (SparseBooleanArray) this.mDevicePermissionMap.get(device.getDeviceName());
            if (uidList == null) {
                return DEBUG;
            }
            boolean z = uidList.get(uid);
            return z;
        }
    }

    public boolean hasPermission(UsbAccessory accessory) {
        synchronized (this.mLock) {
            int uid = Binder.getCallingUid();
            if (uid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || this.mDisablePermissionDialogs) {
                return true;
            }
            SparseBooleanArray uidList = (SparseBooleanArray) this.mAccessoryPermissionMap.get(accessory);
            if (uidList == null) {
                return DEBUG;
            }
            boolean z = uidList.get(uid);
            return z;
        }
    }

    public void checkPermission(UsbDevice device) {
        if (!hasPermission(device)) {
            throw new SecurityException("User has not given permission to device " + device);
        }
    }

    public void checkPermission(UsbAccessory accessory) {
        if (!hasPermission(accessory)) {
            throw new SecurityException("User has not given permission to accessory " + accessory);
        }
    }

    private void requestPermissionDialog(Intent intent, String packageName, PendingIntent pi) {
        int uid = Binder.getCallingUid();
        try {
            if (this.mPackageManager.getApplicationInfo(packageName, 0).uid != uid) {
                throw new IllegalArgumentException("package " + packageName + " does not match caller's uid " + uid);
            }
            long identity = Binder.clearCallingIdentity();
            intent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbPermissionActivity");
            intent.addFlags(268435456);
            intent.putExtra("android.intent.extra.INTENT", pi);
            intent.putExtra(HwBroadcastRadarUtil.KEY_PACKAGE, packageName);
            intent.putExtra("android.intent.extra.UID", uid);
            try {
                this.mUserContext.startActivityAsUser(intent, this.mUser);
            } catch (ActivityNotFoundException e) {
                Slog.e(TAG, "unable to start UsbPermissionActivity");
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } catch (NameNotFoundException e2) {
            throw new IllegalArgumentException("package " + packageName + " not found");
        }
    }

    public void requestPermission(UsbDevice device, String packageName, PendingIntent pi) {
        Intent intent = new Intent();
        if (hasPermission(device)) {
            intent.putExtra("device", device);
            intent.putExtra("permission", true);
            try {
                pi.send(this.mUserContext, 0, intent);
            } catch (CanceledException e) {
            }
            return;
        }
        intent.putExtra("device", device);
        requestPermissionDialog(intent, packageName, pi);
    }

    public void requestPermission(UsbAccessory accessory, String packageName, PendingIntent pi) {
        Intent intent = new Intent();
        if (hasPermission(accessory)) {
            intent.putExtra("accessory", accessory);
            intent.putExtra("permission", true);
            try {
                pi.send(this.mUserContext, 0, intent);
            } catch (CanceledException e) {
            }
            return;
        }
        intent.putExtra("accessory", accessory);
        requestPermissionDialog(intent, packageName, pi);
    }

    public void setDevicePackage(UsbDevice device, String packageName) {
        DeviceFilter filter = new DeviceFilter(device);
        synchronized (this.mLock) {
            boolean changed;
            if (packageName == null) {
                changed = this.mDevicePreferenceMap.remove(filter) != null ? true : DEBUG;
            } else {
                changed = packageName.equals(this.mDevicePreferenceMap.get(filter)) ? DEBUG : true;
                if (changed) {
                    this.mDevicePreferenceMap.put(filter, packageName);
                }
            }
            if (changed) {
                writeSettingsLocked();
            }
        }
    }

    public void setAccessoryPackage(UsbAccessory accessory, String packageName) {
        AccessoryFilter filter = new AccessoryFilter(accessory);
        synchronized (this.mLock) {
            boolean changed;
            if (packageName == null) {
                changed = this.mAccessoryPreferenceMap.remove(filter) != null ? true : DEBUG;
            } else {
                changed = packageName.equals(this.mAccessoryPreferenceMap.get(filter)) ? DEBUG : true;
                if (changed) {
                    this.mAccessoryPreferenceMap.put(filter, packageName);
                }
            }
            if (changed) {
                writeSettingsLocked();
            }
        }
    }

    public void grantDevicePermission(UsbDevice device, int uid) {
        synchronized (this.mLock) {
            String deviceName = device.getDeviceName();
            SparseBooleanArray uidList = (SparseBooleanArray) this.mDevicePermissionMap.get(deviceName);
            if (uidList == null) {
                uidList = new SparseBooleanArray(1);
                this.mDevicePermissionMap.put(deviceName, uidList);
            }
            uidList.put(uid, true);
        }
    }

    public void grantAccessoryPermission(UsbAccessory accessory, int uid) {
        synchronized (this.mLock) {
            SparseBooleanArray uidList = (SparseBooleanArray) this.mAccessoryPermissionMap.get(accessory);
            if (uidList == null) {
                uidList = new SparseBooleanArray(1);
                this.mAccessoryPermissionMap.put(accessory, uidList);
            }
            uidList.put(uid, true);
        }
    }

    public boolean hasDefaults(String packageName) {
        synchronized (this.mLock) {
            if (this.mDevicePreferenceMap.values().contains(packageName)) {
                return true;
            } else if (this.mAccessoryPreferenceMap.values().contains(packageName)) {
                return true;
            } else {
                return DEBUG;
            }
        }
    }

    public void clearDefaults(String packageName) {
        synchronized (this.mLock) {
            if (clearPackageDefaultsLocked(packageName)) {
                writeSettingsLocked();
            }
        }
    }

    private boolean clearPackageDefaultsLocked(String packageName) {
        boolean cleared = DEBUG;
        synchronized (this.mLock) {
            Object[] keys;
            if (this.mDevicePreferenceMap.containsValue(packageName)) {
                keys = this.mDevicePreferenceMap.keySet().toArray();
                for (Object key : keys) {
                    if (packageName.equals(this.mDevicePreferenceMap.get(key))) {
                        this.mDevicePreferenceMap.remove(key);
                        cleared = true;
                    }
                }
            }
            if (this.mAccessoryPreferenceMap.containsValue(packageName)) {
                keys = this.mAccessoryPreferenceMap.keySet().toArray();
                for (Object key2 : keys) {
                    if (packageName.equals(this.mAccessoryPreferenceMap.get(key2))) {
                        this.mAccessoryPreferenceMap.remove(key2);
                        cleared = true;
                    }
                }
            }
        }
        return cleared;
    }

    public void dump(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("Device permissions:");
            for (String deviceName : this.mDevicePermissionMap.keySet()) {
                int i;
                pw.print("  " + deviceName + ": ");
                SparseBooleanArray uidList = (SparseBooleanArray) this.mDevicePermissionMap.get(deviceName);
                int count = uidList.size();
                for (i = 0; i < count; i++) {
                    pw.print(Integer.toString(uidList.keyAt(i)) + " ");
                }
                pw.println();
            }
            pw.println("Accessory permissions:");
            for (UsbAccessory accessory : this.mAccessoryPermissionMap.keySet()) {
                pw.print("  " + accessory + ": ");
                uidList = (SparseBooleanArray) this.mAccessoryPermissionMap.get(accessory);
                count = uidList.size();
                for (i = 0; i < count; i++) {
                    pw.print(Integer.toString(uidList.keyAt(i)) + " ");
                }
                pw.println();
            }
            pw.println("Device preferences:");
            for (DeviceFilter filter : this.mDevicePreferenceMap.keySet()) {
                pw.println("  " + filter + ": " + ((String) this.mDevicePreferenceMap.get(filter)));
            }
            pw.println("Accessory preferences:");
            for (AccessoryFilter filter2 : this.mAccessoryPreferenceMap.keySet()) {
                pw.println("  " + filter2 + ": " + ((String) this.mAccessoryPreferenceMap.get(filter2)));
            }
        }
    }

    private static Intent createDeviceAttachedIntent(UsbDevice device) {
        Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intent.putExtra("device", device);
        intent.addFlags(268435456);
        return intent;
    }
}
