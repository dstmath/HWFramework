package android.view.textclassifier;

import android.app.Person;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Pair;
import android.view.textclassifier.ConversationActions;
import android.view.textclassifier.SelectionSessionLogger;
import android.view.textclassifier.intent.LabeledIntent;
import android.view.textclassifier.intent.TemplateIntentFactory;
import com.android.internal.annotations.VisibleForTesting;
import com.google.android.textclassifier.ActionsSuggestionsModel;
import com.google.android.textclassifier.RemoteActionTemplate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class ActionsSuggestionsHelper {
    private static final int FIRST_NON_LOCAL_USER = 1;
    private static final String TAG = "ActionsSuggestions";
    private static final int USER_LOCAL = 0;

    private ActionsSuggestionsHelper() {
    }

    public static ActionsSuggestionsModel.ConversationMessage[] toNativeMessages(List<ConversationActions.Message> messages, Function<CharSequence, String> languageDetector) {
        List<ConversationActions.Message> messagesWithText = (List) messages.stream().filter($$Lambda$ActionsSuggestionsHelper$6oTtcn9bDEu8FbiyGdntqoQG0.INSTANCE).collect(Collectors.toCollection($$Lambda$OGSS2qx6njxlnp0dnKb4lA3jnw8.INSTANCE));
        if (messagesWithText.isEmpty()) {
            return new ActionsSuggestionsModel.ConversationMessage[0];
        }
        Deque<ActionsSuggestionsModel.ConversationMessage> nativeMessages = new ArrayDeque<>();
        PersonEncoder personEncoder = new PersonEncoder();
        for (int i = messagesWithText.size() - 1; i >= 0; i--) {
            ConversationActions.Message message = messagesWithText.get(i);
            nativeMessages.push(new ActionsSuggestionsModel.ConversationMessage(personEncoder.encode(message.getAuthor()), message.getText().toString(), message.getReferenceTime() == null ? 0 : message.getReferenceTime().toInstant().toEpochMilli(), message.getReferenceTime() == null ? null : message.getReferenceTime().getZone().getId(), languageDetector.apply(message.getText())));
        }
        return (ActionsSuggestionsModel.ConversationMessage[]) nativeMessages.toArray(new ActionsSuggestionsModel.ConversationMessage[nativeMessages.size()]);
    }

    static /* synthetic */ boolean lambda$toNativeMessages$0(ConversationActions.Message message) {
        return !TextUtils.isEmpty(message.getText());
    }

    public static String createResultId(Context context, List<ConversationActions.Message> messages, int modelVersion, List<Locale> modelLocales) {
        StringJoiner localesJoiner = new StringJoiner(SmsManager.REGEX_PREFIX_DELIMITER);
        for (Locale locale : modelLocales) {
            localesJoiner.add(locale.toLanguageTag());
        }
        return SelectionSessionLogger.SignatureParser.createSignature(TextClassifier.DEFAULT_LOG_TAG, String.format(Locale.US, "%s_v%d", localesJoiner.toString(), Integer.valueOf(modelVersion)), Objects.hash(messages.stream().mapToInt($$Lambda$ActionsSuggestionsHelper$YTQv8oPvlmJL4tITUFD4z4JWKRk.INSTANCE), context.getPackageName(), Long.valueOf(System.currentTimeMillis())));
    }

    public static LabeledIntent.Result createLabeledIntentResult(Context context, TemplateIntentFactory templateIntentFactory, ActionsSuggestionsModel.ActionSuggestion nativeSuggestion) {
        RemoteActionTemplate[] remoteActionTemplates = nativeSuggestion.getRemoteActionTemplates();
        if (remoteActionTemplates == null) {
            Log.w(TAG, "createRemoteAction: Missing template for type " + nativeSuggestion.getActionType());
            return null;
        }
        List<LabeledIntent> labeledIntents = templateIntentFactory.create(remoteActionTemplates);
        if (labeledIntents.isEmpty()) {
            return null;
        }
        return labeledIntents.get(0).resolve(context, createTitleChooser(nativeSuggestion.getActionType()), null);
    }

    public static LabeledIntent.TitleChooser createTitleChooser(String actionType) {
        if (ConversationAction.TYPE_OPEN_URL.equals(actionType)) {
            return $$Lambda$ActionsSuggestionsHelper$sY0w9od2zcl4YFel0lG4VB3vf7I.INSTANCE;
        }
        return null;
    }

    static /* synthetic */ CharSequence lambda$createTitleChooser$1(LabeledIntent labeledIntent, ResolveInfo resolveInfo) {
        if (resolveInfo.handleAllWebDataURI) {
            return labeledIntent.titleWithEntity;
        }
        if ("android".equals(resolveInfo.activityInfo.packageName)) {
            return labeledIntent.titleWithEntity;
        }
        return labeledIntent.titleWithoutEntity;
    }

    public static List<ConversationAction> removeActionsWithDuplicates(List<ConversationAction> conversationActions) {
        Map<Pair<String, String>, Integer> counter = new ArrayMap<>();
        for (ConversationAction conversationAction : conversationActions) {
            Pair<String, String> representation = getRepresentation(conversationAction);
            if (representation != null) {
                counter.put(representation, Integer.valueOf(counter.getOrDefault(representation, 0).intValue() + 1));
            }
        }
        List<ConversationAction> result = new ArrayList<>();
        for (ConversationAction conversationAction2 : conversationActions) {
            Pair<String, String> representation2 = getRepresentation(conversationAction2);
            if (representation2 == null || counter.getOrDefault(representation2, 0).intValue() == 1) {
                result.add(conversationAction2);
            }
        }
        return result;
    }

    private static Pair<String, String> getRepresentation(ConversationAction conversationAction) {
        String packageName = null;
        if (conversationAction.getAction() == null) {
            return null;
        }
        ComponentName componentName = ExtrasUtils.getActionIntent(conversationAction.getExtras()).getComponent();
        if (componentName != null) {
            packageName = componentName.getPackageName();
        }
        return new Pair<>(conversationAction.getAction().getTitle().toString(), packageName);
    }

    private static final class PersonEncoder {
        private final Map<Person, Integer> mMapping;
        private int mNextUserId;

        private PersonEncoder() {
            this.mMapping = new ArrayMap();
            this.mNextUserId = 1;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int encode(Person person) {
            if (ConversationActions.Message.PERSON_USER_SELF.equals(person)) {
                return 0;
            }
            Integer result = this.mMapping.get(person);
            if (result == null) {
                this.mMapping.put(person, Integer.valueOf(this.mNextUserId));
                result = Integer.valueOf(this.mNextUserId);
                this.mNextUserId++;
            }
            return result.intValue();
        }
    }

    /* access modifiers changed from: private */
    public static int hashMessage(ConversationActions.Message message) {
        return Objects.hash(message.getAuthor(), message.getText(), message.getReferenceTime());
    }
}
