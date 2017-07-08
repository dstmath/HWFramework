package sun.net.www.protocol.http;

public interface AuthCache {
    AuthCacheValue get(String str, String str2);

    void put(String str, AuthCacheValue authCacheValue);

    void remove(String str, AuthCacheValue authCacheValue);
}
