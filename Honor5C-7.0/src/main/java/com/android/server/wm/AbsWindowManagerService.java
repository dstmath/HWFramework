package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.IWindowManager.Stub;

public abstract class AbsWindowManagerService extends Stub {
    public static final int TOP_LAYER = 400000;
    protected static boolean mUsingHwNavibar;
    Configuration mCurNaviConfiguration;
    protected boolean mIgnoreFrozen;
    public int mLazyModeOn;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.AbsWindowManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.AbsWindowManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.AbsWindowManagerService.<clinit>():void");
    }

    public AbsWindowManagerService() {
        this.mIgnoreFrozen = false;
    }

    public int getLazyMode() {
        return 0;
    }

    public void setLazyMode(int lazyMode) {
    }

    protected void setCropOnSingleHandMode(int singleHandleMode, boolean isMultiWindowApp, int dw, int dh, Rect crop) {
    }

    protected void hwProcessOnMatrix(int rotation, int width, int height, Rect frame, Matrix outMatrix) {
    }

    public boolean isCoverOpen() {
        return true;
    }

    public void setCoverManagerState(boolean isCoverOpen) {
    }

    public void freezeOrThawRotation(int rotation) {
    }

    protected void sendUpdateAppOpsState() {
    }

    protected void setAppOpHideHook(WindowState win, boolean visible) {
    }

    protected void setAppOpVisibilityLwHook(WindowState win, int mode) {
    }

    protected void setVisibleFromParent(WindowState win) {
    }

    public void setNaviBarFlag() {
    }

    public void setFocusedAppForNavi(IBinder token) {
    }

    protected void updateInputImmersiveMode() {
    }

    public void reevaluateStatusBarSize(boolean layoutNaviBar) {
    }

    public Configuration getCurNaviConfiguration() {
        return null;
    }

    protected void addWindowReport(WindowState win, int mode) {
    }

    protected void removeWindowReport(WindowState win) {
    }

    protected void updateAppOpsStateReport(int ops, String packageName) {
    }

    public int getNsdWindowInfo(IBinder token) {
        return 0;
    }

    public String getNsdWindowTitle(IBinder token) {
        return null;
    }

    protected void checkKeyguardDismissDoneLocked() {
    }

    public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) {
    }

    public void updateResourceConfiguration(int displayId, int density, int width, int height) {
    }

    public boolean isSplitMode() {
        return false;
    }

    public void setSplittable(boolean splittable) {
    }
}
