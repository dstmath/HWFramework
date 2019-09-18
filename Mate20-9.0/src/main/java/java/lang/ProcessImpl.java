package java.lang;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.Map;

final class ProcessImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    private ProcessImpl() {
    }

    private static byte[] toCString(String s) {
        if (s == null) {
            return null;
        }
        byte[] bytes = s.getBytes();
        byte[] result = new byte[(bytes.length + 1)];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        result[result.length - 1] = 0;
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x0174 A[SYNTHETIC, Splitter:B:105:0x0174] */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0182  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0158 A[SYNTHETIC, Splitter:B:89:0x0158] */
    static Process start(String[] cmdarray, Map<String, String> environment, String dir, ProcessBuilder.Redirect[] redirects, boolean redirectErrorStream) throws IOException {
        int[] std_fds;
        String[] strArr = cmdarray;
        byte[][] args = new byte[(strArr.length - 1)][];
        int size = args.length;
        for (int i = 0; i < args.length; i++) {
            args[i] = strArr[i + 1].getBytes();
            size += args[i].length;
        }
        byte[] argBlock = new byte[size];
        int i2 = 0;
        for (byte[] arg : args) {
            System.arraycopy(arg, 0, argBlock, i2, arg.length);
            i2 += arg.length + 1;
        }
        int[] envc = new int[1];
        byte[] envBlock = ProcessEnvironment.toEnvironmentBlock(environment, envc);
        FileInputStream f0 = null;
        FileOutputStream f1 = null;
        FileOutputStream f2 = null;
        if (redirects == null) {
            try {
                std_fds = new int[]{-1, -1, -1};
            } catch (Throwable th) {
                th = th;
                int[] iArr = envc;
                int i3 = i2;
                byte[] bArr = argBlock;
            }
        } else {
            try {
                std_fds = new int[3];
                if (redirects[0] == ProcessBuilder.Redirect.PIPE) {
                    std_fds[0] = -1;
                } else if (redirects[0] == ProcessBuilder.Redirect.INHERIT) {
                    std_fds[0] = 0;
                } else {
                    f0 = new FileInputStream(redirects[0].file());
                    std_fds[0] = f0.getFD().getInt$();
                }
                if (redirects[1] == ProcessBuilder.Redirect.PIPE) {
                    std_fds[1] = -1;
                } else if (redirects[1] == ProcessBuilder.Redirect.INHERIT) {
                    std_fds[1] = 1;
                } else {
                    f1 = new FileOutputStream(redirects[1].file(), redirects[1].append());
                    std_fds[1] = f1.getFD().getInt$();
                }
                if (redirects[2] == ProcessBuilder.Redirect.PIPE) {
                    std_fds[2] = -1;
                } else if (redirects[2] == ProcessBuilder.Redirect.INHERIT) {
                    std_fds[2] = 2;
                } else {
                    f2 = new FileOutputStream(redirects[2].file(), redirects[2].append());
                    std_fds[2] = f2.getFD().getInt$();
                }
            } catch (Throwable th2) {
                th = th2;
                int[] iArr2 = envc;
                int i4 = i2;
                byte[] bArr2 = argBlock;
            }
        }
        int[] std_fds2 = std_fds;
        FileInputStream f02 = f0;
        FileOutputStream f12 = f1;
        FileOutputStream f22 = f2;
        try {
            byte[] cString = toCString(strArr[0]);
            r7 = r7;
            FileOutputStream f23 = f22;
            FileOutputStream f13 = f12;
            int[] iArr3 = envc;
            int i5 = i2;
            byte[] bArr3 = argBlock;
            try {
                UNIXProcess uNIXProcess = new UNIXProcess(cString, argBlock, args.length, envBlock, envc[0], toCString(dir), std_fds2, redirectErrorStream);
                if (f02 != null) {
                    try {
                        f02.close();
                    } catch (Throwable th3) {
                        if (f23 != null) {
                            f23.close();
                        }
                        throw th3;
                    }
                }
                if (f13 != null) {
                    try {
                        f13.close();
                    } catch (Throwable th4) {
                        if (f23 != null) {
                            f23.close();
                        }
                        throw th4;
                    }
                }
                if (f23 != null) {
                    f23.close();
                }
                return uNIXProcess;
            } catch (Throwable th5) {
                th = th5;
                f2 = f23;
                f1 = f13;
                f0 = f02;
                if (f0 != null) {
                }
                if (f1 != null) {
                }
                if (f2 != null) {
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            int[] iArr4 = envc;
            int i6 = i2;
            byte[] bArr4 = argBlock;
            f2 = f22;
            f1 = f12;
            f0 = f02;
            if (f0 != null) {
                try {
                    f0.close();
                } catch (Throwable th7) {
                    if (f2 != null) {
                        f2.close();
                    }
                    throw th7;
                }
            }
            if (f1 != null) {
                try {
                    f1.close();
                } catch (Throwable th8) {
                    if (f2 != null) {
                        f2.close();
                    }
                    throw th8;
                }
            }
            if (f2 != null) {
                f2.close();
            }
            throw th;
        }
    }
}
