package com.android.media.remotedisplay;

import android.media.RemoteDisplayState.RemoteDisplayInfo;
import android.text.TextUtils;
import java.util.Objects;

public class RemoteDisplay {
    public static final int PLAYBACK_VOLUME_FIXED = 0;
    public static final int PLAYBACK_VOLUME_VARIABLE = 1;
    public static final int STATUS_AVAILABLE = 2;
    public static final int STATUS_CONNECTED = 4;
    public static final int STATUS_CONNECTING = 3;
    public static final int STATUS_IN_USE = 1;
    public static final int STATUS_NOT_AVAILABLE = 0;
    private RemoteDisplayInfo mImmutableInfo;
    private final RemoteDisplayInfo mMutableInfo;

    public RemoteDisplay(String id, String name) {
        if (TextUtils.isEmpty(id)) {
            throw new IllegalArgumentException("id must not be null or empty");
        }
        this.mMutableInfo = new RemoteDisplayInfo(id);
        setName(name);
    }

    public String getId() {
        return this.mMutableInfo.id;
    }

    public String getName() {
        return this.mMutableInfo.name;
    }

    public void setName(String name) {
        if (!Objects.equals(this.mMutableInfo.name, name)) {
            this.mMutableInfo.name = name;
            this.mImmutableInfo = null;
        }
    }

    public String getDescription() {
        return this.mMutableInfo.description;
    }

    public void setDescription(String description) {
        if (!Objects.equals(this.mMutableInfo.description, description)) {
            this.mMutableInfo.description = description;
            this.mImmutableInfo = null;
        }
    }

    public int getStatus() {
        return this.mMutableInfo.status;
    }

    public void setStatus(int status) {
        if (this.mMutableInfo.status != status) {
            this.mMutableInfo.status = status;
            this.mImmutableInfo = null;
        }
    }

    public int getVolume() {
        return this.mMutableInfo.volume;
    }

    public void setVolume(int volume) {
        if (this.mMutableInfo.volume != volume) {
            this.mMutableInfo.volume = volume;
            this.mImmutableInfo = null;
        }
    }

    public int getVolumeMax() {
        return this.mMutableInfo.volumeMax;
    }

    public void setVolumeMax(int volumeMax) {
        if (this.mMutableInfo.volumeMax != volumeMax) {
            this.mMutableInfo.volumeMax = volumeMax;
            this.mImmutableInfo = null;
        }
    }

    public int getVolumeHandling() {
        return this.mMutableInfo.volumeHandling;
    }

    public void setVolumeHandling(int volumeHandling) {
        if (this.mMutableInfo.volumeHandling != volumeHandling) {
            this.mMutableInfo.volumeHandling = volumeHandling;
            this.mImmutableInfo = null;
        }
    }

    public int getPresentationDisplayId() {
        return this.mMutableInfo.presentationDisplayId;
    }

    public void setPresentationDisplayId(int presentationDisplayId) {
        if (this.mMutableInfo.presentationDisplayId != presentationDisplayId) {
            this.mMutableInfo.presentationDisplayId = presentationDisplayId;
            this.mImmutableInfo = null;
        }
    }

    public String toString() {
        return "RemoteDisplay{" + this.mMutableInfo.toString() + "}";
    }

    RemoteDisplayInfo getInfo() {
        if (this.mImmutableInfo == null) {
            this.mImmutableInfo = new RemoteDisplayInfo(this.mMutableInfo);
        }
        return this.mImmutableInfo;
    }
}
