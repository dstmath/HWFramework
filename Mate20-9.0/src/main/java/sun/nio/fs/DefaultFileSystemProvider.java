package sun.nio.fs;

import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import sun.security.action.GetPropertyAction;

public class DefaultFileSystemProvider {
    private DefaultFileSystemProvider() {
    }

    private static FileSystemProvider createProvider(String cn) {
        try {
            try {
                return (FileSystemProvider) Class.forName(cn).newInstance();
            } catch (IllegalAccessException | InstantiationException x) {
                throw new AssertionError((Object) x);
            }
        } catch (ClassNotFoundException x2) {
            throw new AssertionError((Object) x2);
        }
    }

    public static FileSystemProvider create() {
        String osname = (String) AccessController.doPrivileged(new GetPropertyAction("os.name"));
        if (osname.equals("SunOS")) {
            return createProvider("sun.nio.fs.SolarisFileSystemProvider");
        }
        if (osname.equals("Linux")) {
            return createProvider("sun.nio.fs.LinuxFileSystemProvider");
        }
        if (osname.contains("OS X")) {
            return createProvider("sun.nio.fs.MacOSXFileSystemProvider");
        }
        if (osname.equals("AIX")) {
            return createProvider("sun.nio.fs.AixFileSystemProvider");
        }
        throw new AssertionError((Object) "Platform not recognized");
    }
}
