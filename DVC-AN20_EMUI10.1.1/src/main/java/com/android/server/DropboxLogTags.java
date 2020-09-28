package com.android.server;

import android.util.EventLog;

public class DropboxLogTags {
    public static final int DROPBOX_FILE_COPY = 81002;

    private DropboxLogTags() {
    }

    public static void writeDropboxFileCopy(String filename, int size, String tag) {
        EventLog.writeEvent((int) DROPBOX_FILE_COPY, filename, Integer.valueOf(size), tag);
    }
}
