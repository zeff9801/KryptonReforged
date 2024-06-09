package me.steinborn.krypton.mixin.network.shared.flushconsolidation;

import io.netty.channel.*;
import me.steinborn.krypton.mod.shared.network.ConfigurableAutoFlush;
import net.minecraft.network.NetworkManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Optimizes ClientConnection by adding the ability to skip auto-flushing and using void promises where possible.
 */
@Mixin(NetworkManager.class)
public abstract class ClientConnectionMixin implements ConfigurableAutoFlush {
    @Shadow private Channel channel;
    private AtomicBoolean autoFlush;


    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAddedFields(CallbackInfo ci) {
        this.autoFlush = new AtomicBoolean(true);
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/network/NetworkManager;channel:Lio/netty/channel/Channel;", opcode = Opcodes.GETFIELD))
    public Channel disableForcedFlushEveryTick(NetworkManager clientConnection) {
        return null;
    }

    @Override
    public void setShouldAutoFlush(boolean shouldAutoFlush) {
        boolean prev = this.autoFlush.getAndSet(shouldAutoFlush);
        if (!prev && shouldAutoFlush) {
            this.channel.flush();
        }
    }
}
