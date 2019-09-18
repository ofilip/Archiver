package archiver;

import java.io.*;
import java.util.Vector;

import javax.swing.JOptionPane;

import utils.*;
import exceptions.InvalidArchiveException;
import gui.Main;

/**
 * T��da pro vytv��en� a rozbalov�n� archiv�.
 */
public class Archiver implements SafelyInterruptable {
	/** Velikost kroku pro indikaci pr�b�hu operac�. */
	private static final int PROGRESS_STEP = 1024;
	/** Indik�tor p�eru�en� operace. */
	private boolean interrupt;
	
	/**
	 * Konstruktor.
	 */
	public Archiver() {
		this.interrupt = false;
	}
	
	/**
	 * Vytvo�� nov� archiv.
	 * @param f Jedin� soubor, kter� bude zabalen do archivu.
	 * @param out Soubor nov�ho archivu.
	 * @throws IOException Chyba I/O.
	 */
	public void pack(File f, File out) throws IOException {
		pack(f, out, null);
	}
	
	/**
	 * Vytvo�� nov� archiv.
	 * @param files Seznam soubor� k zabalen�.
	 * @param out Soubor nov�ho archivu.
	 * @throws IOException Chyba I/O.
	 */
	public void pack(Vector<File> files, File out) throws IOException {
		pack(files, out, null);
	}
	
	/**
	 * Vytvo�� nov� archiv.
	 * @param f Jedin� soubor, kter� bude zabalen do archivu.
	 * @param out Soubor nov�ho archivu.
	 * @param p Indik�tor pr�b�hu operace.
	 * @throws IOException Chyba I/O.
	 */
	public void pack(File f, File out, Subprogress p) throws IOException {
		Vector<File> files = new Vector<File>();
		
		files.add(f);
		
		pack(files, out, p);
	}

	/**
	 * Vytvo�� nov� archiv.
	 * @param files Seznam soubor� k zabalen�.
	 * @param out Soubor nov�ho archivu.
	 * @param p Indik�tor pr�b�hu operace.
	 * @throws IOException Chyba I/O.
	 */
	public void pack(Vector<File> files, File out, Subprogress p) throws IOException {
		interrupt = false;
		/* P�ipravit indik�tor pr�b�hu do spr�vn�ho stavu */
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
	 * P�id� soubor/adres�� do pr�v� zpracov�van�ho archivu
	 * @param f P�id�van� soubor/adres��.
	 * @param out Vytv��en� archiv.
	 * @param root Relativn� cesta od ko�enov�ho adres��e archivu.
	 * @param p Indik�tor pr�b�hu operace.
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
	 * Rozbal� archiv.
	 * @param f Soubor archivu.
	 * @param dir C�lov� adres��.
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Chybn� archiv.
	 */
	public void unpack(File f, File dir) throws IOException, InvalidArchiveException {
		unpack(f, dir, null, null);
	}
	
	/**
	 * Rozbal� archiv.
	 * @param f Soubor archivu.
	 * @param dir C�lov� adres��.
	 * @param p Indik�tor pr�b�hu operace.
	 * @param createdFiles Indik�tor vytvo�en�ch soubor� (pro zastaven� operace).
	 * @throws IOException Chyba I/O.
	 * @throws InvalidArchiveException Chybn� archiv.
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
				
			if (type==0) { /* adres�� */
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
	 * Zobraz� dialog pro ji� existuj�c� adres�� b�hem rozbalov�n� archivu.
	 * @param dir Adres��.
	 * @param mergeAllDirs Indikuje, zda m� b�t adres�� v�dy slou�en s nov�m adres��em (v z�vislosti na st�vaj�c� �i p�edchoz� u�ivatele).
	 */
	private void showExistingDirectoryDialog(File dir, PackedBoolean mergeAllDirs) {
		if (mergeAllDirs.getValue()) {
			return;
		}
		
		String[] options = { "Ano", "Ano v�em", "Ne" };
		
		int n = JOptionPane.showOptionDialog(Main.getFrame(),
				"Adres�� '" + dir.getName() + "' ji� existuje. Slou�it jeho obsah s extrahovan�mi soubory?",
				"Adres�� existuje",
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
	 * Rekurzivn� sma�e adres��.
	 * @param dir Adres�� ke smaz�n�.
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
	 * Zobraz� dialog pro ji� existuj�c� soubor b�hem rozbalov�n� archivu.
	 * @param f Soubor.
	 * @param skipAll Indikuje, zda m� b�t existuj�c� soubor v�dy p�esko�en (v z�vislosti na st�vaj�c� �i p�edchoz� u�ivatele).
	 * @param rewriteAll Indukuje, zda m� byt existuj�c� soubor v�dy p�eps�n (v z�vislosti na st�vaj�c� �i p�edchoz� u�ivatele).
	 * @return true, m�-li b�t soubor p�esko�en; false, m�-li b�t p�eps�n.
	 */
	private boolean showExistingFileDialog(File f, PackedBoolean skipAll, PackedBoolean rewriteAll) {
		if (skipAll.getValue()) {
			return true;
		} else if (rewriteAll.getValue()) {
			return false;
		}
		
		String[] options = {"P�esko�it", "P�esko�it v�e", 
				"P�epsat", "P�epsat v�e", "Zru�it operaci"};
		
		int n = JOptionPane.showOptionDialog(Main.getFrame(),
				"Soubor '" + f.getName() + "' ji� existuje.",
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
	 * Bezpe�n� p�eru�� b�h st�vaj�c� operace.
	 */
	@Override
	public void safelyInterrupt() {
		interrupt = true;
	}
}
