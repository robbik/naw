package org.naw.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Selector<T> {

	private final Map<Object, List<T>> map;

	public Selector() {
		map = new SynchronizedHashMap<Object, List<T>>();
	}

	public void add(T o, Object... selections) {
		for (int i = 0, len = selections.length; i < len; ++i) {
			Object selection = selections[i];
			List<T> list;

			synchronized (map) {
				list = map.get(selection);

				if (list == null) {
					list = new ArrayList<T>();
					map.put(selection, list);
				}
			}

			synchronized (list) {
				if (!list.contains(o)) {
					list.add(o);
				}
			}
		}
	}

	public List<T> select(Object selection) {
		List<T> list = map.get(selection);

		if ((list == null) || list.isEmpty()) {
			list = null;
		}

		return list;
	}

	public void remove(T o, Object... selections) {
		int len = selections.length;

		if (len == 0) {
			Set<Object> marked = new HashSet<Object>();

			synchronized (map) {
				for (Map.Entry<Object, List<T>> e : map.entrySet()) {
					List<T> list = e.getValue();

					synchronized (list) {
						if (list.remove(o) && list.isEmpty()) {
							marked.add(e.getKey());
						}
					}
				}

				for (Object e : marked) {
					map.remove(e);
				}
			}

			marked.clear();
			marked = null;
		} else {
			for (int i = 0; i < len; ++i) {
				Object selection = selections[i];

				synchronized (map) {
					List<T> list = map.get(selection);

					if (list != null) {
						synchronized (list) {
							if (list.remove(o) && list.isEmpty()) {
								map.remove(selection);
							}
						}
					}
				}
			}
		}
	}
}
