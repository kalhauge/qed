package dk.kalhauge.qed;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class QedHandler implements HttpHandler {
  public final JsonWrapper wrapper;
  
  public QedHandler(Object facade) {
    this.wrapper = new JsonWrapper(facade);
    }
  
  private void appendResource(StringBuilder result, String name) throws IOException {
    String path = getClass().getResource(name).getPath();
    try (BufferedReader in = new BufferedReader(new FileReader(path))) {
      String line = in.readLine();
      while (line != null) {
        result.append(line).append("\n");
        line = in.readLine();
        }
      }
    }
  
  private void buildScript(HttpExchange exchange, boolean jQuery) throws IOException {
    StringBuilder result = new StringBuilder();
    Class contract = wrapper.getClass();
    if (jQuery) appendResource(result, "/jquery.js");
    appendResource(result, "/qed.js");
    for (JsonWrapper.Operation method : wrapper.getOperations()) {
      result.append(method.toJavaScriptDefinition());
      result.append(" { return new Executor");
      result.append(method.toExecutorDefinition());
      result.append("; }\n");
      }
    Headers headers = exchange.getResponseHeaders();
    headers.add("Content-Type", "text/javascript; charset=utf8");
    byte[] response = result.toString().getBytes("UTF-8");
    exchange.sendResponseHeaders(200, response.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(response);
      }
    }
  
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    System.out.println("Killroy was here...");
    String resource = exchange.getRequestURI().getPath();
    StringBuilder result = new StringBuilder();
    if ("/qed".equals(resource)) buildScript(exchange, false);
    else if ("/qed-query".equals(resource)) buildScript(exchange, true);
    else {
      String name = resource.substring(5);
      System.out.println(name);
      InputStreamReader rin = new InputStreamReader(exchange.getRequestBody());
      BufferedReader reqin = new BufferedReader(rin);
      String line = reqin.readLine();
      System.out.println("  <- "+line);
      String json = wrapper.call(name, line);
      System.out.println("  -> "+json);
      result.append(json);
      Headers headers = exchange.getResponseHeaders();
      headers.add("Content-Type", "application/json; charset=utf8");
      byte[] response = result.toString().getBytes("UTF-8");
      exchange.sendResponseHeaders(200, response.length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(response);
        }
      }
    }
  
  }
