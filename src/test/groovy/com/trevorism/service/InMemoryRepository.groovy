package com.trevorism.service

import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.ComplexFilter
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.data.model.paging.PageRequest
import com.trevorism.data.model.sorting.ComplexSort
import com.trevorism.data.model.sorting.Sort

/**
 * Simple in-memory Repository fake for unit tests. Backs create/get/list/update/delete with a
 * map; the query methods (filter/page/sort) just return everything, which is enough for the
 * service logic under test (it filters in memory via the visibility rules).
 */
class InMemoryRepository<T> implements Repository<T> {

    private final Map<String, T> store = [:]
    private int counter = 0

    @Override List<T> all() { new ArrayList<>(store.values()) }

    @Override List<T> list() { new ArrayList<>(store.values()) }

    @Override T get(String id) { store.get(id) }

    @Override
    T create(T item) {
        if (!item.id) item.id = "id-${++counter}".toString()
        store.put(item.id as String, item)
        return item
    }

    @Override
    T update(String id, T item) {
        item.id = id
        store.put(id, item)
        return item
    }

    @Override T delete(String id) { store.remove(id) }

    @Override void ping() {}

    @Override List<T> filter(ComplexFilter filter) { list() }

    @Override List<T> filter(SimpleFilter filter) { list() }

    @Override List<T> page(PageRequest pageRequest) { list() }

    @Override List<T> sort(ComplexSort sort) { list() }

    @Override List<T> sort(Sort sort) { list() }
}
