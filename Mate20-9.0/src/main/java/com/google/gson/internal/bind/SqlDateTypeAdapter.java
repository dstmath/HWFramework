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
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public final class SqlDateTypeAdapter extends TypeAdapter<Date> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            if (typeToken.getRawType() == Date.class) {
                return new SqlDateTypeAdapter();
            }
            return null;
        }
    };
    private final DateFormat format = new SimpleDateFormat("MMM d, yyyy");

    public synchronized Date read(JsonReader in) throws IOException {
        Date date;
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            date = null;
        } else {
            try {
                date = new Date(this.format.parse(in.nextString()).getTime());
            } catch (ParseException e) {
                throw new JsonSyntaxException((Throwable) e);
            }
        }
        return date;
    }

    public synchronized void write(JsonWriter out, Date value) throws IOException {
        out.value(value == null ? null : this.format.format(value));
    }
}
