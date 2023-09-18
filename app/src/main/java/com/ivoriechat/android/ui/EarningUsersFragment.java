package com.ivoriechat.android.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ivoriechat.android.R;
import com.ivoriechat.android.database.VerifiedUser;
import com.ivoriechat.android.databinding.FragmentEarningUsersBinding;
import com.ivoriechat.android.database.VerifiedUserListViewModel;
import com.ivoriechat.android.utils.UserAdapter;

import java.util.List;

public class EarningUsersFragment extends Fragment {

    private FragmentEarningUsersBinding binding;

    private OnRetrievalServiceListener mListener;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private UserAdapter mAdapter;
    private List<VerifiedUser> mUsers;
    private RecyclerView.LayoutManager mLayoutManager;

    private LinearLayout mEmptyListLayout;

    private static final String TAG = "EarningUsersFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        VerifiedUserListViewModel userListViewModel =
                new ViewModelProvider(this).get(VerifiedUserListViewModel.class);

        binding = FragmentEarningUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mListener.onRetrieveVerifiedUsers();
            mSwipeRefreshLayout.setRefreshing(false);
        });

        mEmptyListLayout = root.findViewById(R.id.recycler_view_empty_layout);

        Observer<List<VerifiedUser>> usersObserver = users -> {
            Log.i(TAG, "Dataset has changed: " + users.size());
            mUsers = users;
            // mAdapter.swap(serviceRequests);
            mAdapter = new UserAdapter(users, getActivity());
            // mAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(mAdapter);

            if (users.size() == 0) {
                mEmptyListLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mEmptyListLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        };

        userListViewModel.getVerifiedUserList().observe(getViewLifecycleOwner(), usersObserver);

        mRecyclerView = root.findViewById(R.id.earning_users_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new UserAdapter(mUsers, getActivity());
        // mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRetrievalServiceListener) {
            mListener = (OnRetrievalServiceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRetrievalServiceListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnRetrievalServiceListener {
        void onRetrieveVerifiedUsers();
    }
}

