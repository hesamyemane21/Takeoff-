package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Construction BOQ", appName)
  }

  @Test
  fun `verify takeoff and bbs rebar formulas`() {
    // Standard rebar unit weight: 16mm -> 16^2 / 162.28 = 1.5775 kg/m
    val weight16 = com.example.model.CalculationEngine.getRebarUnitWeight(16)
    assertEquals(1.5775, weight16, 0.001)

    // Takeoff multiplication test: 2 * 10 * 5 * 2 = 200
    val row = com.example.model.TakeoffRow(
      multiplier = 2.0,
      length = 10.0,
      width = 5.0,
      heightDepth = 2.0
    )
    val vol = com.example.model.CalculationEngine.calculateTakeoffRow(row)
    assertEquals(200.0, vol, 0.01)
  }
}
