package tmsdkobf;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.qq.taf.jce.JceStruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import tmsdk.common.utils.f;
import tmsdk.fg.module.cleanV2.IUpdateCallBack;

public class qw {
    private static qw OB;
    static final Object OI = new Object();
    private qn OC = new qn();
    IUpdateCallBack OD;
    private ArrayList<String> OE;
    private ArrayList<String> OF;
    jy OG = new jy() {
        public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
            f.f("ListNetService", "onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
            if (i3 != 0) {
                synchronized (qw.OI) {
                    qw.this.OF = null;
                    qw.this.OE = null;
                    qw.this.OH = false;
                }
                synchronized (qw.this.mLock) {
                    if (qw.this.OD != null) {
                        qw.this.OD.updateEnd(0);
                        qw.this.OD = null;
                    }
                }
                return;
            }
            f.f("ListNetService", "upload onFinish()");
            synchronized (qw.this.mLock) {
                if (qw.this.OD != null) {
                    qw.this.OD.updateEnd(0);
                    qw.this.OD = null;
                }
            }
            qw.this.OC.K(System.currentTimeMillis());
            qx qxVar = new qx();
            Message obtainMessage = qw.this.mHandler.obtainMessage();
            qxVar.Y = i2;
            qxVar.ey = i;
            qxVar.wL = jceStruct;
            obtainMessage.obj = qxVar;
            obtainMessage.what = 1;
            qw.this.mHandler.sendMessage(obtainMessage);
            if (qw.this.OE == null || qw.this.OE.size() == 0) {
                qw.this.OC.X(true);
                qw.this.OH = false;
                return;
            }
            qw.this.mHandler.sendEmptyMessage(2);
        }
    };
    private boolean OH = false;
    private Handler mHandler;
    private Object mLock = new Object();
    private ob mk = im.bK();
    private HandlerThread vG = im.bJ().newFreeHandlerThread("networkSharkThread");
    ka wb = new ka() {
        public oh<Long, Integer, JceStruct> a(int i, long j, int i2, JceStruct jceStruct) {
            switch (i2) {
                case 13652:
                    synchronized (qw.this.mLock) {
                        if (qw.this.OD != null) {
                            qw.this.OD.updateEnd(0);
                            qw.this.OD = null;
                        }
                        if (jceStruct != null) {
                            f.f("ListNetService", "listener push");
                            qw.this.OC.J(System.currentTimeMillis());
                            qx qxVar = new qx();
                            Message obtainMessage = qw.this.mHandler.obtainMessage();
                            qxVar.Y = i2;
                            qxVar.ey = i;
                            qxVar.ex = j;
                            qxVar.wL = jceStruct;
                            obtainMessage.obj = qxVar;
                            obtainMessage.what = 1;
                            qw.this.mHandler.sendMessage(obtainMessage);
                            break;
                        }
                        f.f("ListNetService", "push == null");
                        return null;
                    }
            }
            return null;
        }
    };

