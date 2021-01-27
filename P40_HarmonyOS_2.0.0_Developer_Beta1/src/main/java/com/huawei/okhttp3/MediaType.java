package com.huawei.okhttp3;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

@Deprecated
public final class MediaType {
    private static final Pattern PARAMETER = Pattern.compile(";\\s*(?:([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)=(?:([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)|\"([^\"]*)\"))?");
    private static final String QUOTED = "\"([^\"]*)\"";
    private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
    private static final Pattern TYPE_SUBTYPE = Pattern.compile("([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)/([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)");
    @Nullable
    private final String charset;
    private final String mediaType;
    private final String subtype;
    private final String type;

    private MediaType(String mediaType2, String type2, String subtype2, @Nullable String charset2) {
        this.mediaType = mediaType2;
        this.type = type2;
        this.subtype = subtype2;
        this.charset = charset2;
    }

    public static MediaType get(String string) {
        String charsetParameter;
        Matcher typeSubtype = TYPE_SUBTYPE.matcher(string);
        if (typeSubtype.lookingAt()) {
            String type2 = typeSubtype.group(1).toLowerCase(Locale.US);
            String subtype2 = typeSubtype.group(2).toLowerCase(Locale.US);
            String charset2 = null;
            Matcher parameter = PARAMETER.matcher(string);
            for (int s = typeSubtype.end(); s < string.length(); s = parameter.end()) {
                parameter.region(s, string.length());
                if (parameter.lookingAt()) {
                    String name = parameter.group(1);
                    if (name != null && name.equalsIgnoreCase("charset")) {
                        String token = parameter.group(2);
                        if (token == null) {
                            charsetParameter = parameter.group(3);
                        } else if (!token.startsWith("'") || !token.endsWith("'") || token.length() <= 2) {
                            charsetParameter = token;
                        } else {
                            charsetParameter = token.substring(1, token.length() - 1);
                        }
                        if (charset2 == null || charsetParameter.equalsIgnoreCase(charset2)) {
                            charset2 = charsetParameter;
                        } else {
                            throw new IllegalArgumentException("Multiple charsets defined: \"" + charset2 + "\" and: \"" + charsetParameter + "\" for: \"" + string + '\"');
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Parameter is not formatted correctly: \"" + string.substring(s) + "\" for: \"" + string + '\"');
                }
            }
            return new MediaType(string, type2, subtype2, charset2);
        }
        throw new IllegalArgumentException("No subtype found for: \"" + string + '\"');
    }

    @Nullable
    public static MediaType parse(String string) {
        try {
            return get(string);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String type() {
        return this.type;
    }

    public String subtype() {
        return this.subtype;
    }

    @Nullable
    public Charset charset() {
        return charset(null);
    }

    @Nullable
    public Charset charset(@Nullable Charset defaultValue) {
        try {
            return this.charset != null ? Charset.forName(this.charset) : defaultValue;
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public String toString() {
        return this.mediaType;
    }

    public boolean equals(@Nullable Object other) {
        return (other instanceof MediaType) && ((MediaType) other).mediaType.equals(this.mediaType);
    }

    public int hashCode() {
        return this.mediaType.hashCode();
    }
}
