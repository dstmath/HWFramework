package android.view.textclassifier;

import android.app.RemoteAction;
import android.content.Intent;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.view.textclassifier.TextLinks;
import com.android.internal.util.ArrayUtils;
import com.google.android.textclassifier.AnnotatorModel;
import java.util.ArrayList;
import java.util.List;

public final class ExtrasUtils {
    private static final String ACTIONS_INTENTS = "actions-intents";
    private static final String ACTION_INTENT = "action-intent";
    private static final String ENTITIES = "entities";
    private static final String ENTITIES_EXTRAS = "entities-extras";
    private static final String ENTITY_TYPE = "entity-type";
    private static final String FOREIGN_LANGUAGE = "foreign-language";
    private static final String IS_SERIALIZED_ENTITY_DATA_ENABLED = "is-serialized-entity-data-enabled";
    private static final String MODEL_NAME = "model-name";
    private static final String MODEL_VERSION = "model-version";
    private static final String SCORE = "score";
    private static final String SERIALIZED_ENTITIES_DATA = "serialized-entities-data";
    private static final String TEXT_LANGUAGES = "text-languages";

    private ExtrasUtils() {
    }

    static Bundle createForeignLanguageExtra(String language, float score, int modelVersion) {
        Bundle bundle = new Bundle();
        bundle.putString(ENTITY_TYPE, language);
        bundle.putFloat(SCORE, score);
        bundle.putInt(MODEL_VERSION, modelVersion);
        bundle.putString(MODEL_NAME, "langId_v" + modelVersion);
        return bundle;
    }

    static void putForeignLanguageExtra(Bundle container, Bundle extra) {
        container.putParcelable(FOREIGN_LANGUAGE, extra);
    }

    public static Bundle getForeignLanguageExtra(TextClassification classification) {
        if (classification == null) {
            return null;
        }
        return classification.getExtras().getBundle(FOREIGN_LANGUAGE);
    }

    static void putTopLanguageScores(Bundle container, EntityConfidence languageScores) {
        String[] languages = (String[]) languageScores.getEntities().subList(0, Math.min(3, languageScores.getEntities().size())).toArray(new String[0]);
        float[] scores = new float[languages.length];
        for (int i = 0; i < languages.length; i++) {
            scores[i] = languageScores.getConfidenceScore(languages[i]);
        }
        container.putStringArray(ENTITY_TYPE, languages);
        container.putFloatArray(SCORE, scores);
    }

    public static ULocale getTopLanguage(Intent intent) {
        Bundle tcBundle;
        Bundle textLanguagesExtra;
        if (intent == null || (tcBundle = intent.getBundleExtra(TextClassifier.EXTRA_FROM_TEXT_CLASSIFIER)) == null || (textLanguagesExtra = tcBundle.getBundle(TEXT_LANGUAGES)) == null) {
            return null;
        }
        String[] languages = textLanguagesExtra.getStringArray(ENTITY_TYPE);
        float[] scores = textLanguagesExtra.getFloatArray(SCORE);
        if (languages == null || scores == null || languages.length == 0 || languages.length != scores.length) {
            return null;
        }
        int highestScoringIndex = 0;
        for (int i = 1; i < languages.length; i++) {
            if (scores[highestScoringIndex] < scores[i]) {
                highestScoringIndex = i;
            }
        }
        return ULocale.forLanguageTag(languages[highestScoringIndex]);
    }

    public static void putTextLanguagesExtra(Bundle container, Bundle extra) {
        container.putBundle(TEXT_LANGUAGES, extra);
    }

    static void putActionsIntents(Bundle container, ArrayList<Intent> actionsIntents) {
        container.putParcelableArrayList(ACTIONS_INTENTS, actionsIntents);
    }

    public static void putActionIntent(Bundle container, Intent actionIntent) {
        container.putParcelable(ACTION_INTENT, actionIntent);
    }

    public static Intent getActionIntent(Bundle container) {
        return (Intent) container.getParcelable(ACTION_INTENT);
    }

    public static void putSerializedEntityData(Bundle container, byte[] serializedEntityData) {
        container.putByteArray(SERIALIZED_ENTITIES_DATA, serializedEntityData);
    }

    public static byte[] getSerializedEntityData(Bundle container) {
        return container.getByteArray(SERIALIZED_ENTITIES_DATA);
    }

    public static void putEntitiesExtras(Bundle container, Bundle entitiesExtras) {
        container.putParcelable(ENTITIES_EXTRAS, entitiesExtras);
    }

    public static String getCopyText(Bundle container) {
        Bundle entitiesExtras = (Bundle) container.getParcelable(ENTITIES_EXTRAS);
        if (entitiesExtras == null) {
            return null;
        }
        return entitiesExtras.getString("text");
    }

    public static ArrayList<Intent> getActionsIntents(TextClassification classification) {
        if (classification == null) {
            return null;
        }
        return classification.getExtras().getParcelableArrayList(ACTIONS_INTENTS);
    }

    public static RemoteAction findAction(TextClassification classification, String intentAction) {
        ArrayList<Intent> actionIntents;
        if (!(classification == null || intentAction == null || (actionIntents = getActionsIntents(classification)) == null)) {
            int size = actionIntents.size();
            for (int i = 0; i < size; i++) {
                Intent intent = actionIntents.get(i);
                if (intent != null && intentAction.equals(intent.getAction())) {
                    return classification.getActions().get(i);
                }
            }
        }
        return null;
    }

    public static RemoteAction findTranslateAction(TextClassification classification) {
        return findAction(classification, Intent.ACTION_TRANSLATE);
    }

    public static String getEntityType(Bundle extra) {
        if (extra == null) {
            return null;
        }
        return extra.getString(ENTITY_TYPE);
    }

    public static float getScore(Bundle extra) {
        if (extra == null) {
            return -1.0f;
        }
        return extra.getFloat(SCORE, -1.0f);
    }

    public static String getModelName(Bundle extra) {
        if (extra == null) {
            return null;
        }
        return extra.getString(MODEL_NAME);
    }

    public static void putEntities(Bundle container, AnnotatorModel.ClassificationResult[] classifications) {
        if (!ArrayUtils.isEmpty(classifications)) {
            ArrayList<Bundle> entitiesBundle = new ArrayList<>();
            for (AnnotatorModel.ClassificationResult classification : classifications) {
                if (classification != null) {
                    Bundle entityBundle = new Bundle();
                    entityBundle.putString(ENTITY_TYPE, classification.getCollection());
                    entityBundle.putByteArray(SERIALIZED_ENTITIES_DATA, classification.getSerializedEntityData());
                    entitiesBundle.add(entityBundle);
                }
            }
            if (!entitiesBundle.isEmpty()) {
                container.putParcelableArrayList("entities", entitiesBundle);
            }
        }
    }

    public static List<Bundle> getEntities(Bundle container) {
        return container.getParcelableArrayList("entities");
    }

    public static boolean isSerializedEntityDataEnabled(TextLinks.Request request) {
        return request.getExtras().getBoolean(IS_SERIALIZED_ENTITY_DATA_ENABLED);
    }

    public static void putIsSerializedEntityDataEnabled(Bundle bundle, boolean isEnabled) {
        bundle.putBoolean(IS_SERIALIZED_ENTITY_DATA_ENABLED, isEnabled);
    }
}
