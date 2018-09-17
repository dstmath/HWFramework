package sun.nio.ch;

import java.nio.channels.AsynchronousChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ShutdownChannelGroupException;
import java.security.AccessController;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import sun.security.action.GetIntegerAction;

class Invoker {
    static final /* synthetic */ boolean -assertionsDisabled = (Invoker.class.desiredAssertionStatus() ^ 1);
    private static final int maxHandlerInvokeCount = ((Integer) AccessController.doPrivileged(new GetIntegerAction("sun.nio.ch.maxCompletionHandlersOnStack", 16))).lambda$-java_util_stream_IntPipeline_14709();
    private static final ThreadLocal<GroupAndInvokeCount> myGroupAndInvokeCount = new ThreadLocal<GroupAndInvokeCount>() {
        protected GroupAndInvokeCount initialValue() {
            return null;
        }
    };

    static class GroupAndInvokeCount {
        private final AsynchronousChannelGroupImpl group;
        private int handlerInvokeCount;

        GroupAndInvokeCount(AsynchronousChannelGroupImpl group) {
            this.group = group;
        }

        AsynchronousChannelGroupImpl group() {
            return this.group;
        }

        int invokeCount() {
            return this.handlerInvokeCount;
        }

        void setInvokeCount(int value) {
            this.handlerInvokeCount = value;
        }

        void resetInvokeCount() {
            this.handlerInvokeCount = 0;
        }

        void incrementInvokeCount() {
            this.handlerInvokeCount++;
        }
    }

    private Invoker() {
    }

    static void bindToGroup(AsynchronousChannelGroupImpl group) {
        myGroupAndInvokeCount.set(new GroupAndInvokeCount(group));
    }

    static GroupAndInvokeCount getGroupAndInvokeCount() {
        return (GroupAndInvokeCount) myGroupAndInvokeCount.get();
    }

    static boolean isBoundToAnyGroup() {
        return myGroupAndInvokeCount.get() != null;
    }

    static boolean mayInvokeDirect(GroupAndInvokeCount myGroupAndInvokeCount, AsynchronousChannelGroupImpl group) {
        if (myGroupAndInvokeCount == null || myGroupAndInvokeCount.group() != group || myGroupAndInvokeCount.invokeCount() >= maxHandlerInvokeCount) {
            return false;
        }
        return true;
    }

    static <V, A> void invokeUnchecked(CompletionHandler<V, ? super A> handler, A attachment, V value, Throwable exc) {
        if (exc == null) {
            handler.completed(value, attachment);
        } else {
            handler.failed(exc, attachment);
        }
        Thread.interrupted();
    }

    static <V, A> void invokeDirect(GroupAndInvokeCount myGroupAndInvokeCount, CompletionHandler<V, ? super A> handler, A attachment, V result, Throwable exc) {
        myGroupAndInvokeCount.incrementInvokeCount();
        invokeUnchecked(handler, attachment, result, exc);
    }

    static <V, A> void invoke(AsynchronousChannel channel, CompletionHandler<V, ? super A> handler, A attachment, V result, Throwable exc) {
        boolean invokeDirect = false;
        boolean identityOkay = false;
        GroupAndInvokeCount thisGroupAndInvokeCount = (GroupAndInvokeCount) myGroupAndInvokeCount.get();
        if (thisGroupAndInvokeCount != null) {
            if (thisGroupAndInvokeCount.group() == ((Groupable) channel).group()) {
                identityOkay = true;
            }
            if (identityOkay && thisGroupAndInvokeCount.invokeCount() < maxHandlerInvokeCount) {
                invokeDirect = true;
            }
        }
        if (invokeDirect) {
            invokeDirect(thisGroupAndInvokeCount, handler, attachment, result, exc);
            return;
        }
        try {
            invokeIndirectly(channel, (CompletionHandler) handler, (Object) attachment, (Object) result, exc);
        } catch (RejectedExecutionException e) {
            if (identityOkay) {
                invokeDirect(thisGroupAndInvokeCount, handler, attachment, result, exc);
                return;
            }
            throw new ShutdownChannelGroupException();
        }
    }

    static <V, A> void invokeIndirectly(AsynchronousChannel channel, final CompletionHandler<V, ? super A> handler, final A attachment, final V result, final Throwable exc) {
        try {
            ((Groupable) channel).group().executeOnPooledThread(new Runnable() {
                public void run() {
                    GroupAndInvokeCount thisGroupAndInvokeCount = (GroupAndInvokeCount) Invoker.myGroupAndInvokeCount.get();
                    if (thisGroupAndInvokeCount != null) {
                        thisGroupAndInvokeCount.setInvokeCount(1);
                    }
                    Invoker.invokeUnchecked(handler, attachment, result, exc);
                }
            });
        } catch (RejectedExecutionException e) {
            throw new ShutdownChannelGroupException();
        }
    }

    static <V, A> void invokeIndirectly(final CompletionHandler<V, ? super A> handler, final A attachment, final V value, final Throwable exc, Executor executor) {
        try {
            executor.execute(new Runnable() {
                public void run() {
                    Invoker.invokeUnchecked(handler, attachment, value, exc);
                }
            });
        } catch (RejectedExecutionException e) {
            throw new ShutdownChannelGroupException();
        }
    }

    static void invokeOnThreadInThreadPool(Groupable channel, Runnable task) {
        GroupAndInvokeCount thisGroupAndInvokeCount = (GroupAndInvokeCount) myGroupAndInvokeCount.get();
        AsynchronousChannelGroupImpl targetGroup = channel.group();
        boolean invokeDirect = thisGroupAndInvokeCount == null ? false : thisGroupAndInvokeCount.group == targetGroup;
        if (invokeDirect) {
            try {
                task.run();
                return;
            } catch (RejectedExecutionException e) {
                throw new ShutdownChannelGroupException();
            }
        }
        targetGroup.executeOnPooledThread(task);
    }

    static <V, A> void invokeUnchecked(PendingFuture<V, A> future) {
        if (-assertionsDisabled || future.isDone()) {
            CompletionHandler<V, ? super A> handler = future.handler();
            if (handler != null) {
                invokeUnchecked(handler, future.attachment(), future.value(), future.exception());
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    static <V, A> void invoke(PendingFuture<V, A> future) {
        if (-assertionsDisabled || future.isDone()) {
            CompletionHandler<V, ? super A> handler = future.handler();
            if (handler != null) {
                invoke(future.channel(), handler, future.attachment(), future.value(), future.exception());
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    static <V, A> void invokeIndirectly(PendingFuture<V, A> future) {
        if (-assertionsDisabled || future.isDone()) {
            CompletionHandler handler = future.handler();
            if (handler != null) {
                invokeIndirectly(future.channel(), handler, future.attachment(), future.value(), future.exception());
                return;
            }
            return;
        }
        throw new AssertionError();
    }
}
