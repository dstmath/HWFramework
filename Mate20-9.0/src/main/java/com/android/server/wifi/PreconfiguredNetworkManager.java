package com.android.server.wifi;

import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
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
    private static String[] CFG_DIRS = {"/cust_spec", "/hw_oem"};
    private static final int EAP_AKA = 5;
    private static final String EAP_AKA_METHOD = "EAP_AKA";
    private static final int EAP_AKA_PRIME = 6;
    private static final String EAP_AKA_PRIME_METHOD = "EAP_AKA_PRIME";
    private static final int EAP_SIM = 4;
    private static final String EAP_SIM_METHOD = "EAP_SIM";
    public static final boolean IS_R1 = (SystemProperties.get("ro.config.hw_opta", "0").equals("389") && SystemProperties.get("ro.config.hw_optb", "0").equals("840"));
    private static final String PRECONFIGUREDNETWORKLIST_NODE_ROOT = "PreconfiguredNetworkList";
    private static final String PRECONFIGUREDNETWORK_EAPMETHOD = "eapMethod";
    private static final String PRECONFIGUREDNETWORK_NODE = "PreconfiguredNetwork";
    private static final String PRECONFIGUREDNETWORK_SSID = "ssid";
    private static final String STORE_FILE_NAME = "PreconfiguredNetwork.xml";
    private static final String TAG = "PreconfiguredNetwork";
    private static PreconfiguredNetworkManager instance = new PreconfiguredNetworkManager();
    private List<PreconfiguredNetwork> preconfiguredNetworks = new ArrayList();

    private PreconfiguredNetworkManager() {
        pasreConfigFile();
    }

    public static PreconfiguredNetworkManager getInstance() {
        return instance;
    }

    public boolean isPreconfiguredNetwork(String ssid) {
        int i = 0;
        int list_size = this.preconfiguredNetworks.size();
        while (i < list_size) {
            PreconfiguredNetwork preconfiguredNetwork = this.preconfiguredNetworks.get(i);
            if (!ssid.equals(preconfiguredNetwork.getSsid())) {
                if (!ssid.equals("\"" + preconfiguredNetwork.getSsid() + "\"")) {
                    i++;
                }
            }
            return true;
        }
        return false;
    }

    public PreconfiguredNetwork match(String ssid) {
        int list_size = this.preconfiguredNetworks.size();
        for (int i = 0; i < list_size; i++) {
            PreconfiguredNetwork preconfiguredNetwork = this.preconfiguredNetworks.get(i);
            if (preconfiguredNetwork.getSsid().equals(ssid)) {
                return preconfiguredNetwork;
            }
        }
        return null;
    }

    private File[] searchFile(File folder) {
        File[] subFolders = folder.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory() || (pathname.isFile() && pathname.getName().equals(PreconfiguredNetworkManager.STORE_FILE_NAME))) {
                    return true;
                }
                return false;
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
        String[] strArr = CFG_DIRS;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String dir = strArr[i];
            File[] result = searchFile(new File(dir));
            if (result.length > 0) {
                Log.i("PreconfiguredNetwork", dir);
                configFile = result[0];
                break;
            }
            i++;
        }
        if (configFile == null || !configFile.exists()) {
            Log.e("PreconfiguredNetwork", "file not find");
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
                    this.preconfiguredNetworks.add(new PreconfiguredNetwork(parser.getAttributeValue(null, "ssid"), getEapMethod(parser.getAttributeValue(null, PRECONFIGUREDNETWORK_EAPMETHOD))));
                } else {
                    try {
                        inputStream2.close();
                        return;
                    } catch (IOException e) {
                        Log.e("PreconfiguredNetwork", "Close inputStream error.");
                        return;
                    }
                }
            }
        } catch (FileNotFoundException e2) {
            Log.e("PreconfiguredNetwork", "Exception in preconfiguredNetwork parse.");
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (XmlPullParserException e3) {
            Log.e("PreconfiguredNetwork", "Exception in preconfiguredNetwork parse.");
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e4) {
            Log.e("PreconfiguredNetwork", "Exception in preconfiguredNetwork parse.");
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    Log.e("PreconfiguredNetwork", "Close inputStream error.");
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
