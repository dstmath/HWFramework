package android.provider;

public class HwMediaStoreDummy implements IHwMediaStore {
    private static IHwMediaStore mInstance = new HwMediaStoreDummy();

    public String getPinyinForSort(String name) {
        return name;
    }

    public static IHwMediaStore getDefault() {
        return mInstance;
    }
}
