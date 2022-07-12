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

import java.util.*

//TODO: EzPM writer
class EzPMBlock {

	private val children: MutableMap<String, EzPMBlock> = HashMap()
	private val parameters: MutableMap<String, String> = HashMap()
	private var childrenKeys: MutableList<String> = ArrayList()
	private var parameterKeys: MutableList<String> = ArrayList()

	fun getBlock(key: String): EzPMBlock? = children[key]
	fun getParameter(key: String): String? = parameters[key]

	fun getParameterAsBoolean(key: String): Boolean? = parameters[key]?.toBooleanStrictOrNull()
	fun getParameterAsInt(key: String): Int? = parameters[key]?.toIntOrNull()
	fun getParameterAsLong(key: String): Long? = parameters[key]?.toLongOrNull()
	fun getParameterAsFloat(key: String): Float? = parameters[key]?.toFloatOrNull()
	fun getParameterAsDouble(key: String): Double? = parameters[key]?.toDoubleOrNull()

	fun getChildrenMap(): Map<String, EzPMBlock> = Collections.unmodifiableMap(children)
	fun getParameterMap(): Map<String, String> = Collections.unmodifiableMap(parameters)

	fun childIterator(): Iterator<Entry<String, EzPMBlock>> = EzPMIterator(children, childrenKeys)
	fun parameterIterator(): Iterator<Entry<String, String>> = EzPMIterator(parameters, parameterKeys)

	/**
	 * Returns all children in the order they were added
	 */
	fun getChildrenList(): List<Entry<String, EzPMBlock>>
	{
		val list = ArrayList<Entry<String, EzPMBlock>>()
		for (key in childrenKeys)
			list.add(Entry(key, children[key]!!))
		return list
	}

	/**
	 * Returns all parameters in the order they were added
	 */
	fun getParameterList(): List<Entry<String, String>>
	{
		val list = ArrayList<Entry<String, String>>()
		for (key in parameterKeys)
			list.add(Entry(key, parameters[key]!!))
		return list
	}

	fun blockCount(): Int = children.size
	fun parameterCount(): Int = parameters.size

	fun hasBlock(key: String): Boolean = children[key] != null
	fun hasParameter(key: String): Boolean = parameters[key] != null

	fun putBlock(key: String, block: EzPMBlock)
	{
		children[key] = block
		childrenKeys.add(key)
	}

	fun putParameter(key: String, value: String)
	{
		parameters[key] = value
		parameterKeys.add(key)
	}

	/**
	 * Removes a sub-block from this block.
	 * Make sure to call [flushIndexing] after you have removed all the elements you want.
	 */
	fun removeBlock(key: String): EzPMBlock? = children.remove(key)

	/**
	 * Removes a parameter from this block.
	 * Make sure to call [flushIndexing] after you have removed all the elements you want.
	 */
	fun removeParameter(key: String): String? = parameters.remove(key)

	/**
	 * Call to remove all invalid key indexes.
	 */
	fun flushIndexing()
	{
		childrenKeys = childrenKeys.filter { children.contains(it) }.toMutableList()
		parameterKeys = parameterKeys.filter { parameters.contains(it) }.toMutableList()
	}

	override fun hashCode(): Int = children.hashCode() + parameters.hashCode()
	override fun equals(other: Any?): Boolean = other is EzPMBlock && children == other.children && parameters == other.parameters
	override fun toString(): String = "Children: [$children] Parameters: [$parameters]"

	class Entry<K, V>(val key: K, val value: V) {
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as Entry<*, *>

			if (key != other.key) return false
			if (value != other.value) return false

			return true
		}

		override fun hashCode(): Int {
			var result = key?.hashCode() ?: 0
			result = 31 * result + (value?.hashCode() ?: 0)
			return result
		}

		override fun toString(): String = "Entry(key=$key, value=$value)"
	}

	class EzPMIterator<K, V>(val map: Map<K, V>, val indexing: List<K>): Iterator<Entry<K, V>> {
		var index = 0

		override fun hasNext(): Boolean = index < indexing.size

		override fun next(): Entry<K, V>
		{
			val key = indexing.get(index++)
			return Entry(key, map[key]!!)
		}
	}
}