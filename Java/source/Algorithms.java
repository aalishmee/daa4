import java.util.*;

public class Algorithms {

    public static Object[] lcs(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            char ai = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                if (ai == b.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1] + 1;
                else dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
        StringBuilder sb = new StringBuilder();
        int i = n, j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sb.append(a.charAt(i - 1)); i--; j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return new Object[]{ dp[n][m], sb.reverse().toString() };
    }

    public static Object[] merge(long[] sizes) {
        int n = sizes.length;
        if (n <= 1) return new Object[]{ 0L, new ArrayList<int[]>() };

        long[] pre = new long[n + 1];
        for (int i = 0; i < n; i++) pre[i + 1] = pre[i] + sizes[i];

        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];

        for (int len = 2; len <= n; len++) {
            for (int l = 0; l + len - 1 < n; l++) {
                int r = l + len - 1;
                long best = Long.MAX_VALUE;
                int bk = l;
                for (int k = l; k < r; k++) {
                    long v = dp[l][k] + dp[k + 1][r];
                    if (v < best) { best = v; bk = k; }
                }
                dp[l][r] = best + (pre[r + 1] - pre[l]);
                split[l][r] = bk;
            }
        }

        List<int[]> plan = new ArrayList<>();
        buildPlan(0, n - 1, split, plan);
        return new Object[]{ dp[0][n - 1], plan };
    }

    private static void buildPlan(int l, int r, int[][] split, List<int[]> plan) {
        if (l == r) return;
        int k = split[l][r];
        buildPlan(l, k, split, plan);
        buildPlan(k + 1, r, split, plan);
        plan.add(new int[]{ l, k, r });
    }

    public static Object[] tree(int n, long[] weights, int[][] edges) {
        if (n == 0) return new Object[]{ 0L, new ArrayList<Integer>() };

        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }

        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        int[] order = new int[n];
        int head = 0, tail = 0;
        parent[0] = -2;
        order[tail++] = 0;
        while (head < tail) {
            int u = order[head++];
            for (int v : adj.get(u)) {
                if (parent[v] == -1) {
                    parent[v] = u;
                    order[tail++] = v;
                }
            }
        }

        long[] take = new long[n];
        long[] skip = new long[n];
        for (int idx = n - 1; idx >= 0; idx--) {
            int u = order[idx];
            long t = weights[u], s = 0;
            for (int v : adj.get(u)) {
                if (parent[v] == u) {
                    t += skip[v];
                    s += Math.max(take[v], skip[v]);
                }
            }
            take[u] = t; skip[u] = s;
        }

        List<Integer> selected = new ArrayList<>();
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{ 0, 0 });
        while (!stack.isEmpty()) {
            int[] top = stack.pop();
            int u = top[0]; boolean ptaken = top[1] == 1;
            if (!ptaken && take[u] > skip[u]) {
                selected.add(u);
                for (int v : adj.get(u)) if (parent[v] == u) stack.push(new int[]{ v, 1 });
            } else {
                for (int v : adj.get(u)) if (parent[v] == u) stack.push(new int[]{ v, 0 });
            }
        }
        return new Object[]{ Math.max(take[0], skip[0]), selected };
    }

    public static List<Integer> naive(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        List<Integer> res = new ArrayList<>();
        if (m == 0) { for (int i = 0; i <= n; i++) res.add(i); return res; }
        if (m > n) return res;
        for (int i = 0; i + m <= n; i++) {
            int j = 0;
            while (j < m && text.charAt(i + j) == pattern.charAt(j)) j++;
            if (j == m) res.add(i);
        }
        return res;
    }

    public static List<Integer> kmp(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        List<Integer> res = new ArrayList<>();
        if (m == 0) { for (int i = 0; i <= n; i++) res.add(i); return res; }
        if (m > n) return res;

        int[] pi = new int[m];
        int k = 0;
        for (int i = 1; i < m; i++) {
            while (k > 0 && pattern.charAt(i) != pattern.charAt(k)) k = pi[k - 1];
            if (pattern.charAt(i) == pattern.charAt(k)) k++;
            pi[i] = k;
        }
        int q = 0;
        for (int i = 0; i < n; i++) {
            while (q > 0 && text.charAt(i) != pattern.charAt(q)) q = pi[q - 1];
            if (text.charAt(i) == pattern.charAt(q)) q++;
            if (q == m) { res.add(i - m + 1); q = pi[q - 1]; }
        }
        return res;
    }

    public static List<Integer> rk(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        List<Integer> res = new ArrayList<>();
        if (m == 0) { for (int i = 0; i <= n; i++) res.add(i); return res; }
        if (m > n) return res;

        final long BASE = 31, MOD = 101;
        long hp = 0, ht = 0, pw = 1;
        for (int j = 0; j < m; j++) {
            hp = (hp * BASE + pattern.charAt(j)) % MOD;
            ht = (ht * BASE + text.charAt(j)) % MOD;
            if (j < m - 1) pw = (pw * BASE) % MOD;
        }
        for (int i = 0; i + m <= n; i++) {
            if (hp == ht) {
                int j = 0;
                while (j < m && text.charAt(i + j) == pattern.charAt(j)) j++;
                if (j == m) res.add(i);
            }
            if (i + m < n) {
                ht = ((ht - text.charAt(i) * pw) % MOD + MOD) % MOD;
                ht = (ht * BASE + text.charAt(i + m)) % MOD;
            }
        }
        return res;
    }

    public static Object[] suffix(String text) {
        int n = text.length();
        if (n == 0) return new Object[]{ new int[0], new int[0], "" };

        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) { sa[i] = i; rank[i] = text.charAt(i); }

        int k = 1;
        while (true) {
            final int kk = k;
            final int[] r = rank;
            Arrays.sort(sa, (x, y) -> {
                if (r[x] != r[y]) return Integer.compare(r[x], r[y]);
                int rx = x + kk < n ? r[x + kk] : -1;
                int ry = y + kk < n ? r[y + kk] : -1;
                return Integer.compare(rx, ry);
            });
            int[] nr = new int[n];
            for (int idx = 1; idx < n; idx++) {
                int a = sa[idx - 1], b = sa[idx];
                int ra = rank[a], rb = rank[b];
                int ra2 = a + kk < n ? rank[a + kk] : -1;
                int rb2 = b + kk < n ? rank[b + kk] : -1;
                nr[b] = nr[a] + ((ra != rb || ra2 != rb2) ? 1 : 0);
            }
            rank = nr;
            if (rank[sa[n - 1]] == n - 1) break;
            k <<= 1;
        }

        int[] saInt = new int[n];
        for (int i = 0; i < n; i++) saInt[i] = sa[i];

        int[] lcp = new int[n];
        int[] inv = new int[n];
        for (int i = 0; i < n; i++) inv[saInt[i]] = i;
        int h = 0;
        for (int i = 0; i < n; i++) {
            int pos = inv[i];
            if (pos > 0) {
                int j = saInt[pos - 1];
                while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) h++;
                lcp[pos] = h;
                if (h > 0) h--;
            } else h = 0;
        }

        int best = 0, bi = 0;
        for (int i = 0; i < n; i++) if (lcp[i] > best) { best = lcp[i]; bi = i; }
        String rep = best > 0 ? text.substring(saInt[bi], saInt[bi] + best) : "";

        return new Object[]{ saInt, lcp, rep };
    }

    static class AcNode {
        HashMap<Character, Integer> ch = new HashMap<>();
        int fail = 0;
    }

    public static List<List<Long>> aho(List<String> patterns, List<String> texts) {
        ArrayList<AcNode> trie = new ArrayList<>();
        trie.add(new AcNode());
        for (int idx = 0; idx < patterns.size(); idx++) {
            int cur = 0;
            for (char c : patterns.get(idx).toCharArray()) {
                Integer nxt = trie.get(cur).ch.get(c);
                if (nxt == null) {
                    nxt = trie.size();
                    trie.get(cur).ch.put(c, nxt);
                    trie.add(new AcNode());
                }
                cur = nxt;
            }
        }
        int S = trie.size();

        ArrayDeque<Integer> dq = new ArrayDeque<>();
        for (Map.Entry<Character, Integer> e : trie.get(0).ch.entrySet()) {
            trie.get(e.getValue()).fail = 0;
            dq.add(e.getValue());
        }
        while (!dq.isEmpty()) {
            int u = dq.poll();
            int fu = trie.get(u).fail;
            for (Map.Entry<Character, Integer> e : trie.get(u).ch.entrySet()) {
                char c = e.getKey(); int v = e.getValue();
                int f = fu;
                while (f != 0 && !trie.get(f).ch.containsKey(c)) f = trie.get(f).fail;
                if (trie.get(f).ch.containsKey(c) && trie.get(f).ch.get(c) != v) f = trie.get(f).ch.get(c);
                else f = 0;
                trie.get(v).fail = f;
                dq.add(v);
            }
        }

        ArrayList<ArrayList<Integer>> rev = new ArrayList<>(S);
        for (int i = 0; i < S; i++) rev.add(new ArrayList<>());
        for (int v = 1; v < S; v++) rev.get(trie.get(v).fail).add(v);
        int[] bottomUp = new int[S];
        int cnt = 0;
        ArrayDeque<Integer> st = new ArrayDeque<>();
        st.push(0);
        int[] pre = new int[S];
        while (!st.isEmpty()) {
            int u = st.pop();
            pre[cnt++] = u;
            for (int v : rev.get(u)) st.push(v);
        }
        for (int i = 0; i < S; i++) bottomUp[i] = pre[S - 1 - i];

        int[] terminal = new int[patterns.size()];
        for (int idx = 0; idx < patterns.size(); idx++) {
            int cur = 0;
            for (char c : patterns.get(idx).toCharArray()) cur = trie.get(cur).ch.get(c);
            terminal[idx] = cur;
        }

        List<List<Long>> result = new ArrayList<>();
        for (String text : texts) {
            long[] visit = new long[S];
            int cur = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                while (cur != 0 && !trie.get(cur).ch.containsKey(c)) cur = trie.get(cur).fail;
                Integer nxt = trie.get(cur).ch.get(c);
                cur = (nxt != null) ? nxt : 0;
                visit[cur]++;
            }
            for (int u : bottomUp) {
                if (u != 0) visit[trie.get(u).fail] += visit[u];
            }
            List<Long> row = new ArrayList<>();
            for (int idx = 0; idx < patterns.size(); idx++) row.add(visit[terminal[idx]]);
            result.add(row);
        }
        return result;
    }
}
