package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdk.common.utils.h;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class os {
    public static final boolean EE = false;
    protected boolean EA;
    private final String EF;
    private ArrayList<String> EG;
    private ArrayList<String> EH;
    private ArrayList<String> EI;
    private volatile int EJ;
    private volatile long EK;
    private ArrayList<String> EL;
    private ArrayList<String> EM;
    private ArrayList<String> EN;
    private ArrayList<String> EO;
    private Object EP;
    private int EQ;
    private ArrayList<String> ER;
    private String ES;
    protected on Et;
    private final String TAG;
    private Context mContext;
    private volatile int mHash;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.os.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.os.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.os.<clinit>():void");
    }

    public os(Context context, boolean z, on onVar) {
        this.TAG = "IpPlot";
        this.EA = false;
        this.EF = TMSDKContext.getStrFromEnvMap(TMSDKContext.TCP_SERVER_ADDRESS);
        this.EG = new ArrayList();
        this.EH = new ArrayList();
        this.EI = new ArrayList();
        this.EL = new ArrayList();
        this.EM = new ArrayList();
        this.EN = new ArrayList();
        this.EO = new ArrayList();
        this.EP = new Object();
        this.EQ = 0;
        this.ER = new ArrayList();
        this.ES = null;
        d.d("IpPlot", "IpPlot() isTest: " + z);
        this.mContext = context;
        this.EA = z;
        this.Et = onVar;
        fW();
        fQ();
        fS();
    }

    private long Y() {
        long j;
        synchronized (this.EP) {
            j = this.EK;
        }
        return j;
    }

    private void a(String str, ArrayList<String> arrayList) {
        if (arrayList == null) {
            d.c("IpPlot", "printList() " + str + " is null");
        } else if (arrayList.size() > 0) {
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                if (!TextUtils.isEmpty((CharSequence) arrayList.get(i))) {
                    d.d("IpPlot", "printList() " + str + "[" + i + "]: " + ((String) arrayList.get(i)));
                }
            }
        } else {
            d.d("IpPlot", "printList() " + str + ".size <= 0");
        }
    }

    private void a(ArrayList<String> arrayList, ArrayList<String> arrayList2, ArrayList<String> arrayList3) {
        if (arrayList != null) {
            d.e("IpPlot", "loadList() cmvips");
            this.EL = y(arrayList);
        }
        if (arrayList2 != null) {
            d.e("IpPlot", "loadList() univips");
            this.EM = y(arrayList2);
        }
        if (arrayList3 != null) {
            d.e("IpPlot", "loadList() ctvips");
            this.EN = y(arrayList3);
        }
    }

    private void a(boolean z, ArrayList<String> arrayList) {
        if (arrayList != null) {
            switch (ge()) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    arrayList.addAll(!z ? this.EL : this.EG);
                    break;
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    arrayList.addAll(!z ? this.EM : this.EH);
                    break;
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    arrayList.addAll(!z ? this.EN : this.EI);
                    break;
            }
        }
    }

    private void b(boolean z, ArrayList<String> arrayList) {
        if (arrayList != null) {
            int ge = ge();
            if (ge != 0) {
                arrayList.addAll(!z ? this.EL : this.EG);
            }
            if (1 != ge) {
                arrayList.addAll(!z ? this.EM : this.EH);
            }
            if (2 != ge) {
                arrayList.addAll(!z ? this.EN : this.EI);
            }
        }
    }

    private String cN(String str) {
        while (str.startsWith(" ")) {
            str = str.substring(1, str.length()).trim();
        }
        while (str.endsWith(" ")) {
            str = str.substring(0, str.length() - 1).trim();
        }
        return str;
    }

    private boolean cO(String str) {
        String cN = cN(str);
        if (cN.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String[] split = cN.split("\\.");
            return split.length >= 4 && Integer.parseInt(split[0]) < 255 && Integer.parseInt(split[1]) < 255 && Integer.parseInt(split[2]) < 255 && Integer.parseInt(split[3]) < 255;
        }
    }

    private void fQ() {
        AtomicLong atomicLong = new AtomicLong();
        AtomicReference atomicReference = new AtomicReference();
        AtomicReference atomicReference2 = new AtomicReference();
        AtomicReference atomicReference3 = new AtomicReference();
        this.Et.a(atomicLong, atomicReference, atomicReference2, atomicReference3);
        synchronized (this.EP) {
            if ((atomicLong.get() <= 0 ? 1 : null) == null) {
                this.EK = atomicLong.get();
            }
            this.mHash = this.Et.ao();
            this.EJ = this.Et.ap();
            a((ArrayList) atomicReference.get(), (ArrayList) atomicReference2.get(), (ArrayList) atomicReference3.get());
        }
    }

    private boolean fR() {
        boolean z = true;
        long Y = Y();
        if (!(Y > 0)) {
            return false;
        }
        if (System.currentTimeMillis() <= Y) {
            z = false;
        }
        return z;
    }

    private void fT() {
        d.e("IpPlot", "printWrokingIpList()");
        synchronized (this.EP) {
            int size = this.ER.size();
            for (int i = 0; i < size; i++) {
                if (!TextUtils.isEmpty((CharSequence) this.ER.get(i))) {
                    d.d("IpPlot", "printWrokingIpList() mWorkingIpList[" + i + "]: " + ((String) this.ER.get(i)));
                }
            }
        }
    }

    private void fU() {
        if (this.EP != null) {
            synchronized (this.EP) {
                this.EQ = 0;
                this.ER.clear();
            }
            return;
        }
        this.EQ = 0;
    }

    private void fV() {
        synchronized (this.EP) {
            fU();
            a(true, this.ER);
            z(this.ER);
            b(true, this.ER);
            d.d("IpPlot", "resetToDefaultList()");
            fT();
            this.EK = 0;
            this.Et.a(0, new ArrayList(), new ArrayList(), new ArrayList());
        }
    }

    private void fW() {
        synchronized (this.EP) {
            if (this.EA) {
                this.EG.clear();
                this.EH.clear();
                this.EI.clear();
                this.EO.clear();
            } else {
                this.EG.clear();
                this.EH.clear();
                this.EI.clear();
                this.EO.clear();
            }
            this.EO.add(this.EF);
        }
    }

    private boolean ga() {
        boolean fR = fR();
        if (fR) {
            d.d("IpPlot", "checkIpListTimeOut() iplist timeout");
            fV();
        }
        return fR;
    }

    private String gb() {
        d.d("IpPlot", "getIpInIpList()");
        synchronized (this.ER) {
            int size = this.ER.size();
            String str;
            if (size > 0) {
                if (this.EQ >= size) {
                    this.EQ = 0;
                }
                str = "";
                try {
                    str = (String) this.ER.get(this.EQ);
                    d.e("IpPlot", "getIpInIpList() mCurIpIdx: " + this.EQ + " ip: " + str);
                    if (TextUtils.isEmpty(str)) {
                        str = getDomain();
                        return str;
                    } else if (cO(str) || str.equals(this.EF)) {
                        return str;
                    } else {
                        str = getDomain();
                        return str;
                    }
                } catch (Throwable th) {
                    d.c("IpPlot", th);
                    return getDomain();
                }
            }
            str = getDomain();
            return str;
        }
    }

    private int ge() {
        int J;
        Object obj = null;
        if (4 == ml.AX) {
            obj = 1;
        }
        if (obj == null) {
            J = h.J(this.mContext);
            if (-1 == J) {
                d.e("IpPlot", "getOper() unknowOper");
                J = 2;
            }
        } else {
            d.e("IpPlot", "getOper() usingWifi");
            J = 2;
        }
        d.e("IpPlot", "getOper() oper:" + J);
        return J;
    }

    private String getDomain() {
        return this.EF;
    }

    private final ArrayList<String> y(ArrayList<String> arrayList) {
        if (arrayList == null || arrayList.size() <= 0) {
            return new ArrayList();
        }
        for (int size = arrayList.size() - 1; size > 0; size--) {
            int random = (int) (((double) (size + 1)) * Math.random());
            String str = (String) arrayList.get(size);
            arrayList.set(size, arrayList.get(random));
            arrayList.set(random, str);
        }
        return arrayList;
    }

    private void z(ArrayList<String> arrayList) {
        if (arrayList != null) {
            arrayList.addAll(this.EO);
        }
    }

    public int W() {
        return this.mHash;
    }

    public int X() {
        return this.EJ;
    }

    public void a(int i, int i2, int i3, ArrayList<String> arrayList, ArrayList<String> arrayList2, ArrayList<String> arrayList3) {
        d.d("IpPlot", "handleNewIpList() hash: " + i + " hashSeqNo: " + i2 + " validperiod: " + i3);
        a("cmvips", (ArrayList) arrayList);
        a("unvips", (ArrayList) arrayList2);
        a("ctvips", (ArrayList) arrayList3);
        synchronized (this.EP) {
            u(i, i2);
            a(arrayList, arrayList2, arrayList3);
            this.EK = System.currentTimeMillis() + ((long) (i3 * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY));
            this.Et.a(this.EK, (ArrayList) arrayList, (ArrayList) arrayList2, (ArrayList) arrayList3);
        }
        fS();
    }

    public boolean ae() {
        return this.EA;
    }

    public void cM(String str) {
        synchronized (this.EP) {
            this.ES = str;
        }
    }

    protected void fS() {
        d.e("IpPlot", "refreshWorkingList()");
        synchronized (this.EP) {
            if (this.ER == null) {
            } else if (ga()) {
            } else {
                fU();
                a(false, this.ER);
                z(this.ER);
                b(false, this.ER);
                fT();
                if (this.ER.size() <= this.EO.size()) {
                    d.e("IpPlot", "refreshWorkingList() only domain");
                    fV();
                    d.d("IpPlot", "refreshWorkingList() resetToDefaultList()");
                    fT();
                }
                Iterator it = this.ER.iterator();
                while (it.hasNext()) {
                    String str = (String) it.next();
                    if (!(str == null || cO(str) || str.equals(this.EF))) {
                        it.remove();
                    }
                }
            }
        }
    }

    public void fX() {
        d.d("IpPlot", "handleNetworkChange()");
        fS();
    }

    public String fY() {
        return "http://" + fZ();
    }

    public String fZ() {
        if (EE) {
            ga();
            String gb = gb();
            d.d("IpPlot", "getIp() ip: " + gb);
            return gb;
        }
        gb = getDomain();
        d.d("IpPlot", "getIp() domain: " + gb);
        return gb;
    }

    public int gc() {
        if (this.ER == null) {
            return 0;
        }
        synchronized (this.ER) {
            if (this.ER == null) {
                return 0;
            }
            int size = this.ER.size();
            return size;
        }
    }

    public boolean gd() {
        boolean z = false;
        d.e("IpPlot", "gotoNextIp()");
        synchronized (this.ER) {
            this.EQ++;
            if (this.EQ >= this.ER.size()) {
                this.EQ = 0;
                z = true;
            }
            d.d("IpPlot", "gotoNextIp() size: " + this.ER.size() + " mCurIpIdx: " + this.EQ);
        }
        return z;
    }

    public void n(boolean z) {
        d.d("IpPlot", "setIsTest() isTest: " + z);
        this.EA = z;
        fW();
        fS();
    }

    public void u(int i, int i2) {
        synchronized (this.EP) {
            this.mHash = i;
            this.EJ = i2;
            this.Et.b(i, i2);
        }
    }
}
