package com.ivoriechat.android.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ivoriechat.android.R;
import com.ivoriechat.android.database.VerifiedUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.CustomViewHolder> {

    private LayoutInflater mInflater; // Stores the layout inflater
    private List<VerifiedUser> mDataset;
    private Context mContext;

    public UserAdapter(List<VerifiedUser> dataset, Context context) {
        super();
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mDataset = dataset;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLayout =
                mInflater.inflate(R.layout.item_user, parent, false);

        // Creates a new ViewHolder in which to store handles to each view resource. This
        // allows bindView() to retrieve stored references instead of calling findViewById for
        // each instance of the layout.
        CustomViewHolder holder = new CustomViewHolder(itemLayout);
        holder.userIconImageView = itemLayout.findViewById(R.id.user_icon);
        holder.userNameTextView = itemLayout.findViewById(R.id.user_name_text);
        holder.profileTextView = itemLayout.findViewById(R.id.profile_text);
        holder.hourlyRateTextView = itemLayout.findViewById(R.id.hourly_rate_text);

        // Stores the resourceHolder instance in itemLayout. This makes resourceHolder
        // available to bindView and other methods that receive a handle to the item view.
        itemLayout.setTag(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        VerifiedUser user = mDataset.get(position);
        holder.userNameTextView.setText(user.getUserRealName());
        holder.profileTextView.setText(user.getProfile());
        holder.hourlyRateTextView.setText(user.getHourlyRate().setScale(0).toString());

        String userIconURL = user.getIconFilePath();
        if (userIconURL == null)
            return;

        RoundedCornersTransform picassorTransform = new RoundedCornersTransform();
        // 设置round corner的radius
        picassorTransform.setRadius(72);

        Picasso.get()
                //.with(mContext)
                .load(userIconURL)
                .resize(144, 144)
                .centerCrop() // Crops an image inside of the bounds specified by resize(int, int) rather than distorting the aspect ratio.
                .transform(picassorTransform)
                .into(holder.userIconImageView);

    }

    @Override
    public int getItemCount() {
        if (mDataset == null) {
            return 0;
        }

        return mDataset.size();
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView userIconImageView;
        public TextView userNameTextView;
        public TextView profileTextView;
        public TextView hourlyRateTextView;
        public CustomViewHolder(View itemView) {
            super(itemView);
        }
    }
}
