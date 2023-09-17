package com.ivoriechat.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ivoriechat.android.databinding.FragmentPayingUsersBinding;
import com.ivoriechat.android.utils.PayingUsersViewModel;

public class PayingUsersFragment extends Fragment {

    private FragmentPayingUsersBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PayingUsersViewModel payingUsersViewModel =
                new ViewModelProvider(this).get(PayingUsersViewModel.class);

        binding = FragmentPayingUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        payingUsersViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}