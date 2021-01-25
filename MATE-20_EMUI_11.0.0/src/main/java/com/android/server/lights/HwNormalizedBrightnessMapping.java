package com.android.server.lights;

import android.graphics.PointF;
import android.util.Log;
import com.android.server.lights.HwBrightnessMappingXmlLoader;
import com.huawei.android.util.SlogEx;
import com.huawei.util.LogEx;
import java.util.Iterator;
import java.util.List;

public class HwNormalizedBrightnessMapping {
    private static final int DEFAULT_MODE = 0;
    private static final boolean HWDEBUG = (LogEx.getLogHWInfo() && LogEx.getHWModuleLog() && Log.isLoggable(TAG, 3));
    private static final boolean HWFLOW;
    private static final int INIT_DEFAULT_BRIGHTNESS = -1;
    private static final int INWARD_MODE = 1;
    private static final int OUTWARD_MODE = 2;
    private static final int POINTS_MAX_SIZE = 100;
    private static final int POINTS_MIN_SIZE = 2;
    private static final int POINT_LENGTH = 2;
    private static final float ROUND_VALUE = 0.5f;
    private static final String TAG = "HwNormalizedBrightnessMapping";
    private int mBrightnessAfterMapMax = -1;
    private int mBrightnessAfterMapMaxForManufacture = -1;
    private int mBrightnessAfterMapMin = -1;
    private int mBrightnessAfterMapMinForManufacture = -1;
    private int mBrightnessBeforeMapMax = -1;
    private int mBrightnessBeforeMapMin = -1;
    private int mCurrentDisplayMode = 0;
    private HwBrightnessMappingXmlLoader.Data mData;
    private int mInwardBrightnessAfterMapMax = -1;
    private int mInwardBrightnessAfterMapMaxForManufacture = -1;
    private int mInwardBrightnessAfterMapMin = -1;
    private int mInwardBrightnessAfterMapMinForManufacture = -1;
    private List<PointF> mInwardMappingLinePointsList;
    private boolean mIsConfigLoaded = false;
    private boolean mIsInwardFoldDevice = false;
    private boolean mIsNeedBrightnessMappingEnable = false;
    private List<PointF> mMappingLinePointsList;
    private int mOutwardBrightnessAfterMapMax = -1;
    private int mOutwardBrightnessAfterMapMaxForManufacture = -1;
    private int mOutwardBrightnessAfterMapMin = -1;
    private int mOutwardBrightnessAfterMapMinForManufacture = -1;
    private List<PointF> mOutwardMappingLinePointsList;

