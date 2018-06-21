package com.inzv1.arcturuspiotrek.inzv1;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    Button button_submit;
    Button button_continue;
    Button button_register;
    EditText editTextLogin;
    EditText editTextPassword;
    String login;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        button_submit = (Button) findViewById(R.id.buttonLoginSubmit);
        button_continue = (Button) findViewById(R.id.buttonContinue);
        button_register = (Button) findViewById(R.id.buttonRegister);
        editTextLogin = (EditText) findViewById(R.id.editTextLogin);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPassword.setTransformationMethod(new PasswordTransformationMethod()); //normalna czcionka w HINT


        button_submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button_submit.setClickable(false);
                button_submit.setBackgroundColor(Color.parseColor("#bfbfbf")); //jasnoszary
                login = editTextLogin.getText().toString();
                password = editTextPassword.getText().toString();
                RESTPostLogin(login, password, new MainActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try{
                            JSONObject res = new JSONObject(result);
                            int userRef =  res.getInt("userRef");
                            String login = res.getString("login");

                                Toast.makeText(LoginActivity.this, "Hello "+login+"!"+userRef, Toast.LENGTH_SHORT).show();
                                Intent main = new Intent(LoginActivity.this, MainActivity.class);
                                main.putExtra("userRef", userRef);
                                main.putExtra("login", login);
                                LoginActivity.this.startActivity(main);

                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this,"catch chyba sie nie udalo przekonwertowac na jsonob", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }


                    }
                });

            }
        });

        button_continue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent main = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(main);
            }
        });


        button_register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse("http://89.76.174.133:3000/register");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


    }






    private void RESTPostLogin(final String login, final String password, final MainActivity.VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://89.76.174.133:3000/rest/login";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                            //Toast.makeText(LoginActivity.this,"ResponseLOGIN: "+ response, Toast.LENGTH_SHORT).show(); // WIADOMOSC MSG Z REST.JS
                            //Toast.makeText(LoginActivity.this,"You have been successfully logged in!", Toast.LENGTH_SHORT).show();
                            callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        button_submit.setClickable(true);
                        button_submit.setBackgroundColor(Color.parseColor("#79ff4d")); //zielony
                        Toast.makeText(LoginActivity.this,"Incorrect credentials!", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(LoginActivity.this,"ErrorLOGIN: "+  error.getMessage(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getBaseContext(),"Error Response", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("login", login);
                params.put("password", password);
                return params;
            }
        };
        //Toast.makeText(getBaseContext(), "w funkji przed queue", Toast.LENGTH_SHORT).show();
        queue.add(postRequest);
        //Toast.makeText(getBaseContext(), "w funkji po queue", Toast.LENGTH_SHORT).show();

    }
}
