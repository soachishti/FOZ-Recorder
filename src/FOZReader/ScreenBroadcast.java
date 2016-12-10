package FOZReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class MyIndexHandler implements HttpHandler {
	public void handle(HttpExchange t) throws IOException {
		
		String response = new Scanner(new File("template/index.html")).useDelimiter("\\Z").next();
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}

class MyImageHandler implements HttpHandler {
	String tmpDir;
	
	public MyImageHandler(String dir) {
		tmpDir = dir;
	}
	
	public void handle(HttpExchange t) throws IOException {
		Headers h = t.getResponseHeaders();
		h.add("Content-Type", "image/jpg");

		File file = new File (tmpDir + "current.jpg");
		byte [] bytearray  = new byte [(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(bytearray, 0, bytearray.length);
		
		t.sendResponseHeaders(200, file.length());
		OutputStream os = t.getResponseBody();
		os.write(bytearray,0,bytearray.length);
		os.close();
	}
}

public class ScreenBroadcast {
	int port = 7881;
	String address;
	HttpServer server;
	String tmpDir;
	
	public void setAddress(String a) {
		address = a;
	}
	
	public void setPort(int p) {
		port = p;
	}
	
	public void setTmpDir(String tmp) {
		tmpDir = tmp;
	}
	
	public String getIPAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress() + ":" + port;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void start() {
		System.out.println("Started");
		try {
			//server = HttpServer.create(new InetSocketAddress(new Inet4Address(address), port), 0);
			server = HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server.createContext("/", new MyIndexHandler());
		server.createContext("/image.jpg", new MyImageHandler(tmpDir));
		server.setExecutor(null); // creates a default executor
		server.start();
	}
	
	public void stop() {
		server.stop(0);
	}
}

