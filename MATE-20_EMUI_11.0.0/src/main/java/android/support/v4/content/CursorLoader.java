package android.support.v4.content;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class CursorLoader extends AsyncTaskLoader<Cursor> {
    CancellationSignal mCancellationSignal;
    Cursor mCursor;
    final Loader<Cursor>.ForceLoadContentObserver mObserver = new Loader.ForceLoadContentObserver();
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;
    Uri mUri;

    @Override // android.support.v4.content.AsyncTaskLoader
    public Cursor loadInBackground() {
        synchronized (this) {
            if (!isLoadInBackgroundCanceled()) {
                this.mCancellationSignal = new CancellationSignal();
            } else {
                throw new OperationCanceledException();
            }
        }
        try {
            Cursor cursor = ContentResolverCompat.query(getContext().getContentResolver(), this.mUri, this.mProjection, this.mSelection, this.mSelectionArgs, this.mSortOrder, this.mCancellationSignal);
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

    @Override // android.support.v4.content.AsyncTaskLoader
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

    public CursorLoader(@NonNull Context context) {
        super(context);
    }

    public CursorLoader(@NonNull Context context, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        super(context);
        this.mUri = uri;
        this.mProjection = projection;
        this.mSelection = selection;
        this.mSelectionArgs = selectionArgs;
        this.mSortOrder = sortOrder;
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.content.Loader
    public void onStartLoading() {
        if (this.mCursor != null) {
            deliverResult(this.mCursor);
        }
        if (takeContentChanged() || this.mCursor == null) {
            forceLoad();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.content.Loader
    public void onStopLoading() {
        cancelLoad();
    }

    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.content.Loader
    public void onReset() {
        super.onReset();
        onStopLoading();
        if (this.mCursor != null && !this.mCursor.isClosed()) {
            this.mCursor.close();
        }
        this.mCursor = null;
    }

    @NonNull
    public Uri getUri() {
        return this.mUri;
    }

    public void setUri(@NonNull Uri uri) {
        this.mUri = uri;
    }

    @Nullable
    public String[] getProjection() {
        return this.mProjection;
    }

    public void setProjection(@Nullable String[] projection) {
        this.mProjection = projection;
    }

    @Nullable
    public String getSelection() {
        return this.mSelection;
    }

    public void setSelection(@Nullable String selection) {
        this.mSelection = selection;
    }

    @Nullable
    public String[] getSelectionArgs() {
        return this.mSelectionArgs;
    }

    public void setSelectionArgs(@Nullable String[] selectionArgs) {
        this.mSelectionArgs = selectionArgs;
    }

    @Nullable
    public String getSortOrder() {
        return this.mSortOrder;
    }

    public void setSortOrder(@Nullable String sortOrder) {
        this.mSortOrder = sortOrder;
    }

    @Override // android.support.v4.content.AsyncTaskLoader, android.support.v4.content.Loader
    @Deprecated
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
