package com.android.server.wm;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.content.res.HwPCMultiWindowCompatibility;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import com.android.server.wm.DefaultHwPCMultiWindowManager;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.android.view.DisplayEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.server.AttributeCacheEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HwPCMultiWindowManager extends DefaultHwPCMultiWindowManager {
    public static final boolean DEFER_RESUME = true;
    private static final int DP_WINDOW_OVERLAP_OFFSET = 30;
    private static final int DP_WINDOW_OVERLAP_OFFSET_MIN = 15;
    static final int FIX_ORIENTATION_LANDSCAPE = 2;
    static final int FIX_ORIENTATION_NONE = 0;
    static final int FIX_ORIENTATION_PORTRAIT = 1;
    private static final int MAX_TIMES_TO_ADJUST_BOUNDS = 30;
    private static final int MSG_REQUEST_ORIENTATION = 1;
    private static final int MSG_REQUEST_ORIENTATION_DELAY = 200;
    private static final List<String> PKG_NO_UPDATE_FOR_PAD_IN_PC = Arrays.asList("com.huawei.systemmanager");
    static final int SCREEN_CUT_INTO_HALF = 2;
    static final int SPECIAL_PACKAGE_TYPE_FULLSCREEN_NO_NAVIGATIONBAR = 7;
    static final int SPECIAL_PACKAGE_TYPE_MAXIMIZED_ONLY = 3;
    static final int SPECIAL_PACKAGE_TYPE_NEED_DELAY = 2;
    static final int SPECIAL_PACKAGE_TYPE_PAD_FULLSCREEN = 6;
    static final int SPECIAL_PACKAGE_TYPE_PORTRAIT = 4;
    static final int SPECIAL_PACKAGE_TYPE_PORTRAIT_MAXIMIZED = 5;
    static final int SPECIAL_PACKAGE_TYPE_VIDEO_NEED_FULLSCREEN = 1;
    public static final String TAG = "HwPCMultiWindowManager";
    private static final Object mLock = new Object();
    private static volatile HwPCMultiWindowManager mSingleInstance = null;
    private int mCurDisplayId = -1;
    private int mDecorHeight = 0;
    final HashMap<String, HashMap<String, Entry>> mEntries;
    final HashMap<String, HashMap<String, Entry>> mEntriesToWrite;
    private boolean mFixOrientationChanged = false;
    final List<String> mFullscreenNoNavigationBar = new ArrayList();
    final Handler mHandler;
    private int mLastRequestedOrientation = 0;
    final List<String> mMaximizedOnlyList = new ArrayList();
    final List<String> mNeedDelayList = new ArrayList();
    private Point mNonDecorScreenSize = new Point();
    final List<String> mPadFullscreenList = new ArrayList();
    public final List<String> mPortraitMaximizedPkgList = new ArrayList();
    final List<String> mPortraitPkgList = new ArrayList();
    final ActivityTaskManagerServiceEx mService;
    private final HwPCMultiWindowSettingsWriter mSettingsWriter;
    final List<String> mSpecialVideosList = new ArrayList();
    private Set<Integer> mUpdateTaskSet = Collections.synchronizedSet(new HashSet());

    private HwPCMultiWindowManager(ActivityTaskManagerServiceEx serviceEx) {
        this.mService = serviceEx;
        this.mEntriesToWrite = new HashMap<>();
        this.mEntries = new HashMap<>();
        HwPCMultiWindowPolicy.initialize(getRightContext(this.mService.getContext()), this);
        this.mSettingsWriter = new HwPCMultiWindowSettingsWriter(this);
        this.mHandler = new WorkerHandler(this.mService.getLooper());
        this.mUpdateTaskSet.clear();
    }

    public static HwPCMultiWindowManager getInstance(ActivityTaskManagerServiceEx serviceEx) {
        if (mSingleInstance == null) {
            synchronized (mLock) {
                if (mSingleInstance == null) {
                    mSingleInstance = new HwPCMultiWindowManager(serviceEx);
                }
            }
        }
        return mSingleInstance;
    }

    public static class Entry extends DefaultHwPCMultiWindowManager.EntryEx {
        public int originalWindowState = this.windowState;
        public final String pkgName;
        public Rect windowBounds = new Rect();
        public int windowState = 1;

        public Entry(String _name) {
            this.pkgName = _name;
        }

        public void copyFrom(Entry entry) {
            this.windowState = entry.windowState;
            this.originalWindowState = entry.originalWindowState;
            this.windowBounds.set(entry.windowBounds);
        }

        public Rect getWindowBounds() {
            return this.windowBounds;
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("windowState=");
            sb.append(Integer.toHexString(this.windowState));
            sb.append("; originalWindowState=");
            sb.append(Integer.toHexString(this.originalWindowState));
            sb.append("; bounds=");
            Rect rect = this.windowBounds;
            if (rect == null) {
                str = "null";
            } else {
                str = rect.toShortString();
            }
            sb.append(str);
            return sb.toString();
        }
    }

    public void putEntry(String deviceKey, String entryKey, Entry entry) {
        if (!TextUtils.isEmpty(deviceKey) && !TextUtils.isEmpty(entryKey)) {
            if (!this.mEntries.containsKey(deviceKey)) {
                this.mEntries.put(deviceKey, new HashMap<>());
            }
            if (!this.mEntriesToWrite.containsKey(deviceKey)) {
                this.mEntriesToWrite.put(deviceKey, new HashMap<>());
            }
            this.mEntriesToWrite.get(deviceKey).put(entry.pkgName, entry);
            if (!entryKey.equals(entry.pkgName)) {
                Entry entrytemp = this.mEntries.get(deviceKey).get(entryKey);
                if (entrytemp == null) {
                    entrytemp = new Entry(entry.pkgName);
                    entrytemp.copyFrom(entry);
                }
                this.mEntries.get(deviceKey).put(entryKey, entrytemp);
            }
        }
    }

    public void putEntryForCurDevice(String entryKey, Entry entry) {
        putEntry(getCurDeviceKey(), entryKey, entry);
    }

    public Entry getEntry(String entryKey) {
        String deviceKey = getCurDeviceKey();
        Entry entry = null;
        if (!this.mEntries.containsKey(deviceKey)) {
            return null;
        }
        Entry entry2 = this.mEntries.get(deviceKey).get(entryKey);
        if (entry2 != null) {
            return entry2;
        }
        if (this.mEntriesToWrite.containsKey(deviceKey)) {
            entry = this.mEntriesToWrite.get(deviceKey).get(entryKey.split("_stackId_")[0]);
        }
        return entry;
    }

    private void removeEntry(String entryKey) {
        String deviceKey = getCurDeviceKey();
        if (this.mEntries.containsKey(deviceKey) && this.mEntries.get(deviceKey).containsKey(entryKey)) {
            this.mEntries.get(deviceKey).remove(entryKey);
        }
    }

    private String getCurDeviceKey() {
        Point deviceSize = new Point();
        Display display = getExtDisplay(false);
        if (display != null) {
            display.getRealSize(deviceSize);
        }
        return deviceSize.x + "_" + deviceSize.y;
    }

    public void storeTaskSettings(TaskRecordEx recordEx) {
        if (recordEx != null && recordEx.getRootActivityInfo() != null && !HwPCUtils.isHiCarCastMode()) {
            String entryKey = getEntryKey(recordEx);
            Entry entry = getEntry(entryKey);
            String name = recordEx.getRootActivityInfo().packageName;
            if (entry == null) {
                entry = new Entry(name);
            }
            calStoredEntry(recordEx, entry);
            putEntryForCurDevice(entryKey, entry);
            this.mSettingsWriter.scheduleWrite();
        }
    }

    private void calStoredEntry(TaskRecordEx recordEx, Entry entry) {
        if (recordEx != null && recordEx.getRootActivityInfo() != null && entry != null) {
            entry.originalWindowState = recordEx.getOriginalWindowState();
            boolean hasBounds = HwPCMultiWindowCompatibility.isLayoutHadBounds(recordEx.getWindowState());
            if (recordEx.instanceOfHwTaskRecord() && recordEx.isSaveBounds()) {
                entry.windowState = recordEx.getHwTaskRecordWindowState();
                if (hasBounds && isInScreen(recordEx.getRequestedOverrideBounds())) {
                    entry.windowBounds.set(recordEx.getRequestedOverrideBounds());
                }
            } else if (hasBounds && isInScreen(recordEx.getRequestedOverrideBounds())) {
                entry.windowBounds.offsetTo(recordEx.getRequestedOverrideBounds().left, recordEx.getRequestedOverrideBounds().top);
            }
        }
    }

    public void restoreTaskWindowState(TaskRecordEx recordEx) {
        if (recordEx != null && recordEx.getRootActivityInfo() != null) {
            recordEx.setOriginalWindowState(getWindowStateByDefault(recordEx, recordEx.getRootActivityInfo().screenOrientation));
            recordEx.setNextWindowState(getWindowState(recordEx));
            HwPCUtils.log(TAG, "restoreTaskWindowState: (N:" + Integer.toHexString(recordEx.getNextWindowState()) + ", O:" + Integer.toHexString(recordEx.getOriginalWindowState()) + ", C:" + Integer.toHexString(recordEx.getWindowState()) + ")");
        }
    }

    private int getWindowState(TaskRecordEx recordEx) {
        int windowState = getWindowStateBySaved(recordEx);
        if (windowState < 0) {
            return recordEx.getOriginalWindowState();
        }
        return windowState;
    }

    private int getWindowStateBySaved(TaskRecordEx recordEx) {
        String entryKey = getEntryKey(recordEx);
        Entry entry = getEntry(entryKey);
        if (entry == null) {
            return -1;
        }
        if (recordEx.getOriginalWindowState() == entry.originalWindowState) {
            return entry.windowState;
        }
        removeEntry(entryKey);
        return -1;
    }

    private boolean isPadFullscreen(TaskRecordEx recordEx) {
        if (recordEx == null || recordEx.getRootActivityInfo() == null || !HwPCUtils.enabledInPad() || (!this.mPadFullscreenList.contains(recordEx.getRootActivityInfo().packageName) && !this.mPadFullscreenList.contains(recordEx.getRootActivityInfo().name.toLowerCase(Locale.ROOT)))) {
            return false;
        }
        return true;
    }

    private boolean isMaximizedOnly(TaskRecordEx recordEx) {
        if (recordEx == null || recordEx.getRootActivityInfo() == null || !this.mMaximizedOnlyList.contains(recordEx.getRootActivityInfo().packageName)) {
            return false;
        }
        return true;
    }

    private boolean isFullscreenNoNavigationBar(TaskRecordEx recordEx) {
        if (recordEx != null && recordEx.getRootActivityInfo() != null && this.mFullscreenNoNavigationBar.contains(recordEx.getRootActivityInfo().packageName.toLowerCase(Locale.ROOT))) {
            return true;
        }
        if (recordEx == null || recordEx.getRootActivityInfo() == null) {
            return false;
        }
        try {
            if (PowerKit.getInstance().getPkgType(this.mService.getContext(), recordEx.getRootActivityInfo().packageName) == SPECIAL_PACKAGE_TYPE_PORTRAIT_MAXIMIZED) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "isFullscreenNoNavigationBar RemoteException");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "unknown exception");
            return false;
        }
    }

    private boolean isMaximizedButPortrait(TaskRecordEx recordEx) {
        if (recordEx == null || recordEx.getRootActivityInfo() == null || !this.mPortraitMaximizedPkgList.contains(recordEx.getRootActivityInfo().packageName)) {
            return false;
        }
        return true;
    }

    private boolean isFullscreenOnly(TaskRecordEx recordEx) {
        if (recordEx == null || recordEx.getRootActivityInfo() == null || recordEx.getRootActivityInfo().packageName.equals("com.android.browser")) {
            return false;
        }
        if (HwPCUtils.enabledInPad() && (recordEx.getRootActivityInfo().packageName.equals("com.example.android.notepad") || recordEx.getRootActivityInfo().packageName.equals("com.huawei.notepad"))) {
            return false;
        }
        int realTheme = recordEx.getRootActivityInfo().getThemeResource();
        if (realTheme == 0) {
            realTheme = recordEx.getRootActivityInfo().applicationInfo.targetSdkVersion < 11 ? 16973829 : 16973931;
        }
        if (AttributeCacheEx.isFloating(recordEx.getRootActivityInfo().packageName, realTheme, UserHandleEx.getUserId(recordEx.getRootActivityInfo().applicationInfo.uid))) {
            return true;
        }
        return false;
    }

    public boolean isPortraitApp(TaskRecordEx recordEx) {
        if (recordEx == null || recordEx.getRootActivityInfo() == null) {
            return false;
        }
        return isPortraitOnly(recordEx, recordEx.getRootActivityInfo().screenOrientation);
    }

    private boolean isPortraitOnly(TaskRecordEx recordEx, int requestOrientation) {
        if (recordEx == null || recordEx.getRootActivityInfo() == null) {
            return false;
        }
        if (requestOrientation == 1 || requestOrientation == SPECIAL_PACKAGE_TYPE_FULLSCREEN_NO_NAVIGATIONBAR || requestOrientation == 9 || requestOrientation == 12 || ((requestOrientation == 14 && !HwPCUtils.enabledInPad()) || this.mPortraitPkgList.contains(recordEx.getRootActivityInfo().packageName))) {
            return true;
        }
        return false;
    }

    private boolean isResizeable(TaskRecordEx recordEx) {
        if (recordEx == null || recordEx.getRootActivityInfo() == null) {
            return false;
        }
        return ActivityInfoEx.isResizeableMode(recordEx.getResizeMode());
    }

    private int getWindowStateByDefault(TaskRecordEx recordEx, int requestOrientation) {
        if (recordEx == null || recordEx.getRootActivityInfo() == null) {
            return HwPCMultiWindowCompatibility.getLandscapeWithPartAction();
        }
        if (isFullscreenNoNavigationBar(recordEx)) {
            return 132100;
        }
        if (isPadFullscreen(recordEx)) {
            return 4;
        }
        if (isMaximizedOnly(recordEx)) {
            return 3;
        }
        if (isFullscreenOnly(recordEx)) {
            return 4;
        }
        if (isMaximizedButPortrait(recordEx)) {
            return 513;
        }
        if (isPortraitOnly(recordEx, requestOrientation)) {
            if (isResizeablePortraitType(recordEx, recordEx.getRootActivityInfo().packageName)) {
                return 1538;
            }
            return 1;
        } else if (isResizeable(recordEx)) {
            return HwPCMultiWindowCompatibility.getLandscapeWithAllAction();
        } else {
            return HwPCMultiWindowCompatibility.getLandscapeWithPartAction();
        }
    }

    private boolean isResizeablePortraitType(TaskRecordEx recordEx, String pkgName) {
        return !HwPCUtils.enabledInPad() && isResizeable(recordEx) && !this.mPortraitPkgList.contains(pkgName);
    }

    public boolean isFixedOrientationPortrait(int screenOrientation) {
        return screenOrientation == 1 || screenOrientation == SPECIAL_PACKAGE_TYPE_FULLSCREEN_NO_NAVIGATIONBAR || screenOrientation == 9 || screenOrientation == 12;
    }

    public boolean isFixedOrientationLandscape(int screenOrientation) {
        return screenOrientation == 0 || screenOrientation == SPECIAL_PACKAGE_TYPE_PAD_FULLSCREEN || screenOrientation == 8 || screenOrientation == 11;
    }

    private int getWindowStateByRequestOrientation(int customRequestedOrientation, String pkgName, TaskRecordEx recordEx) {
        if (this.mFullscreenNoNavigationBar.contains(pkgName)) {
            return 132612;
        }
        if (HwPCUtils.enabledInPad() && this.mPadFullscreenList.contains(pkgName)) {
            return 4;
        }
        if (HwPCUtils.enabledInPad() && this.mMaximizedOnlyList.contains(pkgName)) {
            return 3;
        }
        if (customRequestedOrientation == 1) {
            if (isResizeablePortraitType(recordEx, pkgName)) {
                return recordEx.getWindowState();
            }
            return 1;
        } else if (this.mSpecialVideosList.contains(pkgName)) {
            return HwPCMultiWindowCompatibility.getFullscreenForSomeVideo();
        } else {
            if (this.mPortraitPkgList.contains(pkgName)) {
                return 1;
            }
            return 2;
        }
    }

    public Rect getWindowBounds(TaskRecordEx recordEx) {
        if (HwPCUtils.isHiCarCastMode()) {
            return HwHiCarMultiWindowManager.getInstance().getWindowBounds(recordEx);
        }
        Rect bounds = getWindowBoundsBySaved(recordEx);
        if (bounds == null || bounds.isEmpty()) {
            return getWindowBoundsByDefault(getWindowStateByDefault(recordEx, recordEx.getRootActivityInfo().screenOrientation));
        }
        return bounds;
    }

    private Rect getWindowBoundsBySaved(TaskRecordEx recordEx) {
        Entry entry = getEntry(getEntryKey(recordEx));
        if (entry == null) {
            return null;
        }
        return entry.windowBounds;
    }

    private boolean isInScreen(Rect bounds) {
        if (bounds == null || bounds.isEmpty()) {
            return false;
        }
        Point size = getExtDisplaySize();
        if (bounds.left < 0 || bounds.right > size.x || bounds.top < 0 || bounds.bottom > size.y) {
            return false;
        }
        return true;
    }

    private Rect getWindowBoundsByDefault(int windowState) {
        String str;
        HwPCMultiWindowPolicy.updateDefaultSize(getRightContext(this.mService.getContext()), this);
        int layoutState = HwPCMultiWindowCompatibility.getWindowStateLayout(windowState);
        Rect bounds = new Rect();
        if (layoutState == 1) {
            getBounds(true, bounds);
        } else if (layoutState == 2) {
            getBounds(false, bounds);
        } else if (layoutState != 3) {
            bounds = null;
        } else {
            Point size = getExtDisplaySize();
            bounds.set(0, 0, size.x, size.y);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getWindowBoundsByDefault  ");
        if (bounds == null) {
            str = "null";
        } else {
            str = bounds.toShortString() + " (" + bounds.width() + ", " + bounds.height() + ") windowState: " + windowState;
        }
        sb.append(str);
        HwPCUtils.log(TAG, sb.toString());
        return bounds;
    }

    public Rect getLaunchBounds(TaskRecordEx recordEx) {
        String str;
        Rect outRect = new Rect();
        if (recordEx == null || recordEx.getRootActivityInfo() == null) {
            return outRect;
        }
        if (HwPCUtils.isHiCarCastMode()) {
            return HwHiCarMultiWindowManager.getInstance().getWindowBounds(recordEx);
        }
        getExtDisplaySize(true);
        if (HwPCMultiWindowCompatibility.isLayoutHadBounds(recordEx.getNextWindowState())) {
            outRect = getWindowBounds(recordEx);
            if (!(outRect == null || recordEx.getRootActivityInfo().windowLayout == null)) {
                int width = getFinalWidth(recordEx.getRootActivityInfo().windowLayout);
                if (width > 0) {
                    outRect.right = outRect.left + width;
                }
                int height = getFinalHeight(recordEx.getRootActivityInfo().windowLayout);
                if (height > 0) {
                    outRect.bottom = outRect.top + height;
                }
            }
        } else {
            int windowStateLayout = HwPCMultiWindowCompatibility.getWindowStateLayout(recordEx.getNextWindowState());
            if (windowStateLayout == 3) {
                outRect.set(getMaximizedBounds());
            } else if (windowStateLayout == 4) {
                outRect = null;
            } else if (windowStateLayout == SPECIAL_PACKAGE_TYPE_PORTRAIT_MAXIMIZED) {
                outRect = getWindowBounds(recordEx);
            } else if (windowStateLayout == SPECIAL_PACKAGE_TYPE_PAD_FULLSCREEN) {
                outRect = getWindowBounds(recordEx);
            }
        }
        Rect outRect2 = adjustBounds(outRect, recordEx.getTaskId());
        StringBuilder sb = new StringBuilder();
        sb.append("getLaunchBounds: ");
        if (outRect2 == null) {
            str = "null";
        } else {
            str = outRect2.toShortString() + " (" + outRect2.width() + ", " + outRect2.height() + ")";
        }
        sb.append(str);
        HwPCUtils.log(TAG, sb.toString());
        return outRect2;
    }

    private boolean isRectHasWindowSize(Rect rect) {
        Point size = getExtDisplaySize();
        Rect screenRect = new Rect(0, 0, size.x, size.y);
        if (rect == null || rect.isEmpty() || rect.equals(screenRect)) {
            return false;
        }
        return true;
    }

    private Rect adjustBounds(Rect rect, int taskId) {
        boolean z;
        int offsetY;
        int offsetY2;
        HwPCMultiWindowManager hwPCMultiWindowManager = this;
        if (!isRectHasWindowSize(rect)) {
            return rect;
        }
        Point size = getExtDisplaySize();
        Rect screenRect = new Rect(0, 0, size.x, size.y);
        ArrayList<Rect> bounds = new ArrayList<>();
        int activityDisplaysSize = hwPCMultiWindowManager.mService.getSizeOfActivityDisplayFromStackSupervisor();
        ArrayList<ActivityStackEx> stackExs = new ArrayList<>();
        for (int displayNdx = activityDisplaysSize - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplayEx activityDisplayEx = hwPCMultiWindowManager.mService.getActivityDisplayExFromStackSupervisorByIndex(displayNdx);
            int childCount = activityDisplayEx.getChildCount();
            for (int i = 0; i < childCount; i++) {
                stackExs.add(activityDisplayEx.getChildAt(i));
            }
        }
        int stackSize = stackExs.size();
        for (int i2 = 0; i2 < stackSize; i2++) {
            ActivityStackEx stackEx = stackExs.get(i2);
            if (stackEx.getDisplayId() != 0) {
                if (stackEx.getDisplayId() != -1) {
                    TaskRecordEx taskEx = stackEx.topTask();
                    if (taskEx != null) {
                        if (!(taskId == taskEx.getTaskId() || taskEx.getRequestedOverrideBounds() == null || taskEx.getRequestedOverrideBounds().equals(screenRect))) {
                            bounds.add(taskEx.getRequestedOverrideBounds());
                        }
                    }
                }
            }
        }
        Rect newRect = new Rect(rect);
        int offsetMin = HwPCMultiWindowPolicy.dpToPx(hwPCMultiWindowManager.getRightContext(hwPCMultiWindowManager.mService.getContext()), 15);
        int i3 = 30;
        int offset = HwPCMultiWindowPolicy.dpToPx(hwPCMultiWindowManager.getRightContext(hwPCMultiWindowManager.mService.getContext()), 30);
        int i4 = 0;
        boolean tryForwardX = true;
        boolean tryForwardY = true;
        while (true) {
            if (!hwPCMultiWindowManager.isWindowOverlapped(newRect, bounds)) {
                break;
            }
            int i5 = i4 + 1;
            if (i4 >= i3) {
                break;
            }
            int offsetX = 0;
            boolean noX = false;
            boolean noY = false;
            if (tryForwardX && newRect.right + offsetMin < size.x) {
                offsetX = newRect.right + offset > size.x ? size.x - newRect.right : offset;
            } else if (newRect.left - offsetMin > 0) {
                offsetX = newRect.left - offset > 0 ? 0 - offset : 0 - newRect.left;
                tryForwardX = false;
            } else {
                noX = true;
            }
            if (tryForwardY && newRect.bottom + offsetMin < size.y) {
                offsetY = newRect.bottom + offset > size.y ? size.y - newRect.bottom : offset;
                z = false;
            } else if (newRect.top - offsetMin > 0) {
                if (newRect.top - offset > 0) {
                    z = false;
                    offsetY2 = 0 - offset;
                } else {
                    z = false;
                    offsetY2 = 0 - newRect.top;
                }
                tryForwardY = false;
                offsetY = offsetY2;
            } else {
                z = false;
                noY = true;
                offsetY = 0;
            }
            if (noX && noY) {
                break;
            }
            newRect.offset(offsetX, offsetY);
            hwPCMultiWindowManager = this;
            i4 = i5;
            i3 = 30;
        }
        return newRect;
    }

    private boolean isWindowOverlapped(Rect rect, List<Rect> taskBounds) {
        int taskBoundsSize = taskBounds.size();
        for (int i = 0; i < taskBoundsSize; i++) {
            if (rect.equals(taskBounds.get(i))) {
                HwPCUtils.log(TAG, "isWindowOverlapped return true.");
                return true;
            }
        }
        return false;
    }

    private void getBounds(boolean isPortrait, Rect out) {
        int height;
        int width;
        Point size = getExtDisplaySize();
        if (isPortrait) {
            width = HwPCMultiWindowPolicy.mDefPortraitWidth;
            height = (int) (((float) HwPCMultiWindowPolicy.mDefPortraitWidth) / HwPCMultiWindowPolicy.mPortraitRatio);
        } else {
            height = HwPCMultiWindowPolicy.mDefLandscapeHeight;
            width = (int) (((float) HwPCMultiWindowPolicy.mDefLandscapeHeight) * HwPCMultiWindowPolicy.mLandscapeRatio);
        }
        int height2 = height + this.mDecorHeight;
        if (width > size.x) {
            width = size.x;
            height2 = (int) (((float) size.x) / (isPortrait ? HwPCMultiWindowPolicy.mPortraitRatio : HwPCMultiWindowPolicy.mLandscapeRatio));
        }
        if (height2 > size.y) {
            height2 = size.y;
            width = (int) (((float) size.y) * (isPortrait ? HwPCMultiWindowPolicy.mPortraitRatio : HwPCMultiWindowPolicy.mLandscapeRatio));
        }
        out.set(HwPCMultiWindowPolicy.mWindowMarginLeft, HwPCMultiWindowPolicy.mWindowMarginTop, HwPCMultiWindowPolicy.mWindowMarginLeft + width, HwPCMultiWindowPolicy.mWindowMarginTop + height2);
    }

    private boolean isNeedDelay(TaskRecordEx recordEx) {
        if (recordEx == null || recordEx.getRootActivityInfo() == null) {
            return false;
        }
        String pkgName = recordEx.getRootActivityInfo().packageName;
        if (TextUtils.isEmpty(pkgName) || !this.mNeedDelayList.contains(pkgName)) {
            return false;
        }
        HwPCUtils.log(TAG, "isNeedDelay return true.");
        return true;
    }

    public void updateTaskByRequestedOrientation(TaskRecordEx recordEx, int requestedOrientation) {
        if (isNeedDelay(recordEx)) {
            if (requestedOrientation != 0) {
                int i = this.mLastRequestedOrientation;
                if (!(i == 0 || i == requestedOrientation)) {
                    this.mFixOrientationChanged = true;
                }
                this.mLastRequestedOrientation = requestedOrientation;
            }
            Message msg = Message.obtain();
            this.mHandler.removeMessages(1);
            msg.what = 1;
            msg.arg1 = requestedOrientation;
            msg.arg2 = recordEx.getTaskId();
            this.mHandler.sendMessageDelayed(msg, 200);
            return;
        }
        this.mFixOrientationChanged = false;
        this.mLastRequestedOrientation = 0;
        updateTaskByRequestedOrientationInternal(recordEx, requestedOrientation);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTaskByRequestedOrientationInternal(TaskRecordEx recordEx, int customRequestedOrientation) {
        String str;
        if (customRequestedOrientation != 0) {
            if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || !PKG_NO_UPDATE_FOR_PAD_IN_PC.contains(recordEx.getPkgNameFromTopActivity())) {
                int currentOrientation = 1;
                if (!(HwPCMultiWindowCompatibility.getWindowStateLayout(recordEx.getWindowState()) == 1)) {
                    currentOrientation = 2;
                }
                if (currentOrientation != customRequestedOrientation || ((this.mFixOrientationChanged && customRequestedOrientation == 2) || this.mUpdateTaskSet.contains(Integer.valueOf(recordEx.getTaskId())))) {
                    this.mFixOrientationChanged = false;
                    this.mLastRequestedOrientation = 0;
                    if (HwPCUtils.enabledInPad()) {
                        recordEx.setNextWindowState(getWindowStateByRequestOrientation(customRequestedOrientation, recordEx.getPkgNameFromTopActivity(), recordEx));
                    } else {
                        recordEx.setNextWindowState(getWindowStateByDefault(recordEx, customRequestedOrientation));
                    }
                    HwPCUtils.log(TAG, "updateTaskByRequestedOrientation-pre by default: " + Integer.toHexString(recordEx.getWindowState()) + " to " + Integer.toHexString(recordEx.getNextWindowState()));
                    if (recordEx.getWindowState() != recordEx.getNextWindowState() || this.mUpdateTaskSet.contains(Integer.valueOf(recordEx.getTaskId()))) {
                        Rect rect = getWindowBoundsByDefault(recordEx.getNextWindowState());
                        if (recordEx.getRequestedOverrideBounds() != null && !recordEx.getRequestedOverrideBounds().isEmpty() && isRectHasWindowSize(rect)) {
                            rect.offsetTo(recordEx.getRequestedOverrideBounds().left, recordEx.getRequestedOverrideBounds().top);
                        }
                        Rect rect2 = adjustBounds(rect, recordEx.getTaskId());
                        if (recordEx.instanceOfHwTaskRecord()) {
                            recordEx.setSaveBounds(false);
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("updateTaskByRequestedOrientation: ");
                        sb.append(Integer.toHexString(recordEx.getNextWindowState()));
                        sb.append(" - ");
                        if (rect2 == null) {
                            str = "null";
                        } else {
                            str = rect2.toShortString() + " (" + rect2.width() + ", " + rect2.height() + ")";
                        }
                        sb.append(str);
                        HwPCUtils.log(TAG, sb.toString());
                        if (HwPCUtils.enabledInPad()) {
                            resizeTaskFromPC(recordEx, rect2);
                        }
                    }
                    this.mUpdateTaskSet.remove(Integer.valueOf(recordEx.getTaskId()));
                }
            }
        }
    }

    public void resizeTaskFromPC(TaskRecordEx recordEx, Rect rect) {
        synchronized (this.mService.getGlobalLock()) {
            recordEx.resize(rect, 3, true, false);
        }
    }

    private Display getExtDisplay(boolean isInternal) {
        DisplayManager displayManager = null;
        if (this.mService.isHwWindowManagerService()) {
            displayManager = this.mService.getDisplayManager();
        }
        if (displayManager == null) {
            return null;
        }
        if (isInternal) {
            return displayManager.getDisplay(0);
        }
        Display[] displays = displayManager.getDisplays();
        for (Display d : displays) {
            if (HwPCUtils.isValidExtDisplayId(d.getDisplayId())) {
                return d;
            }
        }
        return null;
    }

    private Point getExtDisplaySize() {
        return getExtDisplaySize(false);
    }

    private Point getExtDisplaySize(boolean forceUpdate) {
        Display display = getExtDisplay(false);
        if (!(display == null || display.getDisplayId() == this.mCurDisplayId)) {
            Point point = this.mNonDecorScreenSize;
            point.x = 0;
            point.y = 0;
            this.mCurDisplayId = display.getDisplayId();
        }
        if ((this.mNonDecorScreenSize.x == 0 || this.mNonDecorScreenSize.y == 0 || forceUpdate) && display != null && this.mService.isHwWindowManagerService()) {
            DisplayPolicyEx policyEx = this.mService.getDisplayContentEx(HwPCUtils.getPCDisplayID()).getDisplayPolicyEx();
            Point fullScreenSize = new Point();
            display.getRealSize(fullScreenSize);
            if (policyEx != null) {
                this.mNonDecorScreenSize.x = policyEx.getNonDecorDisplayWidth(fullScreenSize.x, fullScreenSize.y, 0, 0, (DisplayCutout) null);
                this.mNonDecorScreenSize.y = policyEx.getNonDecorDisplayHeight(fullScreenSize.x, fullScreenSize.y, 0, 0, (DisplayCutout) null);
                this.mDecorHeight = fullScreenSize.y - this.mNonDecorScreenSize.y;
                HwPCUtils.log(TAG, "NonDecorScreenSize=" + this.mNonDecorScreenSize + ",decorHeight=" + this.mDecorHeight);
            }
        }
        return this.mNonDecorScreenSize;
    }

    private Context getRightContext(Context ctx) {
        Display display = getExtDisplay(false);
        if (display != null) {
            return ctx.createDisplayContext(display);
        }
        return ctx;
    }

    public Rect getMaximizedBounds() {
        if (HwPCUtils.isHiCarCastMode()) {
            return HwHiCarMultiWindowManager.getInstance().getMaximizedBounds();
        }
        Point size = getExtDisplaySize(true);
        return new Rect(0, 0, size.x, size.y);
    }

    public Rect getSplitLeftWindowBounds() {
        return new Rect(getMaximizedBounds().left, getMaximizedBounds().top, getMaximizedBounds().width() / 2, getMaximizedBounds().bottom);
    }

    public Rect getSplitRightWindowBounds() {
        return new Rect(getMaximizedBounds().width() / 2, getMaximizedBounds().top, getMaximizedBounds().right, getMaximizedBounds().bottom);
    }

    public boolean isSupportResize(TaskRecordEx recordEx, boolean isFullscreen, boolean isMaximized) {
        if (recordEx == null) {
            return false;
        }
        if (isFullscreen) {
            return HwPCMultiWindowCompatibility.isFullscreenable(recordEx.getWindowState());
        }
        if (isMaximized) {
            return HwPCMultiWindowCompatibility.isMaximizeable(recordEx.getWindowState());
        }
        return HwPCMultiWindowCompatibility.isResizable(recordEx.getWindowState());
    }

    private final class WorkerHandler extends Handler {
        WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                synchronized (HwPCMultiWindowManager.this.mService.getGlobalLock()) {
                    TaskRecordEx taskRecordEx = HwPCMultiWindowManager.this.mService.anyTaskForId(msg.arg2);
                    if (taskRecordEx != null) {
                        HwPCMultiWindowManager.this.updateTaskByRequestedOrientationInternal(taskRecordEx, msg.arg1);
                    }
                }
            }
        }
    }

    private int getFinalWidth(ActivityInfo.WindowLayout windowLayout) {
        int width = 0;
        if (windowLayout.width > 0) {
            width = computeExtWidth(windowLayout.width);
        }
        if (windowLayout.widthFraction > 0.0f) {
            return (int) (((float) getExtDisplaySize().x) * windowLayout.widthFraction);
        }
        return width;
    }

    private int getFinalHeight(ActivityInfo.WindowLayout windowLayout) {
        int height = 0;
        if (windowLayout.height > 0) {
            height = computeExtWidth(windowLayout.height);
        }
        if (windowLayout.heightFraction > 0.0f) {
            return (int) (((float) getExtDisplaySize().y) * windowLayout.heightFraction);
        }
        return height;
    }

    private int computeExtWidth(int val) {
        int internalDpi = 1;
        int externalDpi = 1;
        DisplayInfoEx info = new DisplayInfoEx();
        Display dis = getExtDisplay(true);
        if (dis != null) {
            DisplayEx.getDisplayInfo(dis, info);
            internalDpi = info.getLogicalDensityDpi();
        }
        Display dis2 = getExtDisplay(false);
        if (dis2 != null) {
            DisplayEx.getDisplayInfo(dis2, info);
            externalDpi = info.getLogicalDensityDpi();
        }
        return (val * externalDpi) / internalDpi;
    }

    private String getEntryKey(TaskRecordEx recordEx) {
        int stackId = recordEx.getStackId();
        String name = recordEx.getRootActivityInfo().packageName;
        return name + "_stackId_" + stackId;
    }

    public boolean isSpecialVideo(String pkgName) {
        return this.mSpecialVideosList.contains(pkgName);
    }

    public boolean isOlnyFullscreen(String pkgName) {
        return this.mPadFullscreenList.contains(pkgName) || this.mFullscreenNoNavigationBar.contains(pkgName);
    }

    public void setForceUpdateTask(int taskId) {
        this.mUpdateTaskSet.add(Integer.valueOf(taskId));
    }

    public List<String> getPortraitMaximizedPkgList() {
        return this.mPortraitMaximizedPkgList;
    }
}
