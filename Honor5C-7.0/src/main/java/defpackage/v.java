package defpackage;

import android.os.Bundle;
import com.huawei.android.pushagent.datatype.PushException;
import com.huawei.android.pushagent.datatype.PushException.ErrorType;
import com.huawei.android.pushagent.datatype.pollingmessage.basic.PollingMessage;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel;
import java.io.InputStream;
import java.io.Serializable;
import java.net.SocketException;

/* renamed from: v */
class v extends SocketReadThread {
    public v(ConnectEntity connectEntity) {
        super(connectEntity);
    }

    protected void bk() {
        Throwable e;
        Object obj;
        IPushChannel iPushChannel = null;
        InputStream inputStream;
        try {
            if (this.ah.R == null || this.ah.R.getSocket() == null) {
                aw.e("PushLog2828", "no socket when in readSSLSocket");
                if (iPushChannel != null) {
                    iPushChannel.close();
                }
                if (this.ah.R != null) {
                    this.ah.R.close();
                    this.ah.R = iPushChannel;
                    return;
                }
                return;
            }
            aw.d("PushLog2828", "socket timeout is " + this.ah.R.getSocket().getSoTimeout());
            inputStream = this.ah.R.getInputStream();
            while (!isInterrupted() && this.ah.R.hasConnection()) {
                try {
                    Serializable b;
                    if (inputStream != null) {
                        b = PollingMessage.b(inputStream);
                    } else {
                        aw.i("PushLog2828", "InputStream is null, get pollingMessage failed");
                        Object obj2 = iPushChannel;
                    }
                    if (b != null) {
                        au.bI();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("push_msg", b);
                        this.ah.a(SocketEvent.am, bundle);
                    }
                } catch (SocketException e2) {
                    aw.d("PushLog2828", "SocketException:" + e2.toString());
                } catch (Throwable e3) {
                    aw.d("PushLog2828", "call getEntityByCmdId cause:" + e3.toString(), e3);
                    throw e3;
                } catch (Exception e4) {
                    e3 = e4;
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (this.ah.R != null) {
                this.ah.R.close();
                this.ah.R = iPushChannel;
            }
        } catch (Exception e5) {
            e3 = e5;
            obj = iPushChannel;
            try {
                throw new PushException(e3, ErrorType.x);
            } catch (Throwable th) {
                e3 = th;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (this.ah.R != null) {
                    this.ah.R.close();
                    this.ah.R = iPushChannel;
                }
                throw e3;
            }
        } catch (Throwable th2) {
            e3 = th2;
            obj = iPushChannel;
            if (inputStream != null) {
                inputStream.close();
            }
            if (this.ah.R != null) {
                this.ah.R.close();
                this.ah.R = iPushChannel;
            }
            throw e3;
        }
    }
}
