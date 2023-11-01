package integration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.math.*


class RealFunction1(private val f: (Double) -> Double): (Double) -> Double {
    override operator fun invoke(value: Double): Double = f(value)

    operator fun unaryPlus(): RealFunction1 = this
    operator fun unaryMinus(): RealFunction1 = RealFunction1 { -this(it) }

    operator fun plus(other: (Double) -> Double): RealFunction1 = RealFunction1 { this(it) + other(it) }
    operator fun minus(other: (Double) -> Double): RealFunction1 = RealFunction1 { this(it) - other(it) }
    operator fun times(other: (Double) -> Double): RealFunction1 = RealFunction1 { this(it) * other(it) }
    operator fun div(other: (Double) -> Double): RealFunction1 = RealFunction1 { this(it) / other(it) }
    fun pow(other: (Double) -> Double): RealFunction1 = RealFunction1 { this(it).pow(other(it)) }
    fun log(other: (Double) -> Double): RealFunction1 = RealFunction1 { kotlin.math.log(this(it), other(it)) }

    fun sqrt(): RealFunction1 = andThen { sqrt(it) }
    fun sin(): RealFunction1 = andThen { sin(it) }
    fun cos(): RealFunction1 = andThen { cos(it) }
    fun tan(): RealFunction1 = andThen { tan(it) }

    infix fun andThen(other: (Double) -> Double): RealFunction1 = RealFunction1 { other(this(it)) }

    operator fun plus(value: Number): RealFunction1 = this + value.constant
    operator fun minus(value: Number): RealFunction1 = this - value.constant
    operator fun times(value: Number): RealFunction1 = this * value.constant
    operator fun div(value: Number): RealFunction1 = this / value.constant
    fun pow(value: Number): RealFunction1 = pow(value.constant)
    fun log(value: Number): RealFunction1 = log(value.constant)
}

val Number.constant: RealFunction1
    get() = RealFunction1 { this.toDouble() }
operator fun Number.plus(f: RealFunction1): RealFunction1 = this.constant + f
operator fun Number.minus(f: RealFunction1): RealFunction1 = this.constant - f
operator fun Number.times(f: RealFunction1): RealFunction1 = this.constant * f
operator fun Number.div(f: RealFunction1): RealFunction1 = this.constant / f

val X = RealFunction1 { x -> x }


data class TestSample(val f: (Double) -> Double, val a: Double, val b: Double, val res: Double)

val eps: Double = 0.01

class Tests {
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
