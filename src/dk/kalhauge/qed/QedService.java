package dk.kalhauge.qed;

import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/** The QedService setup a quick HttpServer. To start one
 * quickly use the command:
 *
 *      QedService.fromFacade(facade).start().readCommandsAndStop();
 *
 */
public class QedService {
  private static final Logger log = Logger.getLogger(QedService.class.getName());

  private static final int PORT = 4711;
  private final File root;
  private final Object facade;
  private HttpServer server;

  public QedService(File root, Object facade) {
    this.root = root;
    this.facade = facade;
    }

  public static QedService fromFacade(Object facade) {
      return new QedService(
        new File(facade.getClass().getResource("/").getPath()),
        facade
        );
    }

  public QedService stop() {
    server.stop(0);
    this.server = null;
    log.fine("Stopped QedService");
    return this;
    }

  public QedService start(InetSocketAddress addr) throws IOException {
    log.fine("Starting QedService");
    server = HttpServer.create(addr, 0);
    HttpServer server = HttpServer.create(addr, 0);
    server.createContext("/", new FileHandler(root));
    server.createContext("/qed", new QedHandler(facade));
    server.start();
    log.fine("Started QedService");
    return this;
    }

  public QedService start() throws IOException {
    return start(new InetSocketAddress("localhost", PORT));
    }

  public QedService console() throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String command = in.readLine();
    while (command != null) {
      if("exit".equals(command)) break;
      command = in.readLine();
      }
    return stop();
    }
  
  
  }
