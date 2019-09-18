package utils;

/* XXX:
 * Pøerušení akcí je provádìno pøes promìnnou interrupt, 
 * která je na zaèátku a konci akce nastavena na false
 * a funkce interrupt jí nastaví na true, což pøeruší všechnu 
 * práci. Kdyby se volání interrupt trefilo pøed první pøiøazení
 * do promìnné interrupt, tak se interrupt() neprovede.
 * To je ale dost nepravdìpodobné.
 * Šlo by to synchronizovat, ale to by bylo nepøimìøenì nároèné
 * vzhledem k tomu, že se promìnná sychronized kontroluje každou chvíli.
 */

/**
 * Rozhraní tøíd s bezpeènì pøerušitelnou operací.
 */
public interface SafelyInterruptable {
	/**
	 * Bezpeènì pøeruší probíhající operaci.
	 */
	public void safelyInterrupt();
}
