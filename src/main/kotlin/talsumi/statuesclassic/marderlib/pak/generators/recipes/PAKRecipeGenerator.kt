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

package talsumi.statuesclassic.marderlib.pak.generators.recipes

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import talsumi.statuesclassic.marderlib.easyparametermapping.EzPMBlock
import talsumi.statuesclassic.marderlib.pak.PAKGenerator
import talsumi.statuesclassic.marderlib.util.FileUtil
import talsumi.statuesclassic.marderlib.util.MarderLibConstants
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists

class PAKRecipeGenerator(val namespace: String): PAKGenerator {

	private val generators = mutableListOf<Pair<File, () -> JsonElement>>()
	private var count = 0

	override fun generateFiles(file: EzPMBlock, outputFolder: File, overwrite: Boolean): Int
	{
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

	private fun parseBlock(key: String, block: EzPMBlock)
	{
		val type = block.getParameter("type")
		val subfolders = block.getParameter("subfolder")?.split('/')?.toTypedArray() ?: arrayOf()

		generators.add(Pair(
			getRecipeFile(key, *subfolders))
		{ when (type) {
			"workbench:shaped" -> prepareShapedJson(key, block)
			"workbench:shapeless" -> prepareShapelessJson(key, block)
			else -> throw IllegalArgumentException("Invalid recipe type '$type'")
		} })
	}

	private fun prepareShapelessJson(key: String, block: EzPMBlock): JsonElement
	{
		val output = fillNamespace(block.getParameter("output") ?: throw IllegalArgumentException("Recipe missing parameter 'output'"))
		val inputs = Stack<Pair<String, String>>()

		//Read inputs
		if (block.hasBlock("input")) {
			val patBlock = block.getBlock("input")!!
			for (entry in patBlock.getParameterList()) {
				val type = if (entry.key.startsWith("tag.")) "tag" else "item"
				val value = fillNamespace(entry.key.replaceFirst("tag.", ""))
				inputs.add(Pair(type, value))
			}
		}
		else if (block.hasParameter("input")) {
			val patParameter = block.getParameter("input")!!
			val args = patParameter.split(' ')
			for (arg in args) {
				val type = if (arg.startsWith("tag.")) "tag" else "item"
				val value = fillNamespace(arg.replaceFirst("tag.", ""))
				inputs.add(Pair(type, value))
			}
		}
		else {
			throw IllegalArgumentException("No inputs specified for recipe $key")
		}

		//Read count
		val count = if (block.hasParameter("count"))
			block.getParameterAsInt("count") ?: throw IllegalArgumentException("Output count for recipe $key is not an integer!")
		else
			1

		if (count <= 0 || count > 64)
			throw IllegalArgumentException(if (count <= 0) "Output count for recipe $key is below 1!" else "Output count for recipe $key is above 64!")


		return genShapelessJson(output, inputs.toTypedArray(), count)
	}

	private fun genShapelessJson(output: String, inputs: Array<Pair<String, String>>, count: Int): JsonElement
	{
		val base = JsonObject()

		base.addProperty("type", "minecraft:crafting_shapeless")

		val inputArray = JsonArray()
		for (input in inputs) {
			val subObject = JsonObject()
			subObject.addProperty(input.first, input.second)
			inputArray.add(subObject)
		}
		base.add("ingredients", inputArray)

		val resultObject = JsonObject()
		resultObject.addProperty("item", output)
		if (count > 1)
			resultObject.addProperty("count", count)
		base.add("result", resultObject)

		return base
	}

	private fun prepareShapedJson(key: String, block: EzPMBlock): JsonElement
	{
		val output = fillNamespace(block.getParameter("output") ?: throw IllegalArgumentException("Recipe missing parameter 'output'"))
		val pattern = Stack<String>()
		val map = Stack<Triple<Char, String, String>>()

		//Read pattern
		if (block.hasBlock("pattern")) {
			val patBlock = block.getBlock("pattern")!!
			val args = patBlock.getParameterList()

			if (args.size > 3 || args.isEmpty())
				throw IllegalArgumentException(if (args.size > 3) "Pattern size is too large! Should be at most 3, size: ${args.size}" else "Pattern has no entries!")

			for (arg in args)
				pattern.push(arg.key.substringAfter('"').substringBefore('"'))
		}
		else if (block.hasParameter("pattern")) {
			val patParameter = block.getParameter("pattern")!!
			val args = patParameter.split(' ')

			if (args.size > 3 || args.isEmpty())
				throw IllegalArgumentException(if (args.size > 3) "Pattern size is too large! Should be at most 3, size: ${args.size}" else "Pattern has no entries!")

			for (arg in args)
				pattern.push(arg.substringAfter('"').substringBefore('"'))
		}
		else {
			throw IllegalArgumentException("No pattern specified for recipe $key")
		}

		//Read keys
		for (entry in block.getParameterList()) {
			if (entry.key.length == 1) {
				val mapType = if (entry.value.startsWith("tag.")) "tag" else "item"
				val mapValue = fillNamespace(entry.value.replaceFirst("tag.", ""))
				map.push(Triple(entry.key[0], mapType, mapValue))
			}
		}

		if (map.size == 0)
			throw IllegalArgumentException("No keys specified for recipe $key")

		//Read count
		val count = if (block.hasParameter("count"))
			block.getParameterAsInt("count") ?: throw IllegalArgumentException("Output count for recipe $key is not an integer!")
		else
			1

		if (count <= 0 || count > 64)
			throw IllegalArgumentException(if (count <= 0) "Output count for recipe $key is below 1!" else "Output count for recipe $key is above 64!")

		return genShapedJson(output, pattern.toTypedArray(), map.toTypedArray(), count)
	}

	/**
	 * output: Output item id
	 * pattern: Recipe input pattern
	 * map: Recipe inputs. Char matches pattern, First string is type ("name" or "tag"), Second string is value
	 */
	private fun genShapedJson(output: String, pattern: Array<String>, map: Array<Triple<Char, String, String>>, count: Int): JsonElement
	{
		val base = JsonObject()

		base.addProperty("type", "minecraft:crafting_shaped")

		val patternArray = JsonArray()
		for (segment in pattern)
			patternArray.add(segment)
		base.add("pattern", patternArray)

		val keyObject = JsonObject()
		for (args in map) {
			val subObject = JsonObject()
			subObject.addProperty(args.second, args.third)
			keyObject.add(args.first.toString(), subObject)
		}
		base.add("key", keyObject)

		val resultObject = JsonObject()
		resultObject.addProperty("item", output)
		if (count > 1)
			resultObject.addProperty("count", count)
		base.add("result", resultObject)

		return base
	}

	private fun getRecipeFile(id: String, vararg subfolders: String): File
	{
		return FileUtil.createPathToFile(MarderLibConstants.RECIPE_FOLDER, *subfolders, file = id + ".json")
	}

	private fun fillNamespace(str: String): String = if (str.indexOf(':') > -1) str else "$namespace:$str"
}