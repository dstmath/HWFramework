package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import java.util.Iterator;
import java.util.List;

public class HwBrightnessMapping {
    private static final int DEFAULT_VALUE = 0;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final int MAX_DEFAULT_BRIGHTNESS = 255;
    private static final int MIN_DEFAULT_BRIGHTNESS = 4;
    private static final float ROUND_UP_VALUE = 0.5f;
    private static final float SMALL_VALUE = 1.0E-6f;
    private static final String TAG = "HwBrightnessMapping";
    private final HwBrightnessXmlLoader.Data mData = HwBrightnessXmlLoader.getData();
    private List<PointF> mMappingLinePointsList;

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    public HwBrightnessMapping(List<PointF> linePointsList) {
        this.mMappingLinePointsList = linePointsList;
    }

    /* access modifiers changed from: package-private */
    public int getMappingBrightnessForRealNit(int level) {
        List<PointF> list = this.mMappingLinePointsList;
        if (list == null || list.size() == 0 || level < 4) {
            Slog.w(TAG, "mMappingLinePointsList == null || level<min,level=" + level);
            return level;
        }
        float mappingBrightness = (float) level;
        PointF prePoint = null;
        Iterator<PointF> iter = this.mMappingLinePointsList.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            PointF curPoint = iter.next();
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (((float) level) >= curPoint.x) {
                prePoint = curPoint;
                mappingBrightness = prePoint.y;
            } else if (curPoint.x <= prePoint.x) {
                mappingBrightness = (float) level;
                Slog.w(TAG, "mappingBrightness_prePoint.x <= nexPoint.x,x=" + curPoint.x + ", y = " + curPoint.y);
            } else {
                mappingBrightness = (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (((float) level) - prePoint.x)) + prePoint.y;
            }
        }
        return (int) (0.5f + mappingBrightness);
    }

    public int convertBrightnessLevelToNit(int level) {
        float brightnessNitTmp;
        if (level <= 0) {
            return 0;
        }
        if (this.mData.brightnessLevelToNitMappingEnable) {
            brightnessNitTmp = convertBrightnessLevelToNitInternel(this.mData.brightnessLevelToNitLinePoints, (float) level);
        } else {
            brightnessNitTmp = ((((float) (level - 4)) * (this.mData.screenBrightnessMaxNit - this.mData.screenBrightnessMinNit)) / 251.0f) + this.mData.screenBrightnessMinNit;
        }
        if (brightnessNitTmp < this.mData.screenBrightnessMinNit) {
            brightnessNitTmp = this.mData.screenBrightnessMinNit;
        }
        if (brightnessNitTmp > this.mData.screenBrightnessMaxNit) {
            brightnessNitTmp = this.mData.screenBrightnessMaxNit;
        }
        if (HWDEBUG) {
            Slog.d(TAG, "LevelToNit,level=" + level + ",nit=" + brightnessNitTmp);
        }
        return (int) (0.5f + brightnessNitTmp);
    }

    public int convertBrightnessNitToLevel(int brightnessNit) {
        float brightnessLevelTmp;
        if (brightnessNit <= 0) {
            return 0;
        }
        if (this.mData.brightnessLevelToNitMappingEnable) {
            brightnessLevelTmp = convertBrightnessNitToLevelInternel(this.mData.brightnessLevelToNitLinePoints, (float) brightnessNit);
        } else if (Math.abs(this.mData.screenBrightnessMaxNit - this.mData.screenBrightnessMinNit) < SMALL_VALUE) {
            Slog.w(TAG, "screenBrightnessMaxNit==screenBrightnessMinNit, return MIN_DEFAULT_BRIGHTNESS");
            brightnessLevelTmp = 4.0f;
        } else {
            brightnessLevelTmp = (((((float) brightnessNit) - this.mData.screenBrightnessMinNit) * 251.0f) / (this.mData.screenBrightnessMaxNit - this.mData.screenBrightnessMinNit)) + 4.0f;
        }
        if (brightnessLevelTmp < 4.0f) {
            brightnessLevelTmp = 4.0f;
        }
        if (brightnessLevelTmp > 255.0f) {
            brightnessLevelTmp = 255.0f;
        }
        if (HWDEBUG) {
            Slog.d(TAG, "NitToLevel,brightnessLevelTmp=" + brightnessLevelTmp + ",brightnessNit=" + brightnessNit);
        }
        return (int) (0.5f + brightnessLevelTmp);
    }

    private float convertBrightnessLevelToNitInternel(List<PointF> linePoints, float brightness) {
        if (linePoints == null || linePoints.size() == 0 || ((int) brightness) <= 0) {
            return 0.0f;
        }
        float brightnessNitTmp = 0.0f;
        PointF prePoint = null;
        Iterator<PointF> it = linePoints.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PointF pointItem = it.next();
            if (prePoint == null) {
                prePoint = pointItem;
            }
            if (brightness >= pointItem.x) {
                prePoint = pointItem;
                brightnessNitTmp = prePoint.y;
            } else if (pointItem.x <= prePoint.x) {
                brightnessNitTmp = 0.0f;
                Slog.w(TAG, "LevelToNit,brightnessNitTmpdefault=0.0,for_prePoint.x <= nexPoint.x,x=" + pointItem.x + ", y = " + pointItem.y);
            } else {
                brightnessNitTmp = (((pointItem.y - prePoint.y) / (pointItem.x - prePoint.x)) * (brightness - prePoint.x)) + prePoint.y;
            }
        }
        if (HWDEBUG) {
            Slog.d(TAG, "LevelToNit,brightness=" + brightness + ",TobrightnessNitTmp=" + brightnessNitTmp);
        }
        return brightnessNitTmp;
    }

    private float convertBrightnessNitToLevelInternel(List<PointF> linePoints, float brightnessNit) {
        if (linePoints == null || linePoints.size() == 0 || ((int) brightnessNit) <= 0) {
            return 0.0f;
        }
        float brightnessLevel = 0.0f;
        PointF prePoint = null;
        Iterator<PointF> it = linePoints.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            PointF pointItem = it.next();
            if (prePoint == null) {
                prePoint = pointItem;
            }
            if (brightnessNit >= pointItem.y) {
                prePoint = pointItem;
                brightnessLevel = prePoint.x;
            } else if (pointItem.y <= prePoint.y) {
                brightnessLevel = 0.0f;
                Slog.w(TAG, "NitToLevel,brightnessLevel=0.0,for_prePoint.y <= nexPoint.y,x=" + pointItem.x + ", y = " + pointItem.y);
            } else {
                brightnessLevel = (((pointItem.x - prePoint.x) / (pointItem.y - prePoint.y)) * (brightnessNit - prePoint.y)) + prePoint.x;
            }
        }
        if (HWDEBUG) {
            Slog.d(TAG, "NitToLevel,brightnessLevel=" + brightnessLevel + ",brightnessNit=" + brightnessNit);
        }
        return brightnessLevel;
    }

    /* access modifiers changed from: package-private */
    public int getMappingBrightnessForWindowBrightness(List<PointF> linePoints, int level) {
        if (linePoints == null || linePoints.size() == 0 || level < 4) {
            Slog.w(TAG, "linePoints == null || level<min,level=" + level);
            return level;
        }
        float mappingBrightness = (float) level;
        PointF prePoint = null;
        Iterator<PointF> iter = linePoints.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            PointF curPoint = iter.next();
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (((float) level) >= curPoint.x) {
                prePoint = curPoint;
                mappingBrightness = prePoint.y;
            } else if (curPoint.x <= prePoint.x) {
                mappingBrightness = (float) level;
            } else {
                mappingBrightness = (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (((float) level) - prePoint.x)) + prePoint.y;
            }
        }
        return (int) (0.5f + mappingBrightness);
    }
}
