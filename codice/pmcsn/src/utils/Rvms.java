package utils;
/* ------------------------------------------------------------------------- 
 * This is a Java library that can be used to evaluate the probability 
 * density functions (pdf's), cumulative distribution functions (cdf's), and 
 * inverse distribution functions (idf's) for a variety of discrete and 
 * continuous random variables.
 *
 * The following notational conventions are used
 *                 x : possible value of the random variable
 *                 u : real variable (probability) between 0.0 and 1.0 
 *  a, b, n, p, m, s : distribution-specific parameters
 *
 * There are pdf's, cdf's and idf's for 6 discrete random variables
 *
 *      Random Variable    Range (x)  Mean         Variance
 *
 *      bernoulli(p)       0..1       p            p*(1-p)
 *      binomial(n, p)     0..n       n*p          n*p*(1-p)
 *      equilikely(a, b)   a..b       (a+b)/2      ((b-a+1)*(b-a+1)-1)/12 
 *      geometric(p)       0...       p/(1-p)      p/((1-p)*(1-p))
 *      pascal(n, p)       0...       n*p/(1-p)    n*p/((1-p)*(1-p))
 *      poisson(m)         0...       m            m
 *
 * and for 7 continuous random variables
 *
 *      uniform(a, b)      a < x < b  (a+b)/2      (b-a)*(b-a)/12
 *      exponential(m)     x > 0      m            m*m
 *      erlang(n, b)       x > 0      n*b          n*b*b
 *      normal(m, s)       all x      m            s*s
 *      logNormal(a, b)    x > 0         see below
 *      chiSquare(n)       x > 0      n            2*n
 *      student(n)         all x      0  (n > 1)   n/(n-2)   (n > 2)
 *
 * For the Lognormal(a, b), the mean and variance are
 *
 *                        mean = Exp(a + 0.5*b*b)
 *                    variance = (Exp(b*b) - 1)*Exp(2*a + b*b)
 *
 * Name            : Rvms.java (Random Variable ModelS)
 * Authors         : Steve Park & Dave Geyer
 * Translated by   : Richard Dutton & Jun Wang
 * Language        : Java
 * Latest Revision : 7-1-04
 * ------------------------------------------------------------------------- 
 */

import java.math.*;

public class Rvms{
    final double TINY = 1.0e-10;
    final double SQRT2PI = 2.506628274631;	/* sqrt(2 * pi) */
    
    public Rvms(){}
    

    public double pdfBernoulli(double p, long x)
/* =======================================
 * NOTE: use 0.0 < p < 1.0 and 0 <= x <= 1
 * =======================================
 */
    {
	return ((x == 0) ? 1.0 - p : p);
    }

    public double cdfBernoulli(double p, long x)
/* =======================================
 * NOTE: use 0.0 < p < 1.0 and 0 <= x <= 1 
 * =======================================
 */
    {
	return ((x == 0) ? 1.0 - p : 1.0);
    }

    public long idfBernoulli(double p, double u)
/* =========================================
 * NOTE: use 0.0 < p < 1.0 and 0.0 < u < 1.0 
 * =========================================
 */
    {
	return ((u < 1.0 - p) ? 0 : 1);
    }

    public double pdfEquilikely(long a, long b, long x)
/* ============================================ 
 * NOTE: use a <= x <= b 
 * ============================================
 */
    {
	return (1.0 / (b - a + 1.0));
    }

    public double cdfEquilikely(long a, long b, long x)
/* ============================================
 * NOTE: use a <= x <= b 
 * ============================================
 */
    {
	return ((x - a + 1.0) / (b - a + 1.0));
    }

    public long idfEquilikely(long a, long b, double u)
/* ============================================ 
 * NOTE: use a <= b and 0.0 < u < 1.0 
 * ============================================
 */
    {
	return (a + (long) (u * (b - a + 1)));
    }

