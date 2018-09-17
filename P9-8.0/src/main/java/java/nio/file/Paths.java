package java.nio.file;

import java.net.URI;
import java.nio.file.spi.FileSystemProvider;

public final class Paths {
    private Paths() {
    }

    public static Path get(String first, String... more) {
        return FileSystems.getDefault().getPath(first, more);
    }

    public static Path get(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("Missing scheme");
        } else if (scheme.equalsIgnoreCase("file")) {
            return FileSystems.getDefault().provider().getPath(uri);
        } else {
            for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
                if (provider.getScheme().equalsIgnoreCase(scheme)) {
                    return provider.getPath(uri);
                }
            }
            throw new FileSystemNotFoundException("Provider \"" + scheme + "\" not installed");
        }
    }
}
