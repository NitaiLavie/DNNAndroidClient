package com.dnnproject.android.dnnandroidclient;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Created by nitai on 23/06/17.
 */

public class DnnAboutDialogFragment extends AppCompatDialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        ((TextView) getDialog().findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Context context = this.getContext();
        final SpannableString s =
                new SpannableString(context.getText(R.string.about_text));
        Linkify.addLinks(s, Linkify.WEB_URLS);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(R.string.about_title)
                .setMessage(s)
                .setPositiveButton(R.string.about_done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
//                .setNegativeButton(R.string.about_cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
