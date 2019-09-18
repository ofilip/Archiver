package gui;

import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.*;
import utils.BasicSubprogressOperation;
import utils.SafelyInterruptableThreadWithSubprogress;
import utils.Subprogress;
import utils.SubprogressOperation;
import utils.Utils;
import compressors.*;
import exceptions.InvalidArchiveException;
import archiver.*;
import java.awt.*;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.*;

/**
 * ComboBox ActionListener: Výbìr koøenového adresáøe
 */
class RootsComboBoxActionListener implements ActionListener {
	/**
	 * Upraví výpis složky dle vybraného koøenového adresáøe.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();

        if (cb.isEnabled()) {
            File newDir = (File)cb.getSelectedItem();
	 
	        if (newDir.canRead()==false) {
	        	cb.setSelectedIndex(Main.origRootIndex);
	        	JOptionPane.showMessageDialog(cb, 
	        			"Koøenový adresáø '"
	        			+newDir.getAbsolutePath()
	        			+"' není k dispozici.");
	        	return;
	        }
	        
	        Main.switchDir(newDir);
	        Main.origRootIndex = cb.getSelectedIndex();
        }
    }
}

/**
 * MenuItem ActionListener: Vybrat
 */
class SelectMenuItemActionListener implements ActionListener {
	/**
	 * Vybere oznaèenou položku.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized(Main.fileTable) {
			ListSelectionModel sm = Main.fileTable.getSelectionModel();
			int anchor = sm.getAnchorSelectionIndex();
			
			sm.addSelectionInterval(anchor, anchor);
		}
	}
}

/**
 * MenuItem ActionListener: Vybrat vše
 */
class SelectAllMenuItemActionListener implements ActionListener {
	/**
	 * Vybere všechny položky ve výpisu složky
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized(Main.fileTable) {
			ListSelectionModel sm = Main.fileTable.getSelectionModel();
			sm.setSelectionInterval(0, Main.fileTableModel.getRowCount());
		}
	}
}

/**
 * MenuItem ActionListener:  Zrušit výbìr
 */
class UnselectMenuItemActionListener implements ActionListener {
	/**
	 * Zruší výbìr všech položek ve výpisu složky.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized(Main.fileTable) {
			ListSelectionModel sm = Main.fileTable.getSelectionModel();
			int anchor = sm.getAnchorSelectionIndex();
			sm.removeSelectionInterval(anchor, anchor);
		}
	}
}


/**
 * Okno s progress barem pro vizualizaci prùbìhu akcí. 
 */
class ProgressGUI implements ActionListener {
	/** Progress bar */
	JProgressBar overallProgressBar;
	/** Samotné okno. */
	JFrame frame;
	/** Panel. */
	JPanel panel;
	
	/** Vlákno s požadovanou akcí. */
	private SafelyInterruptableThreadWithSubprogress t;
	/** Indikátor vytvoøených souborù. Slouží ke zpìtnému smazání v pøípadì pøerušení akce. */
	private Vector<File> createdFiles;
	/** Indikátor zobrazení dialogu pro pøerušení operace. Slouží k možnosti pøerušení operace po jejím skonèení. */
	private Boolean interruptDialogShown;

	/**
	 * Konstruktor.
	 * @param t Vlákno s požadovanou akcí.
	 * @param createdFiles Indikátor vytvoøených souborù.
	 */
	public ProgressGUI(SafelyInterruptableThreadWithSubprogress t, Vector<File> createdFiles) {
		this(t, createdFiles, "Progress");
	}
	
	/**
	 * Konstruktor.
	 * @param t Vlákno s požadovanou akcí.
	 * @param createdFiles Indikátor vytvoøených souborù.
	 * @param title Popisek okna.
	 */
	public ProgressGUI(SafelyInterruptableThreadWithSubprogress t, Vector<File> createdFiles, String title) {
		this.t = t;
		this.createdFiles = createdFiles;
		this.interruptDialogShown = false;
		frame = new JFrame(title);
		panel = new JPanel(new BorderLayout());
	
		JButton interruptButton = new JButton("Pøerušit");
		
		interruptButton.addActionListener(this);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		overallProgressBar = new JProgressBar(0, 100);
		overallProgressBar.setValue(0);
		
		panel.add(overallProgressBar, BorderLayout.CENTER);
		panel.add(interruptButton, BorderLayout.PAGE_END);
		
		frame.add(panel);
		frame.setSize(320,95);
		frame.setResizable(false);

		int x = Main.frame.getX() + Main.frame.getWidth() / 2 - frame.getWidth() / 2;
		int y = Main.frame.getY() + Main.frame.getHeight() / 2 - frame.getHeight() / 2;
		
		frame.setLocation(x, y);
		
		frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		    	showInterruptionDialog();
		    }
		});
	}
	
	/**
	 * Indikuje zobrazení pøerušovacího dialogu.
	 * @return true, je-li zobrazen pøerušovací dialog.
	 */
	public boolean getInterruptDialogShown() {
		synchronized(interruptDialogShown) {
			return interruptDialogShown;
		}
	}
	
