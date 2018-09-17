package tmsdkobf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.CallerIdent;
import tmsdk.common.utils.f;
import tmsdkobf.ji.a;
import tmsdkobf.ji.b;
import tmsdkobf.ji.c;

public class jn implements ji {
    private static Object lock = new Object();
    private static jn tl;
    Handler handler;
    private HashSet<jl> oW;
    private ConcurrentLinkedQueue<a> tk;
    b tm;
    pe<Integer, as> tn;

    private jn() {
        this.tk = new ConcurrentLinkedQueue();
        this.tm = null;
        this.tn = new pe(20);
        this.handler = null;
        this.oW = new HashSet();
        this.handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 0:
                        jn.this.handler.removeMessages(0);
                        if (jn.this.tm != null && jn.this.tk.size() > 0) {
                            ArrayList arrayList = new ArrayList();
                            Iterator it = jn.this.tk.iterator();
                            while (it.hasNext()) {
                                a aVar = (a) it.next();
                                it.remove();
                                if (aVar != null) {
                                    arrayList.add(aVar);
                                }
                            }
                            jn.this.tm.j(jn.this.k(arrayList));
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void a(int i, int i2, boolean z, int i3, long j, String str, byte[] bArr, short s) {
        as asVar = new as();
        switch (i) {
            case 1:
                asVar.bR = i2;
                asVar.valueType = i;
                asVar.i = i3;
                break;
            case 2:
                asVar.bR = i2;
                asVar.valueType = i;
                asVar.bS = j;
                break;
            case 3:
                asVar.bR = i2;
                asVar.valueType = i;
                asVar.bT = str;
                break;
            case 4:
                asVar.bR = i2;
                asVar.valueType = i;
                asVar.bU = bArr;
                break;
            case 5:
                asVar.bR = i2;
                asVar.valueType = i;
                asVar.bV = z;
                break;
            case 6:
                asVar.bR = i2;
                asVar.valueType = i;
                asVar.bW = (short) s;
                break;
            default:
                return;
        }
        b(asVar);
        a aVar = new a();
        aVar.ta = asVar;
        f.d("KeyValueProfileService", "[profile上报][" + asVar.bR + "]");
        this.tk.add(aVar);
        this.handler.sendEmptyMessageDelayed(0, 1000);
    }

    public static void a(String str, as asVar, String str2) {
        if (asVar != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("keyid|" + asVar.bR);
            switch (asVar.valueType) {
                case 1:
                    stringBuilder.append("|int|" + asVar.i);
                    break;
                case 2:
                    stringBuilder.append("|long|" + asVar.bS);
                    break;
                case 3:
                    stringBuilder.append("|str|" + asVar.bT);
                    break;
                case 4:
                    stringBuilder.append("|byte[]|" + asVar.bU.length);
                    break;
                case 5:
                    stringBuilder.append("|bool|" + asVar.bV);
                    break;
                case 6:
                    stringBuilder.append("|short|" + asVar.bW);
                    break;
                default:
                    return;
            }
            if (str2 != null) {
                stringBuilder.append(str2);
            }
            gr.f(str, stringBuilder.toString());
        }
    }

    private void b(as asVar) {
        this.tn.put(Integer.valueOf(asVar.bR), asVar);
    }

    public static jn cx() {
        if (tl == null) {
            synchronized (lock) {
                if (tl == null) {
                    tl = new jn();
                }
            }
        }
        return tl;
    }

    private ArrayList<a> k(ArrayList<a> -l_2_R) {
        if (-l_2_R == null || -l_2_R.size() <= 1) {
            return -l_2_R;
        }
        ArrayList arrayList = new ArrayList();
        Iterator it = -l_2_R.iterator();
        while (it.hasNext()) {
            a aVar = (a) it.next();
            if (aVar != null) {
                arrayList.add(aVar);
            }
        }
        ArrayList arrayList2 = arrayList;
        -l_2_R.clear();
        Collections.sort(arrayList2, new Comparator<a>() {
            /* renamed from: a */
            public int compare(a aVar, a aVar2) {
                boolean z = aVar.ta != null ? aVar.ta instanceof as : false;
                boolean z2 = aVar2.ta != null ? aVar2.ta instanceof as : false;
                if (!z && !z2) {
                    return 0;
                }
                if (!z && z2) {
                    return -1;
                }
                if (z && !z2) {
                    return 1;
                }
                if (aVar.action != aVar2.action) {
                    return aVar.action - aVar2.action;
                }
                return ((as) aVar.ta).bR - ((as) aVar2.ta).bR;
            }
        });
        int size = arrayList2.size() - 1;
        for (int i = 0; i < size; i++) {
            a aVar2 = (a) arrayList2.get(i);
            a aVar3 = (a) arrayList2.get(i + 1);
            if (aVar2.action == aVar3.action) {
                as asVar = null;
                as asVar2 = null;
                if (aVar2.ta != null && (aVar2.ta instanceof as)) {
                    asVar = (as) aVar2.ta;
                }
                if (aVar3.ta != null && (aVar3.ta instanceof as)) {
                    asVar2 = (as) aVar3.ta;
                }
                if (asVar != null || asVar2 == null) {
                    if (asVar != null && asVar2 == null) {
                        -l_2_R.add(aVar2);
                    } else if (!((asVar == null && asVar2 == null) || asVar.bR == asVar2.bR)) {
                        -l_2_R.add(aVar2);
                    }
                }
            } else {
                -l_2_R.add(aVar2);
            }
        }
        if (size >= 0) {
            -l_2_R.add(arrayList2.get(size));
        }
        return -l_2_R;
    }

    public void a(int i, boolean z) {
        a(5, i, z, 0, 0, null, null, (short) 0);
    }

    public void a(gt gtVar) {
        gs.bc().a(CallerIdent.getIdent(1, 4294967296L), 4, new c() {
            public ArrayList<JceStruct> cu() {
                return jm.cw().getAll();
            }
        }, gtVar, jo.cy().cz());
    }

    public void a(b bVar) {
        this.tm = bVar;
    }

    public void a(jl jlVar) {
        this.oW.add(jlVar);
        gs.bc().a(jlVar);
    }

    public void ag(int i) {
    }

    public void b(jl jlVar) {
        this.oW.remove(jlVar);
        gs.bc().b(jlVar);
    }

    public int cs() {
        return 4;
    }

    public void ct() {
        ArrayList all = jm.cw().getAll();
        if (all != null && all.size() > 0) {
            Iterator it = all.iterator();
            while (it.hasNext()) {
                JceStruct jceStruct = (JceStruct) it.next();
                if (jceStruct instanceof as) {
                    as asVar = (as) jceStruct;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("key|");
                    stringBuilder.append(asVar.bR);
                    stringBuilder.append("|valueType|");
                    stringBuilder.append(asVar.valueType);
                    stringBuilder.append("|value|");
                    switch (asVar.valueType) {
                        case 1:
                            stringBuilder.append(asVar.i);
                            break;
                        case 2:
                            stringBuilder.append(asVar.bS);
                            break;
                        case 3:
                            stringBuilder.append(asVar.bT);
                            break;
                        case 4:
                            stringBuilder.append(Arrays.toString(asVar.bU));
                            break;
                    }
                    f.d("KeyValueProfileService", stringBuilder.toString());
                }
            }
        }
    }

    public boolean h(ArrayList<a> arrayList) {
        if (arrayList == null || arrayList.size() <= 0) {
            return false;
        }
        ArrayList arrayList2 = new ArrayList();
        boolean a = jm.cw().a(arrayList, arrayList2);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            a aVar = (a) it.next();
            if (!(aVar == null || aVar.ta == null || !(aVar.ta instanceof as))) {
                a("KeyValueProfileService", (as) aVar.ta, "|ret|" + a);
            }
        }
        if (a && arrayList2.size() > 0) {
            int i = 0;
            while (i < arrayList2.size()) {
                Boolean bool = (Boolean) arrayList2.get(i);
                if (bool != null && !bool.booleanValue() && arrayList.size() > i && (((a) arrayList.get(i)).ta instanceof as)) {
                    byte[] a2 = gr.a(4, 0, (as) ((a) arrayList.get(i)).ta);
                    if (a2 != null) {
                        jo.cy().aj(a2.length);
                        gs.bc().j(4, jo.cy().cz());
                    }
                }
                i++;
            }
        }
        return a;
    }

    public void i(ArrayList<a> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            ArrayList arrayList2 = null;
            ArrayList arrayList3 = null;
            ArrayList arrayList4 = null;
            Iterator it;
            a aVar;
            if (jo.cy().cA()) {
                jo.cy().j(false);
                it = arrayList.iterator();
                while (it.hasNext()) {
                    aVar = (a) it.next();
                    if (!(aVar == null || aVar.ta == null || !(aVar.ta instanceof as))) {
                        if (arrayList4 == null) {
                            arrayList4 = new ArrayList();
                        }
                        arrayList4.add((as) aVar.ta);
                    }
                }
            } else {
                it = arrayList.iterator();
                while (it.hasNext()) {
                    aVar = (a) it.next();
                    if (!(aVar == null || aVar.ta == null || !(aVar.ta instanceof as))) {
                        as asVar = (as) aVar.ta;
                        if (jm.cw().ai(asVar.bR) <= 0) {
                            if (arrayList3 == null) {
                                arrayList3 = new ArrayList();
                            }
                            arrayList3.add(asVar);
                        } else {
                            if (arrayList2 == null) {
                                arrayList2 = new ArrayList();
                            }
                            arrayList2.add(asVar);
                        }
                    }
                }
            }
            if (arrayList4 != null && arrayList4.size() > 0) {
                gs.bc().a(CallerIdent.getIdent(1, 4294967296L), 4, 0, arrayList4);
            }
            if (arrayList2 != null && arrayList2.size() > 0) {
                gs.bc().a(CallerIdent.getIdent(1, 4294967296L), 4, 3, arrayList2);
            }
            if (arrayList3 != null && arrayList3.size() > 0) {
                gs.bc().a(CallerIdent.getIdent(1, 4294967296L), 4, 1, arrayList3);
            }
        }
    }

    public void l(int i, int i2) {
        a(1, i, false, i2, 0, null, null, (short) 0);
    }

    public void onImsiChanged() {
        f.d("ImsiChecker", "KV-setFirstReport:[true]");
        jo.cy().j(true);
    }
}
