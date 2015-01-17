/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.kalhauge.qed;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;

/**
 *
 * @author anders
 */
public class JsonWrapperTest {
  private TestFacade facade;
  private JsonWrapper wrapper;
  
  
  public JsonWrapperTest() { }
  
  @Before
  public void setUp() throws Exception {
    facade = new TestFacade();
    wrapper = new JsonWrapper(facade);
    }
  
  @Test
  public void testVoidMethodNoArguments() throws Exception {
    String name = "voidMethodNoArguments";
    String args = "[]";
    wrapper.call(name, args);
    assertThat(facade.getValue(), is("NO_ARGS"));
    }
  
}
