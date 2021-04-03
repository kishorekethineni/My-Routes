package com.kishorekethineni.myroutes.Activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.kishorekethineni.myroutes.Adapters.RouteAdapter;
import com.kishorekethineni.myroutes.Database.DBHelper;
import com.kishorekethineni.myroutes.Models.RouteModel;
import com.kishorekethineni.myroutes.R;

import java.util.List;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, MainActivity.class));
            }
        });

        new fetchTask().execute();
    }

    private class fetchTask extends AsyncTask<String,String, List<RouteModel>> {

        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd=new ProgressDialog(Home.this);
            pd.setMessage("Fetching...");
            pd.show();
        }

        @Override
        protected List<RouteModel> doInBackground(String... strings) {
            DBHelper db=new DBHelper(Home.this);
            return db.getRoutes();
        }

        @Override
        protected void onPostExecute(List<RouteModel> s) {
            super.onPostExecute(s);
            RouteAdapter adapter=new RouteAdapter(s,Home.this);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Home.this);
            RecyclerView recyclerView=findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            pd.dismiss();
        }
    }

}