
import java.util.*;
/*
PROBLEM STATEMENT:
Design an ecommerce search auto complete suggestion. (Type ahead).


Requirements
1. All the ecommerce products should be included in the catalogue
2. Response type should be in milliseconds
3. Fuzzy search should be allowed


Example
when user type c,
Response - ["christmas decorations","christmas tree","christmas lights","gift cards","iphone 13 pro max case",
"advent calendar 2021"]


when user type ca,
["carhartt beanie","candles","cat toys","candy","carhartt hoodie","advent calendar 2021"]


when user type can
["candles","candy","christmas candy","candy canes","can opener","noise cancelling headphones","yankee candles",
"candle making kit",
"christmas candles","canvases for painting"]


when user type cano
["canon camera","canopy","canon","canon m50","canopy tent","canopy bed","canon ivy mini photo printer","canon printer",
"canopy bed curtains","canon lens"]


https://completion.amazon.com/search/complete?search-alias=aps&client=amazon-search-ui&mkt=1&q=canon


I came up with a design with Elastic search DB and cache the results in redis. But Interviewer is not satisfied.
Thought of new cache structure built using trie, which is also not encouraged.


This question is asked for me in oracle cloud infrastructure interview. I could not come up with an optimal solution
even days after the interview. Please point me in the right direction. Thanks in Advance
*/


// Trie Node definition
class TrieNode {
    Map<Character, TrieNode> children;
    boolean isWord;
    String word;  // The word that ends here

    TrieNode() {
        children = new HashMap<>();
        isWord = false;
        word = null;
    }
}

// Trie for storing words
class WordTrie {
    private final TrieNode root;

    public WordTrie() {
        root = new TrieNode();
    }

    // Insert word into Trie
    public void insert(String word) {
        TrieNode node = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            node.children.putIfAbsent(ch, new TrieNode());
            node = node.children.get(ch);
        }
        node.isWord = true;
        node.word = word;
    }

    //  Move curr to the end of prefix in its trie representation.
    public List<String> searchByPrefix(String prefix) {
        List<String> result = new ArrayList<>();
        TrieNode node = root;
        for (char ch : prefix.toLowerCase().toCharArray()) {
            if (node.children.containsKey(ch)) {
                node = node.children.get(ch);
            } else {
                return result;  // No words start with this prefix
            }
        }
        // From this node, run DFS for each child to collect all words below this node
        collectWordsDfs(node, result);
        return result;
    }

    public List<String> fuzzySearchByPrefix(String prefix) {
        List<String> result = new ArrayList<>();
        fuzzySearchHelper(root, prefix, 0, 0, 1, result, 4);
        return result;
    }


    private void fuzzySearchHelper(TrieNode node, String word, int index, int edits, int maxEdits,
                                   List<String> result, int maxResults) {
        if (edits > maxEdits || result.size() >= maxResults) return;
        if (index == word.length()) {
            collectWordsDfs(node, result);
            return;
        }

        char ch = word.charAt(index);
        for (char key : node.children.keySet()) {
            TrieNode child = node.children.get(key);
            // Match
            if (key == ch) {
                fuzzySearchHelper(child, word, index + 1, edits, maxEdits, result, maxResults);
            } else {
                // Substitution
                fuzzySearchHelper(child, word, index + 1, edits + 1, maxEdits, result, maxResults);
                // Insertion (skip a character in the word)
                fuzzySearchHelper(node, word, index + 1, edits + 1, maxEdits, result, maxResults);
                // Deletion (skip a character in the Trie)
                fuzzySearchHelper(child, word, index, edits + 1, maxEdits, result, maxResults);
            }
        }
    }

    private void collectWordsDfs(TrieNode node, List<String> result) {
        if (node.isWord) {
            result.add(node.word);
        }
        for (TrieNode child : node.children.values()) {
            collectWordsDfs(child, result);
        }
    }
}

// Main e-commerce auto-complete suggestion system
public class EcommerceAutocompleteFuzzySearch {

    private final WordTrie wordTrie;
    private final Map<String, Set<String>> wordToProducts;

    public EcommerceAutocompleteFuzzySearch(List<String> products) {
        wordTrie = new WordTrie();
        wordToProducts = new HashMap<>();

        for (String product : products) {
            // Split product into words
            String[] words = product.toLowerCase().split("\\s+");
            for (String word : words) {
                // Insert word into Trie
                wordTrie.insert(word);
                // Add product to inverted index
                wordToProducts.computeIfAbsent(word, k -> new HashSet<>()).add(product);
            }
        }
    }

    // Get auto-complete suggestions
    public List<String> getSuggestions(String prefix, boolean isFuzzySearch) {
        if (null != prefix && (prefix.isBlank() || prefix.isEmpty())) {
            return new ArrayList<>();
        }
        List<String> matchingWords;
        int maxResults = 10;
        prefix = prefix.toLowerCase();
        if (isFuzzySearch) {
            matchingWords = wordTrie.fuzzySearchByPrefix(prefix);
        } else {
            matchingWords = wordTrie.searchByPrefix(prefix);
        }
        Set<String> productSet = new HashSet<>();
        for (String word : matchingWords) {
            Set<String> products = wordToProducts.get(word);
            if (products != null) {
                productSet.addAll(products);
            }
        }
        List<String> result = new ArrayList<>(productSet);
        // Sort the products lexicographically
        Collections.sort(result);
        // Return up to maxResults
        return result.subList(0, Math.min(maxResults, result.size()));
    }

    public static void main(String[] args) {
        // Sample products
        List<String> products = Arrays.asList(
                "decorations christmas", "christmas tree", "christmas lights", "gift cards",
                "iphone 13 pro max case", "advent calendar 2021", "carhartt beanie",
                "candles", "cat toys", "candy", "canon camera", "tom cat", "mobile", "mouse", "mousepad", "money"
        );

        EcommerceAutocompleteFuzzySearch autocompleteSystem = new EcommerceAutocompleteFuzzySearch(products);

        // Simulate typing and getting suggestions
        System.out.println("Suggestions for 'christmas': " + autocompleteSystem.getSuggestions("christmas", false));
        System.out.println("********************************************");
        System.out.println("Suggestions with fuzzy search for 'chrismas': " + autocompleteSystem.getSuggestions("chrismas", true));
        System.out.println("********************************************");
        System.out.println("Suggestions for 'can': " + autocompleteSystem.getSuggestions("can", false));
        System.out.println("********************************************");
        System.out.println("Suggestions with fuzzy search for 'cmn': " + autocompleteSystem.getSuggestions("cxn", true));
        System.out.println("********************************************");
        System.out.println("Suggestions for 'cat': " + autocompleteSystem.getSuggestions("cat", false));
        System.out.println("Fuzzy Suggestions for 'cat': " + autocompleteSystem.getSuggestions("cat", true));
    }
}

