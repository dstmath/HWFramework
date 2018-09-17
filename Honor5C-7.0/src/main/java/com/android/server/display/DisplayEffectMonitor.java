package com.android.server.display;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwServiceFactory.IDisplayEffectMonitor;
import com.android.server.jankshield.TableJankEvent;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.text.SimpleDateFormat;
import java.util.List;

public class DisplayEffectMonitor implements IDisplayEffectMonitor {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final boolean HWLOGWE = true;
    private static final String PARAM_TYPE = "paramType";
    private static final String TAG = "DisplayEffectMonitor";
    private static DisplayEffectMonitor mMonitor;
    private final BackLightBrightnessMonitor mBackLightBrightnessMonitor;
    private final Context mContext;
    private final HandlerThread mHandlerThread;

    public class ParamLogPrinter {
        private static final int LOG_PRINTER_MSG = 1;
        private static final int mMessageDelayInMs = 2000;
        private boolean mFirstTime;
        private long mLastTime;
        private int mLastValue;
        private String mPackageName;
        private boolean mParamDecrease;
        private boolean mParamIncrease;
        private final String mParamName;
        private long mStartTime;
        private int mStartValue;
        private final String mTAG;
        private final SimpleDateFormat mTimeFormater;
        private final Handler mTimeHandler;

        /* renamed from: com.android.server.display.DisplayEffectMonitor.ParamLogPrinter.1 */
        class AnonymousClass1 extends Handler {
            AnonymousClass1(Looper $anonymous0) {
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                synchronized (ParamLogPrinter.this) {
                    if (ParamLogPrinter.this.mFirstTime) {
                        return;
                    }
                    if (ParamLogPrinter.this.mLastValue == msg.arg1) {
                        if (ParamLogPrinter.this.mParamIncrease || ParamLogPrinter.this.mParamDecrease) {
                            ParamLogPrinter.this.printValueChange(ParamLogPrinter.this.mStartValue, ParamLogPrinter.this.mStartTime, ParamLogPrinter.this.mLastValue, ParamLogPrinter.this.mLastTime, ParamLogPrinter.this.mPackageName);
                        } else {
                            ParamLogPrinter.this.printSingleValue(ParamLogPrinter.this.mLastValue, ParamLogPrinter.this.mLastTime, ParamLogPrinter.this.mPackageName);
                        }
                        removeMessages(ParamLogPrinter.LOG_PRINTER_MSG);
                        ParamLogPrinter.this.mFirstTime = DisplayEffectMonitor.HWLOGWE;
                    }
                }
            }
        }

        public ParamLogPrinter(String paramName, String tag) {
            this.mFirstTime = DisplayEffectMonitor.HWLOGWE;
            this.mTimeFormater = new SimpleDateFormat("HH:mm:ss.SSS");
            this.mTimeHandler = new AnonymousClass1(DisplayEffectMonitor.this.mHandlerThread.getLooper());
            this.mParamName = paramName;
            this.mTAG = tag;
        }

