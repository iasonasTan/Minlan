package com.app.minlan;

import android.util.Log;

import java.util.List;
import java.util.function.Consumer;

final class AsyncIterator<T> {
    private final List<T> mItems;
    private final Consumer<T> mConsumer;

    AsyncIterator(List<T> items, Consumer<T> consumer) {
        mItems = items;
        mConsumer = consumer;
    }

    public void startProcessing() {
        int ITEMS_PER_THREAD = 20;
        for (int i = 0; i < mItems.size(); i += ITEMS_PER_THREAD) {
            int endIdx = Math.min(i + ITEMS_PER_THREAD, mItems.size()-1);
            new ListFragmentProcessor(i, endIdx, mItems, mConsumer)
                    .start();
        }
    }

    final class ListFragmentProcessor extends Thread {
        private final int mStart, mEnd;
        private final List<T> mItems;
        private final Consumer<T> mConsumer;

        ListFragmentProcessor(int start, int end, List<T> items, Consumer<T> consumer) {
            mStart = start;
            mEnd = end;
            mItems = items;
            mConsumer = consumer;
        }

        @Override
        public void run() {
            long startTime = System.nanoTime();
            for (int i = mStart; i < mEnd; i++) {
                System.out.println("Processor range: " + mStart + "-" + mEnd);
                T item = mItems.get(i);
                mConsumer.accept(item);
            }
            long endTime = System.nanoTime();
            long deltaTime = endTime - startTime;
            Log.d("asyncIter", "Async iterator #"+hashCode()+" finished. (took " + deltaTime/1_000_000 + " milliseconds)");
        }
    }
}
