package tmsdkobf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.f;

public class lm {
    private static Object lock = new Object();
    private static lm yC = null;
    private static Object yD = new Object();
    private Context context = TMSDKContext.getApplicaionContext();
    ConcurrentHashMap<String, a> yB = new ConcurrentHashMap();

    class a extends if {
        public String action = null;
        public Runnable yE = null;

        a() {
        }

        public void doOnRecv(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                f.f("cccccc", "action...");
                if (this.action.equals(action) && this.yE != null) {
                    im.bJ().addTask(new Runnable() {
                        public void run() {
                            a.this.yE.run();
                            lm.this.bH(action);
                        }
                    }, "AlarmerTaskReceiver");
                }
            }
        }
    }

    private lm() {
    }

    public static lm eC() {
        if (yC == null) {
            synchronized (lock) {
                if (yC == null) {
                    yC = new lm();
                }
            }
        }
        return yC;
    }

    public void a(String str, long j, Runnable runnable) {
        try {
            synchronized (yD) {
                BroadcastReceiver aVar = new a();
                this.context.registerReceiver(aVar, new IntentFilter(str));
                aVar.yE = runnable;
                aVar.action = str;
                PendingIntent broadcast = PendingIntent.getBroadcast(this.context, 0, new Intent(str), 0);
                AlarmManager alarmManager = (AlarmManager) this.context.getSystemService("alarm");
                this.yB.put(str, aVar);
                alarmManager.set(0, System.currentTimeMillis() + j, broadcast);
            }
        } catch (Throwable th) {
        }
    }

    public void bH(String str) {
        synchronized (yD) {
            a aVar = (a) this.yB.remove(str);
            if (aVar != null) {
                oj.h(this.context, str);
                this.context.unregisterReceiver(aVar);
            }
        }
    }
}
