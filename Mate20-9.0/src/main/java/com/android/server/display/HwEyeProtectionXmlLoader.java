package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

final class HwEyeProtectionXmlLoader {
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    /* access modifiers changed from: private */
    public static final boolean HWFLOW;
    private static final String TAG = "HwEyeProtectionXmlLoader";
    /* access modifiers changed from: private */
    public static Data mData = new Data();
    private static HwEyeProtectionXmlLoader mLoader;
    private static final Object mLock = new Object();

    public static class Data {
        public int brightTimeDelay = 1000;
        public boolean brightTimeDelayEnable = true;
        public float brightTimeDelayLuxThreshold = 30.0f;
        public float brightenDebounceTime = 1000.0f;
        public List<PointF> brightenlinePoints = new ArrayList();
        public int darkTimeDelay = 10000;
        public float darkTimeDelayBeta0 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float darkTimeDelayBeta1 = 1.0f;
        public float darkTimeDelayBeta2 = 0.333f;
        public boolean darkTimeDelayEnable = false;
        public float darkTimeDelayLuxThreshold = 50.0f;
        public int darkenDebounceTime = HwAPPQoEUtils.APP_TYPE_STREAMING;
        public List<PointF> darkenlinePoints = new ArrayList();
        public int postMaxMinAvgFilterNoFilterNum = 6;
        public int postMaxMinAvgFilterNum = 5;
        public int postMeanFilterNoFilterNum = 4;
        public int postMeanFilterNum = 3;
        public int postMethodNum = 1;
        public int preMeanFilterNoFilterNum = 7;
        public int preMeanFilterNum = 3;
        public int preMethodNum = 0;
        public float preWeightedMeanFilterAlpha = 0.5f;
        public float preWeightedMeanFilterLuxTh = 12.0f;
        public int preWeightedMeanFilterMaxFuncLuxNum = 3;
        public int preWeightedMeanFilterNoFilterNum = 7;
        public int preWeightedMeanFilterNum = 1;

        public void printData() {
            if (HwEyeProtectionXmlLoader.HWFLOW) {
                Slog.i(HwEyeProtectionXmlLoader.TAG, "printData() preMeanFilterNum=" + this.preMeanFilterNum + ", preMeanFilterNoFilterNum=" + this.preMeanFilterNoFilterNum + ", preWeightedMeanFilterNum=" + this.preWeightedMeanFilterNum + ", preWeightedMeanFilterMaxFuncLuxNum=" + this.preWeightedMeanFilterMaxFuncLuxNum + ", preWeightedMeanFilterLuxTh=" + this.preWeightedMeanFilterLuxTh + ", preWeightedMeanFilterAlpha=" + this.preWeightedMeanFilterAlpha + ", preWeightedMeanFilterNoFilterNum=" + this.preWeightedMeanFilterNoFilterNum + ", postMeanFilterNum=" + this.postMeanFilterNum + ", postMeanFilterNoFilterNum=" + this.postMeanFilterNoFilterNum + ", postMaxMinAvgFilterNum=" + this.postMaxMinAvgFilterNum + ", postMeanFilterNoFilterNum=" + this.postMeanFilterNoFilterNum + ", preMethodNum=" + this.preMethodNum + ", postMethodNum=" + this.postMethodNum + ", brightTimeDelay=" + this.brightTimeDelay + ", brightenDebounceTime=" + this.brightenDebounceTime + ", brightTimeDelayLuxThreshold=" + this.brightTimeDelayLuxThreshold + ", brightTimeDelayEnable=" + this.brightTimeDelayEnable + ", darkTimeDelayLuxThreshold=" + this.darkTimeDelayLuxThreshold + ", darkTimeDelayEnable=" + this.darkTimeDelayEnable + ", darkTimeDelayBeta2=" + this.darkTimeDelayBeta2 + ", darkTimeDelayBeta1=" + this.darkTimeDelayBeta1 + ", darkTimeDelayBeta0=" + this.darkTimeDelayBeta0 + ", darkTimeDelay=" + this.darkTimeDelay + ", brightenlinePoints=" + this.brightenlinePoints + ", darkenlinePoints=" + this.darkenlinePoints);
            }
        }