    public double pdfBinomial(long n, double p, long x)
/* ============================================ 
 * NOTE: use 0 <= x <= n and 0.0 < p < 1.0 
 * ============================================
 */
    {
	double s, t;
	
	s = logChoose(n, x);
	t = x * Math.log(p) + (n - x) * Math.log(1.0 - p);
	return (Math.exp(s + t));
    }

    public double cdfBinomial(long n, double p, long x)
/* ============================================ 
 * NOTE: use 0 <= x <= n and 0.0 < p < 1.0 
 * ============================================
 */
    {
	if (x < n)
	    return (1.0 - inBeta(x + 1, n - x, p));
	else
	    return (1.0);
    }

    public long idfBinomial(long n, double p, double u)
/* ================================================= 
 * NOTE: use 0 <= n, 0.0 < p < 1.0 and 0.0 < u < 1.0 
 * =================================================
 */
    {
	long x = (long) (n * p);             /* start searching at the mean */
	
	if (cdfBinomial(n, p, x) <= u)
	    while (cdfBinomial(n, p, x) <= u)
		x++;
	else if (cdfBinomial(n, p, 0) <= u)
	    while (cdfBinomial(n, p, x - 1) > u)
		x--;
	else
	    x = 0;
	return (x);
    }

    public double pdfGeometric(double p, long x)
/* ===================================== 
 * NOTE: use 0.0 < p < 1.0 and x >= 0 
 * =====================================
 */
    {
	return ((1.0 - p) * Math.exp(x * Math.log(p)));
    }

    public double cdfGeometric(double p, long x)
/* ===================================== 
 * NOTE: use 0.0 < p < 1.0 and x >= 0 
 * =====================================
 */
    {
	return (1.0 - Math.exp((x + 1) * Math.log(p)));
    }

    public long idfGeometric(double p, double u)
/* ========================================= 
 * NOTE: use 0.0 < p < 1.0 and 0.0 < u < 1.0 
 * =========================================
 */
    {
	return ((long) (Math.log(1.0 - u) / Math.log(p)));
    }

    public double pdfPascal(long n, double p, long x)
/* =========================================== 
 * NOTE: use n >= 1, 0.0 < p < 1.0, and x >= 0 
 * ===========================================
 */
    {
	double  s, t;
	
	s = logChoose(n + x - 1, x);
	t = x * Math.log(p) + n * Math.log(1.0 - p);
	return (Math.exp(s + t));
    }

    public double cdfPascal(long n, double p, long x)
/* =========================================== 
 * NOTE: use n >= 1, 0.0 < p < 1.0, and x >= 0 
 * ===========================================
 */
    {
	return (1.0 - inBeta(x + 1, n, p));
    }

    public long idfPascal(long n, double p, double u)
/* ================================================== 
 * NOTE: use n >= 1, 0.0 < p < 1.0, and 0.0 < u < 1.0 
 * ==================================================
 */
    {
	long x = (long) (n * p / (1.0 - p));    /* start searching at the mean */
	
	if (cdfPascal(n, p, x) <= u)
	    while (cdfPascal(n, p, x) <= u)
		x++;
	else if (cdfPascal(n, p, 0) <= u)
	    while (cdfPascal(n, p, x - 1) > u)
		x--;
	else
	    x = 0;
	return (x);
    }

    public double pdfPoisson(double m, long x)
/* ===================================
 * NOTE: use m > 0 and x >= 0 
 * ===================================
 */
    {
	double t;
	
	t = - m + x * Math.log(m) - logFactorial(x);
	return (Math.exp(t));
    }

    public double cdfPoisson(double m, long x)
/* =================================== 
 * NOTE: use m > 0 and x >= 0 
 * ===================================
 */
    {
	return (1.0 - inGamma(x + 1, m));
    }

