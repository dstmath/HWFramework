package android.webkit;

import java.util.Map;

public class WebStorage {

    public static class Origin {
        private String mOrigin;
        private long mQuota;
        private long mUsage;

        protected Origin(String origin, long quota, long usage) {
            this.mOrigin = null;
            this.mQuota = 0;
            this.mUsage = 0;
            this.mOrigin = origin;
            this.mQuota = quota;
            this.mUsage = usage;
        }

        public String getOrigin() {
            return this.mOrigin;
        }

        public long getQuota() {
            return this.mQuota;
        }

        public long getUsage() {
            return this.mUsage;
        }
    }

    @Deprecated
    public interface QuotaUpdater {
        void updateQuota(long j);
    }

    public void getOrigins(ValueCallback<Map> valueCallback) {
    }

    public void getUsageForOrigin(String origin, ValueCallback<Long> valueCallback) {
    }

    public void getQuotaForOrigin(String origin, ValueCallback<Long> valueCallback) {
    }

    @Deprecated
    public void setQuotaForOrigin(String origin, long quota) {
    }

    public void deleteOrigin(String origin) {
    }

    public void deleteAllData() {
    }

    public static WebStorage getInstance() {
        return WebViewFactory.getProvider().getWebStorage();
    }
}
