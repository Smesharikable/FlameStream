package com.spbsu.datastream.example.invertedindex.utils;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

/**
 * User: Artem
 * Date: 19.03.2017
 * Time: 13:05
 */
public class InvertedIndexStorage {
  public static final int PREV_VALUE_NOT_FOUND = -1;
  private static final int DEFAULT_MAX_WINDOW_SIZE = 100;

  private final int maxWindowSize;
  private TLongArrayList[] storage;

  public InvertedIndexStorage() {
    this(DEFAULT_MAX_WINDOW_SIZE);
  }

  public InvertedIndexStorage(int maxWindowSize) {
    if (maxWindowSize <= 1) {
      throw new IllegalArgumentException("Max window size should be > 1");
    }
    this.maxWindowSize = maxWindowSize;
    storage = new TLongArrayList[1];
    storage[0] = new TLongArrayList();
  }

  public long tryToFindAndUpdate(long value, int newPosition, int newRange) {
    final int windowIndex = findWindow(value);
    final TLongArrayList window = storage[windowIndex];
    int searchIndex = window.binarySearch(value);
    if (searchIndex < 0) {
      searchIndex = -searchIndex - 1;
    }

    final long searchValue;
    if (searchIndex < window.size() && PagePositionLong.pageId(searchValue = window.get(searchIndex)) == PagePositionLong.pageId(value)) {
      final long newValue = PagePositionLong.createPagePosition(PagePositionLong.pageId(value), newPosition, PagePositionLong.version(value), newRange);
      window.set(searchIndex, newValue);
      return searchValue;
    } else {
      return PREV_VALUE_NOT_FOUND;
    }
  }

  public void insert(long value) {
    int windowIndex = findWindow(value);
    final TLongArrayList window = storage[windowIndex];
    final int insertIndex = -window.binarySearch(value) - 1;
    if (insertIndex < 0) {
      throw new IllegalArgumentException("Storage contains such value");
    }

    if (window.size() + 1 > maxWindowSize) {
      final TLongArrayList firstWindow = new TLongArrayList();
      final TLongArrayList secondWindow = new TLongArrayList();
      for (int i = 0; i < window.size() / 2; i++) {
        firstWindow.add(window.get(i));
      }
      for (int i = window.size() / 2; i < window.size(); i++) {
        secondWindow.add(window.get(i));
      }
      //window.clear(0);

      final TLongArrayList[] newStorage = new TLongArrayList[storage.length + 1];
      System.arraycopy(storage, 0, newStorage, 0, windowIndex);
      System.arraycopy(storage, windowIndex + 2 - 1, newStorage, windowIndex + 2, newStorage.length - (windowIndex + 2));
      newStorage[windowIndex] = firstWindow;
      newStorage[windowIndex + 1] = secondWindow;
      storage = newStorage;

      insert(value);
    } else {
      window.insert(insertIndex, value);
    }
  }

  public TLongList toList() {
    final TLongList result = new TLongArrayList();
    for (TLongArrayList list : storage) {
      result.addAll(list);
    }
    return result;
  }

  private int findWindow(long value) {
    //TODO: implement binary search
    int windowIndex = 0;
    while (windowIndex < storage.length && !storage[windowIndex].isEmpty() && value > storage[windowIndex].get(0)) {
      if (storage[windowIndex].get(storage[windowIndex].size() - 1) > value) {
        break;
      } else {
        windowIndex++;
      }
    }
    return windowIndex >= storage.length ? windowIndex - 1 : windowIndex;
  }
}