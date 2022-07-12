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
import com.google.gson.JsonObject
import net.minecraft.util.Identifier
import talsumi.statuesclassic.marderlib.util.FileUtil
import talsumi.statuesclassic.marderlib.util.MarderLibConstants
import java.io.File

internal class BlockStateEntry(private val blockId: Identifier, private val rotType: RotType?, private val hasItem: Boolean, val defaultModel: BlockTextureMapping?) {

	private val variants = HashMap<String, BlockTextureMapping>()
	private var masterVariant: String? = null

	/**
	 * Adds a variant to the entry. It is assumed this entry is created via [BlockTextureMapping.createFrom] called on [defaultModel], if it exists.
	 */
	fun addVariant(variantName: String, variant: BlockTextureMapping)
	{
		variants[variantName] = variant
	}

	/**
	 * Sets the model [name] as the 'master' variant, used when generating item models.
	 */
	fun setMasterVariant(name: String)
	{
		masterVariant = name
	}

	fun toJson(): List<Pair<File, () -> JsonElement>>
	{
		val generators = mutableListOf<Pair<File, () -> JsonElement>>()
		val hasCustomItem = variants.containsKey("item")

		//Block Models
		if (variants.size == 0 || (variants.size == 1 && hasCustomItem)) {
			if (defaultModel == null)
				throw RuntimeException("Default model is missing, and there are no registered variants! BlockId: $blockId")

			generators.add(Pair(
				getDefaultModelFile())
			{ genVariantJson(defaultModel) })

			if (hasCustomItem)
				generators.add(Pair(
					getVariantModelFile("item"))
				{ genVariantJson(variants["item"]!!) })
		}
		else {
			for (variant in variants)
				generators.add(Pair(
					getVariantModelFile(variant.key))
				{ genVariantJson(variant.value) })
		}

		//Item Model
		if (hasItem) {
			generators.add(Pair(
				FileUtil.createPathToFile(MarderLibConstants.ITEM_MODEL_FOLDER, file = blockId.path + ".json"))
			{ genItemJson() })
		}

		//BlockState
		generators.add(Pair(
			FileUtil.createPathToFile(MarderLibConstants.BLOCKSTATES_FOLDER, file = blockId.path + ".json"))
		{ genBlockStateJson() })

		return generators
	}

	private fun formatVariantName(name: String): String = name.replace('=', '_').replace(',', '_')

	/**
	 * Returns the path to [variant], usable inside a BlockState json.
	 */
	private fun getModelPath(variant: String): String = "${blockId.namespace}:block/${blockId.path}/${formatVariantName(variant)}"

	/**
	 * Returns the path to the default model, usable inside a BlockState json.
	 */
	private fun getDefaultModelPath(): String = "${blockId.namespace}:block/${blockId.path}"

	private fun getMasterVariantPath(): String? = masterVariant?.let { getModelPath(it) }

	/**
	 * Gets the filesystem path to a blockstate.
	 */
	private fun getBlockStateFile(): File = FileUtil.createPathToFile(MarderLibConstants.BLOCKSTATES_FOLDER, file = blockId.path + ".json")

	/**
	 * Gets the filesystem path to a default model.
	 */
	private fun getDefaultModelFile(): File = FileUtil.createPathToFile(MarderLibConstants.BLOCK_MODEL_FOLDER, file = blockId.path + ".json")

	/**
	 * Gets the filesystem path to a variant.
	 */
	private fun getVariantModelFile(name: String): File = FileUtil.createPathToFile(MarderLibConstants.BLOCK_MODEL_FOLDER, blockId.path, file = formatVariantName(name) + ".json")

	private fun genVariantJson(mapping: BlockTextureMapping): JsonElement
	{
		val base = JsonObject()

		base.addProperty("parent", "block/cube")

		val texObj = JsonObject()
		texObj.addProperty("particle", mapping.particleTexture)
		for (face in Face.values())
			texObj.addProperty(face.cardinalName, mapping.textures[face.ordinal])
		base.add("textures", texObj)

		return base
	}

	private fun genItemJson(): JsonElement
	{
		val modelFile = if (variants.containsKey("item")) getModelPath("item") else if (variants.size > 0) getMasterVariantPath() else getDefaultModelPath()
		val base = JsonObject()

		base.addProperty("parent", modelFile)

		return base
	}

	private fun genBlockStateJson(): JsonElement
	{
		val base = JsonObject()
		val variantsJson = JsonObject()
		base.add("variants", variantsJson)

		if (rotType != null) {
			//With rotation
			for (type in rotType.properties.withIndex()) {
				val property = type.value
				val entries = rotType.rotation[type.index]

				if (variants.size > 0) {
					//With variants
					for (varName in variants.keys) {
						val variantObj = JsonObject()
						variantObj.addProperty("model", getModelPath(varName))
						for (entry in entries)
							variantObj.addProperty(entry.first, entry.second)

						variantsJson.add("$property,$varName", variantObj)
					}
				}
				else {
					//Without variants
					val variantObj = JsonObject()
					variantObj.addProperty("model", getDefaultModelPath())
					for (entry in entries)
						variantObj.addProperty(entry.first, entry.second)
					variantsJson.add(property, variantObj)
				}
			}
		}
		else {
			//Without rotation
			if (variants.size > 0) {
				//With variants
				for (varName in variants.keys) {
					val variantObj = JsonObject()
					variantObj.addProperty("model", getModelPath(varName))
					variantsJson.add(varName, variantObj)
				}
			}
			else {
				//Without variants
				val jsonObj = JsonObject()
				jsonObj.addProperty("model", getDefaultModelPath())
				variantsJson.add("", jsonObj)
			}
		}

		return base
	}
}