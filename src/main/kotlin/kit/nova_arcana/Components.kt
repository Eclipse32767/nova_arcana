package kit.nova_arcana

import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

val ManaKey = ComponentRegistry.getOrCreate(Identifier("nova_arcana:mana"), Mana().javaClass)
class Components: EntityComponentInitializer {
    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(ManaKey) {Mana()}
    }
}

class ManaHandle(private val provider: Any) {
    private fun get(): Mana {
        return ManaKey.get(provider)
    }
    var mana: Int
        get() = get().manapts
        set(v) {
            get().manapts = v
        }
    var manacap: Int
        get() = get().manacap
        set(v) {
            get().manacap = v
        }
    var castTimer: Int
        get() = get().castTimer
        set(v) {
            get().castTimer = v
        }
    var lastpos: Vec3d
        get() = Vec3d(get().lastX, get().lastY, get().lastZ)
        set(v) {
            get().lastX = v.x
            get().lastY = v.y
            get().lastZ = v.z
        }
    var lastRegen: Int
        get() = get().lastRegen
        set(v) {
            get().lastRegen = v
        }
    var lastspnt: Int
        get() = get().lastpts
        set(v) {
            get().lastpts = v
        }
    fun syncMana() {
        ManaKey.sync(provider)
    }
}