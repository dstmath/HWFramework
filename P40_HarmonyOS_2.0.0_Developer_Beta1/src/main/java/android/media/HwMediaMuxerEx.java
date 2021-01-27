package android.media;

import android.media.MediaCodec;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HwMediaMuxerEx {
    private HwMediaMuxer mHwMediaMuxer;

    public HwMediaMuxerEx(String path, int format) throws IOException {
        this.mHwMediaMuxer = new HwMediaMuxer(path, format);
    }

    public HwMediaMuxerEx(FileDescriptor fd, int format) throws IOException {
        this.mHwMediaMuxer = new HwMediaMuxer(fd, format);
    }

    public void setOrientationHint(int degrees) {
        this.mHwMediaMuxer.setOrientationHint(degrees);
    }

    public void setLocation(float latitude, float longitude) {
        this.mHwMediaMuxer.setLocation(latitude, longitude);
    }

    public void setUserTag(String userTag) {
        this.mHwMediaMuxer.setUserTag(userTag);
    }

    public void start() {
        this.mHwMediaMuxer.start();
    }

    public void stop() {
        this.mHwMediaMuxer.stop();
    }

    public int addTrack(MediaFormat format) {
        return this.mHwMediaMuxer.addTrack(format);
    }

    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        this.mHwMediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }

    public void release() {
        this.mHwMediaMuxer.release();
    }
}
