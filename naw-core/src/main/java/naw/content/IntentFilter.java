package naw.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import naw.os.Bundle;
import rk.commons.util.ObjectHelper;

public class IntentFilter implements Cloneable {
    public static final int MATCH = 0x0100000;
    
    /**
     * The filter didn't match due to different actions.
     */
    public static final int NO_MATCH_ACTION = -3;
    
    /**
     * The filter didn't match because it required one or more categories
     * that were not in the Intent.
     */
    public static final int NO_MATCH_CATEGORY = -4;

    public static final int NO_MATCH_EXTRAS = -5;
    
    private final Set<String> mProcessIds;

    private final Set<String> mActions;
    
    private final ArrayList<String> mCategories;
    
    private final Map<String, Object> mExtras;

    public IntentFilter() {
    	mProcessIds = new HashSet<String>();
    	
        mActions = new HashSet<String>();
        mCategories = new ArrayList<String>();
        
        mExtras = new HashMap<String, Object>();
    }

    public IntentFilter(IntentFilter o) {
    	mProcessIds = new HashSet<String>();
    	
        mActions = new HashSet<String>(o.mActions);
        mCategories = new ArrayList<String>(o.mCategories);
        
        mExtras = new HashMap<String, Object>(o.mExtras);
    }

    @Override
    public Object clone() {
        return new IntentFilter(this);
    }
    
    public final void addProcessId(String processId) {
    	mProcessIds.add(processId.intern());
    }

    public final void addAction(String action) {
        mActions.add(action.intern());
    }

    public final Set<String> actions() {
        return mActions;
    }

    public final boolean hasAction(String action) {
        return action != null && mActions.contains(action);
    }

    public final boolean matchAction(String action) {
        return hasAction(action);
    }

    public final void addCategory(String category) {
        if (!mCategories.contains(category)) {
        	mCategories.add(category.intern());
        }
    }

    public final int countCategories() {
        return mCategories.size();
    }

    public final String getCategory(int index) {
        return mCategories.get(index);
    }

    public final boolean hasCategory(String category) {
        return category != null && mCategories.contains(category);
    }

    public final boolean matchCategory(String category) {
        return hasCategory(category);
    }
    
    public final boolean matchCategories(Set<String> categories) {
        if (categories == null) {
            return mCategories.isEmpty();
        }

        for (String category : mCategories) {
            if (!categories.contains(category)) {
                return false;
            }
        }

        return true;
    }
    
    public final void addExtra(String key, Object o) {
    	mExtras.put(key, o);
    }
    
    public final boolean matchExtras(Bundle extras) {
    	if (extras == null) {
    		return mExtras.isEmpty();
    	}
    	
    	for (Map.Entry<String, Object> e : mExtras.entrySet()) {
    		String key = e.getKey();
    		
    		if (!extras.containsKey(key)) {
    			return false;
    		}
    		
    		if (!ObjectHelper.equals(extras.getSerializable(key), e.getValue())) {
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    public final int match(Intent intent) {
    	return match(intent.getAction(), intent.getCategories(), intent.getExtras());
    }

    public final int match(String action, Set<String> categories, Bundle extras) {
    	if ((action != null) && !matchAction(action)) {
    		return NO_MATCH_ACTION;
    	}
    	
        if (!matchCategories(categories)) {
            return NO_MATCH_CATEGORY;
        }
        
        if (!matchExtras(extras)) {
        	return NO_MATCH_EXTRAS;
        }
        
        return MATCH;
    }
}
