/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.jdbc;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

public class DatabaseMetaDataWrapper implements DatabaseMetaData
{
	private final DatabaseMetaData targetMetaData;
	private final Connection targetConnection;

	public DatabaseMetaDataWrapper(DatabaseMetaData targetMetaData,
			Connection targetConnection)
	{
		super();
		this.targetMetaData = targetMetaData;
		this.targetConnection = targetConnection;
	}

	public boolean allProceduresAreCallable() throws SQLException
	{
		return this.targetMetaData.allProceduresAreCallable();
	}

	public boolean allTablesAreSelectable() throws SQLException
	{
		return this.targetMetaData.allTablesAreSelectable();
	}

	public boolean dataDefinitionCausesTransactionCommit() throws SQLException
	{
		return this.targetMetaData.dataDefinitionCausesTransactionCommit();
	}

	public boolean dataDefinitionIgnoredInTransactions() throws SQLException
	{
		return this.targetMetaData.dataDefinitionIgnoredInTransactions();
	}

	public boolean deletesAreDetected(int type) throws SQLException
	{
		return this.targetMetaData.deletesAreDetected(type);
	}

	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
	{
		return this.targetMetaData.doesMaxRowSizeIncludeBlobs();
	}

	public ResultSet getAttributes(String catalog, String schemaPattern,
			String typeNamePattern, String attributeNamePattern)
			throws SQLException
	{
		// 这里connection 是null吧，调用不到的感觉
		return new TResultSetWrapper(null, this.targetMetaData.getAttributes(
				catalog, schemaPattern, typeNamePattern, attributeNamePattern));
	}

	public ResultSet getBestRowIdentifier(String catalog, String schema,
			String table, int scope, boolean nullable) throws SQLException
	{
		return this.targetMetaData.getBestRowIdentifier(catalog, schema, table,
				scope, nullable);
	}

	public String getCatalogSeparator() throws SQLException
	{
		return this.targetMetaData.getCatalogSeparator();
	}

	public String getCatalogTerm() throws SQLException
	{
		return this.targetMetaData.getCatalogTerm();
	}

