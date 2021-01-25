package android.content;

import android.annotation.UnsupportedAppUsage;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

@Deprecated
public class CursorLoader extends AsyncTaskLoader<Cursor> {
    @UnsupportedAppUsage
    CancellationSignal mCancellationSignal;
    Cursor mCursor;
    @UnsupportedAppUsage
    final Loader<Cursor>.ForceLoadContentObserver mObserver = new Loader.ForceLoadContentObserver();
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;
    Uri mUri;

    @Override // android.content.AsyncTaskLoader
    public Cursor loadInBackground() {
        synchronized (this) {
            if (!isLoadInBackgroundCanceled()) {
                this.mCancellationSignal = new CancellationSignal();
            } else {
                throw new OperationCanceledException();
            }
        }
        try {
            Cursor cursor = getContext().getContentResolver().query(this.mUri, this.mProjection, this.mSelection, this.mSelectionArgs, this.mSortOrder, this.mCancellationSignal);
            if (cursor != null) {
                try {
                    cursor.getCount();
                    cursor.registerContentObserver(this.mObserver);
                } catch (RuntimeException ex) {
                    cursor.close();
                    throw ex;
                }
            }
            synchronized (this) {
                this.mCancellationSignal = null;
            }
            return cursor;
        } catch (Throwable th) {
            synchronized (this) {
                this.mCancellationSignal = null;
                throw th;
            }
        }
    }

    @Override // android.content.AsyncTaskLoader
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();
        synchronized (this) {
            if (this.mCancellationSignal != null) {
                this.mCancellationSignal.cancel();
            }
        }
    }

    public void deliverResult(Cursor cursor) {
        if (!isReset()) {
            Cursor oldCursor = this.mCursor;
            this.mCursor = cursor;
            if (isStarted()) {
                super.deliverResult((CursorLoader) cursor);
            }
            if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
                oldCursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
    }

    public CursorLoader(Context context) {
        super(context);
    }

    public CursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context);
        this.mUri = uri;
        this.mProjection = projection;
        this.mSelection = selection;
        this.mSelectionArgs = selectionArgs;
        this.mSortOrder = sortOrder;
    }

    /* access modifiers changed from: protected */
    @Override // android.content.Loader
    public void onStartLoading() {
        Cursor cursor = this.mCursor;
        if (cursor != null) {
            deliverResult(cursor);
        }
        if (takeContentChanged() || this.mCursor == null) {
            forceLoad();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.content.Loader
    public void onStopLoading() {
        cancelLoad();
    }

    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.content.Loader
    public void onReset() {
        super.onReset();
        onStopLoading();
        Cursor cursor = this.mCursor;
        if (cursor != null && !cursor.isClosed()) {
            this.mCursor.close();
        }
        this.mCursor = null;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public void setUri(Uri uri) {
        this.mUri = uri;
    }

    public String[] getProjection() {
        return this.mProjection;
    }

    public void setProjection(String[] projection) {
        this.mProjection = projection;
    }

    public String getSelection() {
        return this.mSelection;
    }

    public void setSelection(String selection) {
        this.mSelection = selection;
    }

    public String[] getSelectionArgs() {
        return this.mSelectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        this.mSelectionArgs = selectionArgs;
    }

    public String getSortOrder() {
        return this.mSortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.mSortOrder = sortOrder;
    }

    @Override // android.content.AsyncTaskLoader, android.content.Loader
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print("mUri=");
        writer.println(this.mUri);
        writer.print(prefix);
        writer.print("mProjection=");
        writer.println(Arrays.toString(this.mProjection));
        writer.print(prefix);
        writer.print("mSelection=");
        writer.println(this.mSelection);
        writer.print(prefix);
        writer.print("mSelectionArgs=");
        writer.println(Arrays.toString(this.mSelectionArgs));
        writer.print(prefix);
        writer.print("mSortOrder=");
        writer.println(this.mSortOrder);
        writer.print(prefix);
        writer.print("mCursor=");
        writer.println(this.mCursor);
        writer.print(prefix);
        writer.print("mContentChanged=");
        writer.println(this.mContentChanged);
    }
}
