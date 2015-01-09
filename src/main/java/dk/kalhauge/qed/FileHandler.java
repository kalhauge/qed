package dk.kalhauge.qed;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileHandler implements HttpHandler {
  private final File root;
  
  public FileHandler(File root) {
    this.root = root;
    }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    File file = new File(root, exchange.getRequestURI().getPath());
    System.out.println("PATH: '"+file.getAbsolutePath()+"'");
    Headers headers = exchange.getResponseHeaders();
    headers.add("Content-Type", "text/html; charset=utf8");
    FileInputStream in = new FileInputStream(file);
    try (OutputStream out = exchange.getResponseBody()) {
      exchange.sendResponseHeaders(200, file.length());
      byte[] buffer = new byte[1024];
      int count;
      while ((count = in.read(buffer)) > 0) out.write(buffer, 0, count);
      }
    }
  
  }
