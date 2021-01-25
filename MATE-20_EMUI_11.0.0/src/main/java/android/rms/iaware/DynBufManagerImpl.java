package android.rms.iaware;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.view.InputEvent;
import android.view.MotionEvent;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.TraceEx;
import com.huawei.android.os.TraceExt;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class DynBufManagerImpl extends DefaultDynBufManager {
    private static final int DOWNGRADE_FLING_TIME = 3;
    private static final int INVALID_VALUE = -1;
    private static final boolean LOG_SWITCH = SystemPropertiesEx.getBoolean("persist.sys.iaware.dynbuf.log.switch", false);
    private static final int NOT_END_FLING_COUNT = 1;
    private static final Object SLOCK = new Object();
    private static final String TAG = "DynBufManager";
    private static DynBufManagerImpl sInstance;
    private int mCurrentBufCount = 0;
    private DynBufSdkCallback mDynBufSdkCallback = null;
    private boolean mDynamicAdjust = false;
    private boolean mFeatureEnable = false;
    private int mFlingTime = 0;
    private float mFrameIntervalMs = 11.11f;
    private boolean mIsCurSurfaceTextureUpdate = false;
    private boolean mIsCurrentVsyncMultiView = false;
    private boolean mIsCurrentVsyncUpdate = false;
    private boolean mIsEndFlingBeforeDown = true;
    private boolean mIsFling = false;
    private boolean mIsLastSurfaceTextureUpdate = false;
    private boolean mIsLastVsyncMultiView = false;
    private boolean mIsLastVsyncUpdate = false;
    private ConcurrentHashMap<Integer, Integer> mJankCount = null;
    private int mMapSize = 0;
    private int mMaxBufCount = 0;
    private ArrayList<String> mMaxBufList = new ArrayList<>();
    private ConcurrentHashMap<Integer, Integer> mMissCount = null;
    private int mPid;
    private int mRefreshRateIndex = -1;
    private boolean mShouldAddVsync = false;
    private int mTargetBufCount = 0;
    private ArrayList<String> mTargetBufCountList = new ArrayList<>();
    private ConcurrentHashMap<Integer, OverScrollerInfo> overScrollerMap = new ConcurrentHashMap<>();

    private class OverScrollerInfo {
        private int mFrameIndex;
        private float mLastTime;

        private OverScrollerInfo() {
            this.mFrameIndex = 0;
            this.mLastTime = 0.0f;
        }

        static /* synthetic */ int access$008(OverScrollerInfo x0) {
            int i = x0.mFrameIndex;
            x0.mFrameIndex = i + 1;
            return i;
        }
    }

    public static DynBufManagerImpl getDefault() {
        DynBufManagerImpl dynBufManagerImpl;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new DynBufManagerImpl();
            }
            dynBufManagerImpl = sInstance;
        }
        return dynBufManagerImpl;
    }

    public void init(Context context) {
        String pkg;
        DynBufSdkCallback dynBufSdkCallback;
        if (context != null && (pkg = context.getPackageName()) != null) {
            this.mIsEndFlingBeforeDown = true;
            this.mFlingTime = 0;
            this.mPid = Process.myPid();
            if (this.mRefreshRateIndex != -1 && (dynBufSdkCallback = this.mDynBufSdkCallback) == null) {
                if (dynBufSdkCallback == null) {
                    this.mDynBufSdkCallback = new DynBufSdkCallback();
                }
                IAwareSdk.asyncReportDataWithCallback(3042, pkg, this.mDynBufSdkCallback, System.currentTimeMillis());
            }
        }
    }

    public void onVsync() {
        if (Process.myTid() == this.mPid) {
            this.mIsLastVsyncUpdate = this.mIsCurrentVsyncUpdate;
            this.mIsCurrentVsyncUpdate = false;
            this.mIsLastVsyncMultiView = this.mIsCurrentVsyncMultiView;
            this.mIsCurrentVsyncMultiView = false;
            this.mIsLastSurfaceTextureUpdate = this.mIsCurSurfaceTextureUpdate;
            this.mIsCurrentVsyncUpdate = false;
        }
    }

    public void initFrameInterval(long frameIntervalNanos) {
        long hz = 1000000000 / frameIntervalNanos;
        if (hz < 70) {
            this.mRefreshRateIndex = 0;
        } else if (hz < 70 || hz >= 100) {
            this.mRefreshRateIndex = 2;
        } else {
            this.mRefreshRateIndex = 1;
        }
        updateConfigByRefreshRate();
        this.mFrameIntervalMs = (float) (frameIntervalNanos / 1000000);
    }

    public void beginFling(boolean flinging, int hash) {
        int i;
        if (isEnable() && flinging) {
            if (LOG_SWITCH) {
                TraceEx.traceBegin(TraceEx.getTraceTagView(), "beginFling " + hash);
            }
            OverScrollerInfo info = this.overScrollerMap.get(Integer.valueOf(hash));
            if (info != null) {
                info.mFrameIndex = 0;
                info.mLastTime = 0.0f;
            } else {
                this.overScrollerMap.put(Integer.valueOf(hash), new OverScrollerInfo());
            }
            this.mIsFling = true;
            this.mIsCurrentVsyncUpdate = false;
            this.mIsLastVsyncUpdate = false;
            if (this.mDynamicAdjust && this.mFlingTime >= 3 && (i = this.mTargetBufCount) > 2) {
                this.mTargetBufCount = i - 1;
                this.mFlingTime = 0;
            }
            this.mFlingTime++;
            if (LOG_SWITCH) {
                TraceEx.traceEnd(TraceEx.getTraceTagView());
            }
        }
    }

    public void endFling(boolean flinging, int hash) {
        if (isEnable() && flinging) {
            if (LOG_SWITCH) {
                long traceTagView = TraceEx.getTraceTagView();
                TraceEx.traceBegin(traceTagView, "endFling " + hash);
            }
            this.mIsFling = false;
            if (this.overScrollerMap.get(Integer.valueOf(hash)) != null) {
                this.overScrollerMap.remove(Integer.valueOf(hash));
            } else {
                AwareLog.e(TAG, "endFling hash null");
            }
            if (LOG_SWITCH) {
                printStats();
                TraceEx.traceEnd(TraceEx.getTraceTagView());
            }
        }
    }

    public void notifyInputEvent(InputEvent event) {
        if (isEnable() && (event instanceof MotionEvent) && ((MotionEvent) event).getAction() == 0 && this.mIsLastVsyncUpdate) {
            this.mIsEndFlingBeforeDown = !this.mIsFling;
        }
    }

    public int getTargetBufCount() {
        if (!isEnable() || !this.mIsFling) {
            return 0;
        }
        if (!this.mIsEndFlingBeforeDown) {
            return Math.max(Math.min(this.mTargetBufCount - 1, this.mMaxBufCount), 0);
        }
        return Math.max(Math.min(this.mTargetBufCount, this.mMaxBufCount), 0);
    }

    public void updateSurfaceBufCount(int count) {
        if (isEnable()) {
            if (LOG_SWITCH) {
                TraceExt.traceCounter(TraceEx.getTraceTagView(), "Surface.mBufferCount", count);
            }
            this.mCurrentBufCount = count;
            int threshold = this.mTargetBufCount - 2;
            if (!this.mIsEndFlingBeforeDown) {
                threshold--;
            }
            if (this.mCurrentBufCount < threshold && this.mIsFling) {
                this.mShouldAddVsync = true;
            } else if (this.mCurrentBufCount >= threshold || !this.mIsFling) {
                this.mShouldAddVsync = false;
            }
        }
    }

    public void updateMultiViews() {
        this.mIsCurrentVsyncMultiView = true;
    }

    public long updateSplineTime(boolean flinging, long currentTime, int hash) {
        if (!isEnable() || !flinging || Process.myTid() != this.mPid) {
            return currentTime;
        }
        OverScrollerInfo info = this.overScrollerMap.get(Integer.valueOf(hash));
        if (info == null) {
            info = new OverScrollerInfo();
            this.overScrollerMap.put(Integer.valueOf(hash), info);
        }
        OverScrollerInfo.access$008(info);
        boolean isJank = true;
        this.mIsCurrentVsyncUpdate = true;
        float newTime = info.mLastTime + this.mFrameIntervalMs;
        if (((float) currentTime) <= newTime) {
            isJank = false;
        }
        float jitterMs = ((float) currentTime) - newTime;
        if (isJank) {
            int jitterFrame = (int) Math.ceil((double) (jitterMs / this.mFrameIntervalMs));
            countJank(jitterFrame);
            countMissed(jitterFrame);
        }
        info.mLastTime = isJank ? (float) currentTime : newTime;
        if (LOG_SWITCH) {
            long traceTagView = TraceEx.getTraceTagView();
            TraceEx.traceBegin(traceTagView, hash + ",obj:" + info + ",index:" + info.mFrameIndex + ",before:" + currentTime + ",after:" + info.mLastTime);
            TraceEx.traceEnd(TraceEx.getTraceTagView());
        }
        return (long) info.mLastTime;
    }

    public boolean canAddVsync() {
        boolean can = false;
        if (Process.myTid() != this.mPid) {
            return false;
        }
        if (LOG_SWITCH) {
            long traceTagView = TraceEx.getTraceTagView();
            TraceEx.traceBegin(traceTagView, "canAddVsync:" + this.mShouldAddVsync + " isFling:" + this.mIsFling + " isEndFlingInDown:" + this.mIsEndFlingBeforeDown + " isUpdate:" + this.mIsLastVsyncUpdate + " isMultiView:" + this.mIsLastVsyncMultiView);
        }
        if (this.mFeatureEnable && this.mShouldAddVsync && this.mIsLastVsyncUpdate && this.mIsFling && !this.mIsLastVsyncMultiView) {
            can = true;
        }
        if (LOG_SWITCH) {
            TraceEx.traceEnd(TraceEx.getTraceTagView());
        }
        return can;
    }

    public void updateSurfaceTexture() {
        this.mIsCurSurfaceTextureUpdate = true;
    }

    public boolean isLastVyncMultiView() {
        return this.mIsLastVsyncMultiView;
    }

    public boolean isLastVsyncSurfaceTextureUpdate() {
        return this.mIsLastSurfaceTextureUpdate;
    }

    private void countJank(int jankCount) {
        int curCount;
        if (this.mJankCount != null) {
            Integer index = Integer.valueOf(Math.min(jankCount, this.mMapSize));
            if (this.mDynamicAdjust && jankCount > this.mTargetBufCount) {
                this.mTargetBufCount = Math.min(jankCount, this.mMaxBufCount);
                this.mFlingTime = 0;
            }
            Integer curCount2 = this.mJankCount.get(Integer.valueOf(jankCount));
            if (curCount2 != null) {
                curCount = Integer.valueOf(curCount2.intValue() + 1);
            } else {
                curCount = 1;
            }
            this.mJankCount.put(index, curCount);
        }
    }

    private void countMissed(int jankCount) {
        int curCount;
        if (this.mMissCount != null && jankCount > this.mTargetBufCount) {
            Integer index = Integer.valueOf(Math.min(jankCount, this.mMapSize));
            Integer curCount2 = this.mMissCount.get(Integer.valueOf(jankCount));
            if (curCount2 != null) {
                curCount = Integer.valueOf(curCount2.intValue() + 1);
            } else {
                curCount = 1;
            }
            this.mMissCount.put(index, curCount);
        }
    }

    private void printStats() {
        ConcurrentHashMap<Integer, Integer> concurrentHashMap = this.mJankCount;
        if (!(concurrentHashMap == null || this.mMissCount == null)) {
            String valueString = "jankStat ";
            for (Integer key : concurrentHashMap.keySet()) {
                String valueString2 = valueString + key + ":" + this.mJankCount.get(key).toString();
                Integer miss = this.mMissCount.get(key);
                valueString = miss == null ? valueString2 + " " : valueString2 + "(" + miss.toString() + ") ";
            }
            AwareLog.i(TAG, valueString);
        }
    }

    private boolean isEnable() {
        return this.mFeatureEnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConfigByRefreshRate() {
        try {
            if (this.mRefreshRateIndex < this.mTargetBufCountList.size()) {
                this.mTargetBufCount = Integer.parseInt(this.mTargetBufCountList.get(this.mRefreshRateIndex));
            }
            if (this.mRefreshRateIndex < this.mMaxBufList.size()) {
                this.mMaxBufCount = Integer.parseInt(this.mMaxBufList.get(this.mRefreshRateIndex));
            }
        } catch (NumberFormatException e) {
            AwareLog.i(TAG, "format error");
        }
    }

    private class DynBufSdkCallback extends Binder implements IInterface {
        private static final String SDK_CALLBACK_DESCRIPTOR = "android.rms.iaware.DynBufSdkCallback";
        private static final int TRANSACTION_DYN_BUFF_MANAGER_INIT = 1;

        public DynBufSdkCallback() {
            attachInterface(this, SDK_CALLBACK_DESCRIPTOR);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code < 1 || code > 16777215) {
                return super.onTransact(code, data, reply, flags);
            }
            if (code != 1) {
                return false;
            }
            try {
                data.enforceInterface(SDK_CALLBACK_DESCRIPTOR);
                DynBufManagerImpl.this.mFeatureEnable = data.readBoolean();
                DynBufManagerImpl.this.mDynamicAdjust = data.readBoolean();
                data.readStringList(DynBufManagerImpl.this.mTargetBufCountList);
                data.readStringList(DynBufManagerImpl.this.mMaxBufList);
                DynBufManagerImpl.this.updateConfigByRefreshRate();
                DynBufManagerImpl dynBufManagerImpl = DynBufManagerImpl.this;
                dynBufManagerImpl.mMapSize = dynBufManagerImpl.mMaxBufCount + 1;
                DynBufManagerImpl dynBufManagerImpl2 = DynBufManagerImpl.this;
                dynBufManagerImpl2.mJankCount = new ConcurrentHashMap(dynBufManagerImpl2.mMapSize);
                DynBufManagerImpl dynBufManagerImpl3 = DynBufManagerImpl.this;
                dynBufManagerImpl3.mMissCount = new ConcurrentHashMap(dynBufManagerImpl3.mMapSize);
                AwareLog.i(DynBufManagerImpl.TAG, "enable:" + DynBufManagerImpl.this.mFeatureEnable + " dynAdjust:" + DynBufManagerImpl.this.mDynamicAdjust + " targetCount:" + DynBufManagerImpl.this.mTargetBufCount + " max:" + DynBufManagerImpl.this.mMaxBufCount);
                return true;
            } catch (SecurityException e) {
                return false;
            }
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }
    }
}
