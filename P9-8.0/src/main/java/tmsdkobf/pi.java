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

final class pi extends BaseManagerC {
    private LinkedList<b> Jy = new LinkedList();
    private id Jz;
    private Context mContext;

    interface a {
        void cp(String str);
    }

    static final class b implements pg {
        private pg JA;

        public b(pg pgVar) {
            this.JA = pgVar;
        }

        public final void aQ(final String str) {
            im.bJ().newFreeThread(new Runnable() {
                public void run() {
                    b.this.JA.aQ(str);
                }
            }, "onPackageAddedThread").start();
        }

        public void aR(final String str) {
            im.bJ().newFreeThread(new Runnable() {
                public void run() {
                    b.this.JA.aR(str);
                }
            }, "onPackageReinstallThread").start();
        }

        public final void aS(final String str) {
            im.bJ().newFreeThread(new Runnable() {
                public void run() {
                    b.this.JA.aS(str);
                }
            }, "onPackageRemovedThread").start();
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof b)) {
                return false;
            }
            return this.JA.getClass().equals(((b) obj).JA.getClass());
        }
    }

    private final class c extends id {
        private d JD;

        private c() {
        }

        public IBinder onBind() {
            return null;
        }

        public void onCreate(Context context) {
            super.onCreate(context);
            this.JD = new d();
            this.JD.register();
        }

        public void onDestory() {
            this.JD.hK();
            super.onDestory();
        }
    }

    private final class d extends if {
        private a JF;
        private a JG;
        private a JH;

        private d() {
            this.JF = new a() {
                public void cp(String str) {
                    Iterator it = pi.this.Jy.iterator();
                    while (it.hasNext()) {
                        ((b) it.next()).aQ(str);
                    }
                }
            };
            this.JG = new a() {
                public void cp(String str) {
                    Iterator it = pi.this.Jy.iterator();
                    while (it.hasNext()) {
                        ((b) it.next()).aS(str);
                    }
                }
            };
            this.JH = new a() {
                public void cp(String str) {
                    Iterator it = pi.this.Jy.iterator();
                    while (it.hasNext()) {
                        ((b) it.next()).aR(str);
                    }
                }
            };
        }

        private void a(final a aVar, final String str) {
            im.bJ().newFreeThread(new Runnable() {
                public void run() {
                    synchronized (pi.this.Jy) {
                        aVar.cp(str);
                    }
                }
            }, "handlePackageChangeThread").start();
        }

        public void doOnRecv(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            Object obj = -1;
            if (extras != null && extras.containsKey("android.intent.extra.REPLACING")) {
                obj = !extras.getBoolean("android.intent.extra.REPLACING") ? 1 : null;
            }
            if (action.equals("android.intent.action.PACKAGE_ADDED") && obj != null) {
                a(this.JF, intent.getDataString().substring(8));
            } else if (action.equals("android.intent.action.PACKAGE_REMOVED") && obj != null) {
                a(this.JG, intent.getDataString().substring(8));
            } else if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                a(this.JH, intent.getDataString().substring(8));
            }
        }

        public void hK() {
            pi.this.mContext.unregisterReceiver(this);
        }

        public void register() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
            intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.setPriority(Integer.MAX_VALUE);
            intentFilter.addDataScheme("package");
            pi.this.mContext.registerReceiver(this, intentFilter);
        }
    }

    pi() {
    }

    public pg c(pg pgVar) {
        synchronized (this.Jy) {
            Object bVar = pgVar == null ? null : new b(pgVar);
            if (bVar != null) {
                if (!this.Jy.contains(bVar)) {
                    this.Jy.add(bVar);
                    return pgVar;
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
