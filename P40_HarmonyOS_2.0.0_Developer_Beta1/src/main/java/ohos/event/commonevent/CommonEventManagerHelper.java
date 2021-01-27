package ohos.event.commonevent;

public final class CommonEventManagerHelper {
    private static final CommonEventAdapter SERVICE = new CommonEventAdapter();

    public static ICommonEventManager getService() {
        return SERVICE;
    }

    private CommonEventManagerHelper() {
    }
}
