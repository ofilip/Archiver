package utils;

/**
 * T��da definuj�c� bezpe�n� p�eru�iteln� vl�kno s indik�torem pr�b�hu.
 */
public abstract class SafelyInterruptableThreadWithSubprogress 
	extends ThreadWithSubprogress 
	implements SafelyInterruptable {}
