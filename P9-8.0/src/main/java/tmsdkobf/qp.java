package tmsdkobf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class qp implements qq {
    final boolean NZ;
    final boolean Oa;
    final ra Ob;
    public Set<String> Oc;
    public Map<String, qv> Od;
    List<qt> Oe = new ArrayList();
    qt Of;
    List<qt> Og;
    List<qt> Oh;

    public qp(ra raVar) {
        this.Ob = raVar;
        this.NZ = this.Ob.jS();
        this.Oa = this.Ob.jR();
    }

    private String a(qv qvVar, boolean z) {
        if (qvVar == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(!z ? "0;" : "1;");
        boolean z2 = false;
        if (qvVar.Ot != null) {
            for (qu quVar : qvVar.Ot) {
                if (this.Oa) {
                    if (z) {
                        if (quVar.Nt == 3) {
                            qu.a(stringBuilder, quVar, z, z2);
                            z2 = false;
                        }
                    } else if (quVar.Nt != 1) {
                        qu.a(stringBuilder, quVar, z, z2);
                        z2 = false;
                    }
                } else if (!(z || quVar.Nt == 1 || quVar.Nt == 2)) {
                    qu.a(stringBuilder, quVar, z, z2);
                    z2 = false;
                }
                z2 = true;
                qu.a(stringBuilder, quVar, z, z2);
                z2 = false;
            }
        }
        return stringBuilder.toString();
    }

    public void A(List<qt> list) {
        this.Oh = list;
    }

    public void B(List<qt> list) {
        this.Og = list;
    }

    public String a(qv -l_3_R, Map<String, ov> map) {
        if (-l_3_R == null) {
            return null;
        }
        boolean z = true;
        for (String containsKey : -l_3_R.Oz.keySet()) {
            if (map.containsKey(containsKey)) {
                z = false;
                break;
            }
        }
        if (-l_3_R.Ot == null) {
            -l_3_R.Ot = qo.jz().cV(-l_3_R.MB);
        }
        return a(-l_3_R, z);
    }

    public void a(qt qtVar) {
        this.Of = qtVar;
    }

    public Map<String, qv> cX(String str) {
        Map<String, qv> cW = qo.jz().cW(str);
        if (cW == null || cW.size() < 1) {
            return null;
        }
        for (Entry entry : cW.entrySet()) {
            qv qvVar = (qv) entry.getValue();
            if (qvVar != null && qvVar.Oz.containsKey(str)) {
                cW.put(entry.getKey(), qvVar);
            }
        }
        this.Od = cW;
        return cW;
    }

    public void cY(String str) {
        if (this.Oc == null) {
            this.Oc = new HashSet();
        }
        this.Oc.add(str);
    }

    public qv cZ(String str) {
        qv qvVar = (qv) this.Od.get(str);
        if (qvVar.Oz == null) {
            qvVar.Oz = qo.jz().j(str, this.NZ);
        }
        return qvVar;
    }

    public qt da(String str) {
        for (qt qtVar : this.Oe) {
            if (str.equals(qtVar.Oj)) {
                return qtVar;
            }
        }
        return null;
    }

    public boolean jD() {
        qu.jL();
        if (this.Od == null) {
            if (!new qs().a(this.NZ, this)) {
                return false;
            }
            this.Od = qo.jz().jA();
        }
        return true;
    }

    public String[] jE() {
        if (this.Oc == null) {
            return null;
        }
        return (String[]) this.Oc.toArray(new String[0]);
    }

    public String[] jF() {
        if (this.Od == null) {
            return null;
        }
        return (String[]) this.Od.keySet().toArray(new String[0]);
    }

    public List<qt> jG() {
        if (!this.Oa) {
            return this.Oe;
        }
        List<qt> arrayList = new ArrayList();
        for (qt qtVar : this.Oe) {
            if (!qtVar.Or) {
                arrayList.add(qtVar);
            }
        }
        return arrayList;
    }

    public List<qt> jH() {
        if (!this.Oa) {
            return this.Oh;
        }
        List<qt> arrayList = new ArrayList();
        for (qt qtVar : this.Oh) {
            if (!qtVar.Or) {
                arrayList.add(qtVar);
            }
        }
        return arrayList;
    }

    public List<qt> jI() {
        return this.Og;
    }

    public qt jJ() {
        return this.Of;
    }

    public void z(List<qt> list) {
        this.Oe = list;
    }
}
