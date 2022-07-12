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

internal class BlockTextureMapping(val defaultTexture: String?, var particleTexture: String?, val textures: Array<String?>, val allowMissing: Boolean = false) {

	init {
		for (tex in textures.withIndex()) {
			if (tex.value == null)
				textures[tex.index] = defaultTexture ?: if (allowMissing) null else throw RuntimeException("Default texture is missing, and we cannot find a texture for face ${Face.values()[tex.index]}. If this is intended, please mark your entry with 'allow_missing'")
		}

		particleTexture = particleTexture ?: defaultTexture ?: if (allowMissing) null else throw RuntimeException("Default texture is missing, and we do not have a particle texture specified. If this is intended, please mark your entry with 'allow_missing'")
	}

	/**
	 * Creates a new BlockTextureMapping from this one, overriding any old textures.
	 */
	fun createFrom(defaultTexture: String?, particleTexture: String?, overrideTextures: Array<String?>): BlockTextureMapping
	{
		val newTextures = this.textures.clone()
		val particleTexture = particleTexture ?: defaultTexture ?: this.particleTexture

		for (tex in overrideTextures.withIndex())
			if (tex.value != null || defaultTexture != null)
				newTextures[tex.index] = tex.value ?: defaultTexture

		return BlockTextureMapping(defaultTexture ?: this.defaultTexture, particleTexture, newTextures)
	}
}