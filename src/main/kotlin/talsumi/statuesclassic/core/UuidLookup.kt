package talsumi.statuesclassic.core

import com.mojang.authlib.Agent
import com.mojang.authlib.GameProfile
import com.mojang.authlib.ProfileLookupCallback
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl
import net.minecraft.server.MinecraftServer
import java.lang.Exception
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object UuidLookup {

    private val executor = Executors.newCachedThreadPool()

    /**
     * An immediate UUID lookup.
     */
    fun lookupUuidFromServer(server: MinecraftServer, username: String, whenFound: (UUID) -> Unit)
    {
        executor.execute {
            server.gameProfileRepo.findProfilesByNames(arrayOf(username), Agent.MINECRAFT, object: ProfileLookupCallback {
            override fun onProfileLookupSucceeded(profile: GameProfile) {
                server.execute {
                    whenFound.invoke(profile?.id)
                }
            }

            override fun onProfileLookupFailed(profile: GameProfile?, exception: Exception?) = Unit
        }) }
    }
}