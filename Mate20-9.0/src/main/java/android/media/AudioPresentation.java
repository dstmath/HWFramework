package android.media;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AudioPresentation {
    public static final int MASTERED_FOR_3D = 3;
    public static final int MASTERED_FOR_HEADPHONE = 4;
    public static final int MASTERED_FOR_STEREO = 1;
    public static final int MASTERED_FOR_SURROUND = 2;
    public static final int MASTERING_NOT_INDICATED = 0;
    private final boolean mAudioDescriptionAvailable;
    private final boolean mDialogueEnhancementAvailable;
    private final Map<String, String> mLabels;
    private final String mLanguage;
    private final int mMasteringIndication;
    private final int mPresentationId;
    private final int mProgramId;
    private final boolean mSpokenSubtitlesAvailable;

    @Retention(RetentionPolicy.SOURCE)
    public @interface MasteringIndicationType {
    }

    public AudioPresentation(int presentationId, int programId, Map<String, String> labels, String language, int masteringIndication, boolean audioDescriptionAvailable, boolean spokenSubtitlesAvailable, boolean dialogueEnhancementAvailable) {
        this.mPresentationId = presentationId;
        this.mProgramId = programId;
        this.mLanguage = language;
        this.mMasteringIndication = masteringIndication;
        this.mAudioDescriptionAvailable = audioDescriptionAvailable;
        this.mSpokenSubtitlesAvailable = spokenSubtitlesAvailable;
        this.mDialogueEnhancementAvailable = dialogueEnhancementAvailable;
        this.mLabels = new HashMap(labels);
    }

    public int getPresentationId() {
        return this.mPresentationId;
    }

    public int getProgramId() {
        return this.mProgramId;
    }

    public Map<Locale, String> getLabels() {
        Map<Locale, String> localeLabels = new HashMap<>();
        for (Map.Entry<String, String> entry : this.mLabels.entrySet()) {
            localeLabels.put(new Locale(entry.getKey()), entry.getValue());
        }
        return localeLabels;
    }

    public Locale getLocale() {
        return new Locale(this.mLanguage);
    }

    public int getMasteringIndication() {
        return this.mMasteringIndication;
    }

    public boolean hasAudioDescription() {
        return this.mAudioDescriptionAvailable;
    }

    public boolean hasSpokenSubtitles() {
        return this.mSpokenSubtitlesAvailable;
    }

    public boolean hasDialogueEnhancement() {
        return this.mDialogueEnhancementAvailable;
    }
}
