package tmsdkobf;

import tmsdk.common.tcc.DeepCleanEngine;
import tmsdk.common.tcc.SdcardScannerFactory;

public class rb {
    ra Ob;
    DeepCleanEngine Pa;
    boolean Pb = false;
    qz Pc;

    public rb(qz qzVar) {
        this.Pc = qzVar;
    }

    private void kc() {
        this.Ob.release();
        this.Ob = null;
    }

    public boolean a(final ra raVar) {
        this.Ob = raVar;
        if (this.Ob == null || raVar.jT() == null) {
            return false;
        }
        im.bJ().addTask(new Runnable() {
            public void run() {
                try {
                    rb.this.Pa = SdcardScannerFactory.getDeepCleanEngine(raVar);
                    if (rb.this.Pa != null) {
                        if (rb.this.Pb) {
                            rb.this.cancel();
                        }
                        rb.this.Ob.onScanStarted();
                        if (rb.this.ka()) {
                            if (rb.this.Pb) {
                                rb.this.Ob.jW();
                            } else {
                                rb.this.Ob.jX();
                            }
                        }
                        rb.this.Pa.release();
                        rb.this.Pa = null;
                        rb.this.kb();
                        rb.this.kc();
                        return;
                    }
                    rb.this.Ob.onScanError(-1);
                    rb.this.kc();
                } catch (Exception e) {
                    e.printStackTrace();
                    rb.this.Ob.onScanError(-4);
                }
            }
        }, null);
        return true;
    }

    public void cancel() {
        this.Pb = true;
        if (this.Pa != null) {
            this.Pa.cancel();
        }
    }

    protected boolean ka() {
        return true;
    }

    protected void kb() {
    }

    public void release() {
        if (this.Pa != null) {
            this.Pa.cancel();
        }
    }
}
