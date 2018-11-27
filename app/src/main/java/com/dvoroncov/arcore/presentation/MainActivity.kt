package com.dvoroncov.arcore.presentation

import RxBus
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dvoroncov.arcore.R
import com.dvoroncov.arcore.data.CloudAnchorStorageManager
import com.dvoroncov.arcore.data.models.AnchorModel
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val cloudAnchorStorageManager: CloudAnchorStorageManager by inject()
    private val disposable: CompositeDisposable = CompositeDisposable()
    private var trackingStateDisposable: Disposable? = null
    private var cloudAnchorStateDisposable: Disposable? = null
    private var modelRenderable: ModelRenderable? = null
    private var node: TransformableNode? = null
    private var createdAnchor: Anchor? = null
    private var anchorNode: AnchorNode? = null
    private var adapter = ModelsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        modelsRecyclerView.layoutManager = linearLayoutManager
        modelsRecyclerView.adapter = adapter
        createButton.setOnClickListener { onCreateButtonClick() }
        cancelButton.setOnClickListener { onCancelButtonClick() }

        (arFragment as CloudAnchorArFragment).setOnTapArPlaneListener { hitResult, _, _ -> onTapArPlane(hitResult) }

        trackingStateDisposable = Observable.interval(0, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .doOnNext { checkTrackingState() }
                .subscribe()
    }

    private fun checkTrackingState() {
        val frame = (arFragment as CloudAnchorArFragment).arSceneView.arFrame
        if (frame != null) {
            val state = frame.camera.trackingState
            if (state == TrackingState.TRACKING) {
                showLoadedModels()
                trackingStateDisposable?.dispose()
                disposable.add(
                        RxBus.listen(RxBus.AddNewAnchor::class.java)
                                .subscribe {
                                    addNewAnchor(it.anchorModel)
                                }
                )
            }
        }
    }

    private fun onTapArPlane(hitResult: HitResult) {
        if (creatingProgress.visibility != Button.VISIBLE) {
            val selectedModel = resources.getStringArray(R.array.models)[adapter.selectedModel]
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(selectedModel))
                    .build()
                    .thenAccept { modelRenderable -> createNewAnchor(hitResult, modelRenderable, selectedModel) }
                    .exceptionally {
                        val toast = Toast.makeText(this, "Unable to load amenemhat modelRenderable", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        null
                    }
        }
    }

    private fun createNewAnchor(hitResult: HitResult, modelRenderable: ModelRenderable, selectedModel: String) {
        this.modelRenderable = modelRenderable
        if (createdAnchor == null) {
            createdAnchor = (arFragment as CloudAnchorArFragment).session.hostCloudAnchor(hitResult.createAnchor())
            addNewAnchor(
                    createdAnchor!!,
                    selectedModel
            )

            cloudAnchorStateDisposable = Observable.interval(0, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { checkCloudAnchorState(selectedModel) }
                    .subscribe()

            creatingProgress.visibility = View.VISIBLE
        }

    }

    private fun addNewAnchor(anchorModel: AnchorModel) {
        if (TextUtils.isEmpty(anchorModel.anchorId)
                or TextUtils.isEmpty(anchorModel.model)) {
            return
        }

        val anchor = (arFragment as CloudAnchorArFragment).session.resolveCloudAnchor(anchorModel.anchorId)
        runOnUiThread { addNewAnchor(anchor, anchorModel.model) }
    }

    private fun addNewAnchor(anchor: Anchor, model: String) {
        anchorNode = AnchorNode(anchor)
        anchorNode!!.setParent((arFragment as CloudAnchorArFragment).arSceneView.scene)
        val transformationSystem = (arFragment as CloudAnchorArFragment).transformationSystem

        node = TransformableNode(transformationSystem)
        node!!.translationController.isEnabled = false
        node!!.rotationController.isEnabled = false
        node!!.scaleController.isEnabled = false
        node!!.setParent(anchorNode)

        val horizontalViewNode = Node()
        horizontalViewNode.setParent(node)
        horizontalViewNode.localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), -90f)
        ModelRenderable.builder()
                .setSource(this, Uri.parse(model))
                .build()
                .thenAccept { modelRenderable -> horizontalViewNode.renderable = modelRenderable }
                .exceptionally {
                    val toast = Toast.makeText(this, "Unable to load amenemhat modelRenderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    null
                }
    }

    private fun checkCloudAnchorState(selectedModel: String) {
        val state = createdAnchor!!.cloudAnchorState
        if (state == Anchor.CloudAnchorState.SUCCESS) {
            cloudAnchorStateDisposable?.dispose()

            val cloudAnchorId = createdAnchor!!.cloudAnchorId
            showToast(state.toString() + ": " + cloudAnchorId)
            cloudAnchorStorageManager.uploadCloudAnchorID(AnchorModel(cloudAnchorId, selectedModel))

            createdAnchor = null
            creatingProgress.visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun onCreateButtonClick() {
        cancelButton.visibility = View.VISIBLE
        modelsRecyclerView.visibility = View.VISIBLE
        createButton.visibility = View.GONE
    }

    private fun showLoadedModels() {
        val anchorModels = cloudAnchorStorageManager.cloudAnchorModels
        for (anchorModel in anchorModels) {
            addNewAnchor(anchorModel)
        }
    }

    private fun onCancelButtonClick() {
        cancelButton.visibility = View.GONE
        modelsRecyclerView.visibility = View.GONE
        creatingProgress.visibility = View.GONE
        createButton.visibility = View.VISIBLE

        // TODO: 13.09.2018 clear createdAnchor
        if (anchorNode != null) {
            (arFragment as CloudAnchorArFragment).arSceneView.scene.removeChild(anchorNode!!)
            anchorNode = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
