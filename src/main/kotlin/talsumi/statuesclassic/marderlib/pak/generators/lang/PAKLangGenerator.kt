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

package talsumi.statuesclassic.marderlib.pak.generators.lang

import com.google.gson.JsonObject
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry
import talsumi.statuesclassic.marderlib.easyparametermapping.EzPMBlock
import talsumi.statuesclassic.marderlib.pak.PAKGenerator
import talsumi.statuesclassic.marderlib.util.FileUtil
import java.io.File
import java.nio.file.Files

//TODO: PAK Language file generator removes ':' and trailing characters from values.
class PAKLangGenerator(val namespace: String): PAKGenerator {

	val actualNamespace = namespace.substringBefore('|')
	val langFileName = namespace.substringAfter('|')
	val mappings = HashMap<String, String>()
	val mappingsList = ArrayList<Pair<String, String>>()
	val newMappings = ArrayList<Pair<String, String>>()
	var lineAt = 0

	override fun generateFiles(file: EzPMBlock, outputFolder: File, overwrite: Boolean): Int
	{
		val langFile = FileUtil.createPathToFile(outputFolder, "lang", file = "$langFileName.json")
		val stream = Files.lines(langFile.toPath())
		val iterator = stream.iterator()

		while (true) {
			val line = formatLine(nextLine(iterator) ?: break)

			if (line.first == "PAKGenerated")
				break;

			mappings[line.first] = line.second
			mappingsList.add(line)
		}

		stream.close()

		for (entry in file.getParameterMap())
			if (entry.key != "autogenerate")
				parseEntry(entry.key, entry.value)

		if (file.getParameterAsBoolean("autogenerate") == true) {
			Registry.ITEM.forEach { parseItem(it) }
			Registry.BLOCK.forEach { parseBlock(it) }
		}

		val json = JsonObject()
		for (mapping in mappingsList)
			json.addProperty(mapping.first, mapping.second)
		json.addProperty("PAKGenerated", "PAKGenerated")
		for (mapping in newMappings)
			json.addProperty(mapping.first, mapping.second)

		if (!langFile.exists() || overwrite)
			writeJsonToFile(langFile.toPath(), json)

		return 0
	}

	private fun parseBlock(block: Block)
	{
		val id = block.registryEntry.registryKey().value

		if (id.namespace == actualNamespace) {
			val entry = "block.${id.namespace}.${id.path}"
			val translation = formatId(id.path)

			if (!mappings.containsKey(entry)) {
				newMappings.add(Pair(entry, translation))
				mappings[entry] = translation
			}
		}
	}

	private fun parseItem(item: Item)
	{
		val id = item.registryEntry.registryKey().value

		if (item !is BlockItem && id.namespace == actualNamespace) {
			val entry = "item.${id.namespace}.${id.path}"
			val translation = formatId(id.path)

			if (!mappings.containsKey(entry)) {
				newMappings.add(Pair(entry, translation))
				mappings[entry] = translation
			}
		}
	}

	private fun parseEntry(key: String, value: String)
	{
		if (!mappings.containsKey(key)) {
			newMappings.add(Pair(key, value))
			mappings[key] = value
		}
	}

	private fun nextLine(iterator: Iterator<String>): String?
	{
		while (iterator.hasNext()) {
			var str = iterator.next().trim()
			lineAt++
			if (str.isNotEmpty() && !str.startsWith('{') && !str.endsWith('}'))
				return str.trim()
		}

		return null
	}

	private fun formatId(id: String): String
	{
		var name = ""
		val words = id.split('_')
		for (word in words.withIndex())
			name+=word.value.replaceFirstChar { it.uppercase() } + if (word.index >= words.size-1) "" else " "
		return name
	}

	private fun formatLine(line: String): Pair<String, String>
	{
		val args = line.replace("\"", "").replace(",", "").split(':')
		return Pair(args[0].trim(), args[1].trim())
	}
}