package com.jobeslegacy.engine.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.componentTypeOf

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
class GridCollisionResultComponent(var axes: Axes, var dir: Int) : Component<GridCollisionResultComponent> {
    enum class Axes { X, Y, Z }

    override fun type(): ComponentType<GridCollisionResultComponent> = when (axes) {
        Axes.X -> GridCollisionX
        Axes.Y -> GridCollisionY
        Axes.Z -> GridCollisionZ
    }

    companion object {
        val GridCollisionX = componentTypeOf<GridCollisionResultComponent>()
        val GridCollisionY = componentTypeOf<GridCollisionResultComponent>()
        val GridCollisionZ = componentTypeOf<GridCollisionResultComponent>()
    }
}