package talsumi.statuesclassic.core

import com.mojang.authlib.Agent
import com.mojang.authlib.GameProfile
import com.mojang.authlib.ProfileLookupCallback
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*
import java.util.concurrent.Executors

object UUIDLookups {

    private val executor = Executors.newCachedThreadPool()
    private val cache = HashMap<String, UUID?>()
    private val metadata = HashMap<UUID, Metadata>()
    private val maxFrequencyMillis = 1000L

    /**
     * An immediate UUID lookup. [whenFound] and [whenFailed] will be called on the server thread.
     */
    fun lookupUuidFromServer(server: MinecraftServer, username: String, whenFound: (UUID) -> Unit, whenFailed: (() -> Unit) = {})
    {
        internalLookup(server, username, whenFound, whenFailed)
    }

    /**
     * A uuid lookup with limits on frequency. [whenFound] and [whenFailed] will be called on the server thread.
     */
    fun lookupUuidFromClient(player: ServerPlayerEntity, server: MinecraftServer, username: String, whenFound: (UUID) -> Unit, whenFailed: (() -> Unit) = {})
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
        //If the player has made a lookup recently, allow one pending lookup to be processed.
        /*else {
            if (!meta.hasPending) {
                metadata[player.uuid]?.hasPending = true
                internalLookup(server, username, whenFound = {
                    metadata[player.uuid]?.hasPending = false
                    whenFound.invoke(it)
                })
            }
        }*/
    }

    private fun internalLookup(server: MinecraftServer, username: String, whenFound: (UUID) -> Unit, whenFailed: () -> Unit)
    {
        val cached = cache[username]
        if (cached != null)
            return server.execute { whenFound.invoke(cached) }
        else if (cache.containsKey(username))
            server.execute(whenFailed)

        executor.execute {
            server.gameProfileRepo.findProfilesByNames(arrayOf(username), Agent.MINECRAFT, object: ProfileLookupCallback {
                override fun onProfileLookupSucceeded(profile: GameProfile) {
                    cache[username] = profile.id
                    server.execute {
                        whenFound.invoke(profile?.id)
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