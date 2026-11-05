package votingsystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Sorter {

    public static <T> List<T> mergeSort(List<T> items, Comparator<T> cmp) {
        if (items.size() <= 1) {
            return new ArrayList<>(items);
        }
        int mid = items.size() / 2;
        List<T> left = mergeSort(items.subList(0, mid), cmp);
        List<T> right = mergeSort(items.subList(mid, items.size()), cmp);
        List<T> merged = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) {
                merged.add(left.get(i++));
            } else {
                merged.add(right.get(j++));
            }
        }
        while (i < left.size()) merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));
        return merged;
    }
}
