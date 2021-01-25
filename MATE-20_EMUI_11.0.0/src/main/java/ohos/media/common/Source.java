package ohos.media.common;

import java.io.FileDescriptor;
import java.util.Map;

public class Source {
    public static final int AUDIO_SOURCE_INIT = -1;
    private static final int SOURCE_TYPE_FILE_DESCRIPTOR = 2;
    private static final int SOURCE_TYPE_UNKNOWN = 0;
    private static final int SOURCE_TYPE_URI = 1;
    public static final int VIDEO_SOURCE_INIT = -1;
    private FileDescriptor fd;
    private int fileType = 0;
    private Map<String, String> header;
    private long length = 0;
    private long offset = 0;
    private int recorderAudioSource = -1;
    private int recorderVideoSource = -1;
    private String uri;

    public Source() {
    }

    public Source(String str) {
        this.uri = str;
        this.fileType = 1;
    }

    public Source(String str, Map<String, String> map) {
        this.uri = str;
        this.header = map;
        this.fileType = 1;
    }

    public Source(FileDescriptor fileDescriptor) {
        this.fd = fileDescriptor;
        this.fileType = 2;
    }

    public Source(FileDescriptor fileDescriptor, long j, long j2) {
        this.fd = fileDescriptor;
        this.offset = j;
        this.length = j2;
        this.fileType = 2;
    }

    public int getFileType() {
        return this.fileType;
    }

    public int getRecorderAudioSource() {
        return this.recorderAudioSource;
    }

    public void setRecorderAudioSource(int i) {
        this.recorderAudioSource = i;
    }

    public int getRecorderVideoSource() {
        return this.recorderVideoSource;
    }

    public void setRecorderVideoSource(int i) {
        this.recorderVideoSource = i;
    }

    public String getUri() {
        return this.uri;
    }
}
