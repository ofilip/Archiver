package compressors;

import java.io.*;

import exceptions.InvalidArchiveException;
import utils.BasicSubprogressOperation;
import utils.BitInputStream;
import utils.BitOutputStream;
import utils.SafelyInterruptable;
import utils.ProgressUpdater;
import utils.Subprogress;
import utils.SubprogressOperation;

/**
 * Kompresor pro metodu FGK (adaptivní huffmanùv kód).
 */
public class FGKCompressor implements Compressor, SafelyInterruptable {
	/** Velikost kroku indikace prùbìhu operací. */
	private static final int PROGRESS_STEP = 1024;
	
	/** Oèekávaný pomìr doby komprese k dobì archivace. */
	public static final double PROGRESS_COMPRESS_RATIO = 19;
	/** Oèekávaný pomìr doby dekomprese k dobì dearchivace. */
	public static final double PROGRESS_DECOMPRESS_RATIO = 19;
	
	/** Indikátor pøerušení operace. */
	private Boolean interrupt;
	
	/**
	 * Konstruktor.
	 */
	public FGKCompressor() {
		interrupt = false;
	}
	
	@Override
	public void Compress(File in, File out) throws IOException {
		Compress(in, out, null);
	}
	
	@Override
	public void Compress(File in, File out, Subprogress p) throws IOException {
		interrupt = false;
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
		
		FGKTree tree = new FGKTree();
		
		FileInputStream is = new FileInputStream(in);
		FileOutputStream os = new FileOutputStream(out);
		
		BufferedInputStream bis = new BufferedInputStream(is);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		
		long flen = in.length();
		byte[] header = new byte[8];
		for (int i=7; i>=0; i--) {
			header[i] = (byte)(flen);
			flen = flen>>8;
		}
		
		bos.write(header);
		
		BitOutputStream bitos = new BitOutputStream(bos);
		
		int c;
		ProgressUpdater u = new ProgressUpdater(p, PROGRESS_STEP, PROGRESS_COMPRESS_RATIO);
		while (!interrupt&&-1!=(c = bis.read())) {
			tree.IncreaseFrequency((char)c);
			bitos.writeBits(tree.getCode(), tree.getCodeLength());
			
			u.update();
		}
		
		bis.close();
		bitos.close();
		interrupt = false;
	}
	
	@Override
	public void Decompress(File in, File out) throws IOException, InvalidArchiveException {
		Decompress(in, out, null);
	}

	@Override
	public void Decompress(File in, File out, Subprogress p) throws IOException, InvalidArchiveException {
		interrupt = false;
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
		
		FileInputStream is = new FileInputStream(in);
		BufferedInputStream bis = new BufferedInputStream(is);
		
		byte[] header = new byte[8];
		long characters_count = 0;
		
		if (bis.read(header)==-1) {
			bis.close();
			throw new InvalidArchiveException();
		}
		
		for (int i=0; i<8; i++) {
			characters_count = characters_count << 8;
			characters_count += ((short)header[i]+256)%256;
		}

		FileOutputStream os = new FileOutputStream(out);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BitInputStream bitis = new BitInputStream(bis);
		
		FGKTree tree = new FGKTree();
		
		ProgressUpdater u = new ProgressUpdater(p, PROGRESS_STEP, PROGRESS_DECOMPRESS_RATIO/8.0);

		while (!interrupt&&characters_count>0) {
			CodeTreeNode n = tree.getRoot();
			while (!(n instanceof FGKLeaf)) {
				int bit = bitis.readBit();
				u.update();
				CodeTreeBranching br = (CodeTreeBranching)n;
				
				if (bit==-1) {
					throw new InvalidArchiveException();
				}
				
				if (bit==0) {
					n = br.getLeft();
				} else {
					n = br.getRight();
				}
			}
			
			FGKLeaf leaf = (FGKLeaf)n;
			
			if (leaf.isNYT()) {
				int c = bitis.readBits(8);
				u.update(8);
				if (c==-1) {
					bitis.close();
					bos.close();
					out.delete();
					throw new InvalidArchiveException();
				}
				bos.write(c);
				tree.IncreaseFrequency((char)c, true);
			} else {
				char c = leaf.getChar();
				bos.write(c);
				tree.IncreaseFrequency(c, true);
			}
			characters_count--;
		}
		
		bitis.close();
		bos.close();
		interrupt = false;
	}

	@Override
	public void safelyInterrupt() {
		interrupt = true;;
	}
}
