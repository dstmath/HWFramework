package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import java.security.Key;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import javax.crypto.Cipher;

public class nq {
    private b DS;
    private nw Dm;
    private Object mLock = new Object();

    public interface a {
        void a(int i, int i2, int i3);
    }

    public static class b {
        public volatile String DW = "";
        public volatile String DX = "";

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mSessionId: ");
            stringBuilder.append(this.DW);
            stringBuilder.append(" mEncodeKey: ");
            stringBuilder.append(this.DX);
            return stringBuilder.toString();
        }
    }

    public nq(Context context, nw nwVar) {
        this.Dm = nwVar;
        this.DS = new b();
        load();
    }

    static void a(Context context, int i, b bVar) {
        try {
            Intent intent = new Intent(String.format("action.rsa.got:%s", new Object[]{context.getPackageName()}));
            intent.setPackage(context.getPackageName());
            intent.putExtra("k.rc", i);
            if (i == 0 && bVar != null) {
                intent.putExtra("k.r.k", bVar.DX);
                intent.putExtra("k.r.s", bVar.DW);
            }
            context.sendBroadcast(intent);
        } catch (Throwable th) {
            mb.b("RsaKeyCertifier", "[rsa_key]sendBroadcast(): " + th, th);
        }
    }

    private String bo(int i) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom secureRandom = new SecureRandom();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i2 = 0; i2 < i; i2++) {
            stringBuffer.append(str.charAt(secureRandom.nextInt(62)));
        }
        return stringBuffer.toString();
    }

    private byte[] cg(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        byte[] bArr = null;
        try {
            Key generatePublic = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(tmsdk.common.utils.b.decode("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDb49jFnNqMDLdl87UtY5jOMqqdMuvQg65Zuva3Qm1tORQGBuM04u7fqygA64XbOx9e/KPNkDNDmqS8SlsAPL1fV2lqM/phgV0NY62TJqSR+PLngwJd2rhYR8wQ1N0JE+R59a5c08EGsd6axStjHsVu2+evCf/SWU9Y/oQpEtOjGwIDAQAB", 0)));
            Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            instance.init(1, generatePublic);
            bArr = instance.doFinal(str.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bArr;
    }

    private void load() {
        b aE = this.Dm.gl().aE();
        if (aE == null) {
            mb.s("RsaKeyCertifier", "[rsa_key]load(), no record!");
            return;
        }
        synchronized (this.mLock) {
            this.DS.DX = aE.DX;
            this.DS.DW = aE.DW;
            mb.n("RsaKeyCertifier", "[rsa_key]load(), mEncodeKey: " + this.DS.DX + " mSessionId: " + this.DS.DW);
        }
    }

    private void x(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            mb.o("RsaKeyCertifier", "[rsa_key] saveRsaKey(), argument is null! encodeKey: " + str + " sessionId: " + str2);
            return;
        }
        synchronized (this.mLock) {
            this.DS.DX = str;
            this.DS.DW = str2;
            this.Dm.gl().b(this.DS);
        }
    }

    static void y(Context context) {
        try {
            mb.n("RsaKeyCertifier", "[rsa_key]requestSendProcessUpdateRsaKey()");
            Intent intent = new Intent(String.format("action.up.rsa:%s", new Object[]{context.getPackageName()}));
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent);
        } catch (Throwable th) {
            mb.b("RsaKeyCertifier", "[rsa_key]requestSendProcessUpdateRsaKey(): " + th, th);
        }
    }

    public void a(a aVar) {
        mb.d("RsaKeyCertifier", "[rsa_key]updateRsaKey()");
        final int fP = ns.fW().fP();
        String bo = bo(16);
        byte[] cg = cg(bo);
        if (cg != null) {
            JceStruct awVar = new aw();
            awVar.cf = cg;
            mb.n("RsaKeyCertifier", "[rsa_key]updateRsaKey() reqRSA.enc: " + com.tencent.tcuser.util.a.bytesToHexString(awVar.cf));
            ArrayList arrayList = new ArrayList();
            final bw bwVar = new bw();
            bwVar.ey = fP;
            bwVar.bz = 152;
            bwVar.eE |= 2;
            bwVar.data = nh.a(null, awVar, bwVar.bz, bwVar);
            arrayList.add(bwVar);
            nt.ga().a(bwVar.ey, -1, null);
            final a aVar2 = aVar;
            this.Dm.a(arrayList, new nr(bo) {
                public void a(boolean z, int i, int i2, ArrayList<ce> arrayList) {
                    mb.n("RsaKeyCertifier", "[rsa_key]updateRsaKey(), isTcpChannel: " + z + ", seqNo " + bwVar.ey + ", retCode: " + i);
                    if (i != 0) {
                        mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), retCode: " + i);
                        aVar2.a(fP, 152, i);
                    } else if (arrayList == null) {
                        mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), null == serverSashimis");
                        aVar2.a(fP, 152, -21250000);
                    } else if (arrayList.size() > 0) {
                        ce ceVar = (ce) arrayList.get(0);
                        if (ceVar == null) {
                            mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), serverSashimi is null");
                            aVar2.a(fP, 152, -21250000);
                        } else if (ceVar.eB != 0) {
                            mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), mazu error: " + ceVar.eB);
                            aVar2.a(fP, 152, ceVar.eB);
                        } else if (ceVar.eC != 0) {
                            mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), rs.dataRetCode: " + ceVar.eC);
                            aVar2.a(fP, 152, -21300000);
                        } else if (ceVar.data != null) {
                            try {
                                JceStruct a = nh.a(null, this.DY.getBytes(), ceVar.data, new ax(), false, ceVar.eE);
                                if (a != null) {
                                    ax axVar = (ax) a;
                                    if (TextUtils.isEmpty(axVar.K)) {
                                        mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), ret.sessionId is null");
                                        aVar2.a(fP, 152, -21280000);
                                        return;
                                    }
                                    nq.this.x(this.DY, axVar.K);
                                    mb.n("RsaKeyCertifier", "[rsa_key]updateRsaKey(), encodeKey: " + this.DY + " sessionId: " + axVar.K);
                                    aVar2.a(fP, 152, 0);
                                    return;
                                }
                                mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), decode jce failed: null == js");
                                aVar2.a(fP, 152, -21000400);
                            } catch (Exception e) {
                                mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), decode jce exception: " + e);
                                aVar2.a(fP, 152, -21000400);
                            }
                        } else {
                            mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), null == rs.data");
                            aVar2.a(fP, 152, -21000005);
                        }
                    } else {
                        mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), serverSashimis.size() <= 0");
                        aVar2.a(fP, 152, -21250000);
                    }
                }
            });
            return;
        }
        mb.o("RsaKeyCertifier", "[rsa_key]updateRsaKey(), gen dynamic key failed");
        aVar.a(fP, 152, -20001000);
    }

    public b ap() {
        b bVar = new b();
        synchronized (this.mLock) {
            bVar.DW = this.DS.DW;
            bVar.DX = this.DS.DX;
        }
        return bVar;
    }

    public void refresh() {
        mb.n("RsaKeyCertifier", "refresh()");
        load();
    }
}
