import java.io.*;
import java.util.*;

/**
 * 24.
Data Elegance in Bits: Exploring and Implementing Huffman Coding and Lempel-Ziv-Welch (LZW) Compression Algorithms

Data Compression, Information Theory

Explore and implement data compression algorithms like Huffman coding or Lempel-Ziv-Welch (LZW) compression

Programming environment (e.g., Python, C++)

Data compression algorithms, compression libraries

Develop methods to save storage space and transmit data more efficiently

Smaller file sizes for storage and faster data transmission

 */
public class CompressionDemo {
    // Huffman Implementation

    static class Huffman {
        static class Node implements Comparable<Node> {
            final int freq;
            final Character ch; // if leaf, non-null
            final Node left, right;
            Node(int freq, Character ch, Node left, Node right) {
                this.freq = freq; this.ch = ch; this.left = left; this.right = right;
            }
            boolean isLeaf() { return ch != null; }
            public int compareTo(Node o) { return Integer.compare(this.freq, o.freq); }
        }

        // Build frequency map
        public static Map<Character, Integer> freqMap(String s) {
            Map<Character, Integer> m = new HashMap<>();
            for (char c : s.toCharArray()) m.put(c, m.getOrDefault(c, 0) + 1);
            return m;
        }

        // Build tree
        public static Node buildTree(Map<Character,Integer> freq) {
            PriorityQueue<Node> pq = new PriorityQueue<>();
            for (Map.Entry<Character,Integer> e : freq.entrySet()) {
                pq.add(new Node(e.getValue(), e.getKey(), null, null));
            }
            if (pq.isEmpty()) return null;
            while (pq.size() > 1) {
                Node a = pq.poll();
                Node b = pq.poll();
                pq.add(new Node(a.freq + b.freq, null, a, b));
            }
            return pq.poll();
        }

        // Build codes map
        public static Map<Character, String> buildCodes(Node root) {
            Map<Character,String> map = new HashMap<>();
            if (root == null) return map;
            buildCodesRec(root, "", map);
            // edge case: single unique char -> assign "0"
            if (map.size() == 1) {
                Character only = map.keySet().iterator().next();
                map.put(only, "0");
            }
            return map;
        }
        private static void buildCodesRec(Node node, String prefix, Map<Character,String> map) {
            if (node.isLeaf()) {
                map.put(node.ch, prefix);
            } else {
                buildCodesRec(node.left, prefix + '0', map);
                buildCodesRec(node.right, prefix + '1', map);
            }
        }

        // Encode to byte[] using a BitOutputStream
        public static byte[] encode(String input, Map<Character,String> codes) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BitOutputStream bos = new BitOutputStream(baos);
            for (char c : input.toCharArray()) {
                String code = codes.get(c);
                for (char bit : code.toCharArray()) bos.writeBit(bit == '1' ? 1 : 0);
            }
            bos.flush();
            return baos.toByteArray();
        }

        // Decode from byte[] using the tree
        public static String decode(byte[] data, Node root, int originalLength /*chars expected*/ ) throws IOException {
            BitInputStream bis = new BitInputStream(new ByteArrayInputStream(data));
            StringBuilder sb = new StringBuilder();
            Node node = root;
            // Special-case: root is leaf (single unique char)
            if (root.isLeaf()) {
                // all bits correspond to repetitions of root.ch -- but we need to know how many characters:
                // This function expects originalLength to know how many chars to emit.
                for (int i = 0; i < originalLength; i++) sb.append(root.ch);
                return sb.toString();
            }
            int bit;
            while ((bit = bis.readBit()) != -1) {
                node = (bit == 0) ? node.left : node.right;
                if (node.isLeaf()) {
                    sb.append(node.ch);
                    node = root;
                }
            }
            return sb.toString();
        }

        // Helper classes for bit I/O
        static class BitOutputStream implements Closeable {
            private OutputStream out;
            private int currentByte = 0;
            private int numBitsFilled = 0;
            BitOutputStream(OutputStream out) { this.out = out; }
            void writeBit(int bit) throws IOException {
                if (!(bit == 0 || bit == 1)) throw new IllegalArgumentException("bit must be 0 or 1");
                currentByte = (currentByte << 1) | bit;
                numBitsFilled++;
                if (numBitsFilled == 8) {
                    out.write(currentByte);
                    numBitsFilled = 0;
                    currentByte = 0;
                }
            }
            void flush() throws IOException {
                if (numBitsFilled > 0) {
                    currentByte <<= (8 - numBitsFilled);
                    out.write(currentByte);
                    currentByte = 0;
                    numBitsFilled = 0;
                }
                out.flush();
            }
            public void close() throws IOException { flush(); out.close(); }
        }

