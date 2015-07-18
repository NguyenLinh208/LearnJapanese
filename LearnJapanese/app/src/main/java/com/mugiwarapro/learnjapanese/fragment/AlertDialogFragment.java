package com.mugiwarapro.learnjapanese.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;

import com.mugiwarapro.learnjapanese.R;
import com.mugiwarapro.learnjapanese.activity.DbCreate;
import com.mugiwarapro.learnjapanese.activity.DbCreateActivity;

import android.content.DialogInterface;

/**
 * Created by usr0200475 on 15/07/18.
 */
public class AlertDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.creat_data_dialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent install = new Intent(getActivity(), DbCreate.class);
                        startActivity(install);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
