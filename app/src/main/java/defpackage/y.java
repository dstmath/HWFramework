package defpackage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.IPushMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatReqMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.bd.Reporter;
import java.net.Socket;
import java.util.Set;

/* renamed from: y */
public class y extends q {
    private boolean ar;
    private long as;
    private long at;
    private long au;
    private int av;
    private String aw;
    private String ax;
    private String ay;

    public y(Context context) {
        super(context);
        this.ar = false;
        this.as = 7200000;
        this.at = this.as;
        this.au = this.as;
        this.av = 0;
        this.aw = "";
        this.ax = "";
        this.ay = null;
    }

    private void a(k kVar, String str) {
        Object obj = null;
        if (!TextUtils.isEmpty(str)) {
            try {
                Object obj2;
                Set<String> keySet = kVar.ai().keySet();
                if (keySet != null && keySet.size() > 0) {
                    for (String str2 : keySet) {
                        if (str2.contains(str)) {
                            String str3 = (String) kVar.ai().get(str2);
                            aw.d("PushLog2828", "apnName is:" + str2 + ",apnHeartBeat is:" + str3);
                            String[] split = str3.split("_");
                            this.at = Long.parseLong(split[0]) * 1000;
                            this.au = Long.parseLong(split[1]) * 1000;
                            obj2 = 1;
                            break;
                        }
                    }
                }
                obj2 = null;
                obj = obj2;
            } catch (Throwable e) {
                aw.d("PushLog2828", e.toString(), e);
            }
        }
        if (obj == null) {
            this.at = kVar.v() * 1000;
            this.au = kVar.w() * 1000;
        }
        aw.d("PushLog2828", "after all, minHeartBeat is :" + this.at + ",maxHeartBeat is:" + this.au);
    }

    private String bm() {
        String str = "";
        try {
            if (ChannelMgr.aW() != null) {
                Socket socket = ChannelMgr.aW().getSocket();
                if (socket != null) {
                    str = socket.getLocalAddress().getHostAddress();
                }
            }
        } catch (Exception e) {
            aw.d("PushLog2828", e.toString());
        }
        return str == null ? "" : str;
    }

    private Long bn() {
        String a = ag.a(this.context, "cloudpush_fixHeatBeat", "");
        try {
            long parseLong = 1000 * Long.parseLong(a.trim());
            aw.d("PushLog2828", "get heart beat from config, value:" + parseLong + " so neednot ajust");
            return Long.valueOf(parseLong);
        } catch (NumberFormatException e) {
            if ((2 == this.batteryStatus && 5 != this.batteryStatus) || 1 != au.G(this.context)) {
                return null;
            }
            aw.d("PushLog2828", "in wifi and in charging, cannot ajust heartBeat");
            return Long.valueOf(60000);
        } catch (Throwable e2) {
            aw.d("PushLog2828", "get cloudpush_fixHeatBeat:" + a + " cause:" + e2.toString(), e2);
            if (2 == this.batteryStatus) {
            }
            aw.d("PushLog2828", "in wifi and in charging, cannot ajust heartBeat");
            return Long.valueOf(60000);
        }
    }

