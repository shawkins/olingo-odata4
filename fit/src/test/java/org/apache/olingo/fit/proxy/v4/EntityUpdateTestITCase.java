/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.fit.proxy.v4;

import static org.apache.olingo.fit.proxy.v4.AbstractTestITCase.container;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.UUID;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.client.api.v4.EdmEnabledODataClient;
import org.apache.olingo.ext.proxy.EntityContainerFactory;
import org.apache.olingo.ext.proxy.commons.EntityInvocationHandler;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.InMemoryEntities;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Address;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Order;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.OrderCollection;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Customer;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.OrderDetail;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.OrderDetailKey;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.
        PaymentInstrument;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Person;
import org.junit.Test;

/**
 * This is the unit test class to check entity update operations.
 */
public class EntityUpdateTestITCase extends AbstractTestITCase {

  protected EntityContainerFactory<EdmEnabledODataClient> getContainerFactory() {
    return containerFactory;
  }

  protected InMemoryEntities getContainer() {
    return container;
  }

  @Test
  public void update() {
    Person person = getContainer().getPeople().get(1);

    final Address address = person.getHomeAddress();
    address.setCity("XXX");

    getContainer().flush();

    person = getContainer().getPeople().get(1);
    assertEquals("XXX", person.getHomeAddress().getCity());
  }

  @Test
  public void multiKey() {
    final OrderDetailKey orderDetailKey = new OrderDetailKey();
    orderDetailKey.setOrderID(7);
    orderDetailKey.setProductID(5);

    OrderDetail orderDetail = getContainer().getOrderDetails().get(orderDetailKey);
    assertNotNull(orderDetail);
    assertEquals(7, orderDetail.getOrderID(), 0);
    assertEquals(5, orderDetail.getProductID(), 0);

    orderDetail.setQuantity(5);

    getContainer().flush();

    orderDetail = getContainer().getOrderDetails().get(orderDetailKey);
    orderDetail.setQuantity(5);
  }

  @Test
  public void patchLink() {
    // 1. create customer
    Customer customer = getContainer().getCustomers().newCustomer();
    customer.setPersonID(977);
    customer.setFirstName("Test");
    customer.setLastName("Test");

    final Address homeAddress = getContainer().complexFactory().newCompanyAddress();
    homeAddress.setStreet("V.le Gabriele D'Annunzio");
    homeAddress.setCity("Pescara");
    homeAddress.setPostalCode("65127");
    customer.setHomeAddress(homeAddress);

    customer.setNumbers(Collections.<String>emptyList());
    customer.setEmails(Collections.<String>emptyList());
    customer.setCity("Pescara");

    final Calendar birthday = Calendar.getInstance();
    birthday.clear();
    birthday.set(1977, 8, 8);
    customer.setBirthday(birthday);

    customer.setTimeBetweenLastTwoOrders(BigDecimal.valueOf(0.0000002));    
    
    // 2. create order and set it to customer
    final int orderId = RandomUtils.nextInt(400, 410);
    
    Order order = getContainer().getOrders().newOrder();
    order.setOrderID(orderId);

    final OrderCollection orders = getContainer().getOrders().newOrderCollection();
    orders.add(order);

    customer.setOrders(orders);
    order.setCustomerForOrder(customer);

    getContainer().flush();

    // 3. check everything after flush
    order = getContainer().getOrders().get(orderId);
    assertEquals(orderId, order.getOrderID(), 0);

    customer = getContainer().getCustomers().get(977);

    //assertEquals(1, customer.getOrders().size());

    int count = 0;
    for (Order inside : customer.getOrders()) {
      if (inside.getOrderID() == orderId) {
        count++;
      }
    }
    assertEquals(1, count);
    assertEquals(977, order.getCustomerForOrder().getPersonID(), 0);
    
    // 4. delete customer and order
    getContainer().getCustomers().delete(977);
    getContainer().getOrders().delete(orderId);
    
    getContainer().flush();
  }

  @Test
  public void concurrentModification() {
    Order order = getContainer().getOrders().get(8);
    final String etag = ((EntityInvocationHandler) Proxy.getInvocationHandler(order)).getETag();
    assertTrue(StringUtils.isNotBlank(etag));

    order.setShelfLife(BigDecimal.TEN);

    getContainer().flush();

    order = getContainer().getOrders().get(8);
    assertEquals(BigDecimal.TEN, order.getShelfLife());
  }

  @Test
  public void contained() {
    PaymentInstrument instrument = getContainer().getAccounts().get(101).getMyPaymentInstruments().get(101901);

    final String newName = UUID.randomUUID().toString();
    instrument.setFriendlyName(newName);

    getContainer().flush();

    instrument = getContainer().getAccounts().get(101).getMyPaymentInstruments().get(101901);
    assertEquals(newName, instrument.getFriendlyName());
  }
}
