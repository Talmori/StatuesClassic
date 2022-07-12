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

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import org.spongepowered.include.com.google.common.io.Files
import talsumi.statuesclassic.marderlib.easyparametermapping.EzPMBlock
import java.io.File
import java.nio.file.Path

/**
 * Implement this class and register an instance to [PAKGeneratorRegistry], then set the type in an .mlpak file to load it.
 *
 * Subclasses should have their file/folder output behaviour listed in their documentation.
 */
interface PAKGenerator {

	fun generateFiles(file: EzPMBlock, outputFolder: File, overwrite: Boolean): Int

	/**
	 * Convenience method to automatically encode a [JsonElement] into a file.
	 */
	fun writeJsonToFile(file: Path, json: JsonElement)
	{
		file.parent.toFile().mkdirs()

		val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
		Files.write(gson.toJson(json), file.toFile(), Charsets.UTF_8)
	}
}