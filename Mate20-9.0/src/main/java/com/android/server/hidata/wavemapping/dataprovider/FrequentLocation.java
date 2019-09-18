package com.android.server.hidata.wavemapping.dataprovider;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.LocationDAO;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.huawei.android.location.activityrecognition.HwActivityChangedEvent;
import com.huawei.android.location.activityrecognition.HwActivityChangedExtendEvent;
import com.huawei.android.location.activityrecognition.HwActivityRecognition;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionExtendEvent;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionHardwareSink;
import com.huawei.android.location.activityrecognition.HwActivityRecognitionServiceConnection;
import com.huawei.android.location.activityrecognition.HwEnvironmentChangedEvent;
import com.huawei.android.location.activityrecognition.OtherParameters;

public class FrequentLocation {
    public static final String TAG = ("WMapping." + FrequentLocation.class.getSimpleName());
    private static FrequentLocation mFrequentLocation = null;
    private HwActivityRecognitionServiceConnection connect = new HwActivityRecognitionServiceConnection() {
        public void onServiceConnected() {
            LogUtil.i("onServiceConnected()");
            boolean unused = FrequentLocation.this.isConnected = true;
            String[] unused2 = FrequentLocation.this.mSupportedEnvironments = FrequentLocation.this.getSupportedEnvironment();
            if (FrequentLocation.this.mSupportedEnvironments == null || FrequentLocation.this.mSupportedEnvironments.length == 0) {
                LogUtil.d("Can't get supported environment.");
            } else {
                for (int i = 0; i < FrequentLocation.this.mSupportedEnvironments.length; i++) {
                    LogUtil.d(" mSupportedEnvironments:" + FrequentLocation.this.mSupportedEnvironments[i]);
                }
            }
            long currentTime = System.currentTimeMillis();
            double param2 = (double) (currentTime >> 32);
            double d = param2;
            OtherParameters params = new OtherParameters((double) (4294967295L & currentTime), param2, 0.0d, 0.0d, "");
            boolean result = FrequentLocation.this.initEnvironmentFunction(HwActivityRecognition.ENV_TYPE_HOME, params);
            LogUtil.d("initEnvironmentFunction ENV_TYPE_HOME:" + result);
            boolean result2 = FrequentLocation.this.initEnvironmentFunction(HwActivityRecognition.ENV_TYPE_OFFICE, params);
            LogUtil.d("initEnvironmentFunction ENV_TYPE_OFFICE:" + result2);
            OtherParameters otherParameters = params;
            boolean result3 = FrequentLocation.this.enableEnvironmentEvent(HwActivityRecognition.ENV_TYPE_HOME, 1, 0, otherParameters);
            LogUtil.d("enableEnvironmentEvent ENV_TYPE_HOME EVENT_TYPE_ENTER:" + result3);
            boolean result4 = FrequentLocation.this.enableEnvironmentEvent(HwActivityRecognition.ENV_TYPE_HOME, 2, 0, otherParameters);
            LogUtil.d("enableEnvironmentEvent ENV_TYPE_HOME EVENT_TYPE_EXIT:" + result4);
            boolean result5 = FrequentLocation.this.enableEnvironmentEvent(HwActivityRecognition.ENV_TYPE_OFFICE, 1, 0, otherParameters);
            LogUtil.d("enableEnvironmentEvent ENV_TYPE_OFFICE EVENT_TYPE_ENTER:" + result5);
            boolean result6 = FrequentLocation.this.enableEnvironmentEvent(HwActivityRecognition.ENV_TYPE_OFFICE, 2, 0, otherParameters);
            LogUtil.d("enableEnvironmentEvent ENV_TYPE_OFFICE EVENT_TYPE_EXIT:" + result6);
        }

        public void onServiceDisconnected() {
            LogUtil.i("onServiceDisconnected()");
            boolean unused = FrequentLocation.this.isConnected = false;
        }
    };
    private HwActivityRecognition hwAR;
    private HwActivityRecognitionHardwareSink hwArSink = new HwActivityRecognitionHardwareSink() {
        public void onActivityChanged(HwActivityChangedEvent activityChangedEvent) {
            LogUtil.i("onActivityChanged .....");
        }

        public void onActivityExtendChanged(HwActivityChangedExtendEvent activityChangedExtendEvent) {
            LogUtil.i("onActivityExtendChanged .....");
        }

        public void onEnvironmentChanged(HwEnvironmentChangedEvent environmentChangedEvent) {
            LogUtil.i("onEnvironmentChanged .....");
            if (environmentChangedEvent != null) {
                for (HwActivityRecognitionExtendEvent event : environmentChangedEvent.getEnvironmentRecognitionEvents()) {
                    if (event != null) {
                        int eventType = event.getEventType();
                        String activityType = event.getActivity();
                        LogUtil.d("HwActivityRecognition callback");
                        if (eventType == 1 && activityType.equals(HwActivityRecognition.ENV_TYPE_HOME)) {
                            FrequentLocation.this.sendNotification(Constant.NAME_FREQLOCATION_HOME, 30);
                        } else if (eventType == 1 && activityType.equals(HwActivityRecognition.ENV_TYPE_OFFICE)) {
                            FrequentLocation.this.sendNotification(Constant.NAME_FREQLOCATION_OFFICE, 30);
                        } else if (eventType == 2 && activityType.equals(HwActivityRecognition.ENV_TYPE_OFFICE)) {
                            FrequentLocation.this.sendNotification(Constant.NAME_FREQLOCATION_OFFICE, 50);
                        } else if (eventType != 2 || !activityType.equals(HwActivityRecognition.ENV_TYPE_HOME)) {
                            LogUtil.d("unknown event");
                        } else {
                            FrequentLocation.this.sendNotification(Constant.NAME_FREQLOCATION_HOME, 50);
                        }
                        LogUtil.i("Activity:" + activityType + " EventType:" + FrequentLocation.this.getEventType(eventType) + " Timestamp:" + event.getTimestampNs() + " OtherParams:" + event.getOtherParams());
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean isConnected = false;
    private String mLocation = Constant.NAME_FREQLOCATION_OTHER;
    private LocationDAO mLocationDAO;
    private Handler mMachineHandler;
    private long mOOBTime = 0;
    /* access modifiers changed from: private */
    public String[] mSupportedEnvironments;
    private ParameterInfo param;

    public boolean isConnected() {
        return this.isConnected;
    }

    public FrequentLocation(Handler handler) {
        LogUtil.i("FrequentLocation");
        this.mMachineHandler = handler;
        this.mLocationDAO = new LocationDAO();
        this.param = ParamManager.getInstance().getParameterInfo();
        this.mOOBTime = System.currentTimeMillis();
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
        LogUtil.d("updateWaveMapping location:" + location + " action:" + action);
        if (action == 0 && location == 0) {
            sendNotification(Constant.NAME_FREQLOCATION_HOME, 30);
        } else if (action == 0 && location == 1) {
            sendNotification(Constant.NAME_FREQLOCATION_OFFICE, 30);
        } else if (action == 1 && location == 0) {
            sendNotification(Constant.NAME_FREQLOCATION_HOME, 50);
        } else if (action == 1 && location == 1) {
            sendNotification(Constant.NAME_FREQLOCATION_OFFICE, 50);
        } else {
            LogUtil.d("UNKNOWN EVENT");
            return false;
        }
        return true;
    }

    public boolean queryFrequentLocationState() {
        LogUtil.i("queryFrequentLocationState");
        this.mLocationDAO.insertOOBTime(this.mOOBTime);
        String freqlocation = this.mLocationDAO.getFrequentLocation();
        if (freqlocation != null && (freqlocation.equals(Constant.NAME_FREQLOCATION_HOME) || freqlocation.equals(Constant.NAME_FREQLOCATION_OFFICE))) {
            this.mLocation = freqlocation;
            LogUtil.d("From database - device is already in:" + this.mLocation);
            Message msg = Message.obtain(this.mMachineHandler, 30);
            Bundle bundle = new Bundle();
            bundle.putCharSequence("LOCATION", this.mLocation);
            msg.setData(bundle);
            this.mMachineHandler.sendMessage(msg);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void sendNotification(String location, int actionMessageID) {
        String oldLocation = this.mLocation;
        if (actionMessageID == 50 && !this.mLocation.equals(location)) {
            LogUtil.d("MSG_OUT_FREQ_LOCATION - location not match mLocation:" + this.mLocation + " location:" + location);
        } else if (actionMessageID != 30 || !this.mLocation.equals(location)) {
            if (actionMessageID == 50) {
                this.mLocation = Constant.NAME_FREQLOCATION_OTHER;
                location = Constant.NAME_FREQLOCATION_OTHER;
            } else {
                this.mLocation = location;
                if (location.equals(Constant.NAME_FREQLOCATION_OTHER)) {
                    actionMessageID = 50;
                }
            }
            long now = System.currentTimeMillis();
            long lastupdatetime = this.mLocationDAO.getlastUpdateTime();
            long benefitCHRTime = this.mLocationDAO.getBenefitCHRTime();
            long maxtime = benefitCHRTime > lastupdatetime ? benefitCHRTime : lastupdatetime;
            if (maxtime > 0 && now > maxtime) {
                int duration_minutes = Math.round(((float) (now - maxtime)) / 60000.0f);
                this.mLocationDAO.accCHRLeavebyFreqLoc(oldLocation);
                this.mLocationDAO.addCHRDurationbyFreqLoc(duration_minutes, oldLocation);
            }
            this.mLocationDAO.insert(this.mLocation);
            this.mLocationDAO.accCHREnterybyFreqLoc(this.mLocation);
            if (actionMessageID == 30 && !oldLocation.equals(location)) {
                LogUtil.i("MSG_IN_FREQ_LOCATION - location is different oldLocation:" + oldLocation + " location:" + location);
                StringBuilder sb = new StringBuilder();
                sb.append("send addtional MSG_OUT_FREQ_LOCATION in advanced for old location:");
                sb.append(oldLocation);
                LogUtil.d(sb.toString());
                Message msg = Message.obtain(this.mMachineHandler, 50);
                Bundle bundle = new Bundle();
                bundle.putCharSequence("LOCATION", oldLocation);
                msg.setData(bundle);
                this.mMachineHandler.sendMessage(msg);
            }
            LogUtil.d("FrequentLocation switch from " + oldLocation + " to " + this.mLocation);
            Message msg2 = Message.obtain(this.mMachineHandler, actionMessageID);
            Bundle bundle2 = new Bundle();
            bundle2.putCharSequence("LOCATION", location);
            msg2.setData(bundle2);
            this.mMachineHandler.sendMessage(msg2);
        } else {
            LogUtil.d("MSG_IN_FREQ_LOCATION - location is the same mLocation:" + this.mLocation + " location:" + location);
        }
    }

    public void connectService(Context context) {
        LogUtil.i("connectService");
        this.hwAR = new HwActivityRecognition(context);
        this.hwAR.connectService(this.hwArSink, this.connect);
    }

    public void disconnectService() {
        if (this.hwAR != null && this.isConnected) {
            LogUtil.i("disconnectService");
            this.hwAR.disconnectService();
            this.isConnected = false;
            this.mSupportedEnvironments = null;
        }
    }

    public String[] getSupportedEnvironment() {
        if (this.hwAR != null) {
            return this.hwAR.getSupportedEnvironments();
        }
        return new String[0];
    }

    public HwEnvironmentChangedEvent getCurrentEnvironment() {
        if (this.hwAR != null) {
            return this.hwAR.getCurrentEnvironment();
        }
        return null;
    }

    public boolean initEnvironmentFunction(String environment, OtherParameters params) {
        if (this.hwAR != null) {
            return this.hwAR.initEnvironmentFunction(environment, params);
        }
        return false;
    }

    public boolean exitEnvironmentFunction(String environment, OtherParameters params) {
        if (this.hwAR != null) {
            return this.hwAR.exitEnvironmentFunction(environment, params);
        }
        return false;
    }

    public boolean enableEnvironmentEvent(String environment, int eventType, long reportLatencyNs, OtherParameters params) {
        if (this.hwAR != null) {
            return this.hwAR.enableEnvironmentEvent(environment, eventType, reportLatencyNs, params);
        }
        return false;
    }

    public boolean disableEnvironmentEvent(String environment, int eventType) {
        if (this.hwAR != null) {
            return this.hwAR.disableEnvironmentEvent(environment, eventType);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public Object getEventType(int type) {
        if (type == 1) {
            return "enter";
        }
        if (type == 2) {
            return "exit";
        }
        return String.valueOf(type);
    }

    public boolean flush() {
        if (this.hwAR != null) {
            return this.hwAR.flush();
        }
        return false;
    }

    public void checkInFrequentLocation(Context context) {
        LogUtil.i("checkInFrequentLocation");
        try {
            HwEnvironmentChangedEvent currentEnvironment = getCurrentEnvironment();
            if (currentEnvironment != null) {
                for (HwActivityRecognitionExtendEvent event : currentEnvironment.getEnvironmentRecognitionEvents()) {
                    if (event != null) {
                        int eventType = event.getEventType();
                        String activityType = event.getActivity();
                        LogUtil.d("Check HwActivityRecognition Activity:" + activityType + " EventType:" + getEventType(eventType));
                        if (eventType == 1 && activityType.equals(HwActivityRecognition.ENV_TYPE_HOME)) {
                            sendNotification(Constant.NAME_FREQLOCATION_HOME, 30);
                        } else if (eventType != 1 || !activityType.equals(HwActivityRecognition.ENV_TYPE_OFFICE)) {
                            sendNotification(Constant.NAME_FREQLOCATION_OTHER, 30);
                        } else {
                            sendNotification(Constant.NAME_FREQLOCATION_OFFICE, 30);
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            LogUtil.e("LocatingState RuntimeException,e" + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("LocatingState,e" + e2.getMessage());
        }
    }
}
