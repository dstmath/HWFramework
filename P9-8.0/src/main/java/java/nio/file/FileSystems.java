package java.nio.file;

import java.io.IOException;
import java.net.URI;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import sun.nio.fs.DefaultFileSystemProvider;

public final class FileSystems {

    private static class DefaultFileSystemHolder {
        static final FileSystem defaultFileSystem = defaultFileSystem();

        private DefaultFileSystemHolder() {
        }

        private static FileSystem defaultFileSystem() {
            return ((FileSystemProvider) AccessController.doPrivileged(new PrivilegedAction<FileSystemProvider>() {
                public FileSystemProvider run() {
                    return DefaultFileSystemHolder.getDefaultProvider();
                }
            })).getFileSystem(URI.create("file:///"));
        }

        private static FileSystemProvider getDefaultProvider() {
            FileSystemProvider provider = DefaultFileSystemProvider.create();
            String propValue = System.getProperty("java.nio.file.spi.DefaultFileSystemProvider");
            if (propValue != null) {
                String[] split = propValue.split(",");
                int length = split.length;
                int i = 0;
                while (i < length) {
                    try {
                        provider = (FileSystemProvider) Class.forName(split[i], true, ClassLoader.getSystemClassLoader()).getDeclaredConstructor(FileSystemProvider.class).newInstance(provider);
                        if (provider.getScheme().equals("file")) {
                            i++;
                        } else {
                            throw new Error("Default provider must use scheme 'file'");
                        }
                    } catch (Throwable x) {
                        throw new Error(x);
                    }
                }
            }
            return provider;
        }
    }

    private FileSystems() {
    }

    public static FileSystem getDefault() {
        return DefaultFileSystemHolder.defaultFileSystem;
    }

    public static FileSystem getFileSystem(URI uri) {
        String scheme = uri.getScheme();
        for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if (scheme.equalsIgnoreCase(provider.getScheme())) {
                return provider.getFileSystem(uri);
            }
        }
        throw new ProviderNotFoundException("Provider \"" + scheme + "\" not found");
    }

    public static FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        return newFileSystem(uri, env, null);
    }

    public static FileSystem newFileSystem(URI uri, Map<String, ?> env, ClassLoader loader) throws IOException {
        String scheme = uri.getScheme();
        for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if (scheme.equalsIgnoreCase(provider.getScheme())) {
                return provider.newFileSystem(uri, (Map) env);
            }
        }
        if (loader != null) {
            for (FileSystemProvider provider2 : ServiceLoader.load(FileSystemProvider.class, loader)) {
                if (scheme.equalsIgnoreCase(provider2.getScheme())) {
                    return provider2.newFileSystem(uri, (Map) env);
                }
            }
        }
        throw new ProviderNotFoundException("Provider \"" + scheme + "\" not found");
    }

    public static FileSystem newFileSystem(Path path, ClassLoader loader) throws IOException {
        if (path == null) {
            throw new NullPointerException();
        }
        Map env = Collections.emptyMap();
        for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            try {
                return provider.newFileSystem(path, env);
            } catch (UnsupportedOperationException e) {
            }
        }
        if (loader != null) {
            for (FileSystemProvider provider2 : ServiceLoader.load(FileSystemProvider.class, loader)) {
                try {
                    return provider2.newFileSystem(path, env);
                } catch (UnsupportedOperationException e2) {
                }
            }
        }
        throw new ProviderNotFoundException("Provider not found");
    }
}
