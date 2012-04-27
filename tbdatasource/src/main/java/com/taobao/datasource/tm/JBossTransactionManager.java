package com.taobao.datasource.tm;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.taobao.datasource.tm.integrity.TransactionIntegrity;

public class JBossTransactionManager implements TransactionManager {

	public void setDefaultTransactionTimeout(int seconds) {
		getTxManager().setDefaultTransactionTimeout(seconds);
	}

	public void setGlobalIdsEnabled(boolean newValue) {
		getTxManager().setGlobalIdsEnabled(newValue);
	}

	public void setInterruptThreads(boolean interruptThreads) {
		getTxManager().setInterruptThreads(interruptThreads);
	}

	public void setTransactionIntegrity(TransactionIntegrity integrity) {
		getTxManager().setTransactionIntegrity(integrity);
	}

	public void setTransactionTimeout(int seconds) throws SystemException {
		getTxManager().setTransactionTimeout(seconds);
	}

	private TxManager getTxManager() {
		return TxManager.getInstance();
	}

	public void begin() throws NotSupportedException, SystemException {
		getTxManager().begin();

	}

	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
			SecurityException, IllegalStateException, SystemException {
		getTxManager().commit();

	}

	public int getStatus() throws SystemException {
		return getTxManager().getStatus();
	}

	public Transaction getTransaction() throws SystemException {
		return getTxManager().getTransaction();
	}

	public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException,
			SystemException {
		getTxManager().resume(transaction);

	}

	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		getTxManager().rollback();

	}

	public void setRollbackOnly() throws IllegalStateException, SystemException {
		getTxManager().setRollbackOnly();

	}

	public Transaction suspend() throws SystemException {
		return getTxManager().suspend();
	}
}
