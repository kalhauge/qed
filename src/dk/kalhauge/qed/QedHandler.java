package dk.kalhauge.qed;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

public class QedHandler implements HttpHandler {
  private static Gson gson = new Gson();
  public final Object target;
  public static final Set<String> ignore = new HashSet<String>(
    Arrays.asList(new String [] {
      "wait", "equals", "toString", "hashCode", "getClass", "notify", "notifyAll"
        })
      );
  
  public QedHandler(Object target) {
    this.target = target;
    }
  
  private String parameterList(Parameter[] parameters) {
    String list = "";
    for (Parameter parameter : parameters) {
      if (list.isEmpty()) list = parameter.getName();
      else list += ", "+parameter.getName();
      }
    return list;
    }
  
  private Method findMethodCalled(String name, int count) {
    for (Method method : target.getClass().getMethods()) 
        if (method.getName().equals(name) && method.getParameterCount() == count) return method;
    throw new RuntimeException("No such method");
    }
  
  private String call(String name, String json) {
    try {
      JsonArray all = gson.fromJson(json, JsonArray.class);      
      Method method = findMethodCalled(name, all.size());
      Object[] parameters = new Object[all.size()];
      for (int index = 0; index < parameters.length; index++) {
        parameters[index] = gson.fromJson(all.get(index), method.getParameterTypes()[index]);
        }
      Object result = method.invoke(target, parameters);
      return gson.toJson(result);
      }
    catch (JsonSyntaxException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      return null;
      } 
    }
  
  private BufferedReader getResourceReader(String file) throws FileNotFoundException {
    String path = getClass().getResource(file).getPath();
    return new BufferedReader(new FileReader(path));
    }
  
  private void buildScript(HttpExchange exchange, boolean jQuery) throws IOException {
    StringBuilder result = new StringBuilder();
    Class contract = target.getClass();
    if (jQuery) {
      try (BufferedReader in = getResourceReader("/jquery.js")) {
        String line = in.readLine();
        while (line != null) {
          result.append(line).append("\n");
          line = in.readLine();
          }
        }
      }
    try (BufferedReader in = getResourceReader("/qed.js")) {
      String line = in.readLine();
      while (line != null) {
        result.append(line).append("\n");
        line = in.readLine();
        }
      }
    for (Method method : contract.getMethods()) {
      if (ignore.contains(method.getName())) continue;
      result.append("function ").append(method.getName()).append("(");
      String list = parameterList(method.getParameters());
      result.append(list);
      result.append(") { return new Executor('").append(method.getName()).append("'");
      if (!list.isEmpty()) result.append(", ");
      result.append(list).append("); }\n");
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
      String json = call(name, line);
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
