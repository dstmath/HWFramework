package android.view;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.IWindowManager.Stub;
import android.view.View.AttachInfo;
import android.widget.TextView;
import com.huawei.android.hwaps.HwapsWrapper;
import com.huawei.android.hwaps.IEventAnalyzed;
import com.huawei.android.hwaps.IFpsController;
import com.huawei.hsm.permission.ConnectPermission;
import huawei.android.app.admin.HwDeviceAdminInfo;
import huawei.android.pfw.HwPFWStartupPackageList;
import huawei.android.view.HwMotionEvent;
import huawei.com.android.internal.widget.HwFragmentContainer;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HwNsdImpl implements IHwNsdImpl {
    private static final int APS_SUPPORT_2DSDR = 4096;
    private static final int BINDER_NSD_CURSOR_SET_BMP = 2000;
    private static final int BINDER_NSD_INTERRUPT_CMD = 2002;
    public static final int BINDER_NSD_QUERY_REFRESH_TIME = 2003;
    private static final int BINDER_NSD_QUERY_VISIBLE_LAYER = 2001;
    private static final int CHECK_ADVIEW_PARENT_NUM_MAX = 5;
    private static final int COMPAT_MODE_ENABLE_BIT = 32768;
    private static final int CONFIGTYPE_BLACKLIST = 7000;
    private static final int CONFIGTYPE_QUERYRESULTLIST = 7001;
    private static final int CONFIGTYPE_WHITELIST = 9998;
    private static final boolean HWDBG = true;
    private static final boolean HWLOGW_E = true;
    private static int Level = 0;
    private static final int NSD_CASE_LAUNCHER_IDLE = 1;
    private static final int NSD_CASE_NOTHING = 0;
    private static final int NSD_CMD_DRAW_AND_CONTROL_1 = 101;
    private static final int NSD_CMD_DRAW_AND_CONTROL_2 = 82;
    private static final int NSD_CMD_DRAW_AND_CONTROL_3 = 65;
    private static final int NSD_CMD_DRAW_AND_SAVE_1 = 101;
    private static final int NSD_CMD_DRAW_AND_SAVE_2 = 65;
    private static final int NSD_CMD_DRAW_AND_SAVE_3 = 82;
    private static final int NSD_CMD_NODRAW_AND_SAVE_1 = 67;
    private static final int NSD_CMD_NODRAW_AND_SAVE_2 = 101;
    private static final int NSD_CMD_NODRAW_AND_SAVE_3 = 135;
    private static final int NSD_CMD_NULL = 255;
    private static final int NSD_HIGH_SCREEN_HIGHT = 1920;
    private static final int NSD_INTERRUPT_CMD_READY_SCROLL = 104;
    private static final int NSD_INTERRUPT_CMD_RELEASE = 101;
    private static final int NSD_INTERRUPT_CMD_REQUIRE = 102;
    private static final int NSD_INTERRUPT_CMD_RESET = 103;
    private static final int NSD_INTERRUPT_CMD_STOP = 100;
    private static final int NSD_ISREADY_CONTROL_CASE = 2;
    private static final int NSD_ISREADY_SAVE_CASE = 1;
    private static final int NSD_ISREADY_SCROLL_CASE = 0;
    private static final int NSD_LOW_SCREEN_HIGHT = 960;
    private static final int NSD_MIDDLE_SCREEN_HIGHT = 1280;
    private static final int NSD_SHOULD_RELEASE = 0;
    private static final int NSD_SHOULD_RESET_DRAWN = 1;
    private static final int NSD_SHOULD_START_SCROLL = 2;
    private static final int NSD_SUPPORT_CURSOR = 256;
    private static final int NSD_SUPPORT_LAUNCHER = 512;
    private static final int NSD_SUPPORT_NONE_MASK = -4;
    private static final int NSD_SUPPORT_NOTHING = 0;
    private static final String TAG = "NSD";
    private static HwNsdImpl sInstance;
    private boolean mCanSaveBySf;
    private int mCheckSFControlCount;
    private int mCheckTimes;
    private int mCmdPosition;
    private Context mContext;
    private int mCtrlBySf;
    private int[] mCursorPosition;
    private IEventAnalyzed mEventAnalyzed;
    private int mFirstBlinkCount;
    private IFpsController mFpsController;
    private boolean mHasReadNsdSupportProperty;
    private boolean mHasReleaseNSD;
    private boolean mIsFirstEnterAPS;
    private int mIsNsdSupport;
    private long mLastBlinkTime;
    private int mLastX;
    private int mLastY;
    private String mLayerName;
    private Map<String, Integer> mMapCheckResult;
    private int mMaxSavaPageNo;
    private int mMinSavePageNo;
    private boolean mNeedCheckSFControl;
    private String mOldPkgName;
    private int mPageMustSaveFlag;
    private int mPageNo;
    private int mPageNum;
    private int mPagesSaveFlag;
    private int mRecheckFlag;
    private int mScreenWidth;
    private View mView;
    private IWindowManager mWindowManagerService;
    private int mZorder;
    private int nsdCaseNo;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.HwNsdImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.HwNsdImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.HwNsdImpl.<clinit>():void");
    }

    private boolean isSFReady(int r12) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0061 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r11 = this;
        r10 = 0;
        r9 = 1;
        r5 = 0;
        r6 = 0;
        r4 = 0;
        r0 = android.os.Parcel.obtain();
        r3 = android.os.Parcel.obtain();
        r7 = "SurfaceFlinger";	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r2 = android.os.ServiceManager.getService(r7);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        if (r2 == 0) goto L_0x0033;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
    L_0x0016:
        r7 = "android.ui.ISurfaceComposer";	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r0.writeInterfaceToken(r7);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r7 = 102; // 0x66 float:1.43E-43 double:5.04E-322;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r0.writeInt(r7);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r7 = 2002; // 0x7d2 float:2.805E-42 double:9.89E-321;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = 0;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r2.transact(r7, r0, r3, r8);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r5 = r3.readInt();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r6 = r3.readInt();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r4 = r3.readInt();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
    L_0x0033:
        if (r0 == 0) goto L_0x0038;
    L_0x0035:
        r0.recycle();
    L_0x0038:
        if (r3 == 0) goto L_0x003d;
    L_0x003a:
        r3.recycle();
    L_0x003d:
        switch(r12) {
            case 0: goto L_0x0073;
            case 1: goto L_0x0096;
            case 2: goto L_0x008a;
            default: goto L_0x0040;
        };
    L_0x0040:
        return r10;
    L_0x0041:
        r1 = move-exception;
        r7 = "NSD";	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8.<init>();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r9 = "isSFReady error : ";	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = r8.append(r9);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = r8.append(r1);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = r8.toString();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        android.util.Log.e(r7, r8);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        if (r0 == 0) goto L_0x0061;
    L_0x005e:
        r0.recycle();
    L_0x0061:
        if (r3 == 0) goto L_0x0066;
    L_0x0063:
        r3.recycle();
    L_0x0066:
        return r10;
    L_0x0067:
        r7 = move-exception;
        if (r0 == 0) goto L_0x006d;
    L_0x006a:
        r0.recycle();
    L_0x006d:
        if (r3 == 0) goto L_0x0072;
    L_0x006f:
        r3.recycle();
    L_0x0072:
        throw r7;
    L_0x0073:
        if (r9 != r5) goto L_0x0083;
    L_0x0075:
        r7 = r11.mPagesSaveFlag;
        if (r6 != r7) goto L_0x0083;
    L_0x0079:
        r7 = "NSD";
        r8 = "NSD_ISREADY_SCROLL_CASE  ok";
        android.util.Log.v(r7, r8);
        return r9;
    L_0x0083:
        r7 = r11.mPagesSaveFlag;
        if (r7 == r6) goto L_0x0040;
    L_0x0087:
        r11.mPagesSaveFlag = r6;
        return r10;
    L_0x008a:
        if (r9 != r4) goto L_0x0040;
    L_0x008c:
        r7 = "NSD";
        r8 = "NSD_ISREADY_CONTROL_CASE  ok";
        android.util.Log.v(r7, r8);
        return r9;
    L_0x0096:
        if (r9 != r5) goto L_0x0040;
    L_0x0098:
        r7 = "NSD";
        r8 = "NSD_ISREADY_SAVE_CASE  ok";
        android.util.Log.v(r7, r8);
        return r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.HwNsdImpl.isSFReady(int):boolean");
    }

    public void resetSaveNSD(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.HwNsdImpl.resetSaveNSD(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.HwNsdImpl.resetSaveNSD(int):void");
    }

    public HwNsdImpl() {
        this.mHasReadNsdSupportProperty = false;
        this.nsdCaseNo = NSD_SUPPORT_NOTHING;
        this.mOldPkgName = "";
        this.mMapCheckResult = new HashMap();
        this.mIsFirstEnterAPS = HWLOGW_E;
        this.mEventAnalyzed = null;
        this.mFpsController = null;
        this.mContext = null;
        this.mPageNo = NSD_CMD_NULL;
        this.mCanSaveBySf = false;
        this.mHasReleaseNSD = false;
        this.mNeedCheckSFControl = false;
        this.mCmdPosition = SystemProperties.getInt("sys.nsd.launchertop", -1);
        this.mScreenWidth = NSD_SUPPORT_NOTHING;
        this.mRecheckFlag = NSD_INTERRUPT_CMD_STOP;
        this.mCheckTimes = NSD_SUPPORT_NOTHING;
        this.mCheckSFControlCount = NSD_SUPPORT_NOTHING;
        this.mPageMustSaveFlag = NSD_SUPPORT_NOTHING;
        this.mWindowManagerService = Stub.asInterface(ServiceManager.getService(FreezeScreenScene.WINDOW_PARAM));
        this.mCursorPosition = new int[NSD_SHOULD_START_SCROLL];
        this.mLastBlinkTime = 0;
        this.mFirstBlinkCount = NSD_SUPPORT_NOTHING;
    }

    protected void HwNsdImpl() {
        Log.e(TAG, "nsd new HwNsdImpl ");
    }

    public static synchronized HwNsdImpl getDefault() {
        HwNsdImpl hwNsdImpl;
        synchronized (HwNsdImpl.class) {
            if (sInstance == null) {
                sInstance = new HwNsdImpl();
            }
            hwNsdImpl = sInstance;
        }
        return hwNsdImpl;
    }

    private void readNsdSupportProperty() {
        this.mIsNsdSupport = SystemProperties.getInt("sys.aps.support", NSD_SUPPORT_NOTHING);
        Log.v(TAG, "NSD readNsdSupportProperty  :  " + this.mIsNsdSupport);
        this.mHasReadNsdSupportProperty = HWLOGW_E;
    }

    public boolean checkIfSupportNsd() {
        if (!this.mHasReadNsdSupportProperty) {
            readNsdSupportProperty();
        }
        if (this.mIsNsdSupport != 0) {
            return HWLOGW_E;
        }
        return false;
    }

    public boolean checkIfNsdSupportLauncher() {
        if (this.mHasReleaseNSD || -1 == this.mCmdPosition) {
            return false;
        }
        if (!this.mHasReadNsdSupportProperty) {
            readNsdSupportProperty();
        }
        if (NSD_SUPPORT_LAUNCHER == (this.mIsNsdSupport & NSD_SUPPORT_LAUNCHER)) {
            return HWLOGW_E;
        }
        return false;
    }

    public boolean checkIfNsdSupportCursor() {
        this.mIsNsdSupport = SystemProperties.getInt("sys.aps.support", NSD_SUPPORT_NOTHING);
        if (NSD_SUPPORT_CURSOR == (this.mIsNsdSupport & NSD_SUPPORT_CURSOR)) {
            return HWLOGW_E;
        }
        return false;
    }

    public boolean isCase(View v) {
        if (this.mCtrlBySf < NSD_SHOULD_RESET_DRAWN) {
            return false;
        }
        this.mCheckTimes += NSD_SHOULD_RESET_DRAWN;
        if (this.mCheckTimes >= this.mRecheckFlag) {
            this.mCheckTimes = NSD_SUPPORT_NOTHING;
            this.mHasReadNsdSupportProperty = false;
        }
        if (isLauncherIdle(v)) {
            this.nsdCaseNo = NSD_SHOULD_RESET_DRAWN;
            return HWLOGW_E;
        }
        this.nsdCaseNo = NSD_SUPPORT_NOTHING;
        return false;
    }

    private boolean isLauncherIdle(View view) {
        if (view == null || !"com.huawei.android.launcher".equals(view.getContext().getPackageName())) {
            return false;
        }
        ViewGroup pV = getCaseVGInLauncherIdle(view);
        if (pV == null || !pV.getClass().getSimpleName().contains("Workspace")) {
            return false;
        }
        ViewGroup pViewGroup = pV;
        int i = NSD_SUPPORT_NOTHING;
        do {
            pV = getViewGroup(pViewGroup.getChildAt(i));
            i += NSD_SHOULD_RESET_DRAWN;
        } while (pV != null);
        this.mPageNum = i - 1;
        computeSavePageNo();
        return HWLOGW_E;
    }

    public boolean isNeedAppDraw() {
        if (this.mCtrlBySf < NSD_SHOULD_START_SCROLL) {
            return HWLOGW_E;
        }
        if (!this.mNeedCheckSFControl || isSFReady(NSD_SHOULD_START_SCROLL)) {
            this.mNeedCheckSFControl = false;
        } else {
            if (this.mCheckSFControlCount > 3) {
                Log.d(TAG, "isNeedAppDraw stop draw !");
                stopNsd();
                this.mNeedCheckSFControl = false;
            }
            this.mCheckSFControlCount += NSD_SHOULD_RESET_DRAWN;
        }
        return false;
    }

    public void startNsd(int cmd) {
        if (checkIfNsdSupportLauncher()) {
            switch (cmd) {
                case NSD_SHOULD_START_SCROLL /*2*/:
                    try {
                        if (!isSFReady(NSD_SUPPORT_NOTHING)) {
                            Log.d(TAG, "SF is not ready!");
                            break;
                        }
                        Log.v(TAG, "NSD_SHOULD_START_SCROLL");
                        if (this.mCtrlBySf == 0) {
                            this.mCtrlBySf = NSD_SHOULD_RESET_DRAWN;
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "startNsd error : " + e);
                        return;
                    }
                    break;
            }
        }
    }

    public void stopNsd() {
        if (this.mCtrlBySf >= NSD_SHOULD_RESET_DRAWN) {
            this.mCtrlBySf = NSD_SUPPORT_NOTHING;
            setCmdStopFlag(NSD_INTERRUPT_CMD_STOP);
            Log.v(TAG, "NSD stop scroll");
        }
        this.mNeedCheckSFControl = HWLOGW_E;
        this.mCheckSFControlCount = NSD_SUPPORT_NOTHING;
        this.mPageNo = NSD_CMD_NULL;
    }

    public void releaseNSD() {
        this.mCtrlBySf = NSD_SUPPORT_NOTHING;
        this.mHasReleaseNSD = HWLOGW_E;
        this.mIsNsdSupport &= -513;
        sendInterruptCmd(NSD_INTERRUPT_CMD_RELEASE);
        Log.w(TAG, "NSD releaseNSD");
    }

    public void adjNsd(String para) {
        setCmdAdjFlag(para);
    }

    public void enableNsdSave() {
        this.mCanSaveBySf = HWLOGW_E;
    }

    private ViewGroup getCaseVGInLauncherIdle(View view) {
        if (view == null) {
            return null;
        }
        try {
            ViewGroup pV = getViewGroup(view);
            if (pV == null) {
                return null;
            }
            pV = getViewGroup(pV.getChildAt(NSD_SUPPORT_NOTHING));
            if (pV == null) {
                return null;
            }
            pV = getViewGroup(pV.getChildAt(NSD_SHOULD_RESET_DRAWN));
            if (pV == null) {
                return null;
            }
            pV = getViewGroup(pV.getChildAt(NSD_SUPPORT_NOTHING));
            if (pV == null) {
                return null;
            }
            return pV;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean canSave(View view) {
        if (view == null || !this.mCanSaveBySf) {
            return false;
        }
        if (this.mScreenWidth == 0) {
            this.mScreenWidth = view.getResources().getDisplayMetrics().widthPixels;
        }
        switch (this.nsdCaseNo) {
            case NSD_SUPPORT_NOTHING /*0*/:
                return false;
            case NSD_SHOULD_RESET_DRAWN /*1*/:
                ViewGroup pV = getCaseVGInLauncherIdle(view);
                if (pV == null || pV.mScrollX % this.mScreenWidth != 0) {
                    return false;
                }
                this.mPageNo = Math.round(((float) pV.mScrollX) / ((float) this.mScreenWidth));
                return ((this.mPagesSaveFlag & (NSD_SHOULD_RESET_DRAWN << this.mPageNo)) != (NSD_SHOULD_RESET_DRAWN << this.mPageNo) && isSFReady(NSD_SHOULD_RESET_DRAWN)) ? HWLOGW_E : false;
            default:
                return false;
        }
    }

    private void computeSavePageNo() {
        if (this.mPageMustSaveFlag == 0) {
            switch (this.mPageNum) {
                case NSD_SHOULD_START_SCROLL /*2*/:
                case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                    this.mMinSavePageNo = NSD_SUPPORT_NOTHING;
                    this.mMaxSavaPageNo = this.mPageNum - 1;
                    break;
                case CHECK_ADVIEW_PARENT_NUM_MAX /*5*/:
                    this.mMinSavePageNo = NSD_SHOULD_RESET_DRAWN;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
                    this.mMinSavePageNo = NSD_SHOULD_RESET_DRAWN;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                case HwMotionEvent.TOOL_TYPE_FINGER_KNUCKLE /*7*/:
                    this.mMinSavePageNo = NSD_SHOULD_START_SCROLL;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                case HwMotionEvent.TOOL_TYPE_BEZEL /*8*/:
                    this.mMinSavePageNo = NSD_SHOULD_START_SCROLL;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                case HwDeviceAdminInfo.USES_POLICY_SET_MDM_EMAIL /*9*/:
                    this.mMinSavePageNo = 3;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                default:
                    this.mMaxSavaPageNo = NSD_SUPPORT_NOTHING;
                    this.mMinSavePageNo = NSD_SUPPORT_NOTHING;
                    break;
            }
            for (int index = this.mMinSavePageNo; index <= this.mMaxSavaPageNo; index += NSD_SHOULD_RESET_DRAWN) {
                this.mPageMustSaveFlag |= NSD_SHOULD_RESET_DRAWN << index;
            }
        }
    }

    private boolean setCmdSaveFlag(DisplayListCanvas cv) {
        if (cv == null || this.mPageNo < this.mMinSavePageNo || this.mPageNo > this.mMaxSavaPageNo || this.mMinSavePageNo == this.mMaxSavaPageNo) {
            return false;
        }
        Paint paint = new Paint();
        paint.setARGB(254, NSD_INTERRUPT_CMD_RELEASE, NSD_CMD_DRAW_AND_SAVE_2, NSD_CMD_DRAW_AND_SAVE_3);
        cv.drawLine((float) ((this.mPageNo * this.mScreenWidth) + NSD_SUPPORT_NOTHING), 0.0f, (float) ((this.mPageNo * this.mScreenWidth) + NSD_SHOULD_RESET_DRAWN), 0.0f, paint);
        paint.setARGB(NSD_CMD_NULL, NSD_SHOULD_RESET_DRAWN, this.mPageNo - this.mMinSavePageNo, this.mPageNum);
        cv.drawLine((float) ((this.mPageNo * this.mScreenWidth) + NSD_SHOULD_START_SCROLL), 0.0f, (float) ((this.mPageNo * this.mScreenWidth) + 3), 0.0f, paint);
        this.mPagesSaveFlag |= NSD_SHOULD_RESET_DRAWN << this.mPageNo;
        this.mCtrlBySf = NSD_SUPPORT_NOTHING;
        Log.v(TAG, "NSDAP : cmd save : screen [ " + this.mPageNo + " ]");
        return HWLOGW_E;
    }

    private ViewGroup getViewGroup(View view) {
        if (view == null) {
            return null;
        }
        ViewGroup retVG = null;
        if (view instanceof ViewGroup) {
            retVG = (ViewGroup) view;
        }
        return retVG;
    }

    private boolean canDrawBySf(View view) {
        switch (this.nsdCaseNo) {
            case NSD_SUPPORT_NOTHING /*0*/:
                return false;
            case NSD_SHOULD_RESET_DRAWN /*1*/:
                if (this.mPagesSaveFlag != this.mPageMustSaveFlag || this.mCtrlBySf == 0) {
                    return false;
                }
                ViewGroup pV = getCaseVGInLauncherIdle(view);
                if (pV == null) {
                    return false;
                }
                this.mCtrlBySf += NSD_SHOULD_RESET_DRAWN;
                if (this.mScreenWidth == 0) {
                    this.mScreenWidth = view.getResources().getDisplayMetrics().widthPixels;
                }
                this.mPageNo = Math.round(((float) pV.mScrollX) / ((float) this.mScreenWidth));
                return HWLOGW_E;
            default:
                return false;
        }
    }

    private boolean setCmdDrawFlag(DisplayListCanvas cv) {
        if (cv == null || this.mCtrlBySf > NSD_SHOULD_START_SCROLL) {
            return false;
        }
        this.mNeedCheckSFControl = HWLOGW_E;
        this.mCheckSFControlCount = NSD_SUPPORT_NOTHING;
        sendCmdReadyScrollFlag(NSD_INTERRUPT_CMD_READY_SCROLL);
        Log.e("yubo", "NSD send draw cmd time : " + SystemClock.uptimeMillis());
        Log.v(TAG, "NSDAP : cmd draw : screen [ " + this.mPageNo + " ]");
        return HWLOGW_E;
    }

    private void sendInterruptCmd(int cmdcode) {
        Parcel data = Parcel.obtain();
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInt(cmdcode);
                data.writeInt(this.mPagesSaveFlag);
                data.writeInt(this.mPageNo - this.mMinSavePageNo);
                data.writeInt(this.mPageNum);
                flinger.transact(BINDER_NSD_INTERRUPT_CMD, data, null, NSD_SUPPORT_NOTHING);
                Log.v(TAG, "sendInterruptCmd   cmdcode  : " + cmdcode);
            }
            if (data != null) {
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "sendInterruptCmd error : " + ex);
            if (data != null) {
                data.recycle();
            }
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
        }
    }

    private void setCmdStopFlag(int stopCmd) {
        sendInterruptCmd(stopCmd);
    }

    private void sendCmdReadyScrollFlag(int readyScrollCmd) {
        sendInterruptCmd(readyScrollCmd);
    }

    private boolean setCmdAdjFlag(String para) {
        if (para == null) {
            return false;
        }
        Log.v(TAG, "NSD setCmdAdjFlag");
        sendInterruptCmd(Integer.parseInt(para));
        return HWLOGW_E;
    }

    private void processCase(View view, DisplayListCanvas cv) {
        if (view != null && cv != null) {
            if (canSave(view)) {
                this.mCanSaveBySf = false;
                setCmdSaveFlag(cv);
                return;
            }
            this.mCanSaveBySf = false;
            if (canDrawBySf(view)) {
                setCmdDrawFlag(cv);
            }
        }
    }

    public void setView(View view) {
        this.mView = view;
    }

    public void processCase(DisplayListCanvas cv) {
        processCase(this.mView, cv);
    }

    public boolean isScreenHight(int hight) {
        if (NSD_HIGH_SCREEN_HIGHT == hight || NSD_MIDDLE_SCREEN_HIGHT == hight || NSD_LOW_SCREEN_HIGHT == hight) {
            return HWLOGW_E;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkAdBlock(View inView, String pkgName) {
        boolean retBlockStatus = false;
        int prop = SystemProperties.getInt("debug.aps.enable", NSD_SUPPORT_NOTHING);
        if ((prop != NSD_SHOULD_RESET_DRAWN && prop != NSD_SHOULD_START_SCROLL && prop != 9999) || inView == null) {
            return false;
        }
        String viewClsName = inView.getClass().getName();
        if (this.mMapCheckResult.containsKey(viewClsName)) {
            if (NSD_SHOULD_RESET_DRAWN == ((Integer) this.mMapCheckResult.get(viewClsName)).intValue()) {
                blockAdView(inView);
                retBlockStatus = HWLOGW_E;
            }
            return retBlockStatus;
        }
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        if (eventAnalyzed == null || !eventAnalyzed.isAdCheckEnable(pkgName)) {
            return false;
        }
        int nCount = NSD_SUPPORT_NOTHING;
        String clsName = "";
        ViewParent parent = null;
        while (true) {
            if (nCount == 0) {
                clsName = viewClsName;
                parent = inView.getParent();
            } else if (parent != null) {
                clsName = parent.getClass().getName();
                parent = parent.getParent();
            }
            int checkResult = eventAnalyzed.checkAd(clsName);
            if (checkResult > 0) {
                break;
            }
            nCount += NSD_SHOULD_RESET_DRAWN;
            if (parent != null && nCount < CHECK_ADVIEW_PARENT_NUM_MAX) {
            }
        }
        if (NSD_SHOULD_RESET_DRAWN == checkResult) {
            blockAdView(inView);
            retBlockStatus = HWLOGW_E;
            this.mMapCheckResult.put(viewClsName, Integer.valueOf(NSD_SHOULD_RESET_DRAWN));
        } else if (NSD_SHOULD_START_SCROLL == checkResult) {
            unBlockAdView(inView);
        } else {
            this.mMapCheckResult.put(viewClsName, Integer.valueOf(NSD_SUPPORT_NOTHING));
        }
        return retBlockStatus;
    }

    private void blockAdView(View inView) {
        if (inView.getVisibility() != 8) {
            inView.setVisibility(8);
            Log.i("AdCheck", "APS: blockAdView! ");
        }
    }

    private void unBlockAdView(View inView) {
        if (inView.getVisibility() == 8) {
            inView.setVisibility(NSD_SUPPORT_NOTHING);
            Log.i("AdCheck", "APS: unBlockAdView! ");
        }
    }

    public void drawBackground(TextView view, Canvas canvas) {
        if (view == null || canvas == null) {
            Log.w(TAG, "NSD drawBackground fail!");
            return;
        }
        ArrayList<View> mViewList = new ArrayList();
        mViewList.clear();
        mViewList.add(view);
        for (View parent = (View) view.getParent(); parent != null; parent = (View) parent.getParent()) {
            mViewList.add(parent);
            if (parent == view.getRootView()) {
                break;
            }
        }
        int[] locationOnScreen = new int[NSD_SHOULD_START_SCROLL];
        for (int i = mViewList.size() - 1; i >= 0; i--) {
            View v = (View) mViewList.get(i);
            if (v.getBackground() != null) {
                locationOnScreen[NSD_SHOULD_RESET_DRAWN] = NSD_SUPPORT_NOTHING;
                locationOnScreen[NSD_SUPPORT_NOTHING] = NSD_SUPPORT_NOTHING;
                v.getLocationOnScreen(locationOnScreen);
                canvas.save();
                canvas.translate((float) locationOnScreen[NSD_SUPPORT_NOTHING], (float) locationOnScreen[NSD_SHOULD_RESET_DRAWN]);
                v.getBackground().draw(canvas);
                canvas.restore();
            }
        }
    }

    public int getTextViewZOrderId(AttachInfo attachInfo) {
        int zOrder = -1;
        try {
            zOrder = this.mWindowManagerService.getNsdWindowInfo(getCurrentWindowToken(attachInfo));
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException is thrown when trying to get zid of this layer");
        }
        return zOrder;
    }

    public boolean isCursorCompleteVisible(TextView view, Rect cursorGlobalBounds, AttachInfo mAttachInfo) {
        if (view == null || cursorGlobalBounds == null || mAttachInfo == null || this.mWindowManagerService == null) {
            Log.w(TAG, "NSD isCursorCompleteVisible fail!");
            return false;
        }
        Rect mVisibleRect = new Rect();
        if (!view.getGlobalVisibleRect(mVisibleRect)) {
            return false;
        }
        mVisibleRect.left -= 10;
        if (!mVisibleRect.contains(cursorGlobalBounds)) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z;
        try {
            int cwinfo = this.mWindowManagerService.getNsdWindowInfo(getCurrentWindowToken(mAttachInfo));
            if (cwinfo == 0) {
                z = false;
                return z;
            }
            this.mLayerName = this.mWindowManagerService.getNsdWindowTitle(getCurrentWindowToken(mAttachInfo));
            if (this.mLayerName != null) {
                if (!this.mLayerName.isEmpty()) {
                    this.mZorder = cwinfo;
                    this.mFirstBlinkCount += NSD_SHOULD_RESET_DRAWN;
                    IBinder flinger = ServiceManager.getService("SurfaceFlinger");
                    if (flinger != null) {
                        data.writeInterfaceToken("android.ui.ISurfaceComposer");
                        flinger.transact(BINDER_NSD_QUERY_VISIBLE_LAYER, data, reply, NSD_SUPPORT_NOTHING);
                        long lastRefreshTime = reply.readLong();
                        long now = System.nanoTime() / 1000000;
                        if (now - lastRefreshTime >= 300) {
                            if (now - this.mLastBlinkTime <= 700) {
                                this.mLastBlinkTime = now;
                                if (this.mFirstBlinkCount <= NSD_SHOULD_START_SCROLL) {
                                    recycleParcel(data, reply);
                                    return false;
                                }
                                int count = reply.readInt();
                                boolean overlap = false;
                                boolean currentLayerIsVisible = false;
                                for (int i = NSD_SUPPORT_NOTHING; i < count; i += NSD_SHOULD_RESET_DRAWN) {
                                    int isVisible = reply.readInt();
                                    int layer = reply.readInt();
                                    int left = reply.readInt();
                                    int top = reply.readInt();
                                    int right = reply.readInt();
                                    int bottom = reply.readInt();
                                    if (isVisible != 0 && layer >= cwinfo) {
                                        if (layer == cwinfo) {
                                            currentLayerIsVisible = HWLOGW_E;
                                        } else {
                                            if (Rect.intersects(new Rect(left, top, right, bottom), cursorGlobalBounds)) {
                                                overlap = HWLOGW_E;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!currentLayerIsVisible) {
                                    Log.w(TAG, "current layer is not visible");
                                }
                                if (currentLayerIsVisible && !overlap) {
                                    recycleParcel(data, reply);
                                    return HWLOGW_E;
                                }
                            }
                        }
                        Log.d(TAG, " NSD isCursorCompleteVisible time not match : refresh : " + (now - lastRefreshTime) + " , blink time : " + (now - this.mLastBlinkTime));
                        this.mLastBlinkTime = now;
                        this.mFirstBlinkCount = NSD_SUPPORT_NOTHING;
                        recycleParcel(data, reply);
                        return false;
                    }
                    recycleParcel(data, reply);
                    return false;
                }
            }
            recycleParcel(data, reply);
            return false;
        } catch (RemoteException e) {
            z = TAG;
            Log.e(z, "isCursorCompleteVisible error");
        } finally {
            recycleParcel(data, reply);
        }
    }

    public boolean isCursorOpaque(TextView view) {
        if (view == null) {
            Log.w(TAG, "NSD isCursorOpaque fail!");
            return false;
        }
        View v = view;
        while (v.getAlpha() >= HwFragmentMenuItemView.ALPHA_NORMAL) {
            if (v != view.getRootView()) {
                v = (View) v.getParent();
                if (v == null) {
                }
            }
            return HWLOGW_E;
        }
        return false;
    }

    public int getDisplayOrientation(TextView view) {
        if (view != null) {
            return view.getResources().getConfiguration().orientation;
        }
        Log.w(TAG, "NSD getDisplayOrientation fail!");
        return -1;
    }

    public boolean drawBitmapCursor(int refreshTimes, TextView txView, Rect cursorbounds) {
        if (txView == null || cursorbounds == null || cursorbounds.width() <= 0 || cursorbounds.height() <= 0) {
            Log.w(TAG, "NSD drawBitmapCursor fail!");
            return false;
        }
        String curPkgName = txView.getContext().getPackageName();
        if ((curPkgName.equals("com.sina.weibo") || curPkgName.equals("com.tencent.mm")) && cursorbounds.width() < 3) {
            cursorbounds.left--;
            cursorbounds.right += NSD_SHOULD_RESET_DRAWN;
        }
        this.mCursorPosition[NSD_SUPPORT_NOTHING] = NSD_SUPPORT_NOTHING;
        this.mCursorPosition[NSD_SHOULD_RESET_DRAWN] = NSD_SUPPORT_NOTHING;
        txView.getLocationOnScreen(this.mCursorPosition);
        int[] iArr = this.mCursorPosition;
        iArr[NSD_SUPPORT_NOTHING] = iArr[NSD_SUPPORT_NOTHING] + ((txView.getTotalPaddingLeft() + cursorbounds.left) - txView.mScrollX);
        iArr = this.mCursorPosition;
        iArr[NSD_SHOULD_RESET_DRAWN] = iArr[NSD_SHOULD_RESET_DRAWN] + (txView.getTotalPaddingTop() + cursorbounds.top);
        Bitmap mCursorBmp = Bitmap.createBitmap(cursorbounds.width(), cursorbounds.height(), Config.ARGB_8888);
        if (mCursorBmp == null) {
            Log.w(TAG, "NSD mCursorBmp is null!");
            return false;
        }
        Canvas mCursorCanvas = new Canvas(mCursorBmp);
        boolean isUseHintLayout = false;
        mCursorCanvas.save();
        mCursorCanvas.translate((float) (-this.mCursorPosition[NSD_SUPPORT_NOTHING]), (float) (-this.mCursorPosition[NSD_SHOULD_RESET_DRAWN]));
        if ((txView.getText() == null || txView.getText().toString().equals("")) && txView.getHint() != null) {
            isUseHintLayout = HWLOGW_E;
        }
        drawBackground(txView, mCursorCanvas);
        mCursorCanvas.restore();
        mCursorCanvas.save();
        mCursorCanvas.translate((float) (-cursorbounds.left), (float) (-cursorbounds.top));
        mCursorCanvas.clipRect(cursorbounds);
        txView.editorUpdate(mCursorCanvas, isUseHintLayout);
        mCursorCanvas.restore();
        boolean ret = sendCursorBmpToSF(refreshTimes, this.mCursorPosition[NSD_SUPPORT_NOTHING], this.mCursorPosition[NSD_SHOULD_RESET_DRAWN], mCursorBmp);
        mCursorBmp.recycle();
        return ret;
    }

    public boolean isCursorBlinkCase(TextView txView, Rect cursorbounds) {
        if (txView == null || cursorbounds == null || cursorbounds.width() <= 0 || cursorbounds.height() <= 0) {
            Log.w(TAG, "NSD isCursorBlinkCase fail!");
            return false;
        } else if (txView.mScrollY != 0) {
            Log.d(TAG, "txView.mScrollX or mScrollX not match case return");
            return false;
        } else {
            String curPkgName = txView.getContext().getPackageName();
            if (curPkgName == null) {
                Log.d(TAG, "curPkgName is null");
                return false;
            }
            boolean equals = (curPkgName.equals("com.android.contacts") || curPkgName.equals("com.android.mms") || curPkgName.equals(ConnectPermission.MMS_PACKAGE) || curPkgName.equals("com.android.email") || curPkgName.equals("com.sina.weibo") || curPkgName.equals("com.sina.weibog3")) ? HWLOGW_E : curPkgName.equals("com.tencent.mm");
            if (!equals) {
                Log.d(TAG, "curPkgName is not in list");
                return false;
            } else if (curPkgName.equals(this.mOldPkgName)) {
                this.mCursorPosition[NSD_SUPPORT_NOTHING] = NSD_SUPPORT_NOTHING;
                this.mCursorPosition[NSD_SHOULD_RESET_DRAWN] = NSD_SUPPORT_NOTHING;
                txView.getLocationOnScreen(this.mCursorPosition);
                int[] iArr = this.mCursorPosition;
                iArr[NSD_SUPPORT_NOTHING] = iArr[NSD_SUPPORT_NOTHING] + ((txView.getTotalPaddingLeft() + cursorbounds.left) - txView.mScrollX);
                iArr = this.mCursorPosition;
                iArr[NSD_SHOULD_RESET_DRAWN] = iArr[NSD_SHOULD_RESET_DRAWN] + (txView.getTotalPaddingTop() + cursorbounds.top);
                if (this.mLastX == this.mCursorPosition[NSD_SUPPORT_NOTHING] && this.mLastY == this.mCursorPosition[NSD_SHOULD_RESET_DRAWN]) {
                    return (isCursorCompleteVisible(txView, new Rect(this.mCursorPosition[NSD_SUPPORT_NOTHING], this.mCursorPosition[NSD_SHOULD_RESET_DRAWN], this.mCursorPosition[NSD_SUPPORT_NOTHING] + cursorbounds.width(), this.mCursorPosition[NSD_SHOULD_RESET_DRAWN] + cursorbounds.height()), txView.getAttachInfo()) && isCursorOpaque(txView) && getDisplayOrientation(txView) == NSD_SHOULD_RESET_DRAWN) ? HWLOGW_E : false;
                } else {
                    this.mLastX = this.mCursorPosition[NSD_SUPPORT_NOTHING];
                    this.mLastY = this.mCursorPosition[NSD_SHOULD_RESET_DRAWN];
                    Log.d(TAG, "cursor position changed");
                    return false;
                }
            } else {
                this.mOldPkgName = curPkgName;
                Log.d(TAG, "mOldPkgName! = curPkgName!");
                return false;
            }
        }
    }

    public boolean sendCursorBmpToSF(int refreshTimes, int x, int y, Bitmap bmp) {
        if (bmp == null) {
            Log.w(TAG, "sendCursorBmpToSF bmp is null");
            return false;
        }
        Parcel data = Parcel.obtain();
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                int len = bmp.getByteCount();
                data.writeInt(refreshTimes);
                data.writeString(this.mLayerName);
                data.writeInt(this.mZorder);
                data.writeInt(x);
                data.writeInt(y);
                data.writeInt(width);
                data.writeInt(height);
                data.writeInt(len);
                data.writeInt(6);
                ByteBuffer bb = ByteBuffer.allocate(bmp.getByteCount());
                bmp.copyPixelsToBuffer(bb);
                data.writeByteArray(bb.array());
                flinger.transact(BINDER_NSD_CURSOR_SET_BMP, data, null, NSD_SUPPORT_NOTHING);
                return HWLOGW_E;
            }
            Log.e(TAG, "Error! get service:SurfaceFlinger return null!");
            recycleParcel(data, null);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error! call finger.transact throws RemoteException!");
        } finally {
            recycleParcel(data, null);
        }
    }

    public IBinder getCurrentWindowToken(AttachInfo mAttachInfo) {
        return mAttachInfo.mWindowToken;
    }

    public void setCurrentDrawTime(long currentDrawTime) {
    }

    public void setIsDrawCursor(int isDrawCursor) {
    }

    private void recycleParcel(Parcel data, Parcel reply) {
        if (data != null) {
            data.recycle();
        }
        if (reply != null) {
            reply.recycle();
        }
    }

    public void adaptPowerSave(Context context, MotionEvent event) {
        createEventAnalyzed();
        if (this.mEventAnalyzed != null && isSupportAps()) {
            this.mEventAnalyzed.processAnalyze(context, event.getAction(), event.getEventTime(), (int) event.getX(), (int) event.getY(), event.getPointerCount(), event.getDownTime());
        }
    }

    public void initAPS(Context context, int screenWidth, int myPid) {
        if (this.mIsFirstEnterAPS) {
            createEventAnalyzed();
            if (this.mEventAnalyzed != null) {
                this.mEventAnalyzed.initAPS(context, screenWidth, myPid);
            }
            this.mIsFirstEnterAPS = false;
        }
    }

    public synchronized void createEventAnalyzed() {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
    }

    public boolean isGameProcess(String pkgName) {
        createEventAnalyzed();
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.isGameProcess(pkgName);
        }
        return false;
    }

    public boolean isSupportAps() {
        return SystemProperties.getInt("sys.aps.support", NSD_SUPPORT_NOTHING) > 0 ? HWLOGW_E : false;
    }

    public boolean isSupportAPSEventAnalysis() {
        return NSD_SHOULD_RESET_DRAWN == (SystemProperties.getInt("sys.aps.support", NSD_SUPPORT_NOTHING) & NSD_SHOULD_RESET_DRAWN) ? HWLOGW_E : false;
    }

    public boolean isAPSReady() {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.isAPSReady();
        }
        return false;
    }

    public void powerCtroll() {
        if (this.mFpsController == null) {
            this.mFpsController = HwapsWrapper.getFpsController();
        } else {
            this.mFpsController.powerCtroll();
        }
    }

    public void setAPSOnPause() {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
        if (this.mEventAnalyzed != null) {
            this.mEventAnalyzed.setHasOnPaused(HWLOGW_E);
        }
    }

    private boolean isNsdLauncherCase(View view) {
        if (checkIfNsdSupportLauncher() && isCase(view) && !isNeedAppDraw()) {
            return HWLOGW_E;
        }
        return false;
    }

    public boolean StopSdrForSpecial(String strinfo, int keycode) {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.StopSdrForSpecial(strinfo, keycode);
        }
        Log.e(TAG, "APS: SDR: mEventAnalyzed is null");
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkIs2DSDRCase(Context context, ViewRootImpl viewRoot) {
        String APP_NAME_WILD_CARD_CHAR = "*";
        if (viewRoot == null || context == null) {
            Log.i("2DSDR", "APS: 2DSDR: AbsListView.java, viewRoot or context is null");
            return false;
        }
        ApplicationInfo info = context.getApplicationInfo();
        if (info == null || info.targetSdkVersion < 14 || (SystemProperties.getInt("sys.aps.support", NSD_SUPPORT_NOTHING) & APS_SUPPORT_2DSDR) == 0) {
            return false;
        }
        float startRatio = Float.parseFloat(SystemProperties.get("sys.2dsdr.startratio", "-1.0"));
        if (startRatio <= 0.0f || HwFragmentMenuItemView.ALPHA_NORMAL <= startRatio || viewRoot.getView() == null) {
            return false;
        }
        Display display = viewRoot.getView().getDisplay();
        if (display == null || display.getDisplayId() != 0) {
            return false;
        }
        String appPkgName = context.getApplicationInfo().packageName;
        String targetPkgName = SystemProperties.get("sys.2dsdr.pkgname", "");
        if (targetPkgName.equals("*") || targetPkgName.equals(appPkgName)) {
            return HWLOGW_E;
        }
        return false;
    }

    private float computeSDRStartRatio(Context context, View rootView, View scrollView) {
        float areaRatio = (float) ((((double) (scrollView.getWidth() * scrollView.getHeight())) * 1.0d) / ((double) (rootView.getWidth() * rootView.getHeight())));
        if (((double) areaRatio) < 0.7d) {
            return -1.0f;
        }
        float startRatio = Float.parseFloat(SystemProperties.get("sys.2dsdr.startratio", "-1.0"));
        if (0.0f >= startRatio || startRatio >= HwFragmentMenuItemView.ALPHA_NORMAL) {
            return (float) (240.0d / ((double) (context.getResources().getDisplayMetrics().ydpi * areaRatio)));
        }
        return startRatio;
    }

    private float doComputeSDRRatioChange(float startRatio, float initialVelocity, float currentVelocity, int ratioBase) {
        int startNum = (int) Math.ceil((double) (((float) ratioBase) * startRatio));
        if (startNum == ratioBase) {
            return -1.0f;
        }
        return (float) ((((double) (startNum + ((int) (0.5d + (((double) (Math.abs(initialVelocity) - Math.abs(currentVelocity))) / ((double) (Math.abs(initialVelocity) / ((float) (ratioBase - startNum))))))))) * 1.0d) / ((double) ratioBase));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public float computeSDRRatio(Context context, View rootView, View scrollView, float initialVelocity, float currentVelocity, int ratioBase) {
        float startRatio = computeSDRStartRatio(context, rootView, scrollView);
        if (startRatio < 0.0f || HwFragmentMenuItemView.ALPHA_NORMAL < startRatio || ratioBase <= NSD_SHOULD_RESET_DRAWN) {
            return -1.0f;
        }
        return doComputeSDRRatioChange(startRatio, initialVelocity, currentVelocity, ratioBase);
    }

    public int computeSDRRatioBase(Context context, View viewRoot, View scrollView) {
        if (viewRoot.getWidth() == scrollView.getWidth() && viewRoot.getHeight() == scrollView.getHeight()) {
            for (int ratioBase = 16; ratioBase > NSD_SHOULD_RESET_DRAWN; ratioBase--) {
                if (viewRoot.getHeight() % ratioBase == 0) {
                    return ratioBase;
                }
            }
        } else {
            float density = context.getResources().getDisplayMetrics().density;
            if (((int) (10.0f * density)) % 10 == 0) {
                return (int) density;
            }
        }
        return -1;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public String[] getCustAppList(int type) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        String[] result = new String[NSD_SUPPORT_NOTHING];
        if (eventAnalyzed != null && this.mContext != null) {
            return eventAnalyzed.getCustAppList(this.mContext, type);
        }
        Log.w(TAG, "APS: SDR: Customized2d: getCustApplist eventAnalyzed null");
        return result;
    }

    public int getCustScreenDimDurationLocked(int screenOffTimeout) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        if (eventAnalyzed != null) {
            return eventAnalyzed.getCustScreenDimDurationLocked(screenOffTimeout);
        }
        Log.w(TAG, "APS: Screen Dim: getCustScreenDimDuration eventAnalyzed null");
        return -1;
    }

    public String[] getCustAppList(Context context, int type) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        String[] result = new String[NSD_SUPPORT_NOTHING];
        if (eventAnalyzed != null && context != null) {
            return eventAnalyzed.getCustAppList(context, type);
        }
        Log.w(TAG, "APS: SDR: Customized2d: getCustApplist eventAnalyzed null");
        return result;
    }

    public String[] getQueryResultGameList(Context context, int type) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        String[] result = new String[NSD_SUPPORT_NOTHING];
        if (eventAnalyzed != null && context != null) {
            return eventAnalyzed.getQueryResultGameList(context, type);
        }
        Log.w(TAG, "APS: SDR: HwNsdImp: getQueryResultGameList eventAnalyzed null or context is null.");
        return result;
    }

    public void setLowResolutionMode(Context context, boolean enableLowResolutionMode) {
        int i = NSD_SUPPORT_NOTHING;
        Log.i("sdr", "APS: SDR: HwNsdImpl.setLowResolutionMod, enableLowResolutionMode = " + enableLowResolutionMode);
        String[] queryResultList = getQueryResultGameList(context, CONFIGTYPE_QUERYRESULTLIST);
        ActivityManager am = (ActivityManager) context.getSystemService(FreezeScreenScene.ACTIVITY_PARAM);
        int length;
        int i2;
        String packageName;
        if (enableLowResolutionMode) {
            String[] blackListCompatModeAppsArray = getCustAppList(context, CONFIGTYPE_BLACKLIST);
            ArrayList<String> compatModeBlackListApps = new ArrayList();
            length = blackListCompatModeAppsArray.length;
            for (i2 = NSD_SUPPORT_NOTHING; i2 < length; i2 += NSD_SHOULD_RESET_DRAWN) {
                compatModeBlackListApps.add(blackListCompatModeAppsArray[i2]);
            }
            i2 = queryResultList.length;
            while (i < i2) {
                packageName = queryResultList[i];
                if (!compatModeBlackListApps.contains(packageName)) {
                    am.setPackageScreenCompatMode(packageName, NSD_SHOULD_RESET_DRAWN);
                }
                i += NSD_SHOULD_RESET_DRAWN;
            }
            return;
        }
        String[] whiteListCompatModeAppsArray = getCustAppList(context, CONFIGTYPE_WHITELIST);
        ArrayList<String> compatModeWhiteListApps = new ArrayList();
        length = whiteListCompatModeAppsArray.length;
        for (i2 = NSD_SUPPORT_NOTHING; i2 < length; i2 += NSD_SHOULD_RESET_DRAWN) {
            compatModeWhiteListApps.add(whiteListCompatModeAppsArray[i2]);
        }
        length = queryResultList.length;
        for (i2 = NSD_SUPPORT_NOTHING; i2 < length; i2 += NSD_SHOULD_RESET_DRAWN) {
            packageName = queryResultList[i2];
            if (!compatModeWhiteListApps.contains(packageName)) {
                am.setPackageScreenCompatMode(packageName, NSD_SUPPORT_NOTHING);
            }
        }
    }

    public boolean isLowResolutionSupported() {
        if ((COMPAT_MODE_ENABLE_BIT & SystemProperties.getInt("sys.aps.support", NSD_SUPPORT_NOTHING)) != 0) {
            return HWLOGW_E;
        }
        return false;
    }
}
