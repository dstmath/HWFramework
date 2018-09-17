package tmsdkobf;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class qj implements Comparable<qj> {
    static long Nm = 0;
    String Nd;
    String Ne;
    int Nf = 0;
    String Ng;
    long Nh = -1;
    long Ni = 0;
    long Nj = -1;
    long Nk = 0;
    int Nl;
    List<String> Nn = new LinkedList();
    long No = 0;
    String Np;
    List<String> Nq = new LinkedList();
    long Nr = 0;
    long Ns = 0;
    int Nt = 0;
    int Nu = 0;
    String Nv;
    public List<Integer> Nw;
    String mDescription;

    qj(String str, String str2) {
        this.mDescription = str;
        this.Nd = str2;
    }

    private static boolean C(String str, String str2) {
        return str.charAt(0) != '/' ? str2.endsWith(str) : qk.cT(str.substring(1)).matcher(str2).find();
    }

    private void a(File file, long j) {
        synchronized (this.Nn) {
            this.Nn.add(file.getAbsolutePath());
            this.No += j;
            Nm += j;
        }
    }

    private void b(File file, long j) {
        synchronized (this.Nq) {
            this.Nq.add(file.getAbsolutePath());
            this.Nr += j;
            Nm += j;
        }
    }

    /* JADX WARNING: Missing block: B:11:0x002a, code:
            if ((r12 <= r10.Ni) == false) goto L_0x002c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean c(File file, long j) {
        String toLowerCase = file.getName().toLowerCase(Locale.getDefault());
        if (this.Ng != null && !this.Ng.isEmpty() && !C(this.Ng, toLowerCase)) {
            return false;
        }
        if (this.Nh != -1) {
            if (!(j < this.Nh)) {
            }
            return false;
        }
        long j2 = Long.MAX_VALUE;
        if (!toLowerCase.equals(".nomedia")) {
            j2 = file.lastModified();
        }
        if (this.Nj != -1) {
            if (!(j2 < this.Nj)) {
                if (!(j2 <= this.Nk)) {
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    public boolean V(boolean z) {
        return !z ? 3 == this.Nt : 1 != this.Nt;
    }

    /* renamed from: a */
    public int compareTo(qj qjVar) {
        return qjVar.Nf - this.Nf;
    }

    public boolean h(File file) {
        long length = file.length();
        if (this.Nf == 0 || (this.Nl == 4 && c(file, length))) {
            a(file, length);
            return true;
        } else if (this.Nl != 3) {
            return false;
        } else {
            if ((TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toDays(file.lastModified()) > this.Ns ? 1 : null) == null) {
                a(file, length);
            } else {
                b(file, length);
            }
            return true;
        }
    }
}
