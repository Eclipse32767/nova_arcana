package kit.nova_arcana

import kit.nova_arcana.datagen.BlockLootGen
import kit.nova_arcana.datagen.RecipeGen
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object TemplateModDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(gen: FabricDataGenerator) {
		val pack = gen.createPack()
		pack.addProvider { v -> RecipeGen(v) }
		pack.addProvider { v -> BlockLootGen(v) }
	}
}




