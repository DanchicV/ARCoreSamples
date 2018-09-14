package com.dvoroncov.arcore_samples.cloud_anchor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.dvoroncov.arcore_samples.R;

public class ConnectDialogFragment extends DialogFragment {

    interface ConnectDialogResultListener {
        void onOkPressed(int code);

        void onCancelPressed();
    }

    private ConnectDialogResultListener resultListener;
    private EditText shortCodeField;

    void setResultListener(ConnectDialogResultListener resultListener) {
        this.resultListener = resultListener;
    }

    private LinearLayout getDialogLayout() {
        Context context = getContext();
        LinearLayout layout = new LinearLayout(context);
        shortCodeField = new EditText(context);
        shortCodeField.setInputType(InputType.TYPE_CLASS_NUMBER);
        shortCodeField.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        shortCodeField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        layout.addView(shortCodeField);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(getDialogLayout())
                .setTitle(R.string.connect)
                .setPositiveButton(
                        android.R.string.ok,
                        (dialog, which) -> {
                            Editable shortCodeText = shortCodeField.getText();
                            if (resultListener != null
                                    && shortCodeText != null
                                    && shortCodeText.length() > 0) {
                                resultListener.onOkPressed(Integer.parseInt(shortCodeText.toString()));
                            }
                        })
                .setNegativeButton(
                        android.R.string.cancel,
                        (dialog, which) -> resultListener.onCancelPressed()
                );
        return builder.create();
    }
}
