package defpackage;

import android.content.Context;
import android.os.Bundle;
import com.huawei.android.pushagent.datatype.IPushMessage;
import com.huawei.android.pushagent.datatype.pollingmessage.PollingDataReqMessage;
import com.huawei.android.pushagent.datatype.pollingmessage.PollingDataRspMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import com.huawei.bd.Reporter;
import java.net.InetSocketAddress;
import java.util.Date;

/* renamed from: s */
public class s extends ConnectEntity {
    public s(j jVar, Context context) {
        super(jVar, context, new u(context), s.class.getSimpleName());
        bl();
    }

    public void a(SocketEvent socketEvent, Bundle bundle) {
        aw.d("PushLog2828", "enter PollingConnectEntity:notifyEvent(" + socketEvent + ",bd:" + bundle + ")");
        switch (t.ao[socketEvent.ordinal()]) {
            case Reporter.ACTIVITY_CREATE /*1*/:
                this.S.bb();
                this.S.h(System.currentTimeMillis());
                try {
                    a(new PollingDataReqMessage(ae.l(this.context).R()));
                    this.R.getSocket().setSoTimeout((int) (ae.l(this.context).I() * 1000));
                } catch (Throwable e) {
                    aw.d("PushLog2828", "call send cause:" + e.toString(), e);
                }
            case Reporter.ACTIVITY_RESUME /*2*/:
                IPushMessage iPushMessage = (IPushMessage) bundle.getSerializable("push_msg");
                if (iPushMessage == null) {
                    aw.i("PushLog2828", "push_msg is null");
                    return;
                }
                aw.i("PushLog2828", "received polling Msg:" + iPushMessage.getClass().getSimpleName());
                if (iPushMessage instanceof PollingDataRspMessage) {
                    PollingDataRspMessage pollingDataRspMessage = (PollingDataRspMessage) iPushMessage;
                    if (pollingDataRspMessage.ay() < null || pollingDataRspMessage.ay() > ConnectEntityMode.values().length) {
                        aw.e("PushLog2828", "received mode:" + pollingDataRspMessage.ay() + " cannot be recongnized");
                        return;
                    }
                    ConnectEntityMode connectEntityMode = ConnectEntityMode.values()[pollingDataRspMessage.ay()];
                    ChannelMgr.g(this.context).a(connectEntityMode);
                    if (ConnectEntityMode.N == connectEntityMode && PushBastet.ac(this.context).bZ()) {
                        aw.i("PushLog2828", "bastet has started, but now is polling mode, close pushchannel to stop bastet");
                        ChannelMgr.aW().close();
                        PushBastet.ac(this.context).ca();
                    }
                    this.S.i((long) (pollingDataRspMessage.aA() * 1000));
                    if (pollingDataRspMessage.az() || connectEntityMode == ConnectEntityMode.M) {
                        try {
                            ChannelMgr.aW().a(true, pollingDataRspMessage.az());
                        } catch (Throwable e2) {
                            aw.d("PushLog2828", e2.toString(), e2);
                        }
                    }
                    try {
                        this.R.close();
                    } catch (Throwable e22) {
                        aw.d("PushLog2828", "call channel close cause:" + e22.toString(), e22);
                    }
                }
            default:
        }
    }

    public synchronized void a(boolean z) {
        aw.d("PushLog2828", "enter PollingConnectEntity:connect(forceCon:" + z + ")");
        this.S.bc();
        if (ae.l(this.context).am()) {
            if (hasConnection()) {
                aw.i("PushLog2828", "Polling aready connect, just wait Rsp!");
            } else {
                if (!z) {
                    if (System.currentTimeMillis() < this.S.be() + this.S.e(false) && System.currentTimeMillis() > this.S.be()) {
                        aw.i("PushLog2828", "cannot connect, heartBeatInterval:" + this.S.e(false) + " lastCntTime:" + new Date(this.S.be()));
                    }
                }
                if (au.G(this.context) == -1) {
                    aw.i("PushLog2828", "no network, so cannot connect Polling");
                } else if (this.Q == null || !this.Q.isAlive()) {
                    aw.d("PushLog2828", "begin to create new socket, so close socket");
                    aZ();
                    close();
                    InetSocketAddress h = ae.l(this.context).h(false);
                    if (h != null) {
                        aw.d("PushLog2828", "get pollingSrvAddr:" + h);
                        this.P.D = h.getAddress().getHostAddress();
                        this.P.port = h.getPort();
                        this.Q = new v(this);
                        this.Q.start();
                    } else {
                        aw.e("PushLog2828", "no valid pollingSrvAddr, just wait!!");
                    }
                } else {
                    aw.i("PushLog2828", "aready in connect, just wait!! srvSocket:" + this.Q.toString());
                }
            }
        }
    }

    public synchronized void a(boolean z, boolean z2) {
        a(z);
    }

    public ConnectEntityMode ba() {
        return ConnectEntityMode.N;
    }

    public boolean bl() {
        if (this.P == null) {
            this.P = new j("", -1, false, ChannelType.aE);
        }
        return true;
    }
}