        public synchronized void updateParam(int param, String packageName) {
            boolean z = DisplayEffectMonitor.HWLOGWE;
            synchronized (this) {
                if (this.mFirstTime) {
                    this.mStartValue = param;
                    this.mLastValue = this.mStartValue;
                    this.mStartTime = System.currentTimeMillis();
                    this.mLastTime = this.mStartTime;
                    this.mPackageName = packageName;
                    this.mParamIncrease = DisplayEffectMonitor.HWFLOW;
                    this.mParamDecrease = DisplayEffectMonitor.HWFLOW;
                    this.mTimeHandler.removeMessages(LOG_PRINTER_MSG);
                    this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(LOG_PRINTER_MSG, param, 0), TableJankEvent.recMAXCOUNT);
                    this.mFirstTime = DisplayEffectMonitor.HWFLOW;
                } else if (this.mLastValue == param) {
                } else {
                    boolean z2;
                    if (!this.mParamIncrease || param >= this.mLastValue) {
                        if (!this.mParamDecrease || param <= this.mLastValue) {
                            if (!(this.mParamIncrease || this.mParamDecrease)) {
                                if (param > this.mLastValue) {
                                    z2 = DisplayEffectMonitor.HWLOGWE;
                                } else {
                                    z2 = DisplayEffectMonitor.HWFLOW;
                                }
                                this.mParamIncrease = z2;
                                if (param < this.mLastValue) {
                                    z2 = DisplayEffectMonitor.HWLOGWE;
                                } else {
                                    z2 = DisplayEffectMonitor.HWFLOW;
                                }
                                this.mParamDecrease = z2;
                            }
                            this.mLastValue = param;
                            this.mLastTime = System.currentTimeMillis();
                            this.mTimeHandler.removeMessages(LOG_PRINTER_MSG);
                            this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(LOG_PRINTER_MSG, param, 0), TableJankEvent.recMAXCOUNT);
                            return;
                        }
                    }
                    printValueChange(this.mStartValue, this.mStartTime, this.mLastValue, this.mLastTime, this.mPackageName);
                    this.mStartValue = this.mLastValue;
                    this.mLastValue = param;
                    this.mStartTime = this.mLastTime;
                    this.mLastTime = System.currentTimeMillis();
                    if (this.mLastValue > this.mStartValue) {
                        z2 = DisplayEffectMonitor.HWLOGWE;
                    } else {
                        z2 = DisplayEffectMonitor.HWFLOW;
                    }
                    this.mParamIncrease = z2;
                    if (this.mLastValue >= this.mStartValue) {
                        z = DisplayEffectMonitor.HWFLOW;
                    }
                    this.mParamDecrease = z;
                    this.mTimeHandler.removeMessages(LOG_PRINTER_MSG);
                    this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(LOG_PRINTER_MSG, param, 0), TableJankEvent.recMAXCOUNT);
                }
            }
        }

        public synchronized void resetParam(int param, String packageName) {
            if (this.mFirstTime) {
                if (this.mLastValue == param) {
                    return;
                }
            } else if (this.mParamIncrease || this.mParamDecrease) {
                printValueChange(this.mStartValue, this.mStartTime, this.mLastValue, this.mLastTime, this.mPackageName);
            } else {
                printSingleValue(this.mLastValue, this.mLastTime, this.mPackageName);
            }
            printResetValue(param, packageName);
            this.mLastValue = param;
            this.mLastTime = System.currentTimeMillis();
            this.mTimeHandler.removeMessages(LOG_PRINTER_MSG);
            this.mFirstTime = DisplayEffectMonitor.HWLOGWE;
        }

        public synchronized void changeName(int param, String packageName) {
            if (!this.mFirstTime) {
                if (this.mParamIncrease || this.mParamDecrease) {
                    printValueChange(this.mStartValue, this.mStartTime, this.mLastValue, this.mLastTime, this.mPackageName);
                } else {
                    printSingleValue(this.mLastValue, this.mLastTime, this.mPackageName);
                }
            }
            printNameChange(this.mLastValue, this.mPackageName, param, packageName);
            this.mStartValue = param;
            this.mLastValue = this.mStartValue;
            this.mStartTime = System.currentTimeMillis();
            this.mLastTime = this.mStartTime;
            this.mPackageName = packageName;
            this.mParamIncrease = DisplayEffectMonitor.HWFLOW;
            this.mParamDecrease = DisplayEffectMonitor.HWFLOW;
            this.mTimeHandler.removeMessages(LOG_PRINTER_MSG);
            this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(LOG_PRINTER_MSG, param, 0), TableJankEvent.recMAXCOUNT);
            this.mFirstTime = DisplayEffectMonitor.HWFLOW;
        }

        private void printSingleValue(int value, long time, String packageName) {
            if (DisplayEffectMonitor.HWFLOW) {
                Slog.i(this.mTAG, this.mParamName + " " + value + " @" + this.mTimeFormater.format(Long.valueOf(time)) + (packageName != null ? " by " + packageName : AppHibernateCst.INVALID_PKG));
            }
        }

        private void printValueChange(int startValue, long startTime, int endValue, long endTime, String packageName) {
            if (DisplayEffectMonitor.HWFLOW) {
                Slog.i(this.mTAG, this.mParamName + " " + startValue + " -> " + endValue + " @" + this.mTimeFormater.format(Long.valueOf(endTime)) + " " + (endTime - startTime) + "ms" + (packageName != null ? " by " + packageName : AppHibernateCst.INVALID_PKG));
            }
        }

        private void printResetValue(int value, String packageName) {
            if (DisplayEffectMonitor.HWFLOW) {
                Slog.i(this.mTAG, this.mParamName + " reset " + value + (packageName != null ? " by " + packageName : AppHibernateCst.INVALID_PKG));
            }
        }

        private void printNameChange(int value1, String packageName1, int value2, String packageName2) {
            if (DisplayEffectMonitor.HWFLOW) {
                Slog.i(this.mTAG, this.mParamName + " " + value1 + (packageName1 != null ? " by " + packageName1 : AppHibernateCst.INVALID_PKG) + " -> " + value2 + (packageName2 != null ? " by " + packageName2 : AppHibernateCst.INVALID_PKG));
            }
        }
    }

    static {
        boolean z = HWLOGWE;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : HWFLOW : HWLOGWE;
        HWDEBUG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : HWFLOW;
        }
        HWFLOW = z;
    }

    public static synchronized DisplayEffectMonitor getInstance(Context context) {
        DisplayEffectMonitor displayEffectMonitor;
        synchronized (DisplayEffectMonitor.class) {
            if (mMonitor == null) {
                if (context != null) {
                    try {
                        mMonitor = new DisplayEffectMonitor(context);
                    } catch (Exception e) {
                        Slog.e(TAG, "getInstance() failed! " + e);
                    }
                } else {
                    Slog.w(TAG, "getInstance() failed! input context and instance is both null");
                }
            }
            displayEffectMonitor = mMonitor;
        }
        return displayEffectMonitor;
    }

    private DisplayEffectMonitor(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mBackLightBrightnessMonitor = new BackLightBrightnessMonitor(this);
        if (HWFLOW) {
            Slog.i(TAG, "new instance success");
        }
    }

    public void sendMonitorParam(ArrayMap<String, Object> params) {
        if (params == null || !(params.get(PARAM_TYPE) instanceof String)) {
            Slog.e(TAG, "sendMonitorParam() input params format error!");
            return;
        }
        String paramType = (String) params.get(PARAM_TYPE);
        if (HWDEBUG) {
            Slog.d(TAG, "sendMonitorParam() paramType: " + paramType);
        }
        if (this.mBackLightBrightnessMonitor.isParamOwner(paramType)) {
            this.mBackLightBrightnessMonitor.sendMonitorParam(params);
        } else {
            Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
        }
    }

    public boolean isAppAlive(String packageName) {
        if (packageName == null) {
            return HWFLOW;
        }
        try {
            List<RunningTaskInfo> runningTasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(10);
            if (runningTasks == null || runningTasks.isEmpty()) {
                return HWFLOW;
            }
            for (RunningTaskInfo task : runningTasks) {
                if (packageName.equals(task.topActivity.getPackageName())) {
                    return HWLOGWE;
                }
            }
            return HWFLOW;
        } catch (Exception e) {
            Slog.e(TAG, "isAppAlive() failed to get topActivity PackageName " + e);
        }
    }
}
