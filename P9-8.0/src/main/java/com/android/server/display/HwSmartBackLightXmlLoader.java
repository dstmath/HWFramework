package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwSmartBackLightXmlLoader {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final String TAG = "HwSmartBackLightXmlLoader";
    private static final String XML_NAME = "SBLConfig.xml";
    private static Data mData = new Data();
    private static HwSmartBackLightXmlLoader mLoader;
    private static final Object mLoaderLock = new Object();

    public static class Data implements Cloneable {
        public int apicalADLevel = 128;
        public int brighenDebounceTime = 3000;
        public List<PointF> brightenLinePoints = new ArrayList();
        public int darkenDebounceTime = 3000;
        public List<PointF> darkenLinePoints = new ArrayList();
        public int darknessAmbidentBrightnessShift = 0;
        public int darknessApicalADLevel = 0;
        public int inDoorThreshold = 5000;
        public int indoorAmbidentBrightnessShift = 0;
        public int indoorApicalADLevel = 0;
        public int lightSensorRateMills = 300;
        public int outDoorThreshold = 8000;
        public int outdoorAmbidentBrightnessShift = 0;
        public int outdoorApicalADLevel = 0;
        public boolean sceneCameraEnable = false;
        public boolean sceneGalleryEnable = false;
        public boolean sceneVideoEnable = false;
        public int videoSceneDarknessThreshold = 0;
        public boolean videoSceneEnhanceEnabled = false;
        public int videoSceneIndoorThreshold = 0;

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
            if (HwSmartBackLightXmlLoader.HWFLOW) {
                Slog.i(HwSmartBackLightXmlLoader.TAG, "printData() lightSensorRateMills=" + this.lightSensorRateMills + ", apicalADLevel=" + this.apicalADLevel + ", sceneVideoEnable=" + this.sceneVideoEnable + ", sceneGalleryEnable=" + this.sceneGalleryEnable + ", sceneCameraEnable=" + this.sceneCameraEnable + ", outDoorThreshold=" + this.outDoorThreshold + ", inDoorThreshold=" + this.inDoorThreshold + ", brighenDebounceTime=" + this.brighenDebounceTime + ", darkenDebounceTime=" + this.darkenDebounceTime);
                Slog.i(HwSmartBackLightXmlLoader.TAG, "printData() brightenLinePoints=" + this.brightenLinePoints);
                Slog.i(HwSmartBackLightXmlLoader.TAG, "printData() darkenLinePoints=" + this.darkenLinePoints);
            }
        }

        public void loadDefaultConfig() {
            if (HwSmartBackLightXmlLoader.HWFLOW) {
                Slog.i(HwSmartBackLightXmlLoader.TAG, "loadDefaultConfig()");
            }
            this.lightSensorRateMills = 300;
            this.apicalADLevel = 128;
            this.sceneVideoEnable = false;
            this.sceneGalleryEnable = false;
            this.sceneCameraEnable = false;
            this.outDoorThreshold = 8000;
            this.inDoorThreshold = 5000;
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

    private static class Element_ApicalADLevel extends HwXmlElement {
        /* synthetic */ Element_ApicalADLevel(Element_ApicalADLevel -this0) {
            this();
        }

        private Element_ApicalADLevel() {
        }

        public String getName() {
            return "ApicalADLevel";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.apicalADLevel = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSmartBackLightXmlLoader.mData.apicalADLevel > 0 && HwSmartBackLightXmlLoader.mData.apicalADLevel <= 255;
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
            HwSmartBackLightXmlLoader.mData.brighenDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSmartBackLightXmlLoader.mData.brighenDebounceTime < 10000;
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
            HwSmartBackLightXmlLoader.mData.brightenLinePoints = HwXmlElement.parsePointFList(parser, HwSmartBackLightXmlLoader.mData.brightenLinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwSmartBackLightXmlLoader.checkPointsListIsOK(HwSmartBackLightXmlLoader.mData.brightenLinePoints);
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
            HwSmartBackLightXmlLoader.mData.darkenDebounceTime = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSmartBackLightXmlLoader.mData.darkenDebounceTime < 10000;
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
            HwSmartBackLightXmlLoader.mData.darkenLinePoints = HwXmlElement.parsePointFList(parser, HwSmartBackLightXmlLoader.mData.darkenLinePoints);
            return true;
        }

        protected boolean checkValue() {
            return HwSmartBackLightXmlLoader.checkPointsListIsOK(HwSmartBackLightXmlLoader.mData.darkenLinePoints);
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
            HwSmartBackLightXmlLoader.mData.inDoorThreshold = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSmartBackLightXmlLoader.mData.inDoorThreshold >= 0;
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
            HwSmartBackLightXmlLoader.mData.lightSensorRateMills = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSmartBackLightXmlLoader.mData.lightSensorRateMills > 0;
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
            HwSmartBackLightXmlLoader.mData.outDoorThreshold = HwXmlElement.string2Int(parser.nextText());
            return true;
        }

        protected boolean checkValue() {
            return HwSmartBackLightXmlLoader.mData.outDoorThreshold >= HwSmartBackLightXmlLoader.mData.inDoorThreshold;
        }
    }

    private static class Element_SBLConfig extends HwXmlElement {
        private boolean mParseStarted;

        /* synthetic */ Element_SBLConfig(Element_SBLConfig -this0) {
            this();
        }

        private Element_SBLConfig() {
        }

        public String getName() {
            return "SBLConfig";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (this.mParseStarted) {
                return false;
            }
            this.mParseStarted = true;
            return true;
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
            HwSmartBackLightXmlLoader.mData.sceneCameraEnable = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_SceneGallery extends HwXmlElement {
        /* synthetic */ Element_SceneGallery(Element_SceneGallery -this0) {
            this();
        }

        private Element_SceneGallery() {
        }

        public String getName() {
            return "SceneGallery";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.sceneGalleryEnable = HwXmlElement.string2Boolean(parser.nextText());
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

    private static class Element_SceneVideo extends HwXmlElement {
        /* synthetic */ Element_SceneVideo(Element_SceneVideo -this0) {
            this();
        }

        private Element_SceneVideo() {
        }

        public String getName() {
            return "SceneVideo";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.sceneVideoEnable = HwXmlElement.string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance(Element_VideoSceneEnhance -this0) {
            this();
        }

        private Element_VideoSceneEnhance() {
        }

        public String getName() {
            return "VideoSceneEnhance";
        }

        protected boolean isOptional() {
            return true;
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_DarknessApicalLevel extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_DarknessApicalLevel(Element_VideoSceneEnhance_DarknessApicalLevel -this0) {
            this();
        }

        private Element_VideoSceneEnhance_DarknessApicalLevel() {
        }

        public String getName() {
            return "DarknessApicalADLevel";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.darknessApicalADLevel = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_DarknessConfig extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_DarknessConfig(Element_VideoSceneEnhance_DarknessConfig -this0) {
            this();
        }

        private Element_VideoSceneEnhance_DarknessConfig() {
        }

        public String getName() {
            return "DarknessConfig";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_DarknessLuxShift extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_DarknessLuxShift(Element_VideoSceneEnhance_DarknessLuxShift -this0) {
            this();
        }

        private Element_VideoSceneEnhance_DarknessLuxShift() {
        }

        public String getName() {
            return "DarknessAmbidentBrightnessShift";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.darknessAmbidentBrightnessShift = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_DarknessThreshold extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_DarknessThreshold(Element_VideoSceneEnhance_DarknessThreshold -this0) {
            this();
        }

        private Element_VideoSceneEnhance_DarknessThreshold() {
        }

        public String getName() {
            return "VideoSceneDarknessThreshold";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.videoSceneDarknessThreshold = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_Enabled extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_Enabled(Element_VideoSceneEnhance_Enabled -this0) {
            this();
        }

        private Element_VideoSceneEnhance_Enabled() {
        }

        public String getName() {
            return "VideoSceneEnhanceEnabled";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.videoSceneEnhanceEnabled = Boolean.parseBoolean(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_IndoorApicalLevel extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_IndoorApicalLevel(Element_VideoSceneEnhance_IndoorApicalLevel -this0) {
            this();
        }

        private Element_VideoSceneEnhance_IndoorApicalLevel() {
        }

        public String getName() {
            return "IndoorApicalADLevel";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.indoorApicalADLevel = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_IndoorConfig extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_IndoorConfig(Element_VideoSceneEnhance_IndoorConfig -this0) {
            this();
        }

        private Element_VideoSceneEnhance_IndoorConfig() {
        }

        public String getName() {
            return "IndoorConfig";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_IndoorLuxShift extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_IndoorLuxShift(Element_VideoSceneEnhance_IndoorLuxShift -this0) {
            this();
        }

        private Element_VideoSceneEnhance_IndoorLuxShift() {
        }

        public String getName() {
            return "IndoorAmbidentBrightnessShift";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.indoorAmbidentBrightnessShift = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_IndoorThreshold extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_IndoorThreshold(Element_VideoSceneEnhance_IndoorThreshold -this0) {
            this();
        }

        private Element_VideoSceneEnhance_IndoorThreshold() {
        }

        public String getName() {
            return "VideoSceneIndoorThreshold";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.videoSceneIndoorThreshold = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_OutdoorApicalLevel extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_OutdoorApicalLevel(Element_VideoSceneEnhance_OutdoorApicalLevel -this0) {
            this();
        }

        private Element_VideoSceneEnhance_OutdoorApicalLevel() {
        }

        public String getName() {
            return "OutdoorApicalADLevel";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.outdoorApicalADLevel = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_OutdoorConfig extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_OutdoorConfig(Element_VideoSceneEnhance_OutdoorConfig -this0) {
            this();
        }

        private Element_VideoSceneEnhance_OutdoorConfig() {
        }

        public String getName() {
            return "OutdoorConfig";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_OutdoorLuxShift extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_OutdoorLuxShift(Element_VideoSceneEnhance_OutdoorLuxShift -this0) {
            this();
        }

        private Element_VideoSceneEnhance_OutdoorLuxShift() {
        }

        public String getName() {
            return "OutdoorAmbidentBrightnessShift";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.outdoorAmbidentBrightnessShift = Integer.parseInt(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_Recognition extends HwXmlElement {
        /* synthetic */ Element_VideoSceneEnhance_Recognition(Element_VideoSceneEnhance_Recognition -this0) {
            this();
        }

        private Element_VideoSceneEnhance_Recognition() {
        }

        public String getName() {
            return "VideoSceneRecognition";
        }

        protected boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
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
                    mLoader = new HwSmartBackLightXmlLoader();
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

    private HwSmartBackLightXmlLoader() {
        if (HWDEBUG) {
            Slog.d(TAG, "HwSmartBackLightXmlLoader()");
        }
        if (!parseXml(getXmlPath())) {
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
        HwXmlElement rootElement = parser.registerRootElement(new Element_SBLConfig());
        rootElement.registerChildElement(new Element_LightSensorRateMills());
        rootElement.registerChildElement(new Element_ApicalADLevel());
        HwXmlElement sceneRecognition = rootElement.registerChildElement(new Element_SceneRecognition());
        sceneRecognition.registerChildElement(new Element_SceneVideo());
        sceneRecognition.registerChildElement(new Element_SceneGallery());
        sceneRecognition.registerChildElement(new Element_SceneCamera());
        rootElement.registerChildElement(new Element_OutDoorThreshold());
        rootElement.registerChildElement(new Element_InDoorThreshold());
        rootElement.registerChildElement(new Element_BrighenDebounceTime());
        rootElement.registerChildElement(new Element_DarkenDebounceTime());
        rootElement.registerChildElement(new Element_BrightenLinePoints()).registerChildElement(new Element_BrightenLinePoints_Point());
        rootElement.registerChildElement(new Element_DarkenLinePoints()).registerChildElement(new Element_DarkenLinePoints_Point());
        HwXmlElement videoSceneEnhance = rootElement.registerChildElement(new Element_VideoSceneEnhance());
        videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_Enabled());
        HwXmlElement videoSceneEnhanceRecognition = videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_Recognition());
        videoSceneEnhanceRecognition.registerChildElement(new Element_VideoSceneEnhance_DarknessThreshold());
        videoSceneEnhanceRecognition.registerChildElement(new Element_VideoSceneEnhance_IndoorThreshold());
        HwXmlElement videoSceneEnhanceDarkConfig = videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_DarknessConfig());
        videoSceneEnhanceDarkConfig.registerChildElement(new Element_VideoSceneEnhance_DarknessApicalLevel());
        videoSceneEnhanceDarkConfig.registerChildElement(new Element_VideoSceneEnhance_DarknessLuxShift());
        HwXmlElement videoSceneEnhanceIndoorConfig = videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_IndoorConfig());
        videoSceneEnhanceIndoorConfig.registerChildElement(new Element_VideoSceneEnhance_IndoorApicalLevel());
        videoSceneEnhanceIndoorConfig.registerChildElement(new Element_VideoSceneEnhance_IndoorLuxShift());
        HwXmlElement videoSceneEnhanceOutdoorConfig = videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_OutdoorConfig());
        videoSceneEnhanceOutdoorConfig.registerChildElement(new Element_VideoSceneEnhance_OutdoorApicalLevel());
        videoSceneEnhanceOutdoorConfig.registerChildElement(new Element_VideoSceneEnhance_OutdoorLuxShift());
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
