package com.yourname.companionmod.mixin;

import com.yourname.companionmod.CompanionMod;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StateHolder.class)
public abstract class StateHolderMixin<O, S> {
    @Shadow(remap = false)
    public abstract boolean hasProperty(Property<?> property);

    @SuppressWarnings("unchecked")
    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true, remap = false)
    private <T extends Comparable<T>> void companionmod$guardDoorOpenProperty(Property<T> property,
                                                                               CallbackInfoReturnable<T> cir) {
        if (!this.hasProperty(property) && property == BlockStateProperties.OPEN) {
            CompanionMod.LOGGER.debug("Prevented invalid OPEN property read on {}", this);
            cir.setReturnValue((T) Boolean.FALSE);
        }
    }
}
