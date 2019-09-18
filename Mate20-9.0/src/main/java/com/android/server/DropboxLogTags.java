package com.android.server;

import android.util.EventLog;

public class DropboxLogTags {
    public static final int DROPBOX_FILE_COPY = 81002;

    private DropboxLogTags() {
    }

    public static void writeDropboxFileCopy(String filename, int size, String tag) {
        EventLog.writeEvent(DROPBOX_FILE_COPY, new Object[]{filename, Integer.valueOf(size), tag});
    }
}
