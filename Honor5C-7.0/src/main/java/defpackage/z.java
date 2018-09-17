package defpackage;

import android.os.Bundle;
import com.huawei.android.pushagent.datatype.PushException;
import com.huawei.android.pushagent.datatype.PushException.ErrorType;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.PushDataReqMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;

/* renamed from: z */
public class z extends SocketReadThread {
    z(ConnectEntity connectEntity) {
        super(connectEntity);
    }

    protected void bk() {
        byte aP;
        Throwable e;
        InputStream inputStream = null;
        InputStream inputStream2;
        try {
            if (this.ah.R == null || this.ah.R.getSocket() == null) {
                aw.e("PushLog2828", "no socket when in readSSLSocket");
                if (inputStream != null) {
                    inputStream.close();
                }
                if (this.ah.R != null) {
                    this.ah.R.close();
                    return;
                }
                return;
            }
            Socket socket = this.ah.R.getSocket();
            if (socket != null) {
                aw.d("PushLog2828", "socket timeout is " + socket.getSoTimeout());
            }
            inputStream2 = this.ah.R.getInputStream();
            byte b = (byte) -1;
            int i = -1;
            while (!isInterrupted() && this.ah.R.hasConnection()) {
                try {
                    int i2;
                    if (b != (byte) -1) {
                        i2 = b;
                        b = (byte) -1;
                    } else if (inputStream2 != null) {
                        i = inputStream2.read();
                        if (NewHeartBeatRspMessage.ax() == ((byte) i)) {
                            au.a(this.mContext, 200);
                            i2 = i;
                        } else {
                            au.a(this.mContext, 5000);
                            i2 = i;
                        }
                    } else {
                        aw.d("PushLog2828", "inputstream is null, cannot get cmdId");
                        i2 = i;
                    }
                    if (-1 == i2) {
                        aw.d("PushLog2828", "read -1 data, socket may be close");
                        break;
                    }
                    String f = au.f(new byte[]{(byte) i2});
                    aw.i("PushLog2828", "received a msg cmdId:" + f);
                    try {
                        Serializable b2;
                        if (PushDataReqMessage.ax() == ((byte) i2)) {
                            aw.d("PushLog2828", "is PushDataReqMessage set read TimeOut 100");
                            if (socket != null) {
                                socket.setSoTimeout(100);
                            } else {
                                aw.d("PushLog2828", "socket is null");
                            }
                            b2 = n.b(Byte.valueOf((byte) i2), inputStream2);
                            if (b2 != null) {
                                PushDataReqMessage pushDataReqMessage = (PushDataReqMessage) b2;
                                if (pushDataReqMessage.aP() != (byte) -1) {
                                    aw.d("PushLog2828", "is get next cmdId, so set it");
                                    aP = pushDataReqMessage.aP();
                                }
                            }
                            aP = b;
                        } else {
                            b2 = n.b(Byte.valueOf((byte) i2), inputStream2);
                            aP = b;
                        }
                        if (b2 != null) {
                            try {
                                au.bI();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("push_msg", b2);
                                this.ah.a(SocketEvent.am, bundle);
                            } catch (InstantiationException e2) {
                                aw.e("PushLog2828", "call getEntityByCmdId(cmd:" + i2 + " cause InstantiationException");
                                if (socket != null) {
                                    continue;
                                } else if (ChannelMgr.aU() != this.ah.ba()) {
                                    this.ah.R.getSocket().setSoTimeout(0);
                                } else {
                                    this.ah.R.getSocket().setSoTimeout((int) (this.ah.S.e(false) + ae.l(this.mContext).ad()));
                                }
                                b = aP;
                                i = i2;
                            } catch (Exception e3) {
                                aw.e("PushLog2828", "call getEntityByCmdId(cmd:" + i2 + " Exception");
                                if (socket != null) {
                                    continue;
                                } else if (ChannelMgr.aU() != this.ah.ba()) {
                                    this.ah.R.getSocket().setSoTimeout(0);
                                } else {
                                    this.ah.R.getSocket().setSoTimeout((int) (this.ah.S.e(false) + ae.l(this.mContext).ad()));
                                }
                                b = aP;
                                i = i2;
                            }
                        } else {
                            aw.e("PushLog2828", "received invalid msg, cmdId" + f);
                        }
                        if (socket == null) {
                            continue;
                        } else if (ChannelMgr.aU() == this.ah.ba()) {
                            this.ah.R.getSocket().setSoTimeout(0);
                        } else {
                            this.ah.R.getSocket().setSoTimeout((int) (this.ah.S.e(false) + ae.l(this.mContext).ad()));
                        }
                    } catch (InstantiationException e4) {
                        aP = b;
                        aw.e("PushLog2828", "call getEntityByCmdId(cmd:" + i2 + " cause InstantiationException");
                        if (socket != null) {
                            continue;
                        } else if (ChannelMgr.aU() != this.ah.ba()) {
                            this.ah.R.getSocket().setSoTimeout((int) (this.ah.S.e(false) + ae.l(this.mContext).ad()));
                        } else {
                            this.ah.R.getSocket().setSoTimeout(0);
                        }
                        b = aP;
                        i = i2;
                    } catch (Exception e5) {
                        aP = b;
                        aw.e("PushLog2828", "call getEntityByCmdId(cmd:" + i2 + " Exception");
                        if (socket != null) {
                            continue;
                        } else if (ChannelMgr.aU() != this.ah.ba()) {
                            this.ah.R.getSocket().setSoTimeout((int) (this.ah.S.e(false) + ae.l(this.mContext).ad()));
                        } else {
                            this.ah.R.getSocket().setSoTimeout(0);
                        }
                        b = aP;
                        i = i2;
                    }
                    b = aP;
                    i = i2;
                } catch (SocketException e6) {
                    e = e6;
                    inputStream = inputStream2;
                } catch (IOException e7) {
                    e = e7;
                } catch (Exception e8) {
                    e = e8;
                } catch (Throwable th) {
                    if (socket != null) {
                        if (ChannelMgr.aU() == this.ah.ba()) {
                            this.ah.R.getSocket().setSoTimeout(0);
                        } else {
                            this.ah.R.getSocket().setSoTimeout((int) (this.ah.S.e(false) + ae.l(this.mContext).ad()));
                        }
                    }
                }
            }
            if (inputStream2 != null) {
                inputStream2.close();
            }
            if (this.ah.R != null) {
                this.ah.R.close();
            }
            throw new PushException(" read normal Exit", ErrorType.x);
        } catch (SocketException e9) {
            e = e9;
            try {
                throw new PushException(e, ErrorType.x);
            } catch (Throwable th2) {
                e = th2;
                inputStream2 = inputStream;
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                if (this.ah.R != null) {
                    this.ah.R.close();
                }
                throw e;
            }
        } catch (IOException e10) {
            e = e10;
            inputStream2 = inputStream;
            try {
                throw new PushException(e, ErrorType.x);
            } catch (Throwable th3) {
                e = th3;
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                if (this.ah.R != null) {
                    this.ah.R.close();
                }
                throw e;
            }
        } catch (Exception e11) {
            e = e11;
            inputStream2 = inputStream;
            throw new PushException(e, ErrorType.x);
        } catch (Throwable th4) {
            e = th4;
            inputStream2 = inputStream;
            if (inputStream2 != null) {
                inputStream2.close();
            }
            if (this.ah.R != null) {
                this.ah.R.close();
            }
            throw e;
        }
    }
}
