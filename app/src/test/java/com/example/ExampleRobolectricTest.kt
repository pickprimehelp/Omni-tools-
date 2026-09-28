package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.tools.calculator.evalMath
import com.example.util.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OmniTool", appName)
    }

    @Test
    fun `evalMath evaluates basic and complex arithmetic correctly`() {
        assertEquals(4.0, evalMath("2+2"), 0.001)
        assertEquals(14.0, evalMath("2+3*4"), 0.001)
        assertEquals(20.0, evalMath("(2+3)*4"), 0.001)
        assertEquals(8.0, evalMath("2^3"), 0.001)
    }

    @Test
    fun `qr code generator creates non null bitmap`() {
        val bitmap = QrCodeGenerator.generateQrBitmap("https://example.com", size = 256)
        assertNotNull(bitmap)
        assertEquals(256, bitmap.width)
        assertEquals(256, bitmap.height)
    }
}
