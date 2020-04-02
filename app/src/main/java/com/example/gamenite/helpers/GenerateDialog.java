package com.example.gamenite.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public interface GenerateDialog {

    default boolean generateConfirmationDialog(Context context, String title, String text){
        final boolean[] confirmed = {false};
        new MaterialAlertDialogBuilder(context)
                .setTitle(title).setMessage(text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmed[0] = true;
                    }
                }).setNegativeButton("No",null).show();
        return confirmed[0];
    }

}
