/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2019, Sualeh Fatehi <sualeh@hotmail.com>.
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


import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static schemacrawler.test.utility.CommandlineTestUtility.commandlineExecution;
import static schemacrawler.test.utility.ExecutableTestUtility.hasSameContentAndTypeAs;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.outputOf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import schemacrawler.schemacrawler.InfoLevel;
import schemacrawler.test.utility.*;
import schemacrawler.tools.options.OutputFormat;
import schemacrawler.tools.text.schema.SchemaTextDetailType;

@ExtendWith(TestAssertNoSystemErrOutput.class)
@ExtendWith(TestAssertNoSystemOutOutput.class)
@ExtendWith(TestLoggingExtension.class)
@ExtendWith(TestDatabaseConnectionParameterResolver.class)
public abstract class AbstractSpinThroughCommandLineTest
{

  private static final String SPIN_THROUGH_OUTPUT = "spin_through_output/";

  @BeforeAll
  public static void clean()
    throws Exception
  {
    TestUtility.clean(SPIN_THROUGH_OUTPUT);
  }

  @Test
  public void spinThroughMain(final DatabaseConnectionInfo connectionInfo)
    throws Exception
  {
    assertAll(infoLevels().flatMap(infoLevel -> outputFormats()
      .flatMap(outputFormat -> schemaTextDetailTypes()
        .map(schemaTextDetailType -> () -> {

          final String referenceFile = referenceFile(schemaTextDetailType,
                                                     infoLevel,
                                                     outputFormat);

          final String command = schemaTextDetailType.name();

          final Map<String, String> argsMap = new HashMap<>();
          argsMap.put("-sequences", ".*");
          argsMap.put("-synonyms", ".*");
          argsMap.put("-routines", ".*");
          argsMap.put("-no-info", Boolean.FALSE.toString());
          argsMap.put("-info-level", infoLevel.name());

          assertThat(outputOf(commandlineExecution(connectionInfo,
                                                   command,
                                                   argsMap,
                                                   "/hsqldb.INFORMATION_SCHEMA.config.properties",
                                                   outputFormat)),
                     hasSameContentAndTypeAs(classpathResource(
                       SPIN_THROUGH_OUTPUT + referenceFile), outputFormat));

        }))));
  }

  protected abstract Stream<OutputFormat> outputFormats();

  private Stream<InfoLevel> infoLevels()
  {
    return Arrays.stream(InfoLevel.values())
      .filter(infoLevel -> infoLevel != InfoLevel.unknown);
  }

  private String referenceFile(final SchemaTextDetailType schemaTextDetailType,
                               final InfoLevel infoLevel,
                               final OutputFormat outputFormat)
  {
    final String referenceFile = String.format("%d%d.%s_%s.%s",
                                               schemaTextDetailType.ordinal(),
                                               infoLevel.ordinal(),
                                               schemaTextDetailType,
                                               infoLevel,
                                               outputFormat.getFormat());
    return referenceFile;
  }

  private Stream<SchemaTextDetailType> schemaTextDetailTypes()
  {
    return Arrays.stream(SchemaTextDetailType.values());
  }

}
