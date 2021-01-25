package ohos.ai.engine.aimodel;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class AiModelBean implements Sequenceable {
    private static final long DEFAULT_INVALID_ORIGINID = 0;
    private String filePath;
    private long originId;

    public AiModelBean() {
        this(0, null);
    }

    public AiModelBean(long j, String str) {
        this.originId = j;
        this.filePath = str;
    }

    public long getOriginId() {
        return this.originId;
    }

    public void setOriginId(Long l) {
        this.originId = l.longValue();
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String str) {
        this.filePath = str;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeLong(this.originId);
        parcel.writeString(this.filePath);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.originId = parcel.readLong();
        this.filePath = parcel.readString();
        return true;
    }
}
