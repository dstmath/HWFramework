package java.nio.channels;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

public interface AsynchronousByteChannel extends AsynchronousChannel {
    Future<Integer> read(ByteBuffer byteBuffer);

    <A> void read(ByteBuffer byteBuffer, A a, CompletionHandler<Integer, ? super A> completionHandler);

    Future<Integer> write(ByteBuffer byteBuffer);

    <A> void write(ByteBuffer byteBuffer, A a, CompletionHandler<Integer, ? super A> completionHandler);
}
