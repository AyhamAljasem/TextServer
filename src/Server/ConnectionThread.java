package Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConnectionThread {
    protected Socket socket;

    public ConnectionThread(Socket socket) {
        this.socket = socket;
    }
    public void run()
    {
        DataInputStream inputStream=null;
        String source=null;
        String fileName=null;
        String text=null;
        try {
            inputStream=new DataInputStream(socket.getInputStream());
            String recived;
            while (true)
            {
                recived=inputStream.readUTF();
                System.out.println(recived);
                if(recived==null||recived.equalsIgnoreCase("EXIT"))
                {
                    if(source!=null&&fileName!=null)
                    {
                        if(text==null)
                        {
                            view(fileName,new DataOutputStream(socket.getOutputStream()));
                        }
                        else {
                            edit(fileName,text);
                        }
                    }
                    else
                    {
                        System.out.println("Incomplete data");
                    }
                    socket.close();
                    return;
                }
                else{
                    if(source==null)
                    {
                        source=recived;
                    }
                    else if(fileName==null){
                        fileName=recived;
                    }
                    else if(recived!=null){
                        text=recived;
                    }

                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void view(String fileName,DataOutputStream outputStream)
    {
        File file= new File("TextFiles/"+fileName+".txt");
        if(!file.exists())
        {
            try {
                outputStream.writeUTF("File Not Found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            String text;
            try {
                text = new String(Files.readAllBytes(Paths.get("TextFiles/"+fileName+".txt")));
                outputStream.writeUTF(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void edit(String fileName,String text)
    {

    }


}
