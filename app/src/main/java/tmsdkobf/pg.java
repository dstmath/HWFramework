package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class pg {
    private static pg Hy;
    private static Object lock;
    private a Hv;
    private ArrayList<pi> Hw;
    private pf Hx;
    private Context context;
    private Handler mHandler;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pg.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ pg Hz;

        AnonymousClass1(pg pgVar, Looper looper) {
            this.Hz = pgVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    pa.h("SharkTcpControler", "open connection");
                    this.Hz.Hx.ce(10);
                    break;
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    pa.h("SharkTcpControler", "close connection");
                    this.Hz.Hx.cf(2);
                    break;
                case FileInfo.TYPE_BIGFILE /*3*/:
                    this.Hz.gE();
                    break;
            }
            super.handleMessage(message);
        }
    }

    /* compiled from: Unknown */
    class a extends jj {
        final /* synthetic */ pg Hz;

        a(pg pgVar) {
            this.Hz = pgVar;
        }

        public void doOnRecv(Context context, Intent intent) {
            String action = intent.getAction();
            String str = intent.getPackage();
            d.e("SharkTcpControler", "SharkControlReceiver.onReceive() action[" + action + "]pkg[" + str + "]");
            if (action == null || str == null || !str.equals(TMSDKContext.getApplicaionContext().getPackageName())) {
                d.e("SharkTcpControler", "SharkControlReceiver.onReceive() action");
                return;
            }
            if (action.equals("tmsdk.common.module.sdknetpool.sharknetwork.SharkControler.SHARK_CONTROL_ACTION_EXE_RULE_CYCLE")) {
                this.Hz.mHandler.sendEmptyMessage(3);
            } else if (action.equals("tmsdk.common.module.sdknetpool.sharknetwork.SharkControler.SHARK_CONTROL_ACTION_EXE_RULE_CLOSE")) {
                this.Hz.mHandler.sendEmptyMessage(1);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.pg.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.pg.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.pg.<clinit>():void");
    }

    private pg() {
        this.Hw = new ArrayList();
        this.mHandler = new AnonymousClass1(this, Looper.getMainLooper());
        this.context = TMSDKContext.getApplicaionContext();
        this.Hx = jq.cu();
        this.Hv = new a(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tmsdk.common.module.sdknetpool.sharknetwork.SharkControler.SHARK_CONTROL_ACTION_EXE_RULE_CLOSE");
        intentFilter.addAction("tmsdk.common.module.sdknetpool.sharknetwork.SharkControler.SHARK_CONTROL_ACTION_EXE_RULE_CYCLE");
        try {
            this.context.registerReceiver(this.Hv, intentFilter);
        } catch (Throwable th) {
            d.c("SharkTcpControler", th);
        }
        B(null);
    }

    private void B(ArrayList<c> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            this.Hw.clear();
            pa.h("SharkTcpControler", "set half tcp policy");
            pa.h("SharkTcpControler", "start keet noKeep");
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                c cVar = (c) it.next();
                pa.h("SharkTcpControler", "start:" + cVar.start + " keep:" + cVar.a + " noKeep:" + cVar.b);
                if (cVar.start >= 0 && cVar.a > 0 && cVar.b > 0) {
                    pi piVar = new pi();
                    piVar.HF = cVar.start;
                    piVar.HG = cVar.a;
                    piVar.HH = cVar.b;
                    a(piVar);
                }
            }
        }
        if (this.Hw.size() == 0) {
            gC();
        }
        sort();
        gD();
    }

    private void a(pi piVar) {
        if (piVar != null && piVar.HF >= 0 && piVar.HG >= 0 && piVar.HH >= 0) {
            this.Hw.add(piVar);
        }
    }

    private static final int ck(int i) {
        return i * 60;
    }

    private static final int cl(int i) {
        return ck(i * 60);
    }

    public static pg gB() {
        if (Hy == null) {
            synchronized (lock) {
                if (Hy == null) {
                    Hy = new pg();
                }
            }
        }
        return Hy;
    }

    private void gC() {
        this.Hw.clear();
        pi piVar = new pi();
        piVar = new pi();
        piVar.HF = cl(0);
        piVar.HG = ck(10);
        piVar.HH = ck(60);
        this.Hw.add(piVar);
        piVar = new pi();
        piVar.HF = cl(8);
        piVar.HG = ck(15);
        piVar.HH = ck(15);
        this.Hw.add(piVar);
        piVar = new pi();
        piVar.HF = cl(15);
        piVar.HG = ck(10);
        piVar.HH = ck(20);
        this.Hw.add(piVar);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void gD() {
        if (!(this.Hw == null || this.Hw.size() == 0 || ((pi) this.Hw.get(0)).HF <= 0)) {
            pi piVar = (pi) this.Hw.get(this.Hw.size() - 1);
            pi piVar2 = new pi();
            piVar2.HF = cl(0);
            piVar2.HG = piVar.HG;
            piVar2.HH = piVar.HH;
            this.Hw.add(piVar2);
            sort();
        }
    }

    private void gE() {
        gF();
        int gG = gG();
        if (gG >= 0 && this.Hw != null && gG <= this.Hw.size()) {
            pi piVar = (pi) this.Hw.get(gG);
            this.mHandler.sendEmptyMessage(0);
            pm.a(this.context, "tmsdk.common.module.sdknetpool.sharknetwork.SharkControler.SHARK_CONTROL_ACTION_EXE_RULE_CLOSE", (long) (piVar.HG * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY));
            pm.a(this.context, "tmsdk.common.module.sdknetpool.sharknetwork.SharkControler.SHARK_CONTROL_ACTION_EXE_RULE_CYCLE", (long) ((piVar.HG + piVar.HH) * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY));
            pa.h("SharkTcpControler", "now open connection, after " + piVar.HG + " s close connection, and after " + piVar.HH + " s open connection");
            return;
        }
        d.f("SharkTcpControler", "index error : i " + gG + " size " + (this.Hw != null ? this.Hw.size() : -1));
    }

    private void gF() {
        pa.h("SharkTcpControler", "clear msg");
        pm.f(this.context, "tmsdk.common.module.sdknetpool.sharknetwork.SharkControler.SHARK_CONTROL_ACTION_EXE_RULE_CLOSE");
        pm.f(this.context, "tmsdk.common.module.sdknetpool.sharknetwork.SharkControler.SHARK_CONTROL_ACTION_EXE_RULE_CYCLE");
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(0);
    }

    private int gG() {
        int gH = gH();
        if (this.Hw == null || this.Hw.size() == 0) {
            return -1;
        }
        int size = this.Hw.size() - 1;
        while (size >= 0 && ((pi) this.Hw.get(size)).HF > gH) {
            size--;
        }
        return size;
    }

    private int gH() {
        Calendar instance = Calendar.getInstance();
        if (instance == null) {
            return 0;
        }
        return instance.get(13) + ((instance.get(11) * 3600) + (instance.get(12) * 60));
    }

    private void sort() {
        if (this.Hw != null && this.Hw.size() != 0) {
            Collections.sort(this.Hw, new Comparator<pi>() {
                final /* synthetic */ pg Hz;

                {
                    this.Hz = r1;
                }

                public int a(pi piVar, pi piVar2) {
                    return piVar.HF - piVar2.HF;
                }

                public /* synthetic */ int compare(Object obj, Object obj2) {
                    return a((pi) obj, (pi) obj2);
                }
            });
        }
    }

    public static void stop() {
        if (Hy != null) {
            Hy.gF();
        }
    }

    public void a(om omVar) {
        if (omVar != null) {
            ArrayList aC = omVar.aC();
            if (aC != null && aC.size() != 0) {
                B(aC);
            }
        }
    }

    public void start() {
        gE();
    }
}
