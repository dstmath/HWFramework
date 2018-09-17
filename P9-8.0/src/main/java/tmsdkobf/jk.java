package tmsdkobf;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.SparseArray;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.utils.f;
import tmsdkobf.ji.a;
import tmsdkobf.ji.b;

public class jk {
    private static Object lock = new Object();
    private static jk tf;
    Handler handler;
    private SparseArray<ji> td;
    HandlerThread te;

    private jk() {
        this.td = new SparseArray();
        this.te = null;
        this.handler = null;
        this.te = ((ki) fj.D(4)).newFreeHandlerThread("ProfileServiceManager");
        this.te.start();
        this.handler = new Handler(this.te.getLooper()) {
            public void handleMessage(Message message) {
                f.h("demo", "handler 000 : " + System.currentTimeMillis() + "  msg.what = " + message.what);
                final ji jiVar;
                switch (message.what) {
                    case 1:
                        jj jjVar = (jj) message.obj;
                        ArrayList arrayList = jjVar.tc;
                        ji jiVar2 = jjVar.tb;
                        if (!(arrayList == null || arrayList.size() <= 0 || jiVar2 == null)) {
                            jiVar2.i(arrayList);
                            break;
                        }
                    case 2:
                        Object obj = message.obj;
                        if (obj != null && (obj instanceof ji)) {
                            jiVar = (ji) obj;
                            jk.this.td.remove(jiVar.cs());
                            jk.this.td.append(jiVar.cs(), jiVar);
                            jiVar.a(new gt() {
                                public void a(int i, ArrayList<JceStruct> arrayList, int i2, int i3) {
                                    if (i2 == 0) {
                                        jiVar.h(jk.this.a(i, (ArrayList) arrayList));
                                        if (i == 0) {
                                            jiVar.ag(i3);
                                        }
                                    }
                                }
                            });
                            break;
                        }
                        return;
                        break;
                    case 3:
                        Integer num = (Integer) message.obj;
                        if (num != null) {
                            jiVar = (ji) jk.this.td.get(num.intValue());
                            if (jiVar != null) {
                                jiVar.ct();
                                break;
                            }
                            return;
                        }
                        return;
                    case 4:
                        jk.this.b((ji) jk.this.td.get(((Integer) message.obj).intValue()));
                        break;
                }
                f.h("demo", "handler 001 : " + System.currentTimeMillis());
            }
        };
    }

    private void b(final ji jiVar) {
        if (jiVar != null) {
            jiVar.a(new b() {
                public void j(ArrayList<a> arrayList) {
                    if (arrayList != null && arrayList.size() > 0) {
                        Message.obtain(jk.this.handler, 1, new jj(jiVar, arrayList)).sendToTarget();
                    }
                }
            });
        }
    }

    public static jk cv() {
        if (tf == null) {
            synchronized (lock) {
                if (tf == null) {
                    tf = new jk();
                }
            }
        }
        return tf;
    }

    protected ArrayList<a> a(int i, ArrayList<JceStruct> arrayList) {
        ArrayList<a> arrayList2 = new ArrayList();
        if (arrayList == null || arrayList.size() == 0) {
            return arrayList2;
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            JceStruct jceStruct = (JceStruct) it.next();
            if (jceStruct != null) {
                a aVar = new a();
                aVar.ta = jceStruct;
                aVar.action = i;
                arrayList2.add(aVar);
            }
        }
        return arrayList2;
    }

    public void a(ji jiVar) {
        if (jiVar != null && this.handler != null) {
            Message.obtain(this.handler, 2, jiVar).sendToTarget();
        }
    }

    public void ah(int i) {
        Message.obtain(this.handler, 4, Integer.valueOf(i)).sendToTarget();
    }
}
