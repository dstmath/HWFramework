package com.android.server.wm;

import android.common.HwFrameworkFactory;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.iawareperf.UniPerf;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.util.ArrayUtils;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.huawei.pgmng.log.LogPower;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

class RootWindowContainer extends WindowContainer<DisplayContent> {
    private static final int SET_SCREEN_BRIGHTNESS_OVERRIDE = 1;
    private static final int SET_USER_ACTIVITY_TIMEOUT = 2;
    private static final String TAG = "WindowManager";
    private static final Consumer<WindowState> sRemoveReplacedWindowsConsumer = $$Lambda$RootWindowContainer$Vvv8jzH2oSE9eakZwTuKd5NpsU.INSTANCE;
    private float mAppBrightnessLast = -1.0f;
    String mAppBrightnessPackageName;
    private String mAppBrightnessPackageNameLast = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    private final Consumer<WindowState> mCloseSystemDialogsConsumer = new Consumer() {
        public final void accept(Object obj) {
            RootWindowContainer.lambda$new$0(RootWindowContainer.this, (WindowState) obj);
        }
    };
    private String mCloseSystemDialogsReason;
    private HwServiceFactory.IDisplayEffectMonitor mDisplayEffectMonitor;
    private final SurfaceControl.Transaction mDisplayTransaction = new SurfaceControl.Transaction();
    private final Handler mHandler;
    private Session mHoldScreen = null;
    WindowState mHoldScreenWindow = null;
    private Object mLastWindowFreezeSource = null;
    private boolean mObscureApplicationContentOnSecondaryDisplays = false;
    WindowState mObscuringWindow = null;
    boolean mOrientationChangeComplete = true;
    private float mScreenBrightness = -1.0f;
    private boolean mSustainedPerformanceModeCurrent = false;
    private boolean mSustainedPerformanceModeEnabled = false;
    private final ArrayList<Integer> mTmpStackIds = new ArrayList<>();
    private final ArrayList<TaskStack> mTmpStackList = new ArrayList<>();
    private boolean mUpdateRotation = false;
    private long mUserActivityTimeout = -1;
    private IVRSystemServiceManager mVrMananger;
    boolean mWallpaperActionPending = false;
    final WallpaperController mWallpaperController;
    private boolean mWallpaperForceHidingChanged = false;
    boolean mWallpaperMayChange = false;