    private boolean bp() {
        int G = au.G(this.context);
        String F = au.F(this.context);
        String I = au.I(this.context);
        if (1 == G) {
            I = "wifi";
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("HasFindHeartBeat_" + F + "_" + G + "_" + I, Boolean.valueOf(this.ar));
        contentValues.put("HearBeatInterval_" + F + "_" + G + "_" + I, Long.valueOf(this.as));
        contentValues.put("ClientIP_" + F + "_" + G, this.ay);
        if (this.ar) {
            I = au.a(System.currentTimeMillis() + ae.l(this.context).ar(), "yyyy-MM-dd HH:mm:ss SSS");
            aw.d("PushLog2828", "when find best heart beat,save the valid end time " + I + " to xml.");
            contentValues.put("HeartBeatValid", I);
        }
        return new bt(this.context, bd()).a(contentValues);
    }

    public String bd() {
        return "PushHearBeat";
    }

    public /* synthetic */ q bg() {
        return bo();
    }

    public void bh() {
        try {
            long e = e(false);
            if (this.af.bS()) {
                aw.d("PushLog2828", "bastet started, do not need to check heartbeat timeout");
                e = this.af.cc();
            } else {
                bq.b(PushService.c().getContext(), new Intent("com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT").putExtra("timer_reason", "timeOutWaitPushSrvRsp").putExtra("connect_mode", ConnectEntityMode.M.ordinal()).setPackage(this.context.getPackageName()), ae.l(this.context).ad());
            }
            h(System.currentTimeMillis());
            IPushMessage newHeartBeatReqMessage = new NewHeartBeatReqMessage();
            newHeartBeatReqMessage.d((byte) ((int) Math.ceil((((double) e) * 1.0d) / 60000.0d)));
            ChannelMgr.aW().a(newHeartBeatReqMessage);
        } catch (Throwable e2) {
            aw.d("PushLog2828", "call pushChannel.send cause Exception:" + e2.toString(), e2);
        }
    }

    protected boolean bi() {
        int G = au.G(this.context);
        String F = au.F(this.context);
        switch (G) {
            case 0:
                return (G == this.av && F.equals(this.aw) && au.I(this.context).equals(this.ax)) ? false : true;
            case Reporter.ACTIVITY_CREATE /*1*/:
                return (G == this.av && F.equals(this.aw) && bm().equals(this.ay)) ? false : true;
            default:
                aw.d("PushLog2828", "isEnvChange:netType:" + G + false);
                return false;
        }
    }

    public y bo() {
        try {
            if (ChannelMgr.aW() == null) {
                aw.d("PushLog2828", "system is in start, wait net for heartBeat");
                return null;
            }
            String asString;
            this.ay = bm();
            ContentValues cn = new bt(this.context, bd()).cn();
            if (cn != null) {
                asString = cn.getAsString("HeartBeatValid");
                aw.d("PushLog2828", "hear beat valid from xml is " + asString);
                if (!TextUtils.isEmpty(asString) && (System.currentTimeMillis() >= au.n(asString) || System.currentTimeMillis() + ae.l(this.context).ar() < au.n(asString))) {
                    PushService.a(new Intent("com.huawei.android.push.intent.HEARTBEAT_VALID_ARRIVED"));
                }
            } else {
                aw.d("PushLog2828", "PushHearBeat preferences is null");
            }
            this.av = au.G(this.context);
            this.aw = au.F(this.context);
            k l = ae.l(this.context);
            this.at = l.v() * 1000;
            this.au = l.w() * 1000;
            this.ar = false;
            aw.d("PushLog2828", "in loadHeartBeat netType:" + this.av + " mccMnc:" + this.aw);
            ContentValues cn2 = new bt(this.context, bd()).cn();
            switch (this.av) {
                case -1:
                    this.as = l.C() * 1000;
                    return this;
                case 0:
                    this.ax = au.I(this.context);
                    aw.d("PushLog2828", "in loadHeartBeat apnName:" + this.ax);
                    a(l, this.ax);
                    break;
                case Reporter.ACTIVITY_CREATE /*1*/:
                    this.at = l.t() * 1000;
                    this.au = l.u() * 1000;
                    this.ax = "wifi";
                    this.as = this.at;
                    if (cn2 != null) {
                        asString = cn2.getAsString("ClientIP_" + this.aw + "_" + this.av);
                        if (this.ay == null || !this.ay.equals(asString)) {
                            aw.d("PushLog2828", "curIP:" + this.ay + " oldIP:" + asString + ", there are diff, so need find heartBeat again");
                            return this;
                        }
                    }
                    break;
                default:
                    aw.e("PushLog2828", "unKnow net type");
                    return this;
            }
            this.as = this.at;
            if (cn2 == null) {
                return this;
            }
            if (cn2.containsKey("HasFindHeartBeat_" + this.aw + "_" + this.av + "_" + this.ax) && cn2.containsKey("HearBeatInterval_" + this.aw + "_" + this.av + "_" + this.ax)) {
                this.ar = cn2.getAsBoolean("HasFindHeartBeat_" + this.aw + "_" + this.av + "_" + this.ax).booleanValue();
                Integer asInteger = cn2.getAsInteger("HearBeatInterval_" + this.aw + "_" + this.av + "_" + this.ax);
                int intValue = asInteger != null ? asInteger.intValue() : 0;
                if (((long) intValue) < 180000) {
                    return this;
                }
                this.as = (long) intValue;
                return this;
            }
            aw.d("PushLog2828", "have no this heartbeat config, use default");
            return this;
        } catch (Throwable e) {
            aw.d("PushLog2828", "call loadHeartBeat cause:" + e.toString(), e);
            return this;
        }
    }

    public long e(boolean z) {
        if (-1 == au.G(this.context)) {
            aw.i("PushLog2828", "no network, use no network heartbeat");
            return ae.l(this.context).C() * 1000;
        }
        Long bn = bn();
        if (bn != null) {
            return bn.longValue();
        }
        if (bi()) {
            bo();
        }
        long j = this.as;
        if (this.ar) {
            return j;
        }
        j = z ? this.as : this.as + 30000;
        return j <= this.at ? this.at : j >= this.au ? this.au : j;
    }

    public void f(boolean z) {
        aw.d("PushLog2828", "enter adjustHeartBeat:(findHeartBeat:" + this.ar + " RspTimeOut:" + z + " beatInterval:" + this.as + " range:[" + this.at + "," + this.au + "]," + "isHearBeatTimeReq:" + this.ae + " batteryStatus:" + this.batteryStatus + ")");
        if (bn() != null || this.ar) {
            return;
        }
        if (this.ae) {
            d(false);
            this.as = e(z);
            if (z || this.as <= this.at || this.as >= this.au) {
                this.ar = true;
                aw.i("PushLog2828", "after all the best heartBeat Interval:" + this.as + "ms");
            } else {
                aw.d("PushLog2828", "set current heartBeatInterval " + this.as + "ms");
            }
            bp();
            return;
        }
        aw.d("PushLog2828", "It is not hearBeatTimeReq");
    }

    public boolean i(long j) {
        return true;
    }

    public String toString() {
        String str = "=";
        String str2 = " ";
        return new StringBuffer().append("HasFindHeartBeat").append(str).append(this.ar).append(str2).append("HearBeatInterval").append(str).append(this.as).append(str2).append("minHeartBeat").append(str).append(this.at).append(str2).append("maxHeartBeat").append(str).append(this.au).toString();
    }
}
