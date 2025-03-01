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

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.ObjectPropertyTestUtility.checkBooleanProperties;
import static schemacrawler.test.utility.ObjectPropertyTestUtility.checkIntegerProperties;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.DataTypeType;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.IndexColumnSortSequence;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Reducer;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraint;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableConstraintType;
import schemacrawler.schema.View;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.test.utility.TestContextParameterResolver;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.test.utility.TestUtility;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
@ExtendWith(TestContextParameterResolver.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SchemaCrawlerCoverageTest {

  private Catalog catalog;

  @Test
  public void catalogLookup() throws Exception {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");

    assertThat(catalog.lookupSchema(null), isEmpty());
    assertThat(catalog.lookupSchema("UNKNOWN"), isEmpty());
    assertThat(catalog.lookupSchema("PUBLIC.BOOKS"), isPresentAndIs(schema));

    final Sequence sequence = new MutableSequence(schema, "PUBLISHER_ID_SEQ");
    assertThat(catalog.lookupSequence(schema, null), isEmpty());
    assertThat(catalog.lookupSequence(schema, "UNKNOWN"), isEmpty());
    assertThat(catalog.lookupSequence(schema, "PUBLISHER_ID_SEQ"), isPresentAndIs(sequence));

    final Synonym synonym = new MutableSynonym(schema, "PUBLICATIONS");
    assertThat(catalog.lookupSynonym(schema, null), isEmpty());
    assertThat(catalog.lookupSynonym(schema, "UNKNOWN"), isEmpty());
    assertThat(catalog.lookupSynonym(schema, "PUBLICATIONS"), isPresentAndIs(synonym));

    final ColumnDataType systemColumnDataType =
        new MutableColumnDataType(new SchemaReference(), "VARCHAR", DataTypeType.system);
    assertThat(catalog.lookupSystemColumnDataType(null), isEmpty());
    assertThat(catalog.lookupSystemColumnDataType("UNKNOWN"), isEmpty());
    assertThat(catalog.lookupSystemColumnDataType("VARCHAR"), isPresentAndIs(systemColumnDataType));

    final Table table = new MutableTable(schema, "AUTHORS");
    assertThat(catalog.lookupTable(schema, null), isEmpty());
    assertThat(catalog.lookupTable(schema, "UNKNOWN"), isEmpty());
    assertThat(catalog.lookupTable(schema, "AUTHORS"), isPresentAndIs(table));
  }

  @Test
  public void catalogReduce() throws Exception {
    final Reducer reducer = spy(Reducer.class);

    catalog.reduce(Catalog.class, (Reducer<Catalog>) reducer);
    verifyNoMoreInteractions(reducer);

    assertThrows(NullPointerException.class, () -> catalog.reduce(null, reducer));
    assertThrows(NullPointerException.class, () -> catalog.reduce(Table.class, null));
  }

  @Test
  public void columnDataTypeProperties() throws Exception {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog.lookupTable(schema, "AUTHORS").get();
    final Column column = table.lookupColumn("FIRSTNAME").get();
    final ColumnDataType columnDataType = column.getColumnDataType();

    checkBooleanProperties(
        columnDataType,
        "autoIncrementable",
        "caseSensitive",
        "fixedPrecisionScale",
        "nullable",
        "unsigned");
  }

  @Test
  public void columnProperties() throws Exception {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog.lookupTable(schema, "AUTHORS").get();
    final Column column = table.lookupColumn("FIRSTNAME").get();

    checkBooleanProperties(column, "autoIncremented", "generated", "hidden");
  }

  @Test
  public void coverIndexColumn() {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog.lookupTable(schema, "AUTHORS").get();
    final Index index = table.lookupIndex("IDX_B_AUTHORS").get();
    final IndexColumn indexColumn = index.getColumns().get(0);
    final Column column = table.lookupColumn(indexColumn.getName()).get();

    compareColumnFields(indexColumn, column);

    assertThat(indexColumn.hasDefinition(), is(false));
    assertThat(indexColumn.getIndex(), is(index));
    assertThat(indexColumn.getIndexOrdinalPosition(), is(1));
    assertThat(indexColumn.getSortSequence(), is(IndexColumnSortSequence.ascending));
  }

  @Test
  public void coverTableConstraintColumn() {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog.lookupTable(schema, "AUTHORS").get();
    final TableConstraint tableConstraint = new ArrayList<>(table.getTableConstraints()).get(0);
    final TableConstraintColumn tableConstraintColumn =
        tableConstraint.getConstrainedColumns().get(0);
    final Column column = table.lookupColumn(tableConstraintColumn.getName()).get();

    compareColumnFields(tableConstraintColumn, column);

    assertThat(tableConstraintColumn.getTableConstraint(), is(tableConstraint));
    assertThat(tableConstraintColumn.getTableConstraintOrdinalPosition(), is(1));
  }

  @Test
  public void getRoutines() throws Exception {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");

    final Routine routine1 =
        new MutableFunction(schema, "NEW_PUBLISHER", "NEW_PUBLISHER_FORCE_VALUE");
    final Routine routine2 = new MutableFunction(schema, "NEW_PUBLISHER", "NEW_PUBLISHER_10160");

    assertThat(
        catalog.getRoutines(schema, "NEW_PUBLISHER"), containsInAnyOrder(routine1, routine2));
    assertThat(catalog.getRoutines(schema), hasItem(routine1));
    assertThat(catalog.getRoutines(schema), hasItem(routine2));
    assertThat(catalog.getRoutines(), hasItem(routine1));
    assertThat(catalog.getRoutines(), hasItem(routine2));
  }

  @Test
  public void indexProperties() throws Exception {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog.lookupTable(schema, "AUTHORS").get();
    final Index index = table.lookupIndex("IDX_B_AUTHORS").get();

    checkIntegerProperties(index, "cardinality", "pages");
    checkBooleanProperties(index, "unique");
  }

  @Test
  public void jdbcDriverInfoProperties() throws Exception {
    final JdbcDriverInfo jdbcDriverInfo = catalog.getJdbcDriverInfo();

    checkBooleanProperties(jdbcDriverInfo, "jdbcCompliant");

    assertThat(
        jdbcDriverInfo.toString(),
        matchesPattern(
            Pattern.compile(
                "-- driver: HSQL Database Engine Driver 2.6.0\\R"
                    + "-- driver class: org.hsqldb.jdbc.JDBCDriver\\R"
                    + "-- url: jdbc:hsqldb:hsql:\\/\\/0.0.0.0:\\d*/schemacrawler\\d*\\R"
                    + "-- jdbc compliant: false",
                Pattern.DOTALL)));
  }

  @BeforeAll
  public void loadCatalog(final Connection connection) throws Exception {

    final SchemaRetrievalOptions schemaRetrievalOptions = TestUtility.newSchemaRetrievalOptions();

    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder()
            .includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"))
            .includeAllSynonyms()
            .includeAllSequences()
            .includeAllRoutines();
    final LoadOptionsBuilder loadOptionsBuilder =
        LoadOptionsBuilder.builder().withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions())
            .withLoadOptions(loadOptionsBuilder.toOptions());

    catalog = getCatalog(connection, schemaRetrievalOptions, schemaCrawlerOptions);
  }

  @Test
  public void primaryKey() throws Exception {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog.lookupTable(schema, "AUTHORS").get();
    final PrimaryKey primaryKey = table.getPrimaryKey();

    assertThat(primaryKey.getFullName(), is("PUBLIC.BOOKS.AUTHORS.PK_AUTHORS"));
    assertThat(primaryKey.getConstrainedColumns().toString(), is("[PUBLIC.BOOKS.AUTHORS.ID]"));
    assertThat(primaryKey.getType(), is(TableConstraintType.primary_key));
    assertThat(primaryKey.isDeferrable(), is(false));
    assertThat(primaryKey.isInitiallyDeferred(), is(false));

    final TableConstraint constraint = new MutableTableConstraint(table, primaryKey.getName());
    final Optional<TableConstraint> optionalTableConstraint =
        table.lookupTableConstraint(primaryKey.getName());
    assertThat(optionalTableConstraint, isPresentAndIs(constraint));
  }

  @Test
  public void schemaCrawlerExceptions() throws Exception {

    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    final SchemaRetrievalOptions schemaRetrievalOptions =
        SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions();

    final Connection connection1 = mock(Connection.class);
    when(connection1.isClosed()).thenThrow(new SQLException("Forced SQL exception"));

    final Throwable exception =
        assertThrows(
            SchemaCrawlerException.class,
            () -> new SchemaCrawler(connection1, schemaRetrievalOptions, schemaCrawlerOptions));
    assertThat(exception.getCause().getMessage(), is("Forced SQL exception"));

    final Connection connection2 = mock(Connection.class);
    final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
    when(connection2.isClosed()).thenReturn(false);
    when(connection2.getMetaData()).thenReturn(databaseMetaData);
    final SchemaCrawler schemaCrawler =
        new SchemaCrawler(connection2, schemaRetrievalOptions, schemaCrawlerOptions);
    assertThrows(SchemaCrawlerException.class, () -> schemaCrawler.crawl());
  }

  @Test
  public void sequenceProperties() throws Exception {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Sequence sequence = catalog.lookupSequence(schema, "PUBLISHER_ID_SEQ").get();

    checkBooleanProperties(sequence, "cycle");
  }

  @Test
  public void tableAttributes() throws Exception {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog.lookupTable(schema, "AUTHORS").get();

    assertThat(table.hasAttribute("unknown"), is(false));
    assertThat(table.lookupAttribute("unknown"), isEmpty());

    assertThat(table.getAttribute("unknown", "no value"), is("no value"));
    assertThat(table.getAttribute("unknown", 10.5f), is(10.5f));

    assertThat(table.hasAttribute("new_one"), is(false));
    table.setAttribute("new_one", "some_value");
    assertThat(table.getAttribute("new_one", ""), is("some_value"));
    table.setAttribute("new_one", null);
    assertThat(table.hasAttribute("new_one"), is(false));
    table.setAttribute("new_one", "some_value");
    assertThat(table.hasAttribute("new_one"), is(true));
    table.removeAttribute("new_one");
    assertThat(table.hasAttribute("new_one"), is(false));
  }

  @Test
  public void viewProperties() throws Exception {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final View view = (View) catalog.lookupTable(schema, "AUTHORSLIST").get();

    checkBooleanProperties(view, "updatable");
  }

  private void compareColumnFields(final Column wrappedColumn, final Column column) {
    assertThat(wrappedColumn.getFullName(), is(column.getFullName()));
    assertThat(wrappedColumn.getColumnDataType(), is(column.getColumnDataType()));
    assertThat(wrappedColumn.getDecimalDigits(), is(column.getDecimalDigits()));
    assertThat(wrappedColumn.getOrdinalPosition(), is(column.getOrdinalPosition()));
    assertThat(wrappedColumn.getSize(), is(column.getSize()));
    assertThat(wrappedColumn.getWidth(), is(column.getWidth()));
    assertThat(wrappedColumn.isNullable(), is(column.isNullable()));
    assertThat(wrappedColumn.getDefaultValue(), is(column.getDefaultValue()));
    assertThat(wrappedColumn.getPrivileges(), is(column.getPrivileges()));
    assertThat(wrappedColumn.isAutoIncremented(), is(column.isAutoIncremented()));
    assertThat(wrappedColumn.isGenerated(), is(column.isGenerated()));
    assertThat(wrappedColumn.isHidden(), is(column.isHidden()));
    assertThat(wrappedColumn.isPartOfForeignKey(), is(column.isPartOfForeignKey()));
    assertThat(wrappedColumn.isPartOfIndex(), is(column.isPartOfIndex()));
    assertThat(wrappedColumn.isPartOfPrimaryKey(), is(column.isPartOfPrimaryKey()));
    assertThat(wrappedColumn.isPartOfUniqueIndex(), is(column.isPartOfUniqueIndex()));
    assertThat(wrappedColumn.getType(), is(column.getType()));
  }
}