	public ResultSet getCatalogs() throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getCatalogs());
	}

	public ResultSet getColumnPrivileges(String catalog, String schema,
			String table, String columnNamePattern) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getColumnPrivileges(
				catalog, schema, table, columnNamePattern));
	}

	public ResultSet getColumns(String catalog, String schemaPattern,
			String tableNamePattern, String columnNamePattern)
			throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getColumns(catalog,
				schemaPattern, tableNamePattern, columnNamePattern));
	}

	public Connection getConnection() throws SQLException
	{
		return targetConnection;
	}

	public ResultSet getCrossReference(String primaryCatalog,
			String primarySchema, String primaryTable, String foreignCatalog,
			String foreignSchema, String foreignTable) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getCrossReference(
				primaryCatalog, primarySchema, primaryTable, foreignCatalog,
				foreignSchema, foreignTable));
	}

	public int getDatabaseMajorVersion() throws SQLException
	{
		return this.targetMetaData.getDatabaseMajorVersion();
	}

	public int getDatabaseMinorVersion() throws SQLException
	{
		return this.targetMetaData.getDatabaseMinorVersion();
	}

	public String getDatabaseProductName() throws SQLException
	{
		return this.targetMetaData.getDatabaseProductName();
	}

	public String getDatabaseProductVersion() throws SQLException
	{
		return this.targetMetaData.getDatabaseProductVersion();
	}

	public int getDefaultTransactionIsolation() throws SQLException
	{
		return this.targetMetaData.getDefaultTransactionIsolation();
	}

	public int getDriverMajorVersion()
	{
		return this.targetMetaData.getDriverMajorVersion();
	}

	public int getDriverMinorVersion()
	{
		return this.targetMetaData.getDriverMinorVersion();
	}

	public String getDriverName() throws SQLException
	{
		return this.targetMetaData.getDriverName();
	}

	public String getDriverVersion() throws SQLException
	{
		return this.targetMetaData.getDriverVersion();
	}

	public ResultSet getExportedKeys(String catalog, String schema, String table)
			throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getExportedKeys(
				catalog, schema, table));
	}

	public String getExtraNameCharacters() throws SQLException
	{
		return this.targetMetaData.getExtraNameCharacters();
	}

	public String getIdentifierQuoteString() throws SQLException
	{
		return this.targetMetaData.getIdentifierQuoteString();
	}

	public ResultSet getImportedKeys(String catalog, String schema, String table)
			throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getImportedKeys(
				catalog, schema, table));
	}

	public ResultSet getIndexInfo(String catalog, String schema, String table,
			boolean unique, boolean approximate) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getIndexInfo(catalog,
				schema, table, unique, approximate));
	}

	public int getJDBCMajorVersion() throws SQLException
	{
		return this.targetMetaData.getJDBCMajorVersion();
	}

	public int getJDBCMinorVersion() throws SQLException
	{
		return this.targetMetaData.getJDBCMinorVersion();
	}

	public int getMaxBinaryLiteralLength() throws SQLException
	{
		return this.targetMetaData.getMaxBinaryLiteralLength();
	}

	public int getMaxCatalogNameLength() throws SQLException
	{
		return this.targetMetaData.getMaxCatalogNameLength();
	}

	public int getMaxCharLiteralLength() throws SQLException
	{
		return this.targetMetaData.getMaxCharLiteralLength();
	}

	public int getMaxColumnNameLength() throws SQLException
	{
		return this.targetMetaData.getMaxColumnNameLength();
	}

	public int getMaxColumnsInGroupBy() throws SQLException
	{
		return this.targetMetaData.getMaxColumnsInGroupBy();
	}

	public int getMaxColumnsInIndex() throws SQLException
	{
		return this.targetMetaData.getMaxColumnsInIndex();
	}

	public int getMaxColumnsInOrderBy() throws SQLException
	{
		return this.targetMetaData.getMaxColumnsInOrderBy();
	}

	public int getMaxColumnsInSelect() throws SQLException
	{
		return this.targetMetaData.getMaxColumnsInSelect();
	}

	public int getMaxColumnsInTable() throws SQLException
	{
		return this.targetMetaData.getMaxColumnsInTable();
	}

	public int getMaxConnections() throws SQLException
	{
		return this.targetMetaData.getMaxConnections();
	}

	public int getMaxCursorNameLength() throws SQLException
	{
		return this.targetMetaData.getMaxCursorNameLength();
	}

	public int getMaxIndexLength() throws SQLException
	{
		return this.targetMetaData.getMaxIndexLength();
	}

	public int getMaxProcedureNameLength() throws SQLException
	{
		return this.targetMetaData.getMaxProcedureNameLength();
	}

	public int getMaxRowSize() throws SQLException
	{
		return this.targetMetaData.getMaxRowSize();
	}

	public int getMaxSchemaNameLength() throws SQLException
	{
		return this.targetMetaData.getMaxSchemaNameLength();
	}

	public int getMaxStatementLength() throws SQLException
	{
		return this.targetMetaData.getMaxStatementLength();
	}

	public int getMaxStatements() throws SQLException
	{
		return this.targetMetaData.getMaxStatements();
	}

	public int getMaxTableNameLength() throws SQLException
	{
		return this.targetMetaData.getMaxTableNameLength();
	}

	public int getMaxTablesInSelect() throws SQLException
	{
		return this.targetMetaData.getMaxTablesInSelect();
	}

	public int getMaxUserNameLength() throws SQLException
	{
		return this.targetMetaData.getMaxUserNameLength();
	}

	public String getNumericFunctions() throws SQLException
	{
		return this.targetMetaData.getNumericFunctions();
	}

	public ResultSet getPrimaryKeys(String catalog, String schema, String table)
			throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getPrimaryKeys(
				catalog, schema, table));
	}

	public ResultSet getProcedureColumns(String catalog, String schemaPattern,
			String procedureNamePattern, String columnNamePattern)
			throws SQLException
	{
		return new TResultSetWrapper(null,
				targetMetaData.getProcedureColumns(catalog, schemaPattern,
						procedureNamePattern, columnNamePattern));
	}

	public String getProcedureTerm() throws SQLException
	{
		return this.targetMetaData.getProcedureTerm();
	}

	public ResultSet getProcedures(String catalog, String schemaPattern,
			String procedureNamePattern) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getProcedures(
				catalog, schemaPattern, procedureNamePattern));
	}

	public int getResultSetHoldability() throws SQLException
	{
		return this.targetMetaData.getResultSetHoldability();
	}

	public String getSQLKeywords() throws SQLException
	{
		return this.targetMetaData.getSQLKeywords();
	}

	public int getSQLStateType() throws SQLException
	{
		return this.targetMetaData.getSQLStateType();
	}

	public String getSchemaTerm() throws SQLException
	{
		return this.targetMetaData.getSchemaTerm();
	}

	public ResultSet getSchemas() throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getSchemas());
	}

	public String getSearchStringEscape() throws SQLException
	{
		return this.targetMetaData.getSearchStringEscape();
	}

	public String getStringFunctions() throws SQLException
	{
		return this.targetMetaData.getStringFunctions();
	}

	public ResultSet getSuperTables(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getSuperTables(
				catalog, schemaPattern, tableNamePattern));
	}

	public ResultSet getSuperTypes(String catalog, String schemaPattern,
			String typeNamePattern) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getSuperTypes(
				catalog, schemaPattern, typeNamePattern));
	}

	public String getSystemFunctions() throws SQLException
	{
		return this.targetMetaData.getSystemFunctions();
	}

	public ResultSet getTablePrivileges(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getTablePrivileges(
				catalog, schemaPattern, tableNamePattern));
	}

	public ResultSet getTableTypes() throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getTableTypes());
	}

	public ResultSet getTables(String catalog, String schemaPattern,
			String tableNamePattern, String[] types) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getTables(catalog,
				schemaPattern, tableNamePattern, types));
	}

	public String getTimeDateFunctions() throws SQLException
	{
		return this.targetMetaData.getTimeDateFunctions();
	}

	public ResultSet getTypeInfo() throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getTypeInfo());
	}

	public ResultSet getUDTs(String catalog, String schemaPattern,
			String typeNamePattern, int[] types) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getUDTs(catalog,
				schemaPattern, typeNamePattern, types));
	}

	public String getURL() throws SQLException
	{
		return this.targetMetaData.getURL();
	}

	public String getUserName() throws SQLException
	{
		return this.targetMetaData.getUserName();
	}

	public ResultSet getVersionColumns(String catalog, String schema,
			String table) throws SQLException
	{
		return new TResultSetWrapper(null, targetMetaData.getVersionColumns(
				catalog, schema, table));
	}

	public boolean insertsAreDetected(int type) throws SQLException
	{
		return this.targetMetaData.insertsAreDetected(type);
	}

	public boolean isCatalogAtStart() throws SQLException
	{
		return this.targetMetaData.isCatalogAtStart();
	}

	public boolean isReadOnly() throws SQLException
	{
		return this.targetMetaData.isReadOnly();
	}

	public boolean locatorsUpdateCopy() throws SQLException
	{
		return this.targetMetaData.locatorsUpdateCopy();
	}

	public boolean nullPlusNonNullIsNull() throws SQLException
	{
		return this.targetMetaData.nullPlusNonNullIsNull();
	}

	public boolean nullsAreSortedAtEnd() throws SQLException
	{
		return this.targetMetaData.nullsAreSortedAtEnd();
	}

	public boolean nullsAreSortedAtStart() throws SQLException
	{
		return this.targetMetaData.nullsAreSortedAtStart();
	}

	public boolean nullsAreSortedHigh() throws SQLException
	{
		return this.targetMetaData.nullsAreSortedHigh();
	}

	public boolean nullsAreSortedLow() throws SQLException
	{
		return this.targetMetaData.nullsAreSortedLow();
	}

	public boolean othersDeletesAreVisible(int type) throws SQLException
	{
		return this.targetMetaData.othersDeletesAreVisible(type);
	}

	public boolean othersInsertsAreVisible(int type) throws SQLException
	{
		return this.targetMetaData.othersInsertsAreVisible(type);
	}

	public boolean othersUpdatesAreVisible(int type) throws SQLException
	{
		return this.targetMetaData.othersUpdatesAreVisible(type);
	}

	public boolean ownDeletesAreVisible(int type) throws SQLException
	{
		return this.targetMetaData.ownDeletesAreVisible(type);
	}

	public boolean ownInsertsAreVisible(int type) throws SQLException
	{
		return this.targetMetaData.ownInsertsAreVisible(type);
	}

	public boolean ownUpdatesAreVisible(int type) throws SQLException
	{
		return this.targetMetaData.ownUpdatesAreVisible(type);
	}

	public boolean storesLowerCaseIdentifiers() throws SQLException
	{
		return this.targetMetaData.storesLowerCaseIdentifiers();
	}

	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
	{
		return this.targetMetaData.storesLowerCaseQuotedIdentifiers();
	}

	public boolean storesMixedCaseIdentifiers() throws SQLException
	{
		return this.targetMetaData.storesMixedCaseIdentifiers();
	}

	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
	{
		return this.targetMetaData.storesMixedCaseQuotedIdentifiers();
	}

	public boolean storesUpperCaseIdentifiers() throws SQLException
	{
		return this.targetMetaData.storesUpperCaseIdentifiers();
	}

	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
	{
		return this.targetMetaData.storesUpperCaseQuotedIdentifiers();
	}

	public boolean supportsANSI92EntryLevelSQL() throws SQLException
	{
		return this.targetMetaData.supportsANSI92EntryLevelSQL();
	}

	public boolean supportsANSI92FullSQL() throws SQLException
	{
		return this.targetMetaData.supportsANSI92FullSQL();
	}

	public boolean supportsANSI92IntermediateSQL() throws SQLException
	{
		return this.targetMetaData.supportsANSI92IntermediateSQL();
	}

	public boolean supportsAlterTableWithAddColumn() throws SQLException
	{
		return this.targetMetaData.supportsAlterTableWithAddColumn();
	}

	public boolean supportsAlterTableWithDropColumn() throws SQLException
	{
		return this.targetMetaData.supportsAlterTableWithDropColumn();
	}

	public boolean supportsBatchUpdates() throws SQLException
	{
		return this.targetMetaData.supportsBatchUpdates();
	}

	public boolean supportsCatalogsInDataManipulation() throws SQLException
	{
		return this.targetMetaData.supportsCatalogsInDataManipulation();
	}

	public boolean supportsCatalogsInIndexDefinitions() throws SQLException
	{
		return this.targetMetaData.supportsCatalogsInIndexDefinitions();
	}

	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
	{
		return this.targetMetaData.supportsCatalogsInPrivilegeDefinitions();
	}

	public boolean supportsCatalogsInProcedureCalls() throws SQLException
	{
		return this.targetMetaData.supportsCatalogsInProcedureCalls();
	}

	public boolean supportsCatalogsInTableDefinitions() throws SQLException
	{
		return this.targetMetaData.supportsCatalogsInTableDefinitions();
	}

	public boolean supportsColumnAliasing() throws SQLException
	{
		return this.targetMetaData.supportsColumnAliasing();
	}

	public boolean supportsConvert() throws SQLException
	{
		return this.targetMetaData.supportsConvert();
	}

	public boolean supportsConvert(int fromType, int toType)
			throws SQLException
	{
		return this.targetMetaData.supportsConvert(fromType, toType);
	}

	public boolean supportsCoreSQLGrammar() throws SQLException
	{
		return this.targetMetaData.supportsCoreSQLGrammar();
	}

	public boolean supportsCorrelatedSubqueries() throws SQLException
	{
		return this.targetMetaData.supportsCorrelatedSubqueries();
	}

	public boolean supportsDataDefinitionAndDataManipulationTransactions()
			throws SQLException
	{
		return this.targetMetaData
				.supportsDataDefinitionAndDataManipulationTransactions();
	}

	public boolean supportsDataManipulationTransactionsOnly()
			throws SQLException
	{
		return this.targetMetaData.supportsDataManipulationTransactionsOnly();
	}

	public boolean supportsDifferentTableCorrelationNames() throws SQLException
	{
		return this.targetMetaData.supportsDifferentTableCorrelationNames();
	}

	public boolean supportsExpressionsInOrderBy() throws SQLException
	{
		return this.targetMetaData.supportsExpressionsInOrderBy();
	}

	public boolean supportsExtendedSQLGrammar() throws SQLException
	{
		return this.targetMetaData.supportsExtendedSQLGrammar();
	}

	public boolean supportsFullOuterJoins() throws SQLException
	{
		return this.targetMetaData.supportsFullOuterJoins();
	}

	public boolean supportsGetGeneratedKeys() throws SQLException
	{
		return this.targetMetaData.supportsGetGeneratedKeys();
	}

	public boolean supportsGroupBy() throws SQLException
	{
		return this.targetMetaData.supportsGroupBy();
	}

	public boolean supportsGroupByBeyondSelect() throws SQLException
	{
		return this.targetMetaData.supportsGroupByBeyondSelect();
	}

	public boolean supportsGroupByUnrelated() throws SQLException
	{
		return this.targetMetaData.supportsGroupByUnrelated();
	}

	public boolean supportsIntegrityEnhancementFacility() throws SQLException
	{
		return this.targetMetaData.supportsIntegrityEnhancementFacility();
	}

	public boolean supportsLikeEscapeClause() throws SQLException
	{
		return this.targetMetaData.supportsLikeEscapeClause();
	}

	public boolean supportsLimitedOuterJoins() throws SQLException
	{
		return this.targetMetaData.supportsLimitedOuterJoins();
	}

	public boolean supportsMinimumSQLGrammar() throws SQLException
	{
		return this.targetMetaData.supportsMinimumSQLGrammar();
	}

	public boolean supportsMixedCaseIdentifiers() throws SQLException
	{
		return this.targetMetaData.supportsMixedCaseIdentifiers();
	}

	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
	{
		return this.targetMetaData.supportsMixedCaseQuotedIdentifiers();
	}

	public boolean supportsMultipleOpenResults() throws SQLException
	{
		return this.targetMetaData.supportsMultipleOpenResults();
	}

	public boolean supportsMultipleResultSets() throws SQLException
	{
		return this.targetMetaData.supportsMultipleResultSets();
	}

	public boolean supportsMultipleTransactions() throws SQLException
	{
		return this.targetMetaData.supportsMultipleTransactions();
	}

	public boolean supportsNamedParameters() throws SQLException
	{
		return this.targetMetaData.supportsNamedParameters();
	}

	public boolean supportsNonNullableColumns() throws SQLException
	{
		return this.targetMetaData.supportsNonNullableColumns();
	}

	public boolean supportsOpenCursorsAcrossCommit() throws SQLException
	{
		return this.targetMetaData.supportsOpenCursorsAcrossCommit();
	}

	public boolean supportsOpenCursorsAcrossRollback() throws SQLException
	{
		return this.targetMetaData.supportsOpenCursorsAcrossRollback();
	}

	public boolean supportsOpenStatementsAcrossCommit() throws SQLException
	{
		return this.targetMetaData.supportsOpenStatementsAcrossCommit();
	}

	public boolean supportsOpenStatementsAcrossRollback() throws SQLException
	{
		return this.targetMetaData.supportsOpenStatementsAcrossRollback();
	}

	public boolean supportsOrderByUnrelated() throws SQLException
	{
		return this.targetMetaData.supportsOrderByUnrelated();
	}

	public boolean supportsOuterJoins() throws SQLException
	{
		return this.targetMetaData.supportsOuterJoins();
	}

	public boolean supportsPositionedDelete() throws SQLException
	{
		return this.targetMetaData.supportsPositionedDelete();
	}

	public boolean supportsPositionedUpdate() throws SQLException
	{
		return this.targetMetaData.supportsPositionedUpdate();
	}

	public boolean supportsResultSetConcurrency(int type, int concurrency)
			throws SQLException
	{
		return this.targetMetaData.supportsResultSetConcurrency(type,
				concurrency);
	}

	public boolean supportsResultSetHoldability(int holdability)
			throws SQLException
	{
		return this.targetMetaData.supportsResultSetHoldability(holdability);
	}

	public boolean supportsResultSetType(int type) throws SQLException
	{
		return this.targetMetaData.supportsResultSetType(type);
	}

	public boolean supportsSavepoints() throws SQLException
	{
		return this.targetMetaData.supportsSavepoints();
	}

	public boolean supportsSchemasInDataManipulation() throws SQLException
	{
		return this.targetMetaData.supportsSchemasInDataManipulation();
	}

	public boolean supportsSchemasInIndexDefinitions() throws SQLException
	{
		return this.targetMetaData.supportsSchemasInIndexDefinitions();
	}

	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
	{
		return this.targetMetaData.supportsSchemasInPrivilegeDefinitions();
	}

	public boolean supportsSchemasInProcedureCalls() throws SQLException
	{
		return this.targetMetaData.supportsSchemasInProcedureCalls();
	}

	public boolean supportsSchemasInTableDefinitions() throws SQLException
	{
		return this.targetMetaData.supportsSchemasInTableDefinitions();
	}

	public boolean supportsSelectForUpdate() throws SQLException
	{
		return this.targetMetaData.supportsSelectForUpdate();
	}

	public boolean supportsStatementPooling() throws SQLException
	{
		return this.targetMetaData.supportsStatementPooling();
	}

	public boolean supportsStoredProcedures() throws SQLException
	{
		return this.targetMetaData.supportsStoredProcedures();
	}

	public boolean supportsSubqueriesInComparisons() throws SQLException
	{
		return this.targetMetaData.supportsSubqueriesInComparisons();
	}

	public boolean supportsSubqueriesInExists() throws SQLException
	{
		return this.targetMetaData.supportsSubqueriesInExists();
	}

	public boolean supportsSubqueriesInIns() throws SQLException
	{
		return this.targetMetaData.supportsSubqueriesInIns();
	}

	public boolean supportsSubqueriesInQuantifieds() throws SQLException
	{
		return this.targetMetaData.supportsSubqueriesInQuantifieds();
	}

	public boolean supportsTableCorrelationNames() throws SQLException
	{
		return this.targetMetaData.supportsTableCorrelationNames();
	}

	public boolean supportsTransactionIsolationLevel(int level)
			throws SQLException
	{
		return this.targetMetaData.supportsTransactionIsolationLevel(level);
	}

	public boolean supportsTransactions() throws SQLException
	{
		return this.targetMetaData.supportsTransactions();
	}

	public boolean supportsUnion() throws SQLException
	{
		return this.targetMetaData.supportsUnion();
	}

	public boolean supportsUnionAll() throws SQLException
	{
		return this.targetMetaData.supportsUnionAll();
	}

	public boolean updatesAreDetected(int type) throws SQLException
	{
		return this.targetMetaData.updatesAreDetected(type);
	}

	public boolean usesLocalFilePerTable() throws SQLException
	{
		return this.targetMetaData.usesLocalFilePerTable();
	}

	public boolean usesLocalFiles() throws SQLException
	{
		return this.targetMetaData.usesLocalFiles();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return this.getClass().isAssignableFrom(iface);
	}

	public Clob createClob() throws SQLException
	{
		return this.targetConnection.createClob();
	}

	public RowIdLifetime getRowIdLifetime() throws SQLException
	{
		return this.targetMetaData.getRowIdLifetime();
	}

	public ResultSet getSchemas(String catalog, String schemaPattern)
			throws SQLException
	{
		return this.targetMetaData.getSchemas(catalog, schemaPattern);
	}

	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
	{
		return this.targetMetaData.supportsStoredFunctionsUsingCallSyntax();
	}

	public boolean autoCommitFailureClosesAllResultSets() throws SQLException
	{
		return this.targetMetaData.autoCommitFailureClosesAllResultSets();
	}

	public ResultSet getClientInfoProperties() throws SQLException
	{
		return this.targetMetaData.getClientInfoProperties();
	}

	public ResultSet getFunctions(String catalog, String schemaPattern,
			String functionNamePattern) throws SQLException
	{
		return this.targetMetaData.getFunctions(catalog, schemaPattern,
				functionNamePattern);
	}

	public ResultSet getFunctionColumns(String catalog, String schemaPattern,
			String functionNamePattern, String columnNamePattern)
			throws SQLException
	{
		return this.targetMetaData.getFunctionColumns(catalog, schemaPattern,
				functionNamePattern, columnNamePattern);
	}

	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		try
		{
			return (T) this;
		} catch (Exception e)
		{
			throw new SQLException(e);
		}
	}
}
