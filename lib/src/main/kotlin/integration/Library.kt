package integration


internal typealias CalculateFunction = (a: Double, b: Double, values: List<Double>) -> Double

abstract class IntegrationMethod: ((Double) -> Double, Double, Double, Int) -> Double {

    override operator fun invoke(f: (Double) -> Double, a: Double, b: Double, n: Int): Double {
        assert(n > 0)

        var l = a
        var delta = (b - l) / n
        val params = offsets.map { f(l + delta * it) }.toTypedArray()
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

    private val swapIndices: MutableList<Pair<Int, Int>> = arrayListOf()
    private val evalIndices: MutableList<Pair<Int, Double>> = arrayListOf()

    protected abstract val offsets: List<Double>
    protected abstract val calculate: CalculateFunction

    init {
        // constructing evalIndices and swapIndices
        // complexity: O(offsets.size ** 2)
        // can be achieved O(offsets.size) complexity, but it does not matter for this particular case

        offsets.forEachIndexed { index, offset ->
            when (val targetIndex = offsets.indexOf(offset + 1)) {
                -1   -> evalIndices.add(Pair(index, offset))
                else -> swapIndices.add(Pair(index, targetIndex))
            }
        }

        // topological sorting swapIndices
        swapIndices.sortBy { offsets[it.first] }
    }
}

abstract class RectangleMethod: IntegrationMethod() {
    protected abstract val offset: Double
    override val offsets = listOf(offset)
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
    override val offsets = listOf(0.0, 1.0)
    override val calculate: CalculateFunction = { a, b, (fa, fb) ->
        (b - a) * (fa + fb) / 2
    }
}

object SimpsonMethod: IntegrationMethod() {
    override val offsets = listOf(0.0, 0.5, 1.0)
    override val calculate: CalculateFunction = { a, b, (fa, fm, fb) ->
        ((b - a) / 6) * (fa + 4*fm + fb)
    }
}
