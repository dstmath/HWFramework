package com.android.server.wifi;

import android.util.ArraySet;
import android.util.Log;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class WakeupConfigStoreData implements WifiConfigStore.StoreData {
    private static final String TAG = "WakeupConfigStoreData";
    private static final String XML_TAG_FEATURE_STATE_SECTION = "FeatureState";
    private static final String XML_TAG_IS_ACTIVE = "IsActive";
    private static final String XML_TAG_IS_ONBOARDED = "IsOnboarded";
    private static final String XML_TAG_NETWORK_SECTION = "Network";
    private static final String XML_TAG_NOTIFICATIONS_SHOWN = "NotificationsShown";
    private static final String XML_TAG_SECURITY = "Security";
    private static final String XML_TAG_SSID = "SSID";
    private boolean mHasBeenRead = false;
    private final DataSource<Boolean> mIsActiveDataSource;
    private final DataSource<Boolean> mIsOnboardedDataSource;
    private final DataSource<Set<ScanResultMatchInfo>> mNetworkDataSource;
    private final DataSource<Integer> mNotificationsDataSource;

    public interface DataSource<T> {
        T getData();

        void setData(T t);
    }

    public WakeupConfigStoreData(DataSource<Boolean> isActiveDataSource, DataSource<Boolean> isOnboardedDataSource, DataSource<Integer> notificationsDataSource, DataSource<Set<ScanResultMatchInfo>> networkDataSource) {
        this.mIsActiveDataSource = isActiveDataSource;
        this.mIsOnboardedDataSource = isOnboardedDataSource;
        this.mNotificationsDataSource = notificationsDataSource;
        this.mNetworkDataSource = networkDataSource;
    }

    public boolean hasBeenRead() {
        return this.mHasBeenRead;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void serializeData(XmlSerializer out) throws XmlPullParserException, IOException {
        writeFeatureState(out);
        for (ScanResultMatchInfo scanResultMatchInfo : this.mNetworkDataSource.getData()) {
            writeNetwork(out, scanResultMatchInfo);
        }
    }

    private void writeFeatureState(XmlSerializer out) throws IOException, XmlPullParserException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_FEATURE_STATE_SECTION);
        XmlUtil.writeNextValue(out, XML_TAG_IS_ACTIVE, this.mIsActiveDataSource.getData());
        XmlUtil.writeNextValue(out, XML_TAG_IS_ONBOARDED, this.mIsOnboardedDataSource.getData());
        XmlUtil.writeNextValue(out, XML_TAG_NOTIFICATIONS_SHOWN, this.mNotificationsDataSource.getData());
        XmlUtil.writeNextSectionEnd(out, XML_TAG_FEATURE_STATE_SECTION);
    }

    private void writeNetwork(XmlSerializer out, ScanResultMatchInfo scanResultMatchInfo) throws XmlPullParserException, IOException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_NETWORK_SECTION);
        XmlUtil.writeNextValue(out, "SSID", scanResultMatchInfo.networkSsid);
        XmlUtil.writeNextValue(out, XML_TAG_SECURITY, Integer.valueOf(scanResultMatchInfo.networkType));
        XmlUtil.writeNextSectionEnd(out, XML_TAG_NETWORK_SECTION);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0037, code lost:
        if (r4.equals(com.android.server.wifi.WakeupConfigStoreData.XML_TAG_FEATURE_STATE_SECTION) == false) goto L_0x0044;
     */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0054  */
    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        if (!this.mHasBeenRead) {
            Log.d(TAG, "WifiWake user data has been read");
            this.mHasBeenRead = true;
        }
        if (in != null) {
            Set<ScanResultMatchInfo> networks = new ArraySet<>();
            String[] headerName = new String[1];
            while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
                boolean z = false;
                String str = headerName[0];
                int hashCode = str.hashCode();
                if (hashCode != -786828786) {
                    if (hashCode == 1362433883) {
                    }
                } else if (str.equals(XML_TAG_NETWORK_SECTION)) {
                    z = true;
                    if (z) {
                        parseFeatureState(in, outerTagDepth + 1);
                    } else if (z) {
                        networks.add(parseNetwork(in, outerTagDepth + 1));
                    }
                }
                z = true;
                if (z) {
                }
            }
            this.mNetworkDataSource.setData(networks);
        }
    }

    private void parseFeatureState(XmlPullParser in, int outerTagDepth) throws IOException, XmlPullParserException {
        boolean isActive = false;
        boolean isOnboarded = false;
        int notificationsShown = 0;
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                char c = 65535;
                int hashCode = str.hashCode();
                if (hashCode != -1725092580) {
                    if (hashCode != -684272400) {
                        if (hashCode == 898665769 && str.equals(XML_TAG_NOTIFICATIONS_SHOWN)) {
                            c = 2;
                        }
                    } else if (str.equals(XML_TAG_IS_ACTIVE)) {
                        c = 0;
                    }
                } else if (str.equals(XML_TAG_IS_ONBOARDED)) {
                    c = 1;
                }
                if (c == 0) {
                    isActive = ((Boolean) value).booleanValue();
                } else if (c == 1) {
                    isOnboarded = ((Boolean) value).booleanValue();
                } else if (c == 2) {
                    notificationsShown = ((Integer) value).intValue();
                } else {
                    throw new XmlPullParserException("Unknown value found: " + valueName[0]);
                }
            } else {
                throw new XmlPullParserException("Missing value name");
            }
        }
        this.mIsActiveDataSource.setData(Boolean.valueOf(isActive));
        this.mIsOnboardedDataSource.setData(Boolean.valueOf(isOnboarded));
        this.mNotificationsDataSource.setData(Integer.valueOf(notificationsShown));
    }

    private ScanResultMatchInfo parseNetwork(XmlPullParser in, int outerTagDepth) throws IOException, XmlPullParserException {
        ScanResultMatchInfo scanResultMatchInfo = new ScanResultMatchInfo();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                char c = 65535;
                int hashCode = str.hashCode();
                if (hashCode != 2554747) {
                    if (hashCode == 1013767008 && str.equals(XML_TAG_SECURITY)) {
                        c = 1;
                    }
                } else if (str.equals("SSID")) {
                    c = 0;
                }
                if (c == 0) {
                    scanResultMatchInfo.networkSsid = (String) value;
                } else if (c == 1) {
                    scanResultMatchInfo.networkType = ((Integer) value).intValue();
                } else {
                    throw new XmlPullParserException("Unknown tag under WakeupConfigStoreData: " + valueName[0]);
                }
            } else {
                throw new XmlPullParserException("Missing value name");
            }
        }
        return scanResultMatchInfo;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void resetData() {
        this.mNetworkDataSource.setData(Collections.emptySet());
        this.mIsActiveDataSource.setData(false);
        this.mIsOnboardedDataSource.setData(false);
        this.mNotificationsDataSource.setData(0);
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public boolean hasNewDataToSerialize() {
        return true;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public String getName() {
        return TAG;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public int getStoreFileId() {
        return 1;
    }
}
