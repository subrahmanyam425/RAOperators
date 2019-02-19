package com.msc.operators;

/**
 * An example that demonstrates running and post-processing JUnit tests in order to grade a VPL activity.
 */
import com.msc.operators.operator.TableTest;
import java.util.Formatter;
import java.util.Locale;

public class Grade {

  /**
   * Run some unit tests and process the results, calculating an overall grade for this activity.
   * Print out comments for each tests and the overall grade, using the format required by vpl_execution.
   * @param args
   */
  public static void main(String[] args) {
    TableTest t = new TableTest();
    int grade = 0;
    try {
      t.testProject(); //worth 50 marks
      System.out.println(formatOutput("Test One", "50", null));
      grade += 50;
    } catch (AssertionError e) {
      System.out.println(formatOutput("Test One", "50", e));
    }
    try {
      t.testSelect(); //worth 50 marks
      System.out.println(formatOutput("Test Two", "50", null));
      grade += 50;
    } catch (AssertionError e) {
      System.out.println(formatOutput("Test Two", "50", e));
    }
    try {
      t.testUnion(); //worth 50 marks
      System.out.println(formatOutput("Test Three", "50", null));
      grade += 50;
    } catch (AssertionError e) {
      System.out.println(formatOutput("Test Three", "50", e));
    }
    try {
      t.testMinus(); //worth 50 marks
      System.out.println(formatOutput("Test Four", "50", null));
      grade += 50;
    } catch (AssertionError e) {
      System.out.println(formatOutput("Test Four", "50", e));
    }
    try {
      t.testJoinByAttributes(); //worth 50 marks
      System.out.println(formatOutput("Test Five", "50", null));
      grade += 50;
    } catch (AssertionError e) {
      System.out.println(formatOutput("Test Five", "50", e));
    }
    try {
      t.testJoinTables(); //worth 50 marks
      System.out.println(formatOutput("Test Six", "50", null));
      grade += 50;
    } catch (AssertionError e) {
      System.out.println(formatOutput("Test Six", "50", e));
    }
    System.out.println("Grade :=>> "+grade);
  }

  /**
   * Format one or more comments for a tests.
   * @param testName
   * @param value
   * @param e
   * @return
   */
  private static String formatOutput(String testName, String value, AssertionError e) {
    StringBuilder sb = new StringBuilder();
    Formatter f = new Formatter(sb, Locale.getDefault());
    String grade = (e == null ? value : "0");
    f.format("Comment :=>> %s: %s. %s marks\n", testName, (e == null ? "success" : "failure"), grade);
    if (e != null) {
      f.format("<|-- \n%s\n --|>\n", e.getMessage());
    }
    return sb.toString();
  }
}
