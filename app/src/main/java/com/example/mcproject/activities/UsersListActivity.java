package com.example.mcproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import com.example.mcproject.R;
import com.example.mcproject.activities.adapters.UserAdapter;
import com.example.mcproject.databinding.ActivityUsersListBinding;
import com.example.mcproject.activities.Users;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UsersListActivity extends AppCompatActivity implements UserListener {
    String Current_ID;
    String User_ID;
    String Type;

    private ActivityUsersListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent users = getIntent();
        Current_ID = users.getStringExtra("Current_ID");
        User_ID = users.getStringExtra("User_ID ");
        Type = users.getStringExtra("Type");
        chatListener(User_ID);
        if (Type.equals("Counsellor")) {

            OkHttpClient client = new OkHttpClient();

            HttpUrl.Builder url_builder = HttpUrl.parse("https://lamp.ms.wits.ac.za/~s2465557/load_client_chats.php?").newBuilder();
            url_builder.addQueryParameter("Counsellor_ID", Current_ID);
            //need to send request variables/post parameters of datetime in this format from android
            String url = url_builder.build().toString();

            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) { //possible difference to mcproject signin
                        final String json = response.body().string();
                        UsersListActivity.this.runOnUiThread(new Runnable() { //possible difference to mcproject signin activity vs main activity here
                            @Override
                            public void run() {
                                try {
                                    processJSONCounsellor(json);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
        }else
        {
            OkHttpClient client = new OkHttpClient();

            HttpUrl.Builder url_builder = HttpUrl.parse("https://lamp.ms.wits.ac.za/~s2465557/load_counsellor_chat.php?").newBuilder();
            String Client_ID = "";
            url_builder.addQueryParameter("Client_ID",Current_ID);
            //need to send request variables/post parameters of datetime in this format from android
            String url = url_builder.build().toString();

            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()){ //possible difference to mcproject signin
                        final String json = response.body().string();
                        UsersListActivity.this.runOnUiThread(new Runnable() { //possible difference to mcproject signin activity vs main activity here
                            @Override
                            public void run() {
                                try {
                                    processJSONClient(json);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public void processJSONCounsellor(String json) throws JSONException {
        loading(true);
        //This is an array with only one element
        JSONArray jsonArray = new JSONArray(json);
        //extract each json object in the json array
        String all_client_details = "";
        List<Users> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String Client_username = jsonObject.getString("Client_name");
            String Chat_ID = jsonObject.getString("Chat_ID");
            String Last_Message = jsonObject.getString("Last_Message");

            Users Clients = new Users();
            Clients.name = Client_username;
            Clients.RecentMsg = Last_Message;
            Clients.Chat_ID = Chat_ID;
            users.add(Clients);
        }
        loading(false);
        if (users.size()>0){
                UserAdapter userAdapter = new UserAdapter(users,this);
                binding.usersRecyclerView.setAdapter(userAdapter);
                binding.usersRecyclerView.setVisibility(View.VISIBLE);
        }else {
            showErrorMsg();
        }

        //Store above values accordingly

    }

    public void processJSONClient(String json) throws JSONException {
        loading(true);
        //This is an array with only one element
        JSONArray jsonArray = new JSONArray(json);
        //extract each json object in the json array
        String all_counsellor_details;

        JSONObject jsonObject = jsonArray.getJSONObject(0);
        String Counsellor_name = jsonObject.getString("Counsellor_name");
        String Chat_ID = jsonObject.getString("Chat_ID");
        String Last_Message = jsonObject.getString("Last_Message");

        loading(false);

        List<Users> users = new ArrayList<>();
        Users Counsellor = new Users();
        Counsellor.name = Counsellor_name;
        Counsellor.RecentMsg = Last_Message;
        Counsellor.Chat_ID = Chat_ID;
        users.add(Counsellor);
        if (users.size()>0){
            UserAdapter userAdapter = new UserAdapter(users, this);
            binding.usersRecyclerView.setAdapter(userAdapter);
            binding.usersRecyclerView.setVisibility(View.VISIBLE);
        }else {
              showErrorMsg();
        }

        //Store above values accordingly
    }

    private void showErrorMsg(){
        binding.ErrorMessage.setText(String.format("%s","No user available"));
        binding.ErrorMessage.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    private void chatListener(String User_ID){

        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onClickUsers(Users users) {
        Intent chat = new Intent(UsersListActivity.this, ChatActivity.class);
        chat.putExtra("User_ID", users);
        startActivity(chat);
        finish();
    }
}