package kit.nova_arcana.mixin;


import kit.nova_arcana.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public abstract class ScrewMilk extends Item {
    public ScrewMilk(Settings settings) {
        super(settings);
    }
    @Inject(
            at = @At("HEAD"),
            method = "finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;",
            cancellable = true
    )
    public void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user.getStatusEffect(ModEffects.INSTANCE.getFAIR_GROUND()) != null) {
            cir.setReturnValue(stack);
            cir.cancel();
        }
    }
}
