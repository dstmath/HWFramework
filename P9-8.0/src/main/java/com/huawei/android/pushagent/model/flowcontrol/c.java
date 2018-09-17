package com.huawei.android.pushagent.model.flowcontrol;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.a.i;
import com.huawei.android.pushagent.model.flowcontrol.a.a;
import com.huawei.android.pushagent.model.flowcontrol.a.b;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class c {
    private static c dz = null;
    private Context dy = null;
    private List<a> ea = new LinkedList();
    private List<a> eb = new LinkedList();
    private List<a> ec = new LinkedList();
    private List<a> ed = new LinkedList();
    private List<a> ee = new LinkedList();
    private List<a> ef = new LinkedList();

    private c(Context context) {
        this.dy = context;
        mv();
        if (this.ea.size() == 0 && this.eb.size() == 0 && this.ec.size() == 0 && this.ed.size() == 0 && this.ee.size() == 0 && this.ef.size() == 0) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "Connect Control is not set, begin to config it");
            mr();
        }
    }

    public static synchronized boolean mj(Context context) {
        synchronized (c.class) {
            dz = ms(context);
            if (dz == null) {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "cannot get ConnectControlMgr instance, may be system err!!");
                return false;
            }
            boolean mq = dz.mq();
            return mq;
        }
    }

    public static synchronized boolean mp(Context context, int i) {
        synchronized (c.class) {
            dz = ms(context);
            if (dz == null) {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "cannot get ConnectControlMgr instance, may be system err!!");
                return false;
            }
            boolean mo = dz.mo(i);
            return mo;
        }
    }

    public static synchronized c ms(Context context) {
        c cVar;
        synchronized (c.class) {
            if (dz == null) {
                dz = new c(context);
            }
            cVar = dz;
        }
        return cVar;
    }

    public static void mi(Context context) {
        dz = ms(context);
        if (dz != null && (dz.mu() ^ 1) != 0) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "TRS cfg change, need reload");
            dz.mr();
        }
    }

    private boolean mt(List<a> list, List<a> list2) {
        if (list == null && list2 == null) {
            return true;
        }
        if (list == null || list2 == null || list.size() != list2.size()) {
            return false;
        }
        for (a aVar : list) {
            boolean z;
            for (a lf : list2) {
                if (aVar.lf(lf)) {
                    z = true;
                    continue;
                    break;
                }
            }
            z = false;
            continue;
            if (!z) {
                return false;
            }
        }
        return true;
    }

    private boolean mu() {
        List linkedList = new LinkedList();
        linkedList.add(new b(86400000, g.aq(this.dy).cn()));
        linkedList.add(new b(3600000, g.aq(this.dy).co()));
        if (mt(linkedList, this.ea)) {
            linkedList = new LinkedList();
            linkedList.add(new b(86400000, g.aq(this.dy).cp()));
            if (mt(linkedList, this.eb)) {
                List linkedList2 = new LinkedList();
                for (Entry entry : g.aq(this.dy).cq().entrySet()) {
                    linkedList2.add(new b(((Long) entry.getKey()).longValue() * 1000, ((Long) entry.getValue()).longValue()));
                }
                if (mt(linkedList2, this.ec)) {
                    linkedList = new LinkedList();
                    linkedList.add(new b(86400000, g.aq(this.dy).cr()));
                    linkedList.add(new b(3600000, g.aq(this.dy).cs()));
                    if (mt(linkedList, this.ed)) {
                        linkedList = new LinkedList();
                        linkedList.add(new b(86400000, g.aq(this.dy).ct()));
                        if (mt(linkedList, this.ee)) {
                            linkedList2 = new LinkedList();
                            for (Entry entry2 : g.aq(this.dy).cu().entrySet()) {
                                linkedList2.add(new b(((Long) entry2.getKey()).longValue() * 1000, ((Long) entry2.getValue()).longValue()));
                            }
                            if (mt(linkedList2, this.ef)) {
                                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "cur control is equal trs cfg");
                                return true;
                            }
                            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "wifiVolumeControl cfg is change!!");
                            return false;
                        }
                        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "wifiTrsFlowControl cfg is change!!");
                        return false;
                    }
                    com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "wifiTrsFirstFlowControl cfg is change!");
                    return false;
                }
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "flowcControl cfg is change!!");
                return false;
            }
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "trsFlowControl cfg is change!!");
            return false;
        }
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "trsFirstFlowControl cfg is change!");
        return false;
    }

    private boolean mr() {
        this.ea.clear();
        this.ea.add(new b(86400000, g.aq(this.dy).cn()));
        this.ea.add(new b(3600000, g.aq(this.dy).co()));
        this.eb.clear();
        this.eb.add(new b(86400000, g.aq(this.dy).cp()));
        this.ec.clear();
        for (Entry entry : g.aq(this.dy).cq().entrySet()) {
            this.ec.add(new b(((Long) entry.getKey()).longValue() * 1000, ((Long) entry.getValue()).longValue()));
        }
        this.ed.clear();
        this.ed.add(new b(86400000, g.aq(this.dy).cr()));
        this.ed.add(new b(3600000, g.aq(this.dy).cs()));
        this.ee.clear();
        this.ee.add(new b(86400000, g.aq(this.dy).ct()));
        this.ef.clear();
        for (Entry entry2 : g.aq(this.dy).cu().entrySet()) {
            this.ef.add(new b(((Long) entry2.getKey()).longValue() * 1000, ((Long) entry2.getValue()).longValue()));
        }
        mx();
        return true;
    }

    private boolean mq() {
        if (1 == com.huawei.android.pushagent.utils.b.tm(this.dy)) {
            return mn(this.ed, this.ee);
        }
        return mn(this.ea, this.eb);
    }

    private boolean mo(int i) {
        if (1 == com.huawei.android.pushagent.utils.b.tm(this.dy)) {
            return mm(this.ef);
        }
        return mm(this.ec);
    }

    private boolean ml(List<a> list, long j) {
        if (list == null || list.size() == 0) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "there is no volome control");
            return true;
        }
        for (a lg : list) {
            if (!lg.lg(j)) {
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "push connect time exceed volum");
                return false;
            }
        }
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "check control flow pass");
        return true;
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean mk(List<a> list, long j) {
        if (list != null) {
            if (list.size() != 0) {
                for (a aVar : list) {
                    if (!aVar.lh(j)) {
                        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", " control info:" + aVar);
                        return false;
                    }
                }
                return true;
            }
        }
    }

    private boolean mx() {
        try {
            com.huawei.android.pushagent.utils.d.a aVar = new com.huawei.android.pushagent.utils.d.a(this.dy, "PushConnectControl");
            if (my(aVar, this.ed, "wifiTrsFirstFlowControlData") && my(aVar, this.ee, "wifiTrsFlowControlData") && my(aVar, this.ef, "wifiVolumeControlData") && my(aVar, this.ea, "trsFirstFlowControlData") && my(aVar, this.eb, "trsFlowControlData") && my(aVar, this.ec, "volumeControlData")) {
                return true;
            }
            return false;
        } catch (Throwable e) {
            com.huawei.android.pushagent.utils.d.c.se("PushLog2951", e.toString(), e);
            return false;
        }
    }

    private boolean mv() {
        try {
            com.huawei.android.pushagent.utils.d.a aVar = new com.huawei.android.pushagent.utils.d.a(this.dy, "PushConnectControl");
            mw(aVar, this.ea, "trsFirstFlowControlData");
            mw(aVar, this.eb, "trsFlowControlData");
            mw(aVar, this.ec, "volumeControlData");
            mw(aVar, this.ed, "wifiTrsFirstFlowControlData");
            mw(aVar, this.ee, "wifiTrsFlowControlData");
            mw(aVar, this.ef, "wifiVolumeControlData");
            return true;
        } catch (Throwable e) {
            com.huawei.android.pushagent.utils.d.c.se("PushLog2951", e.toString(), e);
            return false;
        }
    }

    private boolean mw(com.huawei.android.pushagent.utils.d.a aVar, List<a> list, String str) {
        int i = 0;
        String str2 = "\\|";
        list.clear();
        String rt = aVar.rt(str);
        if (TextUtils.isEmpty(rt)) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", str + " is not set");
        } else {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", str + "=" + rt);
            String[] split = rt.split(str2);
            if (split == null || split.length == 0) {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", str + " len 0, maybe system err");
                return false;
            }
            int length = split.length;
            while (i < length) {
                String str3 = split[i];
                b bVar = new b();
                if (bVar.lj(str3)) {
                    list.add(bVar);
                }
                i++;
            }
        }
        return true;
    }

    private boolean my(com.huawei.android.pushagent.utils.d.a aVar, List<a> list, String str) {
        String str2 = "|";
        StringBuffer stringBuffer = new StringBuffer();
        for (a li : list) {
            stringBuffer.append(li.li()).append(str2);
        }
        if (aVar.rv(str, stringBuffer.toString())) {
            return true;
        }
        com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "save " + str + " failed!!");
        return false;
    }

    private boolean mn(List<a> list, List<a> list2) {
        if (0 == i.ea(this.dy).ej()) {
            if (ml(list, 1)) {
                mk(list, 1);
            } else {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "trsFirstFlowControl not allowed to pass!!");
                return false;
            }
        } else if (ml(list2, 1)) {
            mk(list2, 1);
        } else {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "trsFlowControl not allowed to pass!!");
            return false;
        }
        mx();
        return true;
    }

    private boolean mm(List<a> list) {
        if (ml(list, 1)) {
            mk(list, 1);
            mx();
            return true;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "volumeControl not allow to pass!!");
        return false;
    }
}
