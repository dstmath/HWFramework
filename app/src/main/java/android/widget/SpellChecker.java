package android.widget;

import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.WordIterator;
import android.text.style.SpellCheckSpan;
import android.text.style.SuggestionSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.util.Locale;

public class SpellChecker implements SpellCheckerSessionListener {
    public static final int AVERAGE_WORD_LENGTH = 7;
    private static final boolean DBG = false;
    public static final int MAX_NUMBER_OF_WORDS = 50;
    private static final int MIN_SENTENCE_LENGTH = 50;
    private static final int SPELL_PAUSE_DURATION = 400;
    private static final int SUGGESTION_SPAN_CACHE_SIZE = 10;
    private static final String TAG = null;
    private static final int USE_SPAN_RANGE = -1;
    public static final int WORD_ITERATOR_INTERVAL = 350;
    final int mCookie;
    private Locale mCurrentLocale;
    private int[] mIds;
    private boolean mIsSentenceSpellCheckSupported;
    private int mLength;
    private int mSpanSequenceCounter;
    private SpellCheckSpan[] mSpellCheckSpans;
    SpellCheckerSession mSpellCheckerSession;
    private SpellParser[] mSpellParsers;
    private Runnable mSpellRunnable;
    private final LruCache<Long, SuggestionSpan> mSuggestionSpanCache;
    private TextServicesManager mTextServicesManager;
    private final TextView mTextView;
    private WordIterator mWordIterator;

    private class SpellParser {
        private Object mRange;

        private SpellParser() {
            this.mRange = new Object();
        }

        public void parse(int start, int end) {
            int parseEnd;
            int max = SpellChecker.this.mTextView.length();
            if (end > max) {
                Log.w(SpellChecker.TAG, "Parse invalid region, from " + start + " to " + end);
                parseEnd = max;
            } else {
                parseEnd = end;
            }
            if (parseEnd > start) {
                setRangeSpan((Editable) SpellChecker.this.mTextView.getText(), start, parseEnd);
                parse();
            }
        }

        public boolean isFinished() {
            return ((Editable) SpellChecker.this.mTextView.getText()).getSpanStart(this.mRange) < 0 ? true : SpellChecker.DBG;
        }

        public void stop() {
            removeRangeSpan((Editable) SpellChecker.this.mTextView.getText());
        }

        private void setRangeSpan(Editable editable, int start, int end) {
            editable.setSpan(this.mRange, start, end, 33);
        }

