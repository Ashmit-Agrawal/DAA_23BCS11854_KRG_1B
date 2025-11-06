class Solution {
    static class Pair {
        int v, w;
        Pair(int v, int w) {
            this.v = v;
            this.w = w;
        }
    }

    static int[] dijkstra(int V, int[][] edges, int src) {

        // Adjacency List
        ArrayList<ArrayList<Pair>> graph = new ArrayList<>();
        for(int i = 0; i < V; i++)
            graph.add(new ArrayList<>());

        for (int[] e : edges) {
            graph.get(e[0]).add(new Pair(e[1], e[2]));
            graph.get(e[1]).add(new Pair(e[0], e[2])); 
        }

        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        PriorityQueue<Pair> pq = new PriorityQueue<>((a, b) -> a.w - b.w);
        pq.add(new Pair(src, 0));

        boolean[] visited = new boolean[V];

        while(!pq.isEmpty()) {
            Pair p = pq.poll();
            int u = p.v;

            if(visited[u]) continue;
            visited[u] = true;

            for(Pair it : graph.get(u)) {
                int v = it.v, w = it.w;

                if(dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    pq.add(new Pair(v, dist[v]));
                }
            }
        }
        return dist;
    }
}
