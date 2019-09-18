package com.android.internal.telephony;

import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TelephonyDevController extends Handler {
    private static final boolean DBG = true;
    private static final int EVENT_HARDWARE_CONFIG_CHANGED = 1;
    private static final String LOG_TAG = "TDC";
    private static final Object mLock = new Object();
    private static ArrayList<HardwareConfig> mModems = new ArrayList<>();
    private static ArrayList<HardwareConfig> mSims = new ArrayList<>();
    private static Message sRilHardwareConfig;
    private static TelephonyDevController sTelephonyDevController;

    private static void logd(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private static void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    public static TelephonyDevController create() {
        TelephonyDevController telephonyDevController;
        synchronized (mLock) {
            if (sTelephonyDevController == null) {
                sTelephonyDevController = new TelephonyDevController();
                telephonyDevController = sTelephonyDevController;
            } else {
                throw new RuntimeException("TelephonyDevController already created!?!");
            }
        }
        return telephonyDevController;
    }

    public static TelephonyDevController getInstance() {
        TelephonyDevController telephonyDevController;
        synchronized (mLock) {
            if (sTelephonyDevController != null) {
                telephonyDevController = sTelephonyDevController;
            } else {
                throw new RuntimeException("TelephonyDevController not yet created!?!");
            }
        }
        return telephonyDevController;
    }

    private void initFromResource() {
        String[] hwStrings = Resources.getSystem().getStringArray(17236040);
        if (hwStrings != null) {
            for (String hwString : hwStrings) {
                HardwareConfig hw = new HardwareConfig(hwString);
                if (hw.type == 0) {
                    updateOrInsert(hw, mModems);
                } else if (hw.type == 1) {
                    updateOrInsert(hw, mSims);
                }
            }
        }
    }

    private TelephonyDevController() {
        initFromResource();
        mModems.trimToSize();
        mSims.trimToSize();
    }

    public static void registerRIL(CommandsInterface cmdsIf) {
        cmdsIf.getHardwareConfig(sRilHardwareConfig);
        if (sRilHardwareConfig != null) {
            AsyncResult ar = (AsyncResult) sRilHardwareConfig.obj;
            if (ar.exception == null) {
                handleGetHardwareConfigChanged(ar);
            }
        }
        cmdsIf.registerForHardwareConfigChanged(sTelephonyDevController, 1, null);
    }

    public static void unregisterRIL(CommandsInterface cmdsIf) {
        cmdsIf.unregisterForHardwareConfigChanged(sTelephonyDevController);
    }

    public void handleMessage(Message msg) {
        if (msg.what != 1) {
            loge("handleMessage: Unknown Event " + msg.what);
            return;
        }
        logd("handleMessage: received EVENT_HARDWARE_CONFIG_CHANGED");
        handleGetHardwareConfigChanged((AsyncResult) msg.obj);
    }

    private static void updateOrInsert(HardwareConfig hw, ArrayList<HardwareConfig> list) {
        int size;
        synchronized (mLock) {
            size = list.size();
            int i = 0;
            while (true) {
                if (i >= size) {
                    break;
                }
                HardwareConfig item = list.get(i);
                if (item.uuid.compareTo(hw.uuid) == 0) {
                    logd("updateOrInsert: removing: " + item);
                    list.remove(i);
                    break;
                }
                i++;
            }
            logd("updateOrInsert: inserting: " + hw);
            list.add(hw);
        }
        int i2 = size;
    }

    private static void handleGetHardwareConfigChanged(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("handleGetHardwareConfigChanged - returned an error.");
            return;
        }
        List hwcfg = (List) ar.result;
        for (int i = 0; i < hwcfg.size(); i++) {
            HardwareConfig hw = (HardwareConfig) hwcfg.get(i);
            if (hw != null) {
                if (hw.type == 0) {
                    updateOrInsert(hw, mModems);
                } else if (hw.type == 1) {
                    updateOrInsert(hw, mSims);
                }
            }
        }
    }

    public static int getModemCount() {
        int count;
        synchronized (mLock) {
            count = mModems.size();
            logd("getModemCount: " + count);
        }
        return count;
    }

    public HardwareConfig getModem(int index) {
        synchronized (mLock) {
            if (mModems.isEmpty()) {
                loge("getModem: no registered modem device?!?");
                return null;
            } else if (index > getModemCount()) {
                loge("getModem: out-of-bounds access for modem device " + index + " max: " + getModemCount());
                return null;
            } else {
                logd("getModem: " + index);
                HardwareConfig hardwareConfig = mModems.get(index);
                return hardwareConfig;
            }
        }
    }

    public int getSimCount() {
        int count;
        synchronized (mLock) {
            count = mSims.size();
            logd("getSimCount: " + count);
        }
        return count;
    }

    public HardwareConfig getSim(int index) {
        synchronized (mLock) {
            if (mSims.isEmpty()) {
                loge("getSim: no registered sim device?!?");
                return null;
            } else if (index > getSimCount()) {
                loge("getSim: out-of-bounds access for sim device " + index + " max: " + getSimCount());
                return null;
            } else {
                logd("getSim: " + index);
                HardwareConfig hardwareConfig = mSims.get(index);
                return hardwareConfig;
            }
        }
    }

    public HardwareConfig getModemForSim(int simIndex) {
        synchronized (mLock) {
            if (!mModems.isEmpty()) {
                if (!mSims.isEmpty()) {
                    if (simIndex > getSimCount()) {
                        loge("getModemForSim: out-of-bounds access for sim device " + simIndex + " max: " + getSimCount());
                        return null;
                    }
                    logd("getModemForSim " + simIndex);
                    HardwareConfig sim = getSim(simIndex);
                    Iterator<HardwareConfig> it = mModems.iterator();
                    while (it.hasNext()) {
                        HardwareConfig modem = it.next();
                        if (modem.uuid.equals(sim.modemUuid)) {
                            return modem;
                        }
                    }
                    return null;
                }
            }
            loge("getModemForSim: no registered modem/sim device?!?");
            return null;
        }
    }

    public ArrayList<HardwareConfig> getAllSimsForModem(int modemIndex) {
        synchronized (mLock) {
            if (mSims.isEmpty()) {
                loge("getAllSimsForModem: no registered sim device?!?");
                return null;
            } else if (modemIndex > getModemCount()) {
                loge("getAllSimsForModem: out-of-bounds access for modem device " + modemIndex + " max: " + getModemCount());
                return null;
            } else {
                logd("getAllSimsForModem " + modemIndex);
                ArrayList<HardwareConfig> result = new ArrayList<>();
                HardwareConfig modem = getModem(modemIndex);
                Iterator<HardwareConfig> it = mSims.iterator();
                while (it.hasNext()) {
                    HardwareConfig sim = it.next();
                    if (sim.modemUuid.equals(modem.uuid)) {
                        result.add(sim);
                    }
                }
                return result;
            }
        }
    }

    public ArrayList<HardwareConfig> getAllModems() {
        ArrayList<HardwareConfig> modems;
        synchronized (mLock) {
            modems = new ArrayList<>();
            if (mModems.isEmpty()) {
                logd("getAllModems: empty list.");
            } else {
                Iterator<HardwareConfig> it = mModems.iterator();
                while (it.hasNext()) {
                    modems.add(it.next());
                }
            }
        }
        return modems;
    }

    public ArrayList<HardwareConfig> getAllSims() {
        ArrayList<HardwareConfig> sims;
        synchronized (mLock) {
            sims = new ArrayList<>();
            if (mSims.isEmpty()) {
                logd("getAllSims: empty list.");
            } else {
                Iterator<HardwareConfig> it = mSims.iterator();
                while (it.hasNext()) {
                    sims.add(it.next());
                }
            }
        }
        return sims;
    }
}
