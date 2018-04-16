package io.swagslash.settlersofcatan.network.wifi;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peak.salut.SalutDevice;

import java.util.List;

import io.swagslash.settlersofcatan.R;

/**

 * TODO: Replace the implementation with code for your data type.
 */
public class MyLobbyMemberRecyclerViewAdapter extends RecyclerView.Adapter<MyLobbyMemberRecyclerViewAdapter.ViewHolder> {

    private final List<SalutDevice> mValues;

    public MyLobbyMemberRecyclerViewAdapter(List<SalutDevice> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_lobbymember, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mIdView.setText(mValues.get(position).readableName);
        holder.mItem = mValues.get(position);
        //holder.mContentView.setText(mValues.get(position).deviceName);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public SalutDevice mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    public void setLobbies(List<SalutDevice> lobbies) {
        mValues.clear();
        mValues.addAll(lobbies);
        this.notifyDataSetChanged();
    }
    public void addMember(SalutDevice member){
        mValues.add(member);
        notifyDataSetChanged();
    }
}
