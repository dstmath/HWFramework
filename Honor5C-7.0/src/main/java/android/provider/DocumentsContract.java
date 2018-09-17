package android.provider;

import android.bluetooth.BluetoothAssignedNumbers;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.wifi.AnqpInformationElement;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.CalendarContract.CalendarCache;
import android.provider.VoicemailContract.Voicemails;
import android.security.keymaster.KeymasterDefs;
import android.service.voice.VoiceInteractionSession;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.Engine;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.telecom.AudioState;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import libcore.io.IoUtils;

public final class DocumentsContract {
    public static final String ACTION_BROWSE = "android.provider.action.BROWSE";
    public static final String ACTION_DOCUMENT_ROOT_SETTINGS = "android.provider.action.DOCUMENT_ROOT_SETTINGS";
    public static final String ACTION_MANAGE_DOCUMENT = "android.provider.action.MANAGE_DOCUMENT";
    public static final String EXTRA_ERROR = "error";
    public static final String EXTRA_EXCLUDE_SELF = "android.provider.extra.EXCLUDE_SELF";
    public static final String EXTRA_FANCY_FEATURES = "android.content.extra.FANCY";
    public static final String EXTRA_INFO = "info";
    public static final String EXTRA_LOADING = "loading";
    public static final String EXTRA_ORIENTATION = "android.provider.extra.ORIENTATION";
    public static final String EXTRA_PACKAGE_NAME = "android.content.extra.PACKAGE_NAME";
    public static final String EXTRA_PARENT_URI = "parentUri";
    public static final String EXTRA_PROMPT = "android.provider.extra.PROMPT";
    public static final String EXTRA_RESULT = "result";
    public static final String EXTRA_SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED";
    public static final String EXTRA_SHOW_FILESIZE = "android.content.extra.SHOW_FILESIZE";
    public static final String EXTRA_TARGET_URI = "android.content.extra.TARGET_URI";
    public static final String EXTRA_URI = "uri";
    public static final String METHOD_COPY_DOCUMENT = "android:copyDocument";
    public static final String METHOD_CREATE_DOCUMENT = "android:createDocument";
    public static final String METHOD_DELETE_DOCUMENT = "android:deleteDocument";
    public static final String METHOD_IS_CHILD_DOCUMENT = "android:isChildDocument";
    public static final String METHOD_MOVE_DOCUMENT = "android:moveDocument";
    public static final String METHOD_REMOVE_DOCUMENT = "android:removeDocument";
    public static final String METHOD_RENAME_DOCUMENT = "android:renameDocument";
    public static final String PACKAGE_DOCUMENTS_UI = "com.android.documentsui";
    private static final String PARAM_MANAGE = "manage";
    private static final String PARAM_QUERY = "query";
    private static final String PATH_CHILDREN = "children";
    private static final String PATH_DOCUMENT = "document";
    private static final String PATH_RECENT = "recent";
    private static final String PATH_ROOT = "root";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_TREE = "tree";
    public static final String PROVIDER_INTERFACE = "android.content.action.DOCUMENTS_PROVIDER";
    private static final String TAG = "DocumentsContract";
    private static final int THUMBNAIL_BUFFER_SIZE = 131072;

