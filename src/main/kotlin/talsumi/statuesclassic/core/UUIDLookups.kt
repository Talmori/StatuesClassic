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

package talsumi.statuesclassic.core

import com.mojang.authlib.Agent
import com.mojang.authlib.GameProfile
import com.mojang.authlib.ProfileLookupCallback
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import talsumi.statuesclassic.StatuesClassic
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap

object UUIDLookups {

    private val executor = Executors.newCachedThreadPool()
    private val cache = HashMap<String, GameProfile?>()
    private val metadata = HashMap<UUID, Metadata>()
    private val maxFrequencyMillis = 1000L

    fun rawGet(username: String): GameProfile?
    {
        return cache[username]
    }

    /**
     * An immediate profile lookup. [whenFound] and [whenFailed] will be called on the server thread.
     */
    fun lookupProfileFromServer(server: MinecraftServer, username: String, whenFound: (GameProfile) -> Unit, whenFailed: (() -> Unit) = {})
    {
        internalLookup(server, username, whenFound, whenFailed)
    }

    /**
     * A profile lookup with limits on frequency. [whenFound] and [whenFailed] will be called on the server thread.
     */
    fun lookupProfileFromClient(player: ServerPlayerEntity, server: MinecraftServer, username: String, whenFound: (GameProfile) -> Unit, whenFailed: (() -> Unit) = {})
    {
        val time = System.currentTimeMillis()
        val meta = metadata[player.uuid] ?: run {
            val meta = Metadata(time, false)
            metadata[player.uuid] = meta
            meta
        }

        //If the player hasn't made any lookups recently, immediately return a lookup.
        if (time >= meta.timeout) {
            internalLookup(server, username, whenFound, whenFailed)
            meta.timeout = time + maxFrequencyMillis
        }
        else {
            StatuesClassic.LOGGER.warn("Player ${player.name} (UUID ${player.uuid})'s frequency of requested UUID lookups has passed the allowed amount!")
        }
    }

    private fun internalLookup(server: MinecraftServer, username: String, whenFound: (GameProfile) -> Unit, whenFailed: () -> Unit)
    {
        val username = username.lowercase().take(16)
        val cached = cache[username]
        if (cached != null)
            return server.execute { whenFound.invoke(cached) }
        else if (cache.containsKey(username))
            return server.execute(whenFailed)

        executor.execute {
            server.gameProfileRepo.findProfilesByNames(arrayOf(username), Agent.MINECRAFT, object: ProfileLookupCallback {
                override fun onProfileLookupSucceeded(profile: GameProfile)
                {
                    cache[username] = profile
                    server.execute {
                        whenFound.invoke(profile)
                    }
                }

                override fun onProfileLookupFailed(profile: GameProfile?, exception: Exception?)
                {
                    cache[username] = null
                    server.execute(whenFailed)
                }
            })
        }
    }

    class Metadata(var timeout: Long, var hasPending: Boolean)
}