    static {
        boolean z = true;
        if (!LogEx.getLogHWInfo() && (!LogEx.getHWModuleLog() || !Log.isLoggable(TAG, 4))) {
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

    public void initInwardFoldScreenMinMaxBrightness(int inwardBrightnessAfterMapMin, int inwardBrightnessAfterMapMax, int outwardBrightnessAfterMapMin, int outwardBrightnessAfterMapMax) {
        this.mInwardBrightnessAfterMapMin = inwardBrightnessAfterMapMin;
        this.mInwardBrightnessAfterMapMax = inwardBrightnessAfterMapMax;
        this.mOutwardBrightnessAfterMapMin = outwardBrightnessAfterMapMin;
        this.mOutwardBrightnessAfterMapMax = outwardBrightnessAfterMapMax;
        SlogEx.i(TAG, "initInwardFoldScreenMinMaxBrightness,mInwardBrightnessAfterMapMin=" + this.mInwardBrightnessAfterMapMin + ",mInwardBrightnessAfterMapMax=" + this.mInwardBrightnessAfterMapMax + ",mOutwardBrightnessAfterMapMin=" + this.mOutwardBrightnessAfterMapMin + ",mOutwardBrightnessAfterMapMax=" + this.mOutwardBrightnessAfterMapMax);
    }

    public boolean isNeedBrightnessMappingEnable() {
        this.mData = HwBrightnessMappingXmlLoader.getData();
        if (!this.mIsConfigLoaded) {
            initParametersFromXml();
        }
        return this.mIsNeedBrightnessMappingEnable;
    }

    private void initParametersFromXml() {
        HwBrightnessMappingXmlLoader.Data data = this.mData;
        if (data == null) {
            loadDefaultConfig();
            SlogEx.i(TAG, "initBrightnessMapping,no need BrightnessMapping");
            this.mIsNeedBrightnessMappingEnable = false;
            return;
        }
        this.mIsInwardFoldDevice = data.isInwardFoldDevice;
        if (!this.mIsInwardFoldDevice) {
            this.mBrightnessAfterMapMinForManufacture = this.mData.brightnessAfterMapMinForManufacture;
            this.mBrightnessAfterMapMaxForManufacture = this.mData.brightnessAfterMapMaxForManufacture;
            this.mMappingLinePointsList = this.mData.mappingLinePointsList;
        } else {
            this.mInwardBrightnessAfterMapMinForManufacture = this.mData.inwardBrightnessAfterMapMinForManufacture;
            this.mInwardBrightnessAfterMapMaxForManufacture = this.mData.inwardBrightnessAfterMapMaxForManufacture;
            this.mOutwardBrightnessAfterMapMinForManufacture = this.mData.outwardBrightnessAfterMapMinForManufacture;
            this.mOutwardBrightnessAfterMapMaxForManufacture = this.mData.outwardBrightnessAfterMapMaxForManufacture;
            this.mInwardMappingLinePointsList = this.mData.inwardMappingLinePointsList;
            this.mOutwardMappingLinePointsList = this.mData.outwardMappingLinePointsList;
        }
        if (isConfigLoadedFromXmlValid()) {
            printConfigFromXml();
            this.mIsNeedBrightnessMappingEnable = true;
            SlogEx.i(TAG, "initBrightnessMapping,BrightnessMapping");
        } else {
            loadDefaultConfig();
            this.mIsNeedBrightnessMappingEnable = false;
            SlogEx.i(TAG, "initBrightnessMapping,no need BrightnessMapping");
        }
        this.mIsConfigLoaded = true;
    }

    private boolean isDefaultPointValid(float pointX, float pointY) {
        if (pointX < ((float) this.mBrightnessBeforeMapMin)) {
            SlogEx.w(TAG, "pointX < mBrightnessBeforeMapMin,pointX=" + pointX + ",mBrightnessBeforeMapMin=" + this.mBrightnessBeforeMapMin);
            return false;
        } else if (pointX > ((float) this.mBrightnessBeforeMapMax)) {
            SlogEx.w(TAG, "pointX > mBrightnessBeforeMapMax,pointX=" + pointX + ",mBrightnessBeforeMapMax=" + this.mBrightnessBeforeMapMax);
            return false;
        } else if (pointY < ((float) this.mBrightnessAfterMapMin)) {
            SlogEx.w(TAG, "pointY < mBrightnessAfterMapMin,pointY=" + pointY + ",mBrightnessAfterMapMin=" + this.mBrightnessAfterMapMin);
            return false;
        } else if (pointY <= ((float) this.mBrightnessAfterMapMax)) {
            return true;
        } else {
            SlogEx.w(TAG, "pointY > mBrightnessAfterMapMax,pointY=" + pointY + ",mBrightnessAfterMapMax=" + this.mBrightnessAfterMapMax);
            return false;
        }
    }

    private boolean isInwardPointValid(float pointX, float pointY) {
        if (pointX < ((float) this.mBrightnessBeforeMapMin)) {
            SlogEx.w(TAG, "inward pointX < mBrightnessBeforeMapMin,pointX=" + pointX + ",mBrightnessBeforeMapMin=" + this.mBrightnessBeforeMapMin);
            return false;
        } else if (pointX > ((float) this.mBrightnessBeforeMapMax)) {
            SlogEx.w(TAG, "inward pointX > mBrightnessBeforeMapMax,pointX=" + pointX + ",mBrightnessBeforeMapMax=" + this.mBrightnessBeforeMapMax);
            return false;
        } else if (pointY < ((float) this.mInwardBrightnessAfterMapMin)) {
            SlogEx.w(TAG, "inward pointY < mInwardBrightnessAfterMapMin,pointY=" + pointY + ",mInwardBrightnessAfterMapMin=" + this.mInwardBrightnessAfterMapMin);
            return false;
        } else if (pointY <= ((float) this.mInwardBrightnessAfterMapMax)) {
            return true;
        } else {
            SlogEx.w(TAG, "inward pointY > mInwardBrightnessAfterMapMax,pointY=" + pointY + ",mInwardBrightnessAfterMapMax=" + this.mInwardBrightnessAfterMapMax);
            return false;
        }
    }

    private boolean isOutwardPointValid(float pointX, float pointY) {
        if (pointX < ((float) this.mBrightnessBeforeMapMin)) {
            SlogEx.w(TAG, "outward pointX < mBrightnessBeforeMapMin,pointX=" + pointX + ",mBrightnessBeforeMapMin=" + this.mBrightnessBeforeMapMin);
            return false;
        } else if (pointX > ((float) this.mBrightnessBeforeMapMax)) {
            SlogEx.w(TAG, "outward pointX > mBrightnessBeforeMapMax,pointX=" + pointX + ",mBrightnessBeforeMapMax=" + this.mBrightnessBeforeMapMax);
            return false;
        } else if (pointY < ((float) this.mOutwardBrightnessAfterMapMin)) {
            SlogEx.w(TAG, "outward pointY < mOutwardBrightnessAfterMapMin,pointY=" + pointY + ",mOutwardBrightnessAfterMapMin=" + this.mOutwardBrightnessAfterMapMin);
            return false;
        } else if (pointY <= ((float) this.mOutwardBrightnessAfterMapMax)) {
            return true;
        } else {
            SlogEx.w(TAG, "outward pointY > mOutwardBrightnessAfterMapMax,pointY=" + pointY + ",mOutwardBrightnessAfterMapMax=" + this.mOutwardBrightnessAfterMapMax);
            return false;
        }
    }

    private void loadDefaultConfig() {
        if (!this.mIsInwardFoldDevice) {
            this.mBrightnessAfterMapMin = -1;
            this.mBrightnessAfterMapMax = -1;
            this.mBrightnessAfterMapMinForManufacture = -1;
            this.mBrightnessAfterMapMaxForManufacture = -1;
            List<PointF> list = this.mMappingLinePointsList;
            if (list != null) {
                list.clear();
                return;
            }
            return;
        }
        this.mInwardBrightnessAfterMapMin = -1;
        this.mInwardBrightnessAfterMapMax = -1;
        this.mInwardBrightnessAfterMapMinForManufacture = -1;
        this.mInwardBrightnessAfterMapMaxForManufacture = -1;
        this.mOutwardBrightnessAfterMapMin = -1;
        this.mOutwardBrightnessAfterMapMax = -1;
        this.mOutwardBrightnessAfterMapMinForManufacture = -1;
        this.mOutwardBrightnessAfterMapMaxForManufacture = -1;
        List<PointF> list2 = this.mInwardMappingLinePointsList;
        if (list2 != null) {
            list2.clear();
        }
        List<PointF> list3 = this.mOutwardMappingLinePointsList;
        if (list3 != null) {
            list3.clear();
        }
        SlogEx.i(TAG, "loadDefaultConfig,mIsInwardFoldDevice=" + this.mIsInwardFoldDevice);
    }

    private boolean isConfigLoadedFromXmlValid() {
        int i;
        if (this.mIsInwardFoldDevice) {
            return isInwardFoldDeviceConfigLoadedFromXmlValid();
        }
        int i2 = this.mBrightnessAfterMapMinForManufacture;
        int i3 = this.mBrightnessAfterMapMin;
        if (i2 < i3 || i2 > (i = this.mBrightnessAfterMapMax)) {
            SlogEx.w(TAG, "MinForManufacture is wrong,=" + this.mBrightnessAfterMapMinForManufacture);
            return false;
        }
        int i4 = this.mBrightnessAfterMapMaxForManufacture;
        if (i4 < i3 || i4 > i) {
            SlogEx.w(TAG, "MaxForManufacture is wrong,=" + this.mBrightnessAfterMapMaxForManufacture);
            return false;
        } else if (i2 >= i4) {
            SlogEx.w(TAG, "MinMaxForManufacture is wrong,min=" + this.mBrightnessAfterMapMinForManufacture + ",max=" + this.mBrightnessAfterMapMaxForManufacture);
            return false;
        } else if (isPointsListValid(this.mMappingLinePointsList, 0)) {
            return true;
        } else {
            SlogEx.w(TAG, "checkPointsList mMappingLinePointsList is wrong!");
            return false;
        }
    }

    private boolean isInwardFoldDeviceConfigLoadedFromXmlValid() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5 = this.mInwardBrightnessAfterMapMinForManufacture;
        if (i5 < 0 || i5 < (i = this.mInwardBrightnessAfterMapMin) || i5 > (i2 = this.mInwardBrightnessAfterMapMax)) {
            SlogEx.w(TAG, "mInwardBrightnessAfterMapMinForManufacture is wrong, value=" + this.mInwardBrightnessAfterMapMinForManufacture);
            return false;
        }
        int i6 = this.mInwardBrightnessAfterMapMaxForManufacture;
        if (i6 < 0 || i6 < i || i6 > i2) {
            SlogEx.w(TAG, "mInwardBrightnessAfterMapMaxForManufacture is wrong, value=" + this.mInwardBrightnessAfterMapMaxForManufacture);
            return false;
        } else if (i5 >= i6) {
            SlogEx.w(TAG, "mInwardBrightnessAfterMapMinForManufacture is wrong,min=" + this.mInwardBrightnessAfterMapMinForManufacture + ",max=" + this.mInwardBrightnessAfterMapMaxForManufacture);
            return false;
        } else {
            int i7 = this.mOutwardBrightnessAfterMapMinForManufacture;
            if (i7 < 0 || i7 < (i3 = this.mOutwardBrightnessAfterMapMin) || i7 > (i4 = this.mOutwardBrightnessAfterMapMax)) {
                SlogEx.w(TAG, "mOutwardBrightnessAfterMapMinForManufacture is wrong, value=" + this.mOutwardBrightnessAfterMapMinForManufacture);
                return false;
            }
            int i8 = this.mOutwardBrightnessAfterMapMaxForManufacture;
            if (i8 < 0 || i8 < i3 || i8 > i4) {
                SlogEx.w(TAG, "mOutwardBrightnessAfterMapMaxForManufacture is wrong, value=" + this.mOutwardBrightnessAfterMapMaxForManufacture);
                return false;
            } else if (i7 >= i8) {
                SlogEx.w(TAG, "mOutwardBrightnessAfterMapMinForManufacture is wrong,min=" + this.mOutwardBrightnessAfterMapMinForManufacture + ",max=" + this.mOutwardBrightnessAfterMapMaxForManufacture);
                return false;
            } else if (!isPointsListValid(this.mInwardMappingLinePointsList, 1)) {
                SlogEx.w(TAG, "checkPointsList mInwardMappingLinePointsList is wrong!");
                return false;
            } else if (isPointsListValid(this.mOutwardMappingLinePointsList, 2)) {
                return true;
            } else {
                SlogEx.w(TAG, "checkPointsList mOutwardMappingLinePointsList is wrong!");
                return false;
            }
        }
    }

    private boolean isPointsListValid(List<PointF> linePointsList, int mode) {
        if (linePointsList == null) {
            SlogEx.w(TAG, "LoadXML false for linePointsListIn == null");
            return false;
        }
        int listSize = linePointsList.size();
        if (listSize < 2 || listSize >= 100) {
            SlogEx.w(TAG, "LoadXML false for linePointsListIn number is wrong,size=" + listSize);
            return false;
        }
        PointF lastPoint = null;
        for (PointF pointItem : linePointsList) {
            if (mode == 1) {
                if (!isInwardPointValid(pointItem.x, pointItem.y)) {
                    return false;
                }
            } else if (mode == 2) {
                if (!isOutwardPointValid(pointItem.x, pointItem.y)) {
                    return false;
                }
            } else if (!isDefaultPointValid(pointItem.x, pointItem.y)) {
                return false;
            }
            if (lastPoint == null) {
                lastPoint = pointItem;
            } else if (lastPoint.x >= pointItem.x) {
                SlogEx.e(TAG, "LoadXML false,lastPoint.x=" + lastPoint.x + ",curPoint.x=" + pointItem.x);
                return false;
            } else if (lastPoint.y > pointItem.y) {
                SlogEx.e(TAG, "LoadXML false,lastPoint.y=" + lastPoint.y + ",curPoint.y=" + pointItem.y);
                return false;
            } else {
                lastPoint = pointItem;
            }
        }
        return true;
    }

    private void printConfigFromXml() {
        if (!HWFLOW) {
            return;
        }
        if (!this.mIsInwardFoldDevice) {
            SlogEx.i(TAG, "minForManufacture=" + this.mBrightnessAfterMapMinForManufacture + ",maxForManufacture=" + this.mBrightnessAfterMapMaxForManufacture);
            StringBuilder sb = new StringBuilder();
            sb.append("mMappingLinePointsList=");
            sb.append(this.mMappingLinePointsList);
            SlogEx.i(TAG, sb.toString());
            return;
        }
        SlogEx.i(TAG, "mIsInwardFoldDevice=" + this.mIsInwardFoldDevice);
        SlogEx.i(TAG, "InwardMinForManufacture=" + this.mInwardBrightnessAfterMapMinForManufacture + ",InwardMaxForManufacture=" + this.mInwardBrightnessAfterMapMaxForManufacture);
        SlogEx.i(TAG, "OutwardMinForManufacture=" + this.mOutwardBrightnessAfterMapMinForManufacture + ",OutwardMaxForManufacture=" + this.mOutwardBrightnessAfterMapMaxForManufacture);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("mInwardMappingLinePointsList=");
        sb2.append(this.mInwardMappingLinePointsList);
        SlogEx.i(TAG, sb2.toString());
        SlogEx.i(TAG, "mOutwardMappingLinePointsList=" + this.mOutwardMappingLinePointsList);
    }

    public int getMappingBrightness(int level, int displayMode) {
        if (displayMode == 1) {
            return getMappingBrightnessInternal(level, displayMode, this.mInwardMappingLinePointsList);
        }
        if (displayMode == 2) {
            return getMappingBrightnessInternal(level, displayMode, this.mOutwardMappingLinePointsList);
        }
        return getMappingBrightnessInternal(level, displayMode, this.mMappingLinePointsList);
    }

    private int getMappingBrightnessInternal(int level, int displayMode, List<PointF> mappingList) {
        int i;
        int i2;
        if (mappingList == null || (i = this.mBrightnessAfterMapMin) == -1 || (i2 = this.mBrightnessAfterMapMax) == -1 || i == i2) {
            return -1;
        }
        float mappingBrightness = -1.0f;
        PointF prePoint = null;
        Iterator<PointF> it = mappingList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PointF pointItem = it.next();
            if (prePoint == null) {
                prePoint = pointItem;
            }
            if (((float) level) >= pointItem.x) {
                prePoint = pointItem;
                mappingBrightness = prePoint.y;
            } else if (pointItem.x <= prePoint.x) {
                SlogEx.w(TAG, "mapping_prePoint.x <= nextPoint.x,x=" + pointItem.x + ",y=" + pointItem.y);
                return -1;
            } else {
                mappingBrightness = (((pointItem.y - prePoint.y) * (((float) level) - prePoint.x)) / (pointItem.x - prePoint.x)) + prePoint.y;
            }
        }
        if (HWDEBUG) {
            SlogEx.d(TAG, "levelBeforeMap=" + level + ",mappingBrightness=" + mappingBrightness);
        }
        return getValidBrightness((int) (ROUND_VALUE + mappingBrightness), displayMode);
    }

