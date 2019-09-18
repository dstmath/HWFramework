package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.server.gesture.GestureNavConst;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

final class HwSmartBackLightXmlLoader {
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    /* access modifiers changed from: private */
    public static final boolean HWFLOW;
    private static final String TAG = "HwSmartBackLightXmlLoader";
    private static final String XML_NAME = "SBLConfig.xml";
    /* access modifiers changed from: private */
    public static Data mData = new Data();
    private static HwSmartBackLightXmlLoader mLoader;
    private static final Object mLoaderLock = new Object();

    public static class Data {
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

        public void printData() {
            if (HwSmartBackLightXmlLoader.HWFLOW) {
                Slog.i(HwSmartBackLightXmlLoader.TAG, "printData() lightSensorRateMills=" + this.lightSensorRateMills + ", apicalADLevel=" + this.apicalADLevel + ", sceneVideoEnable=" + this.sceneVideoEnable + ", sceneGalleryEnable=" + this.sceneGalleryEnable + ", sceneCameraEnable=" + this.sceneCameraEnable + ", outDoorThreshold=" + this.outDoorThreshold + ", inDoorThreshold=" + this.inDoorThreshold + ", brighenDebounceTime=" + this.brighenDebounceTime + ", darkenDebounceTime=" + this.darkenDebounceTime);
                StringBuilder sb = new StringBuilder();
                sb.append("printData() brightenLinePoints=");
                sb.append(this.brightenLinePoints);
                Slog.i(HwSmartBackLightXmlLoader.TAG, sb.toString());
                StringBuilder sb2 = new StringBuilder();
                sb2.append("printData() darkenLinePoints=");
                sb2.append(this.darkenLinePoints);
                Slog.i(HwSmartBackLightXmlLoader.TAG, sb2.toString());
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
            this.brightenLinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 15.0f));
            this.brightenLinePoints.add(new PointF(2.0f, 15.0f));
            this.brightenLinePoints.add(new PointF(10.0f, 19.0f));
            this.brightenLinePoints.add(new PointF(100.0f, 239.0f));
            this.brightenLinePoints.add(new PointF(500.0f, 439.0f));
            this.brightenLinePoints.add(new PointF(1000.0f, 989.0f));
            this.brightenLinePoints.add(new PointF(40000.0f, 989.0f));
            this.darkenLinePoints.clear();
            this.darkenLinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f));
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

    private static class Element_BaseGroup extends HwXmlElement {
        private Element_BaseGroup() {
        }

        public String getName() {
            return "BaseGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"LightSensorRateMills", "ApicalADLevel", "OutDoorThreshold", "InDoorThreshold", "BrighenDebounceTime", "DarkenDebounceTime"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -790346984:
                    if (valueName.equals("InDoorThreshold")) {
                        c = 3;
                        break;
                    }
                case -599365355:
                    if (valueName.equals("DarkenDebounceTime")) {
                        c = 5;
                        break;
                    }
                case -85825767:
                    if (valueName.equals("BrighenDebounceTime")) {
                        c = 4;
                        break;
                    }
                case 701544399:
                    if (valueName.equals("OutDoorThreshold")) {
                        c = 2;
                        break;
                    }
                case 1001305191:
                    if (valueName.equals("LightSensorRateMills")) {
                        c = 0;
                        break;
                    }
                case 1593528077:
                    if (valueName.equals("ApicalADLevel")) {
                        c = 1;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwSmartBackLightXmlLoader.mData.lightSensorRateMills = string2Int(parser.nextText());
                    break;
                case 1:
                    HwSmartBackLightXmlLoader.mData.apicalADLevel = string2Int(parser.nextText());
                    break;
                case 2:
                    HwSmartBackLightXmlLoader.mData.outDoorThreshold = string2Int(parser.nextText());
                    break;
                case 3:
                    HwSmartBackLightXmlLoader.mData.inDoorThreshold = string2Int(parser.nextText());
                    break;
                case 4:
                    HwSmartBackLightXmlLoader.mData.brighenDebounceTime = string2Int(parser.nextText());
                    break;
                case 5:
                    HwSmartBackLightXmlLoader.mData.darkenDebounceTime = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwSmartBackLightXmlLoader.mData.lightSensorRateMills > 0 && HwSmartBackLightXmlLoader.mData.apicalADLevel > 0 && HwSmartBackLightXmlLoader.mData.apicalADLevel <= 255 && HwSmartBackLightXmlLoader.mData.outDoorThreshold >= HwSmartBackLightXmlLoader.mData.inDoorThreshold && HwSmartBackLightXmlLoader.mData.inDoorThreshold >= 0 && HwSmartBackLightXmlLoader.mData.brighenDebounceTime < 10000 && HwSmartBackLightXmlLoader.mData.darkenDebounceTime < 10000;
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
            HwSmartBackLightXmlLoader.mData.brightenLinePoints = parsePointFList(parser, HwSmartBackLightXmlLoader.mData.brightenLinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwSmartBackLightXmlLoader.checkPointsListIsOK(HwSmartBackLightXmlLoader.mData.brightenLinePoints);
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
            HwSmartBackLightXmlLoader.mData.darkenLinePoints = parsePointFList(parser, HwSmartBackLightXmlLoader.mData.darkenLinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwSmartBackLightXmlLoader.checkPointsListIsOK(HwSmartBackLightXmlLoader.mData.darkenLinePoints);
        }
    }

    private static class Element_SBLConfig extends HwXmlElement {
        private boolean mParseStarted;

        private Element_SBLConfig() {
        }

        public String getName() {
            return "SBLConfig";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (this.mParseStarted) {
                return false;
            }
            this.mParseStarted = true;
            return true;
        }
    }

    private static class Element_SceneRecognition extends HwXmlElement {
        private Element_SceneRecognition() {
        }

        public String getName() {
            return "SceneRecognition";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_SceneRecognitionGroup extends HwXmlElement {
        private Element_SceneRecognitionGroup() {
        }

        public String getName() {
            return "SceneRecognitionGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"SceneVideo", "SceneGallery", "SceneCamera"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x0054  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0072  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -229548177) {
                if (hashCode != 922867377) {
                    if (hashCode == 2093405510 && valueName.equals("SceneGallery")) {
                        c = 1;
                        switch (c) {
                            case 0:
                                HwSmartBackLightXmlLoader.mData.sceneVideoEnable = string2Boolean(parser.nextText());
                                break;
                            case 1:
                                HwSmartBackLightXmlLoader.mData.sceneGalleryEnable = string2Boolean(parser.nextText());
                                break;
                            case 2:
                                HwSmartBackLightXmlLoader.mData.sceneCameraEnable = string2Boolean(parser.nextText());
                                break;
                            default:
                                Slog.e(this.TAG, "unknow valueName=" + valueName);
                                return false;
                        }
                        return true;
                    }
                } else if (valueName.equals("SceneCamera")) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("SceneVideo")) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
            return true;
        }
    }

    private static class Element_VideoSceneEnhance extends HwXmlElement {
        private Element_VideoSceneEnhance() {
        }

        public String getName() {
            return "VideoSceneEnhance";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_DarknessConfig extends HwXmlElement {
        private Element_VideoSceneEnhance_DarknessConfig() {
        }

        public String getName() {
            return "DarknessConfig";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_DarknessConfigGroup extends HwXmlElement {
        private Element_VideoSceneEnhance_DarknessConfigGroup() {
        }

        public String getName() {
            return "VideoSceneEnhance_DarknessConfigGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"DarknessApicalADLevel", "DarknessAmbidentBrightnessShift"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0054  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1707245654) {
                if (hashCode == -1232861888 && valueName.equals("DarknessApicalADLevel")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            HwSmartBackLightXmlLoader.mData.darknessApicalADLevel = Integer.parseInt(parser.nextText());
                            break;
                        case 1:
                            HwSmartBackLightXmlLoader.mData.darknessAmbidentBrightnessShift = Integer.parseInt(parser.nextText());
                            break;
                        default:
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                    }
                    return true;
                }
            } else if (valueName.equals("DarknessAmbidentBrightnessShift")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_Enabled extends HwXmlElement {
        private Element_VideoSceneEnhance_Enabled() {
        }

        public String getName() {
            return "VideoSceneEnhanceEnabled";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwSmartBackLightXmlLoader.mData.videoSceneEnhanceEnabled = Boolean.parseBoolean(parser.nextText());
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_IndoorConfig extends HwXmlElement {
        private Element_VideoSceneEnhance_IndoorConfig() {
        }

        public String getName() {
            return "IndoorConfig";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_IndoorConfigGroup extends HwXmlElement {
        private Element_VideoSceneEnhance_IndoorConfigGroup() {
        }

        public String getName() {
            return "VideoSceneEnhance_IndoorConfigGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"IndoorApicalADLevel", "IndoorAmbidentBrightnessShift"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0054  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -936208028) {
                if (hashCode == 1455497082 && valueName.equals("IndoorApicalADLevel")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            HwSmartBackLightXmlLoader.mData.indoorApicalADLevel = Integer.parseInt(parser.nextText());
                            break;
                        case 1:
                            HwSmartBackLightXmlLoader.mData.indoorAmbidentBrightnessShift = Integer.parseInt(parser.nextText());
                            break;
                        default:
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                    }
                    return true;
                }
            } else if (valueName.equals("IndoorAmbidentBrightnessShift")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_OutdoorConfig extends HwXmlElement {
        private Element_VideoSceneEnhance_OutdoorConfig() {
        }

        public String getName() {
            return "OutdoorConfig";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_OutdoorConfigGroup extends HwXmlElement {
        private Element_VideoSceneEnhance_OutdoorConfigGroup() {
        }

        public String getName() {
            return "VideoSceneEnhance_OutdoorConfigGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"OutdoorApicalADLevel", "OutdoorAmbidentBrightnessShift"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0054  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1138949669) {
                if (hashCode == -966369103 && valueName.equals("OutdoorApicalADLevel")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            HwSmartBackLightXmlLoader.mData.outdoorApicalADLevel = Integer.parseInt(parser.nextText());
                            break;
                        case 1:
                            HwSmartBackLightXmlLoader.mData.outdoorAmbidentBrightnessShift = Integer.parseInt(parser.nextText());
                            break;
                        default:
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                    }
                    return true;
                }
            } else if (valueName.equals("OutdoorAmbidentBrightnessShift")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_Recognition extends HwXmlElement {
        private Element_VideoSceneEnhance_Recognition() {
        }

        public String getName() {
            return "VideoSceneRecognition";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_VideoSceneEnhance_RecognitionGroup extends HwXmlElement {
        private Element_VideoSceneEnhance_RecognitionGroup() {
        }

        public String getName() {
            return "VideoSceneEnhance_RecognitionGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"VideoSceneDarknessThreshold", "VideoSceneIndoorThreshold"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0054  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1682331859) {
                if (hashCode == -1039405529 && valueName.equals("VideoSceneIndoorThreshold")) {
                    c = 1;
                    switch (c) {
                        case 0:
                            HwSmartBackLightXmlLoader.mData.videoSceneDarknessThreshold = Integer.parseInt(parser.nextText());
                            break;
                        case 1:
                            HwSmartBackLightXmlLoader.mData.videoSceneIndoorThreshold = Integer.parseInt(parser.nextText());
                            break;
                        default:
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                    }
                    return true;
                }
            } else if (valueName.equals("VideoSceneDarknessThreshold")) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
            return true;
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0041, code lost:
        if (0 == 0) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0043, code lost:
        new com.android.server.display.HwSmartBackLightXmlLoader.Data().loadDefaultConfig();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004d, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004f, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0051, code lost:
        throw r1;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:10:0x0014, B:16:0x0023] */
    public static Data getData() {
        Data retData;
        Data data;
        synchronized (mLoaderLock) {
            retData = null;
            try {
                if (mLoader == null) {
                    mLoader = new HwSmartBackLightXmlLoader();
                }
                retData = mData;
                if (retData == null) {
                    data = new Data();
                    retData = data;
                    retData.loadDefaultConfig();
                }
            } catch (RuntimeException e) {
                Slog.e(TAG, "getData() failed! " + e);
                if (0 == 0) {
                    data = new Data();
                }
            }
        }
        return retData;
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
        HwXmlParser xmlParser = new HwXmlParser(xmlPath);
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
        HwXmlElement rootElement = parser.registerRootElement(new Element_SBLConfig());
        rootElement.registerChildElement(new Element_BaseGroup());
        rootElement.registerChildElement(new Element_SceneRecognition()).registerChildElement(new Element_SceneRecognitionGroup());
        rootElement.registerChildElement(new Element_BrightenLinePoints()).registerChildElement(new Element_BrightenLinePoints_Point());
        rootElement.registerChildElement(new Element_DarkenLinePoints()).registerChildElement(new Element_DarkenLinePoints_Point());
        HwXmlElement videoSceneEnhance = rootElement.registerChildElement(new Element_VideoSceneEnhance());
        videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_Enabled());
        videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_Recognition()).registerChildElement(new Element_VideoSceneEnhance_RecognitionGroup());
        videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_DarknessConfig()).registerChildElement(new Element_VideoSceneEnhance_DarknessConfigGroup());
        videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_IndoorConfig()).registerChildElement(new Element_VideoSceneEnhance_IndoorConfigGroup());
        videoSceneEnhance.registerChildElement(new Element_VideoSceneEnhance_OutdoorConfig()).registerChildElement(new Element_VideoSceneEnhance_OutdoorConfigGroup());
    }

    private String getXmlPath() {
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s", new Object[]{XML_NAME}), 0);
        if (xmlFile != null) {
            return xmlFile.getAbsolutePath();
        }
        Slog.e(TAG, "getXmlPath() error! can't find xml file.");
        return null;
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
