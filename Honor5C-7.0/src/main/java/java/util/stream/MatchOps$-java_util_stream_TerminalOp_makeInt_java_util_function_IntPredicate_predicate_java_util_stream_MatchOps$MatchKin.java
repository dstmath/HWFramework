package java.util.stream;

import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.MatchOps.AnonymousClass2MatchSink;

final /* synthetic */ class MatchOps$-java_util_stream_TerminalOp_makeInt_java_util_function_IntPredicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0 implements Supplier {
    private /* synthetic */ MatchKind val$matchKind;
    private /* synthetic */ IntPredicate val$predicate;

    public /* synthetic */ MatchOps$-java_util_stream_TerminalOp_makeInt_java_util_function_IntPredicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0(MatchKind matchKind, IntPredicate intPredicate) {
        this.val$matchKind = matchKind;
        this.val$predicate = intPredicate;
    }

    public Object get() {
        return new AnonymousClass2MatchSink(this.val$matchKind, this.val$predicate);
    }
}
