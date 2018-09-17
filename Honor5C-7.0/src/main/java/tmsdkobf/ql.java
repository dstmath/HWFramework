package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import java.util.Iterator;
import java.util.LinkedList;
import tmsdk.common.TMSService;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.module.urlcheck.UrlCheckType;

/* compiled from: Unknown */
final class ql extends BaseManagerC {
    private LinkedList<b> Jy;
    private jh Jz;
    private Context mContext;

    /* compiled from: Unknown */
    interface a {
        void cW(String str);
    }

    /* compiled from: Unknown */
    static final class b implements qj {
        private qj JA;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.ql.b.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ String JB;
            final /* synthetic */ b JC;

            AnonymousClass1(b bVar, String str) {
                this.JC = bVar;
                this.JB = str;
            }

            public void run() {
                this.JC.JA.bQ(this.JB);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.ql.b.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ String JB;
            final /* synthetic */ b JC;

            AnonymousClass2(b bVar, String str) {
                this.JC = bVar;
                this.JB = str;
            }

            public void run() {
                this.JC.JA.bS(this.JB);
            }
        }

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.ql.b.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ String JB;
            final /* synthetic */ b JC;

            AnonymousClass3(b bVar, String str) {
                this.JC = bVar;
                this.JB = str;
            }

            public void run() {
                this.JC.JA.bR(this.JB);
            }
        }

        public b(qj qjVar) {
            this.JA = qjVar;
        }

        public final void bQ(String str) {
            jq.ct().c(new AnonymousClass1(this, str), "onPackageAddedThread").start();
        }

        public void bR(String str) {
            jq.ct().c(new AnonymousClass3(this, str), "onPackageReinstallThread").start();
        }

        public final void bS(String str) {
            jq.ct().c(new AnonymousClass2(this, str), "onPackageRemovedThread").start();
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof b)) {
                return false;
            }
            return this.JA.getClass().equals(((b) obj).JA.getClass());
        }
    }

    /* compiled from: Unknown */
    private final class c extends jh {
        private d JD;
        final /* synthetic */ ql JE;

        private c(ql qlVar) {
            this.JE = qlVar;
        }

        public IBinder onBind() {
            return null;
        }

        public void onCreate(Context context) {
            super.onCreate(context);
            this.JD = new d(null);
            this.JD.register();
        }

        public void onDestory() {
            this.JD.hM();
            super.onDestory();
        }
    }

    /* compiled from: Unknown */
    private final class d extends jj {
        final /* synthetic */ ql JE;
        private a JF;
        private a JG;
        private a JH;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.ql.d.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ d JI;
            final /* synthetic */ a JJ;
            final /* synthetic */ String JK;

            AnonymousClass4(d dVar, a aVar, String str) {
                this.JI = dVar;
                this.JJ = aVar;
                this.JK = str;
            }

            public void run() {
                synchronized (this.JI.JE.Jy) {
                    this.JJ.cW(this.JK);
                }
            }
        }

        private d(ql qlVar) {
            this.JE = qlVar;
            this.JF = new a() {
                final /* synthetic */ d JI;

                {
                    this.JI = r1;
                }

                public void cW(String str) {
                    Iterator it = this.JI.JE.Jy.iterator();
                    while (it.hasNext()) {
                        ((b) it.next()).bQ(str);
                    }
                }
            };
            this.JG = new a() {
                final /* synthetic */ d JI;

                {
                    this.JI = r1;
                }

                public void cW(String str) {
                    Iterator it = this.JI.JE.Jy.iterator();
                    while (it.hasNext()) {
                        ((b) it.next()).bS(str);
                    }
                }
            };
            this.JH = new a() {
                final /* synthetic */ d JI;

                {
                    this.JI = r1;
                }

                public void cW(String str) {
                    Iterator it = this.JI.JE.Jy.iterator();
                    while (it.hasNext()) {
                        ((b) it.next()).bR(str);
                    }
                }
            };
        }

        private void a(a aVar, String str) {
            jq.ct().c(new AnonymousClass4(this, aVar, str), "handlePackageChangeThread").start();
        }

        public void doOnRecv(Context context, Intent intent) {
            Object obj = null;
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            if (extras == null || !extras.containsKey("android.intent.extra.REPLACING")) {
                obj = -1;
            } else if (!extras.getBoolean("android.intent.extra.REPLACING")) {
                obj = 1;
            }
            if (action.equals("android.intent.action.PACKAGE_ADDED") && r0 != null) {
                a(this.JF, intent.getDataString().substring(8));
            } else if (action.equals("android.intent.action.PACKAGE_REMOVED") && r0 != null) {
                a(this.JG, intent.getDataString().substring(8));
            } else if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                a(this.JH, intent.getDataString().substring(8));
            }
        }

        public void hM() {
            this.JE.mContext.unregisterReceiver(this);
        }

        public void register() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
            intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.setPriority(UrlCheckType.UNKNOWN);
            intentFilter.addDataScheme("package");
            this.JE.mContext.registerReceiver(this, intentFilter);
        }
    }

    ql() {
        this.Jy = new LinkedList();
    }

    public qj c(qj qjVar) {
        synchronized (this.Jy) {
            Object bVar = qjVar == null ? null : new b(qjVar);
            if (bVar != null) {
                if (!this.Jy.contains(bVar)) {
                    this.Jy.add(bVar);
                    return qjVar;
                }
            }
            return null;
        }
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.Jz = new c();
        TMSService.startService(this.Jz, null);
    }
}
