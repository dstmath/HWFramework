package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.server.emcom.ParaManagerConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwEyeProtectionXmlLoader {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final String TAG = "HwEyeProtectionXmlLoader";
    private static Data mData = new Data();
    private static HwEyeProtectionXmlLoader mLoader;
    private static final Object mLock = new Object();

    public static class Data implements Cloneable {
        public int brightTimeDelay = 1000;
        public boolean brightTimeDelayEnable = true;
        public float brightTimeDelayLuxThreshold = 30.0f;
        public float brightenDebounceTime = 1000.0f;
        public List<PointF> brightenlinePoints = new ArrayList();
        public int darkTimeDelay = 10000;
        public float darkTimeDelayBeta0 = 0.0f;
        public float darkTimeDelayBeta1 = 1.0f;
        public float darkTimeDelayBeta2 = 0.333f;
        public boolean darkTimeDelayEnable = false;
        public float darkTimeDelayLuxThreshold = 50.0f;
        public int darkenDebounceTime = ParaManagerConstants.MESSAGE_BASE_MONITOR_RESPONSE;
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

        protected Object clone() throws CloneNotSupportedException {
            Data newData = (Data) super.clone();
            newData.brightenlinePoints = cloneList(this.brightenlinePoints);
            newData.darkenlinePoints = cloneList(this.darkenlinePoints);
            return newData;
        }

        private List<PointF> cloneList(List<PointF> list) {
            if (list == null) {
                return null;
            }
            try {
                List<PointF> newList = new ArrayList();
                for (PointF point : list) {
                    newList.add(new PointF(point.x, point.y));
                }
                return newList;
            } catch (Exception e) {
                Slog.e(HwEyeProtectionXmlLoader.TAG, "cloneList() error!" + e);
                return list;
            }
        }

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
            this.darkenDebounceTime = ParaManagerConstants.MESSAGE_BASE_MONITOR_RESPONSE;
            this.darkTimeDelay = 10000;
            this.darkTimeDelayBeta0 = 0.0f;
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
            this.brightenlinePoints.add(new PointF(0.0f, 2.0f));
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
            this.darkenlinePoints.add(new PointF(0.0f, 1.0f));
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

    private static class Element_BrightTimeDelay extends HwXmlElement {
        /* synthetic */ Element_BrightTimeDelay(Element_BrightTimeDelay -this0) {
            this();
        }

        private Element_BrightTimeDelay() {
        }

        public String getName() {
            return "BrightTimeDelay";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.brightTimeDelay = Integer.parseInt(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.brightTimeDelay > 0;
        }
    }

    private static class Element_BrightTimeDelayEnable extends HwXmlElement {
        /* synthetic */ Element_BrightTimeDelayEnable(Element_BrightTimeDelayEnable -this0) {
            this();
        }

        private Element_BrightTimeDelayEnable() {
        }

        public String getName() {
            return "BrightTimeDelayEnable";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.brightTimeDelayEnable = Boolean.parseBoolean(parser.nextText());
            return true;
        }
    }

    private static class Element_BrightenDebounceTime extends HwXmlElement {
        /* synthetic */ Element_BrightenDebounceTime(Element_BrightenDebounceTime -this0) {
            this();
        }

        private Element_BrightenDebounceTime() {
        }

        public String getName() {
            return "BrightenDebounceTime";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.brightenDebounceTime = Float.parseFloat(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.brightenDebounceTime > 0.0f;
        }
    }

    private static class Element_BrightenLinePoints extends HwXmlElement {
        /* synthetic */ Element_BrightenLinePoints(Element_BrightenLinePoints -this0) {
            this();
        }

        private Element_BrightenLinePoints() {
        }

        public String getName() {
            return "BrightenLinePoints";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_BrightenLinePoints_Point extends HwXmlElement {
        /* synthetic */ Element_BrightenLinePoints_Point(Element_BrightenLinePoints_Point -this0) {
            this();
        }

        private Element_BrightenLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.brightenlinePoints = HwXmlElement.parsePointFList(parser, HwEyeProtectionXmlLoader.mData.brightenlinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.checkPointsListIsOK(HwEyeProtectionXmlLoader.mData.brightenlinePoints);
        }
    }

    private static class Element_BrightenTimeDelayLuxThreshold extends HwXmlElement {
        /* synthetic */ Element_BrightenTimeDelayLuxThreshold(Element_BrightenTimeDelayLuxThreshold -this0) {
            this();
        }

        private Element_BrightenTimeDelayLuxThreshold() {
        }

        public String getName() {
            return "BrightenTimeDelayLuxThreshold";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.brightTimeDelayLuxThreshold = Float.parseFloat(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkTimeDelayEnable extends HwXmlElement {
        /* synthetic */ Element_DarkTimeDelayEnable(Element_DarkTimeDelayEnable -this0) {
            this();
        }

        private Element_DarkTimeDelayEnable() {
        }

        public String getName() {
            return "DarkTimeDelayEnable";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.darkTimeDelayEnable = Boolean.parseBoolean(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkTimeDelayLuxThreshold extends HwXmlElement {
        /* synthetic */ Element_DarkTimeDelayLuxThreshold(Element_DarkTimeDelayLuxThreshold -this0) {
            this();
        }

        private Element_DarkTimeDelayLuxThreshold() {
        }

        public String getName() {
            return "DarkTimeDelayLuxThreshold";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.darkTimeDelayLuxThreshold = Float.parseFloat(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkenDebounceTime extends HwXmlElement {
        /* synthetic */ Element_DarkenDebounceTime(Element_DarkenDebounceTime -this0) {
            this();
        }

        private Element_DarkenDebounceTime() {
        }

        public String getName() {
            return "DarkenDebounceTime";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.darkenDebounceTime = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkenLinePoints extends HwXmlElement {
        /* synthetic */ Element_DarkenLinePoints(Element_DarkenLinePoints -this0) {
            this();
        }

        private Element_DarkenLinePoints() {
        }

        public String getName() {
            return "DarkenLinePoints";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_DarkenLinePoints_Point extends HwXmlElement {
        /* synthetic */ Element_DarkenLinePoints_Point(Element_DarkenLinePoints_Point -this0) {
            this();
        }

        private Element_DarkenLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.darkenlinePoints = HwXmlElement.parsePointFList(parser, HwEyeProtectionXmlLoader.mData.darkenlinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.checkPointsListIsOK(HwEyeProtectionXmlLoader.mData.darkenlinePoints);
        }
    }

    private static class Element_DarkenTimeDelay extends HwXmlElement {
        /* synthetic */ Element_DarkenTimeDelay(Element_DarkenTimeDelay -this0) {
            this();
        }

        private Element_DarkenTimeDelay() {
        }

        public String getName() {
            return "DarkenTimeDelay";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.darkTimeDelay = Integer.parseInt(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.darkTimeDelay > 0;
        }
    }

    private static class Element_DarkenTimeDelayBeta0 extends HwXmlElement {
        /* synthetic */ Element_DarkenTimeDelayBeta0(Element_DarkenTimeDelayBeta0 -this0) {
            this();
        }

        private Element_DarkenTimeDelayBeta0() {
        }

        public String getName() {
            return "DarkenTimeDelayBeta0";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.darkTimeDelayBeta0 = Float.parseFloat(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkenTimeDelayBeta1 extends HwXmlElement {
        /* synthetic */ Element_DarkenTimeDelayBeta1(Element_DarkenTimeDelayBeta1 -this0) {
            this();
        }

        private Element_DarkenTimeDelayBeta1() {
        }

        public String getName() {
            return "DarkenTimeDelayBeta1";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.darkTimeDelayBeta1 = Float.parseFloat(parser.nextText());
            return true;
        }
    }

    private static class Element_DarkenTimeDelayBeta2 extends HwXmlElement {
        /* synthetic */ Element_DarkenTimeDelayBeta2(Element_DarkenTimeDelayBeta2 -this0) {
            this();
        }

        private Element_DarkenTimeDelayBeta2() {
        }

        public String getName() {
            return "DarkenTimeDelayBeta2";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.darkTimeDelayBeta2 = Float.parseFloat(parser.nextText());
            return true;
        }
    }

    private static class Element_EyeProtectionConfig extends HwXmlElement {
        /* synthetic */ Element_EyeProtectionConfig(Element_EyeProtectionConfig -this0) {
            this();
        }

        private Element_EyeProtectionConfig() {
        }

        public String getName() {
            return Utils.HW_EYEPROTECTION_CONFIG_FILE_NAME;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_FilterConfig extends HwXmlElement {
        /* synthetic */ Element_FilterConfig(Element_FilterConfig -this0) {
            this();
        }

        private Element_FilterConfig() {
        }

        public String getName() {
            return "FilterConfig";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_PostMaxMinAvgFilterNoFilterNum extends HwXmlElement {
        /* synthetic */ Element_PostMaxMinAvgFilterNoFilterNum(Element_PostMaxMinAvgFilterNoFilterNum -this0) {
            this();
        }

        private Element_PostMaxMinAvgFilterNoFilterNum() {
        }

        public String getName() {
            return "PostMaxMinAvgFilterNoFilterNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNoFilterNum = Integer.parseInt(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNum > 0 && HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNum <= HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNoFilterNum;
        }
    }

    private static class Element_PostMaxMinAvgFilterNum extends HwXmlElement {
        /* synthetic */ Element_PostMaxMinAvgFilterNum(Element_PostMaxMinAvgFilterNum -this0) {
            this();
        }

        private Element_PostMaxMinAvgFilterNum() {
        }

        public String getName() {
            return "PostMaxMinAvgFilterNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.postMaxMinAvgFilterNum = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_PostMeanFilterNoFilterNum extends HwXmlElement {
        /* synthetic */ Element_PostMeanFilterNoFilterNum(Element_PostMeanFilterNoFilterNum -this0) {
            this();
        }

        private Element_PostMeanFilterNoFilterNum() {
        }

        public String getName() {
            return "PostMeanFilterNoFilterNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.postMeanFilterNoFilterNum = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_PostMeanFilterNum extends HwXmlElement {
        /* synthetic */ Element_PostMeanFilterNum(Element_PostMeanFilterNum -this0) {
            this();
        }

        private Element_PostMeanFilterNum() {
        }

        public String getName() {
            return "PostMeanFilterNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.postMeanFilterNum = Integer.parseInt(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.postMeanFilterNum > 0 && HwEyeProtectionXmlLoader.mData.postMeanFilterNum <= HwEyeProtectionXmlLoader.mData.postMeanFilterNoFilterNum;
        }
    }

    private static class Element_PostMethodNum extends HwXmlElement {
        /* synthetic */ Element_PostMethodNum(Element_PostMethodNum -this0) {
            this();
        }

        private Element_PostMethodNum() {
        }

        public String getName() {
            return "PostMethodNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.postMethodNum = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_PreMeanFilterNoFilterNum extends HwXmlElement {
        /* synthetic */ Element_PreMeanFilterNoFilterNum(Element_PreMeanFilterNoFilterNum -this0) {
            this();
        }

        private Element_PreMeanFilterNoFilterNum() {
        }

        public String getName() {
            return "PreMeanFilterNoFilterNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.preMeanFilterNoFilterNum = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_PreMeanFilterNum extends HwXmlElement {
        /* synthetic */ Element_PreMeanFilterNum(Element_PreMeanFilterNum -this0) {
            this();
        }

        private Element_PreMeanFilterNum() {
        }

        public String getName() {
            return "PreMeanFilterNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.preMeanFilterNum = Integer.parseInt(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.preMeanFilterNum > 0 && HwEyeProtectionXmlLoader.mData.preMeanFilterNum <= HwEyeProtectionXmlLoader.mData.preMeanFilterNoFilterNum;
        }
    }

    private static class Element_PreMethodNum extends HwXmlElement {
        /* synthetic */ Element_PreMethodNum(Element_PreMethodNum -this0) {
            this();
        }

        private Element_PreMethodNum() {
        }

        public String getName() {
            return "PreMethodNum";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.preMethodNum = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_PreWeightedMeanFilterAlpha extends HwXmlElement {
        /* synthetic */ Element_PreWeightedMeanFilterAlpha(Element_PreWeightedMeanFilterAlpha -this0) {
            this();
        }

        private Element_PreWeightedMeanFilterAlpha() {
        }

        public String getName() {
            return "PreWeightedMeanFilterAlpha";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterAlpha = Float.parseFloat(parser.nextText());
            return true;
        }
    }

    private static class Element_PreWeightedMeanFilterLuxTh extends HwXmlElement {
        /* synthetic */ Element_PreWeightedMeanFilterLuxTh(Element_PreWeightedMeanFilterLuxTh -this0) {
            this();
        }

        private Element_PreWeightedMeanFilterLuxTh() {
        }

        public String getName() {
            return "PreWeightedMeanFilterLuxTh";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterLuxTh = Float.parseFloat(parser.nextText());
            return true;
        }
    }

    private static class Element_PreWeightedMeanFilterMaxFuncLuxNum extends HwXmlElement {
        /* synthetic */ Element_PreWeightedMeanFilterMaxFuncLuxNum(Element_PreWeightedMeanFilterMaxFuncLuxNum -this0) {
            this();
        }

        private Element_PreWeightedMeanFilterMaxFuncLuxNum() {
        }

        public String getName() {
            return "PreWeightedMeanFilterMaxFuncLuxNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum = Integer.parseInt(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum > 0;
        }
    }

    private static class Element_PreWeightedMeanFilterNoFilterNum extends HwXmlElement {
        /* synthetic */ Element_PreWeightedMeanFilterNoFilterNum(Element_PreWeightedMeanFilterNoFilterNum -this0) {
            this();
        }

        private Element_PreWeightedMeanFilterNoFilterNum() {
        }

        public String getName() {
            return "PreWeightedMeanFilterNoFilterNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNoFilterNum = Integer.parseInt(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNoFilterNum > 0;
        }
    }

    private static class Element_PreWeightedMeanFilterNum extends HwXmlElement {
        /* synthetic */ Element_PreWeightedMeanFilterNum(Element_PreWeightedMeanFilterNum -this0) {
            this();
        }

        private Element_PreWeightedMeanFilterNum() {
        }

        public String getName() {
            return "PreWeightedMeanFilterNum";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNum = Integer.parseInt(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNum > 0 && HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNum <= HwEyeProtectionXmlLoader.mData.preWeightedMeanFilterNoFilterNum;
        }
    }

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDEBUG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWFLOW = z;
    }

    public static Data getData(String xmlFilePath) {
        Data retData;
        Throwable th;
        synchronized (mLock) {
            Data retData2;
            try {
                if (mLoader == null) {
                    mLoader = new HwEyeProtectionXmlLoader(xmlFilePath);
                }
                retData = (Data) mData.clone();
                if (retData == null) {
                    retData2 = new Data();
                    retData2.loadDefaultConfig();
                    retData = retData2;
                }
            } catch (Exception e) {
                Slog.e(TAG, "getData() error!" + e);
                retData2 = new Data();
                retData2.loadDefaultConfig();
                retData = retData2;
            } catch (Throwable th2) {
                th = th2;
                retData = retData2;
                throw th;
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
        if (HWFLOW) {
            Slog.i(TAG, "parseXml() getXmlPath=" + xmlFilePath);
        }
        HwXmlParser xmlParser = new HwXmlParser(xmlFilePath);
        registerElement(xmlParser);
        if (!xmlParser.parse()) {
            Slog.e(TAG, "parseXml() error! xmlParser.parse() failed!");
            return false;
        } else if (xmlParser.check()) {
            if (HWFLOW) {
                Slog.i(TAG, "parseXml() load success!");
            }
            return true;
        } else {
            Slog.e(TAG, "parseXml() error! xmlParser.check() failed!");
            return false;
        }
    }

    private void registerElement(HwXmlParser parser) {
        HwXmlElement filterConfigElement = parser.registerRootElement(new Element_EyeProtectionConfig()).registerChildElement(new Element_FilterConfig());
        filterConfigElement.registerChildElement(new Element_BrightenDebounceTime());
        filterConfigElement.registerChildElement(new Element_BrightenTimeDelayLuxThreshold());
        filterConfigElement.registerChildElement(new Element_BrightTimeDelay());
        filterConfigElement.registerChildElement(new Element_BrightTimeDelayEnable());
        filterConfigElement.registerChildElement(new Element_BrightenTimeDelayLuxThreshold());
        filterConfigElement.registerChildElement(new Element_DarkenDebounceTime());
        filterConfigElement.registerChildElement(new Element_DarkenTimeDelay());
        filterConfigElement.registerChildElement(new Element_DarkenTimeDelayBeta0());
        filterConfigElement.registerChildElement(new Element_DarkenTimeDelayBeta1());
        filterConfigElement.registerChildElement(new Element_DarkenTimeDelayBeta2());
        filterConfigElement.registerChildElement(new Element_DarkTimeDelayEnable());
        filterConfigElement.registerChildElement(new Element_DarkTimeDelayLuxThreshold());
        filterConfigElement.registerChildElement(new Element_PostMaxMinAvgFilterNoFilterNum());
        filterConfigElement.registerChildElement(new Element_PostMaxMinAvgFilterNum());
        filterConfigElement.registerChildElement(new Element_PostMeanFilterNoFilterNum());
        filterConfigElement.registerChildElement(new Element_PostMeanFilterNum());
        filterConfigElement.registerChildElement(new Element_PostMethodNum());
        filterConfigElement.registerChildElement(new Element_PreMeanFilterNoFilterNum());
        filterConfigElement.registerChildElement(new Element_PreMeanFilterNum());
        filterConfigElement.registerChildElement(new Element_PreMethodNum());
        filterConfigElement.registerChildElement(new Element_PreWeightedMeanFilterAlpha());
        filterConfigElement.registerChildElement(new Element_PreWeightedMeanFilterLuxTh());
        filterConfigElement.registerChildElement(new Element_PreWeightedMeanFilterMaxFuncLuxNum());
        filterConfigElement.registerChildElement(new Element_PreWeightedMeanFilterNoFilterNum());
        filterConfigElement.registerChildElement(new Element_PreWeightedMeanFilterNum());
        filterConfigElement.registerChildElement(new Element_BrightenLinePoints()).registerChildElement(new Element_BrightenLinePoints_Point());
        filterConfigElement.registerChildElement(new Element_DarkenLinePoints()).registerChildElement(new Element_DarkenLinePoints_Point());
    }

    private static boolean checkPointsListIsOK(List<PointF> list) {
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
