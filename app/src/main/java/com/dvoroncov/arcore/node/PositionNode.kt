/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dvoroncov.arcore.node

import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator

import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator

/**
 * Node demonstrating rotation and transformations.
 */
class PositionNode : Node() {

    private var rotationAnimation: ObjectAnimator? = null

    private var rotation: Quaternion? = null
    private val duration: Long = 0

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)

        // Animation hasn't been set up.
        if (rotationAnimation == null) {
            return
        }

        val animatedFraction = rotationAnimation!!.animatedFraction
        rotationAnimation!!.duration = duration
        rotationAnimation!!.setCurrentFraction(animatedFraction)
    }

    fun moveTo(angle: Int, strength: Int) {
        if (strength > 0) {
            rotation = Quaternion(localRotation)
            rotation!!.w = angle.toFloat()
            startAnimation()
        }
    }

    override fun onDeactivate() {
        stopAnimation()
    }

    private fun startAnimation() {
        rotationAnimation = createAnimator()
        rotationAnimation!!.target = this
        rotationAnimation!!.duration = duration
        rotationAnimation!!.start()
    }

    private fun stopAnimation() {
        if (rotationAnimation == null) {
            return
        }
        rotationAnimation!!.cancel()
        rotationAnimation = null
    }

    private fun createAnimator(): ObjectAnimator {
        val orbitAnimation = ObjectAnimator()
        orbitAnimation.setObjectValues(localRotation, rotation)
        orbitAnimation.propertyName = "localRotation"
        orbitAnimation.setEvaluator(QuaternionEvaluator())
        orbitAnimation.repeatCount = 0
        orbitAnimation.interpolator = LinearInterpolator()
        orbitAnimation.setAutoCancel(true)
        return orbitAnimation
    }

    companion object {

        private val MAX_DURATION: Long = 1000
    }
}
