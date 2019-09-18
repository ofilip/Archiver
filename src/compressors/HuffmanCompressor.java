package compressors;

import java.io.*;

import exceptions.InvalidArchiveException;
import utils.*;

/**
 * Kompresor pro statický huffmanùv kód.
 */
public class HuffmanCompressor implements Compressor, SafelyInterruptable {
	/** Velikost kroku indikace prùbìhu operací. */
	private static final int PROGRESS_STEP = 1024;
	
	/* Skupiny konstant PROGRESS_COMPRESS_*_RATIO a PROGRESS_DECOMPRESS_*_RATIO
	 * musí dát v souètu PROGRESS_COMPRESS_RATIO resp. PROGRESS_DECOMPRESS_RATIO
	 */
	
	/** Oèekávaný pomìr doby komprese k dobì archivace. */
	public static final double PROGRESS_COMPRESS_RATIO = 3;
	
	/** Oèekávaný pomìr doby ètení èetností znakù k dobì archivace. */
	private static final double PROGRESS_COMPRESS_READ_FREQUIENCIES_RATIO = 0.7;
	/** Oèekávaný pomìr doby vytváøení tabulky kódù k dobì archivace. */
	private static final double PROGRESS_COMPRESS_BUILD_TABLE_RATIO = 0.01;
	/** Oèekávaný pomìr doby zápisu hlavièky k dobì archivace. */
	private static final double PROGRESS_COMPRESS_WRITE_HEADER_RATIO = 0.01;
	/** Oèekávaný pomìr doby tìla komprimovaného souboru k dobì archivace. */
	private static final double PROGRESS_COMPRESS_WRITE_BODY_RATIO = 2.28;
	
	/** Oèekávaný pomìr doby dekomprese k dobì dearchivace. */
	public static final double PROGRESS_DECOMPRESS_RATIO = 2;
	
	/** Oèekávaný pomìr doby ètení hlavièky k dobì dearchivace. */
	private static final double PROGRESS_DECOMPRESS_READ_HEADER_RATIO = 0.05;
	/** Oèekávaný pomìr doby ètení tìla zkomprimovaného souboru k dobì archivace. */
	private static final double PROGRESS_DECOMPRESS_READ_BODY_RATIO = 1.95;
	
	/** Indikátor pøerušení. */
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
