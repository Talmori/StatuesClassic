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

package talsumi.statuesclassic.marderlib.pak.generators.itemmodels

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.util.Identifier
import talsumi.statuesclassic.marderlib.easyparametermapping.EzPMBlock
import talsumi.statuesclassic.marderlib.pak.PAKGenerator
import talsumi.statuesclassic.marderlib.util.FileUtil
import talsumi.statuesclassic.marderlib.util.MarderLibConstants
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists

class PAKItemModelGenerator(val namespace: String): PAKGenerator {

	private val generators = mutableListOf<Pair<File, () -> JsonElement>>()
	private var count = 0

	override fun generateFiles(file: EzPMBlock, outputFolder: File, overwrite: Boolean): Int
	{
		for (entry in file.getParameterMap())
			parseLine(entry.key, entry.value)
		for (entry in file.getChildrenMap())
			parseBlock(entry.key, entry.value)

		for (entry in generators) {
			val location = Path.of(outputFolder.toString(), entry.first.toString())
			if (!location.exists() || overwrite) {
				writeJsonToFile(location, entry.second.invoke())
				count++
			}
		}

		return count
	}

	private fun parseLine(key: String, value: String)
	{
		val key = Identifier(fillNamespace(key))
		val textures = value.split('|').toTypedArray()

		if (textures.size > 5)
			throw RuntimeException("Too many textures (> 5) specified for item $key!")

		for (entry in textures.withIndex())
			textures[entry.index] = fillNamespace(entry.value)

		generators.add(Pair(
			getModelFile(key))
		{ genJson(textures = textures) })
	}

	private fun parseBlock(key: String, block: EzPMBlock)
	{
		val key = Identifier(fillNamespace(key))
		val parent = block.getParameter("parent") ?: "minecraft:item/generated"

		val textures = block.getParameter("textures")?.split('|')?.toTypedArray() ?: throw RuntimeException("Texture entry for item $key has no textures specified!")

		if (textures.size > 5)
			throw RuntimeException("Too many textures (> 5) specified for item $key!")

		for (entry in textures.withIndex())
			textures[entry.index] = fillNamespace(entry.value)

		generators.add(Pair(
			getModelFile(key))
		{ genJson(parent, *textures) })
	}

	private fun genJson(parent: String = "minecraft:item/generated", vararg textures: String): JsonElement
	{
		val base = JsonObject()
		base.addProperty("parent", parent)
		val texObj = JsonObject()

		for (tex in textures.withIndex())
			texObj.addProperty("layer${tex.index}", tex.value)

		base.add("textures", texObj)

		return base
	}

	private fun getModelFile(itemId: Identifier): File = FileUtil.createPathToFile(MarderLibConstants.ITEM_MODEL_FOLDER, file = itemId.path + ".json")

	private fun fillNamespace(str: String): String = if (str.indexOf(':') > -1) str else "$namespace:$str"
}