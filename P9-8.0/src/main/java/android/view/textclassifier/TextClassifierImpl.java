package android.view.textclassifier;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.LocaleList;
import android.os.ParcelFileDescriptor;
import android.provider.Browser;
import android.service.notification.ZenModeConfig;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.WordIterator;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.LogException;
import android.util.Patterns;
import android.view.View;
import android.view.textclassifier.TextSelection.Builder;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TextClassifierImpl implements TextClassifier {
    private static final String LOG_TAG = "TextClassifierImpl";
    private static final String MODEL_DIR = "/etc/textclassifier/";
    private static final String MODEL_FILE_REGEX = "textclassifier\\.smartselection\\.(.*)\\.model";
    private static final String UPDATED_MODEL_FILE_PATH = "/data/misc/textclassifier/textclassifier.smartselection.model";
    private final Context mContext;
    @GuardedBy("mSmartSelectionLock")
    private Locale mLocale;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    @GuardedBy("mSmartSelectionLock")
    private Map<Locale, String> mModelFilePaths;
    @GuardedBy("mSmartSelectionLock")
    private SmartSelection mSmartSelection;
    private final Object mSmartSelectionLock = new Object();

    private static final class IntentFactory {
        private IntentFactory() {
        }

        public static Intent create(Context context, String type, String text) {
            type = type.trim().toLowerCase(Locale.ENGLISH);
            text = text.trim();
            if (type.equals("email")) {
                return new Intent("android.intent.action.SENDTO").setData(Uri.parse(String.format("mailto:%s", new Object[]{text})));
            } else if (type.equals("phone")) {
                return new Intent("android.intent.action.DIAL").setData(Uri.parse(String.format("tel:%s", new Object[]{text})));
            } else if (type.equals("address")) {
                return new Intent("android.intent.action.VIEW").setData(Uri.parse(String.format("geo:0,0?q=%s", new Object[]{text})));
            } else if (!type.equals("url")) {
                return null;
            } else {
                String httpPrefix = "http://";
                String httpsPrefix = "https://";
                if (text.toLowerCase().startsWith("http://")) {
                    text = "http://" + text.substring("http://".length());
                } else if (text.toLowerCase().startsWith("https://")) {
                    text = "https://" + text.substring("https://".length());
                } else {
                    text = "http://" + text;
                }
                return new Intent("android.intent.action.VIEW", Uri.parse(text)).putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
            }
        }

        public static String getLabel(Context context, String type) {
            type = type.trim().toLowerCase(Locale.ENGLISH);
            if (type.equals("email")) {
                return context.getString(R.string.email);
            }
            if (type.equals("phone")) {
                return context.getString(R.string.dial);
            }
            if (type.equals("address")) {
                return context.getString(R.string.map);
            }
            if (type.equals("url")) {
                return context.getString(R.string.browse);
            }
            return null;
        }

        public static int getLogType(String type) {
            type = type.trim().toLowerCase(Locale.ENGLISH);
            if (type.equals("email")) {
                return 1;
            }
            if (type.equals("phone")) {
                return 2;
            }
            if (type.equals("address")) {
                return 3;
            }
            if (type.equals("url")) {
                return 4;
            }
            return 0;
        }
    }

    private static final class LinksInfoFactory {

        private static final class LinksInfoImpl implements LinksInfo {
            private final CharSequence mOriginalText;
            private final List<SpanSpec> mSpans;

            LinksInfoImpl(CharSequence originalText, List<SpanSpec> spans) {
                this.mOriginalText = originalText;
                this.mSpans = spans;
            }

            public boolean apply(CharSequence text) {
                boolean z;
                if (text != null) {
                    z = true;
                } else {
                    z = false;
                }
                Preconditions.checkArgument(z);
                if (!(text instanceof Spannable) || !this.mOriginalText.toString().equals(text.toString())) {
                    return false;
                }
                Spannable spannable = (Spannable) text;
                int size = this.mSpans.size();
                for (int i = 0; i < size; i++) {
                    SpanSpec span = (SpanSpec) this.mSpans.get(i);
                    spannable.setSpan(span.mSpan, span.mStart, span.mEnd, 0);
                }
                return true;
            }
        }

        private static final class SpanSpec {
            private final int mEnd;
            private final ClickableSpan mSpan;
            private final int mStart;

            SpanSpec(int start, int end, ClickableSpan span) {
                this.mStart = start;
                this.mEnd = end;
                this.mSpan = span;
            }
        }

        private LinksInfoFactory() {
        }

        public static LinksInfo create(Context context, SmartSelection smartSelection, String text, int linkMask) {
            WordIterator wordIterator = new WordIterator();
            wordIterator.setCharSequence(text, 0, text.length());
            List<SpanSpec> spans = new ArrayList();
            int start = 0;
            while (true) {
                int end = wordIterator.nextBoundary(start);
                if (end == -1) {
                    return new LinksInfoImpl(text, avoidOverlaps(spans, text));
                } else if (!TextUtils.isEmpty(text.substring(start, end))) {
                    int[] selection = smartSelection.suggest(text, start, end);
                    int selectionStart = selection[0];
                    int selectionEnd = selection[1];
                    if (selectionStart >= 0 && selectionEnd <= text.length() && selectionStart <= selectionEnd) {
                        ClassificationResult[] results = smartSelection.classifyText(text, selectionStart, selectionEnd, TextClassifierImpl.getHintFlags(text, selectionStart, selectionEnd));
                        if (results.length > 0) {
                            String type = TextClassifierImpl.getHighestScoringType(results);
                            if (matches(type, linkMask)) {
                                Intent intent = IntentFactory.create(context, type, text.substring(selectionStart, selectionEnd));
                                if (hasActivityHandler(context, intent)) {
                                    spans.add(new SpanSpec(selectionStart, selectionEnd, createSpan(context, intent)));
                                }
                            }
                        }
                    }
                    start = end;
                }
            }
        }

        private static boolean matches(String type, int linkMask) {
            type = type.trim().toLowerCase(Locale.ENGLISH);
            if ((linkMask & 4) != 0 && "phone".equals(type)) {
                return true;
            }
            if ((linkMask & 2) != 0 && "email".equals(type)) {
                return true;
            }
            if ((linkMask & 8) == 0 || !"address".equals(type)) {
                return (linkMask & 1) != 0 && "url".equals(type);
            } else {
                return true;
            }
        }

        private static List<SpanSpec> avoidOverlaps(List<SpanSpec> spans, String text) {
            SpanSpec rep;
            Collections.sort(spans, Comparator.comparingInt(new -$Lambda$Sy__B53nI_asuVbYEz1JE9PRAk8()));
            Map<Integer, SpanSpec> reps = new LinkedHashMap();
            int size = spans.size();
            for (int i = 0; i < size; i++) {
                SpanSpec span = (SpanSpec) spans.get(i);
                rep = (SpanSpec) reps.get(Integer.valueOf(span.mStart));
                if (rep == null || rep.mEnd < span.mEnd) {
                    reps.put(Integer.valueOf(span.mStart), span);
                }
            }
            LinkedList<SpanSpec> result = new LinkedList();
            for (SpanSpec rep2 : reps.values()) {
                if (result.isEmpty()) {
                    result.add(rep2);
                } else {
                    SpanSpec last = (SpanSpec) result.getLast();
                    if (rep2.mStart >= last.mEnd) {
                        result.add(rep2);
                    } else if (rep2.mEnd - rep2.mStart > last.mEnd - last.mStart) {
                        result.set(result.size() - 1, rep2);
                    }
                }
            }
            return result;
        }

        private static ClickableSpan createSpan(final Context context, final Intent intent) {
            return new ClickableSpan() {
                public void onClick(View widget) {
                    context.startActivity(intent);
                }
            };
        }

        private static boolean hasActivityHandler(Context context, Intent intent) {
            boolean z = false;
            if (intent == null) {
                return false;
            }
            ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, 0);
            if (!(resolveInfo == null || resolveInfo.activityInfo == null)) {
                z = true;
            }
            return z;
        }
    }

    TextClassifierImpl(Context context) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
    }

    public TextSelection suggestSelection(CharSequence text, int selectionStartIndex, int selectionEndIndex, LocaleList defaultLocales) {
        validateInput(text, selectionStartIndex, selectionEndIndex);
        try {
            if (text.length() > 0) {
                SmartSelection smartSelection = getSmartSelection(defaultLocales);
                String string = text.toString();
                int[] startEnd = smartSelection.suggest(string, selectionStartIndex, selectionEndIndex);
                int start = startEnd[0];
                int end = startEnd[1];
                if (start > end || start < 0 || end > string.length() || start > selectionStartIndex || end < selectionEndIndex) {
                    Log.d(LOG_TAG, "Got bad indices for input text. Ignoring result.");
                } else {
                    Builder tsBuilder = new Builder(start, end).setLogSource(LOG_TAG);
                    ClassificationResult[] results = smartSelection.classifyText(string, start, end, getHintFlags(string, start, end));
                    int size = results.length;
                    for (int i = 0; i < size; i++) {
                        tsBuilder.setEntityType(results[i].mCollection, results[i].mScore);
                    }
                    return tsBuilder.build();
                }
            }
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Error suggesting selection for text. No changes to selection suggested.", t);
        }
        return TextClassifier.NO_OP.suggestSelection(text, selectionStartIndex, selectionEndIndex, defaultLocales);
    }

    public TextClassification classifyText(CharSequence text, int startIndex, int endIndex, LocaleList defaultLocales) {
        validateInput(text, startIndex, endIndex);
        try {
            if (text.length() > 0) {
                String string = text.toString();
                ClassificationResult[] results = getSmartSelection(defaultLocales).classifyText(string, startIndex, endIndex, getHintFlags(string, startIndex, endIndex));
                if (results.length > 0) {
                    return createClassificationResult(results, string.subSequence(startIndex, endIndex));
                }
            }
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Error getting assist info.", t);
        }
        return TextClassifier.NO_OP.classifyText(text, startIndex, endIndex, defaultLocales);
    }

    public LinksInfo getLinks(CharSequence text, int linkMask, LocaleList defaultLocales) {
        Preconditions.checkArgument(text != null);
        try {
            return LinksInfoFactory.create(this.mContext, getSmartSelection(defaultLocales), text.toString(), linkMask);
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Error getting links info.", t);
            return TextClassifier.NO_OP.getLinks(text, linkMask, defaultLocales);
        }
    }

    public void logEvent(String source, String event) {
        if (LOG_TAG.equals(source)) {
            this.mMetricsLogger.count(event, 1);
        }
    }

    private SmartSelection getSmartSelection(LocaleList localeList) throws FileNotFoundException {
        SmartSelection smartSelection;
        synchronized (this.mSmartSelectionLock) {
            if (localeList == null) {
                localeList = LocaleList.getEmptyLocaleList();
            }
            Locale locale = findBestSupportedLocaleLocked(localeList);
            if (locale == null) {
                throw new FileNotFoundException("No file for null locale");
            }
            if (this.mSmartSelection == null || (Objects.equals(this.mLocale, locale) ^ 1) != 0) {
                destroySmartSelectionIfExistsLocked();
                ParcelFileDescriptor fd = getFdLocked(locale);
                this.mSmartSelection = new SmartSelection(fd.getFd());
                closeAndLogError(fd);
                this.mLocale = locale;
            }
            smartSelection = this.mSmartSelection;
        }
        return smartSelection;
    }

    @GuardedBy("mSmartSelectionLock")
    private ParcelFileDescriptor getFdLocked(Locale locale) throws FileNotFoundException {
        ParcelFileDescriptor updateFd;
        ParcelFileDescriptor factoryFd;
        try {
            updateFd = ParcelFileDescriptor.open(new File(UPDATED_MODEL_FILE_PATH), 268435456);
        } catch (FileNotFoundException e) {
            updateFd = null;
        }
        try {
            String factoryModelFilePath = (String) getFactoryModelFilePathsLocked().get(locale);
            if (factoryModelFilePath != null) {
                factoryFd = ParcelFileDescriptor.open(new File(factoryModelFilePath), 268435456);
            } else {
                factoryFd = null;
            }
        } catch (FileNotFoundException e2) {
            factoryFd = null;
        }
        if (updateFd != null) {
            int updateFdInt = updateFd.getFd();
            boolean localeMatches = Objects.equals(locale.getLanguage().trim().toLowerCase(), SmartSelection.getLanguage(updateFdInt).trim().toLowerCase());
            if (factoryFd == null) {
                if (localeMatches) {
                    return updateFd;
                }
                closeAndLogError(updateFd);
                throw new FileNotFoundException(String.format("No model file found for %s", new Object[]{locale}));
            } else if (!localeMatches) {
                closeAndLogError(updateFd);
                return factoryFd;
            } else if (SmartSelection.getVersion(updateFdInt) > SmartSelection.getVersion(factoryFd.getFd())) {
                closeAndLogError(factoryFd);
                return updateFd;
            } else {
                closeAndLogError(updateFd);
                return factoryFd;
            }
        } else if (factoryFd != null) {
            return factoryFd;
        } else {
            throw new FileNotFoundException(String.format("No model file found for %s", new Object[]{locale}));
        }
    }

    @GuardedBy("mSmartSelectionLock")
    private void destroySmartSelectionIfExistsLocked() {
        if (this.mSmartSelection != null) {
            this.mSmartSelection.close();
            this.mSmartSelection = null;
        }
    }

    @GuardedBy("mSmartSelectionLock")
    private Locale findBestSupportedLocaleLocked(LocaleList localeList) {
        String languages;
        if (localeList.isEmpty()) {
            languages = LocaleList.getDefault().toLanguageTags();
        } else {
            languages = localeList.toLanguageTags() + "," + LocaleList.getDefault().toLanguageTags();
        }
        List<LanguageRange> languageRangeList = LanguageRange.parse(languages);
        List<Locale> supportedLocales = new ArrayList(getFactoryModelFilePathsLocked().keySet());
        Locale updatedModelLocale = getUpdatedModelLocale();
        if (updatedModelLocale != null) {
            supportedLocales.add(updatedModelLocale);
        }
        return Locale.lookup(languageRangeList, supportedLocales);
    }

    @GuardedBy("mSmartSelectionLock")
    private Map<Locale, String> getFactoryModelFilePathsLocked() {
        if (this.mModelFilePaths == null) {
            Map<Locale, String> modelFilePaths = new HashMap();
            File modelsDir = new File(MODEL_DIR);
            if (modelsDir.exists() && modelsDir.isDirectory()) {
                File[] models = modelsDir.listFiles();
                Pattern modelFilenamePattern = Pattern.compile(MODEL_FILE_REGEX);
                for (File modelFile : models) {
                    Matcher matcher = modelFilenamePattern.matcher(modelFile.getName());
                    if (matcher.matches() && modelFile.isFile()) {
                        modelFilePaths.put(Locale.forLanguageTag(matcher.group(1)), modelFile.getAbsolutePath());
                    }
                }
            }
            this.mModelFilePaths = modelFilePaths;
        }
        return this.mModelFilePaths;
    }

    private Locale getUpdatedModelLocale() {
        try {
            ParcelFileDescriptor updateFd = ParcelFileDescriptor.open(new File(UPDATED_MODEL_FILE_PATH), 268435456);
            Locale locale = Locale.forLanguageTag(SmartSelection.getLanguage(updateFd.getFd()));
            closeAndLogError(updateFd);
            return locale;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private TextClassification createClassificationResult(ClassificationResult[] classifications, CharSequence text) {
        PackageManager pm;
        ResolveInfo resolveInfo;
        String str = null;
        TextClassification.Builder builder = new TextClassification.Builder().setText(text.toString());
        int size = classifications.length;
        for (int i = 0; i < size; i++) {
            builder.setEntityType(classifications[i].mCollection, classifications[i].mScore);
        }
        String type = getHighestScoringType(classifications);
        builder.setLogType(IntentFactory.getLogType(type));
        Intent intent = IntentFactory.create(this.mContext, type, text.toString());
        if (intent != null) {
            pm = this.mContext.getPackageManager();
            resolveInfo = pm.resolveActivity(intent, 0);
        } else {
            pm = null;
            resolveInfo = null;
        }
        if (!(resolveInfo == null || resolveInfo.activityInfo == null)) {
            builder.setIntent(intent).setOnClickListener(TextClassification.createStartActivityOnClickListener(this.mContext, intent));
            String packageName = resolveInfo.activityInfo.packageName;
            if (ZenModeConfig.SYSTEM_AUTHORITY.equals(packageName)) {
                builder.setLabel(IntentFactory.getLabel(this.mContext, type));
            } else {
                intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                Drawable icon = resolveInfo.activityInfo.loadIcon(pm);
                if (icon == null) {
                    icon = resolveInfo.loadIcon(pm);
                }
                builder.setIcon(icon);
                CharSequence label = resolveInfo.activityInfo.loadLabel(pm);
                if (label == null) {
                    label = resolveInfo.loadLabel(pm);
                }
                if (label != null) {
                    str = label.toString();
                }
                builder.setLabel(str);
            }
        }
        return builder.build();
    }

    private static int getHintFlags(CharSequence text, int start, int end) {
        int flag = 0;
        CharSequence subText = text.subSequence(start, end);
        if (Patterns.AUTOLINK_EMAIL_ADDRESS.matcher(subText).matches()) {
            flag = 2;
        }
        if (Patterns.AUTOLINK_WEB_URL.matcher(subText).matches() && Linkify.sUrlMatchFilter.acceptMatch(text, start, end)) {
            return flag | 1;
        }
        return flag;
    }

    private static String getHighestScoringType(ClassificationResult[] types) {
        if (types.length < 1) {
            return LogException.NO_VALUE;
        }
        String type = types[0].mCollection;
        float highestScore = types[0].mScore;
        int size = types.length;
        for (int i = 1; i < size; i++) {
            if (types[i].mScore > highestScore) {
                type = types[i].mCollection;
                highestScore = types[i].mScore;
            }
        }
        return type;
    }

    private static void closeAndLogError(ParcelFileDescriptor fd) {
        try {
            fd.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing file.", e);
        }
    }

    private static void validateInput(CharSequence text, int startIndex, int endIndex) {
        boolean z;
        boolean z2 = true;
        if (text != null) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z);
        if (startIndex >= 0) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z);
        if (endIndex <= text.length()) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z);
        if (endIndex <= startIndex) {
            z2 = false;
        }
        Preconditions.checkArgument(z2);
    }
}