        public void loadDefaultConfig() {
            if (HwEyeProtectionXmlLoader.HWFLOW) {
                Slog.i(HwEyeProtectionXmlLoader.TAG, "loadDefaultConfig()");
            }
            this.brightenDebounceTime = 1000.0f;
            this.brightTimeDelay = 1000;
            this.brightTimeDelayEnable = false;
            this.brightTimeDelayLuxThreshold = 30.0f;
            this.darkenDebounceTime = HwAPPQoEUtils.APP_TYPE_STREAMING;
            this.darkTimeDelay = 10000;
            this.darkTimeDelayBeta0 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkTimeDelayBeta1 = 1.0f;
            this.darkTimeDelayBeta2 = 0.333f;
            this.darkTimeDelayEnable = false;
            this.darkTimeDelayLuxThreshold = 50.0f;
            this.postMaxMinAvgFilterNoFilterNum = 6;
            this.postMaxMinAvgFilterNum = 5;
            this.postMeanFilterNoFilterNum = 4;
            this.postMeanFilterNum = 3;
            this.postMethodNum = 1;
            this.preMeanFilterNoFilterNum = 7;
            this.preMeanFilterNum = 3;
            this.preMethodNum = 0;
            this.preWeightedMeanFilterAlpha = 0.5f;
            this.preWeightedMeanFilterLuxTh = 12.0f;
            this.preWeightedMeanFilterMaxFuncLuxNum = 3;
            this.preWeightedMeanFilterNoFilterNum = 7;
            this.preWeightedMeanFilterNum = 1;
            this.brightenlinePoints.clear();
            this.brightenlinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 2.0f));
            this.brightenlinePoints.add(new PointF(5.0f, 10.0f));
            this.brightenlinePoints.add(new PointF(10.0f, 19.0f));
            this.brightenlinePoints.add(new PointF(20.0f, 89.0f));
            this.brightenlinePoints.add(new PointF(30.0f, 200.0f));
            this.brightenlinePoints.add(new PointF(100.0f, 439.0f));
            this.brightenlinePoints.add(new PointF(500.0f, 739.0f));
            this.brightenlinePoints.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePoints.add(new PointF(3000.0f, 1000.0f));
            this.brightenlinePoints.add(new PointF(4000.0f, 2000.0f));
            this.brightenlinePoints.add(new PointF(10000.0f, 3000.0f));
            this.brightenlinePoints.add(new PointF(20000.0f, 10000.0f));
            this.brightenlinePoints.add(new PointF(40000.0f, 40000.0f));
            this.darkenlinePoints.clear();
            this.darkenlinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f));
            this.darkenlinePoints.add(new PointF(1.0f, 1.0f));
            this.darkenlinePoints.add(new PointF(50.0f, 35.0f));
            this.darkenlinePoints.add(new PointF(100.0f, 80.0f));
            this.darkenlinePoints.add(new PointF(200.0f, 170.0f));
            this.darkenlinePoints.add(new PointF(300.0f, 225.0f));
            this.darkenlinePoints.add(new PointF(500.0f, 273.0f));
            this.darkenlinePoints.add(new PointF(600.0f, 322.0f));
            this.darkenlinePoints.add(new PointF(1200.0f, 600.0f));
            this.darkenlinePoints.add(new PointF(1800.0f, 600.0f));
            this.darkenlinePoints.add(new PointF(4000.0f, 2000.0f));
            this.darkenlinePoints.add(new PointF(8000.0f, 4000.0f));
            this.darkenlinePoints.add(new PointF(12000.0f, 6000.0f));
            this.darkenlinePoints.add(new PointF(40000.0f, 20000.0f));
        }
    }

    private static class Element_BrightenLinePoints extends HwXmlElement {
        private Element_BrightenLinePoints() {
        }

        public String getName() {
            return "BrightenLinePoints";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_BrightenLinePoints_Point extends HwXmlElement {
        private Element_BrightenLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.brightenlinePoints = parsePointFList(parser, HwEyeProtectionXmlLoader.mData.brightenlinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwEyeProtectionXmlLoader.checkPointsListIsOK(HwEyeProtectionXmlLoader.mData.brightenlinePoints);
        }
    }

    private static class Element_DarkenLinePoints extends HwXmlElement {
        private Element_DarkenLinePoints() {
        }

        public String getName() {
            return "DarkenLinePoints";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_DarkenLinePoints_Point extends HwXmlElement {
        private Element_DarkenLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.darkenlinePoints = parsePointFList(parser, HwEyeProtectionXmlLoader.mData.darkenlinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwEyeProtectionXmlLoader.checkPointsListIsOK(HwEyeProtectionXmlLoader.mData.darkenlinePoints);
        }
    }

    private static class Element_EyeProtectionConfig extends HwXmlElement {
        private Element_EyeProtectionConfig() {
        }

        public String getName() {
            return Utils.HW_EYEPROTECTION_CONFIG_FILE_NAME;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_FilterConfig extends HwXmlElement {
        private Element_FilterConfig() {
        }

        public String getName() {
            return "FilterConfig";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_FilterGroup extends HwXmlElement {
        private Element_FilterGroup() {
        }

        public String getName() {
            return "FilterGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"BrightenDebounceTime", "DarkenDebounceTime", "DarkTimeDelayEnable", "PostMeanFilterNoFilterNum", "PostMeanFilterNum", "PostMethodNum", "PreMeanFilterNoFilterNum", "PreMeanFilterNum", "PreMethodNum"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1216473595:
                    if (valueName.equals("PostMethodNum")) {
                        c = 5;
                        break;
                    }
                case -806082151:
                    if (valueName.equals("BrightenDebounceTime")) {
                        c = 0;
                        break;
                    }
                case -599365355:
                    if (valueName.equals("DarkenDebounceTime")) {
                        c = 1;
                        break;
                    }
                case 157937894:
                    if (valueName.equals("PreMeanFilterNum")) {
                        c = 7;
                        break;
                    }
                case 620980681:
                    if (valueName.equals("PostMeanFilterNum")) {
                        c = 4;
                        break;
                    }
                case 1431493488:
                    if (valueName.equals("PostMeanFilterNoFilterNum")) {
                        c = 3;
                        break;
                    }
                case 1443034509:
                    if (valueName.equals("PreMeanFilterNoFilterNum")) {
                        c = 6;
                        break;
                    }
                case 1521603939:
                    if (valueName.equals("DarkTimeDelayEnable")) {
                        c = 2;
                        break;
                    }
                case 1524020130:
                    if (valueName.equals("PreMethodNum")) {
                        c = 8;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwEyeProtectionXmlLoader.mData.brightenDebounceTime = Float.parseFloat(parser.nextText());
                    break;
                case 1:
                    HwEyeProtectionXmlLoader.mData.darkenDebounceTime = Integer.parseInt(parser.nextText());
                    break;
                case 2:
                    HwEyeProtectionXmlLoader.mData.darkTimeDelayEnable = Boolean.parseBoolean(parser.nextText());
                    break;
                case 3:
                    HwEyeProtectionXmlLoader.mData.postMeanFilterNoFilterNum = Integer.parseInt(parser.nextText());
                    break;
                case 4:
                    HwEyeProtectionXmlLoader.mData.postMeanFilterNum = Integer.parseInt(parser.nextText());
                    break;
                case 5:
                    HwEyeProtectionXmlLoader.mData.postMethodNum = Integer.parseInt(parser.nextText());
                    break;
                case 6:
                    HwEyeProtectionXmlLoader.mData.preMeanFilterNoFilterNum = Integer.parseInt(parser.nextText());
                    break;
                case 7:
                    HwEyeProtectionXmlLoader.mData.preMeanFilterNum = Integer.parseInt(parser.nextText());
                    break;
                case 8:
                    HwEyeProtectionXmlLoader.mData.preMethodNum = Integer.parseInt(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.brightenDebounceTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwEyeProtectionXmlLoader.mData.postMeanFilterNum > 0 && HwEyeProtectionXmlLoader.mData.postMeanFilterNum <= HwEyeProtectionXmlLoader.mData.postMeanFilterNoFilterNum && HwEyeProtectionXmlLoader.mData.preMeanFilterNum > 0 && HwEyeProtectionXmlLoader.mData.preMeanFilterNum <= HwEyeProtectionXmlLoader.mData.preMeanFilterNoFilterNum;
        }
    }

    private static class Element_FilterOptionalGroup1 extends HwXmlElement {
        private Element_FilterOptionalGroup1() {
        }

        public String getName() {
            return "FilterOptionalGroup1";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"BrightTimeDelay", "BrightTimeDelayEnable", "BrightenTimeDelayLuxThreshold", "DarkenTimeDelay", "DarkenTimeDelayBeta0", "DarkenTimeDelayBeta1", "DarkenTimeDelayBeta2", "DarkTimeDelayLuxThreshold", "PostMaxMinAvgFilterNoFilterNum", "PostMaxMinAvgFilterNum"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1912197839:
                    if (valueName.equals("PostMaxMinAvgFilterNoFilterNum")) {
                        c = 8;
                        break;
                    }
                case -1203123972:
                    if (valueName.equals("DarkTimeDelayLuxThreshold")) {
                        c = 7;
                        break;
                    }
                case -591649441:
                    if (valueName.equals("BrightTimeDelayEnable")) {
                        c = 1;
                        break;
                    }
                case -157474449:
                    if (valueName.equals("BrightenTimeDelayLuxThreshold")) {
                        c = 2;
                        break;
                    }
                case 248648375:
                    if (valueName.equals("DarkenTimeDelay")) {
                        c = 3;
                        break;
                    }
                case 1373278396:
                    if (valueName.equals("BrightTimeDelay")) {
                        c = 0;
                        break;
                    }
                case 1472315337:
                    if (valueName.equals("DarkenTimeDelayBeta0")) {
                        c = 4;
                        break;
                    }
                case 1472315338:
                    if (valueName.equals("DarkenTimeDelayBeta1")) {
                        c = 5;
                        break;
                    }
                case 1472315339:
                    if (valueName.equals("DarkenTimeDelayBeta2")) {
                        c = 6;
                        break;
                    }
                case 1960905866:
                    if (valueName.equals("PostMaxMinAvgFilterNum")) {
                        c = 9;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwEyeProtectionXmlLoader.mData.brightTimeDelay = Integer.parseInt(parser.nextText());
                    break;
                case 1:
                    HwEyeProtectionXmlLoader.mData.brightTimeDelayEnable = Boolean.parseBoolean(parser.nextText());
                    break;
                case 2:
                    HwEyeProtectionXmlLoader.mData.brightTimeDelayLuxThreshold = Float.parseFloat(parser.nextText());
                    break;
                case 3:
                    HwEyeProtectionXmlLoader.mData.darkTimeDelay = Integer.parseInt(parser.nextText());
                    break;
                case 4:
                    HwEyeProtectionXmlLoader.mData.darkTimeDelayBeta0 = Float.parseFloat(parser.nextText());
                    break;
                case 5:
                    HwEyeProtectionXmlLoader.mData.darkTimeDelayBeta1 = Float.parseFloat(parser.nextText());
                    break;
                case 6:
                    HwEyeProtectionXmlLoader.mData.darkTimeDelayBeta2 = Float.parseFloat(parser.nextText());
                    break;
                case 7:
                    HwEyeProtectionXmlLoader.mData.darkTimeDelayLuxThreshold = Float.parseFloat(parser.nextText());
                    break;
                case 8:
                    HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNoFilterNum = Integer.parseInt(parser.nextText());
                    break;
                case 9:
                    HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNum = Integer.parseInt(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.brightTimeDelay > 0 && HwEyeProtectionXmlLoader.mData.darkTimeDelay > 0 && HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNum > 0 && HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNum <= HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNoFilterNum;
        }
    }

    private static class Element_FilterOptionalGroup2 extends HwXmlElement {
        private Element_FilterOptionalGroup2() {
        }

        public String getName() {
            return "FilterOptionalGroup2";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"PreWeightedMeanFilterAlpha", "PreWeightedMeanFilterLuxTh", "PreWeightedMeanFilterMaxFuncLuxNum", "PreWeightedMeanFilterNoFilterNum", "PreWeightedMeanFilterNum"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1627681866:
                    if (valueName.equals("PreWeightedMeanFilterNoFilterNum")) {
                        c = 3;
                        break;
                    }
                case 246136847:
                    if (valueName.equals("PreWeightedMeanFilterNum")) {
                        c = 4;
                        break;
                    }
                case 302040999:
                    if (valueName.equals("PreWeightedMeanFilterAlpha")) {
                        c = 0;
                        break;
                    }
                case 312474924:
                    if (valueName.equals("PreWeightedMeanFilterLuxTh")) {
                        c = 1;
                        break;
                    }
                case 1095802536:
                    if (valueName.equals("PreWeightedMeanFilterMaxFuncLuxNum")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterAlpha = Float.parseFloat(parser.nextText());
                    break;
                case 1:
                    HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterLuxTh = Float.parseFloat(parser.nextText());
                    break;
                case 2:
                    HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum = Integer.parseInt(parser.nextText());
                    break;
                case 3:
                    HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNoFilterNum = Integer.parseInt(parser.nextText());
                    break;
                case 4:
                    HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNum = Integer.parseInt(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum > 0 && HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNoFilterNum > 0 && HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNum > 0 && HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNum <= HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNoFilterNum;
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        if (0 == 0) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0043, code lost:
        new com.android.server.display.HwEyeProtectionXmlLoader.Data().loadDefaultConfig();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004f, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0051, code lost:
        throw r2;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:9:0x0014, B:15:0x0023] */
    public static Data getData(String xmlFilePath) {
        Data data;
        Data retData = null;
        synchronized (mLock) {
            try {
                if (mLoader == null) {
                    mLoader = new HwEyeProtectionXmlLoader(xmlFilePath);
                }
                retData = mData;
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

    private HwEyeProtectionXmlLoader(String xmlFilePath) {
        if (HWDEBUG) {
            Slog.d(TAG, "HwEyeProtectionXmlLoader()");
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
        HwXmlElement filterConfigElement = parser.registerRootElement(new Element_EyeProtectionConfig()).registerChildElement(new Element_FilterConfig());
        filterConfigElement.registerChildElement(new Element_FilterGroup());
        filterConfigElement.registerChildElement(new Element_FilterOptionalGroup1());
        filterConfigElement.registerChildElement(new Element_FilterOptionalGroup2());
        filterConfigElement.registerChildElement(new Element_BrightenLinePoints()).registerChildElement(new Element_BrightenLinePoints_Point());
        filterConfigElement.registerChildElement(new Element_DarkenLinePoints()).registerChildElement(new Element_DarkenLinePoints_Point());
    }

    /* access modifiers changed from: private */
    public static boolean checkPointsListIsOK(List<PointF> list) {
        if (list == null) {
            Slog.e(TAG, "checkPointsListIsOK() error! list is null");
            return false;
        } else if (list.size() < 3 || list.size() >= 100) {
            Slog.e(TAG, "checkPointsListIsOK() error! list size=" + list.size() + " is out of range");
            return false;
        } else {
            PointF lastPoint = null;
            for (PointF point : list) {
                if (lastPoint == null || point.x > lastPoint.x) {
                    lastPoint = point;
                } else {
                    Slog.e(TAG, "checkPointsListIsOK() error! x in list isn't a increasing sequence, " + point.x + "<=" + lastPoint.x);
                    return false;
                }
            }
            return true;
        }
    }
}
