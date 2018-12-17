package com.organizers_group.whack_a_mole.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.organizers_group.whack_a_mole.R;
import com.organizers_group.whack_a_mole.levels.EndLevel;
import com.organizers_group.whack_a_mole.levels.Level;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.jar.Attributes;

public class AuthenticationActivity extends VolumeControlActivity {
    private Button new_user;
    private Button next;
    Level level;
    int score;
    String Name;
    String Age;
    String Email;
    private RequestQueue requestQueue;
    private EditText name;
    private EditText age;
    private EditText email;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);


        requestQueue = Volley.newRequestQueue(this);
        getData();
        progressDialog = new ProgressDialog(this);
        name = findViewById(R.id.nameEt);
        age = findViewById(R.id.ageEt);
        email = findViewById(R.id.emailEt);
        score = getIntent().getExtras().getInt("score");
        level = (Level) getIntent().getExtras().getSerializable("level");
        Button current_user = findViewById(R.id.currentUser);
        new_user = findViewById(R.id.newUser);
        next = findViewById(R.id.next);
        current_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getSharedPreferences("ID", MODE_PRIVATE);
                String restoredId = prefs.getString("user_id", null);
                if (restoredId == null){
                    Toast.makeText(AuthenticationActivity.this, " No Current User Login As New User", Toast.LENGTH_LONG).show();
                } else  {
                    Intent intent = new Intent(AuthenticationActivity.this, EndLevel.class);
                    intent.putExtra("score", score);
                    intent.putExtra("level", level);
                    startActivity(intent);
                }


            }
        });

       new_user.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               name.setText("");
               age.setText("");
               email.setText("");
             new_user.setVisibility(View.GONE);
             next.setVisibility(View.VISIBLE);
           }
       });

       next.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               check();
             progressDialog.setMessage("Loading...");
             progressDialog.show();
               postData();

           }
       });
        SharedPreferences prefs = getSharedPreferences("ID", MODE_PRIVATE);
        String restoredId = prefs.getString("user_id", null);
        if (restoredId == null){
            new_user.setVisibility(View.GONE);
            next.setVisibility(View.VISIBLE);
        }
        email.requestFocus();
          enter();
    }

    public void check() {
        String UserEmail = email.getText().toString();
        String UserName = name.getText().toString();
        String UserAge = age.getText().toString();

        if (TextUtils.isEmpty(UserEmail)) {
            email.setError("Can't Be Empty");
        }  else if (TextUtils.isEmpty(UserName)) {
            name.setError("Can't Be Empty");
        } else if (TextUtils.isEmpty(UserAge)) {
            age.setError("Can't Be Empty");
        }


    }

  //todo send data to ahmed
    public void postData() {
        String URL = "http://cartaman.com/game/wp-json/org/v1/user_son";
        Name = name.getText().toString();
        Age = age.getText().toString();
        Email = email.getText().toString();

        if (Email.indexOf("@")>1 && Email.indexOf("@")<Email.indexOf(".")&&Email.indexOf(".")<Email.indexOf("com")) {

            JSONObject json = new JSONObject();
            try {
                json.put("email", Email);
                json.put("name", Name);
                json.put("age", Age);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // post API
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(AuthenticationActivity.this, EndLevel.class);
                            intent.putExtra("score", score);
                            intent.putExtra("level", level);
                            startActivity(intent);
                            try {
                                String userId = response.getString("id");
                                SharedPreferences.Editor editor = getSharedPreferences("ID", MODE_PRIVATE).edit();
                                editor.putString("user_id", userId);
                                editor.apply();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                            if ((cm != null ? cm.getActiveNetworkInfo() : null) == null) {
                                Toast.makeText(AuthenticationActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
                            }
                        }
                    }) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    try {
                        Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                        if (cacheEntry == null) {
                            cacheEntry = new Cache.Entry();
                        }
                        final long cacheHitButRefreshed = 1000; // in 1 second cache will be hit, but also refreshed on background
                        final long cacheExpired = 5 * 1000; // in 5 seconds this cache entry expires completely
                        long now = System.currentTimeMillis();
                        final long softExpire = now + cacheHitButRefreshed;
                        final long ttl = now + cacheExpired;
                        cacheEntry.data = response.data;
                        cacheEntry.softTtl = softExpire;
                        cacheEntry.ttl = ttl;
                        String headerValue;
                        headerValue = response.headers.get("Date");
                        if (headerValue != null) {
                            cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                        }
                        headerValue = response.headers.get("Last-Modified");
                        if (headerValue != null) {
                            cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                        }
                        cacheEntry.responseHeaders = response.headers;
                        final String jsonString = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers));
                        return Response.success(new JSONObject(jsonString), cacheEntry);
                    } catch (UnsupportedEncodingException | JSONException e) {
                        return Response.error(new ParseError(e));
                    }
                }
            };

            jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) {

                }
            });
            requestQueue.add(jsonObjectRequest);
        }else {
            progressDialog.dismiss();
            Toast.makeText(this, "Email Not Correct", Toast.LENGTH_LONG).show();
        }
    }

    public void enter(){
        email.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == 66) {
                    name.requestFocus();
                }
                return false;
            }
        });

        name.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == 66) {
                    age.requestFocus();
                }
                return false;
            }
        });

    }

    public void getData(){

        String Url = "http://cartaman.com/game/wp-json/org/v1/get_user/";

        SharedPreferences prefs = getSharedPreferences("ID", MODE_PRIVATE);
        String restoredId = prefs.getString("user_id", null);
        if (restoredId != null) {
            String UserId = prefs.getString("user_id", "No name defined");//"No name defined" is the default value.

            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, Url + UserId,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String user_email = response.getString("email");
                                String user_name = response.getString("name");
                                String user_age = response.getString("age");

                                email.setText("Email:"+" "+ user_email);
                                name.setText("Name:"+" "+ user_name);
                                age.setText("Age:"+" "+ user_age);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                    assert cm != null;
                    if (cm.getActiveNetworkInfo() == null){
                        Toast.makeText(AuthenticationActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
                    }

                }
            })
            {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    try {
                        Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                        if (cacheEntry == null) {
                            cacheEntry = new Cache.Entry();
                        }
                        final long cacheHitButRefreshed = 1000; // in 1 second cache will be hit, but also refreshed on background
                        final long cacheExpired = 5 * 1000; // in 5 seconds this cache entry expires completely
                        long now = System.currentTimeMillis();
                        final long softExpire = now + cacheHitButRefreshed;
                        final long ttl = now + cacheExpired;
                        cacheEntry.data = response.data;
                        cacheEntry.softTtl = softExpire;
                        cacheEntry.ttl = ttl;
                        String headerValue;
                        headerValue = response.headers.get("Date");
                        if (headerValue != null) {
                            cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                        }
                        headerValue = response.headers.get("Last-Modified");
                        if (headerValue != null) {
                            cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                        }
                        cacheEntry.responseHeaders = response.headers;
                        final String jsonString = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers));
                        return Response.success(new JSONObject(jsonString), cacheEntry);
                    } catch (UnsupportedEncodingException | JSONException e) {
                        return Response.error(new ParseError(e));
                    }
                }
            };

            objectRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) {

                }
            });
            requestQueue.add(objectRequest);
        }
    }
}
