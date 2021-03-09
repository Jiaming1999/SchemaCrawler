/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2021, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.crawl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.CrawlInfo;
import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.DatabaseUser;
import schemacrawler.schema.NamedObject;
import schemacrawler.schema.NamedObjectKey;
import schemacrawler.schema.Reducer;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaReference;

/**
 * Database and connection information. Created from metadata returned by a JDBC call, and other
 * sources of information.
 *
 * @author Sualeh Fatehi sualeh@hotmail.com
 */
final class MutableCatalog extends AbstractNamedObjectWithAttributes implements Catalog {

  private static final class FilterBySchema implements Predicate<DatabaseObject> {

    private final Schema schema;

    public FilterBySchema(final Schema schema) {
      this.schema = requireNonNull(schema, "No schema provided");
    }

    @Override
    public boolean test(final DatabaseObject databaseObject) {
      return databaseObject != null && databaseObject.getSchema().equals(schema);
    }
  }

  private static final long serialVersionUID = 4051323422934251828L;

  private final NamedObjectList<MutableColumnDataType> columnDataTypes = new NamedObjectList<>();
  private final MutableDatabaseInfo databaseInfo;
  private final MutableJdbcDriverInfo jdbcDriverInfo;
  private final NamedObjectList<MutableRoutine> routines = new NamedObjectList<>();
  private final NamedObjectList<SchemaReference> schemas = new NamedObjectList<>();
  private final NamedObjectList<MutableSequence> sequences = new NamedObjectList<>();
  private final NamedObjectList<MutableSynonym> synonyms = new NamedObjectList<>();
  private final NamedObjectList<MutableTable> tables = new NamedObjectList<>();
  private final NamedObjectList<ImmutableDatabaseUser> databaseUsers = new NamedObjectList<>();
  private final MutableCrawlInfo crawlInfo;

  MutableCatalog(final String name) {
    super(name);
    databaseInfo = new MutableDatabaseInfo();
    jdbcDriverInfo = new MutableJdbcDriverInfo();
    crawlInfo = new MutableCrawlInfo();
  }

