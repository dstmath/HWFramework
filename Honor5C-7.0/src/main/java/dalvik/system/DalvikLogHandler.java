package dalvik.system;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface DalvikLogHandler {
    void publish(Logger logger, String str, Level level, String str2);
}
