package com.dvoroncov.arcore.cloudAnchor

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dvoroncov.arcore.R
import com.dvoroncov.arcore.data.CloudAnchorStorageManager
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val cloudAnchorStorageManager: CloudAnchorStorageManager by inject()
    private var modelRenderable: ModelRenderable? = null
    private var node: TransformableNode? = null
    private var anchor: Anchor? = null
    private var anchorNode: AnchorNode? = null

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createButton.setOnClickListener { onCreateButtonClick() }
        connectButton.setOnClickListener { onConnectButtonClick() }
        cancelButton.setOnClickListener { onCancelButtonClick() }

        initViewRenderable()

        (arFragment as CloudAnchorArFragment).setOnTapArPlaneListener { hitResult, _, _ -> onTapArPlane(hitResult) }
    }

    private fun initViewRenderable() {
        ModelRenderable.builder()
                .setSource(this, Uri.parse("amenemhat.sfb"))
                .build()
                .thenAccept { modelRenderable -> this@MainActivity.modelRenderable = modelRenderable }
                .exceptionally {
                    val toast = Toast.makeText(this, "Unable to load amenemhat modelRenderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    null
                }
    }

    private fun onTapArPlane(hitResult: HitResult) {
        if (anchor == null && modelRenderable != null) {
            setNewAnchor(
                    (arFragment as CloudAnchorArFragment).session.hostCloudAnchor(hitResult.createAnchor())
            )

            disposable = Observable.interval(0, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { checkCloudAnchorState() }
                    .subscribe()
            creatingProgress.visibility = View.VISIBLE
        }
    }

    private fun setNewAnchor(newAnchor: Anchor) {
        anchor = newAnchor
        anchorNode = AnchorNode(newAnchor)
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
        horizontalViewNode.renderable = modelRenderable
    }

    private fun checkCloudAnchorState() {
        val state = anchor!!.cloudAnchorState
        if (state == Anchor.CloudAnchorState.SUCCESS) {
            val code = anchor!!.cloudAnchorId
            showToast(state.toString() + ": " + code)

            val shortCode = cloudAnchorStorageManager.nextShortCode
            cloudAnchorStorageManager.saveCloudAnchorID(shortCode, anchor!!.cloudAnchorId)
            shortCodeTextView.text = getString(R.string.created, shortCode)

            creatingProgress.visibility = View.GONE
            if (disposable != null) {
                disposable!!.dispose()
                disposable = null
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun onCreateButtonClick() {
        cancelButton.visibility = View.VISIBLE
        shortCodeTextView.text = ""
        shortCodeTextView.visibility = View.VISIBLE
        createButton.visibility = View.GONE
        connectButton.visibility = View.GONE
    }

    private fun onConnectButtonClick() {
        shortCodeTextView.visibility = View.GONE
        createButton.visibility = View.GONE
        connectButton.visibility = View.GONE
        cancelButton.visibility = View.VISIBLE

        val dialog = ConnectDialogFragment()
        dialog.setResultListener(object : ConnectDialogFragment.ConnectDialogResultListener {
            override fun onOkPressed(code: Int) {
                val cloudAnchorID = cloudAnchorStorageManager.getCloudAnchorID(code)
                if (TextUtils.isEmpty(cloudAnchorID)) {
                    onCancelButtonClick()
                    return
                }
                shortCodeTextView.text = getString(R.string.connected, code)
                shortCodeTextView.visibility = View.VISIBLE

                val anchor = (arFragment as CloudAnchorArFragment).session.resolveCloudAnchor(cloudAnchorID)
                setNewAnchor(anchor)
            }

            override fun onCancelPressed() {
                onCancelButtonClick()
            }
        })
        dialog.show(supportFragmentManager, "Connect")
    }

    private fun onCancelButtonClick() {
        shortCodeTextView.visibility = View.GONE
        cancelButton.visibility = View.GONE
        creatingProgress.visibility = View.GONE
        createButton.visibility = View.VISIBLE
        connectButton.visibility = View.VISIBLE

        // TODO: 13.09.2018 clear anchor
        if (anchorNode != null) {
            (arFragment as CloudAnchorArFragment).arSceneView.scene.removeChild(anchorNode!!)
            anchorNode = null
        }
    }
}
