package kit.nova_arcana.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface BurnTimeAccessor {
    @Accessor("burnTime")
    int getBurnTime();
    @Accessor("burnTime")
    void setBurnTime(int v);
    @Accessor("fuelTime")
    int getFuelTime();
    @Accessor("fuelTime")
    void setFuelTime(int v);
    @Accessor("cookTime")
    int getCookTime();
    @Accessor("cookTime")
    void setCookTime(int v);
}
