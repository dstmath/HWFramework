package java.sql;

public interface DatabaseMetaData extends Wrapper {
    public static final short attributeNoNulls = (short) 0;
    public static final short attributeNullable = (short) 1;
    public static final short attributeNullableUnknown = (short) 2;
    public static final int bestRowNotPseudo = 1;
    public static final int bestRowPseudo = 2;
    public static final int bestRowSession = 2;
    public static final int bestRowTemporary = 0;
    public static final int bestRowTransaction = 1;
    public static final int bestRowUnknown = 0;
    public static final int columnNoNulls = 0;
    public static final int columnNullable = 1;
    public static final int columnNullableUnknown = 2;
    public static final int functionColumnIn = 1;
    public static final int functionColumnInOut = 2;
    public static final int functionColumnOut = 3;
    public static final int functionColumnResult = 5;
    public static final int functionColumnUnknown = 0;
    public static final int functionNoNulls = 0;
    public static final int functionNoTable = 1;
    public static final int functionNullable = 1;
    public static final int functionNullableUnknown = 2;
    public static final int functionResultUnknown = 0;
    public static final int functionReturn = 4;
    public static final int functionReturnsTable = 2;
    public static final int importedKeyCascade = 0;
    public static final int importedKeyInitiallyDeferred = 5;
    public static final int importedKeyInitiallyImmediate = 6;
    public static final int importedKeyNoAction = 3;
    public static final int importedKeyNotDeferrable = 7;
    public static final int importedKeyRestrict = 1;
    public static final int importedKeySetDefault = 4;
    public static final int importedKeySetNull = 2;
    public static final int procedureColumnIn = 1;
    public static final int procedureColumnInOut = 2;
    public static final int procedureColumnOut = 4;
    public static final int procedureColumnResult = 3;
    public static final int procedureColumnReturn = 5;
    public static final int procedureColumnUnknown = 0;
    public static final int procedureNoNulls = 0;
    public static final int procedureNoResult = 1;
    public static final int procedureNullable = 1;
    public static final int procedureNullableUnknown = 2;
    public static final int procedureResultUnknown = 0;
    public static final int procedureReturnsResult = 2;
    public static final int sqlStateSQL = 2;
    public static final int sqlStateSQL99 = 2;
    public static final int sqlStateXOpen = 1;
    public static final short tableIndexClustered = (short) 1;
    public static final short tableIndexHashed = (short) 2;
    public static final short tableIndexOther = (short) 3;
    public static final short tableIndexStatistic = (short) 0;
    public static final int typeNoNulls = 0;
    public static final int typeNullable = 1;
    public static final int typeNullableUnknown = 2;
    public static final int typePredBasic = 2;
    public static final int typePredChar = 1;
    public static final int typePredNone = 0;
    public static final int typeSearchable = 3;
    public static final int versionColumnNotPseudo = 1;
    public static final int versionColumnPseudo = 2;
    public static final int versionColumnUnknown = 0;

    boolean allProceduresAreCallable() throws SQLException;

    boolean allTablesAreSelectable() throws SQLException;

    boolean autoCommitFailureClosesAllResultSets() throws SQLException;

    boolean dataDefinitionCausesTransactionCommit() throws SQLException;

    boolean dataDefinitionIgnoredInTransactions() throws SQLException;

    boolean deletesAreDetected(int i) throws SQLException;

    boolean doesMaxRowSizeIncludeBlobs() throws SQLException;

    ResultSet getAttributes(String str, String str2, String str3, String str4) throws SQLException;

    ResultSet getBestRowIdentifier(String str, String str2, String str3, int i, boolean z) throws SQLException;

    String getCatalogSeparator() throws SQLException;

    String getCatalogTerm() throws SQLException;

    ResultSet getCatalogs() throws SQLException;

    ResultSet getClientInfoProperties() throws SQLException;

    ResultSet getColumnPrivileges(String str, String str2, String str3, String str4) throws SQLException;

    ResultSet getColumns(String str, String str2, String str3, String str4) throws SQLException;

    Connection getConnection() throws SQLException;

    ResultSet getCrossReference(String str, String str2, String str3, String str4, String str5, String str6) throws SQLException;

    int getDatabaseMajorVersion() throws SQLException;

    int getDatabaseMinorVersion() throws SQLException;

    String getDatabaseProductName() throws SQLException;

    String getDatabaseProductVersion() throws SQLException;

    int getDefaultTransactionIsolation() throws SQLException;

    int getDriverMajorVersion();

    int getDriverMinorVersion();

    String getDriverName() throws SQLException;

    String getDriverVersion() throws SQLException;

