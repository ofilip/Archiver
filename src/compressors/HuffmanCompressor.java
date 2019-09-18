package compressors;

import java.io.*;

import exceptions.InvalidArchiveException;
import utils.*;

/**
 * Kompresor pro statick� huffman�v k�d.
 */
public class HuffmanCompressor implements Compressor, SafelyInterruptable {
	/** Velikost kroku indikace pr�b�hu operac�. */
	private static final int PROGRESS_STEP = 1024;
	
	/* Skupiny konstant PROGRESS_COMPRESS_*_RATIO a PROGRESS_DECOMPRESS_*_RATIO
	 * mus� d�t v sou�tu PROGRESS_COMPRESS_RATIO resp. PROGRESS_DECOMPRESS_RATIO
	 */
	
	/** O�ek�van� pom�r doby komprese k dob� archivace. */
	public static final double PROGRESS_COMPRESS_RATIO = 3;
	
	/** O�ek�van� pom�r doby �ten� �etnost� znak� k dob� archivace. */
	private static final double PROGRESS_COMPRESS_READ_FREQUIENCIES_RATIO = 0.7;
	/** O�ek�van� pom�r doby vytv��en� tabulky k�d� k dob� archivace. */
	private static final double PROGRESS_COMPRESS_BUILD_TABLE_RATIO = 0.01;
	/** O�ek�van� pom�r doby z�pisu hlavi�ky k dob� archivace. */
	private static final double PROGRESS_COMPRESS_WRITE_HEADER_RATIO = 0.01;
	/** O�ek�van� pom�r doby t�la komprimovan�ho souboru k dob� archivace. */
	private static final double PROGRESS_COMPRESS_WRITE_BODY_RATIO = 2.28;
	
	/** O�ek�van� pom�r doby dekomprese k dob� dearchivace. */
	public static final double PROGRESS_DECOMPRESS_RATIO = 2;
	
	/** O�ek�van� pom�r doby �ten� hlavi�ky k dob� dearchivace. */
	private static final double PROGRESS_DECOMPRESS_READ_HEADER_RATIO = 0.05;
	/** O�ek�van� pom�r doby �ten� t�la zkomprimovan�ho souboru k dob� archivace. */
	private static final double PROGRESS_DECOMPRESS_READ_BODY_RATIO = 1.95;
	
	/** Indik�tor p�eru�en�. */
	private PackedBoolean interrupt;
	
	/**
	 * Konstruktor.
	 */
	public HuffmanCompressor() {
		interrupt = new PackedBoolean(false);
	}
	
	@Override
	public void Compress(File in, File out) throws IOException {
		Compress(in, out, null);
	}
	
	@Override
	public void Compress(File in, File out, Subprogress p) throws IOException {
		interrupt.setValue(false);
		if (p==null) {
			SubprogressOperation operation = new BasicSubprogressOperation((long)(PROGRESS_COMPRESS_RATIO*in.length()), "Compressing... "+in.getName());
			SubprogressOperation[] operations = new SubprogressOperation[1];
			operations[0] = operation;
			p = new Subprogress(operations);
		}
		
		if (in.length()==0) {
			FileOutputStream os = new FileOutputStream(out, false);
			os.close();
			p.addToCurrentlyDone(p.getCurrentOperationTodo());
			return;
		}
		
		long[] frequencies = Utils.GetCharacterFrequencies(in, p, PROGRESS_COMPRESS_READ_FREQUIENCIES_RATIO, PROGRESS_STEP, interrupt);
		HuffmanTree tree = new HuffmanTree(frequencies);
		CodeTable table = tree.toCodeTable();
		
		p.addToCurrentlyDone((long)(p.getCurrentOperationTodo()*PROGRESS_COMPRESS_BUILD_TABLE_RATIO));
		
		FileInputStream is = new FileInputStream(in);;
		FileOutputStream os = new FileOutputStream(out, false);
		
		BufferedInputStream bis = new BufferedInputStream(is);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BitOutputStream bitos = new BitOutputStream(bos);
		
		Utils.writeHeader(bitos, table, interrupt);
		p.addToCurrentlyDone((long)(PROGRESS_COMPRESS_WRITE_HEADER_RATIO*p.getCurrentOperationTodo()));
		Utils.writeCompressedBody(bis, bitos, table, p, PROGRESS_COMPRESS_WRITE_BODY_RATIO, PROGRESS_STEP, interrupt);
		
		bis.close();
		bitos.close();
		interrupt.setValue(false);
	}
	
	@Override
	public void Decompress(File in, File out) throws IOException, InvalidArchiveException {
		Decompress(in, out, null);
	}
	
	@Override
	public void Decompress(File in, File out, Subprogress p) throws IOException, InvalidArchiveException {
		interrupt.setValue(false);
		if (p==null) {
			SubprogressOperation operation = new BasicSubprogressOperation((long)(PROGRESS_COMPRESS_RATIO*in.length()), "Compressing... "+in.getName());
			SubprogressOperation[] operations = new SubprogressOperation[1];
			operations[0] = operation;
			p = new Subprogress(operations);
		}
		
		if (in.length()==0) {
			FileOutputStream os = new FileOutputStream(out, false);
			os.close();
			p.addToCurrentlyDone(p.getCurrentOperationTodo());
			return;
		}
		
		FileInputStream is;
		FileOutputStream os;
		BufferedInputStream bis;
		BufferedOutputStream bos;
		BitInputStream bitis;
		
		is = new FileInputStream(in);
		bis = new BufferedInputStream(is);
		bitis = new BitInputStream(bis);
		os = new FileOutputStream(out);
		bos = new BufferedOutputStream(os);
		
		long characterCount;
		long[] frequencies;
		HuffmanTree tree;
		
		try {
			frequencies = Utils.readHeader(bitis, interrupt);
		} catch (InvalidArchiveException e) {
			bitis.close();
			bos.close();
			out.delete();
			throw e;
		}
		
		p.addToCurrentlyDone((long)(PROGRESS_DECOMPRESS_READ_HEADER_RATIO*p.getCurrentOperationTodo()));
		
		tree = new HuffmanTree(frequencies);
		characterCount = Utils.characterCount(frequencies);
		
		try {
			Utils.writeDecompressedBody(bitis, bos, tree, characterCount, p, PROGRESS_STEP, PROGRESS_DECOMPRESS_READ_BODY_RATIO, interrupt);
		} catch (InvalidArchiveException e) {
			bitis.close();
			bos.close();
			out.delete();
			throw e;
		}
		
		bitis.close();
		bos.close();
		interrupt.setValue(false);
	}

	@Override
	public void safelyInterrupt() {
		interrupt.setValue(true);
	}
}
