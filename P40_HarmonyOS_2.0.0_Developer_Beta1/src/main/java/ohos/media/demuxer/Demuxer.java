package ohos.media.demuxer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import ohos.media.common.Format;
import ohos.media.common.Source;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.media.utils.trace.TracerFactory;

public class Demuxer {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(Demuxer.class);
    private static final int SUCCESS = 0;
    private static final Tracer TRACER = TracerFactory.getMediaTracer();
    private long nativeDemuxer;

    private native int nativeGetMediaFlags(long j);

    private native long nativeGetMediaSize(long j);

    private native long nativeGetMediaTime(long j);

    private native int nativeGetMediaTrackId(long j);

    private native int nativeGetTotalTracks(long j);

    private native Map<String, Object> nativeGetTrackFormat(int i, long j);

    private native int nativeInit();

    private native int nativeNext(long j);

    private native int nativeReadBuffer(ByteBuffer byteBuffer, int i, long j);

    private native int nativeRelease(long j);

    private native int nativeRewindTo(long j, int i, long j2);

    private native int nativeSelectTrack(int i, long j);

    private native int nativeSetSource(Source source, long j);

    private native int nativeUnselectTrack(int i, long j);

    static {
        System.loadLibrary("zdemuxer_jni.z");
    }

    public Demuxer() {
        nativeInit();
    }

    public boolean setSource(Source source) {
        if (source != null) {
            int nativeSetSource = nativeSetSource(source, this.nativeDemuxer);
            if (nativeSetSource == 0) {
                return true;
            }
            LOGGER.error("set source failed, error code is %{public}d", Integer.valueOf(nativeSetSource));
            return false;
        }
        throw new IllegalArgumentException("source must not be null");
    }

    public final Format getStreamFormat(int i) {
        if (i >= 0) {
            return new Format((HashMap<String, Object>) ((HashMap) nativeGetTrackFormat(i, this.nativeDemuxer)));
        }
        throw new IllegalArgumentException("Track index should bigger than zero");
    }

    public final int getTotalStreams() {
        return nativeGetTotalTracks(this.nativeDemuxer);
    }

    public boolean specifyStream(int i) {
        if (i >= 0) {
            int nativeSelectTrack = nativeSelectTrack(i, this.nativeDemuxer);
            if (nativeSelectTrack == 0) {
                return true;
            }
            LOGGER.error("select track failed, error code is %{public}d", Integer.valueOf(nativeSelectTrack));
            return false;
        }
        throw new IllegalArgumentException("Track index should bigger than zero");
    }

    public boolean unspecifyStream(int i) {
        if (i >= 0) {
            int nativeUnselectTrack = nativeUnselectTrack(i, this.nativeDemuxer);
            if (nativeUnselectTrack == 0) {
                return true;
            }
            LOGGER.error("unselect track failed, error code is %{public}d", Integer.valueOf(nativeUnselectTrack));
            return false;
        }
        throw new IllegalArgumentException("Track index should equal or bigger than zero");
    }

    public boolean rewindTo(long j) {
        int nativeRewindTo = nativeRewindTo(j, 0, this.nativeDemuxer);
        if (nativeRewindTo == 0) {
            return true;
        }
        LOGGER.error("unselect rewind position failed, error code is %{public}d", Integer.valueOf(nativeRewindTo));
        return false;
    }

    public boolean next() {
        int nativeNext = nativeNext(this.nativeDemuxer);
        if (nativeNext == 0) {
            return true;
        }
        LOGGER.error("move to next frame failed, error code is %{public}d", Integer.valueOf(nativeNext));
        return false;
    }

    public int readBuffer(ByteBuffer byteBuffer, int i) {
        return nativeReadBuffer(byteBuffer, i, this.nativeDemuxer);
    }

    public int getStreamId() {
        return nativeGetMediaTrackId(this.nativeDemuxer);
    }

    public long getFrameTimestamp() {
        return nativeGetMediaTime(this.nativeDemuxer);
    }

    public long getFrameSize() {
        return nativeGetMediaSize(this.nativeDemuxer);
    }

    public int getFrameType() {
        return nativeGetMediaFlags(this.nativeDemuxer);
    }

    public boolean release() {
        int nativeRelease = nativeRelease(this.nativeDemuxer);
        if (nativeRelease == 0) {
            return true;
        }
        TRACER.finishTrace("Demuxer.release");
        LOGGER.error("move to next frame failed, error code is %{public}d", Integer.valueOf(nativeRelease));
        return false;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }
}
