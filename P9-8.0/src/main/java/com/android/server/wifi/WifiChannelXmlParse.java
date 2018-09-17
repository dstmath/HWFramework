package com.android.server.wifi;

import android.util.Log;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WifiChannelXmlParse {
    private static final String CUST_CONFIG_NAME = "xml/wifi_channel_config_cust.xml";
    private static boolean DBG = HWFLOW;
    private static final String DEFAULT_CONFIG_NAME = "xml/wifi_channel_config.xml";
    protected static final boolean HWFLOW;
    private static final String TAG = "WifiChannelXmlParse";
    private static final String WIFI_CHANNEL_CFG = "wifiChannel";
    private static final String WIFI_CHANNEL_CFG_ROOT = "wifi_channel_config";
    private static WifiChannelXmlParse mWifiChannelXmlParse = null;
    private HashMap<String, WifiChannelCfg> mChannelsInfoFor2G = new HashMap();
    private HashMap<String, WifiChannelCfg> mChannelsInfoFor5G = new HashMap();

    private class WifiChannelCfg {
        String bandName;
        boolean enabled;
        boolean is2G;
        ArrayList<Integer> values = new ArrayList();

        public WifiChannelCfg(String bandName, boolean enabled, String value) {
            this.bandName = bandName;
            this.enabled = enabled;
            this.is2G = false;
            String[] valueStr = value.split(",");
            int lenght = valueStr.length;
            int i = 0;
            while (i < lenght) {
                if ("A".equals(valueStr[i])) {
                    this.values.add(Integer.valueOf(36));
                    this.values.add(Integer.valueOf(38));
                    this.values.add(Integer.valueOf(40));
                    this.values.add(Integer.valueOf(42));
                    this.values.add(Integer.valueOf(44));
                    this.values.add(Integer.valueOf(46));
                    this.values.add(Integer.valueOf(48));
                } else if ("B".equals(valueStr[i])) {
                    this.values.add(Integer.valueOf(149));
                    this.values.add(Integer.valueOf(151));
                    this.values.add(Integer.valueOf(153));
                    this.values.add(Integer.valueOf(155));
                    this.values.add(Integer.valueOf(157));
                    this.values.add(Integer.valueOf(159));
                    this.values.add(Integer.valueOf(161));
                    this.values.add(Integer.valueOf(165));
                } else if ("C".equals(valueStr[i])) {
                    this.values.add(Integer.valueOf(153));
                    this.values.add(Integer.valueOf(155));
                    this.values.add(Integer.valueOf(157));
                    this.values.add(Integer.valueOf(159));
                    this.values.add(Integer.valueOf(161));
                    this.values.add(Integer.valueOf(165));
                } else if (Integer.valueOf(valueStr[i]).intValue() >= 1 && Integer.valueOf(valueStr[i]).intValue() <= 14) {
                    this.is2G = true;
                    this.values.add(Integer.valueOf(valueStr[i]));
                } else if (Integer.valueOf(valueStr[i]).intValue() >= 34 && Integer.valueOf(valueStr[i]).intValue() <= 189) {
                    this.values.add(Integer.valueOf(valueStr[i]));
                }
                i++;
            }
        }

        String getBandName() {
            return this.bandName;
        }

        ArrayList<Integer> getValues() {
            return this.values;
        }

        boolean is2G() {
            return this.is2G;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("bandName: ").append(this.bandName);
            sb.append(", enabled ").append(this.enabled);
            sb.append(", values: ");
            int lenght = this.values.size();
            for (int i = 0; i < lenght; i++) {
                sb.append(((Integer) this.values.get(i)).toString());
                sb.append(",");
            }
            return sb.toString();
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

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
            e.printStackTrace();
        }
    }

    public static WifiChannelXmlParse getInstance() {
        if (mWifiChannelXmlParse == null) {
            mWifiChannelXmlParse = new WifiChannelXmlParse();
        }
        return mWifiChannelXmlParse;
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x0086 A:{SYNTHETIC, Splitter: B:47:0x0086} */
    /* JADX WARNING: Removed duplicated region for block: B:65:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x007a A:{SYNTHETIC, Splitter: B:41:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:63:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x006b A:{SYNTHETIC, Splitter: B:33:0x006b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void pasreConfigFile(String fileName) throws IOException {
        FileNotFoundException e;
        XmlPullParserException e2;
        Exception e3;
        Throwable th;
        InputStream inputStream = null;
        File configFile = HwCfgFilePolicy.getCfgFile(fileName, 0);
        if (configFile == null || (configFile.exists() ^ 1) != 0) {
            Log.e(TAG, "file " + fileName + " not find");
            return;
        }
        try {
            InputStream inputStream2 = new FileInputStream(configFile);
            try {
                if (DEFAULT_CONFIG_NAME.equals(fileName)) {
                    pasreConfigFileImpl(inputStream2, false);
                } else {
                    pasreConfigFileImpl(inputStream2, true);
                }
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                inputStream = inputStream2;
            } catch (XmlPullParserException e6) {
                e2 = e6;
                inputStream = inputStream2;
                e2.printStackTrace();
                if (inputStream == null) {
                    try {
                        inputStream.close();
                    } catch (IOException e42) {
                        e42.printStackTrace();
                    }
                }
            } catch (Exception e7) {
                e3 = e7;
                inputStream = inputStream2;
                e3.printStackTrace();
                if (inputStream == null) {
                    try {
                        inputStream.close();
                    } catch (IOException e422) {
                        e422.printStackTrace();
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                inputStream = inputStream2;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4222) {
                        e4222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            try {
                e.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e42222) {
                        e42222.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (XmlPullParserException e9) {
            e2 = e9;
            e2.printStackTrace();
            if (inputStream == null) {
            }
        } catch (Exception e10) {
            e3 = e10;
            e3.printStackTrace();
            if (inputStream == null) {
            }
        }
    }

    private void pasreConfigFileImpl(InputStream inputStream, boolean overlay) throws XmlPullParserException, IOException {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, "UTF-8");
        for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
            switch (event) {
                case 0:
                    if (!DBG) {
                        break;
                    }
                    Log.d(TAG, "START_DOCUMENT");
                    break;
                case 2:
                    if (!WIFI_CHANNEL_CFG.equals(pullParser.getName())) {
                        break;
                    }
                    String name = pullParser.getAttributeValue(0);
                    boolean enabled = 1 == Integer.valueOf(pullParser.getAttributeValue(1)).intValue();
                    WifiChannelCfg wifiChannelCfg = new WifiChannelCfg(name, enabled, pullParser.getAttributeValue(2));
                    if (DBG) {
                        Log.d(TAG, "" + wifiChannelCfg.toString());
                    }
                    if (!enabled) {
                        if (overlay) {
                            if (!wifiChannelCfg.is2G()) {
                                this.mChannelsInfoFor5G.remove(name);
                                break;
                            } else {
                                this.mChannelsInfoFor2G.remove(name);
                                break;
                            }
                        }
                        break;
                    } else if (!wifiChannelCfg.is2G()) {
                        this.mChannelsInfoFor5G.put(name, wifiChannelCfg);
                        break;
                    } else {
                        this.mChannelsInfoFor2G.put(name, wifiChannelCfg);
                        break;
                    }
                default:
                    break;
            }
        }
    }

    public ArrayList<Integer> getValidChannels(String bandName, boolean is2G) {
        WifiChannelCfg wifiChannelCfg = is2G ? (WifiChannelCfg) this.mChannelsInfoFor2G.get(bandName) : (WifiChannelCfg) this.mChannelsInfoFor5G.get(bandName);
        if (DBG) {
            Log.d(TAG, "getValidChannels " + bandName);
        }
        if (wifiChannelCfg == null) {
            return null;
        }
        if (DBG) {
            Log.d(TAG, "getValidChannels got." + wifiChannelCfg.toString());
        }
        return wifiChannelCfg.getValues();
    }

    private void traverseChannels(HashMap<String, WifiChannelCfg> channelsInfo) {
        Log.d(TAG, "channelsInfo : ");
        for (Entry entry : channelsInfo.entrySet()) {
            Log.i(TAG, "" + ((WifiChannelCfg) entry.getValue()).toString());
        }
        Log.d(TAG, ":::end");
    }
}
