package android.view.textclassifier;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.textclassifier.TextLanguage.Builder;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class TextClassificationManager {
    private static final String LOG_TAG = "TextClassificationManager";
    private final Context mContext;
    private LangId mLangId;
    private ParcelFileDescriptor mLangIdFd;
    private final Object mLangIdLock = new Object();
    private TextClassifier mTextClassifier;
    private final Object mTextClassifierLock = new Object();

    public TextClassificationManager(Context context) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
    }

    public TextClassifier getTextClassifier() {
        TextClassifier textClassifier;
        synchronized (this.mTextClassifierLock) {
            if (this.mTextClassifier == null) {
                this.mTextClassifier = new TextClassifierImpl(this.mContext);
            }
            textClassifier = this.mTextClassifier;
        }
        return textClassifier;
    }

    public void setTextClassifier(TextClassifier textClassifier) {
        synchronized (this.mTextClassifierLock) {
            this.mTextClassifier = textClassifier;
        }
    }

    public List<TextLanguage> detectLanguages(CharSequence text) {
        boolean z = true;
        if (text == null) {
            z = false;
        }
        Preconditions.checkArgument(z);
        try {
            if (text.length() > 0) {
                ClassificationResult[] results = getLanguageDetector().findLanguages(text.toString());
                Builder tlBuilder = new Builder(0, text.length());
                int size = results.length;
                for (int i = 0; i < size; i++) {
                    tlBuilder.setLanguage(new Locale.Builder().setLanguageTag(results[i].mLanguage).build(), results[i].mScore);
                }
                return Collections.unmodifiableList(Arrays.asList(new TextLanguage[]{tlBuilder.build()}));
            }
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Error detecting languages for text. Returning empty result.", t);
        }
        return Collections.emptyList();
    }

    private LangId getLanguageDetector() throws FileNotFoundException {
        LangId langId;
        synchronized (this.mLangIdLock) {
            if (this.mLangId == null) {
                this.mLangIdFd = ParcelFileDescriptor.open(new File("/etc/textclassifier/textclassifier.langid.model"), 268435456);
                this.mLangId = new LangId(this.mLangIdFd.getFd());
            }
            langId = this.mLangId;
        }
        return langId;
    }
}
