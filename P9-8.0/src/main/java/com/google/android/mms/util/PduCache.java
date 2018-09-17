package com.google.android.mms.util;

import android.content.ContentUris;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.Telephony.Mms;
import java.util.HashMap;
import java.util.HashSet;

public final class PduCache extends AbstractCache<Uri, PduCacheEntry> {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static final HashMap<Integer, Integer> MATCH_TO_MSGBOX_ID_MAP = new HashMap();
    private static final int MMS_ALL = 0;
    private static final int MMS_ALL_ID = 1;
    private static final int MMS_CONVERSATION = 10;
    private static final int MMS_CONVERSATION_ID = 11;
    private static final int MMS_DRAFTS = 6;
    private static final int MMS_DRAFTS_ID = 7;
    private static final int MMS_INBOX = 2;
    private static final int MMS_INBOX_ID = 3;
    private static final int MMS_OUTBOX = 8;
    private static final int MMS_OUTBOX_ID = 9;
    private static final int MMS_SENT = 4;
    private static final int MMS_SENT_ID = 5;
    private static final String TAG = "PduCache";
    private static final UriMatcher URI_MATCHER = new UriMatcher(-1);
    private static PduCache sInstance;
    private final HashMap<Integer, HashSet<Uri>> mMessageBoxes = new HashMap();
    private final HashMap<Long, HashSet<Uri>> mThreads = new HashMap();
    private final HashSet<Uri> mUpdating = new HashSet();

    static {
        URI_MATCHER.addURI("mms", null, 0);
        URI_MATCHER.addURI("mms", "#", 1);
        URI_MATCHER.addURI("mms", "inbox", 2);
        URI_MATCHER.addURI("mms", "inbox/#", 3);
        URI_MATCHER.addURI("mms", "sent", 4);
        URI_MATCHER.addURI("mms", "sent/#", 5);
        URI_MATCHER.addURI("mms", "drafts", 6);
        URI_MATCHER.addURI("mms", "drafts/#", 7);
        URI_MATCHER.addURI("mms", "outbox", 8);
        URI_MATCHER.addURI("mms", "outbox/#", 9);
        URI_MATCHER.addURI("mms-sms", "conversations", 10);
        URI_MATCHER.addURI("mms-sms", "conversations/#", 11);
        MATCH_TO_MSGBOX_ID_MAP.put(Integer.valueOf(2), Integer.valueOf(1));
        MATCH_TO_MSGBOX_ID_MAP.put(Integer.valueOf(4), Integer.valueOf(2));
        MATCH_TO_MSGBOX_ID_MAP.put(Integer.valueOf(6), Integer.valueOf(3));
        MATCH_TO_MSGBOX_ID_MAP.put(Integer.valueOf(8), Integer.valueOf(4));
    }

    private PduCache() {
    }

    public static final synchronized PduCache getInstance() {
        PduCache pduCache;
        synchronized (PduCache.class) {
            if (sInstance == null) {
                sInstance = new PduCache();
            }
            pduCache = sInstance;
        }
        return pduCache;
    }

    public synchronized boolean put(Uri uri, PduCacheEntry entry) {
        boolean result;
        int msgBoxId = entry.getMessageBox();
        HashSet<Uri> msgBox = (HashSet) this.mMessageBoxes.get(Integer.valueOf(msgBoxId));
        if (msgBox == null) {
            msgBox = new HashSet();
            this.mMessageBoxes.put(Integer.valueOf(msgBoxId), msgBox);
        }
        long threadId = entry.getThreadId();
        HashSet<Uri> thread = (HashSet) this.mThreads.get(Long.valueOf(threadId));
        if (thread == null) {
            thread = new HashSet();
            this.mThreads.put(Long.valueOf(threadId), thread);
        }
        Uri finalKey = normalizeKey(uri);
        result = super.put(finalKey, entry);
        if (result) {
            msgBox.add(finalKey);
            thread.add(finalKey);
        }
        setUpdating(uri, false);
        return result;
    }

    public synchronized void setUpdating(Uri uri, boolean updating) {
        if (updating) {
            this.mUpdating.add(uri);
        } else {
            this.mUpdating.remove(uri);
        }
    }

    public synchronized boolean isUpdating(Uri uri) {
        return this.mUpdating.contains(uri);
    }

    public synchronized PduCacheEntry purge(Uri uri) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case 0:
            case 10:
                purgeAll();
                return null;
            case 1:
                return purgeSingleEntry(uri);
            case 2:
            case 4:
            case 6:
            case 8:
                purgeByMessageBox((Integer) MATCH_TO_MSGBOX_ID_MAP.get(Integer.valueOf(match)));
                return null;
            case 3:
            case 5:
            case 7:
            case 9:
                return purgeSingleEntry(Uri.withAppendedPath(Mms.CONTENT_URI, uri.getLastPathSegment()));
            case 11:
                purgeByThreadId(ContentUris.parseId(uri));
                return null;
            default:
                return null;
        }
    }

    private PduCacheEntry purgeSingleEntry(Uri key) {
        this.mUpdating.remove(key);
        PduCacheEntry entry = (PduCacheEntry) super.purge(key);
        if (entry == null) {
            return null;
        }
        removeFromThreads(key, entry);
        removeFromMessageBoxes(key, entry);
        return entry;
    }

    public synchronized void purgeAll() {
        super.purgeAll();
        this.mMessageBoxes.clear();
        this.mThreads.clear();
        this.mUpdating.clear();
    }

    private Uri normalizeKey(Uri uri) {
        Uri normalizedKey;
        switch (URI_MATCHER.match(uri)) {
            case 1:
                normalizedKey = uri;
                break;
            case 3:
            case 5:
            case 7:
            case 9:
                normalizedKey = Uri.withAppendedPath(Mms.CONTENT_URI, uri.getLastPathSegment());
                break;
            default:
                return null;
        }
        return normalizedKey;
    }

    private void purgeByMessageBox(Integer msgBoxId) {
        if (msgBoxId != null) {
            HashSet<Uri> msgBox = (HashSet) this.mMessageBoxes.remove(msgBoxId);
            if (msgBox != null) {
                for (Uri key : msgBox) {
                    this.mUpdating.remove(key);
                    PduCacheEntry entry = (PduCacheEntry) super.purge(key);
                    if (entry != null) {
                        removeFromThreads(key, entry);
                    }
                }
            }
        }
    }

    private void removeFromThreads(Uri key, PduCacheEntry entry) {
        HashSet<Uri> thread = (HashSet) this.mThreads.get(Long.valueOf(entry.getThreadId()));
        if (thread != null) {
            thread.remove(key);
        }
    }

    private void purgeByThreadId(long threadId) {
        HashSet<Uri> thread = (HashSet) this.mThreads.remove(Long.valueOf(threadId));
        if (thread != null) {
            for (Uri key : thread) {
                this.mUpdating.remove(key);
                PduCacheEntry entry = (PduCacheEntry) super.purge(key);
                if (entry != null) {
                    removeFromMessageBoxes(key, entry);
                }
            }
        }
    }

    private void removeFromMessageBoxes(Uri key, PduCacheEntry entry) {
        HashSet<Uri> msgBox = (HashSet) this.mThreads.get(Long.valueOf((long) entry.getMessageBox()));
        if (msgBox != null) {
            msgBox.remove(key);
        }
    }
}
