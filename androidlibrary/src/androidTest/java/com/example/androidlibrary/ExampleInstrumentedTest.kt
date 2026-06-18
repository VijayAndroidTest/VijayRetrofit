package com.example.androidlibrary

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // This is the package of the test APK (the ".test" one)
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

        // This is the package of your actual library/app code
        val instrumentationContext = InstrumentationRegistry.getInstrumentation().context

        assertEquals("com.example.androidlibrary", instrumentationContext.packageName)
    }
}