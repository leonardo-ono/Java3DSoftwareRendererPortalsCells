package math;

/**
 *
 * @author Leo
 */
public class InvSqrtTest {

    public static void main(String[] args) {
        
        //double d = 1/Math.sqrt(476.476);
        //System.out.println("d=" + d);
        System.out.println("d=" + clamp(434, 25, 255));
        //test1();
        //test2();
        
        System.out.println("" + Integer.toBinaryString(-15) + " " + (-334 >> 31));
    }

    // https://www.gamedev.net/forums/topic/256880-fast-clamp/2558338/
    public static int clamp(int a, int lo, int hi) {	
        lo -= a;
        hi -= a;	
        return a + (lo & -(lo >> 31) - 1) + (hi & (hi >> 31));
    }
    
    private static int COUNT = 50000;
    
    private static void test1() {
        long startTime = System.nanoTime();
        double total = 0;
        for (int i = 0; i < COUNT; i++) {
            double inv = 1.0 /Math.sqrt(i);
            total += inv;
        }
        long deltaTime = System.nanoTime() - startTime;
        System.out.println("time=" + deltaTime);
    }

    private static void test2() {
        long startTime = System.nanoTime();
        float total = 0;
        for (int i = 0; i < COUNT; i++) {
            float inv = invSqrt(i);
            total += inv;
        }
        long deltaTime = System.nanoTime() - startTime;
        System.out.println("time=" + deltaTime);
    }
    
    // https://en.wikipedia.org/wiki/Fast_inverse_square_root
    public static float invSqrt(float number) {
	float threehalfs = 1.5f;
	float x2 = number * 0.5f;
	float y  = number;
	int i  = Float.floatToIntBits(y);           // evil floating point bit level hacking
	i  = 0x5f3759df - ( i >> 1 );               // what the fuck? 
	y  = Float.intBitsToFloat(i);
	y  = y * ( threehalfs - ( x2 * y * y ) );   // 1st iteration
	//y  = y * ( threehalfs - ( x2 * y * y ) );   // 2nd iteration, this can be removed
	return y;
    }
    
    // https://en.wikipedia.org/wiki/Fast_inverse_square_root
    public static double invSqrtDouble(double number) {
	double threehalfs = 1.5f;
	double x2 = number * 0.5f;
	double y  = number;
	long i  = Double.doubleToLongBits(y);           // evil floating point bit level hacking
	i  = 0x5f3759df - ( i >> 1 );               // what the fuck? 
	y  = Double.longBitsToDouble(i);
	y  = y * ( threehalfs - ( x2 * y * y ) );   // 1st iteration
	y  = y * ( threehalfs - ( x2 * y * y ) );   // 2nd iteration, this can be removed
	return y;
    }
    
}
