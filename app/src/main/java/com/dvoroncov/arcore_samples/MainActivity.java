package com.dvoroncov.arcore_samples;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private Renderable renderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);

        //ModelRenderable.builder()
        //        .setSource(this, Uri.parse("amenemhat.sfb"))
        //        .build()
        //        .thenAccept(renderable -> MainActivity.this.renderable = renderable)
        //        .exceptionally(throwable -> {
        //            Toast toast =
        //                    Toast.makeText(this, "Unable to load amenemhat renderable", Toast.LENGTH_LONG);
        //            toast.setGravity(Gravity.CENTER, 0, 0);
        //            toast.show();
        //            return null;
        //        });

        ViewRenderable.builder()
                .setView(this, R.layout.ar_view)
                .build()
                .thenAccept(renderable -> MainActivity.this.renderable = renderable)
                .exceptionally(throwable -> {
                    Toast toast =
                            Toast.makeText(this, "Unable to load amenemhat renderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (renderable == null) {
                return;
            }

            if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return;
            }

            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
            node.setParent(anchorNode);
            node.setRenderable(renderable);
            node.select();
        });
    }
}
