package android_maps_conflict_avoidance.com.google.common.io;

import java.io.IOException;
import java.io.InputStream;

public class Gunzipper {
    private static final Gunzipper instance = new Gunzipper();
    private GunzipInterface implementation;

    public interface GunzipInterface {
        InputStream gunzip(InputStream inputStream) throws IOException;
    }

    public static void setImplementation(GunzipInterface implementation) {
        instance.implementation = implementation;
    }

    public static InputStream gunzip(InputStream source) throws IOException {
        return instance.implementation.gunzip(source);
    }

    private Gunzipper() {
    }
}
