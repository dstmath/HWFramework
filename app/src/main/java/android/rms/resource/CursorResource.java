package android.rms.resource;

import android.os.SystemClock;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.util.Log;

public final class CursorResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "CursorResource";
    private static CursorResource mCursorResource;
    private int mOverloadNum;
    private long mPreReportTime;
    private ResourceConfig[] mResourceConfig;
    private HwSysResManager mResourceManger;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.resource.CursorResource.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.resource.CursorResource.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.resource.CursorResource.<clinit>():void");
    }

    public CursorResource() {
        this.mPreReportTime = 0;
        this.mOverloadNum = 0;
        getConfig(17);
    }

    public static synchronized CursorResource getInstance() {
        CursorResource cursorResource;
        synchronized (CursorResource.class) {
            if (mCursorResource == null) {
                mCursorResource = new CursorResource();
            }
            cursorResource = mCursorResource;
        }
        return cursorResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        int typeID = super.getTypeId(callingUid, null, processTpye);
        if (isResourceCountOverload(callingUid, pkg, typeID, count)) {
            strategy = this.mResourceConfig[typeID].getResourceStrategy();
            if (typeID == 2 && Log.HWINFO) {
                Log.i(TAG, "process uid " + callingUid + " open too many cursor " + pkg);
            }
        }
        return strategy;
    }

    private boolean getConfig(int resourceType) {
        if (this.mResourceConfig != null) {
            return true;
        }
        this.mResourceManger = HwSysResManager.getInstance();
        if (this.mResourceManger == null) {
            Log.w(TAG, "getConfig mResourceManger == null");
            return DEBUG;
        }
        this.mResourceConfig = this.mResourceManger.getResourceConfig(resourceType);
        return this.mResourceConfig != null ? true : DEBUG;
    }

    private boolean isResourceCountOverload(int callingUid, String pkg, int typeID, int count) {
        long id = super.getResourceId(callingUid, null, typeID);
        ResourceConfig config = this.mResourceConfig[typeID];
        int threshold = config.getResourceThreshold();
        int timeInterval = config.getLoopInterval();
        long currentTime = SystemClock.uptimeMillis();
        if (count <= threshold) {
            return DEBUG;
        }
        if (Log.HWINFO) {
            Log.i(TAG, "Cursor is Overload  id=" + id + " OverloadNumber=" + count + " threshold=" + threshold);
        }
        this.mOverloadNum++;
        if (isReportTime(callingUid, typeID, currentTime, timeInterval)) {
            int i = callingUid;
            String str = pkg;
            this.mResourceManger.recordResourceOverloadStatus(i, str, 17, 0, 0, this.mOverloadNum);
            this.mPreReportTime = currentTime;
        }
        return true;
    }

    public boolean isReportTime(int callingUid, int typeID, long currentTime, int timeInterval) {
        if (currentTime - this.mPreReportTime > ((long) timeInterval)) {
            return true;
        }
        return DEBUG;
    }
}
