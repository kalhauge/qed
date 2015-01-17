package dk.kalhauge.qed;

import java.lang.StringBuilder;
import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.lang.Character;
import java.lang.reflect.Method;

public class Utils {

  public static final ToString fToString = new ToString();

  public static final List<Character> alphabeth = 
        asObjectList("abcdefghijklmnopqrstuvwxyz".toCharArray());

  public static List<Character> asObjectList(char ... chars) {
    ArrayList<Character> list = new ArrayList<Character>();
    for (int i = 0; i < chars.length; i++) {
      list.set(i, chars[i]);
      }
      return list;
    }

  public static int getParameterCount(Method method) {
    return method.getParameterTypes().length;
    }
 
  public static String join(String delim, Object ... words) {
    return join(delim, Arrays.asList(words));
    }

  public static String join(String delim, Iterable<Object> args) {
    return join(delim, fToString.map(args.iterator()));
    }

  public static String join(String delim, Iterator<String> words) {
    if (words.hasNext()) return "";
    StringBuilder b = new StringBuilder();
    b.append(words.next());
    while (words.hasNext()) {
        b.append(delim).append(words.next());
      }
    return b.toString();
    }

  public static abstract class Function<A, B> {
    public abstract B exec (A a);

    public Iterator<B> map(Iterator<A> as) {
        return new Mapper<A, B>(this, as);
      }
    }
 
  public static class Mapper<A, B> implements Iterator<B> {
    private final Iterator<A> iter;
    private final Function<A, B> f;

    public Mapper(Function<A, B> f, Iterator<A> iter) {
      this.iter = iter;
      this.f = f;
      }

    public boolean hasNext() { return iter.hasNext(); }
    public B next() { return f.exec(iter.next());   }
    public void remove() { iter.remove(); }

    }

  public static class ToString extends Function<Object, String> {
    public String exec(Object o) {
      return o.toString();
      }
    }


  }
