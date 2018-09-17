package android.security.keystore;

import android.os.IBinder;
import android.security.KeyStore;
import android.security.KeyStoreException;
import android.security.keymaster.OperationResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.ProviderException;
import libcore.util.EmptyArray;

class KeyStoreCryptoOperationChunkedStreamer implements KeyStoreCryptoOperationStreamer {
    private static final int DEFAULT_MAX_CHUNK_SIZE = 65536;
    private byte[] mBuffered;
    private int mBufferedLength;
    private int mBufferedOffset;
    private long mConsumedInputSizeBytes;
    private final Stream mKeyStoreStream;
    private final int mMaxChunkSize;
    private long mProducedOutputSizeBytes;

    interface Stream {
        OperationResult finish(byte[] bArr, byte[] bArr2);

        OperationResult update(byte[] bArr);
    }

    public static class MainDataStream implements Stream {
        private final KeyStore mKeyStore;
        private final IBinder mOperationToken;

        public MainDataStream(KeyStore keyStore, IBinder operationToken) {
            this.mKeyStore = keyStore;
            this.mOperationToken = operationToken;
        }

        public OperationResult update(byte[] input) {
            return this.mKeyStore.update(this.mOperationToken, null, input);
        }

        public OperationResult finish(byte[] signature, byte[] additionalEntropy) {
            return this.mKeyStore.finish(this.mOperationToken, null, signature, additionalEntropy);
        }
    }

    public KeyStoreCryptoOperationChunkedStreamer(Stream operation) {
        this(operation, 65536);
    }

    public KeyStoreCryptoOperationChunkedStreamer(Stream operation, int maxChunkSize) {
        this.mBuffered = EmptyArray.BYTE;
        this.mKeyStoreStream = operation;
        this.mMaxChunkSize = maxChunkSize;
    }

    public byte[] update(byte[] input, int inputOffset, int inputLength) throws KeyStoreException {
        if (inputLength == 0) {
            return EmptyArray.BYTE;
        }
        byte[] result;
        ByteArrayOutputStream bufferedOutput = null;
        while (inputLength > 0) {
            int inputBytesInChunk;
            byte[] chunk;
            if (this.mBufferedLength + inputLength > this.mMaxChunkSize) {
                inputBytesInChunk = this.mMaxChunkSize - this.mBufferedLength;
                chunk = ArrayUtils.concat(this.mBuffered, this.mBufferedOffset, this.mBufferedLength, input, inputOffset, inputBytesInChunk);
            } else if (this.mBufferedLength == 0 && inputOffset == 0 && inputLength == input.length) {
                chunk = input;
                inputBytesInChunk = input.length;
            } else {
                inputBytesInChunk = inputLength;
                chunk = ArrayUtils.concat(this.mBuffered, this.mBufferedOffset, this.mBufferedLength, input, inputOffset, inputBytesInChunk);
            }
            inputOffset += inputBytesInChunk;
            inputLength -= inputBytesInChunk;
            this.mConsumedInputSizeBytes += (long) inputBytesInChunk;
            OperationResult opResult = this.mKeyStoreStream.update(chunk);
            if (opResult == null) {
                throw new KeyStoreConnectException();
            } else if (opResult.resultCode != 1) {
                throw KeyStore.getKeyStoreException(opResult.resultCode);
            } else {
                if (opResult.inputConsumed == chunk.length) {
                    this.mBuffered = EmptyArray.BYTE;
                    this.mBufferedOffset = 0;
                    this.mBufferedLength = 0;
                } else if (opResult.inputConsumed <= 0) {
                    if (inputLength > 0) {
                        throw new KeyStoreException(-1000, "Keystore consumed nothing from max-sized chunk: " + chunk.length + " bytes");
                    }
                    this.mBuffered = chunk;
                    this.mBufferedOffset = 0;
                    this.mBufferedLength = chunk.length;
                } else if (opResult.inputConsumed < chunk.length) {
                    this.mBuffered = chunk;
                    this.mBufferedOffset = opResult.inputConsumed;
                    this.mBufferedLength = chunk.length - opResult.inputConsumed;
                } else {
                    throw new KeyStoreException(-1000, "Keystore consumed more input than provided. Provided: " + chunk.length + ", consumed: " + opResult.inputConsumed);
                }
                if (opResult.output != null && opResult.output.length > 0) {
                    if (inputLength <= 0) {
                        if (bufferedOutput == null) {
                            result = opResult.output;
                        } else {
                            try {
                                bufferedOutput.write(opResult.output);
                                result = bufferedOutput.toByteArray();
                            } catch (IOException e) {
                                throw new ProviderException("Failed to buffer output", e);
                            }
                        }
                        this.mProducedOutputSizeBytes += (long) result.length;
                        return result;
                    } else if (bufferedOutput == null) {
                        bufferedOutput = new ByteArrayOutputStream();
                        try {
                            bufferedOutput.write(opResult.output);
                        } catch (IOException e2) {
                            throw new ProviderException("Failed to buffer output", e2);
                        }
                    } else {
                        continue;
                    }
                }
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

    public byte[] doFinal(byte[] input, int inputOffset, int inputLength, byte[] signature, byte[] additionalEntropy) throws KeyStoreException {
        if (inputLength == 0) {
            input = EmptyArray.BYTE;
            inputOffset = 0;
        }
        byte[] output = ArrayUtils.concat(update(input, inputOffset, inputLength), flush());
        OperationResult opResult = this.mKeyStoreStream.finish(signature, additionalEntropy);
        if (opResult == null) {
            throw new KeyStoreConnectException();
        } else if (opResult.resultCode != 1) {
            throw KeyStore.getKeyStoreException(opResult.resultCode);
        } else {
            this.mProducedOutputSizeBytes += (long) opResult.output.length;
            return ArrayUtils.concat(output, opResult.output);
        }
    }

    public byte[] flush() throws KeyStoreException {
        if (this.mBufferedLength <= 0) {
            return EmptyArray.BYTE;
        }
        ByteArrayOutputStream byteArrayOutputStream = null;
        while (this.mBufferedLength > 0) {
            byte[] chunk = ArrayUtils.subarray(this.mBuffered, this.mBufferedOffset, this.mBufferedLength);
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
                    if (byteArrayOutputStream == null) {
                        if (this.mBufferedLength == 0) {
                            this.mProducedOutputSizeBytes += (long) opResult.output.length;
                            return opResult.output;
                        }
                        byteArrayOutputStream = new ByteArrayOutputStream();
                    }
                    try {
                        byteArrayOutputStream.write(opResult.output);
                    } catch (IOException e) {
                        throw new ProviderException("Failed to buffer output", e);
                    }
                }
            }
        }
        if (this.mBufferedLength > 0) {
            throw new KeyStoreException(-21, "Keystore failed to consume last " + (this.mBufferedLength != 1 ? this.mBufferedLength + " bytes" : "byte") + " of input");
        }
        byte[] result = byteArrayOutputStream != null ? byteArrayOutputStream.toByteArray() : EmptyArray.BYTE;
        this.mProducedOutputSizeBytes += (long) result.length;
        return result;
    }

    public long getConsumedInputSizeBytes() {
        return this.mConsumedInputSizeBytes;
    }

    public long getProducedOutputSizeBytes() {
        return this.mProducedOutputSizeBytes;
    }
}
