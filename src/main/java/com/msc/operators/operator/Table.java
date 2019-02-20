package com.msc.operators.operator;

/****************************************************************************************
 * @file  Table.java
 *
 */

import com.msc.operators.util.PredicateMod;
import com.msc.operators.util.*;
import java.io.*;
import java.util.*;
import static java.lang.System.out;

/****************************************************************************************
 * This class implements relational database tables (including attribute names, domains
 * and a list of tuples.  Five basic relational algebra operator are provided: project,
 * select, union, minus and join.  The insert data manipulation operator is also provided.
 * Missing are update and delete data manipulation operator.
 */
public class Table
    implements Serializable
{
  /** Relative path for storage directory
   */
  private static final String DIR = "src/main/resources/store" + File.separator;

  /** Filename extension for database files
   */
  private static final String EXT = ".dbf";

  /** Counter for naming temporary tables.
   */
  private static int count = 0;

  /** Table name.
   */
  private final String name;

  /** Array of attribute names.
   */
  private final String [] attribute;

  /** Array of attribute domains: a domain may be
   *  integer types: Long, Integer, Short, Byte
   *  real types: Double, Float
   *  string types: Character, String
   */
  private final Class [] domain;

  /** Collection of tuples (data storage).
   */
  private final List <Comparable []> tuples;

  /** Primary key.
   */
  private final String [] key;

  /** Index into tuples (maps key to tuple number).
   */
  private final Map <KeyType, Comparable []> index;

  /** The supported map types.
   */
  private enum MapType { NO_MAP, TREE_MAP, LINHASH_MAP, BPTREE_MAP }

  /** The map type to be used for indices.  Change as needed.
   */
  private static final MapType mType = MapType.TREE_MAP;

  /************************************************************************************
   * Make a map (index) given the MapType.
   */
  private static Map <KeyType, Comparable []> makeMap ()
  {
    switch (mType) {
      case TREE_MAP:    return new TreeMap <> ();
      case LINHASH_MAP: return null; //return new LinHashMap <> (KeyType.class, Comparable [].class); //Change to this once LinHashMap is implemented
      case BPTREE_MAP:  return null; //return new BpTreeMap <> (KeyType.class, Comparable [].class); //Change to this once BpTreeMap is implemented
      default:          return null;
    } // switch
  } // makeMap

  //-----------------------------------------------------------------------------------
  // Constructors
  //-----------------------------------------------------------------------------------

  /************************************************************************************
   * Construct an empty table from the meta-data specifications.
   *
   * @param _name       the name of the relation
   * @param _attribute  the string containing attributes names
   * @param _domain     the string containing attribute domains (data types)
   * @param _key        the primary key
   */
  public Table (String _name, String [] _attribute, Class [] _domain, String [] _key)
  {
    name      = _name;
    attribute = _attribute;
    domain    = _domain;
    key       = _key;
    tuples    = new ArrayList <> ();
    index     = makeMap ();

  } // primary constructor

  /************************************************************************************
   * Construct a table from the meta-data specifications and data in _tuples list.
   *
   * @param _name       the name of the relation
   * @param _attribute  the string containing attributes names
   * @param _domain     the string containing attribute domains (data types)
   * @param _key        the primary key
   * @param _tuples     the list of tuples containing the data
   */
  public Table (String _name, String [] _attribute, Class [] _domain, String [] _key,
      List <Comparable []> _tuples)
  {
    name      = _name;
    attribute = _attribute;
    domain    = _domain;
    key       = _key;
    tuples    = _tuples;
    index     = makeMap ();
  } // constructor

  /************************************************************************************
   * Construct an empty table from the raw string specifications.
   *
   * @param _name       the name of the relation
   * @param attributes  the string containing attributes names
   * @param domains     the string containing attribute domains (data types)
   * @param _key        the primary key
   */
  public Table (String _name, String attributes, String domains, String _key)
  {
    this (_name, attributes.split (" "), findClass (domains.split (" ")), _key.split(" "));

    out.println ("DDL> create table " + name + " (" + attributes + ")");
  } // constructor

  //----------------------------------------------------------------------------------
  // Public Methods
  //----------------------------------------------------------------------------------

  /************************************************************************************
   * Project the tuples onto a lower dimension by keeping only the given attributes.
   * Check whether the original key is included in the projection.
   *
   * #usage movie.project ("title year studioNo")
   *
   * @param attributes  the attributes to project onto
   * @return  a table of projected tuples
   */
  public Table project (String attributes)
  {
    out.println ("RA> " + name + ".project (" + attributes + ")");
    String [] attrs     = attributes.split (" ");
    Class []  colDomain = extractDom (match (attrs), domain);
    String [] newKey    = (Arrays.asList (attrs).containsAll (Arrays.asList (key))) ? key : attrs;

    List <Comparable []> rows = new ArrayList <> ();
    List<String> attrList = new ArrayList<>(Arrays.asList(attribute));

    for (Comparable[] tuple: tuples) {
      Comparable[] newTuple = new Comparable[attrs.length];
      int countVal = 0;
      for (String attr : attrs) {
        int index = attrList.indexOf(attr);
        newTuple[countVal] = tuple[index];
        countVal++;
      }
      rows.add(newTuple);
    }

    return new Table (name + count++, attrs, colDomain, newKey, rows);
  } // project

  /************************************************************************************
   * Select the tuples satisfying the given predicate (Boolean function).
   *
   * #usage movie.select (t -> t[movie.col("year")].equals (1977))
   *
   * @param predicate  the check condition for tuples
   * @return  a table with tuples satisfying the predicate
   */
  public Table select (PredicateMod<Comparable []> predicate)
  {
    out.println ("RA> " + name + ".select (" + predicate + ")");
    List<Comparable[]> rows = new ArrayList <> ();
    for(Comparable[] tuple:tuples) {
      if(predicate.test(tuple)) {
        rows.add(tuple);
      }
    }
    return new Table (name + count++, attribute, domain, key, rows);
  } // select

  /************************************************************************************
   * Select the tuples satisfying the given key predicate (key = value).  Use an index
   * (Map) to retrieve the tuple with the given key value.
   *
   * @param keyVal  the given key value
   * @return  a table with the tuple satisfying the key predicate
   */
  public Table select (KeyType keyVal)
  {
    out.println ("RA> " + name + ".select (" + keyVal + ")");

    List <Comparable []> rows = new ArrayList <> ();

    if(mType != MapType.NO_MAP) {
      if(index.containsKey(keyVal)) {
        rows.add(index.get(keyVal));
      }
    }

    return new Table (name + count++, attribute, domain, key, rows);
  } // select

  /************************************************************************************
   * Union this table and table2.  Check that the two tables are compatible.
   *
   * #usage movie.union (show)
   *
   * @param table2  the rhs table in the union operation
   * @return  a table representing the union
   */
  public Table union (Table table2)
  {
    out.println ("RA> " + name + ".union (" + table2.name + ")");
    if (! compatible (table2)) return null;

    List <Comparable []> rows = new ArrayList <> ();

    if(Arrays.equals(this.attribute, table2.attribute)) {
      for (Comparable[] tuple: tuples) {
        rows.add(tuple);
      }

      for (Comparable[] tuple: table2.tuples) {
        int countVal = 0;
        for (Comparable[] row : rows) {
          countVal = 0;
          for(int i = 0 ; i < row.length ; i++) {
            if(row[i] instanceof Integer) {
              if(((Integer)row[i]).intValue() == ((Integer)tuple[i]).intValue()) {
                countVal++;
              }
            } else {
              if (row[i] == tuple[i]) {
                countVal++;
              }
            }
          }

          if(countVal == tuple.length) {
            break;
          }
        }
        if(countVal != tuple.length) {
          rows.add(tuple);
        }
      }
    }

    return new Table (name + count++, attribute, domain, key, rows);
  } // union

  /************************************************************************************
   * Take the difference of this table and table2.  Check that the two tables are
   * compatible.
   *
   * #usage movie.minus (show)
   *
   * @param table2  The rhs table in the minus operation
   * @return  a table representing the difference
   */
  public Table minus (Table table2)
  {
    out.println ("RA> " + name + ".minus (" + table2.name + ")");
    if (! compatible (table2)) return null;

    List <Comparable []> rows = new ArrayList <> ();

    if(Arrays.equals(this.attribute, table2.attribute)) {
      for (Comparable[] tuple: tuples) {
        rows.add(tuple);
      }

      for (Comparable[] innerTuple: table2.tuples) {
        for (Comparable[] tuple: tuples) {
          int countVal = 0;
          for(int i = 0 ; i < tuple.length ; i++) {
            if(tuple[i] instanceof Integer) {
              if(((Integer)tuple[i]).intValue() == ((Integer) innerTuple[i]).intValue()) {
                countVal++;
              }
            } else {
              if(tuple[i] == innerTuple[i]) {
                countVal++;
              }
            }
            if(countVal == tuple.length) {
              break;
            }
          }
          if(countVal == tuple.length) {
            rows.remove(tuple);
          }
        }

      }
    }

    return new Table (name + count++, attribute, domain, key, rows);
  } // minus

  /************************************************************************************
   * Join this table and table2 by performing an "equi-join".  Tuples from both tables
   * are compared requiring attributes1 to equal attributes2.  Disambiguate attribute
   * names by append "2" to the end of any duplicate attribute name.  Implement using
   * a Nested Loop Join algorithm.
   *
   * #usage movie.join ("studioNo", "name", studio)
   *
   * @param attribute1  the attributes of this table to be compared (Foreign Key)
   * @param attribute2  the attributes of table2 to be compared (Primary Key)
   * @param table2      the rhs table in the join operation
   * @return  a table with tuples satisfying the equality predicate
   */
  public Table join (String attributes1, String attributes2, Table table2)
  {
    out.println ("RA> " + name + ".join (" + attributes1 + ", " + attributes2 + ", "
        + table2.name + ")");

    String [] t_attrs = attributes1.split (" ");
    String [] u_attrs = attributes2.split (" ");

    List <Comparable []> rows = new ArrayList <> ();
    List<String> attrList1 = new ArrayList<>(Arrays.asList(attribute));
    List<String> attrList2 = new ArrayList<>(Arrays.asList(table2.attribute));
    List<Integer> indexList1 = new ArrayList<>();
    List<Integer> indexList2 = new ArrayList<>();

    for (String attr : t_attrs) {
      int index = attrList1.indexOf(attr);
      indexList1.add(index);
    }

    for (String attr : u_attrs) {
      int index = attrList2.indexOf(attr);
      indexList2.add(index);
    }

    for (int i = 0; i < attrList1.size() ; i++) {
      if(attrList2.contains(attrList1.get(i))) {
        int index = attrList2.indexOf(attrList1.get(i));
        attrList2.remove(index);
        attrList2.add(index, attrList1.get(i) + "2");
      }
    }

    int countVal;

    for (Comparable[] tuple : tuples) {
      for (Comparable[] innerTuple : table2.tuples) {
        countVal = 0;
        for(int i = 0 ; i < indexList1.size() ; i++) {
          if(tuple[indexList1.get(i)].equals(innerTuple[indexList2.get(i)])) {
            countVal++;
          }
        }
        if(countVal == indexList1.size()) {
           rows.add(ArrayUtil.concat(tuple, innerTuple));
        }
      }
    }

    return new Table (name + count++, ArrayUtil.concat (attrList1.toArray(new String[attrList1.size()])
        , attrList2.toArray(new String[attrList2.size()])),
        ArrayUtil.concat (domain, table2.domain), key, rows);
  } // join

  /************************************************************************************
   * Join this table and table2 by performing an "equi-join".  Same as above, but implemented
   * using an Index Join algorithm.
   *
   * @param attribute1  the attributes of this table to be compared (Foreign Key)
   * @param attribute2  the attributes of table2 to be compared (Primary Key)
   * @param table2      the rhs table in the join operation
   * @return  a table with tuples satisfying the equality predicate
   */
  public Table i_join (String attributes1, String attributes2, Table table2)
  {
    return null;
  } // i_join

  /************************************************************************************
   * Join this table and table2 by performing an "equi-join".  Same as above, but implemented
   * using a Hash Join algorithm.
   *
   * @param attribute1  the attributes of this table to be compared (Foreign Key)
   * @param attribute2  the attributes of table2 to be compared (Primary Key)
   * @param table2      the rhs table in the join operation
   * @return  a table with tuples satisfying the equality predicate
   */
  public Table h_join (String attributes1, String attributes2, Table table2)
  {
    return null;
  } // h_join

  /************************************************************************************
   * Join this table and table2 by performing an "natural join".  Tuples from both tables
   * are compared requiring common attributes to be equal.  The duplicate column is also
   * eliminated.
   *
   * #usage movieStar.join (starsIn)
   *
   * @param table2  the rhs table in the join operation
   * @return  a table with tuples satisfying the equality predicate
   */
  public Table join (Table table2)
  {
    out.println ("RA> " + name + ".join (" + table2.name + ")");

    List <Comparable []> rows = new ArrayList <> ();

    List<String> attrList1 = new ArrayList<>(Arrays.asList(attribute));
    List<String> attrList2 = new ArrayList<>(Arrays.asList(table2.attribute));
    List<Integer> indexList1 = new ArrayList<>();
    List<Integer> indexList2 = new ArrayList<>();

    for (int i = 0; i < attrList1.size() ; i++) {
      if(attrList2.contains(attrList1.get(i))) {
        int index = attrList2.indexOf(attrList1.get(i));
        indexList1.add(i);
        indexList2.add(index);
      }
    }

    int countVal;

    for (Comparable[] tuple : tuples) {
      for (Comparable[] innerTuple : table2.tuples) {
        countVal = 0;
        for(int i = 0 ; i < indexList1.size() ; i++) {
          if(tuple[indexList1.get(i)].equals(innerTuple[indexList2.get(i)])) {
            countVal++;
          }
        }
        if(countVal == indexList1.size()) {
          Comparable[] newTuple = new Comparable[attrList2.size() - indexList2.size()];
          int j = 0;
          for(int i = 0; i < innerTuple.length ; i++) {
            if(!indexList2.contains(i)) {
              newTuple[j] = innerTuple[i];
              j++;
            }
          }
          rows.add(ArrayUtil.concat(tuple, newTuple));
        }
      }
    }

    for (int i = 0; i < attrList1.size() ; i++) {
      if(attrList2.contains(attrList1.get(i))) {
        attrList2.remove(attrList1.get(i));
      }
    }

    return new Table (name + count++, ArrayUtil.concat (attrList1.toArray(new String[attrList1.size()])
        , attrList2.toArray(new String[attrList2.size()])), ArrayUtil.concat (domain, table2.domain), key, rows);
  } // join

  /************************************************************************************
   * Return the column position for the given attribute name.
   *
   * @param attr  the given attribute name
   * @return  a column position
   */
  public int col (String attr)
  {
    for (int i = 0; i < attribute.length; i++) {
      if (attr.equals (attribute [i])) return i;
    } // for

    return -1;  // not found
  } // col

  /************************************************************************************
   * Insert a tuple to the table.
   *
   * #usage movie.insert ("'Star_Wars'", 1977, 124, "T", "Fox", 12345)
   *
   * @param tup  the array of attribute values forming the tuple
   * @return  whether insertion was successful
   */
  public boolean insert (Comparable [] tup)
  {
    out.println ("DML> insert into " + name + " values ( " + Arrays.toString (tup) + " )");

    if (typeCheck (tup)) {
      tuples.add (tup);
      Comparable [] keyVal = new Comparable [key.length];
      int []        cols   = match (key);
      for (int j = 0; j < keyVal.length; j++) keyVal [j] = tup [cols [j]];
      if (mType != MapType.NO_MAP) index.put (new KeyType (keyVal), tup);
      return true;
    } else {
      return false;
    } // if
  } // insert

  /************************************************************************************
   * Get the name of the table.
   *
   * @return  the table's name
   */
  public String getName ()
  {
    return name;
  } // getName

  /************************************************************************************
   * Get the tuples.
   *
   * @return  the table's info
   */
  public int getTableSize() {
    return tuples.size();
  }

  public List <Comparable[]> getTuples() {
    return tuples;
  }
  /************************************************************************************
   * Print this table.
   */
  public void print ()
  {
    out.println ("\n Table " + name);
    out.print ("|-");
    for (int i = 0; i < attribute.length; i++) out.print ("---------------");
    out.println ("-|");
    out.print ("| ");
    for (String a : attribute) out.printf ("%15s", a);
    out.println (" |");
    out.print ("|-");
    for (int i = 0; i < attribute.length; i++) out.print ("---------------");
    out.println ("-|");
    for (Comparable [] tup : tuples) {
      out.print ("| ");
      for (Comparable attr : tup) out.printf ("%15s", attr);
      out.println (" |");
    } // for
    out.print ("|-");
    for (int i = 0; i < attribute.length; i++) out.print ("---------------");
    out.println ("-|");
  } // print

  /************************************************************************************
   * Print this table's index (Map).
   */
  public void printIndex ()
  {
    out.println ("\n Index for " + name);
    out.println ("-------------------");
    if (mType != MapType.NO_MAP) {
      for (Map.Entry <KeyType, Comparable []> e : index.entrySet ()) {
        out.println (e.getKey () + " -> " + Arrays.toString (e.getValue ()));
      } // for
    } // if
    out.println ("-------------------");
  } // printIndex

  /************************************************************************************
   * Load the table with the given name into memory.
   *
   * @param name  the name of the table to load
   */
  public static Table load (String name)
  {
    Table tab = null;
    try {
      ObjectInputStream ois = new ObjectInputStream (new FileInputStream (DIR + name + EXT));
      tab = (Table) ois.readObject ();
      ois.close ();
    } catch (IOException ex) {
      out.println ("load: IO Exception");
      ex.printStackTrace ();
    } catch (ClassNotFoundException ex) {
      out.println ("load: Class Not Found Exception");
      ex.printStackTrace ();
    } // try
    return tab;
  } // load

  /************************************************************************************
   * Save this table in a file.
   */
  public void save ()
  {
    try {
      ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (DIR + name + EXT));
      oos.writeObject (this);
      oos.close ();
    } catch (IOException ex) {
      out.println ("save: IO Exception");
      ex.printStackTrace ();
    } // try
  } // save

  //----------------------------------------------------------------------------------
  // Private Methods
  //----------------------------------------------------------------------------------

  /************************************************************************************
   * Determine whether the two tables (this and table2) are compatible, i.e., have
   * the same number of attributes each with the same corresponding domain.
   *
   * @param table2  the rhs table
   * @return  whether the two tables are compatible
   */
  private boolean compatible (Table table2)
  {
    if (domain.length != table2.domain.length) {
      out.println ("compatible ERROR: table have different arity");
      return false;
    } // if
    for (int j = 0; j < domain.length; j++) {
      if (domain [j] != table2.domain [j]) {
        out.println ("compatible ERROR: tables disagree on domain " + j);
        return false;
      } // if
    } // for
    return true;
  } // compatible

  /************************************************************************************
   * Match the column and attribute names to determine the domains.
   *
   * @param column  the array of column names
   * @return  an array of column index positions
   */
  private int [] match (String [] column)
  {
    int [] colPos = new int [column.length];

    for (int j = 0; j < column.length; j++) {
      boolean matched = false;
      for (int k = 0; k < attribute.length; k++) {
        if (column [j].equals (attribute [k])) {
          matched = true;
          colPos [j] = k;
        } // for
      } // for
      if ( ! matched) {
        out.println ("match: domain not found for " + column [j]);
      } // if
    } // for

    return colPos;
  } // match

  /************************************************************************************
   * Extract the attributes specified by the column array from tuple t.
   *
   * @param t       the tuple to extract from
   * @param column  the array of column names
   * @return  a smaller tuple extracted from tuple t
   */
  private Comparable [] extract (Comparable [] t, String [] column)
  {
    Comparable [] tup = new Comparable [column.length];
    int [] colPos = match (column);
    for (int j = 0; j < column.length; j++) tup [j] = t [colPos [j]];
    return tup;
  } // extract

  /************************************************************************************
   * Check the size of the tuple (number of elements in list) as well as the type of
   * each value to ensure it is from the right domain.
   *
   * @param t  the tuple as a list of attribute values
   * @return  whether the tuple has the right size and values that comply
   *          with the given domains
   */
  private boolean typeCheck (Comparable [] t)
  {
    if(this.attribute.length != t.length) return false;

    for(int i = 0; i < this.domain.length; i++) {
      if(this.domain[i] != t[i].getClass()) return false;
    }

    return true;
  } // typeCheck

  /************************************************************************************
   * Find the classes in the "java.lang" package with given names.
   *
   * @param className  the array of class name (e.g., {"Integer", "String"})
   * @return  an array of Java classes
   */
  private static Class [] findClass (String [] className)
  {
    Class [] classArray = new Class [className.length];

    for (int i = 0; i < className.length; i++) {
      try {
        classArray [i] = Class.forName ("java.lang." + className [i]);
      } catch (ClassNotFoundException ex) {
        out.println ("findClass: " + ex);
      } // try
    } // for

    return classArray;
  } // findClass

  /************************************************************************************
   * Extract the corresponding domains.
   *
   * @param colPos the column positions to extract.
   * @param group  where to extract from
   * @return  the extracted domains
   */
  private Class [] extractDom (int [] colPos, Class [] group)
  {
    Class [] obj = new Class [colPos.length];

    for (int j = 0; j < colPos.length; j++) {
      obj [j] = group [colPos [j]];
    } // for

    return obj;
  } // extractDom

} // Table class

