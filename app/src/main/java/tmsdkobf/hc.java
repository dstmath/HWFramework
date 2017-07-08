package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.i;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class hc {
    public static long qs;
    private a qr;

    /* compiled from: Unknown */
    public interface a {
        void a(int i, List<String> list, boolean z, long j, String str, String str2, String str3);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.hc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.hc.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.hc.<clinit>():void");
    }

    public hc() {
        init();
    }

    private void a(int i, String str, boolean z, long j, String str2, String str3, String str4) {
        if (this.qr != null) {
            List arrayList = new ArrayList(1);
            arrayList.add(str);
            this.qr.a(i, arrayList, z, j, str2, str3, str4);
        }
    }

    private void a(gx gxVar, String str, int i, long j) {
        boolean z = true;
        List arrayList;
        if (gxVar.pL) {
            if (this.qr == null) {
                return;
            }
            if (str != null) {
                if (gxVar.pE != 1) {
                    z = false;
                }
                a(4, str, z, j, gxVar.pG, gxVar.mPkg, gxVar.mName);
                return;
            }
            arrayList = new ArrayList(gxVar.pJ);
            if (gxVar.pE != 1) {
                z = false;
            }
            b(4, arrayList, z, j, gxVar.pG, gxVar.mPkg, gxVar.mName);
        } else if (this.qr != null) {
            if (str != null) {
                if (gxVar.pE != 1) {
                    z = false;
                }
                a(0, str, z, j, gxVar.pG, gxVar.mPkg, gxVar.mName);
                return;
            }
            arrayList = new ArrayList(gxVar.pJ);
            if (gxVar.pE != 1) {
                z = false;
            }
            b(0, arrayList, z, j, gxVar.pG, gxVar.mPkg, gxVar.mName);
        }
    }

    private static String aB(int i) {
        if (hb.bo()) {
            switch (i) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    return i.dh("eng_apk_not_installed");
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    return i.dh("eng_apk_installed");
                case UrlCheckType.MAKE_MONEY /*9*/:
                    return i.dh("eng_apk_old_version");
                case UrlCheckType.PRIVATE_SERVER /*11*/:
                    return i.dh("eng_apk_new_version");
                case UrlCheckType.MSG_REACTIONARY /*12*/:
                    return i.dh("eng_apk_repeated");
                default:
                    return Integer.toString(i);
            }
        }
        switch (i) {
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                return i.dh("cn_apk_not_installed");
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                return i.dh("cn_apk_installed");
            case UrlCheckType.MAKE_MONEY /*9*/:
                return i.dh("cn_apk_old_version");
            case UrlCheckType.PRIVATE_SERVER /*11*/:
                return i.dh("cn_apk_new_version");
            case UrlCheckType.MSG_REACTIONARY /*12*/:
                return i.dh("cn_apk_repeated");
            default:
                return Integer.toString(i);
        }
    }

    private void b(int i, List<String> list, boolean z, long j, String str, String str2, String str3) {
        if (this.qr != null) {
            this.qr.a(i, list, z, j, str, str2, str3);
        }
    }

    private void init() {
    }

    public void a(gm gmVar, String str, long j) {
        boolean z = false;
        gy gyVar = new gy();
        gyVar.mName = gmVar.mDescription;
        gyVar.pM = gmVar.pg;
        gyVar.pN = new ArrayList();
        gyVar.pN.add(str);
        gyVar.mTotalSize = j;
        if (gmVar.ph) {
            gyVar.pK = true;
            gyVar.pE = 0;
        } else {
            gyVar.pK = false;
            gyVar.pE = 1;
        }
        if (this.qr != null) {
            if (gyVar.pE == 1) {
                z = true;
            }
            a(1, str, z, gyVar.mTotalSize, null, null, gyVar.mName);
        }
    }

    public void a(gx gxVar, int i) {
        a(gxVar, null, i, gxVar.mTotalSize);
    }

    public void a(gx gxVar, int i, String str, String str2, long j) {
        String str3 = str2 != null ? str + str2 : str;
        if (str3 != null) {
            a(gxVar, str3, i, j);
        }
    }

    public void a(a aVar) {
        this.qr = aVar;
    }

    public void b(gt gtVar) {
        boolean z = true;
        if (!(gtVar == null || this.qr == null)) {
            String aZ = gtVar.aZ();
            if (gtVar.getStatus() != 1) {
                z = false;
            }
            a(2, aZ, z, gtVar.ba().getSize(), gtVar.ba().getAppName(), gtVar.ba().getPackageName(), aB(gtVar.al()));
        }
    }
}
