package ru.ifmo.ctddev.kupriyanov.arrayset;

import java.util.*;

/**
 * Created by pinkdonut on 14.03.2016.
 */
public class ArraySortedSet<E> extends AbstractSet<E> implements SortedSet<E> {
    private List<E> data;
    private Comparator<? super E> comparator;

    /* Constructors */
    public ArraySortedSet() {
        this.data = new ArrayList<>();
        this.comparator = null;
    }

    public ArraySortedSet(List<E> list, Comparator<? super E> comparator) {
        this.data = list;
        this.comparator = comparator;
    }

    public ArraySortedSet(Collection<E> collection) {
        this.data = new ArrayList<>();
        addCollectionToASS(collection);
        data.sort((Comparator<? super E>) Comparator.naturalOrder());
    }

    public ArraySortedSet(Collection<E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;
        this.data = new ArrayList<>();
        addCollectionToASS(collection);
        data.sort(comparator);
    }

    /* AbstractSet methods */
    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (E) o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /* SortedSet methods */
    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        int fromPosition = search(fromElement);
        int toPosition = search(toElement);
        return subSet(fromPosition, toPosition);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int toPosition = search(toElement);
        return subSet(0, toPosition);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int fromPosition = search(fromElement);
        return subSet(fromPosition, size());
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException("data is empty");
        }
        return data.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException("data is empty");
        }
        return data.get(size() - 1);
    }

    /* My methods */
    private SortedSet<E> subSet(int fromPosition, int toPosition) {
        return new ArraySortedSet<>(data.subList(fromPosition, toPosition), comparator);
    }

    private int search(E element) {
        int position = Collections.binarySearch(data, element, comparator);
        if (position < 0) {
            position = - position - 1;
        }
        return position;
    }

    private void addCollectionToASS(Collection<E> collection) {
        for (E e : collection) {
            if (!contains(e)) {
                data.add(e);
            }
        }
    }
}
