package com.example.springwebex.util;

import java.util.List;

public class PagenationUtil {
    /**
     * Calculates the total number of pages based on the total count of items and the page size.
     * @param totalCount total count of items
     * @param pageSize number of items per page
     * @return total number of pages
     */
    public static int getTotalPages(int totalCount, int pageSize) {
        if (totalCount < 0) {
            // "Total count cannot be negative"
            return 0;
        }

        if (pageSize <= 0) {
            // Page size must be greater than 0
            return 0;
        }

        // Math.ceil is used to round up the total pages, ensuring that any remaining items are accounted for in an additional page
        // example: if totalCount is 10 and pageSize is 3, totalPages will be 4 (10/3 = 3.33, rounded up to 4)
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    /**
     * Returns a sublist of the given list based on the specified page size and page number.
     * @param <T> the type of elements in the list
     * @param list the list to be paginated
     * @param pageSize number of items per page
     * @param pageNumber the page number to retrieve (1-based index)
     * @return a sublist containing the items for the specified page
     */
    public static <T> List<T> pagenationList(List<T> list, int pageSize, int pageNumber) {
        if (pageSize < 1 || pageNumber < 1) {
            // Page size and page number must be greater than 0
            return List.of(); // Return an empty list if there are no items 
        }

        if (list == null) {
            // "List cannot be null
            return List.of();
        }

        int totalCount = list.size();
        if (totalCount <= 0) {
            return List.of();
        }

        int startInclusive = Math.min((pageNumber - 1) * pageSize, totalCount);
        int endExclusive = Math.min(startInclusive + pageSize, totalCount);

        return list.subList(startInclusive, endExclusive);
    }
}
