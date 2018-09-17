package dalvik.system;

import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import libcore.io.ClassPathURLStreamHandler;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import org.xmlpull.v1.XmlPullParser;

final class DexPathList {
    private static final String DEX_SUFFIX = ".dex";
    private static final String zipSeparator = "!/";
    private final ClassLoader definingContext;
    private Element[] dexElements;
    private IOException[] dexElementsSuppressedExceptions;
    private final List<File> nativeLibraryDirectories;
    private final Element[] nativeLibraryPathElements;
    private final List<File> systemNativeLibraryDirectories;

    static class Element {
        private final DexFile dexFile;
        private final File dir;
        private boolean initialized;
        private final boolean isDirectory;
        private ClassPathURLStreamHandler urlHandler;
        private final File zip;

        public Element(File dir, boolean isDirectory, File zip, DexFile dexFile) {
            this.dir = dir;
            this.isDirectory = isDirectory;
            this.zip = zip;
            this.dexFile = dexFile;
        }

        public String toString() {
            if (this.isDirectory) {
                return "directory \"" + this.dir + "\"";
            }
            if (this.zip == null) {
                return "dex file \"" + this.dexFile + "\"";
            }
            StringBuilder append = new StringBuilder().append("zip file \"").append(this.zip).append("\"");
            String str = (this.dir == null || this.dir.getPath().isEmpty()) ? XmlPullParser.NO_NAMESPACE : ", dir \"" + this.dir + "\"";
            return append.append(str).toString();
        }

        public synchronized void maybeInit() {
            if (!this.initialized) {
                this.initialized = true;
                if (!this.isDirectory && this.zip != null) {
                    try {
                        this.urlHandler = new ClassPathURLStreamHandler(this.zip.getPath());
                    } catch (IOException ioe) {
                        System.logE("Unable to open zip file: " + this.zip, ioe);
                        this.urlHandler = null;
                    }
                }
            }
        }

        public String findNativeLibrary(String name) {
            maybeInit();
            if (this.isDirectory) {
                String path = new File(this.dir, name).getPath();
                if (IoUtils.canOpenReadOnly(path)) {
                    return path;
                }
            } else if (this.urlHandler != null) {
                String entryName = new File(this.dir, name).getPath();
                if (this.urlHandler.isEntryStored(entryName)) {
                    return this.zip.getPath() + DexPathList.zipSeparator + entryName;
                }
            }
            return null;
        }

