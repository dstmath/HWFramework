package dalvik.system;

import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import libcore.io.ClassPathURLStreamHandler;
import libcore.io.IoUtils;
import libcore.io.Libcore;

final class DexPathList {
    private static final String DEX_SUFFIX = ".dex";
    private static final String zipSeparator = "!/";
    private final ClassLoader definingContext;
    private Element[] dexElements;
    private IOException[] dexElementsSuppressedExceptions;
    private final List<File> nativeLibraryDirectories;
    private final NativeLibraryElement[] nativeLibraryPathElements;
    private final List<File> systemNativeLibraryDirectories;

    static class Element {
        private final DexFile dexFile;
        private boolean initialized;
        private final File path;
        private ClassPathURLStreamHandler urlHandler;

        public Element(DexFile dexFile, File dexZipPath) {
            this.dexFile = dexFile;
            this.path = dexZipPath;
        }

        public Element(DexFile dexFile) {
            this.dexFile = dexFile;
            this.path = null;
        }

        public Element(File path) {
            this.path = path;
            this.dexFile = null;
        }

        @Deprecated
        public Element(File dir, boolean isDirectory, File zip, DexFile dexFile) {
            System.err.println("Warning: Using deprecated Element constructor. Do not use internal APIs, this constructor will be removed in the future.");
            if (dir != null && (zip != null || dexFile != null)) {
                throw new IllegalArgumentException("Using dir and zip|dexFile no longer supported.");
            } else if (isDirectory && (zip != null || dexFile != null)) {
                throw new IllegalArgumentException("Unsupported argument combination.");
            } else if (dir != null) {
                this.path = dir;
                this.dexFile = null;
            } else {
                this.path = zip;
                this.dexFile = dexFile;
            }
        }

        private String getDexPath() {
            String str = null;
            if (this.path != null) {
                if (!this.path.isDirectory()) {
                    str = this.path.getAbsolutePath();
                }
                return str;
            } else if (this.dexFile != null) {
                return this.dexFile.getName();
            } else {
                return null;
            }
        }

        public String toString() {
            if (this.dexFile == null) {
                return (this.path.isDirectory() ? "directory \"" : "zip file \"") + this.path + "\"";
            } else if (this.path == null) {
                return "dex file \"" + this.dexFile + "\"";
            } else {
                return "zip file \"" + this.path + "\"";
            }
        }

        public synchronized void maybeInit() {
            if (!this.initialized) {
                if (this.path == null || this.path.isDirectory()) {
                    this.initialized = true;
                    return;
                }
                try {
                    this.urlHandler = new ClassPathURLStreamHandler(this.path.getPath());
                } catch (IOException ioe) {
                    System.logE("Unable to open zip file: " + this.path, ioe);
                    this.urlHandler = null;
                }
                this.initialized = true;
                return;
            }
            return;
        }

        public Class<?> findClass(String name, ClassLoader definingContext, List<Throwable> suppressed) {
            return this.dexFile != null ? this.dexFile.loadClassBinaryName(name, definingContext, suppressed) : null;
        }