	/**
	 * Vrací vlákno se sledovanou operací.
	 * @return Vlákno se sledovanou operací.
	 */
	public Thread getThread() {
		return t;
	}
	
	/**
	 * Zobrazí okno.
	 */
	public void show() {
		frame.setVisible(true);
	}
	
	/**
	 * Skryje okno.
	 */
	public void hide() {
		frame.setVisible(false);
	}

	/**
	 * Button ActionListener: Pøerušit.
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		showInterruptionDialog();
	}
	
	/**
	 * Zobrazí dialog pro pøerušení operace.
	 */
	private void showInterruptionDialog() {
		synchronized (interruptDialogShown) {
			interruptDialogShown = true;
		}
		String[] options = { "Potvrdit", "Zrušit" };
		
		int n = JOptionPane.showOptionDialog(frame, 
							"Chcete skuteènì pøerušit akci? (všechny vytvoøené soubory budou smazány)",
							"Zrušit akci",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[1]);

		if (n==0) {
			t.safelyInterrupt();
			
			while (t.isAlive()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) { }
			}
			
			synchronized (createdFiles) {
				Vector<File> dirs = new Vector<File>();
				
				while (t.isAlive()) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {}
				}
				
				for (File f: createdFiles) {	
					if (f.exists()) {
						if (f.isDirectory()) {
							dirs.add(f);
						} else {
							f.delete();
						}
					}
				}
				
				for (File dir: dirs) {
					dir.delete();
				}
			}
		}
		
		synchronized (interruptDialogShown) {
			interruptDialogShown = false;
		}
	}
}

/**
 * MenuItem ActionListener: Vytvoøit archiv
 */
class MakeArchiveMenuItemActionListener implements ActionListener {
	/**
	 * Zobrazí dialog pro vytvoøení archivu.
	 */
	public void actionPerformed(ActionEvent e) {
		Main.MakeArchive();
	}
}

/**
 * MenuItem ActionListener: Rozbalit archiv
 */
class UnpackMenuItemActionListener implements ActionListener {
	/**
	 * Zobrazí dialog pro rozbalení archivu.
	 */
	public void actionPerformed(ActionEvent e) {
		Main.UnpackArchive();
	}
}

/**
 * Vlákno pro aktualizaci vizualizace prùbìhu operace.
 */
class ProgressThread extends Thread {
	/** Vlákno s vizualizovanou operací. */
	private SafelyInterruptableThreadWithSubprogress th;
	/** Titulek okna s prùbìhem. */
	private String guiTitle;
	/** Indikátor vytvoøených souborù pro možné pøerušení operace. */
	private Vector<File> createdFiles;
	
	/**
	 * Konstruktor.
	 * @param t Vlákno s operací.
	 * @param createdFiles Indikátor vytvoøených souborù.
	 */
	public ProgressThread(SafelyInterruptableThreadWithSubprogress t, Vector<File> createdFiles) {
		this(t, createdFiles, null);
	}
	
	/**
	 * Konstruktor.
	 * @param t Vlákno s operací.
	 * @param createdFiles Indikátor vytvoøených souborù.
	 * @param guiTitle Titulek okna s prùbìhem.
	 */
	public ProgressThread(SafelyInterruptableThreadWithSubprogress t, Vector<File> createdFiles, String guiTitle) {
		this.th = t;
		this.createdFiles = createdFiles;
		if (guiTitle==null) {
			this.guiTitle = "Prùbìh operace";
		} else {
			this.guiTitle = guiTitle;
		}
	}
	
	@Override
	public void run() {
		final int SLEEP_MILLIS = 100;
		Subprogress p = th.getSubprogress();
		
		th.start();
		
		ProgressGUI gui = new ProgressGUI(th, createdFiles, guiTitle);
		JProgressBar pb = gui.overallProgressBar;

		pb.setString("Pøipravuji...");
		pb.setStringPainted(true);
		pb.setMinimum(0);
		pb.setMaximum(1);
		pb.setValue(0);
		Main.frame.setEnabled(false);
		gui.show();
		
		while (!p.isInitialized()&th.isAlive()) {
			try {
				Thread.sleep(SLEEP_MILLIS);
			}  catch (InterruptedException e) {}
		}
		
		do {
			try {
				Thread.sleep(SLEEP_MILLIS);
				int overflowRatio = 1 + (int)(p.getTodo() / Integer.MAX_VALUE);
				pb.setMaximum((int)(p.getTodo() / overflowRatio)); /* Délka komprese se zaktualizuje až po zabalení souborù, proto je tøeba hlídat maximum prùbìhu */
				pb.setValue(Math.min(pb.getMaximum(), (int)(p.getDone()/overflowRatio)));
				pb.setString(String.format("%s (%.0f%%)", p.getCurrentOperationName(), Math.min(100, 100*p.getDoneRatio())));
			} catch (InterruptedException e) {}
		} while(th.isAlive());
		
		while (gui.getInterruptDialogShown()) {
			try {
				Thread.sleep(SLEEP_MILLIS);
			} catch (InterruptedException e) {}
		}
		
		gui.hide();
		Main.refreshDir();
		Main.frame.setEnabled(true);
	}
}

