package com.android.server.devicepolicy;

import android.app.admin.IDevicePolicyManager.Stub;
import android.os.Bundle;
import android.util.Slog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class AbsDevicePolicyManagerService extends Stub {
    protected static final boolean HWDBG = false;
    protected static final boolean HWFLOW = false;
    private static final String TAG = "AbsDevicePolicyManagerService";

    static class HwActiveAdmin {
        private static final String ATTR_VALUE = "value";
        private static final String DOMAIN = "domain";
        private static final String FIELD = "field";
        private static final String ID = "id";
        private static final String INCOMING = "incoming";
        private static final String LABEL = "label";
        private static final String OUTGOING = "outgoing";
        private static final String PROVIDER = "provider";
        private static final String PROVIDERS_PRODUCT = "providers_product";
        private static final String TAG_ALLOWED_INSTALL_PACKAGES = "allowed-install-packages";
        private static final String TAG_ALLOWED_INSTALL_PACKAGES_ITEM = "allowed-install-packages-item";
        private static final String TAG_DISABLED_DEACTIVE_Mdm_PACKAGES = "disabled-deactive-mdm-packages";
        private static final String TAG_DISABLED_DEACTIVE_Mdm_PACKAGES_ITEM = "disabled-deactive-mdm-packages-item";
        private static final String TAG_DISABLE_ADB = "disable-adb";
        private static final String TAG_DISABLE_BACK_KEY = "disable-backKey";
        private static final String TAG_DISABLE_BLUETOOTH = "disable-bluetooth";
        private static final String TAG_DISABLE_BOOTLOADER = "disable-bootloader";
        private static final String TAG_DISABLE_CHANGE_LAUNCHER = "disable-change-launcher";
        private static final String TAG_DISABLE_DATA_CONNECTIVITY = "disable-dataconnectivity";
        private static final String TAG_DISABLE_DECRYPT_SD_CARD = "disable-decrypt-sd-card";
        private static final String TAG_DISABLE_EXTERNAL_STORAGE = "disable-externalstorage";
        private static final String TAG_DISABLE_GPS = "disable-gps";
        private static final String TAG_DISABLE_HOME_KEY = "disable-homekey";
        private static final String TAG_DISABLE_INSTALLSOURCE = "disable-installsource";
        private static final String TAG_DISABLE_NFC = "disable-nfc";
        private static final String TAG_DISABLE_SAFEMODE = "disable-safemode";
        private static final String TAG_DISABLE_SMS = "disable-sms";
        private static final String TAG_DISABLE_StatusBar_ExpandPanel = "disable-expandpanel";
        private static final String TAG_DISABLE_TASK_KEY = "disable-taskkey";
        private static final String TAG_DISABLE_USBDATA = "disable-usbdata";
        private static final String TAG_DISABLE_USBOTG = "disable-usbotg";
        private static final String TAG_DISABLE_VOICE = "disable-voice";
        private static final String TAG_DISABLE_WIFI = "disable-wifi";
        private static final String TAG_DISABLE_WIFIAP = "disable-wifiap";
        private static final String TAG_DISALLOWED_RUNNING_APP_LIST = "disallowedRunning-app-list";
        private static final String TAG_DISALLOWED_RUNNING_APP_LIST_ITEM = "disallowedRunning-app-list-item";
        private static final String TAG_DISALLOWED_UNINSTALL_PACKAGES = "disallowed-uninstall-packages";
        private static final String TAG_DISALLOWED_UNINSTALL_PACKAGES_ITEM = "disallowed-uninstall-packages-item";
        private static final String TAG_INSTALL_SOURCE_WHITELIST = "install-source-whitelist";
        private static final String TAG_INSTALL_SOURCE_WHITELIST_ITEM = "install-source-whitelist_item";
        private static final String TAG_NETWORK_ACCESS_ADDR_WHITELIST = "network-access-whitelist";
        private static final String TAG_NETWORK_ACCESS_ADDR_WHITELIST_ITEM = "network-access-whitelist-item";
        private static final String TAG_PERSISTENT_APP_LIST = "persistent-app-list";
        private static final String TAG_PERSISTENT_APP_LIST_ITEM = "persistent-app-list-item";
        public static final String TAG_POLICIES = "hw_policy";
        private static final String URI = "uri";
        private static final String USERNAME = "username";
        boolean disableAdb;
        boolean disableBackKey;
        boolean disableBluetooth;
        boolean disableBootLoader;
        boolean disableChangeLauncher;
        boolean disableDataConnectivity;
        boolean disableDecryptSDCard;
        boolean disableExternalStorage;
        boolean disableGPS;
        boolean disableHomeKey;
        boolean disableInstallSource;
        boolean disableNFC;
        boolean disableSMS;
        boolean disableSafeMode;
        boolean disableStatusBarExpandPanel;
        boolean disableTaskKey;
        boolean disableUSBData;
        boolean disableUSBOtg;
        boolean disableVoice;
        boolean disableWifi;
        boolean disableWifiAp;
        List<String> disabledDeactiveMdmPackagesList;
        List<String> disallowedRunningAppList;
        List<String> disallowedUninstallPackageList;
        List<String> installPackageWhitelist;
        List<String> installSourceWhitelist;
        List<Bundle> mailProviderlist;
        List<String> networkAccessWhitelist;
        List<String> persistentAppList;

        HwActiveAdmin() {
            this.disableWifi = AbsDevicePolicyManagerService.HWFLOW;
            this.disableBluetooth = AbsDevicePolicyManagerService.HWFLOW;
            this.disableWifiAp = AbsDevicePolicyManagerService.HWFLOW;
            this.disableBootLoader = AbsDevicePolicyManagerService.HWFLOW;
            this.disableUSBData = AbsDevicePolicyManagerService.HWFLOW;
            this.disableExternalStorage = AbsDevicePolicyManagerService.HWFLOW;
            this.disableNFC = AbsDevicePolicyManagerService.HWFLOW;
            this.disableDataConnectivity = AbsDevicePolicyManagerService.HWFLOW;
            this.disableVoice = AbsDevicePolicyManagerService.HWFLOW;
            this.disableSMS = AbsDevicePolicyManagerService.HWFLOW;
            this.disableStatusBarExpandPanel = AbsDevicePolicyManagerService.HWFLOW;
            this.disableInstallSource = AbsDevicePolicyManagerService.HWFLOW;
            this.disableSafeMode = AbsDevicePolicyManagerService.HWFLOW;
            this.disableAdb = AbsDevicePolicyManagerService.HWFLOW;
            this.disableUSBOtg = AbsDevicePolicyManagerService.HWFLOW;
            this.disableGPS = AbsDevicePolicyManagerService.HWFLOW;
            this.disableHomeKey = AbsDevicePolicyManagerService.HWFLOW;
            this.disableBackKey = AbsDevicePolicyManagerService.HWFLOW;
            this.disableTaskKey = AbsDevicePolicyManagerService.HWFLOW;
            this.disableChangeLauncher = AbsDevicePolicyManagerService.HWFLOW;
            this.disableDecryptSDCard = AbsDevicePolicyManagerService.HWFLOW;
            this.installSourceWhitelist = null;
            this.persistentAppList = null;
            this.disallowedRunningAppList = null;
            this.installPackageWhitelist = null;
            this.disallowedUninstallPackageList = null;
            this.networkAccessWhitelist = null;
            this.mailProviderlist = null;
            this.disabledDeactiveMdmPackagesList = null;
        }

        public void writePoliciesToXml(XmlSerializer out) throws IOException {
            if (AbsDevicePolicyManagerService.HWFLOW) {
                Slog.d(AbsDevicePolicyManagerService.TAG, "write policy to xml out");
            }
            out.startTag(null, TAG_POLICIES);
            if (this.disableWifi) {
                out.startTag(null, TAG_DISABLE_WIFI);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableWifi));
                out.endTag(null, TAG_DISABLE_WIFI);
            }
            if (this.disableDecryptSDCard) {
                out.startTag(null, TAG_DISABLE_DECRYPT_SD_CARD);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableDecryptSDCard));
                out.endTag(null, TAG_DISABLE_DECRYPT_SD_CARD);
            }
            if (this.disableBluetooth) {
                out.startTag(null, TAG_DISABLE_BLUETOOTH);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableBluetooth));
                out.endTag(null, TAG_DISABLE_BLUETOOTH);
            }
            if (this.disableWifiAp) {
                out.startTag(null, TAG_DISABLE_WIFIAP);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableWifiAp));
                out.endTag(null, TAG_DISABLE_WIFIAP);
            }
            if (this.disableBootLoader) {
                out.startTag(null, TAG_DISABLE_BOOTLOADER);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableBootLoader));
                out.endTag(null, TAG_DISABLE_BOOTLOADER);
            }
            if (this.disableUSBData) {
                out.startTag(null, TAG_DISABLE_USBDATA);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableUSBData));
                out.endTag(null, TAG_DISABLE_USBDATA);
            }
            if (this.disableExternalStorage) {
                out.startTag(null, TAG_DISABLE_EXTERNAL_STORAGE);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableExternalStorage));
                out.endTag(null, TAG_DISABLE_EXTERNAL_STORAGE);
            }
            if (this.disableNFC) {
                out.startTag(null, TAG_DISABLE_NFC);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableNFC));
                out.endTag(null, TAG_DISABLE_NFC);
            }
            if (this.disableDataConnectivity) {
                out.startTag(null, TAG_DISABLE_DATA_CONNECTIVITY);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableDataConnectivity));
                out.endTag(null, TAG_DISABLE_DATA_CONNECTIVITY);
            }
            if (this.disableVoice) {
                out.startTag(null, TAG_DISABLE_VOICE);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableVoice));
                out.endTag(null, TAG_DISABLE_VOICE);
            }
            if (this.disableSMS) {
                out.startTag(null, TAG_DISABLE_SMS);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableSMS));
                out.endTag(null, TAG_DISABLE_SMS);
            }
            if (this.disableStatusBarExpandPanel) {
                out.startTag(null, TAG_DISABLE_StatusBar_ExpandPanel);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableStatusBarExpandPanel));
                out.endTag(null, TAG_DISABLE_StatusBar_ExpandPanel);
            }
            if (this.disableInstallSource) {
                out.startTag(null, TAG_DISABLE_INSTALLSOURCE);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableInstallSource));
                out.endTag(null, TAG_DISABLE_INSTALLSOURCE);
            }
            if (this.disableSafeMode) {
                out.startTag(null, TAG_DISABLE_SAFEMODE);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableSafeMode));
                out.endTag(null, TAG_DISABLE_SAFEMODE);
            }
            if (this.disableAdb) {
                out.startTag(null, TAG_DISABLE_ADB);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableAdb));
                out.endTag(null, TAG_DISABLE_ADB);
            }
            if (this.disableUSBOtg) {
                out.startTag(null, TAG_DISABLE_USBOTG);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableUSBOtg));
                out.endTag(null, TAG_DISABLE_USBOTG);
            }
            if (this.disableGPS) {
                out.startTag(null, TAG_DISABLE_GPS);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableGPS));
                out.endTag(null, TAG_DISABLE_GPS);
            }
            if (this.disableHomeKey) {
                out.startTag(null, TAG_DISABLE_HOME_KEY);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableHomeKey));
                out.endTag(null, TAG_DISABLE_HOME_KEY);
            }
            if (this.disableBackKey) {
                out.startTag(null, TAG_DISABLE_BACK_KEY);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableBackKey));
                out.endTag(null, TAG_DISABLE_BACK_KEY);
            }
            if (this.disableTaskKey) {
                out.startTag(null, TAG_DISABLE_TASK_KEY);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableTaskKey));
                out.endTag(null, TAG_DISABLE_TASK_KEY);
            }
            if (this.disableChangeLauncher) {
                out.startTag(null, TAG_DISABLE_CHANGE_LAUNCHER);
                out.attribute(null, ATTR_VALUE, Boolean.toString(this.disableChangeLauncher));
                out.endTag(null, TAG_DISABLE_CHANGE_LAUNCHER);
            }
            writeListToXml(out, TAG_ALLOWED_INSTALL_PACKAGES, TAG_ALLOWED_INSTALL_PACKAGES_ITEM, this.installPackageWhitelist);
            writeListToXml(out, TAG_DISALLOWED_UNINSTALL_PACKAGES, TAG_DISALLOWED_UNINSTALL_PACKAGES_ITEM, this.disallowedUninstallPackageList);
            writeListToXml(out, TAG_INSTALL_SOURCE_WHITELIST, TAG_INSTALL_SOURCE_WHITELIST_ITEM, this.installSourceWhitelist);
            writeListToXml(out, TAG_PERSISTENT_APP_LIST, TAG_PERSISTENT_APP_LIST_ITEM, this.persistentAppList);
            writeListToXml(out, TAG_DISALLOWED_RUNNING_APP_LIST, TAG_DISALLOWED_RUNNING_APP_LIST_ITEM, this.disallowedRunningAppList);
            writeListToXml(out, TAG_NETWORK_ACCESS_ADDR_WHITELIST, TAG_NETWORK_ACCESS_ADDR_WHITELIST_ITEM, this.networkAccessWhitelist);
            writeListToXml(out, TAG_DISABLED_DEACTIVE_Mdm_PACKAGES, TAG_DISABLED_DEACTIVE_Mdm_PACKAGES_ITEM, this.disabledDeactiveMdmPackagesList);
            writeProviderListToXml(out, PROVIDERS_PRODUCT, this.mailProviderlist);
            out.endTag(null, TAG_POLICIES);
        }

        public void readPoliciesFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            if (AbsDevicePolicyManagerService.HWFLOW) {
                Slog.d(AbsDevicePolicyManagerService.TAG, "read policy from xml");
            }
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(type == 3 || type == 4)) {
                    String tag = parser.getName();
                    if (TAG_DISABLE_WIFI.equals(tag)) {
                        this.disableWifi = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_BLUETOOTH.equals(tag)) {
                        this.disableBluetooth = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_WIFIAP.equals(tag)) {
                        this.disableWifiAp = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_BOOTLOADER.equals(tag)) {
                        this.disableBootLoader = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_USBDATA.equals(tag)) {
                        this.disableUSBData = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_EXTERNAL_STORAGE.equals(tag)) {
                        this.disableExternalStorage = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_NFC.equals(tag)) {
                        this.disableNFC = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_DATA_CONNECTIVITY.equals(tag)) {
                        this.disableDataConnectivity = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_VOICE.equals(tag)) {
                        this.disableVoice = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_SMS.equals(tag)) {
                        this.disableSMS = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_StatusBar_ExpandPanel.equals(tag)) {
                        this.disableStatusBarExpandPanel = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_INSTALLSOURCE.equals(tag)) {
                        this.disableInstallSource = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_SAFEMODE.equals(tag)) {
                        this.disableSafeMode = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_ADB.equals(tag)) {
                        this.disableAdb = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_USBOTG.equals(tag)) {
                        this.disableUSBOtg = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_GPS.equals(tag)) {
                        this.disableGPS = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_HOME_KEY.equals(tag)) {
                        this.disableHomeKey = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_BACK_KEY.equals(tag)) {
                        this.disableBackKey = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_TASK_KEY.equals(tag)) {
                        this.disableTaskKey = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_DISABLE_CHANGE_LAUNCHER.equals(tag)) {
                        this.disableChangeLauncher = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else if (TAG_INSTALL_SOURCE_WHITELIST.equals(tag)) {
                        this.installSourceWhitelist = readListFromXml(parser, TAG_INSTALL_SOURCE_WHITELIST_ITEM);
                    } else if (TAG_ALLOWED_INSTALL_PACKAGES.equals(tag)) {
                        this.installPackageWhitelist = readListFromXml(parser, TAG_ALLOWED_INSTALL_PACKAGES_ITEM);
                    } else if (TAG_DISALLOWED_UNINSTALL_PACKAGES.equals(tag)) {
                        this.disallowedUninstallPackageList = readListFromXml(parser, TAG_DISALLOWED_UNINSTALL_PACKAGES_ITEM);
                    } else if (TAG_DISABLED_DEACTIVE_Mdm_PACKAGES.equals(tag)) {
                        this.disabledDeactiveMdmPackagesList = readListFromXml(parser, TAG_DISABLED_DEACTIVE_Mdm_PACKAGES_ITEM);
                    } else if (TAG_PERSISTENT_APP_LIST.equals(tag)) {
                        this.persistentAppList = readListFromXml(parser, TAG_PERSISTENT_APP_LIST_ITEM);
                    } else if (TAG_DISALLOWED_RUNNING_APP_LIST.equals(tag)) {
                        this.disallowedRunningAppList = readListFromXml(parser, TAG_DISALLOWED_RUNNING_APP_LIST_ITEM);
                    } else if (TAG_NETWORK_ACCESS_ADDR_WHITELIST.equals(tag)) {
                        this.networkAccessWhitelist = readListFromXml(parser, TAG_NETWORK_ACCESS_ADDR_WHITELIST_ITEM);
                    } else if (PROVIDERS_PRODUCT.equals(tag)) {
                        this.mailProviderlist = readPorvidersList(parser, tag);
                    } else if (TAG_DISABLE_DECRYPT_SD_CARD.equals(tag)) {
                        this.disableDecryptSDCard = Boolean.parseBoolean(parser.getAttributeValue(null, ATTR_VALUE));
                    } else {
                        Slog.w(AbsDevicePolicyManagerService.TAG, "Unknown admin tag: " + tag);
                    }
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private List<Bundle> readPorvidersList(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            List<Bundle> result = new ArrayList();
            int outerDepth = parser.getDepth();
            while (true) {
                int outerType = parser.next();
                if (outerType != 1 && (outerType != 3 || parser.getDepth() > outerDepth)) {
                    if (!(outerType == 3 || outerType == 4)) {
                        String tagnew = parser.getName();
                        if (PROVIDER.equals(tagnew)) {
                            Bundle para = new Bundle();
                            para.putString(ID, parser.getAttributeValue(null, ID));
                            para.putString(LABEL, parser.getAttributeValue(null, LABEL));
                            para.putString(DOMAIN, parser.getAttributeValue(null, DOMAIN));
                            readProvidersItems(parser, para);
                            result.add(para);
                        } else {
                            Slog.w(AbsDevicePolicyManagerService.TAG, "missing value under inner tag[" + tagnew + "]");
                        }
                    }
                }
            }
            return result.isEmpty() ? null : result;
        }

        void readProvidersItems(XmlPullParser parser, Bundle para) throws XmlPullParserException, IOException {
            int outerDepth = parser.getDepth();
            while (true) {
                int outerType = parser.next();
                if (outerType == 1) {
                    return;
                }
                if (outerType == 3 && parser.getDepth() <= outerDepth) {
                    return;
                }
                if (!(outerType == 3 || outerType == 4)) {
                    String tagnew = parser.getName();
                    if (INCOMING.equals(tagnew) && para != null) {
                        para.putString("incominguri", parser.getAttributeValue(null, URI));
                        para.putString("incomingusername", parser.getAttributeValue(null, USERNAME));
                        para.putString("incomingfield", parser.getAttributeValue(null, FIELD));
                    } else if (!OUTGOING.equals(tagnew) || para == null) {
                        Slog.w(AbsDevicePolicyManagerService.TAG, "missing value under inner tag[" + tagnew + "]");
                    } else {
                        para.putString("outgoinguri", parser.getAttributeValue(null, URI));
                        para.putString("outgoingusername", parser.getAttributeValue(null, USERNAME));
                    }
                }
            }
        }

        void writeProviderListToXml(XmlSerializer out, String outerTag, List<Bundle> providerList) throws IllegalArgumentException, IllegalStateException, IOException {
            if (providerList != null) {
                out.startTag(null, outerTag);
                for (Bundle para : providerList) {
                    out.startTag(null, PROVIDER);
                    out.attribute(null, ID, para.getString(ID));
                    out.attribute(null, LABEL, para.getString(LABEL));
                    out.attribute(null, DOMAIN, para.getString(DOMAIN));
                    out.startTag(null, INCOMING);
                    out.attribute(null, URI, para.getString("incominguri"));
                    out.attribute(null, USERNAME, para.getString("incomingusername"));
                    out.attribute(null, FIELD, para.getString("incomingfield"));
                    out.endTag(null, INCOMING);
                    out.startTag(null, OUTGOING);
                    out.attribute(null, URI, para.getString("outgoinguri"));
                    out.attribute(null, USERNAME, para.getString("outgoingusername"));
                    out.endTag(null, OUTGOING);
                    out.endTag(null, PROVIDER);
                }
                out.endTag(null, outerTag);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private List<String> readListFromXml(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
            if (tag == null || tag.isEmpty()) {
                return null;
            }
            List<String> result = new ArrayList();
            int outerDepth = parser.getDepth();
            while (true) {
                int outerType = parser.next();
                if (outerType != 1 && (outerType != 3 || parser.getDepth() > outerDepth)) {
                    if (!(outerType == 3 || outerType == 4)) {
                        String outerTag = parser.getName();
                        if (tag.equals(outerTag)) {
                            String value = parser.getAttributeValue(null, ATTR_VALUE);
                            if (value != null) {
                                result.add(value);
                            } else {
                                Slog.w(AbsDevicePolicyManagerService.TAG, "missing value under inner tag[" + outerTag + "]");
                            }
                        }
                    }
                }
            }
            if (result.isEmpty()) {
                result = null;
            }
            return result;
        }

        void writeListToXml(XmlSerializer out, String outerTag, String innerTag, List<String> someList) throws IllegalArgumentException, IllegalStateException, IOException {
            if (someList != null && !someList.isEmpty()) {
                out.startTag(null, outerTag);
                for (String value : someList) {
                    out.startTag(null, innerTag);
                    out.attribute(null, ATTR_VALUE, value);
                    out.endTag(null, innerTag);
                }
                out.endTag(null, outerTag);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.devicepolicy.AbsDevicePolicyManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.devicepolicy.AbsDevicePolicyManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.devicepolicy.AbsDevicePolicyManagerService.<clinit>():void");
    }

    protected void syncHwDeviceSettingsLocked(int userHandle) {
    }

    protected boolean isSecureBlockEncrypted() {
        return HWFLOW;
    }
}
