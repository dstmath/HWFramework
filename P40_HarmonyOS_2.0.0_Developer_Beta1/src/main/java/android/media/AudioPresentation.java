package android.media;

import android.icu.util.ULocale;
import android.net.wifi.WifiEnterpriseConfig;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class AudioPresentation {
    public static final int MASTERED_FOR_3D = 3;
    public static final int MASTERED_FOR_HEADPHONE = 4;
    public static final int MASTERED_FOR_STEREO = 1;
    public static final int MASTERED_FOR_SURROUND = 2;
    public static final int MASTERING_NOT_INDICATED = 0;
    private static final int UNKNOWN_ID = -1;
    private final boolean mAudioDescriptionAvailable;
    private final boolean mDialogueEnhancementAvailable;
    private final Map<ULocale, CharSequence> mLabels;
    private final ULocale mLanguage;
    private final int mMasteringIndication;
    private final int mPresentationId;
    private final int mProgramId;
    private final boolean mSpokenSubtitlesAvailable;

    @Retention(RetentionPolicy.SOURCE)
    public @interface MasteringIndicationType {
    }

    private AudioPresentation(int presentationId, int programId, ULocale language, int masteringIndication, boolean audioDescriptionAvailable, boolean spokenSubtitlesAvailable, boolean dialogueEnhancementAvailable, Map<ULocale, CharSequence> labels) {
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
        Map<Locale, String> localeLabels = new HashMap<>(this.mLabels.size());
        for (Map.Entry<ULocale, CharSequence> entry : this.mLabels.entrySet()) {
            localeLabels.put(entry.getKey().toLocale(), entry.getValue().toString());
        }
        return localeLabels;
    }

    private Map<ULocale, CharSequence> getULabels() {
        return this.mLabels;
    }

    public Locale getLocale() {
        return this.mLanguage.toLocale();
    }

    private ULocale getULocale() {
        return this.mLanguage;
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AudioPresentation)) {
            return false;
        }
        AudioPresentation obj = (AudioPresentation) o;
        if (this.mPresentationId == obj.getPresentationId() && this.mProgramId == obj.getProgramId() && this.mLanguage.equals(obj.getULocale()) && this.mMasteringIndication == obj.getMasteringIndication() && this.mAudioDescriptionAvailable == obj.hasAudioDescription() && this.mSpokenSubtitlesAvailable == obj.hasSpokenSubtitles() && this.mDialogueEnhancementAvailable == obj.hasDialogueEnhancement() && this.mLabels.equals(obj.getULabels())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mPresentationId), Integer.valueOf(this.mProgramId), Integer.valueOf(this.mLanguage.hashCode()), Integer.valueOf(this.mMasteringIndication), Boolean.valueOf(this.mAudioDescriptionAvailable), Boolean.valueOf(this.mSpokenSubtitlesAvailable), Boolean.valueOf(this.mDialogueEnhancementAvailable), Integer.valueOf(this.mLabels.hashCode()));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append("{ presentation id=" + this.mPresentationId);
        sb.append(", program id=" + this.mProgramId);
        sb.append(", language=" + this.mLanguage);
        sb.append(", labels=" + this.mLabels);
        sb.append(", mastering indication=" + this.mMasteringIndication);
        sb.append(", audio description=" + this.mAudioDescriptionAvailable);
        sb.append(", spoken subtitles=" + this.mSpokenSubtitlesAvailable);
        sb.append(", dialogue enhancement=" + this.mDialogueEnhancementAvailable);
        sb.append(" }");
        return sb.toString();
    }

    public static final class Builder {
        private boolean mAudioDescriptionAvailable = false;
        private boolean mDialogueEnhancementAvailable = false;
        private Map<ULocale, CharSequence> mLabels = new HashMap();
        private ULocale mLanguage = new ULocale("");
        private int mMasteringIndication = 0;
        private final int mPresentationId;
        private int mProgramId = -1;
        private boolean mSpokenSubtitlesAvailable = false;

        public Builder(int presentationId) {
            this.mPresentationId = presentationId;
        }

        public Builder setProgramId(int programId) {
            this.mProgramId = programId;
            return this;
        }

        public Builder setLocale(ULocale language) {
            this.mLanguage = language;
            return this;
        }

        public Builder setMasteringIndication(int masteringIndication) {
            if (masteringIndication == 0 || masteringIndication == 1 || masteringIndication == 2 || masteringIndication == 3 || masteringIndication == 4) {
                this.mMasteringIndication = masteringIndication;
                return this;
            }
            throw new IllegalArgumentException("Unknown mastering indication: " + masteringIndication);
        }

        public Builder setLabels(Map<ULocale, CharSequence> labels) {
            this.mLabels = new HashMap(labels);
            return this;
        }

        public Builder setHasAudioDescription(boolean audioDescriptionAvailable) {
            this.mAudioDescriptionAvailable = audioDescriptionAvailable;
            return this;
        }

        public Builder setHasSpokenSubtitles(boolean spokenSubtitlesAvailable) {
            this.mSpokenSubtitlesAvailable = spokenSubtitlesAvailable;
            return this;
        }

        public Builder setHasDialogueEnhancement(boolean dialogueEnhancementAvailable) {
            this.mDialogueEnhancementAvailable = dialogueEnhancementAvailable;
            return this;
        }

        public AudioPresentation build() {
            return new AudioPresentation(this.mPresentationId, this.mProgramId, this.mLanguage, this.mMasteringIndication, this.mAudioDescriptionAvailable, this.mSpokenSubtitlesAvailable, this.mDialogueEnhancementAvailable, this.mLabels);
        }
    }
}