    public static final class Document {
        public static final String COLUMN_DISPLAY_NAME = "_display_name";
        public static final String COLUMN_DOCUMENT_ID = "document_id";
        public static final String COLUMN_FLAGS = "flags";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_LAST_MODIFIED = "last_modified";
        public static final String COLUMN_MIME_TYPE = "mime_type";
        public static final String COLUMN_SIZE = "_size";
        public static final String COLUMN_SUMMARY = "summary";
        public static final int FLAG_ARCHIVE = 32768;
        public static final int FLAG_DIR_PREFERS_GRID = 16;
        public static final int FLAG_DIR_PREFERS_LAST_MODIFIED = 32;
        public static final int FLAG_DIR_SUPPORTS_CREATE = 8;
        public static final int FLAG_PARTIAL = 65536;
        public static final int FLAG_SUPPORTS_COPY = 128;
        public static final int FLAG_SUPPORTS_DELETE = 4;
        public static final int FLAG_SUPPORTS_MOVE = 256;
        public static final int FLAG_SUPPORTS_REMOVE = 1024;
        public static final int FLAG_SUPPORTS_RENAME = 64;
        public static final int FLAG_SUPPORTS_THUMBNAIL = 1;
        public static final int FLAG_SUPPORTS_WRITE = 2;
        public static final int FLAG_VIRTUAL_DOCUMENT = 512;
        public static final String MIME_TYPE_DIR = "vnd.android.document/directory";

        private Document() {
        }
    }

    public static final class Root {
        public static final String COLUMN_AVAILABLE_BYTES = "available_bytes";
        public static final String COLUMN_CAPACITY_BYTES = "capacity_bytes";
        public static final String COLUMN_DOCUMENT_ID = "document_id";
        public static final String COLUMN_FLAGS = "flags";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_MIME_TYPES = "mime_types";
        public static final String COLUMN_ROOT_ID = "root_id";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_TITLE = "title";
        public static final int FLAG_ADVANCED = 131072;
        public static final int FLAG_EMPTY = 65536;
        public static final int FLAG_HAS_SETTINGS = 262144;
        public static final int FLAG_LOCAL_ONLY = 2;
        public static final int FLAG_REMOVABLE_SD = 524288;
        public static final int FLAG_REMOVABLE_USB = 1048576;
        public static final int FLAG_SUPPORTS_CREATE = 1;
        public static final int FLAG_SUPPORTS_IS_CHILD = 16;
        public static final int FLAG_SUPPORTS_RECENTS = 4;
        public static final int FLAG_SUPPORTS_SEARCH = 8;
        public static final String MIME_TYPE_ITEM = "vnd.android.document/root";

        private Root() {
        }
    }

