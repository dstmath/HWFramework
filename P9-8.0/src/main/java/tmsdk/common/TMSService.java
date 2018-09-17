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
import tmsdkobf.id;
import tmsdkobf.ie;
import tmsdkobf.if;
import tmsdkobf.ik;
import tmsdkobf.md;

public abstract class TMSService extends Service {
    private static final HashMap<Class<?>, id> xs = new HashMap();
    private static final HashMap<Class<?>, ArrayList<ie>> xt = new HashMap();
    private md vu;

    public class TipsReceiver extends if {
        public void doOnRecv(Context context, Intent intent) {
        }
    }

    public static IBinder bindService(Class<? extends id> cls, ie ieVar) {
        Class cls2 = id.class;
        synchronized (id.class) {
            IBinder iBinder = null;
            id idVar = (id) xs.get(cls);
            if (idVar != null) {
                iBinder = idVar.getBinder();
                ArrayList arrayList = (ArrayList) xt.get(cls);
                if (arrayList == null) {
                    arrayList = new ArrayList(1);
                    xt.put(cls, arrayList);
                }
                arrayList.add(ieVar);
            }
            return iBinder;
        }
    }

    public static id startService(id idVar) {
        return startService(idVar, null);
    }

    public static id startService(id idVar, Intent intent) {
        Class cls = id.class;
        synchronized (id.class) {
            if (xs.containsKey(idVar.getClass())) {
                ((id) xs.get(idVar.getClass())).d(intent);
            } else {
                idVar.onCreate(TMSDKContext.getApplicaionContext());
                idVar.d(intent);
                xs.put(idVar.getClass(), idVar);
            }
            return idVar;
        }
    }

    public static boolean stopService(Class<? extends id> cls) {
        Class cls2 = id.class;
        synchronized (id.class) {
            if (xs.containsKey(cls)) {
                List list = (List) xt.get(cls);
                if (list == null || list.size() == 0) {
                    ((id) xs.get(cls)).onDestory();
                    xs.remove(cls);
                    xt.remove(cls);
                    return true;
                }
                return false;
            }
            return true;
        }
    }

    public static synchronized boolean stopService(id idVar) {
        boolean stopService;
        synchronized (TMSService.class) {
            stopService = stopService(idVar.getClass());
        }
        return stopService;
    }

    public static void unBindService(Class<? extends id> cls, ie ieVar) {
        Class cls2 = id.class;
        synchronized (id.class) {
            List list = (List) xt.get(cls);
            if (list != null) {
                list.remove(ieVar);
            }
        }
    }

    public final IBinder onBind(Intent intent) {
        return ik.bF();
    }

    public void onCreate() {
        super.onCreate();
        xs.clear();
        xt.clear();
        this.vu = new md("wup");
    }

    public void onDestroy() {
        Class cls = id.class;
        synchronized (id.class) {
            Iterator it = new ArrayList(xs.values()).iterator();
            while (it.hasNext()) {
                ((id) it.next()).onDestory();
            }
            xs.clear();
            xt.clear();
            super.onDestroy();
        }
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
                    ik.bF().sendMessage(dataEntity);
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
