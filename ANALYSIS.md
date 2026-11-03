# Assignment 4 Analysis
## 1. LCS (Longest Common Subsequence)
**Logic:** The DP table `dp[i][j]` holds the maximum length of a common subsequence for the first `i` characters of string `a` and the first `j` characters of string `b`. 
**Transitions:** If the current characters match (`a[i-1] == b[j-1]`), we take the diagonal value and add 1. Otherwise, we take the maximum of the cell above or the cell to the left.
**Reconstruction:** Starting from the bottom-right of the table, if characters match, we move diagonally and add the character to our result. If they don't, we move toward the larger of the two adjacent cells (up or left) until we hit an edge.
**Complexity:** Time and space are both `O(nm)`. 

## 2. Merge Ordered Blocks (Interval DP)
**Logic:** `dp[l][r]` is the minimum cost to merge blocks from original index `l` to `r`. 
**Transitions:** To merge an interval `[l, r]`, we try every possible split point `k`. The cost is the sum of the left and right child merges, plus the total sum of the sizes in `[l, r]`. We use prefix sums to calculate the range sum in `O(1)`.
**Reconstruction:** I store the best split point `k` for each `[l, r]` in a separate `split` array. A recursive helper function processes the left side, then the right side, and finally appends the parent merge `[l, k, r]`. This guarantees we output child merges before the parent merge.
**Complexity:** Time is `O(n^3)` and space is `O(n^2)`.

## 3. Tree Selection (Maximum Weight Independent Set)
**Logic:** For each node `u`, we track two values: `take[u]` (max weight if we include `u`) and `skip[u]` (max weight if we exclude `u`).
**Transitions:** If we take `u`, we are forced to skip its children: `take[u] = weight[u] + sum(skip[v])`. If we skip `u`, we can either take or skip the children: `skip[u] = sum(max(take[v], skip[v]))`.
**Processing order:** Because `n` can be up to 100,000 and the tree might be a deep chain, I used an iterative stack-based DFS to build the parent array and postorder sequence. This avoids `StackOverflowError`.
**Reconstruction:** We walk top-down. If a node is not blocked by its parent and `take[u] > skip[u]`, we select it and mark its children as blocked. Otherwise, we skip it and let the children decide.
**Complexity:** Time and space are `O(n)`.

## 4. Naive, KMP, and Rabin-Karp Search
**Naive:** This is a simple double loop checking every possible window. Time is `O(nm)` and space is `O(1)`.
**KMP:** To avoid restarting the search after a mismatch, we build a prefix function (`pi`) where `pi[i]` is the length of the longest proper prefix of `pattern[0..i]` that is also a suffix. On a mismatch, we fall back using `q = pi[q-1]`. When we find a full match, we also fall back to `pi[q-1]` to catch overlapping matches. Time is `O(n+m)` and space is `O(m)`.
**Rabin-Karp:** Uses a rolling hash with base 31 and modulus 101. When the text window hash matches the pattern hash, we do an explicit character check to avoid false positives. The hash update is `O(1)`: we subtract the leftmost character's contribution, shift the rest, and add the new rightmost character. Time is `O(n+m)` average, but can degrade to `O(nm)` if there are many hash collisions. Space is `O(1)`.

## 5. Suffix Array and Kasai LCP
**Suffix Array:** I used prefix doubling. Initially, I sort suffixes by their first character. Then, I sort by rank pairs `(rank[i], rank[i+k])`, doubling `k` each round, until all ranks are unique. Java's built-in comparator sort makes this `O(n log^2 n)`.
**Kasai LCP:** This computes the longest common prefix between adjacent suffixes in the sorted array. It runs in `O(n)` by reusing the `h` value from the previous suffix (only decrementing it by 1 and comparing further if possible). 
**Repeated substring:** The longest repeated substring (allowing overlaps) is simply the maximum value in the `lcp` array. 
**Complexity:** Time is `O(n log^2 n)` and space is `O(n)`.

## 6. Aho-Corasick
**Logic:** I build a trie of all patterns and then compute failure links using BFS. If a transition is missing during the search, we follow failure links until we find a match or hit the root.
**Counting efficiently:** Instead of traversing output links for every match (which is too slow for dense matches), I aggregate counts. During the text scan, I increment a `visit` counter for the current state. Then, I propagate these counts up the reverse-failure tree in bottom-up order. This way, each state inherits the match counts of all states that have it as a suffix.
**Complexity:** Trie construction is `O(L)` where `L` is total pattern length. Text processing is `O(|text| + S + p)` where `S` is the number of trie states and `p` is the number of patterns.

## Testing and Comparison
I ran the public test suite (`python tools/run.py --language Java --check`), which passed all 44 cases, including empty inputs, overlaps, large values, and the state reset batch. 

For the search algorithms (Naive, KMP, RK), I verified they produce identical outputs on random and repetitive strings (like `text="aaaaa"`, `pattern="aaa"`). I also tested the Rabin-Karp collision case (`text="abdj"`, `pattern="ab"`) to ensure the explicit character verification step correctly rejects false hash matches.

## AI Acknowledgment
I used an AI assistant to help draft the initial Java implementations and debug a type-signature mismatch with the course template's `Main.java`. I verified all algorithms by running the public checks and tracing through edge cases manually.
