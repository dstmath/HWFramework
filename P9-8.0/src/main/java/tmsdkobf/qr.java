package tmsdkobf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import tmsdk.common.TMSDKContext;

public class qr implements qq {
    final boolean NZ;
    final boolean Oa;
    final ra Ob;
    public Set<String> Oc;
    public Map<String, qv> Od;
    List<qt> Oe = new ArrayList();
    qt Of;
    List<qt> Og;
    List<qt> Oh;

    public qr(ra raVar) {
        this.Ob = raVar;
        this.NZ = this.Ob.jS();
        this.Oa = this.Ob.jR();
    }

    private String a(qv qvVar, boolean z, boolean z2) {
        if (qvVar == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(!z ? "0;" : "1;");
        boolean z3 = false;
        if (qvVar.Ot != null) {
            for (qu quVar : qvVar.Ot) {
                if (z2) {
                    if (z) {
                        if (quVar.Nt == 3) {
                            qu.a(stringBuilder, quVar, z, z3);
                            z3 = false;
                        }
                    } else if (quVar.Nt != 1) {
                        qu.a(stringBuilder, quVar, z, z3);
                        z3 = false;
                    }
                } else if (!(z || quVar.Nt == 1 || quVar.Nt == 2)) {
                    qu.a(stringBuilder, quVar, z, z3);
                    z3 = false;
                }
                z3 = true;
                qu.a(stringBuilder, quVar, z, z3);
                z3 = false;
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
        boolean z = true;
        for (String containsKey : -l_3_R.Oz.keySet()) {
            if (map.containsKey(containsKey)) {
                z = false;
                break;
            }
        }
        return a(-l_3_R, z, this.Oa);
    }

    public void a(qt qtVar) {
        this.Of = qtVar;
    }

    public Map<String, qv> cX(String str) {
        jD();
        if (this.Od == null || this.Od.size() == 0) {
            return null;
        }
        Map<String, qv> hashMap = new HashMap();
        for (Entry entry : this.Od.entrySet()) {
            qv qvVar = (qv) entry.getValue();
            if (qvVar != null && qvVar.Oz.containsKey(str)) {
                hashMap.put(entry.getKey(), qvVar);
            }
        }
        return hashMap;
    }

    public void cY(String str) {
        if (this.Oc == null) {
            this.Oc = new HashSet();
        }
        this.Oc.add(str);
    }

    public qv cZ(String str) {
        return (qv) this.Od.get(str);
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
        if (this.Od == null) {
            qs qsVar = new qs();
            if (!qsVar.a(this.NZ, this) || !qsVar.T(TMSDKContext.getApplicaionContext())) {
                return false;
            }
            this.Od = qsVar.Od;
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
