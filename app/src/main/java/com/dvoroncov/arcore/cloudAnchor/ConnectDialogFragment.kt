package com.dvoroncov.arcore.cloudAnchor

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.text.InputFilter
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import com.dvoroncov.arcore.R

class ConnectDialogFragment : DialogFragment() {

    private var resultListener: ConnectDialogResultListener? = null
    private var shortCodeField: EditText? = null

    private val dialogLayout: LinearLayout
        get() {
            val context = context
            val layout = LinearLayout(context)
            shortCodeField = EditText(context)
            shortCodeField!!.inputType = InputType.TYPE_CLASS_NUMBER
            shortCodeField!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            shortCodeField!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(8))
            layout.addView(shortCodeField)
            layout.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            return layout
        }

    internal interface ConnectDialogResultListener {
        fun onOkPressed(code: Int)

        fun onCancelPressed()
    }

    internal fun setResultListener(resultListener: ConnectDialogResultListener) {
        this.resultListener = resultListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogLayout)
                .setTitle(R.string.connect)
                .setPositiveButton(
                        android.R.string.ok
                ) { _, _ ->
                    val shortCodeText = shortCodeField!!.text
                    if (resultListener != null
                            && shortCodeText != null
                            && shortCodeText.isNotEmpty()) {
                        resultListener!!.onOkPressed(Integer.parseInt(shortCodeText.toString()))
                    }
                }
                .setNegativeButton(
                        android.R.string.cancel
                ) { _, _ -> resultListener!!.onCancelPressed() }
        return builder.create()
    }
}
