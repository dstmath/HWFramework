package java.lang;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;

final class ProcessImpl {
    static final /* synthetic */ boolean -assertionsDisabled = (ProcessImpl.class.desiredAssertionStatus() ^ 1);

    private ProcessImpl() {
    }

    private static byte[] toCString(String s) {
        if (s == null) {
            return null;
        }
        byte[] bytes = s.getBytes();
        byte[] result = new byte[(bytes.length + 1)];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        result[result.length - 1] = (byte) 0;
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00b3 A:{SYNTHETIC, Splitter: B:42:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00b8 A:{SYNTHETIC, Splitter: B:45:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00b3 A:{SYNTHETIC, Splitter: B:42:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00b8 A:{SYNTHETIC, Splitter: B:45:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00b3 A:{SYNTHETIC, Splitter: B:42:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00b8 A:{SYNTHETIC, Splitter: B:45:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00bd  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static Process start(String[] cmdarray, Map<String, String> environment, String dir, Redirect[] redirects, boolean redirectErrorStream) throws IOException {
        Throwable th;
        if (-assertionsDisabled || (cmdarray != null && cmdarray.length > 0)) {
            int i;
            int[] std_fds;
            byte[][] args = new byte[(cmdarray.length - 1)][];
            int size = args.length;
            for (i = 0; i < args.length; i++) {
                args[i] = cmdarray[i + 1].getBytes();
                size += args[i].length;
            }
            byte[] argBlock = new byte[size];
            i = 0;
            for (byte[] arg : args) {
                System.arraycopy(arg, 0, argBlock, i, arg.length);
                i += arg.length + 1;
            }
            int[] envc = new int[1];
            byte[] envBlock = ProcessEnvironment.toEnvironmentBlock(environment, envc);
            FileInputStream f0 = null;
            FileOutputStream f1 = null;
            FileOutputStream f2 = null;
            if (redirects == null) {
                try {
                    std_fds = new int[]{-1, -1, -1};
                } catch (Throwable th2) {
                    th = th2;
                    if (f0 != null) {
                    }
                    if (f1 != null) {
                    }
                    if (f2 != null) {
                    }
                    throw th;
                }
            }
            FileOutputStream fileOutputStream;
            std_fds = new int[3];
            if (redirects[0] == Redirect.PIPE) {
                std_fds[0] = -1;
            } else if (redirects[0] == Redirect.INHERIT) {
                std_fds[0] = 0;
            } else {
                FileInputStream f02 = new FileInputStream(redirects[0].file());
                try {
                    std_fds[0] = f02.getFD().getInt$();
                    f0 = f02;
                } catch (Throwable th3) {
                    th = th3;
                    f0 = f02;
                    if (f0 != null) {
                    }
                    if (f1 != null) {
                    }
                    if (f2 != null) {
                    }
                    throw th;
                }
            }
            if (redirects[1] == Redirect.PIPE) {
                std_fds[1] = -1;
            } else if (redirects[1] == Redirect.INHERIT) {
                std_fds[1] = 1;
            } else {
                fileOutputStream = new FileOutputStream(redirects[1].file(), redirects[1].append());
                try {
                    std_fds[1] = fileOutputStream.getFD().getInt$();
                    f1 = fileOutputStream;
                } catch (Throwable th4) {
                    th = th4;
                    f1 = fileOutputStream;
                    if (f0 != null) {
                    }
                    if (f1 != null) {
                    }
                    if (f2 != null) {
                    }
                    throw th;
                }
            }
            if (redirects[2] == Redirect.PIPE) {
                std_fds[2] = -1;
            } else if (redirects[2] == Redirect.INHERIT) {
                std_fds[2] = 2;
            } else {
                fileOutputStream = new FileOutputStream(redirects[2].file(), redirects[2].append());
                try {
                    std_fds[2] = fileOutputStream.getFD().getInt$();
                    f2 = fileOutputStream;
                } catch (Throwable th5) {
                    th = th5;
                    f2 = fileOutputStream;
                    if (f0 != null) {
                        try {
                            f0.close();
                        } catch (Throwable th6) {
                            if (f2 != null) {
                                f2.close();
                            }
                        }
                    }
                    if (f1 != null) {
                        try {
                            f1.close();
                        } catch (Throwable th7) {
                            if (f2 != null) {
                                f2.close();
                            }
                        }
                    }
                    if (f2 != null) {
                        f2.close();
                    }
                    throw th;
                }
            }
            Process uNIXProcess = new UNIXProcess(toCString(cmdarray[0]), argBlock, args.length, envBlock, envc[0], toCString(dir), std_fds, redirectErrorStream);
            if (f0 != null) {
                try {
                    f0.close();
                } catch (Throwable th8) {
                    if (f2 != null) {
                        f2.close();
                    }
                }
            }
            if (f1 != null) {
                try {
                    f1.close();
                } catch (Throwable th9) {
                    if (f2 != null) {
                        f2.close();
                    }
                }
            }
            if (f2 != null) {
                f2.close();
            }
            return uNIXProcess;
        }
        throw new AssertionError();
    }
}
