package com.ivoriechat.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.ivoriechat.android.R;


public class AuthenticationReminderDialogFragment extends DialogFragment {

    public AuthenticationReminderDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView;
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.fragment_authentication_reminder_dialog, null);
        builder.setView(rootView);

        Button cancelButton = rootView.findViewById(R.id.remind_me_later_button);
        cancelButton.setOnClickListener(view -> dismiss());

        Button goSettingsNowButton = rootView.findViewById(R.id.login_now_button);
        goSettingsNowButton.setOnClickListener(view -> {
            dismiss();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        return builder.create();
    }

}