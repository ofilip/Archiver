package compressors;

import java.util.*;

import utils.Utils;

/**
 * Strom s bitov�mi k�dy statick�ho huffmanova k�du.
 */
public class HuffmanTree extends CodeTree {
	/**
	 * Konstruktor. Vytvo�� strom na z�klad� �etnost� znak� ve vstupn�ch datech.
	 * @param frequencies Pole �etnost� znak�.
	 */
	public HuffmanTree(long[] frequencies) {
		super();
		LinkedList<HuffmanNode> orig = new LinkedList<HuffmanNode>();
		LinkedList<HuffmanNode> joined = new LinkedList<HuffmanNode>();
		
		for (int i=0; i<256; i++) {
			long f = frequencies[i];
			
			if (f>0) {
				orig.add(new HuffmanLeaf((char)i, frequencies[i]));
			}
		}
		
		Collections.sort(orig);
		
		HuffmanNode n1 = orig.poll();
		HuffmanNode n2 = orig.poll();
		
		if (n2==null) {
			root = n1;
			return;
		}
		
		joined.add(new HuffmanBranching(n1,n2));

		while (true) {
			if (!orig.isEmpty()&&orig.peek().getFreq()<joined.peek().getFreq()) {
				n1 = orig.poll();
			} else {
				n1 = joined.poll();
			}
			if (orig.isEmpty()&&joined.isEmpty()) {
				break;
			} else if (orig.isEmpty()) {
				n2 = joined.poll();
			} else if (joined.isEmpty()) {
				n2 = orig.poll();
			} else if (orig.peek().getFreq()<joined.peek().getFreq()) {
				n2 = orig.poll(); 
			} else {
				n2 = joined.poll();
			}
			
			joined.add(new HuffmanBranching(n1,n2));
		}
		
		root = n1;
	}
	
	/**
	 * Vytvo�� tabulku k�d� na z�klad� informac� ze stromu.
	 * @return Tabulka k�d�.
	 */
	public CodeTable toCodeTable() {
		CodeTable table = new CodeTable();
		table.table = new CodeRecord[256];
		
		Arrays.fill(table.table, new CodeRecord((char) 0, 0));
		table.alphabetSize = 0;
		table.maxFreq = 0;
		table.maxFreq = 0;
		
		fillCodeTable(table, (HuffmanNode)root, new byte[0], 0);
		
		return table;
	}
	
	/**
	 * Rekurzivn� vypln� tabulku k�d� na z�klad� informac� ze stromu.
	 * @param table Vypl�ovan� tabulka.
	 * @param node Aktu�ln� uzel.
	 * @param code Aktu�ln� k�d.
	 * @param clen Aktu�ln� d�lka k�du.
	 */
	void fillCodeTable(final CodeTable table, HuffmanNode node, byte[] code, int clen) {
		if (node instanceof HuffmanLeaf) {
			HuffmanLeaf leaf = (HuffmanLeaf)node;
			char c = leaf.getChar();
			long f = leaf.getFreq();
			
			
			table.alphabetSize++;
			table.characterCount += f;
			if (table.maxFreq<f) {
				table.maxFreq = f;
			}
			table.table[c] = new CodeRecord(c, f, code, clen);
		} else {
			byte[] code0 = Utils.AddToCode(code, clen, 0);
			byte[] code1 = Utils.AddToCode(code.clone(), clen, 1);
			HuffmanBranching br = (HuffmanBranching)node;
			
			fillCodeTable(table, (HuffmanNode) br.left, code0, clen+1);
			fillCodeTable(table, (HuffmanNode) br.right, code1, clen+1);
		}
	}
}


