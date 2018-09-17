package java.util.stream;

import java.util.function.LongPredicate;
import java.util.function.Supplier;
import java.util.stream.MatchOps.AnonymousClass3MatchSink;

final /* synthetic */ class MatchOps$-java_util_stream_TerminalOp_makeLong_java_util_function_LongPredicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0 implements Supplier {
    private /* synthetic */ MatchKind val$matchKind;
    private /* synthetic */ LongPredicate val$predicate;

    public /* synthetic */ MatchOps$-java_util_stream_TerminalOp_makeLong_java_util_function_LongPredicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0(MatchKind matchKind, LongPredicate longPredicate) {
        this.val$matchKind = matchKind;
        this.val$predicate = longPredicate;
    }

    public Object get() {
        return new AnonymousClass3MatchSink(this.val$matchKind, this.val$predicate);
    }
}
