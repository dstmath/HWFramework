package tmsdk.common;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import tmsdkobf.jh;
import tmsdkobf.ji;
import tmsdkobf.jj;
import tmsdkobf.jo;
import tmsdkobf.nc;

/* compiled from: Unknown */
public abstract class TMSService extends Service {
    private static final HashMap<Class<?>, jh> Ag = null;
    private static final HashMap<Class<?>, ArrayList<ji>> Ah = null;
    private nc yq;

    /* compiled from: Unknown */
    public class TipsReceiver extends jj {
        final /* synthetic */ TMSService Ai;

        public TipsReceiver(TMSService tMSService) {
            this.Ai = tMSService;
        }

        public void doOnRecv(Context context, Intent intent) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.TMSService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.TMSService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.TMSService.<clinit>():void");
    }

    public static IBinder bindService(Class<? extends jh> cls, ji jiVar) {
        IBinder iBinder;
        synchronized (jh.class) {
            jh jhVar = (jh) Ag.get(cls);
            if (jhVar == null) {
                iBinder = null;
            } else {
                IBinder binder = jhVar.getBinder();
                ArrayList arrayList = (ArrayList) Ah.get(cls);
                if (arrayList == null) {
                    arrayList = new ArrayList(1);
                    Ah.put(cls, arrayList);
                }
                arrayList.add(jiVar);
                iBinder = binder;
            }
        }
        return iBinder;
    }

    public static jh startService(jh jhVar) {
        return startService(jhVar, null);
    }

    public static jh startService(jh jhVar, Intent intent) {
        synchronized (jh.class) {
            if (Ag.containsKey(jhVar.getClass())) {
                ((jh) Ag.get(jhVar.getClass())).e(intent);
            } else {
                jhVar.onCreate(TMSDKContext.getApplicaionContext());
                jhVar.e(intent);
                Ag.put(jhVar.getClass(), jhVar);
            }
        }
        return jhVar;
    }

    public static boolean stopService(Class<? extends jh> cls) {
        synchronized (jh.class) {
            if (Ag.containsKey(cls)) {
                List list = (List) Ah.get(cls);
                if (list == null || list.size() == 0) {
                    ((jh) Ag.get(cls)).onDestory();
                    Ag.remove(cls);
                    Ah.remove(cls);
                    return true;
                }
                return false;
            }
            return true;
        }
    }

    public static synchronized boolean stopService(jh jhVar) {
        boolean stopService;
        synchronized (TMSService.class) {
            stopService = stopService(jhVar.getClass());
        }
        return stopService;
    }

    public static void unBindService(Class<? extends jh> cls, ji jiVar) {
        synchronized (jh.class) {
            List list = (List) Ah.get(cls);
            if (list != null) {
                list.remove(jiVar);
            }
        }
    }

    public final IBinder onBind(Intent intent) {
        return jo.cp();
    }

    public void onCreate() {
        super.onCreate();
        Ag.clear();
        Ah.clear();
        this.yq = new nc("wup");
    }

    public void onDestroy() {
        synchronized (jh.class) {
            Iterator it = new ArrayList(Ag.values()).iterator();
            while (it.hasNext()) {
                ((jh) it.next()).onDestory();
            }
            Ag.clear();
            Ah.clear();
        }
        super.onDestroy();
    }

    public void onStart(Intent intent, int i) {
        String str = null;
        super.onStart(intent, i);
        if (intent != null) {
            str = intent.getAction();
        }
        if (str != null && str.equals("com.tencent.tmsecure.action.SKIP_SMS_RECEIVED_EVENT")) {
            DataEntity dataEntity = new DataEntity(3);
            String stringExtra = intent.getStringExtra("command");
            String stringExtra2 = intent.getStringExtra("data");
            if (stringExtra != null && stringExtra2 != null) {
                try {
                    Bundle bundle = dataEntity.bundle();
                    bundle.putString("command", stringExtra);
                    bundle.putString("data", stringExtra2);
                    jo.cp().sendMessage(dataEntity);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        super.onStartCommand(intent, i, i2);
        return 1;
    }
}
