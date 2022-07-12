/*
 * MIT License
 *
 * Copyright (c) 2022 Talsumi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package talsumi.statuesclassic.marderlib.pak.generators.blockmodels

/**
 * Rotation types, with the parameters that must be encoded into a blockstate json.
 */
internal enum class RotType(val properties: Array<String>, val rotation: Array<Array<Pair<String, Int>>>) {
	NONE(arrayOf(), arrayOf()),
	COMPASS(
		arrayOf("facing=north", "facing=east", "facing=south", "facing=west"),
		arrayOf(arrayOf(), arrayOf(Pair("y", 90)), arrayOf(Pair("y", 180)), arrayOf(Pair("y", 270)))),
	AXIS(
		arrayOf("axis=x", "axis=y", "axis=z"),
		arrayOf(arrayOf(Pair("x", 90), Pair("y", 90)), arrayOf(), arrayOf(Pair("x", 90)))),
	ALL(
		arrayOf("facing=north", "facing=east", "facing=south", "facing=west", "facing=up", "facing=down"),
		arrayOf(arrayOf(Pair("x", 90)), arrayOf(Pair("x", 90), Pair("y", 90)), arrayOf(Pair("x", 90), Pair("y", 180)), arrayOf(Pair("x", 90), Pair("y", 270)), arrayOf(), arrayOf(Pair("x", 180))));

	companion object {
		private val map = HashMap<String, RotType>()

		init {
			for (type in RotType.values())
				map[type.name.lowercase()] = type
		}

		fun getFace(name: String): RotType = map[name.lowercase()] ?: throw RuntimeException("Invalid rotation type $name!")
	}
}