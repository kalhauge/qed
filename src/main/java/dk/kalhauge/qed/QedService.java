package dk.kalhauge.qed;

import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class QedService {
  private static final int PORT = 4711;
  private final File root;
  private HttpServer server;

  public QedService(Object root, Object facade) throws IOException {
    if (root instanceof File) this.root = (File)root;
    else this.root = new File(root.getClass().getResource("/").getPath());
    this.server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
    server.createContext("/", new FileHandler(this.root));
    server.createContext("/qed", new QedHandler(facade));
    System.out.println("QED Service listening on "+PORT);
    server.start();
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String command = in.readLine();
    while (command != null) {
      if("exit".equals(command)) break;
      command = in.readLine();
      }
    server.stop(0);
    }
  
  public void stop() {
    server.stop(0);
    }
  
  }
