package android.provider;

public class HwMediaStoreDummy implements IHwMediaStore {
    private static IHwMediaStore sInstance = new HwMediaStoreDummy();

    @Override // android.provider.IHwMediaStore
    public String getPinyinForSort(String name) {
        return name;
    }

    public static IHwMediaStore getDefault() {
        return sInstance;
    }
}
