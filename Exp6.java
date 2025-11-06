class Solution {
    static boolean isSubsetSum(int arr[], int sum) {
        int N = arr.length;
        boolean dp[][] = new boolean[N + 1][sum + 1];

        for (int i = 0; i <= N; i++) {
            dp[i][0] = true;
        }

        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= sum; j++) {
                dp[i][j] = dp[i - 1][j];
                if (j >= arr[i - 1]) {
                    dp[i][j] = dp[i][j] || dp[i - 1][j - arr[i - 1]];
                }
            }
        }

        return dp[N][sum];
    }
}
