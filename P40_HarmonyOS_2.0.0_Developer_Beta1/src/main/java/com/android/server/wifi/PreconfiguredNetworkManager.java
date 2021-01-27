package com.android.server.wifi;

import android.os.SystemProperties;
import android.util.Xml;
import android.util.wifi.HwHiLog;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PreconfiguredNetworkManager {
    private static final int EAP_AKA = 5;
    private static final String EAP_AKA_METHOD = "EAP_AKA";
    private static final int EAP_AKA_PRIME = 6;
    private static final String EAP_AKA_PRIME_METHOD = "EAP_AKA_PRIME";
    private static final int EAP_SIM = 4;
    private static final String EAP_SIM_METHOD = "EAP_SIM";
    public static final boolean IS_R1 = ("389".equals(SystemProperties.get("ro.config.hw_opta", "0")) && "840".equals(SystemProperties.get("ro.config.hw_optb", "0")));
    private static final String PRECONFIGUREDNETWORKLIST_NODE_ROOT = "PreconfiguredNetworkList";
    private static final String PRECONFIGUREDNETWORK_EAPMETHOD = "eapMethod";
    private static final String PRECONFIGUREDNETWORK_NODE = "PreconfiguredNetwork";
    private static final String PRECONFIGUREDNETWORK_SSID = "ssid";
    private static final String STORE_FILE_NAME = "PreconfiguredNetwork.xml";
    private static final String TAG = "PreconfiguredNetwork";
    private static String[] cfgDirs = {"/cust_spec", "/hw_oem"};
    private static PreconfiguredNetworkManager instance = new PreconfiguredNetworkManager();
    private List<PreconfiguredNetwork> preconfiguredNetworks = new ArrayList();

    private PreconfiguredNetworkManager() {
        pasreConfigFile();
    }

    public static PreconfiguredNetworkManager getInstance() {
        return instance;
    }

    public boolean isPreconfiguredNetwork(String ssid) {
        int listSize = this.preconfiguredNetworks.size();
        for (int i = 0; i < listSize; i++) {
            PreconfiguredNetwork preconfiguredNetwork = this.preconfiguredNetworks.get(i);
            if (ssid.equals(preconfiguredNetwork.getSsid())) {
                return true;
            }
            if (ssid.equals("\"" + preconfiguredNetwork.getSsid() + "\"")) {
                return true;
            }
        }
        return false;
    }

    public PreconfiguredNetwork match(String ssid) {
        int listSize = this.preconfiguredNetworks.size();
        for (int i = 0; i < listSize; i++) {
            PreconfiguredNetwork preconfiguredNetwork = this.preconfiguredNetworks.get(i);
            if (preconfiguredNetwork.getSsid().equals(ssid)) {
                return preconfiguredNetwork;
            }
        }
        return null;
    }

    private File[] searchFile(File folder) {
        File[] subFolders = folder.listFiles(new FileFilter() {
            /* class com.android.server.wifi.PreconfiguredNetworkManager.AnonymousClass1 */

            @Override // java.io.FileFilter
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                if (!pathname.isFile() || !pathname.getName().equals(PreconfiguredNetworkManager.STORE_FILE_NAME)) {
                    return false;
                }
                return true;
            }
        });
        List<File> result = new ArrayList<>();
        if (subFolders != null) {
            for (File subFile : subFolders) {
                if (subFile.isFile()) {
                    result.add(subFile);
                } else {
                    for (File file : searchFile(subFile)) {
                        result.add(file);
                    }
                }
            }
        }
        return (File[]) result.toArray(new File[0]);
    }

    private void pasreConfigFile() {
        InputStream inputStream = null;
        File configFile = null;
        String[] strArr = cfgDirs;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String dir = strArr[i];
            File[] result = searchFile(new File(dir));
            if (result.length > 0) {
                HwHiLog.i("PreconfiguredNetwork", false, dir, new Object[0]);
                configFile = result[0];
                break;
            }
            i++;
        }
        if (configFile == null || !configFile.exists()) {
            HwHiLog.e("PreconfiguredNetwork", false, "file not find", new Object[0]);
            return;
        }
        try {
            InputStream inputStream2 = new FileInputStream(configFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream2, "UTF-8");
            XmlUtils.beginDocument(parser, PRECONFIGUREDNETWORKLIST_NODE_ROOT);
            while (true) {
                XmlUtils.nextElement(parser);
                if ("PreconfiguredNetwork".equalsIgnoreCase(parser.getName())) {
                    this.preconfiguredNetworks.add(new PreconfiguredNetwork(parser.getAttributeValue(null, PRECONFIGUREDNETWORK_SSID), getEapMethod(parser.getAttributeValue(null, PRECONFIGUREDNETWORK_EAPMETHOD))));
                } else {
                    try {
                        inputStream2.close();
                        return;
                    } catch (IOException e) {
                        HwHiLog.e("PreconfiguredNetwork", false, "Close inputStream error.", new Object[0]);
                        return;
                    }
                }
            }
        } catch (FileNotFoundException e2) {
            HwHiLog.e("PreconfiguredNetwork", false, "Exception in preconfiguredNetwork parse.", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (XmlPullParserException e3) {
            HwHiLog.e("PreconfiguredNetwork", false, "Exception in preconfiguredNetwork parse.", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (IOException e4) {
            HwHiLog.e("PreconfiguredNetwork", false, "Exception in preconfiguredNetwork parse.", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    HwHiLog.e("PreconfiguredNetwork", false, "Close inputStream error.", new Object[0]);
                }
            }
            throw th;
        }
    }

    private int getEapMethod(String eap_mode) {
        if (eap_mode.equals(EAP_AKA_METHOD)) {
            return 5;
        }
        if (eap_mode.equals(EAP_SIM_METHOD)) {
            return 4;
        }
        if (eap_mode.equals(EAP_AKA_PRIME_METHOD)) {
            return 6;
        }
        return 5;
    }
}
