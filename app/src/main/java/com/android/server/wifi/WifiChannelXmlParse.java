package com.android.server.wifi;

import android.util.Log;
import android.util.Xml;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
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
    private static boolean DBG = false;
    private static final String DEFAULT_CONFIG_NAME = "xml/wifi_channel_config.xml";
    protected static final boolean HWFLOW = false;
    private static final String TAG = "WifiChannelXmlParse";
    private static final String WIFI_CHANNEL_CFG = "wifiChannel";
    private static final String WIFI_CHANNEL_CFG_ROOT = "wifi_channel_config";
    private static WifiChannelXmlParse mWifiChannelXmlParse;
    private HashMap<String, WifiChannelCfg> mChannelsInfoFor2G;
    private HashMap<String, WifiChannelCfg> mChannelsInfoFor5G;

    private class WifiChannelCfg {
        String bandName;
        boolean enabled;
        boolean is2G;
        ArrayList<Integer> values;

        public WifiChannelCfg(String bandName, boolean enabled, String value) {
            this.values = new ArrayList();
            this.bandName = bandName;
            this.enabled = enabled;
            this.is2G = WifiChannelXmlParse.HWFLOW;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiChannelXmlParse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.WifiChannelXmlParse.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiChannelXmlParse.<clinit>():void");
    }

    private WifiChannelXmlParse() {
        this.mChannelsInfoFor2G = new HashMap();
        this.mChannelsInfoFor5G = new HashMap();
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

    private void pasreConfigFile(String fileName) throws IOException {
        FileNotFoundException e;
        Throwable th;
        XmlPullParserException e2;
        Exception e3;
        InputStream inputStream = null;
        File configFile = HwCfgFilePolicy.getCfgFile(fileName, 0);
        if (configFile == null || !configFile.exists()) {
            Log.e(TAG, "file " + fileName + " not find");
            return;
        }
        try {
            InputStream inputStream2 = new FileInputStream(configFile);
            try {
                if (DEFAULT_CONFIG_NAME.equals(fileName)) {
                    pasreConfigFileImpl(inputStream2, HWFLOW);
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
                inputStream = inputStream2;
            } catch (FileNotFoundException e5) {
                e = e5;
                inputStream = inputStream2;
                try {
                    e.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e42) {
                            e42.printStackTrace();
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e422) {
                            e422.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e6) {
                e2 = e6;
                inputStream = inputStream2;
                e2.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4222) {
                        e4222.printStackTrace();
                    }
                }
            } catch (Exception e7) {
                e3 = e7;
                inputStream = inputStream2;
                e3.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e42222) {
                        e42222.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = inputStream2;
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            e.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (XmlPullParserException e9) {
            e2 = e9;
            e2.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e10) {
            e3 = e10;
            e3.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void pasreConfigFileImpl(InputStream inputStream, boolean overlay) throws XmlPullParserException, IOException {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, "UTF-8");
        for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
            switch (event) {
                case MessageUtil.SWITCH_TO_WIFI_AUTO /*0*/:
                    if (!DBG) {
                        break;
                    }
                    Log.d(TAG, "START_DOCUMENT");
                    break;
                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                    if (!WIFI_CHANNEL_CFG.equals(pullParser.getName())) {
                        break;
                    }
                    String name = pullParser.getAttributeValue(0);
                    boolean enabled = 1 == Integer.valueOf(pullParser.getAttributeValue(1)).intValue() ? true : HWFLOW;
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