        private void removeRangeSpan(Editable editable) {
            editable.removeSpan(this.mRange);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void parse() {
            int start;
            int wordEnd;
            Editable editable = (Editable) SpellChecker.this.mTextView.getText();
            if (SpellChecker.this.mIsSentenceSpellCheckSupported) {
                start = Math.max(0, editable.getSpanStart(this.mRange) - 50);
            } else {
                start = editable.getSpanStart(this.mRange);
            }
            int end = editable.getSpanEnd(this.mRange);
            int wordIteratorWindowEnd = Math.min(end, start + SpellChecker.WORD_ITERATOR_INTERVAL);
            SpellChecker.this.mWordIterator.setCharSequence(editable, start, wordIteratorWindowEnd);
            int wordStart = SpellChecker.this.mWordIterator.preceding(start);
            if (wordStart == SpellChecker.USE_SPAN_RANGE) {
                wordEnd = SpellChecker.this.mWordIterator.following(start);
                if (wordEnd != SpellChecker.USE_SPAN_RANGE) {
                    wordStart = SpellChecker.this.mWordIterator.getBeginning(wordEnd);
                }
            } else {
                wordEnd = SpellChecker.this.mWordIterator.getEnd(wordStart);
            }
            if (wordEnd == SpellChecker.USE_SPAN_RANGE) {
                removeRangeSpan(editable);
                return;
            }
            SpellCheckSpan[] spellCheckSpans = (SpellCheckSpan[]) editable.getSpans(start + SpellChecker.USE_SPAN_RANGE, end + 1, SpellCheckSpan.class);
            SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) editable.getSpans(start + SpellChecker.USE_SPAN_RANGE, end + 1, SuggestionSpan.class);
            int wordCount = 0;
            boolean scheduleOtherSpellCheck = SpellChecker.DBG;
            boolean z;
            int i;
            if (!SpellChecker.this.mIsSentenceSpellCheckSupported) {
                while (wordStart <= end) {
                    if (wordEnd >= start && wordEnd > wordStart) {
                        if (wordCount >= SpellChecker.MIN_SENTENCE_LENGTH) {
                            scheduleOtherSpellCheck = true;
                            break;
                        }
                        int length;
                        if (wordStart < start && wordEnd > start) {
                            removeSpansAt(editable, start, spellCheckSpans);
                            removeSpansAt(editable, start, suggestionSpans);
                        }
                        if (wordStart < end && wordEnd > end) {
                            removeSpansAt(editable, end, spellCheckSpans);
                            removeSpansAt(editable, end, suggestionSpans);
                        }
                        z = true;
                        if (wordEnd == start) {
                            i = 0;
                            while (true) {
                                length = spellCheckSpans.length;
                                if (i >= r0) {
                                    break;
                                }
                                if (editable.getSpanEnd(spellCheckSpans[i]) == start) {
                                    break;
                                }
                                i++;
                            }
                        }
                        if (wordStart == end) {
                            i = 0;
                            while (true) {
                                length = spellCheckSpans.length;
                                if (i >= r0) {
                                    break;
                                }
                                if (editable.getSpanStart(spellCheckSpans[i]) == end) {
                                    break;
                                }
                                i++;
                            }
                        }
                        if (z) {
                            SpellChecker.this.addSpellCheckSpan(editable, wordStart, wordEnd);
                        }
                        wordCount++;
                    }
                    int originalWordEnd = wordEnd;
                    wordEnd = SpellChecker.this.mWordIterator.following(wordEnd);
                    if (wordIteratorWindowEnd < end && (wordEnd == SpellChecker.USE_SPAN_RANGE || wordEnd >= wordIteratorWindowEnd)) {
                        wordIteratorWindowEnd = Math.min(end, originalWordEnd + SpellChecker.WORD_ITERATOR_INTERVAL);
                        SpellChecker.this.mWordIterator.setCharSequence(editable, originalWordEnd, wordIteratorWindowEnd);
                        wordEnd = SpellChecker.this.mWordIterator.following(originalWordEnd);
                    }
                    if (wordEnd == SpellChecker.USE_SPAN_RANGE) {
                        break;
                    }
                    wordStart = SpellChecker.this.mWordIterator.getBeginning(wordEnd);
                    if (wordStart == SpellChecker.USE_SPAN_RANGE) {
                        break;
                    }
                }
            }
            if (wordIteratorWindowEnd < end) {
                scheduleOtherSpellCheck = true;
            }
            int spellCheckEnd = SpellChecker.this.mWordIterator.preceding(wordIteratorWindowEnd);
            boolean correct = spellCheckEnd != SpellChecker.USE_SPAN_RANGE ? true : SpellChecker.DBG;
            if (correct) {
                spellCheckEnd = SpellChecker.this.mWordIterator.getEnd(spellCheckEnd);
                correct = spellCheckEnd != SpellChecker.USE_SPAN_RANGE ? true : SpellChecker.DBG;
            }
            if (correct) {
                int spellCheckStart = wordStart;
                z = true;
                i = 0;
                while (true) {
                    if (i >= SpellChecker.this.mLength) {
                        break;
                    }
                    SpellCheckSpan spellCheckSpan = SpellChecker.this.mSpellCheckSpans[i];
                    if (SpellChecker.this.mIds[i] >= 0 && !spellCheckSpan.isSpellCheckInProgress()) {
                        int spanStart = editable.getSpanStart(spellCheckSpan);
                        int spanEnd = editable.getSpanEnd(spellCheckSpan);
                        if (spanEnd >= spellCheckStart && spellCheckEnd >= spanStart) {
                            if (spanStart <= spellCheckStart && spellCheckEnd <= spanEnd) {
                                break;
                            }
                            editable.removeSpan(spellCheckSpan);
                            spellCheckStart = Math.min(spanStart, spellCheckStart);
                            spellCheckEnd = Math.max(spanEnd, spellCheckEnd);
                        }
                    }
                    i++;
                }
                if (spellCheckEnd >= start) {
                    if (spellCheckEnd <= spellCheckStart) {
                        Log.w(SpellChecker.TAG, "Trying to spellcheck invalid region, from " + start + " to " + end);
                    } else if (z) {
                        SpellChecker.this.addSpellCheckSpan(editable, spellCheckStart, spellCheckEnd);
                    }
                }
                wordStart = spellCheckEnd;
            } else {
                removeRangeSpan(editable);
                return;
            }
            if (!scheduleOtherSpellCheck || wordStart == SpellChecker.USE_SPAN_RANGE || wordStart > end) {
                removeRangeSpan(editable);
            } else {
                setRangeSpan(editable, wordStart, end);
            }
            SpellChecker.this.spellCheck();
        }