        public URL findResource(String name) {
            maybeInit();
            if (this.isDirectory) {
                File resourceFile = new File(this.dir, name);
                if (resourceFile.exists()) {
                    try {
                        return resourceFile.toURI().toURL();
                    } catch (MalformedURLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            if (this.urlHandler == null) {
                return null;
            }
            return this.urlHandler.getEntryUrlOrNull(name);
        }
    }

    public DexPathList(ClassLoader definingContext, String dexPath, String librarySearchPath, File optimizedDirectory) {
        if (definingContext == null) {
            throw new NullPointerException("definingContext == null");
        } else if (dexPath == null) {
            throw new NullPointerException("dexPath == null");
        } else {
            if (optimizedDirectory != null) {
                if (optimizedDirectory.exists()) {
                    boolean canWrite;
                    if (optimizedDirectory.canRead()) {
                        canWrite = optimizedDirectory.canWrite();
                    } else {
                        canWrite = false;
                    }
                    if (!canWrite) {
                        throw new IllegalArgumentException("optimizedDirectory not readable/writable: " + optimizedDirectory);
                    }
                }
                throw new IllegalArgumentException("optimizedDirectory doesn't exist: " + optimizedDirectory);
            }
            this.definingContext = definingContext;
            List suppressedExceptions = new ArrayList();
            this.dexElements = makeDexElements(splitDexPath(dexPath), optimizedDirectory, suppressedExceptions, definingContext);
            this.nativeLibraryDirectories = splitPaths(librarySearchPath, false);
            this.systemNativeLibraryDirectories = splitPaths(System.getProperty("java.library.path"), true);
            List allNativeLibraryDirectories = new ArrayList(this.nativeLibraryDirectories);
            allNativeLibraryDirectories.addAll(this.systemNativeLibraryDirectories);
            this.nativeLibraryPathElements = makePathElements(allNativeLibraryDirectories, suppressedExceptions, definingContext);
            if (suppressedExceptions.size() > 0) {
                this.dexElementsSuppressedExceptions = (IOException[]) suppressedExceptions.toArray(new IOException[suppressedExceptions.size()]);
            } else {
                this.dexElementsSuppressedExceptions = null;
            }
        }
    }

    public String toString() {
        List<File> allNativeLibraryDirectories = new ArrayList(this.nativeLibraryDirectories);
        allNativeLibraryDirectories.addAll(this.systemNativeLibraryDirectories);
        return "DexPathList[" + Arrays.toString(this.dexElements) + ",nativeLibraryDirectories=" + Arrays.toString((File[]) allNativeLibraryDirectories.toArray(new File[allNativeLibraryDirectories.size()])) + "]";
    }

    public List<File> getNativeLibraryDirectories() {
        return this.nativeLibraryDirectories;
    }

    public void addDexPath(String dexPath, File optimizedDirectory) {
        List<IOException> suppressedExceptionList = new ArrayList();
        Element[] newElements = makeDexElements(splitDexPath(dexPath), optimizedDirectory, suppressedExceptionList, this.definingContext);
        if (newElements != null && newElements.length > 0) {
            Element[] oldElements = this.dexElements;
            this.dexElements = new Element[(oldElements.length + newElements.length)];
            System.arraycopy(oldElements, 0, this.dexElements, 0, oldElements.length);
            System.arraycopy(newElements, 0, this.dexElements, oldElements.length, newElements.length);
        }
        if (suppressedExceptionList.size() > 0) {
            IOException[] newSuppressedExceptions = (IOException[]) suppressedExceptionList.toArray(new IOException[suppressedExceptionList.size()]);
            if (this.dexElementsSuppressedExceptions != null) {
                IOException[] oldSuppressedExceptions = this.dexElementsSuppressedExceptions;
                this.dexElementsSuppressedExceptions = new IOException[(oldSuppressedExceptions.length + newSuppressedExceptions.length)];
                System.arraycopy(oldSuppressedExceptions, 0, this.dexElementsSuppressedExceptions, 0, oldSuppressedExceptions.length);
                System.arraycopy(newSuppressedExceptions, 0, this.dexElementsSuppressedExceptions, oldSuppressedExceptions.length, newSuppressedExceptions.length);
                return;
            }
            this.dexElementsSuppressedExceptions = newSuppressedExceptions;
        }
    }

    private static List<File> splitDexPath(String path) {
        return splitPaths(path, false);
    }

    private static List<File> splitPaths(String searchPath, boolean directoriesOnly) {
        List<File> result = new ArrayList();
        if (searchPath != null) {
            for (String path : searchPath.split(File.pathSeparator)) {
                if (directoriesOnly) {
                    try {
                        if (!OsConstants.S_ISDIR(Libcore.os.stat(path).st_mode)) {
                        }
                    } catch (ErrnoException e) {
                    }
                }
                result.add(new File(path));
            }
        }
        return result;
    }

    private static Element[] makeDexElements(List<File> files, File optimizedDirectory, List<IOException> suppressedExceptions, ClassLoader loader) {
        return makeElements(files, optimizedDirectory, suppressedExceptions, false, loader);
    }

    private static Element[] makePathElements(List<File> files, List<IOException> suppressedExceptions, ClassLoader loader) {
        return makeElements(files, null, suppressedExceptions, true, loader);
    }

    private static Element[] makePathElements(List<File> files, File optimizedDirectory, List<IOException> suppressedExceptions) {
        return makeElements(files, optimizedDirectory, suppressedExceptions, false, null);
    }

    private static Element[] makeElements(List<File> files, File optimizedDirectory, List<IOException> suppressedExceptions, boolean ignoreDexFiles, ClassLoader loader) {
        Element[] elements = new Element[files.size()];
        int elementsPos = 0;
        for (File file : files) {
            int elementsPos2;
            File zip = null;
            File dir = new File(XmlPullParser.NO_NAMESPACE);
            DexFile dexFile = null;
            String path = file.getPath();
            String name = file.getName();
            if (path.contains(zipSeparator)) {
                String[] split = path.split(zipSeparator, 2);
                zip = new File(split[0]);
                dir = new File(split[1]);
                elementsPos2 = elementsPos;
            } else if (file.isDirectory()) {
                elementsPos2 = elementsPos + 1;
                elements[elementsPos] = new Element(file, true, null, null);
            } else if (file.isFile()) {
                if (ignoreDexFiles || !name.endsWith(DEX_SUFFIX)) {
                    zip = file;
                    if (!ignoreDexFiles) {
                        try {
                            dexFile = loadDexFile(file, optimizedDirectory, loader, elements);
                        } catch (IOException suppressed) {
                            suppressedExceptions.add(suppressed);
                            elementsPos2 = elementsPos;
                        }
                    }
                } else {
                    try {
                        dexFile = loadDexFile(file, optimizedDirectory, loader, elements);
                    } catch (IOException suppressed2) {
                        System.logE("Unable to load dex file: " + file, suppressed2);
                        suppressedExceptions.add(suppressed2);
                        elementsPos2 = elementsPos;
                    }
                }
                elementsPos2 = elementsPos;
            } else {
                System.logW("ClassLoader referenced unknown path: " + file);
                elementsPos2 = elementsPos;
            }
            if (zip == null && dexFile == null) {
                elementsPos = elementsPos2;
            } else {
                elementsPos = elementsPos2 + 1;
                elements[elementsPos2] = new Element(dir, false, zip, dexFile);
            }
        }
        if (elementsPos != elements.length) {
            return (Element[]) Arrays.copyOf(elements, elementsPos);
        }
        return elements;
    }

    private static DexFile loadDexFile(File file, File optimizedDirectory, ClassLoader loader, Element[] elements) throws IOException {
        if (optimizedDirectory == null) {
            return new DexFile(file, loader, elements);
        }
        return DexFile.loadDex(file.getPath(), optimizedPathFor(file, optimizedDirectory), 0, loader, elements);
    }

    private static String optimizedPathFor(File path, File optimizedDirectory) {
        String fileName = path.getName();
        if (!fileName.endsWith(DEX_SUFFIX)) {
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot < 0) {
                fileName = fileName + DEX_SUFFIX;
            } else {
                StringBuilder sb = new StringBuilder(lastDot + 4);
                sb.append(fileName, 0, lastDot);
                sb.append(DEX_SUFFIX);
                fileName = sb.toString();
            }
        }
        return new File(optimizedDirectory, fileName).getPath();
    }

    public Class findClass(String name, List<Throwable> suppressed) {
        for (Element element : this.dexElements) {
            DexFile dex = element.dexFile;
            if (dex != null) {
                Class clazz = dex.loadClassBinaryName(name, this.definingContext, suppressed);
                if (clazz != null) {
                    return clazz;
                }
            }
        }
        if (this.dexElementsSuppressedExceptions != null) {
            suppressed.addAll(Arrays.asList(this.dexElementsSuppressedExceptions));
        }
        return null;
    }

    public URL findResource(String name) {
        for (Element element : this.dexElements) {
            URL url = element.findResource(name);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public Enumeration<URL> findResources(String name) {
        ArrayList<URL> result = new ArrayList();
        for (Element element : this.dexElements) {
            URL url = element.findResource(name);
            if (url != null) {
                result.add(url);
            }
        }
        return Collections.enumeration(result);
    }

    public String findLibrary(String libraryName) {
        String fileName = System.mapLibraryName(libraryName);
        for (Element element : this.nativeLibraryPathElements) {
            String path = element.findNativeLibrary(fileName);
            if (path != null) {
                return path;
            }
        }
        return null;
    }
}
