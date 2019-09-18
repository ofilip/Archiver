package compressors;

/**
 * Uzel stromu statického huffmanova kódu.
 */
public interface HuffmanNode extends CodeTreeNode, Comparable<HuffmanNode> {
	/**
	 * Vrací èetnost znaku.
	 * @return Èetnost znaku.
	 */
	long getFreq();
}
