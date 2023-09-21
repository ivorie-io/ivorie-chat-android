package com.ivoriechat.android.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivoriechat.android.R;
import com.ivoriechat.android.databinding.FragmentVaultBinding;
import com.ivoriechat.android.utils.AppGeneral;
import com.ivoriechat.android.utils.VaultViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import io.metamask.androidsdk.Dapp;
import io.metamask.androidsdk.EthereumViewModel;
import io.metamask.androidsdk.RequestError;

@AndroidEntryPoint
public class VaultFragment extends Fragment {

    private FragmentVaultBinding binding;

    private static final String TAG = "VaultFragment";

    private LinearLayout mApplyLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        VaultViewModel vaultViewModel =
                new ViewModelProvider(this).get(VaultViewModel.class);

        binding = FragmentVaultBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // vaultViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        EthereumViewModel ethereumViewModel = new ViewModelProvider(this).get(EthereumViewModel.class);
        Dapp ivorieDapp = new Dapp(getString(R.string.app_name), getString(R.string.app_url));
        /*
        ethereumViewModel.connect(ivorieDapp, (result) -> {
            if (result instanceof RequestError) {
                Log.e(TAG, "Ethereum connection error: ${result.message}");
            } else {
                Log.d(TAG, "Ethereum connection result: $result");
            }
            return null;
        });
        */

        mApplyLayout = root.findViewById(R.id.apply_layout);
        mApplyLayout.setOnClickListener(v -> {
            // open new page
            Intent intent = new Intent(getActivity(), VerificationActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}