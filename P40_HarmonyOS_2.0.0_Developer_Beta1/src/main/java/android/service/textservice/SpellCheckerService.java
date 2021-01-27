package android.service.textservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.method.WordIterator;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import com.android.internal.textservice.ISpellCheckerService;
import com.android.internal.textservice.ISpellCheckerServiceCallback;
import com.android.internal.textservice.ISpellCheckerSession;
import com.android.internal.textservice.ISpellCheckerSessionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

public abstract class SpellCheckerService extends Service {
    private static final boolean DBG = false;
    public static final String SERVICE_INTERFACE = "android.service.textservice.SpellCheckerService";
    private static final String TAG = SpellCheckerService.class.getSimpleName();
    private final SpellCheckerServiceBinder mBinder = new SpellCheckerServiceBinder(this);

    public abstract Session createSession();

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public static abstract class Session {
        private InternalISpellCheckerSession mInternalSession;
        private volatile SentenceLevelAdapter mSentenceLevelAdapter;

        public abstract void onCreate();

        public abstract SuggestionsInfo onGetSuggestions(TextInfo textInfo, int i);

        public final void setInternalISpellCheckerSession(InternalISpellCheckerSession session) {
            this.mInternalSession = session;
        }

        public SuggestionsInfo[] onGetSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
            int length = textInfos.length;
            SuggestionsInfo[] retval = new SuggestionsInfo[length];
            for (int i = 0; i < length; i++) {
                retval[i] = onGetSuggestions(textInfos[i], suggestionsLimit);
                retval[i].setCookieAndSequence(textInfos[i].getCookie(), textInfos[i].getSequence());
            }
            return retval;
        }

