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
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import talsumi.marderlib.screenhandler.EnhancedScreenHandler
import talsumi.statuesclassic.content.ModScreenHandlers
import talsumi.statuesclassic.core.StatueCreation
import talsumi.statuesclassic.core.StatueData
import java.util.*


class StatueCreationScreenHandler(type: ScreenHandlerType<*>?, syncId: Int, val hitFace: Direction?, val playerFacing: Direction?, val parentPos: BlockPos?, val world: World?) : EnhancedScreenHandler(type, syncId) {

    //Client Constructor
    constructor(syncId: Int, inv: PlayerInventory) : this(syncId, null, null, null, null)

    //Common Constructor
    constructor(syncId: Int, hitFace: Direction?, playerFacing: Direction?, parentPos: BlockPos?, world: World?) : this(
        ModScreenHandlers.statue_creation_screen,
        syncId,
        hitFace,
        playerFacing,
        parentPos,
        world
    ) {
        setup()
    }

    fun setup() {

    }

    fun form(uuid: UUID, data: StatueData)
    {
        if (parentPos != null && world != null) {
            val direction = if (hitFace == Direction.UP || hitFace == Direction.DOWN) playerFacing!!.opposite else hitFace!!
            StatueCreation.tryCreateStatue(parentPos!!, world!!, uuid, data, direction)
        }
        for (listener in getListeners())
            listener.marderlib_getOwningPlayer().closeHandledScreen()
    }

    override fun canUse(player: PlayerEntity): Boolean
    {
        return true
    }

    companion object {
        fun makeFactory(player: PlayerEntity, hitFace: Direction, pos: BlockPos, world: World): NamedScreenHandlerFactory
        {
            return object: NamedScreenHandlerFactory {
                override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler?
                {
                    return StatueCreationScreenHandler(syncId, player.horizontalFacing, hitFace, pos, world)
                }
                override fun getDisplayName(): Text {
                    return TranslatableText("")
                }
            }
        }
    }
}