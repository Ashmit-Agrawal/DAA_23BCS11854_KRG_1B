class Solution {
    static int knapsack(int W,int val[], int wt[]) {
        int n = wt.length;
        int dp[] = new int[W + 1];

        for (int i = 0; i < n; i++) {
            for (int w = W; w >= wt[i]; w--) {
                dp[w] = Math.max(dp[w], val[i] + dp[w - wt[i]]);
            }
        }
        return dp[W];
    }
}
