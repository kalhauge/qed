package dk.kalhauge.qed;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import static dk.kalhauge.qed.Utils.*;

public class JsonWrapper {
  private static final Gson gson = new Gson();
  public static final Set<String> ignore = new HashSet<>();
  static { 
    ignore.add("wait");
    ignore.add("equals");
    ignore.add("toString");
    ignore.add("hashCode");
    ignore.add("getClass");
    ignore.add("notify");
    ignore.add("notifyAll");
    }
  
  private final Object object;
  private final Collection<Operation> methods = new ArrayList<>();
  
  public JsonWrapper(Object object) {
    System.out.println("- Wrapper");
    this.object = object;
    for (Method method : object.getClass().getMethods()) {
      if (ignore.contains(method.getName())) continue;
      methods.add(Operation.fromParameterCount(method.getName(), getParameterCount(method)));
      }
    }
  
  private Method findMethodCalled(String name, int count) {
    for (Method method : object.getClass().getMethods()) 
        if (method.getName().equals(name) && getParameterCount(method) == count) return method;
    throw new RuntimeException("No such method");
    }
  
  public String call(String name, String arguments) {
    try {
      JsonArray all = gson.fromJson(arguments, JsonArray.class);      
      Method method = findMethodCalled(name, all.size());
      Object[] parameters = new Object[all.size()];
      for (int index = 0; index < parameters.length; index++) {
        parameters[index] = gson.fromJson(all.get(index), method.getParameterTypes()[index]);
        }
      Object result = method.invoke(object, parameters);
      return gson.toJson(result);
      }
    catch (IllegalAccessException | InvocationTargetException e) {
      return null;
      } 
    }
  
  public Collection<Operation> getOperations() { return methods; }
  
  public static class Operation {
    private final String name;
    private final String params;

    Operation(String name, String params) {
      this.name = name;
      this.params = params;
      }

    public static Operation fromParameterCount(String name, int parameterCount) {
      return new Operation(name, join(", ", alphabeth.subList(0, parameterCount)));
      }
    
    public String toJavaScriptDefinition() {
      return join("", "function ", name, "(", params, ")");
      }
    
    public String toExecutorDefinition() {
      String content = params.isEmpty() ? name : join(", ", name, params);
      return join("", "(", content, ")");
      }
    
    }
  
  }
