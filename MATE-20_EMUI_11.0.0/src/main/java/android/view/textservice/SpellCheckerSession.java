package android.view.textservice;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.textservice.ISpellCheckerSession;
import com.android.internal.textservice.ISpellCheckerSessionListener;
import com.android.internal.textservice.ITextServicesSessionListener;
import dalvik.system.CloseGuard;
import java.util.LinkedList;
import java.util.Queue;

public class SpellCheckerSession {
    private static final boolean DBG = false;
    private static final int MSG_ON_GET_SUGGESTION_MULTIPLE = 1;
    private static final int MSG_ON_GET_SUGGESTION_MULTIPLE_FOR_SENTENCE = 2;
    public static final String SERVICE_META_DATA = "android.view.textservice.scs";
    private static final String TAG = SpellCheckerSession.class.getSimpleName();
    private final CloseGuard mGuard = CloseGuard.get();
    private final Handler mHandler = new Handler() {
        /* class android.view.textservice.SpellCheckerSession.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                SpellCheckerSession.this.handleOnGetSuggestionsMultiple((SuggestionsInfo[]) msg.obj);
            } else if (i == 2) {
                SpellCheckerSession.this.handleOnGetSentenceSuggestionsMultiple((SentenceSuggestionsInfo[]) msg.obj);
            }
        }
    };
    private final InternalListener mInternalListener;
    private final SpellCheckerInfo mSpellCheckerInfo;
    @UnsupportedAppUsage
    private final SpellCheckerSessionListener mSpellCheckerSessionListener;
    private final SpellCheckerSessionListenerImpl mSpellCheckerSessionListenerImpl;
    private final TextServicesManager mTextServicesManager;

    public interface SpellCheckerSessionListener {
        void onGetSentenceSuggestions(SentenceSuggestionsInfo[] sentenceSuggestionsInfoArr);

        void onGetSuggestions(SuggestionsInfo[] suggestionsInfoArr);
    }

    public SpellCheckerSession(SpellCheckerInfo info, TextServicesManager tsm, SpellCheckerSessionListener listener) {
        if (info == null || listener == null || tsm == null) {
            throw new NullPointerException();
        }
        this.mSpellCheckerInfo = info;
        this.mSpellCheckerSessionListenerImpl = new SpellCheckerSessionListenerImpl(this.mHandler);
        this.mInternalListener = new InternalListener(this.mSpellCheckerSessionListenerImpl);
        this.mTextServicesManager = tsm;
        this.mSpellCheckerSessionListener = listener;
        this.mGuard.open("finishSession");
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
        this.mGuard.close();
        this.mSpellCheckerSessionListenerImpl.close();
        this.mTextServicesManager.finishSpellCheckerService(this.mSpellCheckerSessionListenerImpl);
    }

    public void getSentenceSuggestions(TextInfo[] textInfos, int suggestionsLimit) {
        this.mSpellCheckerSessionListenerImpl.getSentenceSuggestionsMultiple(textInfos, suggestionsLimit);
    }

    @Deprecated
    public void getSuggestions(TextInfo textInfo, int suggestionsLimit) {
        getSuggestions(new TextInfo[]{textInfo}, suggestionsLimit, false);
    }

    @Deprecated
    public void getSuggestions(TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
        this.mSpellCheckerSessionListenerImpl.getSuggestionsMultiple(textInfos, suggestionsLimit, sequentialWords);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnGetSuggestionsMultiple(SuggestionsInfo[] suggestionInfos) {
        this.mSpellCheckerSessionListener.onGetSuggestions(suggestionInfos);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnGetSentenceSuggestionsMultiple(SentenceSuggestionsInfo[] suggestionInfos) {
        this.mSpellCheckerSessionListener.onGetSentenceSuggestions(suggestionInfos);
    }

    /* access modifiers changed from: private */
    public static final class SpellCheckerSessionListenerImpl extends ISpellCheckerSessionListener.Stub {
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
        private final Queue<SpellCheckerParams> mPendingTasks = new LinkedList();
        private int mState = 0;
        private HandlerThread mThread;

        private static String taskToString(int task) {
            if (task == 1) {
                return "TASK_CANCEL";
            }
            if (task == 2) {
                return "TASK_GET_SUGGESTIONS_MULTIPLE";
            }
            if (task == 3) {
                return "TASK_CLOSE";
            }
            if (task == 4) {
                return "TASK_GET_SUGGESTIONS_MULTIPLE_FOR_SENTENCE";
            }
            return "Unexpected task=" + task;
        }

        private static String stateToString(int state) {
            if (state == 0) {
                return "STATE_WAIT_CONNECTION";
            }
            if (state == 1) {
                return "STATE_CONNECTED";
            }
            if (state == 2) {
                return "STATE_CLOSED_AFTER_CONNECTION";
            }
            if (state == 3) {
                return "STATE_CLOSED_BEFORE_CONNECTION";
            }
            return "Unexpected state=" + state;
        }

        public SpellCheckerSessionListenerImpl(Handler handler) {
            this.mHandler = handler;
        }

        /* access modifiers changed from: private */
        public static class SpellCheckerParams {
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

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void processTask(ISpellCheckerSession session, SpellCheckerParams scp, boolean async) {
            Handler handler;
            if (async || (handler = this.mAsyncHandler) == null) {
                int i = scp.mWhat;
                if (i == 1) {
                    try {
                        session.onCancel();
                    } catch (RemoteException e) {
                        String str = SpellCheckerSession.TAG;
                        Log.e(str, "Failed to cancel " + e);
                    }
                } else if (i == 2) {
                    try {
                        session.onGetSuggestionsMultiple(scp.mTextInfos, scp.mSuggestionsLimit, scp.mSequentialWords);
                    } catch (RemoteException e2) {
                        String str2 = SpellCheckerSession.TAG;
                        Log.e(str2, "Failed to get suggestions " + e2);
                    }
                } else if (i == 3) {
                    try {
                        session.onClose();
                    } catch (RemoteException e3) {
                        String str3 = SpellCheckerSession.TAG;
                        Log.e(str3, "Failed to close " + e3);
                    }
                } else if (i == 4) {
                    try {
                        session.onGetSentenceSuggestionsMultiple(scp.mTextInfos, scp.mSuggestionsLimit);
                    } catch (RemoteException e4) {
                        String str4 = SpellCheckerSession.TAG;
                        Log.e(str4, "Failed to get suggestions " + e4);
                    }
                }
            } else {
                scp.mSession = session;
                handler.sendMessage(Message.obtain(handler, 1, scp));
            }
            if (scp.mWhat == 3) {
                synchronized (this) {
                    processCloseLocked();
                }
            }
        }

        private void processCloseLocked() {
            this.mISpellCheckerSession = null;
            HandlerThread handlerThread = this.mThread;
            if (handlerThread != null) {
                handlerThread.quit();
            }
            this.mHandler = null;
            this.mPendingTasks.clear();
            this.mThread = null;
            this.mAsyncHandler = null;
            int i = this.mState;
            if (i == 0) {
                this.mState = 3;
            } else if (i != 1) {
                String str = SpellCheckerSession.TAG;
                Log.e(str, "processCloseLocked is called unexpectedly. mState=" + stateToString(this.mState));
            } else {
                this.mState = 2;
            }
        }

        public void onServiceConnected(ISpellCheckerSession session) {
            synchronized (this) {
                int i = this.mState;
                if (i != 0) {
                    if (i != 3) {
                        String str = SpellCheckerSession.TAG;
                        Log.e(str, "ignoring onServiceConnected due to unexpected mState=" + stateToString(this.mState));
                    }
                } else if (session == null) {
                    Log.e(SpellCheckerSession.TAG, "ignoring onServiceConnected due to session=null");
                } else {
                    this.mISpellCheckerSession = session;
                    if ((session.asBinder() instanceof Binder) && this.mThread == null) {
                        this.mThread = new HandlerThread("SpellCheckerSession", 10);
                        this.mThread.start();
                        this.mAsyncHandler = new Handler(this.mThread.getLooper()) {
                            /* class android.view.textservice.SpellCheckerSession.SpellCheckerSessionListenerImpl.AnonymousClass1 */

                            @Override // android.os.Handler
                            public void handleMessage(Message msg) {
                                SpellCheckerParams scp = (SpellCheckerParams) msg.obj;
                                SpellCheckerSessionListenerImpl.this.processTask(scp.mSession, scp, true);
                            }
                        };
                    }
                    this.mState = 1;
                    while (!this.mPendingTasks.isEmpty()) {
                        processTask(session, this.mPendingTasks.poll(), false);
                    }
                }
            }
        }

        public void cancel() {
            processOrEnqueueTask(new SpellCheckerParams(1, null, 0, false));
        }

        public void getSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
            processOrEnqueueTask(new SpellCheckerParams(2, textInfos, suggestionsLimit, sequentialWords));
        }

        public void getSentenceSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit) {
            processOrEnqueueTask(new SpellCheckerParams(4, textInfos, suggestionsLimit, false));
        }

        public void close() {
            processOrEnqueueTask(new SpellCheckerParams(3, null, 0, false));
        }

        public boolean isDisconnected() {
            boolean z;
            synchronized (this) {
                z = true;
                if (this.mState == 1) {
                    z = false;
                }
            }
            return z;
        }

        /* JADX INFO: Multiple debug info for r0v4 com.android.internal.textservice.ISpellCheckerSession: [D('closeTask' android.view.textservice.SpellCheckerSession$SpellCheckerSessionListenerImpl$SpellCheckerParams), D('session' com.android.internal.textservice.ISpellCheckerSession)] */
        private void processOrEnqueueTask(SpellCheckerParams scp) {
            synchronized (this) {
                if (scp.mWhat != 3 || (this.mState != 2 && this.mState != 3)) {
                    if (this.mState != 0 && this.mState != 1) {
                        String str = SpellCheckerSession.TAG;
                        Log.e(str, "ignoring processOrEnqueueTask due to unexpected mState=" + stateToString(this.mState) + " scp.mWhat=" + taskToString(scp.mWhat));
                    } else if (this.mState != 0) {
                        processTask(this.mISpellCheckerSession, scp, false);
                    } else if (scp.mWhat == 3) {
                        processCloseLocked();
                    } else {
                        SpellCheckerParams closeTask = null;
                        if (scp.mWhat == 1) {
                            while (!this.mPendingTasks.isEmpty()) {
                                SpellCheckerParams tmp = this.mPendingTasks.poll();
                                if (tmp.mWhat == 3) {
                                    closeTask = tmp;
                                }
                            }
                        }
                        this.mPendingTasks.offer(scp);
                        if (closeTask != null) {
                            this.mPendingTasks.offer(closeTask);
                        }
                    }
                }
            }
        }

        @Override // com.android.internal.textservice.ISpellCheckerSessionListener
        public void onGetSuggestions(SuggestionsInfo[] results) {
            synchronized (this) {
                if (this.mHandler != null) {
                    this.mHandler.sendMessage(Message.obtain(this.mHandler, 1, results));
                }
            }
        }

        @Override // com.android.internal.textservice.ISpellCheckerSessionListener
        public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
            synchronized (this) {
                if (this.mHandler != null) {
                    this.mHandler.sendMessage(Message.obtain(this.mHandler, 2, results));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class InternalListener extends ITextServicesSessionListener.Stub {
        private final SpellCheckerSessionListenerImpl mParentSpellCheckerSessionListenerImpl;

        public InternalListener(SpellCheckerSessionListenerImpl spellCheckerSessionListenerImpl) {
            this.mParentSpellCheckerSessionListenerImpl = spellCheckerSessionListenerImpl;
        }

        @Override // com.android.internal.textservice.ITextServicesSessionListener
        public void onServiceConnected(ISpellCheckerSession session) {
            this.mParentSpellCheckerSessionListenerImpl.onServiceConnected(session);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mGuard != null) {
                this.mGuard.warnIfOpen();
                close();
            }
        } finally {
            super.finalize();
        }
    }

    public ITextServicesSessionListener getTextServicesSessionListener() {
        return this.mInternalListener;
    }

    public ISpellCheckerSessionListener getSpellCheckerSessionListener() {
        return this.mSpellCheckerSessionListenerImpl;
    }
}
