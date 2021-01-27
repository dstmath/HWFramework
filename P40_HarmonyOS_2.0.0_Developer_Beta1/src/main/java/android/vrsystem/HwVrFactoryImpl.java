package android.vrsystem;

public class HwVrFactoryImpl extends DefaultHwVrFactory {
    public DefaultHwVrServiceManager getHwVrServiceManager() {
        return HwVrServiceManagerImpl.getDefault();
    }
}
