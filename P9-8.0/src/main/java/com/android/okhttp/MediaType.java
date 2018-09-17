package com.android.okhttp;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MediaType {
    private static final Pattern PARAMETER = Pattern.compile(";\\s*(?:([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)=(?:([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)|\"([^\"]*)\"))?");
    private static final String QUOTED = "\"([^\"]*)\"";
    private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
    private static final Pattern TYPE_SUBTYPE = Pattern.compile("([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)/([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)");
    private final String charset;
    private final String mediaType;
    private final String subtype;
    private final String type;

    private MediaType(String mediaType, String type, String subtype, String charset) {
        this.mediaType = mediaType;
        this.type = type;
        this.subtype = subtype;
        this.charset = charset;
    }

    public static MediaType parse(String string) {
        Matcher typeSubtype = TYPE_SUBTYPE.matcher(string);
        if (!typeSubtype.lookingAt()) {
            return null;
        }
        String type = typeSubtype.group(1).toLowerCase(Locale.US);
        String subtype = typeSubtype.group(2).toLowerCase(Locale.US);
        String charset = null;
        Matcher parameter = PARAMETER.matcher(string);
        for (int s = typeSubtype.end(); s < string.length(); s = parameter.end()) {
            parameter.region(s, string.length());
            if (!parameter.lookingAt()) {
                return null;
            }
            String name = parameter.group(1);
            if (name != null && (name.equalsIgnoreCase("charset") ^ 1) == 0) {
                String charsetParameter;
                if (parameter.group(2) != null) {
                    charsetParameter = parameter.group(2);
                } else {
                    charsetParameter = parameter.group(3);
                }
                if (charset == null || (charsetParameter.equalsIgnoreCase(charset) ^ 1) == 0) {
                    charset = charsetParameter;
                } else {
                    throw new IllegalArgumentException("Multiple different charsets: " + string);
                }
            }
        }
        return new MediaType(string, type, subtype, charset);
    }

    public String type() {
        return this.type;
    }

    public String subtype() {
        return this.subtype;
    }

    public Charset charset() {
        return this.charset != null ? Charset.forName(this.charset) : null;
    }

    public Charset charset(Charset defaultValue) {
        return this.charset != null ? Charset.forName(this.charset) : defaultValue;
    }

    public String toString() {
        return this.mediaType;
    }

    public boolean equals(Object o) {
        return o instanceof MediaType ? ((MediaType) o).mediaType.equals(this.mediaType) : false;
    }

    public int hashCode() {
        return this.mediaType.hashCode();
    }
}
