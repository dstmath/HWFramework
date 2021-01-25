package android.view.textclassifier;

import android.app.RemoteAction;
import android.content.Context;
import android.content.Intent;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.ParcelFileDescriptor;
import android.telephony.SmsManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Pair;
import android.view.textclassifier.ActionsModelParamsSupplier;
import android.view.textclassifier.ConversationAction;
import android.view.textclassifier.ConversationActions;
import android.view.textclassifier.ModelFileManager;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassifier;
import android.view.textclassifier.TextLanguage;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextSelection;
import android.view.textclassifier.intent.ClassificationIntentFactory;
import android.view.textclassifier.intent.LabeledIntent;
import android.view.textclassifier.intent.LegacyClassificationIntentFactory;
import android.view.textclassifier.intent.TemplateClassificationIntentFactory;
import android.view.textclassifier.intent.TemplateIntentFactory;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.google.android.textclassifier.ActionsSuggestionsModel;
import com.google.android.textclassifier.AnnotatorModel;
import com.google.android.textclassifier.LangIdModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TextClassifierImpl implements TextClassifier {
    private static final String ACTIONS_FACTORY_MODEL_FILENAME_REGEX = "actions_suggestions\\.(.*)\\.model";
    private static final String ANNOTATOR_FACTORY_MODEL_FILENAME_REGEX = "textclassifier\\.(.*)\\.model";
    private static final File ANNOTATOR_UPDATED_MODEL_FILE = new File("/data/misc/textclassifier/textclassifier.model");
    private static final boolean DEBUG = false;
    private static final File FACTORY_MODEL_DIR = new File("/etc/textclassifier/");
    private static final String LANG_ID_FACTORY_MODEL_FILENAME_REGEX = "lang_id.model";
    private static final String LOG_TAG = "androidtc";
    private static final File UPDATED_ACTIONS_MODEL = new File("/data/misc/textclassifier/actions_suggestions.model");
    private static final File UPDATED_LANG_ID_MODEL_FILE = new File("/data/misc/textclassifier/lang_id.model");
    @GuardedBy({"mLock"})
    private ModelFileManager.ModelFile mActionModelInUse;
    @GuardedBy({"mLock"})
    private ActionsSuggestionsModel mActionsImpl;
    private final ModelFileManager mActionsModelFileManager;
    private final Supplier<ActionsModelParamsSupplier.ActionsModelParams> mActionsModelParamsSupplier;
    @GuardedBy({"mLock"})
    private AnnotatorModel mAnnotatorImpl;
    private final ModelFileManager mAnnotatorModelFileManager;
    @GuardedBy({"mLock"})
    private ModelFileManager.ModelFile mAnnotatorModelInUse;
    private final ClassificationIntentFactory mClassificationIntentFactory;
    private final Context mContext;
    private final TextClassifier mFallback;
    private final GenerateLinksLogger mGenerateLinksLogger;
    @GuardedBy({"mLock"})
    private LangIdModel mLangIdImpl;
    private final ModelFileManager mLangIdModelFileManager;
    @GuardedBy({"mLock"})
    private ModelFileManager.ModelFile mLangIdModelInUse;
    private final Object mLock;
    private final SelectionSessionLogger mSessionLogger;
    private final TextClassificationConstants mSettings;
    private final TemplateIntentFactory mTemplateIntentFactory;
    private final TextClassifierEventTronLogger mTextClassifierEventTronLogger;

    public TextClassifierImpl(Context context, TextClassificationConstants settings, TextClassifier fallback) {
        ClassificationIntentFactory classificationIntentFactory;
        this.mLock = new Object();
        this.mSessionLogger = new SelectionSessionLogger();
        this.mTextClassifierEventTronLogger = new TextClassifierEventTronLogger();
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mFallback = (TextClassifier) Preconditions.checkNotNull(fallback);
        this.mSettings = (TextClassificationConstants) Preconditions.checkNotNull(settings);
        this.mGenerateLinksLogger = new GenerateLinksLogger(this.mSettings.getGenerateLinksLogSampleRate());
        this.mAnnotatorModelFileManager = new ModelFileManager(new ModelFileManager.ModelFileSupplierImpl(FACTORY_MODEL_DIR, ANNOTATOR_FACTORY_MODEL_FILENAME_REGEX, ANNOTATOR_UPDATED_MODEL_FILE, $$Lambda$jJq8RXuVdjYF3lPq77PEw1NJLM.INSTANCE, $$Lambda$NxwbyZSxofZ4Z5SQhfXmtLQ1nxk.INSTANCE));
        this.mLangIdModelFileManager = new ModelFileManager(new ModelFileManager.ModelFileSupplierImpl(FACTORY_MODEL_DIR, LANG_ID_FACTORY_MODEL_FILENAME_REGEX, UPDATED_LANG_ID_MODEL_FILE, $$Lambda$0biFK4yZBmWN1EO2wtnXskzuEcE.INSTANCE, $$Lambda$TextClassifierImpl$RRbXefHgcUymI9P95ArUyMvfbw.INSTANCE));
        this.mActionsModelFileManager = new ModelFileManager(new ModelFileManager.ModelFileSupplierImpl(FACTORY_MODEL_DIR, ACTIONS_FACTORY_MODEL_FILENAME_REGEX, UPDATED_ACTIONS_MODEL, $$Lambda$9N8WImc0VBjy2oxI_Gk5_Pbye_A.INSTANCE, $$Lambda$XeE_KI7QgMKzF9vYRSoFWAolyuA.INSTANCE));
        this.mTemplateIntentFactory = new TemplateIntentFactory();
        if (this.mSettings.isTemplateIntentFactoryEnabled()) {
            classificationIntentFactory = new TemplateClassificationIntentFactory(this.mTemplateIntentFactory, new LegacyClassificationIntentFactory());
        } else {
            classificationIntentFactory = new LegacyClassificationIntentFactory();
        }
        this.mClassificationIntentFactory = classificationIntentFactory;
        this.mActionsModelParamsSupplier = new ActionsModelParamsSupplier(this.mContext, new Runnable() {
            /* class android.view.textclassifier.$$Lambda$TextClassifierImpl$iSt_GuetO6Vtdk0MA4zZ4lzaM */

            @Override // java.lang.Runnable
            public final void run() {
                TextClassifierImpl.this.lambda$new$1$TextClassifierImpl();
            }
        });
    }

    static /* synthetic */ String lambda$new$0(Integer fd) {
        return "*";
    }

    public /* synthetic */ void lambda$new$1$TextClassifierImpl() {
        synchronized (this.mLock) {
            this.mActionsImpl = null;
            this.mActionModelInUse = null;
        }
    }

    public TextClassifierImpl(Context context, TextClassificationConstants settings) {
        this(context, settings, TextClassifier.NO_OP);
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextSelection suggestSelection(TextSelection.Request request) {
        int start;
        int start2;
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            int rangeLength = request.getEndIndex() - request.getStartIndex();
            String string = request.getText().toString();
            if (string.length() > 0) {
                if (rangeLength <= this.mSettings.getSuggestSelectionMaxRangeLength()) {
                    String localesString = concatenateLocales(request.getDefaultLocales());
                    String detectLanguageTags = detectLanguageTagsFromText(request.getText());
                    ZonedDateTime refTime = ZonedDateTime.now();
                    AnnotatorModel annotatorImpl = getAnnotatorImpl(request.getDefaultLocales());
                    if (!this.mSettings.isModelDarkLaunchEnabled() || request.isDarkLaunchAllowed()) {
                        int[] startEnd = annotatorImpl.suggestSelection(string, request.getStartIndex(), request.getEndIndex(), new AnnotatorModel.SelectionOptions(localesString, detectLanguageTags));
                        int start3 = startEnd[0];
                        start2 = startEnd[1];
                        start = start3;
                    } else {
                        start = request.getStartIndex();
                        start2 = request.getEndIndex();
                    }
                    if (start < start2 && start >= 0) {
                        if (start2 <= string.length()) {
                            if (start <= request.getStartIndex() && start2 >= request.getEndIndex()) {
                                TextSelection.Builder tsBuilder = new TextSelection.Builder(start, start2);
                                AnnotatorModel.ClassificationResult[] results = annotatorImpl.classifyText(string, start, start2, new AnnotatorModel.ClassificationOptions(refTime.toInstant().toEpochMilli(), refTime.getZone().getId(), localesString, detectLanguageTags), (Object) null, (String) null);
                                int size = results.length;
                                for (int i = 0; i < size; i++) {
                                    tsBuilder.setEntityType(results[i].getCollection(), results[i].getScore());
                                }
                                return tsBuilder.setId(createId(string, request.getStartIndex(), request.getEndIndex())).build();
                            }
                        }
                    }
                    Log.d("androidtc", "Got bad indices for input text. Ignoring result.");
                }
            }
        } catch (Throwable t) {
            Log.e("androidtc", "Error suggesting selection for text. No changes to selection suggested.", t);
        }
        return this.mFallback.suggestSelection(request);
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextClassification classifyText(TextClassification.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            int rangeLength = request.getEndIndex() - request.getStartIndex();
            String string = request.getText().toString();
            if (string.length() > 0 && rangeLength <= this.mSettings.getClassifyTextMaxRangeLength()) {
                String localesString = concatenateLocales(request.getDefaultLocales());
                String detectLanguageTags = detectLanguageTagsFromText(request.getText());
                ZonedDateTime refTime = request.getReferenceTime() != null ? request.getReferenceTime() : ZonedDateTime.now();
                AnnotatorModel.ClassificationResult[] results = getAnnotatorImpl(request.getDefaultLocales()).classifyText(string, request.getStartIndex(), request.getEndIndex(), new AnnotatorModel.ClassificationOptions(refTime.toInstant().toEpochMilli(), refTime.getZone().getId(), localesString, detectLanguageTags), this.mContext, getResourceLocalesString());
                if (results.length > 0) {
                    return createClassificationResult(results, string, request.getStartIndex(), request.getEndIndex(), refTime.toInstant());
                }
            }
        } catch (Throwable t) {
            Log.e("androidtc", "Error getting text classification info.", t);
        }
        return this.mFallback.classifyText(request);
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextLinks generateLinks(TextLinks.Request request) {
        Collection<String> entitiesToIdentify;
        String callingPackageName;
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkTextLength(request.getText(), getMaxGenerateLinksTextLength());
        TextClassifier.Utils.checkMainThread();
        if (!this.mSettings.isSmartLinkifyEnabled() && request.isLegacyFallback()) {
            return TextClassifier.Utils.generateLegacyLinks(request);
        }
        String textString = request.getText().toString();
        TextLinks.Builder builder = new TextLinks.Builder(textString);
        try {
            long startTimeMs = System.currentTimeMillis();
            ZonedDateTime refTime = ZonedDateTime.now();
            if (request.getEntityConfig() != null) {
                entitiesToIdentify = request.getEntityConfig().resolveEntityListModifications(getEntitiesForHints(request.getEntityConfig().getHints()));
            } else {
                entitiesToIdentify = this.mSettings.getEntityListDefault();
            }
            String localesString = concatenateLocales(request.getDefaultLocales());
            String detectLanguageTags = detectLanguageTagsFromText(request.getText());
            AnnotatorModel annotatorImpl = getAnnotatorImpl(request.getDefaultLocales());
            boolean isSerializedEntityDataEnabled = ExtrasUtils.isSerializedEntityDataEnabled(request);
            AnnotatorModel.AnnotatedSpan[] annotations = annotatorImpl.annotate(textString, new AnnotatorModel.AnnotationOptions(refTime.toInstant().toEpochMilli(), refTime.getZone().getId(), localesString, detectLanguageTags, entitiesToIdentify, AnnotatorModel.AnnotationUsecase.SMART.getValue(), isSerializedEntityDataEnabled));
            int length = annotations.length;
            int i = 0;
            int i2 = 0;
            while (i2 < length) {
                AnnotatorModel.AnnotatedSpan span = annotations[i2];
                AnnotatorModel.ClassificationResult[] results = span.getClassification();
                if (results.length != 0) {
                    if (entitiesToIdentify.contains(results[i].getCollection())) {
                        Map<String, Float> entityScores = new ArrayMap<>();
                        for (int i3 = i; i3 < results.length; i3++) {
                            entityScores.put(results[i3].getCollection(), Float.valueOf(results[i3].getScore()));
                        }
                        Bundle extras = new Bundle();
                        if (isSerializedEntityDataEnabled) {
                            ExtrasUtils.putEntities(extras, results);
                        }
                        builder.addLink(span.getStartIndex(), span.getEndIndex(), entityScores, extras);
                    }
                }
                i2++;
                i = 0;
            }
            TextLinks links = builder.build();
            long endTimeMs = System.currentTimeMillis();
            if (request.getCallingPackageName() == null) {
                callingPackageName = this.mContext.getPackageName();
            } else {
                callingPackageName = request.getCallingPackageName();
            }
            this.mGenerateLinksLogger.logGenerateLinks(request.getText(), links, callingPackageName, endTimeMs - startTimeMs);
            return links;
        } catch (Throwable t) {
            Log.e("androidtc", "Error getting links info.", t);
            return this.mFallback.generateLinks(request);
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public int getMaxGenerateLinksTextLength() {
        return this.mSettings.getGenerateLinksMaxTextLength();
    }

    private Collection<String> getEntitiesForHints(Collection<String> hints) {
        boolean editable = hints.contains(TextClassifier.HINT_TEXT_IS_EDITABLE);
        if (editable == hints.contains(TextClassifier.HINT_TEXT_IS_NOT_EDITABLE)) {
            return this.mSettings.getEntityListDefault();
        }
        if (editable) {
            return this.mSettings.getEntityListEditable();
        }
        return this.mSettings.getEntityListNotEditable();
    }

    @Override // android.view.textclassifier.TextClassifier
    public void onSelectionEvent(SelectionEvent event) {
        this.mSessionLogger.writeEvent(event);
    }

    @Override // android.view.textclassifier.TextClassifier
    public void onTextClassifierEvent(TextClassifierEvent event) {
        try {
            SelectionEvent selEvent = event.toSelectionEvent();
            if (selEvent != null) {
                this.mSessionLogger.writeEvent(selEvent);
            } else {
                this.mTextClassifierEventTronLogger.writeEvent(event);
            }
        } catch (Exception e) {
            Log.e("androidtc", "Error writing event", e);
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public TextLanguage detectLanguage(TextLanguage.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            TextLanguage.Builder builder = new TextLanguage.Builder();
            LangIdModel.LanguageResult[] langResults = getLangIdImpl().detectLanguages(request.getText().toString());
            for (int i = 0; i < langResults.length; i++) {
                builder.putLocale(ULocale.forLanguageTag(langResults[i].getLanguage()), langResults[i].getScore());
            }
            return builder.build();
        } catch (Throwable t) {
            Log.e("androidtc", "Error detecting text language.", t);
            return this.mFallback.detectLanguage(request);
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public ConversationActions suggestConversationActions(ConversationActions.Request request) {
        Preconditions.checkNotNull(request);
        TextClassifier.Utils.checkMainThread();
        try {
            ActionsSuggestionsModel actionsImpl = getActionsImpl();
            if (actionsImpl == null) {
                return this.mFallback.suggestConversationActions(request);
            }
            ActionsSuggestionsModel.ConversationMessage[] nativeMessages = ActionsSuggestionsHelper.toNativeMessages(request.getConversation(), new Function() {
                /* class android.view.textclassifier.$$Lambda$TextClassifierImpl$ftqsQqJYwUdrdbbr9jz3p4AWos */

                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return TextClassifierImpl.this.detectLanguageTagsFromText((CharSequence) obj);
                }
            });
            if (nativeMessages.length == 0) {
                return this.mFallback.suggestConversationActions(request);
            }
            return createConversationActionResult(request, actionsImpl.suggestActionsWithIntents(new ActionsSuggestionsModel.Conversation(nativeMessages), (ActionsSuggestionsModel.ActionSuggestionOptions) null, this.mContext, getResourceLocalesString(), getAnnotatorImpl(LocaleList.getDefault())));
        } catch (Throwable t) {
            Log.e("androidtc", "Error suggesting conversation actions.", t);
            return this.mFallback.suggestConversationActions(request);
        }
    }

    private ConversationActions createConversationActionResult(ConversationActions.Request request, ActionsSuggestionsModel.ActionSuggestion[] nativeSuggestions) {
        Collection<String> expectedTypes = resolveActionTypesFromRequest(request);
        List<ConversationAction> conversationActions = new ArrayList<>();
        for (ActionsSuggestionsModel.ActionSuggestion nativeSuggestion : nativeSuggestions) {
            String actionType = nativeSuggestion.getActionType();
            if (expectedTypes.contains(actionType)) {
                LabeledIntent.Result labeledIntentResult = ActionsSuggestionsHelper.createLabeledIntentResult(this.mContext, this.mTemplateIntentFactory, nativeSuggestion);
                RemoteAction remoteAction = null;
                Bundle extras = new Bundle();
                if (labeledIntentResult != null) {
                    remoteAction = labeledIntentResult.remoteAction;
                    ExtrasUtils.putActionIntent(extras, labeledIntentResult.resolvedIntent);
                }
                ExtrasUtils.putSerializedEntityData(extras, nativeSuggestion.getSerializedEntityData());
                ExtrasUtils.putEntitiesExtras(extras, TemplateIntentFactory.nameVariantsToBundle(nativeSuggestion.getEntityData()));
                conversationActions.add(new ConversationAction.Builder(actionType).setConfidenceScore(nativeSuggestion.getScore()).setTextReply(nativeSuggestion.getResponseText()).setAction(remoteAction).setExtras(extras).build());
            }
        }
        List<ConversationAction> conversationActions2 = ActionsSuggestionsHelper.removeActionsWithDuplicates(conversationActions);
        if (request.getMaxSuggestions() >= 0 && conversationActions2.size() > request.getMaxSuggestions()) {
            conversationActions2 = conversationActions2.subList(0, request.getMaxSuggestions());
        }
        return new ConversationActions(conversationActions2, ActionsSuggestionsHelper.createResultId(this.mContext, request.getConversation(), this.mActionModelInUse.getVersion(), this.mActionModelInUse.getSupportedLocales()));
    }

    /* access modifiers changed from: private */
    public String detectLanguageTagsFromText(CharSequence text) {
        if (!this.mSettings.isDetectLanguagesFromTextEnabled()) {
            return null;
        }
        float threshold = getLangIdThreshold();
        if (threshold < 0.0f || threshold > 1.0f) {
            Log.w("androidtc", "[detectLanguageTagsFromText] unexpected threshold is found: " + threshold);
            return null;
        }
        TextLanguage textLanguage = detectLanguage(new TextLanguage.Request.Builder(text).build());
        int localeHypothesisCount = textLanguage.getLocaleHypothesisCount();
        List<String> languageTags = new ArrayList<>();
        for (int i = 0; i < localeHypothesisCount; i++) {
            ULocale locale = textLanguage.getLocale(i);
            if (textLanguage.getConfidenceScore(locale) < threshold) {
                break;
            }
            languageTags.add(locale.toLanguageTag());
        }
        if (languageTags.isEmpty()) {
            return null;
        }
        return String.join(SmsManager.REGEX_PREFIX_DELIMITER, languageTags);
    }

    private Collection<String> resolveActionTypesFromRequest(ConversationActions.Request request) {
        List<String> defaultActionTypes;
        if (request.getHints().contains("notification")) {
            defaultActionTypes = this.mSettings.getNotificationConversationActionTypes();
        } else {
            defaultActionTypes = this.mSettings.getInAppConversationActionTypes();
        }
        return request.getTypeConfig().resolveEntityListModifications(defaultActionTypes);
    }

    private AnnotatorModel getAnnotatorImpl(LocaleList localeList) throws FileNotFoundException {
        LocaleList localeList2;
        ParcelFileDescriptor pfd;
        synchronized (this.mLock) {
            if (localeList == null) {
                try {
                    localeList2 = LocaleList.getDefault();
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                localeList2 = localeList;
            }
            ModelFileManager.ModelFile bestModel = this.mAnnotatorModelFileManager.findBestModelFile(localeList2);
            if (bestModel != null) {
                if (this.mAnnotatorImpl == null || !Objects.equals(this.mAnnotatorModelInUse, bestModel)) {
                    Log.d("androidtc", "Loading " + bestModel);
                    ParcelFileDescriptor pfd2 = ParcelFileDescriptor.open(new File(bestModel.getPath()), 268435456);
                    if (pfd2 != null) {
                        try {
                            this.mAnnotatorImpl = new AnnotatorModel(pfd2.getFd());
                            this.mAnnotatorModelInUse = bestModel;
                        } catch (Throwable th2) {
                            maybeCloseAndLogError(pfd2);
                            throw th2;
                        }
                    }
                    maybeCloseAndLogError(pfd2);
                }
                pfd = this.mAnnotatorImpl;
            } else {
                throw new FileNotFoundException("No annotator model for " + localeList2.toLanguageTags());
            }
        }
        return pfd;
    }

    private LangIdModel getLangIdImpl() throws FileNotFoundException {
        ParcelFileDescriptor pfd;
        synchronized (this.mLock) {
            ModelFileManager.ModelFile bestModel = this.mLangIdModelFileManager.findBestModelFile(null);
            if (bestModel != null) {
                if (this.mLangIdImpl == null || !Objects.equals(this.mLangIdModelInUse, bestModel)) {
                    Log.d("androidtc", "Loading " + bestModel);
                    ParcelFileDescriptor pfd2 = ParcelFileDescriptor.open(new File(bestModel.getPath()), 268435456);
                    if (pfd2 != null) {
                        try {
                            this.mLangIdImpl = new LangIdModel(pfd2.getFd());
                            this.mLangIdModelInUse = bestModel;
                        } catch (Throwable th) {
                            maybeCloseAndLogError(pfd2);
                            throw th;
                        }
                    }
                    maybeCloseAndLogError(pfd2);
                }
                pfd = this.mLangIdImpl;
            } else {
                throw new FileNotFoundException("No LangID model is found");
            }
        }
        return pfd;
    }

    private ActionsSuggestionsModel getActionsImpl() throws FileNotFoundException {
        synchronized (this.mLock) {
            ModelFileManager.ModelFile bestModel = this.mActionsModelFileManager.findBestModelFile(LocaleList.getDefault());
            if (bestModel == null) {
                return null;
            }
            if (this.mActionsImpl == null || !Objects.equals(this.mActionModelInUse, bestModel)) {
                Log.d("androidtc", "Loading " + bestModel);
                ParcelFileDescriptor pfd = ParcelFileDescriptor.open(new File(bestModel.getPath()), 268435456);
                if (pfd == null) {
                    try {
                        Log.d("androidtc", "Failed to read the model file: " + bestModel.getPath());
                        return null;
                    } finally {
                        maybeCloseAndLogError(pfd);
                    }
                } else {
                    this.mActionsImpl = new ActionsSuggestionsModel(pfd.getFd(), this.mActionsModelParamsSupplier.get().getSerializedPreconditions(bestModel));
                    this.mActionModelInUse = bestModel;
                    maybeCloseAndLogError(pfd);
                }
            }
            return this.mActionsImpl;
        }
    }

    private String createId(String text, int start, int end) {
        String createId;
        synchronized (this.mLock) {
            createId = SelectionSessionLogger.createId(text, start, end, this.mContext, this.mAnnotatorModelInUse.getVersion(), this.mAnnotatorModelInUse.getSupportedLocales());
        }
        return createId;
    }

    private static String concatenateLocales(LocaleList locales) {
        return locales == null ? "" : locales.toLanguageTags();
    }

    /* JADX INFO: Multiple debug info for r1v7 android.app.RemoteAction: [D('textLanguagesBundle' android.os.Bundle), D('action' android.app.RemoteAction)] */
    private TextClassification createClassificationResult(AnnotatorModel.ClassificationResult[] classifications, String text, int start, int end, Instant referenceTime) {
        LabeledIntent.TitleChooser titleChooser;
        List<LabeledIntent> labeledIntents;
        String classifiedText = text.substring(start, end);
        TextClassification.Builder builder = new TextClassification.Builder().setText(classifiedText);
        int typeCount = classifications.length;
        boolean z = false;
        AnnotatorModel.ClassificationResult highestScoringResult = typeCount > 0 ? classifications[0] : null;
        for (int i = 0; i < typeCount; i++) {
            builder.setEntityType(classifications[i]);
            if (classifications[i].getScore() > highestScoringResult.getScore()) {
                highestScoringResult = classifications[i];
            }
        }
        Pair<Bundle, Bundle> languagesBundles = generateLanguageBundles(text, start, end);
        Bundle textLanguagesBundle = languagesBundles.first;
        Bundle foreignLanguageBundle = languagesBundles.second;
        builder.setForeignLanguageExtra(foreignLanguageBundle);
        boolean isPrimaryAction = true;
        ClassificationIntentFactory classificationIntentFactory = this.mClassificationIntentFactory;
        Context context = this.mContext;
        if (foreignLanguageBundle != null) {
            z = true;
        }
        Bundle textLanguagesBundle2 = textLanguagesBundle;
        List<LabeledIntent> labeledIntents2 = classificationIntentFactory.create(context, classifiedText, z, referenceTime, highestScoringResult);
        LabeledIntent.TitleChooser titleChooser2 = $$Lambda$TextClassifierImpl$naj1VfHYH1Qfut8yLHu8DlsggQE.INSTANCE;
        for (LabeledIntent labeledIntent : labeledIntents2) {
            LabeledIntent.Result result = labeledIntent.resolve(this.mContext, titleChooser2, textLanguagesBundle2);
            if (result != null) {
                Intent intent = result.resolvedIntent;
                RemoteAction action = result.remoteAction;
                if (isPrimaryAction) {
                    labeledIntents = labeledIntents2;
                    titleChooser = titleChooser2;
                    builder.setIcon(action.getIcon().loadDrawable(this.mContext));
                    builder.setLabel(action.getTitle().toString());
                    builder.setIntent(intent);
                    builder.setOnClickListener(TextClassification.createIntentOnClickListener(TextClassification.createPendingIntent(this.mContext, intent, labeledIntent.requestCode)));
                    isPrimaryAction = false;
                } else {
                    labeledIntents = labeledIntents2;
                    titleChooser = titleChooser2;
                }
                builder.addAction(action, intent);
                textLanguagesBundle2 = textLanguagesBundle2;
                labeledIntents2 = labeledIntents;
                titleChooser2 = titleChooser;
            }
        }
        return builder.setId(createId(text, start, end)).build();
    }

    private Pair<Bundle, Bundle> generateLanguageBundles(String context, int start, int end) {
        if (!this.mSettings.isTranslateInClassificationEnabled()) {
            return null;
        }
        try {
            float threshold = getLangIdThreshold();
            if (threshold >= 0.0f) {
                if (threshold <= 1.0f) {
                    EntityConfidence languageScores = detectLanguages(context, start, end);
                    if (languageScores.getEntities().isEmpty()) {
                        return Pair.create(null, null);
                    }
                    Bundle textLanguagesBundle = new Bundle();
                    ExtrasUtils.putTopLanguageScores(textLanguagesBundle, languageScores);
                    String language = languageScores.getEntities().get(0);
                    float score = languageScores.getConfidenceScore(language);
                    if (score < threshold) {
                        return Pair.create(textLanguagesBundle, null);
                    }
                    Log.v("androidtc", String.format(Locale.US, "Language detected: <%s:%.2f>", language, Float.valueOf(score)));
                    Locale detected = new Locale(language);
                    LocaleList deviceLocales = LocaleList.getDefault();
                    int size = deviceLocales.size();
                    for (int i = 0; i < size; i++) {
                        if (deviceLocales.get(i).getLanguage().equals(detected.getLanguage())) {
                            return Pair.create(textLanguagesBundle, null);
                        }
                    }
                    return Pair.create(textLanguagesBundle, ExtrasUtils.createForeignLanguageExtra(detected.getLanguage(), score, getLangIdImpl().getVersion()));
                }
            }
            Log.w("androidtc", "[detectForeignLanguage] unexpected threshold is found: " + threshold);
            return Pair.create(null, null);
        } catch (Throwable t) {
            Log.e("androidtc", "Error generating language bundles.", t);
            return Pair.create(null, null);
        }
    }

    private EntityConfidence detectLanguages(String text, int start, int end) throws FileNotFoundException {
        EntityConfidence moreTextScores;
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(end <= text.length());
        Preconditions.checkArgument(start <= end);
        float[] langIdContextSettings = this.mSettings.getLangIdContextSettings();
        int minimumTextSize = (int) langIdContextSettings[0];
        float penalizeRatio = langIdContextSettings[1];
        float subjectTextScoreRatio = langIdContextSettings[2];
        float moreTextScoreRatio = 1.0f - subjectTextScoreRatio;
        Log.v("androidtc", String.format(Locale.US, "LangIdContextSettings: minimumTextSize=%d, penalizeRatio=%.2f, subjectTextScoreRatio=%.2f, moreTextScoreRatio=%.2f", Integer.valueOf(minimumTextSize), Float.valueOf(penalizeRatio), Float.valueOf(subjectTextScoreRatio), Float.valueOf(moreTextScoreRatio)));
        if (end - start < minimumTextSize && penalizeRatio <= 0.0f) {
            return new EntityConfidence(Collections.emptyMap());
        }
        String subject = text.substring(start, end);
        EntityConfidence scores = detectLanguages(subject);
        if (subject.length() < minimumTextSize) {
            if (subject.length() != text.length()) {
                if (subjectTextScoreRatio * penalizeRatio < 1.0f) {
                    if (moreTextScoreRatio >= 0.0f) {
                        moreTextScores = detectLanguages(TextClassifier.Utils.getSubString(text, start, end, minimumTextSize));
                    } else {
                        moreTextScores = new EntityConfidence(Collections.emptyMap());
                    }
                    Map<String, Float> newScores = new ArrayMap<>();
                    Set<String> languages = new ArraySet<>();
                    languages.addAll(scores.getEntities());
                    languages.addAll(moreTextScores.getEntities());
                    for (String language : languages) {
                        newScores.put(language, Float.valueOf(((scores.getConfidenceScore(language) * subjectTextScoreRatio) + (moreTextScores.getConfidenceScore(language) * moreTextScoreRatio)) * penalizeRatio));
                    }
                    return new EntityConfidence(newScores);
                }
            }
        }
        return scores;
    }

    private EntityConfidence detectLanguages(String text) throws FileNotFoundException {
        LangIdModel.LanguageResult[] langResults = getLangIdImpl().detectLanguages(text);
        Map<String, Float> languagesMap = new ArrayMap<>();
        for (LangIdModel.LanguageResult langResult : langResults) {
            languagesMap.put(langResult.getLanguage(), Float.valueOf(langResult.getScore()));
        }
        return new EntityConfidence(languagesMap);
    }

    private float getLangIdThreshold() {
        try {
            if (this.mSettings.getLangIdThresholdOverride() >= 0.0f) {
                return this.mSettings.getLangIdThresholdOverride();
            }
            return getLangIdImpl().getLangIdThreshold();
        } catch (FileNotFoundException e) {
            Log.v("androidtc", "Using default foreign language threshold: 0.5");
            return 0.5f;
        }
    }

    @Override // android.view.textclassifier.TextClassifier
    public void dump(IndentingPrintWriter printWriter) {
        synchronized (this.mLock) {
            printWriter.println("TextClassifierImpl:");
            printWriter.increaseIndent();
            printWriter.println("Annotator model file(s):");
            printWriter.increaseIndent();
            for (ModelFileManager.ModelFile modelFile : this.mAnnotatorModelFileManager.listModelFiles()) {
                printWriter.println(modelFile.toString());
            }
            printWriter.decreaseIndent();
            printWriter.println("LangID model file(s):");
            printWriter.increaseIndent();
            for (ModelFileManager.ModelFile modelFile2 : this.mLangIdModelFileManager.listModelFiles()) {
                printWriter.println(modelFile2.toString());
            }
            printWriter.decreaseIndent();
            printWriter.println("Actions model file(s):");
            printWriter.increaseIndent();
            for (ModelFileManager.ModelFile modelFile3 : this.mActionsModelFileManager.listModelFiles()) {
                printWriter.println(modelFile3.toString());
            }
            printWriter.decreaseIndent();
            printWriter.printPair("mFallback", this.mFallback);
            printWriter.decreaseIndent();
            printWriter.println();
        }
    }

    private static void maybeCloseAndLogError(ParcelFileDescriptor fd) {
        if (fd != null) {
            try {
                fd.close();
            } catch (IOException e) {
                Log.e("androidtc", "Error closing file.", e);
            }
        }
    }

    private String getResourceLocalesString() {
        try {
            return this.mContext.getResources().getConfiguration().getLocales().toLanguageTags();
        } catch (NullPointerException e) {
            return LocaleList.getDefault().toLanguageTags();
        }
    }
}