/**
 * Vlákno pro vytvoøení archivu a jeho kompresi.
 */
class CompressThread extends SafelyInterruptableThreadWithSubprogress {
	/** Index metody. 0..shannon-fano, 1..huffman, 2..fgk*/
	private int methodIndex;
	/** Doèasný soubor pro vytvoøení archivu. */
	private File tmp;
	/** Soubor pro vytvoøení kompromovaného archivu. */
	private File f;
	/** Soubory oznaèené k archivaci. */
	private Vector<File> selectedFiles;
	/** Indikátor celkového prùbìhu operace. */
	private Subprogress progress;
	/** Indikátor prùbìhu komprese. */
	private BasicSubprogressOperation compressionOperation;
	/** Indikátor vytvoøených souborù pro pøerušení operace. */
	private Vector<File> createdFiles;
	/** Fáze operace. */
	private Integer phase;
	/** Indikátor pøerušení operace. */
	private boolean interrupt;
	
	/**
	 * Konstruktor.
	 * @param selectedFiles Soubory oznaèené k archivaci.
	 * @param methodIndex Index metody.
	 *   0..shannon-fano,
	 *   1..huffman,
	 *   2..fgk.
	 * @param tmp Doèasný soubor pro vytvoøení archivu.
	 * @param out Soubor pro vytvoøení komprimovaného archivu.
	 * @param createdFiles Indikátor vytvoøených soubprù pro pøerušení operace.
	 */
	public CompressThread(Vector<File> selectedFiles, int methodIndex, File tmp, File out, Vector<File> createdFiles) {
		synchronized (createdFiles) {
			if (createdFiles==null) {
				this.createdFiles = new Vector<File>();
			} else {
				this.createdFiles = createdFiles;
			}
		}
		this.selectedFiles = selectedFiles;
		this.methodIndex = methodIndex;
		this.tmp = tmp;
		this.f = out;
		this.progress = new Subprogress();
		this.phase = 0;
		this.interrupt = false;
		
		Vector<File> files = Utils.listAllFiles(selectedFiles);
		SubprogressOperation[] operations = new SubprogressOperation[files.size()+1];
		
		int i=0;
		long approxCompressionTodo = 0;
		for (File f: files) {
			operations[i] = new BasicSubprogressOperation(f.length(), "Balím... "+f.getName());
			approxCompressionTodo += f.length();
			i++;
		}
		switch (methodIndex) {
			case 0:
				approxCompressionTodo = (long)(ShannonFanoCompressor.PROGRESS_COMPRESS_RATIO*approxCompressionTodo);
				break;
			case 1:
				approxCompressionTodo = (long)(HuffmanCompressor.PROGRESS_COMPRESS_RATIO*approxCompressionTodo);
				break;
			case 2:
				approxCompressionTodo = (long)(FGKCompressor.PROGRESS_COMPRESS_RATIO*approxCompressionTodo);
				break;
		}
		compressionOperation = new BasicSubprogressOperation(approxCompressionTodo, "Komprimuji... "+out.getName());
		operations[i] = compressionOperation;
		progress = new Subprogress(operations);
	}
	
	@Override
	public void run() {
		try {
			if (!getInterrupt()) {
				synchronized (createdFiles) {
					createdFiles.add(tmp);
				}
				
				Main.archiver.pack(selectedFiles, tmp, progress);
			}
			
			synchronized (this.phase) {
				this.phase = 1;
			}
			
			synchronized (createdFiles) {
				createdFiles.add(f);
			}
			
			if (!getInterrupt()) {
				switch (methodIndex) {
					case 0:
						progress.setTodo(progress.getTodo()+(long)(ShannonFanoCompressor.PROGRESS_COMPRESS_RATIO*tmp.length())-compressionOperation.getTodo());
						compressionOperation.setTodo((long)(ShannonFanoCompressor.PROGRESS_COMPRESS_RATIO*tmp.length()));
						Main.sfCompressor.Compress(tmp, f, progress);
						break;
					case 1:
						progress.setTodo(progress.getTodo()+(long)(HuffmanCompressor.PROGRESS_COMPRESS_RATIO*tmp.length())-compressionOperation.getTodo());
						compressionOperation.setTodo((long)(HuffmanCompressor.PROGRESS_COMPRESS_RATIO*tmp.length()));
						Main.huffCompressor.Compress(tmp, f, progress);
						break;
					default:
						progress.setTodo(progress.getTodo()+(long)(FGKCompressor.PROGRESS_COMPRESS_RATIO*tmp.length())-compressionOperation.getTodo());
						compressionOperation.setTodo((long)(FGKCompressor.PROGRESS_COMPRESS_RATIO*tmp.length()));
						Main.fgkCompressor.Compress(tmp, f, progress);
						break;
			}
			}
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Chyba I/O: "+ex.getMessage());
			f.delete();
		} finally {
			tmp.delete();		
		}
	}
	
