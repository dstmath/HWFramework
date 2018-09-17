package com.android.server.am;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfo.WindowLayout;
import android.content.res.HwPCMultiWindowCompatibility;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.WindowManagerPolicy;
import com.android.internal.R;
import com.android.server.AttributeCache;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.wm.HwWindowManagerService;
import com.huawei.pgmng.plug.PGSdk;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwPCMultiWindowManager {
    private static final int DP_WINDOW_OVERLAP_OFFSET = 30;
    private static final int DP_WINDOW_OVERLAP_OFFSET_MIN = 15;
    static final int FIX_ORIENTATION_LANDSCAPE = 2;
    static final int FIX_ORIENTATION_NONE = 0;
    static final int FIX_ORIENTATION_PORTRAIT = 1;
    private static final int MAX_TIMES_TO_ADJUST_BOUNDS = 30;
    private static final int MSG_REQUEST_ORIENTATION = 1;
    private static final int MSG_REQUEST_ORIENTATION_DELAY = 200;
    static final int SPECIAL_PACKAGE_TYPE_FULLSCREEN_NO_NAVIGATIONBAR = 7;
    static final int SPECIAL_PACKAGE_TYPE_MAXIMIZED_ONLY = 3;
    static final int SPECIAL_PACKAGE_TYPE_NEED_DELAY = 2;
    static final int SPECIAL_PACKAGE_TYPE_PAD_FULLSCREEN = 6;
    static final int SPECIAL_PACKAGE_TYPE_PORTRAIT = 4;
    static final int SPECIAL_PACKAGE_TYPE_PORTRAIT_MAXIMIZED = 5;
    static final int SPECIAL_PACKAGE_TYPE_VIDEO_NEED_FULLSCREEN = 1;
    static final String TAG = "HwPCMultiWindowManager";
    private static final Object mLock = new Object();
    private static volatile HwPCMultiWindowManager mSingleInstance = null;
    private int mCurDisplayId = -1;
    private int mDecorHeight = 0;
    final HashMap<String, HashMap<String, Entry>> mEntries;
    boolean mFixOrientationChanged = false;
    final List<String> mFullscreenNoNavigationBar = new ArrayList();
    final Handler mHandler;
    int mLastRequestedOrientation = 0;
    final List<String> mMaximizedOnlyList = new ArrayList();
    final List<String> mNeedDelayList = new ArrayList();
    private Point mNonDecorScreenSize = new Point();
    final List<String> mPadFullscreenList = new ArrayList();
    final List<String> mPortraitMaximizedPkgList = new ArrayList();
    final List<String> mPortraitPkgList = new ArrayList();
    final ActivityManagerService mService;
    final HwPCMultiWindowSettingsWriter mSettingsWriter;
    final List<String> mSpecialVideosList = new ArrayList();
    private Set<Integer> mUpdateTaskSet = Collections.synchronizedSet(new HashSet());

    public static class Entry {
        public int originalWindowState = this.windowState;
        public final String pkgName;
        public Rect windowBounds = new Rect();
        public int windowState = 1;

        public Entry(String _name) {
            this.pkgName = _name;
        }

        public String toString() {
            return "pkgName=" + this.pkgName + "; windowState=" + Integer.toHexString(this.windowState) + "; " + "originalWindowState=" + Integer.toHexString(this.originalWindowState) + "; " + "bounds=" + (this.windowBounds == null ? "null" : this.windowBounds.toShortString());
        }
    }

    private final class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (HwPCMultiWindowManager.this.mService) {
                        TaskRecord tr = HwPCMultiWindowManager.this.mService.mStackSupervisor.anyTaskForIdLocked(msg.arg2);
                        if (tr != null) {
                            HwPCMultiWindowManager.this.updateTaskByRequestedOrientationInternal(tr, msg.arg1);
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static HwPCMultiWindowManager getInstance(ActivityManagerService service) {
        if (mSingleInstance == null) {
            synchronized (mLock) {
                if (mSingleInstance == null) {
                    mSingleInstance = new HwPCMultiWindowManager(service);
                }
            }
        }
        return mSingleInstance;
    }

    private HwPCMultiWindowManager(ActivityManagerService service) {
        this.mService = service;
        this.mEntries = new HashMap();
        HwPCMultiWindowPolicy.initialize(getRightContext(this.mService.mContext), this);
        this.mSettingsWriter = new HwPCMultiWindowSettingsWriter(this);
        this.mHandler = new WorkerHandler(this.mService.mHandler.getLooper());
        this.mUpdateTaskSet.clear();
    }

    public void putEntry(String deviceKey, String entryKey, Entry entry) {
        if (!TextUtils.isEmpty(deviceKey) && !TextUtils.isEmpty(entryKey)) {
            if (!this.mEntries.containsKey(deviceKey)) {
                this.mEntries.put(deviceKey, new HashMap());
            }
            ((HashMap) this.mEntries.get(deviceKey)).put(entryKey, entry);
        }
    }

    public void putEntryForCurDevice(String entryKey, Entry entry) {
        putEntry(getCurDeviceKey(), entryKey, entry);
    }

    public Entry getEntry(String entryKey) {
        String deviceKey = getCurDeviceKey();
        if (this.mEntries.containsKey(deviceKey)) {
            return (Entry) ((HashMap) this.mEntries.get(deviceKey)).get(entryKey);
        }
        return null;
    }

    private void removeEntry(String entryKey) {
        String deviceKey = getCurDeviceKey();
        if (this.mEntries.containsKey(deviceKey) && ((HashMap) this.mEntries.get(deviceKey)).containsKey(entryKey)) {
            ((HashMap) this.mEntries.get(deviceKey)).remove(entryKey);
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

    public void storeTaskSettings(TaskRecord record) {
        if (record != null && record.mRootActivityInfo != null) {
            String name = record.mRootActivityInfo.packageName;
            Entry entry = getEntry(name);
            if (entry == null) {
                entry = new Entry(name);
            }
            calStoredEntry(record, entry);
            putEntryForCurDevice(name, entry);
            this.mSettingsWriter.scheduleWrite();
        }
    }

    private void calStoredEntry(TaskRecord record, Entry entry) {
        if (record != null && record.mRootActivityInfo != null && entry != null) {
            entry.originalWindowState = record.mOriginalWindowState;
            boolean hasBounds = HwPCMultiWindowCompatibility.isLayoutHadBounds(record.mWindowState);
            if ((record instanceof HwTaskRecord) && ((HwTaskRecord) record).mSaveBounds) {
                entry.windowState = ((HwTaskRecord) record).mWindowState;
                if (hasBounds && isInScreen(record.mBounds)) {
                    entry.windowBounds.set(record.mBounds);
                }
            } else if (hasBounds && isInScreen(record.mBounds)) {
                entry.windowBounds.offsetTo(record.mBounds.left, record.mBounds.top);
            }
        }
    }

    public void restoreTaskWindowState(TaskRecord record) {
        if (record != null && record.mRootActivityInfo != null) {
            record.mOriginalWindowState = getWindowStateByDefault(record, record.mRootActivityInfo.screenOrientation);
            record.mNextWindowState = getWindowState(record);
            HwPCUtils.log(TAG, "restoreTaskWindowState: (N:" + Integer.toHexString(record.mNextWindowState) + ", O:" + Integer.toHexString(record.mOriginalWindowState) + ", C:" + Integer.toHexString(record.mWindowState) + ")");
        }
    }

    private int getWindowState(TaskRecord record) {
        int windowState = getWindowStateBySaved(record);
        if (windowState < 0) {
            return record.mOriginalWindowState;
        }
        return windowState;
    }

    private int getWindowStateBySaved(TaskRecord record) {
        String pkgName = record.mRootActivityInfo.packageName;
        Entry entry = getEntry(pkgName);
        if (entry == null) {
            return -1;
        }
        if (record.mOriginalWindowState == entry.originalWindowState) {
            return entry.windowState;
        }
        removeEntry(pkgName);
        return -1;
    }

    private boolean isPadFullscreen(TaskRecord record) {
        if (!HwPCUtils.enabledInPad() || record == null || record.mRootActivityInfo == null || (!this.mPadFullscreenList.contains(record.mRootActivityInfo.packageName) && !this.mPadFullscreenList.contains(record.mRootActivityInfo.name.toLowerCase()))) {
            return false;
        }
        return true;
    }

    private boolean isMaximizedOnly(TaskRecord record) {
        if (record == null || record.mRootActivityInfo == null || !this.mMaximizedOnlyList.contains(record.mRootActivityInfo.packageName)) {
            return false;
        }
        return true;
    }

    private boolean isFullscreenNoNavigationBar(TaskRecord record) {
        if (record == null || record.mRootActivityInfo == null || !this.mFullscreenNoNavigationBar.contains(record.mRootActivityInfo.packageName)) {
            return false;
        }
        return true;
    }

    private boolean isMaximizedButPortrait(TaskRecord record) {
        if (record == null || record.mRootActivityInfo == null || !this.mPortraitMaximizedPkgList.contains(record.mRootActivityInfo.packageName)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:27:0x006d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isFullscreenOnly(TaskRecord record) {
        boolean z = true;
        if (record == null || record.mRootActivityInfo == null || record.mRootActivityInfo.packageName.equals("com.android.browser")) {
            return false;
        }
        int realTheme = record.mRootActivityInfo.getThemeResource();
        if (realTheme == 0) {
            realTheme = record.mRootActivityInfo.applicationInfo.targetSdkVersion < 11 ? 16973829 : 16973931;
        }
        com.android.server.AttributeCache.Entry ent = AttributeCache.instance().get(record.mRootActivityInfo.packageName, realTheme, R.styleable.Window, UserHandle.getUserId(record.mRootActivityInfo.applicationInfo.uid));
        if (ent != null ? ent.array.getBoolean(4, false) : false) {
            return true;
        }
        try {
            if (PGSdk.getInstance().getPkgType(this.mService.mContext, record.mRootActivityInfo.packageName) != 5) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPortraitApp(TaskRecord record) {
        if (record == null || record.mRootActivityInfo == null) {
            return false;
        }
        boolean ret = isPortraitOnly(record, record.mRootActivityInfo.screenOrientation);
        HwPCUtils.log(TAG, "isPortraitApp: record.mRootActivityInfo.packageName = " + record.mRootActivityInfo.packageName + ",ret = " + ret);
        return ret;
    }

    private boolean isPortraitOnly(TaskRecord record, int requestOrientation) {
        if (requestOrientation == 1 || requestOrientation == 7 || requestOrientation == 9 || requestOrientation == 12 || (requestOrientation == 14 && (HwPCUtils.enabledInPad() ^ 1) != 0)) {
            return true;
        }
        if (record == null || record.mRootActivityInfo == null || !this.mPortraitPkgList.contains(record.mRootActivityInfo.packageName)) {
            return false;
        }
        return true;
    }

    private boolean isResizeable(TaskRecord record) {
        boolean z = false;
        if (record == null) {
            return false;
        }
        if ((record.mRootActivityInfo.applicationInfo.flags & 1) == 0) {
            return ActivityInfo.isResizeableMode(record.mResizeMode);
        }
        if (2 == record.mResizeMode) {
            z = true;
        }
        return z;
    }

    private int getWindowStateByDefault(TaskRecord record, int requestOrientation) {
        if (isFullscreenNoNavigationBar(record)) {
            return 132612;
        }
        if (isPadFullscreen(record)) {
            return 4;
        }
        if (isMaximizedOnly(record)) {
            return 3;
        }
        if (isFullscreenOnly(record)) {
            return 4;
        }
        if (isMaximizedButPortrait(record)) {
            return 513;
        }
        if (isPortraitOnly(record, requestOrientation)) {
            return 1;
        }
        if (isResizeable(record)) {
            return HwPCMultiWindowCompatibility.getLandscapeWithAllAction();
        }
        return HwPCMultiWindowCompatibility.getLandscapeWithPartAction();
    }

    static boolean isFixedOrientationPortrait(int screenOrientation) {
        if (screenOrientation == 1 || screenOrientation == 7 || screenOrientation == 9 || screenOrientation == 12) {
            return true;
        }
        return false;
    }

    static boolean isFixedOrientationLandscape(int screenOrientation) {
        if (screenOrientation == 0 || screenOrientation == 6 || screenOrientation == 8 || screenOrientation == 11) {
            return true;
        }
        return false;
    }

    private int getWindowStateByRequestOrientation(int customRequestedOrientation, String pkgName) {
        if (this.mFullscreenNoNavigationBar.contains(pkgName)) {
            return 132612;
        }
        if (HwPCUtils.enabledInPad() && this.mPadFullscreenList.contains(pkgName)) {
            return 4;
        }
        if (HwPCUtils.enabledInPad() && this.mMaximizedOnlyList.contains(pkgName)) {
            HwPCUtils.log(TAG, "WindowState in Pad: Maximized " + pkgName);
            return 3;
        } else if (customRequestedOrientation == 1) {
            return 1;
        } else {
            if (this.mSpecialVideosList.contains(pkgName)) {
                return HwPCMultiWindowCompatibility.getFullscreenForSomeVideo();
            }
            if (this.mPortraitPkgList.contains(pkgName)) {
                return 1;
            }
            return 2;
        }
    }

    public Rect getWindowBounds(TaskRecord record) {
        Rect bounds = getWindowBoundsBySaved(record);
        if (bounds == null || bounds.isEmpty()) {
            return getWindowBoundsByDefault(getWindowStateByDefault(record, record.mRootActivityInfo.screenOrientation));
        }
        return bounds;
    }

    private Rect getWindowBoundsBySaved(TaskRecord record) {
        Entry entry = getEntry(record.mRootActivityInfo.packageName);
        if (entry == null) {
            return null;
        }
        return entry.windowBounds;
    }

    private boolean isInScreen(Rect bounds) {
        boolean z = false;
        if (bounds == null || bounds.isEmpty()) {
            return false;
        }
        Point size = getExtDisplaySize();
        if (bounds.left >= 0 && bounds.right <= size.x && bounds.top >= 0 && bounds.bottom <= size.y) {
            z = true;
        }
        return z;
    }

    private Rect getWindowBoundsByDefault(int windowState) {
        HwPCMultiWindowPolicy.updateDefaultSize(getRightContext(this.mService.mContext), this);
        int layoutState = HwPCMultiWindowCompatibility.getWindowStateLayout(windowState);
        Rect bounds = new Rect();
        switch (layoutState) {
            case 1:
                getBounds(true, bounds);
                return bounds;
            case 2:
                getBounds(false, bounds);
                return bounds;
            case 3:
                Point size = getExtDisplaySize();
                bounds.set(0, 0, size.x, size.y);
                return bounds;
            default:
                return null;
        }
    }

    public Rect getLaunchBounds(TaskRecord record) {
        Rect outRect = new Rect();
        if (record == null || record.mRootActivityInfo == null) {
            return outRect;
        }
        String str;
        if (!HwPCMultiWindowCompatibility.isLayoutHadBounds(record.mNextWindowState)) {
            switch (HwPCMultiWindowCompatibility.getWindowStateLayout(record.mNextWindowState)) {
                case 3:
                    outRect.set(getMaximizedBounds());
                    break;
                case 4:
                    outRect = null;
                    break;
            }
        }
        outRect = getWindowBounds(record);
        if (!(outRect == null || record.mRootActivityInfo.windowLayout == null)) {
            int width = getFinalWidth(record.mRootActivityInfo.windowLayout);
            if (width > 0) {
                outRect.right = outRect.left + width;
            }
            int height = getFinalHeight(record.mRootActivityInfo.windowLayout);
            if (height > 0) {
                outRect.bottom = outRect.top + height;
            }
        }
        outRect = adjustBounds(outRect, record.taskId);
        String str2 = TAG;
        StringBuilder append = new StringBuilder().append("getLaunchBounds: ");
        if (outRect == null) {
            str = "null";
        } else {
            str = outRect.toShortString() + " (" + outRect.width() + ", " + outRect.height() + ")";
        }
        HwPCUtils.log(str2, append.append(str).toString());
        return outRect;
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
        if (!isRectHasWindowSize(rect)) {
            return rect;
        }
        int i;
        Point size = getExtDisplaySize();
        Rect screenRect = new Rect(0, 0, size.x, size.y);
        ArrayList<Rect> bounds = new ArrayList();
        ArrayList<ActivityStack> stacks = this.mService.mStackSupervisor.getStacks();
        int stackSize = stacks.size();
        for (i = 0; i < stackSize; i++) {
            ActivityStack stack = (ActivityStack) stacks.get(i);
            if (!(stack.mDisplayId == 0 || stack.mDisplayId == -1)) {
                TaskRecord task = stack.topTask();
                if (!(task == null || taskId == task.taskId || task.mBounds == null || (task.mBounds.equals(screenRect) ^ 1) == 0)) {
                    bounds.add(task.mBounds);
                }
            }
        }
        Rect newRect = new Rect(rect);
        int offsetMin = HwPCMultiWindowPolicy.dpToPx(getRightContext(this.mService.mContext), 15);
        int offset = HwPCMultiWindowPolicy.dpToPx(getRightContext(this.mService.mContext), 30);
        i = 0;
        boolean tryForwardX = true;
        boolean tryForwardY = true;
        while (isWindowOverlapped(newRect, bounds)) {
            int i2 = i + 1;
            if (i >= 30) {
                break;
            }
            int offsetX = 0;
            int offsetY = 0;
            boolean noX = false;
            boolean noY = false;
            if (tryForwardX && newRect.right + offsetMin < size.x) {
                offsetX = newRect.right + offset > size.x ? size.x - newRect.right : offset;
            } else if (newRect.left - offsetMin > 0) {
                tryForwardX = false;
                offsetX = newRect.left - offset > 0 ? 0 - offset : 0 - newRect.left;
            } else {
                noX = true;
            }
            if (tryForwardY && newRect.bottom + offsetMin < size.y) {
                offsetY = newRect.bottom + offset > size.y ? size.y - newRect.bottom : offset;
            } else if (newRect.top - offsetMin > 0) {
                tryForwardY = false;
                offsetY = newRect.top - offset > 0 ? 0 - offset : 0 - newRect.top;
            } else {
                noY = true;
            }
            if (noX && noY) {
                i = i2;
                break;
            }
            newRect.offset(offsetX, offsetY);
            i = i2;
        }
        return newRect;
    }

    private boolean isWindowOverlapped(Rect rect, ArrayList<Rect> taskBounds) {
        int taskBoundsSize = taskBounds.size();
        for (int i = 0; i < taskBoundsSize; i++) {
            if (rect.equals((Rect) taskBounds.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void getBounds(boolean isPortrait, Rect out) {
        int width;
        int height;
        Point size = getExtDisplaySize();
        if (isPortrait) {
            width = HwPCMultiWindowPolicy.mDefPortraitWidth;
            height = (int) (((float) HwPCMultiWindowPolicy.mDefPortraitWidth) / HwPCMultiWindowPolicy.mPortraitRatio);
        } else {
            height = HwPCMultiWindowPolicy.mDefLandscapeHeight;
            width = (int) (((float) HwPCMultiWindowPolicy.mDefLandscapeHeight) * HwPCMultiWindowPolicy.mLandscapeRatio);
        }
        height += this.mDecorHeight;
        if (width > size.x) {
            width = size.x;
            height = (int) (((float) size.x) / (isPortrait ? HwPCMultiWindowPolicy.mPortraitRatio : HwPCMultiWindowPolicy.mLandscapeRatio));
        }
        if (height > size.y) {
            height = size.y;
            width = (int) ((isPortrait ? HwPCMultiWindowPolicy.mPortraitRatio : HwPCMultiWindowPolicy.mLandscapeRatio) * ((float) size.y));
        }
        out.set(HwPCMultiWindowPolicy.mWindowMarginLeft, HwPCMultiWindowPolicy.mWindowMarginTop, HwPCMultiWindowPolicy.mWindowMarginLeft + width, HwPCMultiWindowPolicy.mWindowMarginTop + height);
    }

    private boolean isNeedDelay(TaskRecord record) {
        if (record == null || record.mRootActivityInfo == null) {
            return false;
        }
        String pkgName = record.mRootActivityInfo.packageName;
        if (!TextUtils.isEmpty(pkgName) && this.mNeedDelayList.contains(pkgName)) {
            return true;
        }
        return false;
    }

    public void updateTaskByRequestedOrientation(TaskRecord record, int requestedOrientation) {
        if (isNeedDelay(record)) {
            if (requestedOrientation != 0) {
                if (!(this.mLastRequestedOrientation == 0 || this.mLastRequestedOrientation == requestedOrientation)) {
                    this.mFixOrientationChanged = true;
                }
                this.mLastRequestedOrientation = requestedOrientation;
            }
            Message msg = Message.obtain();
            this.mHandler.removeMessages(1);
            msg.what = 1;
            msg.arg1 = requestedOrientation;
            msg.arg2 = record.taskId;
            this.mHandler.sendMessageDelayed(msg, 200);
            return;
        }
        this.mFixOrientationChanged = false;
        this.mLastRequestedOrientation = 0;
        updateTaskByRequestedOrientationInternal(record, requestedOrientation);
    }

    public void updateTaskByRequestedOrientationInternal(TaskRecord record, int customRequestedOrientation) {
        if (customRequestedOrientation != 0) {
            if ((HwPCMultiWindowCompatibility.getWindowStateLayout(record.mWindowState) == 1 ? 1 : 2) != customRequestedOrientation || ((this.mFixOrientationChanged && customRequestedOrientation == 2) || this.mUpdateTaskSet.contains(Integer.valueOf(record.taskId)))) {
                this.mFixOrientationChanged = false;
                this.mLastRequestedOrientation = 0;
                record.mNextWindowState = getWindowStateByRequestOrientation(customRequestedOrientation, record.getTopActivity() == null ? "" : record.getTopActivity().packageName);
                HwPCUtils.log(TAG, "updateTaskByRequestedOrientation-pre: " + Integer.toHexString(record.mWindowState) + " to " + Integer.toHexString(record.mNextWindowState));
                if (record.mWindowState != record.mNextWindowState || this.mUpdateTaskSet.contains(Integer.valueOf(record.taskId))) {
                    String str;
                    Rect rect = getWindowBoundsByDefault(record.mNextWindowState);
                    if (!(record.mBounds == null || (record.mBounds.isEmpty() ^ 1) == 0 || !isRectHasWindowSize(rect))) {
                        rect.offsetTo(record.mBounds.left, record.mBounds.top);
                    }
                    rect = adjustBounds(rect, record.taskId);
                    if (record instanceof HwTaskRecord) {
                        ((HwTaskRecord) record).mSaveBounds = false;
                    }
                    String str2 = TAG;
                    StringBuilder append = new StringBuilder().append("updateTaskByRequestedOrientation: ").append(Integer.toHexString(record.mNextWindowState)).append(LogHelper.SEPARATOR);
                    if (rect == null) {
                        str = "null";
                    } else {
                        str = rect.toShortString() + " (" + rect.width() + ", " + rect.height() + ")";
                    }
                    HwPCUtils.log(str2, append.append(str).toString());
                    resizeTaskFromPC(record, rect);
                }
                this.mUpdateTaskSet.remove(Integer.valueOf(record.taskId));
            }
        }
    }

    void resizeTaskFromPC(TaskRecord record, Rect rect) {
        record.resize(rect, 3, true, false);
    }

    private Display getExtDisplay(boolean isInternal) {
        DisplayManager displayManager = null;
        if (this.mService.mWindowManager instanceof HwWindowManagerService) {
            displayManager = ((HwWindowManagerService) this.mService.mWindowManager).getDisplayManager();
        }
        if (displayManager == null) {
            return null;
        }
        if (isInternal) {
            return displayManager.getDisplay(0);
        }
        for (Display d : displayManager.getDisplays()) {
            if (HwPCUtils.isValidExtDisplayId(d.getDisplayId())) {
                return d;
            }
        }
        return null;
    }

    private Point getExtDisplaySize() {
        Display display = getExtDisplay(false);
        if (!(display == null || display.getDisplayId() == this.mCurDisplayId)) {
            this.mNonDecorScreenSize.x = 0;
            this.mNonDecorScreenSize.y = 0;
            this.mCurDisplayId = display.getDisplayId();
        }
        if ((this.mNonDecorScreenSize.x == 0 || this.mNonDecorScreenSize.y == 0) && display != null && (this.mService.mWindowManager instanceof HwWindowManagerService)) {
            WindowManagerPolicy policy = ((HwWindowManagerService) this.mService.mWindowManager).getPolicy();
            Point fullScreenSize = new Point();
            display.getRealSize(fullScreenSize);
            if (policy != null) {
                this.mNonDecorScreenSize.x = policy.getNonDecorDisplayWidth(fullScreenSize.x, fullScreenSize.y, 0, 0, display.getDisplayId());
                this.mNonDecorScreenSize.y = policy.getNonDecorDisplayHeight(fullScreenSize.x, fullScreenSize.y, 0, 0, display.getDisplayId());
                this.mDecorHeight = fullScreenSize.y - this.mNonDecorScreenSize.y;
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
        Point size = getExtDisplaySize();
        return new Rect(0, 0, size.x, size.y);
    }

    public boolean isSupportResize(TaskRecord record, boolean isFullscreen, boolean isMaximized) {
        if (record == null) {
            return false;
        }
        if (isFullscreen) {
            return HwPCMultiWindowCompatibility.isFullscreenable(record.mWindowState);
        }
        if (isMaximized) {
            return HwPCMultiWindowCompatibility.isMaximizeable(record.mWindowState);
        }
        return ActivityInfo.isResizeableMode(record.mResizeMode);
    }

    private int getFinalWidth(WindowLayout windowLayout) {
        int width = 0;
        if (windowLayout.width > 0) {
            width = computeExtWidth(windowLayout.width);
        }
        if (windowLayout.widthFraction > 0.0f) {
            return (int) (((float) getExtDisplaySize().x) * windowLayout.widthFraction);
        }
        return width;
    }

    private int getFinalHeight(WindowLayout windowLayout) {
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
        int internalDPI = 1;
        int externalDPI = 1;
        DisplayInfo info = new DisplayInfo();
        Display dis = getExtDisplay(true);
        if (dis != null) {
            dis.getDisplayInfo(info);
            internalDPI = info.logicalDensityDpi;
        }
        dis = getExtDisplay(false);
        if (dis != null) {
            dis.getDisplayInfo(info);
            externalDPI = info.logicalDensityDpi;
        }
        return (val * externalDPI) / internalDPI;
    }

    public boolean isSpecialVideo(String pkgName) {
        return this.mSpecialVideosList.contains(pkgName);
    }

    public void setForceUpdateTask(int taskId) {
        this.mUpdateTaskSet.add(Integer.valueOf(taskId));
    }
}
