package com.dvoroncov.arcore_samples.cloud_anchor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dvoroncov.arcore_samples.R;
import com.dvoroncov.arcore_samples.cloud_anchor.utils.StorageManager;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.BaseTransformableNode;
import com.google.ar.sceneform.ux.SelectionVisualizer;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private CloudAnchorArFragment arFragment;
    private Button createButton;
    private Button connectButton;
    private Button cancelButton;
    private TextView shortCodeTextView;
    private ProgressBar creatingProgressBar;

    private StorageManager storageManager;
    private ViewRenderable viewRenderable;
    private TransformableNode node;
    private Anchor anchor;
    private AnchorNode anchorNode;

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arFragment = (CloudAnchorArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);

        shortCodeTextView = findViewById(R.id.short_code_text_view);
        creatingProgressBar = findViewById(R.id.creating_progress);

        createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(v -> onCreateButtonClick());

        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(v -> onConnectButtonClick());

        cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> onCancelButtonClick());

        storageManager = new StorageManager(this);

        initViewRenderable();

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> onTapArPlane(hitResult));
    }

    private void initViewRenderable() {
        ViewRenderable.builder()
                .setView(this, R.layout.ar_view)
                .build()
                .thenAccept(viewRenderable -> MainActivity.this.viewRenderable = viewRenderable)
                .exceptionally(throwable -> {
                    Toast toast = Toast.makeText(this, "Unable to load amenemhat viewRenderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });
    }

    private void onTapArPlane(HitResult hitResult) {
        if (anchor == null && viewRenderable != null) {
            setNewAnchor(
                    arFragment.getSession().hostCloudAnchor(hitResult.createAnchor())
            );

            disposable = Observable.interval(0, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(aLong -> checkCloudAnchorState())
                    .subscribe();
            creatingProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void setNewAnchor(Anchor newAnchor) {
        FrameLayout selectedNodeBackground = viewRenderable.getView().findViewById(R.id.selected_node_bg);
        ToggleButton translationButton = viewRenderable.getView().findViewById(R.id.translation_toggle_button);
        ToggleButton rotationButton = viewRenderable.getView().findViewById(R.id.rotation_toggle_button);
        ToggleButton scaleButton = viewRenderable.getView().findViewById(R.id.scale_toggle_button);
        translationButton.setOnCheckedChangeListener((buttonView, isChecked) -> node.getTranslationController().setEnabled(isChecked));
        rotationButton.setOnCheckedChangeListener((buttonView, isChecked) -> node.getRotationController().setEnabled(isChecked));
        scaleButton.setOnCheckedChangeListener((buttonView, isChecked) -> node.getScaleController().setEnabled(isChecked));

        anchor = newAnchor;
        anchorNode = new AnchorNode(newAnchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        TransformationSystem transformationSystem = arFragment.getTransformationSystem();
        transformationSystem.setSelectionVisualizer(new SelectionVisualizer() {
            @Override
            public void applySelectionVisual(BaseTransformableNode node) {
                if (selectedNodeBackground != null) {
                    selectedNodeBackground.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void removeSelectionVisual(BaseTransformableNode node) {
                if (selectedNodeBackground != null) {
                    selectedNodeBackground.setVisibility(View.GONE);
                }
            }
        });

        node = new TransformableNode(transformationSystem);
        node.getTranslationController().setEnabled(false);
        node.getRotationController().setEnabled(false);
        node.getScaleController().setEnabled(false);
        node.setParent(anchorNode);

        Node horizontalViewNode = new Node();
        horizontalViewNode.setParent(node);
        horizontalViewNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1f, 0, 0), -90));
        horizontalViewNode.setRenderable(viewRenderable);
    }

    private void checkCloudAnchorState() {
        Anchor.CloudAnchorState state = anchor.getCloudAnchorState();
        if (state == Anchor.CloudAnchorState.SUCCESS) {
            String code = anchor.getCloudAnchorId();
            showToast(state.toString() + ": " + code);

            int shortCode = storageManager.getNextShortCode();
            storageManager.saveCloudAnchorID(shortCode, anchor.getCloudAnchorId());
            shortCodeTextView.setText(getString(R.string.created, shortCode));

            creatingProgressBar.setVisibility(View.GONE);
            if (disposable != null) {
                disposable.dispose();
                disposable = null;
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void onCreateButtonClick() {
        cancelButton.setVisibility(View.VISIBLE);
        shortCodeTextView.setText("");
        shortCodeTextView.setVisibility(View.VISIBLE);
        createButton.setVisibility(View.GONE);
        connectButton.setVisibility(View.GONE);
    }

    private void onConnectButtonClick() {
        shortCodeTextView.setVisibility(View.GONE);
        createButton.setVisibility(View.GONE);
        connectButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.VISIBLE);

        ConnectDialogFragment dialog = new ConnectDialogFragment();
        dialog.setResultListener(new ConnectDialogFragment.ConnectDialogResultListener() {
            @Override
            public void onOkPressed(int code) {
                String cloudAnchorID = storageManager.getCloudAnchorID(code);
                if (TextUtils.isEmpty(cloudAnchorID)) {
                    onCancelButtonClick();
                    return;
                }
                shortCodeTextView.setText(getString(R.string.connected, code));
                shortCodeTextView.setVisibility(View.VISIBLE);

                Anchor anchor = arFragment.getSession().resolveCloudAnchor(cloudAnchorID);
                setNewAnchor(anchor);
            }

            @Override
            public void onCancelPressed() {
                onCancelButtonClick();
            }
        });
        dialog.show(getSupportFragmentManager(), "Connect");
    }

    private void onCancelButtonClick() {
        shortCodeTextView.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        creatingProgressBar.setVisibility(View.GONE);
        createButton.setVisibility(View.VISIBLE);
        connectButton.setVisibility(View.VISIBLE);

        // TODO: 13.09.2018 clear anchor
        if (anchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(anchorNode);
            anchorNode = null;
        }
    }
}
