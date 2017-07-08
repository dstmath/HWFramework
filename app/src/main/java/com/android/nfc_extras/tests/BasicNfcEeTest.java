package com.android.nfc_extras.tests;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.test.InstrumentationTestCase;
import com.android.nfc_extras.NfcAdapterExtras;
import com.android.nfc_extras.NfcAdapterExtras.CardEmulationRoute;
import com.android.nfc_extras.NfcExecutionEnvironment;
import java.io.IOException;
import java.util.Arrays;

public class BasicNfcEeTest extends InstrumentationTestCase {
    public static final byte[] SELECT_CARD_MANAGER_COMMAND = null;
    public static final byte[] SELECT_CARD_MANAGER_RESPONSE = null;
    private NfcAdapterExtras mAdapterExtras;
    private Context mContext;
    private NfcExecutionEnvironment mEe;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.nfc_extras.tests.BasicNfcEeTest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.nfc_extras.tests.BasicNfcEeTest.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.nfc_extras.tests.BasicNfcEeTest.<clinit>():void");
    }

    protected void setUp() throws Exception {
        super.setUp();
        this.mContext = getInstrumentation().getTargetContext();
        this.mAdapterExtras = NfcAdapterExtras.get(NfcAdapter.getDefaultAdapter(this.mContext));
        this.mEe = this.mAdapterExtras.getEmbeddedExecutionEnvironment();
    }

    public void testSendCardManagerApdu() throws IOException {
        this.mEe.open();
        try {
            byte[] out = this.mEe.transceive(SELECT_CARD_MANAGER_COMMAND);
            assertTrue(out.length >= SELECT_CARD_MANAGER_RESPONSE.length);
            assertByteArrayEquals(SELECT_CARD_MANAGER_RESPONSE, Arrays.copyOfRange(out, out.length - SELECT_CARD_MANAGER_RESPONSE.length, out.length));
        } finally {
            this.mEe.close();
        }
    }

    public void testSendCardManagerApduMultiple() throws IOException {
        for (int i = 0; i < 10; i++) {
            this.mEe.open();
            try {
                byte[] out = this.mEe.transceive(SELECT_CARD_MANAGER_COMMAND);
                byte[] trailing = Arrays.copyOfRange(out, out.length - SELECT_CARD_MANAGER_RESPONSE.length, out.length);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                this.mEe.close();
            } catch (IOException e2) {
            } catch (Throwable th) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e3) {
                }
                this.mEe.close();
            }
        }
        testSendCardManagerApdu();
    }

    public void testEnableEe() {
        this.mAdapterExtras.setCardEmulationRoute(new CardEmulationRoute(2, this.mEe));
        CardEmulationRoute newRoute = this.mAdapterExtras.getCardEmulationRoute();
        assertEquals(2, newRoute.route);
        assertEquals(this.mEe, newRoute.nfcEe);
    }

    public void testDisableEe() {
        this.mAdapterExtras.setCardEmulationRoute(new CardEmulationRoute(1, null));
        CardEmulationRoute newRoute = this.mAdapterExtras.getCardEmulationRoute();
        assertEquals(1, newRoute.route);
        assertNull(newRoute.nfcEe);
    }

    private static void assertByteArrayEquals(byte[] b1, byte[] b2) {
        assertEquals(b1.length, b2.length);
        for (int i = 0; i < b1.length; i++) {
            assertEquals(b1[i], b2[i]);
        }
    }
}
