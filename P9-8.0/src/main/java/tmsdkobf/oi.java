package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import tmsdkobf.nw.d;

public class oi {
    private nw Dm;
    private String Im = "";
    private boolean In = false;

    public oi(Context context, nw nwVar, boolean z) {
        this.Dm = nwVar;
        this.Im = this.Dm.gl().aJ();
        mb.n("VidCertifier", "[cu_vid]VidCertifier(), mVidFromPhone: " + this.Im);
    }

    private oh<Long, Integer, JceStruct> a(long j, int i, bz bzVar) {
        mb.n("VidCertifier", "[cu_vid]handleSCPushUpdatedVid(), pushId: " + j + " serverShasimiSeqNo: " + i);
        if (bzVar != null) {
            if (bzVar.eM == 0) {
                c(1, true);
            } else if (bzVar.eM == 1) {
                c(1, false);
            }
            return null;
        }
        mb.s("VidCertifier", "[cu_vid]handlePushCheckVid(), scPushUpdatedVid == null");
        return null;
    }

    private bu d(int i, boolean z) {
        bu buVar = null;
        String aJ = this.Dm.gl().aJ();
        Object aK = this.Dm.gl().aK();
        if (aJ == null) {
            aJ = "";
        }
        if (aK == null) {
            aK = "";
        }
        mb.n("VidCertifier", "[cu_vid]getCSUpdateVidIfNeed(), updateReason: " + i + " myVid: " + aJ + " commonVid: " + aK);
        if (z) {
            buVar = new bu();
            buVar.ev = i;
            buVar.ew = aJ;
            buVar.ep = aK;
        } else if (fA()) {
            mb.n("VidCertifier", "[cu_vid]getCSUpdateVidIfNeed(), should register, donot update");
            return null;
        } else if (TextUtils.isEmpty(aJ) || TextUtils.isEmpty(aK) || aJ.equals(aK)) {
            mb.n("VidCertifier", "[cu_vid]getCSUpdateVidIfNeed(), not diff, donnot update");
        } else {
            buVar = new bu();
            buVar.ev = i;
            buVar.ew = aJ;
            buVar.ep = aK;
        }
        return buVar;
    }

    private boolean fA() {
        return nu.aB() ? TextUtils.isEmpty(this.Im) : false;
    }

    private bs hb() {
        bs bsVar = new bs();
        String aK = this.Dm.gl().aK();
        if (aK == null) {
            aK = "";
        }
        bsVar.ep = aK;
        mb.n("VidCertifier", "[cu_vid]getCSRegistVid(), req.commonVid: " + bsVar.ep);
        return bsVar;
    }

    public void c(int i, boolean z) {
        mb.n("VidCertifier", "[cu_vid]updateVidIfNeed(), updateReason: " + i + " force: " + z);
        if (this.Dm.gl().aH()) {
            JceStruct d = d(i, z);
            if (d != null) {
                nu.gf().a(5007, d, new cc(), 0, new jy() {
                    public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                        if (i3 == 0 && i4 == 0 && jceStruct != null) {
                            String str = ((cc) jceStruct).eN;
                            if (TextUtils.isEmpty(str)) {
                                mb.s("VidCertifier", "[cu_vid]updateVidIfNeed()-onFinish(), seqNo: " + i + ", vid is empty: " + str);
                                return;
                            }
                            mb.n("VidCertifier", "[cu_vid]updateVidIfNeed()-onFinish(), succ, vid: " + str);
                            oi.this.Im = str;
                            oi.this.Dm.gl().c(str, false);
                            oi.this.Dm.gl().d(str, false);
                            return;
                        }
                        mb.s("VidCertifier", "[cu_vid]updateVidIfNeed()-onFinish(), seqNo: " + i + " retCode: " + i3 + " dataRetCode: " + i4 + " resp: " + jceStruct);
                    }
                }, 30000);
                return;
            }
            return;
        }
        mb.s("VidCertifier", "[cu_vid]updateVidIfNeed(), not support vid, do nothing");
    }

    public void c(d dVar) {
        d dVar2 = dVar;
        dVar2.a(0, 15020, new bz(), 0, new ka() {
            public oh<Long, Integer, JceStruct> a(int i, long j, int i2, JceStruct jceStruct) {
                if (jceStruct != null) {
                    switch (i2) {
                        case 15020:
                            return oi.this.a(j, i, (bz) jceStruct);
                        default:
                            return null;
                    }
                }
                mb.o("VidCertifier", "onRecvPush() null == push");
                return null;
            }
        }, false);
        mb.n("VidCertifier", "[cu_vid]registerSharkPush Cmd_SCPushUpdatedVid, cmdId=15020");
    }

    public void gB() {
        mb.n("VidCertifier", "[cu_vid]registerVidIfNeed()");
        if (!this.Dm.gl().aH()) {
            mb.s("VidCertifier", "[cu_vid]registerVidIfNeed(), not support vid, do nothing");
        } else if (this.In) {
            mb.n("VidCertifier", "[cu_vid]registerVidIfNeed(), registering, ignore");
        } else if (fA()) {
            this.Dm.gl().aI();
            this.In = true;
            nu.gf().a(5006, hb(), new cb(), 0, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    if (i3 == 0 && i4 == 0 && jceStruct != null) {
                        String str = ((cb) jceStruct).eN;
                        if (TextUtils.isEmpty(str)) {
                            mb.s("VidCertifier", "[cu_vid]registerVidIfNeed()-onFinish(), seqNo: " + i + ", vid is empty: " + str);
                        } else {
                            mb.n("VidCertifier", "[cu_vid]registerVidIfNeed()-onFinish(), succ, vid: " + str);
                            oi.this.Im = str;
                            oi.this.Dm.gl().c(str, true);
                            oi.this.Dm.gl().d(str, true);
                        }
                    } else {
                        mb.s("VidCertifier", "[cu_vid]registerVidIfNeed()-onFinish(), seqNo: " + i + " retCode: " + i3 + " dataRetCode: " + i4 + " resp: " + jceStruct);
                    }
                    oi.this.In = false;
                }
            }, 30000);
        } else {
            mb.n("VidCertifier", "[cu_vid]registerVidIfNeed(), not necessary, mVidFromPhone: " + this.Im);
        }
    }
}
