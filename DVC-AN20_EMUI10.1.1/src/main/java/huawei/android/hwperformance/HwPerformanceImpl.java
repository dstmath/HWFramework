package huawei.android.hwperformance;

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
    private static final boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
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

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0035, code lost:
        r1 = th;
     */
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
        return;
        while (true) {
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
                Log.e(TAG, "event transact Exception e = " + e.toString());
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
                Log.e(TAG, "set transact Exception e = " + e.toString());
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
                Log.e(TAG, "get transact Exception e = " + e.toString());
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
            int i = tags[0];
            if (i == 1) {
                type = 1;
            } else if (i == 2) {
                type = 2;
            } else if (i == 4) {
                type = 3;
            } else if (i == 5) {
                type = 4;
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
        int i = tags[0];
        if (i == 1) {
            type = PERF_TAG_L_CPU_MIN_CODE;
        } else if (i == 2) {
            type = PERF_TAG_L_CPU_MAX_CODE;
        } else if (i == 4) {
            type = PERF_TAG_B_CPU_MIN_CODE;
        } else if (i == 5) {
            type = PERF_TAG_B_CPU_MAX_CODE;
        }
        newValues.add(Integer.valueOf(type));
        newValues.add(Integer.valueOf(values[0] / 10));
        if (values[1] > 0) {
            newValues.add(Integer.valueOf((int) PERF_TAG_CTL_CPUS_ON_CODE));
            newValues.add(Integer.valueOf(values[1]));
        }
        if (values[2] > 0) {
            newValues.add(Integer.valueOf((int) PERF_TAG_CTL_CPUS_ON_LIMIT_CODE));
            newValues.add(Integer.valueOf(values[2]));
        }
        int valueSize = newValues.size();
        int[] param = new int[valueSize];
        for (int i2 = 0; i2 < valueSize; i2++) {
            param[i2] = newValues.get(i2).intValue();
            if (HWDBG) {
                Log.d(TAG, "qcom param i = " + i2 + ", param[i] = " + param[i2]);
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
        int i = usingPlatform;
        if (1 == i) {
            ret = perfLockCPUSet(duration, tags, values);
        } else if (2 == i) {
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

    /* JADX INFO: Multiple debug info for r3v29 int: [D('cpusOn' int), D('onBCluster' int)] */
    /* JADX INFO: Multiple debug info for r3v30 int: [D('cpusOn' int), D('cpusOnLimit' int)] */
    /* JADX INFO: Multiple debug info for r3v31 int: [D('eventId' int), D('cpusOnLimit' int)] */
    /* JADX INFO: Multiple debug info for r3v32 int: [D('eventId' int), D('payload' int)] */
    /* JADX INFO: Multiple debug info for r3v33 int: [D('pid' int), D('payload' int)] */
    /* JADX INFO: Multiple debug info for r3v34 int: [D('pid' int), D('duration' int)] */
    public int perfConfigSet(int[] tags, int[] values, String pkg_name) {
        int pid;
        boolean z;
        int ret = 0;
        int payload = -1;
        int eventId = -1;
        int powerType = -1;
        int onBCluster = -1;
        int cpusOn = -1;
        int cpusOnLimit = -1;
        int[] cpuTags = new int[5];
        int[] cpuValues = new int[5];
        if (Binder.getCallingUid() != 1000) {
            Log.i(TAG, "Permission denied for the caller is not systemic");
            return -1;
        } else if (isInputInvalid(tags, values)) {
            return -3;
        } else {
            if (HWDBG) {
                Log.d(TAG, "perfConfigSet start read param");
            }
            int loop = 0;
            int duration = -1;
            int pid2 = -1;
            while (true) {
                int pid3 = pid2;
                if (loop < tags.length) {
                    int i = tags[loop];
                    if (i != 0) {
                        if (i == 1 || i == 2 || i == 4 || i == 5) {
                            cpuTags[0] = tags[loop];
                            cpuValues[0] = values[loop];
                        } else if (i != 20) {
                            switch (i) {
                                case BastetParameters.HONGBAO_SPEEDUP_STOP:
                                    cpusOn = values[loop];
                                    pid2 = pid3;
                                    break;
                                case 102:
                                    cpusOnLimit = values[loop];
                                    pid2 = pid3;
                                    break;
                                case 103:
                                    eventId = values[loop];
                                    pid2 = pid3;
                                    break;
                                case 104:
                                    payload = values[loop];
                                    pid2 = pid3;
                                    break;
                                case 105:
                                    pid2 = values[loop];
                                    break;
                                case 106:
                                    duration = values[loop];
                                    pid2 = pid3;
                                    break;
                            }
                        } else {
                            onBCluster = values[loop];
                            pid2 = pid3;
                        }
                        pid2 = pid3;
                    } else {
                        powerType = values[loop];
                        pid2 = pid3;
                    }
                    loop++;
                    ret = ret;
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
                    while (i2 < tags.length) {
                        if (100 == tags[i2]) {
                            int i3 = values[i2];
                            if (i3 == 0) {
                                pid = pid3;
                                z = true;
                                handleIOSet(true, pid, pkg_name);
                            } else if (i3 == 1) {
                                pid = pid3;
                                handleIOSet(false, pid, pkg_name);
                                z = true;
                            } else if (i3 == 2 || i3 == 3 || i3 == 5 || i3 == 6) {
                                handleCPUSet(duration, cpuTags, cpuValues);
                                pid = pid3;
                                z = true;
                            } else if (i3 != 12) {
                                pid = pid3;
                                z = true;
                            } else {
                                handleEventSet(eventId, pkg_name, payload);
                                pid = pid3;
                                z = true;
                            }
                        } else {
                            pid = pid3;
                            z = true;
                        }
                        i2++;
                        pid3 = pid;
                    }
                    if (!HWDBG) {
                        return ret;
                    }
                    Log.d(TAG, "perfConfigSet ret = " + ret);
                    return ret;
                }
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
            int i = usingPlatform;
            if (1 == i) {
                ret = -2;
            } else if (2 != i) {
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
