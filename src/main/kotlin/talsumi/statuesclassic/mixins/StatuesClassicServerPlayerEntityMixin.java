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

package talsumi.statuesclassic.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import talsumi.statuesclassic.mixininterfaces.StatuesClassicPlayerListenerGrabber;


/**
 * Combined with {@link StatuesClassicServerPlayerEntityAnonymous2Mixin}, allows retrieval of a {@link ServerPlayerEntity} instance from it's {@link ScreenHandlerListener}.
 */
@Mixin(ServerPlayerEntity.class)
public class StatuesClassicServerPlayerEntityMixin {

	@SuppressWarnings("unchecked")
	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(MinecraftServer server, ServerWorld world, GameProfile profile, CallbackInfo info)
	{
		((StatuesClassicPlayerListenerGrabber)((StatuesClassicServerPlayerEntityAccessor)(Object)this).statuesclassic_getScreenHandlerListener()).statuesclassic_setOwningPlayer((ServerPlayerEntity) (Object) this);
	}
}