package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import tmsdkobf.nw.f;

public class ng {
    private static String TAG = "HttpNetworkManager";
    private nl CT;
    private om CU;
    private boolean CX = false;
    private int CY = 0;
    private LinkedList<a> CZ = new LinkedList();
    private Context mContext;
    private Handler mHandler = new Handler(nu.getLooper()) {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    synchronized (ng.this.mLock) {
                        if (ng.this.CY >= 4) {
                            mb.s(ng.TAG, "[http_control]handleMessage(), not allow start, running tasks(>=4): " + ng.this.CY);
                        } else {
                            a aVar = (a) ng.this.CZ.poll();
                            if (aVar == null) {
                                mb.d(ng.TAG, "[http_control]handleMessage(), allow start but no data to send, running tasks: " + ng.this.CY);
                            } else {
                                mb.n(ng.TAG, "[http_control]handleMessage(), allow start, running tasks: " + ng.this.CY);
                                ng.this.CY = ng.this.CY + 1;
                                ng.this.b(aVar.Dh, aVar.data, aVar.Di);
                            }
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private final Object mLock = new Object();

    private class a {
        public f Dh = null;
        public tmsdkobf.nf.a Di = null;
        public byte[] data = null;

        public a(byte[] bArr, f fVar, tmsdkobf.nf.a aVar) {
            this.data = bArr;
            this.Dh = fVar;
            this.Di = aVar;
        }
    }

    public ng(Context context, nl nlVar, om omVar, boolean z) {
        this.mContext = context;
        this.CT = nlVar;
        this.CU = omVar;
        this.CX = z;
    }

    private void b(final f fVar, final byte[] bArr, final tmsdkobf.nf.a aVar) {
        Runnable anonymousClass2 = new Runnable() {
            public void run() {
                int a;
                AtomicReference atomicReference = new AtomicReference();
                try {
                    a = new nf(ng.this.mContext, ng.this.CT, ng.this.CU, ng.this.CX).a(fVar, bArr, atomicReference);
                } catch (Throwable th) {
                    mb.c(ng.TAG, "sendDataAsyn(), exception:", th);
                    a = -1200;
                }
                final int i = a;
                final byte[] bArr = (byte[]) atomicReference.get();
                Runnable anonymousClass1 = new Runnable() {
                    public void run() {
                        if (aVar != null) {
                            aVar.b(i, bArr);
                        }
                    }
                };
                ki kiVar = (ki) fj.D(4);
                if (nu.aC()) {
                    kiVar.a(anonymousClass1, "shark-http-callback");
                } else {
                    kiVar.addTask(anonymousClass1, "shark-http-callback");
                }
                synchronized (ng.this.mLock) {
                    ng.this.CY = ng.this.CY - 1;
                    if (ng.this.CZ.size() > 0) {
                        ng.this.mHandler.sendEmptyMessage(1);
                    }
                    mb.d(ng.TAG, "[http_control]-------- send finish, running tasks: " + ng.this.CY + ", waiting tasks: " + ng.this.CZ.size());
                }
            }
        };
        ki kiVar = (ki) fj.D(4);
        if (nu.aC()) {
            kiVar.a(anonymousClass2, "shark-http-send");
        } else {
            kiVar.addTask(anonymousClass2, "shark-http-send");
        }
    }

    public void a(f fVar, byte[] bArr, tmsdkobf.nf.a aVar) {
        synchronized (this.mLock) {
            this.CZ.add(new a(bArr, fVar, aVar));
            mb.r(TAG, "[http_control]sendDataAsyn(), waiting tasks: " + this.CZ.size());
        }
        this.mHandler.sendEmptyMessage(1);
    }
}
