package Server;

import Basic.Connection;
import Basic.Response;
import Encryp.Symmetric;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

public class main {
    public static Map<String, PublicKey> keys;
    public static void main(String[] args) {
       ServerSocket serverSocket=null;
        Socket socket=null;
        try{
            serverSocket=new ServerSocket(Connection.PORT);
            System.out.println("Server Online");

        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true)
        {
            System.out.println("Waiting for clients");
            try {
                socket=serverSocket.accept();
                System.out.println("Connected");
                ConnectionThread connectionThread=new ConnectionThread(socket);
                connectionThread.run();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
