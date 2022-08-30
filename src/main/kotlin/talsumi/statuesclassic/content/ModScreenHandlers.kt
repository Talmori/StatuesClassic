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

package talsumi.statuesclassic.content

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.util.Identifier
import talsumi.statuesclassic.StatuesClassic
import talsumi.statuesclassic.content.screen.StatueCreationScreenHandler
import talsumi.statuesclassic.content.screen.StatueEquipmentScreenHandler

object ModScreenHandlers {

    val statue_equipment_screen = ScreenHandlerRegistry.registerExtended(Identifier(StatuesClassic.MODID, "statue_equipment_screen")) { id, inv, buf -> StatueEquipmentScreenHandler(id, inv, buf)}
    val statue_creation_screen = ScreenHandlerRegistry.registerExtended(Identifier(StatuesClassic.MODID, "statue_creation_screen")) { id, inv, buf -> StatueCreationScreenHandler(id, inv, buf) }

    fun wake() = Unit
}