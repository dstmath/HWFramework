package tmsdkobf;

import android.telephony.PhoneStateListener;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.DualSimTelephonyManager;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class ja {
    private int tn;
    private PhoneStateListener to;
    private PhoneStateListener tp;
    private List<b> tq;

    /* compiled from: Unknown */
    public interface b {
        void bx(String str);

        void by(String str);

        void bz(String str);

        void i(String str, String str2);
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ja.1 */
    class AnonymousClass1 extends PhoneStateListener {
        final /* synthetic */ ja tr;

        AnonymousClass1(ja jaVar, int i) {
            this.tr = jaVar;
            super(i);
        }

        public void onCallStateChanged(int i, String str) {
            String str2 = null;
            switch (i) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    this.tr.by(str);
                    break;
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    qz qzVar = jq.uh;
                    if (qzVar != null) {
                        str2 = qzVar.cA(0);
                    }
                    this.tr.j(str, str2);
                    break;
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    if (this.tr.tn != 1) {
                        if (this.tr.tn == 0) {
                            this.tr.bC(str);
                            break;
                        }
                    }
                    this.tr.bx(str);
                    break;
                    break;
            }
            this.tr.tn = i;
            super.onCallStateChanged(i, str);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ja.2 */
    class AnonymousClass2 extends PhoneStateListener {
        final /* synthetic */ ja tr;

        AnonymousClass2(ja jaVar, int i) {
            this.tr = jaVar;
            super(i);
        }

        public void onCallStateChanged(int i, String str) {
            String str2 = null;
            switch (i) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    this.tr.by(str);
                    break;
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    qz qzVar = jq.uh;
                    if (qzVar != null) {
                        str2 = qzVar.cA(1);
                    }
                    if (str2 == null) {
                        d.c("PhoneStateManager", "Incoming call from 2nd sim card but no card value!");
                    }
                    this.tr.j(str, str2);
                    break;
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    if (this.tr.tn != 1) {
                        if (this.tr.tn == 0) {
                            this.tr.bC(str);
                            break;
                        }
                    }
                    this.tr.bx(str);
                    break;
                    break;
            }
            this.tr.tn = i;
            super.onCallStateChanged(i, str);
        }
    }

    /* compiled from: Unknown */
    private static class a {
        static ja ts;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ja.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ja.a.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ja.a.<clinit>():void");
        }
    }

    public ja() {
        this.tn = 0;
        this.tq = new ArrayList();
        register();
    }

    private void bC(String str) {
        d.d("PhoneStateManager", "onOutCall number=" + str);
        synchronized (this.tq) {
            for (b bz : this.tq) {
                bz.bz(str);
            }
        }
    }

    private void bx(String str) {
        d.d("PhoneStateManager", "onConnect number=" + str);
        synchronized (this.tq) {
            for (b bx : this.tq) {
                bx.bx(str);
            }
        }
    }

    private void by(String str) {
        d.d("PhoneStateManager", "onHoldOff number=" + str);
        synchronized (this.tq) {
            for (b by : this.tq) {
                by.by(str);
            }
        }
    }

    public static ja ce() {
        return a.ts;
    }

    private void j(String str, String str2) {
        d.d("PhoneStateManager", "onCallComing number=" + str);
        synchronized (this.tq) {
            for (b i : this.tq) {
                i.i(str, str2);
            }
        }
    }

    private void register() {
        int i;
        DualSimTelephonyManager instance;
        qz qzVar = jq.uh;
        if (qzVar != null && qzVar.il()) {
            try {
                this.to = new AnonymousClass1(this, 0);
                this.tp = new AnonymousClass2(this, 1);
                i = 0;
            } catch (Throwable th) {
            }
            if (i != 0) {
                this.to = new PhoneStateListener() {
                    final /* synthetic */ ja tr;

                    {
                        this.tr = r1;
                    }

                    public void onCallStateChanged(int i, String str) {
                        String str2 = null;
                        qz qzVar = jq.uh;
                        if (qzVar != null) {
                            String im = qzVar.im();
                            if (im != null && im.indexOf("htc") > -1) {
                                if (im.indexOf("t328w") > -1 || im.indexOf("t328d") > -1) {
                                    super.onCallStateChanged(i, str);
                                    return;
                                }
                            }
                        }
                        switch (i) {
                            case SpaceManager.ERROR_CODE_OK /*0*/:
                                this.tr.by(str);
                                break;
                            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                                if (qzVar != null) {
                                    str2 = qzVar.cA(0);
                                }
                                this.tr.j(str, str2);
                                break;
                            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                                if (this.tr.tn != 1) {
                                    if (this.tr.tn == 0) {
                                        this.tr.bC(str);
                                        break;
                                    }
                                }
                                this.tr.bx(str);
                                break;
                                break;
                        }
                        this.tr.tn = i;
                        super.onCallStateChanged(i, str);
                    }
                };
                this.tp = new PhoneStateListener() {
                    final /* synthetic */ ja tr;

                    {
                        this.tr = r1;
                    }

                    public void onCallStateChanged(int i, String str) {
                        String str2 = null;
                        switch (i) {
                            case SpaceManager.ERROR_CODE_OK /*0*/:
                                this.tr.by(str);
                                break;
                            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                                qz qzVar = jq.uh;
                                if (qzVar != null) {
                                    str2 = qzVar.cA(1);
                                }
                                if (str2 == null) {
                                    d.c("PhoneStateManager", "Incoming call from 2nd sim card but no card value!");
                                }
                                this.tr.j(str, str2);
                                break;
                            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                                if (this.tr.tn != 1) {
                                    if (this.tr.tn == 0) {
                                        this.tr.bC(str);
                                        break;
                                    }
                                }
                                this.tr.bx(str);
                                break;
                                break;
                        }
                        this.tr.tn = i;
                        super.onCallStateChanged(i, str);
                    }
                };
            }
            instance = DualSimTelephonyManager.getInstance();
            instance.listenPhonesState(0, this.to, 32);
            instance.listenPhonesState(1, this.tp, 32);
        }
        i = 1;
        if (i != 0) {
            this.to = new PhoneStateListener() {
                final /* synthetic */ ja tr;

                {
                    this.tr = r1;
                }

                public void onCallStateChanged(int i, String str) {
                    String str2 = null;
                    qz qzVar = jq.uh;
                    if (qzVar != null) {
                        String im = qzVar.im();
                        if (im != null && im.indexOf("htc") > -1) {
                            if (im.indexOf("t328w") > -1 || im.indexOf("t328d") > -1) {
                                super.onCallStateChanged(i, str);
                                return;
                            }
                        }
                    }
                    switch (i) {
                        case SpaceManager.ERROR_CODE_OK /*0*/:
                            this.tr.by(str);
                            break;
                        case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                            if (qzVar != null) {
                                str2 = qzVar.cA(0);
                            }
                            this.tr.j(str, str2);
                            break;
                        case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                            if (this.tr.tn != 1) {
                                if (this.tr.tn == 0) {
                                    this.tr.bC(str);
                                    break;
                                }
                            }
                            this.tr.bx(str);
                            break;
                            break;
                    }
                    this.tr.tn = i;
                    super.onCallStateChanged(i, str);
                }
            };
            this.tp = new PhoneStateListener() {
                final /* synthetic */ ja tr;

                {
                    this.tr = r1;
                }

                public void onCallStateChanged(int i, String str) {
                    String str2 = null;
                    switch (i) {
                        case SpaceManager.ERROR_CODE_OK /*0*/:
                            this.tr.by(str);
                            break;
                        case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                            qz qzVar = jq.uh;
                            if (qzVar != null) {
                                str2 = qzVar.cA(1);
                            }
                            if (str2 == null) {
                                d.c("PhoneStateManager", "Incoming call from 2nd sim card but no card value!");
                            }
                            this.tr.j(str, str2);
                            break;
                        case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                            if (this.tr.tn != 1) {
                                if (this.tr.tn == 0) {
                                    this.tr.bC(str);
                                    break;
                                }
                            }
                            this.tr.bx(str);
                            break;
                            break;
                    }
                    this.tr.tn = i;
                    super.onCallStateChanged(i, str);
                }
            };
        }
        instance = DualSimTelephonyManager.getInstance();
        instance.listenPhonesState(0, this.to, 32);
        instance.listenPhonesState(1, this.tp, 32);
    }

    @Deprecated
    public void a(b bVar) {
        synchronized (this.tq) {
            this.tq.add(0, bVar);
        }
    }

    public boolean b(b bVar) {
        boolean remove;
        synchronized (this.tq) {
            remove = !this.tq.contains(bVar) ? true : this.tq.remove(bVar);
        }
        return remove;
    }
}
