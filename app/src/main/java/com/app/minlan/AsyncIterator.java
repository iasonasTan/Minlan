package com.app.minlan;

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
        int ITEMS_PER_THREAD = 8;
        for (int i = 0; i < mItems.size(); i += ITEMS_PER_THREAD) {
            new ListFragmentProcessor(i, i + ITEMS_PER_THREAD, mItems, mConsumer)
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
            for (int i = mStart; i < mEnd; i++) {
                System.out.println("Processor range: " + mStart + "-" + mEnd);
                try {
                    T item = mItems.get(i);
                    mConsumer.accept(item);
                } catch (NullPointerException | IndexOutOfBoundsException ignored) {
                    // ignore
                }
            }
        }
    }
}
