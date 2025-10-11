package dev.terraquad.integral.client.mixin;

import dev.terraquad.integral.client.IntegralClient;
import dev.terraquad.integral.networking.ClientEvent;
import dev.terraquad.integral.networking.ClientEventC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ResourcePackManager.class)
public abstract class ResourcePackManagerMixin {
    @Inject(method = "createResourcePacks", at = @At("HEAD"))
    private void emitEventAfterReload(CallbackInfoReturnable<List<ResourcePack>> cir) {
        if (IntegralClient.getReady()) {
            ClientPlayNetworking.send(new ClientEventC2SPayload(ClientEvent.RELOAD));
        }
    }
}
