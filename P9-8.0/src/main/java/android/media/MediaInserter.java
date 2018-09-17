package android.media;

import android.common.HwFrameworkFactory;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.net.Uri;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MediaInserter {
    private final int mBufferSizePerUri;
    private final HashMap<Uri, List<ContentValues>> mPriorityRowMap = new HashMap();
    private final ContentProviderClient mProvider;
    private final HashMap<Uri, List<ContentValues>> mRowMap = new HashMap();

    public MediaInserter(ContentProviderClient provider, int bufferSizePerUri) {
        this.mProvider = provider;
        this.mBufferSizePerUri = bufferSizePerUri;
    }

    public void insert(Uri tableUri, ContentValues values) throws RemoteException {
        insert(tableUri, values, false);
    }

    public void insertwithPriority(Uri tableUri, ContentValues values) throws RemoteException {
        insert(tableUri, values, true);
    }

    private void insert(Uri tableUri, ContentValues values, boolean priority) throws RemoteException {
        HashMap<Uri, List<ContentValues>> rowmap = priority ? this.mPriorityRowMap : this.mRowMap;
        List<ContentValues> list = (List) rowmap.get(tableUri);
        if (list == null) {
            list = new ArrayList();
            rowmap.put(tableUri, list);
        }
        list.add(new ContentValues(values));
        if (list.size() >= HwFrameworkFactory.getHwMediaScannerManager().getBufferSize(tableUri, this.mBufferSizePerUri)) {
            flushAllPriority();
            flush(tableUri, list);
        }
    }

    public void flushAll() throws RemoteException {
        flushAllPriority();
        for (Uri tableUri : this.mRowMap.keySet()) {
            flush(tableUri, (List) this.mRowMap.get(tableUri));
        }
        this.mRowMap.clear();
    }

    private void flushAllPriority() throws RemoteException {
        for (Uri tableUri : this.mPriorityRowMap.keySet()) {
            flush(tableUri, (List) this.mPriorityRowMap.get(tableUri));
        }
        this.mPriorityRowMap.clear();
    }

    private void flush(Uri tableUri, List<ContentValues> list) throws RemoteException {
        if (!list.isEmpty()) {
            this.mProvider.bulkInsert(tableUri, (ContentValues[]) list.toArray(new ContentValues[list.size()]));
            list.clear();
        }
    }
}
