package android.attention;

public abstract class AttentionManagerInternal {

    public static abstract class AttentionCallbackInternal {
        public abstract void onFailure(int i);

        public abstract void onSuccess(int i, long j);
    }

    public abstract void cancelAttentionCheck(AttentionCallbackInternal attentionCallbackInternal);

    public abstract boolean checkAttention(long j, AttentionCallbackInternal attentionCallbackInternal);

    public abstract boolean isAttentionServiceSupported();
}
