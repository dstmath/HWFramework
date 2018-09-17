package android.media.effect.effects;

import android.filterfw.core.OneShotScheduler;
import android.filterpacks.videoproc.BackDropperFilter;
import android.filterpacks.videoproc.BackDropperFilter.LearningDoneListener;
import android.media.effect.EffectContext;
import android.media.effect.EffectUpdateListener;
import android.media.effect.FilterGraphEffect;

public class BackDropperEffect extends FilterGraphEffect {
    private static final String mGraphDefinition = "@import android.filterpacks.base;\n@import android.filterpacks.videoproc;\n@import android.filterpacks.videosrc;\n\n@filter GLTextureSource foreground {\n  texId = 0;\n  width = 0;\n  height = 0;\n  repeatFrame = true;\n}\n\n@filter MediaSource background {\n  sourceUrl = \"no_file_specified\";\n  waitForNewFrame = false;\n  sourceIsUrl = true;\n}\n\n@filter BackDropperFilter replacer {\n  autowbToggle = 1;\n}\n\n@filter GLTextureTarget output {\n  texId = 0;\n}\n\n@connect foreground[frame]  => replacer[video];\n@connect background[video]  => replacer[background];\n@connect replacer[video]    => output[frame];\n";
    private EffectUpdateListener mEffectListener = null;
    private LearningDoneListener mLearningListener = new LearningDoneListener() {
        public void onLearningDone(BackDropperFilter filter) {
            if (BackDropperEffect.this.mEffectListener != null) {
                BackDropperEffect.this.mEffectListener.onEffectUpdated(BackDropperEffect.this, null);
            }
        }
    };

    public BackDropperEffect(EffectContext context, String name) {
        super(context, name, mGraphDefinition, "foreground", "output", OneShotScheduler.class);
        this.mGraph.getFilter("replacer").setInputValue("learningDoneListener", this.mLearningListener);
    }

    public void setParameter(String parameterKey, Object value) {
        if (parameterKey.equals("source")) {
            this.mGraph.getFilter("background").setInputValue("sourceUrl", value);
        } else if (parameterKey.equals("context")) {
            this.mGraph.getFilter("background").setInputValue("context", value);
        }
    }

    public void setUpdateListener(EffectUpdateListener listener) {
        this.mEffectListener = listener;
    }
}
