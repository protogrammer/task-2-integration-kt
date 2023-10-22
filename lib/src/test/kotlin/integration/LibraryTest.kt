package integration

import kotlin.test.Test
import kotlin.test.assertEquals


data class TestSample(val f: (Double) -> Double, val a: Double, val b: Double, val res: Double)

val eps: Double = 0.01

class LibraryTest {

    @Test fun exampleIntegrals() =
        listOf(
            TestSample(2*X + 1, 0.0, 5.0, 30.0),
            TestSample(X.sin(), 0.0, 10.0, 1.8391),
            TestSample(2.constant.pow(X), -5.0, 5.0, 46.121),
            TestSample(X.pow(3).cos(), 0.0, 5.0, 0.765074),
            TestSample(X.pow(7).log(2), 0.5, 1.5, -0.456759),
            TestSample((X.pow(2) - 7*X + 10) / X.sqrt(), 1.0, 10.0, 26.43),
        ).forEach { (f, a, b, res) ->
            assertEquals(LeftRectangleMethod(f, a, b, 100000), res, eps)
            assertEquals(RightRectangleMethod(f, a, b, 100000), res, eps)
            assertEquals(MidpointRectangleMethod(f, a, b, 1000), res, eps)
            assertEquals(TrapezoidMethod(f, a, b, 1000), res, eps)
            assertEquals(SimpsonMethod(f, a, b, 100), res, eps)
        }
}
