package org.apache.http.client.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

@Deprecated
public class URLEncodedUtils {
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String NAME_VALUE_SEPARATOR = "=";
    private static final String PARAMETER_SEPARATOR = "&";

    public static List<NameValuePair> parse(URI uri, String encoding) {
        List<NameValuePair> result = Collections.emptyList();
        String query = uri.getRawQuery();
        if (query == null || query.length() <= 0) {
            return result;
        }
        result = new ArrayList();
        parse(result, new Scanner(query), encoding);
        return result;
    }

    public static List<NameValuePair> parse(HttpEntity entity) throws IOException {
        String str = null;
        List<NameValuePair> result = Collections.emptyList();
        if (isEncoded(entity)) {
            String content = EntityUtils.toString(entity);
            Header encoding = entity.getContentEncoding();
            if (content != null && content.length() > 0) {
                result = new ArrayList();
                Scanner scanner = new Scanner(content);
                if (encoding != null) {
                    str = encoding.getValue();
                }
                parse(result, scanner, str);
            }
        }
        return result;
    }

    public static boolean isEncoded(HttpEntity entity) {
        Header contentType = entity.getContentType();
        return contentType != null ? contentType.getValue().equalsIgnoreCase(CONTENT_TYPE) : false;
    }

    public static void parse(List<NameValuePair> parameters, Scanner scanner, String encoding) {
        scanner.useDelimiter(PARAMETER_SEPARATOR);
        while (scanner.hasNext()) {
            String[] nameValue = scanner.next().split(NAME_VALUE_SEPARATOR);
            if (nameValue.length == 0 || nameValue.length > 2) {
                throw new IllegalArgumentException("bad parameter");
            }
            String name = decode(nameValue[0], encoding);
            String value = null;
            if (nameValue.length == 2) {
                value = decode(nameValue[1], encoding);
            }
            parameters.add(new BasicNameValuePair(name, value));
        }
    }

    public static String format(List<? extends NameValuePair> parameters, String encoding) {
        StringBuilder result = new StringBuilder();
        for (NameValuePair parameter : parameters) {
            String encodedName = encode(parameter.getName(), encoding);
            String value = parameter.getValue();
            String encodedValue = value != null ? encode(value, encoding) : "";
            if (result.length() > 0) {
                result.append(PARAMETER_SEPARATOR);
            }
            result.append(encodedName);
            result.append(NAME_VALUE_SEPARATOR);
            result.append(encodedValue);
        }
        return result.toString();
    }

    private static String decode(String content, String encoding) {
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }
        try {
            return URLDecoder.decode(content, encoding);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }

    private static String encode(String content, String encoding) {
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }
        try {
            return URLEncoder.encode(content, encoding);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }
}
