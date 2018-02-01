package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.cr.input.generate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class SequenceGenerator {

	private int[][] matrix;
	private SortedMap<Integer, List<String>> sequences;
	private SortedMap<Integer, List<int[]>> finalSequences;

	public SequenceGenerator(int maxLength) {
		matrix = generateMatrix(maxLength);
		sequences = Collections
				.synchronizedSortedMap(new TreeMap<Integer, List<String>>());
		finalSequences = Collections
				.synchronizedSortedMap(new TreeMap<Integer, List<int[]>>());

		generateSequence_FirstDiagonal(maxLength);
		generateSequence_SecondDiagonal(maxLength);

		processSequences();
	}
	
	private void printMatrix() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	private void processSequences() {
		for (int length : sequences.keySet()) {
			List<int[]> newList = new ArrayList<int[]>();

			for (String s : sequences.get(length)) {
				String[] parts = s.split("-");
				int[] array = new int[parts.length];
				for (int i = 0; i < parts.length; i++) {
					array[i] = Integer.parseInt(parts[i]);
				}
				newList.add(array);
			}
			finalSequences.put(length, newList);
		}
	}

	private void generateSequence_SecondDiagonal(int maxLength) {
		if (maxLength < 2) {
			return;
		}
		for (int i = 0; i < maxLength; i++) {
			String s = "";
			for (int j = 0; j < maxLength - i; j++) {
				s += matrix[i][j] + "-";
			}
			int length = maxLength - i;
			s = s.substring(0, s.length() - 1);
			List<String> list = sequences.containsKey(length) ? sequences
					.get(length) : new ArrayList<String>();
			if (!list.contains(s)) {
				list.add(s);
			}
			sequences.put(length, list);
		}

		generateSequence_SecondDiagonal(maxLength - 1);
	}

	private void generateSequence_FirstDiagonal(int maxLength) {
		if (maxLength < 2) {
			return;
		}

		for (int i = 0; i < maxLength; i++) {
			String s = "";
			for (int j = i; j < maxLength; j++) {
				s += matrix[i][j] + "-";
			}
			s = s.substring(0, s.length() - 1);
			int length = maxLength - i;
			List<String> list = sequences.containsKey(length) ? sequences
					.get(length) : new ArrayList<String>();
			if (!list.contains(s)) {
				list.add(s);
			}
			sequences.put(length, list);
		}

		generateSequence_FirstDiagonal(maxLength - 1);
	}

	private int[][] generateMatrix(int maxLength) {
		int[][] matrix = new int[maxLength][maxLength];
		for (int i = 0; i < maxLength; i++) {
			for (int j = 0; j < maxLength; j++) {
				matrix[i][j] = j + 1;
			}
		}
		return matrix;
	}

	private void print(SortedMap<Integer, List<int[]>> seq) {
		for (int length : seq.keySet()) {
			System.out.println(" - L = " + length);
			List<int[]> list = seq.get(length);
			for (int[] array : list) {
				System.out.print(" -- ");
				for (int i : array) {
					System.out.print(i + " ");
				}
				System.out.println();
			}
		}
	}
	
	public SortedMap<Integer, List<int[]>> getFinalSequences() {
		return finalSequences;
	}

	public static void main(String[] args) {
		SequenceGenerator sg = new SequenceGenerator(4);
		sg.print(sg.getFinalSequences());
	}
}
