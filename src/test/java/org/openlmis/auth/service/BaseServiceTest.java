package org.openlmis.auth.service;

import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.openlmis.auth.domain.BaseEntity;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseServiceTest {

  static class SaveAnswer<T extends BaseEntity> implements Answer<T> {

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
      T obj = (T) invocation.getArguments()[0];

      if (null == obj) {
        return null;
      }

      if (null == obj.getId()) {
        obj.setId(UUID.randomUUID());
      }

      extraSteps(obj);

      return obj;
    }

    void extraSteps(T obj) {
      // should be overriden if extra steps are required.
    }

  }
}
