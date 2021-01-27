package com.android.server.wifi;

import android.util.Log;
import android.util.Xml;
import android.util.wifi.HwHiLog;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WifiChannelXmlParse {
    private static final String CUST_CONFIG_NAME = "xml/wifi_channel_config_cust.xml";
    private static boolean DBG = HWFLOW;
    private static final String DEFAULT_CONFIG_NAME = "xml/wifi_channel_config.xml";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "WifiChannelXmlParse";
    private static final String WIFI_CHANNEL_CFG = "wifiChannel";
    private static final String WIFI_CHANNEL_CFG_ROOT = "wifi_channel_config";
    private static WifiChannelXmlParse mWifiChannelXmlParse = null;
    private HashMap<String, WifiChannelCfg> mChannelsInfoFor2G = new HashMap<>();
    private HashMap<String, WifiChannelCfg> mChannelsInfoFor5G = new HashMap<>();

    private WifiChannelXmlParse() {
        try {
            pasreConfigFile(DEFAULT_CONFIG_NAME);
            if (DBG) {
                traverseChannels(this.mChannelsInfoFor2G);
                traverseChannels(this.mChannelsInfoFor5G);
            }
            pasreConfigFile(CUST_CONFIG_NAME);
            if (DBG) {
                traverseChannels(this.mChannelsInfoFor2G);
                traverseChannels(this.mChannelsInfoFor5G);
            }
        } catch (IOException e) {
            HwHiLog.e(TAG, false, "pasreConfigFile failed", new Object[0]);
        }
    }

    public static WifiChannelXmlParse getInstance() {
        if (mWifiChannelXmlParse == null) {
            mWifiChannelXmlParse = new WifiChannelXmlParse();
        }
        return mWifiChannelXmlParse;
    }

    private void pasreConfigFile(String fileName) throws IOException {
        InputStream inputStream = null;
        File configFile = HwCfgFilePolicy.getCfgFile(fileName, 0);
        if (configFile == null || !configFile.exists()) {
            HwHiLog.e(TAG, false, "file %{public}s not find", new Object[]{fileName});
            return;
        }
        try {
            InputStream inputStream2 = new FileInputStream(configFile);
            if (DEFAULT_CONFIG_NAME.equals(fileName)) {
                pasreConfigFileImpl(inputStream2, false);
            } else {
                pasreConfigFileImpl(inputStream2, true);
            }
            try {
                inputStream2.close();
            } catch (IOException e) {
                HwHiLog.e(TAG, false, "pasreConfigFile failed", new Object[0]);
            }
        } catch (FileNotFoundException e2) {
            HwHiLog.e(TAG, false, "pasreConfigFileImpl error", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (XmlPullParserException e3) {
            HwHiLog.e(TAG, false, "pasreConfigFileImpl happen fail", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Exception e4) {
            HwHiLog.e(TAG, false, "pasreConfigFileImpl failed", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    HwHiLog.e(TAG, false, "pasreConfigFile failed", new Object[0]);
                }
            }
            throw th;
        }
    }

    private void pasreConfigFileImpl(InputStream inputStream, boolean overlay) throws XmlPullParserException, IOException {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, "UTF-8");
        for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
            if (event != 0) {
                if (event != 2) {
                    if (event != 3) {
                    }
                } else if (WIFI_CHANNEL_CFG.equals(pullParser.getName())) {
                    String name = pullParser.getAttributeValue(0);
                    boolean enabled = 1 == Integer.valueOf(pullParser.getAttributeValue(1)).intValue();
                    WifiChannelCfg wifiChannelCfg = new WifiChannelCfg(name, enabled, pullParser.getAttributeValue(2));
                    if (DBG) {
                        HwHiLog.d(TAG, false, "%{public}s", new Object[]{wifiChannelCfg.toString()});
                    }
                    if (enabled) {
                        if (wifiChannelCfg.is2G()) {
                            this.mChannelsInfoFor2G.put(name, wifiChannelCfg);
                        } else {
                            this.mChannelsInfoFor5G.put(name, wifiChannelCfg);
                        }
                    } else if (overlay) {
                        if (wifiChannelCfg.is2G()) {
                            this.mChannelsInfoFor2G.remove(name);
                        } else {
                            this.mChannelsInfoFor5G.remove(name);
                        }
                    }
                }
            } else if (DBG) {
                HwHiLog.d(TAG, false, "START_DOCUMENT", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    public class WifiChannelCfg {
        String bandName;
        boolean enabled;
        boolean is2G;
        ArrayList<Integer> values = new ArrayList<>();

        public WifiChannelCfg(String bandName2, boolean enabled2, String value) {
            this.bandName = bandName2;
            this.enabled = enabled2;
            this.is2G = false;
            String[] valueStr = value.split(",");
            int lenght = valueStr.length;
            for (int i = 0; i < lenght; i++) {
                if ("A".equals(valueStr[i])) {
                    this.values.add(36);
                    this.values.add(38);
                    this.values.add(40);
                    this.values.add(42);
                    this.values.add(44);
                    this.values.add(46);
                    this.values.add(48);
                } else if ("B".equals(valueStr[i])) {
                    this.values.add(149);
                    this.values.add(151);
                    this.values.add(153);
                    this.values.add(155);
                    this.values.add(157);
                    this.values.add(159);
                    this.values.add(161);
                    this.values.add(165);
                } else if ("C".equals(valueStr[i])) {
                    this.values.add(153);
                    this.values.add(155);
                    this.values.add(157);
                    this.values.add(159);
                    this.values.add(161);
                    this.values.add(165);
                } else if (Integer.valueOf(valueStr[i]).intValue() >= 1 && Integer.valueOf(valueStr[i]).intValue() <= 14) {
                    this.is2G = true;
                    this.values.add(Integer.valueOf(valueStr[i]));
                } else if (Integer.valueOf(valueStr[i]).intValue() >= 34 && Integer.valueOf(valueStr[i]).intValue() <= 189) {
                    this.values.add(Integer.valueOf(valueStr[i]));
                }
            }
        }

        /* access modifiers changed from: package-private */
        public String getBandName() {
            return this.bandName;
        }

        /* access modifiers changed from: package-private */
        public ArrayList<Integer> getValues() {
            return this.values;
        }

        /* access modifiers changed from: package-private */
        public boolean is2G() {
            return this.is2G;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("bandName: " + this.bandName);
            sb.append(", enabled " + this.enabled);
            sb.append(", values: ");
            int lenght = this.values.size();
            for (int i = 0; i < lenght; i++) {
                sb.append(this.values.get(i).toString());
                sb.append(",");
            }
            return sb.toString();
        }
    }

    public ArrayList<Integer> getValidChannels(String bandName, boolean is2G) {
        WifiChannelCfg wifiChannelCfg = (is2G ? this.mChannelsInfoFor2G : this.mChannelsInfoFor5G).get(bandName);
        if (DBG) {
            HwHiLog.d(TAG, false, "getValidChannels %{public}s", new Object[]{bandName});
        }
        if (wifiChannelCfg == null) {
            return null;
        }
        if (DBG) {
            HwHiLog.d(TAG, false, "%{public}s getValidChannels got.", new Object[]{wifiChannelCfg.toString()});
        }
        return wifiChannelCfg.getValues();
    }

    private void traverseChannels(HashMap<String, WifiChannelCfg> channelsInfo) {
        Iterator iter = channelsInfo.entrySet().iterator();
        HwHiLog.d(TAG, false, "channelsInfo : ", new Object[0]);
        while (iter.hasNext()) {
            HwHiLog.i(TAG, false, "%{public}s", new Object[]{iter.next().getValue().toString()});
        }
        HwHiLog.d(TAG, false, ":::end", new Object[0]);
    }
}
