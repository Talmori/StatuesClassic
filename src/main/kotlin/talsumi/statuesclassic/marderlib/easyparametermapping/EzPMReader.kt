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

package talsumi.statuesclassic.marderlib.easyparametermapping

import java.io.File
import java.nio.file.Files
import java.util.stream.Stream

class EzPMReader(private val stream: Stream<String>) {

	private var lineAt = 0
	private val constants = HashMap<String, String>()
	private val baseBlock = EzPMBlock()
	private val iterator: Iterator<String> = stream.iterator()

	constructor(file: File) : this(Files.lines(file.toPath()))
	constructor(file: String) : this(File(file))

	fun read(): EzPMBlock {
		while (true) {
			val line = nextLine() ?: break

			try {
				readSection(line)
			} catch (e: Exception) {
				println("Error while reading EzPM file at line $lineAt")
				e.printStackTrace()
			}
		}

		stream.close()

		return baseBlock
	}

	private fun readSection(line: String) {
		var line = line
		if (line.startsWith('$')) {
			val args = line.substring(1).split(' ')
			constants[args[0]] = args[1]
			return
		}

		if (line.endsWith("{")) {
			val args = line.substring(0, line.length-1).trim().split(' ')
			val block = getBlock()
			for (key in args)
				baseBlock.putBlock(key, block)
			return
		}


		//val args = line.split(' ', limit = 2)
		//baseBlock.putParameter(args[0], if (args.size > 1) constantOverride(args[1]) else "")

		var split = line.indexOf(' ')
		if (line.startsWith('\\')) {
			split = -1
			line = line.substring(1)
		}
		var key = if (split > -1) line.substring(0, split) else line
		baseBlock.putParameter(constantOverride(key), if (split > -1) constantOverride(line.substring(split+1)) else "")
	}

	private fun getBlock(): EzPMBlock
	{
		val startLine = lineAt
		val block = EzPMBlock()

		while (true) {
			var line = nextLine() ?: throw RuntimeException("Did not find closing bracket for block at line $startLine")

			if (line.endsWith("{")) {
				val args = line.substring(0, line.length-1).trim().split(' ')
				val nestedBlock = getBlock()
				for (key in args)
					block.putBlock(key, nestedBlock)
				continue
			}

			if (line.endsWith('}'))
				break

			//val args = line.split(' ')
			//block.putParameter(args[0], if (args.size > 1) constantOverride(args[1]) else "")

			var split = line.indexOf(' ')
			if (line.startsWith('\\')) {
				split = -1
				line = line.substring(1)
			}
			val key = if (split > -1) line.substring(0, split) else line
			block.putParameter(constantOverride(key), if (split > -1) constantOverride(line.substring(split+1)) else "")
		}

		return block
	}

	private fun constantOverride(parameter: String): String
	{
		return if (parameter.startsWith('$')) constants[parameter.substring(1)] ?: throw RuntimeException("Invalid constant $parameter") else parameter
	}

	private fun nextLine(): String?
	{
		while (iterator.hasNext()) {
			lineAt++
			var str = iterator.next()
			val comment = str.indexOf('#')
			if (comment > -1)
				str = str.substring(0, comment)

			if (str.isNotEmpty())
				return str.trim()
		}

		return null
	}
}