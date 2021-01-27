package ohos.batterysipperadapter;

public class BatterySipperHelper {
    private static volatile BatterySipperService batterySipperService;

    private BatterySipperHelper() {
    }

    public static BatterySipperService getService() {
        if (batterySipperService == null) {
            synchronized (BatterySipperHelper.class) {
                if (batterySipperService == null) {
                    batterySipperService = new BatterySipperService();
                }
            }
        }
        return batterySipperService;
    }
}
