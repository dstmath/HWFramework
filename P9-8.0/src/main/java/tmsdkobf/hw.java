package tmsdkobf;

import android.telephony.PhoneStateListener;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.DualSimTelephonyManager;
import tmsdk.common.utils.f;

public class hw {
    private int qM = 0;
    private PhoneStateListener qN;
    private PhoneStateListener qO;
    private List<b> qP = new ArrayList();

    public interface b {
        void aA(String str);

        void aB(String str);

        void az(String str);

        void g(String str, String str2);
    }

    private static class a {
        static hw qR = new hw();
    }

    public hw() {
        register();
    }

    private void aA(String str) {
        f.f("PhoneStateManager", "onHoldOff number=" + str);
        synchronized (this.qP) {
            for (b aA : this.qP) {
                aA.aA(str);
            }
        }
    }

    private void aE(String str) {
        f.f("PhoneStateManager", "onOutCall number=" + str);
        synchronized (this.qP) {
            for (b aB : this.qP) {
                aB.aB(str);
            }
        }
    }

    private void az(String str) {
        f.f("PhoneStateManager", "onConnect number=" + str);
        synchronized (this.qP) {
            for (b az : this.qP) {
                az.az(str);
            }
        }
    }

    public static hw bx() {
        return a.qR;
    }

    private void h(String str, String str2) {
        f.f("PhoneStateManager", "onCallComing number=" + str);
        synchronized (this.qP) {
            for (b g : this.qP) {
                g.g(str, str2);
            }
        }
    }

    private void register() {
        qc qcVar = im.rE;
        Object obj = 1;
        if (qcVar != null && qcVar.iu()) {
            try {
                this.qN = new PhoneStateListener(0) {
                    public void onCallStateChanged(int i, String str) {
                        String str2 = null;
                        switch (i) {
                            case 0:
                                hw.this.aA(str);
                                break;
                            case 1:
                                qc qcVar = im.rE;
                                if (qcVar != null) {
                                    str2 = qcVar.bT(0);
                                }
                                hw.this.h(str, str2);
                                break;
                            case 2:
                                if (hw.this.qM != 1) {
                                    if (hw.this.qM == 0) {
                                        hw.this.aE(str);
                                        break;
                                    }
                                }
                                hw.this.az(str);
                                break;
                                break;
                        }
                        hw.this.qM = i;
                        super.onCallStateChanged(i, str);
                    }
                };
                this.qO = new PhoneStateListener(1) {
                    public void onCallStateChanged(int i, String str) {
                        String str2 = null;
                        switch (i) {
                            case 0:
                                hw.this.aA(str);
                                break;
                            case 1:
                                qc qcVar = im.rE;
                                if (qcVar != null) {
                                    str2 = qcVar.bT(1);
                                }
                                if (str2 == null) {
                                    f.e("PhoneStateManager", "Incoming call from 2nd sim card but no card value!");
                                }
                                hw.this.h(str, str2);
                                break;
                            case 2:
                                if (hw.this.qM != 1) {
                                    if (hw.this.qM == 0) {
                                        hw.this.aE(str);
                                        break;
                                    }
                                }
                                hw.this.az(str);
                                break;
                                break;
                        }
                        hw.this.qM = i;
                        super.onCallStateChanged(i, str);
                    }
                };
                obj = null;
            } catch (Throwable th) {
            }
        }
        if (obj != null) {
            this.qN = new PhoneStateListener() {
                public void onCallStateChanged(int i, String str) {
                    qc qcVar = im.rE;
                    if (qcVar != null) {
                        String iv = qcVar.iv();
                        if (iv != null && iv.indexOf("htc") > -1 && (iv.indexOf("t328w") > -1 || iv.indexOf("t328d") > -1)) {
                            super.onCallStateChanged(i, str);
                            return;
                        }
                    }
                    switch (i) {
                        case 0:
                            hw.this.aA(str);
                            break;
                        case 1:
                            hw.this.h(str, qcVar != null ? qcVar.bT(0) : null);
                            break;
                        case 2:
                            if (hw.this.qM != 1) {
                                if (hw.this.qM == 0) {
                                    hw.this.aE(str);
                                    break;
                                }
                            }
                            hw.this.az(str);
                            break;
                            break;
                    }
                    hw.this.qM = i;
                    super.onCallStateChanged(i, str);
                }
            };
            this.qO = new PhoneStateListener() {
                public void onCallStateChanged(int i, String str) {
                    String str2 = null;
                    switch (i) {
                        case 0:
                            hw.this.aA(str);
                            break;
                        case 1:
                            qc qcVar = im.rE;
                            if (qcVar != null) {
                                str2 = qcVar.bT(1);
                            }
                            if (str2 == null) {
                                f.e("PhoneStateManager", "Incoming call from 2nd sim card but no card value!");
                            }
                            hw.this.h(str, str2);
                            break;
                        case 2:
                            if (hw.this.qM != 1) {
                                if (hw.this.qM == 0) {
                                    hw.this.aE(str);
                                    break;
                                }
                            }
                            hw.this.az(str);
                            break;
                            break;
                    }
                    hw.this.qM = i;
                    super.onCallStateChanged(i, str);
                }
            };
        }
        DualSimTelephonyManager instance = DualSimTelephonyManager.getInstance();
        instance.listenPhonesState(0, this.qN, 32);
        instance.listenPhonesState(1, this.qO, 32);
    }

    @Deprecated
    public void a(b bVar) {
        synchronized (this.qP) {
            this.qP.add(0, bVar);
        }
    }

    public boolean b(b bVar) {
        boolean remove;
        synchronized (this.qP) {
            remove = !this.qP.contains(bVar) ? true : this.qP.remove(bVar);
        }
        return remove;
    }
}
