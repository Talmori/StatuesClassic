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

package talsumi.statuesclassic.marderlib.pak

import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import talsumi.statuesclassic.marderlib.easyparametermapping.EzPMReader
import talsumi.statuesclassic.marderlib.pak.generators.blockdrops.PAKBlockDropGenerator
import talsumi.statuesclassic.marderlib.pak.generators.blockmodels.PAKBlockModelGenerator
import talsumi.statuesclassic.marderlib.pak.generators.itemmodels.PAKItemModelGenerator
import talsumi.statuesclassic.marderlib.pak.generators.lang.PAKLangGenerator
import talsumi.statuesclassic.marderlib.pak.generators.recipes.PAKRecipeGenerator
import talsumi.statuesclassic.marderlib.pak.generators.tags.PAKTagGenerator
import java.io.File
import java.nio.file.Files

object PAKGeneratorRegistry {

	private val registry = HashMap<Identifier, List<(namespace: String) -> PAKGenerator>>()
	private val logger = LogManager.getLogger()

	init {
		regGenerators(Identifier("marderlib:block"), ::PAKBlockModelGenerator)
		regGenerators(Identifier("marderlib:item"), ::PAKItemModelGenerator)
		regGenerators(Identifier("marderlib:lang"), ::PAKLangGenerator)
		regGenerators(Identifier("marderlib:block_drops"), ::PAKBlockDropGenerator)
		regGenerators(Identifier("marderlib:tags"), ::PAKTagGenerator)
		regGenerators(Identifier("marderlib:recipes"), ::PAKRecipeGenerator)
	}

	fun regGenerators(type: Identifier, vararg generatorFactories: (namespace: String) -> PAKGenerator)
	{
		if (!registry.containsKey(type))
			registry[type] = generatorFactories.toList()
		else
			throw RuntimeException("Generators for $type already exist!")
	}

	fun getGenerator(type: Identifier, version: Int, namespace: String): PAKGenerator
	{
		if (registry[type]?.get(version - 1) != null)
			return registry[type]!![version - 1].invoke(namespace)
		else
			throw RuntimeException("Generator $type and $version does not exist!")
	}

	/**
	 * Loads a file and runs the generator specified in its header.
	 *
	 * Outputs are placed in outputFolder, however the generator may place its files in subfolders.
	 *
	 * Generator documentation should include output behaviour.
	 */
	fun generateFromFile(file: File, outputFolder: File, log: Boolean = false, overwrite: Boolean = false)
	{
		val sTime = System.currentTimeMillis()

		val ourStream = Files.lines(file.toPath())
		val argMap = HashMap<String, String>()
		val args = ourStream.findFirst().get().replaceFirst('#', ' ').trim().split(' ')
		ourStream.close()

		for (entry in args) {
			val splitEntry = entry.split('=')
			argMap[splitEntry[0]] = splitEntry[1]
		}
		val type = argMap["type"] ?: throw RuntimeException("Type argument not found in header when loading file $file. Make sure your header is valid.")
		val version = argMap["version"]?.toInt() ?: throw RuntimeException("Version argument not found in header when loading file $file. Make sure your header is valid.")
		val namespace = argMap["namespace"] ?: throw RuntimeException("Namespace argument not found in header when loading file $file. Make sure your header is valid.")

		val parsedFile = EzPMReader(file).read()
		val generator = getGenerator(Identifier(type.lowercase()), version.toInt(), namespace)

		val count = generator.generateFiles(parsedFile, outputFolder, overwrite)

		val eTime = System.currentTimeMillis()
		if (log)
			logger.info("Ran generator for file $file in ${eTime-sTime} milliseconds. It created $count files.")
	}
}