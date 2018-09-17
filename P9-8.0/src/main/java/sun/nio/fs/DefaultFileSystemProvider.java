package sun.nio.fs;

import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import sun.security.action.GetPropertyAction;

public class DefaultFileSystemProvider {
    private DefaultFileSystemProvider() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0012 A:{Splitter: B:2:0x0004, ExcHandler: java.lang.IllegalAccessException (r2_0 'x' java.lang.Object)} */
    /* JADX WARNING: Missing block: B:8:0x0012, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0018, code:
            throw new java.lang.AssertionError(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static FileSystemProvider createProvider(String cn) {
        try {
            try {
                return (FileSystemProvider) Class.forName(cn).newInstance();
            } catch (Object x) {
            }
        } catch (Object x2) {
            throw new AssertionError(x2);
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
