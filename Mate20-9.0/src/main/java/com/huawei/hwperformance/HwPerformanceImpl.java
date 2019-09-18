package com.huawei.hwperformance;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.BoostFramework;
import android.util.Log;
import com.huawei.android.bastet.BastetParameters;
import java.util.ArrayList;

public final class HwPerformanceImpl implements HwPerformance {
    private static final String B_CORE_CPUS = null;
    private static final boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final String L_CORE_CPUS = null;
    static final int PERF_CONFIG_GET = 3;
    static final int PERF_CONFIG_SET = 2;
    static final int PERF_EVENT = 1;
    private static final int PERF_TAG_B_CPU_MAX_CODE = 1082146816;
    private static final int PERF_TAG_B_CPU_MIN_CODE = 1082130432;
    private static final int PERF_TAG_CTL_CPUS_ON_CODE = 1090519040;
    private static final int PERF_TAG_CTL_CPUS_ON_LIMIT_CODE = 1090535424;
    private static final int PERF_TAG_L_CPU_MAX_CODE = 1082147072;
    private static final int PERF_TAG_L_CPU_MIN_CODE = 1082130688;
    private static final int PLATFORM_DEFAULT = 0;
    private static final int PLATFORM_HISI = 2;
    private static final int PLATFORM_QCOM = 1;
    private static final String TAG = "HwPerformanceImpl";
    private static HwPerformance mInstance;
    private static final Object mLock = new Object();
    private static int usingPlatform = 0;

    private HwPerformanceImpl() {
        initUsingPlatform();
    }

    public static synchronized HwPerformance getDefault() {
        HwPerformance hwPerformance;
        synchronized (HwPerformanceImpl.class) {
            if (mInstance == null) {
                mInstance = new HwPerformanceImpl();
            }
            hwPerformance = mInstance;
        }
        return hwPerformance;
    }

    private synchronized void initUsingPlatform() {
        synchronized (mLock) {
            if (ServiceManager.checkService("perfhub") != null) {
                usingPlatform = 2;
            } else {
                usingPlatform = 1;
            }
            if (HWDBG) {
                Log.d(TAG, "init using Platform = " + usingPlatform);
            }
        }
    }

    private boolean isInputInvalid(int[] tags, int[] values) {
        boolean ret = false;
        if (tags == null || values == null) {
            ret = true;
        } else if (tags.length != values.length) {
            Log.d(TAG, "Input Invalid length not match.");
            ret = true;
        }
        if (HWDBG) {
            Log.d(TAG, "Input Invalid ret = " + ret);
        }
        return ret;
    }

