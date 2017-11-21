package myapp.doan.tuanchau.vn.trackingapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by tuanchau on 11/5/17.
 */

public class ListOnlineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView txtEmail;

    public void setItemClickListenener(ItemClickListenener itemClickListenener) {
        this.itemClickListenener = itemClickListenener;
    }

    ItemClickListenener itemClickListenener;



    public ListOnlineViewHolder(View itemView) {
        super(itemView);
        txtEmail = itemView.findViewById(R.id.txt_email);
        itemView.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        itemClickListenener.onClick(view,getAdapterPosition());

    }
}
