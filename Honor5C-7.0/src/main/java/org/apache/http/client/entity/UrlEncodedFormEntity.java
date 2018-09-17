package org.apache.http.client.entity;

import java.io.UnsupportedEncodingException;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

@Deprecated
public class UrlEncodedFormEntity extends StringEntity {
    public UrlEncodedFormEntity(List<? extends NameValuePair> parameters, String encoding) throws UnsupportedEncodingException {
        super(URLEncodedUtils.format(parameters, encoding), encoding);
        setContentType(URLEncodedUtils.CONTENT_TYPE);
    }

    public UrlEncodedFormEntity(List<? extends NameValuePair> parameters) throws UnsupportedEncodingException {
        super(URLEncodedUtils.format(parameters, HTTP.ISO_8859_1), HTTP.ISO_8859_1);
        setContentType(URLEncodedUtils.CONTENT_TYPE);
    }
}
