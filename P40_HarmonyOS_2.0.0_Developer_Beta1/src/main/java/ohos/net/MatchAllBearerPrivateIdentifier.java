package ohos.net;

public final class MatchAllBearerPrivateIdentifier extends BearerPrivateIdentifier {
    public static boolean checkNotMatchAllBearerPrivateIdentifier(BearerPrivateIdentifier bearerPrivateIdentifier) {
        return !(bearerPrivateIdentifier instanceof MatchAllBearerPrivateIdentifier);
    }
}