    private int getValidBrightness(int brightness, int displayMode) {
        int brightnessOut = brightness;
        if (displayMode == 1) {
            if (brightness < this.mInwardBrightnessAfterMapMin) {
                brightnessOut = this.mInwardBrightnessAfterMapMin;
            }
            if (brightness > this.mInwardBrightnessAfterMapMax) {
                return this.mInwardBrightnessAfterMapMax;
            }
            return brightnessOut;
        } else if (displayMode == 2) {
            if (brightness < this.mOutwardBrightnessAfterMapMin) {
                brightnessOut = this.mOutwardBrightnessAfterMapMin;
            }
            if (brightness > this.mOutwardBrightnessAfterMapMax) {
                return this.mOutwardBrightnessAfterMapMax;
            }
            return brightnessOut;
        } else {
            if (brightness < this.mBrightnessAfterMapMin) {
                brightnessOut = this.mBrightnessAfterMapMin;
            }
            if (brightness > this.mBrightnessAfterMapMax) {
                return this.mBrightnessAfterMapMax;
            }
            return brightnessOut;
        }
    }

    public int getMappingBrightnessForManufacture(int level, int displayMode) {
        if (displayMode == 1) {
            return getMappingBrightnessForManufactureInternal(level, displayMode, this.mInwardBrightnessAfterMapMinForManufacture, this.mInwardBrightnessAfterMapMaxForManufacture);
        }
        if (displayMode == 2) {
            return getMappingBrightnessForManufactureInternal(level, displayMode, this.mOutwardBrightnessAfterMapMinForManufacture, this.mOutwardBrightnessAfterMapMaxForManufacture);
        }
        return getMappingBrightnessForManufactureInternal(level, displayMode, this.mBrightnessAfterMapMinForManufacture, this.mBrightnessAfterMapMaxForManufacture);
    }

