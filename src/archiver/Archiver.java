package archiver;

import java.io.*;
import java.util.Vector;

import javax.swing.JOptionPane;

import utils.*;
import exceptions.InvalidArchiveException;
import gui.Main;

/**
 * Tøída pro vytváøení a rozbalování archivù.
 */
public class Archiver implements SafelyInterruptable {
	/** Velikost kroku pro indikaci prùbìhu operací. */
	private static final int PROGRESS_STEP = 1024;
	/** Indikátor pøerušení operace. */
	private boolean interrupt;
	
	/**
	 * Konstruktor.
	 */
	public Archiver() {
		this.interrupt = false;
	}
	
	/**
	 * Vytvoøí nový archiv.
	 * @param f Jediný soubor, který bude zabalen do archivu.
	 * @param out Soubor nového archivu.
	 * @throws IOException Chyba I/O.
	 */
	public void pack(File f, File out) throws IOException {
		pack(f, out, null);
	}
	
	/**
	 * Vytvoøí nový archiv.
	 * @param files Seznam souborù k zabalení.
	 * @param out Soubor nového archivu.
	 * @throws IOException Chyba I/O.
	 */
	public void pack(Vector<File> files, File out) throws IOException {
		pack(files, out, null);
	}
	
	/**
	 * Vytvoøí nový archiv.
	 * @param f Jediný soubor, který bude zabalen do archivu.
	 * @param out Soubor nového archivu.
	 * @param p Indikátor prùbìhu operace.
	 * @throws IOException Chyba I/O.
	 */
	public void pack(File f, File out, Subprogress p) throws IOException {
		Vector<File> files = new Vector<File>();
		
		files.add(f);
		
		pack(files, out, p);
	}

	/**
	 * Vytvoøí nový archiv.
	 * @param files Seznam souborù k zabalení.
	 * @param out Soubor nového archivu.
	 * @param p Indikátor prùbìhu operace.
	 * @throws IOException Chyba I/O.
	 */
	public void pack(Vector<File> files, File out, Subprogress p) throws IOException {
		interrupt = false;
		/* Pøipravit indikátor prùbìhu do správného stavu */
		if (p==null) {
			Vector<File> filesToProcess = Utils.listAllFiles(files);
			p = new FileSubprogress(filesToProcess);
		}
		
		FileOutputStream os = new FileOutputStream(out);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		DataOutputStream dos = new DataOutputStream(bos);
		
		for (File f: files) {
			pack(f, dos, "", p);
			if (interrupt) {
				break;
			}
		}
		
		dos.close();
		interrupt = false;
	}
	
	/**
	 * Pøidá soubor/adresáø do právì zpracovávaného archivu
	 * @param f Pøidávaný soubor/adresáø.
	 * @param out Vytváøený archiv.
	 * @param root Relativní cesta od koøenového adresáøe archivu.
	 * @param p Indikátor prùbìhu operace.
	 * @throws IOException Chyba I/O.
	 */
	private void pack(File f, DataOutputStream out, String root, Subprogress p) throws IOException {
		String filename = root+f.getName();
		if (f.isDirectory()) {
			out.writeByte(0);
			out.writeUTF(filename);
			
			String new_root = root+f.getName()+'/';
			
			for (File f_in: f.listFiles()) {
				pack(f_in, out, new_root, p);
				if (interrupt) {
					break;
				}
			}
		} else {
			out.writeByte(1);
			out.writeUTF(filename);
			out.writeLong(f.length());
			
			FileInputStream is = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(is);
			
			int counter = 0;
			int b;
			while (!interrupt&&(-1!=(b = bis.read()))) {
				out.writeByte(b);
				if (counter++ % PROGRESS_STEP == 0) {
					p.addToCurrentlyDone(PROGRESS_STEP);
				}
			}
			
			bis.close();
			p.moveToNextOperation();
		}
	}
	
	/**
	 * Rozbalí archiv.
	 * @param f Soubor archivu.
	 * @param dir Cílový adresáø.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Chybný archiv.
	 */
	public void unpack(File f, File dir) throws IOException, InvalidArchiveException {
		unpack(f, dir, null, null);
	}
	
