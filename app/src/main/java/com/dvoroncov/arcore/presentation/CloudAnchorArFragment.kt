package com.dvoroncov.arcore.presentation

import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

class CloudAnchorArFragment : ArFragment() {

    val session: Session
        get() = arSceneView.session

    override fun getSessionConfiguration(session: Session): Config {
        val config = Config(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        return config
    }
}