    public long idfPoisson(double m, double u)
/* =================================== 
 * NOTE: use m > 0 and 0.0 < u < 1.0 
 * ===================================
 */
    {
	long x = (long) m;                    /* start searching at the mean */
	
	if (cdfPoisson(m, x) <= u)
	    while (cdfPoisson(m, x) <= u)
		x++;
	else if (cdfPoisson(m, 0) <= u)
	    while (cdfPoisson(m, x - 1) > u)
		x--;
	else
	    x = 0;
	return (x);
    }

    public double pdfUniform(double a, double b, double x)
/* =============================================== 
 * NOTE: use a < x < b 
 * ===============================================
 */
    {
	return (1.0 / (b - a));
    }

    public double cdfUniform(double a, double b, double x)
/* =============================================== 
 * NOTE: use a < x < b 
 * ===============================================
 */
    {
	return ((x - a) / (b - a));
    }

    public double idfUniform(double a, double b, double u)
/* =============================================== 
 * NOTE: use a < b and 0.0 < u < 1.0 
 * ===============================================
 */
    {
	return (a + (b - a) * u);
    }

    public double pdfExponential(double m, double x)
/* ========================================= 
 * NOTE: use m > 0 and x > 0 
 * =========================================
 */
    {
	return ((1.0 / m) * Math.exp(- x / m));
    }

    public double cdfExponential(double m, double x)
/* ========================================= 
 * NOTE: use m > 0 and x > 0 
 * =========================================
 */
    {
	return (1.0 - Math.exp(- x / m));
    }

    public double idfExponential(double m, double u)
/* ========================================= 
 * NOTE: use m > 0 and 0.0 < u < 1.0 
 * =========================================
 */
    {
	return (- m * Math.log(1.0 - u));
    }

    public double pdfErlang(long n, double b, double x)
	/* ============================================ 
	 * NOTE: use n >= 1, b > 0, and x > 0 
	 * ============================================
	 */
    {
	double t;

	t = (n - 1) * Math.log(x / b) - (x / b) - Math.log(b) - logGamma(n);
	return (Math.exp(t));
    }

    public double cdfErlang(long n, double b, double x)
	/* ============================================ 
	 * NOTE: use n >= 1, b > 0, and x > 0 
	 * ============================================
	 */
    {
	return (inGamma(n, x / b));
    }

    public double idfErlang(long n, double b, double u)
	/* ============================================ 
	 * NOTE: use n >= 1, b > 0 and 0.0 < u < 1.0 
	 * ============================================
	 */
    {
	double t, x = n * b;                   /* initialize to the mean, then */

	do {                                   /* use Newton-Raphson iteration */
	    t = x;
	    x = t + (u - cdfErlang(n, b, t)) / pdfErlang(n, b, t);
	    if (x <= 0.0)
		x = 0.5 * t;
	} while (Math.abs(x - t) >= TINY);
	return (x);
    }

    public double pdfStandard(double x)
	/* =================================== 
	 * NOTE: x can be any value 
	 * ===================================
	 */
    {
	return (Math.exp(- 0.5 * x * x) / SQRT2PI);
    }

    public double cdfStandard(double x)
	/* =================================== 
	 * NOTE: x can be any value 
	 * ===================================
	 */
    { 
	double t;

	t = inGamma(0.5, 0.5 * x * x);
	if (x < 0.0)
	    return (0.5 * (1.0 - t));
	else
	    return (0.5 * (1.0 + t));
    }

    public double idfStandard(double u)
	/* =================================== 
	 * NOTE: 0.0 < u < 1.0 
	 * ===================================
	 */
    { 
	double t, x = 0.0;                    /* initialize to the mean, then  */

	do {                                  /* use Newton-Raphson iteration  */
	    t = x;
	    x = t + (u - cdfStandard(t)) / pdfStandard(t);
	} while (Math.abs(x - t) >= TINY);
	return (x);
    }

    public double pdfNormal(double m, double s, double x)
	/* ============================================== 
	 * NOTE: x and m can be any value, but s > 0.0 
	 * ==============================================
	 */
    { 
	double t = (x - m) / s;

	return (pdfStandard(t) / s);
    }

