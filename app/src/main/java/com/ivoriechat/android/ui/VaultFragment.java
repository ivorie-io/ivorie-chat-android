package com.ivoriechat.android.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivoriechat.android.databinding.FragmentVaultBinding;
import com.ivoriechat.android.utils.VaultViewModel;

public class VaultFragment extends Fragment {

    private FragmentVaultBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        VaultViewModel vaultViewModel =
                new ViewModelProvider(this).get(VaultViewModel.class);

        binding = FragmentVaultBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // vaultViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}