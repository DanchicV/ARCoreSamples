package com.dvoroncov.arcore.data

import com.dvoroncov.arcore.data.models.AnchorModel
import com.google.firebase.database.*


class CloudAnchorStorageManager {

    var cloudAnchorModels = mutableListOf<AnchorModel>()

    private val reference: DatabaseReference

    init {
        val database = FirebaseDatabase.getInstance()
        reference = database.reference
        loadCloudAnchorIDs()
    }

    private fun loadCloudAnchorIDs() {
        reference.child(CLOUD_ANCHOR_LIST_KEY)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (postSnapshot in dataSnapshot.children) {
                            val anchorModel = AnchorModel(postSnapshot.key!!, postSnapshot.value as String)
                            if (!cloudAnchorModels.contains(anchorModel)) {
                                cloudAnchorModels.add(anchorModel)
                                RxBus.publish(RxBus.AddNewAnchor(anchorModel))
                            }
                        }
                    }
                })
    }

    fun uploadCloudAnchorID(anchorModel: AnchorModel) {
        cloudAnchorModels.add(anchorModel)
        reference.child(CLOUD_ANCHOR_LIST_KEY)
                .updateChildren(mutableMapOf(Pair(anchorModel.anchorId, anchorModel.model as Any)))
    }

    companion object {
        private const val CLOUD_ANCHOR_LIST_KEY = "cloud_anchor_list"
    }
}