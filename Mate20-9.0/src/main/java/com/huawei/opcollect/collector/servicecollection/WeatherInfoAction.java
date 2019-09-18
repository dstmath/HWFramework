package com.huawei.opcollect.collector.servicecollection;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.android.totemweather.aidl.IRequestCallBack;
import com.huawei.android.totemweather.aidl.IRequestCityWeather;
import com.huawei.android.totemweather.aidl.RequestData;
import com.huawei.nb.model.collectencrypt.RawWeatherInfo;
import com.huawei.opcollect.location.HwLocation;
import com.huawei.opcollect.location.ILocationListener;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.strategy.OdmfActionManager;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import com.huawei.opcollect.utils.WeatherDataUtil;
import com.huawei.opcollect.weather.HwWeatherData;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Date;

public class WeatherInfoAction extends Action implements ILocationListener {
    private static final int MESSAGE_ON_CHANGE = 1;
    private static final int MSG_SERVICE_CONNECTED = 3;
    private static final int MSG_SERVICE_DISCONNECTED = 4;
    private static final int MSG_TIME_OUT = 2;
    private static final long REQUEST_TIME_OUT = 90000;
    private static final String TAG = "WeatherInfoAction";
    private static final String WEATHER_AIDL_SERVICE_ACTION = "com.huawei.totemweather.action.THIRD_REQUEST_WEATHER";
    private static final String WEATHER_PACKAGE_NAME = "com.huawei.android.totemweather";
    private static final long WEATHER_VALID_TIME = 1800000;
    private static WeatherInfoAction sInstance = null;
    private boolean isRequesting = false;
    /* access modifiers changed from: private */
    public Handler mHandler = null;
    private HwLocation mHwLocation = null;
    /* access modifiers changed from: private */
    public HwWeatherData mHwWeatherData = null;
    private long mLastRequestTime = 0;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public IRequestCityWeather mRequestWeather = null;
    private ServiceConnection mWeatherConnection = null;
    /* access modifiers changed from: private */
    public IRequestCallBack mWeatherRequestCallBack = null;

    private static class MyServiceConnection implements ServiceConnection {
        private final WeakReference<WeatherInfoAction> mService;

