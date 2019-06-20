package com.example.chat.client;

import com.example.network.TCPConnection;
import com.example.network.TCPConnectionListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static String IP_ADDR;
    private static int PORT;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 500;
    private static String nick;
    public static ClientWindow This;
    public static JButton but;
    public static boolean flag = false;

    public static void main(String[] args) throws ParserConfigurationException, TransformerException, IOException, SAXException {

        File file = new File("params.xml");
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = f.newDocumentBuilder();
        Document document=b.parse(file);
        Element root = (Element) document.getElementsByTagName("root").item(0);
        PORT =Integer.valueOf(root.getAttribute("PORT"));
        IP_ADDR =root.getAttribute("IP");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                This=new ClientWindow();
            }
        });
    }

    /*For first window*/
    private final JTextField fieldNickname = new JTextField("Guest");
    private final JLabel nicknameLabel=new JLabel("Nickname:");
    private final JTextField fieldPass = new JTextField("");
    private final JLabel passLabel=new JLabel("Passwd:");

    /*For second window*/
    private final JTextArea log = new JTextArea();
    private final JTextField fieldInput = new JTextField(20);
    private final JLabel inputLabel=new JLabel("Message:");



    private TCPConnection connection;

    private ClientWindow(){

        /*------First Window-----------*/

        setTitle("Login");
        setLayout(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);


        but = new JButton("Login");
        ActionListener loginListener = new LoginListener();
        but.addActionListener(loginListener);


        nicknameLabel.setBounds(10,1,70,20);
        fieldNickname.setBounds(75, 1, 200, 20);
        passLabel.setBounds(10,30,70,20);
        fieldPass.setBounds(75, 30, 200, 20);
        but.setBounds(490,420,80,30);
        add(nicknameLabel);
        add(fieldNickname);
        add(passLabel);
        add(fieldPass);
        add(but);
        setResizable(false);
        setVisible(true);


    };

    /*For sending message via Enter*/
    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if(msg.equals("")||msg.startsWith("[Service]")) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickname.getText()+": "+msg);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection set!");
        connection.sendString("[Service]"+fieldNickname.getText()+"#"+fieldPass.getText());
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        if(value.equals("[Service]ALLOWED")){

        }
        else if(value.equals("[Service]FORBIDDEN")){
            tcpConnection.disconnect();
            System.exit(0);
        }
        else
        printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection done for.");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) { ;
        printMsg("Connection exception");
    }

    private synchronized void printMsg(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg+"\n\r");
              //  log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    /*For sending message via Button*/
    public class SendActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String msg = fieldInput.getText();
            if(msg.equals("")||msg.startsWith("[Service]")) return;
            fieldInput.setText(null);
            connection.sendString(fieldNickname.getText()+": "+msg);
        }
    }


    /*After login*/
    public class LoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(fieldNickname.getText()!=null)
            nick = fieldNickname.getText();




            /*------Second Window----------*/
            fieldNickname.setEditable(false);
            fieldNickname.setBorder(BorderFactory.createEmptyBorder());
            remove(but);
            setTitle("ChatRoom");
            Font font = new Font("Verdana", Font.BOLD, 15);
            log.setFont(font);
            log.setEditable(false);
            log.setLineWrap(true);
            log.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));



            ActionListener actionListener = new SendActionListener();
            fieldInput.addActionListener(actionListener);
            fieldInput.setPreferredSize(new Dimension(70,35));
            Font font2 = new Font("Times New Roman", Font.PLAIN, 15);
            fieldInput.setFont(font2);

            inputLabel.setBorder(BorderFactory.createEmptyBorder(0,0,45,0));


            /*SendMessage button*/
            JButton button = new JButton("Send");
            button.addActionListener(actionListener);

            /*Positioning*/
            Border border = BorderFactory.createLineBorder(Color.BLACK);


            log.setBounds(10, 28, 560,370);
            log.setBorder(border);

            JScrollPane sp = new JScrollPane(null,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.setViewportView(log);
            //sp.setPreferredSize(new Dimension(500, 500));
            sp.setBounds(10, 28, 560,370);
            sp.setWheelScrollingEnabled(true);

            inputLabel.setBounds(10,420,70,30);
            inputLabel.setBorder(BorderFactory.createEmptyBorder());
            fieldInput.setBounds(75,420,415,30);
            fieldInput.setBorder(border);
            button.setBounds(490,420,80,30);


            //add(log);
            add(sp);


            add(inputLabel);
            add(fieldInput);
            add(button);


            setResizable(false);
            setVisible(true);

            /*Making connection*/
            try {
                connection = new TCPConnection(This, IP_ADDR, PORT);
            } catch (IOException ee) {
                printMsg("Connection exception");
            }


        }
    }

}
