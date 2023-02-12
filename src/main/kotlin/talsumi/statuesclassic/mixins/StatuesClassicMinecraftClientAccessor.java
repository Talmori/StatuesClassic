package talsumi.statuesclassic.mixins;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Queue;

@Mixin(MinecraftClient.class)
public interface StatuesClassicMinecraftClientAccessor {
    @Accessor
    Queue<Runnable> getRenderTaskQueue();
}
