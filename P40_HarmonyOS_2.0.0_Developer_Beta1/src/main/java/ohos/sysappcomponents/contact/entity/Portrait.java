package ohos.sysappcomponents.contact.entity;

import ohos.utils.net.Uri;

public class Portrait {
    private int id;
    private int portraitFileId;
    private Uri uri;

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public Uri getUri() {
        return this.uri;
    }

    public void setUri(Uri uri2) {
        this.uri = uri2;
    }

    public int getPortraitFileId() {
        return this.portraitFileId;
    }

    public void setPortraitFileId(int i) {
        this.portraitFileId = i;
    }
}
