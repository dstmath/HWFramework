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

    public static ByteBuffer readAiModel(String filePath, byte[] key) {
        ByteBuffer byteBuffer;
        EfsRwChannel channel = null;
        try {
            channel = EfsRwChannel.open(filePath, 257, key);
            byteBuffer = channel.read();
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (EfsException e) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
        } catch (UnsatisfiedLinkError e2) {
            UnsatisfiedLinkError unsatisfiedLinkError = e2;
        } catch (EfsException e3) {
            EfsException efsException = e3;
        }
        return byteBuffer;
        try {
            DSLog.e("Failed to read AI model with UnsatisfiedLinkError.", new Object[0]);
            byteBuffer = null;
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (EfsException e4) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
            return byteBuffer;
        } catch (Throwable th) {
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (EfsException e5) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
            throw th;
        }
    }

    public static int tryReadWorkKey(String filePath, byte[] key, ReadCallback callback) {
        EfsRwChannel channel = null;
        byte[] newKey = null;
        try {
            channel = EfsRwChannel.open(filePath, 257, key);
            channel.read();
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
            if (0 != 0) {
                Arrays.fill(null, (byte) 0);
            }
            if (channel == null) {
                return 0;
            }
            try {
                channel.close();
                return 0;
            } catch (EfsException e) {
                DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                return 0;
            }
        } catch (UnsatisfiedLinkError e2) {
            DSLog.e("tryReadWorkKey Failed to read AI model with UnsatisfiedLinkError.", new Object[0]);
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
            if (0 != 0) {
                Arrays.fill(null, (byte) 0);
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (EfsException e3) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
            return 2;
        } catch (EfsException e4) {
            DSLog.e("Try to rekey after efs exception happened. error: %s", e4.getMessage());
            newKey = callback.efsReloadKey();
            if (newKey == null || newKey.length <= 0) {
                DSLog.e("Failed to rekey with efs exception.", new Object[0]);
                if (key != null) {
                    Arrays.fill(key, (byte) 0);
                }
                if (newKey != null) {
                    Arrays.fill(newKey, (byte) 0);
                }
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (EfsException e5) {
                        DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                    }
                }
                return 3;
            }
            readAiModel(filePath, newKey);
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
            if (newKey != null) {
                Arrays.fill(newKey, (byte) 0);
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (EfsException e6) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
            return 1;
        } catch (Throwable th) {
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
            if (newKey != null) {
                Arrays.fill(newKey, (byte) 0);
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (EfsException e7) {
                    DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
                }
            }
            throw th;
        }
    }
}
