import java.util.NoSuchElementException;

/**
 * @author Shane Pruett
 *  CS342 Section 001
 *  Lab 1: Cache Class
 *  
 *  This Class is used as a generic Most Recently Used cache
 *
 */
public class Cache <E> {

	public java.util.LinkedList<E> cache;
	private int max;

	/**
	 * @param size An integer for the max size of the cache
	 */
	public Cache(int size){

		// the cache uses LinkedList
		cache = new java.util.LinkedList<E>();
		max = size;

	}

	/**
	 * @param findObject The generic object to search the cache for
	 * @return returns the object if it is found, returns null if its not found
	 */
	public E getObject(E findObject){

		E retObject = null;

		// this will find the index of the object if it is found
		int find = cache.indexOf(findObject);

		// if the object is not found then find = -1 and this will be false
		if (find >= 0){
			// remove object from cache then add it to the beginning of the linked list
			// in order to maintain MRU cache algorithm
			cache.addFirst(cache.remove(find));
			retObject = cache.peek();
		}
		
		return retObject;
	}

//	public E getAtIndex(int index){
//		
//		
//		return cache.get(index);
//	}
	
	public BTree.BTreeNode.TreeObject getObject(long seq){
		
		
		
		
		
		
		return null;
	}
	
	
	
	/**
	 * @param object the generic object to add to the cache
	 */
	public E addObject(E object){

		// if the cache is not max size
		if (cache.size() < max){
			cache.addFirst(object);
			return object;
		}

		// when the cache is max
		else {
			// remove the oldest object, add new object to beginning
			E removed = cache.removeLast();
			cache.addFirst(object);
			return removed;
		}


	}

	/**
	 * @return this method is not used in the Test class, but is implemented
	 */
	public E removeObject(E removeObject){
		
		E retObject = null;
		
		int find = cache.indexOf(removeObject);
		if (find >= 0){
			// remove object from cache
			
			retObject = cache.remove(find);
		}
		
		// if the object was not found then throw an exception
		if (retObject == null)
			throw new NoSuchElementException();
		
		return retObject;
	}

	
	/**
	 * Clear the cache
	 */
	public void clearCache(){
		cache.clear();
	}


	/**
	 * @param args none
	 * 
	 * This was used for testing purposes
	 */
	public static void main(String[] args) {
		
//		Cache<Integer> num1 = new Cache<Integer>(3);
//		
//		num1.addObject(1);
//		num1.addObject(2);
//		num1.addObject(3);
//		num1.addObject(4);
//		num1.getObject(1);
//		
//		System.out.println(num1.getObject(1));
		
		
	}


}
