package com.android.server.lights;

import android.graphics.PointF;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwNormalizedBrightnessMapping {
    private static final int DEFAULT_BRIGHTNESS = -1;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable("HwNormalizedBrightnessMapping", 3)));
    private static final boolean HWFLOW;
    private static final String TAG = "HwNormalizedBrightnessMapping";
    private static final String XML_EXT = ".xml";
    private static final String XML_NAME_NOEXT = "HwNormalizedBrightnessMapping";
    private int mBrightnessAfterMapMax = -1;
    private int mBrightnessAfterMapMaxForManufacture = -1;
    private int mBrightnessAfterMapMin = -1;
    private int mBrightnessAfterMapMinForManufacture = -1;
    private int mBrightnessBeforeMapMax = -1;
    private int mBrightnessBeforeMapMin = -1;
    private boolean mConfigLoaded = false;
    private List<PointF> mMappingLinePointsList;
    private boolean mNeedBrightnessMappingEnable = false;

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable("HwNormalizedBrightnessMapping", 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public HwNormalizedBrightnessMapping(int brightnessBeforeMapMin, int brightnessBeforeMapMax, int brightnessAfterMapMin, int brightnessAfterMapMax) {
        this.mBrightnessBeforeMapMin = brightnessBeforeMapMin;
        this.mBrightnessBeforeMapMax = brightnessBeforeMapMax;
        this.mBrightnessAfterMapMin = brightnessAfterMapMin;
        this.mBrightnessAfterMapMax = brightnessAfterMapMax;
        this.mBrightnessAfterMapMinForManufacture = brightnessAfterMapMin;
        this.mBrightnessAfterMapMaxForManufacture = brightnessAfterMapMax;
    }

    public boolean needBrightnessMappingEnable() {
        if (!this.mConfigLoaded) {
            configLoaded();
        }
        return this.mNeedBrightnessMappingEnable;
    }

    private void configLoaded() {
        boolean brightnessMappingEnable = false;
        try {
            if (!getConfig()) {
                Slog.i("HwNormalizedBrightnessMapping", "initBrightnessMapping,no need BrightnessMapping");
                loadDefaultConfig();
            } else {
                brightnessMappingEnable = true;
                Slog.i("HwNormalizedBrightnessMapping", "initBrightnessMapping,need BrightnessMapping,minB=" + this.mBrightnessBeforeMapMin + ",maxB=" + this.mBrightnessBeforeMapMax + ",min=" + this.mBrightnessAfterMapMin + ",max=" + this.mBrightnessAfterMapMax);
            }
        } catch (IOException e) {
            Slog.e("HwNormalizedBrightnessMapping", "initBrightnessMapping IOException: No need BrightnessMapping");
            loadDefaultConfig();
        }
        this.mConfigLoaded = true;
        this.mNeedBrightnessMappingEnable = brightnessMappingEnable;
    }

    private boolean getConfig() throws IOException {
        File xmlFile = getXmlFile();
        if (xmlFile == null) {
            return false;
        }
        FileInputStream inputStream = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(xmlFile);
            if (!getConfigFromXML(inputStream2) || !checkConfigLoadedFromXML()) {
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    Slog.e("HwNormalizedBrightnessMapping", "getConfig inputStream: IOException");
                }
                return false;
            }
            printConfigFromXML();
            try {
                inputStream2.close();
            } catch (IOException e2) {
                Slog.e("HwNormalizedBrightnessMapping", "getConfig inputStream: IOException");
            }
            return true;
        } catch (FileNotFoundException e3) {
            Slog.e("HwNormalizedBrightnessMapping", "getConfig : FileNotFoundException");
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e4) {
            Slog.e("HwNormalizedBrightnessMapping", "getConfig : IOException");
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    Slog.e("HwNormalizedBrightnessMapping", "getConfig inputStream: IOException");
                }
            }
            throw th;
        }
    }

    private File getXmlFile() {
        String lcdname = getLcdPanelName();
        String lcdversion = getVersionFromLCD();
        ArrayList<String> xmlPathList = new ArrayList<>();
        if (lcdversion != null) {
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s%s", new Object[]{"HwNormalizedBrightnessMapping", lcdname, lcdversion, XML_EXT}));
        }
        xmlPathList.add(String.format("/xml/lcd/%s_%s%s", new Object[]{"HwNormalizedBrightnessMapping", lcdname, XML_EXT}));
        xmlPathList.add(String.format("/xml/lcd/%s%s", new Object[]{"HwNormalizedBrightnessMapping", XML_EXT}));
        File xmlFile = null;
        int listSize = xmlPathList.size();
        for (int i = 0; i < listSize; i++) {
            String xmlPath = xmlPathList.get(i);
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 2);
            if (xmlFile != null) {
                if (HWDEBUG) {
                    Slog.i("HwNormalizedBrightnessMapping", "lcdname=" + lcdname + ",lcdversion=" + lcdversion + ",xmlPath=" + xmlPath);
                }
                return xmlFile;
            }
        }
        return xmlFile;
    }

    private String getLcdPanelName() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        String panelName = null;
        if (binder == null) {
            Slog.w("HwNormalizedBrightnessMapping", "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.w("HwNormalizedBrightnessMapping", "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = mService.getEffect(14, 0, name, name.length);
            if (ret != 0) {
                Slog.e("HwNormalizedBrightnessMapping", "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                panelName = new String(name, "UTF-8").trim().replace(' ', '_');
            } catch (UnsupportedEncodingException e) {
                Slog.e("HwNormalizedBrightnessMapping", "Unsupported encoding type!");
            }
            if (HWDEBUG) {
                Slog.i("HwNormalizedBrightnessMapping", "getLcdPanelName() panelName=" + panelName);
            }
            return panelName;
        } catch (RemoteException e2) {
            Slog.e("HwNormalizedBrightnessMapping", "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }

    private String getVersionFromLCD() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        String panelVersion = null;
        if (binder == null) {
            Slog.w("HwNormalizedBrightnessMapping", "getVersionFromLCD() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.w("HwNormalizedBrightnessMapping", "getVersionFromLCD() mService is null!");
            return null;
        }
        byte[] name = new byte[32];
        try {
            int ret = mService.getEffect(14, 3, name, name.length);
            if (ret != 0) {
                Slog.e("HwNormalizedBrightnessMapping", "getVersionFromLCD() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                String lcdVersion = new String(name, "UTF-8").trim();
                int index = lcdVersion.indexOf("VER:");
                if (index != -1) {
                    panelVersion = lcdVersion.substring("VER:".length() + index);
                }
            } catch (UnsupportedEncodingException e) {
                Slog.e("HwNormalizedBrightnessMapping", "Unsupported encoding type!");
            }
            if (HWFLOW) {
                Slog.i("HwNormalizedBrightnessMapping", "getVersionFromLCD() panelVersion=" + panelVersion);
            }
            return panelVersion;
        } catch (RemoteException e2) {
            Slog.e("HwNormalizedBrightnessMapping", "getVersionFromLCD() RemoteException " + e2);
            return null;
        }
    }

    private boolean getConfigFromXML(InputStream inStream) {
        boolean mappingBrighnessLinePointsListsLoadStarted = false;
        boolean mappingBrighnessLinePointsListLoaded = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                switch (eventType) {
                    case 2:
                        String name = parser.getName();
                        if (name.equals("BrightnessAfterMapMinForManufacture")) {
                            this.mBrightnessAfterMapMinForManufacture = Integer.parseInt(parser.nextText());
                            break;
                        } else if (name.equals("BrightnessAfterMapMaxForManufacture")) {
                            this.mBrightnessAfterMapMaxForManufacture = Integer.parseInt(parser.nextText());
                            break;
                        } else if (name.equals("MappingBrightnessPoints")) {
                            mappingBrighnessLinePointsListsLoadStarted = true;
                            break;
                        } else if (name.equals("Point") && mappingBrighnessLinePointsListsLoadStarted) {
                            PointF currentPoint = new PointF();
                            String[] stringValue = parser.nextText().split(",");
                            if (stringValue.length == 2) {
                                currentPoint.x = Float.parseFloat(stringValue[0]);
                                currentPoint.y = Float.parseFloat(stringValue[1]);
                                if (checkPointIsOK(currentPoint.x, currentPoint.y)) {
                                    if (this.mMappingLinePointsList == null) {
                                        this.mMappingLinePointsList = new ArrayList();
                                    }
                                    this.mMappingLinePointsList.add(currentPoint);
                                    break;
                                } else {
                                    return false;
                                }
                            } else {
                                Slog.w("HwNormalizedBrightnessMapping", "stringValue.length is not ok,len=" + stringValue.length);
                                return false;
                            }
                        }
                    case 3:
                        if (parser.getName().equals("MappingBrightnessPoints")) {
                            mappingBrighnessLinePointsListsLoadStarted = false;
                            if (this.mMappingLinePointsList != null) {
                                mappingBrighnessLinePointsListLoaded = true;
                                break;
                            } else {
                                Slog.e("HwNormalizedBrightnessMapping", "no MappingBrightnessPoints loaded!");
                                return false;
                            }
                        } else {
                            continue;
                        }
                }
            }
            if (mappingBrighnessLinePointsListLoaded) {
                if (HWFLOW) {
                    Slog.i("HwNormalizedBrightnessMapping", "getConfigFromeXML success!");
                }
                return true;
            }
        } catch (XmlPullParserException e) {
            Slog.e("HwNormalizedBrightnessMapping", "getConfigFromXML : XmlPullParserException");
        } catch (IOException e2) {
            Slog.e("HwNormalizedBrightnessMapping", "getConfigFromXML : IOException");
        } catch (NumberFormatException e3) {
            Slog.e("HwNormalizedBrightnessMapping", "getConfigFromXML : NumberFormatException");
        }
        Slog.w("HwNormalizedBrightnessMapping", "getConfigFromeXML false!");
        return false;
    }

    private boolean checkPointIsOK(float pointX, float pointY) {
        if (pointX < ((float) this.mBrightnessBeforeMapMin)) {
            Slog.w("HwNormalizedBrightnessMapping", "pointX < mBrightnessBeforeMapMin,pointX=" + pointX + ",mBrightnessBeforeMapMin=" + this.mBrightnessBeforeMapMin);
            return false;
        } else if (pointX > ((float) this.mBrightnessBeforeMapMax)) {
            Slog.w("HwNormalizedBrightnessMapping", "pointX > mBrightnessBeforeMapMax,pointX=" + pointX + ",mBrightnessBeforeMapMax=" + this.mBrightnessBeforeMapMax);
            return false;
        } else if (pointY < ((float) this.mBrightnessAfterMapMin)) {
            Slog.w("HwNormalizedBrightnessMapping", "pointY < mBrightnessAfterMapMin,pointY=" + pointY + ",mBrightnessAfterMapMin=" + this.mBrightnessAfterMapMin);
            return false;
        } else if (pointY <= ((float) this.mBrightnessAfterMapMax)) {
            return true;
        } else {
            Slog.w("HwNormalizedBrightnessMapping", "pointY > mBrightnessAfterMapMax,pointY=" + pointY + ",mBrightnessAfterMapMax=" + this.mBrightnessAfterMapMax);
            return false;
        }
    }

    private void loadDefaultConfig() {
        this.mBrightnessAfterMapMin = -1;
        this.mBrightnessAfterMapMax = -1;
        this.mBrightnessAfterMapMinForManufacture = -1;
        this.mBrightnessAfterMapMaxForManufacture = -1;
        if (this.mMappingLinePointsList != null) {
            this.mMappingLinePointsList.clear();
        }
    }

    private boolean checkConfigLoadedFromXML() {
        if (this.mBrightnessAfterMapMinForManufacture < this.mBrightnessAfterMapMin || this.mBrightnessAfterMapMinForManufacture > this.mBrightnessAfterMapMax) {
            Slog.w("HwNormalizedBrightnessMapping", "MinForManufacture is wrong,=" + this.mBrightnessAfterMapMinForManufacture);
            return false;
        } else if (this.mBrightnessAfterMapMaxForManufacture < this.mBrightnessAfterMapMin || this.mBrightnessAfterMapMaxForManufacture > this.mBrightnessAfterMapMax) {
            Slog.w("HwNormalizedBrightnessMapping", "MaxForManufacture is wrong,=" + this.mBrightnessAfterMapMaxForManufacture);
            return false;
        } else if (this.mBrightnessAfterMapMinForManufacture >= this.mBrightnessAfterMapMaxForManufacture) {
            Slog.w("HwNormalizedBrightnessMapping", "MinMaxForManufacture is wrong,min=" + this.mBrightnessAfterMapMinForManufacture + ",max=" + this.mBrightnessAfterMapMaxForManufacture);
            return false;
        } else if (checkPointsListIsOK(this.mMappingLinePointsList)) {
            return true;
        } else {
            Slog.w("HwNormalizedBrightnessMapping", "checkPointsList mMappingLinePointsList is wrong!");
            return false;
        }
    }

    private boolean checkPointsListIsOK(List<PointF> linePointsList) {
        List<PointF> linePointsListIn = linePointsList;
        if (linePointsListIn == null) {
            Slog.e("HwNormalizedBrightnessMapping", "LoadXML false for linePointsListIn == null");
            return false;
        } else if (linePointsListIn.size() <= 1 || linePointsListIn.size() >= 100) {
            Slog.e("HwNormalizedBrightnessMapping", "LoadXML false for linePointsListIn number is wrong");
            return false;
        } else {
            PointF lastPoint = null;
            for (PointF tmpPoint : linePointsListIn) {
                if (lastPoint == null) {
                    lastPoint = tmpPoint;
                } else if (lastPoint.x >= tmpPoint.x) {
                    Slog.e("HwNormalizedBrightnessMapping", "LoadXML false,lastPoint.x=" + lastPoint.x + ",tmpPoint.x=" + tmpPoint.x);
                    return false;
                } else if (lastPoint.y > tmpPoint.y) {
                    Slog.e("HwNormalizedBrightnessMapping", "LoadXML false,lastPoint.y=" + lastPoint.y + ",tmpPoint.y=" + tmpPoint.y);
                    return false;
                } else {
                    lastPoint = tmpPoint;
                }
            }
            return true;
        }
    }

    private void printConfigFromXML() {
        if (HWFLOW) {
            Slog.i("HwNormalizedBrightnessMapping", "minForManufacture=" + this.mBrightnessAfterMapMinForManufacture + ",maxForManufacture=" + this.mBrightnessAfterMapMaxForManufacture);
            StringBuilder sb = new StringBuilder();
            sb.append("mMappingLinePointsList=");
            sb.append(this.mMappingLinePointsList);
            Slog.i("HwNormalizedBrightnessMapping", sb.toString());
        }
    }

    public int getMappingBrightness(int level) {
        if (this.mMappingLinePointsList == null || this.mBrightnessAfterMapMin == -1 || this.mBrightnessAfterMapMax == -1 || this.mBrightnessAfterMapMin == this.mBrightnessAfterMapMax) {
            return -1;
        }
        float mappingBrightness = -1.0f;
        PointF temp1 = null;
        Iterator<PointF> it = this.mMappingLinePointsList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PointF temp = it.next();
            if (temp1 == null) {
                temp1 = temp;
            }
            if (((float) level) < temp.x) {
                PointF temp2 = temp;
                if (temp2.x <= temp1.x) {
                    if (HWFLOW) {
                        Slog.d("HwNormalizedBrightnessMapping", "mapping_temp1.x <= temp2.x,x" + temp.x + ",y = " + temp.y);
                    }
                    return -1;
                }
                mappingBrightness = (((temp2.y - temp1.y) * (((float) level) - temp1.x)) / (temp2.x - temp1.x)) + temp1.y;
            } else {
                temp1 = temp;
                mappingBrightness = temp1.y;
            }
        }
        if (HWDEBUG) {
            Slog.d("HwNormalizedBrightnessMapping", "levelBeforeMap=" + level + ",mappingBrightness=" + mappingBrightness);
        }
        return getValidBrightness((int) (0.5f + mappingBrightness));
    }

    private int getValidBrightness(int brightness) {
        if (brightness < this.mBrightnessAfterMapMin) {
            return this.mBrightnessAfterMapMin;
        }
        if (brightness > this.mBrightnessAfterMapMax) {
            return this.mBrightnessAfterMapMax;
        }
        return brightness;
    }

    public int getMappingBrightnessForManufacture(int level) {
        float brightnessIn = (float) level;
        if (this.mBrightnessAfterMapMinForManufacture == -1 || this.mBrightnessAfterMapMaxForManufacture == -1 || this.mBrightnessAfterMapMinForManufacture == this.mBrightnessAfterMapMaxForManufacture) {
            return -1;
        }
        float mappingBrightness = ((float) this.mBrightnessAfterMapMinForManufacture) + (((brightnessIn - ((float) this.mBrightnessBeforeMapMin)) * ((float) (this.mBrightnessAfterMapMaxForManufacture - this.mBrightnessAfterMapMinForManufacture))) / ((float) (this.mBrightnessBeforeMapMax - this.mBrightnessBeforeMapMin)));
        if (HWDEBUG) {
            Slog.d("HwNormalizedBrightnessMapping", "levelBeforeMap=" + level + ",mappingBrightnessForManufacture=" + mappingBrightness);
        }
        return getValidBrightnessForManufacture((int) (0.5f + mappingBrightness));
    }

    private int getValidBrightnessForManufacture(int brightness) {
        if (brightness < this.mBrightnessAfterMapMinForManufacture) {
            return this.mBrightnessAfterMapMinForManufacture;
        }
        if (brightness > this.mBrightnessAfterMapMaxForManufacture) {
            return this.mBrightnessAfterMapMaxForManufacture;
        }
        return brightness;
    }

    public int getMappingBrightnessHighPrecision(int level) {
        if (this.mMappingLinePointsList == null || this.mBrightnessAfterMapMin == -1 || this.mBrightnessAfterMapMax == -1 || this.mBrightnessBeforeMapMin == -1 || this.mBrightnessBeforeMapMax == -1 || this.mBrightnessAfterMapMin == this.mBrightnessAfterMapMax) {
            return level;
        }
        float mappingBrightness = (float) level;
        float tempBrightness = (float) level;
        int listSize = this.mMappingLinePointsList.size();
        int i = 1;
        while (true) {
            if (i >= listSize) {
                break;
            }
            PointF temp1 = this.mMappingLinePointsList.get(i - 1);
            PointF temp2 = this.mMappingLinePointsList.get(i);
            if (((float) level) < temp1.x || ((float) level) > temp2.x) {
                i++;
            } else if (temp1.x >= temp2.x) {
                Slog.w("HwNormalizedBrightnessMapping", "origlevel=" + level + ",temp1.x=" + temp1.x + " >= temp2.x=" + temp2.x);
                return level;
            } else {
                tempBrightness = (((((float) level) - temp1.x) * (temp2.y - temp1.y)) / (temp2.x - temp1.x)) + temp1.y;
                mappingBrightness = (((tempBrightness - ((float) this.mBrightnessAfterMapMin)) * ((float) (this.mBrightnessBeforeMapMax - this.mBrightnessBeforeMapMin))) / ((float) (this.mBrightnessAfterMapMax - this.mBrightnessAfterMapMin))) + ((float) this.mBrightnessBeforeMapMin);
            }
        }
        if (HWDEBUG != 0) {
            Slog.d("HwNormalizedBrightnessMapping", "level=" + level + ",mappingBrightnessHigh=" + mappingBrightness + ",tempBrightness=" + tempBrightness);
        }
        return getValidBrightnessHighPrecision((int) (0.5f + mappingBrightness));
    }

    private int getValidBrightnessHighPrecision(int brightness) {
        if (brightness < this.mBrightnessBeforeMapMin) {
            return this.mBrightnessBeforeMapMin;
        }
        if (brightness > this.mBrightnessBeforeMapMax) {
            return this.mBrightnessBeforeMapMax;
        }
        return brightness;
    }
}