	/**
	 * Nastaví indikátor pøerušení.
	 * @param interrupt Nová hodnota indikátoru pøerušení
	 */
	private synchronized void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}
	
	/**
	 * Vrací indikátor pøerušení.
	 * @return Indikátor pøerušení.
	 */
	private synchronized boolean getInterrupt() {
		return interrupt;
	}
	
	@Override
	public Subprogress getSubprogress() {
		return progress;
	}

	@Override
	public void safelyInterrupt() {
		setInterrupt(true);
		synchronized (this.phase) {
			if (this.phase==0) {
				Main.archiver.safelyInterrupt();
			} else if (this.phase==1) {
				switch (methodIndex) {
					case 0:
						Main.sfCompressor.safelyInterrupt();
						break;
					case 1:
						Main.huffCompressor.safelyInterrupt();
						break;
					case 2:
						Main.fgkCompressor.safelyInterrupt();
						break;
				}
			}
		}
	}
}

/**
 * Vlákno pro dekompresi archivu a jeho rozbalení.
 */
class DecompressThread extends SafelyInterruptableThreadWithSubprogress {
	/** Indikátor prùbìhu celé operace. */
	private Subprogress progress;
	/** Indikátor prùbìhu dekomprese. */
	BasicSubprogressOperation decompressOperation;
	/** Indikátor prùbìhu rozbalování. */
	BasicSubprogressOperation unpackOperation;
	/** Soubor komprimovaného archivu. */
	private File f;
	/** Doèasný soubor pro dekompresi archivu. */ 
	private File tmp;
	/** Cílová složka pro rozbalení archivu. */
	private File out;
	/** Indikátor vytvoøených souborù pro pøerušení operace. */
	private Vector<File> createdFiles;
	/** Fáze operace. */
	private Integer phase;
	/** Indikátor pøerušení operace. */
	private boolean interrupt = false;
	
	/**
	 * Konstruktor. 
	 * @param f Soubor komprimovaného archivu.
	 * @param tmp Doèasný soubor pro dekompresi archivu.
	 * @param out Cílová složka pro rozbalení archivu.
	 * @param createdFiles Indikátor vytvoøených souborù pro pøerušení operace.
	 */
	public DecompressThread(File f, File tmp, File out, Vector<File> createdFiles) {
		synchronized (createdFiles) {
			if (createdFiles==null) {
				this.createdFiles = new Vector<File>();
			} else {
				this.createdFiles = createdFiles;
			}
		}
		
		progress = new Subprogress();
		this.f = f;
		this.tmp = tmp;
		this.out = out;
		this.phase = 0;
	}
	
	@Override
	public void run() {
		String filename = f.getName();
		boolean stop = false;
		
		synchronized (createdFiles) {
			createdFiles.add(tmp);
		}
		
		try {
			if (!getInterrupt()) {
				if (filename.endsWith(".ar.sf")) {
					SubprogressOperation[] operations = new SubprogressOperation[2];
					decompressOperation = new BasicSubprogressOperation((long)(ShannonFanoCompressor.PROGRESS_DECOMPRESS_RATIO*f.length()), "Dekomprimuji... "+f.getName());
					operations[0] = decompressOperation;
					unpackOperation = new BasicSubprogressOperation(f.length(), "Rozbaluji... "+tmp.getName());
					operations[1] = unpackOperation;
					progress.initialize(operations, false);
					Main.sfCompressor.Decompress(f, tmp, progress);
				} else if (filename.endsWith(".ar.huff")) {
					SubprogressOperation[] operations = new SubprogressOperation[2];
					decompressOperation = new BasicSubprogressOperation((long)(HuffmanCompressor.PROGRESS_DECOMPRESS_RATIO*f.length()), "Dekomprimuji... "+f.getName());
					operations[0] = decompressOperation;
					unpackOperation = new BasicSubprogressOperation(f.length(), "Rozbaluji... "+tmp.getName());
					operations[1] = unpackOperation;
					progress.initialize(operations, false);
					Main.huffCompressor.Decompress(f, tmp, progress);
				} else if (filename.endsWith(".ar.fgk")) {
					SubprogressOperation[] operations = new SubprogressOperation[2];
					decompressOperation = new BasicSubprogressOperation((long)(FGKCompressor.PROGRESS_DECOMPRESS_RATIO*f.length()), "Dekomprimuji... "+f.getName());
					operations[0] = decompressOperation;
					unpackOperation = new BasicSubprogressOperation(f.length(), "Rozbaluji... "+tmp.getName());
					operations[1] = unpackOperation;
					progress.initialize(operations, false);
					Main.fgkCompressor.Decompress(f, tmp, progress);
				} else {
					JOptionPane.showMessageDialog(null, "Neznámá pøípona souboru");
					stop = true;
				}
			}
			
			synchronized (this.phase) {
				this.phase = 1;
			}
			
			
			if (!stop&&!getInterrupt()) {
				unpackOperation.setTodo(tmp.length());
				progress.setTodo(decompressOperation.getTodo()+unpackOperation.getTodo());
				progress.moveToNextOperation();
				
				Main.archiver.unpack(tmp, out, progress, createdFiles);
			}
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Chyba I/O: "+e1.getMessage());
		} catch (InvalidArchiveException e1) {
			JOptionPane.showMessageDialog(null, "Chybný formát archivu.");
		} finally {
			if (tmp.exists()) {
				tmp.delete();
			}
		}
	}
	
