package android.widget;

import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.method.WordIterator;
import android.text.style.SpellCheckSpan;
import android.text.style.SuggestionSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.util.Locale;

public class SpellChecker implements SpellCheckerSession.SpellCheckerSessionListener {
    public static final int AVERAGE_WORD_LENGTH = 7;
    private static final boolean DBG = false;
    public static final int MAX_NUMBER_OF_WORDS = 50;
    private static final int MIN_SENTENCE_LENGTH = 50;
    private static final int SPELL_PAUSE_DURATION = 400;
    private static final int SUGGESTION_SPAN_CACHE_SIZE = 10;
    /* access modifiers changed from: private */
    public static final String TAG = SpellChecker.class.getSimpleName();
    private static final int USE_SPAN_RANGE = -1;
    public static final int WORD_ITERATOR_INTERVAL = 350;
    final int mCookie;
    private Locale mCurrentLocale;
    /* access modifiers changed from: private */
    public int[] mIds;
    /* access modifiers changed from: private */
    public boolean mIsSentenceSpellCheckSupported;
    /* access modifiers changed from: private */
    public int mLength;
    private int mSpanSequenceCounter = 0;
    /* access modifiers changed from: private */
    public SpellCheckSpan[] mSpellCheckSpans;
    SpellCheckerSession mSpellCheckerSession;
    /* access modifiers changed from: private */
    public SpellParser[] mSpellParsers = new SpellParser[0];
    private Runnable mSpellRunnable;
    private final LruCache<Long, SuggestionSpan> mSuggestionSpanCache = new LruCache<>(10);
    private TextServicesManager mTextServicesManager;
    /* access modifiers changed from: private */
    public final TextView mTextView;
    /* access modifiers changed from: private */
    public WordIterator mWordIterator;

    private class SpellParser {
        private Object mRange;

        private SpellParser() {
            this.mRange = new Object();
        }

