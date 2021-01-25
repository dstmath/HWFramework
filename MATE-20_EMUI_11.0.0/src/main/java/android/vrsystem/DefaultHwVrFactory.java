package android.vrsystem;

public class DefaultHwVrFactory {

    private static class Instance {
        private static DefaultHwVrFactory sInstance = new DefaultHwVrFactory();

        private Instance() {
        }
    }

    public static DefaultHwVrFactory getInstance() {
        return Instance.sInstance;
    }

    public DefaultHwVrServiceManager getHwVrServiceManager() {
        return DefaultHwVrServiceManager.getDefault();
    }
}
