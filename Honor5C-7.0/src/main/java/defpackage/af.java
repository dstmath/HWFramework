package defpackage;

/* renamed from: af */
class af extends Thread {
    final /* synthetic */ ae aP;

    af(ae aeVar, String str) {
        this.aP = aeVar;
        super(str);
    }

    public void run() {
        try {
            k y = bx.y(this.aP.context, this.aP.o());
            if (y == null) {
                y = new k(this.aP.context);
            }
            if (y.isValid()) {
                this.aP.b(y);
            } else {
                aw.i("PushLog2828", "query trs error:" + this.aP.getResult());
            }
        } catch (Throwable e) {
            aw.d("PushLog2828", e.toString(), e);
        }
    }
}
