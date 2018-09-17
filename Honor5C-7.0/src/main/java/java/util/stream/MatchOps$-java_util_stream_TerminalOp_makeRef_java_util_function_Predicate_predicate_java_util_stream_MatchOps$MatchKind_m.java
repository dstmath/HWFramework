package java.util.stream;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.MatchOps.AnonymousClass1MatchSink;

final /* synthetic */ class MatchOps$-java_util_stream_TerminalOp_makeRef_java_util_function_Predicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0 implements Supplier {
    private /* synthetic */ MatchKind val$matchKind;
    private /* synthetic */ Predicate val$predicate;

    public /* synthetic */ MatchOps$-java_util_stream_TerminalOp_makeRef_java_util_function_Predicate_predicate_java_util_stream_MatchOps$MatchKind_matchKind_LambdaImpl0(MatchKind matchKind, Predicate predicate) {
        this.val$matchKind = matchKind;
        this.val$predicate = predicate;
    }

    public Object get() {
        return new AnonymousClass1MatchSink(this.val$matchKind, this.val$predicate);
    }
}