	/**
	 * Nastaví novou hodnotu indikátoru pøerušení
	 * @param interrupt Nová hodnota indikátoru pøerušení.
	 */
	private synchronized void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}
	
	/**
	 * Vrací indikátor pøerušení.
	 * @return Indikátor pøerušení.
	 */
	private synchronized boolean getInterrupt() {
		return interrupt;
	}

	@Override
	public Subprogress getSubprogress() {
		return progress;
	}

	@Override
	public void safelyInterrupt() {
		setInterrupt(true);
		synchronized (this.phase) {
			String filename = f.getName();
			
			if (this.phase==0) {
				if (filename.endsWith(".ar.sf")) {
					Main.sfCompressor.safelyInterrupt();
				} else if (filename.endsWith(".ar.huff")) {
					Main.huffCompressor.safelyInterrupt();
				} else if (filename.endsWith(".ar.fgk")) {
					Main.fgkCompressor.safelyInterrupt();
				}
			} else if (this.phase==1) {
				Main.archiver.safelyInterrupt();
			}
		}
		
		
	}
}

/**
 * Hlavní tøída grafického uživatelského rozhraní.
 */
public class Main {
	/** Verze programu. */
	static final String version = "1.0";
	/** Index koøenového adresáøe (v combo boxu). */
	static int origRootIndex = 0;
	/** Aktuální adresáø. */
	static File currDir;
	/** Hlavní okno. */
	static JFrame frame;
	/** Titulek s cestou do aktuálního adresáøe. */
	static JTextField pathTextField;
	/** Combo box s koøenovými adresáøi. */
	static JComboBox rootsComboBox;
	/** Combo box s metodami komprese. */
	static JComboBox methodsComboBox;
	/** Datový model pro výpis adresáøe. */
	static DefaultTableModel fileTableModel;
	/** Tabulka pro výpis adresáøe. */
	static JTable fileTable;
	/** Archiver. */
	static Archiver archiver = new Archiver();
	/** Shannon-fano kompresor. */
	static ShannonFanoCompressor sfCompressor = new ShannonFanoCompressor();
	/** Huffman kompresor. */
	static HuffmanCompressor huffCompressor = new HuffmanCompressor();
	/** FGK kompresor. */
	static FGKCompressor fgkCompressor = new FGKCompressor();
	
	/**
	 * Vrací hlavní okno.
	 * @return Hlavní okno.
	 */
	public static JFrame getFrame() {
		return frame;
	}
	
	/**
	 * Vrací pøíponu výsledného archivu podle zvolené metody.
	 * @return Pøípona souboru archivu.
	 */
	static String getExtension() {
		switch (methodsComboBox.getSelectedIndex()) {
		case 0:
			return ".ar.sf";
		case 1:
			return ".ar.huff";
		default:	
			return ".ar.fgk";
		}
	}
	
	/**
	 * Vrací pole oznaèených souborù.
	 * @return Pole oznaèených souborù.
	 */
	static File[] getSelectedFiles() {
		synchronized (fileTable) {
			int[] selection = fileTable.getSelectedRows();
			File[] files = new File[selection.length];
			
			for (int i=0; i<files.length; i++) {
				files[i] = new File(currDir.getAbsolutePath()+File.separatorChar+fileTableModel.getValueAt(selection[i], 1));
			}
		
			return files;
		}
	}
	
	/**
	 * Vrací formátovanou velikost souboru.
	 * @param f Soubor.
	 * @return Formátovaná velikost souboru.
	 */
	static String formattedFileSize(File f) {
		if (f.isDirectory()) {
			return "";
		}
		
		final String[] units = new String[4];
		
		units[0] = "kB";
		units[1] = "MB";
		units[2] = "GB";
		units[3] = "TB";
		
		Long size = f.length();
		
		if (size<1024) {
			return size.toString()+" B"; 
		} else {
			Double divided_size = (double)size;
			
			int i=0;
			while (divided_size>=1024&&i<units.length) {
				divided_size = divided_size / 1024.0;
				i++;
			}
			
			return String.format("%2.2f %s", divided_size, units[i-1]); 
			
		}
	}
	
	/**
	 * Aktualizuje výpis adresáøe.
	 */
	static void refreshDir() {
		switchDir(currDir);
	}
	
