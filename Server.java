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
        this.setSize(700, 480);
        this.setResizable(false);
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
            logMessage("[SERVER STATUS] Server started");

            try
            {
                serverSocket = new ServerSocket(30000);

                Thread serverThread = new Thread(() -> {
                    while (!serverSocket.isClosed()) {
                        try
                        {
                            Socket clientSocket = serverSocket.accept();
                            ServerThread thread = new ServerThread(clientSocket, this);
                            thread.start();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
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

            try
            {
                if(serverSocket != null && !serverSocket.isClosed())
                {
                    serverSocket.close();
                    logMessage("[SERVER STATUS] Server stopped");
                }
            }
            catch (IOException e)
            {
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