    public static android.net.Uri copyDocument(android.content.ContentResolver r4, android.net.Uri r5, android.net.Uri r6) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 13, 14} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r2 = r5.getAuthority();
        r0 = r4.acquireUnstableContentProviderClient(r2);
        r2 = copyDocument(r0, r5, r6);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x0010:
        r1 = move-exception;
        r2 = "DocumentsContract";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r3 = "Failed to copy document";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.util.Log.w(r2, r3, r1);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r2 = 0;
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x001f:
        r2 = move-exception;
        android.content.ContentProviderClient.releaseQuietly(r0);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.DocumentsContract.copyDocument(android.content.ContentResolver, android.net.Uri, android.net.Uri):android.net.Uri");
    }

    public static android.net.Uri createDocument(android.content.ContentResolver r4, android.net.Uri r5, java.lang.String r6, java.lang.String r7) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 13, 14} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r2 = r5.getAuthority();
        r0 = r4.acquireUnstableContentProviderClient(r2);
        r2 = createDocument(r0, r5, r6, r7);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x0010:
        r1 = move-exception;
        r2 = "DocumentsContract";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r3 = "Failed to create document";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.util.Log.w(r2, r3, r1);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r2 = 0;
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x001f:
        r2 = move-exception;
        android.content.ContentProviderClient.releaseQuietly(r0);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.DocumentsContract.createDocument(android.content.ContentResolver, android.net.Uri, java.lang.String, java.lang.String):android.net.Uri");
    }

    public static boolean deleteDocument(android.content.ContentResolver r4, android.net.Uri r5) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:13:? in {4, 9, 10, 12, 14, 15} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r2 = r5.getAuthority();
        r0 = r4.acquireUnstableContentProviderClient(r2);
        deleteDocument(r0, r5);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r2 = 1;
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x0010:
        r1 = move-exception;
        r2 = "DocumentsContract";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r3 = "Failed to delete document";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.util.Log.w(r2, r3, r1);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r2 = 0;
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x001f:
        r2 = move-exception;
        android.content.ContentProviderClient.releaseQuietly(r0);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.DocumentsContract.deleteDocument(android.content.ContentResolver, android.net.Uri):boolean");
    }

    public static android.net.Uri moveDocument(android.content.ContentResolver r4, android.net.Uri r5, android.net.Uri r6, android.net.Uri r7) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 13, 14} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r2 = r5.getAuthority();
        r0 = r4.acquireUnstableContentProviderClient(r2);
        r2 = moveDocument(r0, r5, r6, r7);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x0010:
        r1 = move-exception;
        r2 = "DocumentsContract";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r3 = "Failed to move document";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.util.Log.w(r2, r3, r1);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r2 = 0;
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x001f:
        r2 = move-exception;
        android.content.ContentProviderClient.releaseQuietly(r0);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.DocumentsContract.moveDocument(android.content.ContentResolver, android.net.Uri, android.net.Uri, android.net.Uri):android.net.Uri");
    }

    public static boolean removeDocument(android.content.ContentResolver r4, android.net.Uri r5, android.net.Uri r6) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:13:? in {4, 9, 10, 12, 14, 15} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r2 = r5.getAuthority();
        r0 = r4.acquireUnstableContentProviderClient(r2);
        removeDocument(r0, r5, r6);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r2 = 1;
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x0010:
        r1 = move-exception;
        r2 = "DocumentsContract";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r3 = "Failed to remove document";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.util.Log.w(r2, r3, r1);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r2 = 0;
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x001f:
        r2 = move-exception;
        android.content.ContentProviderClient.releaseQuietly(r0);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.DocumentsContract.removeDocument(android.content.ContentResolver, android.net.Uri, android.net.Uri):boolean");
    }

    public static android.net.Uri renameDocument(android.content.ContentResolver r4, android.net.Uri r5, java.lang.String r6) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 13, 14} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r2 = r5.getAuthority();
        r0 = r4.acquireUnstableContentProviderClient(r2);
        r2 = renameDocument(r0, r5, r6);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x0010:
        r1 = move-exception;
        r2 = "DocumentsContract";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r3 = "Failed to rename document";	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        android.util.Log.w(r2, r3, r1);	 Catch:{ Exception -> 0x0010, all -> 0x001f }
        r2 = 0;
        android.content.ContentProviderClient.releaseQuietly(r0);
        return r2;
    L_0x001f:
        r2 = move-exception;
        android.content.ContentProviderClient.releaseQuietly(r0);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.DocumentsContract.renameDocument(android.content.ContentResolver, android.net.Uri, java.lang.String):android.net.Uri");
    }

    private DocumentsContract() {
    }

    public static Uri buildRootsUri(String authority) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(authority).appendPath(PATH_ROOT).build();
    }

    public static Uri buildRootUri(String authority, String rootId) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(authority).appendPath(PATH_ROOT).appendPath(rootId).build();
    }

    public static Uri buildHomeUri() {
        return buildRootUri("com.android.externalstorage.documents", CalendarCache.TIMEZONE_TYPE_HOME);
    }

    public static Uri buildRecentDocumentsUri(String authority, String rootId) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(authority).appendPath(PATH_ROOT).appendPath(rootId).appendPath(PATH_RECENT).build();
    }

    public static Uri buildTreeDocumentUri(String authority, String documentId) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(authority).appendPath(PATH_TREE).appendPath(documentId).build();
    }

    public static Uri buildDocumentUri(String authority, String documentId) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(authority).appendPath(PATH_DOCUMENT).appendPath(documentId).build();
    }

    public static Uri buildDocumentUriUsingTree(Uri treeUri, String documentId) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(treeUri.getAuthority()).appendPath(PATH_TREE).appendPath(getTreeDocumentId(treeUri)).appendPath(PATH_DOCUMENT).appendPath(documentId).build();
    }

    public static Uri buildDocumentUriMaybeUsingTree(Uri baseUri, String documentId) {
        if (isTreeUri(baseUri)) {
            return buildDocumentUriUsingTree(baseUri, documentId);
        }
        return buildDocumentUri(baseUri.getAuthority(), documentId);
    }

    public static Uri buildChildDocumentsUri(String authority, String parentDocumentId) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(authority).appendPath(PATH_DOCUMENT).appendPath(parentDocumentId).appendPath(PATH_CHILDREN).build();
    }

    public static Uri buildChildDocumentsUriUsingTree(Uri treeUri, String parentDocumentId) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(treeUri.getAuthority()).appendPath(PATH_TREE).appendPath(getTreeDocumentId(treeUri)).appendPath(PATH_DOCUMENT).appendPath(parentDocumentId).appendPath(PATH_CHILDREN).build();
    }

    public static Uri buildSearchDocumentsUri(String authority, String rootId, String query) {
        return new Builder().scheme(VoiceInteractionSession.KEY_CONTENT).authority(authority).appendPath(PATH_ROOT).appendPath(rootId).appendPath(PATH_SEARCH).appendQueryParameter(PARAM_QUERY, query).build();
    }

    public static boolean isDocumentUri(Context context, Uri uri) {
        boolean z = false;
        if (isContentUri(uri) && isDocumentsProvider(context, uri.getAuthority())) {
            List<String> paths = uri.getPathSegments();
            if (paths.size() == 2) {
                return PATH_DOCUMENT.equals(paths.get(0));
            }
            if (paths.size() == 4) {
                if (PATH_TREE.equals(paths.get(0))) {
                    z = PATH_DOCUMENT.equals(paths.get(2));
                }
                return z;
            }
        }
        return false;
    }

    public static boolean isRootUri(Context context, Uri uri) {
        boolean z = false;
        if (!isContentUri(uri) || !isDocumentsProvider(context, uri.getAuthority())) {
            return false;
        }
        List<String> paths = uri.getPathSegments();
        if (paths.size() == 2) {
            z = PATH_ROOT.equals(paths.get(0));
        }
        return z;
    }

    public static boolean isContentUri(Uri uri) {
        return uri != null ? VoiceInteractionSession.KEY_CONTENT.equals(uri.getScheme()) : false;
    }

    public static boolean isTreeUri(Uri uri) {
        List<String> paths = uri.getPathSegments();
        if (paths.size() >= 2) {
            return PATH_TREE.equals(paths.get(0));
        }
        return false;
    }

    private static boolean isDocumentsProvider(Context context, String authority) {
        for (ResolveInfo info : context.getPackageManager().queryIntentContentProviders(new Intent(PROVIDER_INTERFACE), 0)) {
            if (authority.equals(info.providerInfo.authority)) {
                return true;
            }
        }
        return false;
    }

    public static String getRootId(Uri rootUri) {
        List<String> paths = rootUri.getPathSegments();
        if (paths.size() >= 2 && PATH_ROOT.equals(paths.get(0))) {
            return (String) paths.get(1);
        }
        throw new IllegalArgumentException("Invalid URI: " + rootUri);
    }

    public static String getDocumentId(Uri documentUri) {
        List<String> paths = documentUri.getPathSegments();
        if (paths.size() >= 2 && PATH_DOCUMENT.equals(paths.get(0))) {
            return (String) paths.get(1);
        }
        if (paths.size() >= 4 && PATH_TREE.equals(paths.get(0)) && PATH_DOCUMENT.equals(paths.get(2))) {
            return (String) paths.get(3);
        }
        throw new IllegalArgumentException("Invalid URI: " + documentUri);
    }

    public static String getTreeDocumentId(Uri documentUri) {
        List<String> paths = documentUri.getPathSegments();
        if (paths.size() >= 2 && PATH_TREE.equals(paths.get(0))) {
            return (String) paths.get(1);
        }
        throw new IllegalArgumentException("Invalid URI: " + documentUri);
    }

    public static String getSearchDocumentsQuery(Uri searchDocumentsUri) {
        return searchDocumentsUri.getQueryParameter(PARAM_QUERY);
    }

    public static Uri setManageMode(Uri uri) {
        return uri.buildUpon().appendQueryParameter(PARAM_MANAGE, "true").build();
    }

    public static boolean isManageMode(Uri uri) {
        return uri.getBooleanQueryParameter(PARAM_MANAGE, false);
    }

    public static Bitmap getDocumentThumbnail(ContentResolver resolver, Uri documentUri, Point size, CancellationSignal signal) {
        ContentProviderClient client = resolver.acquireUnstableContentProviderClient(documentUri.getAuthority());
        try {
            Bitmap documentThumbnail = getDocumentThumbnail(client, documentUri, size, signal);
            ContentProviderClient.releaseQuietly(client);
            return documentThumbnail;
        } catch (Exception e) {
            if (!(e instanceof OperationCanceledException)) {
                Log.w(TAG, "Failed to load thumbnail for " + documentUri + ": " + e);
            }
            ContentProviderClient.releaseQuietly(client);
            return null;
        } catch (Throwable th) {
            ContentProviderClient.releaseQuietly(client);
        }
    }

    public static Bitmap getDocumentThumbnail(ContentProviderClient client, Uri documentUri, Point size, CancellationSignal signal) throws RemoteException, IOException {
        Bundle openOpts = new Bundle();
        openOpts.putParcelable(ContentResolver.EXTRA_SIZE, size);
        AutoCloseable autoCloseable = null;
        FileDescriptor fd;
        try {
            AssetFileDescriptor afd = client.openTypedAssetFileDescriptor(documentUri, "image/*", openOpts, signal);
            if (afd == null) {
                IoUtils.closeQuietly(afd);
                return null;
            }
            Bitmap bitmap;
            fd = afd.getFileDescriptor();
            long offset = afd.getStartOffset();
            InputStream is = null;
            Os.lseek(fd, offset, OsConstants.SEEK_SET);
            Options opts = new Options();
            opts.inJustDecodeBounds = true;
            if (is != null) {
                BitmapFactory.decodeStream(is, null, opts);
            } else {
                BitmapFactory.decodeFileDescriptor(fd, null, opts);
            }
            int widthSample = opts.outWidth / size.x;
            int heightSample = opts.outHeight / size.y;
            opts.inThumbnailMode = true;
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = Math.min(widthSample, heightSample);
            if (is != null) {
                is.reset();
                bitmap = BitmapFactory.decodeStream(is, null, opts);
            } else {
                try {
                    Os.lseek(fd, offset, OsConstants.SEEK_SET);
                } catch (ErrnoException e) {
                    e.rethrowAsIOException();
                }
                bitmap = BitmapFactory.decodeFileDescriptor(fd, null, opts);
            }
            Bundle extras = afd.getExtras();
            int orientation = extras != null ? extras.getInt(EXTRA_ORIENTATION, 0) : 0;
            if (orientation != 0) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Matrix m = new Matrix();
                m.setRotate((float) orientation, (float) (width / 2), (float) (height / 2));
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, false);
            }
            IoUtils.closeQuietly(afd);
            return bitmap;
        } catch (ErrnoException e2) {
            InputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fd), THUMBNAIL_BUFFER_SIZE);
            bufferedInputStream.mark(THUMBNAIL_BUFFER_SIZE);
        } catch (Throwable th) {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    public static Uri createDocument(ContentProviderClient client, Uri parentDocumentUri, String mimeType, String displayName) throws RemoteException {
        Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, parentDocumentUri);
        in.putString(Voicemails.MIME_TYPE, mimeType);
        in.putString(OpenableColumns.DISPLAY_NAME, displayName);
        return (Uri) client.call(METHOD_CREATE_DOCUMENT, null, in).getParcelable(EXTRA_URI);
    }

    public static boolean isChildDocument(ContentProviderClient client, Uri parentDocumentUri, Uri childDocumentUri) throws RemoteException {
        Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, parentDocumentUri);
        in.putParcelable(EXTRA_TARGET_URI, childDocumentUri);
        Bundle out = client.call(METHOD_IS_CHILD_DOCUMENT, null, in);
        if (out == null) {
            throw new RemoteException("Failed to get a reponse from isChildDocument query.");
        } else if (out.containsKey(EXTRA_RESULT)) {
            return out.getBoolean(EXTRA_RESULT);
        } else {
            throw new RemoteException("Response did not include result field..");
        }
    }

    public static Uri renameDocument(ContentProviderClient client, Uri documentUri, String displayName) throws RemoteException {
        Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, documentUri);
        in.putString(OpenableColumns.DISPLAY_NAME, displayName);
        Uri outUri = (Uri) client.call(METHOD_RENAME_DOCUMENT, null, in).getParcelable(EXTRA_URI);
        return outUri != null ? outUri : documentUri;
    }

    public static void deleteDocument(ContentProviderClient client, Uri documentUri) throws RemoteException {
        Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, documentUri);
        client.call(METHOD_DELETE_DOCUMENT, null, in);
    }

    public static Uri copyDocument(ContentProviderClient client, Uri sourceDocumentUri, Uri targetParentDocumentUri) throws RemoteException {
        Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, sourceDocumentUri);
        in.putParcelable(EXTRA_TARGET_URI, targetParentDocumentUri);
        return (Uri) client.call(METHOD_COPY_DOCUMENT, null, in).getParcelable(EXTRA_URI);
    }

    public static Uri moveDocument(ContentProviderClient client, Uri sourceDocumentUri, Uri sourceParentDocumentUri, Uri targetParentDocumentUri) throws RemoteException {
        Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, sourceDocumentUri);
        in.putParcelable(EXTRA_PARENT_URI, sourceParentDocumentUri);
        in.putParcelable(EXTRA_TARGET_URI, targetParentDocumentUri);
        return (Uri) client.call(METHOD_MOVE_DOCUMENT, null, in).getParcelable(EXTRA_URI);
    }

    public static void removeDocument(ContentProviderClient client, Uri documentUri, Uri parentDocumentUri) throws RemoteException {
        Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, documentUri);
        in.putParcelable(EXTRA_PARENT_URI, parentDocumentUri);
        client.call(METHOD_REMOVE_DOCUMENT, null, in);
    }

    public static AssetFileDescriptor openImageThumbnail(File file) throws FileNotFoundException {
        Bundle extras;
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, KeymasterDefs.KM_ENUM);
        Bundle bundle = null;
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)) {
                case Engine.DEFAULT_STREAM /*3*/:
                    extras = new Bundle(1);
                    extras.putInt(EXTRA_ORIENTATION, BluetoothAssignedNumbers.BDE_TECHNOLOGY);
                    bundle = extras;
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                    extras = new Bundle(1);
                    try {
                        extras.putInt(EXTRA_ORIENTATION, 90);
                        bundle = extras;
                        break;
                    } catch (IOException e) {
                        bundle = extras;
                        break;
                    }
                case AudioState.ROUTE_SPEAKER /*8*/:
                    extras = new Bundle(1);
                    extras.putInt(EXTRA_ORIENTATION, AnqpInformationElement.ANQP_TDLS_CAP);
                    bundle = extras;
                    break;
            }
            long[] thumb = exif.getThumbnailRange();
            if (thumb != null) {
                return new AssetFileDescriptor(pfd, thumb[0], thumb[1], bundle);
            }
        } catch (IOException e2) {
        }
        return new AssetFileDescriptor(pfd, 0, -1, bundle);
    }
}