	/**
	 * Pøepne výpis do nového adresáøe.
	 * @param dir Nový adresáø pro výpis.
	 */
	static void switchDir(File dir) {
		synchronized (fileTable) {
			if (dir==null||!dir.canRead()) {
				return;
			}
			
			boolean sameDir = currDir.getAbsolutePath().equals(dir.getAbsolutePath());
			int[] origSelectionIndices = fileTable.getSelectedRows();
			Set<String> origSelection = null;
			int origAnchorIndex = fileTable.getSelectionModel().getAnchorSelectionIndex();
			String origAnchor = null;
			
			if (sameDir) {
				origSelection = new HashSet<String>();
				
				for (int i=0; i< origSelectionIndices.length; i++) {
					origSelection.add((String) fileTableModel.getValueAt(origSelectionIndices[i], 1));
				}
				origAnchor = origAnchorIndex>=0? (String) fileTableModel.getValueAt(fileTable.getSelectionModel().getAnchorSelectionIndex(), 1): null;
			}
			
			currDir = dir;
			File[] children = dir.listFiles();
			
			DefaultTableModel model = fileTableModel;
			
			while (model.getRowCount()>0) {
				model.removeRow(0);
			}
			
			for (int i=0; i<children.length; i++) {
				File f = children[i];
				String[] rowData = new String[5];
				rowData[0] = f.isDirectory()? "Dir" : "File";
				rowData[1] = f.getName();
				rowData[2] = formattedFileSize(f);
				Date lastModified = new Date(f.lastModified());
				rowData[3] = lastModified.toString();
				model.addRow(rowData);
			}
			
			pathTextField.setText(currDir.getAbsolutePath());
			
			if (sameDir) {
				for (int i=0; i<fileTableModel.getRowCount(); i++) {
					String rowFileName = (String) fileTableModel.getValueAt(i, 1);
					
					if (origSelection.contains(rowFileName)) {
						fileTable.getSelectionModel().addSelectionInterval(i, i);
					}
					
					if (rowFileName.equals(origAnchor)) {
						fileTable.getSelectionModel().setAnchorSelectionIndex(i);
					}
				
				}
			}
		}
	}
	
