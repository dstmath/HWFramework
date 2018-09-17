package tmsdkobf;

import com.qq.taf.jce.JceStruct;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;
import tmsdkobf.kx.a;

public class mt {
    private Object Ba = new Object();
    private mr<String> Bb;
    private mr<String> Bc;
    private mr<String> Bd = new mr(5);
    private mr<String> Be = new mr(5);
    private mr<String> Bf = new mr(5);
    private mr<String> Bg = new mr(5);
    private mr<String> Bh = new mr(5);
    private mr<String> Bi = new mr(5);
    private a Bj = new a() {
        public void dT() {
            mt.this.ff();
        }
    };
    private volatile boolean xT = false;

    private void a(final aq aqVar, final CountDownLatch countDownLatch) {
        if (aqVar != null) {
            im.bK().a(3122, aqVar, null, 0, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    switch (ne.bg(i3)) {
                        case 0:
                            synchronized (mt.this.Ba) {
                                mt.this.Bd.removeAll((Collection) aqVar.bI.get(Integer.valueOf(1)));
                                mt.this.Be.removeAll((Collection) aqVar.bI.get(Integer.valueOf(2)));
                                mt.this.Bf.removeAll((Collection) aqVar.bI.get(Integer.valueOf(3)));
                                mt.this.Bg.removeAll((Collection) aqVar.bI.get(Integer.valueOf(4)));
                                mt.this.Bh.removeAll((Collection) aqVar.bI.get(Integer.valueOf(5)));
                                mt.this.Bi.removeAll((Collection) aqVar.bI.get(Integer.valueOf(6)));
                            }
                    }
                    countDownLatch.countDown();
                }
            }, 5000);
            return;
        }
        countDownLatch.countDown();
    }

    private void a(mr<String> mrVar, boolean z) {
        int size;
        Queue fc;
        if (z) {
            size = this.Bb.size();
            fc = this.Bb.fc();
        } else {
            size = this.Bc.size();
            fc = this.Bc.fc();
            if (size == 0) {
                size = this.Bb.size();
                fc = this.Bb.fc();
            }
        }
        if (size != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            if (size <= 40) {
                for (String str : fc) {
                    stringBuilder.append(str + "``");
                }
            } else {
                int i = 0;
                int i2 = size - 20;
                for (String str2 : fc) {
                    if (i < 20) {
                        stringBuilder.append(str2 + "``");
                    }
                    if (i > i2) {
                        stringBuilder.append(str2 + "``");
                    }
                    i++;
                }
            }
            if (z) {
                this.Bb.clear();
            } else {
                this.Bc.clear();
            }
            mrVar.offer(stringBuilder.toString());
        }
    }

    private void ff() {
        if (gf.S().aa().booleanValue() && this.Bf.size() != 0 && i.K(TMSDKContext.getApplicaionContext()) && !this.xT) {
            this.xT = true;
            im.bJ().addTask(new Runnable() {
                public void run() {
                    f.h("QQPimSecure", "OptimusReport sendOptimusData 01");
                    aq b = mt.this.fg();
                    if (b != null) {
                        CountDownLatch countDownLatch = new CountDownLatch(1);
                        mt.this.a(b, countDownLatch);
                        try {
                            countDownLatch.await(5000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            f.h("QQPimSecure", "OptimusReport sendOptimusData InterruptedException!");
                        }
                    }
                    f.h("QQPimSecure", "OptimusReport sendOptimusData 10");
                    mt.this.xT = false;
                }
            }, "OptimusReport");
        }
    }

    private aq fg() {
        if (this.Bf.size() == 0) {
            return null;
        }
        aq aqVar = new aq();
        aqVar.bC = 26;
        aqVar.bI = new HashMap();
        synchronized (this.Ba) {
            aqVar.bI.put(Integer.valueOf(1), this.Bd.fd());
            aqVar.bI.put(Integer.valueOf(2), this.Be.fd());
            aqVar.bI.put(Integer.valueOf(3), this.Bf.fd());
            aqVar.bI.put(Integer.valueOf(4), this.Bg.fd());
            aqVar.bI.put(Integer.valueOf(5), this.Bh.fd());
            aqVar.bI.put(Integer.valueOf(6), this.Bi.fd());
        }
        return aqVar;
    }

    public synchronized void a(String str, String str2, String str3, String str4, String str5, boolean z, boolean z2) {
        synchronized (this.Ba) {
            a(this.Bi, z);
            this.Bd.offer(str);
            this.Be.offer(str2);
            this.Bf.offer(str3);
            this.Bg.offer(str4);
            this.Bh.offer(str5);
        }
        if (z2) {
            ff();
        }
    }

    public void bZ(String str) {
        Date date = new Date();
        String replace = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date) + "``" + str).replace("\n", "``").replace("|", ":");
        synchronized (this.Ba) {
            if (this.Bb == null) {
                this.Bb = new mr(80);
                this.Bc = new mr(80);
            }
            this.Bb.offer(replace);
        }
    }

    public void destroy() {
        kx.dR().b(this.Bj);
    }

    public void fe() {
        synchronized (this.Ba) {
            if (this.Bb != null) {
                if (this.Bc == null) {
                    this.Bc = new mr(80);
                }
                this.Bc.clear();
                this.Bc.addAll(this.Bb.fc());
                return;
            }
        }
    }

    public void init() {
        kx.dR().a(this.Bj);
    }
}
