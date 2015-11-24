/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.core.uri.parser.search;

import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.junit.Assert;
import org.junit.Test;

public class SearchParserAndTokenizerTest {

  private static final String EOF = "<EOF>";

  @Test
  public void basicParsing() throws Exception {
    assertQuery("\"99\"").resultsIn("'99'");
    assertQuery("a").resultsIn("'a'");
    assertQuery("a AND b").resultsIn("{'a' AND 'b'}");
    assertQuery("a AND b AND c").resultsIn("{{'a' AND 'b'} AND 'c'}");
    assertQuery("a OR b").resultsIn("{'a' OR 'b'}");
    assertQuery("a OR b OR c").resultsIn("{{'a' OR 'b'} OR 'c'}");
    
    assertQuery("NOT a NOT b").resultsIn("{{NOT 'a'} AND {NOT 'b'}}");
    assertQuery("NOT a AND NOT b").resultsIn("{{NOT 'a'} AND {NOT 'b'}}");
    assertQuery("NOT a OR NOT b").resultsIn("{{NOT 'a'} OR {NOT 'b'}}");
    assertQuery("NOT a OR NOT b NOT C").resultsIn("{{NOT 'a'} OR {{NOT 'b'} AND {NOT 'C'}}}");
  }

  @Test
  public void mixedParsing() throws Exception {
    assertQuery("a AND b OR c").resultsIn("{{'a' AND 'b'} OR 'c'}");
    assertQuery("a OR b AND c").resultsIn("{'a' OR {'b' AND 'c'}}");
  }

  @Test
  public void notParsing() throws Exception {
    assertQuery("NOT a AND b OR c").resultsIn("{{{NOT 'a'} AND 'b'} OR 'c'}");
    assertQuery("a OR b AND NOT c").resultsIn("{'a' OR {'b' AND {NOT 'c'}}}");
  }

  @Test
  public void parenthesesParsing() throws Exception {
    assertQuery("a AND (b OR c)").resultsIn("{'a' AND {'b' OR 'c'}}");
    assertQuery("(a OR b) AND NOT c").resultsIn("{{'a' OR 'b'} AND {NOT 'c'}}");
    assertQuery("(a OR B) AND (c OR d AND NOT e OR (f))")
            .resultsIn("{{'a' OR 'B'} AND {{'c' OR {'d' AND {NOT 'e'}}} OR 'f'}}");
    assertQuery("(a OR B) (c OR d NOT e OR (f))")
      .resultsIn("{{'a' OR 'B'} AND {{'c' OR {'d' AND {NOT 'e'}}} OR 'f'}}");
    assertQuery("((((a))))").resultsIn("'a'");
    assertQuery("((((a)))) ((((a))))").resultsIn("{'a' AND 'a'}");
    assertQuery("((((a)))) OR ((((a))))").resultsIn("{'a' OR 'a'}");
    assertQuery("((((((a)))) ((((c))) OR (((C)))) ((((a))))))").resultsIn("{{'a' AND {'c' OR 'C'}} AND 'a'}");
    assertQuery("((((\"a\")))) OR ((((\"a\"))))").resultsIn("{'a' OR 'a'}");
  }
  
  @Test
  public void parseImplicitAnd() throws Exception {
    assertQuery("a b").resultsIn("{'a' AND 'b'}");
    assertQuery("a b c").resultsIn("{{'a' AND 'b'} AND 'c'}");
    assertQuery("a and b").resultsIn("{{'a' AND 'and'} AND 'b'}");
    assertQuery("hey ANDy warhol").resultsIn("{{'hey' AND 'ANDy'} AND 'warhol'}");
    assertQuery("a b OR c").resultsIn("{{'a' AND 'b'} OR 'c'}");
    assertQuery("a \"bc123\" OR c").resultsIn("{{'a' AND 'bc123'} OR 'c'}");
    assertQuery("(a OR x) bc c").resultsIn("{{{'a' OR 'x'} AND 'bc'} AND 'c'}");
    assertQuery("one ((a OR x) bc c)").resultsIn("{'one' AND {{{'a' OR 'x'} AND 'bc'} AND 'c'}}");
  }

