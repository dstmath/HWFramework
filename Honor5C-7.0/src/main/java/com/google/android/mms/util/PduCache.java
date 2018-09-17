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
    private static final HashMap<Integer, Integer> MATCH_TO_MSGBOX_ID_MAP = null;
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
    private static final UriMatcher URI_MATCHER = null;
    private static PduCache sInstance;
    private final HashMap<Integer, HashSet<Uri>> mMessageBoxes;
    private final HashMap<Long, HashSet<Uri>> mThreads;
    private final HashSet<Uri> mUpdating;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.mms.util.PduCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.mms.util.PduCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.mms.util.PduCache.<clinit>():void");
    }

    private PduCache() {
        this.mMessageBoxes = new HashMap();
        this.mThreads = new HashMap();
        this.mUpdating = new HashSet();
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
        setUpdating(uri, LOCAL_LOGV);
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
            case MMS_ALL /*0*/:
            case MMS_CONVERSATION /*10*/:
                purgeAll();
                return null;
            case MMS_ALL_ID /*1*/:
                return purgeSingleEntry(uri);
            case MMS_INBOX /*2*/:
            case MMS_SENT /*4*/:
            case MMS_DRAFTS /*6*/:
            case MMS_OUTBOX /*8*/:
                purgeByMessageBox((Integer) MATCH_TO_MSGBOX_ID_MAP.get(Integer.valueOf(match)));
                return null;
            case MMS_INBOX_ID /*3*/:
            case MMS_SENT_ID /*5*/:
            case MMS_DRAFTS_ID /*7*/:
            case MMS_OUTBOX_ID /*9*/:
                return purgeSingleEntry(Uri.withAppendedPath(Mms.CONTENT_URI, uri.getLastPathSegment()));
            case MMS_CONVERSATION_ID /*11*/:
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
            case MMS_ALL_ID /*1*/:
                normalizedKey = uri;
                break;
            case MMS_INBOX_ID /*3*/:
            case MMS_SENT_ID /*5*/:
            case MMS_DRAFTS_ID /*7*/:
            case MMS_OUTBOX_ID /*9*/:
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
