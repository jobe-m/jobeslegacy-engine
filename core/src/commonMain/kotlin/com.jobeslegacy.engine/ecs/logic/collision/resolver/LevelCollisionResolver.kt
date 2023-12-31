package com.jobeslegacy.engine.ecs.logic.collision.resolver

import com.jobeslegacy.engine.ecs.component.GridCollisionComponent
import com.jobeslegacy.engine.ecs.component.GridCollisionResultComponent
import com.jobeslegacy.engine.ecs.component.GridComponent
import com.jobeslegacy.engine.ecs.component.MoveComponent
import com.jobeslegacy.engine.ecs.logic.collision.checker.LevelCollisionChecker
import com.jobeslegacy.engine.ecs.system.GridSystemConfig
import kotlin.math.floor

/**
 * @author Colton Daily
 * @date 3/10/2023
 */
object LevelCollisionResolver : CollisionResolver() {

    override fun resolveXCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: GridCollisionComponent,
        collisionResult: GridCollisionResultComponent
    ) {
        val checker = collision.checker
        if (checker is LevelCollisionChecker) {
            if (collisionResult.dir == -1) {
                grid.xr = checker.leftCollisionRatio
                move.velocityX *= 0.5f
            }
            if (collisionResult.dir == 1) {
                grid.xr = checker.rightCollisionRatio
                move.velocityX *= 0.5f
            }
        } else {
            super.resolveXCollision(grid, move, collision, collisionResult)
        }
    }

    override fun resolveYCollision(
        grid: GridComponent,
        move: MoveComponent,
        collision: GridCollisionComponent,
        collisionResult: GridCollisionResultComponent
    ) {
        val checker = collision.checker
        if (checker is LevelCollisionChecker) {
            val heightCoordDiff =
                if (checker.useTopCollisionRatio) checker.topCollisionRatio else floor(GridSystemConfig.HEIGHT / GridSystemConfig.GRID_CELL_SIZE_F)
            if (collisionResult.dir == -1) {
                grid.yr = heightCoordDiff
                move.velocityY = 0f
            }
            if (collisionResult.dir == 1) {
                grid.yr = checker.bottomCollisionRatio
                move.velocityY = 0f
            }
        } else {
            super.resolveYCollision(grid, move, collision, collisionResult)
        }
    }
}