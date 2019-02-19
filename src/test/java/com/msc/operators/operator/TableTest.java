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
   * Generates a cinema table for testing.
   *
   * @return a sample cinema table
   */
  public Table createCinemaTable()
  {
    Table cinema = new Table ("cinema", "title year length genre studioName producerNo",
        "String Integer Integer String String Integer", "title year");
    Comparable [] film1 = { "Rocky", 1985, 200, "action", "Universal", 12125 };
    Comparable [] film2 = { "Rambo", 1978, 100, "action", "Universal", 32355 };
    Comparable [] film3 = { "Galaxy_Quest", 1999, 104, "comedy", "DreamWorks", 67890 };
    cinema.insert(film1);
    cinema.insert(film2);
    cinema.insert(film3);
    return cinema;
  }

  /**
   * Generates a studio table for testing.
   *
   * @return a sample studio table
   */
  public Table createStudioTable() {
    Table studio = new Table ("studio", "name address presNo",
        "String String Integer", "name");
    Comparable [] studio0 = { "Fox", "Los_Angeles", 7777 };
    Comparable [] studio1 = { "Universal", "Universal_City", 8888 };
    Comparable [] studio2 = { "DreamWorks", "Universal_City", 9999 };
    studio.insert (studio0);
    studio.insert (studio1);
    studio.insert (studio2);
    return studio;
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

  /**
   * Tests the union method.
   */
  @Test
  public void testUnion()
  {
    Table movie = this.createMovieTable();
    Table cinema = this.createCinemaTable();
    Table movie_union_cinema = movie.union(cinema);
    assertEquals(movie_union_cinema.getTableSize(), 5);
    assertEquals(movie_union_cinema.getTuples().get(4)[0], "Galaxy_Quest");
  }

  /**
   *Tests the minus method.
   */
  @Test
  public void testMinus()
  {
    Table movie = this.createMovieTable();
    Table cinema = this.createCinemaTable();
    Table movie_minus_cinema = movie.minus(cinema);
    assertEquals(movie_minus_cinema.getTableSize(), 2);
    assertEquals(movie_minus_cinema.getTuples().get(0)[0], "Star_Wars");
    assertEquals(movie_minus_cinema.getTuples().get(1)[0], "Star_Wars_2");
  }

  /**
   *Tests the join attributes method.
   */
  @Test
  public void testJoinByAttributes() {
    Table movie = this.createMovieTable();
    Table studio = this.createStudioTable();
    Table movie_join_studio = movie.join("studioName", "name", studio);
    assertEquals(movie_join_studio.getTableSize(), 4);
    assertEquals(movie_join_studio.col("name"), 6);
    assertEquals(movie_join_studio.col("presNo"), 8);
    assertEquals(movie_join_studio.getTuples().get(3)[0], "Rambo");

    Table cinema = this.createCinemaTable();
    Table movie_join_cinema = movie.join("title", "title", cinema);
    assertEquals(movie_join_cinema.getTableSize(), 2);
    assertEquals(movie_join_cinema.col("title2"), 6);
    assertEquals(movie_join_cinema.col("length2"), 8);
    assertEquals(movie_join_cinema.getTuples().get(1)[0], "Rambo");
  }

  /**
   * Tests the join tables method.
   */
  @Test
  public void testJoinTables() {
    Table movie = this.createMovieTable();
    Table cinema = this.createCinemaTable();
    Table movie_join_cinema = movie.join(cinema);
    assertEquals(movie_join_cinema.getTableSize(), 2);
    assertEquals(movie_join_cinema.col("title2"), -1);
    assertEquals(movie_join_cinema.col("length2"), -1);
    assertEquals(movie_join_cinema.getTuples().get(1)[0], "Rambo");
  }
}