  @Test
  public void invalidSearchQuery() throws Exception {
    assertQuery("99").resultsIn(SearchParserException.MessageKeys.TOKENIZER_EXCEPTION);
    assertQuery("NOT").resultsIn(SearchParserException.MessageKeys.INVALID_NOT_OPERAND);
    assertQuery("AND").resultsInExpectedTerm(SearchQueryToken.Token.AND.name());
    assertQuery("OR").resultsInExpectedTerm(SearchQueryToken.Token.OR.name());

    assertQuery("NOT a AND").resultsInExpectedTerm(EOF);
    assertQuery("NOT a OR").resultsInExpectedTerm(EOF);
    assertQuery("a AND").resultsInExpectedTerm(EOF);
    assertQuery("a OR").resultsInExpectedTerm(EOF);

    assertQuery("a OR b)").resultsIn(SearchParserException.MessageKeys.INVALID_END_OF_QUERY);
    assertQuery("a NOT b)").resultsIn(SearchParserException.MessageKeys.INVALID_END_OF_QUERY);
    assertQuery("a AND b)").resultsIn(SearchParserException.MessageKeys.INVALID_END_OF_QUERY);

    assertQuery("(a OR b").resultsIn(SearchParserException.MessageKeys.MISSING_CLOSE);
    assertQuery("(a NOT b").resultsIn(SearchParserException.MessageKeys.MISSING_CLOSE);
    assertQuery("((a AND b)").resultsIn(SearchParserException.MessageKeys.MISSING_CLOSE);
    assertQuery("((a AND b OR c)").resultsIn(SearchParserException.MessageKeys.MISSING_CLOSE);
    assertQuery("a AND (b OR c").resultsIn(SearchParserException.MessageKeys.MISSING_CLOSE);
    assertQuery("(a AND ((b OR c)").resultsIn(SearchParserException.MessageKeys.MISSING_CLOSE);
    
    assertQuery("NOT NOT a").resultsIn(SearchParserException.MessageKeys.INVALID_NOT_OPERAND);
    assertQuery("NOT (a)").resultsIn(SearchParserException.MessageKeys.TOKENIZER_EXCEPTION);
  }

  private static Validator assertQuery(String searchQuery) {
    return Validator.init(searchQuery);
  }

  private static class Validator {
    private boolean log;
    private final String searchQuery;

    private Validator(String searchQuery) {
      this.searchQuery = searchQuery;
    }

    private static Validator init(String searchQuery) {
      return new Validator(searchQuery);
    }

    @SuppressWarnings("unused")
    private Validator withLogging() {
      log = true;
      return this;
    }

    private void resultsIn(SearchParserException.MessageKey key)
            throws SearchTokenizerException {
      try {
        resultsIn(searchQuery);
      } catch (SearchParserException e) {
        Assert.assertEquals("SearchParserException with unexpected message '" + e.getMessage() +
            "' was thrown.", key, e.getMessageKey());
        if(log) {
          System.out.println("Caught SearchParserException with message key " +
              e.getMessageKey() + " and message " + e.getMessage());
        }
        return;
      }
      Assert.fail("SearchParserException with message key " + key.getKey() + " was not thrown.");
    }
    
    public void resultsInExpectedTerm(final String actualToken) throws SearchTokenizerException {
      try {
        resultsIn(searchQuery);
      } catch(SearchParserException e) {
        Assert.assertEquals(SearchParserException.MessageKeys.EXPECTED_DIFFERENT_TOKEN, e.getMessageKey());
        Assert.assertEquals("Expected PHRASE||WORD found: " + actualToken, e.getMessage());
      }
    }
    
    private void resultsIn(String expectedSearchExpression) throws SearchTokenizerException, SearchParserException {
      final SearchExpression searchExpression = getSearchExpression();
      Assert.assertEquals(expectedSearchExpression, searchExpression.toString());
    }

    private SearchExpression getSearchExpression() throws SearchParserException, SearchTokenizerException {
      SearchParser tokenizer = new SearchParser();
      SearchOption result = tokenizer.parse(searchQuery);
      Assert.assertNotNull(result);
      final SearchExpression searchExpression = result.getSearchExpression();
      Assert.assertNotNull(searchExpression);
      if (log) {
        System.out.println(searchExpression);
      }
      return searchExpression;
    }
  }
}