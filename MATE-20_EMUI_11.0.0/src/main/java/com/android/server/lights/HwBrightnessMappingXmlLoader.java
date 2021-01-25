package com.android.server.lights;

import android.graphics.PointF;
import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import com.huawei.util.LogEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public final class HwBrightnessMappingXmlLoader {
    private static final int FAILED_RETURN_VALUE = -1;
    private static final boolean HWDEBUG = (LogEx.getLogHWInfo() && LogEx.getHWModuleLog() && Log.isLoggable(TAG, 3));
    private static final boolean HWFLOW;
    private static final int INIT_DEFAULT_BRIGHTNESS = -1;
    private static final int INIT_PANEL_NAME_LENGTH = 128;
    private static final int INIT_PANEL_VERSON_LENGTH = 32;
    private static final int POINTS_MAX_SIZE = 100;
    private static final int POINTS_MIN_SIZE = 2;
    private static final int SUCCESS_RETURN_VALUE = 0;
    private static final String TAG = "HwBrightnessMappingXmlLoader";
    private static final String XML_EXT = ".xml";
    private static final String XML_NAME_NOEXT = "HwNormalizedBrightnessMapping";
    private static Data sData = new Data();
    private static String sDefaultPanelName = null;
    private static String sDefaultPanelVersion = null;
    private static String sInwardPanelName = null;
    private static String sInwardPanelVersion = null;
    private static boolean sIsInwardFoldDevice = false;
    private static HwBrightnessMappingXmlLoader sLoader = null;
    private static Object sLoaderLock = new Object();
    private static String sOutwardPanelName = null;
    private static String sOutwardPanelVersion = null;
    private IDisplayEngineServiceEx mService = null;

    static {
        boolean z = true;
        if (!LogEx.getLogHWInfo() && (!LogEx.getHWModuleLog() || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public static class Data {
        public int brightnessAfterMapMaxForManufacture = -1;
        public int brightnessAfterMapMinForManufacture = -1;
        public int inwardBrightnessAfterMapMaxForManufacture = -1;
        public int inwardBrightnessAfterMapMinForManufacture = -1;
        public List<PointF> inwardMappingLinePointsList = new ArrayList();
        public boolean isInwardFoldDevice = false;
        public List<PointF> mappingLinePointsList = new ArrayList();
        public int outwardBrightnessAfterMapMaxForManufacture = -1;
        public int outwardBrightnessAfterMapMinForManufacture = -1;
        public List<PointF> outwardMappingLinePointsList = new ArrayList();

        public void printData() {
            if (!HwBrightnessMappingXmlLoader.HWFLOW) {
                return;
            }
            if (!HwBrightnessMappingXmlLoader.sIsInwardFoldDevice) {
                SlogEx.i(HwBrightnessMappingXmlLoader.TAG, "printData() brightnessAfterMapMinForManufacture=" + this.brightnessAfterMapMinForManufacture + ",brightnessAfterMapMaxForManufacture=" + this.brightnessAfterMapMaxForManufacture);
                StringBuilder sb = new StringBuilder();
                sb.append("printData() mappingLinePointsList=");
                sb.append(this.mappingLinePointsList);
                SlogEx.i(HwBrightnessMappingXmlLoader.TAG, sb.toString());
                return;
            }
            SlogEx.i(HwBrightnessMappingXmlLoader.TAG, "printData() sIsInwardFoldDevice=" + HwBrightnessMappingXmlLoader.sIsInwardFoldDevice + ",inwardBrightnessAfterMapMinForManufacture=" + this.inwardBrightnessAfterMapMinForManufacture + ",inwardBrightnessAfterMapMaxForManufacture=" + this.inwardBrightnessAfterMapMaxForManufacture + ",outwardBrightnessAfterMapMinForManufacture=" + this.outwardBrightnessAfterMapMinForManufacture + ",outwardBrightnessAfterMapMaxForManufacture=" + this.outwardBrightnessAfterMapMaxForManufacture);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("printData() inwardMappingLinePointsList=");
            sb2.append(this.inwardMappingLinePointsList);
            SlogEx.i(HwBrightnessMappingXmlLoader.TAG, sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            sb3.append("printData() outwardMappingLinePointsList=");
            sb3.append(this.outwardMappingLinePointsList);
            SlogEx.i(HwBrightnessMappingXmlLoader.TAG, sb3.toString());
        }

        public void loadDefaultConfig() {
            if (HwBrightnessMappingXmlLoader.HWFLOW) {
                SlogEx.i(HwBrightnessMappingXmlLoader.TAG, "loadDefaultConfig()");
            }
            this.brightnessAfterMapMinForManufacture = -1;
            this.brightnessAfterMapMaxForManufacture = -1;
            this.mappingLinePointsList.clear();
            this.inwardBrightnessAfterMapMinForManufacture = -1;
            this.inwardBrightnessAfterMapMaxForManufacture = -1;
            this.outwardBrightnessAfterMapMinForManufacture = -1;
            this.outwardBrightnessAfterMapMaxForManufacture = -1;
            this.inwardMappingLinePointsList.clear();
            this.outwardMappingLinePointsList.clear();
        }
    }

    public static Data getData() {
        Data retData;
        Data data;
        synchronized (sLoaderLock) {
            retData = null;
            try {
                if (sLoader == null) {
                    sLoader = new HwBrightnessMappingXmlLoader();
                }
                retData = sData;
                if (retData == null) {
                    data = new Data();
                    retData = data;
                    retData.loadDefaultConfig();
                }
            } catch (RuntimeException e) {
                SlogEx.w(TAG, "getData() failed, RuntimeException");
                if (0 == 0) {
                    data = new Data();
                }
            } catch (Throwable th) {
                if (0 == 0) {
                    new Data().loadDefaultConfig();
                }
                throw th;
            }
        }
        return retData;
    }

    private HwBrightnessMappingXmlLoader() {
        sIsInwardFoldDevice = HwFoldScreenState.isInwardFoldDevice();
        sData.isInwardFoldDevice = sIsInwardFoldDevice;
        if (HWDEBUG) {
            SlogEx.d(TAG, "HwBrightnessMappingXmlLoader()");
        }
        if (!parseXml(getXmlPath())) {
            sData.loadDefaultConfig();
        }
        sData.printData();
    }

    private boolean parseXml(String xmlPath) {
        if (xmlPath == null) {
            SlogEx.w(TAG, "parseXml() mapping xml is not exist");
            return false;
        }
        HwXmlParser xmlParser = new HwXmlParser(xmlPath);
        registerElement(xmlParser);
        if (!xmlParser.parse()) {
            SlogEx.w(TAG, "parseXml() error! xmlParser.parse() failed!");
            return false;
        } else if (!xmlParser.isXmlDataValid()) {
            SlogEx.w(TAG, "parseXml() error! xmlParser.isXmlDataValid() failed!");
            return false;
        } else if (!HWFLOW) {
            return true;
        } else {
            SlogEx.i(TAG, "parseXml() load success!");
            return true;
        }
    }

    private void registerElement(HwXmlParser parser) {
        HwXmlElement rootElement = parser.registerRootElement(new ElementHwBrightnessMappingConfig());
        if (!sIsInwardFoldDevice) {
            rootElement.registerChildElement(new ElementDefaultBrightnessGroup());
            rootElement.registerChildElement(new ElementDefaultMappingBrightnessLine()).registerChildElement(new ElementDefaultMappingBrightnessLinePoints());
            return;
        }
        rootElement.registerChildElement(new ElementInwardFoldBrightnessGroup());
        rootElement.registerChildElement(new ElementInwardMappingBrightnessLine()).registerChildElement(new ElementInwardMappingBrightnessLinePoints());
        rootElement.registerChildElement(new ElementOutwardMappingBrightnessLine()).registerChildElement(new ElementOutwardMappingBrightnessLinePoints());
    }

    private String getXmlPath() {
        File xmlFile = getXmlFile();
        String xmlCanonicalPath = null;
        if (xmlFile == null) {
            return null;
        }
        try {
            xmlCanonicalPath = xmlFile.getCanonicalPath();
        } catch (IOException e) {
            SlogEx.w(TAG, "get xmlCanonicalPath error IOException!");
        }
        if (HWDEBUG) {
            SlogEx.i(TAG, "get xmlCanonicalPath=" + xmlCanonicalPath);
        }
        return xmlCanonicalPath;
    }

    private File getXmlFile() {
        ArrayList<String> xmlPathList = new ArrayList<>();
        updatePanelName();
        if (sIsInwardFoldDevice) {
            if (!(sInwardPanelName == null || sInwardPanelVersion == null || sOutwardPanelVersion == null)) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s%s", XML_NAME_NOEXT, sInwardPanelName, sInwardPanelVersion, sOutwardPanelVersion, XML_EXT));
            }
            if (!(sInwardPanelName == null || sInwardPanelVersion == null)) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s%s", XML_NAME_NOEXT, sInwardPanelName, sInwardPanelVersion, XML_EXT));
            }
            if (!(sInwardPanelName == null || sOutwardPanelVersion == null)) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s%s", XML_NAME_NOEXT, sInwardPanelName, sOutwardPanelVersion, XML_EXT));
            }
            if (sInwardPanelName != null) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s%s", XML_NAME_NOEXT, sInwardPanelName, XML_EXT));
            }
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s%s", XML_NAME_NOEXT, XML_EXT));
        } else {
            if (!(sDefaultPanelName == null || sDefaultPanelVersion == null)) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s%s", XML_NAME_NOEXT, sDefaultPanelName, sDefaultPanelVersion, XML_EXT));
            }
            if (sDefaultPanelName != null) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s%s", XML_NAME_NOEXT, sDefaultPanelName, XML_EXT));
            }
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s%s", XML_NAME_NOEXT, XML_EXT));
        }
        File xmlFile = null;
        int listSize = xmlPathList.size();
        for (int i = 0; i < listSize; i++) {
            String xmlPath = xmlPathList.get(i);
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 2);
            if (xmlFile != null) {
                if (HWDEBUG) {
                    SlogEx.i(TAG, "xmlPath=" + xmlPath);
                }
                return xmlFile;
            }
        }
        return xmlFile;
    }

    private void updatePanelName() {
        IBinder binder = ServiceManagerEx.getService("DisplayEngineExService");
        if (binder == null) {
            SlogEx.w(TAG, "updatePanelName() binder is null!");
            return;
        }
        this.mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (this.mService == null) {
            SlogEx.w(TAG, "updatePanelName() mService is null!");
            return;
        }
        Bundle data = new Bundle();
        try {
            int ret = this.mService.getEffectEx(14, 13, data);
            if (ret != 0) {
                SlogEx.w(TAG, "updatePanelName() getEffect failed! ret=" + ret);
                return;
            }
            updatePanelNameFromPanelInfos(data);
        } catch (RemoteException e) {
            SlogEx.w(TAG, "updatePanelName() RemoteException ");
        }
    }

    private void updatePanelNameFromPanelInfos(Bundle data) {
        if (data == null) {
            SlogEx.w(TAG, "updatePanelNameFromPanelInfos() failed! data=null");
        } else if (sIsInwardFoldDevice) {
            sInwardPanelName = data.getString("FullpanelName");
            sOutwardPanelName = data.getString("MainpanelName");
            sInwardPanelVersion = data.getString("FulllcdPanelVersion");
            sOutwardPanelVersion = data.getString("MainlcdPanelVersion");
            sInwardPanelName = parsePanelName(sInwardPanelName);
            sOutwardPanelName = parsePanelName(sOutwardPanelName);
            sInwardPanelVersion = parsePanelVersion(sInwardPanelVersion);
            sOutwardPanelVersion = parsePanelVersion(sOutwardPanelVersion);
            if (sOutwardPanelVersion != null) {
                sOutwardPanelVersion = "O" + sOutwardPanelVersion;
            }
            if (HWDEBUG) {
                SlogEx.i(TAG, "sInwardPanelName=" + sInwardPanelName + ",sOutwardPanelName=" + sOutwardPanelName + ",sInwardPanelVersion=" + sInwardPanelVersion + ",sOutwardPanelVersion=" + sOutwardPanelVersion);
            }
            SlogEx.i(TAG, "sInwardPanelVersion=" + sInwardPanelVersion + ",sOutwardPanelVersion=" + sOutwardPanelVersion);
        } else {
            sDefaultPanelName = data.getString("panelName");
            sDefaultPanelVersion = data.getString("lcdPanelVersion");
            sDefaultPanelName = parsePanelName(sDefaultPanelName);
            sDefaultPanelVersion = parsePanelVersion(sDefaultPanelVersion);
            if (HWDEBUG) {
                SlogEx.i(TAG, "sDefaultPanelName=" + sDefaultPanelName + ",sDefaultPanelVersion=" + sDefaultPanelVersion);
            }
            SlogEx.i(TAG, "sDefaultPanelVersion=" + sDefaultPanelVersion);
        }
    }

    private String parsePanelName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().replace(' ', '_');
    }

    private String parsePanelVersion(String name) {
        String lcdVersion;
        int index;
        if (name == null || (index = (lcdVersion = name.trim()).indexOf("VER:")) == -1) {
            return null;
        }
        return lcdVersion.substring("VER:".length() + index);
    }

    /* access modifiers changed from: private */
    public static boolean isPointsListValid(List<PointF> list) {
        if (list == null) {
            SlogEx.w(TAG, "isPointsListValid() error! list is null");
            return false;
        } else if (list.size() < 2 || list.size() >= 100) {
            SlogEx.w(TAG, "isPointsListValid() error! list size=" + list.size() + " is out of range");
            return false;
        } else {
            PointF lastPoint = null;
            for (PointF point : list) {
                if (lastPoint == null || point.x > lastPoint.x) {
                    lastPoint = point;
                } else {
                    SlogEx.w(TAG, "isPointsListValid() error! x in list isn't a increasing sequence, " + point.x + "<=" + lastPoint.x);
                    return false;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementHwBrightnessMappingConfig extends HwXmlElement {
        private boolean mParseStarted;

        private ElementHwBrightnessMappingConfig() {
        }

        @Override // com.android.server.lights.HwXmlElement
        public String getName() {
            return HwBrightnessMappingXmlLoader.XML_NAME_NOEXT;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (this.mParseStarted) {
                return false;
            }
            this.mParseStarted = true;
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDefaultBrightnessGroup extends HwXmlElement {
        private ElementDefaultBrightnessGroup() {
        }

        @Override // com.android.server.lights.HwXmlElement
        public String getName() {
            return "DefaultBrightnessGroup";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public List<String> getNameList() {
            return Arrays.asList("BrightnessAfterMapMinForManufacture", "BrightnessAfterMapMaxForManufacture");
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                SlogEx.e(this.TAG, "parser DefaultBrightnessGroup is null");
                return false;
            }
            String valueName = parser.getName();
            char c = 65535;
            int hashCode = valueName.hashCode();
            if (hashCode != -478194357) {
                if (hashCode == 1836850617 && valueName.equals("BrightnessAfterMapMinForManufacture")) {
                    c = 0;
                }
            } else if (valueName.equals("BrightnessAfterMapMaxForManufacture")) {
                c = 1;
            }
            if (c == 0) {
                HwBrightnessMappingXmlLoader.sData.brightnessAfterMapMinForManufacture = string2Int(parser.nextText());
            } else if (c != 1) {
                SlogEx.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessMappingXmlLoader.sData.brightnessAfterMapMaxForManufacture = string2Int(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isParsedValueValid() {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDefaultMappingBrightnessLine extends HwXmlElement {
        private ElementDefaultMappingBrightnessLine() {
        }

        @Override // com.android.server.lights.HwXmlElement
        public String getName() {
            return "MappingBrightnessPoints";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDefaultMappingBrightnessLinePoints extends HwXmlElement {
        private ElementDefaultMappingBrightnessLinePoints() {
        }

        @Override // com.android.server.lights.HwXmlElement
        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessMappingXmlLoader.sData.mappingLinePointsList = parsePointFList(parser, HwBrightnessMappingXmlLoader.sData.mappingLinePointsList);
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isParsedValueValid() {
            return HwBrightnessMappingXmlLoader.isPointsListValid(HwBrightnessMappingXmlLoader.sData.mappingLinePointsList);
        }
    }

    /* access modifiers changed from: private */
    public static class ElementInwardFoldBrightnessGroup extends HwXmlElement {
        private ElementInwardFoldBrightnessGroup() {
        }

        @Override // com.android.server.lights.HwXmlElement
        public String getName() {
            return "InwardFoldBrightnessGroup";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public List<String> getNameList() {
            return Arrays.asList("InwardBrightnessAfterMapMinForManufacture", "InwardBrightnessAfterMapMaxForManufacture", "OutwardBrightnessAfterMapMinForManufacture", "OutwardBrightnessAfterMapMaxForManufacture");
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                SlogEx.e(this.TAG, "parser InwardFoldBrightnessGroup is null");
                return false;
            }
            String valueName = parser.getName();
            char c = 65535;
            switch (valueName.hashCode()) {
                case -449342358:
                    if (valueName.equals("InwardBrightnessAfterMapMaxForManufacture")) {
                        c = 1;
                        break;
                    }
                    break;
                case -307574065:
                    if (valueName.equals("OutwardBrightnessAfterMapMinForManufacture")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1672348257:
                    if (valueName.equals("OutwardBrightnessAfterMapMaxForManufacture")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1865702616:
                    if (valueName.equals("InwardBrightnessAfterMapMinForManufacture")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                HwBrightnessMappingXmlLoader.sData.inwardBrightnessAfterMapMinForManufacture = string2Int(parser.nextText());
            } else if (c == 1) {
                HwBrightnessMappingXmlLoader.sData.inwardBrightnessAfterMapMaxForManufacture = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessMappingXmlLoader.sData.outwardBrightnessAfterMapMinForManufacture = string2Int(parser.nextText());
            } else if (c != 3) {
                SlogEx.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessMappingXmlLoader.sData.outwardBrightnessAfterMapMaxForManufacture = string2Int(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isParsedValueValid() {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementInwardMappingBrightnessLine extends HwXmlElement {
        private ElementInwardMappingBrightnessLine() {
        }

        @Override // com.android.server.lights.HwXmlElement
        public String getName() {
            return "InwardMappingBrightnessPoints";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementInwardMappingBrightnessLinePoints extends HwXmlElement {
        private ElementInwardMappingBrightnessLinePoints() {
        }

        @Override // com.android.server.lights.HwXmlElement
        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessMappingXmlLoader.sData.inwardMappingLinePointsList = parsePointFList(parser, HwBrightnessMappingXmlLoader.sData.inwardMappingLinePointsList);
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isParsedValueValid() {
            return HwBrightnessMappingXmlLoader.isPointsListValid(HwBrightnessMappingXmlLoader.sData.inwardMappingLinePointsList);
        }
    }

    /* access modifiers changed from: private */
    public static class ElementOutwardMappingBrightnessLine extends HwXmlElement {
        private ElementOutwardMappingBrightnessLine() {
        }

        @Override // com.android.server.lights.HwXmlElement
        public String getName() {
            return "OutwardMappingBrightnessPoints";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementOutwardMappingBrightnessLinePoints extends HwXmlElement {
        private ElementOutwardMappingBrightnessLinePoints() {
        }

        @Override // com.android.server.lights.HwXmlElement
        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessMappingXmlLoader.sData.outwardMappingLinePointsList = parsePointFList(parser, HwBrightnessMappingXmlLoader.sData.outwardMappingLinePointsList);
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.lights.HwXmlElement
        public boolean isParsedValueValid() {
            return HwBrightnessMappingXmlLoader.isPointsListValid(HwBrightnessMappingXmlLoader.sData.outwardMappingLinePointsList);
        }
    }
}
