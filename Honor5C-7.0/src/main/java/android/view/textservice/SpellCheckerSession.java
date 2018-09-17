package android.view.textservice;

import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.textservice.ISpellCheckerSession;
import com.android.internal.textservice.ISpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesManager;
import com.android.internal.textservice.ITextServicesSessionListener;
import com.android.internal.textservice.ITextServicesSessionListener.Stub;
import java.util.LinkedList;
import java.util.Queue;

public class SpellCheckerSession {
    private static final boolean DBG = false;
    private static final int MSG_ON_GET_SUGGESTION_MULTIPLE = 1;
    private static final int MSG_ON_GET_SUGGESTION_MULTIPLE_FOR_SENTENCE = 2;
    public static final String SERVICE_META_DATA = "android.view.textservice.scs";
    private static final String TAG = null;
    private final Handler mHandler;
    private final InternalListener mInternalListener;
    private boolean mIsUsed;
    private final SpellCheckerInfo mSpellCheckerInfo;
    private final SpellCheckerSessionListener mSpellCheckerSessionListener;
    private final SpellCheckerSessionListenerImpl mSpellCheckerSessionListenerImpl;
    private final SpellCheckerSubtype mSubtype;
    private final ITextServicesManager mTextServicesManager;

    private static class InternalListener extends Stub {
        private final SpellCheckerSessionListenerImpl mParentSpellCheckerSessionListenerImpl;

        public InternalListener(SpellCheckerSessionListenerImpl spellCheckerSessionListenerImpl) {
            this.mParentSpellCheckerSessionListenerImpl = spellCheckerSessionListenerImpl;
        }

        public void onServiceConnected(ISpellCheckerSession session) {
            this.mParentSpellCheckerSessionListenerImpl.onServiceConnected(session);
        }
    }

    public interface SpellCheckerSessionListener {
        void onGetSentenceSuggestions(SentenceSuggestionsInfo[] sentenceSuggestionsInfoArr);

        void onGetSuggestions(SuggestionsInfo[] suggestionsInfoArr);
    }

    private static class SpellCheckerSessionListenerImpl extends ISpellCheckerSessionListener.Stub {
        private static final int STATE_CLOSED_AFTER_CONNECTION = 2;
        private static final int STATE_CLOSED_BEFORE_CONNECTION = 3;
        private static final int STATE_CONNECTED = 1;
        private static final int STATE_WAIT_CONNECTION = 0;
        private static final int TASK_CANCEL = 1;
        private static final int TASK_CLOSE = 3;
        private static final int TASK_GET_SUGGESTIONS_MULTIPLE = 2;
        private static final int TASK_GET_SUGGESTIONS_MULTIPLE_FOR_SENTENCE = 4;
        private Handler mAsyncHandler;
        private Handler mHandler;
        private ISpellCheckerSession mISpellCheckerSession;
        private final Queue<SpellCheckerParams> mPendingTasks;
        private int mState;
        private HandlerThread mThread;

        /* renamed from: android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.1 */
        class AnonymousClass1 extends Handler {
            AnonymousClass1(Looper $anonymous0) {
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                SpellCheckerParams scp = msg.obj;
                SpellCheckerSessionListenerImpl.this.processTask(scp.mSession, scp, true);
            }
        }

        private static class SpellCheckerParams {
            public final boolean mSequentialWords;
            public ISpellCheckerSession mSession;
            public final int mSuggestionsLimit;
            public final TextInfo[] mTextInfos;
            public final int mWhat;

            public SpellCheckerParams(int what, TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
                this.mWhat = what;
                this.mTextInfos = textInfos;
                this.mSuggestionsLimit = suggestionsLimit;
                this.mSequentialWords = sequentialWords;
            }
        }

        private static String taskToString(int task) {
            switch (task) {
                case TASK_CANCEL /*1*/:
                    return "TASK_CANCEL";
                case TASK_GET_SUGGESTIONS_MULTIPLE /*2*/:
                    return "TASK_GET_SUGGESTIONS_MULTIPLE";
                case TASK_CLOSE /*3*/:
                    return "TASK_CLOSE";
                case TASK_GET_SUGGESTIONS_MULTIPLE_FOR_SENTENCE /*4*/:
                    return "TASK_GET_SUGGESTIONS_MULTIPLE_FOR_SENTENCE";
                default:
                    return "Unexpected task=" + task;
            }
        }

        private static String stateToString(int state) {
            switch (state) {
                case STATE_WAIT_CONNECTION /*0*/:
                    return "STATE_WAIT_CONNECTION";
                case TASK_CANCEL /*1*/:
                    return "STATE_CONNECTED";
                case TASK_GET_SUGGESTIONS_MULTIPLE /*2*/:
                    return "STATE_CLOSED_AFTER_CONNECTION";
                case TASK_CLOSE /*3*/:
                    return "STATE_CLOSED_BEFORE_CONNECTION";
                default:
                    return "Unexpected state=" + state;
            }
        }

        public SpellCheckerSessionListenerImpl(Handler handler) {
            this.mPendingTasks = new LinkedList();
            this.mState = STATE_WAIT_CONNECTION;
            this.mHandler = handler;
        }

