/*
 * MIT License
 *
 *  Copyright (c) 2022 Talsumi
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 *
 */

package talsumi.statuesclassic.marderlib.data

import net.minecraft.util.math.Vec2f
import kotlin.math.sqrt

/**
 * Mojang's [Vec2f] with doubles.
 */
class Vec2d(val x: Double, val y: Double) {

    companion object {
        val ZERO = Vec2d(0.0, 0.0)
    }

    fun mul(value: Float): Vec2d = Vec2d(x * value, y * value)

    fun add(x: Double, y: Double): Vec2d = Vec2d(this.x + x, this.y + y)
    fun add(vec: Vec2d): Vec2d = Vec2d(this.x + vec.x, this.y + vec.y)

    fun subtract(x: Double, y: Double): Vec2d = Vec2d(this.x - x, this.y - y)
    fun subtract(vec: Vec2d): Vec2d = Vec2d(this.x - vec.x, this.y - vec.y)

    fun normalize(): Vec2d
    {
        val f = sqrt(x * x + y * y)
        return if (f < 1.0E-4f) ZERO else Vec2d((x / f), (y / f))
    }

    fun length(): Double = sqrt(x * x + y * y)

    fun lengthSquared(): Double = (x * x + y * y)

    fun distanceSquared(vec: Vec2d): Double
    {
        val f = vec.x - x
        val g = vec.y - y
        return f * f + g * g
    }

    fun negate(): Vec2d = Vec2d(-x, -y)

    fun dotProduct(vec: Vec2d): Double = x * vec.x + y * vec.y

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vec2d

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun toString(): String = "Vec2d(x=$x, y=$y)"
}