        public void parse(int start, int end) {
            int parseEnd;
            int max = SpellChecker.this.mTextView.length();
            if (end > max) {
                String access$300 = SpellChecker.TAG;
                Log.w(access$300, "Parse invalid region, from " + start + " to " + end);
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
            return ((Editable) SpellChecker.this.mTextView.getText()).getSpanStart(this.mRange) < 0;
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

        public void parse() {
            int start;
            int wordEnd;
            int i;
            int wordStart;
            int wordIteratorWindowEnd;
            int wordEnd2;
            Editable editable = (Editable) SpellChecker.this.mTextView.getText();
            if (SpellChecker.this.mIsSentenceSpellCheckSupported) {
                start = Math.max(0, editable.getSpanStart(this.mRange) - 50);
            } else {
                start = editable.getSpanStart(this.mRange);
            }
            int end = editable.getSpanEnd(this.mRange);
            int i2 = Math.min(end, start + 350);
            SpellChecker.this.mWordIterator.setCharSequence(editable, start, i2);
            int wordStart2 = SpellChecker.this.mWordIterator.preceding(start);
            if (wordStart2 == -1) {
                wordEnd = SpellChecker.this.mWordIterator.following(start);
                if (wordEnd != -1) {
                    wordStart2 = SpellChecker.this.mWordIterator.getBeginning(wordEnd);
                }
            } else {
                wordEnd = SpellChecker.this.mWordIterator.getEnd(wordStart2);
            }
            if (wordEnd == -1) {
                removeRangeSpan(editable);
                return;
            }
            SpellCheckSpan[] spellCheckSpans = (SpellCheckSpan[]) editable.getSpans(start - 1, end + 1, SpellCheckSpan.class);
            SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) editable.getSpans(start - 1, end + 1, SuggestionSpan.class);
            int wordCount = 0;
            boolean scheduleOtherSpellCheck = false;
            if (SpellChecker.this.mIsSentenceSpellCheckSupported) {
                if (i2 < end) {
                    scheduleOtherSpellCheck = true;
                }
                int spellCheckEnd = SpellChecker.this.mWordIterator.preceding(i2);
                boolean z = true;
                boolean correct = spellCheckEnd != -1;
                if (correct) {
                    spellCheckEnd = SpellChecker.this.mWordIterator.getEnd(spellCheckEnd);
                    if (spellCheckEnd == -1) {
                        z = false;
                    }
                    correct = z;
                }
                if (!correct) {
                    removeRangeSpan(editable);
                    return;
                }
                int spellCheckStart = wordStart2;
                boolean createSpellCheckSpan = true;
                int i3 = 0;
                while (true) {
                    wordIteratorWindowEnd = i2;
                    int wordIteratorWindowEnd2 = i3;
                    if (wordIteratorWindowEnd2 >= SpellChecker.this.mLength) {
                        wordEnd2 = wordEnd;
                        break;
                    }
                    SpellCheckSpan spellCheckSpan = SpellChecker.this.mSpellCheckSpans[wordIteratorWindowEnd2];
                    int wordStart3 = wordStart2;
                    if (SpellChecker.this.mIds[wordIteratorWindowEnd2] >= 0) {
                        if (!spellCheckSpan.isSpellCheckInProgress()) {
                            int spanStart = editable.getSpanStart(spellCheckSpan);
                            wordEnd2 = wordEnd;
                            int wordEnd3 = editable.getSpanEnd(spellCheckSpan);
                            if (wordEnd3 >= spellCheckStart && spellCheckEnd >= spanStart) {
                                if (spanStart <= spellCheckStart && spellCheckEnd <= wordEnd3) {
                                    createSpellCheckSpan = false;
                                    break;
                                }
                                editable.removeSpan(spellCheckSpan);
                                spellCheckStart = Math.min(spanStart, spellCheckStart);
                                spellCheckEnd = Math.max(wordEnd3, spellCheckEnd);
                            }
                        } else {
                            wordEnd2 = wordEnd;
                        }
                    } else {
                        wordEnd2 = wordEnd;
                    }
                    i3 = wordIteratorWindowEnd2 + 1;
                    i2 = wordIteratorWindowEnd;
                    wordStart2 = wordStart3;
                    wordEnd = wordEnd2;
                }
                if (spellCheckEnd >= start) {
                    if (spellCheckEnd <= spellCheckStart) {
                        String access$300 = SpellChecker.TAG;
                        Log.w(access$300, "Trying to spellcheck invalid region, from " + start + " to " + end);
                    } else if (createSpellCheckSpan) {
                        SpellChecker.this.addSpellCheckSpan(editable, spellCheckStart, spellCheckEnd);
                    }
                }
                wordStart = spellCheckEnd;
                int i4 = wordIteratorWindowEnd;
                int i5 = wordEnd2;
            } else {
                int wordIteratorWindowEnd3 = i2;
                int i6 = wordEnd;
                wordStart = wordStart2;
                while (true) {
                    if (wordStart <= end) {
                        if (wordEnd >= start && wordEnd > wordStart) {
                            if (wordCount >= 50) {
                                scheduleOtherSpellCheck = true;
                                break;
                            }
                            if (wordStart < start && wordEnd > start) {
                                removeSpansAt(editable, start, spellCheckSpans);
                                removeSpansAt(editable, start, suggestionSpans);
                            }
                            if (wordStart < end && wordEnd > end) {
                                removeSpansAt(editable, end, spellCheckSpans);
                                removeSpansAt(editable, end, suggestionSpans);
                            }
                            boolean createSpellCheckSpan2 = true;
                            if (wordEnd == start) {
                                int i7 = 0;
                                while (true) {
                                    if (i7 >= spellCheckSpans.length) {
                                        break;
                                    } else if (editable.getSpanEnd(spellCheckSpans[i7]) == start) {
                                        createSpellCheckSpan2 = false;
                                        break;
                                    } else {
                                        i7++;
                                    }
                                }
                            }
                            if (wordStart == end) {
                                int i8 = 0;
                                while (true) {
                                    if (i8 >= spellCheckSpans.length) {
                                        break;
                                    } else if (editable.getSpanStart(spellCheckSpans[i8]) == end) {
                                        createSpellCheckSpan2 = false;
                                        break;
                                    } else {
                                        i8++;
                                    }
                                }
                            }
                            if (createSpellCheckSpan2) {
                                SpellChecker.this.addSpellCheckSpan(editable, wordStart, wordEnd);
                            }
                            wordCount++;
                        }
                        int originalWordEnd = wordEnd;
                        int wordEnd4 = SpellChecker.this.mWordIterator.following(wordEnd);
                        if (i2 < end && (wordEnd4 == -1 || wordEnd4 >= i2)) {
                            i2 = Math.min(end, originalWordEnd + 350);
                            SpellChecker.this.mWordIterator.setCharSequence(editable, originalWordEnd, i2);
                            wordEnd4 = SpellChecker.this.mWordIterator.following(originalWordEnd);
                        }
                        wordEnd = wordEnd4;
                        i = -1;
                        if (wordEnd != -1) {
                            wordStart = SpellChecker.this.mWordIterator.getBeginning(wordEnd);
                            if (wordStart == -1) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (scheduleOtherSpellCheck || wordStart == i || wordStart > end) {
                    removeRangeSpan(editable);
                } else {
                    setRangeSpan(editable, wordStart, end);
                }
                SpellChecker.this.spellCheck();
            }
            i = -1;
            if (scheduleOtherSpellCheck) {
            }
            removeRangeSpan(editable);
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

    public SpellChecker(TextView textView) {
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
            this.mSpellCheckerSession = this.mTextServicesManager.newSpellCheckerSession(null, this.mCurrentLocale, this, false);
            this.mIsSentenceSpellCheckSupported = true;
        }
        for (int i = 0; i < this.mLength; i++) {
            this.mIds[i] = -1;
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
        return this.mSpellCheckerSession != null;
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
        return this.mLength - 1;
    }

    /* access modifiers changed from: private */
    public void addSpellCheckSpan(Editable editable, int start, int end) {
        int index = nextSpellCheckSpanIndex();
        SpellCheckSpan spellCheckSpan = this.mSpellCheckSpans[index];
        editable.setSpan(spellCheckSpan, start, end, 33);
        spellCheckSpan.setSpellCheckInProgress(false);
        int[] iArr = this.mIds;
        int i = this.mSpanSequenceCounter;
        this.mSpanSequenceCounter = i + 1;
        iArr[index] = i;
    }

    public void onSpellCheckSpanRemoved(SpellCheckSpan spellCheckSpan) {
        for (int i = 0; i < this.mLength; i++) {
            if (this.mSpellCheckSpans[i] == spellCheckSpan) {
                this.mIds[i] = -1;
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

    /* access modifiers changed from: private */
    public void spellCheck() {
        boolean isEditing;
        if (this.mSpellCheckerSession != null) {
            Editable editable = (Editable) this.mTextView.getText();
            int selectionStart = Selection.getSelectionStart(editable);
            int selectionEnd = Selection.getSelectionEnd(editable);
            TextInfo[] textInfos = new TextInfo[this.mLength];
            int textInfosCount = 0;
            int textInfosCount2 = 0;
            while (true) {
                int i = textInfosCount2;
                if (i >= this.mLength) {
                    break;
                }
                SpellCheckSpan spellCheckSpan = this.mSpellCheckSpans[i];
                if (this.mIds[i] >= 0 && !spellCheckSpan.isSpellCheckInProgress()) {
                    int start = editable.getSpanStart(spellCheckSpan);
                    int end = editable.getSpanEnd(spellCheckSpan);
                    if (selectionStart == end + 1 && WordIterator.isMidWordPunctuation(this.mCurrentLocale, Character.codePointBefore(editable, end + 1))) {
                        isEditing = false;
                    } else if (this.mIsSentenceSpellCheckSupported) {
                        isEditing = selectionEnd <= start || selectionStart > end;
                    } else {
                        isEditing = selectionEnd < start || selectionStart > end;
                    }
                    boolean isEditing2 = isEditing;
                    if (start >= 0 && end > start && isEditing2) {
                        spellCheckSpan.setSpellCheckInProgress(true);
                        int i2 = end;
                        TextInfo textInfo = new TextInfo(editable, start, end, this.mCookie, this.mIds[i]);
                        textInfos[textInfosCount] = textInfo;
                        textInfosCount++;
                    }
                }
                textInfosCount2 = i + 1;
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
                    this.mSpellCheckerSession.getSuggestions(textInfos, 5, false);
                }
            }
        }
    }

    private SpellCheckSpan onGetSuggestionsInternal(SuggestionsInfo suggestionsInfo, int offset, int length) {
        SpellCheckSpan spellCheckSpan;
        int end;
        int start;
        int i = offset;
        int i2 = length;
        if (suggestionsInfo == null || suggestionsInfo.getCookie() != this.mCookie) {
            return null;
        }
        Editable editable = (Editable) this.mTextView.getText();
        int sequenceNumber = suggestionsInfo.getSequence();
        boolean z = false;
        int k = 0;
        while (true) {
            int k2 = k;
            if (k2 >= this.mLength) {
                return null;
            }
            if (sequenceNumber == this.mIds[k2]) {
                int attributes = suggestionsInfo.getSuggestionsAttributes();
                boolean isInDictionary = (attributes & 1) > 0;
                if ((attributes & 2) > 0) {
                    z = true;
                }
                boolean looksLikeTypo = z;
                SpellCheckSpan spellCheckSpan2 = this.mSpellCheckSpans[k2];
                if (isInDictionary || !looksLikeTypo) {
                    spellCheckSpan = spellCheckSpan2;
                    if (this.mIsSentenceSpellCheckSupported) {
                        int spellCheckSpanStart = editable.getSpanStart(spellCheckSpan);
                        int spellCheckSpanEnd = editable.getSpanEnd(spellCheckSpan);
                        if (i == -1 || i2 == -1) {
                            start = spellCheckSpanStart;
                            end = spellCheckSpanEnd;
                        } else {
                            start = spellCheckSpanStart + i;
                            end = start + i2;
                        }
                        if (spellCheckSpanStart >= 0 && spellCheckSpanEnd > spellCheckSpanStart && end > start) {
                            Long key = Long.valueOf(TextUtils.packRangeInLong(start, end));
                            SuggestionSpan tempSuggestionSpan = this.mSuggestionSpanCache.get(key);
                            if (tempSuggestionSpan != null) {
                                editable.removeSpan(tempSuggestionSpan);
                                int i3 = spellCheckSpanStart;
                                this.mSuggestionSpanCache.remove(key);
                            }
                        }
                    }
                } else {
                    spellCheckSpan = spellCheckSpan2;
                    createMisspelledSuggestionSpan(editable, suggestionsInfo, spellCheckSpan2, i, i2);
                }
                return spellCheckSpan;
            }
            k = k2 + 1;
        }
    }

    public void onGetSuggestions(SuggestionsInfo[] results) {
        Editable editable = (Editable) this.mTextView.getText();
        for (SuggestionsInfo onGetSuggestionsInternal : results) {
            SpellCheckSpan spellCheckSpan = onGetSuggestionsInternal(onGetSuggestionsInternal, -1, -1);
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
                SpellCheckSpan spellCheckSpan = null;
                for (int j = 0; j < ssi.getSuggestionsCount(); j++) {
                    SuggestionsInfo suggestionsInfo = ssi.getSuggestionsInfoAt(j);
                    if (suggestionsInfo != null) {
                        SpellCheckSpan scs = onGetSuggestionsInternal(suggestionsInfo, ssi.getOffsetAt(j), ssi.getLengthAt(j));
                        if (spellCheckSpan == null && scs != null) {
                            spellCheckSpan = scs;
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
                    for (SpellParser spellParser : SpellChecker.this.mSpellParsers) {
                        if (!spellParser.isFinished()) {
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
        int end;
        int start;
        String[] suggestions;
        Editable editable2 = editable;
        SpellCheckSpan spellCheckSpan2 = spellCheckSpan;
        int i = offset;
        int i2 = length;
        int spellCheckSpanStart = editable2.getSpanStart(spellCheckSpan2);
        int spellCheckSpanEnd = editable2.getSpanEnd(spellCheckSpan2);
        if (spellCheckSpanStart < 0 || spellCheckSpanEnd <= spellCheckSpanStart) {
            SuggestionsInfo suggestionsInfo2 = suggestionsInfo;
            return;
        }
        if (i == -1 || i2 == -1) {
            start = spellCheckSpanStart;
            end = spellCheckSpanEnd;
        } else {
            start = spellCheckSpanStart + i;
            end = start + i2;
        }
        int suggestionsCount = suggestionsInfo.getSuggestionsCount();
        if (suggestionsCount > 0) {
            suggestions = new String[suggestionsCount];
            for (int i3 = 0; i3 < suggestionsCount; i3++) {
                suggestions[i3] = suggestionsInfo.getSuggestionAt(i3);
            }
            SuggestionsInfo suggestionsInfo3 = suggestionsInfo;
        } else {
            SuggestionsInfo suggestionsInfo4 = suggestionsInfo;
            suggestions = (String[]) ArrayUtils.emptyArray(String.class);
        }
        SuggestionSpan suggestionSpan = new SuggestionSpan(this.mTextView.getContext(), suggestions, 3);
        if (this.mIsSentenceSpellCheckSupported) {
            Long key = Long.valueOf(TextUtils.packRangeInLong(start, end));
            SuggestionSpan tempSuggestionSpan = this.mSuggestionSpanCache.get(key);
            if (tempSuggestionSpan != null) {
                editable2.removeSpan(tempSuggestionSpan);
            }
            this.mSuggestionSpanCache.put(key, suggestionSpan);
        }
        editable2.setSpan(suggestionSpan, start, end, 33);
        this.mTextView.invalidateRegion(start, end, false);
    }

    public static boolean haveWordBoundariesChanged(Editable editable, int start, int end, int spanStart, int spanEnd) {
        if (spanEnd != start && spanStart != end) {
            return true;
        }
        if (spanEnd == start && start < editable.length()) {
            return Character.isLetterOrDigit(Character.codePointAt(editable, start));
        }
        if (spanStart != end || end <= 0) {
            return false;
        }
        return Character.isLetterOrDigit(Character.codePointBefore(editable, end));
    }
}