	/** 
	 * Vytvoøí a zobrazí GUI.
	 */
    static void createAndShowGUI() {
        frame = new JFrame("Archiver");
        JPanel panel = new JPanel(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        /* MENU */
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        
        menu = new JMenu("Soubor"); 
        
        menuItem = new JMenuItem("Vytvoøit archiv");
        menuItem.addActionListener(new MakeArchiveMenuItemActionListener());
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Rozbalit");
        menuItem.addActionListener(new UnpackMenuItemActionListener());
        menu.add(menuItem);
        
        menuBar.add(menu);
        
        menu = new JMenu("Úpravy");
        
        menuItem = new JMenuItem("Vybrat");
        menuItem.addActionListener(new SelectMenuItemActionListener());
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Vybrat vše");
        menuItem.addActionListener(new SelectAllMenuItemActionListener());
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Zrušit výbìr");
        menuItem.addActionListener(new UnselectMenuItemActionListener());
        menu.add(menuItem);
        
        menuBar.add(menu);
        
        frame.setJMenuBar(menuBar);
        
        /* FILEPATH LINE */
        JPanel filepathPanel = new JPanel(new BorderLayout());
        JPanel innerFilepathPanel = new JPanel(new BorderLayout());
        
        filepathPanel.add(innerFilepathPanel, BorderLayout.CENTER);

        JButton dirUpButton;
        URL imageURL = Main.class.getResource("/images/up.png");
        
        if (imageURL!=null) {
            dirUpButton = new JButton(new ImageIcon(imageURL));
        } else {
        	dirUpButton = new JButton("Nahoru");
        }
        dirUpButton.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
        		if (e.getComponent().isEnabled()&&e.getButton()==MouseEvent.BUTTON1) {
        			File newDir = currDir.getParentFile();
        			switchDir(newDir);
        		}
        	}
		});
        dirUpButton.setMargin(new Insets(0,0,0,0));
        filepathPanel.add(dirUpButton, BorderLayout.LINE_START);       
        
        rootsComboBox = new JComboBox(File.listRoots());
        rootsComboBox.setEnabled(false);
        rootsComboBox.addActionListener(new RootsComboBoxActionListener());
        File dir = new File(System.getProperty("user.dir"));
        
        while (dir.getParentFile()!=null) {
        	dir = dir.getParentFile();
        }
        
        for (int i=0; i<rootsComboBox.getItemCount(); i++) {
        	File f = (File) rootsComboBox.getItemAt(i);
        	
        	if (f.equals(dir)) {
        		rootsComboBox.setSelectedIndex(i);
        		origRootIndex = i;
        		break;
        	}
        }
        
        rootsComboBox.setEnabled(true);
        innerFilepathPanel.add(rootsComboBox, BorderLayout.LINE_START);
        
        pathTextField = new JTextField(30);
        pathTextField.setMinimumSize(new Dimension(100, 10));
        pathTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        pathTextField.setEditable(false);
        innerFilepathPanel.add(pathTextField, BorderLayout.CENTER);
        
        methodsComboBox = new JComboBox(new String[]{"sf", "huff", "fgk"});
        methodsComboBox.setEnabled(false);
        methodsComboBox.setSelectedIndex(1);
        methodsComboBox.setRenderer(new BasicComboBoxRenderer() {
			private static final long serialVersionUID = 8404282976436170964L;
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        		String[] tooltips = { "Shannon-Fanùv kód", "Huffmanùv kód", "Adaptivní huffmanùv kód (pomalé)" };
        		if (isSelected) {
        			setBackground(list.getSelectionBackground());
        	        setForeground(list.getSelectionForeground());
        	        if (-1 < index) {
        	            list.setToolTipText(tooltips[index]);
        	            
        	        }
        		} else {
        			setBackground(list.getBackground());
        	        setForeground(list.getForeground());
        		}
        		
        		setFont(list.getFont());
        		setText((value == null) ? "" : value.toString());
        		
        		return this;
        		}
        	});
        methodsComboBox.setEnabled(true);
        filepathPanel.add(methodsComboBox, BorderLayout.LINE_END);
        
        panel.add(filepathPanel, BorderLayout.PAGE_START);
        
        fileTableModel = new DefaultTableModel();
        fileTableModel.addColumn("Typ");
        fileTableModel.addColumn("Název");
        fileTableModel.addColumn("Velikost");
        fileTableModel.addColumn("Zmìnìn");
        fileTable = new JTable(fileTableModel) {
			private static final long serialVersionUID = 5244578766172457057L;
			private TableCellRenderer renderer = new DefaultTableCellRenderer() {
				private static final long serialVersionUID = -6814012472979749499L;
				private final Color LIGHT_YELLOW = new Color(1.0f,1f,0.8f);
				private final Color DARK_BLUE = new Color(0.4f, 0.1f, 0.9f);
				private final Color MEDIUM_BLUE = new Color(0.7f, 0.7f, 1.0f);
				private final Color LIGHT_BLUE = new Color(0.8f, 0.8f, 1.0f);
				private final Font PLAIN_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
				private Font BOLD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);

				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					synchronized (Main.fileTable) {
						super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
						
						if (column==2) { 
							/* velikosti souborù zarovnat doprava */
							setHorizontalAlignment(SwingConstants.RIGHT);
						} else {
							setHorizontalAlignment(SwingConstants.LEFT);
						}
						
						if (table.getModel().getValueAt(row, 0).equals("Dir")&&column==0) { 
							/* adresáøe žlutì */
							if (table.getSelectionModel().isSelectedIndex(row)) {
								setForeground(Color.BLACK);
								setBackground(MEDIUM_BLUE);
							} else {
								setForeground(Color.BLACK);
								setBackground(LIGHT_YELLOW);
							}
							setFont(PLAIN_FONT);
						} else {
							/* archivy tuènì modøe */
							if (table.getSelectionModel().isSelectedIndex(row)) {
	
								setBackground(LIGHT_BLUE);
							} else {
								setBackground(Color.WHITE);
							}
							
							String filename = (String)table.getModel().getValueAt(row, 1);
							
							if (column==1&&isArchive(filename)) {
								setFont(BOLD_FONT);
								setForeground(DARK_BLUE);
							} else {
								setFont(PLAIN_FONT);							
								setForeground(Color.BLACK);
							}
						}
							
						return this;
					}
				}
			};
			
			@Override
			public boolean isCellEditable(int row, int column) {
        	     return false;
        	}
			
			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				return renderer;
			}
        };
        
        fileTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	synchronized (fileTable) {
	                if (e.getComponent().isEnabled() && e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()>=2) {
	                    Point p = e.getPoint();
	                    int row = fileTable.rowAtPoint(p);
	                	String type = (String) fileTableModel.getValueAt(row, 0);
	                    String filename = (String) fileTableModel.getValueAt(row, 1);
	                    
	                    if (type.equals("Dir")) {
	                    	File newDir = new File(currDir.getPath()+File.separatorChar+filename);
	                    	switchDir(newDir);
	                    } else if (isArchive(filename)) {
	                    	UnpackArchive();
	                    }
	                }
            	}
            }
        });
        
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(500);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        fileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileTable.setDoubleBuffered(true);
        JScrollPane scrollPane = new JScrollPane(fileTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        frame.add(panel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
        frame.setVisible(true);
    }
    
    /**
     * Na základì jména souboru urèí, zda se jedná o cestu k archivu. Nekontroluje, zda soubor existuje, jen porovná pøíponu jména.
     * @param filename Jméno souboru.
     * @return true, pokud se jedná o jméno archivu.
     */
    public static boolean isArchive(String filename) {
    	return filename.endsWith(".ar.sf")||filename.endsWith(".ar.huff")||filename.endsWith(".ar.fgk");
    }

    /**
     * Zobrazí dialog pro vytvoøení archivu.
     */
    public static void MakeArchive() {
    	JFileChooser fc = new JFileChooser(Main.currDir);
    	File[] selectedFilesArray = Main.getSelectedFiles();
    	File firstSelected = selectedFilesArray[0];
    	String defaultArchiveName = null;
    	
    	if (selectedFilesArray.length==1) {
    		defaultArchiveName = Utils.getFilenameWithoutExtension(firstSelected);
    	} else if (selectedFilesArray.length>1&&firstSelected.getParentFile().getParentFile()!=null) {
    		defaultArchiveName = Utils.getFilenameWithoutExtension(firstSelected.getParentFile());
    	} else {
    		defaultArchiveName = "Archiv";
    	}
		
		/* Zobraz file dialog */
		fc.setDialogTitle("Vytvoøit archiv");
		fc.setFileFilter(new FileFilter() {
			public String getDescription() {
				switch (Main.methodsComboBox.getSelectedIndex()) {
				case 0:
					return "Archiv zkomprimovaný shannon-fanovým kódem";
				case 1:
					return "Archiv zkomprimovaný huffmanovým kódem";
				default:
					return "Archiv zkomprimovaný adaptivním huffmanovým kódem";
				}
			}
			
			public boolean accept(File f) {
				String ext = Main.getExtension();
				
				return f.getName().endsWith(ext);
			}
		});
		
		if (defaultArchiveName!=null) {
			switch (Main.methodsComboBox.getSelectedIndex()) {
			case 0:
				fc.setSelectedFile(new File(defaultArchiveName+".ar.sf"));
				break;
			case 1:
				fc.setSelectedFile(new File(defaultArchiveName+".ar.huff"));
				break;
			default:
				fc.setSelectedFile(new File(defaultArchiveName+".ar.fgk"));
				break;
			}
		}
		
		/* Zabal do vybraného souboru */
		if (fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
			String ext = Main.getExtension();
			File f = fc.getSelectedFile();
			int methodIndex = Main.methodsComboBox.getSelectedIndex();

			if (!f.getName().endsWith(ext)) {
				f = new File(f.getPath()+ext);
			}
			
			if (f.exists()) {
				String[] options = { "Ano", "Ne" };
				
				int n = JOptionPane.showOptionDialog(frame, 
						"Soubor '"+f.getName()+"' existuje. Chcete jej pøepsat?",
						"Soubor existuje",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[1]);
				
				if (n==0) {
					f.delete();
				} else {
					return;
				}
			}
			
			File tmp = new File(f.getParent()+File.separatorChar+"."+f.getName()+".tmp~");
			
			if (tmp.exists()) {
				Random rnd = new Random();
				String numstr = "";
				
				while (tmp.exists()) {
					Integer n = rnd.nextInt(10);
					numstr = n.toString() + numstr;
					tmp = new File(f.getParent()+File.separatorChar+"."+f.getName()+"."+numstr+".tmp~");
				}
			}
			
			Vector<File> selectedFiles = new Vector<File>();
			
			for (File selectedFile: selectedFilesArray) {
				selectedFiles.add(selectedFile);
			}

			Vector<File> createdFiles = new Vector<File>();
			CompressThread ct = new CompressThread(selectedFiles, methodIndex,tmp,f, createdFiles);
			ProgressThread pt = new ProgressThread(ct, createdFiles, "Vytváøení archivu");
			
			pt.start();
		}
    }
    
    /** 
     * Zobrazí dialog pro rozbalení archivu.
     */
    public static void UnpackArchive() {
		JFileChooser fc = new JFileChooser(Main.currDir);
		
		fc.setDialogTitle("Rozbalit archiv do");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		if (fc.showDialog(null, "Rozbalit")==JFileChooser.APPROVE_OPTION) {
			File dest = fc.getSelectedFile();
			int selection = Main.fileTable.getSelectionModel().getAnchorSelectionIndex();
			String srcName = (String) Main.fileTableModel.getValueAt(selection, 1);
			File src = new File(Main.currDir.getAbsolutePath()+File.separatorChar+srcName);
			File tmp = new File(src.getParent()+File.separatorChar+"."+src.getName()+".tmp~");
			
			if (tmp.exists()) {
				Random rnd = new Random();
				String numstr = "";
				
				while (tmp.exists()) {
					Integer n = rnd.nextInt(10);
					numstr = n.toString() + numstr;
					tmp = new File(src.getParent()+File.separatorChar+"."+src.getName()+"."+numstr+".tmp~");
				}
			}
			
			Vector<File> createdFiles = new Vector<File>();
			DecompressThread ct = new DecompressThread(src, tmp, dest, createdFiles);
			ProgressThread pt = new ProgressThread(ct, createdFiles, "Rozbalování archivu");
			
			pt.start();
		}
    }

    /**
     * Vstupní bod programu.
     * @param args Parametry.
     */
	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
                currDir = new File(System.getProperty("user.dir"));
                Thread switchDirThread = new Thread() {
                	private final int REFRESH_PERIOD_MILLIS = 15000;
                	public void run() {
                		while (true) {
	                		refreshDir();
	                		try {
								Thread.sleep(REFRESH_PERIOD_MILLIS);
							} catch (InterruptedException e) {}
                		}
                	}
                };
                
                switchDirThread.start();
            }
        });
	}
}
