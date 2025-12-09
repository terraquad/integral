package dev.terraquad.integral.client.mixin;

import dev.terraquad.integral.client.IntegralClient;
import dev.terraquad.integral.networking.ClientEvent;
import dev.terraquad.integral.networking.ClientEventC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @Inject(method = "openAllSelected", at = @At("HEAD"))
    private void emitEventAfterReload(CallbackInfoReturnable<List<PackResources>> cir) {
        if (IntegralClient.getReady()) {
            ClientPlayNetworking.send(new ClientEventC2SPayload(ClientEvent.RELOAD));
        }
    }
}
