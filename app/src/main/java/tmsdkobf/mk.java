package tmsdkobf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class mk {
    private static mk AU;
    private static Object lock;
    ConcurrentHashMap<String, a> AT;
    private Context context;
    private Handler mHandler;

    /* compiled from: Unknown */
    class a extends jj {
        public Runnable AV;
        final /* synthetic */ mk AW;
        public String action;

        a(mk mkVar) {
            this.AW = mkVar;
            this.action = null;
            this.AV = null;
        }

        public void doOnRecv(Context context, Intent intent) {
            d.e("AlarmerTask", "AlarmerTaskReceiver.onReceive()");
            String action = intent.getAction();
            if (action == null) {
                d.e("AlarmerTask", "AlarmerTaskReceiver.onReceive() null == action");
            } else if (this.action.equals(action) && this.AV != null) {
                this.AW.mHandler.post(this.AV);
                this.AW.cu(action);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.mk.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.mk.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.mk.<clinit>():void");
    }

    private mk() {
        this.mHandler = null;
        this.context = TMSDKContext.getApplicaionContext();
        this.AT = new ConcurrentHashMap();
        this.mHandler = new Handler(this.context.getMainLooper());
    }

    public static mk eU() {
        if (AU == null) {
            synchronized (lock) {
                if (AU == null) {
                    AU = new mk();
                }
            }
        }
        return AU;
    }

    public void a(String str, long j, Runnable runnable) {
        d.d("AlarmerTask", "\u6dfb\u52a0\u95f9\u949f\u4efb\u52a1 : action : " + str + "  " + (j / 1000) + "s");
        try {
            BroadcastReceiver aVar = new a(this);
            this.context.registerReceiver(aVar, new IntentFilter(str));
            aVar.AV = runnable;
            aVar.action = str;
            PendingIntent broadcast = PendingIntent.getBroadcast(this.context, 0, new Intent(str), 0);
            AlarmManager alarmManager = (AlarmManager) this.context.getSystemService("alarm");
            this.AT.put(str, aVar);
            alarmManager.set(0, System.currentTimeMillis() + j, broadcast);
        } catch (Throwable th) {
            d.c("AlarmerTask", th);
        }
    }

    public void cu(String str) {
        d.d("AlarmerTask", "\u6ce8\u9500\u95f9\u949f\u4efb\u52a1 : action : " + str);
        a aVar = (a) this.AT.remove(str);
        if (aVar != null) {
            pm.f(this.context, str);
            this.context.unregisterReceiver(aVar);
        }
    }
}
