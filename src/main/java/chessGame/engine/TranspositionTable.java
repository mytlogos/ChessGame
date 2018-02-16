package chessGame.engine;

import chessGame.mechanics.board.FigureBoard;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 */
public class TranspositionTable {
    private final ReplacementPolicy policy;
    private final Entry[] items;
    private final int size;


    TranspositionTable(int size) {
        this(size, ReplacementPolicy.DEEP);
    }

    TranspositionTable(int size, ReplacementPolicy policy) {
        this.size = size;
        this.items = new Entry[size];
        this.policy = policy;

    }

    TranspositionTable() {
        this(50000, ReplacementPolicy.DEEP);
    }

    private int addedCounter = 0;

    void printInfo() {
        int count = 0;
        int used = 0;

        for (Entry entry : items) {
            if (entry != null) {
                count++;
                if (entry.isUsed()) used++;
            }
        }

        System.out.println("Content: " + count);
        System.out.println("Replaced: " + (addedCounter-count));
        System.out.println("Used: " + used);
        System.out.println("Filled: " + BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(size), 1, RoundingMode.HALF_EVEN));
    }

    void add(Entry entry) {
        int index = getIndex(entry.getHashKey());
        policy.replace(items, index, entry);
        addedCounter++;
    }

    private int getIndex(long hash) {
        return Math.abs((int) (hash % size));
    }

    Entry getEntry(FigureBoard board) {
        long hash = board.getHash();
        int index = getIndex(hash);
        Entry item = items[index];

        if (item != null && item.getHashKey() == hash) {
            item.setUsed(true);
            return item;
        } else {
            return null;
        }
    }

    void resetUsage() {
        for (Entry item : items) {
            if (item != null) {
                item.setUsed(false);
            }
        }
    }

    enum ReplacementPolicy {
        DEEP,
        ALWAYS,
        DEEP_ALWAYS,
        BIG;

        private void replace(Entry[] entries, int index, Entry entry) {
            Entry previous = entries[index];

            if (previous != null && !previous.isUsed() && previous.getHashKey() == entry.getHashKey()) {
                if (previous.getDepth() <= entry.getDepth()) {
                    entries[index] = entry;
                }
            } else {
                entries[index] = entry;
            }
        }
    }
}
