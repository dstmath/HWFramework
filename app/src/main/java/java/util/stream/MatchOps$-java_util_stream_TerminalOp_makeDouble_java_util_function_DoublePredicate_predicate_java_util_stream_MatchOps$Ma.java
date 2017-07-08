package java.util.stream;

import java.util.function.DoublePredicate;
import java.util.function.Supplier;
import java.util.stream.MatchOps.AnonymousClass4MatchSink;

final /* synthetic */ class MatchOps$-java_util_stream_TerminalOp_makeDouble_java_util_function_DoublePredicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0 implements Supplier {
    private /* synthetic */ MatchKind val$matchKind;
    private /* synthetic */ DoublePredicate val$predicate;

    public /* synthetic */ MatchOps$-java_util_stream_TerminalOp_makeDouble_java_util_function_DoublePredicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0(MatchKind matchKind, DoublePredicate doublePredicate) {
        this.val$matchKind = matchKind;
        this.val$predicate = doublePredicate;
    }

    public Object get() {
        return new AnonymousClass4MatchSink(this.val$matchKind, this.val$predicate);
    }
}
