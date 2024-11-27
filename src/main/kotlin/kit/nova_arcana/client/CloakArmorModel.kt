package kit.nova_arcana.client

import kit.nova_arcana.armor.CloakArmorItem
import net.minecraft.util.Identifier
import software.bernie.geckolib.model.GeoModel

class CloakArmorModel: GeoModel<CloakArmorItem>() {
    override fun getModelResource(p0: CloakArmorItem?): Identifier {
        return Identifier("nova_arcana:geo/desert-witch.geo.json")
    }

    override fun getTextureResource(p0: CloakArmorItem?): Identifier {
        return Identifier("nova_arcana:textures/armor/desert-witch.png")
    }

    override fun getAnimationResource(p0: CloakArmorItem?): Identifier {
        return Identifier("nova_arcana:animations/desert-witch.animation.json")
    }
}

