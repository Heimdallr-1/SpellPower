package net.spelldamage.forge;

import net.spelldamage.SpellDamage;
import net.minecraftforge.fml.common.Mod;

@Mod(SpellDamage.MOD_ID)
public class SpellDamageForge {
    public SpellDamageForge() {
        // Submit our event bus to let architectury register our content on the right time
        // EventBuses.registerModEventBus(SpellDamage.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        SpellDamage.init();
    }
}