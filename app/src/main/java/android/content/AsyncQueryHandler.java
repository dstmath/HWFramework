package android.content;

import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.lang.ref.WeakReference;

public abstract class AsyncQueryHandler extends Handler {
    private static final int EVENT_ARG_DELETE = 4;
    private static final int EVENT_ARG_INSERT = 2;
    private static final int EVENT_ARG_QUERY = 1;
    private static final int EVENT_ARG_UPDATE = 3;
    private static final String TAG = "AsyncQuery";
    private static final boolean localLOGV = false;
    private static Looper sLooper;
    final WeakReference<ContentResolver> mResolver;
    private Handler mWorkerThreadHandler;

    protected static final class WorkerArgs {
        public Object cookie;
        public Handler handler;
        public String orderBy;
        public String[] projection;
        public Object result;
        public String selection;
        public String[] selectionArgs;
        public Uri uri;
        public ContentValues values;

        protected WorkerArgs() {
        }
    }

    protected class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ContentResolver resolver = (ContentResolver) AsyncQueryHandler.this.mResolver.get();
            if (resolver != null) {
                WorkerArgs args = msg.obj;
                int token = msg.what;
                switch (msg.arg1) {
                    case AsyncQueryHandler.EVENT_ARG_QUERY /*1*/:
                        Cursor cursor;
                        try {
                            cursor = resolver.query(args.uri, args.projection, args.selection, args.selectionArgs, args.orderBy);
                            if (cursor != null) {
                                cursor.getCount();
                            }
                        } catch (Exception e) {
                            Log.w(AsyncQueryHandler.TAG, "Exception thrown during handling EVENT_ARG_QUERY", e);
                            cursor = null;
                        }
                        args.result = cursor;
                        break;
                    case AsyncQueryHandler.EVENT_ARG_INSERT /*2*/:
                        args.result = resolver.insert(args.uri, args.values);
                        break;
                    case AsyncQueryHandler.EVENT_ARG_UPDATE /*3*/:
                        args.result = Integer.valueOf(resolver.update(args.uri, args.values, args.selection, args.selectionArgs));
                        break;
                    case AsyncQueryHandler.EVENT_ARG_DELETE /*4*/:
                        args.result = Integer.valueOf(resolver.delete(args.uri, args.selection, args.selectionArgs));
                        break;
                }
                Message reply = args.handler.obtainMessage(token);
                reply.obj = args;
                reply.arg1 = msg.arg1;
                reply.sendToTarget();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.AsyncQueryHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.AsyncQueryHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.AsyncQueryHandler.<clinit>():void");
    }

    public AsyncQueryHandler(ContentResolver cr) {
        this.mResolver = new WeakReference(cr);
        synchronized (AsyncQueryHandler.class) {
            if (sLooper == null) {
                HandlerThread thread = new HandlerThread("AsyncQueryWorker");
                thread.start();
                sLooper = thread.getLooper();
            }
        }
        this.mWorkerThreadHandler = createHandler(sLooper);
    }

    protected Handler createHandler(Looper looper) {
        return new WorkerHandler(looper);
    }

    public void startQuery(int token, Object cookie, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        Message msg = this.mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_QUERY;
        WorkerArgs args = new WorkerArgs();
        args.handler = this;
        args.uri = uri;
        args.projection = projection;
        args.selection = selection;
        args.selectionArgs = selectionArgs;
        args.orderBy = orderBy;
        args.cookie = cookie;
        msg.obj = args;
        this.mWorkerThreadHandler.sendMessage(msg);
    }

    public final void cancelOperation(int token) {
        this.mWorkerThreadHandler.removeMessages(token);
    }

    public final void startInsert(int token, Object cookie, Uri uri, ContentValues initialValues) {
        Message msg = this.mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_INSERT;
        WorkerArgs args = new WorkerArgs();
        args.handler = this;
        args.uri = uri;
        args.cookie = cookie;
        args.values = initialValues;
        msg.obj = args;
        this.mWorkerThreadHandler.sendMessage(msg);
    }

    public final void startUpdate(int token, Object cookie, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Message msg = this.mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_UPDATE;
        WorkerArgs args = new WorkerArgs();
        args.handler = this;
        args.uri = uri;
        args.cookie = cookie;
        args.values = values;
        args.selection = selection;
        args.selectionArgs = selectionArgs;
        msg.obj = args;
        this.mWorkerThreadHandler.sendMessage(msg);
    }

    public final void startDelete(int token, Object cookie, Uri uri, String selection, String[] selectionArgs) {
        Message msg = this.mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_DELETE;
        WorkerArgs args = new WorkerArgs();
        args.handler = this;
        args.uri = uri;
        args.cookie = cookie;
        args.selection = selection;
        args.selectionArgs = selectionArgs;
        msg.obj = args;
        this.mWorkerThreadHandler.sendMessage(msg);
    }

    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
    }

    protected void onInsertComplete(int token, Object cookie, Uri uri) {
    }

    protected void onUpdateComplete(int token, Object cookie, int result) {
    }

    protected void onDeleteComplete(int token, Object cookie, int result) {
    }

    public void handleMessage(Message msg) {
        WorkerArgs args = msg.obj;
        int token = msg.what;
        switch (msg.arg1) {
            case EVENT_ARG_QUERY /*1*/:
                onQueryComplete(token, args.cookie, (Cursor) args.result);
            case EVENT_ARG_INSERT /*2*/:
                onInsertComplete(token, args.cookie, (Uri) args.result);
            case EVENT_ARG_UPDATE /*3*/:
                onUpdateComplete(token, args.cookie, ((Integer) args.result).intValue());
            case EVENT_ARG_DELETE /*4*/:
                onDeleteComplete(token, args.cookie, ((Integer) args.result).intValue());
            default:
        }
    }
}
