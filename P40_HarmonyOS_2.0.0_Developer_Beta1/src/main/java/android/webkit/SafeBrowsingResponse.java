package android.webkit;

public abstract class SafeBrowsingResponse {
    public abstract void backToSafety(boolean z);

    public abstract void proceed(boolean z);

    public abstract void showInterstitial(boolean z);
}
