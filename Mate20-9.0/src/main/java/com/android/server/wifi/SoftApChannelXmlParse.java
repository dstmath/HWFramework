package com.android.server.wifi;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.server.wifi.util.WifiCommonUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SoftApChannelXmlParse {
    private static final int CH_2G_MAX = 14;
    private static final int CH_2G_MIN = 1;
    private static final int CH_5G_MAX = 165;
    private static final int CH_5G_MIN = 36;
    private static final String CONF_FILE_NAME = "softap_channels_policy.xml";
    private static final boolean DBG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int FREQ_CH1 = 2412;
    private static final int FREQ_CH14 = 2484;
    private static final int FREQ_CH36 = 5180;
    private static final int FREQ_DIFF_PER_CH = 5;
    private static final String TAG = "SoftApChannelXmlParse";
    private static final String XML_TAG_CHANNELS_POLICY = "channels_policy";
    private static final String XML_TAG_COUNTRY_CODE = "country_code";
    private static final String XML_TAG_DFS_CHANNELS = "dfs_channels";
    private static final String XML_TAG_INDOOR_CHANNELS = "indoor_channels";
    private static final String XML_TAG_VERSION = "version_number";
    private Integer[] defaultIndoorChannelArray = {36, 40, 44, 48, 52, 56, 60, 64};
    private ChannelsPolicyGroup mChannelsPolicy = null;
    private Context mContext;

    private class ChannelsPolicyGroup {
        public String countryCode = null;
        public List<Integer> dfsChannels = new ArrayList();
        public List<Integer> indoorChannels = new ArrayList();

        public ChannelsPolicyGroup(String ccode, String indoor, String dfs) {
            this.countryCode = ccode;
            setIndoorChannels(indoor);
            setDfsChannels(dfs);
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
            if (!TextUtils.isEmpty(indoorChannelsFromXml)) {
                String[] indoorChannelsStr = indoorChannelsFromXml.split(",");
                for (int i = 0; i < indoorChannelsStr.length; i++) {
                    try {
                        this.indoorChannels.add(Integer.valueOf(indoorChannelsStr[i].trim()));
                    } catch (Exception e) {
                        Log.e(SoftApChannelXmlParse.TAG, e.getMessage());
                    }
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
                        Log.e(SoftApChannelXmlParse.TAG, e.getMessage());
                    }
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
            return sb.toString();
        }
    }

    public SoftApChannelXmlParse(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [com.android.server.wifi.SoftApChannelXmlParse$ChannelsPolicyGroup, java.io.InputStream] */
    private void parseConfFile(Context context, String currentCCode) {
        String countryCode = null;
        String indoorChannels = null;
        String dfsChannels = null;
        ? r3 = 0;
        this.mChannelsPolicy = r3;
        boolean isCCodeFound = false;
        try {
            XmlPullParser parser = Xml.newPullParser();
            InputStream inputStream = context.getAssets().open(CONF_FILE_NAME);
            parser.setInput(inputStream, "UTF-8");
            int eventType = parser.getEventType();
            while (true) {
                if (eventType != 1) {
                    if (eventType != 0) {
                        switch (eventType) {
                            case 2:
                                if (!XML_TAG_VERSION.equals(parser.getName())) {
                                    if (!XML_TAG_COUNTRY_CODE.equals(parser.getName())) {
                                        if (!XML_TAG_INDOOR_CHANNELS.equals(parser.getName())) {
                                            if (XML_TAG_DFS_CHANNELS.equals(parser.getName())) {
                                                dfsChannels = parser.nextText();
                                                break;
                                            }
                                        } else {
                                            indoorChannels = parser.nextText();
                                            break;
                                        }
                                    } else {
                                        countryCode = parser.nextText();
                                        if (countryCode != null && countryCode.trim().equals(currentCCode)) {
                                            isCCodeFound = true;
                                            break;
                                        }
                                    }
                                } else if (DBG) {
                                    Log.d(TAG, "softap_channels_policy VERSION = " + parser.nextText());
                                    break;
                                }
                                break;
                            case 3:
                                if (XML_TAG_CHANNELS_POLICY.equals(parser.getName()) && isCCodeFound && this.mChannelsPolicy == null) {
                                    this.mChannelsPolicy = new ChannelsPolicyGroup(countryCode, indoorChannels, dfsChannels);
                                    break;
                                }
                        }
                    } else if (DBG) {
                        Log.d(TAG, "START_DOCUMENT");
                    }
                    if (!isCCodeFound || this.mChannelsPolicy == null) {
                        eventType = parser.next();
                    }
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        } catch (IOException e2) {
            Log.e(TAG, e2.getMessage());
            if (r3 != 0) {
                r3.close();
            }
        } catch (XmlPullParserException e3) {
            Log.e(TAG, e3.getMessage());
            if (r3 != 0) {
                r3.close();
            }
        } catch (Throwable th) {
            if (r3 != 0) {
                try {
                    r3.close();
                } catch (Exception e4) {
                    Log.e(TAG, e4.getMessage());
                }
            }
            throw th;
        }
    }

    private List<Integer> getIndoorChannelList(String countryCode) {
        if (this.mChannelsPolicy == null || !countryCode.equals(this.mChannelsPolicy.countryCode)) {
            parseConfFile(this.mContext, countryCode);
        }
        if (this.mChannelsPolicy == null) {
            this.mChannelsPolicy = new ChannelsPolicyGroup(countryCode, (List<Integer>) Arrays.asList(this.defaultIndoorChannelArray), (List<Integer>) null);
        }
        if (1 == this.mChannelsPolicy.indoorChannels.size() && this.mChannelsPolicy.indoorChannels.get(0).intValue() == 0) {
            return null;
        }
        return this.mChannelsPolicy.indoorChannels;
    }

    private List<Integer> getDfsChannelList(String countryCode) {
        if (this.mChannelsPolicy == null || !countryCode.equals(this.mChannelsPolicy.countryCode)) {
            parseConfFile(this.mContext, countryCode);
        }
        if (this.mChannelsPolicy == null) {
            this.mChannelsPolicy = new ChannelsPolicyGroup(countryCode, (List<Integer>) Arrays.asList(this.defaultIndoorChannelArray), (List<Integer>) null);
        }
        if (1 == this.mChannelsPolicy.dfsChannels.size() && this.mChannelsPolicy.dfsChannels.get(0).intValue() == 0) {
            return null;
        }
        return this.mChannelsPolicy.dfsChannels;
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

    private static int convertChannelToFrequency(int channel) {
        if (channel >= 1 && channel < 14) {
            return ((channel - 1) * 5) + FREQ_CH1;
        }
        if (channel == 14) {
            return FREQ_CH14;
        }
        if (channel < 36 || channel > CH_5G_MAX) {
            return -1;
        }
        return ((channel - 36) * 5) + FREQ_CH36;
    }

    public static int[] convertChannelListToFrequency(int[] channels) {
        if (channels == null || channels.length == 0) {
            return channels;
        }
        int[] frequency = new int[channels.length];
        for (int i = 0; i < channels.length; i++) {
            frequency[i] = convertChannelToFrequency(channels[i]);
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
