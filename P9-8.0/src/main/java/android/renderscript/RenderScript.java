package android.renderscript;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.SystemProperties;
import android.renderscript.Element.DataType;
import android.util.Log;
import android.view.Surface;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class RenderScript {
    public static final int CREATE_FLAG_LOW_LATENCY = 2;
    public static final int CREATE_FLAG_LOW_POWER = 4;
    public static final int CREATE_FLAG_NONE = 0;
    public static final int CREATE_FLAG_WAIT_FOR_ATTACH = 8;
    static final boolean DEBUG = false;
    static final boolean LOG_ENABLED = false;
    static final String LOG_TAG = "RenderScript_jni";
    static final long TRACE_TAG = 32768;
    private static String mCachePath = null;
    private static ArrayList<RenderScript> mProcessContextList = new ArrayList();
    static Method registerNativeAllocation = null;
    static Method registerNativeFree = null;
    static boolean sInitialized = false;
    static final long sMinorVersion = 1;
    static int sPointerSize;
    static Object sRuntime;
    private Context mApplicationContext;
    long mContext;
    private int mContextFlags = 0;
    private int mContextSdkVersion = 0;
    ContextType mContextType = ContextType.NORMAL;
    private boolean mDestroyed = false;
    volatile Element mElement_ALLOCATION;
    volatile Element mElement_A_8;
    volatile Element mElement_BOOLEAN;
    volatile Element mElement_CHAR_2;
    volatile Element mElement_CHAR_3;
    volatile Element mElement_CHAR_4;
    volatile Element mElement_DOUBLE_2;
    volatile Element mElement_DOUBLE_3;
    volatile Element mElement_DOUBLE_4;
    volatile Element mElement_ELEMENT;
    volatile Element mElement_F16;
    volatile Element mElement_F32;
    volatile Element mElement_F64;
    volatile Element mElement_FLOAT_2;
    volatile Element mElement_FLOAT_3;
    volatile Element mElement_FLOAT_4;
    volatile Element mElement_FONT;
    volatile Element mElement_HALF_2;
    volatile Element mElement_HALF_3;
    volatile Element mElement_HALF_4;
    volatile Element mElement_I16;
    volatile Element mElement_I32;
    volatile Element mElement_I64;
    volatile Element mElement_I8;
    volatile Element mElement_INT_2;
    volatile Element mElement_INT_3;
    volatile Element mElement_INT_4;
    volatile Element mElement_LONG_2;
    volatile Element mElement_LONG_3;
    volatile Element mElement_LONG_4;
    volatile Element mElement_MATRIX_2X2;
    volatile Element mElement_MATRIX_3X3;
    volatile Element mElement_MATRIX_4X4;
    volatile Element mElement_MESH;
    volatile Element mElement_PROGRAM_FRAGMENT;
    volatile Element mElement_PROGRAM_RASTER;
    volatile Element mElement_PROGRAM_STORE;
    volatile Element mElement_PROGRAM_VERTEX;
    volatile Element mElement_RGBA_4444;
    volatile Element mElement_RGBA_5551;
    volatile Element mElement_RGBA_8888;
    volatile Element mElement_RGB_565;
    volatile Element mElement_RGB_888;
    volatile Element mElement_SAMPLER;
    volatile Element mElement_SCRIPT;
    volatile Element mElement_SHORT_2;
    volatile Element mElement_SHORT_3;
    volatile Element mElement_SHORT_4;
    volatile Element mElement_TYPE;
    volatile Element mElement_U16;
    volatile Element mElement_U32;
    volatile Element mElement_U64;
    volatile Element mElement_U8;
    volatile Element mElement_UCHAR_2;
    volatile Element mElement_UCHAR_3;
    volatile Element mElement_UCHAR_4;
    volatile Element mElement_UINT_2;
    volatile Element mElement_UINT_3;
    volatile Element mElement_UINT_4;
    volatile Element mElement_ULONG_2;
    volatile Element mElement_ULONG_3;
    volatile Element mElement_ULONG_4;
    volatile Element mElement_USHORT_2;
    volatile Element mElement_USHORT_3;
    volatile Element mElement_USHORT_4;
    volatile Element mElement_YUV;
    RSErrorHandler mErrorCallback = null;
    private boolean mIsProcessContext = false;
    RSMessageHandler mMessageCallback = null;
    MessageThread mMessageThread;
    ProgramRaster mProgramRaster_CULL_BACK;
    ProgramRaster mProgramRaster_CULL_FRONT;
    ProgramRaster mProgramRaster_CULL_NONE;
    ProgramStore mProgramStore_BLEND_ALPHA_DEPTH_NO_DEPTH;
    ProgramStore mProgramStore_BLEND_ALPHA_DEPTH_TEST;
    ProgramStore mProgramStore_BLEND_NONE_DEPTH_NO_DEPTH;
    ProgramStore mProgramStore_BLEND_NONE_DEPTH_TEST;
    ReentrantReadWriteLock mRWLock;
    volatile Sampler mSampler_CLAMP_LINEAR;
    volatile Sampler mSampler_CLAMP_LINEAR_MIP_LINEAR;
    volatile Sampler mSampler_CLAMP_NEAREST;
    volatile Sampler mSampler_MIRRORED_REPEAT_LINEAR;
    volatile Sampler mSampler_MIRRORED_REPEAT_LINEAR_MIP_LINEAR;
    volatile Sampler mSampler_MIRRORED_REPEAT_NEAREST;
    volatile Sampler mSampler_WRAP_LINEAR;
    volatile Sampler mSampler_WRAP_LINEAR_MIP_LINEAR;
    volatile Sampler mSampler_WRAP_NEAREST;

    public enum ContextType {
        NORMAL(0),
        DEBUG(1),
        PROFILE(2);
        
        int mID;

        private ContextType(int id) {
            this.mID = id;
        }
    }

    static class MessageThread extends Thread {
        static final int RS_ERROR_FATAL_DEBUG = 2048;
        static final int RS_ERROR_FATAL_UNKNOWN = 4096;
        static final int RS_MESSAGE_TO_CLIENT_ERROR = 3;
        static final int RS_MESSAGE_TO_CLIENT_EXCEPTION = 1;
        static final int RS_MESSAGE_TO_CLIENT_NEW_BUFFER = 5;
        static final int RS_MESSAGE_TO_CLIENT_NONE = 0;
        static final int RS_MESSAGE_TO_CLIENT_RESIZE = 2;
        static final int RS_MESSAGE_TO_CLIENT_USER = 4;
        int[] mAuxData = new int[2];
        RenderScript mRS;
        boolean mRun = true;

        MessageThread(RenderScript rs) {
            super("RSMessageThread");
            this.mRS = rs;
        }

        public void run() {
            int[] rbuf = new int[16];
            this.mRS.nContextInitToClient(this.mRS.mContext);
            while (this.mRun) {
                rbuf[0] = 0;
                int msg = this.mRS.nContextPeekMessage(this.mRS.mContext, this.mAuxData);
                int size = this.mAuxData[1];
                int subID = this.mAuxData[0];
                if (msg == 4) {
                    if ((size >> 2) >= rbuf.length) {
                        rbuf = new int[((size + 3) >> 2)];
                    }
                    if (this.mRS.nContextGetUserMessage(this.mRS.mContext, rbuf) != 4) {
                        throw new RSDriverException("Error processing message from RenderScript.");
                    } else if (this.mRS.mMessageCallback != null) {
                        this.mRS.mMessageCallback.mData = rbuf;
                        this.mRS.mMessageCallback.mID = subID;
                        this.mRS.mMessageCallback.mLength = size;
                        this.mRS.mMessageCallback.run();
                    } else {
                        throw new RSInvalidStateException("Received a message from the script with no message handler installed.");
                    }
                } else if (msg == 3) {
                    String e = this.mRS.nContextGetErrorMessage(this.mRS.mContext);
                    if (subID >= 4096 || (subID >= 2048 && (this.mRS.mContextType != ContextType.DEBUG || this.mRS.mErrorCallback == null))) {
                        throw new RSRuntimeException("Fatal error " + subID + ", details: " + e);
                    } else if (this.mRS.mErrorCallback != null) {
                        this.mRS.mErrorCallback.mErrorMessage = e;
                        this.mRS.mErrorCallback.mErrorNum = subID;
                        this.mRS.mErrorCallback.run();
                    } else {
                        Log.e(RenderScript.LOG_TAG, "non fatal RS error, " + e);
                    }
                } else if (msg != 5) {
                    try {
                        sleep(1, 0);
                    } catch (InterruptedException e2) {
                    }
                } else if (this.mRS.nContextGetUserMessage(this.mRS.mContext, rbuf) != 5) {
                    throw new RSDriverException("Error processing message from RenderScript.");
                } else {
                    Allocation.sendBufferNotification((((long) rbuf[1]) << 32) + (((long) rbuf[0]) & 4294967295L));
                }
            }
        }
    }

    public enum Priority {
        LOW(15),
        NORMAL(-8);
        
        int mID;

        private Priority(int id) {
            this.mID = id;
        }
    }

    public static class RSErrorHandler implements Runnable {
        protected String mErrorMessage;
        protected int mErrorNum;

        public void run() {
        }
    }

    public static class RSMessageHandler implements Runnable {
        protected int[] mData;
        protected int mID;
        protected int mLength;

        public void run() {
        }
    }

    static native void _nInit();

    static native int rsnSystemGetPointerSize();

    native void nContextDeinitToClient(long j);

    native String nContextGetErrorMessage(long j);

    native int nContextGetUserMessage(long j, int[] iArr);

    native void nContextInitToClient(long j);

    native int nContextPeekMessage(long j, int[] iArr);

    native long nDeviceCreate();

    native void nDeviceDestroy(long j);

    native void nDeviceSetConfig(long j, int i, int i2);

    native long rsnAllocationAdapterCreate(long j, long j2, long j3);

    native void rsnAllocationAdapterOffset(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9);

    native void rsnAllocationCopyFromBitmap(long j, long j2, Bitmap bitmap);

    native void rsnAllocationCopyToBitmap(long j, long j2, Bitmap bitmap);

    native long rsnAllocationCreateBitmapBackedAllocation(long j, long j2, int i, Bitmap bitmap, int i2);

    native long rsnAllocationCreateBitmapRef(long j, long j2, Bitmap bitmap);

    native long rsnAllocationCreateFromAssetStream(long j, int i, int i2, int i3);

    native long rsnAllocationCreateFromBitmap(long j, long j2, int i, Bitmap bitmap, int i2);

    native long rsnAllocationCreateTyped(long j, long j2, int i, int i2, long j3);

    native long rsnAllocationCubeCreateFromBitmap(long j, long j2, int i, Bitmap bitmap, int i2);

    native void rsnAllocationData1D(long j, long j2, int i, int i2, int i3, Object obj, int i4, int i5, int i6, boolean z);

    native void rsnAllocationData2D(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, long j3, int i7, int i8, int i9, int i10);

    native void rsnAllocationData2D(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, Object obj, int i7, int i8, int i9, boolean z);

    native void rsnAllocationData2D(long j, long j2, int i, int i2, int i3, int i4, Bitmap bitmap);

    native void rsnAllocationData3D(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, int i7, long j3, int i8, int i9, int i10, int i11);

    native void rsnAllocationData3D(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, int i7, Object obj, int i8, int i9, int i10, boolean z);

    native void rsnAllocationElementData(long j, long j2, int i, int i2, int i3, int i4, int i5, byte[] bArr, int i6);

    native void rsnAllocationElementRead(long j, long j2, int i, int i2, int i3, int i4, int i5, byte[] bArr, int i6);

    native void rsnAllocationGenerateMipmaps(long j, long j2);

    native ByteBuffer rsnAllocationGetByteBuffer(long j, long j2, long[] jArr, int i, int i2, int i3);

    native Surface rsnAllocationGetSurface(long j, long j2);

    native long rsnAllocationGetType(long j, long j2);

    native long rsnAllocationIoReceive(long j, long j2);

    native void rsnAllocationIoSend(long j, long j2);

    native void rsnAllocationRead(long j, long j2, Object obj, int i, int i2, boolean z);

    native void rsnAllocationRead1D(long j, long j2, int i, int i2, int i3, Object obj, int i4, int i5, int i6, boolean z);

    native void rsnAllocationRead2D(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, Object obj, int i7, int i8, int i9, boolean z);

    native void rsnAllocationRead3D(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, int i7, Object obj, int i8, int i9, int i10, boolean z);

    native void rsnAllocationResize1D(long j, long j2, int i);

    native void rsnAllocationSetSurface(long j, long j2, Surface surface);

    native void rsnAllocationSetupBufferQueue(long j, long j2, int i);

    native void rsnAllocationShareBufferQueue(long j, long j2, long j3);

    native void rsnAllocationSyncAll(long j, long j2, int i);

    native void rsnAssignName(long j, long j2, byte[] bArr);

    native long rsnClosureCreate(long j, long j2, long j3, long[] jArr, long[] jArr2, int[] iArr, long[] jArr3, long[] jArr4);

    native void rsnClosureSetArg(long j, long j2, int i, long j3, int i2);

    native void rsnClosureSetGlobal(long j, long j2, long j3, long j4, int i);

    native void rsnContextBindProgramFragment(long j, long j2);

    native void rsnContextBindProgramRaster(long j, long j2);

    native void rsnContextBindProgramStore(long j, long j2);

    native void rsnContextBindProgramVertex(long j, long j2);

    native void rsnContextBindRootScript(long j, long j2);

    native void rsnContextBindSampler(long j, int i, int i2);

    native long rsnContextCreate(long j, int i, int i2, int i3);

    native long rsnContextCreateGL(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, float f, int i13);

    native void rsnContextDestroy(long j);

    native void rsnContextDump(long j, int i);

    native void rsnContextFinish(long j);

    native void rsnContextPause(long j);

    native void rsnContextResume(long j);

    native void rsnContextSendMessage(long j, int i, int[] iArr);

    native void rsnContextSetCacheDir(long j, String str);

    native void rsnContextSetPriority(long j, int i);

    native void rsnContextSetSurface(long j, int i, int i2, Surface surface);

    native void rsnContextSetSurfaceTexture(long j, int i, int i2, SurfaceTexture surfaceTexture);

    native long rsnElementCreate(long j, long j2, int i, boolean z, int i2);

    native long rsnElementCreate2(long j, long[] jArr, String[] strArr, int[] iArr);

    native void rsnElementGetNativeData(long j, long j2, int[] iArr);

    native void rsnElementGetSubElements(long j, long j2, long[] jArr, String[] strArr, int[] iArr);

    native long rsnFileA3DCreateFromAsset(long j, AssetManager assetManager, String str);

    native long rsnFileA3DCreateFromAssetStream(long j, long j2);

    native long rsnFileA3DCreateFromFile(long j, String str);

    native long rsnFileA3DGetEntryByIndex(long j, long j2, int i);

    native void rsnFileA3DGetIndexEntries(long j, long j2, int i, int[] iArr, String[] strArr);

    native int rsnFileA3DGetNumIndexEntries(long j, long j2);

    native long rsnFontCreateFromAsset(long j, AssetManager assetManager, String str, float f, int i);

    native long rsnFontCreateFromAssetStream(long j, String str, float f, int i, long j2);

    native long rsnFontCreateFromFile(long j, String str, float f, int i);

    native String rsnGetName(long j, long j2);

    native long rsnInvokeClosureCreate(long j, long j2, byte[] bArr, long[] jArr, long[] jArr2, int[] iArr);

    native long rsnMeshCreate(long j, long[] jArr, long[] jArr2, int[] iArr);

    native int rsnMeshGetIndexCount(long j, long j2);

    native void rsnMeshGetIndices(long j, long j2, long[] jArr, int[] iArr, int i);

    native int rsnMeshGetVertexBufferCount(long j, long j2);

    native void rsnMeshGetVertices(long j, long j2, long[] jArr, int i);

    native void rsnObjDestroy(long j, long j2);

    native void rsnProgramBindConstants(long j, long j2, int i, long j3);

    native void rsnProgramBindSampler(long j, long j2, int i, long j3);

    native void rsnProgramBindTexture(long j, long j2, int i, long j3);

    native long rsnProgramFragmentCreate(long j, String str, String[] strArr, long[] jArr);

    native long rsnProgramRasterCreate(long j, boolean z, int i);

    native long rsnProgramStoreCreate(long j, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, int i, int i2, int i3);

    native long rsnProgramVertexCreate(long j, String str, String[] strArr, long[] jArr);

    native long rsnSamplerCreate(long j, int i, int i2, int i3, int i4, int i5, float f);

    native void rsnScriptBindAllocation(long j, long j2, long j3, int i);

    native long rsnScriptCCreate(long j, String str, String str2, byte[] bArr, int i);

    native long rsnScriptFieldIDCreate(long j, long j2, int i);

    native void rsnScriptForEach(long j, long j2, int i, long[] jArr, long j3, byte[] bArr, int[] iArr);

    native double rsnScriptGetVarD(long j, long j2, int i);

    native float rsnScriptGetVarF(long j, long j2, int i);

    native int rsnScriptGetVarI(long j, long j2, int i);

    native long rsnScriptGetVarJ(long j, long j2, int i);

    native void rsnScriptGetVarV(long j, long j2, int i, byte[] bArr);

    native long rsnScriptGroup2Create(long j, String str, String str2, long[] jArr);

    native void rsnScriptGroup2Execute(long j, long j2);

    native long rsnScriptGroupCreate(long j, long[] jArr, long[] jArr2, long[] jArr3, long[] jArr4, long[] jArr5);

    native void rsnScriptGroupExecute(long j, long j2);

    native void rsnScriptGroupSetInput(long j, long j2, long j3, long j4);

    native void rsnScriptGroupSetOutput(long j, long j2, long j3, long j4);

    native void rsnScriptIntrinsicBLAS_BNNM(long j, long j2, int i, int i2, int i3, long j3, int i4, long j4, int i5, long j5, int i6, int i7);

    native void rsnScriptIntrinsicBLAS_Complex(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, float f, float f2, long j3, long j4, float f3, float f4, long j5, int i10, int i11, int i12, int i13);

    native void rsnScriptIntrinsicBLAS_Double(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, double d, long j3, long j4, double d2, long j5, int i10, int i11, int i12, int i13);

    native void rsnScriptIntrinsicBLAS_Single(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, float f, long j3, long j4, float f2, long j5, int i10, int i11, int i12, int i13);

    native void rsnScriptIntrinsicBLAS_Z(long j, long j2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, double d, double d2, long j3, long j4, double d3, double d4, long j5, int i10, int i11, int i12, int i13);

    native long rsnScriptIntrinsicCreate(long j, int i, long j2);

    native void rsnScriptInvoke(long j, long j2, int i);

    native long rsnScriptInvokeIDCreate(long j, long j2, int i);

    native void rsnScriptInvokeV(long j, long j2, int i, byte[] bArr);

    native long rsnScriptKernelIDCreate(long j, long j2, int i, int i2);

    native void rsnScriptReduce(long j, long j2, int i, long[] jArr, long j3, int[] iArr);

    native void rsnScriptSetTimeZone(long j, long j2, byte[] bArr);

    native void rsnScriptSetVarD(long j, long j2, int i, double d);

    native void rsnScriptSetVarF(long j, long j2, int i, float f);

    native void rsnScriptSetVarI(long j, long j2, int i, int i2);

    native void rsnScriptSetVarJ(long j, long j2, int i, long j3);

    native void rsnScriptSetVarObj(long j, long j2, int i, long j3);

    native void rsnScriptSetVarV(long j, long j2, int i, byte[] bArr);

    native void rsnScriptSetVarVE(long j, long j2, int i, byte[] bArr, long j3, int[] iArr);

    native long rsnTypeCreate(long j, long j2, int i, int i2, int i3, boolean z, boolean z2, int i4);

    native void rsnTypeGetNativeData(long j, long j2, long[] jArr);

    static {
        sInitialized = false;
        if (!SystemProperties.getBoolean("config.disable_renderscript", false)) {
            try {
                Class<?> vm_runtime = Class.forName("dalvik.system.VMRuntime");
                sRuntime = vm_runtime.getDeclaredMethod("getRuntime", new Class[0]).invoke(null, new Object[0]);
                registerNativeAllocation = vm_runtime.getDeclaredMethod("registerNativeAllocation", new Class[]{Integer.TYPE});
                registerNativeFree = vm_runtime.getDeclaredMethod("registerNativeFree", new Class[]{Integer.TYPE});
                try {
                    System.loadLibrary("rs_jni");
                    _nInit();
                    sInitialized = true;
                    sPointerSize = rsnSystemGetPointerSize();
                } catch (UnsatisfiedLinkError e) {
                    Log.e(LOG_TAG, "Error loading RS jni library: " + e);
                    throw new RSRuntimeException("Error loading RS jni library: " + e);
                }
            } catch (Exception e2) {
                Log.e(LOG_TAG, "Error loading GC methods: " + e2);
                throw new RSRuntimeException("Error loading GC methods: " + e2);
            }
        }
    }

    public static long getMinorID() {
        return 1;
    }

    public static long getMinorVersion() {
        return 1;
    }

    synchronized long nContextCreateGL(long dev, int ver, int sdkVer, int colorMin, int colorPref, int alphaMin, int alphaPref, int depthMin, int depthPref, int stencilMin, int stencilPref, int samplesMin, int samplesPref, float samplesQ, int dpi) {
        return rsnContextCreateGL(dev, ver, sdkVer, colorMin, colorPref, alphaMin, alphaPref, depthMin, depthPref, stencilMin, stencilPref, samplesMin, samplesPref, samplesQ, dpi);
    }

    synchronized long nContextCreate(long dev, int ver, int sdkVer, int contextType) {
        return rsnContextCreate(dev, ver, sdkVer, contextType);
    }

    synchronized void nContextDestroy() {
        validate();
        WriteLock wlock = this.mRWLock.writeLock();
        wlock.lock();
        long curCon = this.mContext;
        this.mContext = 0;
        wlock.unlock();
        rsnContextDestroy(curCon);
    }

    synchronized void nContextSetSurface(int w, int h, Surface sur) {
        validate();
        rsnContextSetSurface(this.mContext, w, h, sur);
    }

    synchronized void nContextSetSurfaceTexture(int w, int h, SurfaceTexture sur) {
        validate();
        rsnContextSetSurfaceTexture(this.mContext, w, h, sur);
    }

    synchronized void nContextSetPriority(int p) {
        validate();
        rsnContextSetPriority(this.mContext, p);
    }

    synchronized void nContextSetCacheDir(String cacheDir) {
        validate();
        rsnContextSetCacheDir(this.mContext, cacheDir);
    }

    synchronized void nContextDump(int bits) {
        validate();
        rsnContextDump(this.mContext, bits);
    }

    synchronized void nContextFinish() {
        validate();
        rsnContextFinish(this.mContext);
    }

    synchronized void nContextSendMessage(int id, int[] data) {
        validate();
        rsnContextSendMessage(this.mContext, id, data);
    }

    synchronized void nContextBindRootScript(long script) {
        validate();
        rsnContextBindRootScript(this.mContext, script);
    }

    synchronized void nContextBindSampler(int sampler, int slot) {
        validate();
        rsnContextBindSampler(this.mContext, sampler, slot);
    }

    synchronized void nContextBindProgramStore(long pfs) {
        validate();
        rsnContextBindProgramStore(this.mContext, pfs);
    }

    synchronized void nContextBindProgramFragment(long pf) {
        validate();
        rsnContextBindProgramFragment(this.mContext, pf);
    }

    synchronized void nContextBindProgramVertex(long pv) {
        validate();
        rsnContextBindProgramVertex(this.mContext, pv);
    }

    synchronized void nContextBindProgramRaster(long pr) {
        validate();
        rsnContextBindProgramRaster(this.mContext, pr);
    }

    synchronized void nContextPause() {
        validate();
        rsnContextPause(this.mContext);
    }

    synchronized void nContextResume() {
        validate();
        rsnContextResume(this.mContext);
    }

    synchronized long nClosureCreate(long kernelID, long returnValue, long[] fieldIDs, long[] values, int[] sizes, long[] depClosures, long[] depFieldIDs) {
        long c;
        validate();
        c = rsnClosureCreate(this.mContext, kernelID, returnValue, fieldIDs, values, sizes, depClosures, depFieldIDs);
        if (c == 0) {
            throw new RSRuntimeException("Failed creating closure.");
        }
        return c;
    }

    synchronized long nInvokeClosureCreate(long invokeID, byte[] params, long[] fieldIDs, long[] values, int[] sizes) {
        long c;
        validate();
        c = rsnInvokeClosureCreate(this.mContext, invokeID, params, fieldIDs, values, sizes);
        if (c == 0) {
            throw new RSRuntimeException("Failed creating closure.");
        }
        return c;
    }

    synchronized void nClosureSetArg(long closureID, int index, long value, int size) {
        validate();
        rsnClosureSetArg(this.mContext, closureID, index, value, size);
    }

    synchronized void nClosureSetGlobal(long closureID, long fieldID, long value, int size) {
        validate();
        rsnClosureSetGlobal(this.mContext, closureID, fieldID, value, size);
    }

    synchronized long nScriptGroup2Create(String name, String cachePath, long[] closures) {
        long g;
        validate();
        g = rsnScriptGroup2Create(this.mContext, name, cachePath, closures);
        if (g == 0) {
            throw new RSRuntimeException("Failed creating script group.");
        }
        return g;
    }

    synchronized void nScriptGroup2Execute(long groupID) {
        validate();
        rsnScriptGroup2Execute(this.mContext, groupID);
    }

    synchronized void nAssignName(long obj, byte[] name) {
        validate();
        rsnAssignName(this.mContext, obj, name);
    }

    synchronized String nGetName(long obj) {
        validate();
        return rsnGetName(this.mContext, obj);
    }

    void nObjDestroy(long id) {
        if (this.mContext != 0) {
            rsnObjDestroy(this.mContext, id);
        }
    }

    synchronized long nElementCreate(long type, int kind, boolean norm, int vecSize) {
        validate();
        return rsnElementCreate(this.mContext, type, kind, norm, vecSize);
    }

    synchronized long nElementCreate2(long[] elements, String[] names, int[] arraySizes) {
        validate();
        return rsnElementCreate2(this.mContext, elements, names, arraySizes);
    }

    synchronized void nElementGetNativeData(long id, int[] elementData) {
        validate();
        rsnElementGetNativeData(this.mContext, id, elementData);
    }

    synchronized void nElementGetSubElements(long id, long[] IDs, String[] names, int[] arraySizes) {
        validate();
        rsnElementGetSubElements(this.mContext, id, IDs, names, arraySizes);
    }

    synchronized long nTypeCreate(long eid, int x, int y, int z, boolean mips, boolean faces, int yuv) {
        validate();
        return rsnTypeCreate(this.mContext, eid, x, y, z, mips, faces, yuv);
    }

    synchronized void nTypeGetNativeData(long id, long[] typeData) {
        validate();
        rsnTypeGetNativeData(this.mContext, id, typeData);
    }

    synchronized long nAllocationCreateTyped(long type, int mip, int usage, long pointer) {
        validate();
        return rsnAllocationCreateTyped(this.mContext, type, mip, usage, pointer);
    }

    synchronized long nAllocationCreateFromBitmap(long type, int mip, Bitmap bmp, int usage) {
        validate();
        return rsnAllocationCreateFromBitmap(this.mContext, type, mip, bmp, usage);
    }

    synchronized long nAllocationCreateBitmapBackedAllocation(long type, int mip, Bitmap bmp, int usage) {
        validate();
        return rsnAllocationCreateBitmapBackedAllocation(this.mContext, type, mip, bmp, usage);
    }

    synchronized long nAllocationCubeCreateFromBitmap(long type, int mip, Bitmap bmp, int usage) {
        validate();
        return rsnAllocationCubeCreateFromBitmap(this.mContext, type, mip, bmp, usage);
    }

    synchronized long nAllocationCreateBitmapRef(long type, Bitmap bmp) {
        validate();
        return rsnAllocationCreateBitmapRef(this.mContext, type, bmp);
    }

    synchronized long nAllocationCreateFromAssetStream(int mips, int assetStream, int usage) {
        validate();
        return rsnAllocationCreateFromAssetStream(this.mContext, mips, assetStream, usage);
    }

    synchronized void nAllocationCopyToBitmap(long alloc, Bitmap bmp) {
        validate();
        rsnAllocationCopyToBitmap(this.mContext, alloc, bmp);
    }

    synchronized void nAllocationSyncAll(long alloc, int src) {
        validate();
        rsnAllocationSyncAll(this.mContext, alloc, src);
    }

    synchronized ByteBuffer nAllocationGetByteBuffer(long alloc, long[] stride, int xBytesSize, int dimY, int dimZ) {
        validate();
        return rsnAllocationGetByteBuffer(this.mContext, alloc, stride, xBytesSize, dimY, dimZ);
    }

    synchronized void nAllocationSetupBufferQueue(long alloc, int numAlloc) {
        validate();
        rsnAllocationSetupBufferQueue(this.mContext, alloc, numAlloc);
    }

    synchronized void nAllocationShareBufferQueue(long alloc1, long alloc2) {
        validate();
        rsnAllocationShareBufferQueue(this.mContext, alloc1, alloc2);
    }

    synchronized Surface nAllocationGetSurface(long alloc) {
        validate();
        return rsnAllocationGetSurface(this.mContext, alloc);
    }

    synchronized void nAllocationSetSurface(long alloc, Surface sur) {
        validate();
        rsnAllocationSetSurface(this.mContext, alloc, sur);
    }

    synchronized void nAllocationIoSend(long alloc) {
        validate();
        rsnAllocationIoSend(this.mContext, alloc);
    }

    synchronized long nAllocationIoReceive(long alloc) {
        validate();
        return rsnAllocationIoReceive(this.mContext, alloc);
    }

    synchronized void nAllocationGenerateMipmaps(long alloc) {
        validate();
        rsnAllocationGenerateMipmaps(this.mContext, alloc);
    }

    synchronized void nAllocationCopyFromBitmap(long alloc, Bitmap bmp) {
        validate();
        rsnAllocationCopyFromBitmap(this.mContext, alloc, bmp);
    }

    synchronized void nAllocationData1D(long id, int off, int mip, int count, Object d, int sizeBytes, DataType dt, int mSize, boolean usePadding) {
        validate();
        rsnAllocationData1D(this.mContext, id, off, mip, count, d, sizeBytes, dt.mID, mSize, usePadding);
    }

    synchronized void nAllocationElementData(long id, int xoff, int yoff, int zoff, int mip, int compIdx, byte[] d, int sizeBytes) {
        validate();
        rsnAllocationElementData(this.mContext, id, xoff, yoff, zoff, mip, compIdx, d, sizeBytes);
    }

    synchronized void nAllocationData2D(long dstAlloc, int dstXoff, int dstYoff, int dstMip, int dstFace, int width, int height, long srcAlloc, int srcXoff, int srcYoff, int srcMip, int srcFace) {
        validate();
        rsnAllocationData2D(this.mContext, dstAlloc, dstXoff, dstYoff, dstMip, dstFace, width, height, srcAlloc, srcXoff, srcYoff, srcMip, srcFace);
    }

    synchronized void nAllocationData2D(long id, int xoff, int yoff, int mip, int face, int w, int h, Object d, int sizeBytes, DataType dt, int mSize, boolean usePadding) {
        validate();
        rsnAllocationData2D(this.mContext, id, xoff, yoff, mip, face, w, h, d, sizeBytes, dt.mID, mSize, usePadding);
    }

    synchronized void nAllocationData2D(long id, int xoff, int yoff, int mip, int face, Bitmap b) {
        validate();
        rsnAllocationData2D(this.mContext, id, xoff, yoff, mip, face, b);
    }

    synchronized void nAllocationData3D(long dstAlloc, int dstXoff, int dstYoff, int dstZoff, int dstMip, int width, int height, int depth, long srcAlloc, int srcXoff, int srcYoff, int srcZoff, int srcMip) {
        validate();
        rsnAllocationData3D(this.mContext, dstAlloc, dstXoff, dstYoff, dstZoff, dstMip, width, height, depth, srcAlloc, srcXoff, srcYoff, srcZoff, srcMip);
    }

    synchronized void nAllocationData3D(long id, int xoff, int yoff, int zoff, int mip, int w, int h, int depth, Object d, int sizeBytes, DataType dt, int mSize, boolean usePadding) {
        validate();
        rsnAllocationData3D(this.mContext, id, xoff, yoff, zoff, mip, w, h, depth, d, sizeBytes, dt.mID, mSize, usePadding);
    }

    synchronized void nAllocationRead(long id, Object d, DataType dt, int mSize, boolean usePadding) {
        validate();
        rsnAllocationRead(this.mContext, id, d, dt.mID, mSize, usePadding);
    }

    synchronized void nAllocationRead1D(long id, int off, int mip, int count, Object d, int sizeBytes, DataType dt, int mSize, boolean usePadding) {
        validate();
        rsnAllocationRead1D(this.mContext, id, off, mip, count, d, sizeBytes, dt.mID, mSize, usePadding);
    }

    synchronized void nAllocationElementRead(long id, int xoff, int yoff, int zoff, int mip, int compIdx, byte[] d, int sizeBytes) {
        validate();
        rsnAllocationElementRead(this.mContext, id, xoff, yoff, zoff, mip, compIdx, d, sizeBytes);
    }

    synchronized void nAllocationRead2D(long id, int xoff, int yoff, int mip, int face, int w, int h, Object d, int sizeBytes, DataType dt, int mSize, boolean usePadding) {
        validate();
        rsnAllocationRead2D(this.mContext, id, xoff, yoff, mip, face, w, h, d, sizeBytes, dt.mID, mSize, usePadding);
    }

    synchronized void nAllocationRead3D(long id, int xoff, int yoff, int zoff, int mip, int w, int h, int depth, Object d, int sizeBytes, DataType dt, int mSize, boolean usePadding) {
        validate();
        rsnAllocationRead3D(this.mContext, id, xoff, yoff, zoff, mip, w, h, depth, d, sizeBytes, dt.mID, mSize, usePadding);
    }

    synchronized long nAllocationGetType(long id) {
        validate();
        return rsnAllocationGetType(this.mContext, id);
    }

    synchronized void nAllocationResize1D(long id, int dimX) {
        validate();
        rsnAllocationResize1D(this.mContext, id, dimX);
    }

    synchronized long nAllocationAdapterCreate(long allocId, long typeId) {
        validate();
        return rsnAllocationAdapterCreate(this.mContext, allocId, typeId);
    }

    synchronized void nAllocationAdapterOffset(long id, int x, int y, int z, int mip, int face, int a1, int a2, int a3, int a4) {
        validate();
        rsnAllocationAdapterOffset(this.mContext, id, x, y, z, mip, face, a1, a2, a3, a4);
    }

    synchronized long nFileA3DCreateFromAssetStream(long assetStream) {
        validate();
        return rsnFileA3DCreateFromAssetStream(this.mContext, assetStream);
    }

    synchronized long nFileA3DCreateFromFile(String path) {
        validate();
        return rsnFileA3DCreateFromFile(this.mContext, path);
    }

    synchronized long nFileA3DCreateFromAsset(AssetManager mgr, String path) {
        validate();
        return rsnFileA3DCreateFromAsset(this.mContext, mgr, path);
    }

    synchronized int nFileA3DGetNumIndexEntries(long fileA3D) {
        validate();
        return rsnFileA3DGetNumIndexEntries(this.mContext, fileA3D);
    }

    synchronized void nFileA3DGetIndexEntries(long fileA3D, int numEntries, int[] IDs, String[] names) {
        validate();
        rsnFileA3DGetIndexEntries(this.mContext, fileA3D, numEntries, IDs, names);
    }

    synchronized long nFileA3DGetEntryByIndex(long fileA3D, int index) {
        validate();
        return rsnFileA3DGetEntryByIndex(this.mContext, fileA3D, index);
    }

    synchronized long nFontCreateFromFile(String fileName, float size, int dpi) {
        validate();
        return rsnFontCreateFromFile(this.mContext, fileName, size, dpi);
    }

    synchronized long nFontCreateFromAssetStream(String name, float size, int dpi, long assetStream) {
        validate();
        return rsnFontCreateFromAssetStream(this.mContext, name, size, dpi, assetStream);
    }

    synchronized long nFontCreateFromAsset(AssetManager mgr, String path, float size, int dpi) {
        validate();
        return rsnFontCreateFromAsset(this.mContext, mgr, path, size, dpi);
    }

    synchronized void nScriptBindAllocation(long script, long alloc, int slot) {
        validate();
        rsnScriptBindAllocation(this.mContext, script, alloc, slot);
    }

    synchronized void nScriptSetTimeZone(long script, byte[] timeZone) {
        validate();
        rsnScriptSetTimeZone(this.mContext, script, timeZone);
    }

    synchronized void nScriptInvoke(long id, int slot) {
        validate();
        rsnScriptInvoke(this.mContext, id, slot);
    }

    synchronized void nScriptForEach(long id, int slot, long[] ains, long aout, byte[] params, int[] limits) {
        validate();
        rsnScriptForEach(this.mContext, id, slot, ains, aout, params, limits);
    }

    synchronized void nScriptReduce(long id, int slot, long[] ains, long aout, int[] limits) {
        validate();
        rsnScriptReduce(this.mContext, id, slot, ains, aout, limits);
    }

    synchronized void nScriptInvokeV(long id, int slot, byte[] params) {
        validate();
        rsnScriptInvokeV(this.mContext, id, slot, params);
    }

    synchronized void nScriptSetVarI(long id, int slot, int val) {
        validate();
        rsnScriptSetVarI(this.mContext, id, slot, val);
    }

    synchronized int nScriptGetVarI(long id, int slot) {
        validate();
        return rsnScriptGetVarI(this.mContext, id, slot);
    }

    synchronized void nScriptSetVarJ(long id, int slot, long val) {
        validate();
        rsnScriptSetVarJ(this.mContext, id, slot, val);
    }

    synchronized long nScriptGetVarJ(long id, int slot) {
        validate();
        return rsnScriptGetVarJ(this.mContext, id, slot);
    }

    synchronized void nScriptSetVarF(long id, int slot, float val) {
        validate();
        rsnScriptSetVarF(this.mContext, id, slot, val);
    }

    synchronized float nScriptGetVarF(long id, int slot) {
        validate();
        return rsnScriptGetVarF(this.mContext, id, slot);
    }

    synchronized void nScriptSetVarD(long id, int slot, double val) {
        validate();
        rsnScriptSetVarD(this.mContext, id, slot, val);
    }

    synchronized double nScriptGetVarD(long id, int slot) {
        validate();
        return rsnScriptGetVarD(this.mContext, id, slot);
    }

    synchronized void nScriptSetVarV(long id, int slot, byte[] val) {
        validate();
        rsnScriptSetVarV(this.mContext, id, slot, val);
    }

    synchronized void nScriptGetVarV(long id, int slot, byte[] val) {
        validate();
        rsnScriptGetVarV(this.mContext, id, slot, val);
    }

    synchronized void nScriptSetVarVE(long id, int slot, byte[] val, long e, int[] dims) {
        validate();
        rsnScriptSetVarVE(this.mContext, id, slot, val, e, dims);
    }

    synchronized void nScriptSetVarObj(long id, int slot, long val) {
        validate();
        rsnScriptSetVarObj(this.mContext, id, slot, val);
    }

    synchronized long nScriptCCreate(String resName, String cacheDir, byte[] script, int length) {
        validate();
        return rsnScriptCCreate(this.mContext, resName, cacheDir, script, length);
    }

    synchronized long nScriptIntrinsicCreate(int id, long eid) {
        validate();
        return rsnScriptIntrinsicCreate(this.mContext, id, eid);
    }

    synchronized long nScriptKernelIDCreate(long sid, int slot, int sig) {
        validate();
        return rsnScriptKernelIDCreate(this.mContext, sid, slot, sig);
    }

    synchronized long nScriptInvokeIDCreate(long sid, int slot) {
        validate();
        return rsnScriptInvokeIDCreate(this.mContext, sid, slot);
    }

    synchronized long nScriptFieldIDCreate(long sid, int slot) {
        validate();
        return rsnScriptFieldIDCreate(this.mContext, sid, slot);
    }

    synchronized long nScriptGroupCreate(long[] kernels, long[] src, long[] dstk, long[] dstf, long[] types) {
        validate();
        return rsnScriptGroupCreate(this.mContext, kernels, src, dstk, dstf, types);
    }

    synchronized void nScriptGroupSetInput(long group, long kernel, long alloc) {
        validate();
        rsnScriptGroupSetInput(this.mContext, group, kernel, alloc);
    }

    synchronized void nScriptGroupSetOutput(long group, long kernel, long alloc) {
        validate();
        rsnScriptGroupSetOutput(this.mContext, group, kernel, alloc);
    }

    synchronized void nScriptGroupExecute(long group) {
        validate();
        rsnScriptGroupExecute(this.mContext, group);
    }

    synchronized long nSamplerCreate(int magFilter, int minFilter, int wrapS, int wrapT, int wrapR, float aniso) {
        validate();
        return rsnSamplerCreate(this.mContext, magFilter, minFilter, wrapS, wrapT, wrapR, aniso);
    }

    synchronized long nProgramStoreCreate(boolean r, boolean g, boolean b, boolean a, boolean depthMask, boolean dither, int srcMode, int dstMode, int depthFunc) {
        validate();
        return rsnProgramStoreCreate(this.mContext, r, g, b, a, depthMask, dither, srcMode, dstMode, depthFunc);
    }

    synchronized long nProgramRasterCreate(boolean pointSprite, int cullMode) {
        validate();
        return rsnProgramRasterCreate(this.mContext, pointSprite, cullMode);
    }

    synchronized void nProgramBindConstants(long pv, int slot, long mID) {
        validate();
        rsnProgramBindConstants(this.mContext, pv, slot, mID);
    }

    synchronized void nProgramBindTexture(long vpf, int slot, long a) {
        validate();
        rsnProgramBindTexture(this.mContext, vpf, slot, a);
    }

    synchronized void nProgramBindSampler(long vpf, int slot, long s) {
        validate();
        rsnProgramBindSampler(this.mContext, vpf, slot, s);
    }

    synchronized long nProgramFragmentCreate(String shader, String[] texNames, long[] params) {
        validate();
        return rsnProgramFragmentCreate(this.mContext, shader, texNames, params);
    }

    synchronized long nProgramVertexCreate(String shader, String[] texNames, long[] params) {
        validate();
        return rsnProgramVertexCreate(this.mContext, shader, texNames, params);
    }

    synchronized long nMeshCreate(long[] vtx, long[] idx, int[] prim) {
        validate();
        return rsnMeshCreate(this.mContext, vtx, idx, prim);
    }

    synchronized int nMeshGetVertexBufferCount(long id) {
        validate();
        return rsnMeshGetVertexBufferCount(this.mContext, id);
    }

    synchronized int nMeshGetIndexCount(long id) {
        validate();
        return rsnMeshGetIndexCount(this.mContext, id);
    }

    synchronized void nMeshGetVertices(long id, long[] vtxIds, int vtxIdCount) {
        validate();
        rsnMeshGetVertices(this.mContext, id, vtxIds, vtxIdCount);
    }

    synchronized void nMeshGetIndices(long id, long[] idxIds, int[] primitives, int vtxIdCount) {
        validate();
        rsnMeshGetIndices(this.mContext, id, idxIds, primitives, vtxIdCount);
    }

    synchronized void nScriptIntrinsicBLAS_Single(long id, int func, int TransA, int TransB, int Side, int Uplo, int Diag, int M, int N, int K, float alpha, long A, long B, float beta, long C, int incX, int incY, int KL, int KU) {
        validate();
        rsnScriptIntrinsicBLAS_Single(this.mContext, id, func, TransA, TransB, Side, Uplo, Diag, M, N, K, alpha, A, B, beta, C, incX, incY, KL, KU);
    }

    synchronized void nScriptIntrinsicBLAS_Double(long id, int func, int TransA, int TransB, int Side, int Uplo, int Diag, int M, int N, int K, double alpha, long A, long B, double beta, long C, int incX, int incY, int KL, int KU) {
        validate();
        rsnScriptIntrinsicBLAS_Double(this.mContext, id, func, TransA, TransB, Side, Uplo, Diag, M, N, K, alpha, A, B, beta, C, incX, incY, KL, KU);
    }

    synchronized void nScriptIntrinsicBLAS_Complex(long id, int func, int TransA, int TransB, int Side, int Uplo, int Diag, int M, int N, int K, float alphaX, float alphaY, long A, long B, float betaX, float betaY, long C, int incX, int incY, int KL, int KU) {
        validate();
        rsnScriptIntrinsicBLAS_Complex(this.mContext, id, func, TransA, TransB, Side, Uplo, Diag, M, N, K, alphaX, alphaY, A, B, betaX, betaY, C, incX, incY, KL, KU);
    }

    synchronized void nScriptIntrinsicBLAS_Z(long id, int func, int TransA, int TransB, int Side, int Uplo, int Diag, int M, int N, int K, double alphaX, double alphaY, long A, long B, double betaX, double betaY, long C, int incX, int incY, int KL, int KU) {
        validate();
        rsnScriptIntrinsicBLAS_Z(this.mContext, id, func, TransA, TransB, Side, Uplo, Diag, M, N, K, alphaX, alphaY, A, B, betaX, betaY, C, incX, incY, KL, KU);
    }

    synchronized void nScriptIntrinsicBLAS_BNNM(long id, int M, int N, int K, long A, int a_offset, long B, int b_offset, long C, int c_offset, int c_mult_int) {
        validate();
        rsnScriptIntrinsicBLAS_BNNM(this.mContext, id, M, N, K, A, a_offset, B, b_offset, C, c_offset, c_mult_int);
    }

    public void setMessageHandler(RSMessageHandler msg) {
        this.mMessageCallback = msg;
    }

    public RSMessageHandler getMessageHandler() {
        return this.mMessageCallback;
    }

    public void sendMessage(int id, int[] data) {
        nContextSendMessage(id, data);
    }

    public void setErrorHandler(RSErrorHandler msg) {
        this.mErrorCallback = msg;
    }

    public RSErrorHandler getErrorHandler() {
        return this.mErrorCallback;
    }

    void validateObject(BaseObj o) {
        if (o != null && o.mRS != this) {
            throw new RSIllegalArgumentException("Attempting to use an object across contexts.");
        }
    }

    void validate() {
        if (this.mContext == 0) {
            throw new RSInvalidStateException("Calling RS with no Context active.");
        }
    }

    public void setPriority(Priority p) {
        validate();
        nContextSetPriority(p.mID);
    }

    RenderScript(Context ctx) {
        if (ctx != null) {
            this.mApplicationContext = ctx.getApplicationContext();
        }
        this.mRWLock = new ReentrantReadWriteLock();
        try {
            registerNativeAllocation.invoke(sRuntime, new Object[]{Integer.valueOf(4194304)});
        } catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't invoke registerNativeAllocation:" + e);
            throw new RSRuntimeException("Couldn't invoke registerNativeAllocation:" + e);
        }
    }

    public final Context getApplicationContext() {
        return this.mApplicationContext;
    }

    static synchronized String getCachePath() {
        String str;
        synchronized (RenderScript.class) {
            if (mCachePath == null) {
                String CACHE_PATH = "com.android.renderscript.cache";
                File file = RenderScriptCacheDir.mCacheDir;
                File f = new File(RenderScriptCacheDir.mCacheDir, "com.android.renderscript.cache");
                mCachePath = f.getAbsolutePath();
                f.mkdirs();
            }
            str = mCachePath;
        }
        return str;
    }

    private static RenderScript internalCreate(Context ctx, int sdkVersion, ContextType ct, int flags) {
        if (!sInitialized) {
            Log.e(LOG_TAG, "RenderScript.create() called when disabled; someone is likely to crash");
            return null;
        } else if ((flags & -15) != 0) {
            throw new RSIllegalArgumentException("Invalid flags passed.");
        } else {
            RenderScript rs = new RenderScript(ctx);
            rs.mContext = rs.nContextCreate(rs.nDeviceCreate(), flags, sdkVersion, ct.mID);
            rs.mContextType = ct;
            rs.mContextFlags = flags;
            rs.mContextSdkVersion = sdkVersion;
            if (rs.mContext == 0) {
                throw new RSDriverException("Failed to create RS context.");
            }
            rs.nContextSetCacheDir(getCachePath());
            rs.mMessageThread = new MessageThread(rs);
            rs.mMessageThread.start();
            return rs;
        }
    }

    public static RenderScript create(Context ctx) {
        return create(ctx, ContextType.NORMAL);
    }

    public static RenderScript create(Context ctx, ContextType ct) {
        return create(ctx, ct, 0);
    }

    public static RenderScript create(Context ctx, ContextType ct, int flags) {
        return create(ctx, ctx.getApplicationInfo().targetSdkVersion, ct, flags);
    }

    public static RenderScript create(Context ctx, int sdkVersion) {
        return create(ctx, sdkVersion, ContextType.NORMAL, 0);
    }

    private static RenderScript create(Context ctx, int sdkVersion, ContextType ct, int flags) {
        if (sdkVersion < 23) {
            return internalCreate(ctx, sdkVersion, ct, flags);
        }
        synchronized (mProcessContextList) {
            for (RenderScript prs : mProcessContextList) {
                if (prs.mContextType == ct && prs.mContextFlags == flags && prs.mContextSdkVersion == sdkVersion) {
                    return prs;
                }
            }
            RenderScript prs2 = internalCreate(ctx, sdkVersion, ct, flags);
            prs2.mIsProcessContext = true;
            mProcessContextList.add(prs2);
            return prs2;
        }
    }

    public static void releaseAllContexts() {
        ArrayList<RenderScript> oldList;
        synchronized (mProcessContextList) {
            oldList = mProcessContextList;
            mProcessContextList = new ArrayList();
        }
        for (RenderScript prs : oldList) {
            prs.mIsProcessContext = false;
            prs.destroy();
        }
        oldList.clear();
    }

    public static RenderScript createMultiContext(Context ctx, ContextType ct, int flags, int API_number) {
        return internalCreate(ctx, API_number, ct, flags);
    }

    public void contextDump() {
        validate();
        nContextDump(0);
    }

    public void finish() {
        nContextFinish();
    }

    private void helpDestroy() {
        boolean shouldDestroy = false;
        synchronized (this) {
            if (!this.mDestroyed) {
                shouldDestroy = true;
                this.mDestroyed = true;
            }
        }
        if (shouldDestroy) {
            nContextFinish();
            nContextDeinitToClient(this.mContext);
            this.mMessageThread.mRun = false;
            this.mMessageThread.interrupt();
            boolean hasJoined = false;
            boolean interrupted = false;
            while (!hasJoined) {
                try {
                    this.mMessageThread.join();
                    hasJoined = true;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Log.v(LOG_TAG, "Interrupted during wait for MessageThread to join");
                Thread.currentThread().interrupt();
            }
            nContextDestroy();
        }
    }

    protected void finalize() throws Throwable {
        helpDestroy();
        super.finalize();
    }

    public void destroy() {
        if (!this.mIsProcessContext) {
            validate();
            helpDestroy();
        }
    }

    boolean isAlive() {
        return this.mContext != 0;
    }

    long safeID(BaseObj o) {
        if (o != null) {
            return o.getID(this);
        }
        return 0;
    }
}
