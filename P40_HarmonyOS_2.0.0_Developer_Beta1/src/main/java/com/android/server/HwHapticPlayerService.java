package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.WorkSource;
import android.util.Log;
import com.android.server.job.controllers.JobStatus;
import com.huawei.haptic.HwHapticAttributes;
import com.huawei.haptic.HwHapticChannel;
import com.huawei.haptic.HwHapticCurve;
import com.huawei.haptic.HwHapticPlayer;
import com.huawei.haptic.HwHapticWave;
import com.huawei.haptic.IHwHapticPlayer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HwHapticPlayerService extends IHwHapticPlayer.Stub {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
    public static final String HAPTIC_TYPE_PLAYER = "haptic.player.service";
    private static final int MAX_POINT_VALUE = 128;
    private static final long SLEEP_INTERVAL = 50;
    private static final int SLICE_CURVE_MAX_SIZE = 64;
    private static final String TAG = "HwHapticPlayerService";
    private Context mContext;
    private volatile IBinder mCurToken;
    private volatile int mDuration;
    private final ConcurrentLinkedQueue<AdjustPointInfo> mDynamicAdjustPointInfo;
    private final ConcurrentLinkedQueue<CurveInfo> mHapticCurveInfo;
    private final ConcurrentLinkedQueue<SliceInfo> mHapticSliceInfo;
    private volatile boolean mLooping = false;
    private volatile String mPackageName;
    private volatile int mState;
    private final ConcurrentLinkedQueue<AdjustPointInfo> mStaticAdjustPointInfo;
    private volatile boolean mSwapHaptic = false;
    private volatile HapticPlayThread mThread;
    private final WorkSource mTmpWorkSource = new WorkSource();
    private volatile int mUid;
    private VibratorService mVibrate;
    private final Object mVibratorLock;
    private final PowerManager.WakeLock mWakeLock;

    HwHapticPlayerService(Context context, VibratorService vs, Object lock) {
        this.mContext = context;
        this.mVibrate = vs;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "*haptic_player*");
        this.mWakeLock.setReferenceCounted(true);
        this.mHapticSliceInfo = new ConcurrentLinkedQueue<>();
        this.mStaticAdjustPointInfo = new ConcurrentLinkedQueue<>();
        this.mDynamicAdjustPointInfo = new ConcurrentLinkedQueue<>();
        this.mHapticCurveInfo = new ConcurrentLinkedQueue<>();
        this.mVibratorLock = lock;
    }

    public int play(IBinder token, int uid, String opPkg, HwHapticAttributes attr, HwHapticWave wave) {
        int i = -1;
        if (token == null) {
            Log.e(TAG, "play failed, token is null");
            return -1;
        } else if (token == this.mCurToken && this.mState == 1) {
            Log.e(TAG, "play failed, haptic is playing");
            return 16;
        } else if (opPkg == null) {
            Log.e(TAG, "play failed, opPkg is null");
            return -2;
        } else if (!HwHapticPlayer.checkDefineWave(attr, wave)) {
            Log.e(TAG, "play failed, wave is null");
            return -2;
        } else {
            synchronized (this.mVibratorLock) {
                try {
                    Log.d(TAG, "play uid: " + uid + " getCallingUid: " + Binder.getCallingUid() + " opPkg: " + opPkg + " mState: " + this.mState);
                    long[] duration = {(long) (this.mDuration + 100)};
                    if (this.mLooping) {
                        i = 0;
                    }
                    VibrationEffect effect = VibrationEffect.createWaveform(duration, i);
                    effect.setType(HAPTIC_TYPE_PLAYER);
                    this.mVibrate.startHapticPlay(uid, opPkg, effect, getAudioAttributesUsage(attr.getUsage()), token);
                    if (this.mState != 1) {
                        return 16;
                    }
                    this.mCurToken = token;
                    this.mPackageName = opPkg;
                    this.mUid = uid;
                    this.mThread = new HapticPlayThread(wave, uid, attr.getUsage());
                    this.mThread.start();
                    return 1;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }
    }

    public void stop(IBinder token) {
        synchronized (this.mVibratorLock) {
            if (token != null) {
                if (this.mCurToken == token) {
                    if (this.mThread == null) {
                        Log.w(TAG, "stop found thread is null");
                        return;
                    } else {
                        this.mVibrate.stopHapticPlay(token);
                        return;
                    }
                }
            }
            Log.w(TAG, "stop found token is invalid");
        }
    }

    public void cancel() {
        if (this.mThread != null) {
            Log.d(TAG, "play cancel");
            this.mThread.cancel();
            this.mThread = null;
            this.mCurToken = null;
            this.mUid = 0;
            this.mState = 2;
        }
    }

    /* access modifiers changed from: package-private */
    public void onStateChange(int state, int error) {
        this.mState = state;
    }

    public boolean setDynamicCurve(IBinder token, int type, int channelId, HwHapticCurve curve) {
        synchronized (this.mVibratorLock) {
            if (this.mThread == null) {
                Log.e(TAG, "setDynamicCurve failed, thread is null");
                return false;
            } else if (this.mCurToken != token) {
                Log.e(TAG, "setDynamicCurve failed, token is invalid");
                return false;
            } else if (type != 1 && type != 2) {
                Log.e(TAG, "setDynamicCurve failed, type: " + type);
                return false;
            } else if (channelId > 3) {
                Log.e(TAG, "setDynamicCurve failed, channelId: " + channelId);
                return false;
            } else {
                if (!(curve == null || curve.mAdjustPoints == null)) {
                    if (!curve.mAdjustPoints.isEmpty()) {
                        if (curve.mAdjustPoints.size() > 64) {
                            Log.e(TAG, "setDynamicCurve failed, curve adjustPoints size: " + curve.mAdjustPoints.size());
                            return false;
                        }
                        return setDynamicCurveInner(token, type, channelId, curve);
                    }
                }
                Log.e(TAG, "setDynamicCurve failed, curve is invalid");
                return false;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x001a  */
    private boolean setDynamicCurveInner(IBinder token, int type, int channelId, HwHapticCurve curve) {
        float minValue = 0.0f;
        float maxValue = 1000000.0f;
        if (type == 2) {
            minValue = -2.0f;
            maxValue = 2.0f;
        }
        for (HwHapticCurve.HwAdjustPoint hwAdjustPoint : curve.mAdjustPoints) {
            if (hwAdjustPoint == null) {
                Log.e(TAG, "setDynamicCurve failed, hwAdjustPoint is null");
                return false;
            } else if (((float) hwAdjustPoint.mTimeStamp) < 0.0f || hwAdjustPoint.mTimeStamp > 1800000) {
                Log.e(TAG, "setDynamicCurve failed, hwAdjustPoint.mTimeStamp:" + hwAdjustPoint.mTimeStamp);
                return false;
            } else {
                if (hwAdjustPoint.mValue < minValue || hwAdjustPoint.mValue > maxValue) {
                    Log.e(TAG, "setDynamicCurve failed, type: " + type + ", value: " + hwAdjustPoint.mValue);
                    return false;
                }
                while (r2.hasNext()) {
                }
            }
        }
        TimeStampComparator tc = new TimeStampComparator();
        Collections.sort(curve.mAdjustPoints, tc);
        if (tc.isValid) {
            return this.mThread.setDynamicCurve(type, channelId, curve);
        }
        Log.e(TAG, "setDynamicCurve failed, curve timestamp invalid");
        return false;
    }

    /* access modifiers changed from: private */
    public static class TimeStampComparator implements Comparator<HwHapticCurve.HwAdjustPoint> {
        public boolean isValid;

        private TimeStampComparator() {
            this.isValid = true;
        }

        public int compare(HwHapticCurve.HwAdjustPoint p1, HwHapticCurve.HwAdjustPoint p2) {
            if (!this.isValid) {
                return 0;
            }
            if (p1.mTimeStamp != p2.mTimeStamp) {
                return p1.mTimeStamp - p2.mTimeStamp;
            }
            this.isValid = false;
            return 0;
        }
    }

    public void setLooping(IBinder token, boolean looping) {
        if (this.mCurToken == token) {
            this.mLooping = looping;
        }
    }

    public void setSwapHapticPos(IBinder token, boolean swap) {
        if (this.mCurToken == token) {
            this.mSwapHaptic = swap;
        }
    }

    public boolean isPlaying(IBinder token) {
        if (this.mCurToken == token && this.mState == 1) {
            return true;
        }
        return false;
    }

    private int getAudioAttributesUsage(int usageHint) {
        if (usageHint == 1) {
            return 1;
        }
        if (usageHint == 2) {
            return 4;
        }
        if (usageHint == 3) {
            return 5;
        }
        if (usageHint == 4) {
            return 6;
        }
        if (usageHint != 5) {
            return 0;
        }
        return 14;
    }

    public int getDuration(IBinder token) {
        if (this.mCurToken != token) {
            return 0;
        }
        return this.mDuration;
    }

    public ConcurrentLinkedQueue<SliceInfo> getHapticSliceInfo() {
        return this.mHapticSliceInfo;
    }

    public ConcurrentLinkedQueue<CurveInfo> getHapticCurveInfo() {
        return this.mHapticCurveInfo;
    }

    public ConcurrentLinkedQueue<AdjustPointInfo> getStaticAdjustPointInfo() {
        return this.mStaticAdjustPointInfo;
    }

    public ConcurrentLinkedQueue<AdjustPointInfo> getDynamicAdjustPointInfo() {
        return this.mDynamicAdjustPointInfo;
    }

    /* access modifiers changed from: private */
    public class HapticPlayThread extends Thread {
        private List<CurveData> mCurveDatas = new ArrayList();
        private boolean mForceStop;
        private List<SliceData> mSliceDatas = new ArrayList();
        private long mStart = 0;
        private final int mUid;
        private List<HwHapticChannelUnit> mUnits = new ArrayList();
        private final int mUsageHint;
        private final HwHapticWave mWaveform;

        HapticPlayThread(HwHapticWave waveform, int uid, int usageHint) {
            this.mWaveform = waveform;
            this.mUid = uid;
            this.mUsageHint = usageHint;
            HwHapticPlayerService.this.mTmpWorkSource.set(uid);
            HwHapticPlayerService.this.mWakeLock.setWorkSource(HwHapticPlayerService.this.mTmpWorkSource);
            init();
        }

        private void init() {
            reset();
            HwHapticPlayerService.this.mDuration = 0;
            this.mStart = System.currentTimeMillis();
            Iterator it = this.mWaveform.mHapticChannels.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                HwHapticChannel channel = (HwHapticChannel) it.next();
                if (channel.mDuration > HwHapticPlayerService.this.mDuration) {
                    HwHapticPlayerService.this.mDuration = channel.mDuration;
                }
                if (channel.mChannelId == 3) {
                    this.mUnits.add(new HwHapticChannelUnit(channel, 1));
                    this.mUnits.add(new HwHapticChannelUnit(channel, 2));
                    break;
                }
                this.mUnits.add(new HwHapticChannelUnit(channel));
            }
            Iterator it2 = this.mWaveform.mHapticChannels.iterator();
            while (it2.hasNext()) {
                HwHapticChannel channel2 = (HwHapticChannel) it2.next();
                int channelId = channel2.mChannelId;
                HwHapticCurve intensityCurve = channel2.mIntensityCurve;
                HwHapticCurve sharpnessCurve = channel2.mSharpnessCurve;
                recordStaticAdjustPoint(channelId, 1, intensityCurve);
                recordStaticAdjustPoint(channelId, 2, sharpnessCurve);
            }
        }

        private void recordStaticAdjustPoint(int channelId, int type, HwHapticCurve adjustCurve) {
            if (adjustCurve == null) {
                Log.i(HwHapticPlayerService.TAG, " the static adjust curve is null, curve type is: " + type);
                return;
            }
            for (HwHapticCurve.HwAdjustPoint point : adjustCurve.mAdjustPoints) {
                recordAdjustPoint(channelId, type, point, System.currentTimeMillis(), HwHapticPlayerService.this.mStaticAdjustPointInfo);
            }
        }

        private float calcPointValue(float val) {
            if (val < 0.0f) {
                return 0.0f;
            }
            if (val > 1.0f) {
                return 128.0f;
            }
            return 128.0f * val;
        }

        private void doPlay() {
            int i;
            Iterator<SliceData> it = this.mSliceDatas.iterator();
            while (true) {
                i = 64;
                if (!it.hasNext()) {
                    break;
                }
                SliceData data = it.next();
                long currentSliceTime = System.currentTimeMillis();
                Log.d(HwHapticPlayerService.TAG, "playHapticSlice at " + currentSliceTime + ", ChannelId: " + data.mChannelId + ", timestamp: " + data.mTimeStamp + ", mode: " + data.mMode + ", duration: " + data.mDuration + ", intensity: " + data.mIntensity + ", sharpness: " + data.mSharpness);
                VibratorService unused = HwHapticPlayerService.this.mVibrate;
                VibratorService.playHapticSlice(data.mMode, data.mChannelId, data.mDuration, calcPointValue(data.mIntensity), calcPointValue(data.mSharpness));
                if (HwHapticPlayerService.this.mHapticSliceInfo.size() > 64) {
                    HwHapticPlayerService.this.mHapticSliceInfo.poll();
                }
                HwHapticPlayerService.this.mHapticSliceInfo.add(new SliceInfo(currentSliceTime, data.mChannelId, data.mTimeStamp, data.mMode, data.mDuration, data.mIntensity, data.mSharpness, HwHapticPlayerService.this.mPackageName));
                data.mChannelId = data.mOrgChannelId;
            }
            this.mSliceDatas.clear();
            Iterator<CurveData> it2 = this.mCurveDatas.iterator();
            while (it2.hasNext()) {
                CurveData data2 = it2.next();
                long currentCurveTime = System.currentTimeMillis();
                Log.d(HwHapticPlayerService.TAG, "setCurvePoint at " + currentCurveTime + ", ChannelId: " + data2.mChannelId + ", mTimeStamp: " + data2.mTimeStamp + ", type: " + data2.mType + ", value: " + data2.mValue);
                VibratorService unused2 = HwHapticPlayerService.this.mVibrate;
                VibratorService.setCurvePoint(data2.mType, data2.mChannelId, data2.mValue);
                if (HwHapticPlayerService.this.mHapticCurveInfo.size() > i) {
                    HwHapticPlayerService.this.mHapticCurveInfo.poll();
                }
                HwHapticPlayerService.this.mHapticCurveInfo.add(new CurveInfo(currentCurveTime, data2.mChannelId, data2.mTimeStamp, data2.mType, data2.mValue, HwHapticPlayerService.this.mPackageName, 0));
                data2.mChannelId = data2.mOrgChannelId;
                it2 = it2;
                i = 64;
            }
            this.mCurveDatas.clear();
        }

        private void recordAdjustPoint(int channelId, int type, HwHapticCurve.HwAdjustPoint point, long currentCurveTime, ConcurrentLinkedQueue<AdjustPointInfo> adjustPointInfo) {
            if (adjustPointInfo.size() > 64) {
                adjustPointInfo.poll();
            }
            adjustPointInfo.add(new AdjustPointInfo(currentCurveTime, channelId, point.mTimeStamp, type, point.mValue, HwHapticPlayerService.this.mPackageName));
        }

        /* JADX WARNING: Removed duplicated region for block: B:39:0x00ae A[EDGE_INSN: B:39:0x00ae->B:30:0x00ae ?: BREAK  , SYNTHETIC] */
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            long relativeTimePos;
            Process.setThreadPriority(-8);
            HwHapticPlayerService.this.mWakeLock.acquire();
            try {
                Log.i(HwHapticPlayerService.TAG, "playHaptic begin");
                while (true) {
                    synchronized (this) {
                        if (!this.mForceStop) {
                            relativeTimePos = System.currentTimeMillis() - this.mStart;
                            long minWaitTime = JobStatus.NO_LATEST_RUNTIME;
                            for (HwHapticChannelUnit unit : this.mUnits) {
                                minWaitTime = Math.min(minWaitTime, getNextWaitTimePos(unit, relativeTimePos));
                            }
                            doPlay();
                            if (minWaitTime != JobStatus.NO_LATEST_RUNTIME) {
                                try {
                                    long relativeWaitTime = (this.mStart + minWaitTime) - System.currentTimeMillis();
                                    if (relativeWaitTime > 0) {
                                        wait(relativeWaitTime);
                                    }
                                } catch (InterruptedException e) {
                                    Log.e(HwHapticPlayerService.TAG, "playHaptic thread is interrupted");
                                }
                            } else if (needRepeat()) {
                            }
                            if (relativeTimePos >= 1800000) {
                                break;
                            }
                        } else {
                            Log.i(HwHapticPlayerService.TAG, "playHaptic forceStop");
                            HwHapticPlayerService.this.mHapticCurveInfo.add(new CurveInfo(System.currentTimeMillis(), 0, 0, 0, 0.0f, HwHapticPlayerService.this.mPackageName, 1));
                            HwHapticPlayerService.this.mWakeLock.release();
                            return;
                        }
                    }
                    break;
                }
                HwHapticPlayerService.this.mHapticCurveInfo.add(new CurveInfo(System.currentTimeMillis(), 0, 0, 0, 0.0f, HwHapticPlayerService.this.mPackageName, 2));
                HwHapticPlayerService.this.mVibrate.onVibrationFinished();
                Log.i(HwHapticPlayerService.TAG, "playHaptic end");
                return;
                if (relativeTimePos >= 1800000) {
                }
            } finally {
                HwHapticPlayerService.this.mWakeLock.release();
            }
        }

        public void cancel() {
            synchronized (this) {
                this.mForceStop = true;
                notify();
            }
        }

        public boolean setDynamicCurve(int type, int channelId, HwHapticCurve curve) {
            if (channelId == 3) {
                return setDynamicCurve(type, 1, curve) || setDynamicCurve(type, 2, curve);
            }
            synchronized (this) {
                boolean hasSet = false;
                long now = System.currentTimeMillis();
                Iterator<HwHapticChannelUnit> it = this.mUnits.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    HwHapticChannelUnit unit = it.next();
                    if (unit.mChannelId == channelId) {
                        for (HwHapticCurve.HwAdjustPoint point : curve.mAdjustPoints) {
                            point.mTimeStamp += (int) (now - this.mStart);
                        }
                        unit.setDynamicCurve(type, curve);
                        hasSet = true;
                    }
                }
                if (!hasSet) {
                    Log.e(HwHapticPlayerService.TAG, "setDynamicCurve failed, channelId: " + channelId + ", type: " + type);
                    return false;
                }
                for (HwHapticCurve.HwAdjustPoint point2 : curve.mAdjustPoints) {
                    recordAdjustPoint(channelId, type, point2, now, HwHapticPlayerService.this.mDynamicAdjustPointInfo);
                }
                notify();
                Log.d(HwHapticPlayerService.TAG, "setDynamicCurve ok, channelId: " + channelId + ", type: " + type);
                return true;
            }
        }

        private boolean needRepeat() {
            if (!HwHapticPlayerService.this.mLooping) {
                return false;
            }
            this.mStart = System.currentTimeMillis();
            for (HwHapticChannelUnit unit : this.mUnits) {
                unit.reset(this.mStart);
            }
            return true;
        }

        private void reset() {
            this.mStart = 0;
            for (HwHapticChannelUnit unit : this.mUnits) {
                unit.reset(this.mStart);
            }
            this.mSliceDatas.clear();
            this.mCurveDatas.clear();
        }

        private long getSliceNextWaitTimePos(HwHapticChannelUnit unit, long timePos) {
            if (unit.mCurSlice != null) {
                if (((long) unit.mCurSlice.mTimeStamp) > timePos) {
                    return (long) unit.mCurSlice.mTimeStamp;
                }
                unit.mSliceData.setSlice(unit.mCurSlice);
                boolean hasInsert = false;
                Iterator<SliceData> it = this.mSliceDatas.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    SliceData data = it.next();
                    if (data.equals(unit.mSliceData)) {
                        data.mChannelId |= unit.mSliceData.mChannelId;
                        hasInsert = true;
                        break;
                    }
                }
                if (!hasInsert) {
                    this.mSliceDatas.add(unit.mSliceData);
                }
                unit.mLastSlice = unit.mCurSlice;
                if (unit.mSliceListIndex < unit.mSlices.size()) {
                    unit.mCurSlice = unit.mSlices.get(unit.mSliceListIndex);
                    unit.mSliceListIndex++;
                    return (long) unit.mCurSlice.mTimeStamp;
                }
                unit.mCurSlice = null;
                long t = (long) (unit.mLastSlice.mTimeStamp + unit.mLastSlice.mDuration);
                if (timePos < t) {
                    return t;
                }
                return JobStatus.NO_LATEST_RUNTIME;
            } else if (unit.mLastSlice == null) {
                return JobStatus.NO_LATEST_RUNTIME;
            } else {
                long t2 = (long) (unit.mLastSlice.mTimeStamp + unit.mLastSlice.mDuration);
                if (timePos < t2) {
                    return t2;
                }
                return JobStatus.NO_LATEST_RUNTIME;
            }
        }

        private long getCurveNextWaitTimePos(HwHapticChannelUnit unit, long timePos, int type) {
            HwCurveUnit unit2;
            HwCurveUnit unit1;
            HwHapticCurve.HwAdjustPoint p1;
            long t1;
            HwHapticCurve.HwAdjustPoint p2;
            long t2;
            long t;
            if (type == 0) {
                unit1 = unit.mIntenseCurveUnit[0];
                unit2 = unit.mIntenseCurveUnit[1];
            } else {
                unit1 = unit.mSharpnessCurveUnit[0];
                unit2 = unit.mSharpnessCurveUnit[1];
            }
            if (unit1.mPointsIndex >= unit1.mPoints.size() && unit2.mPointsIndex >= unit2.mPoints.size()) {
                return JobStatus.NO_LATEST_RUNTIME;
            }
            if (unit1.mPointsIndex < unit1.mPoints.size()) {
                HwHapticCurve.HwAdjustPoint p12 = unit1.mPoints.get(unit1.mPointsIndex);
                t1 = (long) p12.mTimeStamp;
                p1 = p12;
            } else {
                t1 = Long.MAX_VALUE;
                p1 = null;
            }
            if (unit2.mPointsIndex < unit2.mPoints.size()) {
                HwHapticCurve.HwAdjustPoint p22 = unit2.mPoints.get(unit2.mPointsIndex);
                t2 = (long) p22.mTimeStamp;
                p2 = p22;
            } else {
                t2 = Long.MAX_VALUE;
                p2 = null;
            }
            long t3 = unit.lastPointTime[type] + HwHapticPlayerService.SLEEP_INTERVAL;
            if (unit.lastPointTime[type] < 0) {
                t = Math.min(t1, t2);
            } else {
                t = Math.min(Math.min(t3, t1), t2);
            }
            if (timePos < t) {
                return t;
            }
            float y1 = calcValue(unit1, p1, t, type);
            float y2 = calcValue(unit2, p2, t, type);
            if (unit.mLastSlice != null) {
                insertCurveData(unit, type, calcPointValue(unit, y1, y2, type), t);
            }
            unit.lastPointTime[type] = t;
            long val = JobStatus.NO_LATEST_RUNTIME;
            if (unit1.mPointsIndex < unit1.mPoints.size()) {
                val = Math.min((long) JobStatus.NO_LATEST_RUNTIME, (long) unit1.mPoints.get(unit1.mPointsIndex).mTimeStamp);
            }
            if (unit2.mPointsIndex < unit2.mPoints.size()) {
                val = Math.min(val, (long) unit2.mPoints.get(unit2.mPointsIndex).mTimeStamp);
            }
            if (val == JobStatus.NO_LATEST_RUNTIME) {
                return JobStatus.NO_LATEST_RUNTIME;
            }
            return Math.min(t + HwHapticPlayerService.SLEEP_INTERVAL, val);
        }

        private void insertCurveData(HwHapticChannelUnit unit, int type, float value, long time) {
            CurveData cd;
            boolean hasInsert = false;
            if (type == 0) {
                if (Float.compare(value, unit.lastIntenseValue) != 0) {
                    unit.lastIntenseValue = value;
                    unit.mIntenseCurveData.mType = 1;
                    unit.mIntenseCurveData.mValue = value;
                    cd = unit.mIntenseCurveData;
                    cd.mTimeStamp = (int) time;
                } else {
                    return;
                }
            } else if (Float.compare(value, unit.lastSharpnessValue) != 0) {
                unit.lastSharpnessValue = value;
                unit.mSharpnessCurveData.mType = 2;
                unit.mSharpnessCurveData.mValue = value;
                cd = unit.mSharpnessCurveData;
                cd.mTimeStamp = (int) time;
            } else {
                return;
            }
            Iterator<CurveData> it = this.mCurveDatas.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                CurveData data = it.next();
                if (data.equals(cd)) {
                    data.mChannelId |= cd.mChannelId;
                    hasInsert = true;
                    break;
                }
            }
            if (!hasInsert) {
                this.mCurveDatas.add(cd);
            }
        }

        private float calcPointValue(HwHapticChannelUnit unit, float y1, float y2, int type) {
            float val;
            float y = y1 * y2;
            if (type == 1) {
                y = y1 + y2;
            }
            if (unit.mLastSlice == null) {
                return 0.0f;
            }
            if (type == 0) {
                val = unit.mLastSlice.mIntensity * y;
            } else {
                val = unit.mLastSlice.mSharpness + y;
            }
            if (val < 0.0f) {
                return 0.0f;
            }
            if (val > 1.0f) {
                return 128.0f;
            }
            return 128.0f * val;
        }

        private float calcValue(HwCurveUnit unit, HwHapticCurve.HwAdjustPoint point, long time, int type) {
            if (point != null) {
                if (time == ((long) point.mTimeStamp)) {
                    unit.mPointsIndex++;
                    return point.mValue;
                } else if (unit.mPointsIndex > 0) {
                    return unit.mCoeffs.get(unit.mPointsIndex - 1).getY(time);
                }
            }
            if (type == 0) {
                return 1.0f;
            }
            return 0.0f;
        }

        private long getNextWaitTimePos(HwHapticChannelUnit unit, long timePos) {
            long t1 = getSliceNextWaitTimePos(unit, timePos);
            if (t1 == JobStatus.NO_LATEST_RUNTIME) {
                return JobStatus.NO_LATEST_RUNTIME;
            }
            return Math.min(t1, Math.min(getCurveNextWaitTimePos(unit, timePos, 0), getCurveNextWaitTimePos(unit, timePos, 1)));
        }
    }

    /* access modifiers changed from: private */
    public static class LinearCoeff {
        public float b;
        public float k;

        public LinearCoeff(long x1, long x2, float y1, float y2) {
            if (x1 == x2) {
                this.k = 0.0f;
                this.b = 0.0f;
            }
            this.k = (y2 - y1) / ((float) (x2 - x1));
            this.b = y1 - (this.k * ((float) x1));
        }

        public float getY(long x) {
            return (this.k * ((float) x)) + this.b;
        }
    }

    /* access modifiers changed from: private */
    public static class HwHapticChannelUnit {
        public static final int DYNAMIC_CURVE_INDEX = 1;
        public static final int INTENSE_INDEX = 0;
        public static final int SHARPNESS_INDEX = 1;
        public static final int STATIC_CURVE_INDEX = 0;
        public float lastIntenseValue;
        public long[] lastPointTime;
        public float lastSharpnessValue;
        public HwHapticChannel mChannel;
        public int mChannelId;
        public HwHapticChannel.HwHapticSlice mCurSlice;
        public CurveData mIntenseCurveData;
        HwCurveUnit[] mIntenseCurveUnit;
        public HwHapticChannel.HwHapticSlice mLastSlice;
        public CurveData mSharpnessCurveData;
        HwCurveUnit[] mSharpnessCurveUnit;
        public SliceData mSliceData;
        public int mSliceListIndex;
        public List<HwHapticChannel.HwHapticSlice> mSlices;

        public HwHapticChannelUnit(HwHapticChannel channel) {
            this(channel, channel.mChannelId);
        }

        public HwHapticChannelUnit(HwHapticChannel channel, int channelId) {
            this.mSlices = new ArrayList();
            this.mCurSlice = null;
            this.mLastSlice = null;
            this.mSliceListIndex = 0;
            this.mSliceData = new SliceData();
            this.mIntenseCurveData = new CurveData();
            this.lastIntenseValue = -1.0f;
            this.mSharpnessCurveData = new CurveData();
            this.lastSharpnessValue = -1.0f;
            this.mIntenseCurveUnit = new HwCurveUnit[2];
            this.mSharpnessCurveUnit = new HwCurveUnit[2];
            this.lastPointTime = new long[]{-50, -50};
            this.mChannel = channel;
            this.mChannelId = channelId;
            if (this.mChannel.mHapticSlices.size() > 0) {
                this.mSlices = this.mChannel.mHapticSlices;
                this.mCurSlice = (HwHapticChannel.HwHapticSlice) channel.mHapticSlices.get(0);
                this.mSliceListIndex = 1;
            }
            this.mIntenseCurveUnit[0] = new HwCurveUnit(channel.mIntensityCurve);
            this.mIntenseCurveUnit[1] = new HwCurveUnit(null);
            this.mSharpnessCurveUnit[0] = new HwCurveUnit(channel.mSharpnessCurve);
            this.mSharpnessCurveUnit[1] = new HwCurveUnit(null);
            SliceData sliceData = this.mSliceData;
            int i = this.mChannelId;
            sliceData.mChannelId = i;
            sliceData.mOrgChannelId = i;
            CurveData curveData = this.mIntenseCurveData;
            curveData.mChannelId = i;
            curveData.mOrgChannelId = i;
            CurveData curveData2 = this.mSharpnessCurveData;
            curveData2.mChannelId = i;
            curveData2.mOrgChannelId = i;
        }

        public void setDynamicCurve(int type, HwHapticCurve curve) {
            if (type == 1) {
                this.mIntenseCurveUnit[1] = new HwCurveUnit(curve);
            } else {
                this.mSharpnessCurveUnit[1] = new HwCurveUnit(curve);
            }
        }

        public void reset(long start) {
            this.mCurSlice = null;
            if (this.mSlices.size() > 0) {
                this.mCurSlice = this.mSlices.get(0);
                this.mSliceListIndex = 1;
            }
            this.mLastSlice = null;
            HwCurveUnit[] hwCurveUnitArr = this.mIntenseCurveUnit;
            hwCurveUnitArr[0].mPointsIndex = 0;
            hwCurveUnitArr[1].mPointsIndex = 0;
            HwCurveUnit[] hwCurveUnitArr2 = this.mSharpnessCurveUnit;
            hwCurveUnitArr2[0].mPointsIndex = 0;
            hwCurveUnitArr2[1].mPointsIndex = 0;
        }
    }

    /* access modifiers changed from: private */
    public static class HwCurveUnit {
        public List<LinearCoeff> mCoeffs = new ArrayList();
        public List<HwHapticCurve.HwAdjustPoint> mPoints = new ArrayList();
        public int mPointsIndex = 0;

        public HwCurveUnit(HwHapticCurve curve) {
            if (curve != null && curve.mAdjustPoints.size() > 0) {
                this.mPoints.addAll(curve.mAdjustPoints);
                for (int i = 0; i < this.mPoints.size() - 1; i++) {
                    HwHapticCurve.HwAdjustPoint p1 = this.mPoints.get(i);
                    HwHapticCurve.HwAdjustPoint p2 = this.mPoints.get(i + 1);
                    this.mCoeffs.add(new LinearCoeff((long) p1.mTimeStamp, (long) p2.mTimeStamp, p1.mValue, p2.mValue));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SliceData {
        public int mChannelId;
        public int mDuration;
        public float mIntensity;
        public int mMode;
        public int mOrgChannelId;
        public float mSharpness;
        public int mTimeStamp;

        private SliceData() {
        }

        public void setSlice(HwHapticChannel.HwHapticSlice slice) {
            this.mTimeStamp = slice.mTimeStamp;
            this.mMode = slice.mType;
            this.mDuration = slice.mDuration;
            this.mIntensity = slice.mIntensity;
            this.mSharpness = slice.mSharpness;
        }

        public boolean equals(SliceData sliceData) {
            return this.mMode == sliceData.mMode && this.mDuration == sliceData.mDuration && Float.compare(sliceData.mIntensity, this.mIntensity) == 0 && Float.compare(sliceData.mSharpness, this.mSharpness) == 0;
        }
    }

    /* access modifiers changed from: private */
    public static class CurveData {
        public int mChannelId;
        public int mOrgChannelId;
        public int mTimeStamp;
        public int mType;
        public float mValue;

        private CurveData() {
        }

        public boolean equals(CurveData data) {
            return this.mType == data.mType && Float.compare(data.mValue, this.mValue) == 0;
        }
    }

    /* access modifiers changed from: package-private */
    public static class SliceInfo {
        private final int mChannelId;
        private final int mDuration;
        private final float mIntensity;
        private final String mPackageName;
        private final float mSharpness;
        private final long mStartSliceTime;
        private final int mTimeStamp;
        private final int mType;

        public SliceInfo(long startSliceTime, int channelId, int timeStamp, int type, int duration, float intensity, float sharpness, String packageName) {
            this.mStartSliceTime = startSliceTime;
            this.mChannelId = channelId;
            this.mTimeStamp = timeStamp;
            this.mType = type;
            this.mDuration = duration;
            this.mIntensity = intensity;
            this.mSharpness = sharpness;
            this.mPackageName = packageName;
        }

        public String toString() {
            return "play haptic slice: startTime: " + new SimpleDateFormat(HwHapticPlayerService.DATE_FORMAT).format(new Date(this.mStartSliceTime)) + ", channel No: " + this.mChannelId + ", timeStamp: " + this.mTimeStamp + ", vibration type: " + this.mType + ", duration: " + this.mDuration + ", intensity: " + this.mIntensity + ", sharpness: " + this.mSharpness + ", package name: " + this.mPackageName;
        }
    }

    /* access modifiers changed from: package-private */
    public static class AdjustPointInfo {
        private final int mChannelId;
        private final String mPackageName;
        private final long mStartCurveTime;
        private final int mTimeStamp;
        private final int mType;
        private final float mValue;

        public AdjustPointInfo(long startCurveTime, int channelId, int timeStamp, int type, float value, String packageName) {
            this.mStartCurveTime = startCurveTime;
            this.mChannelId = channelId;
            this.mTimeStamp = timeStamp;
            this.mType = type;
            this.mValue = value;
            this.mPackageName = packageName;
        }

        public String toString() {
            return "adjust point curve: startTime: " + new SimpleDateFormat(HwHapticPlayerService.DATE_FORMAT).format(new Date(this.mStartCurveTime)) + ", channel No: " + this.mChannelId + ", timeStamp: " + this.mTimeStamp + ", vibration type: " + this.mType + ", value: " + this.mValue + ", package name: " + this.mPackageName;
        }
    }

    /* access modifiers changed from: package-private */
    public static class CurveInfo {
        private final int mChannelId;
        private int mCurveState;
        private final String mPackageName;
        private final long mStartCurveTime;
        private final int mTimeStamp;
        private final int mType;
        private final float mValue;

        public CurveInfo(long startCurveTime, int channelId, int timeStamp, int type, float value, String packageName, int curveState) {
            this.mStartCurveTime = startCurveTime;
            this.mChannelId = channelId;
            this.mTimeStamp = timeStamp;
            this.mType = type;
            this.mValue = value;
            this.mPackageName = packageName;
            this.mCurveState = curveState;
        }

        public String toString() {
            int i = this.mCurveState;
            if (i == 0) {
                return "play haptic curve: startTime: " + new SimpleDateFormat(HwHapticPlayerService.DATE_FORMAT).format(new Date(this.mStartCurveTime)) + ", channel No: " + this.mChannelId + ", timeStamp: " + this.mTimeStamp + ", vibration type: " + this.mType + ", value: " + this.mValue + ", package name: " + this.mPackageName;
            } else if (i == 1) {
                return "play haptic curve: startTime: " + new SimpleDateFormat(HwHapticPlayerService.DATE_FORMAT).format(new Date(this.mStartCurveTime)) + ", play state: play haptic curve is stopped, package name: " + this.mPackageName;
            } else if (i != 2) {
                return "";
            } else {
                return "play haptic curve: startTime: " + new SimpleDateFormat(HwHapticPlayerService.DATE_FORMAT).format(new Date(this.mStartCurveTime)) + ", play state: play haptic curve is completed, package name: " + this.mPackageName;
            }
        }
    }
}
