package com.android.server.hidata.wavemapping.dataprovider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.LocationDao;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class FrequentLocation {
    private static final String KEY_LOCATION = "LOCATION";
    public static final String TAG = ("WMapping." + FrequentLocation.class.getSimpleName());
    private static FrequentLocation mFrequentLocation = null;
    private String mLocation = Constant.NAME_FREQLOCATION_OTHER;
    private LocationDao mLocationDao;
    private Handler mMachineHandler;
    private long mOobTime = 0;
    private ParameterInfo param;

    public FrequentLocation(Handler handler) {
        LogUtil.i(false, "FrequentLocation", new Object[0]);
        this.mMachineHandler = handler;
        this.mLocationDao = new LocationDao();
        this.param = ParamManager.getInstance().getParameterInfo();
        this.mOobTime = System.currentTimeMillis();
    }

    public static synchronized FrequentLocation getInstance(Handler handler) {
        FrequentLocation frequentLocation;
        synchronized (FrequentLocation.class) {
            if (mFrequentLocation == null) {
                mFrequentLocation = new FrequentLocation(handler);
            }
            frequentLocation = mFrequentLocation;
        }
        return frequentLocation;
    }

    public static synchronized FrequentLocation getInstance() {
        FrequentLocation frequentLocation;
        synchronized (FrequentLocation.class) {
            frequentLocation = mFrequentLocation;
        }
        return frequentLocation;
    }

    public boolean updateWaveMapping(int location, int action) {
        LogUtil.d(false, "updateWaveMapping location:%{public}d action:%{public}d", Integer.valueOf(location), Integer.valueOf(action));
        if (action == 0 && location == 0) {
            sendNotification(Constant.NAME_FREQLOCATION_HOME, 30);
        } else if (action == 0 && location == 1) {
            sendNotification(Constant.NAME_FREQLOCATION_OFFICE, 30);
        } else if (action == 1 && location == 0) {
            sendNotification(Constant.NAME_FREQLOCATION_HOME, 50);
        } else if (action == 1 && location == 1) {
            sendNotification(Constant.NAME_FREQLOCATION_OFFICE, 50);
        } else {
            LogUtil.d(false, "UNKNOWN EVENT", new Object[0]);
            return false;
        }
        return true;
    }

    public boolean queryFrequentLocationState() {
        LogUtil.i(false, "queryFrequentLocationState", new Object[0]);
        this.mLocationDao.insertOobTime(this.mOobTime);
        String freqlocation = this.mLocationDao.getFrequentLocation();
        if (freqlocation != null && (Constant.NAME_FREQLOCATION_HOME.equals(freqlocation) || Constant.NAME_FREQLOCATION_OFFICE.equals(freqlocation))) {
            this.mLocation = freqlocation;
            LogUtil.d(false, "From database - device is already in:%{private}s", this.mLocation);
            Message msg = Message.obtain(this.mMachineHandler, 30);
            Bundle bundle = new Bundle();
            bundle.putCharSequence(KEY_LOCATION, this.mLocation);
            msg.setData(bundle);
            this.mMachineHandler.sendMessage(msg);
        }
        return true;
    }

    private void sendNotification(String location, int actionMessageId) {
        String tempLocation = location;
        int tempActionMessageId = actionMessageId;
        String oldLocation = this.mLocation;
        if (tempActionMessageId == 50 && !tempLocation.equals(this.mLocation)) {
            LogUtil.d(false, "MSG_OUT_FREQ_LOCATION - location not match mLocation:%{private}s location:%{private}s", this.mLocation, tempLocation);
        } else if (tempActionMessageId != 30 || !tempLocation.equals(this.mLocation)) {
            if (tempActionMessageId == 50) {
                this.mLocation = Constant.NAME_FREQLOCATION_OTHER;
                tempLocation = Constant.NAME_FREQLOCATION_OTHER;
            } else {
                this.mLocation = tempLocation;
                if (tempLocation.equals(Constant.NAME_FREQLOCATION_OTHER)) {
                    tempActionMessageId = 50;
                }
            }
            long now = System.currentTimeMillis();
            long lastUpdateTime = this.mLocationDao.getLastUpdateTime();
            long benefitChrTime = this.mLocationDao.getBenefitChrTime();
            long maxTime = benefitChrTime > lastUpdateTime ? benefitChrTime : lastUpdateTime;
            if (maxTime > 0 && now > maxTime) {
                int durationMinutes = Math.round(((float) (now - maxTime)) / 60000.0f);
                this.mLocationDao.accChrLeaveByFreqLoc(oldLocation);
                this.mLocationDao.addChrDurationByFreqLoc(durationMinutes, oldLocation);
            }
            this.mLocationDao.insert(this.mLocation);
            this.mLocationDao.accChrEnterByFreqLoc(this.mLocation);
            if (tempActionMessageId == 30 && !oldLocation.equals(tempLocation)) {
                LogUtil.i(false, "MSG_IN_FREQ_LOCATION - location is different oldLocation:%{private}s location:%{private}s", oldLocation, tempLocation);
                Message msg = Message.obtain(this.mMachineHandler, 50);
                Bundle bundle = new Bundle();
                bundle.putCharSequence(KEY_LOCATION, oldLocation);
                msg.setData(bundle);
                this.mMachineHandler.sendMessage(msg);
            }
            LogUtil.d(false, "FrequentLocation switch from %{private}s to %{private}s", oldLocation, this.mLocation);
            Message msg2 = Message.obtain(this.mMachineHandler, tempActionMessageId);
            Bundle bundle2 = new Bundle();
            bundle2.putCharSequence(KEY_LOCATION, tempLocation);
            msg2.setData(bundle2);
            this.mMachineHandler.sendMessage(msg2);
        } else {
            LogUtil.d(false, "MSG_IN_FREQ_LOCATION - location is the same mLocation:%{private}s location:%{private}s", this.mLocation, tempLocation);
        }
    }
}
