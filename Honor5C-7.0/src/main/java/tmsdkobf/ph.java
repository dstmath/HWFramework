package tmsdkobf;

import android.content.Context;
import java.util.concurrent.atomic.AtomicReference;
import tmsdkobf.pb.d;

/* compiled from: Unknown */
public class ph implements or {
    static final /* synthetic */ boolean fJ = false;
    private on Et;
    private boolean HA;
    private oj HB;
    private pk HC;
    private b HD;
    private pj HE;
    private final String TAG;

    /* compiled from: Unknown */
    public interface b {
        void a(boolean z, int i, byte[] bArr);
    }

    /* compiled from: Unknown */
    public interface a {
        void a(boolean z, int i, d dVar);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ph.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ph.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ph.<clinit>():void");
    }

    public ph(boolean z, Context context, oq oqVar, boolean z2, b bVar, tmsdkobf.pk.a aVar, tmsdkobf.pb.a aVar2) {
        this.TAG = "SharkWharf";
        this.HA = false;
        this.HC = pk.gQ();
        this.HA = z;
        this.HD = bVar;
        this.Et = oqVar;
        if (this.HA) {
            this.HE = new pj(context, z2, oqVar);
            this.HB = new oj(context, this.HE, oqVar);
            pk.gQ().a(oqVar, oqVar, this.HE, bVar, aVar, this, aVar2);
        }
    }

    public synchronized void a(d dVar, byte[] bArr, a aVar) {
        if (!this.HA) {
            pa.c("ocean", "not in sending process!", null, null);
            throw new RuntimeException("not in sending process!");
        } else if (dVar != null) {
            long j;
            AtomicReference atomicReference;
            int a;
            if (bArr != null) {
                if (bArr.length > 0) {
                    tmsdk.common.utils.d.e("SharkWharf", "sendData() isSharkVip: " + dVar.Ga + " isTcpHello: " + dVar.Gb + " isTcpFirst: " + dVar.Gc + " data.length: " + bArr.length);
                    if (dVar.Ge) {
                        dVar.Gd = true;
                        if (!dVar.gr()) {
                            tmsdk.common.utils.d.e("SharkWharf", "sendData() isTcpVip");
                            this.HC.a(bArr, aVar, dVar);
                            return;
                        } else if (dVar.gs()) {
                            j = dVar.Gi;
                            tmsdk.common.utils.d.d("SharkWharf", "sendData() tcp\u901a\u9053");
                            this.HC.a(j, dVar.Gb, dVar.Gc, bArr, aVar, dVar);
                            return;
                        } else {
                            tmsdk.common.utils.d.e("SharkWharf", "sendData() isSharkVip connected");
                            dVar.Gd = true;
                            this.HC.a(bArr, aVar, dVar);
                            return;
                        }
                    }
                    dVar.Gd = false;
                    aVar.a(false, 0, dVar);
                    atomicReference = new AtomicReference();
                    if (this.HB == null) {
                        a = this.HB.a(bArr, atomicReference);
                        this.Et.as(a);
                        tmsdk.common.utils.d.e("SharkWharf", "onBefore() only http\u901a\u9053 retCode: " + a);
                        this.HD.a(false, a, (byte[]) atomicReference.get());
                        return;
                    }
                    pa.c("SharkWharf", "mHttpNetwork == null. maybe is fore process.", null, null);
                    return;
                }
            }
            tmsdk.common.utils.d.c("SharkWharf", "sendData() data is empty");
            tmsdk.common.utils.d.e("SharkWharf", "sendData() isSharkVip: " + dVar.Ga + " isTcpHello: " + dVar.Gb + " isTcpFirst: " + dVar.Gc);
            if (dVar.Ge) {
                dVar.Gd = false;
                aVar.a(false, 0, dVar);
                atomicReference = new AtomicReference();
                if (this.HB == null) {
                    pa.c("SharkWharf", "mHttpNetwork == null. maybe is fore process.", null, null);
                    return;
                }
                a = this.HB.a(bArr, atomicReference);
                this.Et.as(a);
                tmsdk.common.utils.d.e("SharkWharf", "onBefore() only http\u901a\u9053 retCode: " + a);
                this.HD.a(false, a, (byte[]) atomicReference.get());
                return;
            }
            dVar.Gd = true;
            if (!dVar.gr()) {
                tmsdk.common.utils.d.e("SharkWharf", "sendData() isTcpVip");
                this.HC.a(bArr, aVar, dVar);
                return;
            } else if (dVar.gs()) {
                tmsdk.common.utils.d.e("SharkWharf", "sendData() isSharkVip connected");
                dVar.Gd = true;
                this.HC.a(bArr, aVar, dVar);
                return;
            } else {
                j = dVar.Gi;
                tmsdk.common.utils.d.d("SharkWharf", "sendData() tcp\u901a\u9053");
                this.HC.a(j, dVar.Gb, dVar.Gc, bArr, aVar, dVar);
                return;
            }
        } else {
            return;
        }
    }

    public void a(a aVar, d dVar, int i, byte[] bArr) {
        boolean z = false;
        if (aVar == null || dVar == null) {
            String str = "SharkWharf";
            StringBuilder append = new StringBuilder().append("onSendFailed() beforeSend is null : ").append(aVar == null).append(" sharkSend is null : ");
            if (dVar == null) {
                z = true;
            }
            tmsdk.common.utils.d.c(str, append.append(z).toString());
            return;
        }
        dVar.Gd = false;
        if (dVar.gr()) {
            tmsdk.common.utils.d.e("SharkWharf", "sendData()");
            aVar.a(true, i, dVar);
            this.HD.a(false, -800, null);
            return;
        }
        tmsdk.common.utils.d.d("SharkWharf", "tcp\u901a\u9053\u53d1\u9001\u5931\u8d25\uff0c\u8f6chttp\u901a\u9053");
        aVar.a(true, i, dVar);
        AtomicReference atomicReference = new AtomicReference();
        if (this.HB != null) {
            int a = this.HB.a(bArr, atomicReference);
            tmsdk.common.utils.d.e("SharkWharf", "onSendFailed() http \u901a\u9053 retCode: " + a);
            this.HD.a(false, a, (byte[]) atomicReference.get());
            return;
        }
        pa.c("SharkWharf", "mHttpNetwork == null. maybe is fore process.", null, null);
    }

    public os gI() {
        return this.HE;
    }

    public pk gk() {
        if (this.HA) {
            return this.HC;
        }
        if (fJ) {
            return this.HC;
        }
        throw new AssertionError("SharkWharf TmsTcpManager is null ");
    }

    public void n(boolean z) {
        if (this.HA && this.HE != null) {
            this.HE.n(z);
        }
    }
}
