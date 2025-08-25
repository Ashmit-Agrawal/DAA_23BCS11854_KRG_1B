import java.util.*;


public class Exp4 {
    public static void main(String[] args) {
        Solution sol = new Solution();
        int[] arr = {1, 2, 2, 3, 3, 3};
        ArrayList<ArrayList<Integer>> result = sol.countFreq(arr);
        System.out.println(result);
    }
}


class Solution {
    public ArrayList<ArrayList<Integer>> countFreq(int[] arr) {
        HashMap<Integer, Integer> map = new HashMap<>();
    
        for (int num : arr) {
            map.put(num, map.getOrDefault(num, 0) + 1);
        }

        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            ArrayList<Integer> temp = new ArrayList<>();
            temp.add(entry.getKey());
            temp.add(entry.getValue());
            result.add(temp);
        }
        
        return result;
    }
}

