package org.naw.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Selector<T> {

	private final Map<String, List<T>> map;

	public Selector() {
		map = new SynchronizedHashMap<String, List<T>>();
	}

	public void add(T o, String... selections) {
		for (int i = 0, len = selections.length; i < len; ++i) {
			String selection = selections[i];
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

	public List<T> select(String selection) {
		List<T> list = map.get(selection);

		if ((list == null) || list.isEmpty()) {
			list = null;
		}

		return list;
	}

	public void remove(T o, String... selections) {
		int len = selections.length;

		if (len == 0) {
			Set<String> marked = new HashSet<String>();

			synchronized (map) {
				for (Map.Entry<String, List<T>> e : map.entrySet()) {
					List<T> list = e.getValue();

					synchronized (list) {
						if (list.remove(o) && list.isEmpty()) {
							marked.add(e.getKey());
						}
					}
				}

				for (String e : marked) {
					map.remove(e);
				}
			}

			marked.clear();
			marked = null;
		} else {
			for (int i = 0; i < len; ++i) {
				String selection = selections[i];

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
