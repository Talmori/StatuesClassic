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

package talsumi.statuesclassic.marderlib.pak.generators.tags

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import talsumi.statuesclassic.marderlib.easyparametermapping.EzPMBlock
import talsumi.statuesclassic.marderlib.pak.PAKGenerator
import talsumi.statuesclassic.marderlib.util.ArrayUtil
import talsumi.statuesclassic.marderlib.util.MarderLibConstants
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class PAKTagGenerator(val namespace: String): PAKGenerator {

	private val generators = mutableListOf<Pair<File, () -> JsonElement>>()
	var lineAt = 0
	var count = 0

	override fun generateFiles(file: EzPMBlock, outputFolder: File, overwrite: Boolean): Int
	{
		for (entry in file.getChildrenMap()) {
			val namespaceIn = entry.key
			readTagsForNamespace(namespaceIn, entry.value, outputFolder)
		}

		for (entry in generators) {
			val location = Path.of(outputFolder.toString(), entry.first.toString())
			if (!location.exists() || overwrite) {
				writeJsonToFile(location, entry.second.invoke())
				count++
			}
		}

		return count
	}

	private fun readTagsForNamespace(namespaceIn: String, tags: EzPMBlock, outputFolder: File)
	{
		for (entry in tags.getChildrenMap()) {
			val tagList = readInTagFile(namespaceIn, entry.key, outputFolder)
			val map = ArrayUtil.hashMapFromArray(tagList.toTypedArray())

			for (tagValue in entry.value.getParameterMap()) {
				val tag = fillNamespace(tagValue.key)
				if (map.contains(tag))
					tagList[map[tag]!!] = tag
				else
					tagList.add(tag)
			}

			generators.add(Pair(
				getTagFile(namespaceIn, entry.key))
			{ genJson(tagList) })
		}
	}

	private fun genJson(tags: List<String>): JsonElement
	{
		val json = JsonObject()
		json.addProperty("replace", false)
		val list = JsonArray()
		for (tag in tags)
			list.add(tag)
		json.add("values", list)
		return json
	}

	private fun readInTagFile(namespaceIn: String, tag: String, outputFolder: File): ArrayList<String>
	{
		val list = ArrayList<String>()
		val file = File(outputFolder, getTagFile(namespaceIn, tag).toString())

		if (!file.exists())
			return list

		val stream = Files.lines(file.toPath())
		val iterator = stream.iterator()

		//Find the start of listings in the file.
		while (true)
			if (iterator.next()?.trim()?.endsWith('[') == true)
				break

		while (true) {
			list.add(formatLine(nextLine(iterator) ?: break))
		}

		stream.close()

		return list
	}

	private fun getTagFile(namespaceIn: String, tag: String): File = File(File(namespaceIn, MarderLibConstants.TAG_FOLDER.toString()), tag + ".json")


	private fun nextLine(iterator: Iterator<String>): String?
	{
		while (iterator.hasNext()) {
			var str = iterator.next().trim()
			lineAt++
			if (str.isNotEmpty())
				if (str.endsWith(']'))
					return null
				else
					return str.trim()
		}

		return null
	}

	private fun formatLine(line: String): String = line.replace("\"", "").replace(",", "")

	private fun fillNamespace(str: String): String = if (str.indexOf(':') > -1) str else "$namespace:$str"
}