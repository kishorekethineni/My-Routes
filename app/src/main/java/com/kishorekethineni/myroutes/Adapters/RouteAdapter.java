package com.kishorekethineni.myroutes.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kishorekethineni.myroutes.Activites.MapActivity;
import com.kishorekethineni.myroutes.Models.RouteModel;
import com.kishorekethineni.myroutes.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    Context activity;
    private final List<RouteModel> list;
    public RouteAdapter(List<RouteModel> list, Context activity) {
        this.list = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RouteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.list_routes, parent, false);
        return new RouteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RouteAdapter.ViewHolder holder, final int position) {
        final RouteModel data = list.get(position);
        holder.from.setText(data.getSource());
        holder.to.setText(data.getDestination());
        holder.address.setText("Not Available");
        holder.time.setText(data.getTimeStamp());

        holder.longPresslayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "preese", Toast.LENGTH_SHORT).show();
                Gson gson = new Gson();
                Intent intent = new Intent(activity, MapActivity.class);
                intent.putExtra("routeObject", gson.toJson(data));
                activity.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView from,to,time,address;
        ConstraintLayout longPresslayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            from = itemView.findViewById(R.id.from);
            to = itemView.findViewById(R.id.to);
            time = itemView.findViewById(R.id.time);
            address = itemView.findViewById(R.id.adress);
            longPresslayout = itemView.findViewById(R.id.longpresslayout);
        }
    }
}