        public URL findResource(String name) {
            maybeInit();
            if (this.urlHandler != null) {
                return this.urlHandler.getEntryUrlOrNull(name);
            }
            if (this.path != null && this.path.isDirectory()) {
                File resourceFile = new File(this.path, name);
                if (resourceFile.exists()) {
                    try {
                        return resourceFile.toURI().toURL();
                    } catch (MalformedURLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            return null;
        }
    }

    static class NativeLibraryElement {
        private boolean initialized;
        private final File path;
        private ClassPathURLStreamHandler urlHandler;
        private final String zipDir;

        public NativeLibraryElement(File dir) {
            this.path = dir;
            this.zipDir = null;
        }

        public NativeLibraryElement(File zip, String zipDir) {
            this.path = zip;
            this.zipDir = zipDir;
            if (zipDir == null) {
                throw new IllegalArgumentException();
            }
        }

        public String toString() {
            if (this.zipDir == null) {
                return "directory \"" + this.path + "\"";
            }
            return "zip file \"" + this.path + "\"" + (!this.zipDir.isEmpty() ? ", dir \"" + this.zipDir + "\"" : "");
        }

        public synchronized void maybeInit() {
            if (!this.initialized) {
                if (this.zipDir == null) {
                    this.initialized = true;
                    return;
                }
                try {
                    this.urlHandler = new ClassPathURLStreamHandler(this.path.getPath());
                } catch (IOException ioe) {
                    System.logE("Unable to open zip file: " + this.path, ioe);
                    this.urlHandler = null;
                }
                this.initialized = true;
                return;
            }
            return;
        }

        public String findNativeLibrary(String name) {
            maybeInit();
            if (this.zipDir == null) {
                String entryPath = new File(this.path, name).getPath();
                if (IoUtils.canOpenReadOnly(entryPath)) {
                    return entryPath;
                }
            } else if (this.urlHandler != null) {
                String entryName = this.zipDir + '/' + name;
                if (this.urlHandler.isEntryStored(entryName)) {
                    return this.path.getPath() + DexPathList.zipSeparator + entryName;
                }
            }
            return null;
        }
    }

    public DexPathList(ClassLoader definingContext, ByteBuffer[] dexFiles) {
        if (definingContext == null) {
            throw new NullPointerException("definingContext == null");
        } else if (dexFiles == null) {
            throw new NullPointerException("dexFiles == null");
        } else if (Arrays.stream(dexFiles).anyMatch(new -$Lambda$xxvwQBVHC44UYbpcpA8j0sUqLOo())) {
            throw new NullPointerException("dexFiles contains a null Buffer!");
        } else {
            this.definingContext = definingContext;
            this.nativeLibraryDirectories = Collections.emptyList();
            this.systemNativeLibraryDirectories = splitPaths(System.getProperty("java.library.path"), true);
            this.nativeLibraryPathElements = makePathElements(this.systemNativeLibraryDirectories);
            ArrayList<IOException> suppressedExceptions = new ArrayList();
            this.dexElements = makeInMemoryDexElements(dexFiles, suppressedExceptions);
            if (suppressedExceptions.size() > 0) {
                this.dexElementsSuppressedExceptions = (IOException[]) suppressedExceptions.toArray(new IOException[suppressedExceptions.size()]);
            } else {
                this.dexElementsSuppressedExceptions = null;
            }
        }
    }

    static /* synthetic */ boolean lambda$-dalvik_system_DexPathList_3307(ByteBuffer v) {
        return v == null;
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
            ArrayList<IOException> suppressedExceptions = new ArrayList();
            this.dexElements = makeDexElements(splitDexPath(dexPath), optimizedDirectory, suppressedExceptions, definingContext);
            this.nativeLibraryDirectories = splitPaths(librarySearchPath, false);
            this.systemNativeLibraryDirectories = splitPaths(System.getProperty("java.library.path"), true);
            List<File> allNativeLibraryDirectories = new ArrayList(this.nativeLibraryDirectories);
            allNativeLibraryDirectories.addAll(this.systemNativeLibraryDirectories);
            this.nativeLibraryPathElements = makePathElements(allNativeLibraryDirectories);
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

    private static Element[] makeInMemoryDexElements(ByteBuffer[] dexFiles, List<IOException> suppressedExceptions) {
        int elementPos;
        IOException suppressed;
        Element[] elements = new Element[dexFiles.length];
        int i = 0;
        int length = dexFiles.length;
        int elementPos2 = 0;
        while (i < length) {
            ByteBuffer buf = dexFiles[i];
            try {
                elementPos = elementPos2 + 1;
                try {
                    elements[elementPos2] = new Element(new DexFile(buf));
                } catch (IOException e) {
                    suppressed = e;
                }
            } catch (IOException e2) {
                suppressed = e2;
                elementPos = elementPos2;
                System.logE("Unable to load dex file: " + buf, suppressed);
                suppressedExceptions.add(suppressed);
                i++;
                elementPos2 = elementPos;
            }
            i++;
            elementPos2 = elementPos;
        }
        if (elementPos2 != elements.length) {
            return (Element[]) Arrays.copyOf(elements, elementPos2);
        }
        return elements;
    }

    private static Element[] makeDexElements(List<File> files, File optimizedDirectory, List<IOException> suppressedExceptions, ClassLoader loader) {
        IOException suppressed;
        Element[] elements = new Element[files.size()];
        int elementsPos = 0;
        for (File file : files) {
            int elementsPos2;
            DexFile dex;
            if (file.isDirectory()) {
                elementsPos2 = elementsPos + 1;
                elements[elementsPos] = new Element(file);
                elementsPos = elementsPos2;
            } else if (!file.isFile()) {
                System.logW("ClassLoader referenced unknown path: " + file);
            } else if (file.getName().endsWith(DEX_SUFFIX)) {
                try {
                    dex = loadDexFile(file, optimizedDirectory, loader, elements);
                    if (dex != null) {
                        elementsPos2 = elementsPos + 1;
                        try {
                            elements[elementsPos] = new Element(dex, null);
                            elementsPos = elementsPos2;
                        } catch (IOException e) {
                            suppressed = e;
                            elementsPos = elementsPos2;
                            System.logE("Unable to load dex file: " + file, suppressed);
                            suppressedExceptions.add(suppressed);
                        }
                    }
                } catch (IOException e2) {
                    suppressed = e2;
                }
            } else {
                dex = null;
                try {
                    dex = loadDexFile(file, optimizedDirectory, loader, elements);
                } catch (IOException suppressed2) {
                    suppressedExceptions.add(suppressed2);
                }
                if (dex == null) {
                    elementsPos2 = elementsPos + 1;
                    elements[elementsPos] = new Element(file);
                    elementsPos = elementsPos2;
                } else {
                    elementsPos2 = elementsPos + 1;
                    elements[elementsPos] = new Element(dex, file);
                    elementsPos = elementsPos2;
                }
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

    private static Element[] makePathElements(List<File> files, File optimizedDirectory, List<IOException> suppressedExceptions) {
        return makeDexElements(files, optimizedDirectory, suppressedExceptions, null);
    }

    private static NativeLibraryElement[] makePathElements(List<File> files) {
        NativeLibraryElement[] elements = new NativeLibraryElement[files.size()];
        int elementsPos = 0;
        for (File file : files) {
            String path = file.getPath();
            int elementsPos2;
            if (path.contains(zipSeparator)) {
                String[] split = path.split(zipSeparator, 2);
                elementsPos2 = elementsPos + 1;
                elements[elementsPos] = new NativeLibraryElement(new File(split[0]), split[1]);
                elementsPos = elementsPos2;
            } else if (file.isDirectory()) {
                elementsPos2 = elementsPos + 1;
                elements[elementsPos] = new NativeLibraryElement(file);
                elementsPos = elementsPos2;
            }
        }
        if (elementsPos != elements.length) {
            return (NativeLibraryElement[]) Arrays.copyOf(elements, elementsPos);
        }
        return elements;
    }

    public Class<?> findClass(String name, List<Throwable> suppressed) {
        for (Element element : this.dexElements) {
            Class<?> clazz = element.findClass(name, this.definingContext, suppressed);
            if (clazz != null) {
                return clazz;
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
        for (NativeLibraryElement element : this.nativeLibraryPathElements) {
            String path = element.findNativeLibrary(fileName);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    List<String> getDexPaths() {
        List<String> dexPaths = new ArrayList();
        for (Element e : this.dexElements) {
            String dexPath = e.getDexPath();
            if (dexPath != null) {
                dexPaths.add(dexPath);
            }
        }
        return dexPaths;
    }
}
