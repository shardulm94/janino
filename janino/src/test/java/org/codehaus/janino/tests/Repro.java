package org.codehaus.janino.tests;

import org.codehaus.janino.ClassBodyEvaluator;
import org.junit.Test;


public class Repro {

  @Test
  public void test() throws Exception {
    String toCook = ""
        + "public class Test {"
        + "  public static int test() {"
        + "    return com.company.user.Country.get();"
        + "  }"
        + "}";

    ClassBodyEvaluator eval = new ClassBodyEvaluator();
    eval.cook("generated.java", toCook);
  }
}
