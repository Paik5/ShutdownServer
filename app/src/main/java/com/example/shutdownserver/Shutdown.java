package com.example.shutdownserver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class Shutdown extends Activity {
 
    //private Button shutdown;
    //private Button restart;
    //private Button check;
    private TextView tv1;
    private TextView tv2;
    private TimePicker tp;
    private int shut = 0;
    private boolean ping = false;
    private String received = "";
    private String hostname;
    private int port;
    private String mac;
    public SharedPreferences host;
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	getMenuInflater().inflate(R.menu.shutdown, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	Log.i("TAG", "Click check");
    	Intent intent = new Intent(this, Settings.class);
    	startActivity(intent);
    	return true;
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shutdown);

        //tp = (TimePicker) findViewById(R.id.timePicker1);
        //tp.setIs24HourView(true);
        tv1 = (TextView) findViewById(R.id.textView);
        tv2 = (TextView) findViewById(R.id.textView1);
        tv1.setText("OFF");
        ping = true;
        Thread aThread = new Thread(new ClientThread());
        aThread.start();
        host = getSharedPreferences("filename", 0);
    }
    
    public void onClickShutdown (View v)
    {
    	shut = 1;
    	Thread cThread = new Thread(new ClientThread());
    	cThread.start();
    }
    
    public void onClickRest (View v)
    {
    	shut = 2;
    	Thread cThread = new Thread(new ClientThread());
    	cThread.start();
    }

    public void onClickTime (View v)
    {
    	shut = 3;
    	tv2.setText((tp.getCurrentHour()) + ":" + (tp.getCurrentMinute()));
    	Thread cThread = new Thread(new ClientThread());
    	cThread.start();
    }
    
    public void onClickWake (View v) throws UnknownHostException
    {
    	shut = 4;   	
    	CharSequence text = getText(R.string.toast_wake);
    	Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
    	Toast toast = Toast.makeText(context, text, duration);
    	toast.show();
    	Thread cThread = new Thread(new ClientThread());
    	cThread.start();
    }
    
    public void onClickCheck (View v)
    {
    	shut = 0;
    	Thread cThread = new Thread(new ClientThread());
    	cThread.start();
    }
    
    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        byte[] bytes = new byte[6];
        String[] mac = macStr.split("(\\:|\\-)");
        if (mac.length != 6)
        {
            throw new IllegalArgumentException("Invalid MAC address...");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(mac[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit...");
        }
        return bytes;
    }
    
    public static byte[] wakeup(String mac) {
        if (mac == null) {
            return null;
        }

                byte[] macBytes = getMacBytes(mac);
                byte[] bytes = new byte[6 + 16 * macBytes.length];
                for (int i = 0; i < 6; i++) {
                    bytes[i] = (byte) 0xff;
                }
                for (int i = 6; i < bytes.length; i += macBytes.length) {
                    System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
                }
            	return bytes;
        }

    public static byte[] Enc (String text) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
    {
        //String text = "Hello World";
        String key = "Bar12345Bar12345"; // 128 bit key
        // Create key and cipher
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        // encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(text.getBytes());
        System.err.println(new String(encrypted));
        return encrypted;
   }
    
    public static String Dec (byte[] rec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
    	String key = "Bar12345Bar12345";
    	Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
    	 // decrypt the text
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        String decrypted = new String(cipher.doFinal(rec));
        System.err.println(decrypted);
    	return decrypted;
    }

   
    public class ClientThread implements Runnable {
    	 
        public void run() {
        	try {
                 	DatagramSocket socket = new DatagramSocket();
                        
                    // send request
                    byte[] bufr = new byte[256];
                    byte[] buf = new byte[256];
                    if(ping)
                    {
                    	buf = ("ping").getBytes();
                    }
                    
                    if(shut == 1)
                    {
                    	//buf = Enc("shutdown");
                      	buf = ("shutdown").getBytes();
                    }
                    if (shut == 2)
                    {
                       	buf = ("restart").getBytes();
                    }
                    if (shut == 3)
                    {
                       	buf = ("scheduled_" + (tp.getCurrentHour()) + ":" + (tp.getCurrentMinute())).getBytes();
                    }
                    if (shut == 4)
                    {
                    	mac = host.getString("mac", "1");
                    	buf = wakeup(mac); 
                    }
                    
                    hostname = host.getString("hostname", "hovno");
                    
                    if (shut != 4)
                    {
                    	port = Integer.parseInt(host.getString("port", "5555"));
                    }
                    else
                    {
                    	port = 9;
                    }
                    
                    InetAddress address = InetAddress.getByName(hostname);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                    
                    socket.send(packet);
                    
                    // get response
                    packet = new DatagramPacket(bufr, bufr.length);
                    socket.receive(packet);
              
                    // display response
                    received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(received);
                    socket.close();
                	                    
                	runOnUiThread(new Runnable() {
                        @Override
                        
                        
                        public void run() {
                        	if (received.equals(""))
                        	{
                        		tv1.setText("OFF");
                        	}
                        	else
                        	{
                        		tv1.setText(received);
                        	}
                        }
                        
                	});
                    buf = null;
                	Log.d("ClientActivity", "C: Closed.");
                	
                
            } catch (Exception e) {
                Log.e("ClientActivity", "C: Error", e);
            
            }
        }
    }
}
    
   