	/**
	 * Rozbalí archiv.
	 * @param f Soubor archivu.
	 * @param dir Cílový adresáø.
	 * @param p Indikátor prùbìhu operace.
	 * @param createdFiles Indikátor vytvoøených souborù (pro zastavení operace).
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Chybný archiv.
	 */
	public void unpack(File f, File dir, Subprogress p, Vector<File> createdFiles) throws IOException, InvalidArchiveException {
	    PackedBoolean skipAll = PackedBoolean.FALSE;
		PackedBoolean rewriteAll = PackedBoolean.FALSE;
		PackedBoolean mergeAllDirs = PackedBoolean.FALSE;
		
		interrupt = false;
		
		if (p==null) {
			Vector<File> files = new Vector<File>();
			files.add(f);
			p = new FileSubprogress(files);
		}
		
		if (createdFiles==null) {
			createdFiles = new Vector<File>();
		}
		
		FileInputStream is = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(is);
		DataInputStream dis = new DataInputStream(bis);
		
		int type;
		ProgressUpdater u = new ProgressUpdater(p, PROGRESS_STEP);
		while (!interrupt&&(-1!=(type = dis.read()))) {
			boolean skipFile = false;
			String filename = dis.readUTF().replace('/', File.separatorChar);
			u.update(1+filename.length());
			File newFile = new File(dir.getAbsolutePath()+File.separatorChar+filename);
			synchronized (createdFiles) {
				createdFiles.add(newFile);
			}
				
			if (type==0) { /* adresáø */
				if (newFile.exists()) {
					if (newFile.isDirectory()) {
						showExistingDirectoryDialog(newFile, mergeAllDirs);
					} else {
						skipFile = showExistingFileDialog(newFile, skipAll, rewriteAll);
						if (!skipFile) {
							newFile.delete();
						}
					}
				}
				
				newFile.mkdir();
			} else if (type==1) { /* soubor */
				if (newFile.exists()) {
					skipFile = showExistingFileDialog(newFile, skipAll, rewriteAll);
					
					if (!skipFile) {
						if (newFile.isFile()) {
							newFile.delete();
						} else {
							deleteDirectory(newFile);
						}
					}
				}
				
				long flen = dis.readLong();
				FileOutputStream os = null;
				BufferedOutputStream bos = null;
				
				if (!skipFile) {
					os = new FileOutputStream(newFile);
					bos = new BufferedOutputStream(os);
				}
				
				while (!interrupt&&flen>0) {
					byte b = dis.readByte();
					if (!skipFile) {
						bos.write(b);
					}
					flen--;
					u.update();
				}
				
				if (!skipFile) {
					bos.close();
				}
			} else {
				throw new InvalidArchiveException();
			}
		}
		
		dis.close();
		interrupt = false;
	}
	
	/**
	 * Zobrazí dialog pro již existující adresáø bìhem rozbalování archivu.
	 * @param dir Adresáø.
	 * @param mergeAllDirs Indikuje, zda má být adresáø vždy slouèen s novým adresáøem (v závislosti na stávající èi pøedchozí uživatele).
	 */
	private void showExistingDirectoryDialog(File dir, PackedBoolean mergeAllDirs) {
		if (mergeAllDirs.getValue()) {
			return;
		}
		
		String[] options = { "Ano", "Ano všem", "Ne" };
		
		int n = JOptionPane.showOptionDialog(Main.getFrame(),
				"Adresáø '" + dir.getName() + "' již existuje. Slouèit jeho obsah s extrahovanými soubory?",
				"Adresáø existuje",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[2]);
				
		switch (n) {
		case 1:
			mergeAllDirs.setValue(true);
		default:
			safelyInterrupt();		
		}
	}
	
	/**
	 * Rekurzivnì smaže adresáø.
	 * @param dir Adresáø ke smazání.
	 */
	private void deleteDirectory(File dir) {
		if (dir.isFile()) {
			dir.delete();
		} else {
			for (File f: dir.listFiles()) {
				deleteDirectory(f);
				dir.delete();
			}
		}
	}
	
	/**
	 * Zobrazí dialog pro již existující soubor bìhem rozbalování archivu.
	 * @param f Soubor.
	 * @param skipAll Indikuje, zda má být existující soubor vždy pøeskoèen (v závislosti na stávající èi pøedchozí uživatele).
	 * @param rewriteAll Indukuje, zda má byt existující soubor vždy pøepsán (v závislosti na stávající èi pøedchozí uživatele).
	 * @return true, má-li být soubor pøeskoèen; false, má-li být pøepsán.
	 */
	private boolean showExistingFileDialog(File f, PackedBoolean skipAll, PackedBoolean rewriteAll) {
		if (skipAll.getValue()) {
			return true;
		} else if (rewriteAll.getValue()) {
			return false;
		}
		
		String[] options = {"Pøeskoèit", "Pøeskoèit vše", 
				"Pøepsat", "Pøepsat vše", "Zrušit operaci"};
		
		int n = JOptionPane.showOptionDialog(Main.getFrame(),
				"Soubor '" + f.getName() + "' již existuje.",
				"Soubor existuje",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);
		
		switch (n) {
		case 1:
			skipAll.setValue(true);
		case 0:
			return true;
		case 3:
			rewriteAll.setValue(true);
		case 2:
			return false;
		default:
			safelyInterrupt();	
			return true;
		}
	}

	/**
	 * Bezpeènì pøeruší bìh stávající operace.
	 */
	@Override
	public void safelyInterrupt() {
		interrupt = true;
	}
}