    private int getMappingBrightnessForManufactureInternal(int level, int displayMode, int brightnessAfterMapMinForManufacture, int brightnessAfterMapMaxForManufacture) {
        int i;
        int i2;
        float brightnessIn = (float) level;
        if (brightnessAfterMapMinForManufacture == -1 || brightnessAfterMapMaxForManufacture == -1 || brightnessAfterMapMinForManufacture == brightnessAfterMapMaxForManufacture || (i = this.mBrightnessBeforeMapMax) == (i2 = this.mBrightnessBeforeMapMin)) {
            return -1;
        }
        float mappingBrightness = ((float) brightnessAfterMapMinForManufacture) + (((brightnessIn - ((float) i2)) * ((float) (brightnessAfterMapMaxForManufacture - brightnessAfterMapMinForManufacture))) / ((float) (i - i2)));
        if (HWDEBUG) {
            SlogEx.i(TAG, "levelBeforeMap=" + level + ",mappingBrightnessForManufacture=" + mappingBrightness + ",brightnessAfterMapMinForManufacture=" + brightnessAfterMapMinForManufacture + ",brightnessAfterMapMaxForManufacture=" + brightnessAfterMapMaxForManufacture + ",displayMode=" + displayMode);
        }
        return getValidBrightnessForManufacture((int) (ROUND_VALUE + mappingBrightness), displayMode);
    }

