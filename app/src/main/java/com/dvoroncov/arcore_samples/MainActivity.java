package com.dvoroncov.arcore_samples;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Light;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private Button buttonX;

    private ViewRenderable viewRenderable;
    private ModelRenderable amenemhatRenderable;
    private ModelRenderable planetaryCrawlerRenderable;
    private ModelRenderable redSphereRenderable;

    private TransformableNode node;
    private Anchor anchor;
    private AnchorNode anchorNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        buttonX = findViewById(R.id.button_x);
        //buttonX.setOnClickListener(v -> {
        //    if (anchorNode != null) {
        //        Vector3 position = anchorNode.getLocalPosition();
        //        position.x += 0.1;
        //        anchorNode.setLocalPosition(position);
        //        anchorNode.setEnabled(true);
        //    }
        //});

        ModelRenderable.builder()
                .setSource(this, Uri.parse("amenemhat.sfb"))
                .build()
                .thenAccept(viewRenderable -> MainActivity.this.amenemhatRenderable = viewRenderable)
                .exceptionally(throwable -> {
                    Toast toast =
                            Toast.makeText(this, "Unable to load amenemhat viewRenderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("planetary_crawler.sfb"))
                .build()
                .thenAccept(viewRenderable -> MainActivity.this.planetaryCrawlerRenderable = viewRenderable)
                .exceptionally(throwable -> {
                    Toast toast =
                            Toast.makeText(this, "Unable to load amenemhat viewRenderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });

        ViewRenderable.builder()
                .setView(this, R.layout.ar_view)
                .build()
                .thenAccept(renderable -> MainActivity.this.viewRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast toast = Toast.makeText(this, "Unable to load amenemhat viewRenderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (planetaryCrawlerRenderable == null) {
                return;
            }

            if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return;
            }

            if (anchor == null) {
                planetaryCrawlerRenderable.setShadowCaster(true);

                anchor = hitResult.createAnchor();
                //anchorNode = new AnchorNode(anchor);
                //anchorNode.setParent(arFragment.getArSceneView().getScene());

                RotatingNode planetVisual = new RotatingNode(0, 1, false);
                planetVisual.setParent(arFragment.getArSceneView().getScene());
                planetVisual.setRenderable(planetaryCrawlerRenderable);

                //Button button = viewRenderable.getView().findViewById(R.id.ar_button);
                //TextView textView = viewRenderable.getView().findViewById(R.id.ar_text_view);
                //button.setOnClickListener(v -> textView.setTextColor(getResources().getColor(android.R.color.holo_red_light)));

                //node = new TransformableNode(arFragment.getTransformationSystem());
                //node.setParent(anchorNode);
                //node.setRenderable(planetaryCrawlerRenderable);
                //node.select();
            }
        });
    }
}
