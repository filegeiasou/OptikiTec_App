package com.example.first_androidstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity
{
    private EditText editTextUsername;
    private EditText editTextPassword;

    private String username;
    private String password;

    private String response;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize text fields
        editTextUsername = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);

        // Add text fields to text watcher listener
        editTextUsername.addTextChangedListener(textWatcher);
        editTextPassword.addTextChangedListener(textWatcher);

        // Initialize button
        button = findViewById(R.id.loginButton);

        // Set button listener to change activity
//        button.setOnClickListener(view -> openMainActivity());
        // The code below is the same as the code above
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendData(view);
            }
        });
    }

    private final TextWatcher textWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            username = editTextUsername.getText().toString().trim();
            password = editTextPassword.getText().toString().trim();

            button.setEnabled(!username.isEmpty() && !password.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void sendData(View view)
    {
        SocketClient socketClient = new SocketClient();
        socketClient.execute(username, password);
    }

    // AAAAAAAAAAAAAAAAAAAAAA
    // You have to adjust AsyncTask<String, Void, String> to your needs. So if we want the 3rd parameter to return a String like in this example,
    // we have to change the 3rd parameter to String.
    class SocketClient extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... strings)
        {
//            Log.d("INFO", "doInBackground: " + strings[0] + " " + strings[1]);
            try
            {
                Socket socket = new Socket("10.0.2.2", 30000);

                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                output.write(strings[0] + "\n");
                output.write(strings[1] + "\n");
                output.flush();

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                response = input.readLine();

                output.close();
                input.close();

                // IMPORTANT: Close the socket after you are done with it.
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.equals("true"))
            {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        }
    }
}