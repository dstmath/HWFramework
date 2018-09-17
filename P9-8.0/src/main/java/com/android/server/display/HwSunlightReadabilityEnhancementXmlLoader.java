package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.server.am.HwActivityManagerService;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwSunlightReadabilityEnhancementXmlLoader {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final String TAG = "HwSunlightReadabilityEnhancementXmlLoader";
    private static final String XML_NAME = "HiACEConfig.xml";
    private static Data mData = new Data();
    private static HwSunlightReadabilityEnhancementXmlLoader mLoader;
    private static Object mLoaderLock = new Object();

    public static class Data implements Cloneable {
        public int brighenDebounceTime = 3000;
        public List<PointF> brightenLinePoints = new ArrayList();
        public int darkenDebounceTime = 3000;
        public List<PointF> darkenLinePoints = new ArrayList();
        public int inDoorThreshold = HwActivityManagerService.SERVICE_ADJ;
        public int lightSensorRateMills = 300;
        public int outDoorThreshold = 1000;
        public boolean sceneCameraEnable = true;
        public boolean usingBLC = true;
        public boolean usingSRE = true;

        protected Object clone() throws CloneNotSupportedException {
            Data newData = (Data) super.clone();
            newData.brightenLinePoints = cloneList(this.brightenLinePoints);
            newData.darkenLinePoints = cloneList(this.darkenLinePoints);
            return newData;
        }

        private List<PointF> cloneList(List<PointF> list) {
            if (list == null) {
                return null;
            }
            List<PointF> newList = new ArrayList();
            for (PointF point : list) {
                newList.add(new PointF(point.x, point.y));
            }
            return newList;
        }

        public void printData() {
            if (HwSunlightReadabilityEnhancementXmlLoader.HWFLOW) {
                Slog.i(HwSunlightReadabilityEnhancementXmlLoader.TAG, "printData() usingBLC=" + this.usingBLC + ", usingSRE=" + this.usingSRE + ", lightSensorRateMills=" + this.lightSensorRateMills + ", sceneCameraEnable=" + this.sceneCameraEnable + ", outDoorThreshold=" + this.outDoorThreshold + ", inDoorThreshold=" + this.inDoorThreshold + ", brighenDebounceTime=" + this.brighenDebounceTime + ", darkenDebounceTime=" + this.darkenDebounceTime);
                Slog.i(HwSunlightReadabilityEnhancementXmlLoader.TAG, "printData() brightenLinePoints=" + this.brightenLinePoints);
                Slog.i(HwSunlightReadabilityEnhancementXmlLoader.TAG, "printData() darkenLinePoints=" + this.darkenLinePoints);
            }
        }

        public void clearConfig() {
            if (HwSunlightReadabilityEnhancementXmlLoader.HWFLOW) {
                Slog.i(HwSunlightReadabilityEnhancementXmlLoader.TAG, "clearConfig()");
            }
            this.usingBLC = false;
            this.usingSRE = false;
            this.sceneCameraEnable = false;
        }

        public void loadDefaultConfig() {
            if (HwSunlightReadabilityEnhancementXmlLoader.HWFLOW) {
                Slog.i(HwSunlightReadabilityEnhancementXmlLoader.TAG, "loadDefaultConfig()");
            }
            this.usingBLC = true;
            this.usingSRE = true;
            this.lightSensorRateMills = 300;
            this.sceneCameraEnable = true;
            this.outDoorThreshold = 1000;
            this.inDoorThreshold = HwActivityManagerService.SERVICE_ADJ;
            this.brighenDebounceTime = 3000;
            this.darkenDebounceTime = 3000;
            this.brightenLinePoints.clear();
            this.brightenLinePoints.add(new PointF(0.0f, 15.0f));
            this.brightenLinePoints.add(new PointF(2.0f, 15.0f));
            this.brightenLinePoints.add(new PointF(10.0f, 19.0f));
            this.brightenLinePoints.add(new PointF(100.0f, 239.0f));
            this.brightenLinePoints.add(new PointF(500.0f, 439.0f));
            this.brightenLinePoints.add(new PointF(1000.0f, 989.0f));
            this.brightenLinePoints.add(new PointF(40000.0f, 989.0f));
            this.darkenLinePoints.clear();
            this.darkenLinePoints.add(new PointF(0.0f, 1.0f));
            this.darkenLinePoints.add(new PointF(1.0f, 1.0f));
            this.darkenLinePoints.add(new PointF(15.0f, 15.0f));
            this.darkenLinePoints.add(new PointF(20.0f, 15.0f));
            this.darkenLinePoints.add(new PointF(85.0f, 80.0f));
            this.darkenLinePoints.add(new PointF(100.0f, 80.0f));
            this.darkenLinePoints.add(new PointF(420.0f, 400.0f));
            this.darkenLinePoints.add(new PointF(500.0f, 400.0f));
            this.darkenLinePoints.add(new PointF(600.0f, 500.0f));
            this.darkenLinePoints.add(new PointF(1000.0f, 500.0f));
            this.darkenLinePoints.add(new PointF(2000.0f, 1000.0f));
            this.darkenLinePoints.add(new PointF(40000.0f, 1000.0f));
        }
    }

    private static class Element_BrighenDebounceTime extends HwXmlElement {
        /* synthetic */ Element_BrighenDebounceTime(Element_BrighenDebounceTime -this0) {
            this();
        }

        private Element_BrighenDebounceTime() {
        }

        public String getName() {
            return "BrighenDebounceTime";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSunlightReadabilityEnhancementXmlLoader.mData.brighenDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSunlightReadabilityEnhancementXmlLoader.mData.brighenDebounceTime < 10000;
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
            HwSunlightReadabilityEnhancementXmlLoader.mData.brightenLinePoints = HwXmlElement.parsePointFList(parser, HwSunlightReadabilityEnhancementXmlLoader.mData.brightenLinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwSunlightReadabilityEnhancementXmlLoader.checkPointsListIsOK(HwSunlightReadabilityEnhancementXmlLoader.mData.brightenLinePoints);
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
            HwSunlightReadabilityEnhancementXmlLoader.mData.darkenDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSunlightReadabilityEnhancementXmlLoader.mData.darkenDebounceTime < 10000;
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
            HwSunlightReadabilityEnhancementXmlLoader.mData.darkenLinePoints = HwXmlElement.parsePointFList(parser, HwSunlightReadabilityEnhancementXmlLoader.mData.darkenLinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwSunlightReadabilityEnhancementXmlLoader.checkPointsListIsOK(HwSunlightReadabilityEnhancementXmlLoader.mData.darkenLinePoints);
        }
    }

    private static class Element_HiACEConfig extends HwXmlElement {
        private boolean mParseStarted;

        /* synthetic */ Element_HiACEConfig(Element_HiACEConfig -this0) {
            this();
        }

        private Element_HiACEConfig() {
        }

        public String getName() {
            return "HiACEConfig";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (this.mParseStarted) {
                return false;
            }
            this.mParseStarted = true;
            return true;
        }
    }

    private static class Element_InDoorThreshold extends HwXmlElement {
        /* synthetic */ Element_InDoorThreshold(Element_InDoorThreshold -this0) {
            this();
        }

        private Element_InDoorThreshold() {
        }

        public String getName() {
            return "InDoorThreshold";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSunlightReadabilityEnhancementXmlLoader.mData.inDoorThreshold = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSunlightReadabilityEnhancementXmlLoader.mData.inDoorThreshold >= 0;
        }
    }

    private static class Element_LightSensorRateMills extends HwXmlElement {
        /* synthetic */ Element_LightSensorRateMills(Element_LightSensorRateMills -this0) {
            this();
        }

        private Element_LightSensorRateMills() {
        }

        public String getName() {
            return "LightSensorRateMills";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSunlightReadabilityEnhancementXmlLoader.mData.lightSensorRateMills = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSunlightReadabilityEnhancementXmlLoader.mData.lightSensorRateMills > 0;
        }
    }

    private static class Element_OutDoorThreshold extends HwXmlElement {
        /* synthetic */ Element_OutDoorThreshold(Element_OutDoorThreshold -this0) {
            this();
        }

        private Element_OutDoorThreshold() {
        }

        public String getName() {
            return "OutDoorThreshold";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSunlightReadabilityEnhancementXmlLoader.mData.outDoorThreshold = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSunlightReadabilityEnhancementXmlLoader.mData.outDoorThreshold >= HwSunlightReadabilityEnhancementXmlLoader.mData.inDoorThreshold;
        }
    }

    private static class Element_SceneCamera extends HwXmlElement {
        /* synthetic */ Element_SceneCamera(Element_SceneCamera -this0) {
            this();
        }

        private Element_SceneCamera() {
        }

        public String getName() {
            return "SceneCamera";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSunlightReadabilityEnhancementXmlLoader.mData.sceneCameraEnable = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_SceneRecognition extends HwXmlElement {
        /* synthetic */ Element_SceneRecognition(Element_SceneRecognition -this0) {
            this();
        }

        private Element_SceneRecognition() {
        }

        public String getName() {
            return "SceneRecognition";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_UsingBLC extends HwXmlElement {
        /* synthetic */ Element_UsingBLC(Element_UsingBLC -this0) {
            this();
        }

        private Element_UsingBLC() {
        }

        public String getName() {
            return "UsingBLC";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSunlightReadabilityEnhancementXmlLoader.mData.usingBLC = HwXmlElement.string2Int(parser.nextText()) == 1;
            return true;
        }
    }

    private static class Element_UsingSRE extends HwXmlElement {
        /* synthetic */ Element_UsingSRE(Element_UsingSRE -this0) {
            this();
        }

        private Element_UsingSRE() {
        }

        public String getName() {
            return "UsingSRE";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSunlightReadabilityEnhancementXmlLoader.mData.usingSRE = HwXmlElement.string2Int(parser.nextText()) == 1;
            return true;
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

    /* JADX WARNING: Missing block: B:15:0x0025, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Data getData() {
        Throwable th;
        synchronized (mLoaderLock) {
            Data retData;
            Data retData2;
            try {
                if (mLoader == null) {
                    mLoader = new HwSunlightReadabilityEnhancementXmlLoader();
                }
                retData = (Data) mData.clone();
                if (retData == null) {
                    retData2 = new Data();
                    retData2.loadDefaultConfig();
                    retData = retData2;
                }
            } catch (CloneNotSupportedException e) {
                Slog.e(TAG, "getData() failed! " + e);
                retData2 = new Data();
                retData2.loadDefaultConfig();
            } catch (RuntimeException e2) {
                Slog.e(TAG, "getData() failed! " + e2);
                retData2 = new Data();
                retData2.loadDefaultConfig();
            } catch (Throwable th2) {
                th = th2;
                retData = retData2;
            }
        }
        throw th;
    }

    private HwSunlightReadabilityEnhancementXmlLoader() {
        if (HWDEBUG) {
            Slog.d(TAG, "HwSunlightReadabilityEnhancementXmlLoader()");
        }
        String xmlPath = getXmlPath();
        if (xmlPath == null) {
            mData.clearConfig();
            mData.printData();
            return;
        }
        if (!parseXml(xmlPath)) {
            mData.loadDefaultConfig();
        }
        mData.printData();
    }

    private boolean parseXml(String xmlPath) {
        if (xmlPath == null) {
            Slog.e(TAG, "parseXml() error! xmlPath is null");
            return false;
        }
        if (HWFLOW) {
            Slog.i(TAG, "parseXml() getXmlPath = " + xmlPath);
        }
        HwXmlParser xmlParser = new HwXmlParser(xmlPath);
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
        HwXmlElement rootElement = parser.registerRootElement(new Element_HiACEConfig());
        rootElement.registerChildElement(new Element_UsingBLC());
        rootElement.registerChildElement(new Element_UsingSRE());
        rootElement.registerChildElement(new Element_LightSensorRateMills());
        rootElement.registerChildElement(new Element_SceneRecognition()).registerChildElement(new Element_SceneCamera());
        rootElement.registerChildElement(new Element_OutDoorThreshold());
        rootElement.registerChildElement(new Element_InDoorThreshold());
        rootElement.registerChildElement(new Element_BrighenDebounceTime());
        rootElement.registerChildElement(new Element_DarkenDebounceTime());
        rootElement.registerChildElement(new Element_BrightenLinePoints()).registerChildElement(new Element_BrightenLinePoints_Point());
        rootElement.registerChildElement(new Element_DarkenLinePoints()).registerChildElement(new Element_DarkenLinePoints_Point());
    }

    private String getXmlPath() {
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s", new Object[]{XML_NAME}), 0);
        if (xmlFile != null) {
            return xmlFile.getAbsolutePath();
        }
        Slog.e(TAG, "getXmlPath() error! can't find xml file.");
        return null;
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
