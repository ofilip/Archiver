package utils;

/**
 * Tøída definující bezpeènì pøerušitelné vlákno s indikátorem prùbìhu.
 */
public abstract class SafelyInterruptableThreadWithSubprogress 
	extends ThreadWithSubprogress 
	implements SafelyInterruptable {}
