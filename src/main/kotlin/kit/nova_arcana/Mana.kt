package kit.nova_arcana

import com.sun.tools.javac.code.Attribute
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.world.PersistentState
import net.minecraft.world.World


class Mana: PlayerComponent<Mana>, AutoSyncedComponent {
    var manapts = 100
    var manacap = 100
    var lastRegen = 0
    var lastpts = 100
    var castTimer = 0
    var lastX = 0.0
    var lastY = 0.0
    var lastZ = 0.0
    override fun readFromNbt(nbt: NbtCompound) {
        manapts = nbt.getInt("manapts")
        manacap = nbt.getInt("manacap")
        castTimer = nbt.getInt("castTimer")
        lastRegen = nbt.getInt("lastRegen")
        lastpts = nbt.getInt("lastpts")
        lastX = nbt.getDouble("lastX")
        lastY = nbt.getDouble("lastY")
        lastZ = nbt.getDouble("lastZ")
    }

    override fun writeToNbt(nbt: NbtCompound) {
        nbt.putInt("manapts", manapts)
        nbt.putInt("manacap", manacap)
        nbt.putInt("castTimer", castTimer)
        nbt.putInt("lastRegen", lastRegen)
        nbt.putInt("lastpts", manapts)
        nbt.putDouble("lastX", lastX)
        nbt.putDouble("lastY", lastY)
        nbt.putDouble("lastZ", lastZ)
    }
}