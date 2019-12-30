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

package schemacrawler.crawl;


import static java.util.Comparator.naturalOrder;
import static sf.util.Utility.isBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import schemacrawler.schema.DatabaseObject;
import schemacrawler.schema.Grant;
import schemacrawler.schema.Privilege;

/**
 * Represents a privilege of a table or column.
 *
 * @author Sualeh Fatehi
 */
final class MutablePrivilege<D extends DatabaseObject>
  extends AbstractDependantObject<D>
  implements Privilege<D>
{

  private static final long serialVersionUID = -1117664231494271886L;


  private final class PrivilegeGrant
    implements Grant<D>
  {

    private static final long serialVersionUID = 356151825191631484L;
    private final String grantee;
    private final String grantor;
    private final boolean isGrantable;

    PrivilegeGrant(final String grantor,
                   final String grantee,
                   final boolean isGrantable)
    {
      this.grantor = grantor;
      this.grantee = grantee;
      this.isGrantable = isGrantable;
    }

    @Override
    public int compareTo(final Grant<D> otherGrant)
    {
      int compare = 0;
      if (compare == 0)
      {
        compare = grantor.compareTo(otherGrant.getGrantor());
      }
      if (compare == 0)
      {
        compare = grantee.compareTo(otherGrant.getGrantee());
      }
      return compare;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGrantee()
    {
      return grantee;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGrantor()
    {
      return grantor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGrantable()
    {
      return isGrantable;
    }

    @Override
    public MutablePrivilege<D> getParent()
    {
      return MutablePrivilege.this;
    }

    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + getParent().hashCode();
      result = prime * result + (grantee == null? 0: grantee.hashCode());
      result = prime * result + (grantor == null? 0: grantor.hashCode());
      result = prime * result + (isGrantable? 1231: 1237);
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
      if (getClass() != obj.getClass())
      {
        return false;
      }
      final PrivilegeGrant other = (PrivilegeGrant) obj;
      if (!getParent().equals(other.getParent()))
      {
        return false;
      }
      if (grantee == null)
      {
        if (other.grantee != null)
        {
          return false;
        }
      }
      else if (!grantee.equals(other.grantee))
      {
        return false;
      }
      if (grantor == null)
      {
        if (other.grantor != null)
        {
          return false;
        }
      }
      else if (!grantor.equals(other.grantor))
      {
        return false;
      }
      return isGrantable == other.isGrantable;
    }

  }


  private final Set<Grant<D>> grants = new HashSet<>();

  MutablePrivilege(final DatabaseObjectReference<D> parent, final String name)
  {
    super(parent, name);
  }

  @Override
  public Collection<Grant<D>> getGrants()
  {
    final List<Grant<D>> values = new ArrayList<>(grants);
    values.sort(naturalOrder());
    return values;
  }

  void addGrant(final String grantor,
                final String grantee,
                final boolean isGrantable)
  {
    if (!isBlank(grantor) && !isBlank(grantee))
    {
      grants.add(new PrivilegeGrant(grantor, grantee, isGrantable));
    }
  }

}
