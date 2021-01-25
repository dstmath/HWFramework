package ohos.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import ohos.utils.FileWatcher;

public class FileWatcher {
    private static WatchEvent.Kind<?>[] DEFAULT_KINDS = {StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY};
    private Map<Path, EventHandler> handlers = null;
    private ExecutorService threadPool = null;
    private Map<WatchKey, Path> watchKeys = null;
    private final WatchService watcher;

    public interface EventHandler {
        void onEvent(WatchEvent<?> watchEvent);
    }

    public FileWatcher() {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            this.handlers = new ConcurrentHashMap();
            this.watchKeys = new ConcurrentHashMap();
            this.threadPool = Executors.newFixedThreadPool(1);
            start();
        } catch (IOException unused) {
            throw new RuntimeException("Get watch service fail.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void register(Path path, EventHandler eventHandler, WatchEvent.Kind<?>... kindArr) throws IOException {
        WatchKey register = path.register(this.watcher, kindArr);
        this.handlers.put(path, eventHandler);
        this.watchKeys.put(register, path);
    }

    private void regitsterAll(final Path path, final EventHandler eventHandler, final WatchEvent.Kind<?>... kindArr) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            /* class ohos.utils.FileWatcher.AnonymousClass1 */

            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                FileWatcher.this.register(path.resolve(path), eventHandler, kindArr);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void addWatch(Path path, EventHandler eventHandler, boolean z, WatchEvent.Kind<?>... kindArr) {
        Objects.requireNonNull(path, "Path is null.");
        Objects.requireNonNull(eventHandler, "Event handler is null.");
        if (isEmpty(kindArr)) {
            kindArr = DEFAULT_KINDS;
        }
        if (z) {
            try {
                regitsterAll(path, eventHandler, kindArr);
            } catch (IOException e) {
                throw new RuntimeException("Add watch fail.", e);
            }
        } else {
            register(path, eventHandler, kindArr);
        }
    }

    private static <T> boolean isEmpty(T[] tArr) {
        return tArr == null || tArr.length == 0;
    }

    private void start() {
        this.threadPool.submit(new Runnable() {
            /* class ohos.utils.$$Lambda$FileWatcher$LcFx1VAcaPJem0yCOuyBERf_O0 */

            @Override // java.lang.Runnable
            public final void run() {
                FileWatcher.this.lambda$start$1$FileWatcher();
            }
        });
    }

    public /* synthetic */ void lambda$start$1$FileWatcher() {
        while (true) {
            try {
                WatchKey take = this.watcher.take();
                if (this.watchKeys.containsKey(take) && this.handlers.containsKey(this.watchKeys.get(take))) {
                    take.pollEvents().forEach(new Consumer() {
                        /* class ohos.utils.$$Lambda$FileWatcher$4doxlK4Qq7kQT5VeGizQhmSVbG0 */

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            FileWatcher.lambda$start$0(FileWatcher.EventHandler.this, (WatchEvent) obj);
                        }
                    });
                    take.reset();
                }
            } catch (InterruptedException unused) {
                return;
            }
        }
    }
}
