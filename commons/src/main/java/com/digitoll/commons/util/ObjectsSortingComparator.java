package com.digitoll.commons.util;

import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectsSortingComparator implements Comparator<Object> {
    private final String sortingParameter;
    private final Sort.Direction sortingDirection;
    private static final String SPLIT_REGEX = "[._]";

    public ObjectsSortingComparator(String sortingParameter, Sort.Direction sortingDirection) {
        this.sortingParameter = sortingParameter;
        this.sortingDirection = sortingDirection;
    }

    @Override
    public int compare(Object o1, Object o2) {
        List<String> params = Arrays.asList(sortingParameter.split(SPLIT_REGEX));
        Object firstObjectValue = getPropertyByString(o1, new ArrayDeque<>(params));
        Object secondObjectValue = getPropertyByString(o2,  new ArrayDeque<>(params));

        if (firstObjectValue == null && secondObjectValue != null) {
            if (sortingDirection.isAscending()) {
                return -1;
            } else {
                return 1;
            }
        } else if (firstObjectValue != null && secondObjectValue == null) {
            if (sortingDirection.isAscending()) {
                return 1;
            } else {
                return -1;
            }
        } else if (firstObjectValue == null && secondObjectValue == null) {
            return 0;
        }


        if (firstObjectValue instanceof Comparable && secondObjectValue instanceof Comparable) {
            if (sortingDirection.isAscending()) {
                return ((Comparable) firstObjectValue).compareTo(secondObjectValue);
            } else {
                return ((Comparable) secondObjectValue).compareTo(firstObjectValue);
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    private Object getPropertyByString(Object object, ArrayDeque<String> params) {
        if (params.isEmpty() || object == null) return object;
        try {
            Field field = object.getClass().getDeclaredField(params.pop());
            field.setAccessible(true);
            return getPropertyByString(field.get(object), params);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
