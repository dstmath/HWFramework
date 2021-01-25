package android.zrhung.watchpoint;

import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.huawei.android.internal.os.BackgroundThreadEx;
import com.huawei.android.os.HandlerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import java.util.Calendar;

public final class SysHungScreenOn extends ZrHungImpl {
    public static final String HUNG_CONFIG_ENABLE = "1";
    private static final int MESSAGE_CHECK_SCREENON = 1000;
    private static final String TAG = "ZrHung.SysHungScreenOn";
    private static SysHungScreenOn sysHungScreenOn = null;
    private Calendar cal = Calendar.getInstance();
    private ZRHung.HungConfig mHungConfig = null;
    private String mHungConfigEnable = "null";
    private int mHungConfigStatus = -1;
    private Handler mScreenOnCheckHandler = new ScreenOnCheckHandler(BackgroundThreadEx.getHandler().getLooper());
    private int mScreenOnDelayTime = 5000;
    private StringBuilder mScreenOnInfo = new StringBuilder();

    /* JADX WARN: Type inference failed for: r0v6, types: [android.zrhung.watchpoint.SysHungScreenOn$ScreenOnCheckHandler, android.os.Handler] */
    public SysHungScreenOn(String wpName) {
        super(wpName);
    }

    public static synchronized SysHungScreenOn getInstance(String wpName) {
        SysHungScreenOn sysHungScreenOn2;
        synchronized (SysHungScreenOn.class) {
            if (sysHungScreenOn == null) {
                sysHungScreenOn = new SysHungScreenOn(wpName);
            }
            sysHungScreenOn2 = sysHungScreenOn;
        }
        return sysHungScreenOn2;
    }

    private final class ScreenOnCheckHandler extends HandlerEx {
        ScreenOnCheckHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            if (msg != null && msg.what == SysHungScreenOn.MESSAGE_CHECK_SCREENON) {
                SysHungScreenOn.this.sendEvent(null);
            }
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public int init(ZrHungData zrHungData) {
        if (this.mHungConfig == null || this.mHungConfigStatus == 1) {
            this.mHungConfig = ZRHung.getHungConfig(11);
            ZRHung.HungConfig hungConfig = this.mHungConfig;
            if (hungConfig != null) {
                this.mHungConfigStatus = hungConfig.status;
                String[] values = this.mHungConfig.value.split(",");
                this.mHungConfigEnable = values[0];
                if (values.length > 1) {
                    this.mScreenOnDelayTime = parseInt(values[1]);
                }
            }
        }
        return 0;
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            SlogEx.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
            return -1;
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean start(ZrHungData zrHungData) {
        try {
            init(null);
            if (!isBootCompleted()) {
                return false;
            }
            if (this.mScreenOnCheckHandler.hasMessages(MESSAGE_CHECK_SCREENON)) {
                return true;
            }
            this.mScreenOnCheckHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_SCREENON, (long) this.mScreenOnDelayTime);
            SlogEx.d(TAG, "SysHungScreenOn start");
            return true;
        } catch (Exception e) {
            SlogEx.e(TAG, "start exception");
            return false;
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean check(ZrHungData zrHungData) {
        return false;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean cancelCheck(ZrHungData zrHungData) {
        return false;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean stop(ZrHungData zrHungData) {
        if (!isBootCompleted()) {
            return false;
        }
        if (this.mScreenOnCheckHandler.hasMessages(MESSAGE_CHECK_SCREENON)) {
            this.mScreenOnCheckHandler.removeMessages(MESSAGE_CHECK_SCREENON);
            SlogEx.d(TAG, "SysHungScreenOn stop");
        }
        StringBuilder sb = this.mScreenOnInfo;
        sb.delete(0, sb.length());
        return true;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData zrHungData) {
        if (this.mHungConfig == null || this.mHungConfigStatus != 0 || !"1".equals(this.mHungConfigEnable)) {
            StringBuilder sb = this.mScreenOnInfo;
            sb.delete(0, sb.length());
            return true;
        }
        if (!ZRHung.sendHungEvent(11, (String) null, this.mScreenOnInfo.toString())) {
            SlogEx.e(TAG, " ZRHung.sendHungEvent failed!");
        }
        SlogEx.i(TAG, this.mScreenOnInfo.toString());
        return true;
    }

    @Override // android.zrhung.ZrHungImpl
    public synchronized boolean addInfo(ZrHungData zrHungData) {
        if (zrHungData == null) {
            return false;
        }
        try {
            if (!isBootCompleted()) {
                return false;
            }
            if (Binder.getCallingUid() >= 10000) {
                SlogEx.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            }
            if (this.mScreenOnInfo != null) {
                StringBuilder sb = this.mScreenOnInfo;
                sb.append(this.cal.get(1));
                sb.append("-");
                sb.append(this.cal.get(2) + 1);
                sb.append("-");
                sb.append(this.cal.get(5));
                sb.append(" ");
                sb.append(this.cal.get(11));
                sb.append(":");
                sb.append(this.cal.get(12));
                sb.append(":");
                sb.append(this.cal.get(13));
                sb.append(":");
                sb.append(zrHungData.getString("addScreenOnInfo"));
                sb.append(System.lineSeparator());
                return true;
            }
            return false;
        } catch (Exception e) {
            SlogEx.e(TAG, "addinfo exception");
        }
    }

    private boolean isBootCompleted() {
        return "1".equals(SystemPropertiesEx.get("sys.boot_completed", "0"));
    }
}
