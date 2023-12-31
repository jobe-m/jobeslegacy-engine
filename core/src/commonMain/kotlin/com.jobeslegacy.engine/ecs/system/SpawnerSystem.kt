package com.jobeslegacy.engine.ecs.system


import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.jobeslegacy.engine.ecs.component.GridComponent
import com.jobeslegacy.engine.ecs.component.OffsetByFrameIndexComponent
import com.jobeslegacy.engine.ecs.component.OffsetComponent
import com.jobeslegacy.engine.ecs.component.SpawnerComponent
import com.jobeslegacy.engine.ecs.entity.config.Invokable
import com.jobeslegacy.engine.ecs.entity.config.isInvalidEntity
import com.jobeslegacy.engine.util.random


/**
 * This system is responsible to spawn new entity objects. It shall be the only system which spawns new objects.
 * It can be configured to periodically spawn entities until a total number of spawned objects is reached, or
 * it can also spawn an unlimited number of entities (run forever until it dies).
 */
class SpawnerSystem(
//    private val korgeViewCache: KorgeViewCache = inject("KorgeViewCache")
) : IteratingSystem(
    family { all(SpawnerComponent) },
    interval = Fixed(1 / 60f)
) {
    override fun onTickEntity(entity: Entity) {
        val spawner = entity[SpawnerComponent]
        if (spawner.interval > 0) {
            if (spawner.nextSpawnIn <= 0) {
                var x: Float = 0.0f
                var y: Float = 0.0f
                var setPosition = false
                entity.getOrNull(GridComponent)?.let {
                    x = it.x
                    y = it.y
                    setPosition = true
                }
                entity.getOrNull(OffsetComponent)?.let {
                    x += it.x
                    y += it.y
                    setPosition = true
                }
                entity.getOrNull(OffsetByFrameIndexComponent)?.let {
// TODO
/*
                    // Get offset depending on current animation and frame index
                    val currentFrameIndex = (korgeViewCache[it.entity] as ImageDataViewEx).currentFrameIndex
                    val animationName = it.entity.getOrNull(Sprite)?.animationName ?: ""
                    val offset = it.list[animationName]?.get(currentFrameIndex)
                        ?: error("SpawnerSystem: Cannot get offset by frame index (entity: ${entity.id}, animationName: '$animationName', currentFrameIndex: $currentFrameIndex)")
                    x += offset.x
                    y += offset.y
                    setPosition = true
*/
                }

                for (i in 0 until spawner.numberOfObjects) {
                    var xx = x
                    var yy = y
                    val newEntity =
                        if (spawner.newEntity.isInvalidEntity()) world.entity {}  // create new entity
                        else spawner.newEntity  // use given entity
                    if (spawner.positionVariation != 0.0f) {
                        xx = x + (-spawner.positionVariation..spawner.positionVariation).random()
                        yy = y + (-spawner.positionVariation..spawner.positionVariation).random()
                    }
                    // Directly set position
                    if (setPosition) newEntity.configure {
                        it += GridComponent().apply {
                            this.x = xx
                            this.y = yy
                        }
                    }

                    // Call the configured spawner function for configuring new objects
                    Invokable.invoke(spawner.function, world, newEntity, spawner.config)
                }

                spawner.numberOfObjectsSpawned += spawner.numberOfObjects
                spawner.nextSpawnIn = spawner.interval
                if (spawner.timeVariation != 0) spawner.nextSpawnIn += (-spawner.timeVariation..spawner.timeVariation).random()
            } else {
                spawner.nextSpawnIn--
            }
        }
        if (spawner.totalNumberOfObjects > 0 && spawner.numberOfObjectsSpawned >= spawner.totalNumberOfObjects) entity.configure {
            entity -= SpawnerComponent
        }
    }
}
