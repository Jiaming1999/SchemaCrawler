/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
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

package schemacrawler.schemacrawler;


import static java.util.Objects.requireNonNull;
import static schemacrawler.schemacrawler.SchemaInfoRetrieval.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Descriptor for level of schema detail to be retrieved when crawling a schema.
 */
public final class SchemaInfoLevel
  implements Options
{

  private final boolean[] schemaInfoRetrievals;
  private final String tag;

  SchemaInfoLevel(final String tag, final Map<SchemaInfoRetrieval, Boolean> schemaInfoRetrievalsMap)
  {
    requireNonNull(tag, "No tag provided");
    this.tag = tag;

    requireNonNull(schemaInfoRetrievalsMap, "No schema info retrievals provided");
    final SchemaInfoRetrieval[] schemaInfoRetrievalsArray = values();
    schemaInfoRetrievals = new boolean[schemaInfoRetrievalsArray.length];
    for (final SchemaInfoRetrieval schemaInfoRetrieval : schemaInfoRetrievalsArray)
    {
      final boolean schemaInfoRetrievalValue = schemaInfoRetrievalsMap.getOrDefault(schemaInfoRetrieval, false);
      schemaInfoRetrievals[schemaInfoRetrieval.ordinal()] = schemaInfoRetrievalValue;
    }
  }

  public String getTag()
  {
    return tag;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(schemaInfoRetrievals);
    result = prime * result + Objects.hash(tag);
    return result;
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (!(obj instanceof SchemaInfoLevel))
    {
      return false;
    }
    final SchemaInfoLevel other = (SchemaInfoLevel) obj;
    return Arrays.equals(schemaInfoRetrievals, other.schemaInfoRetrievals) && Objects.equals(tag, other.tag);
  }

  @Override
  public String toString()
  {
    final StringJoiner settings = new StringJoiner(System.lineSeparator());
    for (final SchemaInfoRetrieval schemaInfoRetrieval : values())
    {
      settings.add(String.format("  %s=%b", schemaInfoRetrieval.name(), is(schemaInfoRetrieval)));
    }
    return String.format("SchemaInfoLevel <%s>%n{%n%s%n}%n", tag, settings);
  }

  public boolean is(final SchemaInfoRetrieval schemaInfoRetrieval)
  {
    if (schemaInfoRetrieval == null)
    {
      return false;
    }
    return schemaInfoRetrievals[schemaInfoRetrieval.ordinal()];
  }

  public boolean isRetrieveAdditionalColumnAttributes()
  {
    return is(retrieveAdditionalColumnAttributes);
  }

  public boolean isRetrieveAdditionalColumnMetadata()
  {
    return is(retrieveAdditionalColumnMetadata);
  }

  public boolean isRetrieveAdditionalDatabaseInfo()
  {
    return is(retrieveAdditionalDatabaseInfo);
  }

  public boolean isRetrieveAdditionalJdbcDriverInfo()
  {
    return is(retrieveAdditionalJdbcDriverInfo);
  }

  public boolean isRetrieveAdditionalTableAttributes()
  {
    return is(retrieveAdditionalTableAttributes);
  }

  public boolean isRetrieveColumnDataTypes()
  {
    return is(retrieveColumnDataTypes);
  }

  public boolean isRetrieveDatabaseInfo()
  {
    return is(retrieveDatabaseInfo);
  }

  public boolean isRetrieveForeignKeyDefinitions()
  {
    return is(retrieveForeignKeyDefinitions);
  }

  public boolean isRetrieveForeignKeys()
  {
    return is(retrieveForeignKeys);
  }

  public boolean isRetrieveIndexColumnInformation()
  {
    return is(retrieveIndexColumnInformation);
  }

  public boolean isRetrieveIndexes()
  {
    return is(retrieveIndexes);
  }

  public boolean isRetrieveIndexInformation()
  {
    return is(retrieveIndexInformation);
  }

  public boolean isRetrievePrimaryKeyDefinitions()
  {
    return is(retrievePrimaryKeyDefinitions);
  }

  public boolean isRetrieveRoutineParameters()
  {
    return is(retrieveRoutineParameters);
  }

  public boolean isRetrieveRoutineInformation()
  {
    return is(retrieveRoutineInformation);
  }

  public boolean isRetrieveRoutines()
  {
    return is(retrieveRoutines);
  }

  public boolean isRetrieveSequenceInformation()
  {
    return is(retrieveSequenceInformation);
  }

  public boolean isRetrieveServerInfo()
  {
    return is(retrieveServerInfo);
  }

  public boolean isRetrieveSynonymInformation()
  {
    return is(retrieveSynonymInformation);
  }

  public boolean isRetrieveTableColumnPrivileges()
  {
    return is(retrieveTableColumnPrivileges);
  }

  public boolean isRetrieveTableColumns()
  {
    return is(retrieveTableColumns);
  }

  public boolean isRetrieveTableConstraintDefinitions()
  {
    return is(retrieveTableConstraintDefinitions);
  }

  public boolean isRetrieveTableConstraintInformation()
  {
    return is(retrieveTableConstraintInformation);
  }

  public boolean isRetrieveTableDefinitionsInformation()
  {
    return is(retrieveTableDefinitionsInformation);
  }

  public boolean isRetrieveTablePrivileges()
  {
    return is(retrieveTablePrivileges);
  }

  public boolean isRetrieveTables()
  {
    return is(retrieveTables);
  }

  public boolean isRetrieveTriggerInformation()
  {
    return is(retrieveTriggerInformation);
  }

  public boolean isRetrieveUserDefinedColumnDataTypes()
  {
    return is(retrieveUserDefinedColumnDataTypes);
  }

  public boolean isRetrieveViewInformation()
  {
    return is(retrieveViewInformation);
  }

  public boolean isRetrieveWeakAssociations()
  {
    return is(retrieveWeakAssociations);
  }

}
