package com.huawei.android.pushagent.model.c;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.http.server.TokenApplyRsp;
import com.huawei.android.pushagent.datatype.tcp.DecoupledPushMessage;
import com.huawei.android.pushagent.datatype.tcp.DeviceRegisterReqMessage;
import com.huawei.android.pushagent.datatype.tcp.DeviceRegisterRspMessage;
import com.huawei.android.pushagent.datatype.tcp.PushDataReqMessage;
import com.huawei.android.pushagent.datatype.tcp.PushDataRspMessage;
import com.huawei.android.pushagent.datatype.tcp.base.IPushMessage;
import com.huawei.android.pushagent.datatype.tcp.base.PushMessage;
import com.huawei.android.pushagent.model.a.h;
import com.huawei.android.pushagent.model.a.i;
import com.huawei.android.pushagent.model.a.j;
import com.huawei.android.pushagent.model.b.e;
import com.huawei.android.pushagent.model.d.a;
import com.huawei.android.pushagent.model.d.d;
import com.huawei.android.pushagent.model.token.TokenApply;
import com.huawei.android.pushagent.utils.d.c;
import com.huawei.android.pushagent.utils.f;
import org.json.JSONObject;

public class b implements e {
    public b(Context context) {
        a.jp().jq();
    }

    public void onReceive(Context context, Intent intent) {
        c.sg("PushLog2951", "enter CommandReceiver:onReceive, intent is:" + intent);
        String action = intent.getAction();
        if ("com.huawei.android.push.intent.CONNECTED".equals(action)) {
            iy(context, intent);
        } else if ("com.huawei.android.push.intent.MSG_RECEIVED".equals(action)) {
            jj(context, intent);
        } else if ("com.huawei.action.push.intent.REPORT_EVENTS".equals(action)) {
            jl(intent);
        } else if ("com.huawei.android.push.intent.REGISTER".equals(action) || "com.huawei.android.push.intent.REGISTER_SPECIAL".equals(action)) {
            jg(context, intent);
        } else if ("com.huawei.android.push.intent.ACTION_TERMINAL_PROTOCAL".equals(action)) {
            jc(context, intent);
        } else if ("com.huawei.android.push.intent.DEREGISTER".equals(action)) {
            jh(context, intent);
        } else if ("com.huawei.intent.action.SELF_SHOW_FLAG".equals(action)) {
            jd(context, intent);
        } else if ("com.huawei.android.push.intent.MSG_RESPONSE".equals(action)) {
            it(context, intent);
        } else if ("com.huawei.android.push.intent.MSG_RSP_TIMEOUT".equals(action)) {
            iv(context);
        } else if ("com.huawei.android.push.intent.RESET_BASTET".equals(action)) {
            ix(context, intent);
        } else if ("com.huawei.android.push.intent.RESPONSE_FAIL".equals(action)) {
            jb(context, intent);
        } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
            iw(context, intent);
        } else if ("android.ctrlsocket.all.allowed".equals(action)) {
            iz(context, intent);
        } else if ("android.scroff.ctrlsocket.status".equals(action)) {
            ja(context, intent);
        } else if ("com.huawei.action.push.intent.CHECK_CHANNEL_CYCLE".equals(action)) {
            is(context);
            com.huawei.android.pushagent.utils.tools.a.qi(context, new Intent("com.huawei.action.push.intent.CHECK_CHANNEL_CYCLE").setPackage(context.getPackageName()), 1200000);
        } else if ("com.huawei.systemmanager.changedata".equals(action)) {
            iu(context, intent);
        }
    }

    private void iu(Context context, Intent intent) {
        c.sh("PushLog2951", "receive network policy change event from system manager.");
        if (context == null || intent == null) {
            c.sf("PushLog2951", "intent is null");
            return;
        }
        String stringExtra = intent.getStringExtra("packagename");
        if ("com.huawei.android.pushagent".equals(stringExtra)) {
            int intExtra = intent.getIntExtra("switch", 1);
            c.sh("PushLog2951", "network policy change, pkg:" + stringExtra + ", flag:" + intExtra);
            i.ea(context).ek(intExtra);
            if (intExtra == 0) {
                Intent intent2 = new Intent("com.huawei.intent.action.PUSH_OFF");
                intent2.setPackage(context.getPackageName());
                intent2.putExtra("Remote_Package_Name", context.getPackageName());
                PushService.yx(intent2);
            } else {
                is(context);
            }
        }
    }

    private void jl(Intent intent) {
        if (intent == null) {
            c.sf("PushLog2951", "sendEventsToServer intent is null");
            return;
        }
        CharSequence stringExtra = intent.getStringExtra("events");
        if (TextUtils.isEmpty(stringExtra)) {
            c.sf("PushLog2951", "sendEventsToServer events is null");
            return;
        }
        IPushMessage decoupledPushMessage = new DecoupledPushMessage((byte) 66);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("cmdid", -10);
            jSONObject.put("v", 2951);
            jSONObject.put("e", stringExtra);
        } catch (Throwable e) {
            c.se("PushLog2951", "create DecoupledPushMessage params error:" + e.toString(), e);
        }
        decoupledPushMessage.wj(jSONObject);
        try {
            c.sg("PushLog2951", "reportEvent start to send report events");
            com.huawei.android.pushagent.model.channel.a.hk().gb(decoupledPushMessage);
        } catch (Exception e2) {
            c.sf("PushLog2951", "send events to push server failed");
        }
    }

    private void iy(Context context, Intent intent) {
        if (context == null || intent == null) {
            c.sf("PushLog2951", "intent is null");
            return;
        }
        String ua = com.huawei.android.pushagent.utils.b.ua(context);
        if (ua == null) {
            c.sf("PushLog2951", "cannot get imei when receviced ACTION_CONNECTED");
            return;
        }
        try {
            com.huawei.android.pushagent.model.channel.a.hk().gb(je(context, ua));
        } catch (Throwable e) {
            c.se("PushLog2951", "call ChannelMgr.getPushChannel().send cause:" + e.toString(), e);
        }
    }

    private void iz(Context context, Intent intent) {
        if (context == null || intent == null) {
            c.sf("PushLog2951", "intent is null");
        } else {
            a.jp().jt(context);
        }
    }

    private void ja(Context context, Intent intent) {
        if (context == null || intent == null) {
            c.sf("PushLog2951", "intent is null");
        } else {
            a.jp().js(intent);
        }
    }

    private void jb(Context context, Intent intent) {
        if (context == null || intent == null) {
            c.sf("PushLog2951", "intent is null");
            return;
        }
        c.sh("PushLog2951", "srv response fail, close channel and set alarm to reconnect!");
        try {
            com.huawei.android.pushagent.a.a.xv(84);
            com.huawei.android.pushagent.model.channel.a.hl(context).hn();
            PushService.yx(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(context.getPackageName()));
        } catch (Throwable e) {
            c.se("PushLog2951", "call channel.close cause exception:" + e.toString(), e);
        }
    }

    private void ix(Context context, Intent intent) {
        if (context == null || intent == null) {
            c.sf("PushLog2951", "intent is null");
            return;
        }
        c.sh("PushLog2951", "reset bastet alarm reach, and reconnect pushserver");
        com.huawei.android.pushagent.utils.bastet.a.ra(context).rc();
        PushService.yx(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(context.getPackageName()));
    }

    private void jj(Context context, Intent intent) {
        PushMessage pushMessage = (PushMessage) intent.getSerializableExtra("push_msg");
        if (pushMessage == null) {
            c.sh("PushLog2951", "msg is null");
            return;
        }
        switch (pushMessage.vt()) {
            case (byte) 65:
                com.huawei.android.pushagent.a.a.xv(74);
                DeviceRegisterRspMessage deviceRegisterRspMessage = (DeviceRegisterRspMessage) pushMessage;
                if (deviceRegisterRspMessage.getResult() != (byte) 0) {
                    c.sf("PushLog2951", "CommandReceiver device register fail:" + deviceRegisterRspMessage.getResult());
                    com.huawei.android.pushagent.model.channel.a.hl(context).hn();
                    break;
                }
                c.sh("PushLog2951", "CommandReceiver device register success");
                com.huawei.android.pushagent.model.channel.a.hp(context).gp(context);
                TokenApply.execute(context);
                com.huawei.android.pushagent.model.token.a.execute(context);
                break;
            case (byte) 67:
                jf(context, (DecoupledPushMessage) pushMessage);
                break;
            case (byte) 68:
                jk(context, (PushDataReqMessage) pushMessage);
                com.huawei.android.pushagent.utils.b.us(context, 100);
                break;
        }
    }

    private void jf(Context context, DecoupledPushMessage decoupledPushMessage) {
        boolean z = true;
        if (decoupledPushMessage == null) {
            c.sf("PushLog2951", "decoupledPushMessage is null");
            return;
        }
        try {
            JSONObject wk = decoupledPushMessage.wk();
            c.sh("PushLog2951", "parse decoupled msg.");
            if (wk == null || !wk.has("cmdid")) {
                c.sg("PushLog2951", "unknown DecoupledPushMessage");
                return;
            }
            int i = wk.getInt("cmdid");
            if (247 == i) {
                int optInt = wk.optInt("result", 1);
                c.sg("PushLog2951", "report result is:" + optInt);
                if (optInt != 0) {
                    z = false;
                }
                com.huawei.android.pushagent.a.a.xy(z);
                com.huawei.android.pushagent.model.d.b.jx().jy();
            } else if (249 == i) {
                TokenApplyRsp tokenApplyRsp = (TokenApplyRsp) com.huawei.android.pushagent.utils.b.b.oy(wk.toString(), TokenApplyRsp.class, new Class[0]);
                if (tokenApplyRsp == null) {
                    c.sf("PushLog2951", "parse decoupledPushMessage failed.");
                } else {
                    new TokenApply(context).responseToken(tokenApplyRsp);
                }
            } else {
                c.sj("PushLog2951", "decoupledPushMessage cmdid is not right");
            }
        } catch (Throwable e) {
            c.se("PushLog2951", "parseDecoupledPushTokenMsg error:" + e.getMessage(), e);
        }
    }

    private void jk(Context context, PushDataReqMessage pushDataReqMessage) {
        c.sg("PushLog2951", "enter rspPushMessage");
        if (pushDataReqMessage.isValid()) {
            byte[] wa = pushDataReqMessage.wa();
            String sb = com.huawei.android.pushagent.utils.d.b.sb(wa);
            PushDataRspMessage pushDataRspMessage = new PushDataRspMessage(wa, (byte) 0, pushDataReqMessage.wb(), pushDataReqMessage.wc(), pushDataReqMessage.vy());
            c.sh("PushLog2951", "Device type is :" + h.dp(context).dq() + " [1:NOT_GDPR, 2:GDPR]");
            if (d.kn().ko(sb)) {
                c.sh("PushLog2951", "msgId has cached, do not sent again");
            } else {
                d.kn().km(sb);
                c.sh("PushLog2951", "msgType: " + pushDataReqMessage.we() + " [0:PassBy msg, 1:System notification, 2:normal notification]");
                e hx = com.huawei.android.pushagent.model.b.a.hx(context, pushDataReqMessage.we(), pushDataReqMessage);
                if (hx == null) {
                    c.sf("PushLog2951", "invalid msgType: " + pushDataReqMessage.we());
                    pushDataRspMessage.wl((byte) 20);
                } else {
                    pushDataRspMessage.wl(hx.in());
                }
            }
            ji(pushDataRspMessage);
            return;
        }
        c.sf("PushLog2951", "reqMsg is inValid");
    }

    private void jg(Context context, Intent intent) {
        com.huawei.android.pushagent.utils.tools.a.qh(context, new Intent("com.huawei.intent.action.PUSH_OFF").setPackage(context.getPackageName()).putExtra("Remote_Package_Name", context.getPackageName()));
        String stringExtra = intent.getStringExtra("pkg_name");
        String stringExtra2 = intent.getStringExtra("userid");
        c.sh("PushLog2951", "CommandReceiver: get the packageName: " + stringExtra + "; userid is " + stringExtra2);
        if (TextUtils.isEmpty(stringExtra)) {
            c.sf("PushLog2951", "CommandReceiver: get the wrong package name from the Client!");
        } else {
            int ve = f.ve(stringExtra2);
            String vd = f.vd(ve);
            boolean ab = com.huawei.android.pushagent.model.a.d.aa(context).ab(stringExtra);
            c.sh("PushLog2951", "responseClientRegistration disagreeFlag:" + ab);
            if (ab) {
                com.huawei.android.pushagent.model.a.d.aa(context).ac(stringExtra, false);
            }
            if (com.huawei.android.pushagent.utils.b.um(context, stringExtra, ve)) {
                stringExtra2 = com.huawei.android.pushagent.utils.b.ue(stringExtra, vd);
                com.huawei.android.pushagent.model.a.f.ak(context).al(stringExtra2, true);
                Object b = com.huawei.android.pushagent.model.a.a.c(context).b(stringExtra2);
                if (!TextUtils.isEmpty(b)) {
                    com.huawei.android.pushagent.model.a.a.c(context).g(com.huawei.android.pushagent.utils.a.e.nu(b));
                }
                is(context);
                if (com.huawei.android.pushagent.model.d.f.kx(context, stringExtra, vd)) {
                    c.sg("PushLog2951", "CommandReceiver: this package:" + stringExtra + " have already registered ");
                    com.huawei.android.pushagent.model.d.b.jx().jw(stringExtra);
                    com.huawei.android.pushagent.utils.b.up(context, stringExtra, vd, com.huawei.android.pushagent.model.a.b.l(context).n(stringExtra2));
                } else {
                    jm(context, stringExtra, vd);
                }
            } else {
                com.huawei.android.pushagent.a.a.xx(60, stringExtra);
                c.sf("PushLog2951", "rec register toke request , but the packageName:" + stringExtra + " was not install !!");
            }
        }
    }

    private void is(Context context) {
        if (com.huawei.android.pushagent.model.channel.a.hk().gc()) {
            c.sh("PushLog2951", "check push connection is exist, sendHearBeat.");
            com.huawei.android.pushagent.model.channel.a.hp(context).fj();
            return;
        }
        c.sh("PushLog2951", "check push connection is not exist, reconnect it.");
        PushService.yx(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(context.getPackageName()));
    }

    private void jm(Context context, String str, String str2) {
        c.sh("PushLog2951", "begin to get token from pushSrv, pkgName is: " + str + ", userId is " + str2);
        String ue = com.huawei.android.pushagent.utils.b.ue(str, str2);
        jn(context, ue);
        c.sh("PushLog2951", "begin to get token from server, userid is: " + str2);
        j.ev(context).ey(ue);
        TokenApply.execute(context);
    }

    private void jn(Context context, String str) {
        if (!com.huawei.android.pushagent.model.channel.a.hk().gc() && TextUtils.isEmpty(j.ev(context).ew(str))) {
            com.huawei.android.pushagent.model.channel.a.hk().gf(true);
            c.sh("PushLog2951", "It is a new gettoken event, set force connect skip control.");
        }
    }

    private DeviceRegisterReqMessage je(Context context, String str) {
        int parseInt = Integer.parseInt(com.huawei.android.pushagent.utils.b.ui(context));
        int dq = h.dp(context).dq();
        c.sh("PushLog2951", "Device type is :" + dq + " [1:NOT_GDPR, 2:GDPR]");
        return new DeviceRegisterReqMessage(str, (byte) dq, (byte) com.huawei.android.pushagent.utils.b.tm(context), parseInt);
    }

    private void jh(Context context, Intent intent) {
        String stringExtra = intent.getStringExtra("pkg_name");
        if (TextUtils.isEmpty(stringExtra)) {
            c.sg("PushLog2951", "packagename is null, cannot deregister");
            return;
        }
        c.sg("PushLog2951", "responseClientUnRegistration: packagename = " + stringExtra);
        String stringExtra2 = intent.getStringExtra("device_token");
        if (TextUtils.isEmpty(stringExtra2)) {
            c.sg("PushLog2951", "origin token is null, cannot deregister");
            return;
        }
        if (intent.getBooleanExtra("isTokenEncrypt", false)) {
            stringExtra2 = com.huawei.android.pushagent.utils.a.e.nu(stringExtra2);
        }
        if (TextUtils.isEmpty(stringExtra2)) {
            c.sg("PushLog2951", "token is null, cannot deregister");
            return;
        }
        String m = com.huawei.android.pushagent.model.a.b.l(context).m(stringExtra2);
        if (TextUtils.isEmpty(m) || (stringExtra.equals(com.huawei.android.pushagent.utils.b.uf(m)) ^ 1) != 0) {
            c.sh("PushLog2951", "token is not exist or not match, don't need to unreg");
            return;
        }
        if (!"unInstall".equals(intent.getStringExtra("from"))) {
            com.huawei.android.pushagent.model.a.f.ak(context).am(m);
        }
        j.ev(context).remove(m);
        com.huawei.android.pushagent.model.a.b.l(context).r(m);
        if (com.huawei.android.pushagent.utils.tools.d.qo()) {
            com.huawei.android.pushagent.utils.tools.d.qt(stringExtra);
        }
        com.huawei.android.pushagent.model.a.a.c(context).i(stringExtra2, m);
        com.huawei.android.pushagent.model.token.a.execute(context);
    }

    private void iw(Context context, Intent intent) {
        String str = "";
        Uri data = intent.getData();
        if (data != null) {
            str = data.getSchemeSpecificPart();
        }
        boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.DATA_REMOVED", true);
        c.sg("PushLog2951", "ACTION_PACKAGE_REMOVED : isRemoveData=" + booleanExtra + " remove pkgName:" + str);
        if (booleanExtra) {
            c.sg("PushLog2951", "responseRemovePackage pkgName= " + str);
            String ue = com.huawei.android.pushagent.utils.b.ue(str, String.valueOf(f.vb()));
            com.huawei.android.pushagent.model.a.e.ae(context).ag(ue);
            ue = com.huawei.android.pushagent.model.a.b.l(context).n(ue);
            Intent intent2 = new Intent();
            intent2.putExtra("pkg_name", str);
            intent2.putExtra("device_token", ue);
            intent2.putExtra("from", "unInstall");
            jh(context, intent2);
        }
    }

    private void it(Context context, Intent intent) {
        if (context != null && intent != null) {
            Object stringExtra = intent.getStringExtra("msgIdStr");
            if (!TextUtils.isEmpty(stringExtra)) {
                String nu = com.huawei.android.pushagent.utils.a.e.nu(stringExtra);
                if (!TextUtils.isEmpty(nu)) {
                    c.sh("PushLog2951", "enter collectAndReportHiAnalytics, msgId is " + nu);
                    com.huawei.android.pushagent.model.d.e.kp().kr(context, nu);
                }
            }
        }
    }

    private void iv(Context context) {
        com.huawei.android.pushagent.model.d.e.kp().ks(context);
    }

    private void jd(Context context, Intent intent) {
        if (context == null || intent == null) {
            c.sf("PushLog2951", "enableReceiveNotifyMsg, context or intent is null");
            return;
        }
        try {
            Object stringExtra = intent.getStringExtra("enalbeFlag");
            if (TextUtils.isEmpty(stringExtra)) {
                c.sf("PushLog2951", "pkgAndFlagEncrypt is null");
                return;
            }
            stringExtra = com.huawei.android.pushagent.utils.a.e.nu(stringExtra);
            if (TextUtils.isEmpty(stringExtra)) {
                c.sh("PushLog2951", "pkgAndFlag is empty");
                return;
            }
            String[] split = stringExtra.split("#");
            if (2 != split.length) {
                c.sh("PushLog2951", "pkgAndFlag is invalid");
                return;
            }
            String str = split[0];
            boolean booleanValue = Boolean.valueOf(split[1]).booleanValue();
            c.sg("PushLog2951", "pkgName:" + str + ",flag:" + booleanValue);
            com.huawei.android.pushagent.model.a.e.ae(context).ah(com.huawei.android.pushagent.utils.b.ue(str, String.valueOf(f.vb())), booleanValue ^ 1);
        } catch (Throwable e) {
            c.se("PushLog2951", e.toString(), e);
        }
    }

    private void jc(Context context, Intent intent) {
        c.sh("PushLog2951", "enter dealwithTerminateAgreement");
        if (context == null || intent == null) {
            c.sf("PushLog2951", "dealwithTerminateAgreement, context or intent is null");
            return;
        }
        try {
            Object stringExtra = intent.getStringExtra("pkg_name");
            boolean booleanExtra = intent.getBooleanExtra("has_disagree_protocal", false);
            c.sh("PushLog2951", "pkg:" + stringExtra + ",flag:" + booleanExtra);
            if (TextUtils.isEmpty(stringExtra)) {
                c.sh("PushLog2951", "dealwithTerminateAgreement, pkgName is empty");
            } else {
                com.huawei.android.pushagent.model.a.d.aa(context).ac(stringExtra, booleanExtra);
            }
        } catch (Throwable e) {
            c.se("PushLog2951", e.toString(), e);
        }
    }

    private void ji(PushDataRspMessage pushDataRspMessage) {
        if (pushDataRspMessage == null) {
            c.sf("PushLog2951", "rspMsg or msgId is null");
            return;
        }
        try {
            com.huawei.android.pushagent.model.channel.a.hk().gb(pushDataRspMessage);
            c.sh("PushLog2951", "rspPushMessage the response msg is :" + pushDataRspMessage.vu() + ",msgId:" + com.huawei.android.pushagent.utils.d.b.sb(pushDataRspMessage.wm()) + ",flag:" + com.huawei.android.pushagent.utils.d.b.sc(pushDataRspMessage.wn()));
        } catch (Throwable e) {
            c.se("PushLog2951", "call ChannelMgr.getPushChannel().send cause:" + e.toString(), e);
        }
    }
}
