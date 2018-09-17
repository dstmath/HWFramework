package android.net;

public abstract class NetworkSpecifier {
    public abstract boolean satisfiedBy(NetworkSpecifier networkSpecifier);
}
