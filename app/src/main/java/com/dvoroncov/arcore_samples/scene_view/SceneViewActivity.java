package com.dvoroncov.arcore_samples.scene_view;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import com.dvoroncov.arcore_samples.R;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.function.Consumer;
import java.util.function.Function;

public class SceneViewActivity extends AppCompatActivity {

    private Scene scene;
    private Node cupCakeNode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_view);

        SceneView sceneView = findViewById(R.id.sceneView);

        scene = sceneView.getScene();
        renderObject(Uri.parse("model.sfb"));
    }

    private void renderObject(Uri parse) {
        ModelRenderable.builder()
                .setSource(this, parse)
                .build()
                .thenAccept(this::addNodeToScene)
                .exceptionally(throwable -> {
                    Toast toast =
                            Toast.makeText(this, "Unable to load amenemhat viewRenderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });

    }

    private void addNodeToScene(ModelRenderable model) {

        if (model != null) {
            cupCakeNode = new Node();
            //cupCakeNode.setParent(scene);
            cupCakeNode.setLocalPosition(new Vector3(0f, 0f, -1f));
            cupCakeNode.setLocalScale(new Vector3(3f, 3f, 3f));
            cupCakeNode.setName("Model");
            cupCakeNode.setRenderable(model);

            scene.addChild(cupCakeNode);
        }
    }
}
