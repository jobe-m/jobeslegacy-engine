package com.jobeslegacy.engine.ecs.system

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.jobeslegacy.engine.ecs.component.GravityComponent
import com.jobeslegacy.engine.ecs.component.PlatformerComponent

/**
 * @author Colton Daily
 * @date 3/11/2023
 */
class PlatformerGravitySystem : IteratingSystem(family { all(PlatformerComponent, GravityComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val platformer = entity[PlatformerComponent]
        val gravity = entity[GravityComponent]

        gravity.enableGravityY = !platformer.onGround
    }
}