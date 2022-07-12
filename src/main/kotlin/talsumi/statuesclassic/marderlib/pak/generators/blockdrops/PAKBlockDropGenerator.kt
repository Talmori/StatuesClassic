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

package talsumi.statuesclassic.marderlib.pak.generators.blockdrops

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.block.Block
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import talsumi.statuesclassic.marderlib.easyparametermapping.EzPMBlock
import talsumi.statuesclassic.marderlib.pak.PAKGenerator
import talsumi.statuesclassic.marderlib.util.FileUtil
import talsumi.statuesclassic.marderlib.util.MarderLibConstants
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists

class PAKBlockDropGenerator(val namespace: String): PAKGenerator {

	private val generators = HashMap<File, () -> JsonElement>()
	private var count = 0

	override fun generateFiles(file: EzPMBlock, outputFolder: File, overwrite: Boolean): Int
	{
		if (file.getParameterAsBoolean("autogenerate") == true)
			Registry.BLOCK.forEach { autoGenerate(it) }
		for (entry in file.getParameterMap())
			if (entry.key != "autogenerate")
				parseLine(entry.key, entry.value)
		for (entry in file.getChildrenMap())
			parseBlock(entry.key, entry.value)

		for (entry in generators) {
			val location = Path.of(outputFolder.toString(), entry.key.toString())
			if (!location.exists() || overwrite) {
				writeJsonToFile(location, entry.value.invoke())
				count++
			}
		}

		return count
	}

	private fun autoGenerate(block: Block)
	{
		val id = block.registryEntry.registryKey().value

		if (id.namespace == namespace) {
			val item = block.asItem()
			if (item != null)
				parseLine(id.toString(), item.registryEntry.registryKey().value.toString())
		}
	}

	private fun parseBlock(key: String, value: EzPMBlock)
	{
		val item = Identifier(fillNamespace(value.getParameter("item") ?: throw RuntimeException("Missing item for $key!")))
		val count = value.getParameterAsInt("count") ?: 1
		val file = getLootFile(Identifier(fillNamespace(key)))

		generators[file] = { makeFile(item, count) }
	}

	private fun parseLine(key: String, value: String)
	{
		val file = getLootFile(Identifier(fillNamespace(key)))

		generators[file] = { makeFile(Identifier(fillNamespace(value)), 1) }
	}

	private fun makeFile(drop: Identifier, dropCount: Int, silkDrop: Identifier? = null, silkDropCount: Int = 0): JsonElement
	{
		val base = JsonObject()
		base.addProperty("type", "minecraft:block")
		val pools = JsonArray()
		val pool = JsonObject()

		pool.addProperty("rolls", 1)
		val entries = JsonArray()
		val entry = makeEntry(drop, dropCount, silkDrop, silkDropCount)

		entries.add(entry)
		pool.add("entries", entries)
		if (dropCount == 1) {
			val array = JsonArray()
			val obj = JsonObject()
			obj.addProperty("condition","minecraft:survives_explosion")
			array.add(obj)

			pool.add("conditions", array)
		}

		pools.add(pool)
		base.add("pools", pools)
		return base
	}

	private fun makeEntry(drop: Identifier, dropCount: Int, silkDrop: Identifier?, silkDropCount: Int): JsonObject
	{
		val simple = silkDrop == null
		val entry = JsonObject()
		entry.addProperty("type", if (simple) "minecraft:item" else "minecraft:alternatives")
		if (simple) {
			entry.addProperty("name", drop.toString())
			if (dropCount > 1)
				entry.add("functions", makeCountFunctionJson(dropCount, true))
		}
		else {
			if (dropCount > 1)
				entry.add("functions", makeCountFunctionJson(dropCount, false))
			//TODO Silk touch for block drops PAK
		}

		return entry
	}

	private fun makeCountFunctionJson(count: Int, add: Boolean): JsonArray
	{
		val functions = JsonArray()
		val countJson = JsonObject()
		val decayJson = JsonObject()

		countJson.addProperty("function", "minecraft:set_count")
		countJson.addProperty("count", count)
		if (!add)
			countJson.addProperty("add", false)
		decayJson.addProperty("function", "minecraft:explosion_decay")

		functions.add(countJson)
		functions.add(decayJson)

		return functions
	}

	private fun getLootFile(blockId: Identifier): File = FileUtil.createPathToFile(MarderLibConstants.BLOCK_LOOT_TABLE_FOLDER, file = blockId.path + ".json")

	private fun fillNamespace(str: String): String = if (str.indexOf(':') > -1) str else "$namespace:$str"
}