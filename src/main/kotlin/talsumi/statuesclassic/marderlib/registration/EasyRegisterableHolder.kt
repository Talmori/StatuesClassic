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

package talsumi.statuesclassic.marderlib.registration

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Allows easy registration of registerable objects held in properties.
 * Fill your class with objects, then call regAll to automatically register them.
 *
 * Annotate objects with [CustomName] if you wish.
 *
 * Note: T is not required to be specified, an EasyRegisterableHolder can hold multiple types of registerables.
 *
 * Somewhat slow.
 */
open class EasyRegisterableHolder<T: Any> {

	private val idMap = HashMap<T, Int>()
	private var id = 0

	fun getAll(): List<T>
	{
		return idMap.keys.toList()
	}

	fun <O: T>reg(obj: O): O
	{
		idMap[obj] = id++
		return obj
	}

	fun regAll(reg: Registry<T>, type: KClass<T>, modId: String): List<T>
	{
		val all = arrayOfNulls<Pair<T, String>>(id)

		for (prop in this::class.memberProperties) {
			val obj = prop.getter.call(this)
			if (type.isInstance(obj)) {
				if (idMap.containsKey(obj))
					all[idMap[obj]!!] = Pair(obj as T, prop.findAnnotation<CustomName>()?.name ?: prop.name.lowercase())
				else
					println("An object exists in a field that has not been registered using reg()! Modid: $modId, Type:$type, Registry:$reg")
			}
		}

		for (obj in all)
			Registry.register(reg, Identifier(modId, obj!!.second), obj!!.first)

		val list = getAll()

		idMap.clear()

		return list
	}
}