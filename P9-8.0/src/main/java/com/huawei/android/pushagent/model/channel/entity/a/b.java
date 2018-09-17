package com.huawei.android.pushagent.model.channel.entity.a;

import android.os.Bundle;
import com.huawei.android.pushagent.datatype.exception.PushException;
import com.huawei.android.pushagent.datatype.exception.PushException.ErrorType;
import com.huawei.android.pushagent.datatype.tcp.HeartBeatRspMessage;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread$SocketEvent;
import com.huawei.android.pushagent.model.channel.entity.a;
import com.huawei.android.pushagent.utils.d.c;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.Socket;

public class b extends a {
    b(com.huawei.android.pushagent.model.channel.entity.b bVar) {
        super(bVar);
    }

    protected void fm() {
        InputStream inputStream = null;
        try {
            if (this.am.av == null || this.am.av.gw() == null) {
                c.sf("PushLog2951", "no socket when in readSSLSocket");
                fk();
                return;
            }
            Socket gw = this.am.av.gw();
            if (gw != null) {
                c.sg("PushLog2951", "socket timeout is " + gw.getSoTimeout());
            }
            inputStream = this.am.av.gv();
            int i = -1;
            while (!isInterrupted() && this.am.av.gu()) {
                if (inputStream != null) {
                    i = inputStream.read();
                    if (HeartBeatRspMessage.wf() == ((byte) i)) {
                        com.huawei.android.pushagent.utils.b.us(this.an, 200);
                    } else {
                        com.huawei.android.pushagent.utils.b.us(this.an, 5000);
                    }
                } else {
                    c.sg("PushLog2951", "inputstream is null, cannot get cmdId");
                }
                if (-1 == i) {
                    com.huawei.android.pushagent.a.a.xv(85);
                    c.sg("PushLog2951", "read -1 data, socket may be close");
                    break;
                }
                fl(i, inputStream);
                if (gw != null) {
                    this.am.av.gw().setSoTimeout(0);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    c.sf("PushLog2951", "close dis failed");
                }
            }
            fk();
            throw new PushException(" read normal Exit", ErrorType.Err_Read);
        } catch (Throwable e2) {
            throw new PushException(e2, ErrorType.Err_Read);
        } catch (Throwable e22) {
            throw new PushException(e22, ErrorType.Err_Read);
        } catch (Throwable e222) {
            throw new PushException(e222, ErrorType.Err_Read);
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    c.sf("PushLog2951", "close dis failed");
                }
            }
            fk();
        }
    }

    private void fk() {
        if (this.am.av != null) {
            try {
                com.huawei.android.pushagent.a.a.xv(86);
                this.am.av.gs();
            } catch (Exception e) {
                c.sg("PushLog2951", "close socket protocol exception");
            }
        }
    }

    private void fl(int i, InputStream inputStream) {
        try {
            Serializable vx = com.huawei.android.pushagent.datatype.tcp.base.a.vx((byte) i, inputStream);
            Bundle bundle = new Bundle();
            if (vx != null) {
                com.huawei.android.pushagent.utils.b.uo();
                bundle.putSerializable("push_msg", vx);
            } else {
                c.sf("PushLog2951", "received invalid cmdId");
            }
            this.am.fs(SocketReadThread$SocketEvent.SocketEvent_MSG_RECEIVED, bundle);
        } catch (InstantiationException e) {
            c.sf("PushLog2951", "call getEntityByCmdId(cmd:" + i + " cause InstantiationException");
        } catch (Exception e2) {
            c.sf("PushLog2951", "call getEntityByCmdId(cmd:" + i + " Exception");
        }
    }
}