    private final class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RootWindowContainer.this.mService.mPowerManagerInternal.setScreenBrightnessOverrideFromWindowManager(msg.arg1);
                    return;
                case 2:
                    RootWindowContainer.this.mService.mPowerManagerInternal.setUserActivityTimeoutOverrideFromWindowManager(((Long) msg.obj).longValue());
                    return;
                default:
                    return;
            }
        }
    }

    public static /* synthetic */ void lambda$new$0(RootWindowContainer rootWindowContainer, WindowState w) {
        if (w.mHasSurface) {
            try {
                w.mClient.closeSystemDialogs(rootWindowContainer.mCloseSystemDialogsReason);
            } catch (RemoteException e) {
            }
        }
    }

    static /* synthetic */ void lambda$static$1(WindowState w) {
        AppWindowToken aToken = w.mAppToken;
        if (aToken != null) {
            aToken.removeReplacedWindowIfNeeded(w);
        }
    }

    private void sendBrightnessToMonitor(float brightness, String packageName) {
        if (this.mDisplayEffectMonitor != null && packageName != null) {
            if (((double) Math.abs(brightness - this.mAppBrightnessLast)) > 1.0E-7d || !this.mAppBrightnessPackageNameLast.equals(packageName)) {
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put("paramType", "windowManagerBrightness");
                params.put("brightness", Integer.valueOf(toBrightnessOverride(brightness)));
                params.put("packageName", packageName);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
                this.mAppBrightnessLast = brightness;
                this.mAppBrightnessPackageNameLast = packageName;
            }
        }
    }

    RootWindowContainer(WindowManagerService service) {
        super(service);
        this.mHandler = new MyHandler(service.mH.getLooper());
        this.mWallpaperController = new WallpaperController(this.mService);
        this.mDisplayEffectMonitor = HwServiceFactory.getDisplayEffectMonitor(this.mService.mContext);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "HwServiceFactory getDisplayEffectMonitor failed!");
        }
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    }

    /* access modifiers changed from: package-private */
    public WindowState computeFocusedWindow() {
        DisplayContent dc;
        if (HwPCUtils.isPcCastModeInServer() || this.mVrMananger.isVRDeviceConnected()) {
            if (this.mVrMananger.isVRDeviceConnected()) {
                dc = getDisplayContent(this.mVrMananger.getVRDisplayID());
                if (this.mVrMananger.isVirtualScreenMode()) {
                    dc = getDisplayContent(0);
                }
            } else {
                dc = getDisplayContent(this.mService.getFocusedDisplayId());
            }
            if (dc != null) {
                WindowState win = dc.findFocusedWindow();
                if (win != null) {
                    return win;
                }
            }
        }
        boolean forceDefaultDisplay = this.mService.isKeyguardShowingAndNotOccluded();
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent dc2 = (DisplayContent) this.mChildren.get(i);
            WindowState win2 = dc2.findFocusedWindow();
            if (win2 != null) {
                if (!forceDefaultDisplay || dc2.isDefaultDisplay) {
                    return win2;
                }
                EventLog.writeEvent(1397638484, new Object[]{"71786287", Integer.valueOf(win2.mOwnerUid), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS});
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void getDisplaysInFocusOrder(SparseIntArray displaysInFocusOrder) {
        displaysInFocusOrder.clear();
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            DisplayContent displayContent = (DisplayContent) this.mChildren.get(i);
            if (!displayContent.isRemovalDeferred()) {
                displaysInFocusOrder.put(i, displayContent.getDisplayId());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getDisplayContent(int displayId) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent current = (DisplayContent) this.mChildren.get(i);
            if (current.getDisplayId() == displayId) {
                return current;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent createDisplayContent(Display display, DisplayWindowController controller) {
        int displayId = display.getDisplayId();
        DisplayContent existing = getDisplayContent(displayId);
        if (existing != null) {
            existing.setController(controller);
            return existing;
        }
        DisplayContent dc = HwServiceFactory.createDisplayContent(display, this.mService, this.mWallpaperController, controller);
        if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
            Slog.v(TAG, "Adding display=" + display);
        }
        DisplayInfo displayInfo = dc.getDisplayInfo();
        Rect rect = new Rect();
        this.mService.mDisplaySettings.getOverscanLocked(displayInfo.name, displayInfo.uniqueId, rect);
        displayInfo.overscanLeft = rect.left;
        displayInfo.overscanTop = rect.top;
        displayInfo.overscanRight = rect.right;
        displayInfo.overscanBottom = rect.bottom;
        if (this.mService.mDisplayManagerInternal != null) {
            this.mService.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayId, displayInfo);
            dc.configureDisplayPolicy();
            if (this.mService.canDispatchPointerEvents()) {
                if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                    Slog.d(TAG, "Registering PointerEventListener for DisplayId: " + displayId);
                }
                dc.mTapDetector = new TaskTapPointerEventListener(this.mService, dc);
                this.mService.registerPointerEventListener(dc.mTapDetector);
                if (displayId == 0) {
                    this.mService.registerPointerEventListener(this.mService.mMousePositionTracker);
                }
            }
            boolean z = true;
            boolean displayInfoType = displayInfo.type == 2;
            if (HwPCUtils.isWirelessProjectionEnabled()) {
                if (!(displayInfo.type == 2 || displayInfo.type == 3)) {
                    z = false;
                }
                displayInfoType = z;
            }
            if (HwPCUtils.enabled() && displayId != -1 && displayId != 0 && ((displayInfoType || (((displayInfo.type == 5 || displayInfo.type == 4) && SystemProperties.getBoolean("hw_pc_support_overlay", false)) || (displayInfo.type == 5 && ("com.hpplay.happycast".equals(displayInfo.ownerPackageName) || "com.huawei.works".equals(displayInfo.ownerPackageName))))) && this.mService.canDispatchExternalPointerEvents())) {
                dc.mTapDetector = new TaskTapPointerEventListener(this.mService, dc);
                this.mService.registerExternalPointerEventListener(dc.mTapDetector);
                try {
                    this.mService.registerExternalPointerEventListener(this.mService.mMousePositionTracker);
                } catch (Exception e) {
                    Slog.w(TAG, "register external pointer event listener", e);
                }
            }
        }
        return dc;
    }

    /* access modifiers changed from: package-private */
    public boolean isLayoutNeeded() {
        int numDisplays = this.mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            if (((DisplayContent) this.mChildren.get(displayNdx)).isLayoutNeeded()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void getWindowsByName(ArrayList<WindowState> output, String name) {
        int objectId = 0;
        try {
            objectId = Integer.parseInt(name, 16);
            name = null;
        } catch (RuntimeException e) {
        }
        getWindowsByName(output, name, objectId);
    }

    private void getWindowsByName(ArrayList<WindowState> output, String name, int objectId) {
        forAllWindows((Consumer<WindowState>) new Consumer(name, output, objectId) {
            private final /* synthetic */ String f$0;
            private final /* synthetic */ ArrayList f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                RootWindowContainer.lambda$getWindowsByName$2(this.f$0, this.f$1, this.f$2, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$getWindowsByName$2(String name, ArrayList output, int objectId, WindowState w) {
        if (name != null) {
            if (w.mAttrs.getTitle().toString().contains(name)) {
                output.add(w);
            }
        } else if (System.identityHashCode(w) == objectId) {
            output.add(w);
        }
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getAppWindowToken(IBinder binder) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken atoken = ((DisplayContent) this.mChildren.get(i)).getAppWindowToken(binder);
            if (atoken != null) {
                return atoken;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getWindowTokenDisplay(WindowToken token) {
        if (token == null) {
            return null;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent dc = (DisplayContent) this.mChildren.get(i);
            if (dc.getWindowToken(token.token) == token) {
                return dc;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int[] setDisplayOverrideConfigurationIfNeeded(Configuration newConfiguration, int displayId) {
        DisplayContent displayContent = getDisplayContent(displayId);
        if (displayContent != null) {
            int[] iArr = null;
            if (!(displayContent.getOverrideConfiguration().diff(newConfiguration) != 0)) {
                Slog.i(TAG, "Do not change display override configuration.");
                return null;
            }
            displayContent.onOverrideConfigurationChanged(newConfiguration);
            this.mTmpStackList.clear();
            if (displayId == 0) {
                setGlobalConfigurationIfNeeded(newConfiguration, this.mTmpStackList);
            } else {
                updateStackBoundsAfterConfigChange(displayId, this.mTmpStackList);
            }
            this.mTmpStackIds.clear();
            int stackCount = this.mTmpStackList.size();
            for (int i = 0; i < stackCount; i++) {
                TaskStack stack = this.mTmpStackList.get(i);
                if (!stack.mDeferRemoval) {
                    this.mTmpStackIds.add(Integer.valueOf(stack.mStackId));
                }
            }
            if (!this.mTmpStackIds.isEmpty()) {
                iArr = ArrayUtils.convertToIntArray(this.mTmpStackIds);
            }
            return iArr;
        }
        throw new IllegalArgumentException("Display not found for id: " + displayId);
    }

    private void setGlobalConfigurationIfNeeded(Configuration newConfiguration, List<TaskStack> changedStacks) {
        if (getConfiguration().diff(newConfiguration) != 0) {
            onConfigurationChanged(newConfiguration);
            updateStackBoundsAfterConfigChange(changedStacks);
        }
    }

    public void onConfigurationChanged(Configuration newParentConfig) {
        prepareFreezingTaskBounds();
        super.onConfigurationChanged(newParentConfig);
        this.mService.mPolicy.onConfigurationChanged();
    }

    private void updateStackBoundsAfterConfigChange(List<TaskStack> changedStacks) {
        int numDisplays = this.mChildren.size();
        for (int i = 0; i < numDisplays; i++) {
            ((DisplayContent) this.mChildren.get(i)).updateStackBoundsAfterConfigChange(changedStacks);
        }
    }

    private void updateStackBoundsAfterConfigChange(int displayId, List<TaskStack> changedStacks) {
        getDisplayContent(displayId).updateStackBoundsAfterConfigChange(changedStacks);
    }

    private void prepareFreezingTaskBounds() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((DisplayContent) this.mChildren.get(i)).prepareFreezingTaskBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack(int windowingMode, int activityType) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            TaskStack stack = ((DisplayContent) this.mChildren.get(i)).getStack(windowingMode, activityType);
            if (stack != null) {
                return stack;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setSecureSurfaceState(int userId, boolean disabled) {
        forAllWindows((Consumer<WindowState>) new Consumer(userId, disabled) {
            private final /* synthetic */ int f$0;
            private final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                RootWindowContainer.lambda$setSecureSurfaceState$3(this.f$0, this.f$1, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$setSecureSurfaceState$3(int userId, boolean disabled, WindowState w) {
        if (w.mHasSurface && userId == UserHandle.getUserId(w.mOwnerUid)) {
            w.mWinAnimator.setSecureLocked(disabled);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateHiddenWhileSuspendedState(ArraySet<String> packages, boolean suspended) {
        forAllWindows((Consumer<WindowState>) new Consumer(packages, suspended) {
            private final /* synthetic */ ArraySet f$0;
            private final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                RootWindowContainer.lambda$updateHiddenWhileSuspendedState$4(this.f$0, this.f$1, (WindowState) obj);
            }
        }, false);
    }

    static /* synthetic */ void lambda$updateHiddenWhileSuspendedState$4(ArraySet packages, boolean suspended, WindowState w) {
        if (packages.contains(w.getOwningPackage())) {
            w.setHiddenWhileSuspended(suspended);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateAppOpsState() {
        forAllWindows((Consumer<WindowState>) $$Lambda$RootWindowContainer$0aCEx04eIvMHmZVtI4ucsiK5s9I.INSTANCE, false);
    }

    static /* synthetic */ boolean lambda$canShowStrictModeViolation$6(int pid, WindowState w) {
        return w.mSession.mPid == pid && w.isVisibleLw();
    }

    /* access modifiers changed from: package-private */
    public boolean canShowStrictModeViolation(int pid) {
        return getWindow(
        /*  JADX ERROR: Method code generation error
            jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: RETURN  (wrap: boolean
              ?: TERNARYnull = true, false) in method: com.android.server.wm.RootWindowContainer.canShowStrictModeViolation(int):boolean, dex: services_classes.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
            	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
            	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
            	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
            	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
            	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
            	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
            	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
            	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
            	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: TERNARYnull = true, false in method: com.android.server.wm.RootWindowContainer.canShowStrictModeViolation(int):boolean, dex: services_classes.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:303)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
            	... 17 more
            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0005: INVOKE  (r0v1 'win' com.android.server.wm.WindowState) = (r2v0 'this' com.android.server.wm.RootWindowContainer A[THIS]), (wrap: com.android.server.wm.-$$Lambda$RootWindowContainer$ZTXupc1zKRWZgWpo-r3so3blHoI
              0x0002: CONSTRUCTOR  (r0v0 com.android.server.wm.-$$Lambda$RootWindowContainer$ZTXupc1zKRWZgWpo-r3so3blHoI) = (r3v0 'pid' int) com.android.server.wm.-$$Lambda$RootWindowContainer$ZTXupc1zKRWZgWpo-r3so3blHoI.<init>(int):void CONSTRUCTOR) com.android.server.wm.RootWindowContainer.getWindow(java.util.function.Predicate):com.android.server.wm.WindowState type: VIRTUAL in method: com.android.server.wm.RootWindowContainer.canShowStrictModeViolation(int):boolean, dex: services_classes.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
            	at jadx.core.codegen.ConditionGen.addCompare(ConditionGen.java:129)
            	at jadx.core.codegen.ConditionGen.add(ConditionGen.java:57)
            	at jadx.core.codegen.ConditionGen.add(ConditionGen.java:46)
            	at jadx.core.codegen.InsnGen.makeTernary(InsnGen.java:935)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:465)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
            	... 20 more
            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0002: CONSTRUCTOR  (r0v0 com.android.server.wm.-$$Lambda$RootWindowContainer$ZTXupc1zKRWZgWpo-r3so3blHoI) = (r3v0 'pid' int) com.android.server.wm.-$$Lambda$RootWindowContainer$ZTXupc1zKRWZgWpo-r3so3blHoI.<init>(int):void CONSTRUCTOR in method: com.android.server.wm.RootWindowContainer.canShowStrictModeViolation(int):boolean, dex: services_classes.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
            	... 27 more
            Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.server.wm.-$$Lambda$RootWindowContainer$ZTXupc1zKRWZgWpo-r3so3blHoI, state: NOT_LOADED
            	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
            	... 32 more
            */
        /*
            this = this;
            com.android.server.wm.-$$Lambda$RootWindowContainer$ZTXupc1zKRWZgWpo-r3so3blHoI r0 = new com.android.server.wm.-$$Lambda$RootWindowContainer$ZTXupc1zKRWZgWpo-r3so3blHoI
            r0.<init>(r3)
            com.android.server.wm.WindowState r0 = r2.getWindow(r0)
            if (r0 == 0) goto L_0x000d
            r1 = 1
            goto L_0x000e
        L_0x000d:
            r1 = 0
        L_0x000e:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.RootWindowContainer.canShowStrictModeViolation(int):boolean");
    }

    /* access modifiers changed from: package-private */
    public void closeSystemDialogs(String reason) {
        this.mCloseSystemDialogsReason = reason;
        forAllWindows(this.mCloseSystemDialogsConsumer, false);
    }

    /* access modifiers changed from: package-private */
    public void removeReplacedWindows() {
        String str;
        this.mService.openSurfaceTransaction();
        try {
            forAllWindows(sRemoveReplacedWindowsConsumer, true);
        } finally {
            str = "removeReplacedWindows";
            this.mService.closeSurfaceTransaction(str);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPendingLayoutChanges(WindowAnimator animator) {
        boolean hasChanges = false;
        int count = this.mChildren.size();
        for (int i = 0; i < count; i++) {
            int pendingChanges = animator.getPendingLayoutChanges(((DisplayContent) this.mChildren.get(i)).getDisplayId());
            if ((pendingChanges & 4) != 0) {
                animator.mBulkUpdateParams |= 16;
            }
            if (pendingChanges != 0) {
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    /* access modifiers changed from: package-private */
    public boolean reclaimSomeSurfaceMemory(WindowStateAnimator winAnimator, String operation, boolean secure) {
        WindowStateAnimator windowStateAnimator = winAnimator;
        WindowSurfaceController surfaceController = windowStateAnimator.mSurfaceController;
        boolean leakedSurface = false;
        boolean killedApps = false;
        boolean z = false;
        EventLog.writeEvent(EventLogTags.WM_NO_SURFACE_MEMORY, new Object[]{windowStateAnimator.mWin.toString(), Integer.valueOf(windowStateAnimator.mSession.mPid), operation});
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            Slog.i(TAG, "Out of memory for surface!  Looking for leaks...");
            int numDisplays = this.mChildren.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                leakedSurface |= ((DisplayContent) this.mChildren.get(displayNdx)).destroyLeakedSurfaces();
            }
            if (!leakedSurface) {
                Slog.w(TAG, "No leaked surfaces; killing applications!");
                SparseIntArray pidCandidates = new SparseIntArray();
                int displayNdx2 = 0;
                while (true) {
                    int displayNdx3 = displayNdx2;
                    if (displayNdx3 >= numDisplays) {
                        break;
                    }
                    ((DisplayContent) this.mChildren.get(displayNdx3)).forAllWindows((Consumer<WindowState>) new Consumer(pidCandidates) {
                        private final /* synthetic */ SparseIntArray f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void accept(Object obj) {
                            RootWindowContainer.lambda$reclaimSomeSurfaceMemory$7(RootWindowContainer.this, this.f$1, (WindowState) obj);
                        }
                    }, z);
                    if (pidCandidates.size() > 0) {
                        int[] pids = new int[pidCandidates.size()];
                        for (int i = z; i < pids.length; i++) {
                            pids[i] = pidCandidates.keyAt(i);
                        }
                        try {
                            try {
                                if (this.mService.mActivityManager.killPids(pids, "Free memory", secure)) {
                                    killedApps = true;
                                }
                            } catch (RemoteException e) {
                            }
                        } catch (RemoteException e2) {
                            boolean z2 = secure;
                        }
                    } else {
                        boolean z3 = secure;
                    }
                    displayNdx2 = displayNdx3 + 1;
                    z = false;
                }
            }
            boolean z4 = secure;
            if (leakedSurface || killedApps) {
                try {
                    Slog.w(TAG, "Looks like we have reclaimed some memory, clearing surface for retry.");
                    if (surfaceController != null) {
                        winAnimator.destroySurface();
                        if (!(windowStateAnimator.mWin.mAppToken == null || windowStateAnimator.mWin.mAppToken.getController() == null)) {
                            windowStateAnimator.mWin.mAppToken.getController().removeStartingWindow();
                        }
                    }
                    try {
                        windowStateAnimator.mWin.mClient.dispatchGetNewSurface();
                    } catch (RemoteException e3) {
                    }
                } catch (Throwable th) {
                    th = th;
                    Binder.restoreCallingIdentity(callingIdentity);
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(callingIdentity);
            return leakedSurface || killedApps;
        } catch (Throwable th2) {
            th = th2;
            boolean z5 = secure;
            Binder.restoreCallingIdentity(callingIdentity);
            throw th;
        }
    }

    public static /* synthetic */ void lambda$reclaimSomeSurfaceMemory$7(RootWindowContainer rootWindowContainer, SparseIntArray pidCandidates, WindowState w) {
        if (!rootWindowContainer.mService.mForceRemoves.contains(w)) {
            WindowStateAnimator wsa = w.mWinAnimator;
            if (wsa.mSurfaceController != null) {
                pidCandidates.append(wsa.mSession.mPid, wsa.mSession.mPid);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void performSurfacePlacement(boolean recoveringMemory) {
        int i;
        boolean updateInputWindowsNeeded = false;
        boolean z = false;
        if (this.mService.mFocusMayChange) {
            this.mService.mFocusMayChange = false;
            updateInputWindowsNeeded = this.mService.updateFocusedWindowLocked(3, false);
        }
        boolean updateInputWindowsNeeded2 = updateInputWindowsNeeded;
        int numDisplays = this.mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ((DisplayContent) this.mChildren.get(displayNdx)).setExitingTokensHasVisible(false);
        }
        this.mHoldScreen = null;
        this.mScreenBrightness = -1.0f;
        this.mUserActivityTimeout = -1;
        this.mObscureApplicationContentOnSecondaryDisplays = false;
        this.mSustainedPerformanceModeCurrent = false;
        this.mService.mTransactionSequence++;
        DisplayContent defaultDisplay = this.mService.getDefaultDisplayContentLocked();
        DisplayInfo defaultInfo = defaultDisplay.getDisplayInfo();
        int defaultDw = defaultInfo.logicalWidth;
        int defaultDh = defaultInfo.logicalHeight;
        this.mService.openSurfaceTransaction();
        try {
            applySurfaceChangesTransaction(recoveringMemory, defaultDw, defaultDh);
        } catch (RuntimeException e) {
            RuntimeException runtimeException = e;
            Slog.wtf(TAG, "Unhandled exception in Window Manager", e);
        } catch (Throwable th) {
            th = th;
            DisplayInfo displayInfo = defaultInfo;
        }
        this.mService.closeSurfaceTransaction("performLayoutAndPlaceSurfaces");
        this.mService.mAnimator.executeAfterPrepareSurfacesRunnables();
        WindowSurfacePlacer surfacePlacer = this.mService.mWindowPlacerLocked;
        if (this.mService.mAppTransition.isReady()) {
            StringBuilder sb = new StringBuilder();
            WindowManagerService windowManagerService = this.mService;
            sb.append(windowManagerService.mAppTransitTrack);
            sb.append(" performsurface");
            windowManagerService.mAppTransitTrack = sb.toString();
            defaultDisplay.pendingLayoutChanges |= surfacePlacer.handleAppTransitionReadyLocked();
        }
        if (isAppAnimating() == 0 && this.mService.mAppTransition.isRunning()) {
            defaultDisplay.pendingLayoutChanges |= this.mService.handleAnimatingStoppedAndTransitionLocked();
        }
        RecentsAnimationController recentsAnimationController = this.mService.getRecentsAnimationController();
        if (recentsAnimationController != null) {
            recentsAnimationController.checkAnimationReady(this.mWallpaperController);
        }
        if (this.mWallpaperForceHidingChanged && defaultDisplay.pendingLayoutChanges == 0 && !this.mService.mAppTransition.isReady()) {
            defaultDisplay.pendingLayoutChanges |= 1;
        }
        this.mWallpaperForceHidingChanged = false;
        if (this.mWallpaperMayChange) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                Slog.v(TAG, "Wallpaper may change!  Adjusting");
            }
            defaultDisplay.pendingLayoutChanges |= 4;
        }
        if (this.mService.mFocusMayChange) {
            this.mService.mFocusMayChange = false;
            if (this.mService.updateFocusedWindowLocked(2, false)) {
                updateInputWindowsNeeded2 = true;
                defaultDisplay.pendingLayoutChanges |= 8;
            }
        }
        if (isLayoutNeeded()) {
            defaultDisplay.pendingLayoutChanges |= 1;
        }
        ArraySet<DisplayContent> touchExcludeRegionUpdateDisplays = handleResizingWindows();
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION && this.mService.mDisplayFrozen) {
            Slog.v(TAG, "With display frozen, orientationChangeComplete=" + this.mOrientationChangeComplete);
        }
        if (this.mOrientationChangeComplete) {
            if (this.mService.mDisplayFrozen) {
                if (this.mLastWindowFreezeSource != null) {
                    Jlog.d(59, Jlog.extractAppName(this.mLastWindowFreezeSource.toString()), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                } else {
                    Jlog.d(59, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                }
                if (this.mService.mIsPerfBoost) {
                    this.mService.mIsPerfBoost = false;
                    WindowSurfacePlacer windowSurfacePlacer = surfacePlacer;
                    UniPerf.getInstance().uniPerfEvent(4105, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, new int[]{-1});
                }
                LogPower.push(130, Integer.toString(this.mService.getDefaultDisplayRotation()));
            }
            if (this.mService.mWindowsFreezingScreen != 0) {
                this.mService.mWindowsFreezingScreen = 0;
                this.mService.mLastFinishedFreezeSource = this.mLastWindowFreezeSource;
                this.mService.mH.removeMessages(11);
            }
            if (this.mService.mDisplayFrozen) {
                Slog.i(TAG, "orientation change is complete, call stopFreezingDisplayLocked");
            }
            this.mService.stopFreezingDisplayLocked();
            if (!this.mService.mDisplayFrozen && !HwPCUtils.isPcCastModeInServer() && !this.mVrMananger.isVRDeviceConnected()) {
                reLayoutIfNeed();
            }
        }
        boolean wallpaperDestroyed = false;
        int i2 = this.mService.mDestroySurface.size();
        if (i2 > 0) {
            while (true) {
                i2--;
                WindowState win = this.mService.mDestroySurface.get(i2);
                win.mDestroying = z;
                if (this.mService.mInputMethodWindow == win) {
                    this.mService.setInputMethodWindowLocked(null);
                }
                if (win.getDisplayContent().mWallpaperController.isWallpaperTarget(win)) {
                    wallpaperDestroyed = true;
                }
                win.destroySurfaceUnchecked();
                win.mWinAnimator.destroyPreservedSurfaceLocked();
                if (i2 <= 0) {
                    break;
                }
                z = false;
            }
            this.mService.mDestroySurface.clear();
        }
        for (int displayNdx2 = 0; displayNdx2 < numDisplays; displayNdx2++) {
            ((DisplayContent) this.mChildren.get(displayNdx2)).removeExistingTokensIfPossible();
        }
        if (wallpaperDestroyed) {
            defaultDisplay.pendingLayoutChanges |= 4;
            defaultDisplay.setLayoutNeeded();
        }
        for (int displayNdx3 = 0; displayNdx3 < numDisplays; displayNdx3++) {
            DisplayContent displayContent = (DisplayContent) this.mChildren.get(displayNdx3);
            if (displayContent.pendingLayoutChanges != 0) {
                displayContent.setLayoutNeeded();
            }
        }
        this.mService.mInputMonitor.updateInputWindowsLw(true);
        this.mService.setHoldScreenLocked(this.mHoldScreen);
        if (!this.mService.mDisplayFrozen) {
            if (this.mScreenBrightness < 0.0f) {
                this.mAppBrightnessPackageName = PackageManagerService.PLATFORM_PACKAGE_NAME;
                sendBrightnessToMonitor(-1.0f, this.mAppBrightnessPackageName);
            } else {
                sendBrightnessToMonitor(this.mScreenBrightness, this.mAppBrightnessPackageName);
            }
            this.mHandler.obtainMessage(1, this.mScreenBrightness < 0.0f ? -1 : toBrightnessOverride(this.mScreenBrightness), 0).sendToTarget();
            DisplayInfo displayInfo2 = defaultInfo;
            this.mHandler.obtainMessage(2, Long.valueOf(this.mUserActivityTimeout)).sendToTarget();
        }
        if (this.mSustainedPerformanceModeCurrent != this.mSustainedPerformanceModeEnabled) {
            this.mSustainedPerformanceModeEnabled = this.mSustainedPerformanceModeCurrent;
            this.mService.mPowerManagerInternal.powerHint(6, this.mSustainedPerformanceModeEnabled ? 1 : 0);
        }
        if (this.mUpdateRotation) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d(TAG, "Performing post-rotate rotation");
            }
            int displayId = defaultDisplay.getDisplayId();
            if (defaultDisplay.updateRotationUnchecked()) {
                this.mService.mH.obtainMessage(18, Integer.valueOf(displayId)).sendToTarget();
            } else {
                this.mUpdateRotation = false;
            }
            DisplayContent vrDisplay = this.mService.mVr2dDisplayId != -1 ? getDisplayContent(this.mService.mVr2dDisplayId) : null;
            if (vrDisplay != null && vrDisplay.updateRotationUnchecked()) {
                this.mService.mH.obtainMessage(18, Integer.valueOf(this.mService.mVr2dDisplayId)).sendToTarget();
            }
        }
        if (this.mService.mWaitingForDrawnCallback != null || (this.mOrientationChangeComplete && !defaultDisplay.isLayoutNeeded() && !this.mUpdateRotation)) {
            this.mService.checkDrawnWindowsLocked();
        }
        int N = this.mService.mPendingRemove.size();
        if (N > 0) {
            if (this.mService.mPendingRemoveTmp.length < N) {
                this.mService.mPendingRemoveTmp = new WindowState[(N + 10)];
            }
            this.mService.mPendingRemove.toArray(this.mService.mPendingRemoveTmp);
            this.mService.mPendingRemove.clear();
            ArrayList<DisplayContent> displayList = new ArrayList<>();
            for (int i3 = 0; i3 < N; i3++) {
                WindowState w = this.mService.mPendingRemoveTmp[i3];
                w.removeImmediately();
                DisplayContent displayContent2 = w.getDisplayContent();
                if (displayContent2 != null && !displayList.contains(displayContent2)) {
                    displayList.add(displayContent2);
                }
            }
            i = 1;
            for (int j = displayList.size() - 1; j >= 0; j--) {
                displayList.get(j).assignWindowLayers(true);
            }
        } else {
            i = 1;
        }
        for (int displayNdx4 = this.mChildren.size() - i; displayNdx4 >= 0; displayNdx4--) {
            ((DisplayContent) this.mChildren.get(displayNdx4)).checkCompleteDeferredRemoval();
        }
        if (updateInputWindowsNeeded2) {
            this.mService.mInputMonitor.updateInputWindowsLw(false);
        }
        this.mService.setFocusTaskRegionLocked(null);
        if (touchExcludeRegionUpdateDisplays != null) {
            DisplayContent focusedDc = this.mService.mFocusedApp != null ? this.mService.mFocusedApp.getDisplayContent() : null;
            Iterator<DisplayContent> it = touchExcludeRegionUpdateDisplays.iterator();
            while (it.hasNext()) {
                DisplayContent dc = it.next();
                if (focusedDc != dc) {
                    dc.setTouchExcludeRegion(null);
                }
            }
        }
        this.mService.enableScreenIfNeededLocked();
        this.mService.scheduleAnimationLocked();
        return;
        this.mService.closeSurfaceTransaction("performLayoutAndPlaceSurfaces");
        throw th;
    }

    private void applySurfaceChangesTransaction(boolean recoveringMemory, int defaultDw, int defaultDh) {
        this.mHoldScreenWindow = null;
        this.mObscuringWindow = null;
        if (this.mService.mWatermark != null) {
            this.mService.mWatermark.positionSurface(defaultDw, defaultDh);
        }
        if (this.mService.mStrictModeFlash != null) {
            this.mService.mStrictModeFlash.positionSurface(defaultDw, defaultDh);
        }
        if (this.mService.mCircularDisplayMask != null) {
            this.mService.mCircularDisplayMask.positionSurface(defaultDw, defaultDh, this.mService.getDefaultDisplayRotation());
        }
        if (this.mService.mEmulatorDisplayOverlay != null) {
            this.mService.mEmulatorDisplayOverlay.positionSurface(defaultDw, defaultDh, this.mService.getDefaultDisplayRotation());
        }
        boolean focusDisplayed = false;
        int count = this.mChildren.size();
        for (int j = 0; j < count; j++) {
            focusDisplayed |= ((DisplayContent) this.mChildren.get(j)).applySurfaceChangesTransaction(recoveringMemory);
        }
        if (focusDisplayed) {
            this.mService.mH.sendEmptyMessage(3);
        }
        this.mService.mDisplayManagerInternal.performTraversal(this.mDisplayTransaction);
        SurfaceControl.mergeToGlobalTransaction(this.mDisplayTransaction);
    }

    private ArraySet<DisplayContent> handleResizingWindows() {
        ArraySet<DisplayContent> touchExcludeRegionUpdateSet = null;
        for (int i = this.mService.mResizingWindows.size() - 1; i >= 0; i--) {
            WindowState win = this.mService.mResizingWindows.get(i);
            if (!win.mAppFreezing) {
                win.reportResized();
                this.mService.mResizingWindows.remove(i);
                if (WindowManagerService.excludeWindowTypeFromTapOutTask(win.mAttrs.type)) {
                    DisplayContent dc = win.getDisplayContent();
                    if (touchExcludeRegionUpdateSet == null) {
                        touchExcludeRegionUpdateSet = new ArraySet<>();
                    }
                    touchExcludeRegionUpdateSet.add(dc);
                }
            }
        }
        return touchExcludeRegionUpdateSet;
    }

    /* access modifiers changed from: package-private */
    public boolean handleNotObscuredLocked(WindowState w, boolean obscured, boolean syswin) {
        WindowManager.LayoutParams attrs = w.mAttrs;
        int attrFlags = attrs.flags;
        boolean onScreen = w.isOnScreen();
        boolean canBeSeen = w.isDisplayedLw();
        int privateflags = attrs.privateFlags;
        boolean displayHasContent = false;
        if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON) {
            Slog.d("DebugKeepScreenOn", "handleNotObscuredLocked w: " + w + ", w.mHasSurface: " + w.mHasSurface + ", w.isOnScreen(): " + onScreen + ", w.isDisplayedLw(): " + w.isDisplayedLw() + ", w.mAttrs.userActivityTimeout: " + w.mAttrs.userActivityTimeout);
        }
        if (w.mHasSurface && onScreen && !syswin && w.mAttrs.userActivityTimeout >= 0 && this.mUserActivityTimeout < 0) {
            if ((w.mAttrs.privateFlags & 1024) == 0 || !this.mService.mDestroySurface.contains(w)) {
                this.mUserActivityTimeout = w.mAttrs.userActivityTimeout;
            } else {
                Slog.e(TAG, "do not set userActivityTimeout this time");
            }
            if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON) {
                Slog.d(TAG, "mUserActivityTimeout set to " + this.mUserActivityTimeout);
            }
        }
        if (w.mAttrs.type == 2000 && !canBeSeen) {
            canBeSeen = w.mHasSurface && this.mService.mPolicy.isKeyguardShowingOrOccluded();
            if (canBeSeen) {
                Slog.w(TAG, "reset canBeSeen for statusbar when keyguard on");
            }
        }
        if (w.mHasSurface && canBeSeen) {
            if ((attrFlags & 128) != 0) {
                if (!this.mService.mPolicy.isKeyguardShowingOrOccluded() || ((w.mAppToken != null && w.mAppToken.mShowWhenLocked) || (attrs.flags & DumpState.DUMP_FROZEN) != 0)) {
                    this.mHoldScreen = w.mSession;
                    this.mHoldScreenWindow = w;
                } else {
                    Slog.d("DebugKeepScreenOn", "handleNotObscuredLocked: keyguard isShowing app can not be seen but ShowWhenLocked " + w);
                }
            } else if (WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON && w == this.mService.mLastWakeLockHoldingWindow) {
                Slog.d("DebugKeepScreenOn", "handleNotObscuredLocked: " + w + " was holding screen wakelock but no longer has FLAG_KEEP_SCREEN_ON!!! called by" + Debug.getCallers(10));
            }
            if (!syswin && w.mAttrs.screenBrightness >= 0.0f && this.mScreenBrightness < 0.0f && w.isVisibleLw()) {
                this.mScreenBrightness = w.mAttrs.screenBrightness;
                this.mAppBrightnessPackageName = w.mAttrs.packageName;
            }
            int type = attrs.type;
            DisplayContent displayContent = w.getDisplayContent();
            if (displayContent != null && displayContent.isDefaultDisplay) {
                if (type == 2023 || (attrs.privateFlags & 1024) != 0) {
                    this.mObscureApplicationContentOnSecondaryDisplays = true;
                }
                displayHasContent = true;
            } else if (displayContent != null && (!this.mObscureApplicationContentOnSecondaryDisplays || (obscured && type == 2009))) {
                displayHasContent = true;
            }
            if ((262144 & privateflags) != 0) {
                this.mSustainedPerformanceModeCurrent = true;
            }
        }
        return displayHasContent;
    }

    /* access modifiers changed from: package-private */
    public boolean copyAnimToLayoutParams() {
        boolean doRequest = false;
        int bulkUpdateParams = this.mService.mAnimator.mBulkUpdateParams;
        if ((bulkUpdateParams & 1) != 0) {
            this.mUpdateRotation = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 2) != 0) {
            this.mWallpaperMayChange = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 4) != 0) {
            this.mWallpaperForceHidingChanged = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 8) == 0) {
            if (this.mService.mDisplayFrozen) {
                Flog.i(308, "Orientation change is not complete");
            }
            this.mOrientationChangeComplete = false;
        } else {
            if (this.mService.mDisplayFrozen) {
                Flog.i(308, "Orientation change is complete");
            }
            this.mOrientationChangeComplete = true;
            this.mLastWindowFreezeSource = this.mService.mAnimator.mLastWindowFreezeSource;
            if (this.mService.mWindowsFreezingScreen != 0) {
                doRequest = true;
            }
        }
        if ((bulkUpdateParams & 16) != 0) {
            this.mWallpaperActionPending = true;
        }
        return doRequest;
    }

    private static int toBrightnessOverride(float value) {
        return (int) (255.0f * value);
    }

    /* access modifiers changed from: package-private */
    public void dumpDisplayContents(PrintWriter pw) {
        pw.println("WINDOW MANAGER DISPLAY CONTENTS (dumpsys window displays)");
        if (this.mService.mDisplayReady) {
            int count = this.mChildren.size();
            for (int i = 0; i < count; i++) {
                ((DisplayContent) this.mChildren.get(i)).dump(pw, "  ", true);
            }
            return;
        }
        pw.println("  NO DISPLAY");
    }

    /* access modifiers changed from: package-private */
    public void dumpLayoutNeededDisplayIds(PrintWriter pw) {
        if (isLayoutNeeded()) {
            pw.print("  mLayoutNeeded on displays=");
            int count = this.mChildren.size();
            for (int displayNdx = 0; displayNdx < count; displayNdx++) {
                DisplayContent displayContent = (DisplayContent) this.mChildren.get(displayNdx);
                if (displayContent.isLayoutNeeded()) {
                    pw.print(displayContent.getDisplayId());
                }
            }
            pw.println();
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpWindowsNoHeader(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        forAllWindows((Consumer<WindowState>) new Consumer(windows, pw, new int[1], dumpAll) {
            private final /* synthetic */ ArrayList f$0;
            private final /* synthetic */ PrintWriter f$1;
            private final /* synthetic */ int[] f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void accept(Object obj) {
                RootWindowContainer.lambda$dumpWindowsNoHeader$8(this.f$0, this.f$1, this.f$2, this.f$3, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$dumpWindowsNoHeader$8(ArrayList windows, PrintWriter pw, int[] index, boolean dumpAll, WindowState w) {
        if (windows == null || windows.contains(w)) {
            pw.println("  Window #" + index[0] + " " + w + ":");
            w.dump(pw, "    ", dumpAll || windows != null);
            index[0] = index[0] + 1;
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpTokens(PrintWriter pw, boolean dumpAll) {
        pw.println("  All tokens:");
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((DisplayContent) this.mChildren.get(i)).dumpTokens(pw, dumpAll);
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean trim) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, trim);
        if (this.mService.mDisplayReady) {
            int count = this.mChildren.size();
            for (int i = 0; i < count; i++) {
                ((DisplayContent) this.mChildren.get(i)).writeToProto(proto, 2246267895810L, trim);
            }
        }
        if (!trim) {
            forAllWindows((Consumer<WindowState>) new Consumer(proto) {
                private final /* synthetic */ ProtoOutputStream f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj) {
                    ((WindowState) obj).writeIdentifierToProto(this.f$0, 2246267895811L);
                }
            }, true);
        }
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return "ROOT";
    }

    /* access modifiers changed from: package-private */
    public void scheduleAnimation() {
        this.mService.scheduleAnimationLocked();
    }

    private void reLayoutIfNeed() {
        int i = this.mService.mDeferRelayoutWindow.size();
        if (i > 0) {
            do {
                i--;
                WindowState win = this.mService.mDeferRelayoutWindow.get(i);
                if (win != null && win.isVisible()) {
                    Slog.d(TAG, "reLayoutIfNeed win:" + win);
                    win.mSeq = win.mSeq + 1;
                    try {
                        win.mClient.dispatchSystemUiVisibilityChanged(win.mSeq, win.mSystemUiVisibility, 0, 0);
                        continue;
                    } catch (RemoteException e) {
                        continue;
                    }
                }
            } while (i > 0);
            this.mService.mDeferRelayoutWindow.clear();
        }
    }
}
