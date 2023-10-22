package integration

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


internal typealias CalculateFunction = (a: Double, b: Double, values: List<Double>) -> Double

abstract class IntegrationMethod: ((Double) -> Double, Double, Double, Int) -> Double {

    override operator fun invoke(f: (Double) -> Double, a: Double, b: Double, n: Int): Double {
        assert(n > 0)

        if (!initialized) {
            // constructing evalIndices and swapIndices
            // complexity: O(offsets.size ** 2)
            // can be achieved O(offsets.size) complexity, but it does not matter for this particular case
            
            offsets_ = offsets()

            offsets_.forEachIndexed { index, offset ->
                when (val targetIndex = offsets_.indexOf(offset + 1)) {
                    -1   -> evalIndices.add(Pair(index, offset))
                    else -> swapIndices.add(Pair(index, targetIndex))
                }
            }

            // topological sorting swapIndices
            swapIndices.sortBy { offsets_[it.first] }

            // not thread-safe
            initialized = true
        }

        var l = a
        var delta = (b - l) / n
        val params = offsets_.map { f(l + delta * it) }.toTypedArray()
        var acc = calculate(l, l + delta, params.toList())

        for (n in n - 1 downTo 1) {
            l += delta
            delta = (b - l) / n

            swapIndices.forEach { (i, j) ->
                // for not reevaluating function result
                params[i] = params[j]
            }
            evalIndices.forEach { (i, offset) ->
                params[i] = f(l + delta * offset)
            }

            acc += calculate(l, l + delta, params.toList())
        }

        return acc
    }

    private var initialized: Boolean = false
    private var offsets_: List<Double> = listOf()
    private val swapIndices: MutableList<Pair<Int, Int>> = arrayListOf()
    private val evalIndices: MutableList<Pair<Int, Double>> = arrayListOf()

    protected abstract fun offsets(): List<Double>
    protected abstract val calculate: CalculateFunction
}

abstract class RectangleMethod: IntegrationMethod() {
    protected abstract val offset: Double
    override fun offsets() = listOf(offset)
    override val calculate: CalculateFunction = { a, b, (value) ->
        (b - a) * value
    }
}

object LeftRectangleMethod: RectangleMethod() {
    override val offset = 0.0
}

object RightRectangleMethod: RectangleMethod() {
    override val offset = 1.0
}

object MidpointRectangleMethod: RectangleMethod() {
    override val offset = 0.5
}

object TrapezoidMethod: IntegrationMethod() {
    override fun offsets() = listOf(0.0, 1.0)
    override val calculate: CalculateFunction = { a, b, (fa, fb) ->
        (b - a) * (fa + fb) / 2
    }
}

object SimpsonMethod: IntegrationMethod() {
    override fun offsets() = listOf(0.0, 0.5, 1.0)
    override val calculate: CalculateFunction = { a, b, (fa, fm, fb) ->
        ((b - a) / 6) * (fa + 4*fm + fb)
    }
}
