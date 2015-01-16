package dk.kalhauge.qed;

import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class QedService implements AutoCloseable {
  private static final int PORT = 4711;
  private File root;
  private final HttpServer server;

  public QedService(Object facade) throws IOException {
    this.root = new File(getClass().getResource("/").getPath());
    this.server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
    
    server.createContext("/", new FileHandler(this.root));
    server.createContext("/qed", new QedHandler(facade));
    System.out.println("QED Service listening on "+PORT);
    }
  
  public QedService root(File root) {
    this.root = root;
    return this;
    }
  
  public QedService root(String path) {
    root = new File(path);
    return this;
    }
  
  public void console() throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    do {
      System.out.print("QED> ");
      String command = in.readLine();
      if("exit".equals(command)) break;
      }
    while (true);
    }
  
  public QedService start() {
    server.start();
    return this;
    }
  
  public void stop() {
    server.stop(0);
    }
  
  @Override
  public void close() {
    server.stop(0);
    }
  
  }