    ResultSet getExportedKeys(String str, String str2, String str3) throws SQLException;

    String getExtraNameCharacters() throws SQLException;

    ResultSet getFunctionColumns(String str, String str2, String str3, String str4) throws SQLException;

    ResultSet getFunctions(String str, String str2, String str3) throws SQLException;

    String getIdentifierQuoteString() throws SQLException;

    ResultSet getImportedKeys(String str, String str2, String str3) throws SQLException;

    ResultSet getIndexInfo(String str, String str2, String str3, boolean z, boolean z2) throws SQLException;

    int getJDBCMajorVersion() throws SQLException;

    int getJDBCMinorVersion() throws SQLException;

    int getMaxBinaryLiteralLength() throws SQLException;

    int getMaxCatalogNameLength() throws SQLException;

    int getMaxCharLiteralLength() throws SQLException;

    int getMaxColumnNameLength() throws SQLException;

    int getMaxColumnsInGroupBy() throws SQLException;

    int getMaxColumnsInIndex() throws SQLException;

    int getMaxColumnsInOrderBy() throws SQLException;

    int getMaxColumnsInSelect() throws SQLException;

    int getMaxColumnsInTable() throws SQLException;

    int getMaxConnections() throws SQLException;

    int getMaxCursorNameLength() throws SQLException;

    int getMaxIndexLength() throws SQLException;

    int getMaxProcedureNameLength() throws SQLException;

    int getMaxRowSize() throws SQLException;

    int getMaxSchemaNameLength() throws SQLException;

    int getMaxStatementLength() throws SQLException;

    int getMaxStatements() throws SQLException;

    int getMaxTableNameLength() throws SQLException;

    int getMaxTablesInSelect() throws SQLException;

    int getMaxUserNameLength() throws SQLException;

    String getNumericFunctions() throws SQLException;

    ResultSet getPrimaryKeys(String str, String str2, String str3) throws SQLException;

    ResultSet getProcedureColumns(String str, String str2, String str3, String str4) throws SQLException;

    String getProcedureTerm() throws SQLException;

    ResultSet getProcedures(String str, String str2, String str3) throws SQLException;

    int getResultSetHoldability() throws SQLException;

    RowIdLifetime getRowIdLifetime() throws SQLException;

    String getSQLKeywords() throws SQLException;

    int getSQLStateType() throws SQLException;

    String getSchemaTerm() throws SQLException;

    ResultSet getSchemas() throws SQLException;

    ResultSet getSchemas(String str, String str2) throws SQLException;

    String getSearchStringEscape() throws SQLException;

    String getStringFunctions() throws SQLException;

    ResultSet getSuperTables(String str, String str2, String str3) throws SQLException;

    ResultSet getSuperTypes(String str, String str2, String str3) throws SQLException;

    String getSystemFunctions() throws SQLException;

    ResultSet getTablePrivileges(String str, String str2, String str3) throws SQLException;

    ResultSet getTableTypes() throws SQLException;

    ResultSet getTables(String str, String str2, String str3, String[] strArr) throws SQLException;

    String getTimeDateFunctions() throws SQLException;

    ResultSet getTypeInfo() throws SQLException;

    ResultSet getUDTs(String str, String str2, String str3, int[] iArr) throws SQLException;

    String getURL() throws SQLException;

    String getUserName() throws SQLException;

    ResultSet getVersionColumns(String str, String str2, String str3) throws SQLException;

    boolean insertsAreDetected(int i) throws SQLException;

    boolean isCatalogAtStart() throws SQLException;

    boolean isReadOnly() throws SQLException;

    boolean locatorsUpdateCopy() throws SQLException;

    boolean nullPlusNonNullIsNull() throws SQLException;

    boolean nullsAreSortedAtEnd() throws SQLException;

    boolean nullsAreSortedAtStart() throws SQLException;

    boolean nullsAreSortedHigh() throws SQLException;

    boolean nullsAreSortedLow() throws SQLException;

    boolean othersDeletesAreVisible(int i) throws SQLException;

    boolean othersInsertsAreVisible(int i) throws SQLException;

    boolean othersUpdatesAreVisible(int i) throws SQLException;

    boolean ownDeletesAreVisible(int i) throws SQLException;

    boolean ownInsertsAreVisible(int i) throws SQLException;

    boolean ownUpdatesAreVisible(int i) throws SQLException;

    boolean storesLowerCaseIdentifiers() throws SQLException;

    boolean storesLowerCaseQuotedIdentifiers() throws SQLException;

    boolean storesMixedCaseIdentifiers() throws SQLException;

    boolean storesMixedCaseQuotedIdentifiers() throws SQLException;

