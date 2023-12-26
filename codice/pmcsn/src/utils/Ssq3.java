package utils;/* -------------------------------------------------------------------------
 * This program is a next-event simulation of a single-server FIFO service
 * node using Exponentially distributed interarrival times and Uniformly
 * distributed service times (i.e., a M/U/1 queue).  The service node is
 * assumed to be initially idle, no arrivals are permitted after the
 * terminal time STOP, and the service node is then purged by processing any
 * remaining jobs in the service node.
 *
 * Name              : Ssq3.java  (Single Server Queue, version 3)
 * Authors           : Steve Park & Dave Geyer
 * Translated by     : Jun Wang
 * Language          : Java
 * Latest Revision   : 6-16-06
 * ------------------------------------------------------------------------- 
 */

import utils.Rngs;

import java.lang.*;
import java.text.*;


class Ssq3Area {                
  double node;                    /* time integrated number in the node  */
  double queue;                   /* time integrated number in the queue */
  double service;                 /* time integrated number in service   */

  void initAreaParas() {
    node = 0.0;
    queue = 0.0;
    service = 0.0;
  }
}

class Ssq3T {
  double arrival;                 /* next arrival time                   */
  double completion;              /* next completion time                */
  double current;                 /* current time                        */
  double next;                    /* next (most imminent) event time     */
  double last;                    /* last arrival time                   */
}


class Ssq3 {

  static double START = 0.0;              /* initial time                   */
  static double STOP  = 20000.0;          /* terminal (close the door) time */
  static double INFINITY = 100.0 * STOP;  /* must be much larger than STOP  */
  
  static double sarrival = START;              /* Why did I do this?       */

  public static void main(String[] args) {
    
    long index  = 0;                  /* used to count departed jobs         */
    long number = 0;                  /* number in the node                  */

    Ssq3 s = new Ssq3();
    
    Rngs r = new Rngs();
    r.plantSeeds(123456789);

    Ssq3T t      = new Ssq3T();
    t.current    = START;           /* set the clock                         */
    t.arrival    = s.getArrival(r); /* schedule the first arrival            */
    t.completion = INFINITY;        /* the first event can't be a completion */

    Ssq3Area area = new Ssq3Area();
    area.initAreaParas();

    while ((t.arrival < STOP) || (number > 0)) {
      t.next          = Math.min(t.arrival, t.completion);  /* next event time   */
      if (number > 0)  {                               /* update integrals  */
        area.node    += (t.next - t.current) * number;
        area.queue   += (t.next - t.current) * (number - 1);
        area.service += (t.next - t.current);
      }
      t.current       = t.next;                    /* advance the clock */

      if (t.current == t.arrival)  {               /* process an arrival */
        number++;
        t.arrival     = s.getArrival(r);
        if (t.arrival > STOP)  {
          t.last      = t.current;
          t.arrival   = INFINITY;
        }
        if (number == 1)
          t.completion = t.current + s.getService(r);
      }
      else {                                        /* process a completion */
        index++;
        number--;
        if (number > 0)
          t.completion = t.current + s.getService(r);
        else
          t.completion = INFINITY;
      }
    }

    DecimalFormat f = new DecimalFormat("###0.00");

    System.out.println("\nfor " + index + " jobs");
    System.out.println("   average interarrival time =   " + f.format(t.last / index));
    System.out.println("   average wait ............ =   " + f.format(area.node / index));
    System.out.println("   average delay ........... =   " + f.format(area.queue / index));
    System.out.println("   average service time .... =   " + f.format(area.service / index));
    System.out.println("   average # in the node ... =   " + f.format(area.node / t.current));
    System.out.println("   average # in the queue .. =   " + f.format(area.queue / t.current));
    System.out.println("   utilization ............. =   " + f.format(area.service / t.current));
  }


  double exponential(double m, Rngs r) {
/* ---------------------------------------------------
 * generate an Exponential random variate, use m > 0.0
 * ---------------------------------------------------
 */
    return (-m * Math.log(1.0 - r.random()));
  }

  double uniform(double a, double b, Rngs r) {
/* ------------------------------------------------
 * generate an Uniform random variate, use a < b
 * ------------------------------------------------
 */
    return (a + (b - a) * r.random());
  }

   double getArrival(Rngs r) {
/* ---------------------------------------------
 * generate the next arrival time, with rate 1/2
 * ---------------------------------------------
 */
    r. selectStream(0);
    sarrival += exponential(2.0, r);
    return (sarrival);
  }


  double getService(Rngs r) {
/* --------------------------------------------
 * generate the next service time with rate 2/3
 * --------------------------------------------
 */
    r. selectStream(1);
    return (uniform(1.0, 2.0, r)); //dovrebbe essere exponential(2.0, r)???
  }

}
