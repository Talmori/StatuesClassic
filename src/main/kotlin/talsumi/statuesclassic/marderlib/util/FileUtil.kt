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

package talsumi.statuesclassic.marderlib.util

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object FileUtil {

	fun createPathString(parent: String, vararg folders: String): String
	{
		return Path.of(parent, *folders).toString()
	}

	fun getFileLocationString(folder: String, file: String): String
	{
		return File(folder, file).toString()
	}

	fun createPathToFileString(parent: String, vararg folders: String, file: String): String
	{
		return getFileLocationString(Path.of(parent, *folders).toString(), file)
	}

	fun createPath(parent: File, vararg folders: String): Path
	{
		return Path.of(parent.toString(), *folders)
	}

	fun getFileLocation(folder: File, file: String): File
	{
		return File(folder.toString(), file.toString())
	}

	fun createPathToFile(parent: File, vararg folders: String, file: String): File
	{
		return getFileLocation(Path.of(parent.toString(), *folders).toFile(), file)
	}

	fun readJsonFile(file: File): JsonElement = JsonParser.parseString(String(Files.readAllBytes(file.toPath())))
}