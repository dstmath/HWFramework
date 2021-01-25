package com.android.server.devicepolicy;

import android.os.Bundle;
import com.android.server.devicepolicy.PolicyStruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwActiveAdmin {
    public static final String TAG_POLICIES = "hw_policy";
    public HashMap<String, PolicyStruct.PolicyItem> adminPolicyItems = new HashMap<>();
    public String deactiveTime = "";
    public String defaultLauncher = "";
    public boolean disableAdb = false;
    public boolean disableBackKey = false;
    public boolean disableBluetooth = false;
    public boolean disableBootLoader = false;
    public boolean disableChangeLauncher = false;
    public boolean disableDataConnectivity = false;
    public boolean disableDecryptSDCard = false;
    public boolean disableExternalStorage = false;
    public boolean disableGPS = false;
    public boolean disableHomeKey = false;
    public boolean disableInstallSource = false;
    public boolean disableNFC = false;
    public boolean disableSMS = false;
    public boolean disableSafeMode = false;
    public boolean disableStatusBarExpandPanel = false;
    public boolean disableTaskKey = false;
    public boolean disableUSBData = false;
    public boolean disableUSBOtg = false;
    public boolean disableVoice = false;
    public boolean disableWifi = false;
    public boolean disableWifiAp = false;
    public List<String> disabledDeactiveMdmPackagesList = null;
    public List<String> disallowedRunningAppList = null;
    public List<String> disallowedUninstallPackageList = null;
    public List<String> installPackageWhitelist = null;
    public List<String> installSourceWhitelist = null;
    public boolean isForcedActive = false;
    public List<Bundle> mailProviderlist = null;
    public List<String> networkAccessWhitelist = null;
    public List<String> persistentAppList = null;
    public List<Bundle> vpnProviderlist = null;

    public void writePoliciesToXml(XmlSerializer out) throws IllegalArgumentException, IllegalStateException, IOException {
    }

    public void readPoliciesFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
    }
}
