package com.android.server.wifi;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SoftApChannelXmlParse {
    private static final String CONF_FILE_NAME = "softap_channels_policy.xml";
    private static final boolean DBG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "SoftApChannelXmlParse";
    private static final String XML_TAG_CHANNELS_POLICY = "channels_policy";
    private static final String XML_TAG_COUNTRY_CODE = "country_code";
    private static final String XML_TAG_DFS_CHANNELS = "dfs_channels";
    private static final String XML_TAG_INDOOR_CHANNELS = "indoor_channels";
    private static final String XML_TAG_VERSION = "version_number";
    private static final String XML_TAG_VHT160_CHANNELS = "vht160_channels";
    private Integer[] defaultIndoorChannelArray = {36, 40, 44, 48, 52, 56, 60, 64};
    private ChannelsPolicyGroup mChannelsPolicy = null;
    private Context mContext;

    public SoftApChannelXmlParse(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: Removed duplicated region for block: B:107:0x0200 A[SYNTHETIC, Splitter:B:107:0x0200] */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x021f  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x0232 A[SYNTHETIC, Splitter:B:119:0x0232] */
    /* JADX WARNING: Removed duplicated region for block: B:127:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:128:? A[RETURN, SYNTHETIC] */
    private void parseConfFile(Context context, String currentCCode) {
        InputStream inputStream;
        Throwable th;
        IOException e;
        XmlPullParserException e2;
        String countryCode = null;
        String indoorChannels = null;
        String dfsChannels = null;
        String vht160Channels = null;
        this.mChannelsPolicy = null;
        boolean isCCodeFound = false;
        InputStream inputStream2 = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            inputStream = context.getAssets().open(CONF_FILE_NAME);
            try {
                parser.setInput(inputStream, "UTF-8");
                String countryCode2 = null;
                String indoorChannels2 = null;
                String dfsChannels2 = null;
                String vht160Channels2 = null;
                boolean isCCodeFound2 = false;
                int eventType = parser.getEventType();
                while (true) {
                    if (eventType == 1) {
                        break;
                    }
                    if (eventType != 0) {
                        if (eventType != 2) {
                            if (eventType == 3) {
                                try {
                                    if (XML_TAG_CHANNELS_POLICY.equals(parser.getName())) {
                                        if (isCCodeFound2 && this.mChannelsPolicy == null) {
                                            this.mChannelsPolicy = new ChannelsPolicyGroup(countryCode2, indoorChannels2, dfsChannels2, vht160Channels2);
                                        }
                                    }
                                } catch (IOException e3) {
                                    e = e3;
                                    inputStream2 = inputStream;
                                    HwHiLog.e(TAG, false, "%{public}s", new Object[]{e.getMessage()});
                                    if (inputStream2 == null) {
                                    }
                                } catch (XmlPullParserException e4) {
                                    e2 = e4;
                                    inputStream2 = inputStream;
                                    isCCodeFound = isCCodeFound2;
                                    countryCode = countryCode2;
                                    indoorChannels = indoorChannels2;
                                    dfsChannels = dfsChannels2;
                                    vht160Channels = vht160Channels2;
                                    try {
                                        HwHiLog.e(TAG, false, "%{public}s", new Object[]{e2.getMessage()});
                                        if (inputStream2 == null) {
                                        }
                                    } catch (Throwable th2) {
                                        inputStream = inputStream2;
                                        th = th2;
                                        if (inputStream != null) {
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    if (inputStream != null) {
                                    }
                                    throw th;
                                }
                            }
                        } else if (XML_TAG_VERSION.equals(parser.getName())) {
                            if (DBG) {
                                HwHiLog.d(TAG, false, "softap_channels_policy VERSION = %{public}s", new Object[]{parser.nextText()});
                            }
                        } else if (XML_TAG_COUNTRY_CODE.equals(parser.getName())) {
                            countryCode = parser.nextText();
                            if (countryCode != null) {
                                try {
                                } catch (IOException e5) {
                                    e = e5;
                                    inputStream2 = inputStream;
                                    HwHiLog.e(TAG, false, "%{public}s", new Object[]{e.getMessage()});
                                    if (inputStream2 == null) {
                                    }
                                } catch (XmlPullParserException e6) {
                                    e2 = e6;
                                    inputStream2 = inputStream;
                                    isCCodeFound = isCCodeFound2;
                                    indoorChannels = indoorChannels2;
                                    dfsChannels = dfsChannels2;
                                    vht160Channels = vht160Channels2;
                                    HwHiLog.e(TAG, false, "%{public}s", new Object[]{e2.getMessage()});
                                    if (inputStream2 == null) {
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    if (inputStream != null) {
                                    }
                                    throw th;
                                }
                                try {
                                    if (countryCode.trim().equals(currentCCode)) {
                                        countryCode2 = countryCode;
                                        isCCodeFound2 = true;
                                    }
                                } catch (IOException e7) {
                                    e = e7;
                                    inputStream2 = inputStream;
                                    HwHiLog.e(TAG, false, "%{public}s", new Object[]{e.getMessage()});
                                    if (inputStream2 == null) {
                                    }
                                } catch (XmlPullParserException e8) {
                                    e2 = e8;
                                    inputStream2 = inputStream;
                                    isCCodeFound = isCCodeFound2;
                                    indoorChannels = indoorChannels2;
                                    dfsChannels = dfsChannels2;
                                    vht160Channels = vht160Channels2;
                                    HwHiLog.e(TAG, false, "%{public}s", new Object[]{e2.getMessage()});
                                    if (inputStream2 == null) {
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    if (inputStream != null) {
                                    }
                                    throw th;
                                }
                            }
                            countryCode2 = countryCode;
                        } else {
                            try {
                                if (XML_TAG_INDOOR_CHANNELS.equals(parser.getName())) {
                                    indoorChannels2 = parser.nextText();
                                } else if (XML_TAG_DFS_CHANNELS.equals(parser.getName())) {
                                    dfsChannels2 = parser.nextText();
                                } else if (XML_TAG_VHT160_CHANNELS.equals(parser.getName())) {
                                    vht160Channels2 = parser.nextText();
                                }
                            } catch (IOException e9) {
                                e = e9;
                                inputStream2 = inputStream;
                                HwHiLog.e(TAG, false, "%{public}s", new Object[]{e.getMessage()});
                                if (inputStream2 == null) {
                                }
                            } catch (XmlPullParserException e10) {
                                e2 = e10;
                                inputStream2 = inputStream;
                                isCCodeFound = isCCodeFound2;
                                countryCode = countryCode2;
                                indoorChannels = indoorChannels2;
                                dfsChannels = dfsChannels2;
                                vht160Channels = vht160Channels2;
                                HwHiLog.e(TAG, false, "%{public}s", new Object[]{e2.getMessage()});
                                if (inputStream2 == null) {
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                if (inputStream != null) {
                                }
                                throw th;
                            }
                        }
                    } else if (DBG) {
                        HwHiLog.d(TAG, false, "START_DOCUMENT", new Object[0]);
                    }
                    if (isCCodeFound2 && this.mChannelsPolicy != null) {
                        HwHiLog.d(TAG, false, "channel policy found: %{public}s", new Object[]{this.mChannelsPolicy.toString()});
                        break;
                    }
                    eventType = parser.next();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e11) {
                        HwHiLog.e(TAG, false, "parseConf fail", new Object[0]);
                    }
                }
            } catch (IOException e12) {
                e = e12;
                inputStream2 = inputStream;
                HwHiLog.e(TAG, false, "%{public}s", new Object[]{e.getMessage()});
                if (inputStream2 == null) {
                }
            } catch (XmlPullParserException e13) {
                e2 = e13;
                inputStream2 = inputStream;
                HwHiLog.e(TAG, false, "%{public}s", new Object[]{e2.getMessage()});
                if (inputStream2 == null) {
                }
            } catch (Throwable th7) {
                th = th7;
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (IOException e14) {
            e = e14;
            HwHiLog.e(TAG, false, "%{public}s", new Object[]{e.getMessage()});
            if (inputStream2 == null) {
                inputStream2.close();
            }
        } catch (XmlPullParserException e15) {
            e2 = e15;
            HwHiLog.e(TAG, false, "%{public}s", new Object[]{e2.getMessage()});
            if (inputStream2 == null) {
                try {
                    inputStream2.close();
                } catch (Exception e16) {
                    HwHiLog.e(TAG, false, "parseConf fail", new Object[0]);
                }
            }
        } catch (Throwable th8) {
            inputStream = null;
            th = th8;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e17) {
                    HwHiLog.e(TAG, false, "parseConf fail", new Object[0]);
                }
            }
            throw th;
        }
    }

    private List<Integer> getIndoorChannelList(String countryCode) {
        ChannelsPolicyGroup channelsPolicyGroup = this.mChannelsPolicy;
        if (channelsPolicyGroup == null || !countryCode.equals(channelsPolicyGroup.countryCode)) {
            parseConfFile(this.mContext, countryCode);
        }
        if (this.mChannelsPolicy == null) {
            this.mChannelsPolicy = new ChannelsPolicyGroup(countryCode, Arrays.asList(this.defaultIndoorChannelArray), null);
        }
        if (1 == this.mChannelsPolicy.indoorChannels.size() && this.mChannelsPolicy.indoorChannels.get(0).intValue() == 0) {
            return null;
        }
        return this.mChannelsPolicy.indoorChannels;
    }

    private List<Integer> getDfsChannelList(String countryCode) {
        ChannelsPolicyGroup channelsPolicyGroup = this.mChannelsPolicy;
        if (channelsPolicyGroup == null || !countryCode.equals(channelsPolicyGroup.countryCode)) {
            parseConfFile(this.mContext, countryCode);
        }
        if (this.mChannelsPolicy == null) {
            this.mChannelsPolicy = new ChannelsPolicyGroup(countryCode, Arrays.asList(this.defaultIndoorChannelArray), null);
        }
        if (1 == this.mChannelsPolicy.dfsChannels.size() && this.mChannelsPolicy.dfsChannels.get(0).intValue() == 0) {
            return null;
        }
        return this.mChannelsPolicy.dfsChannels;
    }

    /* access modifiers changed from: private */
    public class ChannelsPolicyGroup {
        private static final int INITIAL_SIZE = 10;
        public String countryCode = null;
        public List<Integer> dfsChannels = new ArrayList();
        public List<Integer> indoorChannels = new ArrayList();
        private List<Integer> vht160Channels = new ArrayList(10);

        public ChannelsPolicyGroup(String ccode, String indoor, String dfs, String vht160) {
            this.countryCode = ccode;
            setIndoorChannels(indoor);
            setDfsChannels(dfs);
            setVht160Channels(vht160);
        }

        public ChannelsPolicyGroup(String ccode, List<Integer> indoor, List<Integer> dfs) {
            this.countryCode = ccode;
            if (indoor != null) {
                this.indoorChannels.addAll(indoor);
            }
            if (dfs != null) {
                this.dfsChannels.addAll(dfs);
            }
        }

        private void setIndoorChannels(String indoorChannelsFromXml) {
            if (TextUtils.isEmpty(indoorChannelsFromXml)) {
                this.indoorChannels.addAll(Arrays.asList(SoftApChannelXmlParse.this.defaultIndoorChannelArray));
                return;
            }
            String[] indoorChannelsStr = indoorChannelsFromXml.split(",");
            for (int i = 0; i < indoorChannelsStr.length; i++) {
                try {
                    this.indoorChannels.add(Integer.valueOf(indoorChannelsStr[i].trim()));
                } catch (Exception e) {
                    HwHiLog.e(SoftApChannelXmlParse.TAG, false, "setIndoorChannels fail", new Object[0]);
                }
            }
        }

        private void setDfsChannels(String dfsChannelsFromXml) {
            if (!TextUtils.isEmpty(dfsChannelsFromXml)) {
                String[] dfsChannelsStr = dfsChannelsFromXml.split(",");
                for (int i = 0; i < dfsChannelsStr.length; i++) {
                    try {
                        this.dfsChannels.add(Integer.valueOf(dfsChannelsStr[i].trim()));
                    } catch (Exception e) {
                        HwHiLog.e(SoftApChannelXmlParse.TAG, false, "setDfsChannels fail", new Object[0]);
                    }
                }
            }
        }

        private void setVht160Channels(String vht160ChannelsFromXml) {
            String[] vht160ChannelsStr;
            if (!TextUtils.isEmpty(vht160ChannelsFromXml)) {
                for (String str : vht160ChannelsFromXml.split(",")) {
                    this.vht160Channels.add(Integer.valueOf(str.trim()));
                }
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("indoorChannels: ");
            int indoorChannelsLenght = this.indoorChannels.size();
            for (int i = 0; i < indoorChannelsLenght; i++) {
                sb.append(this.indoorChannels.get(i).toString());
                sb.append(",");
            }
            sb.append(", dfsChannels: ");
            int dfsChannelsLenght = this.dfsChannels.size();
            for (int i2 = 0; i2 < dfsChannelsLenght; i2++) {
                sb.append(this.dfsChannels.get(i2).toString());
                sb.append(",");
            }
            sb.append(", vht160Channels: ");
            int vht160ChannelsLength = this.vht160Channels.size();
            for (int i3 = 0; i3 < vht160ChannelsLength; i3++) {
                sb.append(this.vht160Channels.get(i3).toString());
                sb.append(",");
            }
            return sb.toString();
        }
    }

    public boolean isIndoorChannel(int frequency, String countryCode) {
        List<Integer> indoorChannelList = getIndoorChannelList(countryCode);
        if (indoorChannelList == null || indoorChannelList.size() == 0) {
            return false;
        }
        return indoorChannelList.contains(Integer.valueOf(WifiCommonUtils.convertFrequencyToChannelNumber(frequency)));
    }

    public int[] getChannelListWithoutIndoor(int[] origChannels, String countryCode) {
        List<Integer> indoorChannelList = getIndoorChannelList(countryCode);
        if (origChannels == null || origChannels.length == 0) {
            return null;
        }
        if (indoorChannelList == null || indoorChannelList.size() == 0) {
            return (int[]) origChannels.clone();
        }
        int[] channelsWithoutIndoor = new int[origChannels.length];
        int index = 0;
        for (int i = 0; i < origChannels.length; i++) {
            if (!indoorChannelList.contains(Integer.valueOf(origChannels[i]))) {
                channelsWithoutIndoor[index] = origChannels[i];
                index++;
            }
        }
        return Arrays.copyOf(channelsWithoutIndoor, index);
    }

    public int[] getVht160Channels(String countryCode) {
        ChannelsPolicyGroup channelsPolicyGroup = this.mChannelsPolicy;
        if (channelsPolicyGroup == null || !countryCode.equals(channelsPolicyGroup.countryCode)) {
            parseConfFile(this.mContext, countryCode);
        }
        ChannelsPolicyGroup channelsPolicyGroup2 = this.mChannelsPolicy;
        if (channelsPolicyGroup2 == null) {
            HwHiLog.d(TAG, false, "mChannelsPolicy is null", new Object[0]);
            return null;
        }
        int size = channelsPolicyGroup2.vht160Channels.size();
        int[] channels = new int[size];
        for (int i = 0; i < size; i++) {
            channels[i] = ((Integer) this.mChannelsPolicy.vht160Channels.get(i)).intValue();
        }
        return channels;
    }

    public static int[] convertChannelListToFrequency(int[] channels) {
        if (channels == null || channels.length == 0) {
            return channels;
        }
        int[] frequency = new int[channels.length];
        for (int i = 0; i < channels.length; i++) {
            frequency[i] = WifiCommonUtils.convertChannelToFrequency(channels[i]);
        }
        return frequency;
    }

    public static int[] convertFrequencyListToChannel(int[] frequency) {
        if (frequency == null || frequency.length == 0) {
            return frequency;
        }
        int[] channels = new int[frequency.length];
        for (int i = 0; i < frequency.length; i++) {
            channels[i] = WifiCommonUtils.convertFrequencyToChannelNumber(frequency[i]);
        }
        return channels;
    }
}
