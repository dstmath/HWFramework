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

    public void serializeData(XmlSerializer out, boolean shared) throws XmlPullParserException, IOException {
        if (!shared) {
            writeFeatureState(out);
            for (ScanResultMatchInfo scanResultMatchInfo : this.mNetworkDataSource.getData()) {
                writeNetwork(out, scanResultMatchInfo);
            }
            return;
        }
        throw new XmlPullParserException("Share data not supported");
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

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        if (r4.equals(XML_TAG_FEATURE_STATE_SECTION) == false) goto L_0x0048;
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004d  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x001c A[SYNTHETIC] */
    public void deserializeData(XmlPullParser in, int outerTagDepth, boolean shared) throws XmlPullParserException, IOException {
        if (!shared && !this.mHasBeenRead) {
            Log.d(TAG, "WifiWake user data has been read");
            this.mHasBeenRead = true;
        }
        if (in != null) {
            if (!shared) {
                Set<ScanResultMatchInfo> networks = new ArraySet<>();
                String[] headerName = new String[1];
                while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
                    boolean z = false;
                    String str = headerName[0];
                    int hashCode = str.hashCode();
                    if (hashCode == -786828786) {
                        if (str.equals(XML_TAG_NETWORK_SECTION)) {
                            z = true;
                            switch (z) {
                                case false:
                                    parseFeatureState(in, outerTagDepth + 1);
                                    break;
                                case true:
                                    networks.add(parseNetwork(in, outerTagDepth + 1));
                                    break;
                            }
                        }
                    } else if (hashCode == 1362433883) {
                    }
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                    }
                }
                this.mNetworkDataSource.setData(networks);
                return;
            }
            throw new XmlPullParserException("Shared data not supported");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0048, code lost:
        if (r7.equals(XML_TAG_IS_ONBOARDED) != false) goto L_0x004c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0070  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x004f A[SYNTHETIC] */
    private void parseFeatureState(XmlPullParser in, int outerTagDepth) throws IOException, XmlPullParserException {
        boolean isOnboarded = false;
        boolean isActive = false;
        int notificationsShown = 0;
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            char c = 1;
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                int hashCode = str.hashCode();
                if (hashCode != -1725092580) {
                    if (hashCode == -684272400) {
                        if (str.equals(XML_TAG_IS_ACTIVE)) {
                            c = 0;
                            switch (c) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                            }
                        }
                    } else if (hashCode == 898665769 && str.equals(XML_TAG_NOTIFICATIONS_SHOWN)) {
                        c = 2;
                        switch (c) {
                            case 0:
                                isActive = ((Boolean) value).booleanValue();
                                break;
                            case 1:
                                isOnboarded = ((Boolean) value).booleanValue();
                                break;
                            case 2:
                                notificationsShown = ((Integer) value).intValue();
                                break;
                            default:
                                throw new XmlPullParserException("Unknown value found: " + valueName[0]);
                        }
                    }
                }
                c = 65535;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            } else {
                throw new XmlPullParserException("Missing value name");
            }
        }
        this.mIsActiveDataSource.setData(Boolean.valueOf(isActive));
        this.mIsOnboardedDataSource.setData(Boolean.valueOf(isOnboarded));
        this.mNotificationsDataSource.setData(Integer.valueOf(notificationsShown));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002f, code lost:
        if (r5.equals(XML_TAG_SECURITY) == false) goto L_0x003c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0040 A[SYNTHETIC] */
    private ScanResultMatchInfo parseNetwork(XmlPullParser in, int outerTagDepth) throws IOException, XmlPullParserException {
        ScanResultMatchInfo scanResultMatchInfo = new ScanResultMatchInfo();
        while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
            boolean z = true;
            String[] valueName = new String[1];
            Object value = XmlUtil.readCurrentValue(in, valueName);
            if (valueName[0] != null) {
                String str = valueName[0];
                int hashCode = str.hashCode();
                if (hashCode != 2554747) {
                    if (hashCode == 1013767008) {
                    }
                } else if (str.equals("SSID")) {
                    z = false;
                    switch (z) {
                        case false:
                            scanResultMatchInfo.networkSsid = (String) value;
                            break;
                        case true:
                            scanResultMatchInfo.networkType = ((Integer) value).intValue();
                            break;
                        default:
                            throw new XmlPullParserException("Unknown tag under WakeupConfigStoreData: " + valueName[0]);
                    }
                }
                z = true;
                switch (z) {
                    case false:
                        break;
                    case true:
                        break;
                }
            } else {
                throw new XmlPullParserException("Missing value name");
            }
        }
        return scanResultMatchInfo;
    }

    public void resetData(boolean shared) {
        if (!shared) {
            this.mNetworkDataSource.setData(Collections.emptySet());
            this.mIsActiveDataSource.setData(false);
            this.mIsOnboardedDataSource.setData(false);
            this.mNotificationsDataSource.setData(0);
        }
    }

    public String getName() {
        return TAG;
    }

    public boolean supportShareData() {
        return false;
    }
}
