package com.huawei.displayengine;

import android.rms.iaware.AppTypeInfo;
import android.util.Log;
import android.util.Slog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class DisplayEngineDataCleanerXMLLoader {
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    /* access modifiers changed from: private */
    public static final boolean HWFLOW;
    private static final String TAG = "DisplayEngineDataCleanerXMLLoader";
    /* access modifiers changed from: private */
    public static Data mData = new Data();
    private static DisplayEngineDataCleanerXMLLoader mLoader;
    private static final Object mLock = new Object();

    public static class Data implements Cloneable {
        public int alDarkThresh = 10;
        public ArrayList<Integer> ambientLightLUT;
        public ArrayList<Float> brightnessLevelLUT;
        public float comfortZoneCounterWeight = 0.2f;
        public float counterWeightThresh = 0.5f;
        public ArrayList<Integer> darkLevelLUT;
        public ArrayList<Float> darkLevelRoofLUT;
        public int hbmTresh = 3000;
        public int outDoorLevelFloor = 139;
        public float outlierZoneCounterWeight = 0.5f;
        public float safeZoneCounterWeight = 0.3f;

        /* access modifiers changed from: protected */
        public Object clone() throws CloneNotSupportedException {
            Data newData = (Data) super.clone();
            newData.ambientLightLUT = cloneIntegerList(this.ambientLightLUT);
            newData.brightnessLevelLUT = cloneFloatList(this.brightnessLevelLUT);
            newData.darkLevelLUT = cloneIntegerList(this.darkLevelLUT);
            newData.darkLevelRoofLUT = cloneFloatList(this.darkLevelRoofLUT);
            return newData;
        }

        private ArrayList<Integer> cloneIntegerList(ArrayList<Integer> list) {
            if (list == null) {
                return null;
            }
            try {
                return new ArrayList<>(list);
            } catch (Exception e) {
                Slog.e(DisplayEngineDataCleanerXMLLoader.TAG, "cloneList() error!" + e);
                return list;
            }
        }

        private ArrayList<Float> cloneFloatList(ArrayList<Float> list) {
            if (list == null) {
                return null;
            }
            try {
                return new ArrayList<>(list);
            } catch (Exception e) {
                Slog.e(DisplayEngineDataCleanerXMLLoader.TAG, "cloneList() error!" + e);
                return list;
            }
        }

        public void printData() {
            if (DisplayEngineDataCleanerXMLLoader.HWFLOW) {
                Slog.i(DisplayEngineDataCleanerXMLLoader.TAG, "printData() comfortZoneCounterWeight=" + this.comfortZoneCounterWeight + ", safeZoneCounterWeight=" + this.safeZoneCounterWeight + ", outlierZoneCounterWeight=" + this.outlierZoneCounterWeight + ", counterWeightThresh=" + this.counterWeightThresh);
                StringBuilder sb = new StringBuilder();
                sb.append("alDarkThresh=");
                sb.append(this.alDarkThresh);
                sb.append(", hbmTresh=");
                sb.append(this.hbmTresh);
                sb.append(", outDoorLevelFloor=");
                sb.append(this.outDoorLevelFloor);
                Slog.i(DisplayEngineDataCleanerXMLLoader.TAG, sb.toString());
                Slog.i(DisplayEngineDataCleanerXMLLoader.TAG, "ambientLightLUT=" + this.ambientLightLUT);
                Slog.i(DisplayEngineDataCleanerXMLLoader.TAG, "brightnessLevelLUT=" + this.brightnessLevelLUT);
                Slog.i(DisplayEngineDataCleanerXMLLoader.TAG, "darkLevelLUT=" + this.darkLevelLUT);
                Slog.i(DisplayEngineDataCleanerXMLLoader.TAG, "darkLevelRoofLUT=" + this.darkLevelRoofLUT);
            }
        }

        public void loadDefaultConfig() {
            if (DisplayEngineDataCleanerXMLLoader.HWFLOW) {
                Slog.i(DisplayEngineDataCleanerXMLLoader.TAG, "loadDefaultConfig()");
            }
            this.comfortZoneCounterWeight = 0.2f;
            this.safeZoneCounterWeight = 0.3f;
            this.outlierZoneCounterWeight = 0.5f;
            this.counterWeightThresh = 0.5f;
            this.alDarkThresh = 10;
            this.hbmTresh = 3000;
            this.outDoorLevelFloor = 139;
            if (this.ambientLightLUT != null) {
                this.ambientLightLUT.clear();
            }
            this.ambientLightLUT = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 5, 10, 15, 20, 30, 50, 70, 100, 150, 200, 250, Integer.valueOf(AppTypeInfo.PG_TYPE_BASE), 350, 400, 500, 600, 700, 800, 900, 1000, 1200, 1400, 1800, 2400, 3000, 4000, 5000, 6000, 8000, 10000, 15000, 20000, 30000}));
            if (this.brightnessLevelLUT != null) {
                this.brightnessLevelLUT.clear();
            }
            this.brightnessLevelLUT = new ArrayList<>(Arrays.asList(new Float[]{Float.valueOf(5.0f), Float.valueOf(6.7f), Float.valueOf(9.25f), Float.valueOf(13.5f), Float.valueOf(17.75f), Float.valueOf(22.0f), Float.valueOf(26.333f), Float.valueOf(35.0f), Float.valueOf(35.6316f), Float.valueOf(36.5789f), Float.valueOf(38.1579f), Float.valueOf(39.7368f), Float.valueOf(41.3158f), Float.valueOf(42.8947f), Float.valueOf(44.4737f), Float.valueOf(46.0526f), Float.valueOf(49.2105f), Float.valueOf(52.3684f), Float.valueOf(55.5263f), Float.valueOf(58.6824f), Float.valueOf(61.8421f), Float.valueOf(65.0f), Float.valueOf(71.3f), Float.valueOf(77.6f), Float.valueOf(90.2f), Float.valueOf(109.1f), Float.valueOf(128.0f), Float.valueOf(158.0f), Float.valueOf(160.8333f), Float.valueOf(163.6667f), Float.valueOf(169.3333f), Float.valueOf(175.0f), Float.valueOf(195.0f), Float.valueOf(215.0f), Float.valueOf(255.0f)}));
            if (this.darkLevelLUT != null) {
                this.darkLevelLUT.clear();
            }
            this.darkLevelLUT = new ArrayList<>(Arrays.asList(new Integer[]{5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}));
            if (this.darkLevelRoofLUT != null) {
                this.darkLevelRoofLUT.clear();
            }
            this.darkLevelRoofLUT = new ArrayList<>(Arrays.asList(new Float[]{Float.valueOf(6.0f), Float.valueOf(7.0f), Float.valueOf(9.0f), Float.valueOf(10.0f), Float.valueOf(12.0f), Float.valueOf(13.0f), Float.valueOf(15.0f), Float.valueOf(16.0f), Float.valueOf(18.0f), Float.valueOf(19.0f), Float.valueOf(21.0f), Float.valueOf(22.0f)}));
        }
    }

    private static class Element_AlDarkThresh extends HwXmlElement {
        private Element_AlDarkThresh() {
        }

        public String getName() {
            return "AlDarkThresh";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.alDarkThresh = Integer.parseInt(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return DisplayEngineDataCleanerXMLLoader.mData.alDarkThresh >= 0;
        }
    }

    private static class Element_AmbientLightLUT extends HwXmlElement {
        private Element_AmbientLightLUT() {
        }

        public String getName() {
            return "AmbientLightLUT";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.ambientLightLUT = DisplayEngineDataCleanerXMLLoader.parseIntegerList(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return DisplayEngineDataCleanerXMLLoader.mData.ambientLightLUT.size() > 0;
        }
    }

    private static class Element_BrightnessLevelLUT extends HwXmlElement {
        private Element_BrightnessLevelLUT() {
        }

        public String getName() {
            return "BrightnessLevelLUT";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.brightnessLevelLUT = DisplayEngineDataCleanerXMLLoader.parseFloatList(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return DisplayEngineDataCleanerXMLLoader.mData.brightnessLevelLUT.size() > 0;
        }
    }

    private static class Element_ComfortZoneCounterWeight extends HwXmlElement {
        private Element_ComfortZoneCounterWeight() {
        }

        public String getName() {
            return "ComfortZoneCounterWeight";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.comfortZoneCounterWeight = Float.parseFloat(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return Float.compare(DisplayEngineDataCleanerXMLLoader.mData.comfortZoneCounterWeight, 0.0f) >= 0 && Float.compare(DisplayEngineDataCleanerXMLLoader.mData.comfortZoneCounterWeight, 1.0f) <= 0;
        }
    }

    private static class Element_CounterWeightThresh extends HwXmlElement {
        private Element_CounterWeightThresh() {
        }

        public String getName() {
            return "CounterWeightThresh";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.counterWeightThresh = Float.parseFloat(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return Float.compare(DisplayEngineDataCleanerXMLLoader.mData.counterWeightThresh, 0.0f) >= 0 && Float.compare(DisplayEngineDataCleanerXMLLoader.mData.counterWeightThresh, 1.0f) <= 0;
        }
    }

    private static class Element_DarkLevelLUT extends HwXmlElement {
        private Element_DarkLevelLUT() {
        }

        public String getName() {
            return "DarkLevelLUT";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.darkLevelLUT = DisplayEngineDataCleanerXMLLoader.parseIntegerList(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return DisplayEngineDataCleanerXMLLoader.mData.darkLevelLUT.size() > 0;
        }
    }

    private static class Element_DarkLevelRoofLUT extends HwXmlElement {
        private Element_DarkLevelRoofLUT() {
        }

        public String getName() {
            return "DarkLevelRoofLUT";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.darkLevelRoofLUT = DisplayEngineDataCleanerXMLLoader.parseFloatList(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return DisplayEngineDataCleanerXMLLoader.mData.darkLevelRoofLUT.size() > 0;
        }
    }

    private static class Element_DataCleanerConfig extends HwXmlElement {
        private Element_DataCleanerConfig() {
        }

        public String getName() {
            return "DataCleanerConfig";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_HBMThresh extends HwXmlElement {
        private Element_HBMThresh() {
        }

        public String getName() {
            return "HBMThresh";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.hbmTresh = Integer.parseInt(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return DisplayEngineDataCleanerXMLLoader.mData.hbmTresh >= 0;
        }
    }

    private static class Element_OutDoorLevelThresh extends HwXmlElement {
        private Element_OutDoorLevelThresh() {
        }

        public String getName() {
            return "OutDoorLevelThresh";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.outDoorLevelFloor = Integer.parseInt(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return DisplayEngineDataCleanerXMLLoader.mData.outDoorLevelFloor >= 0;
        }
    }

    private static class Element_OutlierZoneCounterWeight extends HwXmlElement {
        private Element_OutlierZoneCounterWeight() {
        }

        public String getName() {
            return "OutlierZoneCounterWeight";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.outlierZoneCounterWeight = Float.parseFloat(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return Float.compare(DisplayEngineDataCleanerXMLLoader.mData.outlierZoneCounterWeight, 0.0f) >= 0 && Float.compare(DisplayEngineDataCleanerXMLLoader.mData.outlierZoneCounterWeight, 1.0f) <= 0;
        }
    }

    private static class Element_SafeZoneCounterWeight extends HwXmlElement {
        private Element_SafeZoneCounterWeight() {
        }

        public String getName() {
            return "SafeZoneCounterWeight";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            DisplayEngineDataCleanerXMLLoader.mData.safeZoneCounterWeight = Float.parseFloat(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return Float.compare(DisplayEngineDataCleanerXMLLoader.mData.safeZoneCounterWeight, 0.0f) >= 0 && Float.compare(DisplayEngineDataCleanerXMLLoader.mData.safeZoneCounterWeight, 1.0f) <= 0;
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0047, code lost:
        if (0 == 0) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0049, code lost:
        new com.huawei.displayengine.DisplayEngineDataCleanerXMLLoader.Data().loadDefaultConfig();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0053, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0055, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0057, code lost:
        throw r2;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:9:0x001a, B:15:0x0029] */
    public static Data getData(String xmlFilePath) {
        Data data;
        Data retData = null;
        synchronized (mLock) {
            try {
                if (mLoader == null) {
                    mLoader = new DisplayEngineDataCleanerXMLLoader(xmlFilePath);
                }
                retData = (Data) mData.clone();
                if (retData == null) {
                    data = new Data();
                    retData = data;
                    retData.loadDefaultConfig();
                }
            } catch (Exception e) {
                Slog.e(TAG, "getData() error!" + e);
                if (0 == 0) {
                    data = new Data();
                }
            }
        }
        return retData;
    }

    private DisplayEngineDataCleanerXMLLoader(String xmlFilePath) {
        if (HWDEBUG) {
            Slog.d(TAG, "DisplayEngineDataCleanerXMLLoader()");
        }
        if (!parseXml(xmlFilePath)) {
            mData.loadDefaultConfig();
        }
        mData.printData();
    }

    private boolean parseXml(String xmlFilePath) {
        if (xmlFilePath == null) {
            Slog.e(TAG, "parseXml() error! xml file path is null");
            return false;
        }
        HwXmlParser xmlParser = new HwXmlParser(xmlFilePath);
        registerElement(xmlParser);
        if (!xmlParser.parse()) {
            Slog.e(TAG, "parseXml() error! xmlParser.parse() failed!");
            return false;
        } else if (!xmlParser.check()) {
            Slog.e(TAG, "parseXml() error! xmlParser.check() failed!");
            return false;
        } else {
            if (HWFLOW) {
                Slog.i(TAG, "parseXml() load success!");
            }
            return true;
        }
    }

    private void registerElement(HwXmlParser parser) {
        HwXmlElement rootElement = parser.registerRootElement(new Element_DataCleanerConfig());
        rootElement.registerChildElement(new Element_ComfortZoneCounterWeight());
        rootElement.registerChildElement(new Element_SafeZoneCounterWeight());
        rootElement.registerChildElement(new Element_OutlierZoneCounterWeight());
        rootElement.registerChildElement(new Element_CounterWeightThresh());
        rootElement.registerChildElement(new Element_AlDarkThresh());
        rootElement.registerChildElement(new Element_HBMThresh());
        rootElement.registerChildElement(new Element_OutDoorLevelThresh());
        rootElement.registerChildElement(new Element_AmbientLightLUT());
        rootElement.registerChildElement(new Element_BrightnessLevelLUT());
        rootElement.registerChildElement(new Element_DarkLevelLUT());
        rootElement.registerChildElement(new Element_DarkLevelRoofLUT());
    }

    /* access modifiers changed from: private */
    public static ArrayList<Float> parseFloatList(String srcString) {
        if (srcString == null) {
            return null;
        }
        String[] s = srcString.split(",");
        ArrayList<Float> parsedList = new ArrayList<>();
        for (String parseFloat : s) {
            parsedList.add(Float.valueOf(Float.parseFloat(parseFloat)));
        }
        return parsedList;
    }

    /* access modifiers changed from: private */
    public static ArrayList<Integer> parseIntegerList(String srcString) {
        if (srcString == null) {
            return null;
        }
        String[] s = srcString.split(",");
        ArrayList<Integer> parsedList = new ArrayList<>();
        for (String parseInt : s) {
            parsedList.add(Integer.valueOf(Integer.parseInt(parseInt)));
        }
        return parsedList;
    }
}
