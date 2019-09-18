package dalvik.system;

public final class BlockGuard {
    public static final int DISALLOW_DISK_READ = 2;
    public static final int DISALLOW_DISK_WRITE = 1;
    public static final int DISALLOW_NETWORK = 4;
    public static final Policy LAX_POLICY = new Policy() {
        public void onWriteToDisk() {
        }

        public void onReadFromDisk() {
        }

        public void onNetwork() {
        }

        public void onUnbufferedIO() {
        }

        public int getPolicyMask() {
            return 0;
        }
    };
    public static final int PASS_RESTRICTIONS_VIA_RPC = 8;
    public static final int PENALTY_DEATH = 64;
    public static final int PENALTY_DIALOG = 32;
    public static final int PENALTY_LOG = 16;
    private static ThreadLocal<Policy> threadPolicy = new ThreadLocal<Policy>() {
        /* access modifiers changed from: protected */
        public Policy initialValue() {
            return BlockGuard.LAX_POLICY;
        }
    };

    public static class BlockGuardPolicyException extends RuntimeException {
        private final String mMessage;
        private final int mPolicyState;
        private final int mPolicyViolated;

        public BlockGuardPolicyException(int policyState, int policyViolated) {
            this(policyState, policyViolated, null);
        }

        public BlockGuardPolicyException(int policyState, int policyViolated, String message) {
            this.mPolicyState = policyState;
            this.mPolicyViolated = policyViolated;
            this.mMessage = message;
            fillInStackTrace();
        }

        public int getPolicy() {
            return this.mPolicyState;
        }

        public int getPolicyViolation() {
            return this.mPolicyViolated;
        }

        public String getMessage() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("policy=");
            sb.append(this.mPolicyState);
            sb.append(" violation=");
            sb.append(this.mPolicyViolated);
            if (this.mMessage == null) {
                str = "";
            } else {
                str = " msg=" + this.mMessage;
            }
            sb.append(str);
            return sb.toString();
        }
    }

    public interface Policy {
        int getPolicyMask();

        void onNetwork();

        void onReadFromDisk();

        void onUnbufferedIO();

        void onWriteToDisk();
    }

    public static Policy getThreadPolicy() {
        return threadPolicy.get();
    }

    public static void setThreadPolicy(Policy policy) {
        if (policy != null) {
            threadPolicy.set(policy);
            return;
        }
        throw new NullPointerException("policy == null");
    }

    private BlockGuard() {
    }
}