    private int getValidBrightnessForManufacture(int brightness, int displayMode) {
        if (displayMode == 1) {
            int i = this.mInwardBrightnessAfterMapMinForManufacture;
            if (brightness < i) {
                return i;
            }
            int i2 = this.mInwardBrightnessAfterMapMaxForManufacture;
            if (brightness > i2) {
                return i2;
            }
        } else if (displayMode == 2) {
            int i3 = this.mOutwardBrightnessAfterMapMinForManufacture;
            if (brightness < i3) {
                return i3;
            }
            int i4 = this.mOutwardBrightnessAfterMapMaxForManufacture;
            if (brightness > i4) {
                return i4;
            }
        } else {
            int i5 = this.mBrightnessAfterMapMinForManufacture;
            if (brightness < i5) {
                return i5;
            }
            int i6 = this.mBrightnessAfterMapMaxForManufacture;
            if (brightness > i6) {
                return i6;
            }
        }
        return brightness;
    }

    public int getMappingBrightnessHighPrecision(int level, int displayMode) {
        if (displayMode == 1) {
            return getMappingBrightnessHighPrecisionInternal(level, displayMode, this.mInwardBrightnessAfterMapMin, this.mInwardBrightnessAfterMapMax, this.mInwardMappingLinePointsList);
        }
        if (displayMode == 2) {
            return getMappingBrightnessHighPrecisionInternal(level, displayMode, this.mOutwardBrightnessAfterMapMin, this.mOutwardBrightnessAfterMapMax, this.mOutwardMappingLinePointsList);
        }
        return getMappingBrightnessHighPrecisionInternal(level, displayMode, this.mBrightnessAfterMapMin, this.mBrightnessAfterMapMax, this.mMappingLinePointsList);
    }

