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
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.d;
import tmsdk.common.utils.j;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class ix extends jj implements tmsdkobf.jq.a {
    private static ix tf;
    private static final String[] tg = null;
    private static final String[] th = null;
    private Handler handler;
    private Context mContext;
    private iw sZ;
    private boolean ta;
    private iy tb;
    private Queue<SmsEntity> tc;
    private a td;
    private boolean te;

    /* compiled from: Unknown */
    private class a implements Runnable {
        final /* synthetic */ ix ti;

        private a(ix ixVar) {
            this.ti = ixVar;
        }

        public void run() {
            if (this.ti.tb != null) {
                while (true) {
                    SmsEntity smsEntity = (SmsEntity) this.ti.tc.poll();
                    if (smsEntity == null) {
                        break;
                    }
                    this.ti.tb.a(smsEntity, this.ti);
                }
            }
            this.ti.te = false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ix.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ix.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ix.<clinit>():void");
    }

    public ix() {
        this.sZ = new iw();
        this.te = false;
        this.handler = new Handler() {
            final /* synthetic */ ix ti;

            {
                this.ti = r1;
            }

            public void handleMessage(Message message) {
                switch (message.what) {
                    case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                        if (this.ti.td == null) {
                            this.ti.td = new a(null);
                        }
                        if (!this.ti.te) {
                            this.ti.te = true;
                            jq.ct().a(this.ti.td, "filterSms");
                        }
                    default:
                }
            }
        };
        this.mContext = TMSDKContext.getApplicaionContext();
        this.tc = new ConcurrentLinkedQueue();
    }

    private ix(Context context) {
        this.sZ = new iw();
        this.te = false;
        this.handler = new Handler() {
            final /* synthetic */ ix ti;

            {
                this.ti = r1;
            }

            public void handleMessage(Message message) {
                switch (message.what) {
                    case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                        if (this.ti.td == null) {
                            this.ti.td = new a(null);
                        }
                        if (!this.ti.te) {
                            this.ti.te = true;
                            jq.ct().a(this.ti.td, "filterSms");
                        }
                    default:
                }
            }
        };
        this.mContext = context;
        this.tc = new ConcurrentLinkedQueue();
    }

    public static synchronized ix e(Context context) {
        ix ixVar;
        synchronized (ix.class) {
            if (tf == null) {
                tf = new ix(context);
            }
            ixVar = tf;
        }
        return ixVar;
    }

    public static boolean f(Context context) {
        return false;
    }

    public static boolean g(Context context) {
        return false;
    }

    public void a(Context context, Intent intent, BroadcastReceiver broadcastReceiver) {
        SmsEntity smsEntity = null;
        String action = intent.getAction();
        d.d("MessageReceiver", this + " action " + action + "   getInstance" + e(context));
        if (j.iM() <= 18 || !g(context) || "android.provider.Telephony.SMS_DELIVER".equals(action) || "android.provider.Telephony.WAP_PUSH_DELIVER".equals(action)) {
            this.sZ.b(intent);
            if (this.sZ.cc()) {
                smsEntity = this.sZ.ca();
            }
            if (!(smsEntity == null || TextUtils.isEmpty(smsEntity.phonenum) || TextUtils.isEmpty(smsEntity.body))) {
                if (f(context)) {
                    this.tc.add(smsEntity);
                    this.handler.sendEmptyMessage(1);
                    if (broadcastReceiver == null) {
                        try {
                            abortBroadcast();
                        } catch (Exception e) {
                            d.c("MessageReceiver", e);
                        }
                    } else {
                        broadcastReceiver.abortBroadcast();
                    }
                } else if (j.iM() > 4 && this.tb != null) {
                    this.tb.a(smsEntity, this);
                }
            }
        }
    }

    public void a(iy iyVar) {
        this.tb = iyVar;
        if (this.tb != null && this.tc.size() > 0) {
            this.handler.sendEmptyMessage(1);
        }
    }

    public void a(qz qzVar) {
        String[] strArr;
        String ie;
        String[] strArr2;
        int length;
        int i = 1;
        int i2 = 0;
        String[] strArr3;
        if (qzVar != null) {
            try {
                String id = qzVar.id();
                if (id != null) {
                    int i3;
                    for (String equalsIgnoreCase : tg) {
                        if (equalsIgnoreCase.equalsIgnoreCase(id)) {
                            i3 = 1;
                            break;
                        }
                    }
                    i3 = 0;
                    if (i3 == 0) {
                        strArr = new String[]{id};
                        ie = qzVar.ie();
                        if (ie != null) {
                            for (String equalsIgnoreCase2 : th) {
                                if (!equalsIgnoreCase2.equalsIgnoreCase(ie)) {
                                    break;
                                }
                            }
                            i = 0;
                            if (i == 0) {
                                strArr3 = new String[]{ie};
                                strArr2 = strArr;
                                strArr = strArr3;
                            }
                        }
                        strArr2 = strArr;
                        strArr = null;
                    }
                }
                strArr = null;
                ie = qzVar.ie();
                if (ie != null) {
                    while (r4 < r7) {
                        if (!equalsIgnoreCase2.equalsIgnoreCase(ie)) {
                            break;
                            if (i == 0) {
                                strArr3 = new String[]{ie};
                                strArr2 = strArr;
                                strArr = strArr3;
                            }
                        } else {
                        }
                    }
                    i = 0;
                    if (i == 0) {
                        strArr3 = new String[]{ie};
                        strArr2 = strArr;
                        strArr = strArr3;
                    }
                }
                strArr2 = strArr;
                strArr = null;
            } catch (Throwable e) {
                d.a("MessageReceiver", "register", e);
            }
        } else if (!this.ta) {
            this.ta = true;
            strArr = tg;
            strArr3 = th;
            strArr2 = strArr;
            strArr = strArr3;
        } else {
            return;
        }
        if (strArr2 != null) {
            for (String ie2 : strArr2) {
                IntentFilter intentFilter = new IntentFilter(ie2);
                intentFilter.addCategory("android.intent.category.DEFAULT");
                intentFilter.setPriority(UrlCheckType.UNKNOWN);
                this.mContext.registerReceiver(this, intentFilter, "android.permission.BROADCAST_SMS", null);
            }
        }
        if (strArr != null) {
            length = strArr.length;
            while (i2 < length) {
                IntentFilter intentFilter2 = new IntentFilter(strArr[i2]);
                intentFilter2.addDataType("application/vnd.wap.sic");
                intentFilter2.addDataType("application/vnd.wap.slc");
                intentFilter2.addDataType("application/vnd.wap.coc");
                intentFilter2.addCategory("android.intent.category.DEFAULT");
                intentFilter2.setPriority(UrlCheckType.UNKNOWN);
                this.mContext.registerReceiver(this, intentFilter2, "android.permission.BROADCAST_SMS", null);
                i2++;
            }
        }
    }

    public boolean cd() {
        return this.ta;
    }

    public void doOnRecv(Context context, Intent intent) {
        a(context, intent, null);
    }

    public void unregister() {
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        if (this.ta) {
            applicaionContext.unregisterReceiver(this);
            this.ta = false;
        }
    }
}
