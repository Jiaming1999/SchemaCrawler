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


import java.util.regex.Pattern;

/**
 * Specifies exclusion patterns that can be applied to the names, definitions,
 * and other attributes of named objects.
 *
 * @author Sualeh Fatehi
 */
public final class RegularExpressionExclusionRule
  implements InclusionRuleWithRegularExpression
{

  private static final long serialVersionUID = 6274652266761961575L;

  private final InclusionRule inclusionRule;

  /**
   * Set exclude pattern. Include nothing.
   *
   * @param patternExclude
   *   Exclusion pattern. If null, excludes nothing.
   */
  public RegularExpressionExclusionRule(final Pattern patternExclude)
  {
    if (patternExclude == null)
    {
      inclusionRule = new IncludeAll();
    }
    else
    {
      inclusionRule = new RegularExpressionRule(null, patternExclude);
    }
  }

  /**
   * Set exclude pattern. Include nothing.
   *
   * @param patternExclude
   *   Exclusion pattern. If null, excludes nothing.
   */
  public RegularExpressionExclusionRule(final String patternExclude)
  {
    this(patternExclude == null? null: Pattern.compile(patternExclude));
  }

  @Override
  public Pattern getExclusionPattern()
  {
    if (inclusionRule instanceof InclusionRuleWithRegularExpression)
    {
      return ((InclusionRuleWithRegularExpression) inclusionRule).getExclusionPattern();
    }
    else
    {
      return InclusionRuleWithRegularExpression.super.getExclusionPattern();
    }
  }

  @Override
  public Pattern getInclusionPattern()
  {
    if (inclusionRule instanceof InclusionRuleWithRegularExpression)
    {
      return ((InclusionRuleWithRegularExpression) inclusionRule).getInclusionPattern();
    }
    else
    {
      return InclusionRuleWithRegularExpression.super.getInclusionPattern();
    }
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result =
      prime * result + (inclusionRule == null? 0: inclusionRule.hashCode());
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
    final RegularExpressionExclusionRule other =
      (RegularExpressionExclusionRule) obj;
    if (inclusionRule == null)
    {
      return other.inclusionRule == null;
    }
    else
    { return inclusionRule.equals(other.inclusionRule); }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    return inclusionRule.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean test(final String text)
  {
    return inclusionRule.test(text);
  }

}
