package android.security.keystore;

import android.os.IBinder;
import android.security.KeyStore;
import android.security.KeyStoreException;
import android.security.keymaster.OperationResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.ProviderException;
import libcore.util.EmptyArray;

/* access modifiers changed from: package-private */
public class KeyStoreCryptoOperationChunkedStreamer implements KeyStoreCryptoOperationStreamer {
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
        OperationResult finish(byte[] bArr, byte[] bArr2);

        OperationResult update(byte[] bArr);
    }

    public KeyStoreCryptoOperationChunkedStreamer(Stream operation) {
        this(operation, 65536);
    }

    public KeyStoreCryptoOperationChunkedStreamer(Stream operation, int maxChunkSize) {
        this.mBuffered = EmptyArray.BYTE;
        this.mKeyStoreStream = operation;
        this.mMaxChunkSize = maxChunkSize;
    }

    @Override // android.security.keystore.KeyStoreCryptoOperationStreamer
    public byte[] update(byte[] input, int inputOffset, int inputLength) throws KeyStoreException {
        byte[] result;
        int inputBytesInChunk;
        byte[] chunk;
        byte[] result2;
        if (inputLength == 0) {
            return EmptyArray.BYTE;
        }
        ByteArrayOutputStream bufferedOutput = null;
        while (inputLength > 0) {
            int i = this.mBufferedLength;
            int i2 = i + inputLength;
            int i3 = this.mMaxChunkSize;
            if (i2 > i3) {
                inputBytesInChunk = i3 - i;
                chunk = ArrayUtils.concat(this.mBuffered, this.mBufferedOffset, i, input, inputOffset, inputBytesInChunk);
            } else if (i == 0 && inputOffset == 0 && inputLength == input.length) {
                chunk = input;
                inputBytesInChunk = input.length;
            } else {
                chunk = ArrayUtils.concat(this.mBuffered, this.mBufferedOffset, this.mBufferedLength, input, inputOffset, inputLength);
                inputBytesInChunk = inputLength;
            }
            inputOffset += inputBytesInChunk;
            inputLength -= inputBytesInChunk;
            this.mConsumedInputSizeBytes += (long) inputBytesInChunk;
            OperationResult opResult = this.mKeyStoreStream.update(chunk);
            if (opResult == null) {
                throw new KeyStoreConnectException();
            } else if (opResult.resultCode == 1) {
                if (opResult.inputConsumed == chunk.length) {
                    this.mBuffered = EmptyArray.BYTE;
                    this.mBufferedOffset = 0;
                    this.mBufferedLength = 0;
                } else if (opResult.inputConsumed <= 0) {
                    if (inputLength <= 0) {
                        this.mBuffered = chunk;
                        this.mBufferedOffset = 0;
                        this.mBufferedLength = chunk.length;
                    } else {
                        throw new KeyStoreException(-1000, "Keystore consumed nothing from max-sized chunk: " + chunk.length + " bytes");
                    }
                } else if (opResult.inputConsumed < chunk.length) {
                    this.mBuffered = chunk;
                    this.mBufferedOffset = opResult.inputConsumed;
                    this.mBufferedLength = chunk.length - opResult.inputConsumed;
                } else {
                    throw new KeyStoreException(-1000, "Keystore consumed more input than provided. Provided: " + chunk.length + ", consumed: " + opResult.inputConsumed);
                }
                if (opResult.output != null && opResult.output.length > 0) {
                    if (this.mBufferedLength + inputLength > 0) {
                        if (bufferedOutput == null) {
                            bufferedOutput = new ByteArrayOutputStream();
                        }
                        try {
                            bufferedOutput.write(opResult.output);
                        } catch (IOException e) {
                            throw new ProviderException("Failed to buffer output", e);
                        }
                    } else {
                        if (bufferedOutput == null) {
                            result2 = opResult.output;
                        } else {
                            try {
                                bufferedOutput.write(opResult.output);
                                result2 = bufferedOutput.toByteArray();
                            } catch (IOException e2) {
                                throw new ProviderException("Failed to buffer output", e2);
                            }
                        }
                        this.mProducedOutputSizeBytes += (long) result2.length;
                        return result2;
                    }
                }
            } else {
                throw KeyStore.getKeyStoreException(opResult.resultCode);
            }
        }
        if (bufferedOutput == null) {
            result = EmptyArray.BYTE;
        } else {
            result = bufferedOutput.toByteArray();
        }
        this.mProducedOutputSizeBytes += (long) result.length;
        return result;
    }

    @Override // android.security.keystore.KeyStoreCryptoOperationStreamer
    public byte[] doFinal(byte[] input, int inputOffset, int inputLength, byte[] signature, byte[] additionalEntropy) throws KeyStoreException {
        if (inputLength == 0) {
            input = EmptyArray.BYTE;
            inputOffset = 0;
        }
        byte[] output = ArrayUtils.concat(update(input, inputOffset, inputLength), flush());
        OperationResult opResult = this.mKeyStoreStream.finish(signature, additionalEntropy);
        if (opResult == null) {
            throw new KeyStoreConnectException();
        } else if (opResult.resultCode == 1) {
            this.mProducedOutputSizeBytes += (long) opResult.output.length;
            return ArrayUtils.concat(output, opResult.output);
        } else {
            throw KeyStore.getKeyStoreException(opResult.resultCode);
        }
    }

    public byte[] flush() throws KeyStoreException {
        String str;
        if (this.mBufferedLength <= 0) {
            return EmptyArray.BYTE;
        }
        ByteArrayOutputStream bufferedOutput = null;
        while (true) {
            int i = this.mBufferedLength;
            if (i <= 0) {
                break;
            }
            byte[] chunk = ArrayUtils.subarray(this.mBuffered, this.mBufferedOffset, i);
            OperationResult opResult = this.mKeyStoreStream.update(chunk);
            if (opResult == null) {
                throw new KeyStoreConnectException();
            } else if (opResult.resultCode != 1) {
                throw KeyStore.getKeyStoreException(opResult.resultCode);
            } else if (opResult.inputConsumed <= 0) {
                break;
            } else {
                if (opResult.inputConsumed >= chunk.length) {
                    this.mBuffered = EmptyArray.BYTE;
                    this.mBufferedOffset = 0;
                    this.mBufferedLength = 0;
                } else {
                    this.mBuffered = chunk;
                    this.mBufferedOffset = opResult.inputConsumed;
                    this.mBufferedLength = chunk.length - opResult.inputConsumed;
                }
                if (opResult.inputConsumed > chunk.length) {
                    throw new KeyStoreException(-1000, "Keystore consumed more input than provided. Provided: " + chunk.length + ", consumed: " + opResult.inputConsumed);
                } else if (opResult.output != null && opResult.output.length > 0) {
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
            throw new KeyStoreException(-21, sb.toString());
        }
        byte[] result = bufferedOutput != null ? bufferedOutput.toByteArray() : EmptyArray.BYTE;
        this.mProducedOutputSizeBytes += (long) result.length;
        return result;
    }

    @Override // android.security.keystore.KeyStoreCryptoOperationStreamer
    public long getConsumedInputSizeBytes() {
        return this.mConsumedInputSizeBytes;
    }

    @Override // android.security.keystore.KeyStoreCryptoOperationStreamer
    public long getProducedOutputSizeBytes() {
        return this.mProducedOutputSizeBytes;
    }

    public static class MainDataStream implements Stream {
        private final KeyStore mKeyStore;
        private final IBinder mOperationToken;

        public MainDataStream(KeyStore keyStore, IBinder operationToken) {
            this.mKeyStore = keyStore;
            this.mOperationToken = operationToken;
        }

        @Override // android.security.keystore.KeyStoreCryptoOperationChunkedStreamer.Stream
        public OperationResult update(byte[] input) {
            return this.mKeyStore.update(this.mOperationToken, null, input);
        }

        @Override // android.security.keystore.KeyStoreCryptoOperationChunkedStreamer.Stream
        public OperationResult finish(byte[] signature, byte[] additionalEntropy) {
            return this.mKeyStore.finish(this.mOperationToken, null, signature, additionalEntropy);
        }
    }
}
