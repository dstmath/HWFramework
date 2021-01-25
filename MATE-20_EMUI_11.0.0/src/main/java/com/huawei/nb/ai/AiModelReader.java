package com.huawei.nb.ai;

import com.huawei.nb.efs.EfsException;
import com.huawei.nb.efs.EfsRwChannel;
import com.huawei.nb.utils.logger.DSLog;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class AiModelReader {
    private static final int READ_NEW_WORKKEY_FAILURE = 3;
    private static final int READ_NEW_WORKKEY_SUCCESS = 1;
    private static final int READ_OLD_WORKKEY_EXCEPTION = 2;
    private static final int READ_OLD_WORKKEY_SUCCESS = 0;

    public interface ReadCallback {
        byte[] efsReloadKey();
    }

    private AiModelReader() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x002e  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0033 A[SYNTHETIC, Splitter:B:25:0x0033] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0044 A[SYNTHETIC, Splitter:B:33:0x0044] */
    public static ByteBuffer readAiModel(String str, byte[] bArr) {
        EfsRwChannel efsRwChannel;
        Throwable th;
        try {
            efsRwChannel = EfsRwChannel.open(str, 257, bArr);
            try {
                ByteBuffer read = efsRwChannel.read();
                if (bArr != null) {
                    Arrays.fill(bArr, (byte) 0);
                }
                if (efsRwChannel != null) {
                    try {
                        efsRwChannel.close();
                    } catch (EfsException unused) {
                        DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                    }
                }
                return read;
            } catch (EfsException | UnsatisfiedLinkError unused2) {
                try {
                    DSLog.e("Failed to read AI model with UnsatisfiedLinkError.", new Object[0]);
                    if (bArr != null) {
                        Arrays.fill(bArr, (byte) 0);
                    }
                    if (efsRwChannel != null) {
                        try {
                            efsRwChannel.close();
                        } catch (EfsException unused3) {
                            DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (bArr != null) {
                    }
                    if (efsRwChannel != null) {
                    }
                    throw th;
                }
            }
        } catch (EfsException | UnsatisfiedLinkError unused4) {
            efsRwChannel = null;
            DSLog.e("Failed to read AI model with UnsatisfiedLinkError.", new Object[0]);
            if (bArr != null) {
            }
            if (efsRwChannel != null) {
            }
            return null;
        } catch (Throwable th3) {
            th = th3;
            efsRwChannel = null;
            if (bArr != null) {
                Arrays.fill(bArr, (byte) 0);
            }
            if (efsRwChannel != null) {
                try {
                    efsRwChannel.close();
                } catch (EfsException unused5) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x006d A[SYNTHETIC, Splitter:B:42:0x006d] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0087 A[SYNTHETIC, Splitter:B:54:0x0087] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x009d A[SYNTHETIC, Splitter:B:64:0x009d] */
    public static int tryReadWorkKey(String str, byte[] bArr, ReadCallback readCallback) {
        Throwable th;
        EfsRwChannel efsRwChannel;
        EfsException e;
        byte[] efsReloadKey;
        try {
            efsRwChannel = EfsRwChannel.open(str, 257, bArr);
            try {
                efsRwChannel.read();
                if (bArr != null) {
                    Arrays.fill(bArr, (byte) 0);
                }
                if (efsRwChannel != null) {
                    try {
                        efsRwChannel.close();
                    } catch (EfsException unused) {
                        DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                    }
                }
                return 0;
            } catch (UnsatisfiedLinkError unused2) {
                DSLog.e("tryReadWorkKey Failed to read AI model with UnsatisfiedLinkError.", new Object[0]);
                if (bArr != null) {
                }
                if (efsRwChannel != null) {
                }
                return 2;
            } catch (EfsException e2) {
                e = e2;
                try {
                    DSLog.e("Try to rekey after efs exception happened. error: %s", e.getMessage());
                    efsReloadKey = readCallback.efsReloadKey();
                    if (efsReloadKey != null) {
                    }
                    DSLog.e("Failed to rekey with efs exception.", new Object[0]);
                    if (bArr != null) {
                    }
                    if (efsReloadKey != null) {
                    }
                    if (efsRwChannel != null) {
                    }
                    return 3;
                } catch (Throwable th2) {
                    th = th2;
                    if (bArr != null) {
                    }
                    if (0 != 0) {
                    }
                    if (efsRwChannel != null) {
                    }
                    throw th;
                }
            }
        } catch (UnsatisfiedLinkError unused3) {
            efsRwChannel = null;
            DSLog.e("tryReadWorkKey Failed to read AI model with UnsatisfiedLinkError.", new Object[0]);
            if (bArr != null) {
                Arrays.fill(bArr, (byte) 0);
            }
            if (efsRwChannel != null) {
                try {
                    efsRwChannel.close();
                } catch (EfsException unused4) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
            return 2;
        } catch (EfsException e3) {
            e = e3;
            efsRwChannel = null;
            DSLog.e("Try to rekey after efs exception happened. error: %s", e.getMessage());
            efsReloadKey = readCallback.efsReloadKey();
            if (efsReloadKey != null || efsReloadKey.length <= 0) {
                DSLog.e("Failed to rekey with efs exception.", new Object[0]);
                if (bArr != null) {
                    Arrays.fill(bArr, (byte) 0);
                }
                if (efsReloadKey != null) {
                    Arrays.fill(efsReloadKey, (byte) 0);
                }
                if (efsRwChannel != null) {
                    try {
                        efsRwChannel.close();
                    } catch (EfsException unused5) {
                        DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                    }
                }
                return 3;
            }
            readAiModel(str, efsReloadKey);
            if (bArr != null) {
                Arrays.fill(bArr, (byte) 0);
            }
            if (efsReloadKey != null) {
                Arrays.fill(efsReloadKey, (byte) 0);
            }
            if (efsRwChannel != null) {
                try {
                    efsRwChannel.close();
                } catch (EfsException unused6) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
            return 1;
        } catch (Throwable th3) {
            th = th3;
            efsRwChannel = null;
            if (bArr != null) {
                Arrays.fill(bArr, (byte) 0);
            }
            if (0 != 0) {
                Arrays.fill((byte[]) null, (byte) 0);
            }
            if (efsRwChannel != null) {
                try {
                    efsRwChannel.close();
                } catch (EfsException unused7) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
            throw th;
        }
    }
}