    private qw() {
        this.vG.start();
        this.mHandler = new Handler(this.vG.getLooper()) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        if (message.obj != null) {
                            qx qxVar = (qx) message.obj;
                            ah ahVar = (ah) qxVar.wL;
                            f.f("ListNetService", "收到云端push");
                            f.f("ListNetService", "onRecvPush + info.seqNo :" + qxVar.ey + "  info.pushId :" + qxVar.ex);
                            f.f("ListNetService", "size:" + ahVar.aU.size());
                            List<String> a = qw.this.D(ahVar.aU);
                            if (!qw.this.OC.jv()) {
                                qw.this.OC.W(true);
                            }
                            JceStruct afVar = new af();
                            afVar.aQ = new ArrayList();
                            for (String str : a) {
                                ag agVar = new ag();
                                agVar.fileName = str;
                                agVar.aT = 0;
                                afVar.aQ.add(agVar);
                            }
                            qw.this.mk.b(qxVar.ey, qxVar.ex, qxVar.Y, afVar);
                            return;
                        }
                        return;
                    case 2:
                        qw.this.jO();
                        return;
                    case 3:
                        qw.this.OE = qw.this.jQ();
                        qw.this.jN();
                        return;
                    case 4:
                        ArrayList m = js.cE().m(js.cE().cG());
                        if (m.size() >= 1) {
                            js.cE().i(m);
                        } else {
                            f.f("ListNetService", "it doesn't need update");
                            qw.this.OE = qw.this.jQ();
                            qw.this.jN();
                        }
                        synchronized (qw.this.mLock) {
                            if (qw.this.OD != null) {
                                qw.this.OD.updateEnd(0);
                                qw.this.OD = null;
                            }
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private List<String> D(List<am> list) {
        return qo.jz().y(list);
    }

    private ArrayList<String> a(String str, File -l_5_R, int i) {
        if (3 == i) {
            return null;
        }
        ArrayList<String> arrayList = new ArrayList();
        File[] listFiles = -l_5_R.listFiles();
        if (listFiles == null) {
            return null;
        }
        File[] fileArr = listFiles;
        for (File file : listFiles) {
            if (file.isDirectory()) {
                arrayList.add(file.getAbsolutePath().substring(str.length()).toLowerCase());
                Collection a = a(str, file, i + 1);
                if (a != null) {
                    arrayList.addAll(a);
                }
            }
        }
        return arrayList;
    }

    public static qw jM() {
        if (OB == null) {
            OB = new qw();
        }
        return OB;
    }

    private void jN() {
        if (!this.OH) {
            this.OH = true;
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private void jO() {
        this.OF = null;
        if (this.OE != null) {
            int size = this.OE.size();
            f.f("ListNetService", "report sd card !!: ");
            if (size > 50) {
                this.OF = new ArrayList();
                for (int i = 0; i < 50; i++) {
                    this.OF.add(this.OE.get(i));
                }
                this.OE.removeAll(this.OF);
            } else if (size <= 0) {
                this.OH = false;
                return;
            } else {
                this.OF = (ArrayList) this.OE.clone();
                this.OE.removeAll(this.OF);
            }
        }
        synchronized (OI) {
            if (this.OF == null) {
            } else if (this.OF.size() >= 1) {
                JceStruct acVar = new ac();
                acVar.aC = this.OF;
                this.mk.a(3652, acVar, new ah(), 0, this.OG);
            }
        }
    }

    private ArrayList<String> jP() {
        if (Environment.getExternalStorageState() == "unmounted") {
            return null;
        }
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        String absolutePath = externalStorageDirectory.getAbsolutePath();
        File[] listFiles = externalStorageDirectory.listFiles();
        ArrayList<String> arrayList = new ArrayList();
        if (listFiles == null) {
            return null;
        }
        File[] fileArr = listFiles;
        for (File file : listFiles) {
            if (file.isDirectory()) {
                arrayList.add(file.getAbsolutePath().substring(absolutePath.length()).toLowerCase());
                Collection a = a(absolutePath, file, 1);
                if (a != null) {
                    arrayList.addAll(a);
                }
            }
        }
        return arrayList;
    }

    private ArrayList<String> jQ() {
        f.f("ListNetService", "get sd card !!: : ");
        return jP();
    }

    public synchronized boolean b(IUpdateCallBack iUpdateCallBack) {
        this.OD = iUpdateCallBack;
        if (qo.jz().jC()) {
            long currentTimeMillis = System.currentTimeMillis() - this.OC.jx();
            if ((System.currentTimeMillis() - this.OC.jy() <= 604800000 ? 1 : null) == null) {
                this.OC.X(false);
            }
            if ((currentTimeMillis <= 604800000 ? 1 : null) == null) {
                js.cE().cF();
            }
            if (0 == this.OC.jy() && 0 == this.OC.jx()) {
                this.OC.W(false);
            }
        } else {
            this.OC.X(false);
            js.cE().cF();
            this.OC.W(false);
        }
        this.mHandler.sendEmptyMessage(4);
        if (!this.OC.jw()) {
            this.mHandler.sendEmptyMessage(3);
        }
        kr.dz();
        f.f("ListNetService", "uploadSoftwareAndDirs guid:" + this.mk.b());
        return true;
    }

    public synchronized void de() {
        if (this.mk == null) {
            this.mk = im.bK();
        }
        if (this.mk == null) {
            try {
                throw new Exception("registerSharkePush failed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.mk.v(13652, 2);
            this.mk.a(13652, new ah(), 2, this.wb);
        }
        return;
    }
}
