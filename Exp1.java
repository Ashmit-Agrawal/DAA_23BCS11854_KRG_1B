public class Exp1 {
    public static void main(String[] args) {
        Solution sol = new Solution();
        double x = 2.0;
        int n = 10;
        double result = sol.myPow(x, n);
        System.out.println(x + " raised to the power " + n + " is: " + result);
    }
}

class Solution {
    public double myPow(double x, int n) {
        if(n == 0) return 1;
        
        if(n%2 == 0){
            double half = myPow(x, n/2);
            return half * half;
        } else {
            if(n > 0){
                return x * myPow(x, n-1);
            } else {
                return 1/x * myPow(x, n+1);
            } 
        }
    }
}