    private int perfhubEvent(int eventId, String PackageName, int... payload) {
        IBinder service = ServiceManager.checkService("perfhub");
        if (service == null) {
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.os.IPerfHub");
        data.writeInt(eventId);
        data.writeString(PackageName);
        data.writeInt(payload.length);
        for (int i : payload) {
            data.writeInt(i);
        }
        try {
            service.transact(1, data, reply, 1);
        } catch (RemoteException e) {
            if (HWDBG) {
                Log.d(TAG, "event transact Exception e = " + e);
            }
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        if (HWDBG) {
            Log.d(TAG, "perfhubEvent ret = " + 0);
        }
        return 0;
    }

    private int perfhubConfigSet(int[] tags, int[] values) {
        IBinder service = ServiceManager.checkService("perfhub");
        if (service == null) {
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.os.IPerfHub");
        data.writeInt(tags.length);
        for (int i : tags) {
            data.writeInt(i);
        }
        for (int j : values) {
            data.writeInt(j);
        }
        try {
            service.transact(2, data, reply, 1);
        } catch (RemoteException e) {
            if (HWDBG) {
                Log.d(TAG, "set transact Exception e = " + e);
            }
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        if (HWDBG) {
            Log.d(TAG, "perfhubConfigSet ret = " + 0);
        }
        return 0;
    }

    private int perfhubConfigGet(int[] tags, int[] values) {
        IBinder service = ServiceManager.checkService("perfhub");
        if (service == null) {
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.os.IPerfHub");
        data.writeInt(tags.length);
        for (int i : tags) {
            data.writeInt(i);
        }
        try {
            service.transact(3, data, reply, 0);
            reply.readException();
            for (int loop = 0; loop < tags.length; loop++) {
                values[loop] = reply.readInt();
                if (HWDBG) {
                    Log.d(TAG, "perfhubConfigGet transact values[" + loop + "]  = " + values[loop]);
                }
            }
        } catch (RemoteException e) {
            if (HWDBG) {
                Log.d(TAG, "get transact Exception e = " + e);
            }
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        if (HWDBG) {
            Log.d(TAG, "perfhubConfigGet ret = " + 0);
        }
        return 0;
    }

    private int perfhubCPUSet(int duration, int[] tags, int[] values) {
        ArrayList<Integer> newValues = new ArrayList<>();
        int type = 0;
        if (duration > 0) {
            newValues.add(Integer.valueOf(duration));
            switch (tags[0]) {
                case 1:
                    type = 1;
                    break;
                case 2:
                    type = 2;
                    break;
                case 4:
                    type = 3;
                    break;
                case 5:
                    type = 4;
                    break;
            }
            newValues.add(Integer.valueOf((type << 16) + values[0]));
            int valueSize = newValues.size();
            int[] val = new int[valueSize];
            for (int j = 0; j < valueSize; j++) {
                val[j] = newValues.get(j).intValue();
                if (HWDBG) {
                    Log.d(TAG, "hisi val j = " + j + ", val[j] = " + val[j]);
                }
            }
            return perfhubEvent(4096, "hwperf", val);
        } else if (duration != 0) {
            return -1;
        } else {
            int[] setTags = new int[3];
            int[] setVals = new int[3];
            setTags[0] = tags[0];
            setVals[0] = values[0];
            if (values[3] >= 0 && values[3] < 4) {
                setTags[1] = tags[3];
                setVals[1] = values[3];
            }
            if (values[4] >= 0 && values[4] <= 1) {
                setTags[2] = tags[4];
                setVals[2] = values[4];
            }
            return perfhubConfigSet(setTags, setVals);
        }
    }

    private int perfLockCPUSet(int duration, int[] tags, int[] values) {
        ArrayList<Integer> newValues = new ArrayList<>();
        int type = 0;
        BoostFramework perf = new BoostFramework();
        switch (tags[0]) {
            case 1:
                type = PERF_TAG_L_CPU_MIN_CODE;
                break;
            case 2:
                type = PERF_TAG_L_CPU_MAX_CODE;
                break;
            case 4:
                type = PERF_TAG_B_CPU_MIN_CODE;
                break;
            case 5:
                type = PERF_TAG_B_CPU_MAX_CODE;
                break;
        }
        newValues.add(Integer.valueOf(type));
        newValues.add(Integer.valueOf(values[0] / 10));
        if (values[1] > 0) {
            newValues.add(Integer.valueOf(PERF_TAG_CTL_CPUS_ON_CODE));
            newValues.add(Integer.valueOf(values[1]));
        }
        if (values[2] > 0) {
            newValues.add(Integer.valueOf(PERF_TAG_CTL_CPUS_ON_LIMIT_CODE));
            newValues.add(Integer.valueOf(values[2]));
        }
        int valueSize = newValues.size();
        int[] param = new int[valueSize];
        for (int i = 0; i < valueSize; i++) {
            param[i] = newValues.get(i).intValue();
            if (HWDBG) {
                Log.d(TAG, "qcom param i = " + i + ", param[i] = " + param[i]);
            }
        }
        return perf.perfLockAcquire(duration, param);
    }

    private int perfLockIOSet(boolean isStart, int pid, String pkg_name) {
        BoostFramework perf = new BoostFramework();
        if (isStart) {
            return perf.perfIOPrefetchStart(pid, pkg_name);
        }
        return perf.perfIOPrefetchStop();
    }

    private int handleIOSet(boolean isStart, int pid, String pkg_name) {
        int ret;
        if (1 == usingPlatform) {
            ret = perfLockIOSet(isStart, pid, pkg_name);
        } else {
            ret = -2;
        }
        if (HWDBG) {
            Log.d(TAG, "handleIOSet ret = " + ret);
        }
        return ret;
    }

    private int handleCPUSet(int duration, int[] tags, int[] values) {
        int ret;
        if (HWDBG) {
            Log.d(TAG, "handleCPUSet duration = " + duration + ", usingPlatform = " + usingPlatform);
        }
        if (1 == usingPlatform) {
            ret = perfLockCPUSet(duration, tags, values);
        } else if (2 == usingPlatform) {
            ret = perfhubCPUSet(duration, tags, values);
        } else {
            Log.d(TAG, "handleCPUSet other platform");
            ret = -2;
        }
        if (HWDBG) {
            Log.d(TAG, "handleCPUSet ret = " + ret);
        }
        return ret;
    }

    private int handleEventSet(int eventId, String PackageName, int... payload) {
        int ret;
        if (2 == usingPlatform) {
            ret = perfhubEvent(eventId, PackageName, payload);
            if (ret != 0) {
                ret = -1;
            }
        } else {
            ret = -2;
        }
        if (HWDBG) {
            Log.d(TAG, "handleEventSet ret = " + ret);
        }
        return ret;
    }

    public int perfConfigSet(int[] tags, int[] values, String pkg_name) {
        boolean z;
        int duration;
        int[] iArr = tags;
        String str = pkg_name;
        int eventId = -1;
        int powerType = -1;
        int onBCluster = -1;
        int cpusOn = -1;
        int cpusOnLimit = -1;
        int[] cpuTags = new int[5];
        int[] cpuValues = new int[5];
        int uid = Binder.getCallingUid();
        if (uid != 1000) {
            Log.i(TAG, "Permission denied for the caller is not systemic");
            return -1;
        }
        int payload = -1;
        if (isInputInvalid(tags, values)) {
            return -3;
        }
        if (HWDBG) {
            Log.d(TAG, "perfConfigSet start read param");
        }
        int duration2 = -1;
        int pid = -1;
        int loop = 0;
        while (true) {
            int uid2 = uid;
            if (loop < iArr.length) {
                int i = iArr[loop];
                if (i != 20) {
                    switch (i) {
                        case 0:
                            powerType = values[loop];
                            break;
                        case 1:
                        case 2:
                            cpuTags[0] = iArr[loop];
                            cpuValues[0] = values[loop];
                            break;
                        default:
                            switch (i) {
                                case 4:
                                case 5:
                                    break;
                                default:
                                    switch (i) {
                                        case BastetParameters.HONGBAO_SPEEDUP_STOP:
                                            cpusOn = values[loop];
                                            break;
                                        case 102:
                                            cpusOnLimit = values[loop];
                                            break;
                                        case 103:
                                            eventId = values[loop];
                                            break;
                                        case 104:
                                            payload = values[loop];
                                            break;
                                        case 105:
                                            pid = values[loop];
                                            break;
                                        case CharacterSets.DEFAULT_CHARSET:
                                            duration2 = values[loop];
                                            continue;
                                    }
                            }
                            cpuTags[0] = iArr[loop];
                            cpuValues[0] = values[loop];
                            break;
                    }
                } else {
                    onBCluster = values[loop];
                }
                loop++;
                uid = uid2;
            } else {
                cpuTags[1] = 101;
                cpuValues[1] = cpusOn;
                cpuTags[2] = 102;
                cpuValues[2] = cpusOnLimit;
                cpuTags[3] = 0;
                cpuValues[3] = powerType;
                cpuTags[4] = 20;
                cpuValues[4] = onBCluster;
                if (HWDBG) {
                    Log.d(TAG, "perfConfigSet start handle set");
                }
                int i2 = 0;
                while (i2 < iArr.length) {
                    if (100 == iArr[i2]) {
                        int i3 = values[i2];
                        if (i3 != 12) {
                            switch (i3) {
                                case 0:
                                    duration = duration2;
                                    z = true;
                                    handleIOSet(true, pid, str);
                                    break;
                                case 1:
                                    duration = duration2;
                                    handleIOSet(false, pid, str);
                                    z = true;
                                    continue;
                                case 2:
                                case 3:
                                    duration = duration2;
                                    handleCPUSet(duration, cpuTags, cpuValues);
                                    break;
                                default:
                                    switch (i3) {
                                        case 5:
                                        case 6:
                                            break;
                                        default:
                                            duration = duration2;
                                            break;
                                    }
                                    duration = duration2;
                                    handleCPUSet(duration, cpuTags, cpuValues);
                                    break;
                            }
                            z = true;
                        } else {
                            duration = duration2;
                            z = true;
                            handleEventSet(eventId, str, payload);
                        }
                    } else {
                        duration = duration2;
                        z = true;
                    }
                    i2++;
                    duration2 = duration;
                    int duration3 = z;
                    iArr = tags;
                }
                if (HWDBG) {
                    Log.d(TAG, "perfConfigSet ret = " + 0);
                }
                return 0;
            }
        }
    }

    public int perfConfigGet(int[] tags, int[] values) {
        int ret;
        if (Binder.getCallingUid() != 1000) {
            Log.i(TAG, "Permission denied for the caller is not systemic");
            return -1;
        } else if (isInputInvalid(tags, values)) {
            return -3;
        } else {
            if (1 == usingPlatform) {
                ret = -2;
            } else if (2 != usingPlatform) {
                ret = -2;
            } else if (perfhubConfigGet(tags, values) != 0) {
                ret = -1;
            } else {
                ret = 0;
            }
            if (HWDBG) {
                Log.d(TAG, "perfConfigGet ret = " + ret);
            }
            return ret;
        }
    }
}
