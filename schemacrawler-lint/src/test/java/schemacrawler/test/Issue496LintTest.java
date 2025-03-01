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

package schemacrawler.test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static schemacrawler.tools.lint.config.LinterConfigUtility.readLinterConfigs;
import static schemacrawler.tools.utility.SchemaCrawlerUtility.getCatalog;

import java.sql.Connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.tools.command.lint.options.LintOptions;
import schemacrawler.tools.command.lint.options.LintOptionsBuilder;
import schemacrawler.tools.lint.Lint;
import schemacrawler.tools.lint.LintCollector;
import schemacrawler.tools.lint.Linters;
import schemacrawler.tools.lint.config.LinterConfigs;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
public class Issue496LintTest {

  @Test
  public void issue496(final Connection connection) throws Exception {

    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder()
            .includeTables(table -> table.equals("PUBLIC.FOR_LINT.WRITERS"));
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions());

    final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);
    assertThat(catalog, notNullValue());
    assertThat(catalog.getSchemas().size(), is(6));
    final Schema schema = catalog.lookupSchema("PUBLIC.FOR_LINT").orElse(null);
    assertThat("FOR_LINT schema not found", schema, notNullValue());
    assertThat("FOR_LINT tables not found", catalog.getTables(schema), hasSize(1));

    final LintOptions lintOptions =
        LintOptionsBuilder.builder().withLinterConfigs("/issue496-linter-configs.yaml").toOptions();

    final LinterConfigs linterConfigs = readLinterConfigs(lintOptions);

    final Linters linters = new Linters(linterConfigs, false);

    linters.lint(catalog, connection);
    final LintCollector lintCollector = linters.getCollector();

    assertThat(lintCollector.size(), is(0));
  }

  @Test
  public void issue496_withoutInclude(final Connection connection) throws Exception {

    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();

    final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);
    assertThat(catalog, notNullValue());
    assertThat(catalog.getSchemas().size(), is(6));
    final Schema schema = catalog.lookupSchema("PUBLIC.FOR_LINT").orElse(null);
    assertThat("FOR_LINT schema not found", schema, notNullValue());
    assertThat("FOR_LINT tables not found", catalog.getTables(schema), hasSize(6));

    final LintOptions lintOptions =
        LintOptionsBuilder.builder().withLinterConfigs("/issue496-linter-configs.yaml").toOptions();

    final LinterConfigs linterConfigs = readLinterConfigs(lintOptions);

    final Linters linters = new Linters(linterConfigs, false);

    linters.lint(catalog, connection);
    final LintCollector lintCollector = linters.getCollector();

    assertThat(lintCollector.size(), is(1));
    assertThat(
        lintCollector.getLints().stream().map(Lint::toString).collect(toList()),
        containsInAnyOrder(
            "[catalog] cycles in table relationships: PUBLIC.FOR_LINT.PUBLICATIONS, PUBLIC.FOR_LINT.WRITERS"));
  }
}
