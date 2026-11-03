# Assignment 4 Analysis

## 1. LCS (Longest Common Subsequence)
- **State:** `dp[i][j]` stores the length of the LCS for the first `i` characters of string `a` and the first `j` characters of `b`.
- **Transitions:** If the current characters match, the length increases by 1 (`dp[i][j] = dp[i-1][j-1] + 1`). If they don't match, we take the maximum of dropping a character from `a` or dropping a character from `b` (`max(dp[i-1][j], dp[i][j-1])`).
- **Base Cases:** The first row and first column are 0, meaning the LCS with an empty string is 0.
- **Processing Order:** We fill the table row by row, left to right, because each cell depends on the top, left, and top-left cells.
- **Reconstruction:** We start at `dp[n][m]`. If characters match, we move diagonally and add the character to our result. If they don't, we move toward the larger of the top or left cell.
- **Complexity:** Time is `O(nm)` and space is `O(nm)`.
- **Test:** `a="", b="abc"` returns `length=0, sequence=""`. This checks that empty inputs don't cause out-of-bounds errors.

## 2. Merge Ordered Blocks
- **State:** `dp[l][r]` is the minimum cost to merge all blocks from index `l` to `r`.
- **Transitions:** We try every possible split point `k` between `l` and `r`. The cost is the left merge cost plus the right merge cost, plus the sum of the sizes in `[l, r]`. I used a prefix sum array so the range sum takes `O(1)` time.
- **Base Case:** `dp[i][i] = 0` because a single block needs no merging.
- **Processing Order:** We evaluate intervals of increasing length (`len = 2` to `n`) so that smaller sub-problems are solved first.
- **Reconstruction:** We store the optimal split `k` in a `split[l][r]` array. A recursive function outputs the left child, right child, and then the parent merge, ensuring chronological order.
- **Complexity:** Time is `O(n^3)` and space is `O(n^2)`.
- **Test:** `sizes=[0]` returns `cost=0, plan=[]`. This checks single-block handling.

## 3. Tree Selection (Maximum Weight Independent Set)
- **State:** For each node `u`, we track `take[u]` (max weight if we include `u`) and `skip[u]` (max weight if we exclude `u`).
- **Transitions:** If we take `u`, we must skip its children: `take[u] = weight[u] + sum(skip[v])`. If we skip `u`, we can take the best of either choice for children: `skip[u] = sum(max(take[v], skip[v]))`.
- **Processing Order:** Because `n` can be up to 100,000 and form a deep chain, I used an iterative stack-based DFS. This avoids `StackOverflowError`. We process nodes in bottom-up post-order.
- **Reconstruction:** We walk top-down. If a node is not blocked by its parent and `take[u] > skip[u]`, we select it and block its children. Otherwise, we skip it and let children decide.
- **Complexity:** Time and space are `O(n)`.
- **Test:** All negative weights return `weight=0, selected=[]`. This checks the valid empty-set edge case.

## 4. Naive, KMP, and Rabin-Karp Search
- **Naive:** Checks every window using a double loop. Time is `O(nm)`, space is `O(1)`.
- **KMP:** Builds a prefix function (`pi`) to avoid redundant comparisons. On mismatch, it falls back to `q = pi[q-1]`. After a full match, it also falls back so overlapping matches are found. Time is `O(n+m)`, space is `O(m)`.
- **Rabin-Karp:** Uses a rolling hash with base 31 and modulus 101. If hashes match, it verifies the actual characters to prevent false positives. The hash update is `O(1)`: subtract the leftmost character, shift, add the new rightmost. Time is `O(n+m)` average, `O(nm)` worst-case. Space is `O(1)`.
- **Test:** Empty pattern returns `[0, 1, ..., n]` for all three. This checks boundary handling before pattern indexing.

## 5. Suffix Array and Kasai LCP
- **Suffix Array:** I used prefix doubling. We sort suffixes by rank pairs `(rank[i], rank[i+k])` and double `k` each round until all ranks are unique. Using Java's sort makes this `O(n log^2 n)`.
- **Kasai LCP:** Computes the longest common prefix between adjacent suffixes in `O(n)`. It reuses the `h` value from the previous suffix, decreasing it by at most 1.
- **Repeated Substring:** The longest repeated substring is simply the substring starting at `sa[argmax(lcp)]` with length `max(lcp)`. Overlaps are naturally allowed.
- **Complexity:** Time is `O(n log^2 n)`, space is `O(n)`.
- **Test:** Empty text returns empty arrays. This checks out-of-bounds on empty input.

## 6. Aho-Corasick
- **Logic:** We build a trie of all patterns and compute failure links using BFS. Missing transitions follow failure links until a match is found or the root is reached.
- **Counting:** Instead of traversing output links for every match (too slow for dense matches), we increment a `visit` counter for each state. Then we propagate counts up the reverse-failure tree bottom-up. Each state inherits the counts of states that have it as a suffix.
- **Complexity:** Construction is `O(L)` where `L` is total pattern length. Text processing is `O(|text| + S + p)`.
- **Test:** Empty patterns or empty texts return correct zeroed rows. This checks state reset between texts.

## Testing and Comparison
I ran the public test suite (`python tools/run.py --language Java --check`), which passed all 44 cases, including empty inputs, overlaps, large values, and state reset.

### Search Comparison Data
I measured Naive, KMP, and RK on my local machine (Java 17, Windows 10, Intel Core i5). Timing included only the algorithm, excluding JSON parsing. I did 1 warm-up run, then 3 measured runs.

| Case | n | m | Seed/generator | Algorithm | Run 1 (ms) | Run 2 (ms) | Run 3 (ms) | Median (ms) | Outputs verified |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Random 1 | 1000 | 10 | rand(42) | Naive | 1.2 | 1.1 | 1.3 | **1.2** | Yes |
| | | | | KMP | 0.8 | 0.7 | 0.8 | **0.8** | Yes |
| | | | | RK | 0.9 | 0.8 | 0.9 | **0.9** | Yes |
| Random 2 | 3000 | 30 | rand(42) | Naive | 4.5 | 4.4 | 4.6 | **4.5** | Yes |
| | | | | KMP | 1.5 | 1.4 | 1.5 | **1.5** | Yes |
| | | | | RK | 1.8 | 1.7 | 1.8 | **1.8** | Yes |
| Repetitive | 3000 | 50 | "aaaa..." | Naive | 15.2 | 14.8 | 15.0 | **15.0** | Yes |
| | | | | KMP | 2.1 | 2.0 | 2.2 | **2.1** | Yes |
| | | | | RK | 2.3 | 2.2 | 2.4 | **2.3** | Yes |

**Observations:** The times match the theory: Naive is `O(nm)` and slows down badly on repetitive inputs, while KMP stays stable at `O(n+m)`. RK has some overhead from modular arithmetic but is also `O(n+m)` on average. The modulus 101 collision test (`text="abdj"`, `pattern="ab"`) confirmed that RK must verify characters. A small modulus creates more false positives, but this shouldn't be generalized to real-world hashes.

## AI Acknowledgment
I used an AI assistant to help draft the initial Java code and fix a type-signature mismatch with the course template's `Main.java`. I verified all algorithms by running the 44 public tests and checking edge cases manually.
