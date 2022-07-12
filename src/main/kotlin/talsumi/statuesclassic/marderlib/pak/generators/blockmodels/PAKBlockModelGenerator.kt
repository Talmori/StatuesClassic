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

import com.google.gson.JsonElement
import net.minecraft.util.Identifier
import talsumi.statuesclassic.marderlib.easyparametermapping.EzPMBlock
import talsumi.statuesclassic.marderlib.pak.PAKGenerator
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists

//TODO: Rewrite to handle custom model parents. Need a registry of all types of models, and Map<Face, Texture> for texture mapping. Also have compat for handling unregistered models.
class PAKBlockModelGenerator(val namespace: String) : PAKGenerator {

	private val NO_ITEM_ARG = "no_item"
	private val TYPE_ARG = "type"
	private val ALLOW_MISSING_ARG = "allow_missing"
	private val fileGenerators = mutableListOf<Pair<File, () -> JsonElement>>()
	private var count = 0

	override fun generateFiles(file: EzPMBlock, outputFolder: File, overwrite: Boolean): Int
	{
		for (entry in file.getChildrenMap())
			parseFileBlock(entry.key, entry.value)
		for (entry in file.getParameterMap())
			parseOneLiner(entry.key, entry.value)

		for (entry in fileGenerators) {
			val location = Path.of(outputFolder.toString(), entry.first.toString())
			if (!location.exists() || overwrite) {
				writeJsonToFile(location, entry.second.invoke())
				count++
			}
		}

		return count
	}

	private fun parseFileBlock(blockId: String, entry: EzPMBlock)
	{
		val hasItem = !entry.hasParameter(NO_ITEM_ARG)
		val rotType = entry.getParameter(TYPE_ARG)?.let { RotType.getFace(it)}
		val blockId = fillNamespace(blockId)
		val simple = entry.blockCount() == 0
		val blockState: BlockStateEntry
		if (simple) {
			val mapping = makeTextureMapping(null, entry)
			blockState = BlockStateEntry(Identifier(blockId), rotType, hasItem, mapping)
		}
		else {
			//TODO: Rewrite variant loading to know which block is set as the the default.
			//Format should be 'default|active=true"
			var defaultModel: EzPMBlock? = null
			var masterVariant: String? = null
			val variants = mutableListOf<Pair<String, EzPMBlock>>()

			for (block in entry.getChildrenMap().entries) {
				val args = block.key.split('|')
				for (arg in args.withIndex()) {
					if (arg.index == 0 && arg.value == "default") {
						defaultModel = block.value
						if (args.size > 1)
							masterVariant = args[arg.index + 1]
					}
					variants.add(Pair(arg.value, block.value))
				}
			}

			//blockState = BlockStateEntry(Identifier(blockId), rotType, hasItem, if (entry.hasBlock("default")) makeTextureMapping(null, entry.getBlock("default")!!) else null)
			blockState = BlockStateEntry(Identifier(blockId), rotType, hasItem, if (defaultModel != null) makeTextureMapping(null, defaultModel) else null)

			for (variant in variants) {
				if (variant.first != "default")
					blockState.addVariant(variant.first, makeTextureMapping(blockState.defaultModel, variant.second))
			}

			if (masterVariant != null)
				blockState.setMasterVariant(masterVariant)
		}

		for (file in blockState.toJson())
			fileGenerators.add(file)
	}

	//Haha
	private fun parseOneLiner(blockId: String, texture: String)
	{
		val texture = fillNamespace(texture)
		val mapping = BlockTextureMapping(texture, texture, arrayOfNulls(6), false)
		val blockState = BlockStateEntry(Identifier(fillNamespace(blockId)), null, true, mapping)

		for (file in blockState.toJson())
			fileGenerators.add(file)
	}

	private fun makeTextureMapping(default: BlockTextureMapping?, entry: EzPMBlock): BlockTextureMapping
	{
		val allowMissing = entry.hasParameter(ALLOW_MISSING_ARG)
		val textures = arrayOfNulls<String>(6)
		for (mapping in entry.getParameterMap())
			if (Face.isFace(mapping.key))
				textures[Face.getFace(mapping.key).ordinal] = fillNamespace(mapping.value)
		val defaultTexture = entry.getParameter("default")?.let { fillNamespace(it) }
		val particleTexture = entry.getParameter("particle")?.let { fillNamespace(it) }

		return default?.createFrom(defaultTexture, particleTexture, textures) ?: BlockTextureMapping(defaultTexture, particleTexture, textures, allowMissing)
	}

	private fun fillNamespace(str: String): String = if (str.indexOf(':') > -1) str else "$namespace:$str"
}