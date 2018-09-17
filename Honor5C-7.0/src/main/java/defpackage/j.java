package defpackage;

import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;

/* renamed from: j */
public class j {
    public String D;
    public ChannelType E;
    public boolean F;
    public int port;

    public j(String str, int i, boolean z, ChannelType channelType) {
        this.D = str;
        this.port = i;
        this.F = z;
        this.E = channelType;
    }

    public String toString() {
        return new StringBuffer().append("ip:").append(this.D).append(" port:").append(this.port).append(" useProxy:").append(this.F).append(" conType").append(this.E).toString();
    }
}