    public double cdfNormal(double m, double s, double x)
	/* ============================================== 
	 * NOTE: x and m can be any value, but s > 0.0 
	 * ==============================================
	 */
    { 
	double t = (x - m) / s;

	return (cdfStandard(t));
    }

    public double idfNormal(double m, double s, double u)
	/* ======================================================= 
	 * NOTE: m can be any value, but s > 0.0 and 0.0 < u < 1.0 
	 * =======================================================
	 */
    {
	return (m + s * idfStandard(u));
    }

    public double pdfLogNormal(double a, double b, double x)
	/* =================================================== 
	 * NOTE: a can have any value, but b > 0.0 and x > 0.0 
	 * ===================================================
	 */
    { 
	double t = (Math.log(x) - a) / b;

	return (pdfStandard(t) / (b * x));
    }

    public double cdfLogNormal(double a, double b, double x)
	/* =================================================== 
	 * NOTE: a can have any value, but b > 0.0 and x > 0.0 
	 * ===================================================
	 */
    { 
	double t = (Math.log(x) - a) / b;

	return (cdfStandard(t));
    }

    public double idfLogNormal(double a, double b, double u)
	/* ========================================================= 
	 * NOTE: a can have any value, but b > 0.0 and 0.0 < u < 1.0 
	 * =========================================================
	 */
    { 
	double t;

	t = a + b * idfStandard(u);
	return (Math.exp(t));
    }

    public double pdfChiSquare(long n, double x)
	/* ===================================== 
	 * NOTE: use n >= 1 and x > 0.0 
	 * =====================================
	 */
    { 
	double t, s = n / 2.0;

	t = (s - 1.0) * Math.log(x / 2.0) - (x / 2.0) - Math.log(2.0) - logGamma(s);
	return (Math.exp(t));
    }

    public double cdfChiSquare(long n, double x)
	/* ===================================== 
	 * NOTE: use n >= 1 and x > 0.0 
	 * =====================================
	 */
    {
	return (inGamma(n / 2.0, x / 2));
    }

    public double idfChiSquare(long n, double u)
	/* ===================================== 
	 * NOTE: use n >= 1 and 0.0 < u < 1.0 
	 * =====================================
	 */
    { 
	double t, x = n;                         /* initialize to the mean, then */

	do {                                     /* use Newton-Raphson iteration */
	    t = x;
	    x = t + (u - cdfChiSquare(n, t)) / pdfChiSquare(n, t);
	    if (x <= 0.0)
		x = 0.5 * t;
	} while (Math.abs(x - t) >= TINY);
	return (x);
    }

    public double pdfStudent(long n, double x)
	/* =================================== 
	 * NOTE: use n >= 1 and x > 0.0 
	 * ===================================
	 */
    { 
	double s, t;

	s = -0.5 * (n + 1) * Math.log(1.0 + ((x * x) / (double) n));
	t = -logBeta(0.5, n / 2.0);
	return (Math.exp(s + t) / Math.sqrt((double) n));
    }

    public double cdfStudent(long n, double x)
	/* =================================== 
	 * NOTE: use n >= 1 and x > 0.0 
	 * ===================================
	 */
    { 
	double s, t;

	t = (x * x) / (n + x * x);
	s = inBeta(0.5, n / 2.0, t);
	if (x >= 0.0)
	    return (0.5 * (1.0 + s));
	else
	    return (0.5 * (1.0 - s));
    }

    public double idfStudent(long n, double u)
	/* =================================== 
	 * NOTE: use n >= 1 and 0.0 < u < 1.0 
	 * ===================================
	 */
    { 
	double t, x = 0.0;                       /* initialize to the mean, then */

	do {                                     /* use Newton-Raphson iteration */
	    t = x;
	    x = t + (u - cdfStudent(n, t)) / pdfStudent(n, t);
	} while (Math.abs(x - t) >= TINY);
	return (x);
    }