        private void processTask(ISpellCheckerSession session, SpellCheckerParams scp, boolean async) {
            if (async || this.mAsyncHandler == null) {
                switch (scp.mWhat) {
                    case TASK_CANCEL /*1*/:
                        try {
                            session.onCancel();
                            break;
                        } catch (RemoteException e) {
                            Log.e(SpellCheckerSession.TAG, "Failed to cancel " + e);
                            break;
                        }
                    case TASK_GET_SUGGESTIONS_MULTIPLE /*2*/:
                        try {
                            session.onGetSuggestionsMultiple(scp.mTextInfos, scp.mSuggestionsLimit, scp.mSequentialWords);
                            break;
                        } catch (RemoteException e2) {
                            Log.e(SpellCheckerSession.TAG, "Failed to get suggestions " + e2);
                            break;
                        }
                    case TASK_CLOSE /*3*/:
                        try {
                            session.onClose();
                            break;
                        } catch (RemoteException e22) {
                            Log.e(SpellCheckerSession.TAG, "Failed to close " + e22);
                            break;
                        }
                    case TASK_GET_SUGGESTIONS_MULTIPLE_FOR_SENTENCE /*4*/:
                        try {
                            session.onGetSentenceSuggestionsMultiple(scp.mTextInfos, scp.mSuggestionsLimit);
                            break;
                        } catch (RemoteException e222) {
                            Log.e(SpellCheckerSession.TAG, "Failed to get suggestions " + e222);
                            break;
                        }
                }
            }
            scp.mSession = session;
            this.mAsyncHandler.sendMessage(Message.obtain(this.mAsyncHandler, TASK_CANCEL, scp));
            if (scp.mWhat == TASK_CLOSE) {
                synchronized (this) {
                    processCloseLocked();
                }
            }
        }

        private void processCloseLocked() {
            this.mISpellCheckerSession = null;
            if (this.mThread != null) {
                this.mThread.quit();
            }
            this.mHandler = null;
            this.mPendingTasks.clear();
            this.mThread = null;
            this.mAsyncHandler = null;
            switch (this.mState) {
                case STATE_WAIT_CONNECTION /*0*/:
                    this.mState = TASK_CLOSE;
                case TASK_CANCEL /*1*/:
                    this.mState = TASK_GET_SUGGESTIONS_MULTIPLE;
                default:
                    Log.e(SpellCheckerSession.TAG, "processCloseLocked is called unexpectedly. mState=" + stateToString(this.mState));
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized void onServiceConnected(ISpellCheckerSession session) {
            synchronized (this) {
                switch (this.mState) {
                    case STATE_WAIT_CONNECTION /*0*/:
                        if (session == null) {
                            Log.e(SpellCheckerSession.TAG, "ignoring onServiceConnected due to session=null");
                            return;
                        }
                        this.mISpellCheckerSession = session;
                        if ((session.asBinder() instanceof Binder) && this.mThread == null) {
                            this.mThread = new HandlerThread("SpellCheckerSession", 10);
                            this.mThread.start();
                            this.mAsyncHandler = new AnonymousClass1(this.mThread.getLooper());
                        }
                        this.mState = TASK_CANCEL;
                        while (!this.mPendingTasks.isEmpty()) {
                            processTask(session, (SpellCheckerParams) this.mPendingTasks.poll(), SpellCheckerSession.DBG);
                        }
                        return;
                    case TASK_CLOSE /*3*/:
                        return;
                    default:
                        Log.e(SpellCheckerSession.TAG, "ignoring onServiceConnected due to unexpected mState=" + stateToString(this.mState));
                        return;
                }
            }
        }

        public void cancel() {
            processOrEnqueueTask(new SpellCheckerParams(TASK_CANCEL, null, STATE_WAIT_CONNECTION, SpellCheckerSession.DBG));
        }

        public void getSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
            processOrEnqueueTask(new SpellCheckerParams(TASK_GET_SUGGESTIONS_MULTIPLE, textInfos, suggestionsLimit, sequentialWords));
        }

        public void getSentenceSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit) {
            processOrEnqueueTask(new SpellCheckerParams(TASK_GET_SUGGESTIONS_MULTIPLE_FOR_SENTENCE, textInfos, suggestionsLimit, SpellCheckerSession.DBG));
        }

        public void close() {
            processOrEnqueueTask(new SpellCheckerParams(TASK_CLOSE, null, STATE_WAIT_CONNECTION, SpellCheckerSession.DBG));
        }

        public boolean isDisconnected() {
            boolean z = true;
            synchronized (this) {
                if (this.mState == TASK_CANCEL) {
                    z = SpellCheckerSession.DBG;
                }
            }
            return z;
        }

