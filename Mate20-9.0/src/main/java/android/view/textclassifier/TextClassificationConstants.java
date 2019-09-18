package android.view.textclassifier;

import android.util.KeyValueListParser;
import android.util.Slog;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public final class TextClassificationConstants {
    private static final String CLASSIFY_TEXT_MAX_RANGE_LENGTH = "classify_text_max_range_length";
    private static final int CLASSIFY_TEXT_MAX_RANGE_LENGTH_DEFAULT = 10000;
    private static final String ENTITY_LIST_DEFAULT = "entity_list_default";
    private static final String ENTITY_LIST_DEFAULT_VALUE = new StringJoiner(":").add("address").add("email").add("phone").add("url").add("date").add(TextClassifier.TYPE_DATE_TIME).add(TextClassifier.TYPE_FLIGHT_NUMBER).toString();
    private static final String ENTITY_LIST_DELIMITER = ":";
    private static final String ENTITY_LIST_EDITABLE = "entity_list_editable";
    private static final String ENTITY_LIST_NOT_EDITABLE = "entity_list_not_editable";
    private static final String GENERATE_LINKS_LOG_SAMPLE_RATE = "generate_links_log_sample_rate";
    private static final int GENERATE_LINKS_LOG_SAMPLE_RATE_DEFAULT = 100;
    private static final String GENERATE_LINKS_MAX_TEXT_LENGTH = "generate_links_max_text_length";
    private static final int GENERATE_LINKS_MAX_TEXT_LENGTH_DEFAULT = 100000;
    private static final String LOCAL_TEXT_CLASSIFIER_ENABLED = "local_textclassifier_enabled";
    private static final boolean LOCAL_TEXT_CLASSIFIER_ENABLED_DEFAULT = true;
    private static final String LOG_TAG = "TextClassificationConstants";
    private static final String MODEL_DARK_LAUNCH_ENABLED = "model_dark_launch_enabled";
    private static final boolean MODEL_DARK_LAUNCH_ENABLED_DEFAULT = false;
    private static final String SMART_LINKIFY_ENABLED = "smart_linkify_enabled";
    private static final boolean SMART_LINKIFY_ENABLED_DEFAULT = true;
    private static final String SMART_SELECTION_ENABLED = "smart_selection_enabled";
    private static final boolean SMART_SELECTION_ENABLED_DEFAULT = true;
    private static final String SMART_SELECT_ANIMATION_ENABLED = "smart_select_animation_enabled";
    private static final boolean SMART_SELECT_ANIMATION_ENABLED_DEFAULT = true;
    private static final String SMART_TEXT_SHARE_ENABLED = "smart_text_share_enabled";
    private static final boolean SMART_TEXT_SHARE_ENABLED_DEFAULT = true;
    private static final String SUGGEST_SELECTION_MAX_RANGE_LENGTH = "suggest_selection_max_range_length";
    private static final int SUGGEST_SELECTION_MAX_RANGE_LENGTH_DEFAULT = 10000;
    private static final String SYSTEM_TEXT_CLASSIFIER_ENABLED = "system_textclassifier_enabled";
    private static final boolean SYSTEM_TEXT_CLASSIFIER_ENABLED_DEFAULT = true;
    private final int mClassifyTextMaxRangeLength;
    private final List<String> mEntityListDefault;
    private final List<String> mEntityListEditable;
    private final List<String> mEntityListNotEditable;
    private final int mGenerateLinksLogSampleRate;
    private final int mGenerateLinksMaxTextLength;
    private final boolean mLocalTextClassifierEnabled;
    private final boolean mModelDarkLaunchEnabled;
    private final boolean mSmartLinkifyEnabled;
    private final boolean mSmartSelectionAnimationEnabled;
    private final boolean mSmartSelectionEnabled;
    private final boolean mSmartTextShareEnabled;
    private final int mSuggestSelectionMaxRangeLength;
    private final boolean mSystemTextClassifierEnabled;

    private TextClassificationConstants(String settings) {
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(settings);
        } catch (IllegalArgumentException e) {
            Slog.e(LOG_TAG, "Bad TextClassifier settings: " + settings);
        }
        this.mSystemTextClassifierEnabled = parser.getBoolean(SYSTEM_TEXT_CLASSIFIER_ENABLED, true);
        this.mLocalTextClassifierEnabled = parser.getBoolean(LOCAL_TEXT_CLASSIFIER_ENABLED, true);
        this.mModelDarkLaunchEnabled = parser.getBoolean(MODEL_DARK_LAUNCH_ENABLED, false);
        this.mSmartSelectionEnabled = parser.getBoolean(SMART_SELECTION_ENABLED, true);
        this.mSmartTextShareEnabled = parser.getBoolean(SMART_TEXT_SHARE_ENABLED, true);
        this.mSmartLinkifyEnabled = parser.getBoolean(SMART_LINKIFY_ENABLED, true);
        this.mSmartSelectionAnimationEnabled = parser.getBoolean(SMART_SELECT_ANIMATION_ENABLED, true);
        this.mSuggestSelectionMaxRangeLength = parser.getInt(SUGGEST_SELECTION_MAX_RANGE_LENGTH, 10000);
        this.mClassifyTextMaxRangeLength = parser.getInt(CLASSIFY_TEXT_MAX_RANGE_LENGTH, 10000);
        this.mGenerateLinksMaxTextLength = parser.getInt(GENERATE_LINKS_MAX_TEXT_LENGTH, 100000);
        this.mGenerateLinksLogSampleRate = parser.getInt(GENERATE_LINKS_LOG_SAMPLE_RATE, 100);
        this.mEntityListDefault = parseEntityList(parser.getString(ENTITY_LIST_DEFAULT, ENTITY_LIST_DEFAULT_VALUE));
        this.mEntityListNotEditable = parseEntityList(parser.getString(ENTITY_LIST_NOT_EDITABLE, ENTITY_LIST_DEFAULT_VALUE));
        this.mEntityListEditable = parseEntityList(parser.getString(ENTITY_LIST_EDITABLE, ENTITY_LIST_DEFAULT_VALUE));
    }

    public static TextClassificationConstants loadFromString(String settings) {
        return new TextClassificationConstants(settings);
    }

    public boolean isLocalTextClassifierEnabled() {
        return this.mLocalTextClassifierEnabled;
    }

    public boolean isSystemTextClassifierEnabled() {
        return this.mSystemTextClassifierEnabled;
    }

    public boolean isModelDarkLaunchEnabled() {
        return this.mModelDarkLaunchEnabled;
    }

    public boolean isSmartSelectionEnabled() {
        return this.mSmartSelectionEnabled;
    }

    public boolean isSmartTextShareEnabled() {
        return this.mSmartTextShareEnabled;
    }

    public boolean isSmartLinkifyEnabled() {
        return this.mSmartLinkifyEnabled;
    }

    public boolean isSmartSelectionAnimationEnabled() {
        return this.mSmartSelectionAnimationEnabled;
    }

    public int getSuggestSelectionMaxRangeLength() {
        return this.mSuggestSelectionMaxRangeLength;
    }

    public int getClassifyTextMaxRangeLength() {
        return this.mClassifyTextMaxRangeLength;
    }

    public int getGenerateLinksMaxTextLength() {
        return this.mGenerateLinksMaxTextLength;
    }

    public int getGenerateLinksLogSampleRate() {
        return this.mGenerateLinksLogSampleRate;
    }

    public List<String> getEntityListDefault() {
        return this.mEntityListDefault;
    }

    public List<String> getEntityListNotEditable() {
        return this.mEntityListNotEditable;
    }

    public List<String> getEntityListEditable() {
        return this.mEntityListEditable;
    }

    private static List<String> parseEntityList(String listStr) {
        return Collections.unmodifiableList(Arrays.asList(listStr.split(":")));
    }
}
