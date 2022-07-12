/*
 * MIT License
 *
 *  Copyright (c) 2022 Talsumi
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 *
 */

package talsumi.statuesclassic.datagen

import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.marderlib.pak.PAKGeneratorRegistry
import talsumi.statuesclassic.marderlib.util.FileUtil
import java.io.File
import java.nio.file.Paths

object RailExpansionPAK {

	var hasRun = false
	val assetDir = FileUtil.createPathString(Paths.get("").toAbsolutePath().parent.toString(), "src", "main", "resources", "assets", StatuesClassic.MODID)
	val dataDir = FileUtil.createPathString(Paths.get("").toAbsolutePath().parent.toString(), "src", "main", "resources", "data", StatuesClassic.MODID)
	val dataParentDir = FileUtil.createPathString(Paths.get("").toAbsolutePath().parent.toString(), "src", "main", "resources", "data")

	fun generate()
	{
		val generatorFolder = File(FileUtil.createPathString(dataDir, "datagen"))
		val blockModels = FileUtil.createPathToFile(generatorFolder, file = "block_models.mlpak")
		val itemModels = FileUtil.createPathToFile(generatorFolder, file = "item_models.mlpak")
		val blockDrops = FileUtil.createPathToFile(generatorFolder, file = "block_drops.mlpak")
		val tags = FileUtil.createPathToFile(generatorFolder, file = "tags.mlpak")
		val recipes = FileUtil.createPathToFile(generatorFolder, file = "recipes.mlpak")

		//PAKGeneratorRegistry.generateFromFile(blockModels, File(assetDir), true, false)
		PAKGeneratorRegistry.generateFromFile(itemModels, File(assetDir), true, false)
		//PAKGeneratorRegistry.generateFromFile(blockDrops, File(dataDir), true, false)
		//PAKGeneratorRegistry.generateFromFile(tags, File(dataParentDir), true, true)
		//PAKGeneratorRegistry.generateFromFile(recipes, File(dataDir), true, false)
	}
}