package org.apache.http.impl.client;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

@Deprecated
public class BasicResponseHandler implements ResponseHandler<String> {
    public String handleResponse(HttpResponse response) throws HttpResponseException, IOException {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        return EntityUtils.toString(entity);
    }
}
