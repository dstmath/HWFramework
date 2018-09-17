package java.util.concurrent;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface CompletionStage<T> {
    CompletionStage<Void> acceptEither(CompletionStage<? extends T> completionStage, Consumer<? super T> consumer);

    CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> completionStage, Consumer<? super T> consumer);

    CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> completionStage, Consumer<? super T> consumer, Executor executor);

    <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> completionStage, Function<? super T, U> function);

    <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> completionStage, Function<? super T, U> function);

    <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> completionStage, Function<? super T, U> function, Executor executor);

    CompletionStage<T> exceptionally(Function<Throwable, ? extends T> function);

    <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> biFunction);

    <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> biFunction);

    <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> biFunction, Executor executor);

    CompletionStage<Void> runAfterBoth(CompletionStage<?> completionStage, Runnable runnable);

    CompletionStage<Void> runAfterBothAsync(CompletionStage<?> completionStage, Runnable runnable);

    CompletionStage<Void> runAfterBothAsync(CompletionStage<?> completionStage, Runnable runnable, Executor executor);

    CompletionStage<Void> runAfterEither(CompletionStage<?> completionStage, Runnable runnable);

    CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> completionStage, Runnable runnable);

    CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> completionStage, Runnable runnable, Executor executor);

    CompletionStage<Void> thenAccept(Consumer<? super T> consumer);

    CompletionStage<Void> thenAcceptAsync(Consumer<? super T> consumer);

    CompletionStage<Void> thenAcceptAsync(Consumer<? super T> consumer, Executor executor);

    <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> completionStage, BiConsumer<? super T, ? super U> biConsumer);

    <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> completionStage, BiConsumer<? super T, ? super U> biConsumer);

    <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> completionStage, BiConsumer<? super T, ? super U> biConsumer, Executor executor);

    <U> CompletionStage<U> thenApply(Function<? super T, ? extends U> function);

    <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> function);

    <U> CompletionStage<U> thenApplyAsync(Function<? super T, ? extends U> function, Executor executor);

    <U, V> CompletionStage<V> thenCombine(CompletionStage<? extends U> completionStage, BiFunction<? super T, ? super U, ? extends V> biFunction);

    <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> completionStage, BiFunction<? super T, ? super U, ? extends V> biFunction);

    <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> completionStage, BiFunction<? super T, ? super U, ? extends V> biFunction, Executor executor);

    <U> CompletionStage<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> function);

    <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> function);

    <U> CompletionStage<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> function, Executor executor);

    CompletionStage<Void> thenRun(Runnable runnable);

    CompletionStage<Void> thenRunAsync(Runnable runnable);

    CompletionStage<Void> thenRunAsync(Runnable runnable, Executor executor);

    CompletableFuture<T> toCompletableFuture();

    CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> biConsumer);

    CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> biConsumer);

    CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> biConsumer, Executor executor);
}
