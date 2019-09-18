package utils;

/* XXX:
 * P�eru�en� akc� je prov�d�no p�es prom�nnou interrupt, 
 * kter� je na za��tku a konci akce nastavena na false
 * a funkce interrupt j� nastav� na true, co� p�eru�� v�echnu 
 * pr�ci. Kdyby se vol�n� interrupt trefilo p�ed prvn� p�i�azen�
 * do prom�nn� interrupt, tak se interrupt() neprovede.
 * To je ale dost nepravd�podobn�.
 * �lo by to synchronizovat, ale to by bylo nep�im��en� n�ro�n�
 * vzhledem k tomu, �e se prom�nn� sychronized kontroluje ka�dou chv�li.
 */

/**
 * Rozhran� t��d s bezpe�n� p�eru�itelnou operac�.
 */
public interface SafelyInterruptable {
	/**
	 * Bezpe�n� p�eru�� prob�haj�c� operaci.
	 */
	public void safelyInterrupt();
}
