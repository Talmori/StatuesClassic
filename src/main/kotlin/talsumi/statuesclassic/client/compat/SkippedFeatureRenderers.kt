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

package talsumi.statuesclassic.client.compat

object SkippedFeatureRenderers {

    private val skipped = HashSet<String>()

    init
    {
        //--------------------------3d Skin Layers--------------------------
        //These behave weirdly with statues, not rotating properly and rendering in full colour even when a statue doesn't, so it's best to skip them.
        //TODO: Find a way to make 3d Skin Layers work with statues
        skipped.add("dev.tr7zw.skinlayers.renderlayers.BodyLayerFeatureRenderer")
        skipped.add("dev.tr7zw.skinlayers.renderlayers.HeadLayerFeatureRenderer")
    }

    fun isSkipped(type: Class<Any>): Boolean
    {
        return skipped.contains(type.canonicalName)
    }
}