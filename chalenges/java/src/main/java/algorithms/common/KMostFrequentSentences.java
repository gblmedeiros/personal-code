package algorithms.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Implementation for a special k-most frequent word which regards sentences.
 * It takes a huge file with sentences that are separated by pipe delimiter "|".
 * 
 * It runs as follow: 
 * Given a huge file of sentences, reads line by line and split sentences delimited by pipe.
 * For each sentence, evaluates its frequency and store in a map. If this map is full, dumps to a file and clear map.  
 * Proceeds until end of file.
 * With the n dump files generated, build a merge file of results using External N-Way Sort strategy and puts each frequency into a <i>min-heap</i> of capacity k.
 * At the end, retrieve the kth frequent elements.
 * 
 * @author gblmedeiros
 *
 */
public class KMostFrequentSentences implements Runnable {

	private static final int MAX_RECORDS = 500;
	private int countDumpFiles = 0;
	private int capacity;
	private File file;
	private int maxRecords;
	private boolean keepDump = false;
	
	public KMostFrequentSentences(int capacity, File file) {
		super();
		this.capacity = capacity;
		this.file = file;
		maxRecords = MAX_RECORDS;
	}
	
	public KMostFrequentSentences(int capacity, File file, int maxRecords, boolean keepDump) {
		super();
		this.capacity = capacity;
		this.file = file;
		this.maxRecords = maxRecords;
		this.keepDump = keepDump;
	}

	@Override
	public void run() {
		File dumpDir;
		try {
			dumpDir = digestFile(file);
			mergeDumps(dumpDir);
			if (!keepDump && dumpDir.exists()) {
				deleteDump(dumpDir);
			}
		} catch (Exception e) {
			System.out.println("Error processing");
			e.printStackTrace();
		}
	}
	
	private void deleteDump(File dumpDir) {
		if (dumpDir.exists()) {
			if (dumpDir.isDirectory()) {
				File[] listFiles = dumpDir.listFiles();
				for (File f : listFiles) {
					deleteDump(f);
				}
			} 
			
			dumpDir.delete();
			
		}
		
	}
	/**
	 * Helper class to store sentence frequency and reduce dictionary updates
	 */
	private static class MyFrequency {
		private int frequency;

		public MyFrequency(int frequency) {
			super();
			this.frequency = frequency;
		}

		public int getFrequency() {
			return frequency;
		}

		public void increment() {
			this.frequency = frequency + 1;
		}
		
	}
	
