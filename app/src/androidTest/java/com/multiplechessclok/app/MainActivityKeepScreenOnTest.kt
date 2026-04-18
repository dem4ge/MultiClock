package com.multiplechessclok.app

import android.view.WindowManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityKeepScreenOnTest {

    @Test
    fun onCreate_setsKeepScreenOnWindowFlag() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val hasFlag =
                    activity.window.attributes.flags and
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0
                assertTrue(
                    "Expected FLAG_KEEP_SCREEN_ON on the activity window",
                    hasFlag,
                )
            }
        }
    }
}
