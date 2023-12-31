package com.jobeslegacy.engine.ecs.system.tweenscripting

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.jobeslegacy.engine.ecs.component.*
import com.jobeslegacy.engine.ecs.component.TweenProperty.*
import com.jobeslegacy.engine.util.Easing
import com.jobeslegacy.engine.ecs.component.MoveComponent
import com.jobeslegacy.engine.ecs.entity.config.Invokable
import com.lehaine.littlekt.log.Logger


/**
 * This system creates Animate... components on entities which should be animated according to the game config.
 */
class TweenScriptSystem : IteratingSystem(
    family { all(TweenScript) },
    interval = EachFrame
) {
    // Internally used variables in createAnimateComponent function
    private lateinit var currentTween: TweenBase
    private lateinit var currentParentTween: ParallelTweens

    private val logger = Logger("AnimationScriptSystem")
    /**
     * When the system is called it checks for the AnimationScript component if the waitTime is over.
     * If yes, then it checks the step at index in the array which type it has.
     *
     * - On TweenSequence type the system creates a new AnimationScript Entity and configures it to operate on the sub-script.
     *   Then it checks all next steps and if they are an AnimationScript it spawns new AnimationScript Entities for each sub-script.
     *   The first next step which is an AnimationStep stops checking and configures the current script with the next waitTime from the next
     *   to be executed animation step. If the array of steps comes to the end before a new AnimationStep is found than the current
     *   AnimationScript Entity is destroyed. The animation of that script is finished.
     *
     * - On AnimationStep type it is first checked if the animation is already active. If not then the waitTime is set to the
     *   duration of the [ParallelTweens] and all specified animation component (e.g. [AnimateDrawableAlpha] and [AnimatePositionShapeX]) are created for
     *   each specified sub-entity. If the animation is already active than that means that the duration of the [ParallelTweens] is over. In
     *   that case it is checked if a next step is available in the steps array. In case the next step is an AnimationStep than the waitTime
     *   is set to the delay of the next animation step.
     *
     * TODO: next is to be checked
     * Then it checks for the next animation step if the delay time was reached and creates or updates the corresponding animate component.
     * It updates the wait-time with the duration of the animation step. After finishing the animation step the delay of the next step is
     * loaded as wait-time.
     * Finally, it checks if the animation script has reached the end. If so it removes the AnimationScript component again from the entity.
     */
    override fun onTickEntity(entity: Entity) {
        val animScript = entity[TweenScript]

        if (animScript.timeProgress >= animScript.waitTime) {
            animScript.timeProgress = 0f

            if (animScript.index >= animScript.tweens.size) {
                world -= entity
            } else when (val currentTween = animScript.tweens[animScript.index]) {
                is SequenceOfTweens -> {
                    var nextTween: TweenBase? = currentTween
                    while (nextTween is SequenceOfTweens) {
                        world.entity { it += TweenScript(tweens = (nextTween as SequenceOfTweens).tweens) }
                        nextTween = checkNextTween(animScript, entity)
                        if (nextTween == null) break
                    }
                }
                else -> {
                    if (animScript.active) checkNextTween(animScript, entity)
                    else {
                        animScript.active = true
                        animScript.waitTime = currentTween.duration ?: 0f

                        if (currentTween is ParallelTweens) {

                            currentTween.tweens.forEach { tween ->
                                // Check if tween has an own delay and spawn a new AnimationSequence for it
                                if (tween.delay != null && tween.delay!! > 0f) {
                                    world.entity {
                                        it += TweenScript(
                                            tweens = listOf(
                                                tween.also { tween ->
                                                    if (tween.duration == null) tween.duration = currentTween.duration ?: 0f
                                                    if (tween.easing == null) tween.easing = currentTween.easing ?: Easing.LINEAR
                                                }
                                            )
                                        )
                                    }
                                } else checkTween(tween, currentTween)
                            }
                        } else if (currentTween !is Wait) // currentTween cannot be TweenSequence and ParallelTweens - so this cast is safe
                            checkTween(currentTween, ParallelTweens())  // ParallelTweens() as 2nd parameter gives default values for delay, duration and easing
                    }
                }
            }
        } else animScript.timeProgress += deltaTime
    }

    private fun checkNextTween(animScript: TweenScript, entity: Entity) : TweenBase? {
        animScript.index++
        animScript.active = false
        return if (animScript.index >= animScript.tweens.size) {
            world -= entity
            null
        } else {
            val nextTween = animScript.tweens[animScript.index]
            animScript.waitTime = nextTween.delay ?: 0f
            nextTween
        }
    }

    private fun checkTween(tween: TweenBase, parentTween: ParallelTweens) {
        currentTween = tween
        currentParentTween = parentTween
        when (tween) {
//            is TweenAppearance -> tween.entity.getOrError(Appearance).let { start ->
//                tween.alpha?.let { end -> createAnimateComponent(AppearanceAlpha, value = start.alpha, change = end - start.alpha) }
//                tween.tint?.let { end ->  createAnimateComponent(AppearanceTint, start.tint ?: Rgb.WHITE,
//                    Rgb(r = end.r - (start.tint?.r ?: 0xff), g = end.g - (start.tint?.g ?: 0xff), b = end.b - (start.tint?.b ?: 0xff))
//                ) }
//                tween.visible?.let { value -> createAnimateComponent(AppearanceVisible, value) }
//            }
            is TweenPositionAndSizeComponent -> tween.entity.getOrError(PositionAndSizeComponent).let { start ->
//                tween.x?.let { end -> createAnimateComponent(PositionShapeX, start.x, end - start.x) }
                tween.y?.let { end -> createAnimateComponent(PositionAndSizeComponentY, start.y, end - start.y) }
            }
            is TweenMoveComponent -> tween.entity.getOrError(MoveComponent).let { start ->
                tween.velocityX?.let { end -> createAnimateComponent(MoveComponentVelocityX, start.velocityX , end - start.velocityX) }
                tween.velocityY?.let { end -> createAnimateComponent(MoveComponentVelocityY, start.velocityY , end - start.velocityY) }
            }
//            is TweenOffset -> tween.entity.getOrError(Offset).let { start ->
//                tween.x?.let { end -> createAnimateComponent(OffsetX, start.x, end - start.x) }
//                tween.y?.let { end -> createAnimateComponent(OffsetY, start.y, end - start.y) }
//            }
//            is TweenLayout -> tween.entity.getOrError(Layout).let { start ->
//                tween.centerX?.let { value -> createAnimateComponent(LayoutCenterX, value) }
//                tween.centerY?.let { value -> createAnimateComponent(LayoutCenterY, value) }
//                tween.offsetX?.let { end -> createAnimateComponent(LayoutOffsetX, start.offsetX, end - start.offsetX) }
//                tween.offsetY?.let { end -> createAnimateComponent(LayoutOffsetY, start.offsetY, end - start.offsetY) }
//            }
            is TweenSprite -> tween.entity.getOrError(SpriteComponent).let { _ ->  // make sure to-be-animated-entity is of type sprite
                tween.startAnimation?.let { value -> createAnimateComponent(TweenSpriteComponentPlayOnce, value) }
//                tween.animationName?.let { value -> createAnimateComponent(SpriteAnimName, value) }
//                tween.isPlaying?.let { value -> createAnimateComponent(SpriteIsPlaying, value) }
//                tween.forwardDirection?.let { value -> createAnimateComponent(SpriteForwardDirection, value) }
//                tween.loop?.let { value -> createAnimateComponent(SpriteLoop, value) }
//                tween.destroyOnPlayingFinished?.let { value -> createAnimateComponent(SpriteDestroyOnPlayingFinished, value) }
            }
//            is TweenSwitchLayerVisibility -> tween.entity.getOrError(SwitchLayerVisibility).let { start ->
//                tween.offVariance?.let { end -> createAnimateComponent(SwitchLayerVisibilityOnVariance, value = start.offVariance, change = end - start.offVariance) }
//                tween.onVariance?.let { end -> createAnimateComponent(SwitchLayerVisibilityOffVariance, start.onVariance, end - start.onVariance) }
//            }
//            is TweenNoisyMove -> tween.entity.getOrError(NoisyMove).let { start ->
//                tween.triggerChangeVariance?.let { end -> createAnimateComponent(NoisyMoveTriggerChangeVariance, value = start.triggerVariance, change = end - start.triggerVariance) }
//                tween.triggerBackVariance?.let { end -> createAnimateComponent(NoisyMoveTriggerBackVariance, value = start.terminateVariance, change = end - start.terminateVariance) }
//                tween.interval?.let { end -> createAnimateComponent(NoisyMoveInterval, value = start.interval, change = end - start.interval) }
//                tween.intervalVariance?.let { end -> createAnimateComponent(NoisyMoveIntervalVariance, value = start.intervalVariance, change = end - start.intervalVariance) }
//                tween.xTarget?.let { end -> createAnimateComponent(NoisyMoveOffsetXTarget, value = start.xTarget, change = end - start.xTarget) }
//                tween.yTarget?.let { end -> createAnimateComponent(NoisyMoveOffsetYTarget, value = start.yTarget, change = end - start.yTarget) }
//                tween.xVariance?.let { end -> createAnimateComponent(NoisyMoveOffsetXVariance, value = start.xVariance, change = end - start.xVariance) }
//                tween.yVariance?.let { end -> createAnimateComponent(NoisyMoveOffsetYVariance, value = start.yVariance, change = end - start.yVariance) }
//                tween.x?.let { end -> createAnimateComponent(NoisyMoveX, value = start.x, change = end - start.x) }
//                tween.y?.let { end -> createAnimateComponent(NoisyMoveY, value = start.y, change = end - start.y) }
//            }
            is TweenSpawnerComponent -> tween.entity.getOrError(SpawnerComponent).let { start ->
                tween.numberOfObjects?.let { end -> createAnimateComponent(SpawnerComponentNumberOfObjects, start.numberOfObjects, end - start.numberOfObjects) }
                tween.interval?.let { end -> createAnimateComponent(SpawnerComponentInterval, start.interval, end - start.interval) }
                tween.timeVariation?.let { end -> createAnimateComponent(SpawnerComponentTimeVariation, start.timeVariation, end - start.timeVariation) }
                tween.positionVariation?.let { end -> createAnimateComponent(SpawnerComponentPositionVariation, start.positionVariation, end - start.positionVariation) }
            }
//            is TweenSound -> tween.entity.getOrError(Sound).let{ start ->
//                tween.startTrigger?.let { value -> createAnimateComponent(SoundStartTrigger, value) }
//                tween.stopTrigger?.let { value -> createAnimateComponent(SoundStopTrigger, value) }
//                tween.position?.let { end -> createAnimateComponent(SoundPosition, start.position, end - start.position) }
//                tween.volume?.let { end -> createAnimateComponent(SoundVolume, start.volume, end - start.volume) }
//            }
//            // A special type of TweenSpawner which directly changes the Spawner component
//            is SpawnEntity -> {
//                val spawnedEntity = if (tween.entity.isInvalidEntity()) world.entity() else tween.entity
//                Invokable.invoke(tween.function, world, spawnedEntity, tween.config)
//            }
//            // A special type of TweenLifeCycle (to be created if needed) which directly changes the LifeCycle component
//            is DeleteEntity -> tween.entity.configure { entityToDelete -> world -= entityToDelete }
            is ExecuteConfigFunction -> Invokable.invoke(tween.function, world, tween.entity, tween.config)
            else -> error("AnimationScriptSystem: Animate function for tween $tween is not implemented!")
        }
    }

    private fun createAnimateComponent(componentProperty: TweenProperty, value: Any, change: Any = Unit) {
        currentTween.entity.configure { animatedEntity ->
            animatedEntity.getOrAdd(componentProperty.type) { TweenComponent(componentProperty) }.also {
                it.change = change
                it.value = value
                it.duration = currentTween.duration ?: currentParentTween.duration ?: 0f
                it.timeProgress = 0f
                it.easing = currentTween.easing ?: currentParentTween.easing ?: Easing.LINEAR
            }
        }
    }

    private inline fun <reified T : Component<*>> Entity.getOrError(componentType: ComponentType<T>) : T =
        getOrNull(componentType) ?: error("AnimationScriptSystem: Entity '${this.id}' does not contain component type '${componentType}'!\nEntity snapshot: \n${world.snapshotOf(this)}".replace("), ", "),\n"))
}
