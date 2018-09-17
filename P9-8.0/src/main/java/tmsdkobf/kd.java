package tmsdkobf;

import java.util.concurrent.atomic.AtomicInteger;

public class kd {
    boolean tF = false;
    AtomicInteger tG = new AtomicInteger();

    public boolean cJ() {
        return this.tF;
    }

    public void setState(int i) {
        this.tG.set(i);
    }
}