    /* ===================================================================
     * The six functions that follow are a 'special function' mini-library
     * used to support the evaluation of pdf, cdf and idf functions.
     * ===================================================================
     */

    public double logGamma(double a)
	/* ======================================================================== 
	 * LogGamma returns the natural log of the gamma function.
	 * NOTE: use a > 0.0 
	 *
	 * The algorithm used to evaluate the natural log of the gamma function is
	 * based on an approximation by C. Lanczos, SIAM J. Numerical Analysis, B,
	 * vol 1, 1964.  The constants have been selected to yield a relative error
	 * which is less than 2.0e-10 for all positive values of the parameter a.    
	 * ======================================================================== 
	 */
    { 
	double s[] = new double[6];
	double sum, temp;
	int    i;

	s[0] =  76.180091729406 / a;
	s[1] = -86.505320327112 / (a + 1.0);
	s[2] =  24.014098222230 / (a + 2.0);
	s[3] =  -1.231739516140 / (a + 3.0);
	s[4] =   0.001208580030 / (a + 4.0);
	s[5] =  -0.000005363820 / (a + 5.0);
	sum  =   1.000000000178;
	for (i = 0; i < 6; i++) 
	    sum += s[i];
	temp = (a - 0.5) * Math.log(a + 4.5) - (a + 4.5) + Math.log(SQRT2PI * sum);
	return (temp);
    }

    public double logFactorial(long n)
	/* ==================================================================
	 * LogFactorial(n) returns the natural log of n!
	 * NOTE: use n >= 0
	 *
	 * The algorithm used to evaluate the natural log of n! is based on a
	 * simple equation which relates the gamma and factorial functions.
	 * ==================================================================
	 */
    {
	return (logGamma(n + 1));
    }

    public double logBeta(double a, double b)
	/* ======================================================================
	 * LogBeta returns the natural log of the beta function.
	 * NOTE: use a > 0.0 and b > 0.0
	 *
	 * The algorithm used to evaluate the natural log of the beta function is 
	 * based on a simple equation which relates the gamma and beta functions.
	 *
	 */
    { 
	return (logGamma(a) + logGamma(b) - logGamma(a + b));
    }

    public double logChoose(long n, long m)
	/* ========================================================================
	 * LogChoose returns the natural log of the binomial coefficient C(n,m).
	 * NOTE: use 0 <= m <= n
	 *
	 * The algorithm used to evaluate the natural log of a binomial coefficient
	 * is based on a simple equation which relates the beta function to a
	 * binomial coefficient.
	 * ========================================================================
	 */
    {
	if (m > 0)
	    return (-logBeta(m, n - m + 1) - Math.log(m));
	else
	    return (0.0);
    }