        MyServiceConnection(WeatherInfoAction action) {
            this.mService = new WeakReference<>(action);
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            OPCollectLog.r(WeatherInfoAction.TAG, "onServiceConnected ok");
            WeatherInfoAction action = (WeatherInfoAction) this.mService.get();
            if (action != null) {
                synchronized (action.mLock) {
                    if (action.mHandler != null) {
                        action.mHandler.obtainMessage(3, service).sendToTarget();
                    }
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            WeatherInfoAction action = (WeatherInfoAction) this.mService.get();
            if (action != null) {
                synchronized (action.mLock) {
                    if (action.mHandler != null) {
                        action.mHandler.sendEmptyMessage(4);
                    }
                }
                OPCollectLog.r(WeatherInfoAction.TAG, "onServiceDisconnected");
            }
        }
    }

    private static class MyWeatherRequestCallBack extends IRequestCallBack.Stub {
        private final WeakReference<WeatherInfoAction> mService;

        MyWeatherRequestCallBack(WeatherInfoAction action) {
            this.mService = new WeakReference<>(action);
        }

        public void onRequestResult(String weatherJsonData, RequestData requestData) throws RemoteException {
            WeatherInfoAction action = (WeatherInfoAction) this.mService.get();
            if (action != null) {
                synchronized (action.mLock) {
                    if (action.mHandler != null) {
                        action.mHandler.removeMessages(2);
                    }
                }
                if (TextUtils.isEmpty(weatherJsonData)) {
                    action.onWeatherFail();
                    return;
                }
                HwWeatherData hwWeatherData = WeatherDataUtil.parserWeather(weatherJsonData);
                if (hwWeatherData == null) {
                    action.onWeatherFail();
                    return;
                }
                synchronized (action.mLock) {
                    if (action.mHandler != null) {
                        action.mHandler.obtainMessage(1, hwWeatherData).sendToTarget();
                    }
                }
            }
        }
    }

    private static class WeatherInfoHandler extends Handler {
        private final WeakReference<WeatherInfoAction> service;

        WeatherInfoHandler(WeatherInfoAction service2) {
            this.service = new WeakReference<>(service2);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WeatherInfoAction action = (WeatherInfoAction) this.service.get();
            if (action == null) {
                OPCollectLog.e(WeatherInfoAction.TAG, "action is null");
            } else if (msg.what == 1) {
                HwWeatherData unused = action.mHwWeatherData = (HwWeatherData) msg.obj;
                action.perform();
            } else if (msg.what == 2) {
                OPCollectLog.e(WeatherInfoAction.TAG, "timeout");
                action.onWeatherFail();
            } else if (msg.what == 3) {
                try {
                    IRequestCityWeather unused2 = action.mRequestWeather = IRequestCityWeather.Stub.asInterface((IBinder) msg.obj);
                    if (action.mRequestWeather != null && action.mContext != null) {
                        action.mRequestWeather.registerCallBack(action.mWeatherRequestCallBack, action.mContext.getPackageName());
                    }
                } catch (RemoteException e) {
                    OPCollectLog.e(WeatherInfoAction.TAG, "registerCallBack failed: " + e.getMessage());
                }
            } else if (msg.what == 4) {
                IRequestCityWeather unused3 = action.mRequestWeather = null;
            }
        }
    }

    public static synchronized WeatherInfoAction getInstance(Context context) {
        WeatherInfoAction weatherInfoAction;
        synchronized (WeatherInfoAction.class) {
            if (sInstance == null) {
                sInstance = new WeatherInfoAction(context, OPCollectConstant.WEATHER_ACTION_NAME);
            }
            weatherInfoAction = sInstance;
        }
        return weatherInfoAction;
    }

    private WeatherInfoAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(queryDailyRecordNum(RawWeatherInfo.class));
    }

    public void enable() {
        super.enable();
        synchronized (this.mLock) {
            if (this.mHandler == null) {
                this.mHandler = new WeatherInfoHandler(this);
            }
        }
        if (this.mWeatherConnection == null) {
            this.mWeatherConnection = new MyServiceConnection(this);
        }
        if (this.mWeatherRequestCallBack == null) {
            this.mWeatherRequestCallBack = new MyWeatherRequestCallBack(this);
        }
        bindWeatherService();
        LocationRecordAction.getInstance(this.mContext).addLocationListener(OPCollectConstant.WEATHER_ACTION_NAME, this);
    }

