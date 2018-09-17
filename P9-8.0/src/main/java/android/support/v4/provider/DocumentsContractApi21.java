package android.support.v4.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.util.ArrayList;

@RequiresApi(21)
class DocumentsContractApi21 {
    private static final String TAG = "DocumentFile";

    DocumentsContractApi21() {
    }

    public static Uri createFile(Context context, Uri self, String mimeType, String displayName) {
        try {
            return DocumentsContract.createDocument(context.getContentResolver(), self, mimeType, displayName);
        } catch (Exception e) {
            return null;
        }
    }

    public static Uri createDirectory(Context context, Uri self, String displayName) {
        return createFile(context, self, "vnd.android.document/directory", displayName);
    }

    public static Uri prepareTreeUri(Uri treeUri) {
        return DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));
    }

    public static Uri[] listFiles(Context context, Uri self) {
        ContentResolver resolver = context.getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(self, DocumentsContract.getDocumentId(self));
        ArrayList<Uri> results = new ArrayList();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = resolver.query(childrenUri, new String[]{"document_id"}, null, null, null);
            while (autoCloseable.moveToNext()) {
                results.add(DocumentsContract.buildDocumentUriUsingTree(self, autoCloseable.getString(0)));
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed query: " + e);
        } finally {
            closeQuietly(autoCloseable);
        }
        return (Uri[]) results.toArray(new Uri[results.size()]);
    }

    public static Uri renameTo(Context context, Uri self, String displayName) {
        try {
            return DocumentsContract.renameDocument(context.getContentResolver(), self, displayName);
        } catch (Exception e) {
            return null;
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }
}
