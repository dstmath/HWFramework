package tmsdkobf;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;

public final class hi {
    private static volatile hi pH;
    private Context mContext;
    private Handler mHandler;
    private Looper mLooper;
    private ArrayList<String> pI = new ArrayList();
    private ConcurrentHashMap<String, ih> pJ = new ConcurrentHashMap();
    private boolean pK;

    private static abstract class a implements ServiceConnection {
        protected ServiceInfo pP;
        protected ih pQ;

        public a(Context context, ServiceInfo serviceInfo) {
            this.pP = serviceInfo;
        }
    }

    private hi(Context context) {
        this.mContext = context;
        this.pK = bl();
        if (this.pK) {
            HandlerThread newFreeHandlerThread = im.bJ().newFreeHandlerThread(hi.class.getName());
            newFreeHandlerThread.start();
            this.mLooper = newFreeHandlerThread.getLooper();
            this.mHandler = new Handler(this.mLooper);
        }
    }

    private boolean a(ServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            return false;
        }
        if (!ir.rV.equals(serviceInfo.packageName)) {
            if (!"com.tencent.qqphonebook".equals(serviceInfo.packageName)) {
                return false;
            }
        }
        return !this.pI.contains(serviceInfo.packageName) && serviceInfo.permission != null && serviceInfo.permission.equals("com.tencent.tmsecure.permission.RECEIVE_SMS") && serviceInfo.exported;
    }

    private ih b(ServiceInfo serviceInfo) {
        ih ihVar = (ih) this.pJ.get(serviceInfo.packageName);
        if (ihVar != null) {
            return ihVar;
        }
        final Intent intent = new Intent();
        intent.setClassName(serviceInfo.packageName, serviceInfo.name);
        final Object obj = new Object();
        final a anonymousClass1 = new a(this.mContext, serviceInfo) {
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                this.pQ = tmsdkobf.ih.a.a(iBinder);
                hi.this.pJ.put(this.pP.packageName, this.pQ);
                synchronized (obj) {
                    obj.notify();
                }
            }

            public void onServiceDisconnected(ComponentName componentName) {
                hi.this.pJ.remove(this.pP.packageName);
                this.pQ = null;
            }
        };
        this.mHandler.post(new Runnable() {
            public void run() {
                hi.this.mContext.bindService(intent, anonymousClass1, 1);
            }
        });
        Object obj2 = obj;
        synchronized (obj) {
            try {
                obj.wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return anonymousClass1.pQ;
    }

    public static hi bi() {
        if (pH == null) {
            Class cls = hi.class;
            synchronized (hi.class) {
                if (pH == null) {
                    pH = new hi(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return pH;
    }

    private boolean bl() {
        PackageInfo packageInfo = TMServiceFactory.getSystemInfoService().getPackageInfo(this.mContext.getPackageName(), 4100);
        if (packageInfo == null) {
            return false;
        }
        int i = 0;
        String[] strArr = packageInfo.requestedPermissions;
        if (strArr != null) {
            String[] strArr2 = strArr;
            for (String equals : strArr) {
                if (equals.equals("com.tencent.tmsecure.permission.RECEIVE_SMS")) {
                    i = 1;
                    break;
                }
            }
        }
        int i2 = 0;
        PermissionInfo[] permissionInfoArr = packageInfo.permissions;
        if (permissionInfoArr != null) {
            PermissionInfo[] permissionInfoArr2 = permissionInfoArr;
            for (PermissionInfo permissionInfo : permissionInfoArr) {
                if (permissionInfo.name.equals("com.tencent.tmsecure.permission.RECEIVE_SMS")) {
                    i2 = 1;
                    break;
                }
            }
        }
        int i3 = 0;
        if (packageInfo.services != null) {
            for (ServiceInfo serviceInfo : packageInfo.services) {
                String str = serviceInfo.permission;
                if (str != null && str.equals("com.tencent.tmsecure.permission.RECEIVE_SMS") && serviceInfo.exported) {
                    i3 = 1;
                    break;
                }
            }
        }
        return (i & i2) & i3;
    }

    public void ax(String str) {
        if (!this.pI.contains(str)) {
            this.pI.add(str);
        }
    }

    public void ay(String str) {
        if (this.pI.contains(str)) {
            this.pI.remove(str);
        }
    }

    public ArrayList<ih> bj() {
        ArrayList<ih> arrayList = new ArrayList();
        if (this.pK) {
            List<ResolveInfo> queryIntentServices = TMServiceFactory.getSystemInfoService().queryIntentServices(new Intent("com.tencent.tmsecure.action.SMS_RECEIVED"), 0);
            ArrayList arrayList2 = new ArrayList();
            if (queryIntentServices != null) {
                for (ResolveInfo resolveInfo : queryIntentServices) {
                    ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                    String str = serviceInfo.packageName;
                    if (!arrayList2.contains(str) && a(serviceInfo)) {
                        Object b = !str.equals(this.mContext.getPackageName()) ? b(serviceInfo) : ik.bF();
                        if (b != null) {
                            arrayList.add(b);
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    public int bk() {
        return 1;
    }
}
