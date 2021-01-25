package ohos.batterysipperadapter;

public class BatterySipperHelper {
    private static BatterySipperService sBatterySipperService;

    public static BatterySipperService getService() {
        if (sBatterySipperService == null) {
            sBatterySipperService = new BatterySipperService();
        }
        return sBatterySipperService;
    }
}
