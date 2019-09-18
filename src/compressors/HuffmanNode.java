package compressors;

/**
 * Uzel stromu statick�ho huffmanova k�du.
 */
public interface HuffmanNode extends CodeTreeNode, Comparable<HuffmanNode> {
	/**
	 * Vrac� �etnost znaku.
	 * @return �etnost znaku.
	 */
	long getFreq();
}
