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
 * Enum for each face in a BlockTextureMapping.
 */
internal enum class Face(val cardinalName: String, val sideName: String) {

	DOWN("down", "bottom"),
	UP("up", "top"),
	NORTH("north", "front"),
	SOUTH("south", "back"),
	EAST("east", "left"),
	WEST("west", "right");

	companion object {
		private val map = HashMap<String, Face>()

		init {
			for (face in Face.values()) {
				map[face.cardinalName] = face
				map[face.sideName] = face
			}
		}

		fun getFace(name: String): Face = map[name.lowercase()] ?: throw RuntimeException("Invalid face $name!")

		fun isFace(name: String): Boolean = map.containsKey(name.lowercase())
	}
}