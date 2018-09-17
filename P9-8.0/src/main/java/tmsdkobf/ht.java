package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.utils.f;
import tmsdk.common.utils.n;

public class ht extends if implements tmsdkobf.im.a {
    private static ht qE = null;
    private static final String[] qF = new String[]{"android.provider.Telephony.SMS_RECEIVED", "android.provider.Telephony.SMS_RECEIVED2", "android.provider.Telephony.GSM_SMS_RECEIVED"};
    private static final String[] qG = new String[]{"android.provider.Telephony.WAP_PUSH_RECEIVED", "android.provider.Telephony.WAP_PUSH_GSM_RECEIVED"};
    private Handler handler;
    private Context mContext;
    private hu qA;
    private Queue<SmsEntity> qB;
    private a qC;
    private boolean qD;
    private hs qy;
    private boolean qz;

    private class a implements Runnable {
        private a() {
        }

        /* synthetic */ a(ht htVar, AnonymousClass1 anonymousClass1) {
            this();
        }

        public void run() {
            if (ht.this.qA != null) {
                while (true) {
                    SmsEntity smsEntity = (SmsEntity) ht.this.qB.poll();
                    if (smsEntity == null) {
                        break;
                    }
                    ht.this.qA.a(smsEntity, ht.this);
                }
            }
            ht.this.qD = false;
        }
    }

    public ht() {
        this.qy = new hs();
        this.qD = false;
        this.handler = new Handler() {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        if (ht.this.qC == null) {
                            ht.this.qC = new a(ht.this, null);
                        }
                        if (!ht.this.qD) {
                            ht.this.qD = true;
                            im.bJ().addTask(ht.this.qC, "filterSms");
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mContext = TMSDKContext.getApplicaionContext();
        this.qB = new ConcurrentLinkedQueue();
    }

    private ht(Context context) {
        this.qy = new hs();
        this.qD = false;
        this.handler = /* anonymous class already generated */;
        this.mContext = context;
        this.qB = new ConcurrentLinkedQueue();
    }

    public static synchronized ht h(Context context) {
        ht htVar;
        synchronized (ht.class) {
            if (qE == null) {
                qE = new ht(context);
            }
            htVar = qE;
        }
        return htVar;
    }

    public static boolean i(Context context) {
        return false;
    }

    public static boolean j(Context context) {
        return false;
    }

    public void a(Context context, Intent intent, BroadcastReceiver broadcastReceiver) {
        String action = intent.getAction();
        f.f("MessageReceiver", this + " action " + action + "   getInstance" + h(context));
        if (n.iX() <= 18 || !j(context) || "android.provider.Telephony.SMS_DELIVER".equals(action) || "android.provider.Telephony.WAP_PUSH_DELIVER".equals(action)) {
            SmsEntity smsEntity = null;
            this.qy.a(intent);
            if (this.qy.bv()) {
                smsEntity = this.qy.bt();
            }
            if (!(smsEntity == null || TextUtils.isEmpty(smsEntity.phonenum) || TextUtils.isEmpty(smsEntity.body))) {
                if (i(context)) {
                    this.qB.add(smsEntity);
                    this.handler.sendEmptyMessage(1);
                    if (broadcastReceiver == null) {
                        try {
                            abortBroadcast();
                        } catch (Exception e) {
                            f.e("MessageReceiver", e);
                        }
                    } else {
                        broadcastReceiver.abortBroadcast();
                    }
                } else if (n.iX() > 4 && this.qA != null) {
                    this.qA.a(smsEntity, this);
                }
            }
        }
    }

    public void a(hu huVar) {
        this.qA = huVar;
        if (this.qA != null && this.qB.size() > 0) {
            this.handler.sendEmptyMessage(1);
        }
    }

    public void a(qc qcVar) {
        String[] strArr;
        int length;
        int i;
        IntentFilter intentFilter;
        String[] strArr2 = null;
        String[] strArr3 = null;
        if (qcVar != null) {
            try {
                Object obj;
                String im = qcVar.im();
                if (im != null) {
                    obj = null;
                    for (String equalsIgnoreCase : qF) {
                        if (equalsIgnoreCase.equalsIgnoreCase(im)) {
                            obj = 1;
                            break;
                        }
                    }
                    if (obj == null) {
                        strArr2 = new String[]{im};
                    }
                }
                im = qcVar.in();
                if (im != null) {
                    obj = null;
                    for (String equalsIgnoreCase2 : qG) {
                        if (equalsIgnoreCase2.equalsIgnoreCase(im)) {
                            obj = 1;
                            break;
                        }
                    }
                    if (obj == null) {
                        strArr3 = new String[]{im};
                    }
                }
            } catch (Throwable e) {
                f.b("MessageReceiver", "register", e);
            }
        } else if (!this.qz) {
            this.qz = true;
            strArr2 = qF;
            strArr3 = qG;
        } else {
            return;
        }
        if (strArr2 != null) {
            strArr = strArr2;
            length = strArr2.length;
            for (i = 0; i < length; i++) {
                intentFilter = new IntentFilter(strArr[i]);
                intentFilter.addCategory("android.intent.category.DEFAULT");
                intentFilter.setPriority(Integer.MAX_VALUE);
                this.mContext.registerReceiver(this, intentFilter, "android.permission.BROADCAST_SMS", null);
            }
        }
        if (strArr3 != null) {
            strArr = strArr3;
            length = strArr3.length;
            for (i = 0; i < length; i++) {
                intentFilter = new IntentFilter(strArr[i]);
                intentFilter.addDataType("application/vnd.wap.sic");
                intentFilter.addDataType("application/vnd.wap.slc");
                intentFilter.addDataType("application/vnd.wap.coc");
                intentFilter.addCategory("android.intent.category.DEFAULT");
                intentFilter.setPriority(Integer.MAX_VALUE);
                this.mContext.registerReceiver(this, intentFilter, "android.permission.BROADCAST_SMS", null);
            }
        }
    }

    public boolean bw() {
        return this.qz;
    }

    public void doOnRecv(Context context, Intent intent) {
        a(context, intent, null);
    }

    public void unregister() {
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        if (this.qz) {
            applicaionContext.unregisterReceiver(this);
            this.qz = false;
        }
    }
}
