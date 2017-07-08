package com.huawei.hwperformance;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.AppTypeInfo;
import android.telephony.HwVSimManager;
import android.util.BoostFramework;
import android.util.Log;
import com.huawei.connectivitylog.ConnectivityLogManager;
import huawei.android.pfw.HwPFWStartupPackageList;
import huawei.android.telephony.wrapper.HuaweiTelephonyManagerWrapper;
import huawei.android.view.HwMotionEvent;
import huawei.android.view.inputmethod.HwSecImmHelper;
import java.util.ArrayList;

public final class HwPerformanceImpl implements HwPerformance {
    private static final String B_CORE_CPUS = null;
    private static final boolean HWDBG = false;
    private static final boolean HWLOGW_E = true;
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
    private static final Object mLock = null;
    private static int usingPlatform;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hwperformance.HwPerformanceImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hwperformance.HwPerformanceImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwperformance.HwPerformanceImpl.<clinit>():void");
    }

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
                usingPlatform = PLATFORM_HISI;
            } else {
                usingPlatform = PLATFORM_QCOM;
            }
            if (HWDBG) {
                Log.d(TAG, "init using Platform = " + usingPlatform);
            }
        }
    }

    private boolean isInputInvalid(int[] tags, int[] values) {
        boolean ret = HWDBG;
        if (tags == null || values == null) {
            ret = HWLOGW_E;
        } else if (tags.length != values.length) {
            Log.d(TAG, "Input Invalid length not match.");
            ret = HWLOGW_E;
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
        int length = payload.length;
        for (int i = PLATFORM_DEFAULT; i < length; i += PLATFORM_QCOM) {
            data.writeInt(payload[i]);
        }
        try {
            service.transact(PLATFORM_QCOM, data, reply, PLATFORM_QCOM);
        } catch (RemoteException e) {
            Log.d(TAG, "event transact Exception e = " + e);
        } finally {
            data.recycle();
            reply.recycle();
        }
        if (HWDBG) {
            Log.d(TAG, "perfhubEvent ret = " + PLATFORM_DEFAULT);
        }
        return PLATFORM_DEFAULT;
    }

    private int perfhubConfigSet(int[] tags, int[] values) {
        int i = PLATFORM_DEFAULT;
        IBinder service = ServiceManager.checkService("perfhub");
        if (service == null) {
            return -1;
        }
        int i2;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.os.IPerfHub");
        data.writeInt(tags.length);
        int length = tags.length;
        for (i2 = PLATFORM_DEFAULT; i2 < length; i2 += PLATFORM_QCOM) {
            data.writeInt(tags[i2]);
        }
        i2 = values.length;
        while (i < i2) {
            data.writeInt(values[i]);
            i += PLATFORM_QCOM;
        }
        try {
            service.transact(PLATFORM_HISI, data, reply, PLATFORM_QCOM);
        } catch (RemoteException e) {
            Log.d(TAG, "set transact Exception e = " + e);
        } finally {
            data.recycle();
            reply.recycle();
        }
        if (HWDBG) {
            Log.d(TAG, "perfhubConfigSet ret = " + PLATFORM_DEFAULT);
        }
        return PLATFORM_DEFAULT;
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
        int length = tags.length;
        for (int i = PLATFORM_DEFAULT; i < length; i += PLATFORM_QCOM) {
            data.writeInt(tags[i]);
        }
        try {
            service.transact(PERF_CONFIG_GET, data, reply, PLATFORM_DEFAULT);
            reply.readException();
            for (int loop = PLATFORM_DEFAULT; loop < tags.length; loop += PLATFORM_QCOM) {
                values[loop] = reply.readInt();
                if (HWDBG) {
                    Log.d(TAG, "perfhubConfigGet transact values[" + loop + "]  = " + values[loop]);
                }
            }
        } catch (RemoteException e) {
            Log.d(TAG, "get transact Exception e = " + e);
        } finally {
            data.recycle();
            reply.recycle();
        }
        if (HWDBG) {
            Log.d(TAG, "perfhubConfigGet ret = " + PLATFORM_DEFAULT);
        }
        return PLATFORM_DEFAULT;
    }

    private int perfhubCPUSet(int duration, int[] tags, int[] values) {
        ArrayList<Integer> newValues = new ArrayList();
        int type = PLATFORM_DEFAULT;
        if (duration > 0) {
            newValues.add(Integer.valueOf(duration));
            switch (tags[PLATFORM_DEFAULT]) {
                case PLATFORM_QCOM /*1*/:
                    type = PLATFORM_QCOM;
                    break;
                case PLATFORM_HISI /*2*/:
                    type = PLATFORM_HISI;
                    break;
                case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                    type = PERF_CONFIG_GET;
                    break;
                case HwMotionEvent.TOOL_TYPE_FINGER_TIP /*5*/:
                    type = 4;
                    break;
            }
            newValues.add(Integer.valueOf((type << 16) + values[PLATFORM_DEFAULT]));
            int[] val = new int[newValues.size()];
            for (int j = PLATFORM_DEFAULT; j < newValues.size(); j += PLATFORM_QCOM) {
                val[j] = ((Integer) newValues.get(j)).intValue();
                if (HWDBG) {
                    Log.d(TAG, "hisi val j = " + j + ", val[j] = " + val[j]);
                }
            }
            return perfhubEvent(HwSecImmHelper.SECURE_IME_NO_HIDE_FLAG, "hwperf", val);
        } else if (duration != 0) {
            return -1;
        } else {
            int[] setTags = new int[PERF_CONFIG_GET];
            int[] setVals = new int[PERF_CONFIG_GET];
            setTags[PLATFORM_DEFAULT] = tags[PLATFORM_DEFAULT];
            setVals[PLATFORM_DEFAULT] = values[PLATFORM_DEFAULT];
            if (values[PERF_CONFIG_GET] >= 0 && values[PERF_CONFIG_GET] < 4) {
                setTags[PLATFORM_QCOM] = tags[PERF_CONFIG_GET];
                setVals[PLATFORM_QCOM] = values[PERF_CONFIG_GET];
            }
            if (values[4] >= 0 && values[4] <= PLATFORM_QCOM) {
                setTags[PLATFORM_HISI] = tags[4];
                setVals[PLATFORM_HISI] = values[4];
            }
            return perfhubConfigSet(setTags, setVals);
        }
    }

    private int perfLockCPUSet(int duration, int[] tags, int[] values) {
        ArrayList<Integer> newValues = new ArrayList();
        int type = PLATFORM_DEFAULT;
        BoostFramework perf = new BoostFramework();
        switch (tags[PLATFORM_DEFAULT]) {
            case PLATFORM_QCOM /*1*/:
                type = PERF_TAG_L_CPU_MIN_CODE;
                break;
            case PLATFORM_HISI /*2*/:
                type = PERF_TAG_L_CPU_MAX_CODE;
                break;
            case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                type = PERF_TAG_B_CPU_MIN_CODE;
                break;
            case HwMotionEvent.TOOL_TYPE_FINGER_TIP /*5*/:
                type = PERF_TAG_B_CPU_MAX_CODE;
                break;
        }
        newValues.add(Integer.valueOf(type));
        newValues.add(Integer.valueOf(values[PLATFORM_DEFAULT] / 10));
        if (values[PLATFORM_QCOM] > 0) {
            newValues.add(Integer.valueOf(PERF_TAG_CTL_CPUS_ON_CODE));
            newValues.add(Integer.valueOf(values[PLATFORM_QCOM]));
        }
        if (values[PLATFORM_HISI] > 0) {
            newValues.add(Integer.valueOf(PERF_TAG_CTL_CPUS_ON_LIMIT_CODE));
            newValues.add(Integer.valueOf(values[PLATFORM_HISI]));
        }
        int[] param = new int[newValues.size()];
        for (int i = PLATFORM_DEFAULT; i < newValues.size(); i += PLATFORM_QCOM) {
            param[i] = ((Integer) newValues.get(i)).intValue();
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
        if (PLATFORM_QCOM == usingPlatform) {
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
        if (PLATFORM_QCOM == usingPlatform) {
            ret = perfLockCPUSet(duration, tags, values);
        } else if (PLATFORM_HISI == usingPlatform) {
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
        if (PLATFORM_HISI == usingPlatform) {
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
        int pid = -1;
        int payload = -1;
        int eventId = -1;
        int duration = -1;
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
            int loop = PLATFORM_DEFAULT;
            while (true) {
                int length = tags.length;
                if (loop < r0) {
                    switch (tags[loop]) {
                        case PLATFORM_DEFAULT /*0*/:
                            powerType = values[loop];
                            break;
                        case PLATFORM_QCOM /*1*/:
                        case PLATFORM_HISI /*2*/:
                        case HwPFWStartupPackageList.STARTUP_LIST_TYPE_MUST_CONTROL_APPS /*4*/:
                        case HwMotionEvent.TOOL_TYPE_FINGER_TIP /*5*/:
                            cpuTags[PLATFORM_DEFAULT] = tags[loop];
                            cpuValues[PLATFORM_DEFAULT] = values[loop];
                            break;
                        case HuaweiTelephonyManagerWrapper.SINGLE_MODE_USIM_CARD /*20*/:
                            onBCluster = values[loop];
                            break;
                        case ConnectivityLogManager.WIFI_USER_CONNECT /*101*/:
                            cpusOn = values[loop];
                            break;
                        case ConnectivityLogManager.WIFI_ACCESS_WEB_SLOWLY /*102*/:
                            cpusOnLimit = values[loop];
                            break;
                        case ConnectivityLogManager.WIFI_POOR_LEVEL /*103*/:
                            eventId = values[loop];
                            break;
                        case ConnectivityLogManager.WIFI_ACCESS_WEB_SLOWLY_EX /*104*/:
                            payload = values[loop];
                            break;
                        case AppTypeInfo.APP_TYPE_EBOOK /*105*/:
                            pid = values[loop];
                            break;
                        case CharacterSets.DEFAULT_CHARSET /*106*/:
                            duration = values[loop];
                            break;
                        default:
                            break;
                    }
                    loop += PLATFORM_QCOM;
                } else {
                    cpuTags[PLATFORM_QCOM] = ConnectivityLogManager.WIFI_USER_CONNECT;
                    cpuValues[PLATFORM_QCOM] = cpusOn;
                    cpuTags[PLATFORM_HISI] = ConnectivityLogManager.WIFI_ACCESS_WEB_SLOWLY;
                    cpuValues[PLATFORM_HISI] = cpusOnLimit;
                    cpuTags[PERF_CONFIG_GET] = PLATFORM_DEFAULT;
                    cpuValues[PERF_CONFIG_GET] = powerType;
                    cpuTags[4] = 20;
                    cpuValues[4] = onBCluster;
                    if (HWDBG) {
                        Log.d(TAG, "perfConfigSet start handle set");
                    }
                    int i = PLATFORM_DEFAULT;
                    while (true) {
                        length = tags.length;
                        if (i < r0) {
                            if (100 == tags[i]) {
                                switch (values[i]) {
                                    case PLATFORM_DEFAULT /*0*/:
                                        handleIOSet(HWLOGW_E, pid, pkg_name);
                                        break;
                                    case PLATFORM_QCOM /*1*/:
                                        handleIOSet(HWDBG, pid, pkg_name);
                                        break;
                                    case PLATFORM_HISI /*2*/:
                                    case PERF_CONFIG_GET /*3*/:
                                    case HwMotionEvent.TOOL_TYPE_FINGER_TIP /*5*/:
                                    case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
                                        handleCPUSet(duration, cpuTags, cpuValues);
                                        break;
                                    case HwVSimManager.NETWORK_TYPE_EVDO_B /*12*/:
                                        int[] iArr = new int[PLATFORM_QCOM];
                                        iArr[PLATFORM_DEFAULT] = payload;
                                        handleEventSet(eventId, pkg_name, iArr);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            i += PLATFORM_QCOM;
                        } else {
                            if (HWDBG) {
                                Log.d(TAG, "perfConfigSet ret = " + PLATFORM_DEFAULT);
                            }
                            return PLATFORM_DEFAULT;
                        }
                    }
                }
            }
        }
    }

    public int perfConfigGet(int[] tags, int[] values) {
        if (Binder.getCallingUid() != 1000) {
            Log.i(TAG, "Permission denied for the caller is not systemic");
            return -1;
        } else if (isInputInvalid(tags, values)) {
            return -3;
        } else {
            int ret;
            if (PLATFORM_QCOM == usingPlatform) {
                ret = -2;
            } else if (PLATFORM_HISI != usingPlatform) {
                ret = -2;
            } else if (perfhubConfigGet(tags, values) != 0) {
                ret = -1;
            } else {
                ret = PLATFORM_DEFAULT;
            }
            if (HWDBG) {
                Log.d(TAG, "perfConfigGet ret = " + ret);
            }
            return ret;
        }
    }
}
