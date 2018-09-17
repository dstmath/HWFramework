package tmsdkobf;

import tmsdk.common.creator.ManagerCreatorC;

public class ij {
    private static volatile boolean ry = false;

    public static void reportChannelInfo() {
        if (!ry) {
            ry = true;
            final md mdVar = new md("tms");
            if (!mdVar.getBoolean("reportlc", false)) {
                im.bJ().addTask(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (((ot) ManagerCreatorC.getManager(ot.class)).hw() == 0) {
                            mdVar.a("reportlc", true, true);
                        }
                        ij.ry = false;
                    }
                }, "nmct");
            }
        }
    }
}