    boolean storesUpperCaseIdentifiers() throws SQLException;

    boolean storesUpperCaseQuotedIdentifiers() throws SQLException;

    boolean supportsANSI92EntryLevelSQL() throws SQLException;

    boolean supportsANSI92FullSQL() throws SQLException;

    boolean supportsANSI92IntermediateSQL() throws SQLException;

    boolean supportsAlterTableWithAddColumn() throws SQLException;

    boolean supportsAlterTableWithDropColumn() throws SQLException;

    boolean supportsBatchUpdates() throws SQLException;

    boolean supportsCatalogsInDataManipulation() throws SQLException;

    boolean supportsCatalogsInIndexDefinitions() throws SQLException;

    boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException;

    boolean supportsCatalogsInProcedureCalls() throws SQLException;

    boolean supportsCatalogsInTableDefinitions() throws SQLException;

    boolean supportsColumnAliasing() throws SQLException;

    boolean supportsConvert() throws SQLException;

    boolean supportsConvert(int i, int i2) throws SQLException;

    boolean supportsCoreSQLGrammar() throws SQLException;

    boolean supportsCorrelatedSubqueries() throws SQLException;

    boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException;

    boolean supportsDataManipulationTransactionsOnly() throws SQLException;

    boolean supportsDifferentTableCorrelationNames() throws SQLException;

    boolean supportsExpressionsInOrderBy() throws SQLException;

    boolean supportsExtendedSQLGrammar() throws SQLException;

    boolean supportsFullOuterJoins() throws SQLException;

    boolean supportsGetGeneratedKeys() throws SQLException;

    boolean supportsGroupBy() throws SQLException;

    boolean supportsGroupByBeyondSelect() throws SQLException;

    boolean supportsGroupByUnrelated() throws SQLException;

    boolean supportsIntegrityEnhancementFacility() throws SQLException;

    boolean supportsLikeEscapeClause() throws SQLException;

    boolean supportsLimitedOuterJoins() throws SQLException;

    boolean supportsMinimumSQLGrammar() throws SQLException;

    boolean supportsMixedCaseIdentifiers() throws SQLException;

    boolean supportsMixedCaseQuotedIdentifiers() throws SQLException;

    boolean supportsMultipleOpenResults() throws SQLException;

    boolean supportsMultipleResultSets() throws SQLException;

    boolean supportsMultipleTransactions() throws SQLException;

    boolean supportsNamedParameters() throws SQLException;

    boolean supportsNonNullableColumns() throws SQLException;

    boolean supportsOpenCursorsAcrossCommit() throws SQLException;

    boolean supportsOpenCursorsAcrossRollback() throws SQLException;

    boolean supportsOpenStatementsAcrossCommit() throws SQLException;

    boolean supportsOpenStatementsAcrossRollback() throws SQLException;

    boolean supportsOrderByUnrelated() throws SQLException;

    boolean supportsOuterJoins() throws SQLException;

    boolean supportsPositionedDelete() throws SQLException;

    boolean supportsPositionedUpdate() throws SQLException;

    boolean supportsResultSetConcurrency(int i, int i2) throws SQLException;

    boolean supportsResultSetHoldability(int i) throws SQLException;

    boolean supportsResultSetType(int i) throws SQLException;

    boolean supportsSavepoints() throws SQLException;

    boolean supportsSchemasInDataManipulation() throws SQLException;

    boolean supportsSchemasInIndexDefinitions() throws SQLException;

    boolean supportsSchemasInPrivilegeDefinitions() throws SQLException;

    boolean supportsSchemasInProcedureCalls() throws SQLException;

    boolean supportsSchemasInTableDefinitions() throws SQLException;

    boolean supportsSelectForUpdate() throws SQLException;

    boolean supportsStatementPooling() throws SQLException;

    boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException;

    boolean supportsStoredProcedures() throws SQLException;

    boolean supportsSubqueriesInComparisons() throws SQLException;

    boolean supportsSubqueriesInExists() throws SQLException;

    boolean supportsSubqueriesInIns() throws SQLException;

    boolean supportsSubqueriesInQuantifieds() throws SQLException;

    boolean supportsTableCorrelationNames() throws SQLException;

    boolean supportsTransactionIsolationLevel(int i) throws SQLException;

    boolean supportsTransactions() throws SQLException;

    boolean supportsUnion() throws SQLException;

    boolean supportsUnionAll() throws SQLException;

    boolean updatesAreDetected(int i) throws SQLException;

    boolean usesLocalFilePerTable() throws SQLException;

    boolean usesLocalFiles() throws SQLException;
}
