package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTypeAdapter extends TypeAdapter<Date> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            return typeToken.getRawType() != Date.class ? null : new DateTypeAdapter();
        }
    };
    private final DateFormat enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
    private final DateFormat iso8601Format = buildIso8601Format();
    private final DateFormat localFormat = DateFormat.getDateTimeInstance(2, 2);

    private static DateFormat buildIso8601Format() {
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return iso8601Format;
    }

    public Date read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.NULL) {
            return deserializeToDate(in.nextString());
        }
        in.nextNull();
        return null;
    }

    private synchronized Date deserializeToDate(String json) {
        try {
        } catch (ParseException e) {
            try {
                return this.enUsFormat.parse(json);
            } catch (ParseException e2) {
                try {
                    return this.iso8601Format.parse(json);
                } catch (ParseException e3) {
                    throw new JsonSyntaxException(json, e3);
                }
            }
        }
        return this.localFormat.parse(json);
    }

    public synchronized void write(JsonWriter out, Date value) throws IOException {
        if (value != null) {
            out.value(this.enUsFormat.format(value));
        } else {
            out.nullValue();
        }
    }
}
