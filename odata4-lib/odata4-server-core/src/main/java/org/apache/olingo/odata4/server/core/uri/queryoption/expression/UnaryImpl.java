/*******************************************************************************
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
 ******************************************************************************/
package org.apache.olingo.odata4.server.core.uri.queryoption.expression;

import org.apache.olingo.odata4.commons.api.ODataApplicationException;
import org.apache.olingo.odata4.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.odata4.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.odata4.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.odata4.server.api.uri.queryoption.expression.UnaryOperator;
import org.apache.olingo.odata4.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.apache.olingo.odata4.server.api.uri.queryoption.expression.VisitableExression;

public class UnaryImpl extends ExpressionImpl implements UnaryOperator, VisitableExression {

  private UnaryOperatorKind operator;
  private ExpressionImpl expression;

  @Override
  public UnaryOperatorKind getOperator() {
    return operator;
  }

  public void setOperator(final UnaryOperatorKind operator) {
    this.operator = operator;
  }

  @Override
  public Expression getOperand() {
    return expression;
  }

  public void setOperand(final ExpressionImpl expression) {
    this.expression = expression;
  }

  @Override
  public <T> T accept(final ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {
    T operand = expression.accept(visitor);
    return visitor.visitUnaryOperator(operator, operand);
  }

}
