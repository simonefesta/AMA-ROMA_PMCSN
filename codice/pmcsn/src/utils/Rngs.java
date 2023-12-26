package utils;/* -------------------------------------------------------------------------
 * This is an Java library for multi-stream random number generation.
 * The use of this library is recommended as a replacement for the Java
 * class Random, particularly in simulation applications where the
 * statistical 'goodness' of the random number generator is important.
 * The library supplies 256 streams of random numbers; use
 * selectStream(s) to switch between streams indexed s = 0,1,...,255.
 *
 * The streams must be initialized.  The recommended way to do this is by
 * using the function plantSeeds(x) with the value of x used to initialize
 * the default stream and all other streams initialized automatically with
 * values dependent on the value of x.  The following convention is used
 * to initialize the default stream:
 *    if x > 0 then x is the state
 *    if x < 0 then the state is obtained from the system clock
 *    if x = 0 then the state is to be supplied interactively.
 *
 * The generator used in this library is a so-called 'Lehmer random number
 * generator' which returns a pseudo-random number uniformly distributed
 * 0.0 and 1.0.  The period is (m - 1) where m = 2,147,483,647 and the
 * smallest and largest possible values are (1 / m) and 1 - (1 / m)
 * respectively.  For more details see:
 *
 *       "Random Number Generators: Good Ones Are Hard To Find"
 *                   Steve Park and Keith Miller
 *              Communications of the ACM, October 1988
 *
 * Name            : Rngs.java  (Random Number Generation - Multiple Streams)
 * Authors         : Steve Park & Dave Geyer
 * Translated by   : Jun Wang & Richard Dutton
 * Language        : Java
 * Latest Revision : 6-10-04
 * ------------------------------------------------------------------------- 
 */

import java.io.*;
import java.util.*;


public class Rngs {

  long MODULUS      = 2147483647; /* DON'T CHANGE THIS VALUE                  */
  long MULTIPLIER   = 48271;      /* DON'T CHANGE THIS VALUE                  */
  static long CHECK = 399268537L; /* DON'T CHANGE THIS VALUE                  */
  long DEFAULT      = 123456789L; /* initial seed, use 0 < DEFAULT < MODULUS  */

  int STREAMS       = 256;        /* # of streams, DON'T CHANGE THIS VALUE    */
  long A256         = 22925;      /* jump multiplier, DON'T CHANGE THIS VALUE */

  /* Barry Lawson 8 Nov 2007 */
  // Consistent with the changes to the Rvgs constructor, the seed[] array and
  // its associated variables should not be declared static.  If they are, 
  // different instances of the Rngs class (which may be considered as
  // _different_ generators) would share the same seed[] array.  Instead,
  // each Rngs instance should have its own copy of seed[].

//  static long[] seed;                     /* current state of each stream   */
//  static int  stream        = 0;          /* stream index, 0 is the default */
//  static int  initialized   = 0;          /* test for stream initialization */

  long[] seed;                     /* current state of each stream   */
  int  stream        = 0;          /* stream index, 0 is the default */
  int  initialized   = 0;          /* test for stream initialization */
  /* Barry Lawson 8 Nov 2007 */
  

  public static void main(String[] args) {
      
    Rngs r = new Rngs();
    r.testRandom();
  }


  public Rngs () {
    seed = new long[STREAMS];

    /* Barry Lawson 8 Nov 2007 */
    // The C version by default has the first entry in the seed[] array
    // set to DEFAULT even if you don't use PlantSeeds(). The Java 
    // version should do the same, otherwise calls to random() without a
    // preceding plantSeeds() or selectStream() will always return 1.0
    // (see comments in Rvgs.java)
    seed[0] = DEFAULT;
    /* Barry Lawson 8 Nov 2007 */
  }
  
