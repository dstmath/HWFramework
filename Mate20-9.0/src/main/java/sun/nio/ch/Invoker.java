package sun.nio.ch;

import java.nio.channels.AsynchronousChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ShutdownChannelGroupException;
import java.security.AccessController;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import sun.security.action.GetIntegerAction;

class Invoker {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int maxHandlerInvokeCount = ((Integer) AccessController.doPrivileged(new GetIntegerAction("sun.nio.ch.maxCompletionHandlersOnStack", 16))).intValue();
    /* access modifiers changed from: private */
    public static final ThreadLocal<GroupAndInvokeCount> myGroupAndInvokeCount = new ThreadLocal<GroupAndInvokeCount>() {
        /* access modifiers changed from: protected */
        public GroupAndInvokeCount initialValue() {
            return null;
        }
    };

    static class GroupAndInvokeCount {
        /* access modifiers changed from: private */
        public final AsynchronousChannelGroupImpl group;
        private int handlerInvokeCount;

        GroupAndInvokeCount(AsynchronousChannelGroupImpl group2) {
            this.group = group2;
        }

        /* access modifiers changed from: package-private */
        public AsynchronousChannelGroupImpl group() {
            return this.group;
        }

        /* access modifiers changed from: package-private */
        public int invokeCount() {
            return this.handlerInvokeCount;
        }

        /* access modifiers changed from: package-private */
        public void setInvokeCount(int value) {
            this.handlerInvokeCount = value;
        }

        /* access modifiers changed from: package-private */
        public void resetInvokeCount() {
            this.handlerInvokeCount = 0;
        }

        /* access modifiers changed from: package-private */
        public void incrementInvokeCount() {
            this.handlerInvokeCount++;
        }
    }

    private Invoker() {
    }

    static void bindToGroup(AsynchronousChannelGroupImpl group) {
        myGroupAndInvokeCount.set(new GroupAndInvokeCount(group));
    }

    static GroupAndInvokeCount getGroupAndInvokeCount() {
        return myGroupAndInvokeCount.get();
    }

    static boolean isBoundToAnyGroup() {
        return myGroupAndInvokeCount.get() != null;
    }

    static boolean mayInvokeDirect(GroupAndInvokeCount myGroupAndInvokeCount2, AsynchronousChannelGroupImpl group) {
        if (myGroupAndInvokeCount2 == null || myGroupAndInvokeCount2.group() != group || myGroupAndInvokeCount2.invokeCount() >= maxHandlerInvokeCount) {
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

    static <V, A> void invokeDirect(GroupAndInvokeCount myGroupAndInvokeCount2, CompletionHandler<V, ? super A> handler, A attachment, V result, Throwable exc) {
        myGroupAndInvokeCount2.incrementInvokeCount();
        invokeUnchecked(handler, attachment, result, exc);
    }

    static <V, A> void invoke(AsynchronousChannel channel, CompletionHandler<V, ? super A> handler, A attachment, V result, Throwable exc) {
        boolean invokeDirect = false;
        boolean identityOkay = false;
        GroupAndInvokeCount thisGroupAndInvokeCount = myGroupAndInvokeCount.get();
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
            invokeIndirectly(channel, handler, attachment, result, exc);
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
                    Invoker.invokeUnchecked(CompletionHandler.this, attachment, result, exc);
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
                    Invoker.invokeUnchecked(CompletionHandler.this, attachment, value, exc);
                }
            });
        } catch (RejectedExecutionException e) {
            throw new ShutdownChannelGroupException();
        }
    }

    static void invokeOnThreadInThreadPool(Groupable channel, Runnable task) {
        boolean invokeDirect;
        GroupAndInvokeCount thisGroupAndInvokeCount = myGroupAndInvokeCount.get();
        AsynchronousChannelGroupImpl targetGroup = channel.group();
        if (thisGroupAndInvokeCount == null) {
            invokeDirect = false;
        } else {
            invokeDirect = thisGroupAndInvokeCount.group == targetGroup;
        }
        if (invokeDirect) {
            try {
                task.run();
            } catch (RejectedExecutionException e) {
                throw new ShutdownChannelGroupException();
            }
        } else {
            targetGroup.executeOnPooledThread(task);
        }
    }

    static <V, A> void invokeUnchecked(PendingFuture<V, A> future) {
        CompletionHandler<V, ? super A> handler = future.handler();
        if (handler != null) {
            invokeUnchecked(handler, future.attachment(), future.value(), future.exception());
        }
    }

    static <V, A> void invoke(PendingFuture<V, A> future) {
        CompletionHandler<V, ? super A> handler = future.handler();
        if (handler != null) {
            invoke(future.channel(), handler, future.attachment(), future.value(), future.exception());
        }
    }

    static <V, A> void invokeIndirectly(PendingFuture<V, A> future) {
        CompletionHandler<V, ? super A> handler = future.handler();
        if (handler != null) {
            invokeIndirectly(future.channel(), handler, future.attachment(), future.value(), future.exception());
        }
    }
}
