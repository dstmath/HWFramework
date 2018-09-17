package android.speech.tts;

import android.media.AudioFormat;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

class FileSynthesisCallback extends AbstractSynthesisCallback {
    private static final boolean DBG = false;
    private static final int MAX_AUDIO_BUFFER_SIZE = 8192;
    private static final String TAG = "FileSynthesisRequest";
    private static final short WAV_FORMAT_PCM = (short) 1;
    private static final int WAV_HEADER_LENGTH = 44;
    private int mAudioFormat;
    private int mChannelCount;
    private final UtteranceProgressDispatcher mDispatcher;
    private boolean mDone = false;
    private FileChannel mFileChannel;
    private int mSampleRateInHz;
    private boolean mStarted = false;
    private final Object mStateLock = new Object();
    protected int mStatusCode;

    FileSynthesisCallback(FileChannel fileChannel, UtteranceProgressDispatcher dispatcher, boolean clientIsUsingV2) {
        super(clientIsUsingV2);
        this.mFileChannel = fileChannel;
        this.mDispatcher = dispatcher;
        this.mStatusCode = 0;
    }

    void stop() {
        synchronized (this.mStateLock) {
            if (this.mDone) {
            } else if (this.mStatusCode == -2) {
            } else {
                this.mStatusCode = -2;
                cleanUp();
                this.mDispatcher.dispatchOnStop();
            }
        }
    }

    private void cleanUp() {
        closeFile();
    }

    private void closeFile() {
        this.mFileChannel = null;
    }

    public int getMaxBufferSize() {
        return 8192;
    }

    public int start(int sampleRateInHz, int audioFormat, int channelCount) {
        if (!(audioFormat == 3 || audioFormat == 2 || audioFormat == 4)) {
            Log.e(TAG, "Audio format encoding " + audioFormat + " not supported. Please use one " + "of AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT or " + "AudioFormat.ENCODING_PCM_FLOAT");
        }
        this.mDispatcher.dispatchOnBeginSynthesis(sampleRateInHz, audioFormat, channelCount);
        synchronized (this.mStateLock) {
            if (this.mStatusCode == -2) {
                int errorCodeOnStop = errorCodeOnStop();
                return errorCodeOnStop;
            } else if (this.mStatusCode != 0) {
                return -1;
            } else if (this.mStarted) {
                Log.e(TAG, "Start called twice");
                return -1;
            } else {
                this.mStarted = true;
                this.mSampleRateInHz = sampleRateInHz;
                this.mAudioFormat = audioFormat;
                this.mChannelCount = channelCount;
                this.mDispatcher.dispatchOnStart();
                FileChannel fileChannel = this.mFileChannel;
                try {
                    fileChannel.write(ByteBuffer.allocate(44));
                    return 0;
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to write wav header to output file descriptor", ex);
                    synchronized (this.mStateLock) {
                        cleanUp();
                        this.mStatusCode = -5;
                        return -1;
                    }
                }
            }
        }
    }

    public int audioAvailable(byte[] buffer, int offset, int length) {
        synchronized (this.mStateLock) {
            if (this.mStatusCode == -2) {
                int errorCodeOnStop = errorCodeOnStop();
                return errorCodeOnStop;
            } else if (this.mStatusCode != 0) {
                return -1;
            } else if (this.mFileChannel == null) {
                Log.e(TAG, "File not open");
                this.mStatusCode = -5;
                return -1;
            } else if (this.mStarted) {
                FileChannel fileChannel = this.mFileChannel;
                byte[] bufferCopy = new byte[length];
                System.arraycopy(buffer, offset, bufferCopy, 0, length);
                this.mDispatcher.dispatchOnAudioAvailable(bufferCopy);
                try {
                    fileChannel.write(ByteBuffer.wrap(buffer, offset, length));
                    return 0;
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to write to output file descriptor", ex);
                    synchronized (this.mStateLock) {
                        cleanUp();
                        this.mStatusCode = -5;
                        return -1;
                    }
                }
            } else {
                Log.e(TAG, "Start method was not called");
                return -1;
            }
        }
    }

    public int done() {
        synchronized (this.mStateLock) {
            if (this.mDone) {
                Log.w(TAG, "Duplicate call to done()");
                return -1;
            } else if (this.mStatusCode == -2) {
                int errorCodeOnStop = errorCodeOnStop();
                return errorCodeOnStop;
            } else if (this.mStatusCode != 0 && this.mStatusCode != -2) {
                this.mDispatcher.dispatchOnError(this.mStatusCode);
                return -1;
            } else if (this.mFileChannel == null) {
                Log.e(TAG, "File not open");
                return -1;
            } else {
                this.mDone = true;
                FileChannel fileChannel = this.mFileChannel;
                int sampleRateInHz = this.mSampleRateInHz;
                int audioFormat = this.mAudioFormat;
                int channelCount = this.mChannelCount;
                try {
                    fileChannel.position(0);
                    fileChannel.write(makeWavHeader(sampleRateInHz, audioFormat, channelCount, (int) (fileChannel.size() - 44)));
                    synchronized (this.mStateLock) {
                        closeFile();
                        this.mDispatcher.dispatchOnSuccess();
                    }
                    return 0;
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to write to output file descriptor", ex);
                    synchronized (this.mStateLock) {
                        cleanUp();
                        return -1;
                    }
                }
            }
        }
    }

    public void error() {
        error(-3);
    }

    public void error(int errorCode) {
        synchronized (this.mStateLock) {
            if (this.mDone) {
                return;
            }
            cleanUp();
            this.mStatusCode = errorCode;
        }
    }

    public boolean hasStarted() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mStarted;
        }
        return z;
    }

    public boolean hasFinished() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mDone;
        }
        return z;
    }

    private ByteBuffer makeWavHeader(int sampleRateInHz, int audioFormat, int channelCount, int dataLength) {
        int sampleSizeInBytes = AudioFormat.getBytesPerSample(audioFormat);
        int byteRate = (sampleRateInHz * sampleSizeInBytes) * channelCount;
        short blockAlign = (short) (sampleSizeInBytes * channelCount);
        short bitsPerSample = (short) (sampleSizeInBytes * 8);
        ByteBuffer header = ByteBuffer.wrap(new byte[44]);
        header.order(ByteOrder.LITTLE_ENDIAN);
        header.put(new byte[]{(byte) 82, (byte) 73, (byte) 70, (byte) 70});
        header.putInt((dataLength + 44) - 8);
        header.put(new byte[]{(byte) 87, (byte) 65, (byte) 86, (byte) 69});
        header.put(new byte[]{(byte) 102, (byte) 109, (byte) 116, (byte) 32});
        header.putInt(16);
        header.putShort((short) 1);
        header.putShort((short) channelCount);
        header.putInt(sampleRateInHz);
        header.putInt(byteRate);
        header.putShort(blockAlign);
        header.putShort(bitsPerSample);
        header.put(new byte[]{(byte) 100, (byte) 97, (byte) 116, (byte) 97});
        header.putInt(dataLength);
        header.flip();
        return header;
    }

    public void rangeStart(int markerInFrames, int start, int end) {
        this.mDispatcher.dispatchOnRangeStart(markerInFrames, start, end);
    }
}
