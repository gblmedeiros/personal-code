package algorithms.common;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the known problem of K-Complementary pairs
 * The problem: given an int array A[n] and a constant K, find all pairs p[i,j]
 * where A[i] + A[j] = K
 * This solution is a sort of divide and conquer algorithm and is expected to run in O(n * log n)
 * 
 * @author gblmedeiros - Limeira, Brazil 04-oct-2016 
 *
 */
public class KComplementaryPair {

	public static class Leaf {
		int value;
		Leaf left; // idx for the less 
		Leaf right; // idx for the greater
		
		public Leaf(int value) {
			super();
			this.value = value;
		}
		
		public void setValue(int value) {
			this.value = value;
		}

		public void setLeft(Leaf left) {
			this.left = left;
		}

		public void setRight(Leaf right) {
			this.right = right;
		}

		public int getValue() {
			return value;
		}

		public Leaf getLeft() {
			return left;
		}

		public Leaf getRight() {
			return right;
		}
		
		
	}

	private static int countIterations;
	
	/**
	 * Given an int array and k, build a tree and searches for k-complementary pairs.
	 * @param values
	 * @param k
	 */
	public static void findKComplemenentaryPairs(int[] values, int k) {
		
		// first of all, we need to divide to conquer, let's build a binary tree
		// I will set root as K. This way, for each new value 'i' I will know which side of the tree
		// I should find 'j'  
		
		Leaf root = new Leaf(k);
		countIterations = 0;
		for (int i = 0; i < values.length; i++) {
			addLeaf(root, values[i]);
		}
		
		
		System.out.print("Tree built: ");
		printTree(root); 					// this is just for debugging and will not be included in asymptotic analysis
		System.out.println("\nTotal of " + countIterations + " iterations to build a tree for an array of " + values.length + " elements");
		
		StringBuffer report = new StringBuffer();
		
		System.out.println("\nPairs:");
		// Trespass array and find value j that satisfies A[i] + A[j] = k
		
		int countTotal = 0;
		for (int i = 0; i < values.length; i++) {
			countIterations = 0;
			int val = values[i];
			int wanted = k - val;
			if (hasValue(root, wanted)) { 
				System.out.println("- A[i,j] = [" + val + "," + wanted + "]");
			}
			report.append("Iteration " + i + " took " + countIterations + " operations to search in the tree\n");
			countTotal += countIterations;
		}
		
		System.out.println(report.toString());
		System.out.println("Total of operations for n=" + values.length + " was " + countTotal + " operations");
		
	}

	private static void printTree(Leaf root) {
		if (root == null) {
			return;
		}
		
		printTree(root.getLeft());
		System.out.print(root.getValue() + " ");
		printTree(root.getRight());
	}

	private static boolean hasValue(Leaf root, int wanted) {
		countIterations++;
		
		if (root != null) {
			int rootValue = root.getValue();
			if (rootValue == wanted) {
				return true;
			} else if (wanted < rootValue) {
				return hasValue(root.getLeft(), wanted);
			} else if (wanted > rootValue) {
				return hasValue(root.getRight(), wanted);
			}
		}
		
		return false;
	}

	private static void addLeaf(Leaf root, int value) {
		if (value < root.getValue()) {
			Leaf left = root.getLeft();
			if (left == null) {
				root.setLeft(new Leaf(value));
				countIterations++;
				return;
			} 
			addLeaf(left, value);
			return;
		}
		
		if (value > root.getValue()) {
			
			Leaf right = root.getRight();
			if (right == null) {
				root.setRight(new Leaf(value));
				countIterations++;
				return;
			} 
			
			addLeaf(right, value);
			return;
			
		}
		
	}
	
//	public static void main(String[] args) {
//		if (args.length < 3) {
//			System.out.println("Usage: ");
//			System.out.println("java -jar k v1 v2 ... vn");
//			System.out.println("\n Please just informe integers");
//			System.exit(0);
//		}
//		
//		int k = Integer.parseInt(args[0]);
//		
//		int[] values = new int[args.length - 1];
//		for (int i = 1; i < args.length; i++) {
//			values[i - 1] = Integer.parseInt(args[i]);
//		}
//		
//		findKComplemenentaryPairs(values, k);
//	}
	
	public static void main(String[] args) {
		Set<Integer> valuesSet = new HashSet<Integer>();
		for (int i = -100; i <= 100; i++) {
			int v = (int) (i * Math.random());
			valuesSet.add(v);
		}
		
		int[] values = new int[valuesSet.size()];
		int i = 0;
		for (Integer v : valuesSet) {
			values[i] = v.intValue();
			i++;
		}
		
		findKComplemenentaryPairs(values, 7);
	}
}
