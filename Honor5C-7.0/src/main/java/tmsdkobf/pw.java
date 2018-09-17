package tmsdkobf;

import android.content.Context;
import tmsdk.common.utils.d;
import tmsdkobf.po.b;
import tmsdkobf.pv.a;

/* compiled from: Unknown */
public class pw {
    public static String TAG;
    private final int Em;
    private on Et;
    private pj HE;
    private int HX;
    private pv IX;
    b IY;
    private volatile boolean IZ;
    private po Ja;
    private Context context;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.pw.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.pw.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.pw.<clinit>():void");
    }

    public pw(Context context) {
        this.Em = 3;
        this.IX = new pv();
        this.context = null;
        this.HE = null;
        this.IY = null;
        this.HX = pk.HX;
        this.IZ = false;
        this.Ja = null;
        this.context = context;
    }

    private void gS() {
        if (hy() && this.Ja != null) {
            this.Ja.stop();
        }
    }

    public void a(on onVar) {
        this.Et = onVar;
    }

    public void a(pj pjVar) {
        this.HE = pjVar;
    }

    public void a(px pxVar) {
        this.IX.a((a) pxVar);
    }

    public void a(boolean z, b bVar) {
        this.IZ = z;
        this.IY = bVar;
        if (this.IZ) {
            this.Ja = new po(this.context, bVar);
            this.Ja.co(this.HX);
            this.Ja.start();
            return;
        }
        if (this.Ja != null) {
            this.Ja.stop();
        }
        this.Ja = null;
    }

    public void close() {
        this.IX.fH();
        gS();
    }

    public void co(int i) {
        if (!(this.HX == i || this.Ja == null)) {
            this.Ja.co(i);
        }
    }

    public int gX() {
        if (mu.fb()) {
            d.f(TAG, "reconnect HttpConnection.couldNotConnect()");
            return -230000;
        }
        gS();
        this.HE.gL();
        long currentTimeMillis = System.currentTimeMillis();
        int a = this.IX.a(this.HE);
        this.Et.j(System.currentTimeMillis() - currentTimeMillis);
        a(this.IZ, this.IY);
        return a;
    }

    public boolean hs() {
        return this.IX.hs();
    }

    public boolean hv() {
        return this.IX.hv();
    }

    public int hx() {
        d.d(TAG, "connect");
        if (mu.fb()) {
            d.f(TAG, "connect HttpConnection.couldNotConnect()");
            return -230000;
        }
        long currentTimeMillis = System.currentTimeMillis();
        int a = this.IX.a(this.context, this.HE);
        this.Et.j(System.currentTimeMillis() - currentTimeMillis);
        return a;
    }

    public boolean hy() {
        return this.IZ;
    }

    public void hz() {
        if (hy()) {
            try {
                this.Ja.reset();
            } catch (Throwable th) {
                d.c(TAG, th);
            }
        }
    }

    public int x(byte[] bArr) {
        int i = -900000;
        for (int i2 = 0; i2 < 3; i2++) {
            i = this.IX.x(bArr);
            if (i == 0) {
                break;
            }
            if (2 != i2) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    d.c(TAG, "sendData() InterruptedException e: " + e.toString());
                }
            }
        }
        return i;
    }
}
