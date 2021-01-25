package com.huawei.security.keystore;

import android.os.IBinder;
import android.util.Pair;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keymaster.HwOperationResult;
import com.huawei.security.keystore.ArrayUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.ProviderException;

public class HwUniversalKeyStoreCryptoOperationStreamer {
    private static final int DEFAULT_MAX_CHUNK_SIZE = 65536;
    private byte[] mBuffered;
    private int mBufferedLength;
    private int mBufferedOffset;
    private long mConsumedInputSizeBytes;
    private final Stream mKeyStoreStream;
    private final int mMaxChunkSize;
    private long mProducedOutputSizeBytes;

    /* access modifiers changed from: package-private */
    public interface Stream {
        HwOperationResult finish(byte[] bArr, byte[] bArr2);

        HwOperationResult update(byte[] bArr);
    }

    public HwUniversalKeyStoreCryptoOperationStreamer(Stream operation) {
        this(operation, 65536);
    }

    public HwUniversalKeyStoreCryptoOperationStreamer(Stream operation, int maxChunkSize) {
        this.mBuffered = ArrayUtils.EmptyArray.BYTE;
        this.mBufferedOffset = 0;
        this.mBufferedLength = 0;
        this.mKeyStoreStream = operation;
        this.mMaxChunkSize = maxChunkSize;
    }

    public byte[] update(byte[] input, int inputOffset, int inputLength) throws HwUniversalKeyStoreException {
        byte[] result;
        int remainLength = inputLength;
        int offset = inputOffset;
        if (remainLength == 0) {
            return ArrayUtils.EmptyArray.BYTE;
        }
        ByteArrayOutputStream bufferedOutput = null;
        while (remainLength > 0) {
            Pair<byte[], Integer> chunkInfo = getAdjustChunk(input, offset, remainLength);
            byte[] chunk = (byte[]) chunkInfo.first;
            int inputBytesInChunk = ((Integer) chunkInfo.second).intValue();
            offset += inputBytesInChunk;
            remainLength -= inputBytesInChunk;
            this.mConsumedInputSizeBytes += (long) inputBytesInChunk;
            HwOperationResult opResult = this.mKeyStoreStream.update(chunk);
            if (opResult == null) {
                throw new ProviderException("Failed to communicate with keystore service");
            } else if (opResult.resultCode == 1) {
                tryUpdateCurrentBuffer(chunk, opResult, remainLength);
                if (opResult.output != null && opResult.output.length > 0) {
                    if (remainLength <= 0) {
                        if (bufferedOutput == null) {
                            result = opResult.output;
                        } else {
                            updateBufferOutput(opResult, bufferedOutput);
                            result = bufferedOutput.toByteArray();
                        }
                        this.mProducedOutputSizeBytes += (long) result.length;
                        return result;
                    } else if (bufferedOutput == null) {
                        bufferedOutput = new ByteArrayOutputStream();
                        updateBufferOutput(opResult, bufferedOutput);
                    }
                }
            } else {
                throw HwKeystoreManager.getKeyStoreException(opResult.resultCode);
            }
        }
        byte[] result2 = bufferedOutput == null ? ArrayUtils.EmptyArray.BYTE : bufferedOutput.toByteArray();
        this.mProducedOutputSizeBytes += (long) result2.length;
        return result2;
    }

    private void updateBufferOutput(HwOperationResult opResult, ByteArrayOutputStream bufferedOutput) {
        try {
            bufferedOutput.write(opResult.output);
        } catch (IOException e) {
            throw new ProviderException("Failed to buffer output", e);
        }
    }

    private Pair<byte[], Integer> getAdjustChunk(byte[] input, int offset, int remainLength) {
        int inputBytesInChunk;
        byte[] chunk;
        ArrayUtils.CopyArray bufferArray = new ArrayUtils.CopyArray(this.mBuffered, this.mBufferedOffset, this.mBufferedLength);
        int i = this.mBufferedLength;
        int i2 = i + remainLength;
        int i3 = this.mMaxChunkSize;
        if (i2 > i3) {
            inputBytesInChunk = i3 - i;
            chunk = ArrayUtils.concat(bufferArray, new ArrayUtils.CopyArray(input, offset, inputBytesInChunk));
        } else if (i == 0 && offset == 0 && remainLength == input.length) {
            chunk = input;
            inputBytesInChunk = input.length;
        } else {
            inputBytesInChunk = remainLength;
            chunk = ArrayUtils.concat(bufferArray, new ArrayUtils.CopyArray(input, offset, inputBytesInChunk));
        }
        return new Pair<>(chunk, Integer.valueOf(inputBytesInChunk));
    }

    private void tryUpdateCurrentBuffer(byte[] chunk, HwOperationResult opResult, int remainLength) throws HwUniversalKeyStoreException {
        if (opResult.inputConsumed == chunk.length) {
            this.mBuffered = ArrayUtils.EmptyArray.BYTE;
            this.mBufferedOffset = 0;
            this.mBufferedLength = 0;
        } else if (opResult.inputConsumed <= 0) {
            if (remainLength <= 0) {
                this.mBuffered = chunk;
                this.mBufferedOffset = 0;
                this.mBufferedLength = chunk.length;
                return;
            }
            throw new HwUniversalKeyStoreException(HwKeymasterDefs.KM_ERROR_UNKNOWN_ERROR, "Keystore consumed nothing from max-sized chunk: " + chunk.length + " bytes");
        } else if (opResult.inputConsumed < chunk.length) {
            this.mBuffered = chunk;
            this.mBufferedOffset = opResult.inputConsumed;
            this.mBufferedLength = chunk.length - opResult.inputConsumed;
        } else {
            throw new HwUniversalKeyStoreException(HwKeymasterDefs.KM_ERROR_UNKNOWN_ERROR, "Keystore consumed more input than provided. Provided: " + chunk.length + ", consumed: " + opResult.inputConsumed);
        }
    }

