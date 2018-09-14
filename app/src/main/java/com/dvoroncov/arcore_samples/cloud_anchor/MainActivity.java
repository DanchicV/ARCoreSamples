package com.dvoroncov.arcore_samples.cloud_anchor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dvoroncov.arcore_samples.R;
import com.dvoroncov.arcore_samples.cloud_anchor.utils.StorageManager;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.TransformableNode;


public class MainActivity extends AppCompatActivity {

    private CloudAnchorArFragment arFragment;
    private Button createButton;
    private Button connectButton;
    private Button cancelButton;
    private TextView shortCodeTextView;

    private StorageManager storageManager;
    private ViewRenderable viewRenderable;
    private TransformableNode node;
    private Anchor anchor;
    private AnchorNode anchorNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arFragment = (CloudAnchorArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);

        shortCodeTextView = findViewById(R.id.short_code_text_view);

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
            viewRenderable.setShadowCaster(true);
            setNewAnchor(
                    arFragment.getSession().hostCloudAnchor(hitResult.createAnchor())
            );
        }
    }

    private void setNewAnchor(Anchor newAnchor) {
        anchor = newAnchor;
        anchorNode = new AnchorNode(newAnchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        Button button = viewRenderable.getView().findViewById(R.id.ar_button);
        TextView textView = viewRenderable.getView().findViewById(R.id.ar_text_view);
        button.setOnClickListener(v -> {
            textView.setTextColor(getColor(R.color.colorAccent));
            checkCloudAnchorState();
        });

        node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(viewRenderable);
        node.select();
    }

    private void checkCloudAnchorState() {
        Anchor.CloudAnchorState state = anchor.getCloudAnchorState();
        showToast(state.toString());

        if (state == Anchor.CloudAnchorState.SUCCESS) {
            String code = anchor.getCloudAnchorId();
            showToast(state.toString() + ": " + code);

            int shortCode = storageManager.getNextShortCode();
            storageManager.saveCloudAnchorID(shortCode, anchor.getCloudAnchorId());
            shortCodeTextView.setText(getString(R.string.created, shortCode));
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
        createButton.setVisibility(View.VISIBLE);
        connectButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.GONE);

        // TODO: 13.09.2018 clear anchor
        if (anchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(anchorNode);
            anchorNode = null;
        }
    }
}