  public double random() {
/* ----------------------------------------------------------------
 * Random returns a pseudo-random real number uniformly distributed
 * between 0.0 and 1.0.
 * ----------------------------------------------------------------
 */
    long Q = MODULUS / MULTIPLIER;
    long R = MODULUS % MULTIPLIER;
    long t;

    t = MULTIPLIER * (seed[stream] % Q) - R * (seed[stream] / Q);
    if (t > 0)
      seed[stream] = t;
    else
      seed[stream] = t + MODULUS;
    return ((double) seed[stream] / MODULUS);
  }

  public void plantSeeds(long x) {
/* ---------------------------------------------------------------------
 * Use this function to set the state of all the random number generator
 * streams by "planting" a sequence of states (seeds), one per stream,
 * with all states dictated by the state of the default stream.
 * The sequence of planted states is separated one from the next by
 * 8,367,782 calls to Random().
 * ---------------------------------------------------------------------
 */
    long Q = MODULUS / A256;
    long R = MODULUS % A256;
    int  j;
    int  s;

    initialized = 1;
    s = stream;                            /* remember the current stream */
    selectStream(0);                       /* change to stream 0          */
    putSeed(x);                            /* set seed[0]                 */
    stream = s;                            /* reset the current stream    */
    for (j = 1; j < STREAMS; j++) {
      x = A256 * (seed[j - 1] % Q) - R * (seed[j - 1] / Q);
      if (x > 0)
        seed[j] = x;
      else
        seed[j] = x + MODULUS;
    }
  }
  
    public void putSeed(long x) {
/* ---------------------------------------------------------------
 * Use this function to set the state of the current random number
 * generator stream according to the following conventions:
 *    if x > 0 then x is the state (unless too large)
 *    if x < 0 then the state is obtained from the system clock
 *    if x = 0 then the state is to be supplied interactively
 * ---------------------------------------------------------------
 */
    boolean ok = false;

    if (x > 0)
      x = x % MODULUS;                            /* correct if x is too large  */
    if (x < 0) {
      Date now = new Date();
      x = now.getTime();
//      x = ((unsigned long) time((time_t *) NULL)) % MODULUS;
    }
    if (x == 0)
      while (!ok) {
        try {
          System.out.print("\nEnter a positive integer seed (9 digits or less) >> ");
          String line;
          InputStreamReader r = new InputStreamReader(System.in);
          BufferedReader ReadThis = new BufferedReader(r);
	  
          line = ReadThis.readLine();
          x = Long.parseLong(line);
        } catch (IOException e) {
        } catch (NumberFormatException nfe) {
        }
        ok = (0 < x) && (x < MODULUS);
        if (!ok)
          System.out.println("\nInput out of range ... try again");
      }
      
    seed[stream] = x;
  }

  public long getSeed() {
/* ---------------------------------------------------------------
 * Use this function to get the state of the current random number
 * generator stream.
 * ---------------------------------------------------------------
 */
    return seed[stream];
  }

   public void selectStream(int index) {
/* ------------------------------------------------------------------
 * Use this function to set the current random number generator
 * stream -- that stream from which the next random number will come.
 * ------------------------------------------------------------------
 */
    stream = index % STREAMS;
    if ((initialized == 0) && (stream != 0))   /* protect against        */
      plantSeeds(DEFAULT);                     /* un-initialized streams */
  }
  
/* ------------------------------------------------------------------
 * Use this (optional) function to test for a correct implementation.
 * ------------------------------------------------------------------
 */
  public void testRandom() {
    long   i;
    long   x;
    double u;
    boolean ok = false;

    selectStream(0);                  /* select the default stream */
    putSeed(1);                       /* and set the state to 1    */
    for(i = 0; i < 10000; i++)
      u = random();
    x = getSeed();                    /* get the new state value   */
    ok = (x == CHECK);                /* and check for correctness */
    
    selectStream(1);                  /* select stream 1                 */
    plantSeeds(1);                    /* set the state of all streams    */
    x = getSeed();                    /* get the state of stream 1       */
    ok = ok && (x == A256);           /* x should be the jump multiplier */
    if (ok)
      System.out.println("\n The implementation of Rngs.java is correct");
    else
      System.out.println("\n ERROR - the implementation of Rngs.java is not correct");
  }
}