    public void disable() {
        super.disable();
        OPCollectLog.r(TAG, "disable");
        synchronized (this.mLock) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(1);
                this.mHandler.removeMessages(2);
                this.mHandler = null;
            }
        }
        OdmfActionManager.getInstance().removeLocationListener(OPCollectConstant.WEATHER_ACTION_NAME, this);
        if (this.mRequestWeather != null && this.mContext != null) {
            try {
                this.mRequestWeather.unregisterCallBack(this.mWeatherRequestCallBack, this.mContext.getPackageName());
            } catch (RemoteException e) {
                OPCollectLog.e(TAG, "unRegisterCallback exception.");
            }
            if (this.mWeatherConnection != null) {
                try {
                    this.mContext.unbindService(this.mWeatherConnection);
                } catch (RuntimeException e2) {
                    OPCollectLog.e(TAG, "unbindService failed: " + e2.getMessage());
                }
            }
            this.mRequestWeather = null;
            this.mWeatherRequestCallBack = null;
            this.mWeatherConnection = null;
        }
    }

    private void bindWeatherService() {
        OPCollectLog.r(TAG, "binder service");
        if (this.mContext != null && this.mWeatherConnection != null) {
            Intent weatherIntent = new Intent(WEATHER_AIDL_SERVICE_ACTION);
            weatherIntent.setAction(WEATHER_AIDL_SERVICE_ACTION);
            weatherIntent.setPackage(WEATHER_PACKAGE_NAME);
            try {
                this.mContext.bindService(weatherIntent, this.mWeatherConnection, 1);
            } catch (RuntimeException e) {
                OPCollectLog.e(TAG, "bindService failed: " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        if (this.mHwWeatherData == null) {
            onWeatherFail();
            return false;
        }
        RawWeatherInfo rawWeatherInfo = new RawWeatherInfo();
        rawWeatherInfo.setMTemprature(Integer.valueOf(this.mHwWeatherData.getCurrent_temperature()));
        rawWeatherInfo.setMWeatherIcon(Integer.valueOf(this.mHwWeatherData.getWeather_icon()));
        if (this.mHwLocation != null) {
            rawWeatherInfo.setMLongitude(Double.valueOf(this.mHwLocation.getLongitude()));
            rawWeatherInfo.setMLatitude(Double.valueOf(this.mHwLocation.getLatitude()));
        }
        rawWeatherInfo.setMTimeStamp(new Date());
        rawWeatherInfo.setMReservedText(OPCollectUtils.formatCurrentTime());
        OdmfCollectScheduler.getInstance().getDataHandler().obtainMessage(4, rawWeatherInfo).sendToTarget();
        this.isRequesting = false;
        this.mLastRequestTime = System.currentTimeMillis();
        return true;
    }

    /* access modifiers changed from: private */
    public void onWeatherFail() {
        synchronized (this.mLock) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(2);
            }
        }
        this.isRequesting = false;
        OPCollectLog.e(TAG, "onWeatherFail failed");
    }

    public void onLocationSuccess(HwLocation hwLocation) {
        OPCollectLog.r(TAG, "callback");
        if (System.currentTimeMillis() - this.mLastRequestTime > WEATHER_VALID_TIME) {
            request(hwLocation);
        }
    }

    private void request(HwLocation hwLocation) {
        OPCollectLog.w(TAG, " request in ");
        if (hwLocation == null || hwLocation.getLatitude() == 0.0d) {
            OPCollectLog.w(TAG, " request parameter illegal. ");
            onWeatherFail();
        } else if (this.isRequesting) {
            OPCollectLog.w(TAG, " is requesting , no need request!");
        } else {
            try {
                if (this.mRequestWeather == null) {
                    OPCollectLog.w(TAG, " request in mRequest null");
                    onWeatherFail();
                    bindWeatherService();
                    return;
                }
                this.isRequesting = true;
                this.mHwLocation = hwLocation;
                RequestData requestData = new RequestData(this.mContext, this.mHwLocation.getLatitude(), this.mHwLocation.getLongitude());
                requestData.setmAllDay(false);
                this.mRequestWeather.requestWeatherByLocationAndSourceType(requestData, 1);
                synchronized (this.mLock) {
                    if (this.mHandler != null) {
                        this.mHandler.sendEmptyMessageDelayed(2, REQUEST_TIME_OUT);
                    }
                }
            } catch (RemoteException e) {
                OPCollectLog.e(TAG, "requestWeather failed: " + e.getMessage());
                onWeatherFail();
            }
        }
    }

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static synchronized void destroyInstance() {
        synchronized (WeatherInfoAction.class) {
            sInstance = null;
        }
    }

    public void dump(int indentNum, PrintWriter pw) {
        super.dump(indentNum, pw);
        if (pw != null) {
            String indent = String.format("%" + indentNum + "s\\-", new Object[]{" "});
            if (this.mRequestWeather == null) {
                pw.println(indent + "mRequestWeather is null");
            } else {
                pw.println(indent + "mRequestWeather not null");
            }
        }
    }
}
