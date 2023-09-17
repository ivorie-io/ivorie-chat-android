package com.ivoriechat.android.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivoriechat.android.databinding.FragmentEarningUsersBinding;
import com.ivoriechat.android.utils.EarningUsersViewModel;

public class EarningUsersFragment extends Fragment {

    private FragmentEarningUsersBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EarningUsersViewModel earningUsersViewModel =
                new ViewModelProvider(this).get(EarningUsersViewModel.class);

        binding = FragmentEarningUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        earningUsersViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

