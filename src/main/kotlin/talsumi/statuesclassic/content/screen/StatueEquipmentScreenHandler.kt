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

package talsumi.statuesclassic.content.screen

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import talsumi.marderlib.screenhandler.EnhancedScreenHandler
import talsumi.statuesclassic.content.ModScreenHandlers
import talsumi.statuesclassic.content.blockentity.StatueBE
import talsumi.statuesclassic.core.StatueCreation

class StatueEquipmentScreenHandler(type: ScreenHandlerType<*>?, syncId: Int, val inv: Inventory, val statue: StatueBE?) : EnhancedScreenHandler(type, syncId) {

    //Client Constructor
    constructor(syncId: Int, inv: PlayerInventory) : this(syncId, inv, SimpleInventory(6), null)

    //Common Constructor
    constructor(syncId: Int, pInv: PlayerInventory, inv: Inventory, statue: StatueBE?) : this(ModScreenHandlers.statue_equipment_screen, syncId, inv, statue)
    {
        setup(pInv, inv)
    }

    fun setup(pInv: PlayerInventory, inv: Inventory)
    {
        addPlayerInventory(pInv, 8, 96)
        addSlotBox(inv, 0, 80, 8, 1, 4, 18, 18)
        addSlot(inv, 4, 61, 34)
        addSlot(inv, 5, 99, 34)
    }

    fun updateHands(left: Float, right: Float)
    {
        if (statue != null)
            StatueCreation.updateStatueHands(statue.pos, statue.world ?: return, left, right)
    }

    override fun canUse(player: PlayerEntity): Boolean
    {
        return true
    }

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack = handleShiftClick(player, index, inv)

    companion object {
        fun makeFactory(statue: StatueBE): NamedScreenHandlerFactory
        {
            return object: NamedScreenHandlerFactory {
                override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler?
                {
                    return StatueEquipmentScreenHandler(syncId, inv, statue.inventory, statue)
                }
                override fun getDisplayName(): Text {
                    return TranslatableText("")
                }
            }
        }
    }
}