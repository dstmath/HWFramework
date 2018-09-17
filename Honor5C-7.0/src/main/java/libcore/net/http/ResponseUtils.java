package libcore.net.http;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResponseUtils {
    public static Charset responseCharset(String contentTypeHeader) throws IllegalCharsetNameException, UnsupportedCharsetException {
        Charset responseCharset = StandardCharsets.UTF_8;
        if (contentTypeHeader == null) {
            return responseCharset;
        }
        String charsetParameter = (String) parseContentTypeParameters(contentTypeHeader).get("charset");
        if (charsetParameter != null) {
            return Charset.forName(charsetParameter);
        }
        return responseCharset;
    }

    private static Map<String, String> parseContentTypeParameters(String contentTypeHeader) {
        Map<String, String> parameters = Collections.EMPTY_MAP;
        String[] fields = contentTypeHeader.split(";");
        if (fields.length > 1) {
            parameters = new HashMap();
            for (int i = 1; i < fields.length; i++) {
                String parameter = fields[i];
                if (!parameter.isEmpty()) {
                    String[] components = parameter.split("=");
                    if (components.length == 2) {
                        String key = components[0].trim().toLowerCase();
                        String value = components[1].trim();
                        if (!(key.isEmpty() || value.isEmpty())) {
                            parameters.put(key, value);
                        }
                    }
                }
            }
        }
        return parameters;
    }
}