        private <T> void removeSpansAt(Editable editable, int offset, T[] spans) {
            for (T span : spans) {
                if (editable.getSpanStart(span) <= offset && editable.getSpanEnd(span) >= offset) {
                    editable.removeSpan(span);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.SpellChecker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.SpellChecker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.SpellChecker.<clinit>():void");
    }

    public SpellChecker(TextView textView) {
        this.mSpellParsers = new SpellParser[0];
        this.mSpanSequenceCounter = 0;
        this.mSuggestionSpanCache = new LruCache(SUGGESTION_SPAN_CACHE_SIZE);
        this.mTextView = textView;
        this.mIds = ArrayUtils.newUnpaddedIntArray(1);
        this.mSpellCheckSpans = new SpellCheckSpan[this.mIds.length];
        setLocale(this.mTextView.getSpellCheckerLocale());
        this.mCookie = hashCode();
    }

    private void resetSession() {
        closeSession();
        this.mTextServicesManager = (TextServicesManager) this.mTextView.getContext().getSystemService("textservices");
        if (!this.mTextServicesManager.isSpellCheckerEnabled() || this.mCurrentLocale == null || this.mTextServicesManager.getCurrentSpellCheckerSubtype(true) == null) {
            this.mSpellCheckerSession = null;
        } else {
            this.mSpellCheckerSession = this.mTextServicesManager.newSpellCheckerSession(null, this.mCurrentLocale, this, DBG);
            this.mIsSentenceSpellCheckSupported = true;
        }
        for (int i = 0; i < this.mLength; i++) {
            this.mIds[i] = USE_SPAN_RANGE;
        }
        this.mLength = 0;
        this.mTextView.removeMisspelledSpans((Editable) this.mTextView.getText());
        this.mSuggestionSpanCache.evictAll();
    }

    private void setLocale(Locale locale) {
        this.mCurrentLocale = locale;
        resetSession();
        if (locale != null) {
            this.mWordIterator = new WordIterator(locale);
        }
        this.mTextView.onLocaleChanged();
    }

    private boolean isSessionActive() {
        return this.mSpellCheckerSession != null ? true : DBG;
    }

    public void closeSession() {
        if (this.mSpellCheckerSession != null) {
            this.mSpellCheckerSession.close();
        }
        for (SpellParser stop : this.mSpellParsers) {
            stop.stop();
        }
        if (this.mSpellRunnable != null) {
            this.mTextView.removeCallbacks(this.mSpellRunnable);
        }
    }

    private int nextSpellCheckSpanIndex() {
        for (int i = 0; i < this.mLength; i++) {
            if (this.mIds[i] < 0) {
                return i;
            }
        }
        this.mIds = GrowingArrayUtils.append(this.mIds, this.mLength, 0);
        this.mSpellCheckSpans = (SpellCheckSpan[]) GrowingArrayUtils.append(this.mSpellCheckSpans, this.mLength, new SpellCheckSpan());
        this.mLength++;
        return this.mLength + USE_SPAN_RANGE;
    }

    private void addSpellCheckSpan(Editable editable, int start, int end) {
        int index = nextSpellCheckSpanIndex();
        SpellCheckSpan spellCheckSpan = this.mSpellCheckSpans[index];
        editable.setSpan(spellCheckSpan, start, end, 33);
        spellCheckSpan.setSpellCheckInProgress(DBG);
        int[] iArr = this.mIds;
        int i = this.mSpanSequenceCounter;
        this.mSpanSequenceCounter = i + 1;
        iArr[index] = i;
    }

    public void onSpellCheckSpanRemoved(SpellCheckSpan spellCheckSpan) {
        for (int i = 0; i < this.mLength; i++) {
            if (this.mSpellCheckSpans[i] == spellCheckSpan) {
                this.mIds[i] = USE_SPAN_RANGE;
                return;
            }
        }
    }

    public void onSelectionChanged() {
        spellCheck();
    }

    public void spellCheck(int start, int end) {
        Locale locale = this.mTextView.getSpellCheckerLocale();
        boolean isSessionActive = isSessionActive();
        if (locale == null || this.mCurrentLocale == null || !this.mCurrentLocale.equals(locale)) {
            setLocale(locale);
            start = 0;
            end = this.mTextView.getText().length();
        } else if (isSessionActive != this.mTextServicesManager.isSpellCheckerEnabled()) {
            resetSession();
        }
        if (isSessionActive) {
            for (SpellParser spellParser : this.mSpellParsers) {
                if (spellParser.isFinished()) {
                    spellParser.parse(start, end);
                    return;
                }
            }
            SpellParser[] newSpellParsers = new SpellParser[(length + 1)];
            System.arraycopy(this.mSpellParsers, 0, newSpellParsers, 0, length);
            this.mSpellParsers = newSpellParsers;
            SpellParser spellParser2 = new SpellParser();
            this.mSpellParsers[length] = spellParser2;
            spellParser2.parse(start, end);
        }
    }

    private void spellCheck() {
        if (this.mSpellCheckerSession != null) {
            Editable editable = (Editable) this.mTextView.getText();
            int selectionStart = Selection.getSelectionStart(editable);
            int selectionEnd = Selection.getSelectionEnd(editable);
            TextInfo[] textInfos = new TextInfo[this.mLength];
            int textInfosCount = 0;
            for (int i = 0; i < this.mLength; i++) {
                SpellCheckSpan spellCheckSpan = this.mSpellCheckSpans[i];
                if (this.mIds[i] >= 0 && !spellCheckSpan.isSpellCheckInProgress()) {
                    int start = editable.getSpanStart(spellCheckSpan);
                    int end = editable.getSpanEnd(spellCheckSpan);
                    boolean apostrophe = (selectionStart == end + 1 && editable.charAt(end) == DateFormat.QUOTE) ? true : DBG;
                    boolean isEditing = this.mIsSentenceSpellCheckSupported ? (apostrophe || (selectionEnd > start && selectionStart <= end)) ? DBG : true : (apostrophe || (selectionEnd >= start && selectionStart <= end)) ? DBG : true;
                    if (start >= 0 && end > start && isEditing) {
                        spellCheckSpan.setSpellCheckInProgress(true);
                        int textInfosCount2 = textInfosCount + 1;
                        textInfos[textInfosCount] = new TextInfo(editable, start, end, this.mCookie, this.mIds[i]);
                        textInfosCount = textInfosCount2;
                    }
                }
            }
            if (textInfosCount > 0) {
                if (textInfosCount < textInfos.length) {
                    TextInfo[] textInfosCopy = new TextInfo[textInfosCount];
                    System.arraycopy(textInfos, 0, textInfosCopy, 0, textInfosCount);
                    textInfos = textInfosCopy;
                }
                if (this.mIsSentenceSpellCheckSupported) {
                    this.mSpellCheckerSession.getSentenceSuggestions(textInfos, 5);
                } else {
                    this.mSpellCheckerSession.getSuggestions(textInfos, 5, DBG);
                }
            }
        }
    }

    private SpellCheckSpan onGetSuggestionsInternal(SuggestionsInfo suggestionsInfo, int offset, int length) {
        if (suggestionsInfo == null || suggestionsInfo.getCookie() != this.mCookie) {
            return null;
        }
        Editable editable = (Editable) this.mTextView.getText();
        int sequenceNumber = suggestionsInfo.getSequence();
        for (int k = 0; k < this.mLength; k++) {
            if (sequenceNumber == this.mIds[k]) {
                int attributes = suggestionsInfo.getSuggestionsAttributes();
                boolean isInDictionary = (attributes & 1) > 0 ? true : DBG;
                boolean looksLikeTypo = (attributes & 2) > 0 ? true : DBG;
                SpellCheckSpan spellCheckSpan = this.mSpellCheckSpans[k];
                if (!isInDictionary && looksLikeTypo) {
                    createMisspelledSuggestionSpan(editable, suggestionsInfo, spellCheckSpan, offset, length);
                } else if (this.mIsSentenceSpellCheckSupported) {
                    int start;
                    int end;
                    int spellCheckSpanStart = editable.getSpanStart(spellCheckSpan);
                    int spellCheckSpanEnd = editable.getSpanEnd(spellCheckSpan);
                    if (offset == USE_SPAN_RANGE || length == USE_SPAN_RANGE) {
                        start = spellCheckSpanStart;
                        end = spellCheckSpanEnd;
                    } else {
                        start = spellCheckSpanStart + offset;
                        end = start + length;
                    }
                    if (spellCheckSpanStart >= 0 && spellCheckSpanEnd > spellCheckSpanStart && end > start) {
                        Long key = Long.valueOf(TextUtils.packRangeInLong(start, end));
                        SuggestionSpan tempSuggestionSpan = (SuggestionSpan) this.mSuggestionSpanCache.get(key);
                        if (tempSuggestionSpan != null) {
                            editable.removeSpan(tempSuggestionSpan);
                            this.mSuggestionSpanCache.remove(key);
                        }
                    }
                }
                return spellCheckSpan;
            }
        }
        return null;
    }

    public void onGetSuggestions(SuggestionsInfo[] results) {
        Editable editable = (Editable) this.mTextView.getText();
        for (SuggestionsInfo onGetSuggestionsInternal : results) {
            SpellCheckSpan spellCheckSpan = onGetSuggestionsInternal(onGetSuggestionsInternal, USE_SPAN_RANGE, USE_SPAN_RANGE);
            if (spellCheckSpan != null) {
                editable.removeSpan(spellCheckSpan);
            }
        }
        scheduleNewSpellCheck();
    }

    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
        Editable editable = (Editable) this.mTextView.getText();
        for (SentenceSuggestionsInfo ssi : results) {
            if (ssi != null) {
                Object spellCheckSpan = null;
                for (int j = 0; j < ssi.getSuggestionsCount(); j++) {
                    SuggestionsInfo suggestionsInfo = ssi.getSuggestionsInfoAt(j);
                    if (suggestionsInfo != null) {
                        SpellCheckSpan scs = onGetSuggestionsInternal(suggestionsInfo, ssi.getOffsetAt(j), ssi.getLengthAt(j));
                        if (spellCheckSpan == null && scs != null) {
                            SpellCheckSpan spellCheckSpan2 = scs;
                        }
                    }
                }
                if (spellCheckSpan != null) {
                    editable.removeSpan(spellCheckSpan);
                }
            }
        }
        scheduleNewSpellCheck();
    }

    private void scheduleNewSpellCheck() {
        if (this.mSpellRunnable == null) {
            this.mSpellRunnable = new Runnable() {
                public void run() {
                    int length = SpellChecker.this.mSpellParsers.length;
                    int i = 0;
                    while (i < length) {
                        SpellParser spellParser = SpellChecker.this.mSpellParsers[i];
                        if (spellParser.isFinished()) {
                            i++;
                        } else {
                            spellParser.parse();
                            return;
                        }
                    }
                }
            };
        } else {
            this.mTextView.removeCallbacks(this.mSpellRunnable);
        }
        this.mTextView.postDelayed(this.mSpellRunnable, 400);
    }

    private void createMisspelledSuggestionSpan(Editable editable, SuggestionsInfo suggestionsInfo, SpellCheckSpan spellCheckSpan, int offset, int length) {
        int spellCheckSpanStart = editable.getSpanStart(spellCheckSpan);
        int spellCheckSpanEnd = editable.getSpanEnd(spellCheckSpan);
        if (spellCheckSpanStart >= 0 && spellCheckSpanEnd > spellCheckSpanStart) {
            int start;
            int end;
            String[] suggestions;
            if (offset == USE_SPAN_RANGE || length == USE_SPAN_RANGE) {
                start = spellCheckSpanStart;
                end = spellCheckSpanEnd;
            } else {
                start = spellCheckSpanStart + offset;
                end = start + length;
            }
            int suggestionsCount = suggestionsInfo.getSuggestionsCount();
            if (suggestionsCount > 0) {
                suggestions = new String[suggestionsCount];
                for (int i = 0; i < suggestionsCount; i++) {
                    suggestions[i] = suggestionsInfo.getSuggestionAt(i);
                }
            } else {
                suggestions = (String[]) ArrayUtils.emptyArray(String.class);
            }
            SuggestionSpan suggestionSpan = new SuggestionSpan(this.mTextView.getContext(), suggestions, 3);
            if (this.mIsSentenceSpellCheckSupported) {
                Long key = Long.valueOf(TextUtils.packRangeInLong(start, end));
                SuggestionSpan tempSuggestionSpan = (SuggestionSpan) this.mSuggestionSpanCache.get(key);
                if (tempSuggestionSpan != null) {
                    editable.removeSpan(tempSuggestionSpan);
                }
                this.mSuggestionSpanCache.put(key, suggestionSpan);
            }
            editable.setSpan(suggestionSpan, start, end, 33);
            this.mTextView.invalidateRegion(start, end, DBG);
        }
    }

    public static boolean haveWordBoundariesChanged(Editable editable, int start, int end, int spanStart, int spanEnd) {
        if (spanEnd != start && spanStart != end) {
            return true;
        }
        if (spanEnd == start && start < editable.length()) {
            return Character.isLetterOrDigit(Character.codePointAt(editable, start));
        }
        if (spanStart != end || end <= 0) {
            return DBG;
        }
        return Character.isLetterOrDigit(Character.codePointBefore(editable, end));
    }
}