	/**
	 * Reads a huge file and generate frequency for sentences.
	 * Each sentence is put into a map until it reaches max records then a new dump file is created.   
	 *   
	 * @param bigFile
	 * @return directory with all dump files 
	 * @throws IOException
	 */
	public File digestFile(File bigFile) throws IOException {
	
		final Map<String, MyFrequency> dictionary = new TreeMap<String, MyFrequency>(); // sorted sentence frequency 
		File dumpDir = new File(bigFile.getParentFile(), "dump");
		try (BufferedReader buffer = new BufferedReader(new FileReader(bigFile))) {
			String line = null;
			while ((line = buffer.readLine()) != null) {
				String[] phrases = line.split(Pattern.quote("|"));
				for (String p : phrases) {
					final String word = p.trim(); 
					if (dictionary.containsKey(word)) {
						updateFrequency(dictionary, word);
					} else {
						if (dictionary.size() > maxRecords) {
							dumpToFile(dumpDir, dictionary);
						}
						dictionary.put(word, new MyFrequency(1));
					}
				}
			}
			
			// flush
			if (dictionary.size() > 0) {
				dumpToFile(dumpDir, dictionary);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found (" + bigFile + ")");
		} catch (IOException e) {
			System.out.println("IO Error");
		}
		
		return dumpDir;
		
	}

	/**
	 * Creates a file and dump contents.
	 * @param dumpDir output directory
	 * @param dictionary a map with sentences
	 * @throws IOException
	 */
	private void dumpToFile(File dumpDir, Map<String, MyFrequency> dictionary) throws IOException {
		dumpDir.mkdir();
		File dumpFile = new File(dumpDir, "dump-" + System.currentTimeMillis() + "-" + String.valueOf(countDumpFiles));
		countDumpFiles++;
		try (BufferedWriter bfw = new BufferedWriter(new FileWriter(dumpFile))) {
			for (Entry<String, MyFrequency> entry : dictionary.entrySet()) {
				bfw.write(entry.getKey() + ";" + entry.getValue().getFrequency());
				bfw.newLine();
			}
			dictionary.clear();
		}
	}

	private void updateFrequency(final Map<String, MyFrequency> dictionary, String p) {
		MyFrequency frequency = dictionary.get(p);
		if (frequency == null) {
			frequency = new MyFrequency(1);
			dictionary.put(p, frequency);							
		} else {
			frequency.increment();
		}
	}
	
	/**
	 * Merge all dump files inside a directory dumpsDir.
	 * It also creates a merge file with all results inside this directory.
	 * At the end, it shows k-most frequent sentences.
	 * @param dumpsDir directory with dump files
	 * @throws IOException
	 */
	public void mergeDumps(File dumpsDir) throws IOException {
		
		if (dumpsDir.exists() && dumpsDir.isDirectory()) {
			File[] files = dumpsDir.listFiles();
			BufferedReader[] bfrs = new BufferedReader[files.length];
			for (int i = 0 ; i < files.length; i++) {
				bfrs[i] = new BufferedReader(new FileReader(files[i]));
			}
			
			if (bfrs.length > 0) {
				
				// minHeap to retrieve k most frequent
				PriorityQueue<HeapNode> heap = new PriorityQueue<>(capacity, (o1, o2) -> {
					return Integer.compare(o1.frequency, o2.frequency);
				});

				File outputFile = new File(dumpsDir, "merge-" + System.currentTimeMillis());
				try (BufferedWriter output = new BufferedWriter(new FileWriter(outputFile))) {
					mergeFiles(bfrs, output, heap);
				}
				
				System.out.println(capacity + "-most frequent sentences: ");
				System.out.println("------------------------------------------------------------------------");
				Iterator<HeapNode> heapIterator = heap.iterator();
				while (heapIterator.hasNext()) {
					HeapNode next = heapIterator.next();
					System.out.println("Sentence: " + next.word + " | Frequency: " + next.frequency);
				}
				
			}
		} else {
			System.out.println(dumpsDir.getAbsolutePath() + " is not a directory");
		}
	}
	
	private class PQDictionary {
		
		private String word;
		private Integer frequency;
		
		private BufferedReader bfr;

		public PQDictionary(BufferedReader bfr) throws IOException {
			super();
			this.bfr = bfr;
			fetch();
		}
		
		// buffers entries
		public void fetch() throws IOException {
			String row = bfr.readLine();
			if (row != null) {
				String[] split = row.split(";");
				word = split[0];
				frequency = Integer.parseInt(split[1]);
			} else {
				word = null;
				frequency = 0;
			}
		}
		
		public String getWord() {
			return word;
		}
		
		public Integer getFrequency() {
			return frequency;
		}
		
		public boolean isEmpty() {
			return word == null;
		}
		public void close() {
			try {
				bfr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Reads entries from all files and merge them.
	 * When merging, if a entry is unique, puts it into min-heap to rank.
	 * @param bfrs
	 * @param output
	 * @param heap
	 * @throws IOException
	 */
	private void mergeFiles(BufferedReader[] bfrs, BufferedWriter output, PriorityQueue<HeapNode> heap) throws IOException {
		PriorityQueue<PQDictionary> pq = new PriorityQueue<>((o1, o2) -> o1.getWord().compareTo(o2.getWord()));
		
		for (int i = 0; i < bfrs.length ; i++) {
			PQDictionary node = new PQDictionary(bfrs[i]);
			pq.add(node);
		}
		
		String lastWord = null; // caches repeated values
		Integer lastWordFrequency = new Integer(0);
		while (!pq.isEmpty()) {
			PQDictionary head = pq.poll();
			// if repeating word, increment frequency
			if (lastWord != null) {
				if (head.getWord().equals(lastWord)) {
					lastWordFrequency += head.getFrequency();				
				} else {
					// if changing word, output last one (just for debugging)
					output.write(lastWord + ";" + lastWordFrequency);
					output.newLine();
					
					// it is a new entry so insert into minHeap if it has space
					// else it checks if greater than min value to remove root and add new one
					HeapNode heapNode = new HeapNode(lastWord, lastWordFrequency);
					if (heap.size() < capacity) {
			            heap.add(heapNode);
			        } else if (heap.peek().frequency < heapNode.frequency) {
			            heap.remove();
			            heap.add(heapNode);
			        }
					
					// keeps last entry
					lastWord = head.getWord();
					lastWordFrequency = head.getFrequency();
				}
			} else {
				lastWord = head.getWord();
				lastWordFrequency = head.getFrequency();
			}
			
			head.fetch();
			if (head.isEmpty()) {
				head.close();
            } else {
                pq.add(head); // add it back
			}
			
		}
	}
	
	private class HeapNode {
		public String word;
		public Integer frequency;
		public HeapNode(String word, Integer frequency) {
			super();
			this.word = word;
			this.frequency = frequency;
		}
	}
	
	public static void main(String[] args) {
		
		if (args.length < 2) {
			showUsage();
			System.exit(0);
		}
		
		File f = new File(args[0]);
		int k = Integer.parseInt(args[1]);
		int maxRecords = MAX_RECORDS;
		boolean keep = false;
		for (int i = 2; i < args.length; i = i + 2) {
			String o = args[i];
			if (o.equals("-m")) {
				maxRecords = Integer.parseInt(args[i + 1]);
			} else if (o.equals("-d")) {
				keep = true;
			}
		}
		KMostFrequentSentences kmost = new KMostFrequentSentences(k, f, maxRecords, keep);
		kmost.run();

	}

	private static void showUsage() {
		System.out.println("Usage:");
		System.out.println("java -jar KMostFrequent <file> <k>");
		System.out.println("file\t: file with sentences to be processed");
		System.out.println("k\t: kth most frequent sentences in file");
		System.out.println("Options: ");
		System.out.println(" -m max-records\t: if huge file, it delimits file's number of records processed");
		System.out.println(" -d\t\t: keep dump and merged files of frequency");
	}

	
}