        static class BitInputStream implements Closeable {
            private InputStream in;
            private int currentByte = 0;
            private int numBitsRemaining = 0;
            BitInputStream(InputStream in) { this.in = in; }
            // returns 0,1 or -1 for EOF
            int readBit() throws IOException {
                if (numBitsRemaining == 0) {
                    currentByte = in.read();
                    if (currentByte == -1) return -1;
                    numBitsRemaining = 8;
                }
                numBitsRemaining--;
                return (currentByte >> numBitsRemaining) & 1;
            }
            public void close() throws IOException { in.close(); }
        }
    }

  
    // LZW Implementation
    static class LZW {
        // Encode: returns array of int codes
        public static List<Integer> encode(String input, int maxDictSize) {
            // initialize dictionary with single chars
            Map<String,Integer> dict = new HashMap<>();
            for (int i = 0; i < 256; i++) dict.put("" + (char)i, i);
            int dictSize = 256;

            String w = "";
            List<Integer> result = new ArrayList<>();
            for (char c : input.toCharArray()) {
                String wc = w + c;
                if (dict.containsKey(wc)) {
                    w = wc;
                } else {
                    result.add(dict.get(w));
                    if (dictSize < maxDictSize) {
                        dict.put(wc, dictSize++);
                    }
                    w = "" + c;
                }
            }
            if (!w.equals("")) result.add(dict.get(w));
            return result;
        }

        // Decode from list of ints
        public static String decode(List<Integer> codes, int maxDictSize) {
            Map<Integer,String> dict = new HashMap<>();
            for (int i = 0; i < 256; i++) dict.put(i, "" + (char)i);
            int dictSize = 256;

            String w = "" + (char)(int)codes.get(0);
            StringBuilder sb = new StringBuilder(w);
            for (int i = 1; i < codes.size(); i++) {
                int k = codes.get(i);
                String entry;
                if (dict.containsKey(k)) {
                    entry = dict.get(k);
                } else if (k == dictSize) {
                    // special case
                    entry = w + w.charAt(0);
                } else {
                    throw new IllegalArgumentException("Bad compressed k: " + k);
                }
                sb.append(entry);
                if (dictSize < maxDictSize) {
                    dict.put(dictSize++, w + entry.charAt(0));
                }
                w = entry;
            }
            return sb.toString();
        }

        // Convert list of int codes to bytes (16-bit per code) and back.
        public static byte[] codesToBytes(List<Integer> codes) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            for (int c : codes) {
                dos.writeShort(c); // 16-bit signed; codes < 65536
            }
            dos.flush();
            return baos.toByteArray();
        }
        public static List<Integer> bytesToCodes(byte[] bytes) throws IOException {
            List<Integer> codes = new ArrayList<>();
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
            try {
                while (true) {
                    int s = dis.readUnsignedShort();
                    codes.add(s);
                }
            } catch (EOFException eof) {
                // finished
            }
            return codes;
        }
    }


    // Demo and helpers

    static int bytesLength(String s) { return s.getBytes().length; }

    public static void main(String[] args) throws Exception {
        // Example text
        String text = "This is an example text to demonstrate Huffman and LZW compression. " +
                "Huffman coding is optimal prefix coding given symbol frequencies. LZW builds a dictionary of repeated strings.";

        System.out.println("Original text (" + text.length() + " chars, " + bytesLength(text) + " bytes):");
        System.out.println(text);
        System.out.println();

        // ---------------- Huffman demo ----------------
        System.out.println("== Huffman Coding ==");
        Map<Character,Integer> freq = Huffman.freqMap(text);
        Huffman.Node tree = Huffman.buildTree(freq);
        Map<Character,String> codes = Huffman.buildCodes(tree);

        System.out.println("Sample codes (char -> code):");
        int shown = 0;
        for (Map.Entry<Character,String> e : codes.entrySet()) {
            System.out.println("'" + printable(e.getKey()) + "' -> " + e.getValue());
            if (++shown >= 10) break;
        }

        byte[] huffEncoded = Huffman.encode(text, codes);
        String huffDecoded = Huffman.decode(huffEncoded, tree, text.length());

        System.out.println("Huffman encoded bytes: " + huffEncoded.length);
        System.out.println("Huffman decoded equals original? " + text.equals(huffDecoded));
        System.out.printf("Huffman compression ratio: %.2f%%\n",
                100.0 * huffEncoded.length / bytesLength(text));
        System.out.println();

        // ---------------- LZW demo ----------------
        System.out.println("== LZW ==");
        int maxDictSize = 4096; // e.g., 12-bit codes (0..4095)
        List<Integer> lzwCodes = LZW.encode(text, maxDictSize);
        byte[] lzwBytes = LZW.codesToBytes(lzwCodes);
        List<Integer> lzwReadCodes = LZW.bytesToCodes(lzwBytes);
        String lzwDecoded = LZW.decode(lzwReadCodes, maxDictSize);

        System.out.println("LZW codes count: " + lzwCodes.size());
        System.out.println("LZW encoded bytes (16-bit per code): " + lzwBytes.length);
        System.out.println("LZW decoded equals original? " + text.equals(lzwDecoded));
        System.out.printf("LZW compression ratio: %.2f%%\n",
                100.0 * lzwBytes.length / bytesLength(text));
        System.out.println();

        System.out.println("Done. Notes:");
        System.out.println("- Huffman output is bit-packed (good for small alphabets).");
        System.out.println("- LZW writes 16-bit codes here. For practical savings you'd pack codes (e.g., 12-bit) into bytes.");
        System.out.println("- For file storage you'd add headers (Huffman tree or canonical code table, and original length).");
    }

    private static String printable(char c) {
        if (c == ' ') return "SPACE";
        if (c == '\n') return "\\n";
        if (c == '\r') return "\\r";
        if (c == '\t') return "\\t";
        return Character.toString(c);
    }
}
