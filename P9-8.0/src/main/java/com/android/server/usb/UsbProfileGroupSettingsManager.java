package com.android.server.usb;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.XmlResourceParser;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.Immutable;
import com.android.internal.app.IntentForwarderActivity;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.XmlUtils;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.audio.AudioService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class UsbProfileGroupSettingsManager {
    private static final boolean DEBUG = false;
    private static final String TAG = UsbProfileGroupSettingsManager.class.getSimpleName();
    private static final File sSingleUserSettingsFile = new File("/data/system/usb_device_manager.xml");
    @GuardedBy("mLock")
    private final HashMap<AccessoryFilter, UserPackage> mAccessoryPreferenceMap = new HashMap();
    private final Context mContext;
    @GuardedBy("mLock")
    private final HashMap<DeviceFilter, UserPackage> mDevicePreferenceMap = new HashMap();
    private final boolean mDisablePermissionDialogs;
    @GuardedBy("mLock")
    private boolean mIsWriteSettingsScheduled;
    private final Object mLock = new Object();
    private final MtpNotificationManager mMtpNotificationManager;
    private final PackageManager mPackageManager;
    MyPackageMonitor mPackageMonitor = new MyPackageMonitor(this, null);
    private final UserHandle mParentUser;
    private final AtomicFile mSettingsFile;
    private final UsbSettingsManager mSettingsManager;
    private final UserManager mUserManager;

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
            if (this.mManufacturer != null && (acc.getManufacturer().equals(this.mManufacturer) ^ 1) != 0) {
                return false;
            }
            if (this.mModel != null && (acc.getModel().equals(this.mModel) ^ 1) != 0) {
                return false;
            }
            if (this.mVersion == null || (acc.getVersion().equals(this.mVersion) ^ 1) == 0) {
                return true;
            }
            return false;
        }

        public boolean contains(AccessoryFilter accessory) {
            if (this.mManufacturer != null && (Objects.equals(accessory.mManufacturer, this.mManufacturer) ^ 1) != 0) {
                return false;
            }
            if (this.mModel != null && (Objects.equals(accessory.mModel, this.mModel) ^ 1) != 0) {
                return false;
            }
            if (this.mVersion == null || (Objects.equals(accessory.mVersion, this.mVersion) ^ 1) == 0) {
                return true;
            }
            return false;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (this.mManufacturer == null || this.mModel == null || this.mVersion == null) {
                return false;
            }
            if (obj instanceof AccessoryFilter) {
                AccessoryFilter filter = (AccessoryFilter) obj;
                if (this.mManufacturer.equals(filter.mManufacturer) && this.mModel.equals(filter.mModel)) {
                    z = this.mVersion.equals(filter.mVersion);
                }
                return z;
            } else if (!(obj instanceof UsbAccessory)) {
                return false;
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
                        Slog.e(UsbProfileGroupSettingsManager.TAG, "invalid number for field " + name, e);
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
                return this.mProtocol == -1 || protocol == this.mProtocol;
            } else {
                return false;
            }
        }

        public boolean matches(UsbDevice device) {
            if (this.mVendorId != -1 && device.getVendorId() != this.mVendorId) {
                return false;
            }
            if (this.mProductId != -1 && device.getProductId() != this.mProductId) {
                return false;
            }
            if (this.mManufacturerName != null && device.getManufacturerName() == null) {
                return false;
            }
            if (this.mProductName != null && device.getProductName() == null) {
                return false;
            }
            if (this.mSerialNumber != null && device.getSerialNumber() == null) {
                return false;
            }
            if (this.mManufacturerName != null && device.getManufacturerName() != null && (this.mManufacturerName.equals(device.getManufacturerName()) ^ 1) != 0) {
                return false;
            }
            if (this.mProductName != null && device.getProductName() != null && (this.mProductName.equals(device.getProductName()) ^ 1) != 0) {
                return false;
            }
            if (this.mSerialNumber != null && device.getSerialNumber() != null && (this.mSerialNumber.equals(device.getSerialNumber()) ^ 1) != 0) {
                return false;
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
            return false;
        }

        public boolean contains(DeviceFilter device) {
            if (this.mVendorId != -1 && device.mVendorId != this.mVendorId) {
                return false;
            }
            if (this.mProductId != -1 && device.mProductId != this.mProductId) {
                return false;
            }
            if (this.mManufacturerName != null && (Objects.equals(this.mManufacturerName, device.mManufacturerName) ^ 1) != 0) {
                return false;
            }
            if (this.mProductName != null && (Objects.equals(this.mProductName, device.mProductName) ^ 1) != 0) {
                return false;
            }
            if (this.mSerialNumber == null || (Objects.equals(this.mSerialNumber, device.mSerialNumber) ^ 1) == 0) {
                return matches(device.mClass, device.mSubclass, device.mProtocol);
            }
            return false;
        }

        public boolean equals(Object obj) {
            if (this.mVendorId == -1 || this.mProductId == -1 || this.mClass == -1 || this.mSubclass == -1 || this.mProtocol == -1) {
                return false;
            }
            if (obj instanceof DeviceFilter) {
                DeviceFilter filter = (DeviceFilter) obj;
                if (filter.mVendorId != this.mVendorId || filter.mProductId != this.mProductId || filter.mClass != this.mClass || filter.mSubclass != this.mSubclass || filter.mProtocol != this.mProtocol) {
                    return false;
                }
                if ((filter.mManufacturerName == null || this.mManufacturerName != null) && ((filter.mManufacturerName != null || this.mManufacturerName == null) && ((filter.mProductName == null || this.mProductName != null) && ((filter.mProductName != null || this.mProductName == null) && ((filter.mSerialNumber == null || this.mSerialNumber != null) && (filter.mSerialNumber != null || this.mSerialNumber == null)))))) {
                    return (filter.mManufacturerName == null || this.mManufacturerName == null || (this.mManufacturerName.equals(filter.mManufacturerName) ^ 1) == 0) && ((filter.mProductName == null || this.mProductName == null || (this.mProductName.equals(filter.mProductName) ^ 1) == 0) && (filter.mSerialNumber == null || this.mSerialNumber == null || (this.mSerialNumber.equals(filter.mSerialNumber) ^ 1) == 0));
                } else {
                    return false;
                }
            } else if (!(obj instanceof UsbDevice)) {
                return false;
            } else {
                UsbDevice device = (UsbDevice) obj;
                if (device.getVendorId() != this.mVendorId || device.getProductId() != this.mProductId || device.getDeviceClass() != this.mClass || device.getDeviceSubclass() != this.mSubclass || device.getDeviceProtocol() != this.mProtocol) {
                    return false;
                }
                if ((this.mManufacturerName == null || device.getManufacturerName() != null) && ((this.mManufacturerName != null || device.getManufacturerName() == null) && ((this.mProductName == null || device.getProductName() != null) && ((this.mProductName != null || device.getProductName() == null) && ((this.mSerialNumber == null || device.getSerialNumber() != null) && (this.mSerialNumber != null || device.getSerialNumber() == null)))))) {
                    return (device.getManufacturerName() == null || (this.mManufacturerName.equals(device.getManufacturerName()) ^ 1) == 0) && ((device.getProductName() == null || (this.mProductName.equals(device.getProductName()) ^ 1) == 0) && (device.getSerialNumber() == null || (this.mSerialNumber.equals(device.getSerialNumber()) ^ 1) == 0));
                } else {
                    return false;
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
        /* synthetic */ MyPackageMonitor(UsbProfileGroupSettingsManager this$0, MyPackageMonitor -this1) {
            this();
        }

        private MyPackageMonitor() {
        }

        public void onPackageAdded(String packageName, int uid) {
            if (UsbProfileGroupSettingsManager.this.mUserManager.isSameProfileGroup(UsbProfileGroupSettingsManager.this.mParentUser.getIdentifier(), UserHandle.getUserId(uid))) {
                UsbProfileGroupSettingsManager.this.handlePackageAdded(new UserPackage(packageName, UserHandle.getUserHandleForUid(uid), null));
            }
        }

        public void onPackageRemoved(String packageName, int uid) {
            if (UsbProfileGroupSettingsManager.this.mUserManager.isSameProfileGroup(UsbProfileGroupSettingsManager.this.mParentUser.getIdentifier(), UserHandle.getUserId(uid))) {
                UsbProfileGroupSettingsManager.this.clearDefaults(packageName, UserHandle.getUserHandleForUid(uid));
            }
        }
    }

    @Immutable
    private static class UserPackage {
        final String packageName;
        final UserHandle user;

        /* synthetic */ UserPackage(String packageName, UserHandle user, UserPackage -this2) {
            this(packageName, user);
        }

        private UserPackage(String packageName, UserHandle user) {
            this.packageName = packageName;
            this.user = user;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof UserPackage)) {
                return false;
            }
            UserPackage other = (UserPackage) obj;
            if (this.user.equals(other.user)) {
                z = this.packageName.equals(other.packageName);
            }
            return z;
        }

        public int hashCode() {
            return (this.user.hashCode() * 31) + this.packageName.hashCode();
        }

        public String toString() {
            return this.user.getIdentifier() + "/" + this.packageName;
        }
    }

    UsbProfileGroupSettingsManager(Context context, UserHandle user, UsbSettingsManager settingsManager) {
        try {
            Context parentUserContext = context.createPackageContextAsUser("android", 0, user);
            this.mContext = context;
            this.mPackageManager = context.getPackageManager();
            this.mSettingsManager = settingsManager;
            this.mUserManager = (UserManager) context.getSystemService("user");
            this.mParentUser = user;
            this.mSettingsFile = new AtomicFile(new File(Environment.getUserSystemDirectory(user.getIdentifier()), "usb_device_manager.xml"));
            this.mDisablePermissionDialogs = context.getResources().getBoolean(17956927);
            synchronized (this.mLock) {
                if (UserHandle.SYSTEM.equals(user)) {
                    upgradeSingleUserLocked();
                }
                readSettingsLocked();
            }
            this.mPackageMonitor.register(context, null, UserHandle.ALL, true);
            this.mMtpNotificationManager = new MtpNotificationManager(parentUserContext, new OnOpenInAppListener() {
                public void onOpenInApp(UsbDevice device) {
                    UsbProfileGroupSettingsManager.this.resolveActivity(UsbProfileGroupSettingsManager.createDeviceAttachedIntent(device), device, false);
                }
            });
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Missing android package");
        }
    }

    void removeAllDefaultsForUser(UserHandle userToRemove) {
        synchronized (this.mLock) {
            boolean needToPersist = false;
            Iterator<Entry<DeviceFilter, UserPackage>> devicePreferenceIt = this.mDevicePreferenceMap.entrySet().iterator();
            while (devicePreferenceIt.hasNext()) {
                if (((UserPackage) ((Entry) devicePreferenceIt.next()).getValue()).user.equals(userToRemove)) {
                    devicePreferenceIt.remove();
                    needToPersist = true;
                }
            }
            Iterator<Entry<AccessoryFilter, UserPackage>> accessoryPreferenceIt = this.mAccessoryPreferenceMap.entrySet().iterator();
            while (accessoryPreferenceIt.hasNext()) {
                if (((UserPackage) ((Entry) accessoryPreferenceIt.next()).getValue()).user.equals(userToRemove)) {
                    accessoryPreferenceIt.remove();
                    needToPersist = true;
                }
            }
            if (needToPersist) {
                scheduleWriteSettingsLocked();
            }
        }
    }

    private void readPreference(XmlPullParser parser) throws XmlPullParserException, IOException {
        String packageName = null;
        UserHandle user = this.mParentUser;
        int count = parser.getAttributeCount();
        for (int i = 0; i < count; i++) {
            if (HwBroadcastRadarUtil.KEY_PACKAGE.equals(parser.getAttributeName(i))) {
                packageName = parser.getAttributeValue(i);
            }
            if ("user".equals(parser.getAttributeName(i))) {
                user = this.mUserManager.getUserForSerialNumber((long) Integer.parseInt(parser.getAttributeValue(i)));
            }
        }
        XmlUtils.nextElement(parser);
        if ("usb-device".equals(parser.getName())) {
            DeviceFilter filter = DeviceFilter.read(parser);
            if (user != null) {
                this.mDevicePreferenceMap.put(filter, new UserPackage(packageName, user, null));
            }
        } else if ("usb-accessory".equals(parser.getName())) {
            AccessoryFilter filter2 = AccessoryFilter.read(parser);
            if (user != null) {
                this.mAccessoryPreferenceMap.put(filter2, new UserPackage(packageName, user, null));
            }
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
            AutoCloseable fis = null;
            try {
                FileInputStream fis2 = new FileInputStream(sSingleUserSettingsFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fis2, StandardCharsets.UTF_8.name());
                    XmlUtils.nextElement(parser);
                    while (parser.getEventType() != 1) {
                        if ("preference".equals(parser.getName())) {
                            readPreference(parser);
                        } else {
                            XmlUtils.nextElement(parser);
                        }
                    }
                    IoUtils.closeQuietly(fis2);
                } catch (IOException e3) {
                    e = e3;
                    fis = fis2;
                } catch (XmlPullParserException e4) {
                    e2 = e4;
                    fis = fis2;
                } catch (Throwable th2) {
                    th = th2;
                    Object fis3 = fis2;
                }
            } catch (IOException e5) {
                e = e5;
                try {
                    Log.wtf(TAG, "Failed to read single-user settings", e);
                    IoUtils.closeQuietly(fis);
                    scheduleWriteSettingsLocked();
                    sSingleUserSettingsFile.delete();
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(fis);
                    throw th;
                }
            } catch (XmlPullParserException e6) {
                e2 = e6;
                Log.wtf(TAG, "Failed to read single-user settings", e2);
                IoUtils.closeQuietly(fis);
                scheduleWriteSettingsLocked();
                sSingleUserSettingsFile.delete();
            }
            scheduleWriteSettingsLocked();
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

    private void scheduleWriteSettingsLocked() {
        if (!this.mIsWriteSettingsScheduled) {
            this.mIsWriteSettingsScheduled = true;
            AsyncTask.execute(new -$Lambda$FKfu8uuaZxaSOkvg1oeLD5zHuwk(this));
        }
    }

    /* synthetic */ void lambda$-com_android_server_usb_UsbProfileGroupSettingsManager_31787() {
        synchronized (this.mLock) {
            FileOutputStream fos = null;
            try {
                fos = this.mSettingsFile.startWrite();
                FastXmlSerializer serializer = new FastXmlSerializer();
                serializer.setOutput(fos, StandardCharsets.UTF_8.name());
                serializer.startDocument(null, Boolean.valueOf(true));
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, "settings");
                for (DeviceFilter filter : this.mDevicePreferenceMap.keySet()) {
                    serializer.startTag(null, "preference");
                    serializer.attribute(null, HwBroadcastRadarUtil.KEY_PACKAGE, ((UserPackage) this.mDevicePreferenceMap.get(filter)).packageName);
                    serializer.attribute(null, "user", String.valueOf(getSerial(((UserPackage) this.mDevicePreferenceMap.get(filter)).user)));
                    filter.write(serializer);
                    serializer.endTag(null, "preference");
                }
                for (AccessoryFilter filter2 : this.mAccessoryPreferenceMap.keySet()) {
                    serializer.startTag(null, "preference");
                    serializer.attribute(null, HwBroadcastRadarUtil.KEY_PACKAGE, ((UserPackage) this.mAccessoryPreferenceMap.get(filter2)).packageName);
                    serializer.attribute(null, "user", String.valueOf(getSerial(((UserPackage) this.mAccessoryPreferenceMap.get(filter2)).user)));
                    filter2.write(serializer);
                    serializer.endTag(null, "preference");
                }
                serializer.endTag(null, "settings");
                serializer.endDocument();
                this.mSettingsFile.finishWrite(fos);
            } catch (IOException e) {
                Slog.e(TAG, "Failed to write settings", e);
                if (fos != null) {
                    this.mSettingsFile.failWrite(fos);
                }
            }
            this.mIsWriteSettingsScheduled = false;
        }
    }

    private boolean packageMatchesLocked(ResolveInfo info, String metaDataName, UsbDevice device, UsbAccessory accessory) {
        if (isForwardMatch(info)) {
            return true;
        }
        XmlResourceParser parser = null;
        try {
            parser = info.activityInfo.loadXmlMetaData(this.mPackageManager, metaDataName);
            if (parser == null) {
                Slog.w(TAG, "no meta-data for " + info);
                if (parser != null) {
                    parser.close();
                }
                return false;
            }
            XmlUtils.nextElement(parser);
            while (parser.getEventType() != 1) {
                String tagName = parser.getName();
                if (device == null || !"usb-device".equals(tagName)) {
                    if (accessory != null) {
                        if ("usb-accessory".equals(tagName) && AccessoryFilter.read(parser).matches(accessory)) {
                            if (parser != null) {
                                parser.close();
                            }
                            return true;
                        }
                    }
                } else if (DeviceFilter.read(parser).matches(device)) {
                    if (parser != null) {
                        parser.close();
                    }
                    return true;
                }
                XmlUtils.nextElement(parser);
            }
            if (parser != null) {
                parser.close();
            }
            return false;
        } catch (Exception e) {
            Slog.w(TAG, "Unable to load component info " + info.toString(), e);
            if (parser != null) {
                parser.close();
            }
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    private ArrayList<ResolveInfo> queryIntentActivitiesForAllProfiles(Intent intent) {
        List<UserInfo> profiles = this.mUserManager.getEnabledProfiles(this.mParentUser.getIdentifier());
        ArrayList<ResolveInfo> resolveInfos = new ArrayList();
        int numProfiles = profiles.size();
        for (int i = 0; i < numProfiles; i++) {
            resolveInfos.addAll(this.mPackageManager.queryIntentActivitiesAsUser(intent, 128, ((UserInfo) profiles.get(i)).id));
        }
        return resolveInfos;
    }

    private boolean isForwardMatch(ResolveInfo match) {
        return match.getComponentInfo().name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_MANAGED_PROFILE);
    }

    private ArrayList<ResolveInfo> preferHighPriority(ArrayList<ResolveInfo> matches) {
        SparseArray<ArrayList<ResolveInfo>> highestPriorityMatchesByUserId = new SparseArray();
        SparseIntArray highestPriorityByUserId = new SparseIntArray();
        ArrayList<ResolveInfo> forwardMatches = new ArrayList();
        int numMatches = matches.size();
        for (int matchNum = 0; matchNum < numMatches; matchNum++) {
            ResolveInfo match = (ResolveInfo) matches.get(matchNum);
            if (isForwardMatch(match)) {
                forwardMatches.add(match);
            } else {
                if (highestPriorityByUserId.indexOfKey(match.targetUserId) < 0) {
                    highestPriorityByUserId.put(match.targetUserId, Integer.MIN_VALUE);
                    highestPriorityMatchesByUserId.put(match.targetUserId, new ArrayList());
                }
                int highestPriority = highestPriorityByUserId.get(match.targetUserId);
                ArrayList<ResolveInfo> highestPriorityMatches = (ArrayList) highestPriorityMatchesByUserId.get(match.targetUserId);
                if (match.priority == highestPriority) {
                    highestPriorityMatches.add(match);
                } else if (match.priority > highestPriority) {
                    highestPriorityByUserId.put(match.targetUserId, match.priority);
                    highestPriorityMatches.clear();
                    highestPriorityMatches.add(match);
                }
            }
        }
        ArrayList<ResolveInfo> combinedMatches = new ArrayList(forwardMatches);
        int numMatchArrays = highestPriorityMatchesByUserId.size();
        for (int matchArrayNum = 0; matchArrayNum < numMatchArrays; matchArrayNum++) {
            combinedMatches.addAll((Collection) highestPriorityMatchesByUserId.valueAt(matchArrayNum));
        }
        return combinedMatches;
    }

    private ArrayList<ResolveInfo> removeForwardIntentIfNotNeeded(ArrayList<ResolveInfo> rawMatches) {
        int i;
        ResolveInfo rawMatch;
        int numRawMatches = rawMatches.size();
        int numParentActivityMatches = 0;
        int numNonParentActivityMatches = 0;
        for (i = 0; i < numRawMatches; i++) {
            rawMatch = (ResolveInfo) rawMatches.get(i);
            if (!isForwardMatch(rawMatch)) {
                if (UserHandle.getUserHandleForUid(rawMatch.activityInfo.applicationInfo.uid).equals(this.mParentUser)) {
                    numParentActivityMatches++;
                } else {
                    numNonParentActivityMatches++;
                }
            }
        }
        if (numParentActivityMatches != 0 && numNonParentActivityMatches != 0) {
            return rawMatches;
        }
        ArrayList<ResolveInfo> matches = new ArrayList(numParentActivityMatches + numNonParentActivityMatches);
        for (i = 0; i < numRawMatches; i++) {
            rawMatch = (ResolveInfo) rawMatches.get(i);
            if (!isForwardMatch(rawMatch)) {
                matches.add(rawMatch);
            }
        }
        return matches;
    }

    private final ArrayList<ResolveInfo> getDeviceMatchesLocked(UsbDevice device, Intent intent) {
        ArrayList<ResolveInfo> matches = new ArrayList();
        List<ResolveInfo> resolveInfos = queryIntentActivitiesForAllProfiles(intent);
        int count = resolveInfos.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
            if (packageMatchesLocked(resolveInfo, intent.getAction(), device, null)) {
                matches.add(resolveInfo);
            }
        }
        return removeForwardIntentIfNotNeeded(preferHighPriority(matches));
    }

    private final ArrayList<ResolveInfo> getAccessoryMatchesLocked(UsbAccessory accessory, Intent intent) {
        ArrayList<ResolveInfo> matches = new ArrayList();
        List<ResolveInfo> resolveInfos = queryIntentActivitiesForAllProfiles(intent);
        int count = resolveInfos.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
            if (packageMatchesLocked(resolveInfo, intent.getAction(), null, accessory)) {
                matches.add(resolveInfo);
            }
        }
        return removeForwardIntentIfNotNeeded(preferHighPriority(matches));
    }

    public void deviceAttached(UsbDevice device) {
        Intent intent = createDeviceAttachedIntent(device);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        resolveActivity(intent, device, true);
    }

    private void resolveActivity(Intent intent, UsbDevice device, boolean showMtpNotification) {
        ArrayList<ResolveInfo> matches;
        ActivityInfo defaultActivity;
        synchronized (this.mLock) {
            matches = getDeviceMatchesLocked(device, intent);
            defaultActivity = getDefaultActivityLocked(matches, (UserPackage) this.mDevicePreferenceMap.get(new DeviceFilter(device)));
        }
        if (showMtpNotification && MtpNotificationManager.shouldShowNotification(this.mPackageManager, device) && defaultActivity == null) {
            this.mMtpNotificationManager.showNotification(device);
        } else {
            resolveActivity(intent, matches, defaultActivity, device, null);
        }
    }

    public void deviceAttachedForFixedHandler(UsbDevice device, ComponentName component) {
        Intent intent = createDeviceAttachedIntent(device);
        this.mContext.sendBroadcast(intent);
        try {
            ApplicationInfo appInfo = this.mPackageManager.getApplicationInfoAsUser(component.getPackageName(), 0, this.mParentUser.getIdentifier());
            this.mSettingsManager.getSettingsForUser(UserHandle.getUserId(appInfo.uid)).grantDevicePermission(device, appInfo.uid);
            Intent activityIntent = new Intent(intent);
            activityIntent.setComponent(component);
            try {
                this.mContext.startActivityAsUser(activityIntent, this.mParentUser);
            } catch (ActivityNotFoundException e) {
                Slog.e(TAG, "unable to start activity " + activityIntent);
            }
        } catch (NameNotFoundException e2) {
            Slog.e(TAG, "Default USB handling package (" + component.getPackageName() + ") not found  for user " + this.mParentUser);
        }
    }

    void usbDeviceRemoved(UsbDevice device) {
        this.mMtpNotificationManager.hideNotification(device.getDeviceId());
    }

    public void accessoryAttached(UsbAccessory accessory) {
        ArrayList<ResolveInfo> matches;
        ActivityInfo defaultActivity;
        Intent intent = new Intent("android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
        intent.putExtra("accessory", accessory);
        intent.addFlags(285212672);
        synchronized (this.mLock) {
            matches = getAccessoryMatchesLocked(accessory, intent);
            defaultActivity = getDefaultActivityLocked(matches, (UserPackage) this.mAccessoryPreferenceMap.get(new AccessoryFilter(accessory)));
        }
        resolveActivity(intent, matches, defaultActivity, null, accessory);
    }

    private void resolveActivity(Intent intent, ArrayList<ResolveInfo> matches, ActivityInfo defaultActivity, UsbDevice device, UsbAccessory accessory) {
        if (matches.size() == 0) {
            if (accessory != null) {
                String uri = accessory.getUri();
                if (uri != null && uri.length() > 0) {
                    Intent dialogIntent = new Intent();
                    dialogIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbAccessoryUriActivity");
                    dialogIntent.addFlags(268435456);
                    dialogIntent.putExtra("accessory", accessory);
                    dialogIntent.putExtra("uri", uri);
                    try {
                        this.mContext.startActivityAsUser(dialogIntent, this.mParentUser);
                    } catch (ActivityNotFoundException e) {
                        Slog.e(TAG, "unable to start UsbAccessoryUriActivity");
                    }
                }
            }
            return;
        }
        if (defaultActivity != null) {
            UsbUserSettingsManager defaultRIUserSettings = this.mSettingsManager.getSettingsForUser(UserHandle.getUserId(defaultActivity.applicationInfo.uid));
            if (device != null) {
                defaultRIUserSettings.grantDevicePermission(device, defaultActivity.applicationInfo.uid);
            } else if (accessory != null) {
                defaultRIUserSettings.grantAccessoryPermission(accessory, defaultActivity.applicationInfo.uid);
            }
            try {
                intent.setComponent(new ComponentName(defaultActivity.packageName, defaultActivity.name));
                this.mContext.startActivityAsUser(intent, UserHandle.getUserHandleForUid(defaultActivity.applicationInfo.uid));
            } catch (ActivityNotFoundException e2) {
                Slog.e(TAG, "startActivity failed", e2);
            }
        } else {
            UserHandle user;
            Intent resolverIntent = new Intent();
            resolverIntent.addFlags(268435456);
            if (matches.size() == 1) {
                ResolveInfo rInfo = (ResolveInfo) matches.get(0);
                resolverIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbConfirmActivity");
                resolverIntent.putExtra("rinfo", rInfo);
                user = UserHandle.getUserHandleForUid(rInfo.activityInfo.applicationInfo.uid);
                if (device != null) {
                    resolverIntent.putExtra("device", device);
                } else {
                    resolverIntent.putExtra("accessory", accessory);
                }
            } else {
                user = this.mParentUser;
                resolverIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbResolverActivity");
                resolverIntent.putParcelableArrayListExtra("rlist", matches);
                resolverIntent.putExtra("android.intent.extra.INTENT", intent);
            }
            try {
                this.mContext.startActivityAsUser(resolverIntent, user);
            } catch (ActivityNotFoundException e22) {
                Slog.e(TAG, "unable to start activity " + resolverIntent, e22);
            }
        }
    }

    private ActivityInfo getDefaultActivityLocked(ArrayList<ResolveInfo> matches, UserPackage userPackage) {
        if (userPackage != null) {
            for (ResolveInfo info : matches) {
                if (info.activityInfo != null && userPackage.equals(new UserPackage(info.activityInfo.packageName, UserHandle.getUserHandleForUid(info.activityInfo.applicationInfo.uid), null))) {
                    return info.activityInfo;
                }
            }
        }
        if (matches.size() == 1) {
            ActivityInfo activityInfo = ((ResolveInfo) matches.get(0)).activityInfo;
            if (activityInfo != null) {
                if (this.mDisablePermissionDialogs) {
                    return activityInfo;
                }
                if (activityInfo.applicationInfo == null || (activityInfo.applicationInfo.flags & 1) == 0) {
                    return null;
                }
                return activityInfo;
            }
        }
        return null;
    }

    private boolean clearCompatibleMatchesLocked(UserPackage userPackage, DeviceFilter filter) {
        ArrayList<DeviceFilter> keysToRemove = new ArrayList();
        for (DeviceFilter device : this.mDevicePreferenceMap.keySet()) {
            if (filter.contains(device) && !((UserPackage) this.mDevicePreferenceMap.get(device)).equals(userPackage)) {
                keysToRemove.add(device);
            }
        }
        if (!keysToRemove.isEmpty()) {
            for (DeviceFilter keyToRemove : keysToRemove) {
                this.mDevicePreferenceMap.remove(keyToRemove);
            }
        }
        return keysToRemove.isEmpty() ^ 1;
    }

    private boolean clearCompatibleMatchesLocked(UserPackage userPackage, AccessoryFilter filter) {
        ArrayList<AccessoryFilter> keysToRemove = new ArrayList();
        for (AccessoryFilter accessory : this.mAccessoryPreferenceMap.keySet()) {
            if (filter.contains(accessory) && !((UserPackage) this.mAccessoryPreferenceMap.get(accessory)).equals(userPackage)) {
                keysToRemove.add(accessory);
            }
        }
        if (!keysToRemove.isEmpty()) {
            for (AccessoryFilter keyToRemove : keysToRemove) {
                this.mAccessoryPreferenceMap.remove(keyToRemove);
            }
        }
        return keysToRemove.isEmpty() ^ 1;
    }

    private boolean handlePackageAddedLocked(UserPackage userPackage, ActivityInfo aInfo, String metaDataName) {
        XmlResourceParser xmlResourceParser = null;
        boolean changed = false;
        try {
            xmlResourceParser = aInfo.loadXmlMetaData(this.mPackageManager, metaDataName);
            if (xmlResourceParser == null) {
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                return false;
            }
            XmlUtils.nextElement(xmlResourceParser);
            while (xmlResourceParser.getEventType() != 1) {
                String tagName = xmlResourceParser.getName();
                if ("usb-device".equals(tagName)) {
                    if (clearCompatibleMatchesLocked(userPackage, DeviceFilter.read(xmlResourceParser))) {
                        changed = true;
                    }
                } else if ("usb-accessory".equals(tagName) && clearCompatibleMatchesLocked(userPackage, AccessoryFilter.read(xmlResourceParser))) {
                    changed = true;
                }
                XmlUtils.nextElement(xmlResourceParser);
            }
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return changed;
        } catch (Exception e) {
            Slog.w(TAG, "Unable to load component info " + aInfo.toString(), e);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Missing block: B:29:0x005b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handlePackageAdded(UserPackage userPackage) {
        synchronized (this.mLock) {
            boolean changed = false;
            try {
                ActivityInfo[] activities = this.mPackageManager.getPackageInfoAsUser(userPackage.packageName, 129, userPackage.user.getIdentifier()).activities;
                if (activities == null) {
                    return;
                }
                for (int i = 0; i < activities.length; i++) {
                    if (handlePackageAddedLocked(userPackage, activities[i], "android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                        changed = true;
                    }
                    if (handlePackageAddedLocked(userPackage, activities[i], "android.hardware.usb.action.USB_ACCESSORY_ATTACHED")) {
                        changed = true;
                    }
                }
                if (changed) {
                    scheduleWriteSettingsLocked();
                }
            } catch (NameNotFoundException e) {
                Slog.e(TAG, "handlePackageUpdate could not find package " + userPackage, e);
            }
        }
    }

    private int getSerial(UserHandle user) {
        return this.mUserManager.getUserSerialNumber(user.getIdentifier());
    }

    void setDevicePackage(UsbDevice device, String packageName, UserHandle user) {
        DeviceFilter filter = new DeviceFilter(device);
        synchronized (this.mLock) {
            boolean changed;
            if (packageName == null) {
                changed = this.mDevicePreferenceMap.remove(filter) != null;
            } else {
                UserPackage userPackage = new UserPackage(packageName, user, null);
                changed = userPackage.equals(this.mDevicePreferenceMap.get(filter)) ^ 1;
                if (changed) {
                    this.mDevicePreferenceMap.put(filter, userPackage);
                }
            }
            if (changed) {
                scheduleWriteSettingsLocked();
            }
        }
    }

    void setAccessoryPackage(UsbAccessory accessory, String packageName, UserHandle user) {
        AccessoryFilter filter = new AccessoryFilter(accessory);
        synchronized (this.mLock) {
            boolean changed;
            if (packageName == null) {
                changed = this.mAccessoryPreferenceMap.remove(filter) != null;
            } else {
                UserPackage userPackage = new UserPackage(packageName, user, null);
                changed = userPackage.equals(this.mAccessoryPreferenceMap.get(filter)) ^ 1;
                if (changed) {
                    this.mAccessoryPreferenceMap.put(filter, userPackage);
                }
            }
            if (changed) {
                scheduleWriteSettingsLocked();
            }
        }
    }

    boolean hasDefaults(String packageName, UserHandle user) {
        UserPackage userPackage = new UserPackage(packageName, user, null);
        synchronized (this.mLock) {
            if (this.mDevicePreferenceMap.values().contains(userPackage)) {
                return true;
            } else if (this.mAccessoryPreferenceMap.values().contains(userPackage)) {
                return true;
            } else {
                return false;
            }
        }
    }

    void clearDefaults(String packageName, UserHandle user) {
        UserPackage userPackage = new UserPackage(packageName, user, null);
        synchronized (this.mLock) {
            if (clearPackageDefaultsLocked(userPackage)) {
                scheduleWriteSettingsLocked();
            }
        }
    }

    private boolean clearPackageDefaultsLocked(UserPackage userPackage) {
        boolean cleared = false;
        synchronized (this.mLock) {
            Object[] keys;
            if (this.mDevicePreferenceMap.containsValue(userPackage)) {
                keys = this.mDevicePreferenceMap.keySet().toArray();
                for (Object key : keys) {
                    if (userPackage.equals(this.mDevicePreferenceMap.get(key))) {
                        this.mDevicePreferenceMap.remove(key);
                        cleared = true;
                    }
                }
            }
            if (this.mAccessoryPreferenceMap.containsValue(userPackage)) {
                keys = this.mAccessoryPreferenceMap.keySet().toArray();
                for (Object key2 : keys) {
                    if (userPackage.equals(this.mAccessoryPreferenceMap.get(key2))) {
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
            pw.println("Device preferences:");
            for (DeviceFilter filter : this.mDevicePreferenceMap.keySet()) {
                pw.println("  " + filter + ": " + this.mDevicePreferenceMap.get(filter));
            }
            pw.println("Accessory preferences:");
            for (AccessoryFilter filter2 : this.mAccessoryPreferenceMap.keySet()) {
                pw.println("  " + filter2 + ": " + this.mAccessoryPreferenceMap.get(filter2));
            }
        }
    }

    private static Intent createDeviceAttachedIntent(UsbDevice device) {
        Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intent.putExtra("device", device);
        intent.addFlags(285212672);
        return intent;
    }
}
