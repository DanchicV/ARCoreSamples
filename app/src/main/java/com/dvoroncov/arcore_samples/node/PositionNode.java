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
package com.dvoroncov.arcore_samples.node;

import android.animation.ObjectAnimator;
import android.support.annotation.Nullable;
import android.view.animation.LinearInterpolator;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;

/**
 * Node demonstrating rotation and transformations.
 */
public class PositionNode extends Node {

    private static final long MAX_DURATION = 1000;

    @Nullable
    private ObjectAnimator rotationAnimation = null;

    private Quaternion rotation = null;
    private long duration = 0;

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        // Animation hasn't been set up.
        if (rotationAnimation == null) {
            return;
        }

        float animatedFraction = rotationAnimation.getAnimatedFraction();
        rotationAnimation.setDuration(duration);
        rotationAnimation.setCurrentFraction(animatedFraction);
    }

    public void moveTo(int angle, int strength) {
        if (strength > 0) {
            rotation = new Quaternion(getLocalRotation());
            rotation.w = angle;
            startAnimation();
        }
    }

    @Override
    public void onDeactivate() {
        stopAnimation();
    }

    private void startAnimation() {
        rotationAnimation = createAnimator();
        rotationAnimation.setTarget(this);
        rotationAnimation.setDuration(duration);
        rotationAnimation.start();
    }

    private void stopAnimation() {
        if (rotationAnimation == null) {
            return;
        }
        rotationAnimation.cancel();
        rotationAnimation = null;
    }

    private ObjectAnimator createAnimator() {
        ObjectAnimator orbitAnimation = new ObjectAnimator();
        orbitAnimation.setObjectValues(getLocalRotation(), rotation);
        orbitAnimation.setPropertyName("localRotation");
        orbitAnimation.setEvaluator(new QuaternionEvaluator());
        orbitAnimation.setRepeatCount(0);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);
        return orbitAnimation;
    }
}
