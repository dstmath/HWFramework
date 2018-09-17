package com.android.nfc_extras.tests;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.test.InstrumentationTestCase;
import com.android.nfc_extras.NfcAdapterExtras;
import com.android.nfc_extras.NfcAdapterExtras.CardEmulationRoute;
import com.android.nfc_extras.NfcExecutionEnvironment;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.TestCase;

public class BasicNfcEeTest extends InstrumentationTestCase {
    public static final byte[] SELECT_CARD_MANAGER_COMMAND = new byte[]{(byte) 0, (byte) -92, (byte) 4, (byte) 0, (byte) 8, (byte) -96, (byte) 0, (byte) 0, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    public static final byte[] SELECT_CARD_MANAGER_RESPONSE = new byte[]{(byte) -112, (byte) 0};
    private NfcAdapterExtras mAdapterExtras;
    private Context mContext;
    private NfcExecutionEnvironment mEe;

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
            TestCase.assertTrue(out.length >= SELECT_CARD_MANAGER_RESPONSE.length);
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
        TestCase.assertEquals(2, newRoute.route);
        TestCase.assertEquals((Object) this.mEe, (Object) newRoute.nfcEe);
    }

    public void testDisableEe() {
        this.mAdapterExtras.setCardEmulationRoute(new CardEmulationRoute(1, null));
        CardEmulationRoute newRoute = this.mAdapterExtras.getCardEmulationRoute();
        TestCase.assertEquals(1, newRoute.route);
        TestCase.assertNull(newRoute.nfcEe);
    }

    private static void assertByteArrayEquals(byte[] b1, byte[] b2) {
        TestCase.assertEquals(b1.length, b2.length);
        for (int i = 0; i < b1.length; i++) {
            TestCase.assertEquals(b1[i], b2[i]);
        }
    }
}