    public byte[] doFinal(byte[] input, int inputOffset, int inputLength, byte[] signature, byte[] additionalEntropy) throws HwUniversalKeyStoreException {
        byte[] updateBytes = input;
        int offset = inputOffset;
        if (inputLength == 0) {
            updateBytes = ArrayUtils.EmptyArray.BYTE;
            offset = 0;
        }
        byte[] output = ArrayUtils.concat(update(updateBytes, offset, inputLength), flush());
        HwOperationResult opResult = this.mKeyStoreStream.finish(signature, additionalEntropy);
        if (opResult == null) {
            throw new ProviderException("Failed to communicate with keystore service");
        } else if (opResult.resultCode == 1) {
            this.mProducedOutputSizeBytes += (long) opResult.output.length;
            return ArrayUtils.concat(output, opResult.output);
        } else {
            throw HwKeystoreManager.getKeyStoreException(opResult.resultCode);
        }
    }

    public byte[] flush() throws HwUniversalKeyStoreException {
        String str;
        if (this.mBufferedLength <= 0) {
            return ArrayUtils.EmptyArray.BYTE;
        }
        ByteArrayOutputStream bufferedOutput = null;
        while (true) {
            int i = this.mBufferedLength;
            if (i <= 0) {
                break;
            }
            byte[] chunk = ArrayUtils.subArray(this.mBuffered, this.mBufferedOffset, i);
            HwOperationResult opResult = this.mKeyStoreStream.update(chunk);
            if (opResult == null) {
                throw new ProviderException("Failed to communicate with keystore service");
            } else if (opResult.resultCode != 1) {
                throw HwKeystoreManager.getKeyStoreException(opResult.resultCode);
            } else if (opResult.inputConsumed <= 0) {
                break;
            } else {
                tryUpdateCurrentBuffer(opResult, chunk);
                if (opResult.output != null && opResult.output.length > 0) {
                    if (bufferedOutput == null) {
                        if (this.mBufferedLength == 0) {
                            this.mProducedOutputSizeBytes += (long) opResult.output.length;
                            return opResult.output;
                        }
                        bufferedOutput = new ByteArrayOutputStream();
                    }
                    try {
                        bufferedOutput.write(opResult.output);
                    } catch (IOException e) {
                        throw new ProviderException("Failed to buffer output", e);
                    }
                }
            }
        }
        if (this.mBufferedLength > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Keystore failed to consume last ");
            if (this.mBufferedLength != 1) {
                str = this.mBufferedLength + " bytes";
            } else {
                str = "byte";
            }
            sb.append(str);
            sb.append(" of input");
            throw new HwUniversalKeyStoreException(HwKeymasterDefs.KM_ERROR_UNKNOWN_ERROR, sb.toString());
        }
        byte[] result = bufferedOutput != null ? bufferedOutput.toByteArray() : ArrayUtils.EmptyArray.BYTE;
        this.mProducedOutputSizeBytes += (long) result.length;
        return result;
    }

    private void tryUpdateCurrentBuffer(HwOperationResult opResult, byte[] chunk) throws HwUniversalKeyStoreException {
        if (opResult.inputConsumed >= chunk.length) {
            this.mBuffered = ArrayUtils.EmptyArray.BYTE;
            this.mBufferedOffset = 0;
            this.mBufferedLength = 0;
        } else {
            this.mBuffered = chunk;
            this.mBufferedOffset = opResult.inputConsumed;
            this.mBufferedLength = chunk.length - opResult.inputConsumed;
        }
        if (opResult.inputConsumed > chunk.length) {
            throw new HwUniversalKeyStoreException(HwKeymasterDefs.KM_ERROR_UNKNOWN_ERROR, "Keystore consumed more input than provided. Provided: " + chunk.length + ", consumed: " + opResult.inputConsumed);
        }
    }

    public long getConsumedInputSizeBytes() {
        return this.mConsumedInputSizeBytes;
    }

    public long getProducedOutputSizeBytes() {
        return this.mProducedOutputSizeBytes;
    }

    public static class MainDataStream implements Stream {
        private final HwKeystoreManager mKeyStore;
        private final IBinder mOperationToken;

        public MainDataStream(HwKeystoreManager keyStore, IBinder operationToken) {
            this.mKeyStore = keyStore;
            this.mOperationToken = operationToken;
        }

        @Override // com.huawei.security.keystore.HwUniversalKeyStoreCryptoOperationStreamer.Stream
        public HwOperationResult update(byte[] input) {
            return this.mKeyStore.update(this.mOperationToken, null, input);
        }

        @Override // com.huawei.security.keystore.HwUniversalKeyStoreCryptoOperationStreamer.Stream
        public HwOperationResult finish(byte[] signature, byte[] additionalEntropy) {
            return this.mKeyStore.finish(this.mOperationToken, null, signature, additionalEntropy);
        }
    }
}