  /** {@inheritDoc} */
  @Override
  public Collection<ColumnDataType> getColumnDataTypes() {
    return new ArrayList<>(columnDataTypes.values());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<ColumnDataType> getColumnDataTypes(final Schema schema) {
    final FilterBySchema filter = new FilterBySchema(schema);
    final Collection<ColumnDataType> columnDataTypes = new ArrayList<>();
    for (final ColumnDataType columnDataType : this.columnDataTypes) {
      if (filter.test(columnDataType)) {
        columnDataTypes.add(columnDataType);
      }
    }
    return columnDataTypes;
  }

  @Override
  public CrawlInfo getCrawlInfo() {
    return crawlInfo;
  }

  @Override
  public MutableDatabaseInfo getDatabaseInfo() {
    return databaseInfo;
  }

  /** {@inheritDoc} */
  @Override
  public Collection<DatabaseUser> getDatabaseUsers() {
    return new ArrayList<>(databaseUsers.values());
  }

  /** {@inheritDoc} */
  @Override
  public MutableJdbcDriverInfo getJdbcDriverInfo() {
    return jdbcDriverInfo;
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Routine> getRoutines() {
    return new ArrayList<>(routines.values());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Routine> getRoutines(final Schema schema) {
    final FilterBySchema filter = new FilterBySchema(schema);
    final Collection<Routine> routines = new ArrayList<>();
    for (final Routine routine : this.routines) {
      if (filter.test(routine)) {
        routines.add(routine);
      }
    }
    return routines;
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Schema> getSchemas() {
    return new ArrayList<>(schemas.values());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Sequence> getSequences() {
    return new ArrayList<>(sequences.values());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Sequence> getSequences(final Schema schema) {
    final FilterBySchema filter = new FilterBySchema(schema);
    final Collection<Sequence> sequences = new ArrayList<>();
    for (final Sequence sequence : this.sequences) {
      if (filter.test(sequence)) {
        sequences.add(sequence);
      }
    }
    return sequences;
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Synonym> getSynonyms() {
    return new ArrayList<>(synonyms.values());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Synonym> getSynonyms(final Schema schema) {
    final FilterBySchema filter = new FilterBySchema(schema);
    final Collection<Synonym> synonyms = new ArrayList<>();
    for (final Synonym synonym : this.synonyms) {
      if (filter.test(synonym)) {
        synonyms.add(synonym);
      }
    }
    return synonyms;
  }

  /** {@inheritDoc} */
  @Override
  public Collection<ColumnDataType> getSystemColumnDataTypes() {
    return getColumnDataTypes(new SchemaReference());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Table> getTables() {
    return new ArrayList<>(tables.values());
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Table> getTables(final Schema schema) {
    final FilterBySchema filter = new FilterBySchema(schema);
    final Collection<Table> tables = new ArrayList<>();
    for (final Table table : this.tables) {
      if (filter.test(table)) {
        tables.add(table);
      }
    }
    return tables;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Column> lookupColumn(
      final Schema schemaRef, final String tableName, final String name) {

    final Optional<MutableTable> tableOptional = lookupTable(schemaRef, tableName);
    if (tableOptional.isPresent()) {
      final Table table = tableOptional.get();
      return table.lookupColumn(name);
    } else {
      return Optional.empty();
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableColumnDataType> lookupColumnDataType(
      final Schema schema, final String name) {
    return columnDataTypes.lookup(schema, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableRoutine> lookupRoutine(final Schema schema, final String name) {
    return routines.lookup(schema, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<SchemaReference> lookupSchema(final String name) {
    // Schemas need to be looked up by full name, since either the
    // catalog or schema may be null, depending on the database
    if (name == null) {
      return Optional.empty();
    }
    for (final SchemaReference schema : schemas) {
      if (name.equals(schema.getFullName())) {
        return Optional.of(schema);
      }
    }
    return Optional.empty();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableSequence> lookupSequence(final Schema schemaRef, final String name) {
    return sequences.lookup(schemaRef, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableSynonym> lookupSynonym(final Schema schemaRef, final String name) {
    return synonyms.lookup(schemaRef, name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableColumnDataType> lookupSystemColumnDataType(final String name) {
    return lookupColumnDataType(new SchemaReference(), name);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MutableTable> lookupTable(final Schema schemaRef, final String name) {
    return tables.lookup(schemaRef, name);
  }

  @Override
  public <N extends NamedObject> void reduce(final Class<N> clazz, final Reducer<N> reducer) {
    if (reducer == null) {
      return;
    } else if (Schema.class.isAssignableFrom(clazz)) {
      ((Reducer<Schema>) reducer).reduce(schemas);
    } else if (Table.class.isAssignableFrom(clazz)) {
      // Filter the list of tables based on grep criteria, and
      // parent-child relationships
      ((Reducer<Table>) reducer).reduce(tables);
    } else if (Routine.class.isAssignableFrom(clazz)) {
      // Filter the list of routines based on grep criteria
      ((Reducer<Routine>) reducer).reduce(routines);
    } else if (Synonym.class.isAssignableFrom(clazz)) {
      ((Reducer<Synonym>) reducer).reduce(synonyms);
    } else if (Sequence.class.isAssignableFrom(clazz)) {
      ((Reducer<Sequence>) reducer).reduce(sequences);
    }
  }

  void addColumnDataType(final MutableColumnDataType columnDataType) {
    if (columnDataType != null) {
      columnDataTypes.add(columnDataType);
    }
  }

  void addDatabaseUser(final ImmutableDatabaseUser databaseUser) {
    databaseUsers.add(databaseUser);
  }

  void addRoutine(final MutableRoutine routine) {
    routines.add(routine);
  }

  Schema addSchema(final SchemaReference schema) {
    schemas.add(schema);
    return schema;
  }

  Schema addSchema(final String catalogName, final String schemaName) {
    return addSchema(new SchemaReference(catalogName, schemaName));
  }

  void addSequence(final MutableSequence sequence) {
    sequences.add(sequence);
  }

  void addSynonym(final MutableSynonym synonym) {
    synonyms.add(synonym);
  }

  void addTable(final MutableTable table) {
    tables.add(table);
  }

  NamedObjectList<MutableRoutine> getAllRoutines() {
    return routines;
  }

  NamedObjectList<SchemaReference> getAllSchemas() {
    return schemas;
  }

  NamedObjectList<MutableTable> getAllTables() {
    return tables;
  }

  MutableColumnDataType lookupBaseColumnDataTypeByType(final int baseType) {
    final SchemaReference systemSchema = new SchemaReference();
    MutableColumnDataType columnDataType = null;
    int count = 0;
    for (final MutableColumnDataType currentColumnDataType : columnDataTypes) {
      if (baseType == currentColumnDataType.getJavaSqlType().getVendorTypeNumber()) {
        if (currentColumnDataType.getSchema().equals(systemSchema)) {
          columnDataType = currentColumnDataType;
          count = count + 1;
        }
      }
    }
    if (count == 1) {
      return columnDataType;
    } else {
      return null;
    }
  }

  Optional<MutableRoutine> lookupRoutine(final NamedObjectKey routineLookupKey) {
    return routines.lookup(routineLookupKey);
  }

  Optional<MutableTable> lookupTable(final NamedObjectKey tableLookupKey) {
    return tables.lookup(tableLookupKey);
  }

  void setCrawlInfo() {
    crawlInfo.setDatabaseInfo(jdbcDriverInfo, databaseInfo);
  }
}
