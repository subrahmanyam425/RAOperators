package com.msc.operators.operator;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class TableTest
{
  /**
   * Generates a movie table for testing
   *
   * @return a sample movie table
   */
  public Table createMovieTable()
  {
    Table movie = new Table ("movie", "title year length genre studioName producerNo",
        "String Integer Integer String String Integer", "title year");
    Comparable [] film0 = { "Star_Wars", 1977, 124, "sciFi", "Fox", 12345 };
    Comparable [] film1 = { "Star_Wars_2", 1980, 124, "sciFi", "Fox", 12345 };
    Comparable [] film2 = { "Rocky", 1985, 200, "action", "Universal", 12125 };
    Comparable [] film3 = { "Rambo", 1978, 100, "action", "Universal", 32355 };
    movie.insert (film0);
    movie.insert (film1);
    movie.insert (film2);
    movie.insert (film3);
    return movie;
  }

  /**
   * Tests the project method.
   */
  @Test
  public void testProject()
  {
    Table movie = this.createMovieTable();
    Table movie_project = movie.project ("title year");

    assertEquals(movie_project.col("title"), 0);
    assertEquals(movie_project.col("year"), 1);
    assertEquals(movie_project.col("length"), -1);
    assertEquals(movie_project.getTuples().get(0)[0], "Star_Wars");
    assertEquals(movie_project.getTuples().get(0).length, 2);
  }

  /**
   * Tests the select method.
   */
  @Test
  public void testSelect()
  {
    Table movie = this.createMovieTable();
    Table movie_select = movie.select(new KeyType("Star_Wars"));
    assertEquals(movie_select.getTableSize(), 1);
    List<Comparable[]> starWars = movie_select.getTuples();
    assertEquals(starWars.get(0)[0], "Star_Wars");
  }
}
