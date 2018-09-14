package com.dvoroncov.arcore_samples.cloud_anchor;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class CloudAnchorArFragment extends ArFragment {

    public Session getSession() {
        return getArSceneView().getSession();
    }

    protected Config getSessionConfiguration(Session session) {
        Config config = new Config(session);
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
        return config;
    }
}
