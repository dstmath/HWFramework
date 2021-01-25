package com.android.server.uri;

import android.content.ContentProvider;
import android.net.Uri;
import android.util.proto.ProtoOutputStream;

public class GrantUri {
    public boolean prefix;
    public final int sourceUserId;
    public final Uri uri;

    public GrantUri(int sourceUserId2, Uri uri2, boolean prefix2) {
        this.sourceUserId = sourceUserId2;
        this.uri = uri2;
        this.prefix = prefix2;
    }

    public int hashCode() {
        return (((((1 * 31) + this.sourceUserId) * 31) + this.uri.hashCode()) * 31) + (this.prefix ? 1231 : 1237);
    }

    public boolean equals(Object o) {
        if (!(o instanceof GrantUri)) {
            return false;
        }
        GrantUri other = (GrantUri) o;
        if (this.uri.equals(other.uri) && this.sourceUserId == other.sourceUserId && this.prefix == other.prefix) {
            return true;
        }
        return false;
    }

    public String toString() {
        String result = this.uri.toString() + " [user " + this.sourceUserId + "]";
        if (!this.prefix) {
            return result;
        }
        return result + " [prefix]";
    }

    public String toSafeString() {
        String result = this.uri.toSafeString() + " [user " + this.sourceUserId + "]";
        if (!this.prefix) {
            return result;
        }
        return result + " [prefix]";
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333442L, this.uri.toString());
        proto.write(1120986464257L, this.sourceUserId);
        proto.end(token);
    }

    public static GrantUri resolve(int defaultSourceUserHandle, Uri uri2) {
        if ("content".equals(uri2.getScheme())) {
            return new GrantUri(ContentProvider.getUserIdFromUri(uri2, defaultSourceUserHandle), ContentProvider.getUriWithoutUserId(uri2), false);
        }
        return new GrantUri(defaultSourceUserHandle, uri2, false);
    }
}
