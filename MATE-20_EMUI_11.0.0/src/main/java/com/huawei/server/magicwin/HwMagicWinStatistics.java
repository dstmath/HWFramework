package com.huawei.server.magicwin;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import com.huawei.android.app.HiViewEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWinStatistics;
import com.huawei.server.utils.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class HwMagicWinStatistics {
    private static final int IDX_CONTAINER_TYPE = 8;
    private static final int IDX_COUNT = 1;
    private static final int IDX_DRAG_LEFT = 4;
    private static final int IDX_DRAG_LEFT_TO_FULLSCREEEN = 7;
    private static final int IDX_DRAG_MIDDLE = 3;
    private static final int IDX_DRAG_RIGHT = 5;
    private static final int IDX_DRAG_RIGHT_TO_FULLSCREEN = 6;
    private static final int IDX_MW_DURATION = 2;
    private static final int IDX_PKG = 0;
    public static final int ID_OFFSET = 992130000;
    private static final String MGC_CACHE_FILE = "magic_cache";
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long REPORT_INTERVAL = 86400000;
    private static final String SPLIT = ",";
    private static final int STATE_DRAG_LEFT = 1;
    private static final int STATE_DRAG_LEFT_TO_FULLSCREEN = 6;
    private static final int STATE_DRAG_MIDDLE = 0;
    private static final int STATE_DRAG_RIGHT = 2;
    private static final int STATE_DRAG_RIGHT_TO_FULLSCREEN = 5;
    public static final int STATE_MAGIC_WIN = -1;
    public static final int STATE_NOT_MAGIC_WIN = -2;
    private static final String SYSTEM_DIR = "system";
    private static final String TAG = "HWMW_HwMagicWinStatistics";
    public static final int TYPE_MAGIC_WINDOW_MODE_HOME_RECOGNIZED = 4;
    public static final int TYPE_MAGIC_WINDOW_MODE_USAGE = 3;
    private static final int USE_INFO_CNT = 9;
    private static Map<Integer, HwMagicWinStatistics> sInstances;
    private static long sLastReportTime = System.currentTimeMillis();
    private State mCurrentState;
    private State mNoneState = new NoneMwModeState(this, -2);
    private Map<Integer, State> mStates = new HashMap();
    private int mType;
    private Map<String, Usage> mUsages = new HashMap();

    private HwMagicWinStatistics(int type) {
        this.mStates.put(-2, this.mNoneState);
        this.mStates.put(-1, new BaseState(this, -1));
        this.mStates.put(1, new DragLeftState(this, 1));
        this.mStates.put(0, new DragMiddleState(this, 0));
        this.mStates.put(2, new DragRightState(this, 2));
        this.mStates.put(5, new DragRightToFullscreenState(this, 5));
        this.mStates.put(6, new DragLeftToFullscreenState(this, 6));
        this.mCurrentState = this.mNoneState;
        this.mType = type;
    }

    public static HwMagicWinStatistics getInstance(int type) {
        synchronized (HwMagicWinStatistics.class) {
            if (sInstances == null) {
                sInstances = new HashMap();
            }
            if (sInstances.containsKey(Integer.valueOf(type))) {
                return sInstances.get(Integer.valueOf(type));
            }
            Utils.dbg(TAG, "Instantiate type:" + type);
            HwMagicWinStatistics ins = new HwMagicWinStatistics(type);
            sInstances.put(Integer.valueOf(type), ins);
            return ins;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Usage getUsage(String pkg) {
        this.mUsages.putIfAbsent(pkg, new Usage(pkg, this.mType));
        return this.mUsages.get(pkg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String toStringType(int type) {
        if (type == 0) {
            return "Local";
        }
        if (type != 1) {
            return "Invalid";
        }
        return "Virtual";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String toStringMode(int mode) {
        if (mode == -2) {
            return "Not_magic";
        }
        if (mode == -1) {
            return "Magic";
        }
        if (mode == 0) {
            return "Middle";
        }
        if (mode == 1) {
            return "Left";
        }
        if (mode == 2) {
            return "Right";
        }
        if (mode == 5) {
            return "Right_full";
        }
        if (mode != 6) {
            return "Unknown_mode";
        }
        return "Left_full";
    }

    /* access modifiers changed from: private */
    public static class Usage {
        private int mCount = 0;
        private long mDragLeftDuration = 0;
        private long mDragLeftToFullscreenDuration = 0;
        private long mDragMiddleDuration = 0;
        private long mDragRightDuration = 0;
        private long mDragRightToFullscreenDuration = 0;
        private long mMwDuration = 0;
        private String mPackage;
        private int mType;

        Usage(String pkg, int type) {
            this.mPackage = pkg;
            this.mType = type;
        }

        public void count() {
            this.mCount++;
        }

        public void count(int cnt) {
            this.mCount += cnt;
        }

        public void appendTotalDuration(long millisecond) {
            this.mMwDuration += millisecond;
        }

        public void appendDragLeftDuration(long millisecond) {
            this.mDragLeftDuration += millisecond;
        }

        public void appendDragMiddleDuration(long millisecond) {
            this.mDragMiddleDuration += millisecond;
        }

        public void appendDragRightDuration(long millisecond) {
            this.mDragRightDuration += millisecond;
        }

        public void appendDragRightToFullscreenDuration(long millisecond) {
            this.mDragRightToFullscreenDuration += millisecond;
        }

        public void appendDragLeftToFullscreenDuration(long millisecond) {
            this.mDragLeftToFullscreenDuration += millisecond;
        }

        public String toString() {
            return "{app:" + this.mPackage + ", count:" + this.mCount + ", duration:" + (this.mMwDuration / HwMagicWinStatistics.MILLIS_PER_SECOND) + ", duration0:" + (this.mDragMiddleDuration / HwMagicWinStatistics.MILLIS_PER_SECOND) + ", duration1:" + (this.mDragLeftDuration / HwMagicWinStatistics.MILLIS_PER_SECOND) + ", duration2:" + (this.mDragRightDuration / HwMagicWinStatistics.MILLIS_PER_SECOND) + ", duration3:" + (this.mDragRightToFullscreenDuration / HwMagicWinStatistics.MILLIS_PER_SECOND) + ", duration4:" + (this.mDragLeftToFullscreenDuration / HwMagicWinStatistics.MILLIS_PER_SECOND) + ", type:" + this.mType + "}";
        }

        public String toLine() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(this.mPackage);
            buffer.append(HwMagicWinStatistics.SPLIT);
            buffer.append(this.mCount);
            buffer.append(HwMagicWinStatistics.SPLIT);
            buffer.append(this.mMwDuration);
            buffer.append(HwMagicWinStatistics.SPLIT);
            buffer.append(this.mDragMiddleDuration);
            buffer.append(HwMagicWinStatistics.SPLIT);
            buffer.append(this.mDragLeftDuration);
            buffer.append(HwMagicWinStatistics.SPLIT);
            buffer.append(this.mDragRightDuration);
            buffer.append(HwMagicWinStatistics.SPLIT);
            buffer.append(this.mDragRightToFullscreenDuration);
            buffer.append(HwMagicWinStatistics.SPLIT);
            buffer.append(this.mDragLeftToFullscreenDuration);
            buffer.append(HwMagicWinStatistics.SPLIT);
            buffer.append(this.mType);
            return buffer.toString();
        }

        public static Usage fromLine(String line) {
            String[] useInfos = line.split(HwMagicWinStatistics.SPLIT, 0);
            int type = getType(useInfos);
            if (type == -1) {
                return new Usage("", type);
            }
            Usage usage = new Usage(useInfos[0], type);
            usage.count(Integer.valueOf(useInfos[1]).intValue());
            usage.appendTotalDuration(Long.valueOf(useInfos[2]).longValue());
            usage.appendDragMiddleDuration(Long.valueOf(useInfos[3]).longValue());
            usage.appendDragLeftDuration(Long.valueOf(useInfos[4]).longValue());
            usage.appendDragRightDuration(Long.valueOf(useInfos[5]).longValue());
            usage.appendDragRightToFullscreenDuration(Long.valueOf(useInfos[6]).longValue());
            usage.appendDragLeftToFullscreenDuration(Long.valueOf(useInfos[7]).longValue());
            return usage;
        }

        private static int getType(String[] useInfos) {
            if (useInfos != null && useInfos.length == 9) {
                return getType(useInfos[8]);
            }
            return -1;
        }

        private static int getType(String type) {
            if (Integer.toString(0).equals(type)) {
                return 0;
            }
            if (Integer.toString(1).equals(type)) {
                return 1;
            }
            return -1;
        }
    }

    public void stopTick(String reason) {
        this.mCurrentState.count();
        this.mCurrentState.onStop(reason);
        this.mCurrentState = this.mNoneState;
    }

    public static void stopTicks(String reason) {
        Map<Integer, HwMagicWinStatistics> map = sInstances;
        if (map != null) {
            for (HwMagicWinStatistics instance : map.values()) {
                instance.stopTick(reason);
            }
        }
    }

    public void startTick(HwMagicWindowConfig config, String pkg, int state, String reason) {
        startTick(pkg, (state != -1 || !config.isDragable(pkg)) ? state : config.getAppDragMode(pkg), reason);
    }

    private void startTick(String pkg, int state, String reason) {
        if (!this.mCurrentState.isSameState(pkg, state)) {
            if (state == -2 || TextUtils.isEmpty(pkg)) {
                stopTick(reason);
                return;
            }
            this.mCurrentState.onStop(reason);
            this.mCurrentState = this.mStates.getOrDefault(Integer.valueOf(state), this.mNoneState);
            this.mCurrentState.onStart(pkg, reason);
        }
    }

    /* access modifiers changed from: private */
    public interface State {
        boolean isSameState(String str, int i);

        default void onStop(String reason) {
        }

        default void onStart(String pkg, String reason) {
        }

        default void count() {
        }
    }

    private final class NoneMwModeState implements State {
        private int mState;
        private HwMagicWinStatistics mStatistics;

        NoneMwModeState(HwMagicWinStatistics statistics, int state) {
            this.mStatistics = statistics;
            this.mState = state;
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.State
        public boolean isSameState(String pkgName, int state) {
            return this.mState == state;
        }

        public String toString() {
            return "NoneMwState";
        }
    }

    private class BaseState implements State {
        private String mPackage;
        private long mStartTimeStramp = 0;
        private int mState;
        private HwMagicWinStatistics mStatistics;
        private long mStopTimeStramp = 0;

        BaseState(HwMagicWinStatistics statistics, int state) {
            this.mStatistics = statistics;
            this.mState = state;
        }

        public void computeTotalDuration() {
            this.mStatistics.getUsage(this.mPackage).appendTotalDuration(this.mStopTimeStramp - this.mStartTimeStramp);
        }

        public void computeDragLeftDuration() {
            this.mStatistics.getUsage(this.mPackage).appendDragLeftDuration(this.mStopTimeStramp - this.mStartTimeStramp);
        }

        public void computeDragMiddleDuration() {
            this.mStatistics.getUsage(this.mPackage).appendDragMiddleDuration(this.mStopTimeStramp - this.mStartTimeStramp);
        }

        public void computeDragRightDuration() {
            this.mStatistics.getUsage(this.mPackage).appendDragRightDuration(this.mStopTimeStramp - this.mStartTimeStramp);
        }

        public void computeDragRightToFullscreenDuration() {
            this.mStatistics.getUsage(this.mPackage).appendDragRightToFullscreenDuration(this.mStopTimeStramp - this.mStartTimeStramp);
        }

        public void computeDragLeftToFullscreenDuration() {
            this.mStatistics.getUsage(this.mPackage).appendDragLeftToFullscreenDuration(this.mStopTimeStramp - this.mStartTimeStramp);
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.State
        public void onStart(String pkgName, String reason) {
            this.mStartTimeStramp = System.currentTimeMillis();
            this.mPackage = pkgName;
            SlogEx.i(HwMagicWinStatistics.TAG, "start tick. " + this.mPackage + ", " + HwMagicWinStatistics.this.toStringMode(this.mState) + ", " + HwMagicWinStatistics.this.toStringType(this.mStatistics.mType) + ", " + reason + ", " + this);
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.State
        public void onStop(String reason) {
            this.mStopTimeStramp = System.currentTimeMillis();
            computeTotalDuration();
            SlogEx.i(HwMagicWinStatistics.TAG, "stop tick. " + this.mPackage + ", " + HwMagicWinStatistics.this.toStringMode(this.mState) + ", " + HwMagicWinStatistics.this.toStringType(this.mStatistics.mType) + ", " + reason + ", " + this);
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.State
        public void count() {
            this.mStatistics.getUsage(this.mPackage).count();
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.State
        public boolean isSameState(String pkgName, int state) {
            return this.mPackage.equals(pkgName) && this.mState == state;
        }

        public String toString() {
            return "BaseState";
        }
    }

    private final class DragLeftState extends BaseState {
        DragLeftState(HwMagicWinStatistics statistics, int state) {
            super(statistics, state);
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState, com.huawei.server.magicwin.HwMagicWinStatistics.State
        public void onStop(String reason) {
            super.onStop(reason);
            super.computeDragLeftDuration();
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState
        public String toString() {
            return "DragLeftState";
        }
    }

    private final class DragMiddleState extends BaseState {
        DragMiddleState(HwMagicWinStatistics statistics, int state) {
            super(statistics, state);
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState, com.huawei.server.magicwin.HwMagicWinStatistics.State
        public void onStop(String reason) {
            super.onStop(reason);
            super.computeDragMiddleDuration();
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState
        public String toString() {
            return "DragMiddleState";
        }
    }

    private final class DragRightState extends BaseState {
        DragRightState(HwMagicWinStatistics statistics, int state) {
            super(statistics, state);
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState, com.huawei.server.magicwin.HwMagicWinStatistics.State
        public void onStop(String reason) {
            super.onStop(reason);
            super.computeDragRightDuration();
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState
        public String toString() {
            return "DragRightState";
        }
    }

    private final class DragRightToFullscreenState extends BaseState {
        DragRightToFullscreenState(HwMagicWinStatistics statistics, int state) {
            super(statistics, state);
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState, com.huawei.server.magicwin.HwMagicWinStatistics.State
        public void onStop(String reason) {
            super.onStop(reason);
            super.computeDragRightToFullscreenDuration();
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState
        public String toString() {
            return "LeftFsState";
        }
    }

    private final class DragLeftToFullscreenState extends BaseState {
        DragLeftToFullscreenState(HwMagicWinStatistics statistics, int state) {
            super(statistics, state);
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState, com.huawei.server.magicwin.HwMagicWinStatistics.State
        public void onStop(String reason) {
            super.onStop(reason);
            super.computeDragLeftToFullscreenDuration();
        }

        @Override // com.huawei.server.magicwin.HwMagicWinStatistics.BaseState
        public String toString() {
            return "RightFsState";
        }
    }

    public void handleReport(Context context) {
        this.mUsages.forEach(new BiConsumer(context) {
            /* class com.huawei.server.magicwin.$$Lambda$HwMagicWinStatistics$Fpelp9QDpfU_dyv4egFnHddmYc */
            private final /* synthetic */ Context f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                HwMagicWinStatistics.lambda$handleReport$0(this.f$0, (String) obj, (HwMagicWinStatistics.Usage) obj2);
            }
        });
        this.mUsages.clear();
    }

    static /* synthetic */ void lambda$handleReport$0(Context context, String key, Usage value) {
        HiViewEx.report(HiViewEx.byContent(992130003, context, value.toString()));
        Utils.dbg(TAG, value.toString());
    }

    public static void reportAll(Context context) {
        if (System.currentTimeMillis() - sLastReportTime > REPORT_INTERVAL) {
            SlogEx.i(TAG, "Trigger report");
            Map<Integer, HwMagicWinStatistics> map = sInstances;
            if (map != null) {
                for (HwMagicWinStatistics instance : map.values()) {
                    instance.handleReport(context);
                }
            }
            sLastReportTime = System.currentTimeMillis();
            return;
        }
        SlogEx.i(TAG, "Not Ready To Trigger report");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0084, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0089, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008a, code lost:
        r0.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008d, code lost:
        throw r4;
     */
    public static void loadCache() {
        File mwUseFile = new File(new File(Environment.getDataDirectory(), SYSTEM_DIR), MGC_CACHE_FILE);
        if (!mwUseFile.exists()) {
            SlogEx.i(TAG, "No cache to load.");
            return;
        }
        SlogEx.i(TAG, "Load cache.");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mwUseFile));
            parseLastReportTime(reader.readLine());
            Utils.dbg(TAG, " -> " + sLastReportTime);
            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    Utils.dbg(TAG, " -> " + line);
                    Usage usage = Usage.fromLine(line);
                    int type = usage.mType;
                    if (type != -1) {
                        getInstance(type).mUsages.putIfAbsent(usage.mPackage, usage);
                    }
                } else {
                    reader.close();
                    return;
                }
            }
        } catch (IOException e) {
            SlogEx.e(TAG, "Load cache exception.");
        }
    }

    private static long parseLastReportTime(String firstLine) {
        if (firstLine != null) {
            try {
                sLastReportTime = Long.parseLong(firstLine);
                return sLastReportTime;
            } catch (NumberFormatException e) {
                SlogEx.w(TAG, "Timestamp format error. " + firstLine);
            }
        }
        sLastReportTime = System.currentTimeMillis();
        return sLastReportTime;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0086, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008c, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x008f, code lost:
        throw r4;
     */
    public static void saveCache() {
        try {
            BufferedWriter wr = new BufferedWriter(new FileWriter(new File(new File(Environment.getDataDirectory(), SYSTEM_DIR), MGC_CACHE_FILE), false));
            wr.write(String.valueOf(sLastReportTime));
            wr.newLine();
            if (sInstances != null) {
                for (HwMagicWinStatistics instance : sInstances.values()) {
                    SlogEx.i(TAG, "Save cache.Type:" + instance.mType);
                    for (Usage usage : instance.mUsages.values()) {
                        wr.write(usage.toLine());
                        wr.newLine();
                    }
                }
            }
            wr.flush();
            wr.close();
        } catch (IOException e) {
            SlogEx.e(TAG, "Save cache exception.");
        }
    }
}
