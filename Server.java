import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.io.*;
import java.net.*;
import javax.swing.*;


public class Server extends JFrame {
    private ServerSocket serverSocket;
    private JTextArea logArea;
    private final JButton startServerButton;
    private final JButton stopServerButton;
    private final JButton clearLogButton;
    private boolean isServerRunning = false;

    public Server() {
        super("OptikiTec Server");
        setLayout(null);

        startServerButton = new JButton("Start Server");
        startServerButton.setBounds(540, 20, 120, 40);
        startServerButton.addActionListener(event -> startServer());
        // startServerButton.addActionListener(this);

        stopServerButton = new JButton("Stop Server");
        stopServerButton.setBounds(540, 80, 120, 40);
        stopServerButton.setEnabled(false);
        stopServerButton.addActionListener(event -> stopServer());

        clearLogButton = new JButton("Clear Log");
        clearLogButton.setBounds(540, 140, 120, 40);
        clearLogButton.addActionListener(event -> logArea.setText(""));

        // Add buttons to the frame
        this.add(startServerButton);
        this.add(stopServerButton);
        this.add(clearLogButton);

        // Create Text Area for the log messages
        logArea = new JTextArea();
//        logArea.append("Server started\n");
        logArea.setEditable(false);
        logArea.setBackground(Color.DARK_GRAY);
        logArea.setForeground(Color.WHITE);

        // Create Scroll Pane for the Text Area to be scrollable
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBounds(10, 10, 500, 420);

        this.add(scrollPane);
        this.getContentPane().setBackground(Color.BLACK);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(700, 500);
        this.setVisible(true);

        // startServer();
        // boolean a = db("kont","1234");
    }

    /**
     * This method is used to start the server
     */
    public void startServer()
    {
        if (!isServerRunning)
        {
            isServerRunning = true;
            startServerButton.setEnabled(false);
            stopServerButton.setEnabled(true);
            logMessage("Server started");

            try
            {
                serverSocket = new ServerSocket(30000);

                Thread serverThread = new Thread(() -> {
                    while (!serverSocket.isClosed()) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            ServerThread thread = new ServerThread(clientSocket, this);
                            thread.start();
                        } catch (IOException e) {
                            // Handle the exception gracefully (e.g., log it)
                        }
                    }
                });
                serverThread.start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * This method is used to stop the server
     */
    public void stopServer()
    {
        if(isServerRunning)
        {
            isServerRunning = false;
            stopServerButton.setEnabled(false);
            startServerButton.setEnabled(true);

            try {
                if(serverSocket != null && !serverSocket.isClosed())
                {
                    serverSocket.close();
                    logMessage("Server stopped");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is used to log messages to the log area in our JFrame
     * @param message The message that we want to log
     */
    public void logMessage(String message)
    {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /**
     * Main method that creates a new Server object
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        // This is the same as SwingUtilities.invokeLater(() -> new Server());
        SwingUtilities.invokeLater(Server::new);
    }
}

class ServerThread extends Thread
{
    private final Socket clientSocket;
    private final Server server;
    private String username;

    /**
     * Constructor for the ServerThread class
     * @param clientSocket The client socket that the thread will be using
     * @param server We use this to pass a reference to the server object, so we can use it for the log method
     */
    public ServerThread(Socket clientSocket, Server server)
    {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    /**
     * This method is called when the thread starts and runs the code for the login
     */
    @Override
    public void run()
    {
        try
        {
            // Print a message when the thread starts to do some logging for the thread
//            System.out.println("Thread for client started: " + Thread.currentThread().getId());

            // Initialize BufferedReader that will read the login data (username, password) from the client socket
            BufferedReader loginData = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Initialize BufferedWriter that will send the result of the login check to the client socket
            BufferedWriter response = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String username = loginData.readLine();
            String password = loginData.readLine();

//            System.out.println(username + " " + password);

            // Check if the username and password are correct through the SQL query.
            boolean checkCredentials = checkCredentialsQuery(username,password);
            String ok = String.valueOf(checkCredentials);

            if(checkCredentials)
            {
                this.username = username;
                server.logMessage("User " + this.username + " logged in successfully");
                // System.out.println("Client with username " + this.username + " logged in successfully");
            }else
            {
                server.logMessage("User " + username + " failed to log in");
                // System.out.println("Client with username " + username + " failed to log in");
            }

            System.out.println(ok);

            response.write(ok);
            response.newLine();
            response.flush();

            // Print a message when the thread finishes to do some logging for the thread
//            System.out.println("Thread for client finished: " + Thread.currentThread().getId());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                clientSocket.close();

//                if(!clientSocket.isClosed())
                server.logMessage("User " + this.username + " disconnected");
//                System.out.println("Client with username " + this.username + " disconnected");
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method checks if the username and password are correct through an SQL query
     * @param username The username that the client sent and that we use in the query
     * @param password The password that the client sent and that we use in the query
     * @return Returns true if the username and password are correct, false otherwise
     */
    public boolean checkCredentialsQuery(String username , String password)
    {
        try
        {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/optikitec", "root", "root");

            // using a string for the SQL query
            String checkQuery = "SELECT * FROM users WHERE username = ? AND password = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next())
            {
                // JOptionPane.showMessageDialog(this, "This user already exists");
                //System.out.println("User with username " + username + " already exists");
                return true;
            }
            else
            {
                System.out.println("User with username " + username + " does not exist");
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
            return false;
        }
        catch (SQLIntegrityConstraintViolationException key)
        {
            System.out.println("Username already exists. Registration failed!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
