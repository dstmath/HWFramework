package dalvik.system;

import org.xmlpull.v1.XmlPullParser;

public final class BlockGuard {
    public static final int DISALLOW_DISK_READ = 2;
    public static final int DISALLOW_DISK_WRITE = 1;
    public static final int DISALLOW_NETWORK = 4;
    public static final Policy LAX_POLICY = null;
    public static final int PASS_RESTRICTIONS_VIA_RPC = 8;
    public static final int PENALTY_DEATH = 64;
    public static final int PENALTY_DIALOG = 32;
    public static final int PENALTY_LOG = 16;
    private static ThreadLocal<Policy> threadPolicy;

    public interface Policy {
        int getPolicyMask();

        void onNetwork();

        void onReadFromDisk();

        void onWriteToDisk();
    }

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
            return "policy=" + this.mPolicyState + " violation=" + this.mPolicyViolated + (this.mMessage == null ? XmlPullParser.NO_NAMESPACE : " msg=" + this.mMessage);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: dalvik.system.BlockGuard.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: dalvik.system.BlockGuard.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.BlockGuard.<clinit>():void");
    }

    public static Policy getThreadPolicy() {
        return (Policy) threadPolicy.get();
    }

    public static void setThreadPolicy(Policy policy) {
        if (policy == null) {
            throw new NullPointerException("policy == null");
        }
        threadPolicy.set(policy);
    }

    private BlockGuard() {
    }
}
