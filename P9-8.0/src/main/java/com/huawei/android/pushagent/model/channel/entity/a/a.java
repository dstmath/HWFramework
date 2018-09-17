package com.huawei.android.pushagent.model.channel.entity.a;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.tcp.HeartBeatReqMessage;
import com.huawei.android.pushagent.datatype.tcp.base.IPushMessage;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.channel.entity.c;
import com.huawei.android.pushagent.utils.b;
import java.net.Socket;

public class a extends c {
    private String ab = "";
    private String ac = null;
    private String ad = "";
    private boolean ae = false;
    private long af = 7200000;
    private long ag = this.af;
    private long ah = this.af;
    private int ai = 0;

    public a(Context context) {
        super(context);
    }

    public String toString() {
        String str = "=";
        String str2 = " ";
        return new StringBuffer().append("HasFindHeartBeat").append(str).append(this.ae).append(str2).append("HearBeatInterval").append(str).append(this.af).append(str2).append("minHeartBeat").append(str).append(this.ah).append(str2).append("maxHeartBeat").append(str).append(this.ag).toString();
    }

    protected boolean fg() {
        boolean z = true;
        int tm = b.tm(this.bj);
        String tn = b.tn(this.bj);
        switch (tm) {
            case 0:
                String to = b.to(this.bj);
                if (tm == this.ai && (tn.equals(this.ad) ^ 1) == 0) {
                    z = to.equals(this.ab) ^ 1;
                }
                return z;
            case 1:
                if (tm == this.ai && (tn.equals(this.ad) ^ 1) == 0) {
                    z = fd().equals(this.ac) ^ 1;
                }
                return z;
            default:
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "isEnvChange:netType:" + tm + false);
                return false;
        }
    }

    private String fd() {
        String str = "";
        try {
            if (com.huawei.android.pushagent.model.channel.a.hk() != null) {
                Socket ga = com.huawei.android.pushagent.model.channel.a.hk().ga();
                if (ga != null) {
                    str = ga.getLocalAddress().getHostAddress();
                }
            }
        } catch (Exception e) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", e.toString());
        }
        if (str == null) {
            return "";
        }
        return str;
    }

    public long ff(boolean z) {
        if (-1 == b.tm(this.bj)) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "no network, use no network heartbeat");
            return g.aq(this.bj).au() * 1000;
        }
        if (fg()) {
            fh();
        }
        long j = this.af;
        if (!this.ae) {
            if (z) {
                j = this.af;
            } else {
                j = this.af + 30000;
            }
            if (j <= this.ah) {
                j = this.ah;
            } else if (j >= this.ag) {
                j = this.ag;
            }
        }
        return j;
    }

    public void fb(boolean z) {
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "enter adjustHeartBeat:(findHeartBeat:" + this.ae + " RspTimeOut:" + z + " beatInterval:" + this.af + " range:[" + this.ah + "," + this.ag + "]," + "isHearBeatTimeReq:" + this.bk + ")");
        if (this.ae) {
            if (z) {
                this.ae = false;
                this.af -= 60000;
                if (this.af < this.ah) {
                    this.af = this.ah;
                }
            }
        } else if (this.bk) {
            gn(false);
            this.af = ff(z);
            if (z || this.af <= this.ah || this.af >= this.ag) {
                this.ae = true;
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "after all the best heartBeat Interval:" + this.af + "ms");
            } else {
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "set current heartBeatInterval " + this.af + "ms");
            }
        } else {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "It is not hearBeatTimeReq");
            return;
        }
        fi();
    }

    public a fh() {
        try {
            if (com.huawei.android.pushagent.model.channel.a.hk() == null) {
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "system is in start, wait net for heartBeat");
                return null;
            }
            String asString;
            this.ac = fd();
            ContentValues read = new com.huawei.android.pushagent.utils.d.a(this.bj, fc()).read();
            if (read != null) {
                asString = read.getAsString("HeartBeatValid");
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "heart beat valid from xml is " + asString);
                if (!TextUtils.isEmpty(asString) && (System.currentTimeMillis() >= b.tp(asString) || System.currentTimeMillis() + g.aq(this.bj).av() < b.tp(asString))) {
                    PushService.yx(new Intent("com.huawei.android.push.intent.HEARTBEAT_VALID_ARRIVED"));
                }
            } else {
                com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "PushHearBeat preferences is null");
            }
            this.ai = b.tm(this.bj);
            this.ad = b.tn(this.bj);
            g aq = g.aq(this.bj);
            this.ah = aq.get3GMinHeartbeat() * 1000;
            this.ag = aq.get3GMaxHeartbeat() * 1000;
            this.ae = false;
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "in loadHeartBeat netType:" + this.ai + " mccMnc:" + this.ad);
            ContentValues read2 = new com.huawei.android.pushagent.utils.d.a(this.bj, fc()).read();
            switch (this.ai) {
                case -1:
                    this.af = aq.au() * 1000;
                    return this;
                case 0:
                    this.ab = b.to(this.bj);
                    com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "in loadHeartBeat apnName:" + this.ab);
                    fe(aq, this.ab);
                    break;
                case 1:
                    this.ah = aq.getWifiMinHeartbeat() * 1000;
                    this.ag = aq.getWifiMaxHeartbeat() * 1000;
                    this.ab = "wifi";
                    this.af = this.ah;
                    if (read2 != null) {
                        asString = read2.getAsString("ClientIP_" + this.ad + "_" + this.ai);
                        if (this.ac == null || (this.ac.equals(asString) ^ 1) != 0) {
                            return this;
                        }
                    }
                    break;
                default:
                    com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "unKnow net type");
                    return this;
            }
            this.af = this.ah;
            if (read2 != null) {
                if (read2.containsKey("HasFindHeartBeat_" + this.ad + "_" + this.ai + "_" + this.ab) && (read2.containsKey("HearBeatInterval_" + this.ad + "_" + this.ai + "_" + this.ab) ^ 1) == 0) {
                    int intValue;
                    this.ae = read2.getAsBoolean("HasFindHeartBeat_" + this.ad + "_" + this.ai + "_" + this.ab).booleanValue();
                    Integer asInteger = read2.getAsInteger("HearBeatInterval_" + this.ad + "_" + this.ai + "_" + this.ab);
                    if (asInteger != null) {
                        intValue = asInteger.intValue();
                    } else {
                        intValue = 0;
                    }
                    if (((long) intValue) >= 170000) {
                        this.af = (long) intValue;
                    }
                } else {
                    com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "have no this heartbeat config, use default");
                    return this;
                }
            }
            return this;
        } catch (Throwable e) {
            com.huawei.android.pushagent.utils.d.c.se("PushLog2951", "call loadHeartBeat cause:" + e.toString(), e);
            return this;
        }
    }

    private boolean fi() {
        int tm = b.tm(this.bj);
        String tn = b.tn(this.bj);
        String to = b.to(this.bj);
        if (1 == tm) {
            to = "wifi";
        }
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "prepare heartbeat values");
        ContentValues contentValues = new ContentValues();
        contentValues.put("HasFindHeartBeat_" + tn + "_" + tm + "_" + to, Boolean.valueOf(this.ae));
        contentValues.put("HearBeatInterval_" + tn + "_" + tm + "_" + to, Long.valueOf(this.af));
        contentValues.put("ClientIP_" + tn + "_" + tm, this.ac);
        if (this.ae) {
            to = b.tq(System.currentTimeMillis() + g.aq(this.bj).av(), "yyyy-MM-dd HH:mm:ss SSS");
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "when find best heart beat,save the valid end time " + to + " to xml.");
            contentValues.put("HeartBeatValid", to);
        }
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "store heartbeat values");
        return new com.huawei.android.pushagent.utils.d.a(this.bj, fc()).ru(contentValues);
    }

    public void fj() {
        try {
            long ff = ff(false);
            if (this.bl.qy()) {
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "bastet started, do not need to check heartbeat timeout");
                ff = this.bl.qz();
            } else {
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "set HEARTBEAT_RSP_TIMEOUT Alarm");
                com.huawei.android.pushagent.utils.tools.a.qf(PushService.yy().yz(), new Intent("com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT").setPackage(this.bj.getPackageName()), g.aq(this.bj).aw());
            }
            go(System.currentTimeMillis());
            IPushMessage heartBeatReqMessage = new HeartBeatReqMessage();
            heartBeatReqMessage.wh((byte) ((int) Math.ceil((((double) ff) * 1.0d) / 60000.0d)));
            com.huawei.android.pushagent.model.channel.a.hk().gb(heartBeatReqMessage);
        } catch (Throwable e) {
            com.huawei.android.pushagent.utils.d.c.se("PushLog2951", "call pushChannel.send cause Exception:" + e.toString(), e);
        }
    }

    private void fe(g gVar, String str) {
        Object obj = null;
        if (!TextUtils.isEmpty(str)) {
            try {
                Object obj2;
                Iterable<String> keySet = gVar.ax().keySet();
                if (keySet == null || keySet.size() <= 0) {
                    obj2 = null;
                } else {
                    for (String str2 : keySet) {
                        if (str2.contains(str)) {
                            String str3 = (String) gVar.ax().get(str2);
                            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "apnName is:" + str2 + ",apnHeartBeat is:" + str3);
                            String[] split = str3.split("_");
                            this.ah = Long.parseLong(split[0]) * 1000;
                            this.ag = Long.parseLong(split[1]) * 1000;
                            obj2 = 1;
                            break;
                        }
                    }
                    obj2 = null;
                }
                obj = obj2;
            } catch (Throwable e) {
                com.huawei.android.pushagent.utils.d.c.se("PushLog2951", e.toString(), e);
            }
        }
        if (obj == null) {
            this.ah = gVar.get3GMinHeartbeat() * 1000;
            this.ag = gVar.get3GMaxHeartbeat() * 1000;
        }
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "after all, minHeartBeat is :" + this.ah + ",maxHeartBeat is:" + this.ag);
    }

    public String fc() {
        return "PushHearBeat";
    }
}
