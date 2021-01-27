package ohos.utils.xml;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.javax.xml.stream.Location;
import ohos.javax.xml.stream.XMLStreamConstants;
import ohos.javax.xml.stream.XMLStreamReader;

public class XmlUtils {
    private static final Map<Integer, String> EVENT_TYPES = Collections.unmodifiableMap(eventTypeToNames());

    private XmlUtils() {
    }

    private static Map<Integer, String> eventTypeToNames() {
        Field[] fields = XMLStreamConstants.class.getFields();
        HashMap hashMap = new HashMap();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                hashMap.put(Integer.valueOf(field.getInt(XMLStreamConstants.class)), field.getName());
            } catch (IllegalAccessException | IllegalArgumentException unused) {
            } catch (Throwable th) {
                field.setAccessible(false);
                throw th;
            }
            field.setAccessible(false);
        }
        return hashMap;
    }

    public static int getLineNumber(XMLStreamReader xMLStreamReader) {
        return xMLStreamReader.getLocation().getLineNumber();
    }

    private static String toEventName(int i) {
        if (!EVENT_TYPES.containsKey(Integer.valueOf(i))) {
            return "UNKOWN";
        }
        return EVENT_TYPES.get(Integer.valueOf(i));
    }

    public static String currentDescription(XMLStreamReader xMLStreamReader) {
        Location location = xMLStreamReader.getLocation();
        return String.format(Locale.ENGLISH, "event <%s> at line %d, column %d", toEventName(xMLStreamReader.getEventType()), Integer.valueOf(location.getLineNumber()), Integer.valueOf(location.getColumnNumber()));
    }
}