        private void processOrEnqueueTask(SpellCheckerParams scp) {
            synchronized (this) {
                if (this.mState == 0 || this.mState == TASK_CANCEL) {
                    if (this.mState != 0) {
                        ISpellCheckerSession session = this.mISpellCheckerSession;
                        processTask(session, scp, SpellCheckerSession.DBG);
                        return;
                    } else if (scp.mWhat == TASK_CLOSE) {
                        processCloseLocked();
                        return;
                    } else {
                        Object obj = null;
                        if (scp.mWhat == TASK_CANCEL) {
                            while (!this.mPendingTasks.isEmpty()) {
                                SpellCheckerParams tmp = (SpellCheckerParams) this.mPendingTasks.poll();
                                if (tmp.mWhat == TASK_CLOSE) {
                                    SpellCheckerParams closeTask = tmp;
                                }
                            }
                        }
                        this.mPendingTasks.offer(scp);
                        if (obj != null) {
                            this.mPendingTasks.offer(obj);
                        }
                        return;
                    }
                } else {
                    Log.e(SpellCheckerSession.TAG, "ignoring processOrEnqueueTask due to unexpected mState=" + taskToString(scp.mWhat) + " scp.mWhat=" + taskToString(scp.mWhat));
                }
            }
        }

        public void onGetSuggestions(SuggestionsInfo[] results) {
            synchronized (this) {
                if (this.mHandler != null) {
                    this.mHandler.sendMessage(Message.obtain(this.mHandler, TASK_CANCEL, results));
                }
            }
        }

        public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
            synchronized (this) {
                if (this.mHandler != null) {
                    this.mHandler.sendMessage(Message.obtain(this.mHandler, TASK_GET_SUGGESTIONS_MULTIPLE, results));
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.textservice.SpellCheckerSession.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.textservice.SpellCheckerSession.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.SpellCheckerSession.<clinit>():void");
    }

    public SpellCheckerSession(SpellCheckerInfo info, ITextServicesManager tsm, SpellCheckerSessionListener listener, SpellCheckerSubtype subtype) {
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SpellCheckerSession.MSG_ON_GET_SUGGESTION_MULTIPLE /*1*/:
                        SpellCheckerSession.this.handleOnGetSuggestionsMultiple((SuggestionsInfo[]) msg.obj);
                    case SpellCheckerSession.MSG_ON_GET_SUGGESTION_MULTIPLE_FOR_SENTENCE /*2*/:
                        SpellCheckerSession.this.handleOnGetSentenceSuggestionsMultiple((SentenceSuggestionsInfo[]) msg.obj);
                    default:
                }
            }
        };
        if (info == null || listener == null || tsm == null) {
            throw new NullPointerException();
        }
        this.mSpellCheckerInfo = info;
        this.mSpellCheckerSessionListenerImpl = new SpellCheckerSessionListenerImpl(this.mHandler);
        this.mInternalListener = new InternalListener(this.mSpellCheckerSessionListenerImpl);
        this.mTextServicesManager = tsm;
        this.mIsUsed = true;
        this.mSpellCheckerSessionListener = listener;
        this.mSubtype = subtype;
    }

    public boolean isSessionDisconnected() {
        return this.mSpellCheckerSessionListenerImpl.isDisconnected();
    }

    public SpellCheckerInfo getSpellChecker() {
        return this.mSpellCheckerInfo;
    }

    public void cancel() {
        this.mSpellCheckerSessionListenerImpl.cancel();
    }

    public void close() {
        this.mIsUsed = DBG;
        try {
            this.mSpellCheckerSessionListenerImpl.close();
            this.mTextServicesManager.finishSpellCheckerService(this.mSpellCheckerSessionListenerImpl);
        } catch (RemoteException e) {
        }
    }

    public void getSentenceSuggestions(TextInfo[] textInfos, int suggestionsLimit) {
        this.mSpellCheckerSessionListenerImpl.getSentenceSuggestionsMultiple(textInfos, suggestionsLimit);
    }

    @Deprecated
    public void getSuggestions(TextInfo textInfo, int suggestionsLimit) {
        TextInfo[] textInfoArr = new TextInfo[MSG_ON_GET_SUGGESTION_MULTIPLE];
        textInfoArr[0] = textInfo;
        getSuggestions(textInfoArr, suggestionsLimit, DBG);
    }

    @Deprecated
    public void getSuggestions(TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
        this.mSpellCheckerSessionListenerImpl.getSuggestionsMultiple(textInfos, suggestionsLimit, sequentialWords);
    }

    private void handleOnGetSuggestionsMultiple(SuggestionsInfo[] suggestionInfos) {
        this.mSpellCheckerSessionListener.onGetSuggestions(suggestionInfos);
    }

    private void handleOnGetSentenceSuggestionsMultiple(SentenceSuggestionsInfo[] suggestionInfos) {
        this.mSpellCheckerSessionListener.onGetSentenceSuggestions(suggestionInfos);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (this.mIsUsed) {
            Log.e(TAG, "SpellCheckerSession was not finished properly.You should call finishShession() when you finished to use a spell checker.");
            close();
        }
    }

    public ITextServicesSessionListener getTextServicesSessionListener() {
        return this.mInternalListener;
    }

    public ISpellCheckerSessionListener getSpellCheckerSessionListener() {
        return this.mSpellCheckerSessionListenerImpl;
    }
}
