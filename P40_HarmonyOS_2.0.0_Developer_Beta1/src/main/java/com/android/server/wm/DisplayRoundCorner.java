package com.android.server.wm;

import android.common.HwFrameworkFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Slog;
import android.view.SurfaceControl;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DisplayRoundCorner {
    private static final String CONFIG_HW_ROUND_CORNER = SystemProperties.get("hw.config.roundcorner", "");
    private static final int CORNER_NUMS = 4;
    private static final int DIRECTIONS = 4;
    public static final int DISPLAY_AREA_TYPE_CUTOUT = 3;
    public static final int DISPLAY_AREA_TYPE_ROUND_CORNER = 1;
    public static final int DISPLAY_AREA_TYPE_SIDE = 2;
    private static final int LEFT_BOTTOM = 2;
    private static final int LEFT_TOP = 0;
    private static final int RIGHT_BOTTOM = 3;
    private static final int RIGHT_TOP = 1;
    private static final String TAG = "DisplayRoundCorner";
    private static DisplayRoundCorner sInstance;
    private String[] mFourCorners = null;
    private int mHeight = 1;
    private int mPhysicalHeight = 0;
    private HashMap<Integer, Point[]> mRoundCornerRotationMap;
    private HashMap<Integer, Rect> mRoundCornerSafeInsets;
    private HashMap<Integer, List<Rect>> mRoundCornerUnSafeBoundsMap;
    private WindowManagerService mService;
    private HashMap<Integer, Rect> mSideDisplaySafeInsets;
    private HashMap<Integer, List<Rect>> mSideDisplayUnSafeBoundsMap;
    private int mWidth = 1;

    private DisplayRoundCorner(WindowManagerService service) {
        this.mService = service;
        this.mRoundCornerRotationMap = new HashMap<>(4);
        this.mRoundCornerSafeInsets = new HashMap<>(4);
        this.mSideDisplaySafeInsets = new HashMap<>(4);
        this.mRoundCornerUnSafeBoundsMap = new HashMap<>(4);
        this.mSideDisplayUnSafeBoundsMap = new HashMap<>(4);
        initCornerInfo();
    }

    private void initCornerInfo() {
        if (!CONFIG_HW_ROUND_CORNER.isEmpty()) {
            this.mFourCorners = CONFIG_HW_ROUND_CORNER.split(",");
            String[] strArr = this.mFourCorners;
            if (strArr == null) {
                Slog.e(TAG, "Bad Format: " + CONFIG_HW_ROUND_CORNER);
            } else if (strArr.length != 4) {
                Slog.e(TAG, "Bad Format: " + CONFIG_HW_ROUND_CORNER);
                this.mFourCorners = null;
            }
        }
    }

    public static synchronized DisplayRoundCorner getInstance(WindowManagerServiceEx service) {
        DisplayRoundCorner displayRoundCorner;
        synchronized (DisplayRoundCorner.class) {
            if (sInstance == null) {
                sInstance = new DisplayRoundCorner(service == null ? null : service.getWindowManagerService());
            }
            displayRoundCorner = sInstance;
        }
        return displayRoundCorner;
    }

    public static boolean isRoundCornerDisplay() {
        return !CONFIG_HW_ROUND_CORNER.isEmpty();
    }

    public void setScreenSize(int width, int height) {
        if (!(width == this.mWidth && height == this.mHeight)) {
            this.mWidth = Math.min(width, height);
            this.mHeight = Math.max(width, height);
            this.mRoundCornerRotationMap.clear();
            this.mRoundCornerSafeInsets.clear();
            this.mSideDisplaySafeInsets.clear();
            this.mRoundCornerUnSafeBoundsMap.clear();
            this.mSideDisplayUnSafeBoundsMap.clear();
            for (int rotation = 0; rotation < 4; rotation++) {
                calculateSafeInsetsRect(rotation);
            }
        }
    }

    private boolean isValidRotation(int rotation) {
        if (rotation >= 0 && rotation <= 3) {
            return true;
        }
        Slog.v(TAG, "calculateSafeInsetsRect rotation is invalid " + rotation);
        return false;
    }

    private void fillRoundCornerMaps(int rotation, Point[] points) {
        boolean isRotated = false;
        if (points != null) {
            if (rotation == 1 || rotation == 3) {
                isRotated = true;
            }
            List<Rect> unSafeRects = new ArrayList<>(4);
            unSafeRects.add(new Rect(0, 0, points[0].x, points[0].y));
            unSafeRects.add(new Rect(points[1].x, 0, isRotated ? this.mHeight : this.mWidth, points[1].y));
            unSafeRects.add(new Rect(points[2].x, points[2].y, isRotated ? this.mHeight : this.mWidth, isRotated ? this.mWidth : this.mHeight));
            unSafeRects.add(new Rect(0, points[3].y, points[3].x, isRotated ? this.mWidth : this.mHeight));
            this.mRoundCornerUnSafeBoundsMap.put(Integer.valueOf(rotation), unSafeRects);
            this.mRoundCornerRotationMap.put(Integer.valueOf(rotation), points);
        }
    }

    private void calRoundCornerPoints(int rotation) {
        double leftBottomCorner;
        if (this.mFourCorners != null) {
            double constant = (1.0d - (1.0d / Math.sqrt(2.0d))) * ((((double) this.mHeight) * 1.0d) / ((double) getPhysicalHeight()));
            try {
                double leftTopCorner = Math.ceil(Double.valueOf(this.mFourCorners[0]).doubleValue() * constant);
                double rightTopCorner = Math.ceil(Double.valueOf(this.mFourCorners[1]).doubleValue() * constant);
                double rightBottomCorner = Math.ceil(Double.valueOf(this.mFourCorners[3]).doubleValue() * constant);
                double leftBottomCorner2 = Math.ceil(Double.valueOf(this.mFourCorners[2]).doubleValue() * constant);
                Point[] points = new Point[4];
                if (rotation != 0) {
                    if (rotation != 1) {
                        if (rotation == 2) {
                            leftBottomCorner = leftBottomCorner2;
                        } else if (rotation != 3) {
                            return;
                        }
                    }
                    double tmpValue = rotation == 1 ? rightTopCorner : leftBottomCorner2;
                    points[0] = new Point((int) tmpValue, (int) tmpValue);
                    double tmpValue2 = rotation == 1 ? rightBottomCorner : leftTopCorner;
                    points[1] = new Point((int) (((double) this.mHeight) - tmpValue2), (int) tmpValue2);
                    double tmpValue3 = rotation == 1 ? leftBottomCorner2 : rightTopCorner;
                    points[2] = new Point((int) (((double) this.mHeight) - tmpValue3), (int) (((double) this.mWidth) - tmpValue3));
                    double tmpValue4 = rotation == 1 ? leftTopCorner : rightBottomCorner;
                    points[3] = new Point((int) tmpValue4, (int) (((double) this.mWidth) - tmpValue4));
                    fillRoundCornerMaps(rotation, points);
                }
                leftBottomCorner = leftBottomCorner2;
                double tmpValue5 = rotation == 0 ? leftTopCorner : rightBottomCorner;
                points[0] = new Point((int) tmpValue5, (int) tmpValue5);
                double tmpValue6 = rotation == 0 ? rightTopCorner : leftBottomCorner;
                points[1] = new Point((int) (((double) this.mWidth) - tmpValue6), (int) tmpValue6);
                double tmpValue7 = rotation == 0 ? rightBottomCorner : leftTopCorner;
                points[2] = new Point((int) (((double) this.mWidth) - tmpValue7), (int) (((double) this.mHeight) - tmpValue7));
                double tmpValue8 = rotation == 0 ? leftBottomCorner : rightTopCorner;
                points[3] = new Point((int) tmpValue8, (int) (((double) this.mHeight) - tmpValue8));
                fillRoundCornerMaps(rotation, points);
            } catch (NumberFormatException e) {
                Slog.e(TAG, "NumberFormatException " + Arrays.toString(this.mFourCorners) + " CONFIG_HW_ROUND_CORNER " + CONFIG_HW_ROUND_CORNER);
            }
        }
    }

    private void calRoundCornerSafeInsets(int rotation) {
        Point[] points = this.mRoundCornerRotationMap.get(Integer.valueOf(rotation));
        if (points != null) {
            Point leftTop = new Point();
            Point rightBottom = new Point();
            leftTop.x = Math.max(points[0].x, points[3].x);
            leftTop.y = Math.max(points[0].y, points[1].y);
            rightBottom.x = Math.min(points[1].x, points[2].x);
            rightBottom.y = Math.min(points[2].y, points[3].y);
            boolean isRotated = false;
            if (rotation == 1 || rotation == 3) {
                isRotated = true;
            }
            this.mRoundCornerSafeInsets.put(Integer.valueOf(rotation), new Rect(leftTop.x, leftTop.y, (isRotated ? this.mHeight : this.mWidth) - rightBottom.x, (isRotated ? this.mWidth : this.mHeight) - rightBottom.y));
        }
    }

    private int getPhysicalHeight() {
        if (this.mPhysicalHeight == 0) {
            IBinder displayToken = SurfaceControl.getInternalDisplayToken();
            if (displayToken == null) {
                return this.mHeight;
            }
            SurfaceControl.PhysicalDisplayInfo[] configs = SurfaceControl.getDisplayConfigs(displayToken);
            if (configs == null || configs.length == 0) {
                return this.mHeight;
            }
            this.mPhysicalHeight = configs[0].height;
        }
        return this.mPhysicalHeight;
    }

    private int getSideWidth() {
        if (!HwDisplaySizeUtil.hasSideInScreen()) {
            return 0;
        }
        int sideWidth = HwDisplaySizeUtil.getInstance(this.mService).getSafeSideWidth();
        if (sideWidth != 0) {
            return sideWidth;
        }
        Rect sideSafeRect = HwFrameworkFactory.getHwExtDisplaySizeUtil().getDisplaySideSafeInsets();
        return (int) (((float) ((sideSafeRect.left + sideSafeRect.right) / 2)) * ((((float) this.mHeight) * 1.0f) / ((float) getPhysicalHeight())));
    }

    private void calSideDisplayUnSafeRect(int rotation) {
        int sideWidth = getSideWidth();
        Point[] points = new Point[4];
        if (rotation != 0) {
            if (rotation != 1) {
                if (rotation != 2) {
                    if (rotation != 3) {
                        return;
                    }
                }
            }
            points[0] = new Point(0, 0);
            points[1] = new Point(this.mHeight, sideWidth);
            points[2] = new Point(0, this.mWidth - sideWidth);
            points[3] = new Point(this.mHeight, this.mWidth);
            List<Rect> unSafeRects = new ArrayList<>(2);
            unSafeRects.add(new Rect(points[0].x, points[0].y, points[1].x, points[1].y));
            unSafeRects.add(new Rect(points[2].x, points[2].y, points[3].x, points[3].y));
            this.mSideDisplayUnSafeBoundsMap.put(Integer.valueOf(rotation), unSafeRects);
        }
        points[0] = new Point(0, 0);
        points[1] = new Point(sideWidth, this.mHeight);
        points[2] = new Point(this.mWidth - sideWidth, 0);
        points[3] = new Point(this.mWidth, this.mHeight);
        List<Rect> unSafeRects2 = new ArrayList<>(2);
        unSafeRects2.add(new Rect(points[0].x, points[0].y, points[1].x, points[1].y));
        unSafeRects2.add(new Rect(points[2].x, points[2].y, points[3].x, points[3].y));
        this.mSideDisplayUnSafeBoundsMap.put(Integer.valueOf(rotation), unSafeRects2);
    }

    private void calSideDisplaySafeInsets(int rotation) {
        int sideWidth = getSideWidth();
        if (rotation != 0) {
            if (rotation != 1) {
                if (rotation != 2) {
                    if (rotation != 3) {
                        return;
                    }
                }
            }
            this.mSideDisplaySafeInsets.put(Integer.valueOf(rotation), new Rect(0, sideWidth, 0, sideWidth));
            return;
        }
        this.mSideDisplaySafeInsets.put(Integer.valueOf(rotation), new Rect(sideWidth, 0, sideWidth, 0));
    }

    private void calculateSafeInsetsRect(int rotation) {
        if (isValidRotation(rotation)) {
            if (isRoundCornerDisplay()) {
                calRoundCornerPoints(rotation);
                calRoundCornerSafeInsets(rotation);
            }
            if (HwDisplaySizeUtil.hasSideInScreen()) {
                calSideDisplayUnSafeRect(rotation);
                calSideDisplaySafeInsets(rotation);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Rect getRoundCornerSafeInsets(int rotation) {
        if (!isValidRotation(rotation)) {
            return null;
        }
        if (isRoundCornerDisplay()) {
            return this.mRoundCornerSafeInsets.get(Integer.valueOf(rotation));
        }
        Slog.v(TAG, "is not RoundCorner");
        return null;
    }

    /* access modifiers changed from: package-private */
    public List<Rect> getRoundCornerUnsafeBounds(int rotation) {
        if (isValidRotation(rotation) && isRoundCornerDisplay()) {
            return this.mRoundCornerUnSafeBoundsMap.get(Integer.valueOf(rotation));
        }
        return null;
    }

    public Rect getSideDisplaySafeInsets(int rotation) {
        if (isValidRotation(rotation) && HwDisplaySizeUtil.hasSideInScreen()) {
            return this.mSideDisplaySafeInsets.get(Integer.valueOf(rotation));
        }
        return null;
    }

    public List<Rect> getSideDisplayUnsafeBounds(int rotation) {
        if (isValidRotation(rotation) && HwDisplaySizeUtil.hasSideInScreen()) {
            return this.mSideDisplayUnSafeBoundsMap.get(Integer.valueOf(rotation));
        }
        return null;
    }

    public String toString() {
        return "screenWidth " + this.mWidth + " screenHeight " + this.mHeight + " mRoundCornerSafeInsets[0]= " + this.mRoundCornerSafeInsets.get(0) + " mRoundCornerSafeInsets[1]= " + this.mRoundCornerSafeInsets.get(1) + " mRoundCornerSafeInsets[2]= " + this.mRoundCornerSafeInsets.get(2) + " mRoundCornerSafeInsets[3]= " + this.mRoundCornerSafeInsets.get(3);
    }
}
