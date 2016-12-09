package FOZReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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
	public void handle(HttpExchange t) throws IOException {
		Headers h = t.getResponseHeaders();
		h.add("Content-Type", "image/jpg");

		File file = new File ("current.jpg");
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

	public static void main(String[] args) throws IOException, InterruptedException {
		ScreenShot s = new ScreenShot();
		
		s.multiCapture("tmp/", 200);		
		
		
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/", new MyIndexHandler());
		server.createContext("/image.jpg", new MyImageHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}
}

