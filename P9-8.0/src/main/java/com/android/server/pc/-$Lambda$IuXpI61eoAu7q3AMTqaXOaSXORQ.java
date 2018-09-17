package com.android.server.pc;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

final /* synthetic */ class -$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ implements OnCheckedChangeListener {

    /* renamed from: com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ$2 */
    final /* synthetic */ class AnonymousClass2 implements OnClickListener {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(DialogInterface arg0, int arg1) {
            ((HwPCManagerService) this.-$f0).lambda$-com_android_server_pc_HwPCManagerService_24532(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.-$f0 = obj;
        }

        public final void onClick(DialogInterface dialogInterface, int i) {
            $m$0(dialogInterface, i);
        }
    }

    /* renamed from: com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ$3 */
    final /* synthetic */ class AnonymousClass3 implements OnClickListener {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(DialogInterface arg0, int arg1) {
            ((HwPCManagerService) this.-$f0).lambda$-com_android_server_pc_HwPCManagerService_31257(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.-$f0 = obj;
        }

        public final void onClick(DialogInterface dialogInterface, int i) {
            $m$0(dialogInterface, i);
        }
    }

    /* renamed from: com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ$4 */
    final /* synthetic */ class AnonymousClass4 implements OnDismissListener {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(DialogInterface arg0) {
            ((HwPCManagerService) this.-$f0).lambda$-com_android_server_pc_HwPCManagerService_24823(arg0);
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.-$f0 = obj;
        }

        public final void onDismiss(DialogInterface dialogInterface) {
            $m$0(dialogInterface);
        }
    }

    /* renamed from: com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ$5 */
    final /* synthetic */ class AnonymousClass5 implements OnDismissListener {
        private final /* synthetic */ Object -$f0;

        private final /* synthetic */ void $m$0(DialogInterface arg0) {
            ((HwPCManagerService) this.-$f0).lambda$-com_android_server_pc_HwPCManagerService_31547(arg0);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.-$f0 = obj;
        }

        public final void onDismiss(DialogInterface dialogInterface) {
            $m$0(dialogInterface);
        }
    }

    /* renamed from: com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ$6 */
    final /* synthetic */ class AnonymousClass6 implements OnClickListener {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(DialogInterface arg0, int arg1) {
            ((HwPCManagerService) this.-$f1).lambda$-com_android_server_pc_HwPCManagerService_23174((CheckBox) this.-$f2, this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(boolean z, Object obj, Object obj2) {
            this.-$f0 = z;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void onClick(DialogInterface dialogInterface, int i) {
            $m$0(dialogInterface, i);
        }
    }

    /* renamed from: com.android.server.pc.-$Lambda$IuXpI61eoAu7q3AMTqaXOaSXORQ$7 */
    final /* synthetic */ class AnonymousClass7 implements OnClickListener {
        private final /* synthetic */ boolean -$f0;
        private final /* synthetic */ Object -$f1;
        private final /* synthetic */ Object -$f2;

        private final /* synthetic */ void $m$0(DialogInterface arg0, int arg1) {
            ((HwPCManagerService) this.-$f1).lambda$-com_android_server_pc_HwPCManagerService_29972((CheckBox) this.-$f2, this.-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(boolean z, Object obj, Object obj2) {
            this.-$f0 = z;
            this.-$f1 = obj;
            this.-$f2 = obj2;
        }

        public final void onClick(DialogInterface dialogInterface, int i) {
            $m$0(dialogInterface, i);
        }
    }

    public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        $m$0(compoundButton, z);
    }
}
