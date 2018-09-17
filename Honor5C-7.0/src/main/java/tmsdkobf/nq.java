package tmsdkobf;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;

/* compiled from: Unknown */
final class nq implements np {
    private static Method CV;
    private static Method CW;
    private static Field CX;
    private static long CY;
    byte[] CZ;
    private ActivityManager mActivityManager;
    private Context mContext;
    private PackageManager mPackageManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.nq.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.nq.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.nq.<clinit>():void");
    }

    public nq(Context context) {
        this.CZ = new byte[IncomingSmsFilterConsts.PAY_SMS];
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
    }

    public long fu() {
        DataInputStream dataInputStream;
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        NumberFormatException e3;
        if (CY == -1) {
            File file = new File("/proc/meminfo");
            if (file.exists()) {
                try {
                    dataInputStream = new DataInputStream(new FileInputStream(file));
                    try {
                        String readLine = dataInputStream.readLine();
                        if (readLine != null) {
                            CY = Long.parseLong(readLine.trim().split("[\\s]+")[1]);
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e4) {
                                    e4.printStackTrace();
                                }
                            }
                        } else {
                            throw new IOException("/proc/meminfo is empty!");
                        }
                    } catch (FileNotFoundException e5) {
                        e2 = e5;
                        try {
                            e2.printStackTrace();
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e42) {
                                    e42.printStackTrace();
                                }
                            }
                            return (CY <= 0 ? null : 1) == null ? 1 : CY;
                        } catch (Throwable th2) {
                            th = th2;
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e6) {
                                    e6.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e7) {
                        e42 = e7;
                        e42.printStackTrace();
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException e422) {
                                e422.printStackTrace();
                            }
                        }
                        if (CY <= 0) {
                        }
                        if ((CY <= 0 ? null : 1) == null) {
                        }
                    } catch (NumberFormatException e8) {
                        e3 = e8;
                        e3.printStackTrace();
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException e4222) {
                                e4222.printStackTrace();
                            }
                        }
                        if (CY <= 0) {
                        }
                        if ((CY <= 0 ? null : 1) == null) {
                        }
                    }
                } catch (FileNotFoundException e9) {
                    e2 = e9;
                    dataInputStream = null;
                    e2.printStackTrace();
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    if (CY <= 0) {
                    }
                    if ((CY <= 0 ? null : 1) == null) {
                    }
                } catch (IOException e10) {
                    e4222 = e10;
                    dataInputStream = null;
                    e4222.printStackTrace();
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    if (CY <= 0) {
                    }
                    if ((CY <= 0 ? null : 1) == null) {
                    }
                } catch (NumberFormatException e11) {
                    e3 = e11;
                    dataInputStream = null;
                    e3.printStackTrace();
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    if (CY <= 0) {
                    }
                    if ((CY <= 0 ? null : 1) == null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    dataInputStream = null;
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    throw th;
                }
            }
        }
        if (CY <= 0) {
        }
        if ((CY <= 0 ? null : 1) == null) {
        }
    }
}
