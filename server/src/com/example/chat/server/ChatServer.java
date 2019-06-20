package com.example.chat.server;

import com.example.network.TCPConnection;
import com.example.network.TCPConnectionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class ChatServer implements TCPConnectionListener {

    public static final int MAX_CON = 10;
    ArrayList<String>tmp = new ArrayList();

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private ChatServer(){
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(1234)){
            while(true){
                try{
                    new TCPConnection(this, serverSocket.accept());
                } catch(IOException e){
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        if(connections.size()<MAX_CON)
            connections.add(tcpConnection);
        else
            failCallBack(tcpConnection);

        for(String a : tmp){
            if(!a.startsWith("Client connected "))
                tcpConnection.sendString(a);
        }


        //sendToAllConnections(tcpConnection,"Client connected: " + tcpConnection);


    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        if(value.startsWith("[Service]")){
            /**/
            value=value.substring(9,value.length());
            System.out.println("GOT VALUE:"+value+"\r\n");
            String nickname=value.split("#")[0];
            System.out.println(nickname+"\r\n");
            String pass=value.split("#")[1];
            System.out.println(pass+"\r\n");

            File file = new File("file.txt");
            Boolean flag=false;
            try {
                FileReader fileReader=new FileReader(file);
                ArrayList<String> contacts = new ArrayList<String>();
                BufferedReader reader = new BufferedReader(fileReader);
                while(reader.ready()){
                    contacts.add(reader.readLine());
                }
                for(int i=0;i<contacts.size()-1;i+=2){
                    if(contacts.get(i).equals(nickname) && contacts.get(i+1).equals(pass)){
                        flag=true;
                    }
                }

                if(flag){
                    tcpConnection.sendString("[Service]ALLOWED");
                    flag=false;
                }else{
                    tcpConnection.sendString("[Service]FORBIDDEN");
                    tcpConnection.disconnect();
                }

            }catch(IOException e){e.printStackTrace();}
            /**/
        }
            else
        sendToAllConnections(tcpConnection, value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        //sendToAllConnections(tcpConnection, "Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnections(TCPConnection tcpConnection,String value){
        System.out.println(value);
        final int cnt = connections.size();
        if(tmp.size()<=10){tmp.add(value);}
        else{
            tmp.set(1,tmp.get(2));
            tmp.set(2,tmp.get(3));
            tmp.set(3,tmp.get(4));
            tmp.set(4,tmp.get(5));
            tmp.set(5,tmp.get(6));
            tmp.set(6,tmp.get(7));
            tmp.set(7,tmp.get(8));
            tmp.set(8,tmp.get(9));
            tmp.set(9,tmp.get(10));
            tmp.set(10,value);
        }


        for(int i = 0; i < cnt; i++){
                connections.get(i).sendString(value);
        }

    }



    private void failCallBack(TCPConnection tcpConnection){
            tcpConnection.sendString("Room is full! Try later");
    }

}
