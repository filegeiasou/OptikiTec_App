package com.example.first_androidstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import java.net.SocketException;

public class LoginActivity extends AppCompatActivity
{
    private EditText editTextUsername;
    private EditText editTextPassword;

    private String username;
    private String password;

    private String response;
    private Button button;

    /**
     * Called when the activity is starting.  This is where most initialization
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize text fields
        // MHN VALEIS TO LAYOUT ID STA FINDVIEWBYID
        // VALE TA TEXT FIELDS
        editTextUsername = findViewById(R.id.usernameEditText);
        editTextPassword = findViewById(R.id.passwordEditText);

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

        // Overridden method to enable/disable button based on text field input
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

    /**
     * Called when the user taps the Send button
     * @param view
     */
    public void sendData(View view)
    {
        SocketClient socketClient = new SocketClient();
        socketClient.execute(username, password);
    }

    // AAAAAAAAAAAAAAAAAAAAAA
    // You have to adjust AsyncTask<String, Void, String> to your needs. So if we want the 3rd parameter to return a String like in this example,
    // we have to change the 3rd parameter to String.

    /**
     * AsyncTask is a generic class, it uses 3 types: AsyncTask<1, 2, 3>
     *     1: The type of the parameters sent to the task upon execution.
     *     2: The type of the progress units published during the background computation.
     *     3: The type of the result of the background computation.
     */
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

            }
            catch (IOException e)
            {
                e.printStackTrace();

            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
//            if(errorMessage != null)
//            {
//                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
//            }
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