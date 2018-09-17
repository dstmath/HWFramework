package java.lang;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;

final class ProcessImpl {
    static final /* synthetic */ boolean -assertionsDisabled = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ProcessImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ProcessImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.ProcessImpl.<clinit>():void");
    }

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

    static Process start(String[] cmdarray, Map<String, String> environment, String dir, Redirect[] redirects, boolean redirectErrorStream) throws IOException {
        int i;
        int[] std_fds;
        if (!-assertionsDisabled) {
            Object obj = (cmdarray == null || cmdarray.length <= 0) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
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
        FileInputStream fileInputStream = null;
        FileOutputStream f1 = null;
        FileOutputStream f2 = null;
        if (redirects == null) {
            try {
                std_fds = new int[]{-1, -1, -1};
            } catch (Throwable th) {
                th = th;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th2) {
                        if (f2 != null) {
                            f2.close();
                        }
                    }
                }
                if (f1 != null) {
                    try {
                        f1.close();
                    } catch (Throwable th3) {
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
        FileOutputStream fileOutputStream;
        std_fds = new int[3];
        if (redirects[0] == Redirect.PIPE) {
            std_fds[0] = -1;
        } else if (redirects[0] == Redirect.INHERIT) {
            std_fds[0] = 0;
        } else {
            FileInputStream f0 = new FileInputStream(redirects[0].file());
            try {
                std_fds[0] = f0.getFD().getInt$();
                fileInputStream = f0;
            } catch (Throwable th4) {
                Throwable th5;
                th5 = th4;
                fileInputStream = f0;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (f1 != null) {
                    f1.close();
                }
                if (f2 != null) {
                    f2.close();
                }
                throw th5;
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
            } catch (Throwable th6) {
                th5 = th6;
                f1 = fileOutputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (f1 != null) {
                    f1.close();
                }
                if (f2 != null) {
                    f2.close();
                }
                throw th5;
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
            } catch (Throwable th7) {
                th5 = th7;
                f2 = fileOutputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (f1 != null) {
                    f1.close();
                }
                if (f2 != null) {
                    f2.close();
                }
                throw th5;
            }
        }
        Process uNIXProcess = new UNIXProcess(toCString(cmdarray[0]), argBlock, args.length, envBlock, envc[0], toCString(dir), std_fds, redirectErrorStream);
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
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
}
