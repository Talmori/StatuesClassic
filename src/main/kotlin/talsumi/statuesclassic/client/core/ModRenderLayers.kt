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

package talsumi.statuesclassic.client.core

import net.minecraft.client.render.*
import net.minecraft.util.Identifier
import talsumi.statuesclassic.StatuesClassic
import java.util.*

//Oh god.
object ModRenderLayers: RenderLayer(StatuesClassic.MODID,
    VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, {}, {}) {

    private val statueTranslucent: (Identifier, Boolean) -> RenderLayer

    init
    {
        statueTranslucent = { tex, outlined ->
            StatueRenderLayer("statues_translucent",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true,
                StatueRenderLayer.Parameters(
                    Shader {ModShaders.noHueShader},
                    Texture(tex, false, false),
                    TRANSLUCENT_TRANSPARENCY,
                    DISABLE_CULLING,
                    ENABLE_LIGHTMAP,
                    ENABLE_OVERLAY_COLOR
                )
            ) }
    }

    fun getStatueTranslucent(texture: Identifier): RenderLayer = statueTranslucent.invoke(texture, false)

   private class StatueRenderLayer: RenderLayer {

        var openAffectedOutline: Optional<RenderLayer> = Optional.empty()

       constructor(name: String, vertexFormat: VertexFormat, drawMode: VertexFormat.DrawMode, bufferSize: Int, hasCrumbling: Boolean, translucent: Boolean, parameters: Parameters) :
                super(name, vertexFormat, drawMode, bufferSize, hasCrumbling, translucent,
                    {
                        for (parameter in parameters.all)
                            parameter.startDrawing()
                    },
                    {
                        for (parameter in parameters.all)
                            parameter.endDrawing()
                    })

        class Parameters(val shader: Shader, val texture: Texture, val transparency: Transparency, val cull: Cull, val lightmap: Lightmap, val overlay: Overlay)
        {
            val all = arrayOf(shader, texture, transparency, cull, lightmap, overlay, LEQUAL_DEPTH_TEST, NO_LAYERING, MAIN_TARGET, DEFAULT_TEXTURING, ALL_MASK, FULL_LINE_WIDTH)
        }
    }
}