    private int getMappingBrightnessHighPrecisionInternal(int level, int displayMode, int brightnessAfterMapMin, int brightnessAfterMapMax, List<PointF> mappingList) {
        if (mappingList == null || brightnessAfterMapMin == -1 || brightnessAfterMapMax == -1 || this.mBrightnessBeforeMapMin == -1 || this.mBrightnessBeforeMapMax == -1 || brightnessAfterMapMin == brightnessAfterMapMax) {
            return level;
        }
        float mappingBrightness = (float) level;
        float tempBrightness = (float) level;
        int listSize = mappingList.size();
        int i = 1;
        while (true) {
            if (i >= listSize) {
                break;
            }
            PointF prePoint = mappingList.get(i - 1);
            PointF nextPoint = mappingList.get(i);
            if (((float) level) < prePoint.x || ((float) level) > nextPoint.x) {
                i++;
            } else if (prePoint.x >= nextPoint.x) {
                SlogEx.w(TAG, "origlevel=" + level + ",prePoint.x=" + prePoint.x + ">=nextPoint.x=" + nextPoint.x);
                return level;
            } else {
                tempBrightness = (((((float) level) - prePoint.x) * (nextPoint.y - prePoint.y)) / (nextPoint.x - prePoint.x)) + prePoint.y;
                int i2 = this.mBrightnessBeforeMapMax;
                int i3 = this.mBrightnessBeforeMapMin;
                mappingBrightness = (((tempBrightness - ((float) brightnessAfterMapMin)) * ((float) (i2 - i3))) / ((float) (brightnessAfterMapMax - brightnessAfterMapMin))) + ((float) i3);
            }
        }
        if (HWDEBUG) {
            SlogEx.d(TAG, "level=" + level + ",mappingBrightnessHigh=" + mappingBrightness + ",tempBrightness=" + tempBrightness);
        }
        return getValidBrightnessHighPrecision((int) (ROUND_VALUE + mappingBrightness));
    }

    private int getValidBrightnessHighPrecision(int brightness) {
        int i = this.mBrightnessBeforeMapMin;
        if (brightness < i) {
            return i;
        }
        int i2 = this.mBrightnessBeforeMapMax;
        if (brightness > i2) {
            return i2;
        }
        return brightness;
    }
}
