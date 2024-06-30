package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PersonViewHolder> {
    private ArrayList<HashMap<String, String>> personList;

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewUsername;
        public TextView textViewId;
        public Button buttonDelete;

        public PersonViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewId = itemView.findViewById(R.id.textViewId);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }

    public PersonAdapter(ArrayList<HashMap<String, String>> personList) {
        this.personList = personList;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_item, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, @SuppressLint("RecyclerView") int position) {
        HashMap<String, String> currentItem = personList.get(position);
        holder.textViewUsername.setText("username: " + currentItem.get("username"));
        holder.textViewId.setText("id: " + currentItem.get("id"));

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get username and id
                String username = currentItem.get("username");
                String id = currentItem.get("id");

                // Remove from the list
                personList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, personList.size());

                // Call delete function
                deleteUserFromServer(username, id, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteUserFromServer(String username ,String id, View view) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL("https://app.the-safe-zone.online/delete-user");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("id", id);

                    OutputStream os = connection.getOutputStream();
                    os.write(jsonParam.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = connection.getResponseCode();
                    connection.disconnect();
                    return responseCode == HttpURLConnection.HTTP_OK;

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                String message = success ? "User deleted successfully" : "Failed to delete user";
                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}