        public SentenceSuggestionsInfo[] onGetSentenceSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit) {
            if (textInfos == null || textInfos.length == 0) {
                return SentenceLevelAdapter.EMPTY_SENTENCE_SUGGESTIONS_INFOS;
            }
            if (this.mSentenceLevelAdapter == null) {
                synchronized (this) {
                    if (this.mSentenceLevelAdapter == null) {
                        String localeStr = getLocale();
                        if (!TextUtils.isEmpty(localeStr)) {
                            this.mSentenceLevelAdapter = new SentenceLevelAdapter(new Locale(localeStr));
                        }
                    }
                }
            }
            if (this.mSentenceLevelAdapter == null) {
                return SentenceLevelAdapter.EMPTY_SENTENCE_SUGGESTIONS_INFOS;
            }
            int infosSize = textInfos.length;
            SentenceSuggestionsInfo[] retval = new SentenceSuggestionsInfo[infosSize];
            for (int i = 0; i < infosSize; i++) {
                SentenceLevelAdapter.SentenceTextInfoParams textInfoParams = this.mSentenceLevelAdapter.getSplitWords(textInfos[i]);
                ArrayList<SentenceLevelAdapter.SentenceWordItem> mItems = textInfoParams.mItems;
                int itemsSize = mItems.size();
                TextInfo[] splitTextInfos = new TextInfo[itemsSize];
                for (int j = 0; j < itemsSize; j++) {
                    splitTextInfos[j] = mItems.get(j).mTextInfo;
                }
                retval[i] = SentenceLevelAdapter.reconstructSuggestions(textInfoParams, onGetSuggestionsMultiple(splitTextInfos, suggestionsLimit, true));
            }
            return retval;
        }

        public void onCancel() {
        }

        public void onClose() {
        }

        public String getLocale() {
            return this.mInternalSession.getLocale();
        }

        public Bundle getBundle() {
            return this.mInternalSession.getBundle();
        }
    }

    /* access modifiers changed from: private */
    public static class InternalISpellCheckerSession extends ISpellCheckerSession.Stub {
        private final Bundle mBundle;
        private ISpellCheckerSessionListener mListener;
        private final String mLocale;
        private final Session mSession;

        public InternalISpellCheckerSession(String locale, ISpellCheckerSessionListener listener, Bundle bundle, Session session) {
            this.mListener = listener;
            this.mSession = session;
            this.mLocale = locale;
            this.mBundle = bundle;
            session.setInternalISpellCheckerSession(this);
        }

        @Override // com.android.internal.textservice.ISpellCheckerSession
        public void onGetSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit, boolean sequentialWords) {
            int pri = Process.getThreadPriority(Process.myTid());
            try {
                Process.setThreadPriority(10);
                this.mListener.onGetSuggestions(this.mSession.onGetSuggestionsMultiple(textInfos, suggestionsLimit, sequentialWords));
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Process.setThreadPriority(pri);
                throw th;
            }
            Process.setThreadPriority(pri);
        }

        @Override // com.android.internal.textservice.ISpellCheckerSession
        public void onGetSentenceSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit) {
            try {
                this.mListener.onGetSentenceSuggestions(this.mSession.onGetSentenceSuggestionsMultiple(textInfos, suggestionsLimit));
            } catch (RemoteException e) {
            }
        }

        /* JADX INFO: finally extract failed */
        @Override // com.android.internal.textservice.ISpellCheckerSession
        public void onCancel() {
            int pri = Process.getThreadPriority(Process.myTid());
            try {
                Process.setThreadPriority(10);
                this.mSession.onCancel();
                Process.setThreadPriority(pri);
            } catch (Throwable th) {
                Process.setThreadPriority(pri);
                throw th;
            }
        }

        @Override // com.android.internal.textservice.ISpellCheckerSession
        public void onClose() {
            int pri = Process.getThreadPriority(Process.myTid());
            try {
                Process.setThreadPriority(10);
                this.mSession.onClose();
            } finally {
                Process.setThreadPriority(pri);
                this.mListener = null;
            }
        }

        public String getLocale() {
            return this.mLocale;
        }

        public Bundle getBundle() {
            return this.mBundle;
        }
    }

    private static class SpellCheckerServiceBinder extends ISpellCheckerService.Stub {
        private final WeakReference<SpellCheckerService> mInternalServiceRef;

        public SpellCheckerServiceBinder(SpellCheckerService service) {
            this.mInternalServiceRef = new WeakReference<>(service);
        }

        @Override // com.android.internal.textservice.ISpellCheckerService
        public void getISpellCheckerSession(String locale, ISpellCheckerSessionListener listener, Bundle bundle, ISpellCheckerServiceCallback callback) {
            InternalISpellCheckerSession internalSession;
            SpellCheckerService service = this.mInternalServiceRef.get();
            if (service == null) {
                internalSession = null;
            } else {
                Session session = service.createSession();
                InternalISpellCheckerSession internalSession2 = new InternalISpellCheckerSession(locale, listener, bundle, session);
                session.onCreate();
                internalSession = internalSession2;
            }
            try {
                callback.onSessionCreated(internalSession);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SentenceLevelAdapter {
        public static final SentenceSuggestionsInfo[] EMPTY_SENTENCE_SUGGESTIONS_INFOS = new SentenceSuggestionsInfo[0];
        private static final SuggestionsInfo EMPTY_SUGGESTIONS_INFO = new SuggestionsInfo(0, null);
        private final WordIterator mWordIterator;

        public static class SentenceWordItem {
            public final int mLength;
            public final int mStart;
            public final TextInfo mTextInfo;

            public SentenceWordItem(TextInfo ti, int start, int end) {
                this.mTextInfo = ti;
                this.mStart = start;
                this.mLength = end - start;
            }
        }

        public static class SentenceTextInfoParams {
            final ArrayList<SentenceWordItem> mItems;
            final TextInfo mOriginalTextInfo;
            final int mSize;

            public SentenceTextInfoParams(TextInfo ti, ArrayList<SentenceWordItem> items) {
                this.mOriginalTextInfo = ti;
                this.mItems = items;
                this.mSize = items.size();
            }
        }

        public SentenceLevelAdapter(Locale locale) {
            this.mWordIterator = new WordIterator(locale);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private SentenceTextInfoParams getSplitWords(TextInfo originalTextInfo) {
            WordIterator wordIterator = this.mWordIterator;
            CharSequence originalText = originalTextInfo.getText();
            int cookie = originalTextInfo.getCookie();
            int end = originalText.length();
            ArrayList<SentenceWordItem> wordItems = new ArrayList<>();
            wordIterator.setCharSequence(originalText, 0, originalText.length());
            int wordEnd = wordIterator.following(0);
            int wordEnd2 = wordEnd;
            int wordStart = wordIterator.getBeginning(wordEnd);
            while (wordStart <= end && wordEnd2 != -1 && wordStart != -1) {
                if (wordEnd2 >= 0 && wordEnd2 > wordStart) {
                    CharSequence query = originalText.subSequence(wordStart, wordEnd2);
                    wordItems.add(new SentenceWordItem(new TextInfo(query, 0, query.length(), cookie, query.hashCode()), wordStart, wordEnd2));
                }
                wordEnd2 = wordIterator.following(wordEnd2);
                if (wordEnd2 == -1) {
                    break;
                }
                wordStart = wordIterator.getBeginning(wordEnd2);
            }
            return new SentenceTextInfoParams(originalTextInfo, wordItems);
        }

        public static SentenceSuggestionsInfo reconstructSuggestions(SentenceTextInfoParams originalTextInfoParams, SuggestionsInfo[] results) {
            if (results == null || results.length == 0 || originalTextInfoParams == null) {
                return null;
            }
            int originalCookie = originalTextInfoParams.mOriginalTextInfo.getCookie();
            int originalSequence = originalTextInfoParams.mOriginalTextInfo.getSequence();
            int querySize = originalTextInfoParams.mSize;
            int[] offsets = new int[querySize];
            int[] lengths = new int[querySize];
            SuggestionsInfo[] reconstructedSuggestions = new SuggestionsInfo[querySize];
            for (int i = 0; i < querySize; i++) {
                SentenceWordItem item = originalTextInfoParams.mItems.get(i);
                SuggestionsInfo result = null;
                int j = 0;
                while (true) {
                    if (j >= results.length) {
                        break;
                    }
                    SuggestionsInfo cur = results[j];
                    if (cur != null && cur.getSequence() == item.mTextInfo.getSequence()) {
                        result = cur;
                        result.setCookieAndSequence(originalCookie, originalSequence);
                        break;
                    }
                    j++;
                }
                offsets[i] = item.mStart;
                lengths[i] = item.mLength;
                reconstructedSuggestions[i] = result != null ? result : EMPTY_SUGGESTIONS_INFO;
            }
            return new SentenceSuggestionsInfo(reconstructedSuggestions, offsets, lengths);
        }
    }
}
