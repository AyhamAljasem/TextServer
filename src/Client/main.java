package Client;

import Basic.Connection;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class main {


    public static void main(String[] args) {
        for (int i=0;i<100;i++)
        {
            clients();
        }

    }
    public static void clients()
    {
        Socket socket=null;
        try {
            socket=new Socket(Connection.IP,Connection.PORT);
            DataInputStream inputStream=new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream=new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF(( new Random().nextInt())+" oops");
            outputStream.writeUTF("testW");
            outputStream.writeUTF("Written from Client");
            outputStream.writeUTF("Exit");
            try{
            System.out.println(inputStream.readUTF());}
            catch (Exception e)
            {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
