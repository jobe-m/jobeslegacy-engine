package com.jobeslegacy.engine.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.jobeslegacy.engine.ecs.entity.config.nothing
import com.jobeslegacy.engine.util.Identifier
import com.jobeslegacy.engine.util.cache.GameObjectConfigCache
import com.lehaine.littlekt.graphics.g2d.Animation
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.util.datastructure.Pool
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Enables playback of [Animation] classes.
 * @see [Animation]
 * // @param KeyFrameType the type of key frame the [Animation] class is using.
 * @author Marko Koschak
 * @date 1-Dec-2023
 */
@Serializable @SerialName("AnimationComponent")
data class AnimationComponent(
    /** Identifier for getting the [Animation] from GameObjectConfigCache */
    var gameObjectConfigName: Identifier = nothing,

    /** The total amount of frames played across all animations */
    var totalFramesPlayed: Int = 0,
    /** The index of the current frame */
    var currentFrameIdx: Int = 0,  // Do not set directly use set function below!

    // private in animation player
    var animationRequested: Boolean = false,
    var numOfFramesRequested: Int = 0,  //  Do not set directly use set function below!
    var frameDisplayTime: Duration = 100.milliseconds,
    var animationType: AnimationType = AnimationType.STANDARD,
    var lastFrameTime: Duration = Duration.ZERO,
    var remainingDuration: Duration = Duration.ZERO


) : Component<AnimationComponent> {
    // TODO only for debugging - remove later
    var time: Float = 0f

    /**
     * The current playing animations. Set this by using one of the `play` methods.
     * @see [play]
     * @see [playOnce]
     * @see [playLooped]
     */
    private val currentAnimation: Animation<TextureSlice>? get() =
        GameObjectConfigCache.getOrNull(gameObjectConfigName)?.animation

    /**
     * The total frames the [currentAnimation] has.
     */
    val totalFrames: Int get() = currentAnimation?.totalFrames ?: 1

//    private val currentAnimationInstance: AnimationInstance<KeyFrameType>? get() = stack.firstOrNull()  TODO integrate stack later

    val currentKeyFrame: TextureSlice?
        get() = currentAnimation?.getFrame(currentFrameIdx)

    /**
     * Invoked when a frame is changed.
     */
    // TODO check how this can be converted - see below
    //      lambdas cannot be serialized
//    var onFrameChange: ((Int) -> Unit)? = null

    /**
     * Invoked when animation has finished playing.
     */
    // TODO check how we can do deletion of entities when animation finishes - this we cannot store as lambda in the component
    //      lambdas cannot be serialized
    var onAnimationFinish: (() -> Unit)? = null

    @JvmName("internalSetCurrentFrameIdx")
    private fun setCurrentFrameIdx(value: Int) {
        currentFrameIdx = value umod totalFrames
// TODO
//        onFrameChange?.invoke(field)
    }

    @JvmName("internalSetNumOfFramesRequested")
    private fun setNumOfFramesRequested(value: Int) {
        numOfFramesRequested = value
        if (value == 0) {
            stop()
//                stack.removeFirstOrNull()?.let {  TODO integrate stack later
//                    animInstancePool.free(it)
//                }
//                stack.firstOrNull()?.let { animInstance ->
//                    animInstance.anim?.let {
//                        setAnimInfo(it,
//                            animInstance.plays,
//                            animInstance.speed,
//                            animInstance.duration,
//                            animInstance.type)
//                    }
//                }

// TODO
            onAnimationFinish?.invoke()
        }
    }

    @JvmName("internalSetRemainingDuration")
    private fun setRemainingDuration(value: Duration) {
        remainingDuration = value
        if (value <= Duration.ZERO) {
            stop()
//                stack.removeFirstOrNull()?.let {  TODO integrate stack later
//                    animInstancePool.free(it)
//                }
//                stack.firstOrNull()?.let { animInstance ->
//                    animInstance.anim?.let {
//                        setAnimInfo(it,
//                            animInstance.plays,
//                            animInstance.speed,
//                            animInstance.duration,
//                            animInstance.type)
//                    }
//                }
        }
    }

// TODO not needed - maybe move outside of AnimationComponent
//    private val tempFrames = mutableListOf<KeyFrameType>()
//    private val tempIndices = mutableListOf<Int>()
//    private val tempTimes = mutableListOf<Duration>()
//    private val tempAnim = Animation(tempFrames, tempIndices, tempTimes)

// TODO integrate later when we need it
//    private val states = arrayListOf<AnimationState<KeyFrameType>>()
//    private val stack = arrayListOf<AnimationInstance<KeyFrameType>>()
//    private val animInstancePool = Pool(reset = { it.reset() }, 20) {
//        AnimationInstance<KeyFrameType>()
//    }


    /**
     * Play a specified frame for a certain amount of frames.
     */
// TODO check if we need this functionality here or move it outside of AnimationComponent
//    fun play(frame: KeyFrameType, frameTime: Duration = 50.milliseconds, numFrames: Int = 1) {
//        tempFrames.clear()
//        tempIndices.clear()
//        tempTimes.clear()
//        tempFrames += frame
//        repeat(numFrames) {
//            tempIndices += 0
//            tempTimes += frameTime
//        }
//        play(tempAnim)
//    }

    /**
     * Play the specified animation an X number of times.
     * @param animation the animation to play
     * @param times the number of times to play
     * @param queue if `false` will play the animation immediately
     */
    fun play(gameObjectConfigName: Identifier, times: Int = 1, queue: Boolean = false) {
//        if (!queue && stack.isNotEmpty()) {  TODO integrate stack later
//            animInstancePool.free(stack)
//            stack.clear()
//        }
//        stack += animInstancePool.alloc().apply {
//            anim = animation
//            plays = times
//            type = AnimationType.STANDARD
//        }
//        if (!queue) {
//            currentAnimationInstance?.let { animInstance ->
//                animInstance.anim?.let {
//                    setAnimInfo(it, animInstance.plays, animInstance.speed, animInstance.duration, animInstance.type)
//                }
//            }
//        }
        this.gameObjectConfigName = gameObjectConfigName
        setNumOfFramesRequested(times * totalFrames)
        animationType = AnimationType.STANDARD
    }

    /**
     * Play the specified animation one time.
     * @param animation the animation to play
     */
    fun playOnce(gameObjectConfigName: Identifier) = play(gameObjectConfigName)

//    private fun playStateAnimOnce(animation: Animation<KeyFrameType>) {
//        play(animation)
//        stack.lastOrNull()?.let {  TODO integrate stack later
//            it.isStateAnim = true
//        }
//    }

    /**
     * Play the specified animation as a loop.
     * @param animation the animation to play
     */
    fun playLooped(gameObjectConfigName: Identifier) {
//        if (stack.isNotEmpty()) {  TODO integrate stack later
//            animInstancePool.free(stack)
//            stack.clear()
//        }
//        stack += animInstancePool.alloc().apply {
//            anim = animation
//            type = AnimationType.LOOPED
//        }
//        currentAnimationInstance?.let { animInstance ->
//            animInstance.anim?.let {
//                setAnimInfo(it, animInstance.plays, animInstance.speed, animInstance.duration, animInstance.type)
//            }
//        }
        this.gameObjectConfigName = gameObjectConfigName
        animationType = AnimationType.LOOPED
    }

    /**
     * Play the specified animation for a duration.
     * @param animation the animation to play
     * @param duration the duration to play the animation
     * @param queue if `false` will play the animation immediately
     */
    fun play(gameObjectConfigName: Identifier, duration: Duration, queue: Boolean = false) {
//        setAnimInfo(animation)
//        if (!queue && stack.isNotEmpty()) {  TODO integrate stack later
//            animInstancePool.free(stack)
//            stack.clear()
//        }
//        stack += animInstancePool.alloc().apply {
//            anim = animation
//            this.duration = duration
//            type = AnimationType.DURATION
//        }
//        if (!queue) {
//            currentAnimationInstance?.let { animInstance ->
//                animInstance.anim?.let {
//                    setAnimInfo(it, animInstance.plays, animInstance.speed, animInstance.duration, animInstance.type)
//                }
//            }
//        }

        this.gameObjectConfigName = gameObjectConfigName
        setRemainingDuration(duration)
        animationType = AnimationType.DURATION
    }

    /**
     * Runs any updates for any requested animation and grabs the next frame if so.
     */
    fun update(dt: Duration) {
        // TODO integrate later when we need it
        // updateStateAnimations()

        if (animationRequested) {
            nextFrame(dt)
        }
    }

    /**
     * Starts any currently stopped animation from the beginning. This only does something when an animation is stopped with [stop].
     */
    fun start() {
//        currentAnimation?.let {
        animationRequested = true
        setCurrentFrameIdx(0)
        lastFrameTime = Duration.ZERO
//        }
    }

    /**
     * Restarts the current running animation from the beginning. Is the same as invoking [stop] and then [start].
     */
    fun restart()  = start()

    /**
     * Resumes any currently stopped animation. This only does something when an animation is stopped with [stop].
     */
    fun resume() {
//        currentAnimation?.let {
        animationRequested = true
//        }
    }

    /**
     * Stops any running animations. Resume the current animation with [resume].
     */
    fun stop() {
        animationRequested = false
    }

    private fun nextFrame(frameTime: Duration) {
        lastFrameTime += frameTime
        if (lastFrameTime + frameTime >= frameDisplayTime) {
            when (animationType) {
                AnimationType.STANDARD -> {
                    if (numOfFramesRequested > 0) {
                        setNumOfFramesRequested(numOfFramesRequested - 1)
                    }
                }
                AnimationType.DURATION -> {
                    setRemainingDuration(remainingDuration - lastFrameTime)
                }
                AnimationType.LOOPED -> {
                    // do nothing, let it loop
                }
            }

            if (animationRequested) {
                totalFramesPlayed++
                setCurrentFrameIdx(currentFrameIdx + 1)
                frameDisplayTime = currentAnimation?.getFrameTime(currentFrameIdx) ?: Duration.ZERO
                lastFrameTime = Duration.ZERO
            }
        }
    }

// TODO not yet needed
//    private fun setAnimInfo(
//        animation: Animation<KeyFrameType>,
//        cyclesRequested: Int = 1,
//        speed: Float = 1f,
//        duration: Duration = Duration.INFINITE,
//        type: AnimationType = AnimationType.STANDARD,
//    ) {
//        currentFrameIdx = 0
//        frameDisplayTime = animation.getFrameTime(currentFrameIdx) * speed.toDouble()
//        animationType = type
//        animationRequested = true
//        remainingDuration = duration
//        numOfFramesRequested = cyclesRequested * totalFrames
//    }

//    private fun playStateAnimLooped(animation: Animation<KeyFrameType>) {
//        playLooped(animation)
//        stack.lastOrNull()?.let {  TODO integrate stack later
//            it.isStateAnim = true
//        }
//    }

// TODO integrate later when we need it
    /**
     * Priority is represented by the deepest. The deepest has top priority while the shallowest has lowest.
     */
//    fun registerState(
//        anim: Animation<KeyFrameType>,
//        priority: Int,
//        loop: Boolean = true,
//        reason: () -> Boolean = { true },
//    ) {
//        removeState(anim)
//        states.add(AnimationState(anim, priority, loop, reason))
//        states.sortByDescending { priority }
//    }
//
//    fun removeState(anim: Animation<KeyFrameType>) {
//        states.find { it.anim == anim }?.also { states.remove(it) }
//    }
//
//    fun removeAllStates() {
//        states.clear()
//    }
//
//    private fun updateStateAnimations() {
//        if (states.isEmpty()) return
//
//        if (stack.isNotEmpty() && currentAnimationInstance?.isStateAnim == false) {
//            return
//        }
//
//        states.fastForEach { state ->
//            if (state.reason()) {
//                if (currentAnimation == state.anim) return
//                if (state.loop) {
//                    playStateAnimLooped(state.anim)
//                } else {
//                    playStateAnimOnce(state.anim)
//                }
//                return
//            }
//        }
//    }

//    private data class AnimationInstance<KeyFrameType>(
//        var anim: Animation<KeyFrameType>? = null,
//        var plays: Int = 1,
//        var duration: Duration = Duration.INFINITE,
//        var isStateAnim: Boolean = false,
//        var speed: Float = 1f,
//        var type: AnimationType = AnimationType.STANDARD,
//    ) {
//
//        fun reset() {
//            anim = null
//            plays = 1
//            duration = Duration.INFINITE
//            isStateAnim = false
//            speed = 1f
//            type = AnimationType.STANDARD
//        }
//    }

// TODO integrate later when we need it
//
//    private data class AnimationState<KeyFrameType>(
//        val anim: Animation<KeyFrameType>,
//        val priority: Int,
//        val loop: Boolean,
//        val reason: () -> Boolean,
//    )

    override fun type() = AnimationComponent
    companion object : ComponentType<AnimationComponent>()
}

/**
 * The playback types of animation.
 * @author Colton Daily
 * @date 11/27/2021
 */
enum class AnimationType {
    /**
     * An animation that runs from start to end an **X** amount of times.
     */
    STANDARD,

    /**
     * An animation the loops from start to end.
     */
    LOOPED,

    /**
     * An animation type the runs for a certain duration.
     */
    DURATION
}

internal infix fun Int.umod(that: Int): Int {
    val remainder = this % that
    return when {
        remainder < 0 -> remainder + that
        else -> remainder
    }
}