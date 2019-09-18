package android.webkit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TracingConfig {
    public static final int CATEGORIES_ALL = 1;
    public static final int CATEGORIES_ANDROID_WEBVIEW = 2;
    public static final int CATEGORIES_FRAME_VIEWER = 64;
    public static final int CATEGORIES_INPUT_LATENCY = 8;
    public static final int CATEGORIES_JAVASCRIPT_AND_RENDERING = 32;
    public static final int CATEGORIES_NONE = 0;
    public static final int CATEGORIES_RENDERING = 16;
    public static final int CATEGORIES_WEB_DEVELOPER = 4;
    public static final int RECORD_CONTINUOUSLY = 1;
    public static final int RECORD_UNTIL_FULL = 0;
    private final List<String> mCustomIncludedCategories = new ArrayList();
    private int mPredefinedCategories;
    private int mTracingMode;

    public static class Builder {
        private final List<String> mCustomIncludedCategories = new ArrayList();
        private int mPredefinedCategories = 0;
        private int mTracingMode = 1;

        public TracingConfig build() {
            return new TracingConfig(this.mPredefinedCategories, this.mCustomIncludedCategories, this.mTracingMode);
        }

        public Builder addCategories(int... predefinedCategories) {
            for (int categorySet : predefinedCategories) {
                this.mPredefinedCategories |= categorySet;
            }
            return this;
        }

        public Builder addCategories(String... categories) {
            for (String category : categories) {
                this.mCustomIncludedCategories.add(category);
            }
            return this;
        }

        public Builder addCategories(Collection<String> categories) {
            this.mCustomIncludedCategories.addAll(categories);
            return this;
        }

        public Builder setTracingMode(int tracingMode) {
            this.mTracingMode = tracingMode;
            return this;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PredefinedCategories {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TracingMode {
    }

    public TracingConfig(int predefinedCategories, List<String> customIncludedCategories, int tracingMode) {
        this.mPredefinedCategories = predefinedCategories;
        this.mCustomIncludedCategories.addAll(customIncludedCategories);
        this.mTracingMode = tracingMode;
    }

    public int getPredefinedCategories() {
        return this.mPredefinedCategories;
    }

    public List<String> getCustomIncludedCategories() {
        return this.mCustomIncludedCategories;
    }

    public int getTracingMode() {
        return this.mTracingMode;
    }
}
