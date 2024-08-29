package net.spell_power.mixin.attributes;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.spell_power.api.CrossFunctionalAttributes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(EntityAttributeInstance.class)
public abstract class CrossEntityAttributeInstance implements CrossFunctionalAttributes.Provider {
    @Shadow protected abstract Collection<EntityAttributeModifier> getModifiersByOperation(EntityAttributeModifier.Operation operation);
    @Shadow @Final private RegistryEntry<EntityAttribute> type;
    @Shadow public abstract double getValue();

    // MARK: CrossFunctionalAttributes.Provider

    @Nullable private CrossFunctionalAttributes.Proxy proxy;

    @Override
    public void setProxy(CrossFunctionalAttributes.Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public Collection<EntityAttributeModifier> getModifiersByOperation_Cross(EntityAttributeModifier.Operation operation) {
        return getModifiersByOperation(operation);
    }

    @Override
    public void updateIfNecessaryForCross() {
        getValue();
    }

    // MARK: Mixin

    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    private void getValue_UpdateOthers(CallbackInfoReturnable<Double> cir) {
        if (proxy == null) {
            System.err.println("CrossEntityAttributeInstance.getValue_UpdateOthers, proxy is null");
            return; }
        for (var provider: proxy.getCrossProvidersForPowered(type)) {
            provider.updateIfNecessaryForCross();
        }
    }

    @Inject(method = "getModifiersByOperation", at = @At("RETURN"), cancellable = true)
    private void combine_getModifiersByOperation(EntityAttributeModifier.Operation operation, CallbackInfoReturnable<Collection<EntityAttributeModifier>> cir) {
        var originals = cir.getReturnValue();
        if (proxy == null) {
            System.err.println("CrossEntityAttributeInstance.combine_getModifiersByOperation, proxy is null");
            return;
        }
        var providers = proxy.getCrossProvidersForPowered(type);
        if (!providers.isEmpty()) {
            var result = new ArrayList<>(originals);
            for (var provider: proxy.getCrossProvidersForPowered(type)) {
                result.addAll(provider.getModifiersByOperation_Cross(operation));
            }
            cir.setReturnValue(result);
        }
    }
}