    public double inGamma(double a, double x)
	/* ========================================================================
	 * Evaluates the incomplete gamma function.
	 * NOTE: use a > 0.0 and x >= 0.0
	 *
	 * The algorithm used to evaluate the incomplete gamma function is based on
	 * Algorithm AS 32, J. Applied Statistics, 1970, by G. P. Bhattacharjee.
	 * See also equations 6.5.29 and 6.5.31 in the Handbook of Mathematical
	 * Functions, Abramowitz and Stegum (editors).  The absolute error is less 
	 * than 1e-10 for all non-negative values of x.
	 * ========================================================================
	 */
    { 
	double t, sum, term, factor, f, g;
	double c[] = new double[2];
	double p[] = new double[3];
	double q[] = new double[3];
	long   n;

	if (x > 0.0)
	    factor = Math.exp(-x + a * Math.log(x) - logGamma(a));
	else
	    factor = 0.0;
	if (x < a + 1.0) {                 /* evaluate as an infinite series - */
	    t    = a;                        /* A & S equation 6.5.29            */
	    term = 1.0 / a;
	    sum  = term;
	    while (term >= TINY * sum) {     /* sum until 'term' is small */
		t++;
		term *= x / t;
		sum  += term;
	    } 
	    return (factor * sum);
	}
	else {                             /* evaluate as a continued fraction - */
	    p[0]  = 0.0;                     /* A & S eqn 6.5.31 with the extended */
	    q[0]  = 1.0;                     /* pattern 2-a, 2, 3-a, 3, 4-a, 4,... */
	    p[1]  = 1.0;                     /* - see also A & S sec 3.10, eqn (3) */
	    q[1]  = x;
	    f     = p[1] / q[1];
	    n     = 0;
	    do {                             /* recursively generate the continued */
		g  = f;                        /* fraction 'f' until two consecutive */
		n++;                           /* values are small                   */
		if ((n % 2) > 0) {
		    c[0] = ((double) (n + 1) / 2) - a;
		    c[1] = 1.0;
		}
		else {
		    c[0] = (double) n / 2;
		    c[1] = x;
		}
		p[2] = c[1] * p[1] + c[0] * p[0];
		q[2] = c[1] * q[1] + c[0] * q[0];
		if (q[2] != 0.0) {             /* rescale to avoid overflow */
		    p[0] = p[1] / q[2];
		    q[0] = q[1] / q[2];
		    p[1] = p[2] / q[2];
		    q[1] = 1.0;
		    f    = p[1];
		}
	    } while ((Math.abs(f - g) >= TINY) || (q[1] != 1.0));
	    return (1.0 - factor * f);
	}
    }

    public double inBeta(double a, double b, double x)
	/* ======================================================================= 
	 * Evaluates the incomplete beta function.
	 * NOTE: use a > 0.0, b > 0.0 and 0.0 <= x <= 1.0
	 *
	 * The algorithm used to evaluate the incomplete beta function is based on
	 * equation 26.5.8 in the Handbook of Mathematical Functions, Abramowitz
	 * and Stegum (editors).  The absolute error is less than 1e-10 for all x
	 * between 0 and 1.
	 * =======================================================================
	 */
    { 
	double t, factor, f, g, c;
	double p[] = new double[3];
	double q[] = new double[3];
	boolean    swap;
	long   n;

	if (x > (a + 1.0) / (a + b + 1.0)) { /* to accelerate convergence   */
	    swap = true;                          /* complement x and swap a & b */
	    x    = 1.0 - x;
	    t    = a;
	    a    = b;
	    b    = t;
	}
	else                                 /* do nothing */
	    swap = false;
	if (x > 0)
	    factor = Math.exp(a * Math.log(x) + b * Math.log(1.0 - x) - logBeta(a,b)) / a;
	else
	    factor = 0.0;
	p[0] = 0.0;
	q[0] = 1.0;
	p[1] = 1.0;
	q[1] = 1.0;
	f    = p[1] / q[1];
	n    = 0;
	do {                               /* recursively generate the continued */
	    g = f;                           /* fraction 'f' until two consecutive */
	    n++;                             /* values are small                   */
	    if ((n % 2) > 0) {
		t = (double) (n - 1) / 2;
		c = -(a + t) * (a + b + t) * x / ((a + n - 1.0) * (a + n));
	    }
	    else {
		t = (double) n / 2;
		c = t * (b - t) * x / ((a + n - 1.0) * (a + n));
	    }
	    p[2] = p[1] + c * p[0];
	    q[2] = q[1] + c * q[0];
	    if (q[2] != 0.0) {                 /* rescale to avoid overflow */
		p[0] = p[1] / q[2];
		q[0] = q[1] / q[2];
		p[1] = p[2] / q[2];
		q[1] = 1.0;
		f    = p[1];
	    }
	} while ((Math.abs(f - g) >= TINY) || (q[1] != 1.0));
	if (swap) 
	    return (1.0 - factor * f);
	else
	    return (factor * f);
    }
}
