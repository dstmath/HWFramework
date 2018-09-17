package java.lang;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import libcore.io.ClassPathURLStreamHandler;

class VMClassLoader {
    private static final ClassPathURLStreamHandler[] bootClassPathUrlHandlers = createBootClassPathUrlHandlers();

    static native Class findLoadedClass(ClassLoader classLoader, String str);

    private static native String[] getBootClassPathEntries();

    VMClassLoader() {
    }

    private static ClassPathURLStreamHandler[] createBootClassPathUrlHandlers() {
        String[] bootClassPathEntries = getBootClassPathEntries();
        ArrayList<URLStreamHandler> urlStreamHandlers = new ArrayList(bootClassPathEntries.length);
        for (String bootClassPathEntry : bootClassPathEntries) {
            try {
                String entryUri = new File(bootClassPathEntry).toURI().toString();
                urlStreamHandlers.add(new ClassPathURLStreamHandler(bootClassPathEntry));
            } catch (IOException e) {
                System.logE("Unable to open boot classpath entry: " + bootClassPathEntry, e);
            }
        }
        return (ClassPathURLStreamHandler[]) urlStreamHandlers.toArray(new ClassPathURLStreamHandler[urlStreamHandlers.size()]);
    }

    static URL getResource(String name) {
        for (ClassPathURLStreamHandler urlHandler : bootClassPathUrlHandlers) {
            URL url = urlHandler.getEntryUrlOrNull(name);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    static List<URL> getResources(String name) {
        ArrayList<URL> list = new ArrayList();
        for (ClassPathURLStreamHandler urlHandler : bootClassPathUrlHandlers) {
            URL url = urlHandler.getEntryUrlOrNull(name);
            if (url != null) {
                list.add(url);
            }
        }
        return list;
    }
}
